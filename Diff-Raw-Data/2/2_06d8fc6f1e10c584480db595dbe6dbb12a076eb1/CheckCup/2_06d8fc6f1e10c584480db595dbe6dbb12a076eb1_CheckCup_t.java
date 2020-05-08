 package lobos.andrew.cuptagclient;
 
 import java.io.ByteArrayOutputStream;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 
 public class CheckCup extends android.os.AsyncTask<String, Integer, String> {
 
 	Context context;
 	public CheckCup(Context context)
 	{
 		super();
 		this.context = context;
 	}
 	
 	@Override
 	protected String doInBackground(String... input) {
 		try
 		{
 			 if ( input.length != 2 )
 				 return "Bad request";
 			 
 			 HttpClient httpclient = new DefaultHttpClient();
 			 HttpResponse response = httpclient.execute(new HttpGet("http://andrew.lobos.me/cans/check.php?data="+input[0]+"&name="+input[1]));
 			 StatusLine statusLine = response.getStatusLine();
 			 if(statusLine.getStatusCode() == HttpStatus.SC_OK)
 			 {
 			        ByteArrayOutputStream out = new ByteArrayOutputStream();
 			        response.getEntity().writeTo(out);
 			        out.close();
				return out.toString();
 			 } 
 			 else
 			 {
 					return "Request Failed";
 			 }
 		} catch (Exception e)
 		{
 			return "Request Failed "+e.getMessage();
 		}
 	}
 	
 	protected void onPostExecute(String result)
 	{
 		
 		AlertDialog.Builder alert = new AlertDialog.Builder(context).setTitle("Result").setMessage(result);
 		alert.setCancelable(false);
 		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				
 			}
 		});
 		
 		
 		alert.show();
 	}
 
 }
