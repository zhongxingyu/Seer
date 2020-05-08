 package gov.usgs.cida.gdp.dataaccess.helper;
 
 import gov.usgs.cida.gdp.dataaccess.bean.DataTypeCollection;
 import gov.usgs.cida.gdp.dataaccess.bean.DataTypeCollection.DataTypeBean;
 import gov.usgs.cida.gdp.utilities.bean.Time;
 import gov.usgs.cida.gdp.utilities.bean.XmlResponse;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.List;
 import opendap.dap.Attribute;
 import opendap.dap.AttributeTable;
 import opendap.dap.BaseType;
 import opendap.dap.DAP2Exception;
 import opendap.dap.DAS;
 import opendap.dap.DArray;
 import opendap.dap.DArrayDimension;
 import opendap.dap.DConnect2;
 import opendap.dap.DDS;
 import opendap.dap.DGrid;
 import opendap.dap.DataDDS;
 import opendap.dap.Float32PrimitiveVector;
 import opendap.dap.Float64PrimitiveVector;
 import opendap.dap.Int16PrimitiveVector;
 import opendap.dap.Int32PrimitiveVector;
 import opendap.dap.NoSuchAttributeException;
 import opendap.dap.PrimitiveVector;
 import org.slf4j.LoggerFactory;
 import ucar.nc2.units.DateUnit;
 
 public class OpendapServerHelper {
 
 	static org.slf4j.Logger log = LoggerFactory.getLogger(OpendapServerHelper.class);
 
 	public static Time getTimeBean(String datasetUrl, String gridSelection) throws IOException, ParseException {
 
 		//List<String> dateRangeOld = NetCDFUtility.getDateRange(datasetUrl, gridSelection);
 		List<String> dateRange = getOPeNDAPTimeRange(datasetUrl, gridSelection);
 		if (dateRange.isEmpty()) {
 			boolean hasTimeCoord = NetCDFUtility.hasTimeCoordinate(datasetUrl);
 			if (hasTimeCoord) { // This occurs when there is no date range
 				// in the file but dataset has time coords. We want the user
 				// to pick dates but don't have a range to give.
 				dateRange.add("1800-01-01 00:00:00Z");
 				dateRange.add("2100-12-31 00:00:00Z");
 			}
 		}
 
 		Time timeBean = new Time(dateRange);
 
 		return timeBean;
 	}
 
 	public static List<String> getOPeNDAPTimeRange(String datasetUrl, String gridSelection) throws IOException {
 		try {
 			// call das, dds
 			String finalUrl = "";
 			if (datasetUrl.startsWith("dods:")) {
 				finalUrl = "http:" + datasetUrl.substring(5);
 			}
 			else {
 				if (datasetUrl.startsWith("http:")) {
 					finalUrl = datasetUrl;
 				}
 				else {
 					throw new java.net.MalformedURLException(datasetUrl + " must start with dods: or http:");
 				}
 			}
 			DConnect2 dodsConnection = new DConnect2(finalUrl, false);
 			DDS dds = dodsConnection.getDDS(gridSelection);
 
 			DAS das = dodsConnection.getDAS();
 			BaseType selection = dds.getVariable(gridSelection);
 			DArray array = null;
 			if ("Grid".equals(selection.getTypeName())) {
 				DGrid grid = (DGrid) selection;
 				array = (DArray) grid.getVar(0);
 			}
 			else {
 				if ("Array".equals(selection.getTypeName())) {
 					array = (DArray) selection;
 				}
 				else {
 					throw new UnsupportedOperationException("This dataset type is not yet supported");
 				}
 			}
 
 			String timeDim = getTimeDim(das, array);
 			try {
 				AttributeTable attributeTable = das.getAttributeTable(timeDim);
 				Attribute units = attributeTable.getAttribute("units");
 				DateUnit dateUnit = new DateUnit(units.getValueAt(0));
 				DataDDS datadds = dodsConnection.getData("?" + timeDim); // time dimension
 				DArray variable = (DArray) datadds.getVariable(timeDim);
 				return getDatesFromTimeVariable(variable, dateUnit);
 			}
 			catch (Exception e) {
 				e.getMessage();
 				// not time unit
 			}
 		}
 		catch (opendap.dap.parser.ParseException ex) {
 			log.error("Parser exception caught" + ex);
 		}
 		catch (DAP2Exception ex) {
 			log.error("OPeNDAP exception caught" + ex);
 		}
 		catch (Exception ex) {
 			log.error("General exception caught" + ex);
 		}
 		return Collections.EMPTY_LIST;  // Could not get time, fall through
 	}
 
 	private static List<String> getDatesFromTimeVariable(DArray variable, DateUnit dateUnit) {
 		// TODO make utility to cast this stuff for me
 		List<String> dateList = new ArrayList<String>();
 		PrimitiveVector pVector = variable.getPrimitiveVector();
 		double first = 0.0;
 		double last = 0.0;
 		if (pVector instanceof Int32PrimitiveVector) {
 			Int32PrimitiveVector i32Vector = (Int32PrimitiveVector) pVector;
 			first = i32Vector.getValue(0);
 			last = i32Vector.getValue(i32Vector.getLength() - 1);
 		}
 		else if (pVector instanceof Int16PrimitiveVector) {
 			Int16PrimitiveVector i16Vector = (Int16PrimitiveVector) pVector;
 			first = i16Vector.getValue(0);
 			last = i16Vector.getValue(i16Vector.getLength() - 1);
 		}
 		else if (pVector instanceof Float32PrimitiveVector) {
 			Float32PrimitiveVector f32Vector = (Float32PrimitiveVector) pVector;
 			first = f32Vector.getValue(0);
 			last = f32Vector.getValue(f32Vector.getLength() - 1);
 		}
 		else if (pVector instanceof Float64PrimitiveVector) {
 			Float64PrimitiveVector f64Vector = (Float64PrimitiveVector) pVector;
 			first = f64Vector.getValue(0);
 			last = f64Vector.getValue(f64Vector.getLength() - 1);
 		}
 		else {
 			throw new UnsupportedOperationException("This primitive type for time is not yet supported");
 		}
 
 		dateList.add(dateUnit.makeStandardDateString(first));
 		dateList.add(dateUnit.makeStandardDateString(last));
 		return dateList;
 	}
 
 	public static List<XmlResponse> getGridBeanListFromServer(String datasetUrl)
 			throws IllegalArgumentException, IOException {
 
 		List<XmlResponse> result = new ArrayList<XmlResponse>();
 
 		//DataTypeCollection dtcb = NetCDFUtility.getDataTypeCollection(datasetUrl);
 		DataTypeCollection dtcb = callDDSandDAS(datasetUrl);
 		result.add(dtcb);
 		return result;
 	}
 
 	public static DataTypeCollection callDDSandDAS(String datasetUrl) throws IOException {
 		// call das, dds
 		String finalUrl = "";
 		if (datasetUrl.startsWith("dods:")) {
 			finalUrl = "http:" + datasetUrl.substring(5);
 		}
 		else {
 			if (datasetUrl.startsWith("http:")) {
				finalUrl = datasetUrl;
 			}
 			else {
 				throw new java.net.MalformedURLException(datasetUrl + " must start with dods: or http:");
 			}
 		}
 		DConnect2 dodsConnection = new DConnect2(finalUrl, false);
 		List<DataTypeBean> dtbListWithTimes = new LinkedList<DataTypeBean>();
 		List<DataTypeBean> dtbListNoTimes = new LinkedList<DataTypeBean>();
 		try {
 			DDS dds = dodsConnection.getDDS();
 			DAS das = dodsConnection.getDAS();
 
 			Enumeration<BaseType> variables = dds.getVariables();
 			while (variables.hasMoreElements()) {
 				BaseType nextElement = variables.nextElement();
 				String timeDim = null;
 				DataTypeBean dtb = null;
 				if ("Grid".equals(nextElement.getTypeName())) {
 					DGrid grid = (DGrid) nextElement;
 					String longName = grid.getLongName();
 					DArray array = (DArray) grid.getVar(0); // ARRAY section
 					timeDim = getTimeDim(das, array);
 					dtb = createDataTypeBean(array, longName, das);
 				}
 				if ("Array".equals(nextElement.getTypeName())) {
 					DArray array = (DArray) nextElement;
 					String longName = nextElement.getLongName();
 					AttributeTable attributeTable = das.getAttributeTable(longName);
 					if (!attributeTable.hasAttribute("coordinates")) {
 						continue;
 					}
 					int rank = array.numDimensions();
 					timeDim = getTimeDim(das, array);
 					dtb = createDataTypeBean(array, longName, das);
 				}
 				if (dtb != null) {
 					if (timeDim == null) {
 						dtbListNoTimes.add(dtb);
 					}
 					else {
 						dtbListWithTimes.add(dtb);
 					}
 				}
 			}
 		}
 		catch (opendap.dap.parser.ParseException ex) {
 			// do something with exceptions
 		}
 		catch (DAP2Exception ex) {
 			// do something with exceptions
 		}
 		dtbListWithTimes.addAll(dtbListNoTimes);
 		DataTypeBean[] dtbArr = new DataTypeBean[dtbListWithTimes.size()];
 		dtbListWithTimes.toArray(dtbArr);
 		DataTypeCollection dtc = new DataTypeCollection("GRID", dtbArr); // TODO shouldn't be explicit GRID, didn't know where to get it
 		// return
 		return dtc;
 	}
 
 	private static DataTypeBean createDataTypeBean(DArray array, String name, DAS das) throws NoSuchAttributeException {
 		int[] dims = new int[array.numDimensions()];
 		int i = 0;
 		Enumeration<DArrayDimension> dimensions = array.getDimensions();
 		while (dimensions.hasMoreElements()) {
 			DArrayDimension dim = dimensions.nextElement();
 			dims[i] = dim.getSize();
 			i++;
 		}
 		AttributeTable dasAttrs = das.getAttributeTable(name);
 		Attribute long_name = dasAttrs.getAttribute("long_name");
 		String longVal = (long_name == null) ? name : long_name.getValueAt(0);
 		Attribute unitAttr = dasAttrs.getAttribute("units");
 		if (unitAttr == null) {
 			return null; // no units = not a datatype
 		}
 		String units = unitAttr.getValueAt(0);
 		DataTypeBean dtb = new DataTypeBean();
 		dtb.setDescription(longVal);
 		dtb.setName(name);
 		dtb.setRank(dims.length);
 		dtb.setShape(dims);
 		dtb.setShortname(name);
 		dtb.setUnitsstring(units);
 		return dtb;
 	}
 
 	/**
 	 * This tries to get the time dimension from a variable
 	 * Note, it will fail if there are two "time" dimensions in a variable
 	 * This could happen if there is a runHour, forecastHour type dataset
 	 * @param das Attributes from earlier that hold the information
 	 * @param variable Variable to inspect for time
 	 * @return name of the time dimension variable
 	 */
 	private static String getTimeDim(DAS das, DArray variable) {
 		String timeVarName = null;
 		Enumeration<DArrayDimension> dimensions = variable.getDimensions();
 		while (dimensions.hasMoreElements()) {
 			DArrayDimension nextDim = dimensions.nextElement();
 			String name = nextDim.getName();
 			try {
 				AttributeTable attributeTable = das.getAttributeTable(name);
 				Attribute units = attributeTable.getAttribute("units");
 				if (units != null) {
 					DateUnit dateUnit = new DateUnit(units.getValueAt(0));
 				}
 				timeVarName = name;
 			}
 			catch (Exception ioe) {
 				// dimension not a date, keep trying
 			}
 		}
 		return timeVarName;
 	}
 }
