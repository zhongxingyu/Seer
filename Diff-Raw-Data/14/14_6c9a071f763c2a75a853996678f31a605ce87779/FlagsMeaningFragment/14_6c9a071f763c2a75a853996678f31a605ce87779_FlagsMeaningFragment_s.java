 package com.lacike.ciphertools;
 
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 public class FlagsMeaningFragment extends Fragment {
 
 	public static FlagsMeaningFragment newInstance() {
 		return new FlagsMeaningFragment();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.flags_meaning, container, false);
 
 		Context context = getActivity();
 
 		String[] meanings = getResources()
 				.getStringArray(R.array.flags_meaning);
 
 		LinearLayout flagList = (LinearLayout) view
 				.findViewById(R.id.flag_list);
 
 		int paddingDp = 4;
 		int paddingPx = (int) TypedValue.applyDimension(
 				TypedValue.COMPLEX_UNIT_DIP, paddingDp, getResources()
 						.getDisplayMetrics());
		int flagSize = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.flag_size), getResources()
				.getDisplayMetrics());
 		LayoutParams flagParams = new LayoutParams(flagSize, flagSize);
 
 		for (int i = 0; i < meanings.length; i++) {
 
 			LinearLayout linearLayout = new LinearLayout(context);
 			linearLayout.setLayoutParams(new LayoutParams(
 					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
 			linearLayout.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
 
 			ImageView flag = new ImageView(context);
 			flag.setLayoutParams(flagParams);
 			String flagStrId = "flag_"+(char)('a'+i);
 			flag.setContentDescription(flagStrId);
 			int flagId = getResources().getIdentifier(flagStrId, "drawable", "com.lacike.ciphertools");
 			flag.setImageResource(flagId);
 
 			TextView character = new TextView(context);
 			character.setLayoutParams(new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
 			character.setTextAppearance(context,
 					android.R.style.TextAppearance_Large);
 			character.setGravity(Gravity.CENTER_VERTICAL);
 			character.setPadding(paddingPx, 0, paddingPx,0);
 			character.setText(String.valueOf((char) ('A' + i)));
 			
 			TextView meaning = new TextView(context);
 			meaning.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
 					LayoutParams.MATCH_PARENT));
 			meaning.setGravity(Gravity.CENTER_VERTICAL);	
 			meaning.setText(meanings[i]);
 
 			linearLayout.addView(flag);
 			linearLayout.addView(character);
 			linearLayout.addView(meaning);
 
 			flagList.addView(linearLayout);
 		}
 
 		return view;
 	}
 }
