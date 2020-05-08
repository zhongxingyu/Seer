 package wicket.contrib.datepicker;
 
 import wicket.protocol.http.WebApplication;
 
 public class Application extends WebApplication {
 
 	@Override
	public Class getHomePage() {
 		return IndexPage.class;
 	}
 
 }
