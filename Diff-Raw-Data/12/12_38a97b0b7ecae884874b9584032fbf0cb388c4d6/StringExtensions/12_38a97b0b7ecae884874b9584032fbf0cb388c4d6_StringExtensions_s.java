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
 
 /**
  * <b>NOTE: TO USE THESE EXTENSIONS YOU WILL NEED <a href="http://peichhorn.github.com/lombok-pg/">lombok-pg</a> version 0.10.5+</b>
  * <br/><br/>
  * Add's {@link Validation validation} methods for {@link String} objects.
  * To use these add the following to your class:
  * 
  * <pre>@ExtensionMethod({Validation.class, StringExtensions.class})</pre>
  * 
  * @author Matthew L. Maurer maurer.it@gmail.com
  */
public class StringExtensions {
 	
 	private static final String MATCHES_ONEOF_FAIL_MSG = " did not match one of {1}";
 	
 	/**
 	 * Checks if a string matches one of the passed in strings.
 	 * 
 	 * @param thisVal
 	 * @param parameterName
 	 * @param parameter
 	 * @param valsToCheck
 	 * @return
 	 */
 	public static Validation matchesOneOf ( Validation thisVal, String parameterName, String parameter, String[] valsToCheck ) {
 		boolean matchFound = false;
 		Validation valToReturn = thisVal;
 		
 		for ( String valToCheck : valsToCheck ) {
 			if ( parameter.equalsIgnoreCase(valToCheck) ) {
 				matchFound = true;
 				//Lets not spend unneeded time in this loop
 				break;
 			}
 		}
 		
 		if ( !matchFound ) {
 			String message = MATCHES_ONEOF_FAIL_MSG.replace("{1}", Arrays.toString(valsToCheck));
 			valToReturn = thisVal.failedCheck(parameterName, parameter, message);
 		}
 		
 		return valToReturn;
 	}
 }
