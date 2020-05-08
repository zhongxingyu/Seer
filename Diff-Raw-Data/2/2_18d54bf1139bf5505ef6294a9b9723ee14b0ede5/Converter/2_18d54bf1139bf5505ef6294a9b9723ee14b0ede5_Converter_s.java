 package co.geomati.netcdf;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import java.util.UUID;
 
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 
 import ucar.ma2.ArrayDouble;
 import ucar.ma2.ArrayInt;
 import ucar.ma2.DataType;
 import ucar.ma2.Index;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.Attribute;
 import ucar.nc2.Dimension;
 import ucar.nc2.NetcdfFileWriteable;
 import ucar.nc2.Variable;
 
 public class Converter {
 
 	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
 			"yyyy-MM-dd HH:mm:ss:S");
 
 	public static void convert(Dataset dataset, File file)
 			throws ConverterException {
 
 		Properties props = new Properties();
 		BufferedInputStream is;
 		try {
 			is = new BufferedInputStream(new FileInputStream(new File(
 					"conversion.properties")));
 		} catch (FileNotFoundException e) {
 			throw new ConverterException("Cannot find 'conversion.properties'",
 					e);
 		}
 
 		try {
 			props.load(is);
 		} catch (IOException e) {
 			throw new ConverterException("Error reading properties", e);
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 				// ignore
 			}
 		}
 
 		NetcdfFileWriteable nc;
 		try {
 			nc = NetcdfFileWriteable.createNew(file.getAbsolutePath(), false);
 		} catch (IOException e) {
 			throw new ConverterException("Cannot create the file", e);
 		}
 
 		/*
 		 * Global metadata
 		 */
		nc.addGlobalAttribute("uuid", UUID.randomUUID().toString());
 		nc.addGlobalAttribute("naming_authority", "es.icos.dataportal");
 		nc.addGlobalAttribute("standard_name_vocabulary",
 				props.getProperty("vocabulary_url"));
 		nc.addGlobalAttribute("icos_domain", dataset.getIcosDomain().toString());
 		nc.addGlobalAttribute("conventions", "CF-1.5");
 		nc.addGlobalAttribute("Metadata_Conventions",
 				"Unidata Dataset Discovery v1.0");
 		nc.addGlobalAttribute("institution", dataset.getInstitution());
 		nc.addGlobalAttribute("creator_url", dataset.getCreatorURL());
 
 		if (dataset instanceof StationDataset) {
 			/*
 			 * bbox and time range
 			 */
 			StationDataset stationDataset = (StationDataset) dataset;
 			addStation(nc, stationDataset);
 		} else if (dataset instanceof TrajectoryDataset) {
 			nc.addGlobalAttribute("cdm_data_type",
 					CDMDataType.TRAJECTORY.toString());
 			addTrajectory(nc, (TrajectoryDataset) dataset);
 		} else if (dataset instanceof GridDataset) {
 			nc.addGlobalAttribute("cdm_data_type",
 					CDMDataType.TRAJECTORY.toString());
 			addGrid(nc, (GridDataset) dataset);
 		} else {
 			throw new ConverterException("Unsupported Dataset implementation");
 		}
 
 		try {
 			nc.close();
 		} catch (IOException e) {
 			throw new ConverterException("Cannot close created nc file", e);
 		}
 	}
 
 	private static void addStation(NetcdfFileWriteable nc,
 			StationDataset dataset) throws ConverterException {
 		List<Point2D> stationPositions = dataset.getPositions();
 		List<Integer> times = dataset.getTimeStamps();
 		Date referenceDate = dataset.getReferenceDate();
 		TimeUnit timeUnit = dataset.getTimeUnits();
 
 		Rectangle2D bbox = getBBox(stationPositions);
 		long[] timeBox = getTimeBox(times, referenceDate, timeUnit);
 		nc.addGlobalAttribute("geospatial_lat_min", bbox.getMinY());
 		nc.addGlobalAttribute("geospatial_lat_max", bbox.getMaxY());
 		nc.addGlobalAttribute("geospatial_lon_min", bbox.getMinX());
 		nc.addGlobalAttribute("geospatial_lon_max", bbox.getMaxX());
 		DateTimeFormatter parser = ISODateTimeFormat.dateTime();
 		parser = parser.withZoneUTC();
 		nc.addGlobalAttribute("time_coverage_start", parser.print(timeBox[0]));
 		nc.addGlobalAttribute("time_coverage_end", parser.print(timeBox[1]));
 		nc.addGlobalAttribute("cdm_data_type", CDMDataType.STATION.toString());
 
 		ArrayList<Dimension> mainVarDimensions = new ArrayList<Dimension>();
 
 		// time dimension variable
 		Dimension timeDim = nc.addUnlimitedDimension("time");
 		Variable time = nc.addVariable("time", DataType.INT,
 				new Dimension[] { timeDim });
 		time.addAttribute(new Attribute("units", timeUnit.toString()
 				+ " since " + dateFormat.format(referenceDate)));
 		time.addAttribute(new Attribute("axis", "T"));
 		mainVarDimensions.add(timeDim);
 
 		// Position dimension
 		Dimension stationDimension = nc.addDimension("station",
 				stationPositions.size());
 		// Position variables
 		Variable lat = nc.addVariable("lat", DataType.DOUBLE,
 				new Dimension[] { stationDimension });
 		lat.addAttribute(new Attribute("axis", "Y"));
 		lat.addAttribute(new Attribute("standard_name", "latitude"));
 		lat.addAttribute(new Attribute("units", "degrees_north"));
 		Variable lon = nc.addVariable("lon", DataType.DOUBLE,
 				new Dimension[] { stationDimension });
 		lon.addAttribute(new Attribute("axis", "X"));
 		lon.addAttribute(new Attribute("standard_name", "longitude"));
 		lon.addAttribute(new Attribute("units", "degrees_east"));
 
 		/*
 		 * TODO vertical coordinate
 		 */
 
 		/*
 		 * Add variable
 		 */
 		mainVarDimensions.add(stationDimension);
 		String variableName = dataset.getVariableName();
 		Variable mainVar = nc.addVariable(variableName,
 				dataset.getVariableType(),
 				mainVarDimensions.toArray(new Dimension[0]));
 		mainVar.addAttribute(new Attribute("coordinates", "lat lon"));
 		mainVar.addAttribute(new Attribute("long_name", dataset
 				.getVariableLongName()));
 		mainVar.addAttribute(new Attribute("standard_name", dataset
 				.getVariableStandardName()));
 		mainVar.addAttribute(new Attribute("units", dataset.getVariableUnits()));
 
 		try {
 			nc.create();
 
 			/*
 			 * Write time
 			 */
 			try {
 				nc.write(time.getName(),
 						get1Int(lat, times, new IntSampleGetter<Integer>() {
 
 							@Override
 							public int get(Integer t) {
 								return t;
 							}
 						}));
 			} catch (InvalidRangeException e) {
 				throw new RuntimeException("Bug. This should not "
 						+ "happen since time is unlimited", e);
 			}
 
 			/*
 			 * write positions
 			 */
 			try {
 				nc.write(
 						lat.getName(),
 						get1Double(lat, stationPositions,
 								new DoubleSampleGetter<Point2D>() {
 
 									@Override
 									public double get(Point2D t) {
 										return t.getY();
 									}
 								}));
 				nc.write(
 						lon.getName(),
 						get1Double(lat, stationPositions,
 								new DoubleSampleGetter<Point2D>() {
 
 									@Override
 									public double get(Point2D t) {
 										return t.getX();
 									}
 								}));
 			} catch (InvalidRangeException e) {
 				throw new ConverterException("The specified positions exceed "
 						+ "the number of stations", e);
 			}
 
 			/*
 			 * write main variable
 			 */
 			ArrayDouble A = new ArrayDouble.D2(timeDim.getLength(),
 					stationDimension.getLength());
 			Index ima = A.getIndex();
 			for (int i = 0; i < timeDim.getLength(); i++) {
 				for (int j = 0; j < stationDimension.getLength(); j++) {
 					A.setDouble(ima.set(i, j), dataset.getStationData(j, i));
 				}
 			}
 			try {
 				nc.write(mainVar.getName(), A);
 			} catch (InvalidRangeException e) {
 				throw new ConverterException("Too many data on main variable",
 						e);
 			}
 
 		} catch (IOException e) {
 			throw new ConverterException("Cannot create netcdf file", e);
 		}
 	}
 
 	private static long[] getTimeBox(List<Integer> times, Date referenceDate,
 			TimeUnit unit) {
 		long minTime = Long.MAX_VALUE;
 		long maxTime = Long.MIN_VALUE;
 		for (Integer magnitude : times) {
 			long time = referenceDate.getTime()
 					+ getMilliseconds(magnitude, unit);
 			if (time < minTime) {
 				minTime = time;
 			}
 			if (time > maxTime) {
 				maxTime = time;
 			}
 		}
 
 		return new long[] { maxTime, minTime };
 	}
 
 	private static long getMilliseconds(Integer magnitude, TimeUnit unit) {
 		if (unit == TimeUnit.SECOND) {
 			return magnitude * 1000;
 		} else if (unit == TimeUnit.MINUTE) {
 			return magnitude * 1000 * 60;
 		} else if (unit == TimeUnit.HOUR) {
 			return magnitude * 1000 * 60 * 60;
 		} else if (unit == TimeUnit.DAYS) {
 			return magnitude * 1000 * 60 * 60 * 24;
 		} else {
 			throw new RuntimeException("Bug");
 		}
 	}
 
 	private static Rectangle2D getBBox(List<Point2D> points) {
 		double minX = Double.MAX_VALUE;
 		double maxX = Double.MIN_VALUE;
 		double minY = Double.MAX_VALUE;
 		double maxY = Double.MIN_VALUE;
 		for (Point2D p : points) {
 			double x = p.getX();
 			double y = p.getY();
 			if (x < minX) {
 				minX = x;
 			}
 			if (x > maxX) {
 				maxX = x;
 			}
 			if (y < minY) {
 				minY = y;
 			}
 			if (y > maxY) {
 				maxY = y;
 			}
 		}
 
 		double width = maxX - minX;
 		double height = maxY - minY;
 		return new Rectangle2D.Double(minX, minY, width, height);
 	}
 
 	private static void addTrajectory(NetcdfFileWriteable nc, Dataset dataset) {
 		// TODO Auto-generated method stub
 	}
 
 	private static void addGrid(NetcdfFileWriteable nc, Dataset dataset) {
 		throw new UnsupportedOperationException("Not implemented yet");
 	}
 
 	public static <T> ArrayDouble get1Double(Variable var, List<T> list,
 			DoubleSampleGetter<T> getter) throws IOException,
 			InvalidRangeException {
 		Dimension dimension = var.getDimensions().get(0);
 		ArrayDouble A = new ArrayDouble.D1(dimension.getLength());
 		Index ima = A.getIndex();
 		for (int i = 0; i < dimension.getLength(); i++) {
 			A.setDouble(ima.set(i), getter.get(list.get(i)));
 		}
 		return A;
 	}
 
 	public static <T> ArrayInt get1Int(Variable var, List<T> list,
 			IntSampleGetter<T> getter) throws IOException,
 			InvalidRangeException {
 		ArrayInt A = new ArrayInt.D1(list.size());
 		Index ima = A.getIndex();
 		for (int i = 0; i < list.size(); i++) {
 			A.setInt(ima.set(i), getter.get(list.get(i)));
 		}
 		return A;
 	}
 }
