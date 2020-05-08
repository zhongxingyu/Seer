 /*
  * Original Code Copyright Prime Technology.
  * Subsequent Code Modifications Copyright 2011-2012 ICEsoft Technologies Canada Corp. (c)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * NOTE THIS CODE HAS BEEN MODIFIED FROM ORIGINAL FORM
  *
  * Subsequent Code Modifications have been made and contributed by ICEsoft Technologies Canada Corp. (c).
  *
  * Code Modification 1: Integrated with ICEfaces Advanced Component Environment.
  * Contributors: ICEsoft Technologies Canada Corp. (c)
  *
  * Code Modification 2: [ADD BRIEF DESCRIPTION HERE]
  * Contributors: ______________________
  * Contributors: ______________________
  */
 package org.icefaces.ace.component.datetimeentry;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.faces.FacesException;
 import javax.faces.context.FacesContext;
 
 /**
  * Utility class for calendar component
  */
 public class DateTimeEntryUtils {
 
 	public static String getValueAsString(FacesContext facesContext, DateTimeEntry dateTimeEntry) {
 		Object submittedValue = dateTimeEntry.getSubmittedValue();
 		if(submittedValue != null) {
 			return submittedValue.toString();
 		}
 		
 		Object value = dateTimeEntry.getValue();
 		if(value == null) {
 			return null;
 		} else {
 			//first ask the converter
 			if(dateTimeEntry.getConverter() != null) {
 				return dateTimeEntry.getConverter().getAsString(facesContext, dateTimeEntry, value);
 			}
 			//Use built-in converter
 			else {
 				SimpleDateFormat dateFormat = new SimpleDateFormat(dateTimeEntry.getPattern(), dateTimeEntry.calculateLocale(facesContext));
 				dateFormat.setTimeZone(dateTimeEntry.calculateTimeZone());
 				
 				return dateFormat.format(value);
 			}
 		}
 	}
 	
 	public static String getDateAsString(DateTimeEntry dateTimeEntry, Object date) {
 		if(date == null) {
 			return null;
 		}
 		
 		if(date instanceof String){
 			return (String) date;
 		} else if(date instanceof Date) {
 			SimpleDateFormat dateFormat = new SimpleDateFormat(dateTimeEntry.getPattern(), dateTimeEntry.calculateLocale(FacesContext.getCurrentInstance()));
 			dateFormat.setTimeZone(dateTimeEntry.calculateTimeZone());
 			
 			return dateFormat.format((Date) date);
 		} else {
 			throw new FacesException("Date could be either String or java.util.Date");
 		}
 	}
 		
 	/**
 	 * Converts a java date pattern to a jquery date pattern
 	 * 
 	 * @param pattern Pattern to be converted
 	 * @return converted pattern
 	 */
 	public static String convertPattern(String pattern) {
 		if(pattern == null)
 			return null;
 		else {
 			//year
 			pattern = pattern.replaceAll("yy", "y");
 			
 			//month
 			if(pattern.indexOf("MMM") != -1)
 				pattern = pattern.replaceAll("MMM", "M");
 			else
 				pattern = pattern.replaceAll("M", "m");
 			
 			//day of week
 			pattern = pattern.replaceAll("EEE", "D");
 
             //time
             if(pattern.indexOf("H") != -1 || pattern.indexOf("h") != -1) {
                pattern = pattern.replaceAll("H", "h").replaceAll("a", "TT");
             }
 			
 			return pattern;
 		}
 	}
 }
