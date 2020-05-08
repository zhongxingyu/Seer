 package com.delin.speedlogger;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
  
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import android.location.Location;
 import android.os.Environment;
 
 public class GPXSerializer {
 	static final String GPX_STR =			"gpx";
 	static final String VERSION_STR =		"version";
 	static final String VERSION_VALUE =		"1.1";
 	static final String CREATOR_STR =		"creator";
 	static final String CREATOR_VALUE =		"SpeedLogger";
 	static final String METADATA_STR =		"metadata";
 	static final String TRACK_STR = 		"trk";
 	static final String TRKSEG_STR = 		"trkseg";
 	static final String NAME_STR =			"name";
 	static final String TRKPOINT_STR = 		"trkpt";
 	static final String LATITUDE = 			"lat";
 	static final String LONGITUDE = 		"lon";
 	static final String ALTITUDE = 			"ele";
 	static final String TIME = 				"time";
 	static final String SATNUMBER = 		"sat";
 	// non standard, used for internal purposes
 	static final String SPEED = 			"speed";
 	static final String BEARING = 			"bear";
 	static final String ACCURACY = 			"acc";
 	
 	static final String TIMEPATTERN = 		"yyyy-MM-dd'T'HH:mm:ss'Z'";
 	static final String TIMEPATTERN_FILE = 	"yyyy-MM-dd_HH-mm-ss";
 	static final String FILE_EXTENSION = 	".gpx";
 	
 	static final long SEGMENT_TIME_INTERVAL=5000; // milliseconds
 	
 	boolean mStopped=false;
 	boolean mWriteMode;
 	
 	String mFilename = null;
 	DocumentBuilderFactory docFactory = null;
 	DocumentBuilder docBuilder = null;
 	SimpleDateFormat mDateFormat = null;
 	long mLastAddedLocTime;
 	int mGPSFixNumber = 0;
 	
 	// xml objects
 	Document mDoc = null;
 	Element mRootElement = null;
 	Element mTrack = null;
 	Element mTrackSegment = null;
 	NodeList mList; // location list from file
 	
 	public GPXSerializer() {
 		mWriteMode= true; // only write allowed without valid filename
 		SimpleDateFormat dateFormat = new SimpleDateFormat(TIMEPATTERN_FILE);
 		String filename = Environment.getExternalStorageDirectory().getPath()+"/"+CREATOR_VALUE;
 		mLastAddedLocTime=0;
 		
 		File dir = new File(filename); // create app directory
 		dir.mkdir();
 		
 		filename+=("/"+dateFormat.format(new Date()));
 		filename+=FILE_EXTENSION;
 		Initialize(filename);
 	}
 	
 	public GPXSerializer(final String filename, boolean write) {
 		mWriteMode = write;
 		Initialize(filename);
 	}
 	
 	public Location GetFix() {
 		Location loc = new Location("test"); // TODO
 		if (mGPSFixNumber==mList.getLength()) mGPSFixNumber=0;
 		Node nNode = mList.item(mGPSFixNumber++);
 		//System.out.println("\nCurrent Element :" + nNode.getNodeName());		 
 		if (nNode.getNodeType() == Node.ELEMENT_NODE) {		 
 			Element eElement = (Element) nNode;
 			loc.setLatitude(Float.parseFloat(eElement.getAttribute(LATITUDE))); // we assume lat/lon is always with us
 			loc.setLongitude(Float.parseFloat(eElement.getAttribute(LONGITUDE)));
 			loc.setSpeed(Float.parseFloat(eElement.getElementsByTagName(SPEED).item(0).getTextContent()));
 			loc.setAltitude(Float.parseFloat(eElement.getElementsByTagName(ALTITUDE).item(0).getTextContent()));
 			loc.setAccuracy(Float.parseFloat(eElement.getElementsByTagName(ACCURACY).item(0).getTextContent()));			
 			loc.setBearing(Float.parseFloat(eElement.getElementsByTagName(BEARING).item(0).getTextContent()));
			try {
				loc.setTime(mDateFormat.parse(eElement.getElementsByTagName(TIME).item(0).getTextContent()).getTime());
			} catch (Exception e) {
				// that's why I don't like exceptions
				e.printStackTrace();
			}		
 		}
 		return loc;
 	}
 	
 	public Location GetDummyFix() {
 		Location loc = new Location("test"); // TODO
 		loc.setLatitude(5.); // we assume lat/lon is always with us
 		loc.setLongitude(7.);
 		loc.setSpeed((float) 4.);
 		loc.setAltitude(2.);
 		loc.setAccuracy((float) 12.);			
 		loc.setBearing((float) 6.);
 		return loc;
 	}
 	
 	public void AddFix(Location loc) {
 		// compare times of this fix and last added, insert new segment if needed
 		if (loc.getTime()-mLastAddedLocTime>SEGMENT_TIME_INTERVAL) {
 			NewSegment();
 		}
 		
 		Attr attr=null;
 		Text secondText;
 		Element second;
 		Element point = mDoc.createElement(TRKPOINT_STR); // point
 		
 		// add lat/lon
 		attr = mDoc.createAttribute(LATITUDE);
 		attr.setValue(Double.toString(loc.getLatitude()));
 		point.setAttributeNode(attr);
 		attr = mDoc.createAttribute(LONGITUDE);
 		attr.setValue(Double.toString(loc.getLongitude()));
 		point.setAttributeNode(attr);		
 		second = mDoc.createElement(TIME); //add time
 		secondText = mDoc.createTextNode(mDateFormat.format(new Date(loc.getTime())));
 		second.appendChild(secondText);
 		point.appendChild(second);
 		
 		// add optional parameters
 		if (loc.hasAltitude()) { // add altitude if available
 			second = mDoc.createElement(ALTITUDE); //add time
 			secondText = mDoc.createTextNode(Double.toString(loc.getAltitude()));
 			second.appendChild(secondText);
 			point.appendChild(second);
 		}		
 		if (loc.hasSpeed()) { // add speed if available
 			second = mDoc.createElement(SPEED);
 			secondText = mDoc.createTextNode(Float.toString(loc.getSpeed()));
 			second.appendChild(secondText);
 			point.appendChild(second);
 		}
 		if (loc.hasBearing()) {
 			second = mDoc.createElement(BEARING);
 			secondText = mDoc.createTextNode(Float.toString(loc.getBearing()));
 			second.appendChild(secondText);
 			point.appendChild(second);
 		}
 		if (loc.hasAccuracy()) {
 			second = mDoc.createElement(ACCURACY);
 			secondText = mDoc.createTextNode(Float.toString(loc.getAccuracy()));
 			second.appendChild(secondText);
 			point.appendChild(second);
 		}
 		mTrackSegment.appendChild(point); // attach point to segment
 		mLastAddedLocTime = loc.getTime();
 	}
 	
 	private void NewDocument() {
 		Attr attr;
 		// root element
 		mDoc = docBuilder.newDocument();
 		mRootElement = mDoc.createElement(GPX_STR);
 		mDoc.appendChild(mRootElement);
 		
 		attr = mDoc.createAttribute(VERSION_STR);
 		attr.setValue(VERSION_VALUE);
 		mRootElement.setAttributeNode(attr);
 		attr = mDoc.createAttribute(CREATOR_STR);
 		attr.setValue(CREATOR_VALUE);
 		mRootElement.setAttributeNode(attr);		
 		// start new Track
 		NewTrack();	
 	}
 	
 	public void NewSegment() {
 		mTrackSegment = mDoc.createElement(TRKSEG_STR);
 		mTrack.appendChild(mTrackSegment);
 	}
 	
 	public void NewTrack() {
 		mTrack = mDoc.createElement(TRACK_STR);
 		mRootElement.appendChild(mTrack);
 		NewSegment();
 	}
 	
 	public void Stop() {
 		if (!mStopped) { // once stopped do nothing
 			mStopped=true;
 			// write the content into xml file
 			File mFile = new File(mFilename);
 			try {
 				// if file doesn't exists, then create it
 				if (!mFile.exists()) {
 					mFile.createNewFile();
 				}
 			}
 			catch(Exception e) {
 				return;
 			}
 			try {
 				TransformerFactory transformerFactory = TransformerFactory.newInstance();
 				Transformer transformer = transformerFactory.newTransformer();
 				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 				DOMSource source = new DOMSource(mDoc);
 				StreamResult result = new StreamResult(mFile);
 				transformer.transform(source, result);
 				System.out.println("File saved!");
 			}
 			catch(TransformerException tfe) {
 				tfe.printStackTrace();
 			}
 		}
 	}
 	
 	protected void finalize () {
 		Stop();
 	}
 	
 	private void Initialize(final String filename) {
 		mDateFormat = new SimpleDateFormat(TIMEPATTERN);
 		mFilename = filename;
 		docFactory = DocumentBuilderFactory.newInstance();
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 		if (mWriteMode)	NewDocument();
 		else PrepareToRead(filename);
 	}	
 
 	private void PrepareToRead(final String filename) {
 		try {
 			mDoc = docBuilder.parse(new File(filename));
 			mList = mDoc.getElementsByTagName(TRKPOINT_STR);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
