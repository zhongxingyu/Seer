 package com.rackspace.api.clients.veracode.responses;
 
 import java.io.IOException;
 
 import com.rackspace.api.clients.veracode.VeracodeApiException;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.util.EntityUtils;
 import org.w3c.dom.*;
 import org.xml.sax.SAXException;
 import javax.xml.parsers.*;
 import javax.xml.xpath.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: john.madrid
  * Date: 4/27/11
  * Time: 12:09 PM
  * To change this template use File | Settings | File Templates.
  */
 public abstract class AbstractXmlResponse {
 
     protected Document doc;
     protected XPath xpath;
 
     public AbstractXmlResponse(HttpResponse response) throws VeracodeApiException {
         DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = null;
 
         try {
             builder = domFactory.newDocumentBuilder();
         } catch (ParserConfigurationException e) {
             throw new RuntimeException("Xml Configuration Issue parsing response", e);
         }
 
         HttpEntity responseEntity = extractResponse(response);
 
         try {
            this.doc = builder.parse(responseEntity.getContent());
            EntityUtils.consume(responseEntity);
         } catch (SAXException e) {
             throw new VeracodeApiException("Could not Parse Response.", e);
         } catch (IOException e) {
             throw new VeracodeApiException("Could not Parse Response.", e);
         }
 
         XPathFactory factory = XPathFactory.newInstance();
         this.xpath = factory.newXPath();
     }
 
     private HttpEntity extractResponse(HttpResponse response) throws VeracodeApiException {
         HttpEntity responseEntity = response.getEntity();
 
        if(response.getStatusLine()==null || responseEntity==null){
             throw new VeracodeApiException("Response was null");
         }
 
        if(response.getStatusLine().getStatusCode()!=200){
            throw new VeracodeApiException("API Call failed with " + response.getStatusLine().getStatusCode());
        }

         return responseEntity;
     }
 }
