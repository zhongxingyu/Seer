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
 
 package com.gooddata.integration.webdav;
 
 import com.gooddata.exception.HttpMethodException;
 import com.gooddata.exception.HttpMethodNotFinishedYetException;
 import com.gooddata.integration.datatransfer.GdcDataTransferAPI;
 import com.gooddata.integration.rest.configuration.NamePasswordConfiguration;
 import com.gooddata.util.NetUtil;
 import net.sf.json.JSONException;
 import net.sf.json.JSONObject;
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.jackrabbit.webdav.DavConstants;
 import org.apache.jackrabbit.webdav.DavException;
 import org.apache.jackrabbit.webdav.MultiStatus;
 import org.apache.jackrabbit.webdav.MultiStatusResponse;
 import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
 import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * GoodData Webdav API Java wrapper
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class GdcWebDavApiWrapper implements GdcDataTransferAPI {
 
     private static Logger l = Logger.getLogger(GdcWebDavApiWrapper.class);
 
     protected static final String DEFAULT_ARCHIVE_NAME = "upload.zip";
     protected static final String WEBDAV_URI = "/uploads/";
 
     protected HttpClient client;
     protected NamePasswordConfiguration config;
 
     /**
      * Constructs the GoodData WebDav API Java wrapper
      *
      * @param config NamePasswordConfiguration object with the GDC name and password configuration
      */
     public GdcWebDavApiWrapper(NamePasswordConfiguration config) {
         this.config = config;
         client = new HttpClient();
 
         NetUtil.configureHttpProxy(client);
 
         Credentials creds = new UsernamePasswordCredentials(this.config.getUsername(), this.config.getPassword());
         client.getState().setCredentials(AuthScope.ANY, creds);
     }
 
     /**
      * WebDav transfers a local directory to the remote GDC WebDav server
      *
      * @param archiveName the name of the ZIP archive that is going to be transferred
      * @throws java.io.IOException in case of IO issues
      */
     public void transferDir(String archiveName) throws IOException {
         l.debug("Transfering archive " + archiveName);
         File file = new File(archiveName);
         String dir = file.getName().split("\\.")[0];
         MkColMethod mkdir = new MkColMethod(this.config.getUrl() + WEBDAV_URI + dir);
         executeMethodOk(mkdir);
         PutMethod put = new PutMethod(this.config.getUrl() + WEBDAV_URI + dir + "/" + DEFAULT_ARCHIVE_NAME);
        FileInputStream fis = new FileInputStream(file);
        RequestEntity requestEntity = new InputStreamRequestEntity(fis);
         put.setRequestEntity(requestEntity);
         executeMethodOk(put);
        fis.close();
         l.debug("Transferred archive " + archiveName);
     }
 
     /**
      * GET the transfer logs from the FTP server
      *
      * @param remoteDir the primary transfer directory that contains the logs
      * @return Map with the log name and content
      * @throws java.io.IOException in case of IO issues
      */
     public Map<String, String> getTransferLogs(String remoteDir) throws IOException {
         l.debug("Retrieveing transfer logs.");
         Map<String, String> result = new HashMap<String, String>();
         PropFindMethod ls = new PropFindMethod(this.config.getUrl() + WEBDAV_URI + remoteDir + "/", DavConstants.PROPFIND_PROPERTY_NAMES, 1);
         String ret = executeMethodOk(ls);
         String[] files = ret.split(",");
         for (String file : files) {
             if (file.endsWith(".log") || file.endsWith(".json")) {
                 GetMethod get = new GetMethod(this.config.getUrl() + file);
                 String content = executeMethodOk(get);
                 result.put(file, content);
             }
         }
         l.debug("Transfer logs retrieved.");
         return result;
     }
 
     /**
      * Executes HttpMethod and test if the response if 200(OK)
      *
      * @param method the HTTP method
      * @return response body as String
      * @throws com.gooddata.exception.HttpMethodException
      *
      */
     private String executeMethodOk(HttpMethod method) throws HttpMethodException {
         try {
             client.executeMethod(method);
             if (method.getStatusCode() == HttpStatus.SC_OK) {
                 return method.getResponseBodyAsString();
             } else if (method.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                 return "";
             } else if (method.getStatusCode() == HttpStatus.SC_CREATED) {
                 return method.getResponseBodyAsString();
             } else if (method.getStatusCode() == HttpStatus.SC_ACCEPTED) {
                 throw new HttpMethodNotFinishedYetException(method.getResponseBodyAsString());
             } else if (method.getStatusCode() == HttpStatus.SC_MULTI_STATUS) {
                 if (method instanceof PropFindMethod) {
                     PropFindMethod ls = (PropFindMethod) method;
                     MultiStatus m = ls.getResponseBodyAsMultiStatus();
                     MultiStatusResponse[] responses = m.getResponses();
                     String resp = "";
                     for (MultiStatusResponse r : responses) {
                         if (resp.length() == 0)
                             resp = r.getHref();
                         else
                             resp += "," + r.getHref();
                     }
                     return resp;
                 } else {
                     throw new HttpMethodException("Error invoking GoodData WebDav API. MultiStatus from non PROPFIND method.");
                 }
             } else {
                 String msg = method.getStatusCode() + " " + method.getStatusText();
                 String body = method.getResponseBodyAsString();
                 if (body != null) {
                     msg += ": ";
                     try {
                         JSONObject parsedBody = JSONObject.fromObject(body);
                         msg += parsedBody.toString();
                     } catch (JSONException jsone) {
                         msg += body;
                     }
                 }
                 l.debug("Exception executing " + method.getName() + " on " + method.getPath() + ": " + msg);
                 throw new HttpMethodException(msg);
             }
         } catch (HttpException e) {
             l.debug("Error invoking GoodData WebDav API.", e);
             throw new HttpMethodException("Error invoking GoodData WebDav API.", e);
         } catch (IOException e) {
             l.debug("Error invoking GoodData REST API.", e);
             throw new HttpMethodException("Error invoking GoodData WebDav API.", e);
         } catch (DavException e) {
             l.debug("Error invoking GoodData REST API.", e);
             throw new HttpMethodException("Error invoking GoodData WebDav API.", e);
         }
     }
 
 }
