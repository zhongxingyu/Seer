 //
 // $Id: JUploadApplet.java 750 2009-05-06 14:36:50Z etienne_sf $
 //
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 //
 // Created: ?
 // Creator: William JinHua Kwong
 // Last modified: $Date: 2009-05-06 16:36:50 +0200 (mer., 06 mai 2009) $
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.context;
 
 import java.awt.Cursor;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Vector;
 import java.util.regex.Matcher;
 
 import javax.swing.JApplet;
 
 import netscape.javascript.JSException;
 import netscape.javascript.JSObject;
 import wjhk.jupload2.exception.JUploadException;
 
 /**
  * Implementation of the Jupload Context, for an applet. One such context is
  * created at run time.
  * 
  * @see DefaultJUploadContext
  * 
  * @author etienne_sf
  * @version $Revision: 750 $
  */
 public class JUploadContextApplet extends DefaultJUploadContext {
 
     /**
      * The current applet. All applet parameters are reading by using this
      * attribute.
      */
     private JApplet theApplet = null;
 
     /**
      * The default constructor.
      * 
      * @param theApplet The applet is mandatory, to read the applet parameters.
      */
     public JUploadContextApplet(JApplet theApplet) {
         // TODO have a constructor with (this.theApplet=applet)
         if (theApplet == null) {
             throw new IllegalArgumentException("theApplet may not be null");
         }
         this.theApplet = theApplet;
 
         // Let's initialize the DefaultJUploadContext.
        init(theApplet);
     }
 
     /** {@inheritDoc} */
     @Override
     public JApplet getApplet() {
         return this.theApplet;
     }
 
     /** {@inheritDoc} */
     @Override
     public String getParameter(String key, String def) {
         String paramStr = (this.theApplet.getParameter(key) != null ? this.theApplet
                 .getParameter(key)
                 : def);
         displayDebugParameterValue(key, paramStr);
         return paramStr;
     }
 
     /** {@inheritDoc} */
     @Override
     public int getParameter(String key, int def) {
         String paramDef = Integer.toString(def);
         String paramStr = this.theApplet.getParameter(key) != null ? this.theApplet
                 .getParameter(key)
                 : paramDef;
         displayDebugParameterValue(key, paramStr);
         return parseInt(paramStr, def);
     }
 
     /** {@inheritDoc} */
     @Override
     public float getParameter(String key, float def) {
         String paramDef = Float.toString(def);
         String paramStr = this.theApplet.getParameter(key) != null ? this.theApplet
                 .getParameter(key)
                 : paramDef;
         displayDebugParameterValue(key, paramStr);
         return parseFloat(paramStr, def);
     }
 
     /** {@inheritDoc} */
     @Override
     public long getParameter(String key, long def) {
         String paramDef = Long.toString(def);
         String paramStr = this.theApplet.getParameter(key) != null ? this.theApplet
                 .getParameter(key)
                 : paramDef;
         displayDebugParameterValue(key, paramStr);
         return parseLong(paramStr, def);
     }// getParameter(int)
 
     /** {@inheritDoc} */
     @Override
     public boolean getParameter(String key, boolean def) {
         String paramDef = (def ? "true" : "false");
         String paramStr = this.theApplet.getParameter(key) != null ? this.theApplet
                 .getParameter(key)
                 : paramDef;
         displayDebugParameterValue(key, paramStr);
         return parseBoolean(paramStr, def);
     }// getParameter(boolean)
 
     /**
      * Loads cookie and userAgent, and add them to the specific headers for
      * upload requests. {@inheritDoc}
      */
     @Override
     public void readCookieAndUserAgentFromNavigator(Vector<String> headers) {
         // /////////////////////////////////////////////////////////////////////////////
         // Load session data read from the navigator:
         // - cookies (according to readCookieFromNavigator)
         // - User-Agent : useful, as the server will then see a post request
         // coming from the same navigator.
         String cookie = null;
         String userAgent = null;
 
         try {
             // Patch given by Stani: corrects the use of the juploadContext for
             // Firefox on Mac.
             cookie = (String) JSObject.getWindow(this.theApplet).eval(
                     "document.cookie");
             userAgent = (String) JSObject.getWindow(this.theApplet).eval(
                     "navigator.userAgent");
         } catch (JSException e) {
             System.out.println("JSException (" + e.getClass() + ": "
                     + e.getMessage()
                     + ") in DefaultUploadPolicy, trying default values.");
 
             // If we can't have access to the JS objects, we're in development :
             // Let's put some 'hard value', to test the juploadContext from the
             // development tool (mine is eclipse).
 
             // felfert: I need different values so let's make that
             // configurable...
             cookie = System.getProperty("debug_cookie");
             userAgent = System.getProperty("debug_agent");
 
             System.out
                     .println("  no navigator found, reading 'debug_cookie' from system properties ("
                             + cookie + ")");
             System.out
                     .println("  no navigator found, reading 'debug_agent' from system properties ("
                             + userAgent + ")");
             /*
              * Example of parameter when calling the JVM:
              * -Ddebug_cookie="Cookie:cpg146_data=
              * YTo0OntzOjI6IklEIjtzOjMyOiJhZGU3MWIxZmU4OTZjNThhZjQ5N2FiY2ZiNmFlZTUzOCI7czoyOiJhbSI7aToxO3M6NDoibGFuZyI7czo2OiJmcmVuY2giO3M6MzoibGl2IjthOjI6e2k6MDtOO2k6MTtzOjQ6IjE0ODgiO319
              * ;cpg143_data=
              * YTozOntzOjI6IklEIjtzOjMyOiI4NjhhNmQ4ZmNlY2IwMTc5YTJiNmZlMGY3YWQzNThkNSI7czoyOiJhbSI7aToxO3M6NDoibGFuZyI7czo2OiJmcmVuY2giO30
              * %3D;
              * 8387c97d1f683b758a67a0473b586126=5ed998846fec70d6d2f73971b9cbbf0b;
              * b1d7468cf1b317c97c7c284f6bb14ff8
              * =587b82a7abb3d2aca134742b1df9acf7" -Ddebug_agent="userAgent:
              * Mozilla/5.0 (Windows; U; Windows NT 5.0; fr; rv:1.8.1.3)
              * Gecko/20070309 Firefox/2.0.0.3"
              */
         }
         // The cookies and user-agent will be added to the header sent by the
         // juploadContext:
         if (cookie != null)
             headers.add("Cookie: " + cookie);
         if (userAgent != null)
             headers.add("User-Agent: " + userAgent);
 
     }
 
     /**
      * @return The current cursor
      * @see JUploadContext#setCursor(Cursor)
      */
     @Override
     public Cursor getCursor() {
         return this.theApplet.getCursor();
     }
 
     /** @see JUploadContext#setCursor(Cursor) */
     @Override
     public Cursor setCursor(Cursor cursor) {
         Cursor previousCursor = this.theApplet.getCursor();
         this.theApplet.setCursor(cursor);
         return previousCursor;
     }
 
     /** {@inheritDoc} */
     @Override
     public void showStatus(String status) {
         this.getUploadPanel().getStatusLabel().setText(status);
     }
 
     /**
      * @see JUploadContext#displayURL(String, boolean)
      */
     @Override
     public void displayURL(String url, boolean success) {
         try {
             if (url.toLowerCase().startsWith("javascript:")) {
                 // A JavaScript expression was specified. Execute it.
                 String expr = url.substring(11);
 
                 // Replacement of %msg%. Will do something only if the %msg%
                 // string exists in expr.
                 expr = expr.replaceAll("%msg%", Matcher
                         .quoteReplacement(jsString(getUploadPolicy()
                                 .getLastResponseMessage())));
 
                 // Replacement of %body%. Will do something only if the
                 // %body% string exists in expr.
                 expr = expr.replaceAll("%body%", Matcher
                         .quoteReplacement(jsString(getUploadPolicy()
                                 .getLastResponseBody())));
 
                 // Replacement of %success%. Will do something only if the
                 // %success% string exists in expr.
                 expr = expr.replaceAll("%success%", Matcher
                         .quoteReplacement((success) ? "true" : "false"));
 
                 displayDebug("Calling javascript expression: " + expr, 80);
                 JSObject.getWindow(this.theApplet).eval(expr);
             } else if (success) {
                 // This is not a javascript URL: we change the current page
                 // only if no error occurred.
                 String target = getUploadPolicy().getAfterUploadTarget();
                 if (getUploadPolicy().getDebugLevel() >= 100) {
                     getUploadPolicy().alertStr(
                             "No switch to getAfterUploadURL, because debug level is "
                                     + getUploadPolicy().getDebugLevel()
                                     + " (>=100)");
                 } else {
                     // Let's change the current URL to edit names and
                     // comments, for the selected album. Ok, let's go and
                     // add names and comments to the newly updated pictures.
                     this.theApplet.getAppletContext().showDocument(
                             new URL(url), (null == target) ? "_self" : target);
                 }
             }
         } catch (Exception ee) {
             // Oops, no navigator. We are probably in debug mode, within
             // eclipse for instance.
             getUploadPolicy().displayErr(ee);
         }
     }
 
     /**
      * Generates a valid URL, from a String. The generation may add the
      * documentBase of the applet.
      * 
      * @param url A url. Can be a path relative to the current one.
      * @return The normalized URL
      * @throws JUploadException
      */
     @Override
     public String normalizeURL(String url) throws JUploadException {
         if (null == url || url.length() == 0)
             return this.theApplet.getDocumentBase().toString();
         URI uri = null;
         try {
             uri = new URI(url);
             if (null == uri.getScheme())
                 uri = this.theApplet.getDocumentBase().toURI().resolve(url);
             if (!uri.getScheme().equals("http")
                     && !uri.getScheme().equals("https")
                     && !uri.getScheme().equals("ftp")) {
                 throw new JUploadException("URI scheme " + uri.getScheme()
                         + " not supported.");
             }
         } catch (URISyntaxException e) {
             throw new JUploadException(e);
         }
         return uri.toString();
     }
 
     /**
      * Generate a js String, that can be written in a javascript expression.
      * It's up to the caller to put the starting and ending quotes. The double
      * quotes are replaced by simple quotes (to let simple quotes unchanged, as
      * it may be used in common language). Thus, start and end of JS string
      * should be with double quotes, when using the return of this function.
      * 
      * @param s
      * @return The transformed string, that can be written in the output, into a
      *         javascript string. It doesn't contain the starting and ending
      *         double quotes.
      */
     public String jsString(String s) {
         String dollarReplacement = Matcher.quoteReplacement("\\$");
         String singleQuoteReplacement = Matcher.quoteReplacement("\\'");
         String linefeedReplacement = Matcher.quoteReplacement("\\n");
 
         if (s == null || s.equals("")) {
             return "";
         } else {
             s = s.replaceAll("\\$", dollarReplacement);
             s = s.replaceAll("\"", "'");
             s = s.replaceAll("'", singleQuoteReplacement);
             s = s.replaceAll("\n", linefeedReplacement);
             s = s.replaceAll("\r", "");
             return s;
         }
     }
 }
