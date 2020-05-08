 package com.bigfatbrowncat.datetimechoice;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.os.Handler;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.AttributeSet;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 
 public class NumberSpinEdit extends LinearLayout implements View.OnClickListener, 
                                                             View.OnFocusChangeListener, 
                                                             View.OnKeyListener, 
                                                             View.OnLongClickListener,
                                                             View.OnTouchListener,
                                                             TextWatcher {
 
 	public static class DayValueConverter implements ValueConverter {
 
 		@Override
 		public String format(int value) {
 			String s = String.valueOf(value);
 			if (value < 10) s = "0" + s;
 			return s;
 		}
 
 		@Override
 		public int parse(String str) {
 			int value;
 			try {
 				value = Integer.parseInt(str);
 			} catch (NumberFormatException ex) {
 				value = 0;
 			}
 			return value;
 		}
 	}
 	
 	public static class MonthValueConverter implements ValueConverter {
 		private Context context;
 		
 		public MonthValueConverter(Context context) {
 			this.context = context;
 		}
 		
 		@Override
 		public String format(int value) {
 			String[] monthNames = context.getResources().getStringArray(R.array.month_names);
 			String s = monthNames[value - 1];
 			return s;
 		}
 
 		@Override
 		public int parse(String str) {
 			String[] monthNames = context.getResources().getStringArray(R.array.month_names);
 			for (int i = 0; i > monthNames.length; i++) {
 				if (monthNames[i].startsWith(str)) {
 					return i;
 				}
 			}
 			
 			int value;
 			try {
 				value = Integer.parseInt(str);
 			} catch (NumberFormatException ex) {
 				value = 0;
 			}
 			return value;
 		}
 	}
 	
 	public interface OnValueChangeListener {
 		public void onChanged(NumberSpinEdit view);
 
 		/**
 		 * Occurs after user has changed the value inside the edit box 
 		 * or by the buttons, but before the value is committed.
 		 * @param view the sender view
 		 * @param newValue the value which is going to be committed
 		 * @return The handler code should return <code>true</code> in order to commit value change. 
 		 * If it returns <code>false</code>, the change isn't applied.
 		 */
 		public boolean onChanging(NumberSpinEdit view, int newValue);
 
 		/**
 		 * Occurs after user has clicked lowering button
 		 */
 		public void onLowering(NumberSpinEdit view);
 		
 		/**
 		 * Occurs after user has clicked raising button
 		 */
 		public void onRaising(NumberSpinEdit view);
 	}
 	
 	private static class SavedState extends BaseSavedState {
 		private int value;
 
 		// required field that makes Parcelables from a Parcel
 		@SuppressWarnings("unused")
 		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
 			public SavedState createFromParcel(Parcel in) {
 				return new SavedState(in);
 			}
 
 			public SavedState[] newArray(int size) {
 				return new SavedState[size];
 			}
 		};
 
 		private SavedState(Parcel in) {
 			super(in);
 			this.value = in.readInt();
 		}
 
 		SavedState(Parcelable superState) {
 			super(superState);
 		}
 
 		public int getValue() {
 			return value;
 		}
 
 		public void setValue(int value) {
 			this.value = value;
 		}
 
 		@Override
 		public void writeToParcel(Parcel out, int flags) {
 			super.writeToParcel(out, flags);
 			out.writeInt(this.value);
 		}
 	}
 	
 	
 	private static class UpDownHandler extends Handler {
 		private NumberSpinEdit view;
 		private boolean raiseContinuously;
 		private boolean lowerContinuously;
 		private int delay;
 
 		private Runnable upDownRunnable;
 		
 		public UpDownHandler(NumberSpinEdit view, int delay) {
 			super();
 			this.view = view;
 			this.delay = delay;
 			
 			upDownRunnable = new Runnable() {
 				
 				@Override
 				public void run() {
 					boolean doPostDelayed = false;
 					
 					if (raiseContinuously) {
 						if (UpDownHandler.this.view.raiseValue()) {
 							doPostDelayed = true;
 						} else {
 							raiseContinuously = false;
 						}
 					}
 					
 					if (lowerContinuously) {
 						if (UpDownHandler.this.view.lowerValue()) {
 							doPostDelayed = true;
 						} else {
 							lowerContinuously = false;
 						}
 					}
 					
 					if (doPostDelayed) {
 						postDelayed(this, UpDownHandler.this.delay);
 					}
 				}
 			};
 		}
 		
 		public void startLowering() {
 			lowerContinuously = true;
 			post(upDownRunnable);
 		}
 		
 		public void startRaising() {
 			raiseContinuously = true;
 			post(upDownRunnable);
 		}
 		
 		public void stopLowering() {
 			lowerContinuously = false;
 		}
 		
 		public void stopRaising() {
 			raiseContinuously = false;
 		}
 		
 	}
 	
 	public interface ValueConverter {
 		public String format(int value);
 		public int parse(String str);
 	}
 	
 	private UpDownHandler upDownHandler;
 	private ValueConverter valueConverter;
 	
 	private EditText number_editText;
 	private ImageButton raise_button;
 	private ImageButton lower_button;
 	
 	private int ems;
 	private int value;
 	private int minValue;
 	private int maxValue;
 	
 	private OnValueChangeListener valueChangeListener;
 	private boolean isSelfEditing;
 	
 	public NumberSpinEdit(Context context) {
 		super(context);
 		initLayout();
 
 		valueConverter = new DayValueConverter();
 
 		this.ems = 2;
 		this.maxValue = 0;
 		this.maxValue = (int)(Math.pow(10, ems) - 1);
 		this.setValue(0);
 
 		upDownHandler = new UpDownHandler(this, 100);		// TODO Magic number! 100 msec
 		updateButtonsEnabled();
 	}
 
 	public NumberSpinEdit(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initLayout();
 
 		valueConverter = new DayValueConverter();
 
 		TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NumberSpinEdit, 0, 0);
 		setEms(attributes.getInteger(R.styleable.NumberSpinEdit_ems, 2));
 		setMinValue(attributes.getInteger(R.styleable.NumberSpinEdit_minValue, 0));
 		setMaxValue(attributes.getInteger(R.styleable.NumberSpinEdit_maxValue, (int)(Math.pow(10, ems) - 1)));
 		setValue(attributes.getInteger(R.styleable.NumberSpinEdit_value, minValue));
 		attributes.recycle();
 		
 		upDownHandler = new UpDownHandler(this, 100);		// TODO Magic number! 100 msec
 		updateButtonsEnabled();
 	}
 
 	@Override
 	public void afterTextChanged(Editable editable) {
 		if (!isSelfEditing) {
 			if (editable == number_editText.getText()) {
 				changeValue();
 			}
 		}
 	}
 
 	@Override
 	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	protected void changeValue() {
 		int newValue = valueConverter.parse(number_editText.getText().toString());
 		
 		boolean commit = true;
 		boolean updateEditText = false;
 		
 		int newEms = newValue != 0 ? (int)Math.log10(newValue) + 1 : 1;
 		
 		if (newEms > ems) {
 			newValue = maxValue;
 			updateEditText = true;
 		}
 		
 		if (newValue > maxValue) {
 			newValue = maxValue;
 		}
 		
 		if (newValue < minValue) {
 			newValue = minValue;
 		}
 		
		if (valueChangeListener != null && newValue != value) {
			if (!valueChangeListener.onChanging(this, newValue)) {
				commit = false;
				updateEditText = true;
			}
		}
		
 		if (commit) {
 			if (updateEditText) {
 				setValue(newValue);
 			} else {
 				value = newValue;
 			}
 			
 			if (valueChangeListener != null) {
 				valueChangeListener.onChanged(this);
 			}
 
 		} else {
 			// Taking the value back
 			setValue(value);
 		}
 		
 		updateButtonsEnabled();
 	}
 
 	@Override
 	public int getBaseline() {
 		if (number_editText != null) {
 			return number_editText.getBaseline() + number_editText.getTop();
 		} else {
 			return super.getBaseline();
 		}
 	}
 
 	public int getEms() {
 		return ems;
 	}
 
 	public int getMaxValue() {
 		return maxValue;
 	}
 
 	public int getMinValue() {
 		return minValue;
 	}
 
 	public int getValue() {
 		return value;
 	}
 
 	public ValueConverter getValueConverter() {
 		return valueConverter;
 	}
 
 	private void initLayout() {
 		LayoutInflater layoutInflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		layoutInflater.inflate(R.layout.view_number_spin_edit, this, true);
 		
 		number_editText = (EditText)findViewById(R.id.number_editText);
 		raise_button = (ImageButton)findViewById(R.id.raise_button);
 		lower_button = (ImageButton)findViewById(R.id.lower_button);
 		
 		if (!isInEditMode()) {
 			number_editText.addTextChangedListener(this);
 			number_editText.setOnFocusChangeListener(this);
 			number_editText.setOnKeyListener(this);
 			number_editText.setSelectAllOnFocus(true);
 			
 			raise_button.setOnClickListener(this);
 			raise_button.setOnLongClickListener(this);
 			raise_button.setOnTouchListener(this);
 			
 			lower_button.setOnClickListener(this);
 			lower_button.setOnLongClickListener(this);
 			lower_button.setOnTouchListener(this);
 			
 		}
 
 		// Clearing the ID of inner input field in order to prevent re-using same ids.
 		number_editText.setId(NO_ID);
 	}
 	
 	protected boolean lowerValue() {
 		int newValue = value - 1;
 		boolean commit = true;
 		if (valueChangeListener != null) {
 			valueChangeListener.onLowering(this);
 			if (!valueChangeListener.onChanging(this, newValue)) {
 				commit = false;
 			}
 		}
 		
 		if (newValue < minValue) {
 			newValue = minValue;
 		}
 		
 		boolean changed = false;
 		if (commit && newValue != value)
 		{
 			setValue(newValue);
 			changed = true;
 			updateButtonsEnabled();
 
 			if (valueChangeListener != null) {
 				valueChangeListener.onChanged(this);
 			}
 		}
 		return changed;
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.raise_button) {
 			raiseValue();
 		} else if (v.getId() == R.id.lower_button) {
 			lowerValue();
 		}
 		
 	}
 
 	@Override
 	public void onFocusChange(View v, boolean hasFocus) {
		updateValue();	
 	}
 	
 	@Override
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_ENTER) {
 			setValue(value);
 		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
 			raiseValue();
 		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
 			lowerValue();
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onLongClick(View v) {
 		if (v.getId() == R.id.raise_button) {
 			upDownHandler.startRaising();
 		} else if (v.getId() == R.id.lower_button) {
 			upDownHandler.startLowering();
 		}
 		return false;
 	}
 	
 	@Override
 	protected void onRestoreInstanceState(Parcelable state) {
 		if (!(state instanceof SavedState)) {
 			super.onRestoreInstanceState(state);
 			return;
 		}
 
 		SavedState ss = (SavedState) state;
 		super.onRestoreInstanceState(ss.getSuperState());
 
 		setValue(ss.getValue());
 	}
 	
 	@Override
 	protected Parcelable onSaveInstanceState() {
 	    Parcelable superState = super.onSaveInstanceState();
 	    SavedState ss = new SavedState(superState);
 
 	    ss.setValue(value);
 
 	    return ss;
 	}
 	
 	@Override
 	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP)
 		{
 			if (v.getId() == R.id.raise_button) {
 				upDownHandler.stopRaising();
 			} else if (v.getId() == R.id.lower_button) {
 				upDownHandler.stopLowering();
 			}
 		}
 		return false;
 	}
 
 	
 	protected boolean raiseValue() {
 		int newValue = value + 1;
 		boolean commit = true;
 		if (valueChangeListener != null) {
 			valueChangeListener.onRaising(this);
 			if (!valueChangeListener.onChanging(this, newValue)) {
 				commit = false;
 			}
 		}
 		
 		if (newValue > maxValue) {
 			newValue = maxValue;
 		}
 		
 		boolean changed = false;
 		if (commit && newValue != value)
 		{
 			setValue(newValue);
 			changed = true;
 			updateButtonsEnabled();
 			
 			if (valueChangeListener != null) {
 				valueChangeListener.onChanged(this);
 			}
 		}
 		return changed;
 	}
 
 	public void setEms(int ems) {
 		this.ems = ems;
 		if (number_editText != null) {
 			number_editText.setEms(ems);
 		}
 	}
 
 	public void setMaxValue(int maxValue) {
 		this.maxValue = maxValue;
 		if (value > maxValue) setValue(maxValue);
 		if (minValue > maxValue) minValue = maxValue;
 		updateButtonsEnabled();
 	}
 	
 	public void setMinValue(int minValue) {
 		this.minValue = minValue;
 		if (value < minValue) setValue(minValue);
 		if (maxValue < minValue) maxValue = minValue;
 		updateButtonsEnabled();
 	}
 
 	public void setOnValueChangeListener(OnValueChangeListener valueChangeListener) {
 		this.valueChangeListener = valueChangeListener;
 	}
 
 	private void updateValue() {
 		if (number_editText != null) {
 			isSelfEditing = true;
 			number_editText.setText(valueConverter.format(value));
 			isSelfEditing = false;
 			number_editText.selectAll();
 		}
 	}
 	
 	public void setValue(int value) {
 		this.value = value;
 		updateValue();
 		updateButtonsEnabled();
 	}
 
 	public void setValueConverter(ValueConverter valueConverter) {
 		this.valueConverter = valueConverter;
 		updateValue();
 	}
 
 	protected void updateButtonsEnabled() {
 		if (!isInEditMode()) {
 			raise_button.setEnabled(value < maxValue);
 			lower_button.setEnabled(value > minValue);
 		}
 	}
 
 
 }
