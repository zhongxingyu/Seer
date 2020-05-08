 import play.Play;
 import play.mvc.Http.Request;
 import play.mvc.Http.Response;
 import play.test.FunctionalTest;
 
 public abstract class MyFunctionalTest extends FunctionalTest {
     public static Response GET(Object url) {
 	return GET(newRequest(), url);
     }
 
     public static Request newRequest() {
	Request request = new Request();
	request.domain = "localhost";
	request.port = Integer.parseInt(Play.configuration
		.getProperty("http.port"));
	request.method = "GET";
	request.path = "/";
	request.querystring = "";
 	return request;
     }
 }
