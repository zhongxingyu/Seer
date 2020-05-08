 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.component.richtextentry;
 
 import org.icefaces.impl.util.Base64;
 import org.icefaces.impl.util.Util;
 import org.icefaces.util.EnvUtils;
 
 import javax.faces.application.Resource;
 import javax.faces.application.ResourceHandler;
 import javax.faces.application.ResourceHandlerWrapper;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.PreRenderViewEvent;
 import javax.faces.event.SystemEvent;
 import javax.faces.event.SystemEventListener;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.List;
 import java.util.ArrayList;
 import javax.el.ELException;
 import javax.el.ValueExpression;
 import javax.el.ELContext;
 import java.util.regex.Pattern;
 
 public class RichTextEntryResourceHandler extends ResourceHandlerWrapper {
 
     private static final String META_INF_RESOURCES = "/META-INF/resources/";
     private static final String ICEFACES_ACE_LIB = "icefaces.ace";
     private static final String RICHTEXTENTRY_CKEDITOR_DIR = "richtextentry/ckeditor/";
     private static final String CKEDITOR_MAPPING_JS = "ckeditor.mapping.js";
 
     private ResourceHandler handler;
     private Resource apiJS = null;
 
     public RichTextEntryResourceHandler(ResourceHandler handler) {
         this.handler = handler;
         FacesContext.getCurrentInstance().getApplication().subscribeToEvent(PreRenderViewEvent.class, new SystemEventListener() {
             public void processEvent(SystemEvent event) throws AbortProcessingException {
                 try {
                     if (apiJS == null) createResource(RICHTEXTENTRY_CKEDITOR_DIR + CKEDITOR_MAPPING_JS);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
 
             public boolean isListenerForSource(Object source) {
                 return EnvUtils.isICEfacesView(FacesContext.getCurrentInstance());
             }
         });
     }
 
     public ResourceHandler getWrapped() {
         return handler;
     }
 
     public Resource createResource(String resourceName) {
         return createResource(resourceName, null, null);
     }
 
     public Resource createResource(String resourceName, String libraryName) {
         return createResource(resourceName, libraryName, null);
     }
 
     public Resource createResource(String resourceName, String libraryName, String contentType) {
         if ((RICHTEXTENTRY_CKEDITOR_DIR + CKEDITOR_MAPPING_JS).equals(resourceName)) {
             if (apiJS == null) {
 				apiJS = recreateResource(super.createResource(resourceName, ICEFACES_ACE_LIB));
             }
             return apiJS;
         } else {
             return super.createResource(resourceName, libraryName, contentType);
         }
     }
 
     private Resource recreateResource(Resource resource) {
 		byte[] content;
         try {
 			InputStream in = this.getClass().getResourceAsStream(META_INF_RESOURCES + ICEFACES_ACE_LIB + "/" + RICHTEXTENTRY_CKEDITOR_DIR + CKEDITOR_MAPPING_JS);
             content = readIntoByteArray(in);
         } catch (IOException e) {
             content = new byte[0];
         }
        return new ResourceEntry(RICHTEXTENTRY_CKEDITOR_DIR + CKEDITOR_MAPPING_JS, resource, content);
     }
 
     private static byte[] readIntoByteArray(InputStream in) throws IOException {
         byte[] buffer = new byte[4096];
         int bytesRead;
         ByteArrayOutputStream out = new ByteArrayOutputStream();
 
         while ((bytesRead = in.read(buffer)) != -1) {
             out.write(buffer, 0, bytesRead);
         }
         out.flush();
 
         return out.toByteArray();
     }
 
     private static class ResourceEntry extends Resource {
         private Date lastModified = new Date();
         private String localPath;
        private Resource wrapped;
         private byte[] content;
         private String mimeType;
 
        private ResourceEntry(String localPath, Resource wrapped, byte[] content) {
             this.localPath = localPath;
            this.wrapped = wrapped;
             this.content = content;
             FacesContext facesContext = FacesContext.getCurrentInstance();
             ExternalContext externalContext = facesContext.getExternalContext();
             this.mimeType = externalContext.getMimeType(localPath);
         }
 
         public String getLibraryName() {
             return ICEFACES_ACE_LIB;
         }
 
         public String getResourceName() {
             return localPath;
         }
 
         public InputStream getInputStream() throws IOException {
             return new ELEvaluatingInputStream(FacesContext.getCurrentInstance(), new ByteArrayInputStream(content), ICEFACES_ACE_LIB);
         }
 
         public Map<String, String> getResponseHeaders() {
 
             HashMap headers = new HashMap();
             headers.put("ETag", eTag());
             headers.put("Cache-Control", "public");
             headers.put("Content-Type", mimeType);
             headers.put("Date", Util.HTTP_DATE.format(new Date()));
             headers.put("Last-Modified", Util.HTTP_DATE.format(lastModified));
 
             return headers;
         }
 
         public String getContentType() {
             return mimeType;
         }
 
         public String getRequestPath() {
            return wrapped.getRequestPath();
         }
 
         public URL getURL() {
             try {
                 return FacesContext.getCurrentInstance().getExternalContext().getResource(localPath);
             } catch (MalformedURLException e) {
                 throw new RuntimeException(e);
             }
         }
 
         public boolean userAgentNeedsUpdate(FacesContext context) {
             try {
                 Date modifiedSince = Util.HTTP_DATE.parse(context.getExternalContext().getRequestHeaderMap().get("If-Modified-Since"));
                 return lastModified.getTime() > modifiedSince.getTime() + 1000;
             } catch (Throwable e) {
                 return true;
             }
         }
 
         private String eTag() {
             return Base64.encode(String.valueOf(localPath.hashCode()));
         }
     }
 
     // Taken from Mojarra
 	private static class ELEvaluatingInputStream extends InputStream {
 
         private List<Integer> buf = new ArrayList<Integer>(1024);
         private boolean failedExpressionTest = false;
         private boolean writingExpression = false;
         private InputStream inner;
         private FacesContext ctx;
 		private String libraryName;
         private boolean expressionEvaluated;
 		private static Pattern pattern = Pattern.compile(":");
 
         // ---------------------------------------------------- Constructors
 
 
         public ELEvaluatingInputStream(FacesContext ctx,
                                        InputStream inner,
 									   String libraryName) {
 
             this.inner = inner;
             this.ctx = ctx;
 			this.libraryName = libraryName;
 
         }
 
 
         // ------------------------------------------------ Methods from InputStream
 
 
         @Override
         public int read() throws IOException {
             int i;
             char c;
 
             if (failedExpressionTest) {
                 i = nextRead;
                 nextRead = -1;
                 failedExpressionTest = false;
             } else if (writingExpression) {
                 if (0 < buf.size()) {
                     i = buf.remove(0);
                 } else {
                     writingExpression = false;
                     i = inner.read();
                 }
             } else {
                 // Read a character.
                 i = inner.read();
                 c = (char) i;
                 // If it *might* be an expression...
                 if (c == '#') {
                     // read another character.
                     i = inner.read();
                     c = (char) i;
                     // If it's '{', assume we have an expression.
                     if (c == '{') {
                         // read it into the buffer, and evaluate it into the
                         // same buffer.
                         readExpressionIntoBufferAndEvaluateIntoBuffer();
                         // set the flag so that we need to return content
                         // from the buffer.
                         writingExpression = true;
                         // Make sure to swallow the '{'.
                         i = this.read();
                     } else {
                         // It's not an expression, we need to return '#',
                         i = (int) '#';
                         // then return whatever we just read, on the
                         // *next* read;
                         nextRead = (int) c;
                         failedExpressionTest = true;
                     }
                 }
             }
 
             return i;
         }
 
 
         private int nextRead = -1;
 
 
         private void readExpressionIntoBufferAndEvaluateIntoBuffer()
               throws IOException {
             int i;
             char c;
             do {
                 i = inner.read();
                 c = (char) i;
                 if (c == '}') {
                     evaluateExpressionIntoBuffer();
                 } else {
                     buf.add(i);
                 }
             } while (c != '}' && i != -1);
         }
 
         /*
         * At this point, we know that getBuf() returns a List<Integer>
         * that contains the bytes of the expression.
         * Turn it into a String, turn the String into a ValueExpression,
         * evaluate it, store the toString() of it in
         * expressionResult;
         */
         private void evaluateExpressionIntoBuffer() {
             char chars[] = new char[buf.size()];
             for (int i = 0, len = buf.size(); i < len; i++) {
                 chars[i] = (char) (int) buf.get(i);
             }
             String expressionBody = new String(chars);
             int colon;
             // If this expression contains a ":"
             if (-1 != (colon = expressionBody.indexOf(":"))) {
                 // Make sure it contains only one ":"
                 if (!isPropertyValid(expressionBody)) {
                     String message = "INVALID_RESOURCE_FORMAT_COLON_ERROR: " + expressionBody;
                     throw new ELException(message);
                 }
                 String[] parts = pattern.split(expressionBody, 0);
                 if (null == parts[0] || null == parts[1]) {
                     String message = "INVALID_RESOURCE_FORMAT_NO_LIBRARY_NAME_ERROR: " + expressionBody;
                     throw new ELException(message);
 
                 }
                 try {
                     int mark = parts[0].indexOf("[") + 2;
                     char quoteMark = parts[0].charAt(mark - 1);
                     parts[0] = parts[0].substring(mark, colon);
                     if (parts[0].equals("this")) {
                         parts[0] = libraryName;
                         mark = parts[1].indexOf("]") - 1;
                         parts[1] = parts[1].substring(0, mark);
                         expressionBody = "resource[" + quoteMark + parts[0] +
                                          ":" + parts[1] + quoteMark + "]";
                     }
                 }
                 catch (Exception e) {
                     String message = "INVALID_RESOURCE_FORMAT_ERROR: " + expressionBody;
                     throw new ELException(message);
 
                 }
             }
             ELContext elContext = ctx.getELContext();
             expressionEvaluated = true;
             ValueExpression ve =
                   ctx.getApplication().getExpressionFactory().
                         createValueExpression(elContext, "#{" + expressionBody +
                                                          "}", String.class);
             Object value = ve.getValue(elContext);
             String expressionResult = ((value != null) ? value.toString() : "");
             buf.clear();
             for (int i = 0, len = expressionResult.length(); i < len; i++) {
                 buf.add((int) expressionResult.charAt(i));
             }
         }
 
 
         @Override
         public void close() throws IOException {
 
             inner.close();
             super.close();
 
         }
 
         
         private boolean isPropertyValid(String property) {
             int idx = property.indexOf(':');
             return (property.indexOf(':', idx + 1) == -1);
         }
 
     }
 }
