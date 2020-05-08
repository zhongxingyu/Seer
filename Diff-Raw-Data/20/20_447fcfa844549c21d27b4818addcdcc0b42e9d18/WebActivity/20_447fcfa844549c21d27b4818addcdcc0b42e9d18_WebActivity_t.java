 package com.techram.pondyapp;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
import android.content.res.Configuration;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Environment;
 import android.text.Html;
 import android.text.Html.ImageGetter;
 import android.text.method.ScrollingMovementMethod;
 import android.widget.TextView;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 import com.techram.R;
 
 public class WebActivity extends Activity {
 	private AdView adView;
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.web);
 		String url = getIntent().getExtras().getString("Thumbnail");
 		String content = getIntent().getExtras().getString("content");
 		TextView  cnt = (TextView) findViewById(R.id.content);
 		cnt.setText(Html.fromHtml(content, imgGetter, null));
 		cnt.setMovementMethod(new ScrollingMovementMethod());
 		
 		if (adView == null) {
 			adView = (AdView) this.findViewById(R.id.AdView);
 			adView.loadAd(new AdRequest());
 
 		} else {
 			adView.loadAd(new AdRequest());
 		}
 	}
 	
 	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    }

	
	@Override
 	public void onDestroy() {
 		if (adView != null) {
 			adView.destroy();
 		}
 		super.onDestroy();
 	}
 
     private ImageGetter imgGetter = new ImageGetter() {
 
             public Drawable getDrawable(String source) {
                     HttpGet get = new HttpGet(source);
                     DefaultHttpClient client = new DefaultHttpClient();
                     Drawable drawable = null;
                     try {
                             HttpResponse response = client.execute(get);
                             InputStream stream = response.getEntity().getContent();
                             FileOutputStream fileout = new FileOutputStream(new File(
                                             Environment.getExternalStorageDirectory()
                                                             .getAbsolutePath()
                                                             + "/test.jpg"));
                             int read = stream.read();
                             while (read != -1) {
                                     fileout.write(read);
                                     read = stream.read();
                             }
                             fileout.flush();
                             fileout.close();
                             drawable = Drawable.createFromPath(Environment
                                             .getExternalStorageDirectory().getAbsolutePath()
                                             + "/test.jpg");
                             //drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()
                             drawable.setBounds(0, 0, 240, 180);
 
                     } catch (ClientProtocolException e) {
                             e.printStackTrace();
                     } catch (IOException e) {
                             e.printStackTrace();
                     }
                     return drawable;
             }
     };
 }
