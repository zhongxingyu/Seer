 /**
   * Copyright (c) 2011: mnemr.com contributors. All rights reserved.
   *
   * This program is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as published 
   * the Free Software Foundation, either version 3 of the License, 
   * (at your option) any later version.
   *
   * program is distributed in the hope that it will be 
   * but WITHOUT ANY WARRANTY; without even the implied warranty 
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
   * GNU General Public License for more details.
   *
   * You should have received a copy of the GNU General Public 
   * along with this program.  If not, see <http://www.gnu.org/licenses/>.
   *
   **/
 
 package com.mnemr.ui;
 
 import com.mnemr.R;
 import com.mnemr.provider.Mnem;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.text.Editable;
 import android.text.method.KeyListener;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MnemEditorActivity extends Activity {
 
 	protected static final String TAG = "Editor";
 	private static int related = 0;
 	private EditText text;
 	private TextView nmb;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    
 	    if (getIntent().getData() == null)
 	    	getIntent().setData(Mnem.CONTENT_URI);
 	
 	    setContentView(R.layout.editor);
 
 	    text = (EditText) findViewById(R.id.text);
 	    text.setOnKeyListener(new OnKeyListener() {
 			
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (event.getAction() == KeyEvent.ACTION_DOWN) {
 					// scale text to fit screen
 					int textSize = 500;
 					String[] lines = text.getText().toString().split("\\n");
 					for (int i = 0; i < lines.length; i++) {
 						textSize = (int) Math.min(textSize, (text.getTextSize() * 
 								(getWindowManager().getDefaultDisplay().getWidth()-42 ) / 
 								text.getPaint().measureText(lines[i]+"mi")));
 					}
 					Log.d(TAG, "textSize: "+textSize);
 					text.setTextSize(Math.min(105, textSize));
 				}
 		        return false;
 			}
 			
 		});
 	    
 //	    ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();
 
 
 	    if (getIntent().getAction().equals(Intent.ACTION_EDIT)) {
 	    	Cursor cursor = getContentResolver().query(getIntent().getData(), Mnem.PROJECTION, null, null, null);
 	    	if (cursor.getCount() > 0) {
 	    		cursor.moveToFirst();
 	    		text.setText(cursor.getString(cursor.getColumnIndex(Mnem.TEXT)));
 	    	}
 	    	
 	    }
 	    
 	    findViewById(R.id.arrow).setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 
				if (text.getText().length() == 0) {
					((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(500);
 					Toast.makeText(MnemEditorActivity.this, "nothing to nehm :/", Toast.LENGTH_SHORT).show();
					return;
 				}
 				ContentValues values = new ContentValues();
 				values.put(Mnem.TEXT, text.getText().toString());
 				((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(42);
 				
 				if (getIntent().getAction().equals(Intent.ACTION_EDIT)) {
 					getContentResolver().update(getIntent().getData(), values, null, null);
 					Toast.makeText(MnemEditorActivity.this, "updated", Toast.LENGTH_SHORT).show();
 					finish();
 				} else {
 					if (getIntent().getData().getLastPathSegment().equals("mnemons"))
 						getIntent().setData(getContentResolver().insert(getIntent().getData(), values));
 					else
 						getContentResolver().insert(getIntent().getData(), values);
 					Toast.makeText(MnemEditorActivity.this, "genehmt!", Toast.LENGTH_SHORT).show();
 //					text.setText("");
 					if (getIntent().getData().getLastPathSegment().equals("related"))
 						startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData())); 
 					else
 						startActivity(new Intent(Intent.ACTION_INSERT, Uri.withAppendedPath(getIntent().getData(), "related"))); 
 				}
 			}
 		});
 	    
 	    findViewById(R.id.plus).setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View arg0) {
 				
				if (text.getText().length() == 0) {
					((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(500);
					Toast.makeText(MnemEditorActivity.this, "nothing to nehm :/", Toast.LENGTH_SHORT).show();
					return;
				}
 				ContentValues values = new ContentValues();
 				values.put(Mnem.TEXT, text.getText().toString());
 				((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(42);
 				
 				if (getIntent().getData().getLastPathSegment().equals("mnemons"))
 					getIntent().setData(getContentResolver().insert(getIntent().getData(), values));
 				else
 					getContentResolver().insert(getIntent().getData(), values);
 				Toast.makeText(MnemEditorActivity.this, "saved", Toast.LENGTH_SHORT).show();
 				
 				startActivity(new Intent(Intent.ACTION_INSERT, Mnem.CONTENT_URI)
 //				startActivity(new Intent(MnemEditorActivity.this, MnemListActivity.class)
 //				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
 //				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
 				); 
 			}
 		});
 	    
 	    findViewById(R.id.list).setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View arg0) {
 				startActivity(new Intent(MnemEditorActivity.this, MnemListActivity.class)
 //				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
 				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
 				);
 			}
 		});
 	    
 	    related++;
 
 	    
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 	}
 
 	@Override
 	protected void onResume() {
 		int count; 
 		if (getIntent().getData().getLastPathSegment().equals("related")) {
 			count = getContentResolver().query(getIntent().getData(), new String[]{}, null, null, null).getCount()+2;
 		} else if (getIntent().getData().getLastPathSegment().equals("mnemons")) {
 			related = 1;
 			count = 1;
 		} else {
 			count = getContentResolver().query(
 					Uri.withAppendedPath(getIntent().getData(), "/related"), 
 					new String[]{}, null, null, null).getCount()+2;
 		}
 		nmb = (TextView) findViewById(R.id.nmb);
 		nmb.setText(related +"/"+count);
 		super.onResume();
 	}
 	
 	@Override
 	public void finish() {
 		related--;
 		super.finish();
 	}
 	
 }
