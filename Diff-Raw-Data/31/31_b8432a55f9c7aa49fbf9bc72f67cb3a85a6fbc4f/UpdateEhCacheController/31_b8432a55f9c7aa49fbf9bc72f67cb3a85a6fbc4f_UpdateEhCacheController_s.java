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
 public class UpdateEhCacheController implements Serializable {
     private final Logger l = Logger.getLogger(this.getClass().getName());
     
    private TranslationCache tc = TranslationCache.getTranslationCache();
 
     private String newWord;
     
     public String getNewWord() {
         return newWord;
     }
     
     public void setNewWord(String newWord) {
        this.newWord = newWord;
     }
 
     private String translation;
     
     public String getTranslation() {
         return translation;
     }
     
     public void setTranslation(String translation) {
         this.translation = translation;
     }
 
     @PostConstruct
     public void init() {
         this.newWord = getFlashContent();
     }
 
     public String getFlashContent() {
         return (String) FacesContext.getCurrentInstance().getExternalContext().getFlash().get("newWord");
     }
 
     public String updateCache() {
         l.info("flash content is:"        +getFlashContent());
         l.info("provided translation is: "+translation);
         tc.putTranslation(newWord, translation);
         return "goToEhCacheTest";
     }
 }
 
