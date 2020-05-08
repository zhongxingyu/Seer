 package co.geomati.netcdf.ceam;
 
 import java.awt.geom.Point2D;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import ucar.ma2.Array;
 import ucar.ma2.ArrayDouble;
 import ucar.ma2.DataType;
 import ucar.ma2.Index;
 import co.geomati.netcdf.ConverterException;
 import co.geomati.netcdf.IcosDomain;
 import co.geomati.netcdf.StationDataset;
 import co.geomati.netcdf.TimeUnit;
 
 public class CEAMDataset implements StationDataset {
 
 	private String creatorURL;
 	private Point2D position;
 	private Variable variable;
 	private DataType dataType;
 	private TimeUnit timeUnits;
 	private Date referenceDate;
 	private List<Integer> timeStamps;
 
 	public CEAMDataset(VariableGroup variableGroup, String creatorURL,
 			Point2D position) throws ConverterException {
 		this.creatorURL = creatorURL;
 		this.position = position;
 		this.variable = getMainVariable(variableGroup);
 		this.dataType = getDataType(this.variable);
 		Variable timeVariable = getTimeVariable(variableGroup);
 		if (timeVariable != null) {
 			setTimeInfo(timeVariable);
 		}
 	}
 
 	private Variable getTimeVariable(VariableGroup variableGroup) {
 		for (Variable variable : variableGroup) {
 			if (variable.getName().endsWith("_DATE")
 					|| variable.getName().endsWith("_YEAR")) {
 				return variable;
 			}
 		}
 
 		return null;
 	}
 
 	private void setTimeInfo(Variable timeVariable) throws ConverterException {
 		String units = timeVariable.getUnits();
 		if (units.equals("YYYY")) {
 			timeUnits = TimeUnit.COMMON_YEAR;
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
 			try {
 				referenceDate = sdf.parse("0000");
 			} catch (ParseException e) {
 				throw new RuntimeException("bug");
 			}
 			ArrayList<Integer> times = new ArrayList<Integer>();
 			ArrayList<Object> values = timeVariable.getValues();
 			for (Object object : values) {
 				times.add(((Number) object).intValue());
 			}
 			timeStamps = times;
 		} else if (units.equals("DOY/YYYY")) {
 			timeUnits = TimeUnit.DAYS;
 			ArrayList<Integer> times = new ArrayList<Integer>();
 			ArrayList<Object> values = timeVariable.getValues();
 			Date referenceYear = null;
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
 			for (Object object : values) {
 				String dayYear = (String) object;
 				String[] dayYearArray = dayYear.split("/");
 				int day = Integer.parseInt(dayYearArray[0]);
 				Date year;
 				try {
 					year = sdf.parse(dayYearArray[1]);
 				} catch (ParseException e) {
 					throw new ConverterException("invalid year forma: "
 							+ dayYearArray[1]);
 				}
 				if (referenceYear == null) {
 					referenceYear = year;
 				} else if (!referenceYear.equals(year)) {
 					throw new RuntimeException("Wrong reference "
 							+ "year for variable: " + timeVariable.getName());
 				}
 				times.add(day);
 			}
 			timeStamps = times;
 
 			referenceDate = referenceYear;
 		} else {
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	private DataType getDataType(Variable variable) {
 		Object firstValue = variable.getValues().get(0);
 		if (firstValue instanceof Boolean) {
 			return DataType.BOOLEAN;
 		} else if (firstValue instanceof Number) {
 			return DataType.DOUBLE;
 		} else if (firstValue instanceof String) {
 			return DataType.STRING;
 		} else if (firstValue instanceof Date) {
 			return DataType.INT;
 		} else {
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	private Variable getMainVariable(VariableGroup variableGroup) {
 		int minLength = Integer.MAX_VALUE;
 		Variable argMinLength = null;
 		for (Variable variable : variableGroup) {
 			if (variable.getName().length() < minLength) {
 				minLength = variable.getName().length();
 				argMinLength = variable;
 			}
 		}
 
 		return argMinLength;
 	}
 
 	@Override
 	public IcosDomain getIcosDomain() {
 		return IcosDomain.ECOSYSTEM;
 	}
 
 	@Override
 	public String getInstitution() {
 		return "CEAM";
 	}
 
 	@Override
 	public String getCreatorURL() {
 		return creatorURL;
 	}
 
 	@Override
 	public String getVariableName() {
 		return variable.getName();
 	}
 
 	@Override
 	public String getVariableLongName() {
 		return variable.getLongName();
 	}
 
 	@Override
 	public String getVariableStandardName() {
 		return variable.getName();
 	}
 
 	@Override
 	public String getVariableUnits() {
 		return variable.getUnits();
 	}
 
 	@Override
 	public TimeUnit getTimeUnits() {
 		return timeUnits;
 	}
 
 	@Override
 	public Date getReferenceDate() {
 		return referenceDate;
 	}
 
 	@Override
 	public DataType getVariableType() {
 		return dataType;
 	}
 
 	@Override
 	public List<Point2D> getPositions() {
 		return Collections.singletonList(position);
 	}
 
 	@Override
 	public List<Integer> getTimeStamps() {
 		return timeStamps;
 	}
 
 	@Override
 	public Array getStationData() {
 		List<Integer> timeStamps = getTimeStamps();
 		if (timeStamps != null) {
 			int timeSize = timeStamps.size();
 			int stationSize = getPositions().size();
 			ArrayDouble a = new ArrayDouble.D2(timeSize, stationSize);
 			Index ima = a.getIndex();
 			for (int i = 0; i < timeSize; i++) {
 				for (int j = 0; j < stationSize; j++) {
 					Double value = (Double) variable.getValues().get(
 							i * stationSize + j);
 					a.setDouble(ima.set(i, j), value);
 				}
 			}
 
 			return a;
 		} else {
 			int stationSize = getPositions().size();
 			ArrayDouble a = new ArrayDouble.D1(stationSize);
 			Index ima = a.getIndex();
 			for (int j = 0; j < stationSize; j++) {
				Double value = (Double) variable.getValues().get(j);
				a.setDouble(ima.set(j), value);
 			}
 
 			return a;
 		}
 	}
 
 }
