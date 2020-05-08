 /* source: http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;f=core/java/android/widget/NumberPicker.java;hb=HEAD
  * SICK! Added in API 11, renamed from NumberPicker to NumberSpinner
  *
  * Copyright (C) 2008 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.chess.genesis.view;
 
 import android.content.*;
 import android.os.*;
 import android.text.*;
 import android.text.method.*;
 import android.util.*;
 import android.view.*;
 import android.widget.*;
 import com.chess.genesis.*;
 
 /**
  * A view for selecting a number
  *
  * For a dialog using this view, see {@link android.app.TimePickerDialog}.
  * @hide
  */
 public class NumberSpinner extends LinearLayout
 {
 	private final Handler mHandler;
 	private final EditText mText;
 	private final InputFilter mNumberInputFilter;
 
 	private String[] mDisplayedValues;
 
 	/*
 	 * Lower value of the range of numbers allowed for the NumberSpinner
 	 */
 	private int mStart;
 
 	/*
 	 * Upper value of the range of numbers allowed for the NumberSpinner
 	 */
 	private int mEnd;
 
 	/*
 	 * Current value of this NumberSpinner
 	 */
 	private int mCurrent;
 
 	/*
 	 * Previous value of this NumberSpinner.
 	 */
 	private OnChangedListener mListener;
 	private Formatter mFormatter;
 	private int mPrevious;
 	private long mSpeed = 300;
 
 	private boolean mIncrement;
 	private boolean mDecrement;
 
 	/*
 	 * The callback interface used to indicate the number value has been adjusted.
 	 */
 	public interface OnChangedListener
 	{
 		/**
 		* @param picker The NumberSpinner associated with this listener.
 		* @param oldVal The previous value.
 		* @param newVal The new value.
 		*/
 		void onChanged(NumberSpinner picker, int oldVal, int newVal);
 	}
 
 	/**
 	* Interface used to format the number into a string for presentation
 	*/
 	public interface Formatter
 	{
 		String toString(int value);
 	}
 
 	/*
 	 * Use a custom NumberSpinner formatting callback to use two-digit
 	 * minutes strings like "01".  Keeping a static formatter etc. is the
 	 * most efficient way to do this; it avoids creating temporary objects
 	 * on every call to format().
 	 */
 	public static final NumberSpinner.Formatter TWO_DIGIT_FORMATTER = new NumberSpinner.Formatter()
 	{
 		final StringBuilder mBuilder = new StringBuilder();
 		final java.util.Formatter mFmt = new java.util.Formatter(mBuilder, java.util.Locale.US);
 		final Object[] mArgs = new Object[1];
 
 		@Override
 		public String toString(final int value)
 		{
 			mArgs[0] = value;
 			mBuilder.delete(0, mBuilder.length());
 			mFmt.format("%02d", mArgs);
 			return mFmt.toString();
 		}
 	};
 
 	private final Runnable mRunnable = new Runnable()
 	{
 		@Override
 		public synchronized void run()
 		{
 			if (mIncrement) {
 				changeCurrent(mCurrent + 1);
 				mHandler.postDelayed(this, mSpeed);
 			} else if (mDecrement) {
 				changeCurrent(mCurrent - 1);
 				mHandler.postDelayed(this, mSpeed);
 			}
 		}
 	};
 
 	/*
 	 * Create a new number picker
 	 * @param context the application environment
 	 */
 	public NumberSpinner(final Context context)
 	{
 		this(context, null);
 	}
 
 	/**
 	 * Create a new number picker
 	 * @param context the application environment
 	 * @param attrs a collection of attributes
 	 */
 	public NumberSpinner(final Context context, final AttributeSet attrs)
 	{
 		super(context, attrs);
 
 		setOrientation(VERTICAL);
 		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		inflater.inflate(R.layout.number_spinner, this, true);
 		mHandler = new Handler();
 
 		final OnClickListener clickListener = new OnClickListener()
 		{
 			@Override
 			public void onClick(final View v)
 			{
 				validateInput(mText);
 				if (!mText.hasFocus())
 					mText.requestFocus();
 
 				// now perform the increment/decrement
 				if (R.id.increment == v.getId()) {
 					changeCurrent(mCurrent + 1);
 				} else if (R.id.decrement == v.getId()) {
 					changeCurrent(mCurrent - 1);
 				}
 			}
 		};
 
 		final OnFocusChangeListener focusListener = new OnFocusChangeListener()
 		{
 			@Override
 			public void onFocusChange(final View v, final boolean hasFocus)
 			{
 
 				/* When focus is lost check that the text field
 				 * has valid values.
 				 */
 				if (!hasFocus) {
 					validateInput(v);
 				}
 			}
 		};
 
 		final OnLongClickListener longClickListener = new OnLongClickListener()
 		{
 			/*
 			 * We start the long click here but rely on the {@link NumberSpinnerButton}
 			 * to inform us when the long click has ended.
 			 */
 			@Override
 			public boolean onLongClick(final View v)
 			{
 				/* The text view may still have focus so clear it's focus which will
 				 * trigger the on focus changed and any typed values to be pulled.
 				 */
 				mText.clearFocus();
 
 				if (R.id.increment == v.getId()) {
 					mIncrement = true;
 					mHandler.post(mRunnable);
 				} else if (R.id.decrement == v.getId()) {
 					mDecrement = true;
 					mHandler.post(mRunnable);
 				}
 				return true;
 			}
 		};
 
 		final InputFilter inputFilter = new NumberSpinnerInputFilter();
 		mNumberInputFilter = new NumberRangeKeyListener();
 		mIncrementButton = (NumberSpinnerButton) findViewById(R.id.increment);
 		mIncrementButton.setOnClickListener(clickListener);
 		mIncrementButton.setOnLongClickListener(longClickListener);
 		mIncrementButton.setNumberSpinner(this);
 
 		mDecrementButton = (NumberSpinnerButton) findViewById(R.id.decrement);
 		mDecrementButton.setOnClickListener(clickListener);
 		mDecrementButton.setOnLongClickListener(longClickListener);
 		mDecrementButton.setNumberSpinner(this);
 
 		mText = (EditText) findViewById(R.id.timespinner_input);
 		mText.setOnFocusChangeListener(focusListener);
 		mText.setFilters(new InputFilter[] {inputFilter});
 		mText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
 
 		if (!isEnabled()) {
 			setEnabled(false);
 		}
 	}
 
 	/*
 	 * Set the enabled state of this view. The interpretation of the enabled
 	 * state varies by subclass.
 	 *
 	 * @param enabled True if this view is enabled, false otherwise.
 	 */
 	@Override
 	public final void setEnabled(final boolean enabled)
 	{
 		super.setEnabled(enabled);
 
 		mIncrementButton.setEnabled(enabled);
 		mDecrementButton.setEnabled(enabled);
 		mText.setEnabled(enabled);
 	}
 
 	/*
 	 * Set the callback that indicates the number has been adjusted by the user.
 	 * @param listener the callback, should not be null.
 	 */
 	public void setOnChangeListener(final OnChangedListener listener)
 	{
 		mListener = listener;
 	}
 
 	/*
 	 * Set the formatter that will be used to format the number for presentation
 	 * @param formatter the formatter object.  If formatter is null, String.valueOf()
 	 * will be used
 	 */
 	public void setFormatter(final Formatter formatter)
 	{
 		mFormatter = formatter;
 	}
 
 	/*
 	 * Set the range of numbers allowed for the number picker. The current
 	 * value will be automatically set to the start.
 	 *
 	 * @param start the start of the range (inclusive)
 	 * @param end the end of the range (inclusive)
 	 */
 	public void setRange(final int start, final int end)
 	{
 		setRange(start, end, null/*displayedValues*/);
 	}
 
 	/*
 	* Set the range of numbers allowed for the number picker. The current
 	* value will be automatically set to the start. Also provide a mapping
 	* for values used to display to the user.
 	*
 	* @param start the start of the range (inclusive)
 	* @param end the end of the range (inclusive)
 	* @param displayedValues the values displayed to the user.
 	*/
 	public void setRange(final int start, final int end, final String[] displayedValues)
 	{
 		mDisplayedValues = displayedValues;
 		mStart = start;
 		mEnd = end;
 		mCurrent = start;
 		updateView();
 
 		if (displayedValues != null) {
 			// Allow text entry rather than strictly numeric entry.
 			mText.setRawInputType(InputType.TYPE_CLASS_TEXT |
 			InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
 		}
 	}
 
 	/*
 	 * Set the current value for the number picker.
 	 *
 	 * @param current the current value the start of the range (inclusive)
 	 * @throws IllegalArgumentException when current is not within the range
 	 *         of of the number picker
 	 */
 	public void setCurrent(final int current)
 	{
 		if (current < mStart || current > mEnd) {
 			throw new IllegalArgumentException("current should be >= start and <= end");
 		}
 		mCurrent = current;
 		updateView();
 	}
 
 	/*
 	 * Sets the speed at which the numbers will scroll when the +/-
 	 * buttons are longpressed
 	 *
 	 * @param speed The speed (in milliseconds) at which the numbers will scroll
 	 * default 300ms
 	 */
 	public void setSpeed(final long speed)
 	{
 		mSpeed = speed;
 	}
 
 	private String formatNumber(final int value)
 	{
 		return (mFormatter != null)? mFormatter.toString(value) : String.valueOf(value);
 	}
 
 	/**
 	* Sets the current value of this NumberSpinner, and sets mPrevious to the previous
 	* value.  If current is greater than mEnd less than mStart, the value of mCurrent
 	* is wrapped around.
 	*
 	* Subclasses can override this to change the wrapping behavior
 	*
 	* @param current the new value of the NumberSpinner
 	*/
 	protected void changeCurrent(final int current)
 	{
 		mPrevious = mCurrent;
 
 		// Wrap around the values if we go past the start or end
 		if (current > mEnd) {
 			mCurrent = mStart;
 		} else if (current < mStart) {
 			mCurrent = mEnd;
		} else {
			mCurrent = current;
 		}
 		notifyChange();
 		updateView();
 	}
 
 	/*
 	 * Notifies the listener, if registered, of a change of the value of this
 	 * NumberSpinner.
 	 */
 	private void notifyChange()
 	{
 		if (mListener != null) {
 			mListener.onChanged(this, mPrevious, mCurrent);
 		}
 	}
 
 	/*
 	 * Updates the view of this NumberSpinner.  If displayValues were specified
 	 * in {@link #setRange}, the string corresponding to the index specified by
 	 * the current value will be returned.  Otherwise, the formatter specified
 	 * in {@link setFormatter} will be used to format the number.
 	 */
 	private void updateView()
 	{
 		/* If we don't have displayed values then use the
 		* current number else find the correct value in the
 		* displayed values for the current number.
 		*/
 		if (mDisplayedValues == null) {
 			mText.setText(formatNumber(mCurrent));
 		} else {
 			mText.setText(mDisplayedValues[mCurrent - mStart]);
 		}
 		mText.setSelection(mText.getText().length());
 	}
 
 	private void validateCurrentView(final CharSequence str)
 	{
 		final int val = getSelectedPos(str.toString());
 		if ((val >= mStart) && (val <= mEnd) && (mCurrent != val)) {
 			mPrevious = mCurrent;
 			mCurrent = val;
 			notifyChange();
 		}
 		updateView();
 	}
 
 	private void validateInput(final View v)
 	{
 		final String str = String.valueOf(((TextView) v).getText());
 		if ("".equals(str)) {
 			// Restore to the old value as we don't allow empty values
 			updateView();
 		} else {
 			// Check the new value and ensure it's in range
 			validateCurrentView(str);
 		}
 	}
 
 	/*
 	 * @hide
 	 */
 	public void cancelIncrement()
 	{
 		mIncrement = false;
 	}
 
 	/*
 	 * @hide
 	 */
 	public void cancelDecrement()
 	{
 		mDecrement = false;
 	}
 
 	private static final char[] DIGIT_CHARACTERS = new char[]
 	{
 		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
 	};
 
 	private NumberSpinnerButton mIncrementButton;
 	private NumberSpinnerButton mDecrementButton;
 
 	class NumberSpinnerInputFilter implements InputFilter
 	{
 		@Override
 		public CharSequence filter(final CharSequence source, final int start, final int end, final Spanned dest, final int dstart, final int dend)
 		{
 			if (mDisplayedValues == null) {
 				return mNumberInputFilter.filter(source, start, end, dest, dstart, dend);
 			}
 
 			final CharSequence filtered = String.valueOf(source.subSequence(start, end));
 			final String result = dest.subSequence(0, dstart)
 				+ filtered.toString()
 				+ dest.subSequence(dend, dest.length());
 			final String str = String.valueOf(result).toLowerCase();
 			for (String val : mDisplayedValues) {
 				val = val.toLowerCase();
 				if (val.startsWith(str)) {
 					return filtered;
 				}
 			}
 			return "";
 		}
 	}
 
 	class NumberRangeKeyListener extends NumberKeyListener
 	{
 		// XXX This doesn't allow for range limits when controlled by a
 		// soft input method!
 		@Override
 		public int getInputType()
 		{
 			return InputType.TYPE_CLASS_NUMBER;
 		}
 
 		@Override
 		protected char[] getAcceptedChars()
 		{
 			return DIGIT_CHARACTERS;
 		}
 
 		@Override
 		public CharSequence filter(final CharSequence source, final int start, final int end, final Spanned dest, final int dstart, final int dend)
 		{
 			CharSequence filtered = super.filter(source, start, end, dest, dstart, dend);
 			if (filtered == null) {
 				filtered = source.subSequence(start, end);
 			}
 
 			final String result = dest.subSequence(0, dstart)
 				+ filtered.toString()
 				+ dest.subSequence(dend, dest.length());
 
 			if ("".equals(result)) {
 				return result;
 			}
 			final int val = getSelectedPos(result);
 
 			/* Ensure the user can't type in a value greater
 			 * than the max allowed. We have to allow less than min
 			 * as the user might want to delete some numbers
 			 * and then type a new number.
 			 */
 			if (val > mEnd)
 				return "";
 			return filtered;
 		}
 	}
 
 	private int getSelectedPos(final String str)
 	{
 		if (mDisplayedValues == null) {
 			try {
 				return Integer.parseInt(str);
 			} catch (final NumberFormatException e) {
 				/* Ignore as if it's not a number we don't care */
 			}
 		} else {
 			final String dVal = str.toLowerCase();
 			for (int i = 0; i < mDisplayedValues.length; i++) {
 				/* Don't force the user to type in jan when ja will do */
 				if (mDisplayedValues[i].toLowerCase().startsWith(dVal)) {
 					return mStart + i;
 				}
 			}
 
 			/* The user might have typed in a number into the month field i.e.
 			 * 10 instead of OCT so support that too.
 			 */
 			try {
 				return Integer.parseInt(dVal);
 			} catch (final NumberFormatException e) {
 				/* Ignore as if it's not a number we don't care */
 			}
 		}
 		return mStart;
 	}
 
 	/*
 	 * Returns the current value of the NumberSpinner
 	 * @return the current value.
 	 */
 	public int getCurrent()
 	{
 		return mCurrent;
 	}
 
 	/*
 	 * Returns the upper value of the range of the NumberSpinner
 	 * @return the uppper number of the range.
 	 */
 	protected int getEndRange()
 	{
 		return mEnd;
 	}
 
 	/*
 	 * Returns the lower value of the range of the NumberSpinner
 	 * @return the lower number of the range.
 	 */
 	protected int getBeginRange()
 	{
 		return mStart;
 	}
 }
