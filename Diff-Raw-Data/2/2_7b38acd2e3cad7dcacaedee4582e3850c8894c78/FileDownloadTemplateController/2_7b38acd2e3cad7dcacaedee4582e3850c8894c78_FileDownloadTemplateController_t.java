 
 /*****************************************************************************
  * Copyright  2011 , UT-Battelle, LLC All rights reserved
  *
  * OPEN SOURCE LICENSE
  *
  * Subject to the conditions of this License, UT-Battelle, LLC (the
  * Licensor) hereby grants to any person (the Licensee) obtaining a copy
  * of this software and associated documentation files (the "Software"), a
  * perpetual, worldwide, non-exclusive, irrevocable copyright license to use,
  * copy, modify, merge, publish, distribute, and/or sublicense copies of the
  * Software.
  *
  * 1. Redistributions of Software must retain the above open source license
  * grant, copyright and license notices, this list of conditions, and the
  * disclaimer listed below.  Changes or modifications to, or derivative works
  * of the Software must be noted with comments and the contributor and
  * organizations name.  If the Software is protected by a proprietary
  * trademark owned by Licensor or the Department of Energy, then derivative
  * works of the Software may not be distributed using the trademark without
  * the prior written approval of the trademark owner.
  *
  * 2. Neither the names of Licensor nor the Department of Energy may be used
  * to endorse or promote products derived from this Software without their
  * specific prior written permission.
  *
  * 3. The Software, with or without modification, must include the following
  * acknowledgment:
  *
  *    "This product includes software produced by UT-Battelle, LLC under
  *    Contract No. DE-AC05-00OR22725 with the Department of Energy.
  *
  * 4. Licensee is authorized to commercialize its derivative works of the
  * Software.  All derivative works of the Software must include paragraphs 1,
  * 2, and 3 above, and the DISCLAIMER below.
  *
  *
  * DISCLAIMER
  *
  * UT-Battelle, LLC AND THE GOVERNMENT MAKE NO REPRESENTATIONS AND DISCLAIM
  * ALL WARRANTIES, BOTH EXPRESSED AND IMPLIED.  THERE ARE NO EXPRESS OR
  * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE,
  * OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY PATENT, COPYRIGHT,
  * TRADEMARK, OR OTHER PROPRIETARY RIGHTS, OR THAT THE SOFTWARE WILL
  * ACCOMPLISH THE INTENDED RESULTS OR THAT THE SOFTWARE OR ITS USE WILL NOT
  * RESULT IN INJURY OR DAMAGE.  The user assumes responsibility for all
  * liabilities, penalties, fines, claims, causes of action, and costs and
  * expenses, caused by, resulting from or arising out of, in whole or in part
  * the use, storage or disposal of the SOFTWARE.
  *
  *
  ******************************************************************************/
 
 /**
  *
  * @author John Harney (harneyjf@ornl.gov)
  *
  */
 
 package org.esgf.web;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.ws.http.HTTPException;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.log4j.Logger;
 import org.esgf.metadata.JSONArray;
 import org.esgf.metadata.JSONException;
 import org.esgf.metadata.JSONObject;
 import org.esgf.metadata.XML;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("/solrfileproxy")
 public class FileDownloadTemplateController {
 
     private static String solrURL="http://localhost:8983/solr/";
     private final static Logger LOG = Logger.getLogger(FileDownloadTemplateController.class);
 
     //right now the prefix for the solr query is hard coded
     private final static String filePrefix="q=*%3A*&json.nl=map&fq=type%3AFile&fq=parent_id:"; 
     
     //debug flag
     private final static boolean debugFlag = false;
     
     @RequestMapping(method=RequestMethod.GET)
     public @ResponseBody String doGet(HttpServletRequest request, HttpServletResponse response) throws JSONException {
         LOG.debug("doGet");
         return convertTemplateFormat(request, response);
     }
 
     @RequestMapping(method=RequestMethod.POST)
     public @ResponseBody String doPost(HttpServletRequest request, HttpServletResponse response) throws JSONException {
         LOG.debug("doPost");
         return convertTemplateFormat(request, response);
     }
 
     /*
      * Conversion
      */
     
     //responsebody looks like this
     //  <responseHeader>
     //  </responseHeader>
     //  <response>
     //   <doc>
     //    <id>
     //    <parent_id>
     //    <service>
     //    <title>
     //    <size>
     //   </doc>
     //  </response>
     
     //----->
     
     //convert response body to the following format
     //<doc>
     //    <file*>
     //      <file_id></file_id>
     //      <file_size></file_size>
     //      <file_url></file_url>
     //      <services>
     //        <service></service>
     //        <service></service>
     //      </services>
     //    </file>
     //<doc>
     
     
     private String convertTemplateFormat(HttpServletRequest request, HttpServletResponse response) throws JSONException {
 
         String[] names = request.getParameterValues("id[]");
         
         String id = "";
         String responseBody = "";
         JSONObject responseBodyJSON = null;
         String xmlOutput = "";
 
         SAXBuilder builder = new SAXBuilder();
         Document document = null;
         
         document = new Document(new Element("response"));
         if(names != null) {
             
           //traverse all the dataset ids given by the array
             for(int i=0;i<names.length;i++) {
                 
                 id = names[i];
                 responseBody = getResponseBody(id);
                 responseBodyJSON = new JSONObject(responseBody);
 
                 //get the different json texts here
                 JSONObject responseJSON = new JSONObject(responseBodyJSON.get("response").toString());
                 JSONArray docsJSON = responseJSON.getJSONArray("docs");
                 
                 try{
                 //  create <doc> element
                     Element docEl = new Element("doc");
                     
                 //  create doc/dataset_id element
                     Element dataset_idEl = new Element("dataset_id");
                     dataset_idEl.addContent(id);
                     docEl.addContent(dataset_idEl);
          
                     
                 //  for each file found
                     for(int j=0;j<docsJSON.length();j++) {
                         JSONObject docJSON = new JSONObject(docsJSON.get(j).toString());
                         Element fileEl = createFileElement(docJSON);
                         docEl.addContent(fileEl);
                     }
                     
                     
                     document.getRootElement().addContent(docEl);
                 }
                 catch(Exception e) {
                     LOG.debug("\nJSON errors - investigate line 167\n");
                 }
                 
             }
         }
         
         
         XMLOutputter outputter = new XMLOutputter();
         xmlOutput = outputter.outputString(document.getRootElement());
 
         if(debugFlag) {
             LOG.debug("xmlOutput:\n " + xmlOutput);
         }
        
         
         JSONObject returnJSON = XML.toJSONObject(xmlOutput);
 
         String jsonContent = returnJSON.toString();
         if(debugFlag) {
             LOG.debug("json: \n" + returnJSON.toString());
         }
         return jsonContent;
         //return jo.toString();
     }
     
     private static String getResponseBody(String id) {
 
         String responseBody = null;
         
         String newURL = filePrefix + id;
 
 
         // create an http client
         HttpClient client = new HttpClient();
         
         
         //String urlString = solrURL + "select?" + queryString + "&wt=json";
         String urlString = solrURL + "select?" + newURL + "&wt=json";
         
         if(debugFlag) {
             LOG.debug("urlString: " + urlString);
         }
         
         GetMethod method = new GetMethod(urlString);
         
         method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                 new DefaultHttpMethodRetryHandler(3, false));
         try {
             // execute the method
             int statusCode = client.executeMethod(method);
 
             if (statusCode != HttpStatus.SC_OK) {
                 if(debugFlag) {
                     LOG.error("Method failed: " + method.getStatusLine());
                 }
             }
 
             // read the response
             responseBody = method.getResponseBodyAsString();
 
         } catch (HTTPException e) {
             LOG.error("Fatal protocol violation");
             e.printStackTrace();
         } catch (IOException e) {
             LOG.error("Fatal transport error");
             e.printStackTrace();
         } finally {
             method.releaseConnection();
         }
         
         if(responseBody != null) {
             if(debugFlag) {
                 LOG.debug("responseBody: " + responseBody);
             }
         }
         
         return responseBody;
     }
 
     public static Element createFileElement(JSONObject docJSON) throws JSONException {
         // create <file> element
         Element fileEl = new Element("file");
         
         // create file/file_id element
         Element file_idEl = new Element("file_id");
         file_idEl.addContent(docJSON.get("id").toString());
         fileEl.addContent(file_idEl);
 
         // create file/title element
         Element titleEl = new Element("title");
         titleEl.addContent(docJSON.get("title").toString());
         fileEl.addContent(titleEl);
         
         // create file/file_size element
         Element sizeEl = new Element("size");
         sizeEl.addContent(docJSON.get("size").toString());
         fileEl.addContent(sizeEl);
      
         // create file/url element
         Element urlEl = new Element("url");
         JSONArray urlsJSON = docJSON.getJSONArray("url");
         
         urlEl.addContent(urlsJSON.get(0).toString());
         fileEl.addContent(urlEl);
      
         
         // create file/services element
         Element servicesEl = new Element("services");
         
         JSONArray docsJSON = docJSON.getJSONArray("service");
         for(int i=0;i<docsJSON.length();i++) {
             Element serviceEl = new Element("service");
             String serviceStr = docsJSON.get(i).toString();
             String [] serviceTokens = serviceStr.split("|");
             //serviceEl.addContent(docsJSON.get(i).toString());
             serviceEl.addContent(serviceTokens[2]);
            //LOG.debug("service: " + serviceTokens[2]);
             servicesEl.addContent(serviceEl);
         }
         fileEl.addContent(servicesEl);
         
         return fileEl;
     }
    
 }
