 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 William Chesters
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc At paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Constructor;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.melati.poem.Database;
 import org.melati.poem.Field;
 import org.melati.poem.NoAccessTokenPoemException;
 import org.melati.poem.NotInSessionPoemException;
 import org.melati.poem.Persistent;
 import org.melati.poem.PoemLocale;
 import org.melati.poem.PoemThread;
 import org.melati.poem.ReferencePoemType;
 import org.melati.poem.Table;
 import org.melati.poem.User;
 import org.melati.poem.util.StringUtils;
 import org.melati.servlet.Form;
 import org.melati.template.HTMLMarkupLanguage;
 import org.melati.template.MarkupLanguage;
 import org.melati.template.ServletTemplateContext;
 import org.melati.template.ServletTemplateEngine;
 import org.melati.template.TemplateContext;
 import org.melati.template.TemplateEngine;
 import org.melati.util.AcceptCharset;
 import org.melati.util.CharsetException;
 import org.melati.util.DatabaseInitException;
 import org.melati.util.HttpHeader;
 import org.melati.util.HttpUtil;
 import org.melati.util.MelatiBufferedWriter;
 import org.melati.util.MelatiBugMelatiException;
 import org.melati.util.MelatiSimpleWriter;
 import org.melati.util.MelatiStringWriter;
 import org.melati.util.MelatiWriter;
 import org.melati.util.UTF8URLEncoder;
 import org.melati.util.UnexpectedExceptionException;
 
 /**
  * This is the main entry point for using Melati.
  * <p>
  * You will need to create a MelatiConfig in order to construct a Melati.
  * <p>
  * If you are using servlets, you will want to construct a melati with
  * a request and response object.  Otherwise, simply pass in a Writer.
  * <p>
  * If you are using a template engine outside of a servlets context you will 
  * still need the servlets jar in your classpath, annoyingly, as Velocity and 
  * WebMacro introspect all possible methods and throw a ClassNotFound exception 
  * if the servlets classes are not available.  
  * <p>
  * A Melati exists once per request.
  * <p>
  * Melati is typically used with Servlets, POEM (Persistent Object Engine for
  * Melati) and a Template Engine
  *
  * @see org.melati.MelatiConfig
  * @see org.melati.servlet.ConfigServlet
  * @see org.melati.servlet.PoemServlet
  * @see org.melati.servlet.TemplateServlet
  */
 
 public class Melati {
 
   /** UTF-8. */
   public static final String DEFAULT_ENCODING = "UTF-8";
   
   private MelatiConfig config;
   private PoemContext poemContext;
   private HttpServletRequest request;
   private HttpServletResponse response;
   private Database database = null;
   private Table table = null;
   private Persistent object = null;
   private MarkupLanguage markupLanguage = null;
   
   private String[] arguments;
 
   // the template engine that is in use (if any)
   private TemplateEngine templateEngine;
   // the object that is used by the template engine to expand the template
   // against
   private TemplateContext templateContext;
   // check to see if we have got the writer
   private boolean gotwriter = false;
   // are we manually flushing the output
   private boolean flushing = false;
   // are we buffering the output
   private boolean buffered= true;
   // the output writer
   private MelatiWriter writer;
 
   private static final int maxLocales = 10;
   private static Hashtable localeHash = new Hashtable(maxLocales);
 
   private String encoding;
 
   /**
    * Construct a Melati for use with Servlets.
    *
    * @param config - the MelatiConfig
    * @param request - the Servlet Request
    * @param response - the Servlet Response
    */
   public Melati(MelatiConfig config,
                 HttpServletRequest request,
                 HttpServletResponse response) {
     this.request = request;
     this.response = response;
     this.config = config;
   }
 
   /**
    * Construct a melati for use in 'stand alone' mode.
    * NB: you will not have access to servlet related stuff (eg sessions)
    *
    * @param config - the MelatiConfig
    * @param writer - the Writer that all output is written to
    */
 
   public Melati(MelatiConfig config, MelatiWriter writer) {
     this.config = config;
     this.writer = writer;
   }
 
   /**
    * Get the servlet request object.
    *
    * @return the Servlet Request
    */
 
   public HttpServletRequest getRequest() {
     return request;
   }
 
   /**
    * It is sometimes convenient to reconstruct the request object and
    * reset it, for example when returning from a log-in page.
    *
    * @see org.melati.login.HttpSessionAccessHandler
    * @param request - new request object
    */
   public void setRequest(HttpServletRequest request) {
     this.request = request;
   }
 
   /**
    * Used to set response mock in tests.
    * @see org.melati.login.HttpSessionAccessHandler
    * @param response - mock response object
    */
   public void setResponse(HttpServletResponse response) {
     this.response = response;
   }
   
   /**
    * Get the servlet response object.
    *
    * @return - the Servlet Response
    */
 
   public HttpServletResponse getResponse() {
     return response;
   }
 
   /**
    * Set the {@link PoemContext} for this request.  If the Context has a
    * LogicalDatabase set, this will be used to establish a connection
    * to the database.
    *
    * @param context - a PoemContext
    * @throws DatabaseInitException - if the database fails to initialise for
    *                                 some reason
    * @see org.melati.LogicalDatabase
    * @see org.melati.servlet.PoemServlet
    */
   public void setPoemContext(PoemContext context)
       throws DatabaseInitException {
     this.poemContext = context;
     if (poemContext.getLogicalDatabase() != null)
       database = LogicalDatabase.getDatabase(poemContext.getLogicalDatabase());
   }
 
   /**
    * Load a POEM Table and POEM Object for use in this request.  This is useful
    * as often Servlet requests are relevant for a single Table and/or Object.
    *
    * The Table name and Object id are set from the PoemContext.
    *
    * @see org.melati.admin.Admin
    * @see org.melati.servlet.PoemServlet
    */
   public void loadTableAndObject() {
     if (poemContext.getTable() != null && database != null)
       table = database.getTable(poemContext.getTable());
     if (poemContext.getTroid() != null && table != null)
       object = table.getObject(poemContext.getTroid().intValue());
   }
 
 
   /**
    * Get the PoemContext for this Request.
    *
    * @return - the PoemContext for this Request
    */
   public PoemContext getPoemContext() {
     return poemContext;
   }
 
   /**
    * Get the POEM Database for this Request.
    *
    * @return - the POEM Database for this Request
    * @see #setPoemContext
    */
   public Database getDatabase() {
     return database;
   }
   
   /**
    * Return the names of other databases known at the moment. 
    *  
    * @return a Vector of database names
    */
   public Vector getKnownDatabaseNames() {
     return LogicalDatabase.
                getInitialisedDatabaseNames();
   }
 
   /**
    * Get the POEM Table (if any) in use for this Request.
    *
    * @return the POEM Table for this Request
    * @see #loadTableAndObject
    */
   public Table getTable() {
     return table;
   }
 
   /**
    * Get the POEM Object (if any) in use for this Request.
    *
    * @return the POEM Object for this Request
    * @see #loadTableAndObject
    */
   public Persistent getObject() {
     return object;
   }
 
   /**
    * Get the Method (if any) that has been set for this Request.
    *
    * @return the Method for this Request
    * @see org.melati.PoemContext
    * @see org.melati.servlet.ConfigServlet#poemContext
    * @see org.melati.servlet.PoemServlet#poemContext
    */
   public String getMethod() {
     return poemContext.getMethod();
   }
 
   /**
    * Set the template engine to be used for this Request.
    *
    * @param te - the template engine to be used
    * @see org.melati.servlet.TemplateServlet
    */
   public void setTemplateEngine(TemplateEngine te) {
     templateEngine = te;
   }
 
   /**
    * Get the template engine in use for this Request.
    *
    * @return - the template engine to be used
    */
   public TemplateEngine getTemplateEngine() {
     return templateEngine;
   }
 
   /**
    * Set the ServletTemplateContext to be used for this Request.
    *
    * @param tc - the template context to be used
    * @see org.melati.servlet.TemplateServlet
    */
   public void setTemplateContext(TemplateContext tc) {
     templateContext = tc;
   }
 
   /**
    * Get the TemplateContext used for this Request.
    *
    * @return - the template context being used
    */
   public TemplateContext getTemplateContext() {
     return templateContext;
   }
   
   /**
    * Get the TemplateContext used for this Request.
    *
    * @return - the template context being used
    */
   public ServletTemplateContext getServletTemplateContext() {
     return (ServletTemplateContext)templateContext;
   }
 
   /**
    * Get the MelatiConfig associated with this Request.
    *
    * @return - the template context being used
    */
   public MelatiConfig getConfig() {
     return config;
   }
 
   /**
    * Get the PathInfo for this Request split into Parts by '/'.
    *
    * @return - an array of the parts found on the PathInfo
    */
   public String[] getPathInfoParts() {
     String pathInfo = request.getPathInfo();
     if (pathInfo == null || pathInfo.length() < 1) return new String[0];
     pathInfo = pathInfo.substring(1);
     return StringUtils.split(pathInfo, '/');
   }
 
   /**
    * Set the aruments array from the commandline.
    *
    * @param args
    */
   public void setArguments(String[] args) {
     arguments = args;
   }
 
   /**
    * Get the Arguments array.
    *
    * @return the arguments array
    */
   public String[] getArguments() {
     return arguments;
   }
 
   /**
    * Get the Session for this Request.
    *
    * @return - the Session for this Request
    */
   public HttpSession getSession() {
     return getRequest().getSession(true);
   }
 
   /**
    * Get a named context utility eg org.melati.admin.AdminUtils.
    *  
    * @param className Name of a class with a single argument Melati constructor 
    * @return the instantiated class
    */
   public Object getContextUtil(String className) {
     Object util;
     try {
       Constructor c  = Class.forName(className).getConstructor(new Class[] {this.getClass()});
       util = c.newInstance(new Object[] {this});
     } catch (Exception e) {
       throw new MelatiBugMelatiException("Class " + className + 
          " cannot be instantiated.", e);
     }  
     return util;
   }
   
   /**
    * Get the URL for the Logout Page.
    *
    * @return - the URL for the Logout Page
    * @see org.melati.login.Logout
    */
   public String getLogoutURL() {
     StringBuffer url = new StringBuffer();
     HttpUtil.appendRelativeZoneURL(url, getRequest());
     url.append('/');
     url.append(MelatiConfig.getLogoutPageServletClassName());
     url.append('/');
     url.append(poemContext.getLogicalDatabase());
     return url.toString();
   }
 
   /**
    * Get the URL for the Login Page.
    *
    * @return - the URL for the Login Page
    * @see org.melati.login.Login
    */
   public String getLoginURL() {
     StringBuffer url = new StringBuffer();
     HttpUtil.appendRelativeZoneURL(url, getRequest());
     url.append('/');
     url.append(MelatiConfig.getLoginPageServletClassName());
     url.append('/');
     url.append(poemContext.getLogicalDatabase());
     return url.toString();
   }
 
   /**
    * Get the URL for this Servlet Zone.
    *
    * @return - the URL for this Servlet Zone
    * @see org.melati.util.HttpUtil#zoneURL
    */
   public String getZoneURL() {
     return HttpUtil.zoneURL(getRequest());
   }
 
   /**
    * Get the URL for this request.
    * Not used in Melati.
    *
    * @return - the URL for this request
    * @see org.melati.util.HttpUtil#servletURL
    */
   public String getServletURL() {
     return HttpUtil.servletURL(getRequest());
   }
 
   /**
    * Get the URL for the JavascriptLibrary.
    * Convenience method.
    * 
    * @return - the URL for the JavascriptLibrary
    * @see org.melati.MelatiConfig#getJavascriptLibraryURL
    */
   public String getJavascriptLibraryURL() {
     return config.getJavascriptLibraryURL();
   }
 
   /**
    * Returns a PoemLocale object based on the Accept-Language header
    * of this request.
    *
    * If we are using Melati outside of a servlet context then the
    * configured locale is returned.
    *
    * @return a PoemLocale object
    */
   public PoemLocale getPoemLocale() {
     HttpServletRequest r = getRequest();
     PoemLocale ml = null;
     if (r != null) {
       String acceptLanguage = r.getHeader("Accept-Language");
       if (acceptLanguage != null)
         ml = getPoemLocale(acceptLanguage);
     }
    return ml != null ? ml : MelatiConfig.getPoemLocale();
   }
 
   /**
    * Returns a PoemLocale based on a language tag. Locales are cached for
    * future use.
    * 
    * @param languageHeader
    *        A language header from RFC 3282
    * @return a PoemLocale based on a language tag or null if not found
    * See http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
    */
   public static PoemLocale getPoemLocale(String languageHeader) {
 
     // language headers may have multiple language tags sperated by ,
     String tags[] = StringUtils.split(languageHeader, ',');
     PoemLocale ml = null;
 
     // loop through until we find a tag we like
     for (int i = 0; i < tags.length; i++) {
       String tag = tags[i];
 
       // remove quality value if it exists.
       // we'll just try them in order
       int indexSemicolon = tag.indexOf(';');
       if (indexSemicolon != -1)
         tag = tag.substring(0, indexSemicolon);
 
       String lowerTag = tag.trim().toLowerCase();
 
       // try our cache
       ml = (PoemLocale)localeHash.get(lowerTag);
       if (ml != null)
         return ml;
 
       // try creating a locale from this tag
       ml = PoemLocale.fromLanguageTag(lowerTag);
       if (ml != null) {
         localeHash.put(lowerTag, ml);
         return ml;
       }
     }
 
     return null;
   }
   
   /**
    * Suggest a response character encoding and if necessary choose a
    * request encoding.
    * <p>
    * If the request encoding is provided then we choose a response
    * encoding to meet our preferences on the assumption that the
    * client will also indicate next time what its request
    * encoding is.
    * The result can optionally be set in code or possibly in
    * templates using {@link #setResponseContentType(String)}.
    * <p>
    * Otherwise we tread carefully. We assume that the encoding is
    * the first supported encoding of the client's preferences for
    * responses, as indicated by Accept-Charsets, and avoid giving
    * it any reason to change.
    * <p>
    * Actually, the server preference is a bit dodgy for
    * the response because if it does persuade the client to
    * change encodings and future requests include query strings
    * that we are providing now then we may end up with the
    * query strings being automatically decoded using the wrong
    * encoding by request.getParameter(). But by the time we
    * end up with values in such parameters the client and
    * server will probably have settled on particular encodings.
    */
   public void establishCharsets() throws CharsetException {
 
     AcceptCharset ac;
     String acs = request.getHeader("Accept-Charset");
     //assert acs == null || acs.trim().length() > 0 :
     //  "Accept-Charset should not be empty but can be absent";
     // Having said that we don't want to split hairs once debugged
     if (acs != null && acs.trim().length() == 0) {
       acs = null;
     }
     try {
       ac = new AcceptCharset(acs, config.getPreferredCharsets());
     }
     catch (HttpHeader.HttpHeaderException e) {
       throw new CharsetException(
           "An error was detected in your HTTP request header, " +
           "response code: " +
           HttpServletResponse.SC_BAD_REQUEST +
           ": \"" + acs + '"', e);
     }
     if (request.getCharacterEncoding() == null) {
       responseCharset = ac.clientChoice();
       try {
         request.setCharacterEncoding(responseCharset);
       }
       catch (UnsupportedEncodingException e) {
         throw new MelatiBugMelatiException("This should already have been checked by AcceptCharset", e);
       }
     } else {
       responseCharset = ac.serverChoice();
     }
   }
 
   /**
    * Suggested character encoding for use in responses.
    */
   protected String responseCharset = null;
 
   /**
    * Sets the content type for use in the response.
    * <p>
    * Use of this method is optional and only makes sense in a 
    * Servlet context. If the response is null then this is a no-op.
    * <p>
    * If the type starts with "text/" and does not contain a semicolon
    * and a good response character set has been established based on
    * the request Accept-Charset header and server preferences, then this
    * and semicolon separator are automatically appended to the type.
    * I am guessing that this makes sense.
    * <p>
    * Whether this function should be called at all may depend on
    * the application and templates.
    * <p>
    * It should be called before any calls to {@link #getEncoding()}
    * and before writing the response.
    *
    * @see #establishCharsets()
    */
   public void setResponseContentType(String type) {
     if (responseCharset != null && type.startsWith("text/")
         && type.indexOf(";") == -1) {
       type += "; charset=" + responseCharset;
     }
     if (response != null)
       response.setContentType(type);
   }
 
   /**
    * Use this method if you wish to use a different 
    * MarkupLanguage, WMLMarkupLanguage for example. 
    * Cannot be set in MelatiConfig as does not have a noarg constructor.
    * @param ml The ml to set.
    */
   public void setMarkupLanguage(MarkupLanguage ml) {
     this.markupLanguage = ml;
   }
   
   /**
    * Get a {@link MarkupLanguage} for use generating output from templates.
    * Defaults to HTMLMarkupLanguage.
    *
    * @return - a MarkupLanguage, defaulting to HTMLMarkupLanguage
    * @see org.melati.template.TempletLoader
    * @see org.melati.poem.PoemLocale
    */
   public MarkupLanguage getMarkupLanguage() {
     if (markupLanguage == null) 
       markupLanguage = new HTMLMarkupLanguage(this,
                                   config.getTempletLoader(),
                                   getPoemLocale());
     return markupLanguage;
   }
 
   /**
    * Get a HTMLMarkupLanguage.
    * Retained for backward compatibility as there are a lot 
    * of uses in templates.
    *
    * @return - a HTMLMarkupLanguage
    */
   public HTMLMarkupLanguage getHTMLMarkupLanguage() {
     return (HTMLMarkupLanguage)getMarkupLanguage();
   }
 
   /**
    * The URL of the servlet request associated with this <TT>Melati</TT>, with
    * a modified or added form parameter setting (query string component).
    *
    * @param field   The name of the form parameter
    * @param value   The new value for the parameter (unencoded)
    * @return        The request URL with <TT>field=value</TT>.  If there is
    *                already a binding for <TT>field</TT> in the query string
    *                it is replaced, not duplicated.  If there is no query
    *                string, one is added.
    * @see org.melati.servlet.Form
    */
   public String sameURLWith(String field, String value) {
     return Form.sameURLWith(getRequest(), field, value);
   }
 
   /**
    * The URL of the servlet request associated with this <TT>Melati</TT>, with
    * a modified or added form flag setting (query string component).
    *
    * @param field   The name of the form parameter
    * @return        The request URL with <TT>field=1</TT>.  If there is
    *                already a binding for <TT>field</TT> in the query string
    *                it is replaced, not duplicated.  If there is no query
    *                string, one is added.
    * @see org.melati.servlet.Form
    */
   public String sameURLWith(String field) {
     return sameURLWith(field, "1");
   }
 
   /**
    * The URL of the servlet request associated with this <TT>Melati</TT>.
    *
    * @return a string
    */
   public String getSameURL() {
     String qs = getRequest().getQueryString();
     return getRequest().getRequestURI() + (qs == null ? "" : '?' + qs);
   }
 
   /**
    * Turn off buffering of the output stream.
    *
    * By default, melati will buffer the output, which will not be written
    * to the output stream until you call melati.write();
    *
    * Buffering allows us to catch AccessPoemExceptions and redirect the user
    * to the login page.  This could not be done if any bytes have been written
    * to the client
    *
    * @see org.melati.test.FlushingServletTest
    * @throws IOException if a writer has already been selected
    */
   public void setBufferingOff() throws IOException {
     if (gotwriter)
       throw new IOException("You have already requested a Writer, " +
                             "and can't change it's properties now");
     buffered = false;
   }
 
   /**
    * Turn on flushing of the output stream.
    *
    * @throws IOException if there is a problem with the writer
    */
   public void setFlushingOn() throws IOException {
     if (gotwriter)
       throw new IOException("You have already requested a Writer, " +
                             "and can't change it's properties now");
     flushing = true;
   }
 
   /**
    * Have we asked to access the Writer for this request?
    * <p>
    * If you have not accessed the Writer, it is reasonable to assume that
    * nothing has been written to the output stream.
    *
    * @return - have we sucessfully called getWriter()?
    */
   public boolean gotWriter() {
     return gotwriter;
   }
 
   /**
    * Return the encoding that is used for URL encoded query
    * strings.
    * <p>
    * The requirement here is that parameters can be encoded in
    * query strings included in URLs in the body of responses.
    * User interaction may result in subsequent requests with such
    * a URL. The HTML spec. describes encoding of non-alphanumeric
    * ASCII using % and ASCII hex codes and, in the case of forms.
    * says the client may use the response encoding by default.
    * Sun's javadoc for <code>java.net.URLEncoder</code>
    * recommends UTF-8 but the default is the Java platform
    * encoding. Most significantly perhaps,
    * org.mortbay.http.HttpRequest uses the request encoding.
    * We should check that this is correct in the servlet specs.
    * <p>
    * So we assume that the servlet runner may dictate the
    * encoding that will work for multi-national characters in
    * field values encoded in URL's (but not necessarily forms).
    * <p>
    * If the request encoding is used then we have to try and
    * predict it. It will be the same for a session unless a client
    * has some reason to change it. E.g. if we respond to a request
    * in a different encoding and the client is influenced.
    * (See {@link #establishCharsets()}.
    * But that is only a problem if the first or second request
    * in a session includes field values encoded in the URL and
    * user options include manually entering the same in a form
    * or changing their browser configuration.
    * Or we can change the server configuration.
    * <p>
    * It would be better if we had control over what encoding
    * the servlet runner used to decode parameters.
    * Perhaps one day we will.
    * <p>
    * So this method implements the current policy and currently
    * returns the current request encoding.
    * It assumes {@link #establishCharsets()} has been called to
    * set the request encoding if necessary.
    *
    * @return the character encoding
    * @see #establishCharsets()
    * see also org.melati.admin.Admin#selection(ServletTemplateContext, Melati)
    */
   public String getURLQueryEncoding() {
     return request.getCharacterEncoding();
   }
 
   /**
    * Convenience method to URL encode a URL query string.
    *
    * See org.melati.admin.Admin#selection(ServletTemplateContext, Melati)
    */
   /**
    * @param string the String to encode
    * @return the encoded string
    */
   public String urlEncode(String string) {
     try {
       return UTF8URLEncoder.encode(string, getURLQueryEncoding());
     }
     catch (UnexpectedExceptionException e) {
       // Thrown if the encoding is not supported
       return string;
     }
   }
 
   /**
    * Return the encoding that is used for writing.
    * <p>
    * This should always return an encoding and it should be the same
    * for duration of use of an instance.
    *
    * @return Response encoding or a default in stand alone mode
    * @see #setResponseContentType(String)
    */
   public String getEncoding() {
     if (encoding == null)
       encoding = response == null ? DEFAULT_ENCODING :
                                     response.getCharacterEncoding();
     return encoding;
   }
 
   /**
    * Get a Writer for this request.
    *
    * If you have not accessed the Writer, it is reasonable to assume that
    * nothing has been written to the output stream.
    *
    * @return - one of:
    *
    * - the Writer that was used to construct the Melati
    * - the Writer associated with the Servlet Response
    * - a buffered Writer
    * - a ThrowingPrintWriter
    * @throws IOException if there is a problem with the writer
    */
   public MelatiWriter getWriter() throws IOException {
     if (writer == null) writer = createWriter();
     // if we have it, remember that fact
     if (writer != null) gotwriter = true;
     return writer;
   }
 
   /**
    * Get a StringWriter.
    *
    * @return - one of:
    *
    * - a MelatiStringWriter from the template engine
    * - a new MelatiStringWriter if template engine not set
    *
    */
   public MelatiWriter getStringWriter() {
     if (templateEngine == null) {
       return new MelatiStringWriter();
     }
     return templateEngine.getStringWriter();
   }
 
   private MelatiWriter createWriter() throws IOException {
     // first effort is to use the writer supplied by the template engine
     MelatiWriter writerL = null;
     if (response != null) {
       if (templateEngine != null) {
         writerL = ((ServletTemplateEngine)templateEngine).getServletWriter(response, buffered);
       } else {
         if (buffered) {
           writerL = new MelatiBufferedWriter(response.getWriter());
         } else {
           writerL = new MelatiSimpleWriter(response.getWriter());
         }
       }
     }
     if (flushing) writerL.setFlushingOn();
     return writerL;
   }
 
   /**
    * Write the buffered output to the Writer
    * we also need to stop the flusher if it has started.
    *
    * @throws IOException if there is a problem with the writer
    */
   public void write() throws IOException {
     // only write stuff if we have previously got a writer
     if (gotwriter) writer.close();
   }
 
   /**
    * Get a PassbackVariableExceptionHandler for the ServletTemplateEngine.
    * This allows an Exception to be handled inline during Template expansion
    * for example, if you would like to render AccessPoemExceptions to a
    * String to be displayed on the page that is returned to the client.
    *
    * @return - PassbackVariableExceptionHandler specific to the
    * template engine
    *
    * @see org.melati.template.MarkupLanguage#rendered(Object)
    * @see org.melati.poem.TailoredQuery
    */
   public Object getPassbackVariableExceptionHandler() {
     return templateEngine.getPassbackVariableExceptionHandler();
   }
 
   /**
    * Set the <code>VariableExceptionHandler</code> to the
    * passed in parameter.
    *
    * @param veh a <code>VariableExceptionHandler</code>.
    */
   public void setVariableExceptionHandler(Object veh) {
     templateContext.setVariableExceptionHandler(veh);
   }
 
   /**
    * Get a User for this request (if they are logged in).
    * NOTE POEM studiously assumes there isn't necessarily a user, only
    * an AccessToken
    * @return - a User for this request
    */
   public User getUser() {
     try {
       return (User)PoemThread.accessToken();
     }
     catch (NotInSessionPoemException e) {
       return null;
     }
     catch (NoAccessTokenPoemException e) {
       return null;
     }
     catch (ClassCastException e) {
       return null;
     }
   }
   
   /**
    * Establish if field is a ReferencePoemType field.
    * 
    * @param field
    *          the field to check
    * @return whether it is a reference poem type
    */
   public boolean isReferencePoemType(Field field) {
     return field.getType() instanceof ReferencePoemType;
   }
 
   
 }
