 package cs1635.group.booksharing;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 public class BuyActivity extends Activity {
 	Context thisContext = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		thisContext = this;
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_buy);
 
 		EditText searchField = (EditText) findViewById(R.id.search_field);
 		
 		// Get list layout ID
         final LinearLayout listLayout = (LinearLayout)this.findViewById(R.id.results_list);
         
         // Create layout for item
      	final LinearLayout itemLayout = new LinearLayout(this);
      	itemLayout.setOrientation(LinearLayout.HORIZONTAL);
      	
         
      	// Create image
         final ImageView image = new ImageView(this);
         int resID = getResources().getIdentifier("android_text_photo", "drawable", getPackageName());	// Get resource ID of image
         image.setImageResource(resID);
         image.setAdjustViewBounds(true);
         image.setMaxHeight(200);
         image.setMaxWidth(200);
         
         // Create nested layout
         final LinearLayout nestedLayout = new LinearLayout(this);
         nestedLayout.setOrientation(LinearLayout.VERTICAL);
         
         // Create text labels
         final TextView title = new TextView(this);
         title.setText(getResources().getString(R.string.sample_title));
         
         final TextView price = new TextView(this);
         price.setText(getResources().getString(R.string.sample_price));
         
         // Create button
         final Button buyButton = new Button(this);
         buyButton.setText(getResources().getString(R.string.buy));       
         buyButton.setOnClickListener(buyButtonListener);
 		
         searchField.setOnEditorActionListener(new OnEditorActionListener() {
         	@Override
         	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
         		boolean handled = false;
     			
     			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
     				handled = true;
     				
         			// Close keyboard
         			InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
     				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
         			
         			// Erase text
         			
         			// Display search results
 	    			listLayout.addView(itemLayout);
 	    			itemLayout.addView(image);
 	    			itemLayout.addView(nestedLayout);
 	    			nestedLayout.addView(title);
 	    			nestedLayout.addView(price);
 	    			itemLayout.addView(buyButton);
         		}
         		
         		return handled;
         	}
         });
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_buy, menu);
 		return true;
 	}
	
	// Called on Buy button press
	public void goToButton(View view) {
		Intent intent = new Intent(this, BuyBookDetailsActivity.class);
 		startActivity(intent);
 	}
    
 	OnClickListener buyButtonListener = new OnClickListener() {
 		@Override
 		public void onClick(View view) {
 			Intent intent = new Intent(thisContext, BuyBookDetailsActivity.class);
 			startActivity(intent);
 		}
 	};
 	
 }
