 package co.geomati.netcdf;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import ucar.ma2.DataType;
 
 public class SampleDataset implements StationDataset {
 
 	@Override
 	public IcosDomain getIcosDomain() {
 		return IcosDomain.ATMOSFERA;
 	}
 
 	@Override
 	public String getInstitution() {
 		return "Centro Nacional de Atmosferas Unidas";
 	}
 
 	@Override
 	public String getCreatorURL() {
 		return "http://www.cnau.es";
 	}
 
 	@Override
 	public String getVariableName() {
 		return "temperature";
 	}
 
 	@Override
 	public String getVariableLongName() {
 		return "Air temperature at see level";
 	}
 
 	@Override
 	public String getVariableStandardName() {
 		return "air_temperature";
 	}
 
 	@Override
 	public String getVariableUnits() {
 		return "grados jarrrrl";
 	}
 
 	@Override
 	public TimeUnit getTimeUnits() {
 		return TimeUnit.SECOND;
 	}
 
 	@Override
 	public Date getReferenceDate() {
 		return new Date();
 	}
 
 	@Override
 	public DataType getVariableType() {
 		return DataType.DOUBLE;
 	}
 
 	@Override
 	public List<Point2D> getPositions() {
 		ArrayList<Point2D> ret = new ArrayList<Point2D>();
 		ret.add(new Point2D.Double(10, 10));
 		ret.add(new Point2D.Double(20, 20));
 		ret.add(new Point2D.Double(30, 30));
 		return ret;
 	}
 
 	@Override
 	public List<Integer> getTimeStamps() {
 		List<Integer> ret = new ArrayList<Integer>();
 		ret.add(10);
 		ret.add(20);
 		ret.add(30);
 		ret.add(40);
		ret.add(50);
		ret.add(60);
 		return ret;
 	}
 
 	@Override
 	public double getStationData(int stationIndex, int timestampIndex) {
		return 30 + timestampIndex;
 	}
 
 }
