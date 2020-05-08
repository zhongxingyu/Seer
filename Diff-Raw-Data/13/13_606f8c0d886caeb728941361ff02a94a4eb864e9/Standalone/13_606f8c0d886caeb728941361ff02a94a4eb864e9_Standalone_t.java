 
 /*
  * Copyright (c) 1998, 1999, 2000 Semiotek Inc. All Rights Reserved.
  *
  * This software is the confidential intellectual property of
  * of Semiotek Inc.; it is copyrighted and licensed, not sold.
  * You may use it under the terms of the GNU General Public License,
  * version 2, as published by the Free Software Foundation. If you 
  * do not want to use the GPL, you may still use the software after
  * purchasing a proprietary developers license from Semiotek Inc.
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See the attached License.html file for details, or contact us
  * by e-mail at info@semiotek.com to get a copy.
  */
 
 
 import org.webmacro.*;
 import org.webmacro.servlet.WebContext;
 import java.util.Date;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 /**
   * This example demonstrates using WebMacro in "standalone" mode. Instead 
   * of subclassing from WMServlet you create and maintain your own WebMacro
   * object, and you are free to subclass from another servlet. Also, this 
   * technique can be used outside the servlet context.
   * <p>
   * The WebMacro master object is initialized when the servlet is initialized
   * and destroyed when the servlet is destroyed. There is some overhead 
   * involved in creating the interface so you should prefer not to create one
   * on every request, although it is not too expensive. 
   * <p>
   * This servlet can be compiled and installed as an ordinary servlet. You 
   * need to ensure that your WebMacro.properties file is properly configured
   * and available on your CLASSPATH. When setting up WebMacro.properties 
   * make sure that the TemplatePath is correctly set and that the template
   * used by this servlet, "standalone.wm", is available on that path.
   */
 public class Standalone extends HttpServlet
 {
 
    /**
      * This is the core WebMacro interface which we use to create Contexts, 
      * load Templates, and begin other WebMacro operations.
      */
    private WebMacro _wm = null;
 
    /**
      * The init() method will be called by your servlet runner whenever 
      * it wants to instantiate a new copy of your servlet. We initialize
      * WebMacro here so as to avoid the expense of initializing it on 
      * every request. 
      */
    public void init(ServletConfig sc) throws ServletException {
       try {
          if (_wm == null) {
             _wm = new WM();
          }
       } catch (InitException e) {
          throw new ServletException("Could not initialize WebMacro: " + e);
       }
    }
 
    /**
      * It's not strictly necessary to destroy the WebMacro object when 
      * you're done with it--garbage collection will eventually get it--but
      * it makes sure the resources get freed. You really ought to do this,
      * since it might be a lot of resources. 
      */
    public void destroy() {
       if (_wm != null) {
          _wm.destroy();
          _wm = null;
       }
    }
 
 
    /**
      * We only implement the GET method in this servlet. 
      * <p>
      * We create a WebContext object, populate it with some data, select
      * a template, and write the output of that template to the output 
      * stream. Note that you need to pass the WebContext object to the 
      * template in order to write it, you also need an output stream, in
      * this case the HttpServletResponse output stream is used. 
      * <p>
      * If you were using WebMacro outside a servlet context you would not
      * be able to construct a WebContext object, since you would not have 
      * the HttpServletRequest and HttpServletResponse objects which you 
      * need in order to do that. WebContext is a subclass of Context, so 
      * outside of a servlet just construct a Context object. You will 
      * obviously lose the ability to talk about servlet specific things 
      * in your templates (such as "Cookies") but otherwise it's the same.
      * <p>
      * There are a few exceptions you have to deal with. First, WebMacro 
      * may not be able to locate the template you've requested in which 
      * case it'll throw a NotFoundException. Second, the template will 
      * expect to find certain information in the Context, and if you fail 
      * to provide that information a ContextException will be thrown. Aside
      * than WebMacro, in a servlet environment you also have to deal with 
      * the IOException that can be thrown if something goes wrong with 
      * the network connection back to the client.
      */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
 
       try {
          try {
 
             // create a context for the current request
             WebContext c = _wm.getWebContext(req,resp);
 
             // fill up the context with our data
             c.put("Today", new Date());
             c.put("Number", new Long(23));
 
             // WebContext provides some utilities as well
             String other = c.getForm("other"); 
             if (other == null) {
                c.put("hello","hello again!"); // put this into the hash
             } else {
                c.put("hello",other);          // else put this in
             }
 
             // get the template we intend to execute
             Template t = _wm.getTemplate("standalone.wm");
 
             // Create FastWriter for fast output encoding
             FastWriter fw = new FastWriter(resp.getOutputStream(),
                                            resp.getCharacterEncoding());
             // write the template to the output, using our context
             t.write(fw, c);
             fw.close();
          } catch (org.webmacro.NotFoundException e) {
              FastWriter out = new FastWriter(resp.getOutputStream(),
                                              resp.getCharacterEncoding());
              
              out.write("ERROR!  Could not locate template standalone.wm, check that your template path is set properly in WebMacro.properties");
              out.close();
          } catch (org.webmacro.ContextException e) {
              FastWriter out = new FastWriter(resp.getOutputStream(),
                                              resp.getCharacterEncoding());
              out.write("ERROR!  Could not locate required data in the Context.");
              out.close();
          }
       } catch (java.io.IOException e) {
           // what else can we do?
           System.out.println("ERROR: IOException while writing to servlet output stream.");
       }
    }
 
 }
