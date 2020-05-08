 package com.ACM.binarycalculator;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.StringTokenizer;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 /**
  * 
  * @author James Van Gaasbeck, ACM at UCF <jjvg@knights.ucf.edu>
  * 
  * 
  */
 public class CalculatorHexFragment extends SherlockFragment {
 	// this is a tag used for debugging purposes
 	// private static final String TAG = "CalculatorHexFragment";
 
 	// string constant for saving our workingTextViewText
 	private static final String KEY_WORKINGTEXTVIEW_STRING = "workingTextString";
 	private static final int VIEW_NUMBER = 1;
 	// the radix number (base-number) to be used when parsing the string.
 	private static final int VIEWS_RADIX = 16;
 
 	// these are our variables
 	// TextView mComputeTextView;
 	TextView mWorkingTextView;
 	private String mCurrentWorkingText;
 	private String mSavedStateString;
 	String mDataFromActivity;
 	FragmentDataPasser mCallback;
 	public static int numberOfOpenParenthesis;
 	public static int numberOfClosedParenthesis;
 	private ArrayList<String> mExpressions;
 
 	@Override
 	// we need to inflate our View so let's grab all the View IDs and inflate
 	// them.
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		// we need to make a view instance from our layout.
 		View v = inflater.inflate(R.layout.fragment_calculator_hex, container,
 				false);
 
 		// get the textViews by id, notice we have to reference them via the
 		// view instance we just created.
 		mWorkingTextView = (TextView) v
 				.findViewById(R.id.fragment_calculator_hex_workingTextView);
 
 		// initialize variables that need to be
 		mCurrentWorkingText = new String("");
 		mSavedStateString = new String("");
 		mExpressions = new ArrayList<String>();
 
 		// if the we saved something away, grab it!
 		if (savedInstanceState != null) {
 			mSavedStateString = savedInstanceState
 					.getString(KEY_WORKINGTEXTVIEW_STRING);
 			// We need to check that we aren't accessing null data or else it
 			// will crash upon turning the screen.
 			if (mSavedStateString == null) {
 				mSavedStateString = new String("");
 			}
 			// set the text to be what we saved away and just now retrieved.
 			mWorkingTextView.setText(mSavedStateString);
 		}
 
 		View.OnClickListener genericNumberButtonListener = new View.OnClickListener() {
 			// when someone clicks a button that isn't "special" we are going to
 			// add it to the workingTextView
 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 				// mCurrentWorkingText = mWorkingTextView.getText().toString();
 				String textFromButton = textView.getText().toString();
 
 				if (mCurrentWorkingText.length() == 0) {
 					mWorkingTextView.setText(mWorkingTextView.getText()
 							.toString().concat(textFromButton));
 					mCurrentWorkingText = textFromButton;
 				} else {
 
 					if (mCurrentWorkingText.length() <= 47) {
 
 						StringTokenizer toke = new StringTokenizer(
 								mCurrentWorkingText.concat(textFromButton),
 								"-+/x)( ");
 						String numberLengthTest = null;
 						while (toke.hasMoreTokens()) {
 							numberLengthTest = (String) toke.nextToken();
 						}
 						if (numberLengthTest.length() > 11) {
 							return;
 						}
 						// if the working TextView isn't zero we need to append
 						// the
 						// textFromButton to what is already there.
 						mWorkingTextView.setText(mWorkingTextView.getText()
 								.toString().concat(textFromButton));
 						mCurrentWorkingText = mCurrentWorkingText
 								.concat(textFromButton);
 					}
 				}
 				mSavedStateString = mWorkingTextView.getText().toString();
 				onPassData(mSavedStateString);
 			}
 		};
 
 		View.OnClickListener genericOperatorButtonListener = new View.OnClickListener() {
 			// when someone clicks an operator "/x+" NOT "-", "-" is special so
 			// it gets it's own listener. We can't have expressions with
 			// adjacent operators "/+x" nor can we start with them.
 			// We also cannot have a "." followed by an operator "+/x"
 			// Nor can we have a "-" followed by an operator.
 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 				// mCurrentWorkingText = mWorkingTextView.getText().toString();
 				String textFromButton = textView.getText().toString();
 				// see if the workingTextView is empty, if so DON'T add the
 				// operator
 				if (mCurrentWorkingText.length() == 0) {
 					// do NOTHING because we can't start an expression with
 					// "+/x" but we can with "-" which is why we are going to
 					// give the minus/negative sign it's own listener.
 				} else {
 
 					if (mCurrentWorkingText.length() <= 47) {
 						// we can't have adjacent "+/x" nor can we have a "."
 						// followed by "+/x"
 						if (mCurrentWorkingText.endsWith("+ ")
 								|| mCurrentWorkingText.endsWith("x ")
 								|| mCurrentWorkingText.endsWith("/ ")
 								|| mCurrentWorkingText.endsWith(".")
 								|| mCurrentWorkingText.endsWith("- ")
 								|| mCurrentWorkingText.endsWith("-")
 								|| mCurrentWorkingText.endsWith("(")) {
 							// do nothing because we can't have multiple
 							// adjacent
 							// operators
 
 						} else {
 							// add it on up!
 							mWorkingTextView.setText(mWorkingTextView.getText()
 									.toString()
 									.concat(" " + textFromButton + " "));
 							mCurrentWorkingText = mCurrentWorkingText
 									.concat(" " + textFromButton + " ");
 						}
 					}
 				}
 				mSavedStateString = mWorkingTextView.getText().toString();
 				onPassData(mSavedStateString);
 			}
 		};
 
 		View.OnClickListener openParenthesisButtonListener = new View.OnClickListener() {
 			// We can't have a "." followed by a "("
 			// We also can't have something like this "6)"
 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 				// mCurrentWorkingText = mWorkingTextView.getText().toString();
 				String textFromButton = textView.getText().toString();
 
 				if (mCurrentWorkingText.length() == 0) {
 					mWorkingTextView.setText(mWorkingTextView.getText()
 							.toString().concat(textFromButton));
 					mCurrentWorkingText = mCurrentWorkingText
 							.concat(textFromButton);
 
 					CalculatorDecimalFragment.numberOfOpenParenthesis++;
 					CalculatorBinaryFragment.numberOfOpenParenthesis++;
 					CalculatorHexFragment.numberOfOpenParenthesis++;
 					CalculatorOctalFragment.numberOfOpenParenthesis++;
 				} else {
 
 					if (mCurrentWorkingText.endsWith(".")) {
 						// do nothing
 					} else {
 						if (mCurrentWorkingText.length() <= 47) {
 
 							mWorkingTextView.setText(mWorkingTextView.getText()
 									.toString().concat(textFromButton));
 							mCurrentWorkingText = mCurrentWorkingText
 									.concat(textFromButton);
 
 							CalculatorDecimalFragment.numberOfOpenParenthesis++;
 							CalculatorBinaryFragment.numberOfOpenParenthesis++;
 							CalculatorHexFragment.numberOfOpenParenthesis++;
 							CalculatorOctalFragment.numberOfOpenParenthesis++;
 						}
 					}
 
 				}
 				mSavedStateString = mWorkingTextView.getText().toString();
 				onPassData(mSavedStateString);
 			}
 		};
 
 		View.OnClickListener closeParenthesisButtonListener = new View.OnClickListener() {
 			// We can't have any of these "./+-x" followed by a ")" nor can we
 			// have something like this "()"
 			// We also can't have something like this "6)" nor something like
 			// "(4x4)9)"
 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 				// mCurrentWorkingText = mWorkingTextView.getText().toString();
 				String textFromButton = textView.getText().toString();
 
 				if (mCurrentWorkingText.length() == 0) {
 					// do nothing we can't start with ")"
 				} else {
 
 					if (mCurrentWorkingText.length() <= 47) {
 						if (((mCurrentWorkingText.endsWith(".")
 								|| mCurrentWorkingText.endsWith("/ ")
 								|| mCurrentWorkingText.endsWith("x ")
 								|| mCurrentWorkingText.endsWith("+ ")
 								|| mCurrentWorkingText.endsWith("- ")
 								|| mCurrentWorkingText.endsWith("-") || mCurrentWorkingText
 									.endsWith("(")))
 								|| numberOfClosedParenthesis >= numberOfOpenParenthesis) {
 							// do nothing
 						} else {
 
 							mWorkingTextView.setText(mWorkingTextView.getText()
 									.toString().concat(textFromButton));
 							mCurrentWorkingText = mCurrentWorkingText
 									.concat(textFromButton);
 
 							CalculatorBinaryFragment.numberOfClosedParenthesis++;
 							CalculatorDecimalFragment.numberOfClosedParenthesis++;
 							CalculatorOctalFragment.numberOfClosedParenthesis++;
 							CalculatorHexFragment.numberOfClosedParenthesis++;
 						}
 					}
 				}
 				mSavedStateString = mWorkingTextView.getText().toString();
 				onPassData(mSavedStateString);
 			}
 		};
 
 		View.OnClickListener genericMinusButtonListener = new View.OnClickListener() {
 			// we can't have more than 2 adjacent "-"
 			// we also can't have something like this ".-3"
 			// No cases like this "--3" BUT we can have "5--3"
 			// No cases like this "(--3)
 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 				// mCurrentWorkingText = mWorkingTextView.getText().toString();
 				String textFromButton = textView.getText().toString();
 				// see if the workingTextView is empty
 				if (mCurrentWorkingText.length() == 0) {
 					mWorkingTextView.setText(mWorkingTextView.getText()
 							.toString().concat(textFromButton));
 					mCurrentWorkingText = textFromButton;
 				} else if (mCurrentWorkingText.length() == 1
 						&& mCurrentWorkingText.endsWith("-")) {
 					// do nothing so we don't start out with something like this
 					// "--2"
 				} else {
 
 					if (mCurrentWorkingText.length() <= 47) {
 						// we can't have more than 2 adjacent '-'. So get the
 						// last
 						// two char's and check if it's "--"
 						if (mCurrentWorkingText.endsWith(".")
 								|| mCurrentWorkingText.endsWith("--")
 								|| mCurrentWorkingText.endsWith("(-")) {
 							// do nothing because we can't have more than 2
 							// adjacent minus's
 						} else {
 							// otherwise, add it to the view
 							if (mCurrentWorkingText.endsWith("0")
 									|| mCurrentWorkingText.endsWith("1")
 									|| mCurrentWorkingText.endsWith("2")
 									|| mCurrentWorkingText.endsWith("3")
 									|| mCurrentWorkingText.endsWith("4")
 									|| mCurrentWorkingText.endsWith("5")
 									|| mCurrentWorkingText.endsWith("6")
 									|| mCurrentWorkingText.endsWith("7")
 									|| mCurrentWorkingText.endsWith("8")
 									|| mCurrentWorkingText.endsWith("9")
 									|| mCurrentWorkingText.endsWith("A")
 									|| mCurrentWorkingText.endsWith("B")
 									|| mCurrentWorkingText.endsWith("C")
 									|| mCurrentWorkingText.endsWith("D")
 									|| mCurrentWorkingText.endsWith("E")
 									|| mCurrentWorkingText.endsWith("F")) {
 								mWorkingTextView.setText(mWorkingTextView
 										.getText().toString()
 										.concat(" " + textFromButton + " "));
 								mCurrentWorkingText = mCurrentWorkingText
 										.concat(" " + textFromButton + " ");
 							} else {
 								mWorkingTextView.setText(mWorkingTextView
 										.getText().toString()
 										.concat(textFromButton));
 								mCurrentWorkingText = mCurrentWorkingText
 										.concat(textFromButton);
 							}
 						}
 					}
 				}
 				// need to pass data to our call back so all fragments can
 				// be
 				// updated with the new workingTextView
 				mSavedStateString = mWorkingTextView.getText().toString();
 				onPassData(mSavedStateString);
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
 				if (mCurrentWorkingText != null) {
 					if (mCurrentWorkingText.length() != 0) {
 
 						if (mCurrentWorkingText.endsWith(")")) {
 							CalculatorDecimalFragment.numberOfClosedParenthesis--;
 							CalculatorBinaryFragment.numberOfClosedParenthesis--;
 							CalculatorHexFragment.numberOfClosedParenthesis--;
 							CalculatorOctalFragment.numberOfClosedParenthesis--;
 						} else if (mCurrentWorkingText.endsWith("(")) {
 							CalculatorDecimalFragment.numberOfOpenParenthesis--;
 							CalculatorBinaryFragment.numberOfOpenParenthesis--;
 							CalculatorHexFragment.numberOfOpenParenthesis--;
 							CalculatorOctalFragment.numberOfOpenParenthesis--;
 						}
 						// we need to delete the spaces around the operators
 						// also, not just the last char added to the
 						// workingTextView
 						if (mCurrentWorkingText.endsWith(" + ")
 								|| mCurrentWorkingText.endsWith(" - ")
 								|| mCurrentWorkingText.endsWith(" x ")
 								|| mCurrentWorkingText.endsWith(" / ")) {
 
 							// this deletes the last three char's
 							mCurrentWorkingText = mCurrentWorkingText
 									.substring(0,
 											mCurrentWorkingText.length() - 3);
 
 							mWorkingTextView.setText(mCurrentWorkingText);
 						} else {
 
 							// if it's not an operator with spaces around it,
 							// just delete the last char
 							mCurrentWorkingText = mCurrentWorkingText
 									.substring(0,
 											mCurrentWorkingText.length() - 1);
 
 							mWorkingTextView
 									.setText(mWorkingTextView
 											.getText()
 											.toString()
 											.substring(
 													0,
 													mWorkingTextView.length() - 1));
 						}
 					}
 				}
 				mSavedStateString = mWorkingTextView.getText().toString();
 				onPassData(mSavedStateString);
 			}
 		};
 
 		// get a reference to our TableLayout XML
 		TableLayout tableLayout = (TableLayout) v
 				.findViewById(R.id.fragment_calculator_hex_tableLayout);
 
 		// get a reference to the first (topmost) row so we can set the clear
 		// all button manually, because it was annoying trying to work it in to
 		// the for loop
 		TableRow firstRow = (TableRow) tableLayout.getChildAt(0);
 		// the clear all button was decided to be the third button in the
 
 		Button open = (Button) firstRow.getChildAt(0);
 		open.setText("(");
 		open.setOnClickListener(openParenthesisButtonListener);
 
 		Button close = (Button) firstRow.getChildAt(1);
 		close.setText(")");
 		close.setOnClickListener(closeParenthesisButtonListener);
 
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
 				mCurrentWorkingText = new String("");
 				mExpressions = new ArrayList<String>();
 				// update the Static variable in our activity so we can use it
 				// as a fragment argument
 				// mComputeTextView.setText("");
 
 				CalculatorDecimalFragment.numberOfOpenParenthesis = 0;
 				CalculatorBinaryFragment.numberOfOpenParenthesis = 0;
 				CalculatorHexFragment.numberOfOpenParenthesis = 0;
 				CalculatorOctalFragment.numberOfOpenParenthesis = 0;
 
 				CalculatorDecimalFragment.numberOfClosedParenthesis = 0;
 				CalculatorBinaryFragment.numberOfClosedParenthesis = 0;
 				CalculatorHexFragment.numberOfClosedParenthesis = 0;
 				CalculatorOctalFragment.numberOfClosedParenthesis = 0;
 
 				mSavedStateString = mWorkingTextView.getText().toString();
 				onPassData(mSavedStateString);
 			}
 
 		});
 
 		ImageButton backspaceButton = (ImageButton) firstRow.getChildAt(3);
 		backspaceButton.setOnClickListener(backspaceButtonListener);
 
 		// get a reference to the second row of the table (AND, OR, NAND)
 		TableRow secondRow = (TableRow) tableLayout.getChildAt(1);
 
 		Button dButton = (Button) secondRow.getChildAt(0);
 		dButton.setText("D");
 		dButton.setOnClickListener(genericNumberButtonListener);
 
 		Button eButton = (Button) secondRow.getChildAt(1);
 		eButton.setText("E");
 		eButton.setOnClickListener(genericNumberButtonListener);
 
 		Button fButton = (Button) secondRow.getChildAt(2);
 		fButton.setText("F");
 		fButton.setOnClickListener(genericNumberButtonListener);
 
 		TableRow thirdRow = (TableRow) tableLayout.getChildAt(2);
 		// the NOR button
 		Button aButton = (Button) thirdRow.getChildAt(0);
 		aButton.setText("A");
 		aButton.setOnClickListener(genericNumberButtonListener);
 		// XOR button
 		Button bButton = (Button) thirdRow.getChildAt(1);
 		bButton.setText("B");
 		bButton.setOnClickListener(genericNumberButtonListener);
 		// XNOR button
 		Button cButton = (Button) thirdRow.getChildAt(2);
 		cButton.setText("C");
 		cButton.setOnClickListener(genericNumberButtonListener);
 
 		TableRow fourthRow = (TableRow) tableLayout.getChildAt(3);
 		// button '1'
 		Button sevenButton = (Button) fourthRow.getChildAt(0);
 		sevenButton.setText("7");
 		sevenButton.setOnClickListener(genericNumberButtonListener);
 		// bitwise shift Left button
 		Button eightButton = (Button) fourthRow.getChildAt(1);
 		eightButton.setText("8");
 		eightButton.setOnClickListener(genericNumberButtonListener);
 		// bitwise shift Right button
 		Button nineButton = (Button) fourthRow.getChildAt(2);
 		nineButton.setText("9");
 		nineButton.setOnClickListener(genericNumberButtonListener);
 
 		Button divideButt = (Button) fourthRow.getChildAt(3);
 		divideButt.setText("/");
 		divideButt.setOnClickListener(genericOperatorButtonListener);
 
 		// now we need to get the last row of buttons and get them to the
 		// screen.
 		TableRow fifthRow = (TableRow) tableLayout.getChildAt(4);
 		// set the decimal button
 		Button fourButton = (Button) fifthRow.getChildAt(0);
 		fourButton.setText("4");
 		fourButton.setOnClickListener(genericNumberButtonListener);
 		// set the zero button
 		Button fiveButton = (Button) fifthRow.getChildAt(1);
 		fiveButton.setText("5");
 		fiveButton.setOnClickListener(genericNumberButtonListener);
 		// set the plus button
 		Button sixButton = (Button) fifthRow.getChildAt(2);
 		sixButton.setText("6");
 		sixButton.setOnClickListener(genericNumberButtonListener);
 		// set the equals button, it will have it's own separate listener to
 		// compute the inputed value
 
 		Button multButt = (Button) fifthRow.getChildAt(3);
 		multButt.setText("x");
 		multButt.setOnClickListener(genericOperatorButtonListener);
 
 		TableRow sixthRow = (TableRow) tableLayout.getChildAt(5);
 
 		Button oneButton = (Button) sixthRow.getChildAt(0);
 		oneButton.setText("1");
 		oneButton.setOnClickListener(genericNumberButtonListener);
 
 		Button twoButton = (Button) sixthRow.getChildAt(1);
 		twoButton.setText("2");
 		twoButton.setOnClickListener(genericNumberButtonListener);
 
 		Button threeButton = (Button) sixthRow.getChildAt(2);
 		threeButton.setText("3");
 		threeButton.setOnClickListener(genericNumberButtonListener);
 
 		Button minusButt = (Button) sixthRow.getChildAt(3);
 		minusButt.setText("-");
 		minusButt.setOnClickListener(genericMinusButtonListener);
 
 		TableRow lastRow = (TableRow) tableLayout.getChildAt(6);
 
 		Button equalsButton = (Button) lastRow.getChildAt(0);
 		equalsButton.setText("=");
 		equalsButton.setOnClickListener(new OnClickListener() {
 			// EQUALS button on click listener
 			@Override
 			public void onClick(View v) {
 				// Do arithmetic
 
 				// Now we need to display the answer on a completely new line
 				// store the answer in a variable then add that variable to the
 				// textView and add a new line because the next expression will
 				// start on a newline, also add the answer to the 'mExpressions"
 				// list with the newLine characters
 				mExpressions.add(mCurrentWorkingText);
 
 				String fourtyTwo = Integer.toHexString(42);
 				// 42 is obviously not the real answer, just a place holder to
 				// display
 				// how the fully functioning app should work. The real computed
 				// answer should be inserted in it's place
 				String answer = "\n" + fourtyTwo + "\n";

 				mExpressions.add(answer);
 				mWorkingTextView.setText(mWorkingTextView.getText().toString()
 						.concat(answer));
 				mSavedStateString = mWorkingTextView.getText().toString();

 				onPassData(mSavedStateString);

 				mCurrentWorkingText = new String("");
 			}
 		});
 
 		Button zeroButton = (Button) lastRow.getChildAt(1);
 		zeroButton.setText("0");
 		zeroButton.setOnClickListener(genericNumberButtonListener);
 
 		Button decimalPointButton = (Button) lastRow.getChildAt(2);
 		decimalPointButton.setText(".");
 		decimalPointButton.setOnClickListener(new OnClickListener() {
 			// we can't put a "." up there if there has already been one in
 			// the current token (number)
 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 				// mCurrentWorkingText = mWorkingTextView.getText().toString();
 				String textFromButton = textView.getText().toString();
 
 				// see if the workingTextView is empty, if so just add the '.'
 				if (mCurrentWorkingText.length() == 0) {
 
 					mWorkingTextView.setText(mWorkingTextView.getText()
 							.toString().concat(textFromButton));
 					mCurrentWorkingText = textFromButton;
 
 				} else {
 
 					if (mCurrentWorkingText.length() <= 47) {
 						StringTokenizer toke = new StringTokenizer(
 								mCurrentWorkingText, "+-/x)(", true);
 						String currentElement = null;
 						// get the current(last) token(number) so we can test if
 						// it
 						// has a '.' in it.
 						while (toke.hasMoreTokens()) {
 							currentElement = toke.nextElement().toString();
 						}
 						// if the working TextView isn't zero we need to append
 						// the
 						// textFromButton to what is already there. AND we need
 						// to
 						// check if the current token already has a '.' in it
 						// because we can't have something like '2..2' or
 						// 2.2.33'
 						if (mCurrentWorkingText.endsWith(".")
 								|| currentElement.contains(".")) {
 							// do nothing here so we don't end up with
 							// expressions
 							// like "2..2" or "2.3.22"
 						} else {
 							// otherwise we're all good and just add the ".' up
 							// there.
 							mWorkingTextView.setText(mWorkingTextView.getText()
 									.toString().concat(textFromButton));
 							mCurrentWorkingText = mCurrentWorkingText
 									.concat(textFromButton);
 						}
 					}
 
 				}
 				mSavedStateString = mWorkingTextView.getText().toString();
 				onPassData(mSavedStateString);
 			}
 		});
 
 		Button plusButt = (Button) lastRow.getChildAt(3);
 		plusButt.setText("+");
 		plusButt.setOnClickListener(genericOperatorButtonListener);
 
 		return v;
 	}
 
 	public static SherlockFragment newInstance() {
 		CalculatorHexFragment binFrag = new CalculatorHexFragment();
 		return binFrag;
 	}
 
 	// method to save the state of the application during the activity life
 	// cycle. This is so we can preserve the values in the textViews upon screen
 	// rotation.
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		// Log.i(TAG, "onSaveInstanceState");
 		outState.putString(KEY_WORKINGTEXTVIEW_STRING, mSavedStateString);
 	}
 
 	// need to make sure the fragment life cycle complies with the
 	// actionBarSherlock support library
 	@Override
 	public void onAttach(Activity activity) {
 		if (!(activity instanceof SherlockFragmentActivity)) {
 			throw new IllegalStateException(getClass().getSimpleName()
 					+ " must be attached to a SherlockFragmentActivity.");
 		}
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
 					"x+-/)( \n", true);
 			StringBuilder builder = new StringBuilder();
 
 			while (toke.hasMoreElements()) {
 				String aToken = (String) toke.nextElement().toString();
 				if (aToken.equals("+") || aToken.equals("x")
 						|| aToken.equals("-") || aToken.equals("/")
 						|| aToken.equals("(") || aToken.equals(")")
 						|| aToken.equals(" ") || aToken.equals("\n")) {
 
 					builder.append(aToken);
 
 				}
 				// if our token contains a "." in it then that means that we
 				// need to do some conversion trickery
 				else if (aToken.contains(".")) {
 					if (aToken.endsWith(".")) {
 						// don't do any conversions when the number is still
 						// being
 						// inputed and in the current state of something like
 						// this
 						// "5."
 						return;
 					}
 					// split the string around the "." delimiter.
 					String[] parts = aToken.split("\\.");
 					StringBuilder tempBuilder = new StringBuilder();
 
 					if (aToken.charAt(0) == '.') {
 
 					} else {
 
 						// add the portion of the number to the left of the
 						// "."
 						// to our string this doesn't need any conversion
 						// nonsense.
 						tempBuilder.append(Integer.toHexString(Integer
 								.parseInt(parts[0], base)));
 					}
 					// convert the fraction portion
 					String getRidOfZeroBeforePoint = null;
 
 					if (base == 10) {
 						String fractionWithRadixPoint = "." + parts[1];
 						String converted = Fractions
 								.convertFractionPortionFromDecimal(
 										fractionWithRadixPoint, VIEWS_RADIX);
 						parts = converted.split("\\.");
 						tempBuilder.append(".").append(parts[0]);
 					} else {
 
 						getRidOfZeroBeforePoint = Fractions
 								.convertFractionPortionToDecimal(parts[1], base);
 
 						// the conversion returns just the fraction
 						// portion
 						// with
 						// a "0" to the left of the ".", so let's get
 						// rid of
 						// that extra zero.
 						getRidOfZeroBeforePoint = getRidOfZeroBeforePoint
 								.substring(1, getRidOfZeroBeforePoint.length());
 						String partsAgain[] = getRidOfZeroBeforePoint
 								.split("\\.");
 
 						String converted = Fractions
 								.convertFractionPortionFromDecimal(
 										getRidOfZeroBeforePoint, VIEWS_RADIX);
 						partsAgain = converted.split("\\.");
 						tempBuilder.append(".").append(partsAgain[0]);
 					}
 
 					// add that to the string that gets put on the textView
 					// (this may be excessive) (I wrote this late at night
 					// so stuff probably got a little weird)
 					builder.append(tempBuilder.toString());
 
 				} else {
 					BigInteger sizeTestBigInt = new BigInteger(aToken, base);
 					if (sizeTestBigInt.bitLength() < 64) {
 						mCurrentWorkingText = Long.toHexString(Long.parseLong(
 								aToken, base));
 						builder.append(mCurrentWorkingText);
 					}
 				}

				String[] dontUpperCaseX = builder.toString().split("x");
				StringBuilder safeUpperCase = new StringBuilder();
				for (int i = 0; i < dontUpperCaseX.length; i++) {
					if (i != dontUpperCaseX.length - 1) {
						safeUpperCase.append(
								dontUpperCaseX[i].toUpperCase(Locale
										.getDefault())).append("x");
					} else {
						safeUpperCase.append(dontUpperCaseX[i]
								.toUpperCase(Locale.getDefault()));
					}
				}
				mCurrentWorkingText = safeUpperCase.toString();
 
 				mWorkingTextView.setText(mCurrentWorkingText);
 				mSavedStateString = mWorkingTextView.getText().toString();
 			}
 		} else {
 			mCurrentWorkingText = "";
 			mWorkingTextView.setText(mCurrentWorkingText);
 			mSavedStateString = mWorkingTextView.getText().toString();
 		}
 	}
 
 }
