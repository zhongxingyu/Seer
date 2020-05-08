 package edu.usc.sirlab;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.usc.sirlab.kml.Style;
 
 public class CGSFault extends Fault implements Serializable {
 	private static final long serialVersionUID = 2179217494419608637L;
	private static final String BASE_URL = "http://quaketables.quakesim.org/fault.jsp?ds=";
 	private String faultClass;
 	private String zone;
 	private String geometry, geometryDirection;
 	private Double length, lengthError;
 	private Double slipRate, slipRateError;
 	private String rank;
 	private Double mMax;
 	private Double charRate;
 	private Double recurrence;
 	private Double downDipWidth, downDipWidthError;
 	private Double rupTop, rupBottom;
 	private Double rake, dip, daz;
 	private String comment;
 	
 	public CGSFault(FaultDataSet dataSet, String id, String name) {
 		super(dataSet, id, name);
 	}
 
 	public String getFaultClass() {
 		return faultClass;
 	}
 
 	public void setFaultClass(String faultClass) {
 		this.faultClass = faultClass;
 	}
 
 	public String getZone() {
 		return zone;
 	}
 
 	public void setZone(String zone) {
 		this.zone = zone;
 	}
 
 	public String getGeometry() {
 		return geometry;
 	}
 
 	public void setGeometry(String geometry) {
 		this.geometry = geometry;
 	}
 
 	public String getGeometryDirection() {
 		return geometryDirection;
 	}
 
 	public void setGeometryDirection(String geometryDirection) {
 		this.geometryDirection = geometryDirection;
 	}
 
 	public Double getLength() {
 		return length;
 	}
 
 	public void setLength(Double length) {
 		this.length = length;
 	}
 
 	public Double getLengthError() {
 		return lengthError;
 	}
 
 	public void setLengthError(Double lengthError) {
 		this.lengthError = lengthError;
 	}
 
 	public Double getSlipRate() {
 		return slipRate;
 	}
 
 	public void setSlipRate(Double slipRate) {
 		this.slipRate = slipRate;
 	}
 
 	public Double getSlipRateError() {
 		return slipRateError;
 	}
 
 	public void setSlipRateError(Double slipRateError) {
 		this.slipRateError = slipRateError;
 	}
 
 	public String getRank() {
 		return rank;
 	}
 
 	public void setRank(String rank) {
 		this.rank = rank;
 	}
 
 	public Double getmMax() {
 		return mMax;
 	}
 
 	public void setmMax(Double mMax) {
 		this.mMax = mMax;
 	}
 
 	public Double getCharRate() {
 		return charRate;
 	}
 
 	public void setCharRate(Double charRate) {
 		this.charRate = charRate;
 	}
 
 	public Double getRecurrence() {
 		return recurrence;
 	}
 
 	public void setRecurrence(Double recurrence) {
 		this.recurrence = recurrence;
 	}
 
 	public Double getDownDipWidth() {
 		return downDipWidth;
 	}
 
 	public void setDownDipWidth(Double downDipWidth) {
 		this.downDipWidth = downDipWidth;
 	}
 
 	public Double getDownDipWidthError() {
 		return downDipWidthError;
 	}
 
 	public void setDownDipWidthError(Double downDipWidthError) {
 		this.downDipWidthError = downDipWidthError;
 	}
 
 	public Double getRupTop() {
 		return rupTop;
 	}
 
 	public void setRupTop(Double rupTop) {
 		this.rupTop = rupTop;
 	}
 
 	public Double getRupBottom() {
 		return rupBottom;
 	}
 
 	public void setRupBottom(Double rupBottom) {
 		this.rupBottom = rupBottom;
 	}
 
 	public Double getRake() {
 		return rake;
 	}
 
 	public void setRake(Double rake) {
 		this.rake = rake;
 	}
 
 	public Double getDip() {
 		return dip;
 	}
 
 	public void setDip(Double dip) {
 		this.dip = dip;
 	}
 
 	public Double getDaz() {
 		return daz;
 	}
 
 	public void setDaz(Double daz) {
 		this.daz = daz;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 	
 	public List<String[]> getHTMLParameters() {
 		List<String[]> param = new ArrayList<String[]>();
 		
 		if(name != null) {
 			String[] p = {"Fault Name", name};
 			param.add(p);
 		}
 		if(zone != null) {
 			String[] p = {"Zone", zone};
 			param.add(p);
 		}
 		if(faultClass != null) {
 			String[] p = {"Class", faultClass};
 			param.add(p);
 		}
 		if(geometry != null) {
 			String[] p = {"Geometry", geometry};
 			param.add(p);
 		}
 		if(geometryDirection != null) {
 			String[] p = {"Geometry Direction", geometryDirection};
 			param.add(p);
 		}
 		if(length != null) {
 			String[] p = {"Length", new String(length + "&nbsp;&nbsp; &plusmn;" + lengthError)};
 			param.add(p);
 		}
 		if(slipRate != null) {
 			String[] p = {"Slip Rate", new String(slipRate + "&nbsp;&nbsp; &plusmn;" + slipRateError)};
 			param.add(p);
 		}
 		if(rank != null) {
 			String[] p = {"Rank", rank};
 			param.add(p);
 		}
 		if(mMax != null) {
 			String[] p = {"Maximum Magnitude", mMax.toString()};
 			param.add(p);
 		}
 		if(charRate != null) {
 			String[] p = {"Characteristic Rate", charRate.toString()};
 			param.add(p);
 		}
 		if(recurrence != null) {
 			String[] p = {"Recurrence Interval", recurrence.toString()};
 			param.add(p);
 		}
 		if(downDipWidth != null) {
 			String[] p = {"Down Dip Width", new String(downDipWidth + "&nbsp;&nbsp; &plusmn;" + downDipWidthError)};
 			param.add(p);
 		}
 		if(rupTop != null) {
 			String[] p = {"Rupture - Top", rupTop.toString()};
 			param.add(p);
 		}
 		if(rupBottom != null) {
 			String[] p = {"Rupture - Bottom", rupBottom.toString()};
 			param.add(p);
 		}
 		if(rake != null) {
 			String[] p = {"Rake", rake.toString()};
 			param.add(p);
 		}
 		if(dip != null) {
 			String[] p = {"Dip", dip.toString()};
 			param.add(p);
 		}
 		if(daz != null) {
 			String[] p = {"Daz", daz.toString()};
 			param.add(p);
 		}
 		if(getTraces() != null && getTraces().size() > 0) {
 			String[] p = {"Location", getTracesString()};
 			param.add(p);
 		}
 		if(comment != null) {
 			String[] p = {"Comment", comment};
 			param.add(p);
 		}
 		return param;
 	}
 	
 	public String getKMLPlacemark(Style style, boolean putPlacemark) {
 		//TODO: Use getHTMLParameters() to make this short and easy
 		
 		String description = "";
 		if(name != null)
 			description += "<b>Fault Name</b>:" + name + "<br>";
 		if(zone != null)
 			description += "<b>Zone</b>: " + zone + "<br>";
 		if(faultClass != null)
 			description += "<b>Class</b>: " + faultClass + "<br>";
 		if(geometry != null)
 			description += "<b>Geometry</b>: " + geometry + "<br>";
 		if(geometryDirection != null)
 			description += "<b>Geometry Direction</b>: " + geometryDirection + "<br>";
 		if(length != null)
 			description += "<b>Length</b>: " + length + "&nbsp;&nbsp; &plusmn;" + lengthError + "<br>";
 		if(slipRate != null)
 			description += "<b>Slip Rate</b>: " + slipRate + "&nbsp;&nbsp; &plusmn;" + slipRateError + "<br>";
 		if(rank != null)
 			description += "<b>Rank</b>: " + rank + "<br>";
 		if(mMax != null)
 			description += "<b>Maximum Magnitude</b>: " + mMax + "<br>";
 		if(charRate != null)
 			description += "<b>Characteristic Rate</b>: " + charRate + "<br>";
 		if(recurrence != null)
 			description += "<b>Recurrence Interval</b>: " + recurrence + "<br>";
 		if(downDipWidth != null)
 			description += "<b>Down Dip Width</b>: " + downDipWidth + "&nbsp;&nbsp; &plusmn;" + downDipWidthError + "<br>";
 		if(rupTop != null)
 			description += "<b>Rupture - Top</b>: " + rupTop + "<br>";
 		if(rupBottom != null)
 			description += "<b>Rupture - Bottom</b>: " + rupBottom + "<br>";
 		if(rake != null)
 			description += "<b>Rake</b>: " + rake + "<br>";
 		if(dip != null)
 			description += "<b>Dip</b>: " + dip + "<br>";
 		if(daz != null)
 			description += "<b>Daz</b>: " + daz + "<br>";
 		if(getTraces() != null && getTraces().size() > 0)
 			description += "<b>Location</b>: " + getTracesString() + "<br>";
 		if(comment != null)
 			description += "<b>Comment</b>: " + comment + "<br>";
 		description += "<b>Details</b>: " + BASE_URL + dataSet.getId() + "&fid=" + id + "<br>";
 		description += " <br> ";
 		description += "This fault is part of the <a href=\"" + BASE_URL + dataSet.getId() + "\">" + dataSet.getName() + "</a>.";
 		
 		String kmlTraces = "";
 		for(FaultTracePoint p : getTraces()) {
 			kmlTraces += p.getLon() + "," + p.getLat() + " ";
 		}
 		
 		String myString = "";
 		myString += "<Placemark>";
 		myString += "<name>" + name + "</name>";
 		myString += "<description><![CDATA[" + description + "]]></description>";
 		myString += "<styleUrl>" + "#" + style.getId() + "</styleUrl>";
 		myString += "<LineString>";
 		myString += "<altitudeMode>clampToGround</altitudeMode>"; //"<tessellate>1</tessellate>";
 		myString += "<coordinates>" + kmlTraces.trim() + "</coordinates>"; 
 		myString += "</LineString>";
 		myString += "</Placemark>";
 		
 		return myString;
 	}
 }
