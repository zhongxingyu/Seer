 package com.myapp;
 
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AddActivity extends Activity {
 
 	static final int CUSTOM_DIALOG_ID = 0; 
 	static String customText;
 	static int index;
 	int clickCounter = 0;
 	ArrayAdapter<String> adapter;
 	ArrayList<String> inputs = new ArrayList<String>();
 	
 	@Override
 	public void onBackPressed() {
 		Intent intent = new Intent(AddActivity.this, IndexActivity.class);
 		startActivity(intent);
 		finish();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add);
 		adapter = new ArrayAdapter<String>(AddActivity.this, android.R.layout.simple_list_item_1, inputs);
 		final ListView lv = (ListView) findViewById(R.id.listView3);
 		lv.setAdapter(adapter);
 		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
 			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 				index = position;
 				customText = ((TextView) view).getText().toString();
 				showDialog(CUSTOM_DIALOG_ID);
 				return true;
 			}
 		});
 		Button addRow = (Button) findViewById(R.id.button5);
 		addRow.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (clickCounter == 0) {
 					inputs.add("enter title here");
 				} else {
 					inputs.add("enter choice" + clickCounter + " here");
 				}
 				adapter.notifyDataSetChanged();
 				clickCounter++;
 			}
 		});
 		Button deleteRow = (Button) findViewById(R.id.button6);
 		deleteRow.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (clickCounter == 0) return;
 				clickCounter--;
 				inputs.remove(clickCounter);
 				adapter.notifyDataSetChanged();
 			}
 		});
 		Button submit = (Button) findViewById(R.id.button7);
 		submit.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (clickCounter < 3) {
 					showAlertDialog("Your vote has at least 3 rows.");
 					return;
 				}
 				ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 				for (int i = 0; i < clickCounter; i++) {
 					if (inputs.get(i).equals("")) {
 						showAlertDialog("Your vote contains errors.");
 						return;
 					}
 					postParameters.add(new BasicNameValuePair("counter", ((Integer) clickCounter).toString()));
 					postParameters.add(new BasicNameValuePair("owner", LoginActivity.username));
 					if (i == 0) {
 						postParameters.add(new BasicNameValuePair("title", inputs.get(i)));
 					} else {
 						postParameters.add(new BasicNameValuePair("choice"+i, inputs.get(i)));
 					}
 				}
 				String result = CustomHttpClient.executeHttpPost("http://teamwiki.phpfogapp.com/add.php", postParameters);
 				if ("success\n".equals(result)) {
 					Intent intent = new Intent(AddActivity.this, IndexActivity.class);
 					startActivity(intent);
 	        		Toast.makeText(getApplicationContext(), "Your vote has been recorded.", Toast.LENGTH_SHORT).show();
 	        		finish();
 				} else {
 					showAlertDialog("Your vote is not saved.");
 				}
 			}
 		});
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog;
 		switch (id) {
 		case CUSTOM_DIALOG_ID:
 			AlertDialog.Builder builder;
 			AlertDialog alertDialog;
 
 			Context mContext = AddActivity.this;
 			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
 			View layout = inflater.inflate(R.layout.custom_dialog, (ViewGroup) findViewById(R.id.layout_root));
 
 			final EditText et = (EditText) layout.findViewById(R.id.editText12);
 			et.setText(customText);
 
 			builder = new AlertDialog.Builder(mContext);
 			builder.setView(layout);
 			builder.setMessage("Enter your vote text here");
 			builder.setCancelable(false);
 			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					inputs.set(index, et.getText().toString());
 					adapter.notifyDataSetChanged();
 					removeDialog(CUSTOM_DIALOG_ID);
 				}
 			});
 			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					removeDialog(CUSTOM_DIALOG_ID);
 				}
 			});
 			alertDialog = builder.create();
 			dialog = alertDialog;
 			break;
 		default:
 			dialog = null;
 		}
 		return dialog;
 	}
 
 	private void showAlertDialog(String message) {
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(message)
 		       .setCancelable(false)
 		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {}
 		       });
 		AlertDialog alert = builder.create();
 		alert.show();
     }
 
 }
