 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package de.weltraumschaf.juberblog.template;
 
 import de.weltraumschaf.juberblog.Constants;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.TemplateExceptionHandler;
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import org.apache.commons.lang3.Validate;
 import org.apache.log4j.Logger;
 
 /**
  * Factory to create template configurations.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public final class Configurations {
 
     public static final String SCAFFOLD_TEMPLATE_DIR = "/de/weltraumschaf/juberblog/scaffold/templates";
 
     /**
      * Log facility.
      */
     private static final Logger LOG = Logger.getLogger(Configurations.class);
 
     /**
      * Hidden for pure static factory.
      */
     private Configurations() {
         super();
     }
 
     /**
      * Creates a configuration with an exception handler which logs and rethrows exceptions.
      *
      * @param templateDirectory must not be {@code null} or empty
      * @return always new instance, never {@code null}
      * @throws IOException on ani I/O error reading the template directory
      * @throws URISyntaxException on any URI error for template directory
      */
     public static Configuration forTests(final String templateDirectory) throws IOException, URISyntaxException {
         final Configuration cfg = configureTemplates(templateDirectory);
         cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
         return cfg;
     }
 
     /**
      * Creates a configuration with an exception handler which ignores template errors.
      *
      * @param templateDirectory must not be {@code null} or empty
      * @return always new instance, never {@code null}
      * @throws IOException on ani I/O error reading the template directory
      * @throws URISyntaxException on any URI error for template directory
      */
     public static Configuration forProduction(final String templateDirectory) throws IOException, URISyntaxException {
         final Configuration cfg = configureTemplates(templateDirectory);
         cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
         return cfg;
     }
 
     /**
      * Creates a configuration with an exception handler which add template errors as HTML to output.
      *
      * @param templateDirectory must not be {@code null} or empty
      * @return always new instance, never {@code null}
      * @throws IOException on ani I/O error reading the template directory
      * @throws URISyntaxException on any URI error for template directory
      */
     public static Configuration forDrafts(final String templateDirectory) throws IOException, URISyntaxException {
         final Configuration cfg = configureTemplates(templateDirectory);
         cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
         return cfg;
     }
 
     /**
      * Configure templates and returns configuration.
      *
      * @param templateDirectory must not be {@code null} or empty
      * @return never {@code null}
      * @throws IOException if template directory can't be read
      * @throws URISyntaxException if template directory URI can't be created from class loader
      */
     private static Configuration configureTemplates(final String templateDirectory)
         throws IOException, URISyntaxException {
         Validate.notEmpty(templateDirectory, "Template directory must not be nul or empty!");
         LOG.debug("Configure templates for directory " + templateDirectory);
         final Configuration cfg = new Configuration();
         cfg.setObjectWrapper(new DefaultObjectWrapper());
         cfg.setDefaultEncoding(Constants.DEFAULT_ENCODING.toString());
         cfg.setIncompatibleImprovements(Constants.FREEMARKER_VERSION);
 
         if (templateDirectory.startsWith("/de/weltraumschaf/juberblog/")) {
             // Read resources via class loader for tests.
             cfg.setDirectoryForTemplateLoading(new File(Configurations.class.getResource(templateDirectory).toURI()));
         } else {
             cfg.setDirectoryForTemplateLoading(new File(templateDirectory));
         }
 
         return cfg;
     }
 
 }
