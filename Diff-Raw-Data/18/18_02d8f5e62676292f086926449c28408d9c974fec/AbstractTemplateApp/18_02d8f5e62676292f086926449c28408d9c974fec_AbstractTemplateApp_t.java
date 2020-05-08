 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2005 Tim Pizey
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
  *     Tim Pizey <timp At paneris.org>
  *     http://paneris.org/~timp
  */
 
 package org.melati.app;
 
 import java.io.IOException;
 import java.util.Hashtable;
 
 
 import org.melati.Melati;
 import org.melati.poem.util.ArrayUtils;
 import org.melati.template.TemplateEngine;
 import org.melati.template.TemplateContext;
 import org.melati.util.MelatiConfigurationException;
 import org.melati.util.MelatiException;
 
 /**
  * Base class to use Melati as an application with a Template Engine.
  * 
  * To create your own application simply extend this class, 
  * overriding the {@link #doTemplateRequest} method.
  */
 public abstract class AbstractTemplateApp extends AbstractPoemApp implements App {
 
   protected TemplateEngine templateEngine;
   
   
 
   /** 
    * {@inheritDoc}
    * @see org.melati.app.AbstractPoemApp#init(java.lang.String[])
    */
   public Melati init(String[] args) throws MelatiException {
     Melati melati = super.init(args);
     templateEngine = melatiConfig.getTemplateEngine();
     TemplateContext templateContext = null;
     templateEngine.init(melatiConfig);
     templateContext = templateEngine.getTemplateContext(melati);
     if (templateContext == null) {
       try {
         super.term(melati);
       } catch (IOException e) {
         e = null;
       }
      throw new MelatiConfigurationException(
          "Have you configured a template engine? " + 
          "org.melati.MelatiConfig.templateEngine currently set to " + 
          templateEngine.getClass().getName());
     }
     melati.setTemplateContext(templateContext);
     String[] argsWithoutOutput = melati.getArguments();
     Hashtable form = new Hashtable();
     if (argsWithoutOutput.length > 4) {
       loadForm(form,(String[])ArrayUtils
               .section(argsWithoutOutput, 4, argsWithoutOutput.length));
     }
     templateContext = melati.getTemplateContext(); 
     templateContext.put("Form", form);
     
     return melati;
   }
 
   private void loadForm(Hashtable form, String[] tuples) {
     if (tuples.length != ((tuples.length/2)*2))
       throw new InvalidArgumentsException (tuples, 
              new RuntimeException("Number of paired arguments is not even:" + tuples.length));
     boolean isValue = false;
     String name = "";
     for (int i = 0; i < tuples.length; i++) {
       if (isValue) {  
         form.put(name, tuples[i]);
         isValue = false;
       } else { 
         name = tuples[i];
         isValue = true;
       }
     }
 
   }
 
   /**
    * Fulfill {@link AbstractPoemApp}'s promises.
    * 
    * @param melati the {@link Melati} 
    * @throws Exception if anything goes wrong 
    * @see org.melati.app.AbstractPoemApp#doPoemRequest(org.melati.Melati)
    */
   protected void doPoemRequest(Melati melati) throws Exception {
     /* 
     templateEngine = melatiConfig.getTemplateEngine();
     templateEngine.init(melatiConfig);
     melati.setTemplateEngine(templateEngine);
     TemplateContext templateContext = templateEngine.getTemplateContext(melati); 
     melati.setTemplateContext(templateContext);
     */
     TemplateContext templateContext = melati.getTemplateContext();
     templateContext.put("melati", melati);
     templateContext.put("ml", melati.getMarkupLanguage());
 
     String templateName = doTemplateRequest(melati,templateContext);
 
     if (templateName == null)
       templateName = this.getClass().getName().replace('.', '/');
     templateName = addExtension(templateName);
     templateEngine.expandTemplate(melati.getWriter(), 
                                   templateName,
                                   templateContext);
   }
   
   /**
    * The template extension is added in an overridable method
    * to allow the application developer to specify their own template
    * extensions.
    */
   protected String addExtension(String templateName) {
     if (!templateName.endsWith(templateEngine.templateExtension()))  
       return templateName + templateEngine.templateExtension();
     else
       return templateName;      
   }
  
   /**
    * Override this method to build up your own output.
    *
    * @param melati the current {@link Melati}
    * @param templateContext the current {@link TemplateContext}
    * @return a Template name, possibly excluding extension.
    */
   protected abstract String doTemplateRequest(Melati melati, 
                                               TemplateContext templateContext)
       throws Exception;
 }
