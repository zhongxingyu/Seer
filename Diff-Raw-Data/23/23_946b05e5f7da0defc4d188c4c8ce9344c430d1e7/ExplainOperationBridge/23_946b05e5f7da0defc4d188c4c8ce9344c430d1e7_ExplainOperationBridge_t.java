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
 
 import gov.loc.www.zing.srw.ExplainRequestType;
 import gov.loc.www.zing.srw.ExplainResponseType;
 import gov.loc.www.zing.srw.srw_bindings.ExplainSoapBindingStub;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.rmi.RemoteException;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 
 import org.apache.axis.AxisFault;
 import org.apache.axis.encoding.SerializationContext;
 import org.apache.axis.encoding.Serializer;
 import org.apache.axis.types.URI.MalformedURIException;
 
 /**
  * This bridge executes an SRW Explain operation call.
  */
 public class ExplainOperationBridge extends V1Bridge implements OperationBridge
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
      * Execute the explain operation
      * 
      * @param parameters map of paramaters to be sent
     * @param serviceUrl the URL to the web service
      * @return the response as XML
      */
     public String execute(Map parameters, java.net.URL serviceUrl)
     {
         try
         {
             // create the request object setting the version to 1.1, record
             // packing to xml
             ExplainRequestType request = new ExplainRequestType();
             request.setVersion("1.1");
             request.setRecordPacking("xml");
 
             // try and get all the explain paramaters from the supplied map
             String recordPacking = (String) parameters.get(RECORD_PACKING_KEY);
             String stylesheet = (String) parameters.get(STYLESHEET_KEY);
 
             // if we have a value for recordPacking then set in the request
             if (recordPacking != null && recordPacking.length() > 0)
             {
                 request.setRecordPacking(recordPacking);
             }
 
             // TODO ADD EXTRA REQUEST DATA HANDLING TO EXPLAIN BRIDGE
 
             // create the binding stubb and execute the request
             ExplainSoapBindingStub binding = new ExplainSoapBindingStub(serviceUrl, null);
             ExplainResponseType response = binding.explainOperation(request);
             response.setEchoedExplainRequest(request);
             // make sure we got a response
             if (response == null)
             {
                 return createExplainDiagnosticResponse("2", "No response from server", "", null);
             }
             // obtain the serialiser to return this to XML
             QName explainQName = new QName("http://www.loc.gov/zing/srw/", "explainResponse");
             Serializer responseSer = ExplainResponseType.getSerializer(null, ExplainResponseType.class, explainQName);
             StringWriter responseWriter = new StringWriter();
             // switch pretty print off
             SerializationContext context = new SerializationContext(responseWriter);
             context.setPretty(false);
             responseSer.serialize(explainQName, null, response, context);
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
             return createExplainDiagnosticResponse("6", "Stylesheet URI is invalid", "stylesheet", exc);
         }
         catch (AxisFault exc)
         {
             exc.printStackTrace();
             return createExplainDiagnosticResponse("2", "Unable to contact server", null, exc);
         }
 
         catch (RemoteException exc)
         {
             exc.printStackTrace();
             return createExplainDiagnosticResponse("2", "Unable to contact server", null, exc);
         }
         catch (IOException exc)
         {
             exc.printStackTrace();
             return createExplainDiagnosticResponse("1", "Unable to parse SRW response", "", exc);
         }
     }
 }
