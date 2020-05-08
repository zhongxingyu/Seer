 /*
  * Copyright (C) 2010-2012 Klaus Reimer <k@ailis.de>
  * See LICENSE.md for licensing information.
  */
 
 package de.ailis.jasdoc.freemarker;
 
 import java.io.IOException;
 import java.io.StringWriter;
 
 import de.ailis.jasdoc.freemarker.directives.MessageDirective;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 /**
  * This factory is used to access the freemarker templates. It also manages the
  * freemarker engine configuration and the interface between the templates and
  * the model.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 public final class TemplateFactory
 {
    /** The freemarker configuration. */
     private final Configuration cfg;
 
     /**
      * Constructor.
      *
      * @param theme
      *            The theme.
      *
      */
     public TemplateFactory(final String theme)
     {
         this.cfg = new Configuration();
         this.cfg.setTemplateLoader(new ThemeTemplateLoader(theme));
         this.cfg.setObjectWrapper(new DefaultObjectWrapper());
         this.cfg.setDefaultEncoding("UTF-8");
         this.cfg.setWhitespaceStripping(true);
         this.cfg.setSharedVariable("message", new MessageDirective());
         this.cfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
     }
 
     /**
      * Returns the template with the specified name.
      *
      * @param name
      *            The template name
      * @return The template
      * @throws IOException
      *             When template could not be loaded.
      */
     public Template getTemplate(final String name) throws IOException
     {
         return this.cfg.getTemplate(name);
     }
 
     /**
      * Processes the specified template and returns the result as a string.
      *
      * @param template
      *            The template to process
      * @param rootMap
      *            The root map
      * @return The output
      */
     public String processTemplate(final Template template,
         final Object rootMap)
     {
         try
         {
             final StringWriter writer = new StringWriter();
             try
             {
                 template.process(rootMap, writer);
                 return writer.toString();
             }
             finally
             {
                 writer.close();
             }
         }
         catch (final TemplateException e)
         {
             throw new FreemarkerException("Unable to process template: "
                 + e, e);
         }
         catch (final IOException e)
         {
             throw new FreemarkerException("Unable to process template: "
                 + e, e);
         }
     }
 }
