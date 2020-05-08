 package co.geomati.netcdf;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 import java.util.UUID;
 
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 
 import ucar.ma2.Array;
 import ucar.ma2.ArrayDouble;
 import ucar.ma2.ArrayInt;
 import ucar.ma2.DataType;
 import ucar.ma2.Index;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.Attribute;
 import ucar.nc2.Dimension;
 import ucar.nc2.NetcdfFileWriteable;
 import ucar.nc2.Variable;
 import co.geomati.netcdf.dataset.Dataset;
 import co.geomati.netcdf.dataset.DatasetDoubleVariable;
 import co.geomati.netcdf.dataset.DatasetIntVariable;
 import co.geomati.netcdf.dataset.GeoreferencedStation;
 import co.geomati.netcdf.dataset.Station;
 import co.geomati.netcdf.dataset.TimeSerie;
 import co.geomati.netcdf.dataset.Trajectory;
 
 public class Converter {
 
 	private static final String VOCABULARY_URL = "http://ciclope.cmima.csic.es:8080/dataportal/xml/vocabulario.xml";
 	private static final ConverterException WRONG_VARIABLE_IMPLEMENTATION = new ConverterException(
 			"Variables must " + "implement "
 					+ DatasetIntVariable.class.getName() + " or "
 					+ DatasetDoubleVariable.class.getName());
 	private static final String LON_VARIABLE_NAME = "lon";
 	private static final String LAT_VARIABLE_NAME = "lat";
 	private static final String TIME_VARIABLE_AND_DIMENSION_NAME = "time";
 	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
 			"yyyy-MM-dd HH:mm:ss:S");
 
 	static {
 		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT0"));
 	}
 
 	public static void convert(DatasetConversion conversion) {
 		Report r = new Report();
 		for (int i = 0; i < conversion.getDatasetCount(); i++) {
 			try {
 				r.addRecord();
 				Dataset dataset = conversion.getDataset(i);
 				r.setDatasetName(dataset.getMainVariable().getStandardName());
 				File tempFile = new File(System.getProperty("java.io.tmpdir")
 						+ "/" + conversion.getOutputFileName(dataset) + ".nc");
 				convert(dataset, tempFile);
 			} catch (RuntimeException e) {
 				r.datasetError(e);
 			} catch (ConverterException e) {
 				r.datasetError(e);
 			}
 		}
 
 		System.out.println(r);
 	}
 
 	static void convert(Dataset dataset, File file) throws ConverterException {
 		NetcdfFileWriteable nc;
 		try {
 			nc = NetcdfFileWriteable.createNew(file.getAbsolutePath(), false);
 		} catch (IOException e) {
 			throw new ConverterException("Cannot create the file", e);
 		}
 
 		/*
 		 * Global metadata
 		 */
 		nc.addGlobalAttribute("id", UUID.randomUUID().toString());
 		nc.addGlobalAttribute("naming_authority", "UUID");
 		nc.addGlobalAttribute("standard_name_vocabulary", VOCABULARY_URL);
 		nc.addGlobalAttribute("icos_domain", dataset.getIcosDomain().toString());
 		nc.addGlobalAttribute("conventions", "CF-1.5");
 		nc.addGlobalAttribute("Metadata_Conventions",
 				"Unidata Dataset Discovery v1.0");
 		nc.addGlobalAttribute("institution", dataset.getInstitution().getName());
 		nc.addGlobalAttribute("creator_url", dataset.getInstitution().getUrl());
 
 		/*
 		 * Add main variable
 		 */
 		ArrayList<Dimension> mainVarDimensions = new ArrayList<Dimension>();
 		Dimension timeDimension = null;
 		if (dataset instanceof TimeSerie) {
 			writeTimeBox(nc, (TimeSerie) dataset);
 			// time dimension variable
 			Variable time = createTimeVariable(nc, (TimeSerie) dataset);
 			timeDimension = time.getDimension(0);
 			mainVarDimensions.add(timeDimension);
 		}
 		if (dataset instanceof Trajectory) {
 			nc.addGlobalAttribute("cdm_data_type",
 					CDMDataType.TRAJECTORY.toString());
 			List<Point2D> trajectoryPoints = ((Trajectory) dataset)
 					.getTrajectoryPoints();
 			writeBBoxGlobalAttributes(nc, trajectoryPoints);
 
 			// Position variables
 			Variable lat = nc.addVariable(LAT_VARIABLE_NAME, DataType.DOUBLE,
 					new Dimension[] { timeDimension });
 			lat.addAttribute(new Attribute("axis", "Y"));
 			lat.addAttribute(new Attribute("standard_name", "latitude"));
 			lat.addAttribute(new Attribute("units", "degrees_north"));
 			Variable lon = nc.addVariable(LON_VARIABLE_NAME, DataType.DOUBLE,
 					new Dimension[] { timeDimension });
 			lon.addAttribute(new Attribute("axis", "X"));
 			lon.addAttribute(new Attribute("standard_name", "longitude"));
 			lon.addAttribute(new Attribute("units", "degrees_east"));
 		} else if (dataset instanceof Station) {
 			nc.addGlobalAttribute("cdm_data_type",
 					CDMDataType.STATION.toString());
 			// Position dimension
 			Dimension stationDimension = nc.addDimension("station",
 					((Station) dataset).getStationCount());
 			mainVarDimensions.add(stationDimension);
 			if (dataset instanceof GeoreferencedStation) {
 				List<Point2D> stationPositions = ((GeoreferencedStation) dataset)
 						.getStationPositions();
 				writeBBoxGlobalAttributes(nc, stationPositions);
 
 				// Position variables
 				Variable lat = nc.addVariable(LAT_VARIABLE_NAME,
 						DataType.DOUBLE, new Dimension[] { stationDimension });
 				lat.addAttribute(new Attribute("axis", "Y"));
 				lat.addAttribute(new Attribute("standard_name", "latitude"));
 				lat.addAttribute(new Attribute("units", "degrees_north"));
 				Variable lon = nc.addVariable(LON_VARIABLE_NAME,
 						DataType.DOUBLE, new Dimension[] { stationDimension });
 				lon.addAttribute(new Attribute("axis", "X"));
 				lon.addAttribute(new Attribute("standard_name", "longitude"));
 				lon.addAttribute(new Attribute("units", "degrees_east"));
 			}
 		}
 		co.geomati.netcdf.dataset.DatasetVariable mainVariable = dataset
 				.getMainVariable();
 		String variableName = mainVariable.getName();
 		Variable mainVar = nc.addVariable(variableName,
 				getVariableType(mainVariable),
 				mainVarDimensions.toArray(new Dimension[0]));
 		mainVar.addAttribute(new Attribute("coordinates", LAT_VARIABLE_NAME
 				+ " " + LON_VARIABLE_NAME));
 		mainVar.addAttribute(new Attribute("long_name", mainVariable
 				.getLongName()));
 		mainVar.addAttribute(new Attribute("standard_name", mainVariable
 				.getStandardName()));
 		mainVar.addAttribute(new Attribute("units", mainVariable.getUnits()
 				.trim()));
 
 		Number fillValue = mainVariable.getFillValue();
 		if (fillValue != null) {
 			mainVar.addAttribute(new Attribute("_FillValue", fillValue));
 		}
 
 		try {
 			nc.create();
 
 			int requiredSize = 1;
 			if (dataset instanceof TimeSerie) {
 				List<Integer> timeStamps = ((TimeSerie) dataset)
 						.getTimeStamps();
 				requiredSize = timeStamps.size();
 			}
 			if (dataset instanceof Station) {
 				requiredSize *= ((Station) dataset).getStationCount();
 			}
 			/*
 			 * Write time
 			 */
 			if (dataset instanceof TimeSerie) {
 				writeTimeValues(nc, ((TimeSerie) dataset).getTimeStamps(),
 						TIME_VARIABLE_AND_DIMENSION_NAME);
 			}
 
 			/*
 			 * write positions
 			 */
 			List<Point2D> points = null;
 			if (dataset instanceof GeoreferencedStation) {
 				points = ((GeoreferencedStation) dataset).getStationPositions();
 			} else if (dataset instanceof Trajectory) {
 				points = ((Trajectory) dataset).getTrajectoryPoints();
 			}
 			if (points != null) {
 				try {
 					nc.write(LAT_VARIABLE_NAME,
 							get1Double(points, new YGetter()));
 					nc.write(LON_VARIABLE_NAME,
 							get1Double(points, new XGetter()));
 				} catch (InvalidRangeException e) {
 					throw new ConverterException(
 							"The specified positions exceed "
 									+ "the number of stations", e);
 				}
 			}
 
 			/*
 			 * write main variable
 			 */
 			Array a;
 			if (mainVariable instanceof DatasetIntVariable) {
 				List<Integer> data = ((DatasetIntVariable) mainVariable)
 						.getData();
 				checkSize(requiredSize, data);
 				a = intToArray(data, getShape(dataset));
 			} else if (mainVariable instanceof DatasetDoubleVariable) {
 				List<Double> data = ((DatasetDoubleVariable) mainVariable)
 						.getData();
 				checkSize(requiredSize, data);
 				a = doubleToArray(data, getShape(dataset));
 			} else {
 				throw WRONG_VARIABLE_IMPLEMENTATION;
 			}
 			try {
 				nc.write(mainVar.getName(), a);
 			} catch (InvalidRangeException e) {
 				throw new ConverterException("Too many data on main variable",
 						e);
 			}
 		} catch (IOException e) {
 			throw new ConverterException("Cannot create netcdf file", e);
 		}
 
 		try {
 			nc.close();
 		} catch (IOException e) {
 			throw new ConverterException("Cannot close created nc file", e);
 		}
 	}
 
 	private static void checkSize(int requiredSize, List<?> data)
 			throws ConverterException {
 		if (data.size() != requiredSize) {
 			throw new ConverterException("Wrong number of "
 					+ "main variable samples. " + requiredSize + " expected");
 		}
 	}
 
 	private static void writeBBoxGlobalAttributes(NetcdfFileWriteable nc,
 			List<Point2D> stationPositions) {
 		Rectangle2D bbox = getBBox(stationPositions);
 		nc.addGlobalAttribute("geospatial_lat_min", bbox.getMinY());
 		nc.addGlobalAttribute("geospatial_lat_max", bbox.getMaxY());
 		nc.addGlobalAttribute("geospatial_lon_min", bbox.getMinX());
 		nc.addGlobalAttribute("geospatial_lon_max", bbox.getMaxX());
 	}
 
 	private static int[] getShape(Dataset dataset) {
 		ArrayList<Integer> shape = new ArrayList<Integer>();
 		if (dataset instanceof TimeSerie) {
 			shape.add(((TimeSerie) dataset).getTimeStamps().size());
 		}
 		if (dataset instanceof Station) {
 			shape.add(((Station) dataset).getStationCount());
 		}
 
 		int[] ret = new int[shape.size()];
 		for (int i = 0; i < ret.length; i++) {
 			ret[i] = shape.get(i);
 		}
 
 		return ret;
 	}
 
 	private static Array doubleToArray(List<Double> data, int[] shape) {
 		Array a = ArrayDouble.factory(DataType.DOUBLE, shape);
 		Index ima = a.getIndex();
 		IndexDecorator index = new IndexDecorator(ima, shape);
 		for (Double sample : data) {
 			a.setDouble(index.get(), sample);
 
 			index.inc();
 		}
 
 		return a;
 	}
 
 	private static Array intToArray(List<Integer> data, int[] shape) {
 		Array a = ArrayInt.factory(DataType.INT, shape);
 		Index ima = a.getIndex();
 		IndexDecorator index = new IndexDecorator(ima, shape);
 		for (Integer sample : data) {
 			a.setInt(index.get(), sample);
 
 			index.inc();
 		}
 
 		return a;
 	}
 
 	private static final class XGetter implements DoubleSampleGetter<Point2D> {
 		@Override
 		public double get(Point2D t) {
 			return t.getX();
 		}
 	}
 
 	private static final class YGetter implements DoubleSampleGetter<Point2D> {
 		@Override
 		public double get(Point2D t) {
 			return t.getY();
 		}
 	}
 
 	private static class IndexDecorator {
 
 		private Index index;
 		private int[] indices;
 		private int[] shape;
 
 		public IndexDecorator(Index ima, int[] shape) {
 			this.index = ima;
 			this.indices = new int[shape.length];
 			this.shape = shape;
 		}
 
 		public Index get() {
 			return index.set(indices);
 		}
 
 		public void inc() {
 			int currentChangingIndex = indices.length - 1;
 			while (currentChangingIndex >= 0) {
 				indices[currentChangingIndex]++;
 				if (indices[currentChangingIndex] == shape[currentChangingIndex]) {
 					indices[currentChangingIndex] = 0;
 					currentChangingIndex--;
 				} else {
 					break;
 				}
 			}
 
 		}
 
 	}
 
 	private static DataType getVariableType(
 			co.geomati.netcdf.dataset.DatasetVariable mainVariable)
 			throws ConverterException {
 		if (mainVariable instanceof DatasetIntVariable) {
 			return DataType.INT;
 		} else if (mainVariable instanceof DatasetDoubleVariable) {
 			return DataType.DOUBLE;
 		} else {
 			throw WRONG_VARIABLE_IMPLEMENTATION;
 		}
 	}
 
 	private static void writeTimeValues(NetcdfFileWriteable nc,
 			List<Integer> timestamps, String timeVariableName)
 			throws IOException {
 		try {
 			nc.write(timeVariableName,
 					get1Int(timestamps, new IntSampleGetter<Integer>() {
 
 						@Override
 						public int get(Integer t) {
 							return t;
 						}
 					}));
 		} catch (InvalidRangeException e) {
 			throw new RuntimeException("Bug. This should not "
 					+ "happen since time is unlimited", e);
 		}
 	}
 
 	private static Variable createTimeVariable(NetcdfFileWriteable nc,
 			TimeSerie dataset) {
 		Variable time = null;
 		Date referenceDate = dataset.getReferenceDate();
 		TimeUnit timeUnit = dataset.getTimeUnits();
 		Dimension timeDim = nc
 				.addUnlimitedDimension(TIME_VARIABLE_AND_DIMENSION_NAME);
 		time = nc.addVariable(TIME_VARIABLE_AND_DIMENSION_NAME, DataType.INT,
 				new Dimension[] { timeDim });
 		time.addAttribute(new Attribute("units", timeUnit.toString()
 				+ " since " + dateFormat.format(referenceDate)));
 		time.addAttribute(new Attribute("axis", "T"));
 
 		return time;
 	}
 
 	private static void writeTimeBox(NetcdfFileWriteable nc, TimeSerie dataset) {
 		List<Integer> times = dataset.getTimeStamps();
 		Date referenceDate = dataset.getReferenceDate();
 		TimeUnit timeUnit = dataset.getTimeUnits();
 		long[] timeBox = getTimeBox(times, referenceDate, timeUnit);
 		DateTimeFormatter parser = ISODateTimeFormat.dateTime();
 		parser = parser.withZoneUTC();
 		nc.addGlobalAttribute("time_coverage_start", parser.print(timeBox[0]));
 		nc.addGlobalAttribute("time_coverage_end", parser.print(timeBox[1]));
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
 			return magnitude * 1000L;
 		} else if (unit == TimeUnit.MINUTE) {
 			return magnitude * 1000L * 60;
 		} else if (unit == TimeUnit.HOUR) {
 			return magnitude * 1000L * 60 * 60;
 		} else if (unit == TimeUnit.DAYS) {
 			return magnitude * 1000L * 60 * 60 * 24;
 		} else if (unit == TimeUnit.COMMON_YEAR) {
 			return magnitude * 1000L * 60 * 60 * 24 * 365;
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
 
 	public static <T> ArrayDouble get1Double(List<T> list,
 			DoubleSampleGetter<T> getter) throws IOException,
 			InvalidRangeException {
 		ArrayDouble A = new ArrayDouble.D1(list.size());
 		Index ima = A.getIndex();
 		for (int i = 0; i < list.size(); i++) {
 			A.setDouble(ima.set(i), getter.get(list.get(i)));
 		}
 		return A;
 	}
 
 	public static <T> ArrayInt get1Int(List<T> list, IntSampleGetter<T> getter)
 			throws IOException, InvalidRangeException {
 		ArrayInt A = new ArrayInt.D1(list.size());
 		Index ima = A.getIndex();
 		for (int i = 0; i < list.size(); i++) {
 			A.setInt(ima.set(i), getter.get(list.get(i)));
 		}
 		return A;
 	}
 }
