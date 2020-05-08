 package org.mixer2.sample.view;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.mixer2.jaxb.xhtml.Html;
 import org.mixer2.jaxb.xhtml.Script;
 import org.mixer2.springmvc.AbstractMixer2XhtmlView;
 import org.mixer2.xhtml.PathAjuster;
 import org.mixer2.xhtml.exception.TagTypeUnmatchException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.core.io.ResourceLoader;
 import org.springframework.stereotype.Component;
 
 import com.google.gson.Gson;
 
 @Component
 @Scope("prototype")
 public class mapView extends AbstractMixer2XhtmlView {
     
     @Autowired
     protected ResourceLoader resourceLoader;
 
     @Override
     protected Html createHtml(Map<String, Object> model,
             HttpServletRequest request, HttpServletResponse response)
             throws IOException, TagTypeUnmatchException {
         
        @SuppressWarnings("unchecked")
         Map<String,Double> latLngMap = (Map<String,Double>) model.get("latLngMap");
 
         // load html template
         String mainTemplate = "classpath:m2mockup/m2template/map.html";
         Html html = getMixer2Engine().loadHtmlTemplate(
                 resourceLoader.getResource(mainTemplate).getInputStream());
         
         // set default value for google maps api
         Gson gson = new Gson();
         Script script = html.getHead().getById("defaultValuesJson", Script.class);
         script.setContent("var defaultValues = " + gson.toJson(latLngMap) + ";");
 
         // replace static file path
         Pattern pattern = Pattern.compile("^\\.+/.*m2static/(.*)$");
         String ctx = request.getContextPath();
         PathAjuster.replacePath(html, pattern, ctx + "/m2static/$1");
 
         return html;
     }
     
 }
