 package middleware;
 
 import webserver.Request;
 import webserver.Response;
 
 public class HTTPVersionMiddleware implements Middleware{
 
 
 	public void execute(Request request, Response response) throws MiddlewareException{
		if(request.httpMajor != 1 || request.httpMinor != 0 || request.httpMinor != 1){
 			response.code = Response.STATUS_505;
 			throw new MiddlewareException("Invalid http protocol version");
 		}
		response.httpMajor = request.httpMajor;
		response.httpMinor = request.httpMinor;
 	}
 
 }
