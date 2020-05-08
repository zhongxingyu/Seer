 package de.hpi.fgis.json;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 import com.mongodb.DBObject;
 import com.mongodb.util.JSON;
 
 /**
  * utility to read a stream of tweet objects and returns single objects (all objects have to be split by newline(s))
  * @author tongr
  *
  */
 public class DBObjectStreamIterator implements Iterator<DBObject> {
 	private BufferedReader reader;
 	private DBObject next;
 	
 	public DBObjectStreamIterator(BufferedReader reader) {
 		this.reader = reader;
 		
 		setNext();
 	}
	private void setNext() {
 		next = null;
 		// do all the magic
 		if(reader!=null) {
 			StringBuilder objTxt = new StringBuilder();
 			String line;
 			
 			do {
 				try {
 					line = reader.readLine();
 				} catch (IOException e) {
 					line = null;
 					reader = null;
 				}
 				// ignore "undefined" lines
 				if(line!=null && !line.trim().equalsIgnoreCase("undefined")) {
 					objTxt.append(line).append('\n');
 					
 					try {
 						next = (DBObject) JSON.parse(objTxt.toString());
 					} catch (RuntimeException e) {
 						// nothing to do -> try to parse an object including the next line
 					}
 				}
 			} while (next==null && line!=null);
 		}
 	}
 
 	@Override
 	public DBObject next() {
		DBObject current;
		synchronized (this) {
			current = next;
			if(current==null) {
				throw new NoSuchElementException();
			}
			
			setNext();	
 		}
 		return current;
 	}
 
 	@Override
 	public boolean hasNext() {
 		return next!=null;
 	}
 
 	@Override
 	public void remove() {
 		throw new IllegalStateException();
 	}
 	
 }
