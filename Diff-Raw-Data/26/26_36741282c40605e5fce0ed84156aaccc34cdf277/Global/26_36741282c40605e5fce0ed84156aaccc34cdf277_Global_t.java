 import static play.mvc.Results.notFound;
 import play.Application;
 import play.GlobalSettings;
 import play.Logger;
import play.Play;
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
	public Result onError(RequestHeader header, Throwable t) {
		if (Play.isDev()) {
			return super.onError(header, t);
		}
 		return notFound("Page Not Found");
 	}

 	@Override
	public Result onHandlerNotFound(RequestHeader header) {
		if (Play.isDev()) {
			return super.onHandlerNotFound(header);
		}
 		return notFound("Page Not Found");
 	}
 	
 	@Override
	public Result onBadRequest(RequestHeader header, String paramString) {
		if (Play.isDev()) {
			super.onBadRequest(header, paramString);
		}
 		return notFound("Page Not Found");
		
 	}
 }
