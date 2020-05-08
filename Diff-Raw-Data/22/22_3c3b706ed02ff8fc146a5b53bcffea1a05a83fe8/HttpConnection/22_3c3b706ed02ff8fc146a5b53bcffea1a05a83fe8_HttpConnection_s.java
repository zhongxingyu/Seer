 /*
  ************************************************************************
  Copyright [2011] [PagSeguro Internet Ltda.]
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  ************************************************************************
  */
 
 package br.com.uol.pagseguro.utils;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import br.com.uol.pagseguro.exception.PagSeguroServiceException;
 import br.com.uol.pagseguro.logs.Log;
 import br.com.uol.pagseguro.properties.PagSeguroConfig;
 import br.com.uol.pagseguro.properties.PagSeguroSystem;
 
 /**
  * 
  * Class HttpConnection
  */
 public class HttpConnection {
 
     /**
      * @var Log
      */
     private Log log = new Log(HttpConnection.class);
 
     /**
      * POST
      * 
      * @param url
      * @param data
      * @param timeout
      * @param charset
      * @return
      * @throws PagSeguroServiceException
      * @throws ParseException
      * @throws IOException
      * @throws SAXException
      * @throws ParserConfigurationException
      */
     public HttpURLConnection post(String url, Map<Object, Object> data, String timeout, String charset)
             throws PagSeguroServiceException {
         return this.connection("POST", url, charset);
     }
 
     /**
      * GET
      * 
      * @param url
      * @param timeout
      * @param charset
      * @return
      * @throws PagSeguroServiceException
      * @throws IOException
      */
     public HttpURLConnection get(String url, String timeout, String charset) throws PagSeguroServiceException {
         return this.connection("GET", url, charset);
     }
 
     /**
      * connection POST/GET
      * 
      * @param method
      * @param urlPS
      * @param charset
      * @return
      * @throws Exception
      * @throws ParseException
      * @throws IOException
      * @throws SAXException
      * @throws ParserConfigurationException
      */
     private HttpURLConnection connection(String method, String urlPS, String charset) throws PagSeguroServiceException {
 
         URL url = null;
         HttpURLConnection connection = null;
 
         try {
 
             // Creates a connection
             url = new URL(urlPS);
 
             connection = (HttpURLConnection) url.openConnection();
             connection.setDoOutput(true);
             connection.setDoInput(true);
             connection.setRequestMethod(method);
 
             connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=" + charset);
             connection.setRequestProperty("lib-description", "java:" + PagSeguroSystem.getLibversion());
             connection.setRequestProperty("language-engine-description",
                     "java:" + PagSeguroSystem.getLanguageEnginedescription());
 
             String moduleVersion = PagSeguroConfig.getModuleVersion();
             if (moduleVersion != null) {
                 connection.setRequestProperty("module-description", moduleVersion);
             }
 
             String cmsVersion = PagSeguroConfig.getCmsVersion();
             if (cmsVersion != null) {
                 connection.setRequestProperty("cms-description", cmsVersion);
             }
 
             if ("POST".equalsIgnoreCase(method)) {
                connection.getOutputStream().write(urlPS.getBytes(PagSeguroConfig.getApplicationCharset()));
             }
 
             return connection;
 
         } catch (MalformedURLException e) {
             log.error("Error when trying execute method connection: " + e.getMessage());
             throw new PagSeguroServiceException("Error when trying create Url object", e);
         } catch (IOException e) {
             log.error("Error when trying execute method connection: " + e.getMessage());
             throw new PagSeguroServiceException("Error when trying write or set request method", e);
         } catch (Exception e) {
             log.error("Error when trying execute method connection: " + e.getMessage());
             throw new PagSeguroServiceException("Generic error", e);
         }
     }
 
 }
