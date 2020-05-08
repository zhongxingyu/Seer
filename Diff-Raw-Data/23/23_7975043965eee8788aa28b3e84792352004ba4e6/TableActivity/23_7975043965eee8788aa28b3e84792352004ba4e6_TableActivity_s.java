 package com.brosser;
 
 import android.app.Activity;
 import android.view.View.OnClickListener;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import com.brosser.model.ElementTable;
 
 public class TableActivity extends Activity implements OnClickListener {
 	
 	private Button[][] buttons = new Button[18][7];
     
 	/** Called when the activity is first created. */
     @Override 
     public void onCreate(Bundle state) {
         super.onCreate(state);
         
         // Install the view
         setContentView(R.layout.table);
         
         // Populate the button matrix
         // NB! Button names are 1 ... 18
     	for(int i=0; i<ElementTable.getGroups(); i++) {
     		for(int j=0; j<ElementTable.getPeriods(); j++) {
     			String buttonID = "btn" + (i+1) + "a" + (j+1);
     			int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
 
     			if(resID != 0) {
 	    			buttons[i][j] = ((Button) findViewById(resID));
 	    			buttons[i][j].setOnClickListener(this);
     			}
     		}
     	}
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	// Fix screen orientation to landscape mode
     	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
     }
 
     /** Callback function for the buttons */
 	@Override
 	public void onClick(View src)  {
 
 		for(int i=0; i<ElementTable.getGroups(); i++) {
 			for(int j=0; j<ElementTable.getPeriods(); j++) {
 				if(src == buttons[i][j]) {
 					ElementTable.setActiveElement(ElementTable.getElement(i, j));
 				}
 			}
 		}
 		
 		// Show element view for the selected element
 		TabHostActivity tabHost = (TabHostActivity) this.getParent();
 		tabHost.switchTab(1);
 	}
 }
