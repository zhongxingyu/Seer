 package org.acl.root;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ShowLogActivity extends Activity implements View.OnClickListener {
 	public static final String LOGFILE = "log.txt";
 	private Button btnBack;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.show_log);
 
 		loadScreen();
 		btnBack = (Button) findViewById(R.id.btnBack);
 		btnBack.setOnClickListener(this);
 	}
 
 	private void loadScreen() {
 
 		FileInputStream fis = null;
 		InputStreamReader inputreader = null;
 		BufferedReader buffreader = null;
 				
 		LinearLayout callList = (LinearLayout) findViewById(R.id.showLogLayout);
 		TextView text;
 
 		try {
 			fis = openFileInput(LOGFILE);
 
 			inputreader = new InputStreamReader(fis);
 			buffreader = new BufferedReader(inputreader);

 			String line;
 			// read every line of the file into the line-variable, on line at
 			// the time
 			while ((line = buffreader.readLine()) != null) {
 
 				text = new TextView(this);
 				text.setLayoutParams(new LayoutParams(
 						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 				text.setClickable(false);
 				text.setText(line);
 				callList.addView(text);
 			}
 		} catch (FileNotFoundException e) {
			Toast.makeText(this, "File Still not created", Toast.LENGTH_SHORT)
 					.show();
 		} catch (IOException e) {
 			Toast.makeText(this, "Exception" + e.toString(), Toast.LENGTH_SHORT)
 					.show();
 		} finally {
 			try {
 				if (buffreader != null) buffreader.close();
 				if (inputreader != null) inputreader.close();
 				if (fis != null) fis.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	@Override
 	public void onClick(View view) {
 
 		boolean result = deleteFile(LOGFILE);
 
 		if (result) {
 			LinearLayout callList = (LinearLayout) findViewById(R.id.showLogLayout);
 			callList.removeAllViews();
 		} else {
 			Toast.makeText(this, "File has not been deleted", Toast.LENGTH_SHORT)
 			.show();
 		}
 
 	}
 
 }
