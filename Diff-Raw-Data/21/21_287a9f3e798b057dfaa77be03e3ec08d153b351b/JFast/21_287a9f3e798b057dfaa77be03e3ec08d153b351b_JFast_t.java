 
 package org.bumblescript.jfast;
 
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 
 public class JFast
 {
     private ServerSocket socket;
 
     public JFast(int port)
         throws IOException
     {
         socket = new ServerSocket(port);
     }
 
     public JFast()
         throws IOException, JFastException
     {
         try
         {
             Properties environment = System.getProperties();
             int portNumber = Integer.parseInt((String)
                 environment.getProperty("FCGI_PORT"));
             socket = new ServerSocket(portNumber);
         }
         catch(Exception exception)
         {
             throw new JFastException("Missing \"FCGI_PORT\" in environment.");
         }
     }        
     
     public JFastRequest acceptRequest()
         throws IOException, JFastException
     {
         Socket requestSocket = socket.accept();
	try {
	    JFastRequest req = new JFastRequest(requestSocket);
	    requestSocket = null;
	    return req;
	}
	finally {
	    if (requestSocket != null) {
	        requestSocket.close();
	    }
	}
     }    
     
     
     
     
     
     
     
     
     
     
     
     
     /** FastCGI version **/
     public static final int VERSION = 1;
     
     /** body lengths **/
     public static final int MAX_LENGTH = 0xffff;
     public static final int HEADER_LENGTH = 8;
     public static final int END_REQUEST_BODY_LENGTH = 8;
     
     /** record types **/
     public static final int BEGIN_REQUEST = 1;
     public static final int ABORT_REQUEST = 2;
     public static final int END_REQUEST = 3;
     public static final int PARAMETERS = 4;
     public static final int STDIN = 5;
     public static final int STDOUT = 6;
     public static final int STDERR = 7;
     public static final int DATA = 8;
     public static final int GET_VALUES = 9;
     public static final int GET_VALUES_RESULT = 10;
     public static final int UNKNOWN_TYPE = 11;
     public static final int MAX_TYPE = UNKNOWN_TYPE;
     
     /** request IDs **/
     public static final int NULL_REQUEST = 0; 
     
     /** roles **/
     public static final int RESPONDER = 1;
     public static final int AUTHORIZER = 2;
     public static final int FILTER = 3;
     
     /** protocol status **/
     public static final int REQUEST_COMPLETE = 0;
     public static final int NO_MULTIPLEX_CONNECTION = 1;
     public static final int OVERLOAD = 2;
     public static final int UNKNOWN_ROLE = 3;
     
     /** process function codes **/
     public static final int STREAM_RECORD = 0;
     public static final int SKIP = 1;
     public static final int BEGIN_RECORD = 2;
     public static final int MANAGEMENT_RECORD = 3;
     
     /** mask flags **/
     public static final int KEEP_CONNECTION = 1;
     
     /** errors **/
     public static final int UNSUPPORTED_VERSION = -2;
     public static final int PROTOCOL_ERROR = -3;
     public static final int PARAMETER_ERROR = -4;
     public static final int CALL_SEQUENCE_ERROR = -5;
     
     /** value keys **/
     public static final String MAX_CONNECTIONS_KEY = "FCGI_MAX_CONNS";
     public static final String MAX_REQUESTS_KEY = "FCGI_MAX_REQS";
     public static final String MULTIPLEX_CONNECTIONS_KEY = "FCGI_MPXS_CONNS";
 }
