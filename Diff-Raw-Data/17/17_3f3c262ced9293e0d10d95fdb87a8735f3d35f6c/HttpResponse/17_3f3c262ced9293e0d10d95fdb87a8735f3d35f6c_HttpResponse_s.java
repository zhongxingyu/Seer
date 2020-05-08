 package aid.network.http;
 
 public class HttpResponse {
 	public int code;
 	public String codeDescription;
 	public String contentType;
 	public int contentLength;
 	public String content;
 	
 	public HttpResponse() {
 		code=0;
 		codeDescription=new String();
 		contentType=new String();
 		content=new String();
 	}
 }
