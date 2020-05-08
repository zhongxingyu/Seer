 package com.socialcomputing.wps.server.planDictionnary.connectors;
 
 import java.io.PrintStream;
 import java.io.PrintWriter;
 
 /**
  * <p>Title: WPS Connectors</p>
  * <p>Description: </p>
  * <p>Copyright: Copyright (c) 2000</p>
  * <p>Company: VOYEZ VOUS</p>
  * @author Franck Valetas
  * @version 1.0
  */
 
 public class JMIException extends Exception
 {
 	private static final long serialVersionUID = -6564839622930056614L;
 	
 	public enum ORIGIN {
 	    DEFINITION, PARAMETER, PROPERTY, INTERNAL, CONNECTOR
 	}
 	
 	protected ORIGIN origin;
 	protected Throwable cause;
 	protected long code;
 
	    public JMIException()
 	{
 		this("Error occurred in WPS connector.");
 	}
 
 	public JMIException( String message)
 	{
 		this( message, null);
 	}
 
     public JMIException(String message, Throwable cause)
     {
         this( ORIGIN.INTERNAL, message, cause);
     }
     
     public JMIException(ORIGIN origin, String message)
     {
         this( origin, message, null);
     }
 
     public JMIException(ORIGIN origin, long code, String message)
     {
         this( origin, code, message, null);
     }
     
     public JMIException(ORIGIN origin, String message, Throwable cause) {
         this( origin, 0, message, null);
     }
     
 	public JMIException(ORIGIN origin, long code, String message, Throwable cause)
 	{
 		super(message);
 		this.origin = origin;
 		this.cause = cause;
 		this.code = code;
 	}
 
 	public String getMessage()
 	{
 		if (cause != null) {
 			return super.getMessage() + ": " + cause.getMessage();
 		} else {
 			return super.getMessage();
 		}
 	}
 
 	public void printStackTrace()
 	{
 		super.printStackTrace();
 		if (cause != null) {
 			System.err.print("Root cause: ");
 			cause.printStackTrace();
 		}
 	}
 
 	public void printStackTrace(PrintStream s)
 	{
 		super.printStackTrace(s);
 		if (cause != null) {
 			s.print("Root cause: ");
 			cause.printStackTrace(s);
 		}
 	}
 
 	public void printStackTrace(PrintWriter w)
 	{
 		super.printStackTrace(w);
 		if (cause != null) {
 			w.print("Root cause: ");
 			cause.printStackTrace(w);
 		}
 	}
 
 	public Throwable getCause()
 	{
 		return cause;
 	}
 	
     public ORIGIN getOrigin()
     {
         return origin;
     }
    /**
     * @return the code
     */
    public long getCode() {
        return code;
    }

 }
