 package co.geomati.netcdf.utm;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Positions {
 
 	public class Latitude implements UTMVariable {
 
 		@Override
 		public void addSample(String sample) {
 			Positions.this.setLatitude(Double.parseDouble(sample));
 		}
 
 	}
 
 	public class Longitude implements UTMVariable {
 
 		@Override
 		public void addSample(String sample) {
 			Positions.this.setLongitude(Double.parseDouble(sample));
 		}
 
 	}
 
 	private double lat = Double.NaN;
 	private double lon = Double.NaN;
 	private ArrayList<Point2D> positions = new ArrayList<Point2D>();
 
 	public void setLatitude(double lat) {
 		this.lat = lat;
 		addPosition();
 	}
 
 	public void setLongitude(double lon) {
 		this.lon = lon;
 		addPosition();
 	}
 
 	private void addPosition() {
 		if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
			positions.add(new Point2D.Double(lon, lat));
 			lat = Double.NaN;
 			lon = Double.NaN;
 		}
 	}
 
 	public List<Point2D> getTrajectoryPoints() {
 		return positions;
 	}
 
 }
