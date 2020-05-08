 package org.esgf.metadata;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.ws.http.HTTPException;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.jdom.Element;
 import org.jdom.output.XMLOutputter;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 
 @Controller
 @RequestMapping(value="/metadataview_files")
 public class FileExtractorController {
 
     private static String searchAPIURL = "http://localhost:8081/esg-search/search?";
     
     @SuppressWarnings("unchecked")
     @RequestMapping(method=RequestMethod.GET)
     public @ResponseBody String getMetadataFiles(final HttpServletRequest request) {
 
         String id = request.getParameter("id");
         
         
 
         String marker = "\"response\":";
         
         String responseBody = null;
         
         // create an http client
         HttpClient client = new HttpClient();
 
         //attact the dataset id to the query string
         GetMethod method = new GetMethod(searchAPIURL);
         
         
         
         String solrQueryString = "";
         
         //add the dataset to the query string
         try {
             solrQueryString += "format=application%2Fsolr%2Bjson&type=File&dataset_id=" + URLEncoder.encode(id,"UTF-8").toString();
             //System.out.println("\nthis.solrQueryString->\t" + URLEncoder.encode(dataset_id,"UTF-8").toString());
             //System.out.println("\n\tthis.solrQueryString->\t" + solrQueryString);
             
         } catch (UnsupportedEncodingException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         
         method.setQueryString(solrQueryString);
         
         method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                 new DefaultHttpMethodRetryHandler(3, false));
         
         
         
         try {
             // execute the method
             int statusCode = client.executeMethod(method);
 
             if (statusCode != HttpStatus.SC_OK) {
                     System.out.println("Method failed: " + method.getStatusLine());
 
             }
 
             // read the response
             responseBody = method.getResponseBodyAsString();
         } catch (HTTPException e) {
             System.out.println("Fatal protocol violation");
             e.printStackTrace();
         } catch (IOException e) {
             System.out.println("Fatal transport error");
             e.printStackTrace();
         } finally {
             method.releaseConnection();
         }
 
       //just get the important part of the response (i.e. leave off the header and the facet info)
         int start = responseBody.lastIndexOf(marker) + marker.length();
         int end = responseBody.length();
         String responseString = responseBody.substring(start,end);
         
 
         List<String> values = new ArrayList<String>();
         List<String> sizes = new ArrayList<String>();
 
         JSONObject jsonResponse = null;
         JSONArray jsonArray = null;
         String numFound = null;
         try {
             
             jsonResponse = new JSONObject(responseString);
             
             jsonArray = jsonResponse.getJSONArray("docs");
             
             
             
             for(int i=0;i<jsonArray.length();i++) {
                 JSONObject elem = null;
                 try {
                     elem = jsonArray.getJSONObject(i);
                 } catch (JSONException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 Iterator iter = elem.sortedKeys();
                 
                 
                 while(iter.hasNext()) {
                     String key = (String)iter.next();
                     
                     if(key.equals("id")) {
                         String valueString = "";
                         try {
                             valueString = (String)elem.getString(key);
                         } catch (JSONException e) {
                             // TODO Auto-generated catch block
                             e.printStackTrace();
                         }
                         values.add(valueString);
                     } else if(key.equals("size")) {
                         String valueString = "";
                         try {
                             valueString = (String)elem.getString(key);
                         } catch (JSONException e) {
                             // TODO Auto-generated catch block
                             e.printStackTrace();
                         }
                         sizes.add(valueString);
                         
                         
                     }
                     
                     
                 }
                 
                 
             }
 
             
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             System.out.println("Problem converting Solr responseq to json string - getDocElement");
             //e.printStackTrace();
         }
        

        Element filesEl = new Element("files");
        
        for(int i=0;i<values.size();i++) {
            Element fileEl = new Element("file");

            Element fileIdEl = new Element("fileId");
            fileIdEl.addContent(values.get(i));
            fileEl.addContent(fileIdEl);
 
            Element sizeEl = new Element("size");
            sizeEl.addContent(sizes.get(i));
            fileEl.addContent(sizeEl);
            
            filesEl.addContent(fileEl);
        }
         
        
        
        

        /*
         Element filesEl = new Element("files");
        
         for(int i=0;i<values.size();i++) {
             //System.out.println("i: " + values.get(i));
             Element fileIdEl = new Element("fileId");
             fileIdEl.addContent(values.get(i));
             filesEl.addContent(fileIdEl);
             Element sizeEl = new Element("size");
             sizeEl.addContent(sizes.get(i));
             filesEl.addContent(sizeEl);
         }
        */
         
         String xml = "";
         
 
         XMLOutputter outputter = new XMLOutputter();
         xml = outputter.outputString(filesEl);
         
 
         JSONObject json = null;
         
         try {
             json = XML.toJSONObject(xml);
         } catch (JSONException e) {
             System.out.println("Problem in toJSONObject");
             e.printStackTrace();
         }
         
         return json.toString();
     }
     
 }
