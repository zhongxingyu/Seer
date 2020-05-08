 package se3350y.aleph.firealertscanner;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Spinner;
 
 public class FireHoseCabinetGoodPoorElement extends inspectionElement {
 	
 	private Spinner goodPoor;
 	
 	public FireHoseCabinetGoodPoorElement(Context context){
 		goodPoor = new Spinner(context);
		goodPoor.setSelection(0);
 		
 	}
 	
 	public void setGoodPoor(int i) {
 		// TODO Auto-generated method stub
 		goodPoor.setSelection(i);
 		
 	}
 
 	public int getGoodPoor() {
 		// TODO Auto-generated method stub
 		return goodPoor.getSelectedItemPosition();
 	}
 	
 	//Holds the view for the RadioButtons
 		static class goodPoorViewHolder {
 	        protected Spinner VH_spinner;
 	      }
 	
 	//Creates the row on the page
 	public View XMLInflator(View convertView, ViewGroup parent, Object context){
 		
 		View view = null;
 		
 		if (convertView == null || convertView.getTag() != this.getTag()) {
 			LayoutInflater infalInflater = (LayoutInflater) context;
 			view = infalInflater.inflate(R.layout.firehosecabinet_goodpoor_item, null);
 			
 			
 			final goodPoorViewHolder viewHolder = new goodPoorViewHolder();
 			
 			
 			viewHolder.VH_spinner = (Spinner) view.findViewById(R.id.goodPoorSpinner);
 			
 			
 			viewHolder.VH_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 				
 
 				@Override
 				public void onItemSelected(AdapterView<?> arg0, View arg1,
 						int posSelected, long arg3) {
 					// TODO Auto-generated method stub
 					
 					FireHoseCabinetGoodPoorElement element = (FireHoseCabinetGoodPoorElement) viewHolder.VH_spinner.getTag();
 					
 					element.setGoodPoor(posSelected);
 					
					setCompleted(true);
 					
 				}
 				
 
 				@Override
 				public void onNothingSelected(AdapterView<?> arg0) {
 					// TODO Auto-generated method stub
 				}
             });
 			
 			viewHolder.VH_spinner.setOnTouchListener(new OnTouchListener(){
 
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					// TODO Auto-generated method stub
 					OnElementChangeMade();
 					
 					return false;
 				}});
 			
 			view.setTag(viewHolder);
 		    viewHolder.VH_spinner.setTag(this);
 			
 		}
 		else{
 			view = convertView;
 			((goodPoorViewHolder) view.getTag()).VH_spinner.setTag(this);
 		}
 		
 		goodPoorViewHolder holder = (goodPoorViewHolder) view.getTag();
 		
 		holder.VH_spinner.setSelection(this.getGoodPoor());
 		
 		
 		return view;
 			
 	}
 
 }
