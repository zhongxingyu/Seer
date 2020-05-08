 package com.gomotion;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.preference.DialogPreference;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 
 /**
  * Dialog shown when the user whishes to select
  * default indoor exercise settings (sets, reps and rest time)
  * 
  * @author Jack Hindmarch
  *
  */
 public class DefaultBodyWeightPreference extends DialogPreference
 {
 	private String values;
	private final String DEFAULT_VALUE = "5,10,60";
 	private Spinner sets;
 	private Spinner reps;
 	private Spinner rest;
 	
 
 	public DefaultBodyWeightPreference(Context context, AttributeSet attrs)
     {
         super(context, attrs);
         
         setDialogLayoutResource(R.layout.dialog_body_weight_settings);
         setPositiveButtonText(android.R.string.ok);
         setNegativeButtonText(android.R.string.cancel);
                 
         setDialogIcon(null);
     }
 	
 	public String getValues()
 	{
 		return values;
 	}
 	
 	@Override
 	protected void onBindDialogView(View view)
     {
     	sets = (Spinner) view.findViewById(R.id.set_spinner);    	
     	reps = (Spinner) view.findViewById(R.id.rep_spinner);
     	rest = (Spinner) view.findViewById(R.id.rest_spinner);
 		
 		/** Add numbers to set chooser **/
 		List<String> setNums = new LinkedList<String>();
 		for(int i = 1; i <= 50; i++)
 		{
 			setNums.add(String.valueOf(i));
 		}
 		
 		ArrayAdapter<String> setList = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, setNums);
 		setList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		
 		/** Add numbers to rep chooser **/
 		List<String> repNums = new LinkedList<String>();
 		for(int i = 1; i <= 200; i++)
 		{
 			repNums.add(String.valueOf(i));
 		}
 		
 		ArrayAdapter<String> repList = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, repNums);
 		repList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		
 		/** Add numbers to rest chooser **/
 		List<String> restNums = new LinkedList<String>();
		for(int i = 1; i <= 300; i++)
 		{
 			restNums.add(String.valueOf(i));
 		}
 		
 		ArrayAdapter<String> restList = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, restNums);
 		restList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		
 		sets.setAdapter(setList);
 		reps.setAdapter(repList);
 		rest.setAdapter(restList);
 
 		String[] str = values.split(",");
 		
     	sets.setSelection(Integer.valueOf( str[0] ) - 1);
     	reps.setSelection(Integer.valueOf( str[1] ) - 1);
     	rest.setSelection(Integer.valueOf( str[2] ) - 1);
 
     	
 		super.onBindDialogView(view);
 	}
 
 	@Override
     protected Object onGetDefaultValue(TypedArray a, int index)
     {
         return a.getString(index);
     }
     
     @Override
     protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
     {
         if (restorePersistedValue) {
             // Restore existing state
             values = this.getPersistedString(DEFAULT_VALUE);
 
         } else {
             // Set default state from the XML attribute
             values = (String) defaultValue;
             persistString(values);
         }
     }    
 
     @Override
     protected void onDialogClosed(boolean positiveResult)
     {
         // When the user selects "OK", persist the new value
         if (positiveResult)
         {
         	values = String.format("%s,%s,%s", sets.getSelectedItem().toString(), reps.getSelectedItem().toString(), rest.getSelectedItem().toString());        	
             persistString(values);
         }
     }
 }
