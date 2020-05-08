 package jp.co.nttcom.camel.documentbuilder.model.builder;
 
 import freemarker.template.Configuration;
 import freemarker.template.ObjectWrapper;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Locale;
 import jp.co.nttcom.camel.documentbuilder.xml.model.Component;
 import jp.co.nttcom.camel.documentbuilder.xml.model.Model;
 import org.apache.commons.io.FileUtils;
 
 public class HTMLBuilderFactory extends BuilderFactory {
 
     private static final String COMPONENT_TEMPLATE = "component.ftl";
 
     private static final String INDEX_TEMPLATE = "index.ftl";
    
    private static final String ENCODING = "UTF-8";
 
    private final Configuration cfg;
 
     HTMLBuilderFactory() {
        super();
         cfg = new Configuration();
         cfg.setClassForTemplateLoading(HTMLBuilderFactory.class, "");
         cfg.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
         cfg.setEncoding(Locale.JAPAN, "UTF-8");
         cfg.setOutputEncoding("UTF-8");
     }
 
     @Override
     public void build(Model model, File dir) throws IOException {
         buildHtmls(model, dir);
         buildIndex(model, dir);
     }
 
     private void buildHtmls(Model model, File dir) throws IOException {
         for (Component component : model.getComponents()) {
             String html = createHtml(component);
             writeHtml(component.getId(), component.getName(), html, dir);
         }
     }
 
     private String createHtml(Component component) throws IOException {
        Template template = cfg.getTemplate(COMPONENT_TEMPLATE, ENCODING);
         StringWriter writer = new StringWriter();
         try {
             template.process(component, writer);
         } catch (TemplateException ex) {
             throw new IOException(ex);
         }
         return writer.toString();
     }
 
     private void writeHtml(String id, String name, String html, File dir) throws IOException {
         String fileName = id + "_" + name + ".html";
         File f = new File(dir, fileName);
        FileUtils.writeStringToFile(f, html, ENCODING);
     }
 
     private void buildIndex(Model model, File dir) throws IOException {
         String html = createIndex(model);
         writeIndex(html, dir);
     }
 
     private String createIndex(Model model) throws IOException {
        Template template = cfg.getTemplate(INDEX_TEMPLATE, ENCODING);
         StringWriter writer = new StringWriter();
         try {
             template.process(model, writer);
         } catch (TemplateException ex) {
             throw new IOException(ex);
         }
         return writer.toString();
     }
 
     private void writeIndex(String html, File dir) throws IOException {
         String fileName = "index.html";
         File f = new File(dir, fileName);
        FileUtils.writeStringToFile(f, html, ENCODING);
     }
 }
