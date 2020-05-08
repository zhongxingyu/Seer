 /** 
  * Copyright 2010 Douglas Linder.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package twisted.client;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.client.Window;
 
 /** Logs messages. */
 public class ComponentLog {
 	
 	/** Possible debug levels */
	public enum DebugLevel { 
 		
 		/** Keep last MAX_LOG_SIZE messages. */
 		KEEP,
 		
 		/** Print debug messages out at the end of the page in hidden divs. */
 		APPEND,
 		
 		/** Alert exceptions. */
 		ALERT,
 		
 		/** Pass exceptions upwards. */
 		RAISE
 	};
 	
 	/** Debug level: Do everything. */
 	public static final EnumSet<DebugLevel> DEBUG_ALL = EnumSet.allOf(DebugLevel.class);
 	
 	/** Maximum size of messages to keep. */
 	private static final int MAX_LOG_SIZE = 50;
 	
 	/** The log. */
 	private static ArrayList<String> log = null;
 	
 	/** The current debug level. */
 	private static EnumSet<DebugLevel> debugLevel = EnumSet.noneOf(DebugLevel.class);
 	
 	/** 
 	 * Change debug level.
 	 * <p>
 	 * You can create an EnumSet using EnumSet.of, like this:<br/>
 	 * EnumSet.of(DebugLevel.ALERT, DebugLevel.KEEP, DebugLevel.RAISE);
 	 */
 	public static void setDebugLevel(EnumSet<DebugLevel> level) {
 		debugLevel = level;
 		if (debugLevel.contains(DebugLevel.KEEP) && (log == null))
 			log = new ArrayList<String>();
 		else
 			log = null;
 	}
 	
 	/** Logs a message. */
 	public static void trace(String msg) {
 		if (debugLevel.contains(DebugLevel.KEEP)) {
 			if (log.size() == MAX_LOG_SIZE) 
 				log.remove(0);
 			log.add(msg);
 			GWT.log(msg, null);
 			if (debugLevel.contains(DebugLevel.APPEND)) {
 				Element e = Document.get().createDivElement();
 				e.setInnerText(msg);
 				e.setClassName("ComponentLog");
 				Document.get().getBody().appendChild(e);
 			}
 		}
 	}
 	
 	/** Processes an exception with no listener. */
 	public static void exception(Throwable e) {
 		ComponentLog.trace(e.toString());
 		if (debugLevel.contains(DebugLevel.ALERT))
 			Window.alert(e.toString());
 		if (debugLevel.contains(DebugLevel.RAISE))
 			throwException(e.toString());
 	}
 	
 	/** Really, really throws an exception. */
 	private static native void throwException(String error) /*-{
 		throw(error);
 	}-*/;
 }
