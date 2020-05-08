 package aid.network.http;
 
 import java.io.*;
 import java.net.*;
 
 import aid.network.http.*;
 
 public class HttpClient {
 	String urls;
 	HttpURLConnection connection;
 	
 	String getRequest;
 	String postRequest;
 	int requestLength;
 
 	PrintStream outputStream;
 	BufferedReader inputStream;
 	
 	HttpResponse response;
 	
 	//---------- construct methods ----------
 	
 	public HttpClient() {
 		urls=new String();
 		
 		getRequest=new String();
 		postRequest=new String();
 		requestLength=0;
 		
 		response=new HttpResponse();
 	}
 	
 	public boolean setUrl(String _url) {
 		// TODO: validate _url
 		urls=_url;
 		
 		return true;
 	}
 	
 	//---------- request methods ----------
 	
 	public boolean addGet(String _key, String _value) {
 		boolean ret=false;
 		
 		try {
 			if ( getRequest.isEmpty() ) {
 				getRequest = "?" + _key + "=" + URLEncoder.encode(_value, "UTF-8");
 			} else {
 				getRequest += ("&" + _key + "=" + URLEncoder.encode(_value,	"UTF-8"));
 			}
 			ret=true;
 		} catch (Exception e) {
 			System.err.println("--- oops: "+e);
 			e.printStackTrace();
 			
 			ret=false;
 		}
 		
 		return ret;
 	}
 	
 	/* feature
 	public boolean addPost(String _key, String _value) {
 		boolean ret=false;
 		
 		try {
 			if ( postRequest.isEmpty() ) {
 				postRequest = _key + "=" + URLEncoder.encode(_value, "UTF-8");
 			} else {
 				postRequest += ("&" + _key + "=" + URLEncoder.encode(_value,	"UTF-8"));
 			}
 			ret=true;
 		} catch (Exception e) {
 			System.err.println("--- oops: "+e);
 			e.printStackTrace();
 			
 			ret=false;
 		}
 		
 		return ret;
 	}
 	*/
 	
 	//---------- execute ----------
 	
 	public HttpResponse execute() {
 		String answer=new String();
 		
 		if ( !urls.isEmpty() ) {
 			if ( !getRequest.isEmpty() ) {
 				urls+=getRequest;
 				
 				try {
 					connection=(HttpURLConnection) new URL(urls).openConnection();
 					connection.setDoInput(true);
 					
 					// debug stuff
 //					int sz=connection.getHeaderFields().size();					
 //					for ( int i=0; i<sz; i++) {
 //						System.out.println("+++ header #"+i+" "+
 //								connection.getHeaderFieldKey(i)+
 //								": "+
 //								connection.getHeaderField(i)+
 //								" ("+connection.getHeaderField(i).length()+")"
 //								);
 //					}
 					
 					response.code=connection.getResponseCode();
 					response.codeDescription=connection.getResponseMessage();
 					
 					// if response /bad/ - return only error code
 					if ( response.code!=HttpURLConnection.HTTP_OK ) {
 						return response;
 					}
 					
 					// else - read answer
 					
 					String line;
 					inputStream = new BufferedReader(
 							new InputStreamReader( connection.getInputStream() )
 							);
 
 					while ( (line = inputStream.readLine() )!= null ) {
 						answer += (line + "\n");
 					}
 					inputStream.close();
 					
 					response.contentType=connection.getContentType();
 					response.contentLength=connection.getContentLength();
 					response.content=answer.replaceFirst("\n$", "");
 				} catch (Exception e) {
 					System.err.println("--- oops: "+e);
 					e.printStackTrace();
 					
 					response.code=-1;
 					response.codeDescription=e.getMessage();
 				}
 			}
 		}
 		
 		return response;
 	}
 }
