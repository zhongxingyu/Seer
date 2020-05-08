 package edu.usc.sirlab;
 
 import java.io.Serializable;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.usc.sirlab.kml.Style;
 
 public class QuakeSimFault extends Fault implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private static final String BASE_URL = "http://quakesim.org/quaketables/fault.jsp?ds=";
 	
 	private String comment;
 	
 	private Double depth;
 	private Double dipAngel;
 	private Double locationX, locationY;
 	private Double length;
 	private Double width;
 	private Double strikeAngel;
 	private Double dipSlip;
 	private Double strikeSlip;
 	private final DecimalFormat df = new DecimalFormat("#.###");
 	
 	public QuakeSimFault(FaultDataSet dataSet, String id, String name) {
 		super(dataSet, id, name);
 	}
 	
 	public QuakeSimFault(CGSFault fault) {
 		super(fault.dataSet, fault.id, fault.name);
 		this.setComment(fault.getComment());
 		this.setTraces(fault.getTraces());
 		this.depth = fault.getRupBottom();
 		this.dipAngel = fault.getDip();
 		
 		double latStart = fault.getTraces().get(0).getLat();
 		double lonStart = fault.getTraces().get(0).getLon();
 		double latEnd = fault.getTraces().get(1).getLat();
 		double lonEnd = fault.getTraces().get(1).getLon();
 		
 		double equard = 6378.139;
 		double flattening = 1.0 / 298.247;
 		double yFactor = 111.32;
		double xFactor = equard * (Math.PI / 180.0) * Math.cos(latStart * Math.PI / 180.0) * (1.0 - (flattening * Math.sin(latStart * Math.PI / 180.0) * Math.sin(latStart * Math.PI / 180.0)));
 		
 		this.locationX = xFactor * (lonEnd - lonStart);
 		this.locationY = yFactor * (latEnd - latStart);
 		this.length = Math.sqrt((locationX * locationX) + (locationY * locationY));
 		this.strikeAngel = Math.atan2(locationX, locationY) * (180.0 / Math.PI);
 		this.width = Math.abs(fault.getRupBottom() - fault.getRupTop()) / Math.sin(dipAngel);
 		
 		if(fault.getRake() != null && fault.getSlipRate() != null) {
 			this.strikeSlip = fault.getSlipRate() * Math.cos(fault.getRake() * Math.PI / 180.0);
 			this.dipSlip = fault.getSlipRate() * Math.sin(fault.getRake() * Math.PI / 180.0);
 		}
 		else {
 			this.dipSlip = null;
 			this.strikeSlip = null;
 		}
 	}
 	
 	public QuakeSimFault(NCALFault fault) {
 		super(fault.dataSet, fault.id, fault.name);
 		this.setTraces(fault.getTraces());
 		this.depth = fault.getDepth();
 		this.dipAngel = fault.getDip();
 		
 		double latStart = fault.getTraces().get(0).getLat();
 		double lonStart = fault.getTraces().get(0).getLon();
 		double latEnd = fault.getTraces().get(1).getLat();
 		double lonEnd = fault.getTraces().get(1).getLon();
 		
 		double equard = 6378.139;
 		double flattening = 1.0 / 298.247;
 		double yFactor = 111.32;
 		double xFactor = equard * (Math.PI / 180.0) * Math.cos(latStart * Math.PI / 180.0) * (1.0 - (flattening * Math.sin(lonStart * Math.PI / 180.0)));
 		
 		this.locationX = xFactor * (lonEnd - lonStart);
 		this.locationY = yFactor * (latEnd - latStart);
 		this.length = Math.sqrt((locationX * locationX) + (locationY * locationY));
 		this.strikeAngel = Math.atan2(locationX, locationY) * (180.0 / Math.PI);
 		//TODO: Not sure about this
 		this.width = Math.abs(fault.getDepth() - 0.0) / Math.sin(dipAngel);
 		
 		if(fault.getRake() != null && fault.getSlipRate() != null) {
 			this.strikeSlip = fault.getSlipRate() * Math.cos(fault.getRake() * Math.PI / 180.0);
 			this.dipSlip = fault.getSlipRate() * Math.sin(fault.getRake() * Math.PI / 180.0);
 		}
 		else {
 			this.dipSlip = null;
 			this.strikeSlip = null;
 		}
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	public Double getDepth() {
 		return depth;
 	}
 
 	public void setDepth(Double depth) {
 		this.depth = depth;
 	}
 
 	public Double getDipAngel() {
 		return dipAngel;
 	}
 
 	public void setDipAngel(Double dipAngel) {
 		this.dipAngel = dipAngel;
 	}
 
 	public Double getLocationX() {
 		return locationX;
 	}
 
 	public void setLocationX(Double locationX) {
 		this.locationX = locationX;
 	}
 
 	public Double getLocationY() {
 		return locationY;
 	}
 
 	public void setLocationY(Double locationY) {
 		this.locationY = locationY;
 	}
 
 	public Double getLength() {
 		return length;
 	}
 
 	public void setLength(Double length) {
 		this.length = length;
 	}
 
 	public Double getWidth() {
 		return width;
 	}
 
 	public void setWidth(Double width) {
 		this.width = width;
 	}
 
 	public Double getStrikeAngel() {
 		return strikeAngel;
 	}
 
 	public void setStrikeAngel(Double strikeAngel) {
 		this.strikeAngel = strikeAngel;
 	}
 
 	public Double getDipSlip() {
 		return dipSlip;
 	}
 
 	public void setDipSlip(Double dipSlip) {
 		this.dipSlip = dipSlip;
 	}
 
 	public Double getStrikeSlip() {
 		return strikeSlip;
 	}
 
 	public void setStrikeSlip(Double strikeSlip) {
 		this.strikeSlip = strikeSlip;
 	}
 	
 	public List<String[]> getHTMLParameters() {
 		List<String[]> param = new ArrayList<String[]>();
 		
 		if(name != null) {
 			String[] p = {"Fault Name", name};
 			param.add(p);
 		}
 		if(length != null) {
 			String[] p = {"Length", df.format(length).toString()};
 			param.add(p);
 		}
 		if(width != null) {
 			String[] p = {"Width", df.format(width).toString()};
 			param.add(p);
 		}
 		if(depth != null) {
 			String[] p = {"Depth", df.format(depth).toString()};
 			param.add(p);
 		}
 		if(dipAngel != null) {
 			String[] p = {"DipAngel", df.format(dipAngel).toString()};
 			param.add(p);
 		}
 		if(strikeAngel != null) {
 			String[] p = {"Strike Angel", df.format(strikeAngel).toString()};
 			param.add(p);
 		}
 		if(dipSlip != null) {
 			String[] p = {"Dip Slip", df.format(dipSlip).toString()};
 			param.add(p);
 		}
 		if(strikeSlip != null) {
 			String[] p = {"Strike Slip", strikeSlip.toString()};
 			param.add(p);
 		}
 		if(locationX != null && locationY != null) {
 			String[] p = {"Location [x, y]", new String("[" + df.format(locationX) + ", " + df.format(locationY) + "]")};
 			param.add(p);
 		}
 		if(getTraces() != null && getTraces().size() > 0) {
 			String[] p = {"Location [lat, lon]", getTracesString()};
 			param.add(p);
 		}
 		if(comment != null) {
 			String[] p = {"Comment", comment};
 			param.add(p);
 		}
 		
 		String[] disclamer = {"Data Format", "This data has been recalculated to conform with the QuakeSim fault format. " +
 				"The original published data is available at the <a href=\"" + BASE_URL + dataSet.getId() + "&fid=" + id + "\">" +
 				dataSet.getNickName() + " fault page" + "</a>"};
 		param.add(disclamer);
 		
 		return param;
 	}
 	
 	public String getKMLPlacemark(Style style, boolean putPlacemark) {
 		//TODO: Use getHTMLParameters() to make this short and easy
 		List<String[]> parameters = getHTMLParameters();
 		String[] details = {"Details", BASE_URL + dataSet.getId() + "&fid=" + id + "&f=qt"};
 		String description = "";
 		
 		parameters.add(details);
 		for(String[] p : parameters) {
 			description += "<b>" + p[0] + "</b>: " + p[1] + "<br>";
 		}
 		
 		//TODO: FIX this so you get it from the FaultTracePoint class of Fault Class
 		String tracePoints = "";
 		for(FaultTracePoint p : getTraces()) {
 			tracePoints += p.getLon() + "," + p.getLat() + " ";
 		}
 		
 		String myString = "";
 		myString += "<Placemark>";
 		myString += "<name>" + name + "</name>";
 		myString += "<description><![CDATA[" + description + "]]></description>";
 		myString += "<styleUrl>" + "#" + style.getId() + "</styleUrl>";
 		myString += "<LineString>";
 		myString += "<altitudeMode>clampToGround</altitudeMode>"; //"<tessellate>1</tessellate>";
 		myString += "<coordinates>" + tracePoints.trim() + "</coordinates>"; 
 		myString += "</LineString>";
 		myString += "</Placemark>";
 		
 		return myString;
 	}
 }
