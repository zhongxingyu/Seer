 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet.impl;
 
 import org.gridlab.gridsphere.portlet.*;
 
 import javax.servlet.http.HttpServletResponse;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 
 /**
  * A <code>SportletURI</code> provides an implementation of a
  * <code>PortletURI</code>
  */
 public class SportletURI implements PortletURI {
 
     private HttpServletResponse res = null;
     private Map store = new HashMap();
     private boolean redirect = true;
     private String contextPath = null;
     private String id = "";
 
     /**
      * Cannot instantiate uninitialized SportletResponse
      */
     private SportletURI() {
     }
 
     /**
      * Constructs a SportletURI from a <code>HttpServletResponse</code> and a
      * context path obtained from a <code>HttpServletRequest</code>
      *
      * @param res a <code>HttpServletResponse</code>
      * @param contextPath the request context path
      */
     public SportletURI(HttpServletResponse res, String contextPath) {
         this.store = new HashMap();
         this.contextPath = contextPath;
         this.res = res;
         this.id = createUniquePrefix(2);
     }
 
     /**
      *  A string utility that produces a string composed of
      * <code>numChars</code> number of characters
      *
      * @param numChars the number of characters in the resulting <code>String</code>
      * @return the <code>String</code>
      */
     private String createUniquePrefix(int numChars) {
         StringBuffer s = new StringBuffer();
         for (int i = 0; i <= numChars; i++) {
             int nextChar = (int) (Math.random() * 62);
             if (nextChar < 10) //0-9
                 s.append(nextChar);
             else if (nextChar < 36) //a-z
                 s.append((char) (nextChar - 10 + 'a'));
             else
                 s.append((char) (nextChar - 36 + 'A'));
         }
         return s.toString();
     }
 
     /**
      * Determines if the generated URI should be referring back to itself
      *
      * @param redirect <code>true</code> if the generated URI should be
      * redirected, <code>false</code> otherwise
      */
     public void setReturn(boolean redirect) {
         this.redirect = redirect;
     }
 
     /**
      * Adds the given parameter to this URI. A portlet container may wish to
      * prefix the attribute names internally, to preserve a unique namespace
      * for the portlet.
      *
      * @param name the parameter name
      * @param value the parameter value
      */
     public void addParameter(String name, String value) {
         store.put(name, value);
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
      * <p>
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
 
                 Map actionParams = dpa.getParameters();
                 if (!actionParams.isEmpty()) store.put(SportletProperties.PREFIX, id);
                 Set set = actionParams.keySet();
                 Iterator it = set.iterator();
                 while (it.hasNext()) {
                     String name = (String) it.next();
                     String newname = id + "_" + name;
                     String value = (String) actionParams.get(name);
                     store.put(newname, value);
                 }
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
     public void setPortletMode(Portlet.Mode mode) {
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
         String url = contextPath;
         String newURL;
         Set set = store.keySet();
         if (!set.isEmpty()) {
             // add question mark
             url = contextPath + contextPath + "?";
         } else {
             return contextPath + url;
         }
         boolean firstParam = true;
         Iterator it = set.iterator();
         //try {
         while (it.hasNext()) {
             if (!firstParam)
                 url += "&";
             String name = (String)it.next();
             String encname = URLEncoder.encode(name);
            String encvalue = URLEncoder.encode((String) store.get(name));
            url += encname + "=" + encvalue;
             firstParam = false;
         }
         /*
         } catch (UnsupportedEncodingException e) {
             System.err.println("Unable to support UTF-8 encoding!");
         }*/
         if (redirect) {
             newURL = res.encodeRedirectURL(url);
         } else {
             newURL = res.encodeURL(url);
         }
         return newURL;
     }
 
 }
