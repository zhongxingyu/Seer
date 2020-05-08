 package paket;
 import java.io.File;
 import java.io.FileWriter;
import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
 import java.util.Map;
 
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 
 public class FreeMarker {
 	public Configuration getCfg() {
 		return cfg;
 	}
 
 	public void setCfg(Configuration cfg) {
 		this.cfg = cfg;
 	}
 
 	public Template getTemplate() {
 		return template;
 	}
 
 	public void setTemplate(Template template) {
 		this.template = template;
 	}
 
 	Configuration cfg;
 	Template template;
 
 	public FreeMarker() {
 		super();
 		cfg = new Configuration();
 	}
 
 	public String process(Map<String, Object> input) {
 		Writer file = null;
 		String str = null;
 		try {
 			// Configuration
 			//cfg.setDirectoryForTemplateLoading(new File("templates"));
 			// load template
 			//File f = new File("C:/templates/helloworld.ftl");
 			//template = cfg.set
 					
 					
 			cfg.setClassForTemplateLoading(this.getClass(), "/");
 			template = cfg.getTemplate("templates/helloworld.ftl");
 			// File output
 			file = new FileWriter(new File("output.html"));
 			
 
 			template.process(input, file);
 			
 			
 			file.flush();
 
 			// Also write output to console
 			System.out.println("Ispis fajla");
 			//Writer out = new OutputStreamWriter(System.out);
 			StringWriter sv = new StringWriter();
 			
 			template.process(input, sv);
 			str = sv.toString();
 			
 			sv.flush();
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 
 		} finally {
 			if (file != null) {
 				try {
 					file.close();
 				} catch (Exception e2) {
 				}
 			}
 		}
 		return str;
 	}
 }
