 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.gooddata.chargify;
 
 import com.gooddata.exception.HttpMethodException;
 import com.gooddata.util.CSVWriter;
 import com.gooddata.util.FileUtil;
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.DeleteMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.log4j.Logger;
 import org.jaxen.JaxenException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.soap.SOAPException;
 import javax.xml.xpath.*;
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.*;
 
 /**
  * Java wrapper of selected MS Sugar CRM Online web services
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class ChargifyWrapper {
 
     private static Logger l = Logger.getLogger(ChargifyWrapper.class);
 
     private final static String HTTPS = "https://";
 
 
     private final static String CHARGIFY_AUTH_REALM = "Chargify API";
 
     private final static String CHARGIFY_ENDPOINT = "chargify.com";
 
     private static final int PAGE_COUNT = 20;
 
 
     // Chargify domain
     private String domain;
     // Chargify API token
     private String apiToken;
 
     private HttpClient client;
 
 
     /**
      * Constructor
      *
      * @param hostName Chargify domain
      * @param apiToken Chargify password
      */
     public ChargifyWrapper(String hostName, String apiToken) {
         setDomain(hostName);
         setApiToken(apiToken);
         client = new HttpClient();
 
         final String proxyHost = System.getProperty("http.proxyHost");
         final int proxyPort = System.getProperty("http.proxyPort") == null
                 ? 8080 : Integer.parseInt(System.getProperty("http.proxyPort"));
 
         if (proxyHost != null) {
             client.getHostConfiguration().setProxy(proxyHost, proxyPort);
         }
         client.getHostConfiguration().setHost(getDomain());
 
         client.getState().setCredentials(
                 new AuthScope(getDomain() + "." + CHARGIFY_ENDPOINT, 443, CHARGIFY_AUTH_REALM),
                 new UsernamePasswordCredentials(getApiToken(), "x")
         );
     }
 
 
     public int getAllData(String entity, String[] fields, String csvFile)
             throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
         int cnt = 0;
         CSVWriter cw = FileUtil.createUtf8CsvEscapingWriter(new File(csvFile));
         try {
             int pageNumber = 1;
             String cookie = "";
             boolean hasNext = true;
             while (hasNext) {
                 List<Map<String,String>> ret = new ArrayList<Map<String,String>>();
                 getData(entity, pageNumber++, ret);
                 for(Map<String,String>  m : ret) {
                     String[] row = new String[fields.length];
                     for(int i=0; i<fields.length; i++) {
                         row[i] = m.get(fields[i]);
                     }
                     cw.writeNext(row);
                 }
                 int count = ret.size();
                 cnt += count;
                 cw.flush();
                 if(count < PAGE_COUNT)
                     hasNext = false;
             }
         }
         finally {
             cw.close();
         }
         return cnt;
     }
 
 
     public void getData(String entity, int page, List<Map<String, String>> ret)
             throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
         String path = HTTPS + getDomain() + "." + CHARGIFY_ENDPOINT + "/" + entity + ".xml?page="+page;
         GetMethod m = createGetMethod(path);
         int rc = executeHttpMethod(m);
         if (rc == HttpStatus.SC_OK) {
             String payload = readRespone(m);
             //System.err.println(payload);
             DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
             domFactory.setNamespaceAware(true);
             DocumentBuilder builder = domFactory.newDocumentBuilder();
             Document doc = builder.parse(new InputSource(new StringReader(payload)));
             XPath xpath = XPathFactory.newInstance().newXPath();
             XPathExpression expr = xpath.compile("//"+entity);
             Object result = expr.evaluate(doc, XPathConstants.NODESET);
             NodeList nodes = (NodeList) result;
             if(nodes != null && nodes.getLength() > 0) {
                 Node root = nodes.item(0);
                 NodeList rows = root.getChildNodes();
                 if(rows != null && rows.getLength() > 0) {
                     for(int i=0; i < rows.getLength(); i++) {
                         Node row = rows.item(i);
                         if(row != null && row instanceof Element) {
                             NodeList columns = row.getChildNodes();
                             Map<String, String> r = new HashMap<String, String>();
                             if(columns != null && columns.getLength() > 0) {
                                 for(int j=0; j < columns.getLength(); j++) {
                                     Node name = columns.item(j);
                                     boolean isNested = false;
                                     if(name != null && name instanceof Element) {
                                         NodeList nestedNodes = name.getChildNodes();
                                         if(nestedNodes != null && nestedNodes.getLength() > 0) {
                                             for(int k=0; k < nestedNodes.getLength(); k++) {
                                                 Node nestedNode = nestedNodes.item(k);
                                                 if(nestedNode != null && nestedNode instanceof Element) {
                                                     isNested = true;
                                                     Node value = nestedNode.getFirstChild();
                                                     if(value != null) {
                                                         r.put(name.getNodeName()+"_"+nestedNode.getNodeName(), value.getNodeValue());
                                                     }
                                                     else {
                                                         r.put(name.getNodeName()+"_"+nestedNode.getNodeName(), "");
                                                     }
                                                 }
                                             }
                                         }
                                         if(!isNested) {
                                             Node value = name.getFirstChild();
                                             if(value != null) {
                                                 r.put(name.getNodeName(), value.getNodeValue());
                                             }
                                             else {
                                                 r.put(name.getNodeName(), "");
                                             }
                                         }
                                     }
                                 }
                             }
                             else {
                                 throw new IOException("getData: No columns in the row.");
                             }
                             ret.add(r);
                         }
                     }
                 }
             }
         } else {
             System.err.println("HTTP response " + rc);
         }
     }
 
     /**
      * Executes HttpMethod and test if the response if 200(OK)
      *
      * @param method the HTTP method
      * @return HTTP return code
      * @throws com.gooddata.exception.HttpMethodException
      *
      */
     private int executeHttpMethod(HttpMethod method) throws HttpMethodException {
         try {
             client.executeMethod(method);
             return method.getStatusCode();
         } catch (HttpException e) {
             l.debug("Error invoking Chargify REST API.", e);
             throw new HttpMethodException("Error invoking Chargify REST API.", e);
         } catch (IOException e) {
             l.debug("Error invoking GoodData REST API.", e);
             throw new HttpMethodException("Error invoking Chargify REST API.", e);
         }
     }
 
     private String readRespone(HttpMethod m) throws IOException {
         return m.getResponseBodyAsString();
     }
 
 
     private static GetMethod createGetMethod(String path) {
         return configureHttpMethod(new GetMethod(path));
     }
 
     private static PostMethod createPostMethod(String path) {
         return configureHttpMethod(new PostMethod(path));
     }
 
     private static DeleteMethod createDeleteMethod(String path) {
         return configureHttpMethod(new DeleteMethod(path));
     }
 
     private static <T extends HttpMethod> T configureHttpMethod(T request) {
         request.setRequestHeader("Content-Type", "text/xml");
         request.setRequestHeader("Accept", "text/xml");
        request.setRequestHeader("User-Agent", "GoodData CL/1.2.20-BETA");
         return request;
     }
 
 
 // Getters and Setters
 
     public String getDomain() {
         return domain;
     }
 
     public void setDomain(String domain) {
         this.domain = domain;
     }
 
     public String getApiToken() {
         return apiToken;
     }
 
     public void setApiToken(String apiToken) {
         this.apiToken = apiToken;
     }
 }
