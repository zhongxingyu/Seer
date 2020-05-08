 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package datext;
 
 import datext.util.Formatter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 /**
  *
  * @author cybergnome
  */
 public abstract class DaTextList extends DaTextVariable implements java.util.List<DaTextVariable>{
 	// TODO documentation
 	// TODO make default implementation
 	// TODO make default implementation localize its numbers
 	// TODO update parse to use the new DaText lists
 	// TODO update other classes to use the new DaText lists
 	
 
 	/**
 	 * Adds a new element to this list.
 	 * @param value The value to add to the list
 	 * @param annotation Annotation to go with this variable. Can be null.
 	 */
 	public abstract void add(String value, String annotation);
 	/**
 	 * Adds a new element to this list.
 	 * @param value The value to add to the list
 	 * @param annotation Annotation to go with this variable. Can be null.
 	 */
 	public abstract void add(byte[] value, String annotation);
 	/**
 	 * Adds a new element to this list.
 	 * @param value The value to add to the list
 	 * @param annotation Annotation to go with this variable. Can be null.
 	 */
 	public abstract void add(double value, String annotation);
 	/**
 	 * Adds a new element to this list.
 	 * @param value The value to add to the list
 	 * @param annotation Annotation to go with this variable. Can be null.
 	 */
 	public abstract void add(long value, String annotation);
 	/**
 	 * Adds a new element to this list.
 	 * @param value The value to add to the list
 	 * @param annotation Annotation to go with this variable. Can be null.
 	 */
 	public abstract void add(int value, String annotation);
 	
 	@Override public DaTextList asList(){
 		return this;
 	}
 	
 	public String getAsText(int index){
 		return this.get(index).asText();
 	}
 	
 	public long getAsLong(int index) throws UnsupportedOperationException, NumberFormatException{
 		return this.get(index).asLong();
 	}
 	public int getAsInt(int index) throws UnsupportedOperationException, NumberFormatException{
 		return this.get(index).asInt();
 	}
 	public double getAsNumber(int index) throws UnsupportedOperationException, NumberFormatException{
 		return this.get(index).asNumber();
 	}
 	public byte[] getAsBinary(int index) throws UnsupportedOperationException, NumberFormatException{
 		return this.get(index).asBinary();
 	}
 	public DaTextObject getAsObject(int index) throws UnsupportedOperationException{
 		return this.get(index).asObject();
 	}
 	
 	/**
 	 * Throws an UnsupportedOperationException because list types are not 
 	 * convertible. 
 	 */
 	@Deprecated
 	@Override
 	public int asInt() throws UnsupportedOperationException, NumberFormatException {
 		throw new UnsupportedOperationException(DaTextList.class.getSimpleName() 
 				+ " is not convertable to numerical value");
 	}
 
 	/**
 	 * Throws an UnsupportedOperationException because list types are not 
 	 * convertible. 
 	 */
 	@Deprecated
 	@Override
 	public long asLong() throws UnsupportedOperationException, NumberFormatException {
 		throw new UnsupportedOperationException(DaTextList.class.getSimpleName() 
 				+ " is not convertable to numerical value");
 	}
 
 	/**
 	 * Throws an UnsupportedOperationException because list types are not 
 	 * convertible. 
 	 */
 	@Deprecated
 	@Override
 	public double asNumber() throws UnsupportedOperationException, NumberFormatException {
 		throw new UnsupportedOperationException(DaTextList.class.getSimpleName() 
 				+ " is not convertable to numerical value");
 	}
 
 	/**
 	 * Throws an UnsupportedOperationException because list types are not 
 	 * convertible. 
 	 */
 	@Deprecated
 	@Override
 	public byte[] asBinary() throws UnsupportedOperationException, NumberFormatException {
 		throw new UnsupportedOperationException(DaTextList.class.getSimpleName() 
 				+ " is not convertable to numerical value");
 	}
 
 	/**
 	 * Throws an UnsupportedOperationException because list types are not 
 	 * convertible. 
 	 */
 	@Deprecated
 	@Override
 	public DaTextObject asObject() throws UnsupportedOperationException {
 		throw new UnsupportedOperationException(DaTextList.class.getSimpleName() 
 				+ " is not convertable to " + DaTextObject.class.getSimpleName());
 	}
 	
 	
 	/**
 	 * Throws an UnsupportedOperationException because list types are not 
 	 * convertible. 
 	 */
 	@Deprecated
 	@Override
 	public void set(String value) {
 		throw new UnsupportedOperationException("Cannot change value of "+DaTextList.class.getSimpleName()); 
 	}
 
 	/**
 	 * Throws an UnsupportedOperationException because list types are not 
 	 * convertible. 
 	 */
 	@Deprecated
 	@Override
 	public void set(int value) {
 		throw new UnsupportedOperationException("Cannot change value of "+DaTextList.class.getSimpleName()); 
 	}
 
 	/**
 	 * Throws an UnsupportedOperationException because list types are not 
 	 * convertible. 
 	 */
 	@Deprecated
 	@Override
 	public void set(long value) {
 		throw new UnsupportedOperationException("Cannot change value of "+DaTextList.class.getSimpleName()); 
 	}
 
 	/**
 	 * Throws an UnsupportedOperationException because list types are not 
 	 * convertible. 
 	 */
 	@Deprecated
 	@Override
 	public void set(double value) {
 		throw new UnsupportedOperationException("Cannot change value of "+DaTextList.class.getSimpleName()); 
 	}
 
 	/**
 	 * Throws an UnsupportedOperationException because list types are not 
 	 * convertible. 
 	 */
 	@Deprecated
 	@Override
 	public void set(byte[] value) {
 		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 	}
 
 	
 
 	/**
 	 * Throws an UnsupportedOperationException because list types are not 
 	 * convertible. 
 	 */
 	@Deprecated
 	@Override
 	public void set(DaTextObject value) {
 		throw new UnsupportedOperationException("Cannot change value of "+DaTextList.class.getSimpleName()); 
 	}
 	
 	/**
 	 * Writes this DaTextObjext as a string to the given stream writer. 
 	 * The output is valid DaText that would create an identical copy if parsed.
 	 * @param outputStream A writer to the stream.
 	 * @param doIndent If <code>true</code>, then tab characters will be 
 	 * inserted to improve human legibility.
 	 * @param indent If <code>doIndent</code> is <code>true</code>, then 
 	 * this is the number of tabs to indent (typically the toplevel DaText 
 	 * object would be written with an indent of 0)
 	 * @throws java.io.IOException Thrown if there was an error writing to the 
 	 * stream.
 	 */
 	@Override public void serialize(java.io.Writer outputStream, boolean doIndent, int indent) throws java.io.IOException{
 			readLock.lock();
 		try {
 			outputStream.write("[");
 			boolean first = true;
 			for(DaTextVariable v : this){
 				if (v != null) {
 					String annote = v.getAnnotation();
 					if (annote != null) {
 						if (doIndent) {
 							for (int i = 0; i < indent; i++) {
 								outputStream.write("\t");
 							}
 						}
 						outputStream.write(annote);
 						outputStream.write("\r\n");
 					}
 				}
 				if(doIndent){
 					for(int i = 0; i < indent; i++){
 						outputStream.write("\t");
 					}
 				}
 				if(v != null){
 					if(first){
 						first = false;
 					} else {
 						outputStream.write(", ");
 					}
 					if(v instanceof DaTextObject){
 						outputStream.write("\r\n");
 					}
					v.serialize(outputStream, doIndent, indent+1);
 				}
 				outputStream.write("]\r\n");
 			}
 		} finally {
 			readLock.unlock();
 		}
 	}
 	
 }
