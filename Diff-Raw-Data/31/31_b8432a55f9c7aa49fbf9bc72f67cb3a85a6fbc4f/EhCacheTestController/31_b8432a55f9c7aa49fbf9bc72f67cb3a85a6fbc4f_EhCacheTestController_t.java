 package backingbeans;
 
 import java.io.Serializable;
 import java.util.ResourceBundle;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.ViewScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.FacesConverter;
 import javax.faces.model.DataModel;
 import javax.faces.model.ListDataModel;
 import javax.faces.model.SelectItem;
 import javax.faces.bean.ManagedProperty;
 import java.util.logging.Logger;
 import java.util.List;
 import java.util.Arrays;
 import java.util.ArrayList;
 import javax.faces.event.ActionEvent;
 import javax.annotation.PostConstruct;
 import javax.naming.InitialContext;
 import java.util.Map;
 import java.util.HashMap;
 import javax.faces.event.ComponentSystemEvent;
 import java.util.Collection;
 import java.util.Date;
 import javax.servlet.http.HttpSession;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.authc.IncorrectCredentialsException;
 import org.apache.shiro.authc.UnknownAccountException;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.subject.Subject;
 
 import translation.TranslationCache;
 
 @ManagedBean
 @ViewScoped
 public class EhCacheTestController implements Serializable {
     private final Logger l = Logger.getLogger(this.getClass().getName());
     private static final String CLASSNAME=EhCacheTestController.class.getName();
     
    private static final transient TranslationCache tc = TranslationCache.getTranslationCache();
 
     private String msg;
     
     public String getMsg() {
         return msg;
     }
     
     public void setMsg(String msg) {
         this.msg = msg;
     }
 
 
     private String key;
     
     public String getKey() {
         return key;
     }
     
     public void setKey(String key) {
         this.key = key;
     }
 
 
     public String queryCache() {
         l.info(String.format("cache contains: %d elements", tc.getSizeOfTranslationDictionary()));
         String translation = tc.getTranslation(key);
         l.info(String.format("%s --> %s", key, translation));
         msg = (translation==null)?String.format("translation for key '%s' not found", key):
                                   String.format("translation for '%s' is '%s'"      , key, translation);
         if (translation==null) {
             FacesContext.getCurrentInstance().getExternalContext().getFlash().put("newWord", getKey());
             return "goToUpdateCache";
         } else return null;
     }
 }
 
