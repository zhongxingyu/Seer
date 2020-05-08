 /*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
 package org.gridlab.gridsphere.portlet.impl;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portletcontainer.GridSphereProperties;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.ServletOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Locale;
 
 public class SportletResponse implements PortletResponse {
 
     private HttpServletResponse res = null;
     private HttpServletRequest req = null;
     private SportletURI sportletURI = null;
 
     public SportletResponse(HttpServletResponse res, PortletRequest req) {
         this.res = res;
         this.req = req;
         String mimeType = req.getClient().getMimeType();
         res.setContentType(mimeType);
        sportletURI = new SportletURI(res);
     }
 
     /**
      * Adds the specified cookie to the response.
      *
      * @param cookie the cookie to be added
      */
     public void addCookie(Cookie cookie) {
         res.addCookie(cookie);
     }
 
     /**
      * Adds a response header with the given name and date-value.
      *
      * @param name the name to be added
      * @param date the date-value
      */
     public void addDateHeader(String name, long date) {
         res.addDateHeader(name, date);
     }
 
     /**
      * Adds a response header with the given name and value.
      *
      * @param name the name of the header
      * @param value the additional header value
      */
     public void addHeader(String name, String value) {
         res.addHeader(name, value);
     }
 
     /**
      * Adds a response header with the given name and integer value.
      *
      * @param name the name of the header
      * @param value the additional header value
      */
     public void addIntHeader(String name, int value) {
         res.addIntHeader(name, value);
     }
 
     /**
      * Returns a boolean indicating whether the named response header has already been set.
      *
      * @return true if response header name has been sent, false otherwise
      */
     public boolean containsHeader(String name) {
         return res.containsHeader(name);
     }
 
     /**
      * Creates a portlet URI pointing at the referrer of the portlet.
      *
      * @return the portletURI
      */
     public PortletURI createReturnURI() {
         addURIParameters();
         sportletURI.addParameter(GridSphereProperties.PORTLETMODE, (String)Portlet.Mode.VIEW.toString());
         sportletURI.setReturn(true);
         return sportletURI;
     }
 
 
     /**
      * Creates a portlet URI pointing to the current portlet mode.
      *
      * @return the portlet URI
      */
     public PortletURI createURI() {
         addURIParameters();
         sportletURI.setReturn(false);
         return (PortletURI)sportletURI;
     }
 
     /**
      * Creates a portlet URI pointing to the current portlet mode and given portlet window state.
      *
      * @param state the portlet window state
      * @see addURIParameters
      */
     public PortletURI createURI(PortletWindow.State state) {
         addURIParameters();
         sportletURI.setWindowState(state);
         return (PortletURI)sportletURI;
     }
 
     /**
      * Add any additional parameters to the URI
      * <ul><li>
      * GridSphereProperties.PORTLETID
      * </li></ul>
      */
     protected void addURIParameters() {
         sportletURI.addParameter(GridSphereProperties.COMPONENT_ID, (String)req.getAttribute(GridSphereProperties.COMPONENT_ID));
     }
 
     /**
      * Maps the given string value into this portlet's namespace.
      *
      * @param aValue the string value
      */
     public String encodeNamespace(String aValue) {
         return null;
     }
 
     /**
      * Returns the encoded URI of the resource at the given path.
      *
      * @param path the given path
      * @return the encoded URI
      */
     public String encodeURL(String path) {
         return res.encodeURL(path);
     }
 
     /**
      * Returns the name of the charset used for the MIME body sent in this response.
      *
      * @return the character encoding
      */
     public String getCharacterEncoding() {
         return res.getCharacterEncoding();
     }
 
     /**
      * Returns the content type that can be used to contribute markup to the portlet response.
      *
      * @return the content type
      */
     public String getContentType() {
         return "html";
     }
 
     /**
      * Returns the writer object that can be used to contribute markup to the portlet response.
      *
      * @return the writer
      */
     public PrintWriter getWriter() throws IOException {
         return res.getWriter();
     }
 
     /**
      * Sets a response header with the given name and date-value.
      *
      * @param name the header name
      * @param date the header date-value
      */
     public void setDateHeader(String name, long date) {
         res.setDateHeader(name, date);
     }
 
     /**
      * Sets a response header with the given name and value.
      *
      * @param name the header name
      * @param value the header value
      */
     public void setHeader(String name, String value) {
         res.setHeader(name, value);
     }
 
     /**
      * Sets a response header with the given name and integer value.
      * If the header had already been set, the new value overwrites the previous one.
      * The containsHeader  method can be used to test for the presence of a header before setting its value.
      *
      * We need to think about all header methods and how to solve nameclashes, etc.
      *
      *
      */
     public void setIntHeader(String name, int value) {
         res.setIntHeader(name, value);
     }
 
     public ServletOutputStream getOutputStream() throws IOException {
         return res.getOutputStream();
     }
 
     public void setContentLength(int i) {
         res.setContentLength(i);
     }
 
     public void setContentType(String s) {
         res.setContentType(s);
     }
 
     public void setBufferSize(int i) throws IllegalStateException {
         res.setBufferSize(i);
     }
 
     public int getBufferSize() {
         return res.getBufferSize();
     }
 
     public void flushBuffer() throws IOException {
         res.flushBuffer();
     }
 
 
     public void resetBuffer() throws IllegalStateException {
         //res.resetBuffer();
     }
 
 
     public boolean isCommitted() {
         return res.isCommitted();
     }
 
     public void reset() throws IllegalStateException {
         res.reset();
     }
 
     public void setLocale(Locale locale) {
         res.setLocale(locale);
     }
 
     public Locale getLocale() {
         return res.getLocale();
     }
 
     public String encodeRedirectURL(String s) {
         return res.encodeRedirectURL(s);
     }
 
     public String encodeRedirectUrl(String s) {
         return res.encodeRedirectUrl(s);
     }
 
     public String encodeUrl(String s) {
         return res.encodeUrl(s);
     }
 
     public void sendError(int i) throws IOException {
         res.sendError(i);
     }
 
     public void sendError(int i, String s) throws IOException {
         res.sendError(i, s);
     }
 
     public void sendRedirect(String s) throws IOException {
         res.sendRedirect(s);
     }
 
     public void setStatus(int i) {
         res.setStatus(i);
     }
 
     public void setStatus(int i, String s) {
         res.setStatus(i, s);
     }
 
 
 }
 
