 /**
  * JAFER Toolkit Project. Copyright (C) 2002, JAFER Toolkit Project, Oxford
  * University. This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of the License,
  * or (at your option) any later version. This library is distributed in the
  * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
  * the GNU Lesser General Public License for more details. You should have
  * received a copy of the GNU Lesser General Public License along with this
  * library; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package org.jafer.sru.bridge;
 
 import gov.loc.www.zing.srw.SearchRetrieveRequestType;
 import gov.loc.www.zing.srw.SearchRetrieveResponseType;
 import gov.loc.www.zing.srw.srw_bindings.SRWSoapBindingStub;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.rmi.RemoteException;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 
 import org.apache.axis.AxisFault;
 import org.apache.axis.encoding.SerializationContext;
 import org.apache.axis.encoding.Serializer;
 import org.apache.axis.types.NonNegativeInteger;
 import org.apache.axis.types.PositiveInteger;
 import org.apache.axis.types.URI.MalformedURIException;
 
 /**
  * This bridge executes an SRW Search Retrieve operation call.
  */
 public class SearchRetrieveOperationBridge extends V1Bridge implements OperationBridge
 {
 
     /**
      * Stores a reference to key used to find the recordPacking in paramaters
      */
     private static final String RECORD_PACKING_KEY = "recordPacking";
 
     /**
      * Stores a reference to key used to find the stylesheet in paramaters
      */
     private static final String STYLESHEET_KEY = "stylesheet";
 
     /**
      * Stores a reference to key used to find the query in paramaters
      */
     private static final String QUERY_KEY = "query";
 
     /**
      * Stores a reference to key used to find the startRecord in paramaters
      */
     private static final String START_RECORD_KEY = "startRecord";
 
     /**
      * Stores a reference to key used to find the maximumRecords in paramaters
      */
     private static final String MAXIMUM_RECORDS_KEY = "maximumRecords";
 
     /**
      * Stores a reference to key used to find the recordSchema in paramaters
      */
     private static final String RECORD_SCHEMA_KEY = "recordSchema";
 
     /**
      * Stores a reference to key used to find the recordXPath in paramaters
      */
     private static final String RECORD_XPATH_KEY = "recordXPath";
 
     /**
      * Stores a reference to key used to find the resultSetTTL in paramaters
      */
     private static final String RESULT_SET_TTL_KEY = "resultSetTTL";
 
     /**
      * Stores a reference to key used to find the sortKeys in paramaters
      */
     private static final String SORT_KEYS_KEY = "sortKeys";
 
     /**
      * Execute the explain operation
      *
      * @param parameters map of paramaters to be sent
     * @param the URL to the web service
      * @return the response as XML
      */
     public String execute(Map parameters, java.net.URL serviceUrl)
     {
         try
         {
             // create the request object setting the version to 1.1, record
             // packing to xml
             SearchRetrieveRequestType request = new SearchRetrieveRequestType();
             request.setVersion("1.1");
             request.setRecordPacking("xml");
 
             // try and get all the search retrieve paramaters from the supplied
             // map
             String query = (String) parameters.get(QUERY_KEY);
             String maxRecords = (String) parameters.get(MAXIMUM_RECORDS_KEY);
             String startRecord = (String) parameters.get(START_RECORD_KEY);
             String recordPacking = (String) parameters.get(RECORD_PACKING_KEY);
             String recordSchema = (String) parameters.get(RECORD_SCHEMA_KEY);
             String recordXPath = (String) parameters.get(RECORD_XPATH_KEY);
             String resultSetTTL = (String) parameters.get(RESULT_SET_TTL_KEY);
             String sortKeys = (String) parameters.get(SORT_KEYS_KEY);
             String stylesheet = (String) parameters.get(STYLESHEET_KEY);
 
             // if we have a value for query then set in the request
             if (query != null && query.length() > 0)
             {
                 request.setQuery(query);
             }
             else
             {
                 return createSearchDiagnosticResponse("7", "No query specified", "query", null);
             }
             // if we have a value for maxRecords then set in the request
             if (maxRecords != null && maxRecords.length() > 0)
             {
                 request.setMaximumRecords(new NonNegativeInteger(maxRecords));
             }
             // if we have a value for startRecord then set in the request
             if (startRecord != null && startRecord.length() > 0)
             {
                 request.setStartRecord(new PositiveInteger(startRecord));
             }
             // if we have a value for recordPacking then set in the request
             if (recordPacking != null && recordPacking.length() > 0)
             {
                 request.setRecordPacking(recordPacking);
             }
             // if we have a value for recordSchema then set in the request
             if (recordSchema != null && recordSchema.length() > 0)
             {
                 request.setRecordSchema(recordSchema);
             }
             // if we have a value for recordXPath then set in the request
             if (recordXPath != null && recordXPath.length() > 0)
             {
                 request.setRecordXPath(recordXPath);
             }
             // if we have a value for resultSetTTL then set in the request
             if (resultSetTTL != null && resultSetTTL.length() > 0)
             {
                 request.setResultSetTTL(new NonNegativeInteger(resultSetTTL));
             }
             // if we have a value for sortKeys then set in the request
             if (sortKeys != null && sortKeys.length() > 0)
             {
                 request.setSortKeys(sortKeys);
             }
             // create the binding stubb and execute the request
             SRWSoapBindingStub binding = new SRWSoapBindingStub(serviceUrl, null);
             SearchRetrieveResponseType response = binding.searchRetrieveOperation(request);
             // make sure we got a response
             if (response == null)
             {
                 return createSearchDiagnosticResponse("2", "No response from server", "", null);
             }
             // obtain the serialiser to return this to XML
             QName searchQName = new QName("http://www.loc.gov/zing/srw/", "searchRetrieveResponse");
             Serializer ser = SearchRetrieveResponseType.getSerializer(null, SearchRetrieveResponseType.class, searchQName);
             StringWriter responseWriter = new StringWriter();
             ser.serialize(searchQName, null, response, new SerializationContext(responseWriter));
             String SRWResult = responseWriter.getBuffer().toString();
             // if we have a value for stylesheet then set in the XML part of
             // reponse
             if (stylesheet != null && stylesheet.length() > 0)
             {
                 // add a stylesheet ref infront of returned result
                 return "<?xml-stylesheet type=\"text/xsl\" href=\"" + stylesheet + "\"?>" + SRWResult;
             }
             return SRWResult;
         }
 
         catch (MalformedURIException exc)
         {
             exc.printStackTrace();
             return createSearchDiagnosticResponse("6", "Stylesheet URI is invalid", "stylesheet", exc);
         }
         catch (AxisFault exc)
         {
             exc.printStackTrace();
             return createSearchDiagnosticResponse("2", "Unable to contact server", null, exc);
         }
 
         catch (RemoteException exc)
         {
             exc.printStackTrace();
             return createSearchDiagnosticResponse("2", "Unable to contact server", null, exc);
         }
         catch (IOException exc)
         {
             exc.printStackTrace();
             return createSearchDiagnosticResponse("1", "Unable to parse SRW response", "", exc);
         }
     }
 
 }
