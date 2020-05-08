 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.mail.templating.velocity;
 
 import de.cosmocode.palava.mail.templating.LocalizedMailTemplate;
 import de.cosmocode.palava.mail.templating.MailAttachmentTemplate;
 import de.cosmocode.palava.mail.templating.TemplateEngine;
 import de.cosmocode.palava.mail.templating.TemplateException;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
 import org.apache.velocity.runtime.resource.util.StringResourceRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.StringWriter;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * @author Tobias Sarnowski
  */
 public class VelocityTemplateEngine implements TemplateEngine {
     private static final Logger LOG = LoggerFactory.getLogger(VelocityTemplateEngine.class);
 
     public static final String ENCODING = "UTF-8";
 
     protected static final String K_SUBJECT = "subject";
     protected static final String K_BODY = "body";
     protected static final String K_SNIPPETS = "snippets";
     protected static final String K_EMBEDDED = "embedded";
     protected static final String K_ATTACHMENTS = "attachments";
 
     public VelocityTemplateEngine() {
        Properties config = new Properties();
 
         // see http://velocity.apache.org/engine/devel/apidocs/org/apache/velocity/runtime/resource/loader/StringResourceLoader.html
         config.put("resource.loader", "string");
         config.put("string.resource.loader.description", "Velocity StringResource loader");
        config.put("string.resource.loader.class", "org.apache.velocity.runtime.resource.loader.StringResourceLoader");
        config.put("string.resource.loader.repository.class", "org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl");
         config.put("string.resource.loader.repository.name", VelocityTemplateEngine.class.getName());
 
         try {
             Velocity.init(config);
         } catch (Exception e) {
             throw new IllegalStateException(e);
         }
     }
 
     @Override
     public LocalizedMailTemplate generate(final LocalizedMailTemplate template, Map<String, ? extends Object> variables) throws TemplateException {
         // generate template
         final String NAME = "/" + template.getName() + "/";
         StringResourceRepository repo = StringResourceLoader.getRepository(VelocityTemplateEngine.class.getName());
 
         repo.putStringResource(NAME + K_SUBJECT, template.getSubject());
         repo.putStringResource(NAME + K_BODY, template.getBody());
         for (Map.Entry<String,String> snippet: template.getSnippets().entrySet()) {
             repo.putStringResource(NAME + K_SNIPPETS + "/" + snippet.getKey(), snippet.getValue());
         }
         for (MailAttachmentTemplate embedded: template.getEmbedded()) {
             repo.putStringResource(NAME + K_EMBEDDED + "/" + embedded.getName(), embedded.getName());
             for (Map.Entry<String,String> config: embedded.getConfiguration().entrySet()) {
                 repo.putStringResource(NAME + K_EMBEDDED + "/" + embedded.getName() + "/" + config.getKey(), config.getValue());
             }
         }
         for (MailAttachmentTemplate attachment: template.getEmbedded()) {
             repo.putStringResource(NAME + K_ATTACHMENTS + "/" + attachment.getName(), attachment.getName());
             for (Map.Entry<String,String> config: attachment.getConfiguration().entrySet()) {
                 repo.putStringResource(NAME + K_ATTACHMENTS + "/" + attachment.getName() + "/" + config.getKey(), config.getValue());
             }
         }
 
         // configure variables
         final VelocityContext context = new VelocityContext();
        for (Map.Entry<String,? extends Object> entry: variables.entrySet()) {
             context.put(entry.getKey(), entry.getValue());
         }
 
         return new ParsedMailTemplate(NAME, template, context);
     }
 }
