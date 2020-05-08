 /**
  *  Copyright 2008 - 2011
  *            Matthew L. Maurer maurer.it@gmail.com
  *  
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *  
  *       http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package net.maurerit.validation;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * <b>NOTE: TO USE THESE EXTENSIONS YOU WILL NEED <a href="http://peichhorn.github.com/lombok-pg/">lombok-pg</a> version 0.10.5+</b>
  * <br/><br/>
  * Add's {@link Validation validation} methods for {@link List} objects.
  * To use these add the following to your class:
  * 
  * <pre>@ExtensionMethod({Validation.class, ListExtensions.class})</pre>
  * 
  * @author Matthew L. Maurer maurer.it@gmail.com
  */
 public final class ListExtensions {
 
 	private static final String CONTAINS_CHKFAIL_MSG = "{0} did not contain one of {1}";
 	
	private ListExtensions ( ) { }
	
 	/**
 	 * Checks a {@link List} to see if it contains something that is in the varargs array.
 	 * 
 	 * @param thisVal
 	 * @param parameterName
 	 * @param list
 	 * @param shouldContain
 	 * @return
 	 */
 	public static <T> Validation containsOneOf ( Validation thisVal, String parameterName, List<T> list, T... shouldContain ) {
 		boolean foundAMatch = false;
 		Validation valToReturn = thisVal;
 		
 		for ( Object currentContain : shouldContain ) {
 			if ( list.contains(currentContain) ) {
 				foundAMatch = true;
 				//Lets not spend unneeded time in this loop
 				break;
 			}
 		}
 		
 		if ( !foundAMatch ) {
 			String message = CONTAINS_CHKFAIL_MSG.replace("{0}", list.toString()).replace("{1}", Arrays.toString(shouldContain));
 			valToReturn = thisVal.failedCheck(parameterName, list, message);
 		}
 		
 		return valToReturn;
 	}
 }
