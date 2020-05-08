 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.db;
 
 /**
  * @(#)TableException.java  1.16 95/12/14 Don Corley
  *
  * Copyright © 2012 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  */
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.model.DBException;
 import org.jbundle.model.Task;
 
 
 /**
  * Table and Record Exceptions.
  * Errors are handled in several ways in the system.
  * <pre>
  * 1. A standard exception (ie., kXyzError) is simply thrown as an exception: throw new DatabaseException(kXyzError);
  * 2. A standard error (ie., kXyzError) is simply returned: return kXyzError;
  * 3. A Special exception is thrown with the desciption: throw new DatabaseException("Too Long!");
  * 4. A Special error is returned after setting the last error message: return task.setLastError("Too long");
  * Note: All calls have the option of adding a warning level.
  * If you need the error description, just call:
  *  ex.getMessage();
  * If you want the error string from the error number then,
  *  strError = task.getLastError(iErrorCode);
  *  if (strError == null)
  *      strError = new DatabaseException(iErrorCode).getMessage();
  * </pre>
  *
  * @version 1.0.0
  * @author    Don Corley
  */
 public class DatabaseException extends DBException
 {
 	private static final long serialVersionUID = 1L;
 
 	/**
      * For Exception conversions.
      */
     public static final String DBEXCEPTION_TEXT = "DBException: ";
     /**
      * Error code.
      */
     protected int m_iWarningLevel = DBConstants.WARNING_MESSAGE;
 
     /**
      * Constructor for standard errors.
      */
     public DatabaseException(int iErrorCode)
     {
         super();    // Get the error text, or make it up
         this.init(null, iErrorCode, DBConstants.WARNING_MESSAGE);
     }
     /**
      * Constructor for standard errors.
      */
     public DatabaseException(int iErrorCode,int iWarningLevel)
     {
         super();    // Get the error text, or make it up
         this.init(null, iErrorCode, iWarningLevel);
     }
     /**
      * Constructor for standard errors.
      */
     public DatabaseException(int iError, String strError)
     {
         super(strError);
         this.init(strError, iError, DBConstants.WARNING_MESSAGE);
     }
     /**
      * Constructor.
      */
     public DatabaseException(String strError)
     {
         super(strError);
         this.init(strError, DBConstants.ERROR_STRING, DBConstants.WARNING_MESSAGE);
     }
     /**
      * Constructor.
      */
     public DatabaseException(String strError,int iWarningLevel)
     {
         super(strError);
         this.init(strError, DBConstants.ERROR_STRING, iWarningLevel);
     }
     /**
      * Constructor.
      */
     public void init(String strError, int iErrorCode, int iWarningLevel)
     {
         super.init(strError, iErrorCode, iWarningLevel);
         m_iWarningLevel = iWarningLevel;
     }
     /**
      * Return the error code.
      */
     public int getErrorCode()
     {
         return m_iErrorCode;
     }
     /**
      * Return the error code.
      */
     public int getWarningLevel()
     {
         return m_iWarningLevel;
     }
     /**
      * Return the error message.
      */
     public String getMessage()
     {
         return this.getMessage(null);
     }
     /**
      * Return the error message.
      */
     public String getMessage(Task task)
     {
         String strError = null;
         switch (m_iErrorCode)
         {
         case DBConstants.NORMAL_RETURN:
             strError = "Normal return";break; // Never
         case DBConstants.END_OF_FILE:
             strError = "End of file";break;
         case DBConstants.KEY_NOT_FOUND:
             strError = "Key not found";break;
         case DBConstants.DUPLICATE_KEY:
             strError = "Duplicate key";break;
         case DBConstants.FILE_NOT_OPEN:
             strError = "File not open";break;
         case DBConstants.FILE_ALREADY_OPEN:
             strError = "File already open";break;
         case DBConstants.FILE_TABLE_FULL:
             strError = "File table full";break;
         case DBConstants.FILE_INCONSISTENCY:
             strError = "File inconsistency";break;
         case DBConstants.INVALID_RECORD:
             strError = "Invalid record";break;
         case DBConstants.FILE_NOT_FOUND:
             strError = "File not found";break;
         case DBConstants.INVALID_KEY:
             strError = "Invalid Key";break;
         case DBConstants.FILE_ALREADY_EXISTS:
             strError = "File Already Exists";break;
         case DBConstants.NULL_FIELD:
             strError = "Null field";break;
         case DBConstants.READ_NOT_NEW:
             strError = "Read not new";break;
         case DBConstants.NO_ACTIVE_QUERY:
             strError = "No active query";break;
         case DBConstants.RECORD_NOT_LOCKED:
             strError = "Record not locked";break;
         case DBConstants.RETRY_ERROR:
             strError = "Retry error";break;
         case DBConstants.ERROR_READ_ONLY:
             strError = "Can't update read only file";break;
         case DBConstants.ERROR_APPEND_ONLY:
             strError = "Can't update append only file";break;
         case DBConstants.RECORD_LOCKED:
             strError = "Record locked";break;
         case DBConstants.BROKEN_PIPE:
             strError = "Broken Pipe";break;
         case DBConstants.DUPLICATE_COUNTER:
             strError = "Duplicate counter";break;
         case DBConstants.DB_NOT_FOUND:
             strError = "Database not found";break;
         case DBConstants.NEXT_ERROR_CODE:
         case DBConstants.ERROR_RETURN:
         case DBConstants.ERROR_STRING:
         default:
             strError = super.getMessage();  // See if there is a built-in message
            if ((strError == null) || (strError.length() == 0) || (strError.startsWith("null")))
                 if (task != null)
             {   // Unknown error code, see if it was the last error
                 strError = task.getLastError(m_iErrorCode);
                 if ((strError != null) && (strError.length() > 0))
                     break;  // Yes, return the error message
             }
             break;
         }
         return strError;
     }
     /**
      * This is a utility method to convert a DBException to an Exception, for thin returns.
      */
     public DBException toException()
     {
         return new DBException(DBEXCEPTION_TEXT + this.getErrorCode() + ' ' + this.getMessage());
     }
     /**
      * Convert a normal Exception to a DBException.
      */
     public static DatabaseException toDatabaseException(Exception ex)
     {
         if (ex instanceof DatabaseException)
             return (DatabaseException)ex;
         int iErrorCode = DBConstants.ERROR_STRING;
         if (ex instanceof DBException)
             iErrorCode = ((DBException)ex).getErrorCode();
         String strMessage = ex.getMessage();
         if (strMessage != null)
             if (strMessage.startsWith(DBEXCEPTION_TEXT))
         {
             int iNextPos = strMessage.indexOf(' ', DBEXCEPTION_TEXT.length());
             if (iNextPos != -1)
             {
                 iErrorCode = Integer.parseInt(strMessage.substring(DBEXCEPTION_TEXT.length(), iNextPos));
                 if (iNextPos + 1 < strMessage.length())
                     strMessage = strMessage.substring(iNextPos + 1);
             }
         }
         return new DatabaseException(iErrorCode, strMessage);
     }
     /**
      * toString.
      */
     public String toString()
     {
     	return "DatabaseException: " + this.getMessage();
     }
 }
