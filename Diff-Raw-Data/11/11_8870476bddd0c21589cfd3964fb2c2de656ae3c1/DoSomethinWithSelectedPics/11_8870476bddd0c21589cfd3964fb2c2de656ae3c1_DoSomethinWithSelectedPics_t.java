 package com.annuletconsulting.gallerytest;
 
 import com.annuletconsulting.gallery.PicTabsFragment;
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class DoSomethinWithSelectedPics extends Activity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_do_something);
		((TextView) findViewById(R.id.output)).setText(arrayToString(getIntent().getStringArrayExtra(PicTabsFragment.PIC_PATHS_ARRAY)));
	}

	private String arrayToString(String[] stringArray) {
		StringBuffer out = new StringBuffer();
		for (String string : stringArray) {
			out.append(string);
			out.append("\n");
		}
		return out.toString();
 	}
 }
