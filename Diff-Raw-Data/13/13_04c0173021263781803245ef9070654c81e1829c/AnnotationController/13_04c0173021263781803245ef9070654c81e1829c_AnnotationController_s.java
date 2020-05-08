 /**
  * AnnotationController.java created on 19.07.2012
  */
 
 package gst.data;
 
 import gst.test.Debug;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.jfree.data.xy.XYSeries;
 import org.unisens.Entry;
 import org.unisens.Event;
 import org.unisens.EventEntry;
 
 /**
  * Buffered {@link gst.data.DataController} implementation for {@code EventEntry}-type data in an {@link gst.data.UnisensDataset}.
  * @author Enrico Grunitz
 * @version 0.2.0 (06.08.2012)
  * @see gst.data.DataController
  */
 public class AnnotationController extends DataController {
 	/** sample number of the first event */						private long startSampleNumber;
 	/** sample number of the last event */						private long endSampleNumber;
 	/** {@code ArrayList} of all {@code Event}s */				private ArrayList<Event> buffer;
 	/** exists the file? */										private boolean fileExists;
 
 	/**
 	 * Creates the {@code AnnotationController} object for the given {@code EventEntry}.
 	 * @param entry the EventEntry
 	 */
 	public AnnotationController(EventEntry entry) {
 		super(entry);
 		this.buffer = new ArrayList<Event>();
 		// check if the file exists
 		String fileName = this.entry.getUnisens().getPath();
 		if(fileName.endsWith("unisens.xml")) {
 			fileName.substring(0, fileName.length() - 11);
 		}
 		fileName += this.entry.getId();
 		File testFile = new File(fileName);
 		this.fileExists = testFile.exists(); 
 		if(this.fileExists) {
 			this.fillBuffer();
 		}
 		this.initBorderSampleNumbers();
 		this.isBuffered = true;
 	}
 	
 	/**
 	 * addAnnotatioin implementation
 	 * @param time
 	 * @param type
 	 * @param comment
 	 */
 	public void addAnnotation(double time, String type, String comment) {
 		if(type == null) {
 			type = "";
 		}
 		if(comment == null) {
 			comment = "";
 		}
 		long sampleStamp = (long)Math.ceil((time - this.basetime) * ((EventEntry)this.entry).getSampleRate());
 		this.buffer.add(new Event(sampleStamp, type, comment));
 	}
 	
 	public AnnotationList getAnnotation(double time) {
 		long sampleStamp = (long)Math.round((time - this.basetime) * ((EventEntry)this.entry).getSampleRate());
 		ArrayList<Event> events = new ArrayList<Event>(1);
 		Iterator<Event> it = buffer.iterator();
 		while(it.hasNext()) {
 			Event event = it.next();
 			if(event.getSampleStamp() == sampleStamp) {
 				events.add(event);
 			}
 		}
 		Debug.println(Debug.annotationController, "found " + events.size() + " events");
 		return new AnnotationList(events, this.basetime, ((EventEntry)this.entry).getSampleRate());
 	}
 	
 	public void removeAnnotation(AnnotationList annoList) {
 		for(int i = 0; i < annoList.size(); i++) {
 			if(this.buffer.remove(annoList.getEvent(i))) {
 				Debug.println(Debug.annotationController, "item successful removed");
 			} else {
 				Debug.println(Debug.annotationController, "item NOT removed");
 			}
 			
 		}
 	}
 	
 	/**
 	 * Reads data from entry and adds them to the buffer. Doesn't check for existing file.
 	 */
 	private void fillBuffer() {
 		try {
 			buffer.addAll(((EventEntry)this.entry).read(0, (int)((EventEntry)this.entry).getCount()));
 		} catch(IOException ioe) {
 			Debug.println(Debug.annotationController, "IOException while filling buffer");
 		}
 	}
 	
 	/**
 	 * Adds reasonable data to the fields {@link #startSampleNumber} and {@link #endSampleNumber}.
 	 */
 	private void initBorderSampleNumbers() {
 		switch(this.buffer.size()) {
 		case 0:
 			this.startSampleNumber = 0;
 			this.endSampleNumber = 0;
 			break;
 		case 1:
 			this.startSampleNumber = this.buffer.get(0).getSampleStamp();
 			this.endSampleNumber = this.startSampleNumber;
 			break;
 		default:
 			Iterator<Event> it = this.buffer.iterator();
 			this.startSampleNumber = it.next().getSampleStamp();
 			long temp = it.next().getSampleStamp();
 			if(temp >= this.startSampleNumber) {
 				this.endSampleNumber = temp;
 			} else {
 				this.endSampleNumber = this.startSampleNumber;
 				this.startSampleNumber = temp;
 			}
 			while(it.hasNext()) {
 				temp = it.next().getSampleStamp();
 				if(temp < this.startSampleNumber) {
 					this.startSampleNumber = temp;
 				} else if(temp > this.endSampleNumber) {
 					this.endSampleNumber = temp;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * @see gst.data.DataController#getAnnotations(double, double)
 	 */
 	@Override
 	public AnnotationList getAnnotations(double startTime, double endTime) {
 		if(endTime < startTime) {
 			// i can handle negative time-spans
 			double temp = startTime;
 			startTime = endTime;
 			endTime = temp;
 		}
 		// calculate sample numbers from times
 		long startSample = (long)Math.ceil((startTime - this.basetime) * ((EventEntry)this.entry).getSampleRate());
 		long endSample = (long)Math.round((endTime - this.basetime) * ((EventEntry)this.entry).getSampleRate());
 		// create list of interesting events
 		Iterator<Event> it = this.buffer.iterator();
 		ArrayList<Event> events = new ArrayList<Event>();
 		Event event = null;
 		while(it.hasNext()) {
 			event = it.next();
 			if(isInside(event.getSampleStamp(), startSample, endSample)) {
 				events.add(event);
 			}
 		}
 		return new AnnotationList(events, this.basetime, ((EventEntry)this.entry).getSampleRate());
 	}
 
 	/**
 	 * @see gst.data.DataController#getDataPoints(double, double, int)
 	 */
 	@Override
 	public XYSeries getDataPoints(double startTime, double endTime, int maxPoints) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * @see gst.data.DataController#getMaxX()
 	 */
 	@Override
 	public double getMaxX() {
 		return this.basetime + (this.endSampleNumber / ((EventEntry)this.entry).getSampleRate());
 	}
 
 	/**
 	 * @see gst.data.DataController#getMinX()
 	 */
 	@Override
 	public double getMinX() {
 		return this.basetime + (this.startSampleNumber / ((EventEntry)this.entry).getSampleRate());
 	}
 
 	/**
 	 * @see gst.data.DataController#getPhysicalUnit()
 	 */
 	@Override
 	public String getPhysicalUnit() {
 		return "";		// annotation do not have any physical units (hopefully)
 	}
 	
 	/**
 	 * Clears the data from the {@code Entry} and appends the whole buffer.
 	 * @see gst.data.DataController#saveImpl()
 	 */
 	@Override
 	protected void saveImpl() {
 		try {
 			((EventEntry)this.entry).empty();
 			((EventEntry)this.entry).append(this.buffer);
 		} catch(IOException ioe) {
 			Debug.println(Debug.annotationController, "IOException while saving");
 		}
 	}
 	
 	private static boolean isInside(long test, long low, long high) {
 		return (test >= low && test <= high);
 	}
 }
