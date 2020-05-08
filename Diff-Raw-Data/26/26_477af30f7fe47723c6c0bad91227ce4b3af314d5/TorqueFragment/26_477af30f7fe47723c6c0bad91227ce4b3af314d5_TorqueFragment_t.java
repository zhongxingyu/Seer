 package com.atasoft.flangeassist.fragments;
 
 import android.os.*;
 import android.support.v4.app.*;
 import android.view.*;
 import android.view.View.*;
 import android.widget.*;
 import com.atasoft.flangeassist.*;
 
 public class TorqueFragment extends Fragment implements OnClickListener
 {
     View thisFrag;
 	public static final int[] EIGHT_BASE = {1,5,3,7,2,6,4,8};
 	public static final int[] FOUR_BASE = {1,3,2,4};
	public static final String[] PAT_STRINGS = {"4-point", "8-point", "8-point alternate"};
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
 							 Bundle savedInstanceState) {
     	
         View v = inflater.inflate(R.layout.torquepat, container, false);
         thisFrag = v;
         setupSpinners();
         
         Button b = (Button) v.findViewById(R.id.submit);
         b.setOnClickListener(this);
         
         return v;
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
 			case R.id.submit:
 				submit();
 				break;
         }
     }
 	
 		
 	private void setupSpinners() {
 		Spinner patSpin = (Spinner) thisFrag.findViewById(R.id.patSpin);
 		ArrayAdapter<String> patAd = new ArrayAdapter<String>(getActivity(),
 					android.R.layout.simple_spinner_item, PAT_STRINGS);
 		patSpin.setAdapter(patAd);
 	}
 	
 	private void submit() {
 		Spinner patSpin = (Spinner) thisFrag.findViewById(R.id.patSpin);
 		int selectedPos = patSpin.getSelectedItemPosition();
 		int[] patBase = selectedPos > 0 ? EIGHT_BASE : FOUR_BASE;
 		boolean isReverse = selectedPos == 2 ? true : false;
 		
 		String torqueString = genTorquePattern(patBase, isReverse);
 		
 		TextView patReturn = (TextView) thisFrag.findViewById(R.id.torqueReturn);
 		patReturn.setText(torqueString);
 	}
 	
 	private String genTorquePattern(int[] patBase, boolean isReverse) {
 		String errInt = getString(R.string.errInt);
 		String errEven = getString(R.string.errEven);
 		String retString = "";
 		EditText studBox = (EditText) thisFrag.findViewById(R.id.studBox);
 		int studInt = parseInputInt(studBox);
 		
 		if (studInt < 8 || studInt > 200) return errInt;
 		int rem = studInt % 4;
 		if (rem > 0) return errEven;
 		
 		if(!isReverse){
 			for(int i = 0; i < patBase.length; i++) { //Muchos elegante
 				for(int j = 0; j + patBase[i] <= studInt; j = j + patBase[patBase.length - 1]) {
 					retString = retString + ", " + Integer.toString(patBase[i] + j);
 				}
 			}
		} else {  //Reverso mode
			//if 8 divisible, odd base offset is 8, else 4
			int startSub = studInt % 8 > 0 ? 4 : 8;  
 			for(int i = 0; i < patBase.length; i++){
				if(patBase[i] <= 4) {  //1 through 4 count from top 
 					retString += ", " + patBase[i];
					for(int j = studInt - startSub + patBase[i]; j > patBase[i]; j-=8){
						retString += ", " + Integer.toString(j);
 					}
				} else {  //5 through 8 are normal 8 pt
					for(int j = patBase[i]; j <= studInt; j+=8) {
						retString += ", " + Integer.toString(j);
 					}
 				}
 			}
 		}
 		return retString.substring(2);
 	}
 	
 	private int parseInputInt(EditText editText) { 
 		try {
 			return Integer.parseInt(editText.getText().toString());
 		} catch (NumberFormatException e) {
 			return 0;
 		}
 	}
 }
