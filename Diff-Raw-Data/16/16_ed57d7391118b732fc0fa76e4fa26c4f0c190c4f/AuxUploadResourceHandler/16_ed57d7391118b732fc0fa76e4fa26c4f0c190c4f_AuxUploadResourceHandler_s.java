 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
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
 
 package org.icefaces.impl.application;
 
 import org.icefaces.util.EnvUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.Part;
 import javax.servlet.http.HttpSession;
 import javax.faces.application.Resource;
 import javax.faces.application.ResourceHandler;
 import javax.faces.application.ResourceHandlerWrapper;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.UUID;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class AuxUploadResourceHandler extends ResourceHandlerWrapper  {
     private static Logger log = Logger.getLogger(AuxUploadResourceHandler.class.getName());
     public static String AUX_REQ_MAP_KEY = 
             "iceAuxRequestMap";
     public static String CLOUD_PUSH_KEY = 
             "iceCloudPushId";
     private ResourceHandler wrapped;
     private Resource tokenResource = null;
 
     public AuxUploadResourceHandler(ResourceHandler wrapped)  {
         this.wrapped = wrapped;
     }
 
     public ResourceHandler getWrapped() {
         return wrapped;
     }
 
     private Resource getTokenResource()  {
         if (null == tokenResource)  {
             tokenResource = createResource("auxupload.txt");
         }
         return tokenResource;
     }
 
     public void handleResourceRequest(FacesContext facesContext) throws IOException {
         ExternalContext externalContext = facesContext.getExternalContext();
     
         if ( getTokenResource().getRequestPath().equals(
             getResourcePath(facesContext)) )  {
             storeParts(externalContext);
             externalContext.setResponseContentType("text/plain");
             OutputStream out = externalContext.getResponseOutputStream();
             out.write("handled by AuxUploadResourceHandler".getBytes());
             return;
         }
         
         wrapped.handleResourceRequest(facesContext);
         
     }
 
     public static Map getAuxRequestMap()  {
         ExternalContext externalContext = FacesContext.getCurrentInstance()
                 .getExternalContext();
         Map sessionMap = externalContext.getSessionMap();
         Map requestMap = externalContext.getRequestMap();
         Map auxRequestMap;
         auxRequestMap = (Map) requestMap.get(AUX_REQ_MAP_KEY);
         if (null != auxRequestMap)  {
             return auxRequestMap;
         }
         auxRequestMap = (Map) sessionMap.get(AUX_REQ_MAP_KEY);
         if (null != auxRequestMap)  {
             //once the auxiliary upload is used, it is only valid
             //for the current request to allow cleanup
             sessionMap.put(AUX_REQ_MAP_KEY, new HashMap());
         } else {
             auxRequestMap = new HashMap();
         }
         requestMap.put(AUX_REQ_MAP_KEY, auxRequestMap);
         return auxRequestMap;
     }
 
     private void storeParts(ExternalContext externalContext)  {
         HttpServletRequest request = 
                 (HttpServletRequest) externalContext.getRequest();
         HttpSession session = request.getSession();
         try {
         
             Map auxRequestMap = 
                     (Map) session.getAttribute(AUX_REQ_MAP_KEY);
             if (null == auxRequestMap)  {
                 auxRequestMap = new HashMap();
             }
 
            for (Part part : request.getParts()) {
                 String partType = part.getContentType();
                 String partName = part.getName();
                 if (null == partType)  {
                     auxRequestMap.put(partName, 
                             request.getParameter(part.getName()) );
                     if (CLOUD_PUSH_KEY.equals(partName))  {
                         session.setAttribute(CLOUD_PUSH_KEY, 
                                 request.getParameter(part.getName()));
                     }
                 } else {
                     auxRequestMap.put(partName, part );
                 }
             }
             
             session.setAttribute(AUX_REQ_MAP_KEY, auxRequestMap);
 
         } catch (Exception e) {
            e.printStackTrace();
         }
     }
 
     public String getTokenResourcePath()  {
         return getTokenResource().getRequestPath();
     }
 
     private static String getResourcePath(FacesContext facesContext)  {
         ExternalContext externalContext = facesContext.getExternalContext();
         String path = externalContext.getRequestServletPath();
         if (null == path)  {
             path = externalContext.getRequestPathInfo();
         }
         return (externalContext.getRequestContextPath() + path);
     }
 
     public String getCloudPushId()  {
         String cloudPushId = (String) FacesContext.getCurrentInstance()
                 .getExternalContext().getSessionMap().get(CLOUD_PUSH_KEY);
         return cloudPushId;
     }
 
 }
