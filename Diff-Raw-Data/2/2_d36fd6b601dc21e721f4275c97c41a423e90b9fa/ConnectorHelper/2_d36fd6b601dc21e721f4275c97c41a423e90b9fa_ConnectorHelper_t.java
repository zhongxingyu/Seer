 package com.socialcomputing.wps.server.planDictionnary.connectors.utils;
 
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 
 import org.jdom.Element;
 
 import com.socialcomputing.wps.server.planDictionnary.connectors.JMIException;
 
 public abstract class ConnectorHelper {
 
     public abstract void readObject(Element element);
     
     /**
      * Open connections to read data from the backend
      * 
      * @param planType
      * @param wpsparams
      * @throws JMIException
      */
     public abstract void openConnections(int planType, Hashtable<String, Object> wpsparams) throws JMIException;
 
     /**
      * Close all opened connections
      * 
      * @throws JMIException
      */
     public abstract void closeConnections() throws JMIException;
     
     public static String ReplaceParameter(NameValuePair pair, Hashtable<String, Object> wpsparams) {
         if( pair.getValue() == null) return "";
         StringBuilder result = new StringBuilder();
         StringTokenizer st = new StringTokenizer( pair.getValue(), "{}", false);
         while( st.hasMoreTokens()) {
             String token = st.nextToken();
             if( token.startsWith( "$")) {
                 String val = ( String)wpsparams.get( token.substring( 1));
                 if( val == null)
                     val = ( String)wpsparams.get( token);
                 if( val != null)
                     result.append( val);
             }
             else
                 result.append( token);
         }
        return result.length() == 0 ? (pair.getDefaultValue() == null ?  "" : pair.getDefaultValue()): result.toString();
     }
 
     public static String ReplaceParameter(String value, Hashtable<String, Object> wpsparams) throws JMIException {
         if( value == null) return "";
         StringBuilder result = new StringBuilder();
         StringTokenizer st = new StringTokenizer( value, "{}", false);
         while( st.hasMoreTokens()) {
             String token = st.nextToken();
             if( token.startsWith( "$")) {
                 String val = ( String)wpsparams.get( token.substring( 1));
                 if( val == null)
                     val = ( String)wpsparams.get( token);
                 if( val == null)
                     throw new JMIException( "Parameter " + token + " is unknown");
                 result.append( val);
             }
             else
                 result.append( token);
         }
         return result.toString();
     }
 }
