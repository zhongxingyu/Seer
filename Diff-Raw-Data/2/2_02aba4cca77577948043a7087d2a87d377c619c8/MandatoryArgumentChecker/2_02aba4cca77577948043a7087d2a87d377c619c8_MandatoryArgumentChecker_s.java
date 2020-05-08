 /*
  * $Id$
  *
  * Copyright 2003-2006 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common;
 
 /**
  * Utility class used to check mandatory method arguments.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class MandatoryArgumentChecker extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The name of this class.
     */
    private static final String CLASSNAME =
       MandatoryArgumentChecker.class.getName();
 
    /**
     * The name of the method that checks 1 argument. Used in exception
     * handling and/or logging.
     */
    private static final String METHOD_1_NAME;
 
    /**
     * The name of the method that checks 2 arguments. Used in exception
     * handling and/or logging.
     */
    private static final String METHOD_2_NAME;
 
    /**
     * The name of the method that checks 3 arguments. Used in exception
     * handling and/or logging.
     */
    private static final String METHOD_3_NAME;
 
    /**
     * The name of the method that checks 4 arguments. Used in exception
     * handling and/or logging.
     */
    private static final String METHOD_4_NAME;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    static {
       String start = "check(";
       String arg = "java.lang.String,java.lang.String";
       String end = ")";
 
       METHOD_1_NAME = start + arg                                     + end;
       METHOD_2_NAME = start + arg + ',' + arg                         + end;
       METHOD_3_NAME = start + arg + ',' + arg + ',' + arg             + end;
       METHOD_4_NAME = start + arg + ',' + arg + ',' + arg + ',' + arg + end;
    }
 
    /**
     * Checks if the specified argument value is <code>null</code>. If it is
     * <code>null</code>, then an {@link IllegalArgumentException} is thrown.
     *
     * @param argName
     *    the name of the argument, cannot be <code>null</code>.
     *
     * @param argValue
     *    the value of the argument.
     *
     * @throws IllegalArgumentException
     *    if <code>argValue == null</code>.
     */
    public static void check(String argName, Object argValue)
    throws IllegalArgumentException {
 
       // If both are non-null everything is okay, just short-circuit
       if (argName != null && argValue != null) {
          return;
       }
 
       // Check if the name is null
       if (argName == null) {
          throw Utils.logProgrammingError(
             CLASSNAME,               METHOD_1_NAME,
             Utils.getCallingClass(), Utils.getCallingMethod(),
             "argName == null"
          );
       }
 
       // Otherwise the value is null
       if (argValue == null) {
          throw new IllegalArgumentException(argName + " == null");
       }
    }
 
    /**
     * Checks if any of the two specified argument values is <code>null</code>.
     * If at least one value is <code>null</code>, then an
     * {@link IllegalArgumentException} is thrown.
     *
     * @param argName1
     *    the name of the first argument, cannot be <code>null</code>.
     *
     * @param argValue1
     *    the value of the first argument.
     *
     * @param argName2
     *    the name of the second argument, cannot be <code>null</code>.
     *
     * @param argValue2
     *    the value of the second argument.
     *
     * @throws IllegalArgumentException
     *    if <code>argValue1 == null || argValue2 == null</code>.
     */
    public static void check(String argName1, Object argValue1,
                             String argName2, Object argValue2)
    throws IllegalArgumentException {
 
       // If all are non-null everything is okay, just short-circuit
       if (argName1 != null && argValue1 != null &&
           argName2 != null && argValue2 != null) {
          return;
       }
 
       String message;
 
       // Check if any of the names is null
       if (argName1 == null || argName2 == null) {
          if (argName1 == null && argName2 == null) {
             message = "argName1 == null && argName2 == null";
          } else if (argName1 == null) {
             message = "argName1 == null";
          } else {
             message = "argName2 == null";
          }
          throw Utils.logProgrammingError(
             CLASSNAME,               METHOD_2_NAME,
             Utils.getCallingClass(), Utils.getCallingMethod(),
             message
          );
       }
 
       // Otherwise (at least) one of the values must be null
       if (argValue1 == null && argValue2 == null) {
          message = argName1 + " == null && "
                  + argName2 + " == null";
       } else if (argValue1 == null) {
          message = argName1 + " == null";
       } else {
          message = argName2 + " == null";
       }
       throw new IllegalArgumentException(message);
    }
 
    /**
     * Checks if any of the three specified argument values is
     * <code>null</code>. If at least one value is <code>null</code>, then an
     * {@link IllegalArgumentException} is thrown.
     *
     * @param argName1
     *    the name of the first argument, cannot be <code>null</code>.
     *
     * @param argValue1
     *    the value of the first argument.
     *
     * @param argName2
     *    the name of the second argument, cannot be <code>null</code>.
     *
     * @param argValue2
     *    the value of the second argument.
     *
     * @param argName3
     *    the name of the third argument, cannot be <code>null</code>.
     *
     * @param argValue3
     *    the value of the third argument.
     *
     * @throws IllegalArgumentException
     *    if <code>argValue1 == null
     *          || argValue2 == null
     *          || argValue3 == null</code>.
     */
    public static void check(String argName1, Object argValue1,
                             String argName2, Object argValue2,
                             String argName3, Object argValue3)
    throws IllegalArgumentException {
 
       // If all are non-null everything is okay, just short-circuit
       if (argName1 != null && argValue1 != null &&
           argName2 != null && argValue2 != null &&
           argName3 != null && argValue3 != null) {
          return;
       }
 
       // Check if any of the names is null
       String message;
       if (argName1 == null || argName2 == null || argName3 == null) {
          if (argName1 == null && argName2 == null && argName3 == null) {
             message = "argName1 == null && "
                     + "argName2 == null && "
                     + "argName3 == null";
          } else if (argName1 == null && argName2 == null) {
             message = "argName1 == null && argName2 == null";
          } else if (argName1 == null && argName3 == null) {
             message = "argName1 == null && argName3 == null";
          } else if (argName2 == null && argName3 == null) {
             message = "argName2 == null && argName3 == null";
          } else if (argName1 == null) {
             message = "argName1 == null";
          } else if (argName2 == null) {
             message = "argName2 == null";
          } else {
             message = "argName3 == null";
          }
          throw Utils.logProgrammingError(
             CLASSNAME,               METHOD_3_NAME,
             Utils.getCallingClass(), Utils.getCallingMethod(),
             message
          );
       }
 
       // Otherwise (at least) one of the values must be null
       if (argValue1 == null && argValue2 == null && argValue3 == null) {
          message = argName1 + " == null && "
                  + argName2 + " == null && "
                  + argName3 + " == null";
       } else if (argValue1 == null && argValue2 == null) {
          message = argName1 + " == null &&"
                  + argName2 + " == null";
       } else if (argValue1 == null && argValue3 == null) {
          message = argName1 + " == null &&"
                  + argName3 + " == null";
       } else if (argValue2 == null && argValue3 == null) {
          message = argName2 + " == null &&"
                  + argName3 + " == null";
       } else if (argValue1 == null) {
          message = argName1 + " == null";
       } else if (argValue2 == null) {
          message = argName2 + " == null";
       } else {
          message = argName3 + " == null";
       }
       throw new IllegalArgumentException(message);
    }
 
    /**
     * Checks if any of the four specified argument values is
     * <code>null</code>. If at least one value is <code>null</code>, then an
     * {@link IllegalArgumentException} is thrown.
     *
     * @param argName1
     *    the name of the first argument, cannot be <code>null</code>.
     *
     * @param argValue1
     *    the value of the first argument.
     *
     * @param argName2
     *    the name of the second argument, cannot be <code>null</code>.
     *
     * @param argValue2
     *    the value of the second argument.
     *
     * @param argName3
     *    the name of the third argument, cannot be <code>null</code>.
     *
     * @param argValue3
     *    the value of the third argument.
     *
     * @param argName4
     *    the name of the fourth argument, cannot be <code>null</code>.
     *
     * @param argValue4
     *    the value of the fourth argument.
     *
     * @throws IllegalArgumentException
     *    if <code>argValue1 == null || argValue2 == null
     *          || argValue3 == null || argValue4 == null</code>.
     */
    public static void check(String argName1, Object argValue1,
                             String argName2, Object argValue2,
                             String argName3, Object argValue3,
                             String argName4, Object argValue4)
    throws IllegalArgumentException {
 
       // If all are non-null everything is okay, just short-circuit
       if (argName1 != null && argValue1 != null &&
           argName2 != null && argValue2 != null &&
           argName3 != null && argValue3 != null &&
           argName4 != null && argValue4 != null) {
          return;
       }
 
       // Check if any of the names is null
       String message;
       if (argName1 == null || argName2 == null ||
           argName3 == null || argName4 == null) {
          if (argName1 == null && argName2 == null &&
              argName3 == null && argName4 == null) {
             message = "argName1 == null && argName2 == null && "
                     + "argName3 == null && argName4 == null";
          } else if (argName1 == null && argName2 == null &&
                     argName3 == null) {
             message = "argName1 == null && argName2 == null && "
                     + "argName3 == null";
          } else if (argName1 == null && argName2 == null &&
                     argName4 == null) {
             message = "argName1 == null && argName2 == null && "
                     + "argName4 == null";
          } else if (argName1 == null && argName3 == null &&
                     argName4 == null) {
             message = "argName1 == null && argName3 == null && "
                     + "argName4 == null";
          } else if (argName2 == null && argName3 == null &&
                     argName4 == null) {
             message = "argName2 == null && argName3 == null && "
                     + "argName4 == null";
          } else if (argName1 == null && argName2 == null) {
             message = "argName1 == null && argName2 == null";
          } else if (argName1 == null && argName3 == null) {
             message = "argName1 == null && argName3 == null";
          } else if (argName1 == null && argName4 == null) {
             message = "argName1 == null && argName4 == null";
          } else if (argName2 == null && argName3 == null) {
             message = "argName2 == null && argName3 == null";
          } else if (argName2 == null && argName4 == null) {
             message = "argName2 == null && argName4 == null";
          } else if (argName3 == null && argName4 == null) {
             message = "argName3 == null && argName4 == null";
          } else if (argName1 == null) {
             message = "argName1 == null";
          } else if (argName2 == null) {
             message = "argName2 == null";
          } else if (argName3 == null) {
             message = "argName3 == null";
          } else {
             message = "argName4 == null";
          }
          throw Utils.logProgrammingError(
            CLASSNAME,               METHOD_3_NAME,
             Utils.getCallingClass(), Utils.getCallingMethod(),
             message
          );
       }
 
       // Otherwise (at least) one of the values must be null
       if (argValue1 == null && argValue2 == null && argValue3 == null) {
          message = argName1 + " == null && "
                  + argName2 + " == null && "
                  + argName3 + " == null";
       } else if (argValue1 == null && argValue2 == null) {
          message = argName1 + " == null &&"
                  + argName2 + " == null";
       } else if (argValue1 == null && argValue3 == null) {
          message = argName1 + " == null &&"
                  + argName3 + " == null";
       } else if (argValue2 == null && argValue3 == null) {
          message = argName2 + " == null &&"
                  + argName3 + " == null";
       } else if (argValue1 == null) {
          message = argName1 + " == null";
       } else if (argValue2 == null) {
          message = argName2 + " == null";
       } else {
          message = argName3 + " == null";
       }
       throw new IllegalArgumentException(message);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>MandatoryArgumentChecker</code>. This constructor
     * is private since this no instances of this class should be created.
     */
    private MandatoryArgumentChecker() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
