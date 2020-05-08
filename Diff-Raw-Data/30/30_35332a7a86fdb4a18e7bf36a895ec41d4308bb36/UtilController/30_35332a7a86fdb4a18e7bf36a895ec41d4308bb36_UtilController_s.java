 package controllers.abstracts;
 
 import controllers.Application;
 import controllers.filters.UserFirstLoginFilter;
 import controllers.helper.CollectionHelper;
 import controllers.helper.PageHelper;
 import controllers.security.Auth;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.With;
 import validation.EnhancedValidator;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 
 public abstract class UtilController extends Controller {
 
    protected static CollectionHelper collectionHelper() {
        return new CollectionHelper();
    }
 
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
