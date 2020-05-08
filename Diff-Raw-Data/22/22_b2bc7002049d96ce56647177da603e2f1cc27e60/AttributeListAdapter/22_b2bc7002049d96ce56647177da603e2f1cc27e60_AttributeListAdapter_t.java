 /**
  * 
  */
 package net.mindsoup.pathfindercharactersheet.adapters;
 
 import java.util.ArrayList;
 
 import net.mindsoup.pathfindercharactersheet.CharacterActivity;
 import net.mindsoup.pathfindercharactersheet.R;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.internal.widget.IcsAdapterView;
 import com.actionbarsherlock.internal.widget.IcsAdapterView.OnItemSelectedListener;
 import com.actionbarsherlock.internal.widget.IcsSpinner;
 
 /**
  * @author Valentijn
  *
  */
 public class AttributeListAdapter extends ArrayAdapter<CharacterAttributeAdapter> {
 	
 	private final String[] attributeValues = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"};
 	
 	private ArrayList<CharacterAttributeAdapter> attributes;
 	private int viewResourceId;
 	private SherlockFragmentActivity activity;
 
 	public AttributeListAdapter(Context context, int textViewResourceId, ArrayList<CharacterAttributeAdapter> objects, SherlockFragmentActivity activity) {
 		super(context, textViewResourceId, objects);
 		this.attributes = objects;
 		this.viewResourceId = textViewResourceId;
 		this.activity = activity;
 	}
 	
 	@Override
     public View getView(final int attributePosition, View convertView, ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         convertView = inflater.inflate(viewResourceId, null);
         
         TextView tv = (TextView)convertView.findViewById(R.id.attribute_name);
         tv.setText(attributes.get(attributePosition).getAttribute().toString());
 
         tv = (TextView)convertView.findViewById(R.id.attribute_bonus);
         tv.setText(Integer.toString(attributes.get(attributePosition).getBonus()));
         
         tv = (TextView)convertView.findViewById(R.id.attribute_temp_bonus);
         tv.setText(Integer.toString(attributes.get(attributePosition).getTempBonus()));
                
         IcsSpinner spinner = (IcsSpinner)convertView.findViewById(R.id.edit_attribute);
         spinner.setAdapter(new ArrayAdapter<String>(this.getContext(),R.layout.attribute_spinner, attributeValues));
         spinner.setSelection(attributes.get(attributePosition).getValue());
         spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(IcsAdapterView<?> parent, View view, int position, long id) {
 				CharacterAttributeAdapter caa = attributes.get(attributePosition);
 				
 				// if we have changed the value of our attribute
 				if(caa.getValue() != position) {
 					caa.setAttribute(position);
 					((CharacterActivity)getActivity()).updateCharacter();
 				}
 			}
 
 			@Override
 			public void onNothingSelected(IcsAdapterView<?> parent) {}
 		});
         
         
         spinner = (IcsSpinner)convertView.findViewById(R.id.edit_temp_attribute);
         spinner.setAdapter(new ArrayAdapter<String>(this.getContext(),R.layout.attribute_spinner_blue, attributeValues));
        spinner.setSelection(attributes.get(attributePosition).getTotalValue());
         spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(IcsAdapterView<?> parent, View view, int position, long id) {
 				CharacterAttributeAdapter caa = attributes.get(attributePosition);
 				
 				// if we have changed the value of our attribute
				if(caa.getTotalValue() != position) {
					caa.setTempValue(position - caa.getTotalValue() + caa.getTempValue());
 					((CharacterActivity)getActivity()).updateCharacter(false);
 				}
 			}
 
 			@Override
 			public void onNothingSelected(IcsAdapterView<?> parent) {}
 		});
         return convertView;
 	}
 	
 	public SherlockFragmentActivity getActivity() {
 		return this.activity;
 	}
 
 }
