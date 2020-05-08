 package com.a.space;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class SetupSpaceChooserActivity extends Activity {
 	private List<CharSequence> mChosenSpaces;
 	private TextView mSpacesListTextView;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.setup_space_chooser_activity);
 		mChosenSpaces = new LinkedList<CharSequence>();
 		mSpacesListTextView = (TextView)findViewById(R.id.textView2);
 		assert mSpacesListTextView != null;
 		
 		Button pickSpacesButton = (Button)findViewById(R.id.button1);
 		pickSpacesButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View arg0) {
 				showSpacesChooser();
 			}
 		});
 		
 		// TODO disable this button when mChosenSpaces.size() < 1
 		Button finishSetupButton = (Button)findViewById(R.id.button2);
 		finishSetupButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (mChosenSpaces.size() < 1) {
 					showNoSpaceChosenWarning();
 					return;
 				}
 				
 				finish();
 				startActivity(new Intent(SetupSpaceChooserActivity.this, SetupFinishedActivity.class));
 			}
 		});
 
 		showSpacesChooser();
 	}
 	
 	private void showNoSpaceChosenWarning() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Spacehold");
 		builder.setMessage("Choose a space before proceeding.");
 		builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				return;
 			}
 		});
 		
 		builder.create().show();
 	}
 	
 	private void showSpacesChooser() {
 		final CharSequence[] items = getListOfSpaces();
 		boolean[] states = new boolean[items.length];
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Select spaces to join");
 		builder.setMultiChoiceItems(items, states, new DialogInterface.OnMultiChoiceClickListener() {
 			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
 				assert which < items.length;
 				if (isChecked) {
 					mChosenSpaces.add(items[which]);
 				} else {
 					mChosenSpaces.remove(items[which]);
 				}
 				refreshSpaceListTextView();
 			}
 		});
 		builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface arg0, int arg1) {
 				for (CharSequence s : mChosenSpaces) {
 					addSpaceToServer(s);
 				}
 			}
 		});
 		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 		});
 		builder.create().show();
 	}
 	
 	private void refreshSpaceListTextView() {
 		StringBuilder sb = new StringBuilder();
 		for (CharSequence s : mChosenSpaces) {
 			sb.append(s);
 			sb.append("\n");
 		}
 		mSpacesListTextView.setText(sb.toString());
 	}
 	
 	private void addSpaceToServer(CharSequence s) {
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpContext localContext = new BasicHttpContext();
 		SharedPreferences settings = getSharedPreferences(SHUtil.PREFS_NAME, 0);
 		String userName = settings.getString(SHUtil.ACCOUNT_NAME, "");
 		if (userName.equals("")) {
 			Log.wtf("Spacehold", "no user registered with app");
 		}
 
 		HttpGet httpGet = new HttpGet("http://shewu.scripts.mit.edu/Spacehold/spacehold"); // TODO change to our URL
 		try {
 			HttpResponse response = httpClient.execute(httpGet, localContext);
 			BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 			int spacesWritten = Integer.parseInt(in.readLine());
 			if (spacesWritten < mChosenSpaces.size()) {
 				Log.wtf("Spacehold", "did not add expected number of spaces for user " + userName);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private CharSequence[] getListOfSpaces() {
 		CharSequence[] seq = new CharSequence[0];
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet("http://shewu.scripts.mit.edu/Spacehold/spacehold"); // TODO change to our URL
 		try {
 			HttpResponse response = httpClient.execute(httpGet, localContext);
 			
 			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 
 			String line = null;
 			List<String> tmp = new ArrayList<String>();
 			while ((line = reader.readLine()) != null) {
 				tmp.add(line);
 			}
 
 			seq = new CharSequence[tmp.size()];
 			for (int i = 0; i < tmp.size(); ++i) {
 				seq[i] = tmp.get(i);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 				
 		return seq;
 	}
 }
