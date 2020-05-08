 /*
  * Copyright 2005 John R. Fallows
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.java.dev.weblets.impl.faces;
 
 import net.java.dev.weblets.WebletContainer;
 import net.java.dev.weblets.WebletRequest;
 import net.java.dev.weblets.WebletResponse;
 import net.java.dev.weblets.impl.WebletContainerImpl;
 import net.java.dev.weblets.impl.misc.WebletUtilsImpl;
 import net.java.dev.weblets.impl.servlets.WebletRequestImpl;
 import net.java.dev.weblets.impl.servlets.WebletResponseImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
 import javax.faces.FacesException;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.PhaseEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.event.PhaseListener;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.Map;
 import java.util.regex.Matcher;
 

 public class WebletsPhaseListener implements PhaseListener {
 
 
     private static final String WEBLETS_PHASE_LISTENER_ENTERED =
             "net.java.dev.weblets.impl.faces.WebletsPhaseListener.entered";
 
     public void afterPhase(
             PhaseEvent event) {
     }
 
     public void beforePhase(
             PhaseEvent event) {
         //TODO check if not   event.getPhaseId()!=PhaseId.RENDER_RESPONSE is the only one needed
         if (event.getPhaseId() != PhaseId.RESTORE_VIEW && event.getPhaseId() != PhaseId.APPLY_REQUEST_VALUES &&
                 event.getPhaseId() != PhaseId.RENDER_RESPONSE) {
             //try to execute the phase-listener logic only on apply request values
             //and (if restore-view is not called, e.g. in the portlet-case) on render-response
             return;
         }
 
         Boolean isReentry =
                 (Boolean) event.getFacesContext().getExternalContext().getRequestMap().get(
                         WEBLETS_PHASE_LISTENER_ENTERED);
 
         if (isReentry != Boolean.TRUE) {
             event.getFacesContext().getExternalContext().getRequestMap().put(
                     WEBLETS_PHASE_LISTENER_ENTERED, Boolean.TRUE);
             doBeforePhase(event);
         }
     }
 
     protected void doBeforePhase(
             PhaseEvent event) {
         FacesContext context = FacesContext.getCurrentInstance();
         ExternalContext external = context.getExternalContext();
 
         //test stuff remove me once verified
         WebletUtilsImpl utils = new WebletUtilsImpl();
         WebletRequest req = null;
 
         /*
         req = utils.getRequestFromPath(external.getRequest(), "/weblets-demo/faces/weblets/demo$1.0/welcome.js");
 
         ServletContext ctx = (ServletContext) external.getContext();
         String mime = ctx.getMimeType("/faces/weblets/demo$1.0/welcome.js");
 
         InputStream strm = utils.getResourceAsStream(req, mime);
         BufferedReader istrm = new BufferedReader(new InputStreamReader(strm));
         try {
             String line = "";
             while ((line = istrm.readLine()
             ) != null) {
                 System.out.println(line);
             }
             istrm.close();
   
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
 
         req = utils.getRequestFromPath(external.getRequest(), "/faces/weblets/demo$1.0/welcome.js");
         utils.getResourceAsStream(req, mime);
 
         req = utils.getRequestFromPath(external.getRequest(), "http://localhost:8080/weblets-demo/faces/weblets/demo$1.0/welcome.js");
         utils.getResourceAsStream(req, mime);
 
         req = utils.getRequestFromPath(external.getRequest(), "/xxx/aa/demo$1.0/welcome.js");
         utils.getResourceAsStream(req, mime);
 
 
         req = utils.getRequestFromPath(external.getRequest(), "/xxx/aa/demo$1.0/booga.js");
         Object strobj =     utils.getResourceAsStream(req, mime);
        */
 
         WebletContainerImpl container = (WebletContainerImpl) WebletContainer.getInstance();
 
         String pathInfo = external.getRequestServletPath();
         if (pathInfo != null && external.getRequestPathInfo() != null)
             pathInfo += external.getRequestPathInfo();
         if (pathInfo == null && event.getPhaseId() == PhaseId.RESTORE_VIEW) {
             //we are in a portlet environment here, since we do not get an external path info
             //we skip this phase and renter after the restore view
             event.getFacesContext().getExternalContext().getRequestMap().put(
                     WEBLETS_PHASE_LISTENER_ENTERED, Boolean.FALSE);
             return;
         }
         //special portlet treatment here
         //we move to later phases here to the apply request values
         //to become portlet compatible, unfortunately
         //we lose a little bit of performance then
         //but the determination of the pathInfo over
         //the view id is more neutral than over
         //the request servlet which is rundered null
         //in some portlet environments
         if (pathInfo == null)
             pathInfo = context.getViewRoot().getViewId();
 
 
        Matcher matcher = null;
        try {
           matcher = container.getPattern().matcher(pathInfo);
        } catch (NullPointerException ex) {
            Log log = LogFactory.getLog(this.getClass());
            log.error("An error has occurred no pattern or matcher has been detected, this is probably a sign that the weblets context listener has not been started. please add following lines to your web.xml \n"+
                        " <listener>\n" +
                        "       <listener-class>net.java.dev.weblets.WebletsContextListener</listener-class>\n" +
                        " </listener>");
            log.error("Details of the original Error:" + ex.toString());
                    return;
        }
         if (matcher.matches()) {
             Map requestHeaders = external.getRequestHeaderMap();
             String contextPath = external.getRequestContextPath();
             String requestURI = matcher.group(1);
             String ifModifiedHeader = (String) requestHeaders.get("If-Modified-Since");
 
             long ifModifiedSince = -1L;
 
             if (ifModifiedHeader != null) {
                 try {
                     DateFormat rfc1123 = new HttpDateFormat();
                     Date parsed = rfc1123.parse(ifModifiedHeader);
                     ifModifiedSince = parsed.getTime();
                 } catch (ParseException e) {
                     throw new FacesException(e);
                 }
             }
 
             try {
                 String[] parsed =
                         container.parseWebletRequest(contextPath, requestURI, ifModifiedSince);
 
                 if (parsed != null) {
                     ServletContext servletContext = (ServletContext) external.getContext();
                     ServletRequest httpRequest = (ServletRequest) external.getRequest();
                     ServletResponse httpResponse = (ServletResponse) external.getResponse();
 
                     String webletName = parsed[0];
                     String webletPath = parsed[1];
                     String webletPathInfo = parsed[2];
                     WebletRequest webRequest =
                             new WebletRequestImpl(webletName, webletPath, contextPath, webletPathInfo,
                                     ifModifiedSince, httpRequest);
                     String contentName = webRequest.getPathInfo();
                     String contentTypeDefault = servletContext.getMimeType(contentName);
                     WebletResponse webResponse =
                             new WebletResponseImpl(contentTypeDefault, httpResponse);
                     container.service(webRequest, webResponse);
                     context.responseComplete();
                 }
             } catch (IOException e) {
                 throw new FacesException(e);
             }
         }
     }
 
     public PhaseId getPhaseId() {
         /*we can only trigger after the restore view due to the fact
             that the view root is only available then
         */
         return PhaseId.ANY_PHASE;
     }
 
     /**
      * The serialization version.
      */
     private static final long serialVersionUID = -8385571916376473831L;
 }
