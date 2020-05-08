 /*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
 package org.gridlab.gridsphere.portlet.impl;
 
 import org.gridlab.gridsphere.portlet.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletResponseWrapper;
 
 /**
  * A <code>SportletResponse</code> provides an implementation of the
  * <code>PortletResponse</code> by following the decorator patterm.
  * A HttpServletresponse object is used in composition to perform most of
  * the required methods.
  */
 public class SportletResponse extends HttpServletResponseWrapper implements PortletResponse {
 
     private HttpServletRequest req = null;
 
     public SportletResponse(HttpServletResponse res) {
         super(res);
     }
 
     /**
      * Constructs an instance of SportletResponse using an
      * <code>HttpServletResponse</code> and a <code>PortletRequest</code>
      *
      * @param res the <code>HttpServletRequest</code>
      * @param req the <code>PortletRequest</code>
      */
     public SportletResponse(HttpServletResponse res, PortletRequest req) {
         super(res);
         this.req = req;
         //String mimeType = req.getClient().getMimeType();
         //res.setContentType(mimeType);
     }
 
     /**
      * Creates a portlet URI pointing at the referrer of the portlet.
      *
      * @return the portletURI
      */
     public PortletURI createReturnURI() {
         SportletURI sportletURI = new SportletURI(req, this.getHttpServletResponse(), req.getContextPath());
         addURIParameters(sportletURI);
         sportletURI.setReturn(true);
         return sportletURI;
     }
 
 
     /**
      * Creates a portlet URI pointing to the current portlet mode.
      *
      * @return the portlet URI
      */
     public PortletURI createURI() {
         SportletURI sportletURI = new SportletURI(req, this.getHttpServletResponse(), req.getContextPath());
         addURIParameters(sportletURI);
         sportletURI.setReturn(false);
         return sportletURI;
     }
 
     /**
      * Creates a portlet URI pointing to the current portlet mode with the specified security.
      *
      * @return the portlet URI
      */
     public PortletURI createURI(boolean isSecure) {
         SportletURI sportletURI = new SportletURI(req, this.getHttpServletResponse(), req.getContextPath(), isSecure);
         addURIParameters(sportletURI);
         sportletURI.setReturn(false);
         return sportletURI;
     }
 
     /**
      * Creates a portlet URI pointing to another portal component with the specified security setting
      *
      * @return the portlet component label
      */
     public PortletURI createURI(String componentLabel, boolean isSecure) {
         SportletURI sportletURI = new SportletURI(req, this.getHttpServletResponse(), req.getContextPath(), isSecure);
         sportletURI.addParameter(SportletProperties.COMPONENT_ID, componentLabel);
         sportletURI.setReturn(false);
         return sportletURI;
     }
 
     /**
      * Creates a portlet URI pointing to the given portlet mode and current
      * portlet window state.
      *
      * @param mode the portlet mode
      */
     public PortletURI createURI(Portlet.Mode mode) {
         SportletURI sportletURI = new SportletURI(req, this.getHttpServletResponse(), req.getContextPath());
         addURIParameters(sportletURI);
         sportletURI.setPortletMode(mode);
         return sportletURI;
     }
 
     /**
      * Creates a portlet URI pointing to the current portlet mode and given portlet window state.
      *
      * @param state the portlet window state
      * @see #addURIParameters
      */
     public PortletURI createURI(PortletWindow.State state) {
         SportletURI sportletURI = new SportletURI(req, this.getHttpServletResponse(), req.getContextPath());
         addURIParameters(sportletURI);
         sportletURI.setWindowState(state);
         return sportletURI;
     }
 
     /**
      * Add any additional parameters to the URI:
      * <ul><li>
      * SportletProperties.COMPONENT_ID
      * </li></ul>
      */
     protected void addURIParameters(PortletURI sportletURI) {
         sportletURI.addParameter(SportletProperties.COMPONENT_ID, (String) req.getAttribute(SportletProperties.COMPONENT_ID));
     }
 
     /**
      * Maps the given string value into this portlet's namespace.
      *
      * @param aValue the string value
      */
     public String encodeNamespace(String aValue) {
        return aValue + "_" + (String) req.getAttribute(SportletProperties.COMPONENT_ID);
    }

    public void setContentType(String type) {
        this.getHttpServletResponse().setContentType(type);
    }

    public String getContentType() {
        return this.getHttpServletResponse().getContentType();
     }
 
     private HttpServletResponse getHttpServletResponse() {
         return (HttpServletResponse) super.getResponse();
     }
 }
 
