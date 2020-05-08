 import static play.mvc.Results.notFound;
 import play.Application;
 import play.GlobalSettings;
 import play.Logger;
 import play.mvc.Http.RequestHeader;
 import play.mvc.Result;
 
 public class Global extends GlobalSettings {
 
 	@Override
 	public void onStart(Application app) {
 		controllers.Application.loadServices();
 		Logger.info("Application has started");		
 	}
 
 	@Override
 	public void onStop(Application app) {
 		Logger.info("Application shutdown...");
 	}
 
 	@Override
	public Result onError(RequestHeader arg0, Throwable arg1) {
 		return notFound("Page Not Found");
 	}
	
 	@Override
	public Result onHandlerNotFound(RequestHeader paramRequestHeader) {
 		return notFound("Page Not Found");
 	}
 	
 	@Override
	public Result onBadRequest(RequestHeader paramRequestHeader, String paramString) {
 		return notFound("Page Not Found");
 	}

 }
