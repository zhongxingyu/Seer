 package com.dmtprogramming.pathfindercombat;
 
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.widget.EditText;
 
 //text watcher for the various text fields on the page
 class CustomTextWatcher implements TextWatcher {
 	private static final String TAG = "PFCombat";
 	private EditText _text;
 	private String _field;
 	private FragmentBase _fragment;
 	
 	public CustomTextWatcher(EditText e, String field, FragmentBase a) {
 		_text = e;
 		_field = field;
 		_fragment = a;
 	}
 	
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
  public void onTextChanged(CharSequence s, int start, int before, int count) {}
 
  public void afterTextChanged(Editable s) {
  Log.d(TAG, "CustomTextWatcher: Entered!");
 	if (_text == null) {
 		return;
 	}
 	String txt = _text.getText().toString(); 
  	if (txt == null || txt.equals("")) {
  		return;
  	}
  	if (_fragment != null && _fragment.getCharacter() != null) {
 	 	if (_fragment.getCharacter().setData(_field, _text.getText().toString())) {
 			Log.d(TAG, "CustomTextWatcher: " + _field + " updated successfully, updating the sheet");
		  _fragment.updateCharacter(_field);
 	 	}
  	}
  }
 }
