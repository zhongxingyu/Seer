 package com.ACM.binarycalculator;
 
import android.annotation.TargetApi;
import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class CalculatorFragment extends Fragment {
 	// this is a tag used for debugging purposes
 	private static final String TAG = "CalculatorFragment";
 	private static final String KEY_WORKINGTEXTVIEW_STRING = "workingTextString";
 
 	// these are our member variables
 	TextView mComputeTextView;
 	TextView mWorkingTextView;
 	String mCurrentWorkingText;
 
	// this means there is code in here that needs to used or not used on that
	// specific SDK
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
 	@Override
 	// we need to inflate our View so let's grab all the View IDs and inflate
 	// them.
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		// we need to make a view instance from our layout.
 		View v = inflater.inflate(R.layout.fragment_calculator_decimal,
 				container, false);
 
 		// get the textViews by id, notice we have to reference them via the
 		// view instance we just created.
 		mComputeTextView = (TextView) v
 				.findViewById(R.id.fragment_calculator_decimal_computedTextView);
 		mWorkingTextView = (TextView) v
 				.findViewById(R.id.fragment_calculator_decimal_workingTextView);
 
		// apparently the method savedInstanceState.getString() is only
		// available on SDK's >= 12. So we have to add this check before we do
		// it to make sure devices running an API >= 12 only run this code
		if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)) {
			// if the we saved something away, grab it!
			if (savedInstanceState != null) {
				mCurrentWorkingText = savedInstanceState.getString(
						KEY_WORKINGTEXTVIEW_STRING, "");
				// set the text to be what we saved away and just now retrieved.
				mWorkingTextView.setText(mCurrentWorkingText);
			}
 		}
 
 		View.OnClickListener genericButtonListener = new View.OnClickListener() {
 			// when someone clicks a button that isn't "special" we are going to
 			// add it to the workingTextView
 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 				mCurrentWorkingText = mWorkingTextView.getText().toString();
 				String textFromButton = textView.getText().toString();
 				// see if the workingTextView is empty
 				if (mCurrentWorkingText.length() == 0) {
 					mWorkingTextView.setText(textFromButton);
 					mCurrentWorkingText = textFromButton;
 				} else {
 					// if the working TextView isn't zero we need to append the
 					// textFromButton to what is already there.
 					mWorkingTextView.setText(mCurrentWorkingText
 							+ textFromButton);
 					mCurrentWorkingText = mWorkingTextView.getText().toString();
 				}
 			}
 		};
 
 		View.OnClickListener backspaceButtonListener = new View.OnClickListener() {
 			// remove the last thing to be inputed into the workingTextView,
 			// also update the post fix stacks accordingly?
 			@Override
 			public void onClick(View v) {
 				// need to check if the view has anything in it, because if it
 				// doesn't the app will crash when trying to change a null
 				// string.
 				if (mCurrentWorkingText.length() != 0) {
 					mCurrentWorkingText = mCurrentWorkingText.substring(0,
 							mCurrentWorkingText.length() - 1);
 					mWorkingTextView.setText(mCurrentWorkingText);
 				}
 			}
 		};
 
 		// get a reference to our TableLayout XML
 		TableLayout tableLayout = (TableLayout) v
 				.findViewById(R.id.fragment_calculator_decimal_tableLayout);
 
 		// adds the values and listeners to the buttons and pretty much every
 		// button except for a few
 		//
 		// this for loop could probably be cleaned up, because the views had
 		// changed from the original and the for loop had to change as well,
 		// making the for loop look like a logical mess.
 		int numberForTheButton = 1;
 		for (int i = tableLayout.getChildCount() - 2; i >= 0; i--) {
 			// get the tableRow from the table layout
 			TableRow row = (TableRow) tableLayout.getChildAt(i);
 			for (int j = 0; j < row.getChildCount(); j++) {
 				// get the button from the tableRow
 				Button butt = (Button) row.getChildAt(j);
 				// if we are in the first row (topmost), and on the first button
 				// (leftmost), we want that button to be a '('
 				if (i == 0 && j == 0) {
 					butt.setText("(");
 					butt.setOnClickListener(genericButtonListener);
 				}
 				// if we are on the topmost row and the second button, make the
 				// button a ')'
 				else if (i == 0 && j == 1) {
 					butt.setText(")");
 					butt.setOnClickListener(genericButtonListener);
 				}
 				// if we are in one of the number rows, just set the number of
 				// the button
 				else if (j < row.getChildCount() - 1 && i > 0) {
 					butt.setText("" + numberForTheButton++);
 					butt.setOnClickListener(genericButtonListener);
 
 				} else {
 					// this sets the button of the last column of every row
 					if (i == tableLayout.getChildCount() - 2) {
 						butt.setText("-");
 						butt.setOnClickListener(genericButtonListener);
 					} else if (i == tableLayout.getChildCount() - 3) {
 						butt.setText("x");
 						butt.setOnClickListener(genericButtonListener);
 					} else if (i == tableLayout.getChildCount() - 4) {
 						butt.setText("/");
 						butt.setOnClickListener(genericButtonListener);
 					} else if (i == tableLayout.getChildCount() - 5) {
 						butt.setText("<-");
 						butt.setOnClickListener(backspaceButtonListener);
 					}
 				}
 			}
 		} // closes for loop
 
 		// get a reference to the first (topmost) row so we can set the clear
 		// all button manually, because it was annoying trying to work it in to
 		// the for loop
 		TableRow firstRow = (TableRow) tableLayout.getChildAt(0);
 		// the clear all button was decided to be the third button in the
 		// topmost row
 		Button clearAllButton = (Button) firstRow.getChildAt(2);
 		clearAllButton.setText("Clear All");
 		clearAllButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// clear all the text in the working textView, AND maybe the
 				// computed textView as well?
 				// Also, might want to clear out the post fix expression stack
 				mWorkingTextView.setText("");
 				mCurrentWorkingText = "";
 				mComputeTextView.setText("");
 			}
 		});
 
 		// now we need to get the last row of buttons and get them to the
 		// screen.
 		TableRow lastRow = (TableRow) tableLayout.getChildAt(tableLayout
 				.getChildCount() - 1);
 
 		// set the decimal button
 		Button zeroButton = (Button) lastRow.getChildAt(2);
 		zeroButton.setText(".");
 		zeroButton.setOnClickListener(genericButtonListener);
 
 		// set the zero button
 		Button decimalPointButton = (Button) lastRow.getChildAt(1);
 		decimalPointButton.setText("0");
 		decimalPointButton.setOnClickListener(genericButtonListener);
 
 		// set the plus button
 		Button plusButton = (Button) lastRow.getChildAt(3);
 		plusButton.setText("+");
 		plusButton.setOnClickListener(genericButtonListener);
 
 		// set the equals button, it will have it's own separate listener to
 		// compute the inputed value
 		Button equalsButton = (Button) lastRow.getChildAt(0);
 		equalsButton.setText("=");
 		equalsButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO The arithmetic for the inputed numbers. Post fix?
 
 			}
 		});
 
 		return v;
 	}
 
 	// method to save the state of the application during the activity life
 	// cycle. This is so we can preserve the values in the textViews upon screen
 	// rotation.
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		Log.i(TAG, "onSaveInstanceState");
 		outState.putString(KEY_WORKINGTEXTVIEW_STRING, mCurrentWorkingText);
 	}
 }
