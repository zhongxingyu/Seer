 package com.johndaniel.glosar;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.johndaniel.glosar.R;
 
 public class OverviewFragmentListAdapter extends ArrayAdapter<String> {
 
 	private final Context context;
 	private final String[] values;
  
 	public OverviewFragmentListAdapter(Context context, String[] values) {
 		super(context, R.layout.overview_fragment_list_style, values);
 		this.context = context;
 		this.values = values;
 	}
  
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater) context
 			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  
 		View rowView = inflater.inflate(R.layout.overview_fragment_list_style, parent, false);
 		TextView textViewWord = (TextView) rowView.findViewById(R.id.overview_fragment_list_word);
 		TextView textViewTranslations = (TextView) rowView.findViewById(R.id.overview_fragment_list_translations);
 		String[] twoStrings = values[position].split("=");
		if (twoStrings.length > 1){
			if (twoStrings[0].length() == 0){
				twoStrings[0] = "";
			}
			if (twoStrings[1].length() == 0) {
				twoStrings[1] = "";
			}
			textViewWord.setText(twoStrings[0]);
			textViewTranslations.setText(twoStrings[1]);
		} else {
			String[] elseStrings = new String[2];
			elseStrings[0] = values[position].replace("=", "");
			elseStrings[1] = "";
			textViewWord.setText(elseStrings[0]);
			textViewTranslations.setText(elseStrings[1]);
		}
		/*if(twoStrings[0].length() == 1){
 			twoStrings[0] = "";
 		}
 		if(twoStrings[1].length() == 0){
 			twoStrings[1] = "";
		}*/
   
 		return rowView;
 	}
 }
