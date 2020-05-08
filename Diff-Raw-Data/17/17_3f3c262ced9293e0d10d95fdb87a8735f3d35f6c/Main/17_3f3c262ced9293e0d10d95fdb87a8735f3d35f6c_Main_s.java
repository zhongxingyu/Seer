 import java.net.HttpURLConnection;
 
 import aid.network.http.*;
 
 /**
  * this class is just test class
  * main purpose is testing HttpClient
  * @author ilya
  */
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
			String urls="http://127.0.0.1/~ilya/test";
//			String urls="http://127.0.0.1/~ilya/test/403";
 			
 			HttpClient httpClient=new HttpClient();
 	
 			if ( httpClient.setUrl(urls) &&
 					httpClient.addGet("getKey", "getValue") &&
 					httpClient.addGet("getKey2", "getValue2")
 					) {
 				HttpResponse responce=httpClient.execute();
 //				System.out.println(">>>\n"+answer+"\n<<<");
 				if ( responce.code==HttpURLConnection.HTTP_OK ) {
 					System.out.println(">>>\n"+responce.content+"\n<<<");
 				} else {
 					System.out.println("responce: "+responce.code+
 							" - "+responce.codeDescription);
 				}
 			} else {
 				System.err.println("--- oops: something wrong");
 			}
 		} catch (Exception e) {
 			System.err.println("--- oops (main): "+e);
 			e.printStackTrace();
 		}
 	}
 
 }
