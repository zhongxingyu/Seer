 package com.brosser;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.SimpleAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.net.Uri;
 
 import com.brosser.model.Element;
 import com.brosser.model.ElementTable;
 
 public class ElementActivity extends Activity {
 	
 	private ImageButton star;
 	private ImageButton atom;
 	private ImageButton wiki;
 	private Spinner spinner;
 	
     /** Called when the activity is first created. */
     @Override 
     public void onCreate(Bundle state) {
     	super.onCreate(state);
        
         // Install the view
         setContentView(R.layout.element);
         
         // Load images
         star = ((ImageButton)findViewById(R.id.IB_star));
         	star.setImageResource(R.drawable.star_on);
         atom = ((ImageButton)findViewById(R.id.IB_atom));
         	atom.setImageResource(R.drawable.atom_small);
         wiki = ((ImageButton)findViewById(R.id.IB_wiki));
         	wiki.setImageResource(R.drawable.wiki);
         
         // Create the spinner used for selecting element
         createSpinner();	
         	
         // Reload text for currently selected element 	
         reloadText();
      
         // React to events from the buttons
         wiki.setOnClickListener(new ImageButton.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				String url = ElementTable.getActiveElement().getWikiURL();
 				Intent wikiIntent = new Intent(Intent.ACTION_VIEW);
 				wikiIntent.setData(Uri.parse(url));
 				startActivity(wikiIntent);				
 			}
         });
         
         star.setOnClickListener(new ImageButton.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				// Toggle favourite
 				ElementTable.getActiveElement().setStarred(ElementTable.getActiveElement().isStarred() ? false : true);
 				reloadText();
 				createSpinner();
 			}
         });
         
         // Set up a callback for the spinner (selecting element)
         spinner.setOnItemSelectedListener(
         	new OnItemSelectedListener() {
 				@Override
 				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 					if(position != ElementTable.getActiveElement().getNumber()-1) {
 						ElementTable.setActiveElement(ElementTable.getElement(position));
 						reloadText();
 					}
 				}
 				@Override
 				public void onNothingSelected(AdapterView<?> arg0) {
 					// TODO Auto-generated method stub
 				}
 			});
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	// Screen orientation according to sensor
     	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
     }
     
     public void reload() {
         onStop();
         onCreate(getIntent().getExtras());
     }
     
     public void reloadText() {
     	Element active = ElementTable.getActiveElement();
         String text = active.getInfoAsString();
         
         if(active.isStarred()) {
         	((ImageButton)findViewById(R.id.starred)).setBackgroundResource(R.drawable.star_on);
         }
         else {
         	((ImageButton)findViewById(R.id.starred)).setBackgroundResource(R.drawable.transparent);
         }
         
         ((TextView)findViewById(R.id.info)).setText(text);
         ((TextView)findViewById(R.id.header)).setText(active.getName());
     }
     
     private void createSpinner() {
 
     	spinner = ((Spinner)findViewById(R.id.spinner));
     	
         ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
         HashMap<String, Object> map = new HashMap<String, Object>();
         
         // Populate spinner item list
        for(int i=0; i<90; i++) { // 18*7
         	Element element = ElementTable.getElement(i);
         	map = new HashMap<String, Object>();
             map.put("Name", element.getNumber() + " " + element.getSymbol() + " " + element.getName());
             map.put("Icon", (element.isStarred() ? "On" : "Off"));
             list.add(map);
         }        
 
         ElementSpinnerAdapter aspnElements = new ElementSpinnerAdapter(this, list,
                 R.layout.spinner, new String[] { "Name", "Icon" },
                 new int[] { R.id.spinnerrow, R.id.spinnerrowicon });
         
         
         spinner.setAdapter(aspnElements);
         
         int activeNumber = ElementTable.getActiveElement().getNumber();
         spinner.setSelection((activeNumber - 1 - (activeNumber > 57 ? 14 : 0)
         		- (activeNumber > 89 ? 14 : 0)), true);
     }
 
     /** 
      * Private class extending adapter, used for the spinner items
      * Essentially like the normal adapter, only with an added image view
      * next to the text view.
      */
 	    private class ElementSpinnerAdapter extends SimpleAdapter {
 	    	
 	        public ElementSpinnerAdapter(Context context, List<? extends Map<String, ?>> data,
 	                int resource, String[] from, int[] to) {
 	            	super(context, data, resource, from, to);
 	        }
 	        
 	        public View getView(int position, View convertView, ViewGroup parent) {
 
 	        	Context context = getApplicationContext();
 	        	
 	            TextView text = new TextView(context);
 	            ImageView icon = new ImageView(context);
 	            
 	            // unsafe cast!
 	            @SuppressWarnings("unchecked")
 				HashMap<String, Object> data = (HashMap<String, Object>) getItem(position);
 
 	            text.setText((String)data.get("Name"));
 	            text.setTextColor(Color.BLACK);
 	            text.setWidth(150);
 	            text.setTextSize(12);
 	            String iconOn = (String)data.get("Icon");
 	            
 	            if(iconOn == "On") {
 	            	icon.setBackgroundResource(R.drawable.star_on_small);
 	            } 
 	            else {
 	            	icon.setBackgroundResource(R.drawable.transparent_small);
 	            }
 	            icon.setMaxWidth(20);
 	            
 	            LinearLayout layout = new LinearLayout(context);
 	            
 	            layout.setOrientation(LinearLayout.HORIZONTAL);
 	            layout.setGravity(Gravity.LEFT);
 	            
 	            layout.addView(text);
 	            layout.addView(icon);
 	            
 	            return layout;
 	        }
 	        
 	        public View getDropDownView(int position, View convertView, 
 	                ViewGroup parent) { 
 	        	return getView(position, convertView, parent);
 	        }
 	        
 	    }
 
 }
