 package com.android.cb.systeminfocollector;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.text.Editable;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CBSystemInfoCollector extends Activity {
 	CBSystemInfoCenter mSystemInfo;
 	HashMap<String,String> mInfoMap;
 	LinearLayout mLayoutContent;
 	LinearLayout mLayoutButtons;
 	Button mButtonRetest;
 	Button mButtonSave;
 	Button mButtonAbout;
 	Button mButtonExit;
 	DialogInterface.OnClickListener mListenerSave;
 	EditText mEditPath;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.main);
 
         mSystemInfo = new CBSystemInfoCenter(this);
         mInfoMap = new HashMap<String,String>();
 
         mLayoutContent = (LinearLayout) this.findViewById(R.id.linearLayoutContent);
 
         mLayoutButtons = new LinearLayout(this);
         mLayoutButtons.setOrientation(LinearLayout.HORIZONTAL);
         mLayoutButtons.setGravity(Gravity.CENTER_HORIZONTAL);
 
         mButtonRetest = new Button(this);
         mButtonRetest.setText(R.string.button_retest);
         mButtonRetest.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				test();
 			}
         });

         mButtonSave = new Button(this);
         mButtonSave.setText(R.string.button_save);
         mButtonSave.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				showPathDialog();
 			}
         });
 
         mButtonAbout = new Button(this);
         mButtonAbout.setText(R.string.button_about);
         mButtonAbout.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showAboutDialog();
 			}
         });
 
         mButtonExit = new Button(this);
         mButtonExit.setText(R.string.button_exit);
         mButtonExit.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				CBSystemInfoCollector.this.finish();
 			}
         });
 
         mLayoutButtons.addView(mButtonRetest);
         mLayoutButtons.addView(mButtonSave);
         mLayoutButtons.addView(mButtonAbout);
         mLayoutButtons.addView(mButtonExit);
 
         test();
     }
 
     private void showPathDialog() {
     	mEditPath = new EditText(this);
         mEditPath.setText(R.string.dialog_save_path);
     	Editable text = mEditPath.getText();
     	mEditPath.setSelection(text.length());
         mListenerSave = new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dialog, int which) {
 				String path = mEditPath.getText().toString().trim();
 				int res = 0;
 				if (save(path,mInfoMap)) {
 					res = R.string.save_succeed;
 				} else {
 					res = R.string.save_failed;
 				}
 				Toast.makeText(CBSystemInfoCollector.this, CBSystemInfoCollector.this.getResources().getString(res), 0).show();
 			}
     	};
 
 		new AlertDialog.Builder(this).setTitle(R.string.dialog_inputTitle).setIcon(
 		android.R.drawable.ic_dialog_info).setView(mEditPath).setPositiveButton(R.string.dialog_ok, mListenerSave)
 		.setNegativeButton(R.string.dialog_cancel, null).show();
     }
 
     private void test() {
     	mLayoutContent.removeAllViews();
 
     	mInfoMap = mSystemInfo.getInfoTable();
     	Iterator<String> iterator = mInfoMap.keySet().iterator();
         while (iterator.hasNext())
         {
         	String key = iterator.next();
         	TextView viewLabel = new TextView(this);
         	TextView viewContent = new TextView(this);
         	viewLabel.setText(key + new String(": "));
         	viewContent.setText(mInfoMap.get(key));
 
         	LinearLayout layout = new LinearLayout(this);
         	layout.addView(viewLabel);
         	layout.addView(viewContent);
 
         	mLayoutContent.addView(layout);
         }
 
         mLayoutContent.addView(mLayoutButtons);
     }
 
     private boolean save(String path, HashMap<String,String> table) {
     	File file = new File(path);
     	if (!file.exists()) {
 			try {
 				file.createNewFile();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 				return false;
 			}
     	}
 
     	try {
 			FileOutputStream writter = new FileOutputStream(file);
 
 			Iterator<String> iterator = table.keySet().iterator();
 
 			String saprator = new String(": ");
 	        while (iterator.hasNext()) {
 	        	String content = new String();
 	        	String key = iterator.next();
 	        	content = key + saprator;
 	        	content += table.get(key);
 	        	content += "\n";
 
 	        	try {
 					writter.write(content.getBytes());
 				} catch (IOException e) {
 					return false;
 				}
 	        }
 
 	        try {
 				writter.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 			return false;
 		}
 
     	return true;
     }
 
     private void showAboutDialog() {
     	AlertDialog.Builder builder = new Builder(CBSystemInfoCollector.this);
     	builder.setTitle(R.string.dialog_aboutTitle);
     	String content = this.getResources().getString(R.string.about_content);
     	content += "\n";
     	content += this.getResources().getString(R.string.about_contact);
     	builder.setMessage(content);
     	builder.setPositiveButton(R.string.dialog_ok, null);
     	builder.create().show();
     }
 }
