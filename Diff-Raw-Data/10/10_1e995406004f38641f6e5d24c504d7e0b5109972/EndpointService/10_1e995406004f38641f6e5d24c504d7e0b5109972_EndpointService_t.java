 package org.motechproject.commcarehq.service;
 
 import com.sun.org.apache.xerces.internal.parsers.DOMParser;
 import org.joda.time.DateTime;
 import org.motechproject.commcarehq.domain.AlertDocCase;
 import org.motechproject.commcarehq.repository.AllAlertDocCases;
 import org.motechproject.util.StringUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.StringReader;
 
 @Controller
 @RequestMapping("/**")
 public class EndpointService {
 
     private AllAlertDocCases allCareCases;
 
     @Autowired
     public EndpointService(AllAlertDocCases allCareCases) {
         this.allCareCases = allCareCases;
     }
 
     @RequestMapping(value="/", method= RequestMethod.GET)
     public void index(HttpServletResponse response) {
         try {
             response.getOutputStream().print("hello I am up and running. Relax. :)");
         } catch (IOException e) {
         }
     }
 
     @RequestMapping(value="/endpoint",method= RequestMethod.POST)
     public void endpoint(@RequestBody String xmlDocument, HttpServletResponse response) {
         ValidationResponse validationResponse = processDocument(xmlDocument);
         validationResponse.sendResponse(response);
     }
     
     
 
     private AlertDocCase careCase(String xmlDocument) {
         if(StringUtil.isNullOrEmpty(xmlDocument)) {
             throw new IllegalArgumentException();
         }
 
         DOMParser parser = new DOMParser();
 
         InputSource inputSource = new InputSource();
         inputSource.setCharacterStream(new StringReader(xmlDocument));
 
         try {
             parser.parse(inputSource);
             Document document = parser.getDocument();
            NodeList caseList = document.getDocumentElement().getElementsByTagName("case");
            if(caseList.getLength() == 0) {
                throw new MalformedXmlException();
            }
            Element caseElement = (Element) caseList.item(0);
            return new AlertDocCase(caseElement.getAttribute("case_id"), xmlDocument, DateTime.now());
 
         } catch (IOException ex) {
             throw new MalformedXmlException();
         }
         catch (SAXException ex){
             throw new MalformedXmlException();
         }
 
     }
 
     private ValidationResponse processDocument(String xmlDocument) {
         try {
             AlertDocCase careCase = careCase(xmlDocument);
             allCareCases.add(careCase);
         } catch (IllegalArgumentException ex) {
             return ValidationResponse.MISSING;
         }
         catch (MalformedXmlException ex){
             return ValidationResponse.MALFORMED;
         } catch (Exception ex) {
             return ValidationResponse.INTERNAL;
         }
 
         return ValidationResponse.SUCCESS;
     }
 }
