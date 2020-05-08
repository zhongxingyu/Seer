 package com.gingbear.githubtest;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.ApplicationInfo;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class CustomActivity extends Activity  {
 	public static void test(){
 		
 	}
 	/**
 	 * xmlで設定されたButtonにTextとClickをセット
 	 * @param buttonId
 	 * @param text
 	 * @param onClickListener
 	 */
 	private void setButton(int buttonId, String text,  OnClickListener onClickListener){
         Button btn = (Button)findViewById(buttonId);
         btn.setText(text);
         btn.setOnClickListener(onClickListener);
 	}
 	/**
 	 * textを取得するときに，プレファレンスに保存しておく
 	 * @param editTextId
 	 * @param key
 	 * @return
 	 */
 	private String getEditText(int editTextId, String key){
 		EditText editText = (EditText)findViewById(editTextId);
 		String text =  editText.getText().toString();
 		SharedPreferences pref = getSharedPreferences("pref",MODE_WORLD_READABLE|MODE_WORLD_WRITEABLE);
 		Editor e = pref.edit();
 		e.putString(key, text);
 		e.commit();
 		return text;
 	}
 	/**
 	 * プレファレンスにkeyのデータがある場合はそれを使う。
 	 * 無い場合は，与えられた文字列を使う
 	 * @param editTextId
 	 * @param key
 	 * @param initText
 	 */
 	 void setEditText(int editTextId, String key, String initText){
 		SharedPreferences pref =getSharedPreferences("pref",MODE_WORLD_READABLE|MODE_WORLD_WRITEABLE);
 		EditText editText = (EditText)findViewById(editTextId);
		editText.setSelected(false);
 		editText.setText(pref.getString(key, initText));
 	}
 	/**
 	 * メタデータの取得
 	 * @param info
 	 * @param key
 	 */
 	private static String getMetaData(ApplicationInfo info, String key){
 		Object value = info.metaData.get(key);
 		if(value == null) return "";
 		return value.toString();
 	}
 }
