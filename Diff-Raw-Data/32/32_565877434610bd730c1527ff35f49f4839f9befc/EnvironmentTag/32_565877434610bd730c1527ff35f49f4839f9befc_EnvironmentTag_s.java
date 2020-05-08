 package nl.surfnet.bod.web.tag;
 
 import java.io.IOException;
import java.lang.reflect.Field;
 
 import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
 
 import nl.surfnet.bod.util.Environment;
 
import org.springframework.util.ReflectionUtils;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 public class EnvironmentTag extends BodTagSupport {
 
   private String property;
   private String var;
 
   @Override
   public void doTag() throws JspException, IOException {
    Field field = ReflectionUtils.findField(Environment.class, property);
 
    if (field == null) {
      throw new JspException(String.format("Could not find property %s in the environment", property));
     }

    PageContext pageContext = getPageContext();

    Environment env = getEnvironment(pageContext);

    Object value = ReflectionUtils.getField(field, env);

    pageContext.setAttribute(var, value);
   }
 
  private Environment getEnvironment(PageContext pageContext) {
    WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(pageContext
         .getServletContext());
     Environment env = applicationContext.getBean(Environment.class);
     return env;
   }
 
   public String getProperty() {
     return property;
   }
 
   public void setProperty(String property) {
     this.property = property;
   }
 
   public String getVar() {
     return var;
   }
 
   public void setVar(String var) {
     this.var = var;
   }
 
 }
