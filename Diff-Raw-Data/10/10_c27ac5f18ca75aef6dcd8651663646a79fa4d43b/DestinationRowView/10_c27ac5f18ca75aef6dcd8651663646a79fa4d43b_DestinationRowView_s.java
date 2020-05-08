 package org.routy.view;
 
 import java.util.UUID;
 
 import org.routy.R;
 
 import android.content.Context;
 import android.location.Address;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 
 public abstract class DestinationRowView extends LinearLayout {
 
 	private final String TAG = "DestinationRowView";
 	
 	public static final int VALID = 0;
 	public static final int INVALID = 1;
 	public static final int NOT_VALIDATED = 2;
 	
 	private UUID id;
 	private int status;
 	
 	private String addressString;
 	private Address address;
 	private EditText editText;
 //	private Button addButton;
 	private Button removeButton;
 	private boolean ignoreOnFocusLostCallback;
 	private boolean ignoreOnFocusLostCallbackTemp;
 	
 	
 	/**
 	 * Called when the "remove" button is clicked for a DestinationAddView
 	 * @param id		the {@link UUID} of the DestinationAddView to remove
 	 */
 	public abstract void onRemoveClicked(UUID id);
 	
 //	public abstract void onAddClicked(UUID id);
 	
 	public abstract void onFocusLost(UUID id);
 	
 	
 	public DestinationRowView(Context context) {
 		this(context, "");
 	}
 	
 	public DestinationRowView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 		this.id = UUID.randomUUID();
 		this.addressString = "";
 		this.address = null;
 		this.status = DestinationRowView.NOT_VALIDATED;
 		this.ignoreOnFocusLostCallback = false;
 		
 		initViews(context);
 	}
 	
 	public DestinationRowView(Context context, String addressString) {
 		super(context);
 		
 		this.id = UUID.randomUUID();
 		this.addressString = addressString;
 		this.address = null;
 		this.status = DestinationRowView.NOT_VALIDATED;
 		this.ignoreOnFocusLostCallback = false;
 		
 		initViews(context);
 	}
 	
 	public DestinationRowView(Context context, Address address) {
 		super(context);
 		
 		this.id = UUID.randomUUID();
 		this.address = address;
 		this.status = DestinationRowView.VALID;
 		this.addressString = address.getFeatureName();
 		this.ignoreOnFocusLostCallback = false;
 		
 		initViews(context);
 	}
 	
 	
 	private void initViews(Context context) {
 		// Inflate the view for an "add destination" text field and remove button
 		LayoutInflater inflater = LayoutInflater.from(context);
 		inflater.inflate(R.layout.view_destination_row, this);
 		
 		editText = (EditText) findViewById(R.id.edittext_destination_add);
 		editText.setText(addressString);
 		editText.addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				// Changing the text in this row should throw out any previous validation
 				clearValidationStatus();
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 				// this space intentionally left blank
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				// this space intentionally left blank
 			}
 		});
 		editText.setOnFocusChangeListener(new OnFocusChangeListener() {
 			
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (!hasFocus) {
 					Log.v(TAG, "onFocusChange (lost focus) from row with id=" + id);
 					if (!ignoreOnFocusLostCallback) {
 						onFocusLost(id);
 					} else {
 						if (ignoreOnFocusLostCallbackTemp) {
 							ignoreOnFocusLostCallback = false;
 						}
 					}
 				}
 			}
 		});
 		
 		removeButton = (Button) findViewById(R.id.button_destination_remove);
 		removeButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Log.v(TAG, "onRemoveClicked from row with id=" + id);
 				onRemoveClicked(id);
 			}
 		});
 	}
 	
 	
 	public UUID getUUID() {
 		return this.id;
 	}
 	
 	
 	public void setInvalid() {
 		editText.setTextColor(getResources().getColor(R.color.Red));
 		status = DestinationRowView.INVALID;
 	}
 
 
 	public void setValid() {
 		editText.setTextColor(getResources().getColor(R.color.White));
 		status = DestinationRowView.VALID;
 	}
 	
 	
 	public void clearValidationStatus() {
 		editText.setTextColor(getResources().getColor(R.color.White));
 		status = DestinationRowView.NOT_VALIDATED;
 	}
 	
 	
 	/**
 	 * Resets this destination row.  Re-enables the add button, clears the text field, 
 	 * and resets the validation status.
 	 */
 	public void reset() {
 		editText.setText("");
 		status = DestinationRowView.NOT_VALIDATED;
 	}
 	
 	
 	public String getAddressString() {
 		return editText.getText().toString();
 	}
 	
 	
 	public int getStatus() {
 		return status;
 	}
 
 	public void setAddress(Address address) {
 		this.address = address;
 		
 		String formattedAddress = this.address.getExtras().getString("formatted_address");
 		editText.setText(address.getFeatureName() + " - " + formattedAddress);
 		
 	}
 	
 	public Address getAddress() {
 		return this.address;
 	}
 	
 	public void focusOnAddressField() {
 		editText.requestFocus();
 	}
 	
 	
 	/**
 	 * Tell this row to ignore the onFocusLost callback.
 	 * 
 	 * @param temporary		true will restore normal behavior after onFocusLost is called once, 
 	 * 						false will leave it off until manually turned back on
 	 */
 	public void disableOnFocusLostCallback(boolean temporary) {
 		ignoreOnFocusLostCallback = true;
 		ignoreOnFocusLostCallbackTemp = temporary;
 	}
 	
 	public void enableOnFocusLostCallback() {
 		ignoreOnFocusLostCallback = false;
 	}
 
 	public boolean needsValidation() {
 		return getStatus() == DestinationRowView.INVALID || getStatus() == DestinationRowView.NOT_VALIDATED;
 	}
 }
