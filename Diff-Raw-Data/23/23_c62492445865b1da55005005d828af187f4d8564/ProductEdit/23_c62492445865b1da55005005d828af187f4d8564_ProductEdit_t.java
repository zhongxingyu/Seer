 package com.dnsalias.sanja.simplecarbocalc;
 
 import android.app.Activity;
 import static junit.framework.Assert.*;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 /**
  * @author Oleksander "Sanja" Byelkin
  * 
  */
 public class ProductEdit extends Activity {
 	private static final String LOGTAG = "SimpleCarboCalcEdit";
 
 	private Button mConfirm;
 	private Button mCancel;
 	private Cursor mLangCursor;
 	private Spinner mLang;
 	private EditText mProc;
 	private EditText[] mNamesEdit = null;
 	private String[] mLangListLong = null;
 	private String[] mLangListShort = null;
 	private String[] mNames = null;
 	private String mMainLang = null;
 	private TextView mLastErr;
 	private int mMainLangIdx = -1;
 	private long mId = -1;
 
 	public static final String EDIT_PROC = "proc_edit";
 	public static final String EDIT_ID = "id_edit";
 	public static final String EDIT_LANGS_SHORT = "shortlang_edit";
 	public static final String EDIT_LANGS_LONG = "longlang_edit";
 	public static final String EDIT_NAMES = "name_edit";
 	public static final String EDIT_MAIN_LANG = "mainlang_edit";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.product_edit);
 
 		Bundle extras = getIntent().getExtras();
 		mId = extras.getLong(EDIT_ID);
 		String procStr = extras.getString(EDIT_PROC);
 		String names[] = extras.getStringArray(EDIT_NAMES);
 		mLangListShort = extras.getStringArray(EDIT_LANGS_SHORT);
 		mLangListLong = extras.getStringArray(EDIT_LANGS_LONG);
 		mNames = extras.getStringArray(EDIT_NAMES);
 		mMainLang = extras.getString(EDIT_MAIN_LANG);
 
 		setTitle((mId > 0 ? R.string.edit_name : R.string.add_name));
 
 		mProc = (EditText) findViewById(R.id.proc);
 		mConfirm = (Button) findViewById(R.id.confirm);
 		mCancel = (Button) findViewById(R.id.cancel);
 		mLastErr = (TextView) findViewById(R.id.lastError);
 		mProc.setText(procStr);
 		mProc.addTextChangedListener(mTextWatcherValidate);
 
 		TableRow rowExample = (TableRow) findViewById(R.id.tableRow3);
 		TableLayout table = (TableLayout) findViewById(R.id.tableLayout1);
 
 		TableRow.LayoutParams prm = new TableRow.LayoutParams(0,
 				ViewGroup.LayoutParams.WRAP_CONTENT, 1);
 		mNamesEdit = new EditText[mLangListShort.length];
 		for (int i = 0; i < mLangListShort.length; i++) {
 			Log.v(LOGTAG, "lang: '" + mLangListShort[i] + "'");
 			if (mMainLang.equals(mLangListShort[i])) {
 				mMainLangIdx = i;
 				TextView text = (TextView) findViewById(R.id.mainLangText);
 				text.setText(mLangListLong[i]);
 				mNamesEdit[i] = (EditText) findViewById(R.id.mainLangName);
 			} else {
 				TableRow row = new TableRow(getBaseContext());
 				TextView text = new TextView(getBaseContext());
 				text.setText(mLangListLong[i]);
 				text.setLayoutParams(prm);
 				row.addView(text);
 				mNamesEdit[i] = new EditText(getBaseContext());
 				mNamesEdit[i].setLayoutParams(prm);
 				row.addView(mNamesEdit[i]);
 				table.addView(row);
 			}
 			if (mNames[i] != null)
 				mNamesEdit[i].setText(mNames[i]);
 		}
 		if (mMainLangIdx == -1)
 			Log.e(LOGTAG, "Main language '" + mMainLang + "' is not found in "
 					+ mLangListShort);
 		else
 			mNamesEdit[mMainLangIdx]
 					.addTextChangedListener(mTextWatcherValidate);
 
 		mCancel.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				setResult(RESULT_CANCELED);
 				finish();
 			}
 
 		});
 
 		mConfirm.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View view) {
 
 				int count = 0;
 				for (int i = 0; i < mLangListShort.length; i++) {
 					mNames[i] = mNamesEdit[i].getText().toString();
 					// replace special symbols we use (if any)
 					mNames[i]= mNames[i].replace('~', '-');
 					mNames[i]= mNames[i].replace('|', '/');
 					if (mNames[i] != null && mNames[i].length() > 0)
 						count++;
 				}
 				assertTrue(count > 0);
 
 				Double proc= TextAndDifitsUtils.getProcent(mProc);
 				proc*= 100; //get percent
 				String error = ProdList.getInstance().editProduct(mId,
 						proc, mLangListShort, mNames);
 
 				if (error == null || error.length() == 0) {
 					Bundle bundle = new Bundle();
 					bundle.putLong(EDIT_ID, mId);
 					Intent intent = new Intent();
 					intent.putExtras(bundle);
 					Log.v(LOGTAG, "result: " + bundle.toString());
 					setResult(RESULT_OK, intent);
 					finish();
 				} else {
 					mLastErr.setText(error);
 				}
 			}
 		});
 
 		validationCheck();
 	}
 
 	private TextWatcher mTextWatcherValidate = new TextWatcher() {
 		public void afterTextChanged(Editable s) {
 			validationCheck();
 		}
 
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 		}
 
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count) {
 		}
 	};
 
 	/**
 	 * Simple and fast validation
 	 */
 	void validationCheck() {
 		Double proc = TextAndDifitsUtils.getProcent(mProc);
		String name = ((mMainLangIdx > 0) ? mNamesEdit[mMainLangIdx].getText().toString() : null);
 		mConfirm.setEnabled(mMainLangIdx != -1 && !proc.isNaN() && name != null
 				&& name.length() != 0);
 	}
 
 }
