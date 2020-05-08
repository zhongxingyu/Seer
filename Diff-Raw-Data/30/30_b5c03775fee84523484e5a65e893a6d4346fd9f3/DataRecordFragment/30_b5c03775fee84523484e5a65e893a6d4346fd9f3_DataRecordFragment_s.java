 package com.megginson.sloop.app;
 
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.megginson.sloop.model.DataEntry;
 import com.megginson.sloop.model.DataRecord;
 
 /**
  * A view fragment wrapping a data record
  * 
  * @author David Megginson
  * @see DataRecord
  * @see DataCollectionPagerAdapter
  */
 public class DataRecordFragment extends Fragment {
 
 	public DataRecordFragment() {
 		super();
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		LinearLayout layout = new LinearLayout(getActivity());
 		layout.setOrientation(LinearLayout.VERTICAL);
 		
 		int position = getArguments().getInt("position");
 		DataRecord dataRecord = MainActivity.dataCollection.get(position);
 
 		for (DataEntry entry : dataRecord) {
 			addLabel(layout, entry.getKey());
 			addValue(layout, entry.getValue());	
 		}
 		
 		ScrollView scrollView = new ScrollView(getActivity());
 		scrollView.addView(layout);
 		
 		return scrollView;
 	}
 	
 	private TextView addLabel (ViewGroup container, String text) {
 		TextView textView = new TextView(getActivity());
 		textView.setText(text);
 		textView.setTypeface(null, Typeface.BOLD);
 		container.addView(textView);
 		return textView;
 	}
 
 	private TextView addValue (ViewGroup container, String text) {
 		TextView textView = new TextView(getActivity());
 		textView.setText(text);
 		container.addView(textView);
 		return textView;
 	}
 }
 
