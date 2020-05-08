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
  * Add's {@link Validation validation} methods for {@link List} objects.
  * To use these add the following to your class:
  * 
 * <pre>@ExtensionMethod({Validation.class, ValidationExtensions.class})</pre>
  * 
  * @author Matthew L. Maurer maurer.it@gmail.com
  */
 public class ListExtensions {
 
 	private static String CONTAINS_CHECK_FAIL_MSG = "{0} did not contain one of {1}";
 	
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
 		
 		for ( Object currentContain : shouldContain ) {
 			if ( list.contains(currentContain) ) {
 				foundAMatch = true;
 			}
 		}
 		
 		if ( foundAMatch ) {
 			return thisVal;
 		}
 		else {
 			String message = CONTAINS_CHECK_FAIL_MSG.replace("{0}", list.toString()).replace("{1}", Arrays.toString(shouldContain));
 			return thisVal.failedCheck(parameterName, list, message);
 		}
 	}
 }
