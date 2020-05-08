 package nl.surfnet.bod.web.tag;
 
 import java.io.IOException;
 
 import javax.servlet.jsp.JspException;
 
 import nl.surfnet.bod.util.Environment;
 
import org.apache.commons.beanutils.PropertyUtils;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 public class EnvironmentTag extends BodTagSupport {
 
   private String property;
   private String var;
 
   @Override
   public void doTag() throws JspException, IOException {
    Environment env = getEnvironment();
 
    try {
      Object value = PropertyUtils.getSimpleProperty(env, property);
      getPageContext().setAttribute(var, value);
    }
    catch (Exception e) {
      throw new JspException(e);
     }
   }
 
  private Environment getEnvironment() {
    WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getPageContext()
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
