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
  *     Tim Joyce <timj At paneris.org>
  */
 
 package org.melati.template.velocity;
 
 import java.io.IOException;
 
 import java.util.Properties;
 
 import org.melati.Melati;
 import org.melati.MelatiConfig;
 import org.melati.poem.AccessPoemException;
 import org.melati.template.AbstractTemplateEngine;
 import org.melati.template.TemplateContext;
 import org.melati.template.ServletTemplateContext;
 import org.melati.template.TemplateEngine;
 import org.melati.template.TemplateEngineException;
 import org.melati.template.NotFoundException;
 import org.melati.util.MelatiBugMelatiException;
 import org.melati.util.MelatiStringWriter;
 import org.melati.util.MelatiWriter;
 
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.exception.MethodInvocationException;
 import org.apache.velocity.exception.ParseErrorException;
 import org.apache.velocity.exception.ResourceNotFoundException;
 
 /**
  * Wrapper for the Velocity Template Engine for use with Melati.
  */
 public class VelocityTemplateEngine extends AbstractTemplateEngine implements
     TemplateEngine {
 
   /** The name of the engine. */
   public static final String NAME = "velocity";
 
   /**
    * This is the string that is looked for when getInitParameter is called.
    */
   // private static final String INIT_PROPS_KEY = "velocity.properties";
   /**
    * Constructor.
    */
   public VelocityTemplateEngine() {
     super();
   }
 
   /**
    * Construct a new Engine.
    * 
    * @param melatiConfig
    *        a {@link MelatiConfig}
    * @throws TemplateEngineException
    *         if any problem occurs with the engine
    */
   public void init(MelatiConfig melatiConfig)
       throws TemplateEngineException {
     try {
       Properties props = loadConfiguration();
       Velocity.init(props);
     } catch (Exception e) {
       throw new TemplateEngineException(e);
     }
   }
 
 
   protected Properties loadConfiguration() {
     Properties p = new Properties();
     p.setProperty("resource.loader", "class");
     p.setProperty("class.resource.loader.class",
         WebMacroClasspathResourceLoader.class.getName());
     return p;
   }
 
   /**
    * Get the template context for Velocity.
    * 
    * @param melati
    *        the {@link Melati}
    * @return a {@link TemplateContext}
    */
   public TemplateContext getTemplateContext(Melati melati) {
     VelocityContext context = new VelocityContext();
     return new VelocityTemplateContext(context);
   }
 
   /**
    * The name of the template engine (used to find the templets).
    * 
    * @return the name of the current configured template engine
    */
   public String getName() {
     return NAME;
   }
 
   /**
    * @return the extension of the templates used by Velocity, including the dot.
    */
   public String templateExtension() {
     return ".vm";
   }
 
   /**
    * Get a template by name.
    * 
    * @param templateName
    *        the name of the template to find
    * @return a template
    * @throws NotFoundException if template not found
    */
   public org.melati.template.Template template(String templateName)
       throws NotFoundException {
     try {
       return new VelocityTemplate(templateName);
     } catch (ResourceNotFoundException e) {
       if (templateName.endsWith(templateExtension())) {
         // have a go at loading the webmacro template, and converting it!
         templateName = templateName.substring(0, templateName
             .lastIndexOf(templateExtension()))
             + ".wm";
         try {
           return new VelocityTemplate(templateName);
         } catch (ParseErrorException p) {
           throw new MelatiBugMelatiException(
              "Problem converting a WebMacro template to a Velocity template: " + templateName,
               p);
         } catch (ResourceNotFoundException e2) {
             throw new NotFoundException(e2);
         } 
       } else throw new NotFoundException(e);
     } catch (Exception e) {
       throw new TemplateEngineException(e);
     }
   }
 
   /**
    * Get a Template by name and expand it against a context.
    * 
    * @param out
    *        a {@link MelatiWriter} to output on
    * @param templateName
    *        the name of the template to expand
    * @param templateContext
    *        the {@link TemplateContext} to expand the template against
    * @throws IOException if TemplateEngine does
    * @throws NotFoundException if template not found
    */
   public void expandTemplate(MelatiWriter out, String templateName,
       TemplateContext templateContext)
       throws IOException, NotFoundException {
     expandTemplate(out, template(templateName), templateContext);
   }
 
   /**
    * Expand a {@link org.melati.template.Template} against the context.
    * 
    * @param out
    *        a {@link MelatiWriter} to output on
    * @param template
    *        the {@link org.melati.template.Template} to expand
    * @param templateContext
    *        the {@link ServletTemplateContext} to expand the template against
    */
   public void expandTemplate(MelatiWriter out,
       org.melati.template.Template template, TemplateContext templateContext)
       throws IOException {
     try {
       template.write(out, templateContext, this);
     } catch (TemplateEngineException problem) {
       Exception underlying = problem.subException;
       if (underlying instanceof AccessPoemException) {
         throw (AccessPoemException)underlying;
       }
       if (underlying instanceof MethodInvocationException) {
         Throwable caught = ((MethodInvocationException)underlying)
             .getWrappedThrowable();
         if (caught instanceof AccessPoemException) {
           throw (AccessPoemException)caught;
         }
       }
       throw problem;
     }
   }
 
   /**
    * Expand the Template against the context to a String.
    * 
    * @param template
    *        the {@link org.melati.template.Template} to expand
    * @param templateContext
    *        the {@link ServletTemplateContext} to expand the template against
    * @throws IOException if TemplateEngine does
    * @return the interpolated template as a String
    * {@inheritDoc}
    * @see org.melati.template.TemplateEngine#expandedTemplate
    */
   public String expandedTemplate(org.melati.template.Template template,
       TemplateContext templateContext)
       throws IOException {
     MelatiStringWriter s = new MelatiStringWriter();
     expandTemplate(s, template, templateContext);
     return s.toString();
   }
 
   /**
    * Return a {@link MelatiStringWriter}.
    * 
    * @return a MelatiStringWriter
    * {@inheritDoc}
    * @see Melati#getStringWriter()
    * @see org.melati.template.TemplateEngine#getStringWriter()
    */
   public MelatiStringWriter getStringWriter() {
     return new MelatiStringWriter();
   }
 
   /**
    * Get the underlying engine.
    * 
    * @return null - for velocity there is none.
    */
   public Object getEngine() {
     return null;
   }
 
   /** 
    * Get a variable exception handler for use if there is 
    * a problem accessing a variable.
    * 
    * FIXME Discover the velocity equivalent
    * 
    * @return a <code>PassbackVariableExceptionHandler</code> 
    *         appropriate for this engine.
    * {@inheritDoc}
    * @see org.melati.template.TemplateEngine#getPassbackVariableExceptionHandler()
    */
   public Object getPassbackVariableExceptionHandler() {
     return  null;
   }
 
   
 }
