 package edu.wustl.cab2b.common.errorcodes;
 
 /**
  * This interface contains the constants for the error codes to be 
  * used in the code throughout the application.
  * Any error code constant added here should have a corresponding
  * entry in the errorcodes.properties file.
  * @author gautam_shetty
  */
 public interface ErrorCodeConstants {
     /** User fetched with incomplete data. */
     public static final String UR_0001 = "UR.0001";
     
     /** User already exists */
     public static final String UR_0002 = "UR.0002";
     
     /** Error while fetching user from database */
     public static final String UR_0003 = "UR.0003";
     
     /** Error while inserting user in database */
     public static final String UR_0004 = "UR.0004";
     
     /** Error while updating user information in database */
     public static final String UR_0005 = "UR.0005";
     
     /** Please recheck identity provider url */
     public static final String UR_0006 = "UR.0006";
     
     /** Unable to authenticate: Invalid credentials */
     public static final String UR_0007 = "UR.0007";
     
     /** Please recheck dorian url */
     public static final String UR_0008 = "UR.0008";
     
     /** Please check the credentials again (User name is case sensitive) */
     public static final String UR_0009 = "UR.0009";
     
     //---------------------------------------------------------------------
     /**Unable to parse domain model XML file.*/
     public static final String GR_0001 = "GR.0001";
 
     //---------------------------------------------------------------------
     /**Unable to persist Entity Group in Dynamic Extension.*/
     public static final String DE_0001 = "DE.0001";
 
     /**Unable to persist Entity in Dynamic Extension.*/
     public static final String DE_0002 = "DE.0002";
 
     /**Inconsistent data in database*/
     public static final String DE_0003 = "DE.0003";
 
     /**Unable to retrive Dynamic Extension objects*/
     public static final String DE_0004 = "DE.0002";
 
     //---------------------------------------------------------------------
     /**Database down.*/
     public static final String DB_0001 = "DB.0001";
 
     /**Unable to create a connection from datasource*/
     public static final String DB_0002 = "DB.0002";
 
     /**Exception while firing Parameterized query.**/
     public static final String DB_0003 = "DB.0003";
 
     /**Exception while firing Update query.**/
     public static final String DB_0004 = "DB.0004";
 
     /**Exception while editing saved query.**/
     public static final String DB_0005 = "DB.0005";
 
     //---------------------------------------------------------------------
     /**Cab2b server down.*/
     public static final String SR_0001 = "SR.0001";
 
     //---------------------------------------------------------------------
     /**File operation failed*/
     public static final String IO_0001 = "IO.0001";
 
     /**Can't find resource bundle.*/
     public static final String IO_0002 = "IO.0002";
 
     /** XML parse error. */
     public static final String IO_0003 = "IO.0003";
 
     //---------------------------------------------------------------------
     /** Java Reflection API Error.*/
     public static final String RF_0001 = "RF.0001";
 
     //---------------------------------------------------------------------
     /** Unknown Error in the Application (Can be used for app. development). */
     public static final String UN_XXXX = "UN.XXXX";
 
     //---------------------------------------------------------------------
     /**Unable to look up resource from JNDI*/
     public static final String JN_0001 = "JN.0001";
 
     public static final String QUERY_INVALID_INPUT = "QM.0001";
 
     public static final String QUERY_EXECUTION_ERROR = "QM.0002";
 
     public static final String QUERY_SAVE_ERROR = "QM.0005";
 
     public static final String QUERY_RETRIEVE_ERROR = "QM.0006";
 
     public static final String CATEGORY_SAVE_ERROR = "CT.0001";
 
     public static final String CATEGORY_RETRIEVE_ERROR = "CT.0002";
 
     public static final String CUSTOM_CATEGORY_ERROR = "CT.0003";
 
     public static final String DATALIST_SAVE_ERROR = "DL.0001";
 
     public static final String DATALIST_RETRIEVE_ERROR = "DL.0002";
 
     public static final String DATACATEGORY_SAVE_ERROR = "DC.001";
 
     public static final String QM_0003 = "QM.0003";
 
     public static final String QM_0004 = "QM.0004";
 
     public static final String CA_0001 = "CA.0001";
 
     public static final String CA_0007 = "CA.0007";
 }
