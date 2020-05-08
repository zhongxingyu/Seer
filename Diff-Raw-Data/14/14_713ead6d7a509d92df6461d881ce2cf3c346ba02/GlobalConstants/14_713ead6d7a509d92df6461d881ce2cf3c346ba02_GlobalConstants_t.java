 /*
  * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
 
 package org.opensubsystems.core.util;
 
 /**
  * Class containing global constants for the system and for the product running
  * this code.
  * 
  * @author bastafidli
  */
 public final class GlobalConstants extends OSSObject
 {
    // Constants ////////////////////////////////////////////////////////////////
    
    /**
     * Constant controlling if error checking is performed. If this is changed 
     * to false and the whole project is rebuilt, compiler should exclude all
     * error checking to improve performance.
     */
    public static final boolean ERROR_CHECKING = true;
 
    /**
     * constant for 0 - Integer
     */
    public static final Integer INTEGER_0 = new Integer(0);
    
    /**
     * constant for 1 - Integer
     */
    public static final Integer INTEGER_1 = new Integer(1);
 
    /**
     * This value can be used to signal yes or true.
     * Use number since this way we can just use equals and not equalsIgnoreCase.
     */
    public static final String VALUE_YES_TRUE_NUMERIC = "1";
 
    /**
     * This value can be used to signal yes or true.
     */
    public static final String VALUE_YES_TRUE_STRING1 = Boolean.TRUE.toString();
    
    /**
     * This value can be used to signal yes or true.
     */
    public static final String VALUE_YES_TRUE_STRING2 = "yes";
    
    /**
     * This value can be used to signal yes or true.
     */
    public static final String VALUE_YES_TRUE_CHARACTER1 
                                  = VALUE_YES_TRUE_STRING1.substring(0, 1);
    
    /**
     * This value can be used to signal yes or true.
     */
    public static final String VALUE_YES_TRUE_CHARACTER2 
                                  = VALUE_YES_TRUE_STRING2.substring(0, 1);
    
    /**
     * This value can be used to signal no or false but in general we should test 
     * on TRUE and if it is not TRUE then it is FALSE. Use number since this way 
     * we can just use equals and not equalsIgnoreCase.
     */
    public static final String VALUE_NO_FALSE_NUMERIC = "0";
 
    /**
     * This value can be used to signal no or false but in general we should test 
     * on TRUE and if it is not TRUE then it is FALSE. Use number since this way 
     * we can just use equals and not equalsIgnoreCase.
     */
    public static final String VALUE_NO_FALSE_STRING1 = Boolean.FALSE.toString();
 
    /**
     * This value can be used to signal no or false but in general we should test 
     * on TRUE and if it is not TRUE then it is FALSE. Use number since this way 
     * we can just use equals and not equalsIgnoreCase.
     */
    public static final String VALUE_NO_FALSE_STRING2 = "no";
 
    /**
     * This value can be used to signal no or false but in general we should test 
     * on TRUE and if it is not TRUE then it is FALSE. Use number since this way 
     * we can just use equals and not equalsIgnoreCase.
     */
    public static final String VALUE_NO_FALSE_CHARACTER1 
                                  = VALUE_NO_FALSE_STRING1.substring(0, 1);
 
    /**
     * This value can be used to signal no or false but in general we should test 
     * on TRUE and if it is not TRUE then it is FALSE. Use number since this way 
     * we can just use equals and not equalsIgnoreCase.
     */
    public static final String VALUE_NO_FALSE_CHARACTER2 
                                  = VALUE_NO_FALSE_STRING2.substring(0, 1);
 
    // Attributes ///////////////////////////////////////////////////////////////
    
    /**
     * Current operating system.
     */
   private static final String s_currentOS;
    
    /**
     * Current operating system.
     */
   private static final String s_currentOSUpper;
    
    /**
     * End of lines characters used on given platform.
     */
   private static final String s_lineSeparator;
 
    /**
     * Separator used to separate various elements of a file path.
     */
   private static final String s_fileSeparator;
 
    /**
     * System specified temporary directory.
     */
   private static final String s_strTempDirectory;
    
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Static initializer.
     */
    static
    {
       String strTemp; 
        
       s_currentOS = System.getProperty("os.name");
       s_currentOSUpper = s_currentOS.toUpperCase();
       s_lineSeparator = System.getProperty("line.separator");
       s_fileSeparator = System.getProperty("file.separator");
       strTemp = System.getProperty("java.io.tmpdir");
       if (!strTemp.endsWith(s_fileSeparator))
       {
          s_strTempDirectory = strTemp + s_fileSeparator;
       }
       else
       {
           s_strTempDirectory = strTemp;
       }
    }
 
    /** 
     * Private constructor since this class cannot be instantiated
     */
    private GlobalConstants(
    )
    {
       // Do nothing
    }
    
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * Are we running on Linux.
     * 
     * @return boolean
     */
    public static boolean isLinux(
    )
    {
       return s_currentOSUpper.indexOf("LINUX") >= 0;
    }
    
    /**
     * Are we running on Windows.
     * 
     * @return boolean
     */
    public static boolean isWindows(
    )
    {
       return s_currentOSUpper.indexOf("WINDOWS") >= 0;
    }
 
    /**
     * @return String - line separator used at current platform
     */
    public static String getLineSeparator()
    {
       return s_lineSeparator;
    }
 
    /**
     * @return String - file separator used at current platform
     */
    public static String getFileSeparator()
    {
       return s_fileSeparator;
    }
 
    /**
     * @return String - system specified temporary directory, it is guaranteed
     *                  to end with file separator
     */
    public static String getTempDirectory()
    {
       return s_strTempDirectory;
    }
 
    /**
     * Test if specified value is true according to defined constants. 
     * 
     * @param  strValue - value to test
     * @return boolean - true if the value represents meaning of "true" otherwise
     *                   you can assume it is false or you can also test the
     *                   isNotTrueOrFalse to determine if the string value doesn't 
     *                   represent neither true or false.  
     */
    public static boolean isTrue(
       String strValue
    )
    {
       // We don't have to even do equalsIgnoreCase since this is a number.
       return Boolean.TRUE.equals(isBoolean(strValue));
    }
    
    /**
     * Test if specified value is neither true or false according to defined 
     * constants. We do not provide method isFalse since we do not want users of
     * this class to user some time isTrue and some time isFalse and get various
     * logical outcomes. 
     * 
     * @param  strValue - value to test
     * @return boolean - true if the value doesn't represent neither true or false
     *                   as defined by the constants in this class
     *                   false if the values represents either true or false  
     */
    public static boolean isNotTrueOrFalse(
       String strValue
    )
    {
       return (isBoolean(strValue) == null);
    }
 
    /**
     * Test if specified value is a string constant identifying either true or 
     * false according to defined constants. 
     * 
     * @param  strValue - value to test
     * @return Boolean - Boolean.TRUE if the value represent true 
     *                   Boolean.FALSE if the value represent false
     *                   null otherwise  
     */
    public static Boolean isBoolean(
       String strValue
    )
    {
       Boolean bReturn = null;
       
       // We don't have to even do equalsIgnoreCase since this is a number.
       if ((VALUE_YES_TRUE_NUMERIC.equals(strValue))
          || (VALUE_YES_TRUE_STRING1.equalsIgnoreCase(strValue))
          || (VALUE_YES_TRUE_STRING2.equalsIgnoreCase(strValue))
          || (VALUE_YES_TRUE_CHARACTER1.equalsIgnoreCase(strValue))
          || (VALUE_YES_TRUE_CHARACTER2.equalsIgnoreCase(strValue)))
       {
          bReturn = Boolean.TRUE;
       }
       else if ((VALUE_NO_FALSE_NUMERIC.equals(strValue))
               || (VALUE_NO_FALSE_STRING1.equalsIgnoreCase(strValue))
               || (VALUE_NO_FALSE_STRING2.equalsIgnoreCase(strValue))
               || (VALUE_NO_FALSE_CHARACTER1.equalsIgnoreCase(strValue))
               || (VALUE_NO_FALSE_CHARACTER2.equalsIgnoreCase(strValue)))
       {
          bReturn = Boolean.FALSE;
       }
       
       return bReturn;
    }
 }
