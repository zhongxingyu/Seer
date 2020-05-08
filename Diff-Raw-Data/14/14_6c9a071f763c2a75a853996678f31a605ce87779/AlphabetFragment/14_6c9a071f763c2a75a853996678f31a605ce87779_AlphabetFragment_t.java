 package com.lacike.ciphertools;
 
 import android.content.Context;
 import android.graphics.Color;
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
 
 public class AlphabetFragment extends Fragment {
 
 	public static AlphabetFragment newInstance() {
 		return new AlphabetFragment();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.alphabet, container, false);
 
 		Context context = getActivity();
 
 		String[] phonetic_alphabet = getResources().getStringArray(
 				R.array.phonetic_alphabet);
 
 		LinearLayout alphabetList = (LinearLayout) view
 				.findViewById(R.id.alphabet_list);
 
 		int paddingDp = 4;
 		int paddingPx = (int) TypedValue.applyDimension(
 				TypedValue.COMPLEX_UNIT_DIP, paddingDp, getResources()
 						.getDisplayMetrics());
		int flagSize = (int) getResources().getDimension(R.dimen.flag_size);
 		LayoutParams flagParams = new LayoutParams(flagSize, flagSize);
 		LayoutParams brailleParams = new LayoutParams(
 				LayoutParams.WRAP_CONTENT, flagSize);
 
 		for (char ch = 'a'; ch <= 'z'; ch++) {
 			int i = ch - 'a';
 			LinearLayout linearLayout = new LinearLayout(context);
 			linearLayout.setLayoutParams(new LayoutParams(
 					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
 			linearLayout.setPadding(0, paddingPx, 2 * paddingPx, paddingPx);
 
 			TextView number = new TextView(context);
 			number.setLayoutParams(new LayoutParams(0,
 					LayoutParams.MATCH_PARENT, 1));
 			number.setTextAppearance(context,
 					android.R.style.TextAppearance_Large);
 			number.setGravity(Gravity.CENTER);
 			// character.setPadding(paddingPx, 0, paddingPx,0);
 			number.setText(String.valueOf(i + 1));
 
 			TextView character = new TextView(context);
 			character.setLayoutParams(new LayoutParams(0,
 					LayoutParams.MATCH_PARENT, 1));
 			character.setTextAppearance(context,
 					android.R.style.TextAppearance_Large);
 			character.setGravity(Gravity.CENTER);
 			// character.setPadding(paddingPx, 0, paddingPx,0);
 			character.setText(String.valueOf((char) ('A' + i)));
 
 			TextView phonetic = new TextView(context);
 			phonetic.setLayoutParams(new LayoutParams(0,
 					LayoutParams.MATCH_PARENT, 2));
 			phonetic.setTextAppearance(context,
 					android.R.style.TextAppearance_Small);
 			phonetic.setGravity(Gravity.CENTER_VERTICAL);
 			// character.setPadding(paddingPx, 0, paddingPx,0);
 			phonetic.setText(phonetic_alphabet[i]);
 
 			TextView morse = new TextView(context);
 			morse.setLayoutParams(new LayoutParams(0,
 					LayoutParams.MATCH_PARENT, 2));
 			morse.setTextAppearance(context,
 					android.R.style.TextAppearance_Large);
 			morse.setGravity(Gravity.CENTER);
 			morse.setText(Text2MorseFragment.morseCode[i]);
 
 			ImageView braille = new ImageView(context);
 			braille.setLayoutParams(brailleParams);
 			braille.setPadding(paddingPx, 0, paddingPx, 0);
 			String brailleStrId = "braille_" + ch;
 			braille.setContentDescription(brailleStrId);
 			int brailleId = getResources().getIdentifier(brailleStrId,
 					"drawable", "com.lacike.ciphertools");
 			braille.setImageResource(brailleId);
 
 			ImageView flag = new ImageView(context);
 			flag.setLayoutParams(flagParams);
 			String flagStrId = "flag_" + ch;
 			flag.setContentDescription(flagStrId);
 			int flagId = getResources().getIdentifier(flagStrId, "drawable",
 					"com.lacike.ciphertools");
 			flag.setImageResource(flagId);
 
 			linearLayout.addView(number);
 			linearLayout.addView(character);
 			linearLayout.addView(phonetic);
 			linearLayout.addView(morse);
 			linearLayout.addView(braille);
 			linearLayout.addView(flag);
 
 			alphabetList.addView(linearLayout);
 
 			TextView separator = new TextView(context);
 			separator.setLayoutParams(new LayoutParams(
 					LayoutParams.MATCH_PARENT, 1));
 			separator.setBackgroundColor(Color.DKGRAY);
 			
 			alphabetList.addView(separator);
 		}
 
 		return view;
 	}
 }
