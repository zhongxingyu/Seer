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
 package de.weltraumschaf.juberblog.layout;
 
 import com.google.common.collect.Maps;
 import freemarker.template.Configuration;
 import freemarker.template.TemplateException;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.Map;
 import org.apache.commons.lang3.Validate;
 
 /**
  * Renders content into a template.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class PageLayout {
 
     /**
      * Template file name relative to configured directory.
      */
     private final String templateFile;
     /**
      * Freemarker configuration.
      */
     private final Configuration configuration;
     /**
      * Page title.
      */
     private String title = "";
     /**
      * Page descripion.
      */
     private String description = "";
 
     /**
      * Dedicated constructor.
      *
      * @param configuration must not be {@code null}
      * @param templateFile must not be {@code null} or empty
      */
     public PageLayout(final Configuration configuration, final String templateFile) {
         super();
         Validate.notNull(configuration, "Configuration must not be null!");
         this.configuration = configuration;
         Validate.notEmpty(templateFile, "Template file must not be null or empty!");
         this.templateFile = templateFile;
     }
 
     /**
      * Set the page title.
      *
      * @param title must not be {@code null}
      */
     public void setTitle(final String title) {
         Validate.notNull(title);
         this.title = title;
     }
 
     /**
      * Set the page description.
      *
      * @param description must not be {@code null}
      */
     public void setDescription(final String description) {
         Validate.notNull(description);
         this.description = description;
     }
 
     /**
      * Renders the given context into the layout.
      *
      * @param content must not be {@code null}
     * @return never {@code null{
      * @throws IOException on any IO error for template file
      * @throws TemplateException on any template parse error
      */
     public String render(final String content) throws IOException, TemplateException {
         Validate.notNull(content);
         final freemarker.template.Template tpl = configuration.getTemplate(templateFile);
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
         final Map<String, Object> templateVariables = Maps.newHashMap();
         templateVariables.put("content", content);
         templateVariables.put("title", title);
         templateVariables.put("description", description);
         templateVariables.put("encoding", configuration.getDefaultEncoding());
         tpl.process(templateVariables, new OutputStreamWriter(out, configuration.getDefaultEncoding()));
         return out.toString(configuration.getDefaultEncoding());
     }
 }
