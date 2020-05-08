 package com.jozeflang.android.germanirregularverbs.main;
 
 import java.io.IOException;
 import java.io.InputStream;
 
import com.jozeflang.android.germanirregularverbs.util.Utils;

 import android.app.Dialog;
import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.Button;
 
 /** 
  * About dialog 
  * @author Jozef Lang (developer@jozeflang.com)
  */
 public class AboutDialog extends Dialog {
 	
	public AboutDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.about_header));
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.about_layout);
 		initElements();
 	}
 	
 	private void initElements() {
 		// Webview
 		WebView webView = (WebView) findViewById(R.id.about_webview);
 		InputStream resourceStream = getContext().getResources().openRawResource(R.raw.about);
 		webView.loadData(Utils.readTextFile(resourceStream), "text/html", "utf-8");
 		try {
 			resourceStream.close();
 		} catch (IOException e) {
 			// Do nothing, just continue;
 		}
 		
 		// Close button
 		Button closeBtn = (Button) findViewById(R.id.about_closeBtn);
 		final Dialog dialog = (Dialog) this;
 		closeBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.cancel();
 			}
 		});
 	}
 	
 	
 
 }
