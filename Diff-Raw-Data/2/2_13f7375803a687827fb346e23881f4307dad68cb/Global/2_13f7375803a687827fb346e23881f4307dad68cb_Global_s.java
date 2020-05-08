 
 
 import net.sf.ehcache.search.Results;
 import play.GlobalSettings;
 import play.mvc.Http.RequestHeader;
 import play.mvc.Result;
 
 /**
  * @author Sander Demeester
  */
 public class Global extends GlobalSettings{
 	
 	public Result onError(Throwable t) {
 	    return internalServerError(
 			views.html.index.render("fout")
 	    );
 	}  
 	
 	 
 	@Override
 	public Result onHandlerNotFound(RequestHeader request){
 		return notFound(
 			views.html.PageNotFound.render("These aren't the pages you're looking for", request.path())
 		);
 	}
 }
 
