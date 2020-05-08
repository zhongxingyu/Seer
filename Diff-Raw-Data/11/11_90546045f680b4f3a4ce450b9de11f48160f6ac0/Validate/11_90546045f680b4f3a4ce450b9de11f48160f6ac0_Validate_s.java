 /* SVN FILE: $Id: Validate.java 5340 2012-09-27 14:48:52Z jvuccolo $ */
 /**
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
 package edu.psu.iam.cpr.core.util;
 
 import java.text.SimpleDateFormat;
 import java.util.regex.*;
 
 import edu.psu.iam.cpr.core.database.Database;
 import edu.psu.iam.cpr.core.database.beans.IdentifierType;
 import edu.psu.iam.cpr.core.database.helpers.DBTypesHelper;
 import edu.psu.iam.cpr.core.database.types.CprPropertyName;
 import edu.psu.iam.cpr.core.error.CprException;
 import edu.psu.iam.cpr.core.error.GeneralDatabaseException;
 import edu.psu.iam.cpr.core.error.ReturnType;
 
 
 /**
  * Validate is a utility class that will validate data inputs 
  * @author slk24
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
  * @version %I%, %G%
  * 
  * @since 1.0
  */
 public class Validate {
 	
     /**
      * The purpose of this routine is valid a field containing a yes/no value.  
      * The routine will automatically return an "N" for values passed in that are
      * null.  The routine will strip out whitespace to find a Y/N value.
      * 
      * @param yesNo contains the string to be validated.
      * @return if successful will return a Y/N, otherwise it will return a null object.
      */
     public static String isValidYesNo(String yesNo) {
     	
     	if (yesNo == null) {
     		return "N";
     	}
     	
     	final String s = yesNo.toUpperCase().trim();
     	
    	if (s.startsWith("Y") || s.startsWith("N")) {
    		return s.substring(0,1);
     	}
     	
     	return null;
     }
 
 	/**
 	 * This routine will use a regular expression to check the input string for a valid 
 	 * PSU Id format
 	 * 
 	 * @param psuId - contains the PSU Id to validate.
 	 * 
 	 * @return true if format is valid, false otherwise
 	 */
     public static boolean isValidPsuId(String psuId) {
     	if (psuId == null || psuId.length() == 0) {
     		return false;
     	}
 	    if (Pattern.matches(CprProperties.getInstance().getProperties().getProperty(CprPropertyName.CPR_REGEX_PSU_ID_NUMBER.toString()), psuId)) {
 	    	return true;
 	    }
 	    return false;
 	}
     
     /**
      * This routine is used to validate a string an attempt to establish an enumerated type.
      * @param db contains a database parameter.
      * @param identifierType contains a string representing an indentifier type to be converted.
      * @return returns either a valid IdentifierType or a null.
      */
     public static IdentifierType isValidIdentifierType(Database db, String identifierType) {
     	
     	try {
 			return (IdentifierType) DBTypesHelper.getInstance(db).getTypeMaps(DBTypesHelper.IDENTIFIER_TYPE).get(identifierType.toUpperCase().trim());
 		} 
     	catch (GeneralDatabaseException e) {
 		}
     	return null;
     }
     
     /**
      * The purpose of this routine is to validate whether an identifier's value is less than the maximum database field length.
      * @param db contains a database connection.
      * @param typeName contains the type's name.
      * @param identifier contains the value of the identifier.
      * @return will return true if the identifier is less than the maximum length.
      * @throws GeneralDatabaseException will be thrown if there are any database exceptions.
      * @throws CprExecption will be thrown if there are any CPR specific problems.
      */
     public static boolean isIdentifierLengthValid(Database db, String typeName, String identifier) 
     throws GeneralDatabaseException, CprException {
 
     	if (typeName.equals(Database.ID_CARD_IDENTIFIER)) {
     		doIdentifierLengthCheck(db, identifier, "Id card number", "PERSON_ID_CARD", "ID_CARD_NUMBER");
     	}
     	else if (typeName.equals(Database.PERSON_ID_IDENTIFIER)) {
     		if (identifier.length() == 0) {
     			throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, "Person identifier");
     		}
     	}
     	else if (typeName.equals(Database.PSU_ID_IDENTIFIER)) {
     		doIdentifierLengthCheck(db, identifier, "PSU Id Number", "PSU_ID", "PSU_ID");
     	}
     	else if (typeName.equals(Database.SSN_IDENTIFIER)) {
     		doIdentifierLengthCheck(db, identifier, "Social Security Number", "PSU_ID", "PSU_ID");
     	}
     	else if (typeName.equals(Database.USERID_IDENTIFIER)) {
     		doIdentifierLengthCheck(db, identifier, "Userid", "USERID", "USERID");
     	}
     	else {
     		doIdentifierLengthCheck(db, identifier, typeName, "PERSON_IDENTIFIER", "IDENTIFIER_VALUE");
     	}
     	return true;
     }
 
 	/**
 	 * This routine is used to perform the bulk of the identifier length check.
 	 * @param db contains the database connection.
 	 * @param identifier contains the value of the identifier.
 	 * @param identifierName contains the english name of the identifier (for error message purposes).
 	 * @param tableName contains the name of the database table.
 	 * @param columnName contains the name of the database column to validate against.
 	 * @throws CprException will be thrown if there are any CPR specific problems.
 	 * @throws GeneralDatabaseException will be thrown if there are any database problems.
 	 */
     public static void doIdentifierLengthCheck(Database db, String identifier, 
     		String identifierName, String tableName, String columnName) throws CprException, GeneralDatabaseException {
 
     	if (identifier.length() == 0) {
     		throw new CprException(ReturnType.INVALID_PARAMETERS_EXCEPTION, identifierName);
     	}
 
     	db.getAllTableColumns(tableName);
     	if (identifier.length() > db.getColumn(columnName).getColumnSize()) {
     		throw new CprException(ReturnType.PARAMETER_LENGTH_EXCEPTION, identifierName);
     	}
     }
     
     /**
      * This routine is used to validate a date.  It is passed in a date in the format of MM/DD/YYYY.  If the 
      * date is valid it will return true, otherwise it will return false.
      * @param dateString contains the date to be validated.
      * @return true if a valid date, otherwise it will return false.
      */
     public static boolean isValidDate(String dateString) {
     	try {
     		final SimpleDateFormat sdf = new SimpleDateFormat(CprProperties.getInstance().getProperties().getProperty(CprPropertyName.CPR_FORMAT_DATE.toString()));
     		sdf.setLenient(false);
     		sdf.parse(dateString);
     		return true;
     	}
     	catch (Exception e) {
     		return false;
     	}
     }
     
     /**
      * This routine is used to validate a partial date of the format MM/DD.
      * @param dateString contains the partial date to be validated.
      * @return will return true if the partial date is valid, otherwise it will return false.
      */
     public static boolean isValidPartialDate(String dateString) {
     	try {
     		final SimpleDateFormat sdf = new SimpleDateFormat(CprProperties.getInstance().getProperties().getProperty(CprPropertyName.CPR_FORMAT_PARTIAL_DATE.toString()));
     		sdf.setLenient(false);
     		sdf.parse(dateString);
     		return true;
     	}
     	catch (Exception e) {
     		return false;
     	}
     }
 }
