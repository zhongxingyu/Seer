 package controllers;
 
 import controllers.helper.PageHelper;
import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.With;
 import validation.EnhancedValidator;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 @With({Application.class, Auth.class})
 public abstract class AbstractController extends Controller {
 
     protected static EnhancedValidator validator() {
         return new EnhancedValidator(validation, params);
     }
 
     protected static PageHelper pageHelper(String page) {
         return new PageHelper(page, renderArgs);
     }
 
    protected static void flashInfo(String message, Object... args) {
        flash.put("info_box", Messages.get(message, args));
     }
 
    protected static void flashError(String message, Object... args) {
        flash.put("error_box", Messages.get(message, args));
    }
 
    protected static void flashSuccess(String message, Object... args) {
        flash.put("success_box", Messages.get(message, args));
    }
 }
