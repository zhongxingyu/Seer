 package com.ACM.binarycalculator;
 
 import java.util.StringTokenizer;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * 
  * @author James Van Gaasbeck, ACM at UCF <jjvg@knights.ucf.edu>
  * 
  * 
  */
 public class CalculatorDecimalFragment extends Fragment {
 
 	// this is a tag used for debugging purposes
 	// private static final String TAG = "CalculatorDecimalFragment";
 
 	// string constant for saving our workingTextViewText
 	private static final String KEY_WORKINGTEXTVIEW_STRING = "workingTextString";
 
 	// the views number in the view pagers, pager adapter
 	private static final int VIEW_NUMBER = 1;
 	// the radix number (base-number) to be used when parsing the string.
 	private static final int VIEWS_RADIX = 10;
 
 	// these are our member variables
 	TextView mComputeTextView;
 	TextView mWorkingTextView;
 	FragmentDataPasser mCallback;
 	static String mCurrentWorkingText;
 
 	// we need to inflate our View so let's grab all the View IDs and inflate
 	// them.
 	@Override
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
 
 		// if the we saved something away to handle the activity life cycle,
 		// grab it!
 		if (savedInstanceState != null) {
 			mCurrentWorkingText = savedInstanceState
 					.getString(KEY_WORKINGTEXTVIEW_STRING);
 			// set the text to be what we saved away and just now retrieved.
 			mWorkingTextView.setText(mCurrentWorkingText);
 		}
 
 		View.OnClickListener genericButtonListener = new View.OnClickListener() {
 			// when someone clicks a button that isn't "special" we are going to
 			// add it to the workingTextView
 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 				mCurrentWorkingText = mWorkingTextView.getText().toString();
 				String textFromButton = textView.getText().toString();
 				boolean inputTextIsOperator = false, inputIsPeriod = false;
 
 				if (textFromButton == "+" || textFromButton == "-"
 						|| textFromButton == "x" || textFromButton == "/") {
 					inputTextIsOperator = true;
 				} else if (textFromButton == ".") {
 					inputIsPeriod = true;
 				}
 
 				// if the button was just a number put it on textView
 				if (!inputTextIsOperator && !inputIsPeriod) {
 					// see if the workingTextView is empty
 					if (mCurrentWorkingText.length() == 0) {
 						mWorkingTextView.setText(textFromButton);
 						mCurrentWorkingText = textFromButton;
 					} else {
 						// if the working TextView isn't zero we need to append
 						// the
 						// textFromButton to what is already there.
 						mWorkingTextView.setText(mCurrentWorkingText
 								+ textFromButton);
 						mCurrentWorkingText = mWorkingTextView.getText()
 								.toString();
 					}
 				} else if (mCurrentWorkingText.length() == 0
 						&& (!inputIsPeriod || inputTextIsOperator)) {
 					// Do nothing if the text view is empty and the user is
 					// trying to input an operator
 				}
 				// if the button is an operator AND the last inputed button
 				// was an operator, don't allow it to go on the textView
 				else if ((mCurrentWorkingText.endsWith("+")
 						|| mCurrentWorkingText.endsWith("-")
 						|| mCurrentWorkingText.endsWith("x")
 						|| mCurrentWorkingText.endsWith("/") || mCurrentWorkingText
 							.endsWith(".")) && (!inputIsPeriod)) {
 					// Do nothing for this case.
 				} else if (mCurrentWorkingText.endsWith(".") && (inputIsPeriod)) {
 					// Do nothing, because we don't want multiple adjacent
 					// decimal points
 					// in the expression
 				}
 				// otherwise add it to the textView
 				else {
 					// see if the workingTextView is empty
 					if (mCurrentWorkingText.length() == 0) {
 						mWorkingTextView.setText(textFromButton);
 						mCurrentWorkingText = textFromButton;
 					} else {
 						// if the working TextView isn't zero we need to append
 						// the
 						// textFromButton to what is already there.
 						mWorkingTextView.setText(mCurrentWorkingText
 								+ textFromButton);
 						mCurrentWorkingText = mWorkingTextView.getText()
 								.toString();
 					}
 				}
 				onPassData(mCurrentWorkingText);
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
 				onPassData(mCurrentWorkingText);
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
 				// update the Static variable in our activity so we can use it
 				// as a fragment argument
 				mComputeTextView.setText("");
 
 				onPassData(mCurrentWorkingText);
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
 				TextView textView = (TextView) v;
 				String StringFromButton = mWorkingTextView.getText().toString();
 
 				String textFromButton = textView.getText().toString();
 				if (textFromButton.compareTo("=") == 0) {
 					ConvertToPostFix convert = new ConvertToPostFix(
 							StringFromButton);
 					double check = convert.getFinalAnswer();
 					if (check % 1 == 0) {
 						check = convert.getFinalAnswer();
 						int wholeNumberAnswer = (int) check;
 						mComputeTextView.setText("" + wholeNumberAnswer);
 					} else
 						mComputeTextView.setText("" + convert.getFinalAnswer());
 				}
 			}
 		});
 
 		return v;
 	}
 
 	public static Fragment newInstance() {
 		CalculatorDecimalFragment decFrag = new CalculatorDecimalFragment();
 		return decFrag;
 	}
 
 	// method to save the state of the application during the activity life
 	// cycle. This is so we can preserve the values in the textViews upon screen
 	// rotation.
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		// Log.i(TAG, "onSaveInstanceState");
 		outState.putString(KEY_WORKINGTEXTVIEW_STRING, mCurrentWorkingText);
 	}
 
 	// fragment life-cycle method
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		// set our dataPasser interface up when the fragment is on the activity
 		try {
 			// hook the call back up to the activity it is attached to, should
 			// do this in a try/catch because the parent activity must implement
 			// the interface.
 			mCallback = (FragmentDataPasser) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(
 					activity.toString()
 							+ " must implement the FragmentDataPasser interface so we can pass data between the fragments.");
 		}
 	}
 
 	// callback method to send data to the activity so we can then update all
 	// the fragments
 	public void onPassData(String dataToBePassed) {
 		mCallback.onDataPassed(dataToBePassed, VIEW_NUMBER, VIEWS_RADIX);
 	}
 
 	// method to receive the data from the activity/other-fragments and update
 	// the textViews accordingly
 	public void updateWorkingTextView(String dataToBePassed, int base) {
 		if (dataToBePassed.length() != 0) {
 			StringTokenizer toke = new StringTokenizer(dataToBePassed,
 					"x+-/.)(", true);
 			StringBuilder builder = new StringBuilder();
 
 			while (toke.hasMoreElements()) {
 				String aToken = (String) toke.nextElement().toString();
 				if (aToken.equals("+") || aToken.equals("x")
 						|| aToken.equals("-") || aToken.equals("/")
 						|| aToken.equals(".") || aToken.equals("(")
 						|| aToken.equals(")")) {
 
 					builder.append(aToken);
 
 				} else {
 					mCurrentWorkingText = Long.toString(Long.parseLong(aToken,
 							base));
 					builder.append(mCurrentWorkingText);
 				}
 			}
 			mCurrentWorkingText = builder.toString();
 
 			mWorkingTextView.setText(mCurrentWorkingText);
 		} else {
 			// if the data is blank set the textView to nothing
 			mCurrentWorkingText = "";
 			mWorkingTextView.setText(mCurrentWorkingText);
 		}
 	}
 }
