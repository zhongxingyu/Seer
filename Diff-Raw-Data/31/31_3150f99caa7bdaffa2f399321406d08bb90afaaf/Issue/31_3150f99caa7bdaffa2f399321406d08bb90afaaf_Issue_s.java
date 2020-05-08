 package br.com.androd;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.net.Uri;
 import android.opengl.Visibility;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 public class Issue extends Activity {
 	
 	private Button btnFundo;
 	private Button btnBrowser;
 	private LinearLayout lnrLayout;
 	private AutoCompleteTextView edtLink;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         btnFundo = (Button) findViewById(R.id.btnFundo);
         lnrLayout = (LinearLayout) findViewById(R.id.linearLayout);
         btnBrowser = (Button) findViewById(R.id.btnBrowser);
         edtLink = (AutoCompleteTextView) findViewById(R.id.autoLink); 
         
         btnFundo.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View arg0) {
 				lnrLayout.setBackgroundColor(Color.BLUE);
 //				if(lnrLayout.getBackground()){
 //					lnrLayout.setBackgroundColor(Color.BLUE);
 //				}else{
 //					lnrLayout.setBackgroundColor(Color.BLACK);
 //				}
 			}
 		});
         
         btnBrowser.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View arg0) {
 				String link = edtLink.getText().toString();
 				
 				final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link));
 				startActivity(intent);
 			}
 		});
     }
 }
