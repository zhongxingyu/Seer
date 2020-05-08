 package tw.ipis.routetaiwan;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.ContactsContract;
 import android.telephony.SmsManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.ContextThemeWrapper;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class sms_send extends Activity {
 	private static final String TAG = "--SMS--";
 	// List view
 	private ListView lv;
 	ArrayAdapter<String> adapter;
 	EditText inputSearch, pos_title;
 	ArrayList<HashMap<String, String>> productList;
 	String title, latlng;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.sms_search_contact);
 
 		Bundle Data = this.getIntent().getExtras();
 		title = Data.getString("title");
 		latlng = Data.getString("latlng");
 
 		String contact[] = get_all_contact();
 
 		lv = (ListView) findViewById(R.id.all_contacts);
 		inputSearch = (EditText) findViewById(R.id.inputSearch);
 		pos_title = (EditText) findViewById(R.id.point_name);
 		pos_title.setText(title);
 
 		// Adding items to listview
 		adapter = new ArrayAdapter<String>(this, R.layout.contact_list, R.id.contact_name, contact);
 		lv.setAdapter(adapter);
 
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				String selectedFromList =(String) (lv.getItemAtPosition(position));
 				inputSearch.setText(selectedFromList.replaceAll("[^+0-9]", ""));
 
 			}
 		});
 
 		inputSearch.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
 				// When user changed the Text
 				sms_send.this.adapter.getFilter().filter(cs);   
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
 					int arg3) {
 
 			}
 
 			@Override
 			public void afterTextChanged(Editable arg0) {
 			}
 		});
 
 	}
 
 	public void start_sending(View v) {
 		final String phoneNo = inputSearch.getText().toString();
 		if(!phoneNo.matches("[+0-9]+")) {
 			Toast.makeText(getApplicationContext(), getResources().getString(R.string.info_illegal_num),
 					Toast.LENGTH_LONG).show();
 			return;
 		}
 		String title_name = pos_title.getText().toString();
 
 		final String sms = String.format("rtw,%s,%s", title_name, latlng);
 
 		Log.i(TAG, "sms="+sms);
 
 		final File chk_fist_use = new File(Environment.getExternalStorageDirectory() + "/.routetaiwan/.sms_remind");
 
 		if(!chk_fist_use.exists()) {
 			AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.ThemeWithCorners));
 			dialog.setTitle(getResources().getString(R.string.notice));
 			dialog.setMessage(getResources().getString(R.string.info_sms_sending));
 
 			CheckBox checkBox = new CheckBox(this);
 			checkBox.setText(getResources().getString(R.string.no_remind));
 			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 				@Override
 				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 					if (isChecked) {
 						if(!chk_fist_use.exists()) {
 							try {
 								chk_fist_use.createNewFile();
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 						}
 					} else {
 						if(chk_fist_use.exists())
 							chk_fist_use.delete();
 					}
 				}
 			});
 
 			LinearLayout linearLayout = new LinearLayout(this);
 			linearLayout.setLayoutParams( new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
 					LinearLayout.LayoutParams.MATCH_PARENT));
 			linearLayout.setOrientation(1);     
 			linearLayout.addView(checkBox);
 
 			dialog.setView(linearLayout);
 
 
 			dialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 
 					SmsManager smsManager = SmsManager.getDefault();
 					smsManager.sendTextMessage(phoneNo, null, sms, null, null);
 					Toast.makeText(getApplicationContext(), getResources().getString(R.string.sms_sent),
 							Toast.LENGTH_LONG).show();
 					finish();
 				}
 			});
 			dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					// do nothing
 					finish();
 				}
 			});
 			dialog.show();
 		}
 		else {
 			SmsManager smsManager = SmsManager.getDefault();
 			smsManager.sendTextMessage(phoneNo, null, sms, null, null);
 			Toast.makeText(getApplicationContext(), getResources().getString(R.string.sms_sent),
 					Toast.LENGTH_LONG).show();
 			finish();
 		}
 	}
 	public static String getMD5EncryptedString(String encTarget){
 		MessageDigest mdEnc = null;
 		try {
 			mdEnc = MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) {
 			System.out.println("Exception while encrypting to md5");
 			e.printStackTrace();
 		} // Encryption algorithm
 		mdEnc.update(encTarget.getBytes(), 0, encTarget.length());
 		String md5 = new BigInteger(1, mdEnc.digest()).toString(16) ;
 		return md5;
 	}
 
 	public void cancel(View v) {
 		finish();
 	}
 
 	public String[] get_all_contact() {
 		ArrayList<String> contact = new ArrayList<String>();
 		ContentResolver cr = getContentResolver();
 		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
 				null, null, null, null);
 		if (cur.getCount() > 0) {
 			while (cur.moveToNext()) {
 				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
 				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 				if (Integer.parseInt(cur.getString(
 						cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
 					Cursor pCur = cr.query(
 							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
 							null,
 							ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
 							new String[]{id}, null);
 					while (pCur.moveToNext()) {
 						String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
 
						contact.add(String.format("%s\t%s", name, phoneNo));
 					}
 					pCur.close();
 				}
 			}
 		}
 		if(contact.size() > 0) {
 			String list[] = new String[contact.size()];
 			for(int i=0; i<contact.size(); i++) {
 				list[i] = contact.get(i);
 			}
 			return list;
 		}
 
 		return new String[0];
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 }
