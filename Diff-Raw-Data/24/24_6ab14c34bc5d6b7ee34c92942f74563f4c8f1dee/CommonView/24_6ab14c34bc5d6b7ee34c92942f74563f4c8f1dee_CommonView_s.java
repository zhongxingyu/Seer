 package org.bloodtorrent.view;
 
 import com.yammer.dropwizard.views.View;
 import lombok.Getter;
 import lombok.Setter;
 import org.bloodtorrent.dto.User;
 
 import java.util.ResourceBundle;
 
import static java.io.File.separator;

 /**
  * Created with IntelliJ IDEA.
  * User: sds
  * Date: 13. 3. 14
  * Time: 오후 3:20
  * To change this template use File | Settings | File Templates.
  */
 public class CommonView extends View {
 
     @Getter @Setter
     private User user;
     @Getter @Setter
     private String message;
 
 
     @Getter
     private ResourceBundle printStrings = ResourceBundle.getBundle("properties/bloodtorrentStrings");
 
     @Getter
     private ResourceBundle printUrls = ResourceBundle.getBundle("properties/bloodtorrentUrls");
 
     public CommonView(String viewPath) {
         super(viewPath);
     }
 
     public CommonView(String viewPath, User user){
         super(viewPath);
         this.user = user;
     }
 
     public CommonView(String viewPath, String message) {
         super(viewPath);
         this.message = message;
     }
 }
