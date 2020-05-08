 package smartcampus.android.template.standalone.Activity.ProfileBlock.RisolutoreSubBlock;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import smartcampus.android.template.standalone.IntroBlock.UserConstant;
 import smartcampus.android.template.universiadi.R;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.Dialog;
 import android.net.http.SslError;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.webkit.SslErrorHandler;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class IceFireWebView extends Activity {
 
 	private Dialog dialog;
 	private WebView webView;
 
 	@SuppressLint("SetJavaScriptEnabled")
 	@Override
 	protected void onCreate(Bundle arg0) {
 		// TODO Auto-generated method stub
 		super.onCreate(arg0);
 		setContentView(R.layout.activity_ice_fire);
 
 		webView = (WebView) findViewById(R.id.ice_fire_webview);
 		webView.setWebViewClient(new MyWebViewClient());
 
 		new AsyncTask<Void, Void, Void>() {
 
 			@Override
 			protected void onPreExecute() {
 				// TODO Auto-generated method stub
 				super.onPreExecute();
 
 //				dialog = new Dialog(IceFireWebView.this);
 //				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 //				dialog.setContentView(R.layout.dialog_wait);
 //				dialog.getWindow().setBackgroundDrawableResource(
 //						R.drawable.dialog_rounded_corner_light_black);
 //				dialog.show();
 //				dialog.setCancelable(true);
 //				dialog.setOnCancelListener(new OnCancelListener() {
 //
 //					@Override
 //					public void onCancel(DialogInterface dialog) {
 //						// TODO Auto-generated method stub
 //						cancel(true);
 //						finish();
 //					}
 //				});
 
 			}
 
 			@Override
 			protected Void doInBackground(Void... params) {
 				// TODO Auto-generated method stub
 				HttpClient httpclient = new DefaultHttpClient();
 				HttpPost httppost = new HttpPost(
 						getString(R.string.URL_ICE_AND_FIRE));
 				try {
 					System.out.print("url:"+getString(R.string.URL_ICE_AND_FIRE)+" parametri:"+new StringEntity("username="
 							+ UserConstant.getUsername() + "&password="
 							+ UserConstant.getPassword()));
					httppost.setEntity(new StringEntity("username="
							+ UserConstant.getUsername() + "&password="
							+ UserConstant.getPassword()));
 
 					HttpResponse response = httpclient.execute(httppost);
 					String data = new BasicResponseHandler()
 							.handleResponse(response);
 					webView.loadData(data, "text/html", "utf-8");
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ClientProtocolException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				return null;
 			}
 
 		}.execute();
 	}
 
 	private class MyWebViewClient extends WebViewClient {
 
 		@Override
 		public void onPageFinished(WebView view, String url) {
 			// TODO Auto-generated method stub
 			super.onPageFinished(view, url);
 
 //			if (dialog != null)
 //				dialog.dismiss();
 		}
 
 		@Override
 		public void onReceivedSslError(WebView view, SslErrorHandler handler,
 				SslError error) {
 
 			// this will ignore the Ssl error and will go forward to your site
 			handler.proceed();
 		}
 	}
 
 }
