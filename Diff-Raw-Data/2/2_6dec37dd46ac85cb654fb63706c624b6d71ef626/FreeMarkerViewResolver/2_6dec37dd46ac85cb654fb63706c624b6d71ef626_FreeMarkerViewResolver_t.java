 package ddth.dasp.hetty.mvc.view.freemarker;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import ddth.dasp.common.utils.RegExpUtils;
 import ddth.dasp.hetty.mvc.view.CacheViewResolver;
 import freemarker.cache.TemplateLoader;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 
 public class FreeMarkerViewResolver extends CacheViewResolver {
 
     private final Logger LOGGER = LoggerFactory.getLogger(FreeMarkerViewResolver.class);
     private final static Pattern PATTERN = Pattern.compile("\\@\\{([^}]+)\\}");
 
     private Configuration cfg;
     private String defaultEncoding = "UTF-8";
     private String defaultContentType = "text/html; charset=utf-8";
     private TemplateLoader templateLoader;
     private String dateFormat = "yyyy-MM-dd";
     private String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
     private String prefix = "", suffix = "";
 
     public String getDefaultEncoding() {
         return defaultEncoding;
     }
 
     public void setDefaultEncoding(String defaultEncoding) {
         this.defaultEncoding = defaultEncoding;
     }
 
     public String getDefaultContentType() {
         return defaultContentType;
     }
 
     public void setDefaultContentType(String defaultContentType) {
         this.defaultContentType = defaultContentType;
     }
 
     public TemplateLoader getTemplateLoader() {
         return templateLoader;
     }
 
     public void setTemplateLoader(TemplateLoader templateLoader) {
         this.templateLoader = templateLoader;
     }
 
     public String getDateFormat() {
         return dateFormat;
     }
 
     public void setDateFormat(String dateFormat) {
         this.dateFormat = dateFormat;
     }
 
     public String getDateTimeFormat() {
         return dateTimeFormat;
     }
 
     public void setDateTimeFormat(String dateTimeFormat) {
         this.dateTimeFormat = dateTimeFormat;
     }
 
     public String getPrefix() {
         return prefix;
     }
 
     public void setPrefix(String prefix) {
         this.prefix = prefix;
     }
 
     public String getSuffix() {
         return suffix;
     }
 
     public void setSuffix(String suffix) {
         this.suffix = suffix;
     }
 
     public void init() {
         super.init();
         cfg = new Configuration();
         cfg.setObjectWrapper(new DefaultObjectWrapper());
         cfg.setAutoFlush(true);
         cfg.setDefaultEncoding(defaultEncoding);
         cfg.setTemplateLoader(templateLoader);
         cfg.setDateFormat(dateFormat);
         cfg.setDateTimeFormat(dateTimeFormat);
         if (!isEnableCache()) {
             // check for a newer version of a template every time it's requested
             cfg.setTemplateUpdateDelay(0);
         } else {
             cfg.setTemplateUpdateDelay(3600 * 24);
         }
     }
 
     public void destroy() {
         try {
             cfg = null;
         } finally {
             super.destroy();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected FreeMarkerView createView(String name, Map<String, String> replacements) {
         String templateName = (prefix != null ? prefix : "") + name
                 + (suffix != null ? suffix : "");
         Matcher m = PATTERN.matcher(templateName);
         StringBuffer sb = new StringBuffer();
         while (m.find()) {
             String replacement = replacements != null ? replacements.get(m.group(1)) : "";
             if (replacement == null) {
                 replacement = "";
             }
             m.appendReplacement(sb, RegExpUtils.regexpReplacementEscape(replacement));
         }
         m.appendTail(sb);
         templateName = sb.toString();
 
         Object templateSource = null;
         try {
             templateSource = templateLoader.findTemplateSource(templateName);
             if (templateSource == null) {
                 return null;
             }
             Template template = cfg.getTemplate(templateName);
             FreeMarkerView view = new FreeMarkerView(template);
             view.setContentType(getDefaultContentType());
             view.setEncoding(getDefaultEncoding());
             return view;
         } catch (IOException e) {
             throw new RuntimeException(e);
         } finally {
             try {
                 templateLoader.closeTemplateSource(templateSource);
             } catch (IOException e) {
                 LOGGER.warn(e.getMessage(), e);
             }
         }
     }
 }
