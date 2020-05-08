 /**  
  * L - the light-weight Java logging utility designed for brevity and simplicity.
  * Copyright (C) 2012 Ajay Gopinath
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
  * 
  * See the GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
 
 package com.agopinath.lthelogutil;
 
 import java.util.Iterator;
 
 import com.agopinath.lthelogutil.streams.LConsoleStream;
 import com.agopinath.lthelogutil.streams.LStream;
 
 /**
  * Provides logging functionality designed with very brief method calls
  * to prevent unnecessary verbosity, and takes care of otherwise tedious
  * code to log in a variety of different ways. Instantiation
  * of this class is prevented because it is inconsistent
  * with the program design. This class is thread-safe and handles
  * any possible concurrency issues internally. For a faster, on-the-fly
  * general-purpose implementation, using <code>Fl</code> is recommended.
  * 
  * @author Ajay
  *
  */
 public final class L {
 	private static final LStreamSet STREAMS;
 	
 	// prevent instantiation
 	private L() {}
 	
 	static {
 		STREAMS = new LStreamSet(5);
 		STREAMS.addLStream(LConsoleStream.getInstance());
 	}
 	
 	/**
 	 * Registers a new <code>LStream</code> with the specified
 	 * name and adds it to the set of <code>LStream</code>s to
 	 * log to.
 	 * @param newLStream - new <code>LStream</code> to add.
 	 * @param newLStreamID - name of the new <code>LStream</code>.
 	 */
 	public static final void addLStream(LStream newLStream, String newLStreamID) {
 		newLStream.setLStreamID(newLStreamID);
 		STREAMS.addLStream(newLStream);
 	}
 	
 	/**
 	 * De-registers the <code>LStream</code> with the specified
 	 * name and removes it from the set of <code>LStream</code>s to
 	 * log to.
 	 * @param lStreamID - name of the <code>LStream</code> to remove.
 	 */
 	public static final void removeLStream(String lStreamID) {
 		LStream lStreamToRemove = STREAMS.getLStreamByName(lStreamID);
 		lStreamToRemove.streamClose();
 		STREAMS.removeLStream(lStreamID);
 	}
 	
 	/**
 	 * Logs the given String to the registered <code>LStream</code>s
 	 * @param toLog - the String to be logged.
 	 * @return The same String that was logged.
 	 */
 	public static final String og(final String toLog) {
 		Iterator<LStream> lStreamsIt = STREAMS.getLStreamIterator();
 		
 		synchronized(lStreamsIt) {
 			for(;lStreamsIt.hasNext(); ) {
 				LStream currStream = lStreamsIt.next();
 				currStream.streamWrite(toLog);
 			}
 		}
 		
 		return toLog;
 	}
 	
 	/**
 	 * Logs the given String to the registered <code>LStream</code>s
 	 * Differs from the method <code>og</code> in that
 	 * "ERROR: " is appended to the beginning of the text, and should
 	 * be used to debug errors.
 	 * @param toLog - the String to be logged.
 	 * @return The same String that was logged.
 	 */
 	public static final String err(final String toLog) {
 		Iterator<LStream> lStreamsIt = STREAMS.getLStreamIterator();
 		
 		synchronized(lStreamsIt) {
 			for(;lStreamsIt.hasNext(); ) {
 				LStream currStream = lStreamsIt.next();
 				currStream.streamWrite("ERROR: " + toLog);
 			}
 		}
 		
 		return toLog;
 	}
 	
 	/**
 	 * Logs the given String to the registered <code>LStream</code>s
 	 * Differs from the method <code>og</code> in that
 	 * "DEBUG: " is appended to the beginning of the text, and should
 	 * be used for general command-debugging.
 	 * @param toLog - the String to be logged.
 	 * @return The same String that was logged.
 	 */
 	public static final String dbg(final String toLog) {
 		Iterator<LStream> lStreamsIt = STREAMS.getLStreamIterator();
 		
 		synchronized(lStreamsIt) {
 			for(;lStreamsIt.hasNext(); ) {
 				LStream currStream = lStreamsIt.next();
 				currStream.streamWrite("DEBUG: " + toLog);
 			}
 		}
 		
 		return toLog;
 	}
 }
