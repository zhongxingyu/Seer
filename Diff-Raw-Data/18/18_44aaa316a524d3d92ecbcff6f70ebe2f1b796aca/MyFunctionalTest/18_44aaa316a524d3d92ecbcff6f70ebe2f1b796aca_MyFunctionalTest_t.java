 import play.Play;
 import play.mvc.Http.Request;
 import play.mvc.Http.Response;
 import play.test.FunctionalTest;
 
 public abstract class MyFunctionalTest extends FunctionalTest {
     public static Response GET(Object url) {
 	return GET(newRequest(), url);
     }
 
     public static Request newRequest() {
	Request request = Request.createRequest("", "GET", "/", "", "", null,
		"", "", false,
		Integer.parseInt(Play.configuration.getProperty("http.port")),
		"localhost", false, null, null);
 	return request;
     }
 }
