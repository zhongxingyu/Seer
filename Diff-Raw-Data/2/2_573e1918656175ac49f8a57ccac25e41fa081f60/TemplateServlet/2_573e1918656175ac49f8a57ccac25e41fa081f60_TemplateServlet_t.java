 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 Tim Joyce
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
  *     Tim Joyce <timj@paneris.org>
  *     http://paneris.org/
  *     68 Sandbanks Rd, Poole, Dorset. BH14 8BY. UK
  */
 
 package org.melati.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletConfig;
 
 import org.melati.Melati;
 import org.melati.util.MelatiWriter;
 import org.melati.template.TemplateEngine;
 import org.melati.template.TemplateContext;
 import org.melati.template.MultipartTemplateContext;
 import org.melati.template.TemplateEngineException;
 import org.melati.template.Template;
 import org.melati.template.NotFoundException;
 
 /**
  * Base class to use Melati with a Template Engine.
  * Simply extend this class, override the doTemplateRequest method
  *
  * @author Tim Joyce
  * $Revision$
  */
 
 public abstract class TemplateServlet extends PoemServlet {
 
   // the template engine
   protected TemplateEngine templateEngine;
 
   /**
    * Inititialise the template engine.
    *
    * @param ServletConfig
    */
 
   public void init(ServletConfig config) throws ServletException {
     super.init(config);
     try {
       templateEngine = melatiConfig.getTemplateEngine();
       if (templateEngine != null)
         templateEngine.init(melatiConfig);
     } catch (TemplateEngineException e) {
       // log it to system.err as ServletExceptions go to the
       // servlet runner log (eg jserv.log), and don't have a stack trace!
       e.printStackTrace(System.err);
       throw new ServletException(e.toString());
     }
   }
 
   /**
    * Set the TemplateEngine and TemplateContext in our Melati.
    * This allows us to parse any uploaded files before we enter
    * our PoemSession (so we don't hang on to transactions
    * unnecessarily).
    */
 
   protected void prePoemSession(Melati melati) throws Exception {
     // for this request, set the Initialised Template Engine
     melati.setTemplateEngine(templateEngine);
     TemplateContext templateContext =
                         templateEngine.getTemplateContext(melati);
 
    melati.setTemplateContext(templateContext);
    
     // If we have an multipart form, we use a different template context
     // which allows us to access the uploaded files as well as fields.
     String contentType = melati.getRequest().getHeader("content-type");
     if (contentType != null &&
         contentType.substring(0,19).equalsIgnoreCase("multipart/form-data")) {
       templateContext =
         new MultipartTemplateContext(melati, templateContext);
     }
 
     melati.setTemplateContext(templateContext);
   }
 
   protected void doPoemRequest(Melati melati) throws Exception {
     TemplateContext templateContext = melati.getTemplateContext();
     templateContext.put("melati", melati);
 
     String templateName = doTemplateRequest(melati,templateContext);
 
     // only expand a template if we have one (it could be a redirect)
     if (templateName != null) {
       templateName = addExtension(templateName);
       templateEngine.expandTemplate(melati.getWriter(), 
                                     templateName,
                                     templateContext);
     }
   }
   
   /**
    * The template extension is added in an overridable method
    * to allow the application developer to specify their own template
    * extensions.
    * <p>
    * FIXME In some subtypes this does nothing (strangely) and in others
    * it adds the template engine extension as here, but only if there
    * is no ".". We need a contract. Checking for a "." assumes that
    * if there is one then we have the full filename. I think this is
    * reasonable and unlikely to break anything that is not already
    * stupid. Can copy code in
    * org.paneris.ftc.servlet.FtcMelatiServlet.addExtension()
    * JimW.
    */
 
   protected String addExtension(String templateName) {
     return templateName + templateEngine.templateExtension();
   }
 
   
   /**
   * a useful utility method that gets a value from the Form.  It will return
   * null if the value is "" or not present
   *
   * @param melati - the melati for this request
   * @param field - the name of the field to get
   *
   * @return - the value of the field requested
   * @deprecated as of 02/04/2001 - use MelatiUtil methods
   */
   public String getFormNulled(Melati melati, String field) {
     String val = melati.getTemplateContext().getForm(field);
     if (val == null) return null;
     return val.equals("")?null:val;
   }
   
   
   /**
    * Send an error message
    */
   public void error(Melati melati, Exception e ) throws IOException {
     // has it been trapped already, if so, we don't need to relog it here
     if (!(e instanceof TrappedException)) {
       try {
         // log it
         e.printStackTrace(System.err);
         // and put it on the page
         MelatiWriter mw =  melati.getWriter();
         // get rid of anything that has been written so far
         mw.reset();
         TemplateContext templateContext = melati.getTemplateContext();
         templateContext.put("melati",melati);
         templateContext.put("exceptionObject", e);
         StringWriter sw = new StringWriter();
         e.printStackTrace(new PrintWriter(sw));
         templateContext.put("error",sw);
         templateContext.put("SysAdminName",getSysAdminName());
         templateContext.put("SysAdminEmail",getSysAdminEmail());
 
         // FIXME we always search in the HTML directory for the error
         // template: this should be configurable
 
         Template errorTemplate;
 
         try {
           errorTemplate = melati.getHTMLMarkupLanguage().templet("error",
                                                                  e.getClass());
         }
         catch (NotFoundException f) {
           errorTemplate = templateEngine.template(
               "error" + templateEngine.templateExtension());
         }
 
         templateEngine.expandTemplate(mw, errorTemplate, templateContext);
         melati.write();
       } catch (Exception f) {
         System.err.println("Error finding/writing error template:");
         f.printStackTrace();
         super.error(melati,e);
       }
     }
   }
 
 
   /**
    * Override this method to build up your own output.
    *
    * @param melatiContext
    * @return Template name, possibly excluding extension.
    */
   protected abstract String doTemplateRequest(Melati melati, 
                                               TemplateContext templateContext)
       throws Exception ;
 }
