 package com.openfridge;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 //TODO Change date picker to simpler version SC
 //TODO Implement saved food behaviour        ZH/EL/JW
 //
 public class ItemEditActivity extends Activity {
     private HashMap<String, Integer> favoritesExpiry;
 	private EditText descField;
 	private FridgeFood food;
 	private static final int MAX_WIDTH_OFFSET = 10;
 	private boolean isUpdate;
 	private DatePicker datePicker;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		populateFavorites();
 		
 		food = FridgeFood.getFoodFromBundle(getIntent().getExtras());
 		isUpdate = (food.getId() != -1);
 
 		setContentView(R.layout.item_edit);
 		
 		if (isUpdate) setUpdateText();
 
 		descField = (EditText) findViewById(R.id.descriptionEditText);
 
 		// Prepopulate date and description field
 
 		String description = food.getDescription();
 		GregorianCalendar expirationDate = food.getExpirationDate();
 
 		descField.setText(description);
 		descField.setSelection(description.length());
 
 		datePicker = ((DatePicker) findViewById(R.id.datePicker1));
 		datePicker.updateDate(expirationDate.get(Calendar.YEAR),
 				expirationDate.get(Calendar.MONTH), // Month from 0
 				expirationDate.get(Calendar.DAY_OF_MONTH));
 
		//only show commonItems if you are creating a new item
		if (food.getId()==-1) { addCommonItemButtons(); } 
 	}
 
 	/** Add the common items buttons which can be clicked to instantly
 	 add a common item */
 	private void addCommonItemButtons() {
 		Display display = getWindowManager().getDefaultDisplay();
 		int maxWidth = display.getWidth() - MAX_WIDTH_OFFSET;
 		int currentWidth = 0;
 
 		ArrayAdapter<CharSequence> descriptions = ArrayAdapter.createFromResource(
 				this, R.array.Common_items,
 				android.R.layout.simple_spinner_item);
 		descriptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
 
 		LinearLayout layout = (LinearLayout) findViewById(R.id.common_items_buttons);
 		LinearLayout currentLayout = getHorizontalLinearLayout();
 
 		for (int i = 0; i < descriptions.getCount(); i++) {
 			CharSequence desc = descriptions.getItem(i);
 
 			Button itemButton = new Button(this);
 			itemButton.setLayoutParams(new LayoutParams(
 					ViewGroup.LayoutParams.WRAP_CONTENT,
 					ViewGroup.LayoutParams.WRAP_CONTENT));
 			itemButton.setText(desc.toString());
 			itemButton
 					.setOnClickListener(new CommonFoodButtonOnClickListener());
 
 			itemButton.measure(0, 0);
 			int buttonWidth = itemButton.getMeasuredWidth();
 			if (currentWidth + buttonWidth > maxWidth) {
 				layout.addView(currentLayout);
 				currentLayout = getHorizontalLinearLayout();
 				currentWidth = 0;
 			}
 
 			currentWidth += buttonWidth;
 			// If the button won't fit on one line, force it a single line
 			if (buttonWidth > maxWidth && currentWidth == buttonWidth) {
 				itemButton = new Button(this);
 				itemButton.setLayoutParams(new LayoutParams(maxWidth,
 						ViewGroup.LayoutParams.WRAP_CONTENT));
 				itemButton.setText(desc.toString());
 				currentLayout.addView(itemButton);
 				layout.addView(currentLayout);
 
 				itemButton
 						.setOnClickListener(new CommonFoodButtonOnClickListener());
 				currentLayout = getHorizontalLinearLayout();
 				currentWidth = 0;
 			} else {
 				currentLayout.addView(itemButton);
 			}
 		}
 		layout.addView(currentLayout);
 	}
 
 	private LinearLayout getHorizontalLinearLayout() {
 		LinearLayout ll = new LinearLayout(this);
 		ll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 				LayoutParams.WRAP_CONTENT));
 		ll.setOrientation(LinearLayout.HORIZONTAL);
 
 		return ll;
 	}
 
 	// Add default expiry offsets in number of days
 	private void populateFavorites() {
 	    favoritesExpiry = new HashMap<String, Integer>();
 	    
 	    favoritesExpiry.put("Eggs", 28);
 	    favoritesExpiry.put("Milk", 14);
 	    favoritesExpiry.put("Cheese", 21);
 	    favoritesExpiry.put("Tomatoes", 5);
 	}
 	
 	private void setUpdateText() {
 	    TextView customItemsTitle 
 	        = (TextView) findViewById(R.id.custom_items_title);
 	    customItemsTitle.setText(R.string.updateItemTitle);
 	    
 	    Button apply = (Button) findViewById(R.id.button1);
 	    apply.setText(R.string.updateButtonTitle);
 	}
 
 	// Utility methods
 	private String getSimpleDateString() {
         DatePicker dp = (DatePicker) findViewById(R.id.datePicker1);
         
         return dp.getYear() + "-" + dp.getMonth() + "-" +dp.getDayOfMonth();
 	}
 
 	// Callbacks
 	public void doneBtnCallback(View view) {
 		
 		try {
 			String description = descField.getText().toString();
 			if (!description.equals("")) {
 				food.setDescription(description.toString());
 				food.setExpirationDate(getSimpleDateString());
 
 				DataClient.getInstance().doNetOp(this,
 						(!isUpdate) ? NetOp.PUSH : NetOp.UPDATE, food);
 
 				finish();
 			} else {
 				Toast.makeText(getBaseContext(), "Please enter a food description",
 						Toast.LENGTH_SHORT).show();
 			}
 		} catch (RuntimeException e) {
 			e.printStackTrace();
 			throw e;
 		}
 	}
 	
 	private Calendar getOffsetDate(int offset) {
 	    Calendar cal = Calendar.getInstance();
 	    cal.add(Calendar.DATE, offset);
 	    return cal;
 	}
 
 	public void CancelClick(View view) {
 		finish();
 	}
 
 	private class CommonFoodButtonOnClickListener implements OnClickListener {
 		@Override
 		public void onClick(View v) {
 			// Populate the form fields based on stored values
 		    String food = ((Button) v).getText().toString();
 		    int daysToExpire = favoritesExpiry.get(food);
 		    
 		    Calendar expiryDate = getOffsetDate(daysToExpire);
 		    
 		    descField.setText(food);
 		    descField.setSelection(food.length());
 		    //ToDO
 		    
 		    datePicker.updateDate(expiryDate.get(Calendar.YEAR), 
 		                          expiryDate.get(Calendar.MONTH), 
 		                          expiryDate.get(Calendar.DAY_OF_MONTH));
 		    
 		    
 		    /*
 			DataClient.getInstance().doNetOp(
 					ItemEditActivity.this,
 					NetOp.PUSH,
 					new FridgeFood(((Button) v).getText().toString(),
 							getSimpleDateString()));
 			finish();*/
 		}
 	}
 }
