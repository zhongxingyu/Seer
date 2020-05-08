 package lu.sven.epcmodeler.http;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ContentProducer;
 import org.apache.http.entity.EntityTemplate;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.log4j.Logger;
 
 public class HTTPClient {
 	public String hostname = "localhost";
 	public Integer port = 1337;
 	private static Logger logger = Logger.getRootLogger();
 	private String responseString;
 	
 	   public HTTPClient(String hostn, String target, final String POSTContent) throws Exception {
 		   // TODO: handle TIMEOUT!!!
 		   HttpClient httpclient = new DefaultHttpClient();
 		   responseString="";
 		   ContentProducer cp = new ContentProducer() {
 			    public void writeTo(OutputStream outstream) throws IOException {
 			        Writer writer = new OutputStreamWriter(outstream, "UTF-8");
 			        writer.write(POSTContent);
 			        writer.flush();
 			    }
 			};
 			
 			HttpEntity entity = new EntityTemplate(cp);
 			HttpPost httppost = new HttpPost("http://"+hostn+":"+this.port+target);
 			httppost.setEntity(entity);
 			HttpResponse response = httpclient.execute(httppost);
 			logger.debug(response.toString());
 			
			InputStream is = response.getEntity().getContent();
 			
 			Writer writer = new StringWriter();
 			char[] buffer = new char[1024];
 			try{
 				Reader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
 				int n;
 				while((n=reader.read(buffer))!=-1){
 					writer.write(buffer,0,n);
 				}
 			}
 			finally{
 				is.close();
 			}
 			responseString = writer.toString();
 	   }
 	   public String getResponseString(){
 		   return responseString;
 	   }
 	    
 	}
