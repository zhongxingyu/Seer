 package controllers;
 
 import data.MessageDTO;
 import models.User;
 import play.mvc.*;
 import models.Room;
 import models.UserRoomRelationship;
 import data.AuthUser;
 import play.data.Form;
 import actions.GlobalContextParams;
 import security.UserSecurityHelper;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * <p>
  * Main controller
  * </p>
  *
  * @author Mikhail Vatalev(m.vatalev@euroats.com)
  */
 
 @With(GlobalContextParams.class)
 public class Application extends Controller {
     public static Result index() {
         // login form
         Form<AuthUser> loginForm = Form.form(AuthUser.class);
 
         User user = GlobalContextParams.loggedUser();
 
         // send message form
         Form<MessageDTO> sendMessageForm = Form.form(MessageDTO.class);
 
         // find rooms
         List<UserRoomRelationship> relationships = new ArrayList<>();
 
         // security validation
         if(user != null) {
             relationships = user.getRelationships();
             // Fill form by default values:
             // TODO find error on filling form, By the way user JS filling form in templates
             MessageDTO messageDTO = new MessageDTO(user.email);
             sendMessageForm.fill(messageDTO);
         }
 
         return ok(views.html.pages.home.render(loginForm, sendMessageForm, relationships));
     }

    public static Result features() {
        return ok(views.html.pages.features.render());
    }
   
 }
