 /* SVN FILE: $Id: ValidateDateOfBirth.java 5340 2012-09-27 14:48:52Z jvuccolo $ */
 /**
  * This class provides an implementation of functions that perform validation of date of birth information.
  * 
  * Copyright 2012 The Pennsylvania State University
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * @package edu.psu.iam.cpr.core.util
  * @author $Author: jvuccolo $
  * @version $Rev: 5340 $
  * @lastrevision $Date: 2012-09-27 10:48:52 -0400 (Thu, 27 Sep 2012) $
  */
 package edu.psu.iam.cpr.core.database.tables.validate;
 
 import java.text.ParseException;
 
import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.joda.time.Years;
 
 import edu.psu.iam.cpr.core.database.tables.DateOfBirthTable;
 import edu.psu.iam.cpr.core.error.CprException;
 import edu.psu.iam.cpr.core.error.ReturnType;
 import edu.psu.iam.cpr.core.util.Validate;
 
 /**
  * This class provides an implementation of functions that perform validation of date of birth information.
  * 
  * Copyright 2012 The Pennsylvania State University
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * @package edu.psu.iam.cpr.core.util
  * @author $Author: jvuccolo $
  * @version $Rev: 5340 $
  * @lastrevision $Date: 2012-09-27 10:48:52 -0400 (Thu, 27 Sep 2012) $
  */
@SuppressWarnings("deprecation")
 public final class ValidateDateOfBirth {
 	
 	private static final int MONTH_DAY_ONLY = 2;
 	private static final int FULL_DOB = 3;
 	private static final int ZERO_YEARS_OLD = 0;
 	private static final int ONE_FIVE_ZERO_YEARS_OLD = 150;
 
 	/**
 	 * Constructor.
 	 */
 	private ValidateDateOfBirth() {
 	}
 	
 	/**
 	 * This routine is used to validate the parameters for an add date of birth request.  
 	 * @param personId contains the person identifier within the CPR.
 	 * @param dateOfBirth contains the date of birth being validated.
 	 * @param updatedBy contains the userid that requested the DOB to be added.
 	 * @return will return upon success a DateOfBirthTable object containing the validated information.
 	 * @throws CprException 
 	 * @throws ParseException 
 	 */
 	public static DateOfBirthTable validateAddDateOfBirthParameters(final long personId, final String dateOfBirth, 
 			final String updatedBy) throws CprException, ParseException  {
 		
 		String localDateOfBirth = ValidateHelper.checkField(null, dateOfBirth, null, "Date of birth", false);
 		String localUpdatedBy = ValidateHelper.checkField(null, updatedBy, null, "Updated by", false);
 		
 		// If they happen to enter a date of birth with dashes replace it with slashes.
 		localDateOfBirth = localDateOfBirth.replace('-', '/');
 		
 		// Validate the date of birth.
 		if (! isDateOfBirthValid(localDateOfBirth)) {
 			throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, "Date of birth");			
 		}
 		
 		// Create a new date of birth object.
 		DateOfBirthTable dobTable = new DateOfBirthTable(personId, localDateOfBirth, localUpdatedBy);
 		
 		return dobTable;
 	}
 	
 	/**
 	 * This routine is used to actually validate the date of birth.
 	 * @param dateOfBirth
 	 * @return will return true if the DOB is valid, otherwise it will return false.
 	 * @throws CprException
 	 * @throws ParseException 
 	 */
 	public static boolean isDateOfBirthValid(final String dateOfBirth) throws CprException, ParseException {
 		
 		// If the DOB ends with a slash we have an issue.
 		if (dateOfBirth.endsWith("/")) {
 			return false;
 		}
 				
 		// Validate the DOB.
 		switch (dateOfBirth.split("/").length) {
 		
 			// MM/DD DOB
 			case MONTH_DAY_ONLY: 
 				if (! Validate.isValidPartialDate(dateOfBirth)) {
 					return false;
 				}
 				break;
 				
 			// MM/DD/YYYY DOB
 			case FULL_DOB: 
 				if (! Validate.isValidDate(dateOfBirth)) {
 					return false;
 				}
 				else {
 					if (! isAgeValid(dateOfBirth)) {
 						return false;
 					}
 				}
 				break;
 					
 			// Anything else is an error.
 			default:
 				return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * This routine is used to validate that the age associated with the date of birth is valid.
 	 * @param dateOfBirth will accept a full date of birth mm/dd/yyyy
 	 * @return will return true if the age is valid, otherwise it will return false.
 	 */
 	private static boolean isAgeValid(final String dateOfBirth) {
 		
 		final String dobParts[] = dateOfBirth.split("/");
 		int month;
 		int day;
 		int year;
 		
 		try {
 			month = Integer.valueOf(dobParts[0]);
 			day = Integer.valueOf(dobParts[1]);
 			year = Integer.valueOf(dobParts[2]);
 		}
 		catch (NumberFormatException e) {
 			return false;
 		}
 		
		final DateMidnight birthDate = new DateMidnight(year, month, day);
 		final DateTime now = new DateTime();
 		int age = Years.yearsBetween(birthDate, now).getYears();
 
 		return (age <= ZERO_YEARS_OLD || age >= ONE_FIVE_ZERO_YEARS_OLD) ? false : true;
 	}
 
 	/**
 	 * This routine is used to validate the parameters for a get date of birth request.
 	 * @param personId contains the person identifier in the CPR.
 	 * @param requestedBy contains the user who requested the information.
 	 * @param returnHistory Y/N flag that indicates whether to return history or not.
 	 * @return DateOfBirthTable
 	 * @throws CprException 
 	 */
 	public static DateOfBirthTable validateGetDateOfBirthParameters(final long personId, final String requestedBy, final String returnHistory) 
 		throws CprException  {
 		
 		@SuppressWarnings("unused")
 		String localRequestedBy = ValidateHelper.checkField(null, requestedBy, null, "Requested by", false);
 		boolean returnHistoryFlag = ValidateHelper.checkReturnHistory(returnHistory);
 		
 		// Validate the return history flag.
 		final DateOfBirthTable dateOfBirthTable = new DateOfBirthTable();
 		dateOfBirthTable.setReturnHistoryFlag(returnHistoryFlag);
 		
 		return dateOfBirthTable;
 	}
 }
