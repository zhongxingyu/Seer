 package wicket.contrib.datepicker;
 
import wicket.Page;
 import wicket.protocol.http.WebApplication;
 
 public class Application extends WebApplication {
 
 	@Override
	public Class<? extends Page> getHomePage() {
 		return IndexPage.class;
 	}
 
 }
