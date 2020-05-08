 package uk.codingbadgers.bFundamentals.error;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 
 import uk.codingbadgers.bFundamentals.bFundamentals;
 
 public class ReportExceptionRunnable implements Runnable {
 
 	private Throwable throwable;
 
 	public ReportExceptionRunnable(Throwable ex) {
 		this.throwable = ex;
 	}
 	
 	@Override
 	public void run() {
 		try {
 			List<NameValuePair> data = new ArrayList<NameValuePair>();
 			data.add(new BasicNameValuePair("password", DigestUtils.md5Hex(bFundamentals.getConfigurationManager().getCrashPassword())));
 			data.add(new BasicNameValuePair("project", "bFundamentals"));
 			data.add(new BasicNameValuePair("cause", getException(throwable)));
 			data.add(new BasicNameValuePair("message", getMessage(throwable)));
 			data.add(new BasicNameValuePair("st", buildStackTrace(throwable)));
			HttpPost post = new HttpPost("http://server.mcbadgercraft.com/crashtracker/report.php");
 			post.setEntity(new UrlEncodedFormEntity(data));
 			
 			DefaultHttpClient client = new DefaultHttpClient();
 			HttpResponse responce = client.execute(post);
             String result = EntityUtils.toString(responce.getEntity());
             if (bFundamentals.getConfigurationManager().isDebugEnabled()) System.out.println(result);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private String getException(Throwable cause) {
 		while (cause.getCause() != null) {
 			cause = cause.getCause();
 		}
 		
 		return cause.getClass().getName();
 	}
 
 	private String getMessage(Throwable cause) {
 		while (cause.getCause() != null) {
 			cause = cause.getCause();
 		}
 		
 		return cause.getMessage();
 	}
 	
 	private String buildStackTrace(Throwable cause) {
 		StringWriter writer = new StringWriter();
 		cause.printStackTrace(new PrintWriter(writer));
 		return writer.toString();
 	}
 }
