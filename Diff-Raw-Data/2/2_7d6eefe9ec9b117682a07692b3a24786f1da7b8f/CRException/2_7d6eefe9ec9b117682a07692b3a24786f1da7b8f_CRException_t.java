 package com.gentics.cr;
 
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.io.StringWriter;
 
 /**
  * 
  * Last changed: $Date$
  * @version $Revision$
  * @author $Author$
  *
  */
 public class CRException extends Exception implements Serializable{
 
 	
 	private static final long serialVersionUID = -3702075481737853571L;
 
 	private String message;
 	
 	private String type;
 	
 	private ERRORTYPE errType = ERRORTYPE.GENERAL_ERROR;
 	
 	@SuppressWarnings("unused")
 	private String stringStack;
 	
 	/**
 	 * Set the error type that can later be evaluated by a client when sending over javaxml
 	 * @param type
 	 */
 	public void setErrorType(ERRORTYPE type)
 	{
 		this.errType = type;
 	}
 	
 	/**
 	 * gets the error type that can later be evaluated by a client when sending over javaxml
 	 * @return type
 	 */
 	public ERRORTYPE getErrorType()
 	{
 		return(this.errType);
 	}
 	
 	/**
 	 * gets the error message
 	 * @return message
 	 */
 	public String getMessage()
 	{
 		return(this.message);
 	}
 	
 	/**
 	 * gets the error type as string
 	 * @return type
 	 */
 	public String getType()
 	{
 		return(this.type);
 	}
 	
 	/**
 	 * Set the error message
 	 * @param newmessage 
 	 */
 	public void setMessage(String newmessage)
 	{
 		this.message=newmessage;
 	}
 	
 	/**
 	 * Set the error type as string
 	 * @param newType 
 	 */
 	public void setType(String newType)
 	{
 		this.type=newType;
 	}
 	
 	/**
 	 * default constructor to create a new CRException
 	 */
 	public CRException()
 	{
 		super();
 	}
 	
 	/**
 	 * Create new CRException from CRError
 	 * @param err
 	 */
 	public CRException(CRError err)
 	{
 		super(err.getMessage());
 		this.message = err.getMessage();
 		this.type = err.getType();
 		this.stringStack = err.getStringStackTrace();
 		this.errType = err.getErrorType();
 	}
 	
 	/**
 	 * 
 	 * @param newtype
 	 * @param newmessage
 	 */	
 	public CRException(String newtype, String newmessage)
 	{
 		super(newmessage);
 		this.message=newmessage;
 		this.type=newtype;
 	}
 	
 	/**
 	 * creates new CRException with ERRORTYPE
 	 * @param newtype
 	 * @param newmessage
 	 * @param type
 	 */
 	public CRException(String newtype, String newmessage, ERRORTYPE type)
 	{
 		super(newmessage);
 		this.message=newmessage;
 		this.type=newtype;
 		this.errType = type;
 	}
 	
 	/**
 	 * Creates a new CRException from an Exception
 	 * @param ex
 	 */
 	public CRException(Exception ex)
 	{
		super(ex.getMessage(), ex);
 		this.message=ex.getMessage();
 		if(this.message==null)this.message=ex.getClass().getName();
 		this.type=ex.getClass().getSimpleName();
 		this.setStackTrace(ex.getStackTrace());
 	}
 	
 	/**
 	 * gets the Stacktrace as String
 	 * @return stringstacktrace
 	 */
     public String getStringStackTrace()
     {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw, true);
         this.printStackTrace(pw);
         pw.flush();
         sw.flush();
         return sw.toString();
     }
     
     /**
      * sets the stacktrace as string that is returned by getStringStackTrace()
      * @param str
      */
     public void setStringStackTrace(String str)
     {
         this.stringStack = str;
     }
     
     /**
      * has to be called before serialization
      * DEPRICATED => USE CRError to serialize an Error
      */
     public void initStringStackForSerialization()
     {
     	this.stringStack = getStringStackTrace();
     }
     
     
     /**
      * Enum
      *
      */
     public enum ERRORTYPE{
 		/**
 		 * Field when no data has been found.
 		 */
 		NO_DATA_FOUND,
 		/**
 		 * Field when a general Error occurred.
 		 */
 		GENERAL_ERROR, 
 		/**
 		 * Field when a fatal Error occurred.
 		 */
 		FATAL_ERROR
 	}
 }
