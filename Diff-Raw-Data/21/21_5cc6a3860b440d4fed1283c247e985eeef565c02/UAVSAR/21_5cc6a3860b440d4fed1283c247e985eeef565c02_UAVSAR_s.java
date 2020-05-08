 package edu.usc.sirlab;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import edu.usc.sirlab.kml.*;
 
 public class UAVSAR implements Serializable {
 	//TODO: consoledate with InSAR
 	private static final long serialVersionUID = 9177366612256L;
 	private int id;
 	private String title, description;
 	private Date date1, date2;
 	private String sourceURL, metaDataURL, imageURL, kmlURL;
 	private GeoPoint reference1, reference2, reference3, reference4;
 	private List<UAVSARCategory> dataCategories;
 	
 	private SimpleDateFormat longFormat = new SimpleDateFormat("MMMMM dd, yyyy @ hh:mm:ss aaa");
 	
 	private static final String SERVER_UAVSAR_URL = "http://quaketables.quakesim.org/uavsar.jsp?uid=";
 	private static final String UAVSAR_REPO_URL = "http://gf19.ucs.indiana.edu:9898/uavsar-data/";
 	
 	public UAVSAR(int id, String title, String description, Date date1,
 			Date date2, String sourceURL, String metaDataURL, String imageURL,
 			String kmlURL, GeoPoint reference1, GeoPoint reference2,
 			GeoPoint reference3, GeoPoint reference4) {
 		this.id = id;
 		this.title = title;
 		this.description = description;
 		this.date1 = date1;
 		this.date2 = date2;
 		this.sourceURL = sourceURL;
 		this.metaDataURL = metaDataURL;
 		this.imageURL = imageURL;
 		this.kmlURL = kmlURL;
 		this.reference1 = reference1;
 		this.reference2 = reference2;
 		this.reference3 = reference3;
 		this.reference4 = reference4;
 		dataCategories = new ArrayList<UAVSARCategory>();
 	}
 	public int getId() {
 		return id;
 	}
 	
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	public String getTitle() {
 		return title;
 	}
 	
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 	
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
 	public Date getDate1() {
 		return date1;
 	}
 	
 	public void setDate1(Date date1) {
 		this.date1 = date1;
 	}
 	
 	public Date getDate2() {
 		return date2;
 	}
 	
 	public void setDate2(Date date2) {
 		this.date2 = date2;
 	}
 	
 	public String getSourceURL() {
 		return sourceURL;
 	}
 	
 	public void setSourceURL(String sourceURL) {
 		this.sourceURL = sourceURL;
 	}
 	
 	public String getMetaDataURL() {
 		return metaDataURL;
 	}
 	
 	public void setMetaDataURL(String metaDataURL) {
 		this.metaDataURL = metaDataURL;
 	}
 	
 	public String getImageURL() {
 		return imageURL;
 	}
 	
 	public void setImageURL(String imageURL) {
 		this.imageURL = imageURL;
 	}
 	
 	public String getKmlURL() {
 		return kmlURL;
 	}
 	
 	public void setKmlURL(String kmlURL) {
 		this.kmlURL = kmlURL;
 	}
 	
 	public GeoPoint getReference1() {
 		return reference1;
 	}
 	
 	public void setReference1(GeoPoint reference1) {
 		this.reference1 = reference1;
 	}
 	
 	public GeoPoint getReference2() {
 		return reference2;
 	}
 	
 	public void setReference2(GeoPoint reference2) {
 		this.reference2 = reference2;
 	}
 	
 	public GeoPoint getReference3() {
 		return reference3;
 	}
 	
 	public void setReference3(GeoPoint reference3) {
 		this.reference3 = reference3;
 	}
 	
 	public GeoPoint getReference4() {
 		return reference4;
 	}
 	
 	public void setReference4(GeoPoint reference4) {
 		this.reference4 = reference4;
 	}
 	
 	public List<UAVSARCategory> getDataCategories() {
 		return dataCategories;
 	}
 	
 	public void setDataCategories(List<UAVSARCategory> data) {
 		this.dataCategories = data;
 	}
 	
 	public void addDataCategory(UAVSARCategory data) {
 		dataCategories.add(data);
 	}
 	/*
 	public List<String[]> getHTMLParameters() {
 		List<String[]> param = new ArrayList<String[]>();
 		
 		
 		private String sourceURL, metaDataURL, imageURL, kmlURL;
 		private GeoPoint reference1, reference2, reference3, reference4;
 		private List<UAVSARCategory> dataCategories;
 		
 		if(title != null) {
 			String[] p = {"Title", title};
 			param.add(p);
 		}
 		
 		if(description != null) {
 			String[] p = {"Site Description", description};
 			param.add(p);
 		}
 		
 		if(date1 != null) {
 			String[] p = {"Time of Acquisition for Pass 1", date1.toString()};
 			param.add(p);
 		}
 		
 		if(date2 != null) {
 			String[] p = {"Time of Acquisition for Pass 2", date2.toString()};
 			param.add(p);
 		}
 		
 		
 		return param;
 	}*/
 	
 	public String getKMLFolder() {
 		return getKMLFolder(true);
 	}
 	
 	public String getKMLFolder(boolean placeOverlay) {
 		String details = getKMLDescription();
 		
 		String placemarkDetails = "<b>Source</b>: " + title + "<br>" + "<b>Details</b>: " + SERVER_UAVSAR_URL + id + "<br>" + "<b>Location</b>: ";
 		
 		/*
 		//Placemarks
 		PlacemarkSimple p1 = new PlacemarkSimple("Reference 1", placemarkDetails + reference1.toString(), PlacemarkSimple.TYPE_POINT);
 		p1.addCoordinates(reference1);
 		//p1.setStyleUrl(PlacemarkSimple.EXTERNAL_STYLE_URL + "sm_ylw-pushpin");
 		PlacemarkSimple p2 = new PlacemarkSimple("Reference 2", placemarkDetails + reference2.toString(), PlacemarkSimple.TYPE_POINT);
 		p2.addCoordinates(reference2);
 		//p2.setStyleUrl(PlacemarkSimple.EXTERNAL_STYLE_URL + "sm_ylw-pushpin");
 		PlacemarkSimple p3 = new PlacemarkSimple("Reference 3", placemarkDetails + reference3.toString(), PlacemarkSimple.TYPE_POINT);
 		p3.addCoordinates(reference3);
 		//p3.setStyleUrl(PlacemarkSimple.EXTERNAL_STYLE_URL + "sm_ylw-pushpin");
 		PlacemarkSimple p4 = new PlacemarkSimple("Reference 4", placemarkDetails + reference4.toString(), PlacemarkSimple.TYPE_POINT);
 		p4.addCoordinates(reference4);
 		//p4.setStyleUrl(PlacemarkSimple.EXTERNAL_STYLE_URL + "sm_ylw-pushpin");
 		*/
 		
 		//Polygon
 		PlacemarkSimple polygon = new PlacemarkSimple(title, details, PlacemarkSimple.TYPE_POLYGON);
 		polygon.setStyleUrl(PlacemarkSimple.EXTERNAL_STYLE_URL + "s_polygon");
 		polygon.addCoordinates(reference1);
 		polygon.addCoordinates(reference2);
 		polygon.addCoordinates(reference4);
 		polygon.addCoordinates(reference3);
 		
 		String myString = "";
 		
		/*
 		myString += "<Folder>";
		myString += "<name>Reference Points</name>";
		myString += p1.getKML();
		myString += p2.getKML();
		myString += p3.getKML();
		myString += p4.getKML();
 		myString += polygon.getKML();
 		myString += "</Folder>";
		*/
 		//Overlay
 		if(placeOverlay) {
 			String url = UAVSAR_REPO_URL + imageURL;
 			//double[] coordinates = {reference2.getLat(), reference3.getLat(), reference1.getLon(), reference4.getLon()}; // North, South, East, West Corners
 			double[] coordinates = {reference2.getLat(), reference3.getLat(), reference4.getLon(), reference1.getLon()}; // North, South, East, West Corners
 			Overlay o = new Overlay(title, details, url, coordinates);
 			
 			myString += "<Folder>";
 			myString += "<name>InSAR Image Overlay</name>";
 			myString += o.getKML();
 			myString += "</Folder>";
 		}
 		
 		return myString;
 	}
 	
 	private String getKMLDescription() {
 		String details = "<b>RPI Product Information</b>";
 		details += "<b>Site Description</b>:" + description + "<br>";
 		details += "<b>Time of Acquisition for Pass 1</b>:" + longFormat.format(getDate1()) + "<br>";
 		details += "<b>Time of Acquisition for Pass 2</b>:" + longFormat.format(getDate2()) + "<br>";
 		details += "<b>Location</b>: " + reference1.toString() + ", " + reference2.toString() + ", " + reference3.toString() + ", " + reference4.toString() + "<br>";
		details += "<b>Links</b>:" + "<a href=\"" + UAVSAR_REPO_URL + getMetaDataURL() + "\" title=\"Metadata for Interferogram\">[Meta Data]</a>, <a href=\"" + UAVSAR_REPO_URL + getImageURL() + "\" title=\"Interferogram URL\">[Thumbnail]</a>, <a href=\"" + UAVSAR_REPO_URL + getKmlURL() + "\" title=\"Low Resolution KML File\">[KML]</a>" + "<br>";
 		details += "<b>Details</b>: " + "<a href=\"" + SERVER_UAVSAR_URL + id + "\">" + SERVER_UAVSAR_URL + id + "</a><br>";
 		details += "<br><br>";
 		
 		List<UAVSARCategory> categories = getDataCategories();
 		for(UAVSARCategory cat : categories) {
 			details += "<b>" + cat.getName() + "</b>";
 			List<UAVSARDataItem> items =  cat.getDataItems();
 			for(UAVSARDataItem i : items) {
 				details += "<b>" + i.getName() + "</b>: ";
 				if(i.getUrl() != null)
 					details += "<a href=\"" + UAVSAR_REPO_URL + i.getUrl() + "\" title=\"Download Data File\">[Data]</a> ";
 				if(i.getVisualizationURL() != null)
 					details += "<a href=\"" + UAVSAR_REPO_URL + i.getVisualizationURL() + "\" title=\"GoogleEarth KMZ File\">[KMZ]</a> ";
 				if(i.getVisualizationPreviewURL() != null)
 					details += "<a href=\"" + UAVSAR_REPO_URL + i.getVisualizationPreviewURL() + "\" title=\"Low Resolution KML File\">[KML]</a>";
 				
 				details += "<br>";
 			}
 			details += "<br>";
 		}
 		
 		details += "<b>Source</b>:" + "<a href=\"" + getSourceURL() + "\" title=\"JPL UAVSAR Project RPI Project Page\">JPL UAVSAR Project</a>" + "<br>";
 		
 		return details;
 	}
 	
 	public String getKMLPlacemark() {
 		String details = getKMLDescription();
 		
 		GeoPoint a = reference1;
 		GeoPoint b = reference4;
 		
 		double lat = (a.getLat() + b.getLat()) / 2.0;
 		double lon = (a.getLon() + b.getLon()) / 2.0;
 		GeoPoint p = new GeoPoint(lat, lon);
 		
 		String myString = "";
 		myString += "<Placemark>";
 		myString += "<name>" + title + "</name>";
 		//TODO: FIX THE URL
 		myString += "<styleUrl>" + "http://gf19.ucs.indiana.edu:9898/maps/styles.kml#s_dot" + "</styleUrl>";
 		myString += "<description><![CDATA[" + details + "]]></description>";
 		myString += "<Point>";
 		myString += "<coordinates>" + p.getKMLCoordinateString() + "</coordinates>";
 		myString += "</Point>";
 		myString += "</Placemark>";
 		
 		return myString;
 	}
 }
