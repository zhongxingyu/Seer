 package com.example.marquee;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Gallery;
 import android.widget.TextView;
 
 public class MarqueeExampleActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.main);
 
 	// this TextView will marquee because it is selected
 	TextView marqueeText1 = (TextView) findViewById(R.id.marquee_text_1);
 	marqueeText1.setSelected(true);
 
 	// this one is also selected
 	TextView marqueeText2 = (TextView) findViewById(R.id.marquee_text_2);
 	marqueeText2.setSelected(true);
 
	// this one is not selected; it is not automatically marquee
 	TextView marqueeText3 = (TextView) findViewById(R.id.marquee_text_3);
 	// marqueeText3.setSelected(true);
 
 	Gallery gallery = (Gallery) findViewById(R.id.gallery);
 	BaseAdapter adapter = new BaseAdapter() {
 
 	    private String[] text = new String[] { "1. Nunc sit amet pellentesque arcu.",
 		    "2. Morbi risus dolor, lacinia sed ultrices ac,", "3. malesuada nec risus. In euismod faucibus nibh.",
 		    "4. Nulla nunc justo, feugiat eu posuere eu," };
 
 	    @Override
 	    public View getView(int position, View convertView, ViewGroup parent) {
 		// ignore applying ViewHolder optimisation for this example
 		
 		LayoutInflater inflater = LayoutInflater.from(MarqueeExampleActivity.this);
 		View v = inflater.inflate(R.layout.gallery_element, parent, false);
 		
 		String s = getItem(position);
 
 		TextView textView = (TextView) v.findViewById(R.id.text);
 		textView.setSelected(true);
 		textView.setText(s);
 		
 		return v;
 	    }
 
 	    @Override
 	    public long getItemId(int position) {
 		return position;
 	    }
 
 	    @Override
 	    public String getItem(int position) {
 		return text[position];
 	    }
 
 	    @Override
 	    public int getCount() {
 		return text.length;
 	    }
 	};
 	gallery.setAdapter(adapter);
     }
 }
