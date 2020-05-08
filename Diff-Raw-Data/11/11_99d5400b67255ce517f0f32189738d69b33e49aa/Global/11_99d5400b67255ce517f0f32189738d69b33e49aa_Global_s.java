 import play.*;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class Global extends GlobalSettings {
  
	@Override
	public Result onError(Throwable err) {
		return Controller.notFound(views.html.errors.error.render());
	}
   
 	@Override
 	public Result onHandlerNotFound(final String uri) {
		return Controller.notFound(views.html.errors.error.render());
 	}
 }
