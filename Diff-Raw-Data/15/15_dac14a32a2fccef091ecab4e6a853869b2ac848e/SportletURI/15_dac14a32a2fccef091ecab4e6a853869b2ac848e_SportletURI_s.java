 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id: SportletURI.java 5032 2006-08-17 18:15:06Z novotny $
  */
 package org.gridsphere.portlet.impl;
 
 import org.gridsphere.portlet.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.net.URLEncoder;
 import java.util.*;
 import java.io.UnsupportedEncodingException;
 
 
 /**
  * A <code>SportletURI</code> provides an implementation of a
  * <code>PortletURI</code>
  */
 public class SportletURI implements PortletURI {
 
     private HttpServletResponse res = null;
     private HttpServletRequest req = null;
     private Map store = new HashMap();
     private boolean isSecure = false;
     private boolean redirect = true;
     //private String contextPath = null;
     //private String servletPath = null;
     private Map actionParams = new HashMap();
     private Set sportletProps = null;
 
     /**
      * Cannot instantiate uninitialized SportletResponse
      */
     private SportletURI() {
     }
 
     /**
      * Constructs a SportletURI from a <code>HttpServletResponse</code> and a
      * context path obtained from a <code>HttpServletRequest</code>
      *
      * @param res         a <code>HttpServletResponse</code>
      */
     public SportletURI(HttpServletRequest req, HttpServletResponse res) {
         this(req, res, false);
     }
 
     /**
      * Constructs a SportletURI from a <code>HttpServletResponse</code> and a
      * context path obtained from a <code>HttpServletRequest</code>
      *
      * @param res         a <code>HttpServletResponse</code>
      */
     public SportletURI(HttpServletRequest req, HttpServletResponse res, boolean isSecure) {
         this.store = new HashMap();
         this.isSecure = isSecure;
 
         this.req = req;
         this.res = res;
         //this.id = createUniquePrefix(2);
         // don't prefix these parameters of an action
         sportletProps = new HashSet();
         String compVar = (String)req.getAttribute(SportletProperties.COMPONENT_ID_VAR);
         if (compVar == null) compVar = SportletProperties.COMPONENT_ID;
         sportletProps.add(compVar);
         sportletProps.add(SportletProperties.PORTLET_WINDOW);
         sportletProps.add(SportletProperties.PORTLET_MODE);
     }
 
     /**
      * Determines if the generated URI should be referring back to itself
      *
      * @param redirect <code>true</code> if the generated URI should be
      *                 redirected, <code>false</code> otherwise
      */
     public void setReturn(boolean redirect) {
         this.redirect = redirect;
     }
 
     /**
      * Adds the given parameter to this URI. A portlet container may wish to
      * prefix the attribute names internally, to preserve a unique namespace
      * for the portlet.
      *
      * @param name  the parameter name
      * @param value the parameter value
      */
     public void addParameter(String name, String value) {
         if (sportletProps.contains(name)) {
             store.put(name, value);
         } else {
             actionParams.put(name, value);
         }
     }
 
     /**
      * Removes the given parameter from this URI.
      *
      * @param name
      */
     public void removeParameter(String name) {
         if (sportletProps.contains(name)) {
             store.remove(name);
         } else {
             actionParams.remove(name);
         }
     }
 
     /**
      * Adds the given action to this URI.
      *
      * @param simpleAction the portlet action String
      */
     public void addAction(String simpleAction) {
         DefaultPortletAction dpa = new DefaultPortletAction(simpleAction);
         addAction(dpa);
     }
 
     /**
      * Adds the given action to this URI. The action is a portlet-defined
      * implementation of the portlet action interface. It can carry any information.
      * How the information is recovered should the next request be for this URI is
      * at the discretion of the portlet container.
      * <p/>
      * Unless the ActionListener interface is implemented at the portlet this
      * action will not be delivered.
      *
      * @param action the portlet action
      */
     public void addAction(PortletAction action) {
         if (action instanceof DefaultPortletAction) {
             DefaultPortletAction dpa = (DefaultPortletAction) action;
             if (!dpa.getName().equals("")) {
                 store.put(SportletProperties.DEFAULT_PORTLET_ACTION, dpa.getName());
                 this.actionParams = dpa.getParameters();
             }
         }
     }
 
     /**
      * Sets the window state that will be invoked by this URI
      *
      * @param state the window state that will be invoked by this URI
      */
     public void setWindowState(PortletWindow.State state) {
         store.put(SportletProperties.PORTLET_WINDOW, state.toString());
     }
 
     /**
      * Sets the portlet mode that will be invoked by this URI
      *
      * @param mode the portlet mode that will be invoked by this URI
      */
     public void setPortletMode(Mode mode) {
         store.put(SportletProperties.PORTLET_MODE, mode.toString());
     }
 
     /**
      * Returns the complete URI as a string. The string is ready to be embedded
      * in markup. Once the string has been created, adding more parameters or
      * other listeners will not modify the string. You have to call this method
      * again, to create an updated string.
      *
      * @return the URI as a string
      */
     public String toString() {
         StringBuffer s = new StringBuffer();
         String port = null;
   
         if (req.isSecure() || isSecure) {
             port = SportletProperties.getInstance().getProperty("gridsphere.port.https");
             s.append("https://");
         } else {
             port = SportletProperties.getInstance().getProperty("gridsphere.port.http");
             s.append("http://");
         }
 
         s.append(req.getServerName());
         s.append(":");
         s.append((!port.equals("")) ? port : String.valueOf(req.getServerPort()));
 
         // add the action params
         Set paramSet = actionParams.keySet();
         Iterator it = paramSet.iterator();
         while (it.hasNext()) {
             String name = (String) it.next();
             String value = (String) actionParams.get(name);
             store.put(name, value);
         }
 
         // if underlying window state is floating then set it in the URI
         if (req.getAttribute(SportletProperties.FLOAT_STATE) != null) store.put(SportletProperties.PORTLET_WINDOW, PortletWindow.State.FLOATING.toString());
 
        String contextPath = SportletProperties.getInstance().getProperty("gridsphere.deploy"); // contextPath;
        String servletPath = SportletProperties.getInstance().getProperty("gridsphere.context");
        
         String url = contextPath;
         String newURL;
         Set set = store.keySet();
         if (!set.isEmpty()) {
             // add question mark
             url += servletPath + "?";
         } else {
             return s.append(url).append(servletPath).toString();
         }
         boolean firstParam = true;
         try {
             it = set.iterator();
             while (it.hasNext()) {
                 if (!firstParam)
 //    url += "&amp;"; // special character replaced with its corresponding entity for XHTML 1.0 Strict compliance
                     url += "&";
                 String name = (String) it.next();
                 String encname = URLEncoder.encode(name, "UTF-8");
                 String val = (String) store.get(name);
                 if (val != null) {
                     String encvalue = URLEncoder.encode(val, "UTF-8");
                     url += encname + "=" + encvalue;
                 } else {
                     url += encname;
                 }
                 firstParam = false;
             }
         } catch (UnsupportedEncodingException e) {
             System.err.println("UTF-8 unsupported encoding!");
             e.printStackTrace();
         }
         if (redirect) {
             newURL = res.encodeRedirectURL(url);
         } else {
             newURL = res.encodeURL(url);
         }
         s.append(newURL);
         //System.err.println("url=" + s.toString());
         return s.toString();
     }
 
 }
