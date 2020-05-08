 package org.trypill.simon;
 
 import java.net.UnknownHostException;
 import java.security.SecureRandom;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.X509TrustManager;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
import android.widget.TextView;
 
 
 public class Main extends Activity {
     Button btnRefresh = null;
     Button btnToggleAlarm = null;
     ImageView imgHSStatus = null;
     ImageView imgAlarmStatus = null;
    TextView lblHSStatus = null;
    TextView lblAlarmStatus = null;
     boolean alarmStatus = false;
     boolean spaceStatus = false;
 	
     
     public Main()
     {
     	initDefaultSSLVerifier();
     }
 
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
 
 	    btnRefresh = (Button) findViewById(R.id.btnRefresh);
 	    btnToggleAlarm = (Button) findViewById(R.id.btnToggleAlarm);
 	    imgHSStatus = (ImageView) findViewById(R.id.imgSpaceStatus);
 	    imgAlarmStatus = (ImageView) findViewById(R.id.imgAlarmStatus);
	    lblHSStatus = (TextView) findViewById(R.id.lblSpaceStatusValue);
	    lblAlarmStatus = (TextView) findViewById(R.id.lblAlarmStatusValue);
         
         btnRefresh.setOnClickListener(new Button.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				doRefresh();
 			}
         });
         
         btnToggleAlarm.setOnClickListener(new Button.OnClickListener() {
         	@Override
 			public void onClick(View v) {
         		Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
                 startActivity(intent);
                 finish();
 			}
         });
         
 
         try
         {
 //        	doLogin();
         }catch (Exception ex){
         	ex.printStackTrace();
         }
         //doRefresh();
     }
     
     private boolean isSpaceOpen() throws UnknownHostException
     {
     	String strAlarm = HttpHelper.getInstance().downloadText("https://www.hackerspace.lu/wiki/Syn2cat");
     	
    	if (strAlarm.contains("Come on in!"))
     		return true;
     	
     	return false;
     }
     
     private boolean isAlarmOn() throws UnknownHostException
     {
     	String strAlarm = HttpHelper.getInstance().downloadText("http://openduino.lan");
     	
     	if (strAlarm.contains("The syn2cat hackerspace is currently open"))
     		return false;
     	
     	return true;
     }
     
     private void displayError(String msg)
     {
     	new AlertDialog.Builder(this)
         .setTitle("Error")
         .setMessage(msg)
         .setPositiveButton("OK", null)
         .show();
     }
     
     private void doRefresh()
     {
         try
         {
         	spaceStatus = isSpaceOpen();
         		
 			if (spaceStatus)
 			{
 				imgHSStatus.setImageResource(R.drawable.security_high);
				lblHSStatus.setText("(Open)");
 			}else{
 				imgHSStatus.setImageResource(R.drawable.security_low);
				lblHSStatus.setText("(Closed)");
 			}
 			
 			try
 			{
 				alarmStatus = isAlarmOn();
 				
 				if (alarmStatus)
				{
 					imgAlarmStatus.setImageResource(R.drawable.user_online);
					lblAlarmStatus.setText("(On)");
				}else{
 					imgAlarmStatus.setImageResource(R.drawable.user_busy);
					lblAlarmStatus.setText("(Off)");
				}
 			}catch (UnknownHostException uh){
 				imgAlarmStatus.setImageResource(R.drawable.warning);
				lblAlarmStatus.setText("(No status)");
 			}
 
 			
         } catch (UnknownHostException uh){
         	displayError("Failed checking for h46645p4c3 status:\n" + uh.getMessage());
         }
     }
     
     private void initDefaultSSLVerifier()
     {
 		/** @TODO verify key */
 		try {
 			HttpsURLConnection
 					.setDefaultHostnameVerifier(new HostnameVerifier() {
 						public boolean verify(String hostname,
 								SSLSession session) {
 							return true;
 						}
 					});
 			SSLContext context = SSLContext.getInstance("TLS");
 			context.init(null, new X509TrustManager[] { new X509TrustManager() {
 				public void checkClientTrusted(X509Certificate[] chain,
 						String authType) throws CertificateException {
 				}
 
 				public void checkServerTrusted(X509Certificate[] chain,
 						String authType) throws CertificateException {
 				}
 
 				public X509Certificate[] getAcceptedIssuers() {
 					return new X509Certificate[0];
 				}
 			} }, new SecureRandom());
 			HttpsURLConnection.setDefaultSSLSocketFactory(context
 					.getSocketFactory());
 		} catch (Exception e) { // should never happen
 			e.printStackTrace();
 		}
     }
 }
