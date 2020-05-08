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
 
 /**
  * This bridge executes an SRW scan operation call.
  */
 import gov.loc.www.zing.srw.ScanRequestType;
 import gov.loc.www.zing.srw.ScanResponseType;
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
 
 public class ScanOperationBridge extends V1Bridge implements OperationBridge
 {
 
     /**
      * Stores a reference to key used to find the scanClause in paramaters
      */
     private static final String SCAN_CLAUSE_KEY = "scanClause";
 
     /**
      * Stores a reference to key used to find the stylesheet in paramaters
      */
     private static final String STYLESHEET_KEY = "stylesheet";
 
     /**
      * Stores a reference to key used to find the responsePosition in paramaters
      */
     private static final String RESPONSE_POSITION_KEY = "responsePosition";
 
     /**
      * Stores a reference to key used to find the maximumTerms in paramaters
      */
     private static final String MAXIMUM_TERMS_KEY = "maximumTerms";
 
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
             // create the request object setting the version to 1.1
             ScanRequestType request = new ScanRequestType();
             request.setVersion("1.1");
 
             // try and get all the search retrieve paramaters from the supplied
             // map
             String scanClause = (String) parameters.get(SCAN_CLAUSE_KEY);
             String responsePosition = (String) parameters.get(RESPONSE_POSITION_KEY);
             String maximumTerms = (String) parameters.get(MAXIMUM_TERMS_KEY);
             String stylesheet = (String) parameters.get(STYLESHEET_KEY);
             // if we have a value for scanClause then set in the request
             if (scanClause != null && scanClause.length() > 0)
             {
                 request.setScanClause(scanClause);
             }
             else
             {
                 // throw an error as this is a mandatory parameter
                 return createScanDiagnosticResponse("7", "No scan clause specified", "scanClause", null);
 
             }
             // if we have a value for responsePosition then set in the request
             if (responsePosition != null && responsePosition.length() > 0)
             {
                 request.setResponsePosition(new NonNegativeInteger(responsePosition));
             }
             // if we have a value for maximumTerms then set in the request
             if (maximumTerms != null && maximumTerms.length() > 0)
             {
                 request.setMaximumTerms(new PositiveInteger(maximumTerms));
             }
 
             // create the binding stubb and execute the request
             SRWSoapBindingStub binding = new SRWSoapBindingStub(serviceUrl, null);
             ScanResponseType response = binding.scanOperation(request);
             // make sure we got a response
             if (response == null)
             {
                 return createScanDiagnosticResponse("2", "No response from server", "", null);
             }
             // obtain the serialiser to return this to XML
             QName scanQName = new QName("http://www.loc.gov/zing/srw/", "scanResponse");
             Serializer ser = ScanResponseType.getSerializer(null, ScanResponseType.class, scanQName);
             StringWriter responseWriter = new StringWriter();
             ser.serialize(scanQName, null, response, new SerializationContext(responseWriter));
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
             return createScanDiagnosticResponse("6", "Stylesheet URI is invalid", "stylesheet", exc);
         }
         catch (AxisFault exc)
         {
             exc.printStackTrace();
             return createScanDiagnosticResponse("2", "Unable to contact server", null, exc);
         }
 
         catch (RemoteException exc)
         {
             exc.printStackTrace();
             return createScanDiagnosticResponse("2", "Unable to contact server", null, exc);
         }
         catch (IOException exc)
         {
             exc.printStackTrace();
             return createScanDiagnosticResponse("1", "Unable to parse SRW response", "", exc);
         }
     }
 
 }
