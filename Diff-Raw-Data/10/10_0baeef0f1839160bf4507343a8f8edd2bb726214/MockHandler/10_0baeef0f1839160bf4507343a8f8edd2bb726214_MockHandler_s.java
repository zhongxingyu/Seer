 /*
  * Copyright 2011 SURFnet bv, The Netherlands
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package nl.surfnet.coin.mock;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.eclipse.jetty.http.HttpMethods;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 import org.springframework.core.io.Resource;
 
 /**
  * {@link MockHandler} is a very simple {@link AbstractHandler} that returns
  * content based on the configuration in the test classes. This class is NOT
  * thread safe.
  *
  * @author oharsta
  */
 public class MockHandler extends AbstractHandler {
 
     /*
     * The resource of the (XML, JSON etc) file that should be returned. It is
     * accessible through the server instance and must be accessible from the
     * classpath
     */
     private Resource[] responseResource;
 
     /**
      * Constructor
      *
      * @param server the Server
      */
     public MockHandler(Server server) {
         setServer(server);
     }
 
     /*
     * Write the contents of the provided file to the response
     */
     private void respond(HttpServletResponse response, HttpServletRequest request)
             throws IOException {
         ServletOutputStream outputStream = response.getOutputStream();
         String requestURI = request.getRequestURI();
         InputStream inputStream = getResponseInputStream(requestURI);
         if (request.getMethod().equals(HttpMethods.POST)) {
         }
         IOUtils.copy(inputStream, outputStream);
         outputStream.flush();
     }
 
     /**
      * Return the result of next call
      */
     protected InputStream getResponseInputStream(String requestURI)
             throws IOException {
         InputStream inputStream = responseResource[0].getInputStream();
         if (responseResource.length > 1) {
             Resource[] stack = new Resource[this.responseResource.length - 1];
             System.arraycopy(this.responseResource, 1, stack, 0, this.responseResource.length - 1);
             this.responseResource = stack;
         }
         return inputStream;
     }
 
     /**
      * Set the Resource which contents will be returned as a SOAP response on the
      * next call
      *
      * @param responseResource the responseResource
      */
     public void setResponseResource(Resource responseResource) {
         this.responseResource = new Resource[]{responseResource};
     }
 
     /**
      * @param responseResource
      */
     public void setResponseResource(Resource[] responseResource) {
         this.responseResource = responseResource;
     }
 
 
     /*
     * (non-Javadoc)
     *
     * @see org.mortbay.jetty.Handler#handle(java.lang.String,
     * javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse, int)
     */
     @Override
     public void handle(String target, Request baseRequest, HttpServletRequest request,
                        HttpServletResponse response) throws IOException,
             ServletException {
         invariant();
         addContentHeader(response);
         respond(response, request);
     }
 
 
     /**
      * @param response
      */
     private void addContentHeader(HttpServletResponse response) {
         String contentType;
         String description = responseResource[0].getDescription();
         // poor man's solution, but it works for most Resource implementation
         if (description.contains("json")) {
             contentType = "application/json";
         } else if (description.contains("xml")) {
             contentType = "text/xml";
         } else {
             contentType = "text/html";
         }
         response.addHeader("Content-Type", contentType);
 
     }
 
     /*
     * Check the response to render back
     */
     private void invariant() {
         if (this.responseResource == null || this.responseResource.length == 0) {
             throw new RuntimeException("No responseResource set");
         }
     }
 
 }
