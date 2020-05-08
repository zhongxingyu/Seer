 package org.sukrupa.platform.web;
 
 import org.antlr.stringtemplate.StringTemplate;
 import org.antlr.stringtemplate.StringTemplateGroup;
 import org.apache.log4j.Logger;
 import org.springframework.web.servlet.view.InternalResourceView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.PrintWriter;
 import java.util.Map;
 
 import static java.lang.String.format;
 
 public class StringTemplateView extends InternalResourceView {
     private static final Logger log = Logger.getLogger(StringTemplateView.class);
 
     @Override
     protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
         String templateRootDir = format("%s/WEB-INF/templates", getServletContext().getRealPath("/"));
 
         log.info(format("Trying to findByStudentId string template group from [%s] using url [%s]",
                 templateRootDir,
                 getUrl()));
 
         StringTemplateGroup group = new StringTemplateGroup("view", templateRootDir);
         StringTemplate template = group.getInstanceOf(getBeanName());
 
         template.setAttributes(model);
 
         PrintWriter writer = response.getWriter();
         writer.print(template);
         writer.flush();
         writer.close();
     }
 }
