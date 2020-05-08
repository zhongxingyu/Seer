 
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
  * @author John Harney (harneyjf@ornl.gov), Feiyi Wang (fwang2@ornl.gov)
  *
  * Changelog:
  *
  * The query string is encoded through encoder instead of manual string
  * The converted template will be:
  *
  * For debug only:
  *
  * The converted template:
  *
  * <response>
  *    <doc>
  *       <dataset_id> whatever </dataset_id>
  *       <file>
  *          <file_id> ... </file_id>
  *          <size> ... </size>
  *          ...
  *       </file>
  *
  *       <file> .... </file>
  *   </doc>
  * </response>
  */
 
 
 
 package org.esgf.filedownload;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.ws.http.HTTPException;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.URIException;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.httpclient.util.URIUtil;
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
 
     private static String solrURL="http://localhost:8983/solr/select";
     private final static Logger LOG = Logger.getLogger(FileDownloadTemplateController.class);
 
     //right now the prefix for the solr query is hard coded
     //the max rows to be returned is best configurable or read from props file
 
     //private final static String filePrefix="q=*%3A*&json.nl=map&fq=type%3AFile&rows=2000&fq=parent_id:";
    private final static String queryString ="q=*:*&json.nl=map&start=0&rows=2000&fq=type:File&fq=parent_id:";
 
 
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
         String xmlOutput = "";
 
         SAXBuilder builder = new SAXBuilder();
         Document document = null;
 
         String jsonContent = null;
 
         document = new Document(new Element("response"));
         if(names != null) {
 
           //traverse all the dataset ids given by the array
             for(int i=0;i<names.length;i++) {
 
                 id = names[i];
                 String marker = "\"response\":";
                 String responseRawString = getResponseBody(id);
                 int start = responseRawString.lastIndexOf(marker) + marker.length();
                 int end = responseRawString.length();
                 String extractedString = responseRawString.substring(start, end);
                 JSONObject responseBody = new JSONObject(extractedString);
                 JSONArray docsJSON = responseBody.getJSONArray("docs");
 
                 try{
                     
                     if(docsJSON.length() == 1) {
                         
                         JSONObject docJSON = new JSONObject(docsJSON.get(0).toString());
                         
                         //  create <doc> element
                             Element docEl = new Element("doc");
 
                         //  create doc/dataset_id element
                             Element dataset_idEl = new Element("dataset_id");
                             dataset_idEl.addContent(id);
                             docEl.addContent(dataset_idEl);
                         
                         
                         // create <file> element
                             Element fileEl = new Element("file");
    
                            // create file/file_id element
                            Element file_idEl = new Element("file_id");
                            file_idEl.addContent("nully");
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
    
                            JSONArray docserviceJSON = docJSON.getJSONArray("service");
                            for(int k=0;k<docserviceJSON.length();k++) {
                                Element serviceEl = new Element("service");
                                String serviceStr = docserviceJSON.get(k).toString();
                                String [] serviceTokens = serviceStr.split("|");
                                //serviceEl.addContent(docsJSON.get(i).toString());
                                serviceEl.addContent(serviceTokens[2]);
                                //LOG.debug("service: " + serviceTokens[2]);
                                servicesEl.addContent(serviceEl);
                            }
                            fileEl.addContent(servicesEl);
                        
                        
                            docEl.addContent(fileEl);
                         
                         
                         
                         
                         
 
 
                     //  for each file found
                         for(int j=0;j<docsJSON.length();j++) {
                             docJSON = new JSONObject(docsJSON.get(j).toString());
                             fileEl = createFileElement(docJSON);
                             docEl.addContent(fileEl);
                         
                         }
                         
                         
                         
                         
                         
 
                         document.getRootElement().addContent(docEl);
                         
                         XMLOutputter outputter = new XMLOutputter();
                         xmlOutput = outputter.outputString(document.getRootElement());
 
                         //LOG.debug("xmlOutput:\n " + xmlOutput);
 
 
                         JSONObject returnJSON = XML.toJSONObject(xmlOutput);
 
                         jsonContent = returnJSON.toString();
                         LOG.debug("json: \n" + returnJSON.toString());
                         
                         //assemble the json string by hand
                         
                         //return null;
                     }
                     else {
                         
                         
                         JSONObject docJSON = new JSONObject(docsJSON.get(0).toString());
                         
                         //  create <doc> element
                             Element docEl = new Element("doc");
 
                         //  create doc/dataset_id element
                             Element dataset_idEl = new Element("dataset_id");
                             dataset_idEl.addContent(id);
                             docEl.addContent(dataset_idEl);
                         
                         
                         // create <file> element
                             Element fileEl = new Element("file");
    
                            // create file/file_id element
                            Element file_idEl = new Element("file_id");
                            file_idEl.addContent("nully");
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
    
                            JSONArray docserviceJSON = docJSON.getJSONArray("service");
                            for(int k=0;k<docserviceJSON.length();k++) {
                                Element serviceEl = new Element("service");
                                String serviceStr = docserviceJSON.get(k).toString();
                                String [] serviceTokens = serviceStr.split("|");
                                //serviceEl.addContent(docsJSON.get(i).toString());
                                serviceEl.addContent(serviceTokens[2]);
                                //LOG.debug("service: " + serviceTokens[2]);
                                servicesEl.addContent(serviceEl);
                            }
                            fileEl.addContent(servicesEl);
                        
                        
                            docEl.addContent(fileEl);
                         
                         
                         
                         
                         
 
 
                     //  for each file found
                         for(int j=0;j<docsJSON.length();j++) {
                             docJSON = new JSONObject(docsJSON.get(j).toString());
                             fileEl = createFileElement(docJSON);
                             docEl.addContent(fileEl);
                         
                         }
                         
                         
                         
 
 
                         document.getRootElement().addContent(docEl);
                         
                         XMLOutputter outputter = new XMLOutputter();
                         xmlOutput = outputter.outputString(document.getRootElement());
 
                         //LOG.debug("xmlOutput:\n " + xmlOutput);
 
 
                         JSONObject returnJSON = XML.toJSONObject(xmlOutput);
 
                         jsonContent = returnJSON.toString();
                         LOG.debug("json: \n" + returnJSON.toString());
                         
                         //return jo.toString();
                     }
                
                 }
                 catch(Exception e) {
                     LOG.debug("\nJSON Error in converting template format \n");
                 }
 
             }
         }
 
         return jsonContent;
         
     }
 
     private String getResponseBody(String id)  {
 
         String responseBody = null;
 
 
         // create an http client
         HttpClient client = new HttpClient();
 
         String combinedQueryStr = queryString + id + "&wt=json";
 
         GetMethod method = new GetMethod(solrURL);
 
         try {
             method.setQueryString(URIUtil.encodeQuery(combinedQueryStr));
         } catch (URIException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                 new DefaultHttpMethodRetryHandler(3, false));
         try {
             // execute the method
             int statusCode = client.executeMethod(method);
 
             if (statusCode != HttpStatus.SC_OK) {
                     LOG.error("Method failed: " + method.getStatusLine());
 
             }
 
             // read the response
             responseBody = method.getResponseBodyAsString();
             //LOG.debug(responseBody);
         } catch (HTTPException e) {
             LOG.error("Fatal protocol violation");
             e.printStackTrace();
         } catch (IOException e) {
             LOG.error("Fatal transport error");
             e.printStackTrace();
         } finally {
             method.releaseConnection();
         }
 
 
         return responseBody;
     }
 
     public Element createFileElement(JSONObject docJSON) throws JSONException {
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
