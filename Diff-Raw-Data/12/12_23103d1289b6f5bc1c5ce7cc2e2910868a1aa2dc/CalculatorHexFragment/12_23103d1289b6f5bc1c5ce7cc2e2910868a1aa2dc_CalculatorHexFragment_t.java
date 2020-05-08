 package com.ACM.binarycalculator.Fragments;
 
 import java.math.BigInteger;
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
 import android.widget.Toast;
 
 import com.ACM.binarycalculator.R;
 import com.ACM.binarycalculator.DataModels.ExpressionHouse;
 import com.ACM.binarycalculator.Interfaces.FragmentDataPasser;
 import com.ACM.binarycalculator.Utilities.Fractions;
 import com.ACM.binarycalculator.Utilities.InfixToPostfix;
 import com.ACM.binarycalculator.Utilities.PostfixEvaluator;
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
 	private static final String KEY_VIEW_NUMBER = "com.ACM.binarycalculator.Fragments.Hex.ViewNumber";
 	private static final String KEY_RADIX = "com.ACM.binarycalculator.Fragments.Hex.Radix";
 	// private static final int VIEW_NUMBER = 1;
 	// the radix number (base-number) to be used when parsing the string.
 	// private static final int VIEWS_RADIX = 16;
 
 	// these are our variables
 	private TextView mWorkingTextView;
 	/*
 	 * The mCurrentWorkingText string variable is the current expression, not
 	 * the entire list.
 	 */
 	private StringBuilder mCurrentWorkingText;
 
 	/*
 	 * mExpressins is the list of all the expressions
 	 */
 	private ExpressionHouse mExpressions;
 	String mDataFromActivity;
 	FragmentDataPasser mCallback;
 	private int positionInPager;
 	private static int viewsRadix;
 	public static int numberOfOpenParenthesis;
 	public static int numberOfClosedParenthesis;
 	public static int numberOfOperators;
 	View.OnClickListener genericHexNumberButtonListener;
 
 	@Override
 	// we need to inflate our View so let's grab all the View IDs and inflate
 	// them.
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		// we need to make a view instance from our layout.
 		View v = inflater.inflate(R.layout.fragment_calculator_hex, container,
 				false);
 
 		positionInPager = getArguments().getInt(KEY_VIEW_NUMBER);
 		viewsRadix = getArguments().getInt(KEY_RADIX);
 
 		// get the textViews by id, notice we have to reference them via the
 		// view instance we just created.
 		mWorkingTextView = (TextView) v
 				.findViewById(R.id.fragment_calculator_hex_workingTextView);
 
 		// initialize variables that need to be
 		mCurrentWorkingText = new StringBuilder("");
 		mExpressions = new ExpressionHouse();
 
 		// if the we saved something away, grab it!
 		if (savedInstanceState != null) {
 			mExpressions = (ExpressionHouse) savedInstanceState
 					.getStringArrayList(KEY_WORKINGTEXTVIEW_STRING);
 
 			// capitalize all of the Hex letter except for the 'x' that is used
 			// as an operand
 			StringBuilder build = new StringBuilder();
 			String listContents = mExpressions.printAllExpressions();
 			for (int i = 0; i < listContents.length(); i++) {
 				Character test = listContents.charAt(i);
 				if (test.equals('x'))
 					build.append(test);
 				else
 					build.append(Character.toUpperCase(test));
 			}
 
 			// set the text to be what we saved away and just now retrieved.
 			mWorkingTextView.setText(build.toString());
 			mCurrentWorkingText.append(mExpressions.getCurrentExpression());
 		}
 
 		genericHexNumberButtonListener = new View.OnClickListener() {
 			// when someone clicks a button that isn't "special" we are going to
 			// add it to the workingTextView
 			@Override
 			public void onClick(View v) {
 
 				TextView textView = (TextView) v;
 				CharSequence textFromButton = textView.getText();
 
 				StringBuilder textViewBuilder = new StringBuilder(
 						mWorkingTextView.getText());
 				// StringBuilder currentWorkingTextBuilder = new
 				// StringBuilder(
 				// mCurrentWorkingText);
 
 				if (mCurrentWorkingText.length() <= 47) {
 					String tokeString = mCurrentWorkingText.toString()
 							+ textFromButton.toString();
 					StringTokenizer toke = new StringTokenizer(tokeString,
 							"-+/x)( ");
 					String numberLengthTest = null;
 					while (toke.hasMoreTokens()) {
 						numberLengthTest = (String) toke.nextToken().toString();
 					}
 					if (numberLengthTest.length() > 11) {
 						return;
 					}
 
 					// Log.d(TAG,
 					// "**TextView before: "
 					// + mWorkingTextView.getText().toString()
 					// + " CurrentWorkingText: "
 					// + mCurrentWorkingText.toString()
 					// + " TxtFromButton: " + textFromButton
 					// + " **");
 
 					CharSequence newTextViewText = (CharSequence) textViewBuilder
 							.append(textFromButton);
 					mWorkingTextView.setText(newTextViewText);
 					mCurrentWorkingText.append(textFromButton);
 
 					// Log.d(TAG,
 					// "**TextView After: "
 					// + mWorkingTextView.getText().toString()
 					// + " CurrentWorkingText: "
 					// + mCurrentWorkingText.toString());
 
 				}
 
 				onPassData(mCurrentWorkingText.toString(), false);
 				mExpressions.updateExpressions(mCurrentWorkingText.toString());
 
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
 				CharSequence textFromButton = textView.getText();
 
 				StringBuilder textViewBuilder = new StringBuilder(
 						mWorkingTextView.getText());
 
 				// see if the workingTextView is empty, if so DON'T add the
 				// operator
 				if (mCurrentWorkingText.length() == 0) {
 					// do NOTHING because we can't start an expression with
 					// "+/x" but we can with "-" which is why we are going to
 					// give the minus/negative sign it's own listener.
 				} else {
 
 					if (mCurrentWorkingText.toString().length() <= 47) {
 						// we can't have adjacent "+/x" nor can we have a "."
 						// followed by "+/x"
 						if (mCurrentWorkingText.toString().endsWith("+ ")
 								|| mCurrentWorkingText.toString()
 										.endsWith("x ")
 								|| mCurrentWorkingText.toString()
 										.endsWith("/ ")
 								|| mCurrentWorkingText.toString().endsWith(".")
 								|| mCurrentWorkingText.toString()
 										.endsWith("- ")
 								|| mCurrentWorkingText.toString().endsWith("-")
 								|| mCurrentWorkingText.toString()
 										.endsWith("( ")
 								|| mCurrentWorkingText.toString().contains("O")
 								|| mCurrentWorkingText.toString().contains("N")) {
 							// do nothing because we can't have multiple
 							// adjacent
 							// operators
 
 						} else {
 							// we're safe to add the operator to the expression
 
 							if (mCurrentWorkingText.toString().endsWith(" ")) {
 								// if the last char in the currentExpression was
 								// a space then don't add the space at the
 								// beginning, because there will be an extra
 								// space there making it look weird and mess up
 								// the calculations.
 
 								CharSequence newTextViewText = (CharSequence) textViewBuilder
 										.append(textFromButton).append(" ");
 
 								mWorkingTextView.setText(newTextViewText);
 								mCurrentWorkingText.append(textFromButton)
 										.append(" ");
 
 								// CalculatorDecimalFragment.numberOfOperators++;
 								// CalculatorOctalFragment.numberOfOperators++;
 								// CalculatorBinaryFragment.numberOfOperators++;
 								// CalculatorHexFragment.numberOfOperators++;
 
 							} else {
 
 								CharSequence newTextViewText = (CharSequence) textViewBuilder
 										.append(" ").append(textFromButton)
 										.append(" ");
 
 								mWorkingTextView.setText(newTextViewText);
 
 								mCurrentWorkingText.append(" ")
 										.append(textFromButton).append(" ");
 
 								// CalculatorDecimalFragment.numberOfOperators++;
 								// CalculatorOctalFragment.numberOfOperators++;
 								// CalculatorBinaryFragment.numberOfOperators++;
 								// CalculatorHexFragment.numberOfOperators++;
 							}
 						}
 					}
 				}
 				// Log.d(TAG, "**Operator, number of operators: "
 				// + numberOfOperators);
 				onPassData(mCurrentWorkingText.toString(), false);
 				mExpressions.updateExpressions(mCurrentWorkingText.toString());
 			}
 		};
 
 		View.OnClickListener openParenthesisButtonListener = new View.OnClickListener() {
 			// We can't have a "." followed by a "("
 			// We also can't have something like this "6)"
 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 
 				CharSequence textFromButton = textView.getText();
 				CharSequence newTextViewText = null;
 
 				StringBuilder textViewBuilder = new StringBuilder(
 						mWorkingTextView.getText());
 
 				if (mCurrentWorkingText.toString().length() == 0) {
 					// if the first thing is a "(" then don't add the
 					// unnecessary space at the front of it.
 					newTextViewText = (CharSequence) textViewBuilder.append(
 							textFromButton).append(" ");
 
 					mWorkingTextView.setText(newTextViewText);
 					mCurrentWorkingText.append(textFromButton).append(" ");
 
 					CalculatorDecimalFragment.numberOfOpenParenthesis++;
 					CalculatorBinaryFragment.numberOfOpenParenthesis++;
 					CalculatorHexFragment.numberOfOpenParenthesis++;
 					CalculatorOctalFragment.numberOfOpenParenthesis++;
 				} else {
 
 					if (mCurrentWorkingText.toString().endsWith(".")
 							|| mCurrentWorkingText.toString().endsWith("D ")
 							|| mCurrentWorkingText.toString().endsWith("R ")) {
 						// do nothing
 					} else {
 						if (mCurrentWorkingText.length() <= 47) {
 
 							// add an implied 'x' behind the scenes for cases
 							// like this "4 ( 4 )"
 							if (mCurrentWorkingText.length() > 0) {
 								Character isAnumberTest = mCurrentWorkingText
 										.charAt(mCurrentWorkingText.length() - 1);
 								if (isOperand(isAnumberTest.toString())
 										|| mCurrentWorkingText.toString()
 												.endsWith(") ")) {
 
 									if (mCurrentWorkingText.toString()
 											.endsWith(") ")) {
 
 										newTextViewText = (CharSequence) textViewBuilder
 												.append(" ")
 												.append(textFromButton)
 												.append(" ");
 
 										mWorkingTextView
 												.setText(newTextViewText);
 										mCurrentWorkingText.append(" ")
 												.append(textFromButton)
 												.append(" ");
 
 									} else {
 
 										newTextViewText = (CharSequence) textViewBuilder
 												.append(" ")
 												.append(textFromButton)
 												.append(" ");
 
 										mWorkingTextView
 												.setText(newTextViewText);
 										mCurrentWorkingText.append(" ")
 												.append(textFromButton)
 												.append(" ");
 									}
 
 									// CalculatorDecimalFragment.numberOfOperators++;
 									// CalculatorBinaryFragment.numberOfOperators++;
 									// CalculatorHexFragment.numberOfOperators++;
 									// CalculatorOctalFragment.numberOfOperators++;
 								} else {
 
 									newTextViewText = (CharSequence) textViewBuilder
 											.append(textFromButton).append(" ");
 
 									mWorkingTextView.setText(newTextViewText);
 									mCurrentWorkingText.append(textFromButton)
 											.append(" ");
 								}
 							} else {
 								newTextViewText = (CharSequence) textViewBuilder
 										.append(textFromButton).append(" ");
 
 								mWorkingTextView.setText(newTextViewText);
 								mCurrentWorkingText.append(textFromButton)
 										.append(" ");
 							}
 
 							CalculatorDecimalFragment.numberOfOpenParenthesis++;
 							CalculatorBinaryFragment.numberOfOpenParenthesis++;
 							CalculatorHexFragment.numberOfOpenParenthesis++;
 							CalculatorOctalFragment.numberOfOpenParenthesis++;
 						}
 					}
 
 				}
 				// Log.d(TAG, "**OpenParenthesis, number of operators: "
 				// + numberOfOperators);
 				onPassData(mCurrentWorkingText.toString(), false);
 				mExpressions.updateExpressions(mCurrentWorkingText.toString());
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
 
 				CharSequence textFromButton = textView.getText();
 				CharSequence newTextViewText = null;
 
 				StringBuilder textViewBuilder = new StringBuilder(
 						mWorkingTextView.getText());
 
 				if (mCurrentWorkingText.toString().length() == 0) {
 					// do nothing we can't start with ")"
 				} else {
 
 					if (mCurrentWorkingText.length() <= 47) {
 						if (((mCurrentWorkingText.toString().endsWith(".")
 								|| mCurrentWorkingText.toString()
 										.endsWith("/ ")
 								|| mCurrentWorkingText.toString()
 										.endsWith("x ")
 								|| mCurrentWorkingText.toString()
 										.endsWith("+ ")
 								|| mCurrentWorkingText.toString()
 										.endsWith("- ")
 								|| mCurrentWorkingText.toString().endsWith("-") || mCurrentWorkingText
 								.toString().endsWith("( ")))
 								|| numberOfClosedParenthesis >= numberOfOpenParenthesis) {
 							// do nothing
 						} else {
 
 							newTextViewText = (CharSequence) textViewBuilder
 									.append(" ").append(textFromButton)
 									.append(" ");
 
 							mWorkingTextView.setText(newTextViewText);
 							mCurrentWorkingText.append(" ")
 									.append(textFromButton).append(" ");
 
 							CalculatorBinaryFragment.numberOfClosedParenthesis++;
 							CalculatorDecimalFragment.numberOfClosedParenthesis++;
 							CalculatorOctalFragment.numberOfClosedParenthesis++;
 							CalculatorHexFragment.numberOfClosedParenthesis++;
 						}
 					}
 				}
 				// Log.d(TAG, "**ClosedParenthesis, number of operators: "
 				// + numberOfOperators);
 				onPassData(mCurrentWorkingText.toString(), false);
 				mExpressions.updateExpressions(mCurrentWorkingText.toString());
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
 
 				CharSequence textFromButton = textView.getText();
 				CharSequence newTextViewText = null;
 
 				StringBuilder textViewBuilder = new StringBuilder(
 						mWorkingTextView.getText());
 
 				// see if the workingTextView is empty
 				if (mCurrentWorkingText.toString().length() == 0) {
 
 					newTextViewText = (CharSequence) textViewBuilder
 							.append(textFromButton);
 
 					mWorkingTextView.setText(newTextViewText);
 					mCurrentWorkingText.append(textFromButton);
 
 				} else if (mCurrentWorkingText.length() == 1
 						&& mCurrentWorkingText.toString().endsWith("-")) {
 					// do nothing so we don't start out with something like this
 					// "--2"
 				} else {
 
 					if (mCurrentWorkingText.length() <= 47) {
 						// we can't have more than 2 adjacent '-'. So get the
 						// last
 						// two char's and check if it's "--"
 						if (mCurrentWorkingText.toString().endsWith(".")
 								|| mCurrentWorkingText.toString()
 										.endsWith("--")
 								|| mCurrentWorkingText.toString()
 										.endsWith("(-")
 								|| mCurrentWorkingText.toString().contains("O")
 								|| mCurrentWorkingText.toString().contains("N")) {
 							// do nothing because we can't have more than 2
 							// adjacent minus's
 						} else {
 							// otherwise, add it to the view
 							if (mCurrentWorkingText.toString().endsWith("0")
 									|| mCurrentWorkingText.toString().endsWith(
 											"1")
 									|| mCurrentWorkingText.toString().endsWith(
 											"2")
 									|| mCurrentWorkingText.toString().endsWith(
 											"3")
 									|| mCurrentWorkingText.toString().endsWith(
 											"4")
 									|| mCurrentWorkingText.toString().endsWith(
 											"5")
 									|| mCurrentWorkingText.toString().endsWith(
 											"6")
 									|| mCurrentWorkingText.toString().endsWith(
 											"7")
 									|| mCurrentWorkingText.toString().endsWith(
 											"8")
 									|| mCurrentWorkingText.toString().endsWith(
 											"9")
 									|| mCurrentWorkingText.toString().endsWith(
											"A")
									|| mCurrentWorkingText.toString().endsWith(
											"B")
									|| mCurrentWorkingText.toString().endsWith(
											"C")
									|| mCurrentWorkingText.toString().endsWith(
											"D")
									|| mCurrentWorkingText.toString().endsWith(
											"E")
									|| mCurrentWorkingText.toString().endsWith(
											"F")
									|| mCurrentWorkingText.toString().endsWith(
 											") ")) {
 
 								// if the last thing was a parenthesis make sure
 								// that we don't add in an extraneous space.
 								if (mCurrentWorkingText.toString().endsWith(
 										") ")) {
 
 									newTextViewText = (CharSequence) textViewBuilder
 											.append(textFromButton).append(" ");
 
 									mWorkingTextView.setText(newTextViewText);
 									mCurrentWorkingText.append(textFromButton)
 											.append(" ");
 
 								} else {
 
 									newTextViewText = (CharSequence) textViewBuilder
 											.append(" ").append(textFromButton)
 											.append(" ");
 
 									mWorkingTextView.setText(newTextViewText);
 									mCurrentWorkingText.append(" ")
 											.append(textFromButton).append(" ");
 								}
 							} else {
 								// this represents a negative sign, not a minus
 								// sign
 
 								newTextViewText = (CharSequence) textViewBuilder
 										.append(textFromButton);
 
 								mWorkingTextView.setText(newTextViewText);
 								mCurrentWorkingText.append(textFromButton);
 							}
 						}
 					}
 				}
 				// need to pass data to our call back so all fragments can
 				// be
 				// updated with the new workingTextView
 				// Log.d(TAG, "**Negative/Minus, number of operators: "
 				// + numberOfOperators);
 				onPassData(mCurrentWorkingText.toString(), false);
 				mExpressions.updateExpressions(mCurrentWorkingText.toString());
 			}
 		};
 
 		// logic in here is pretty messy because there are a lot of cases to
 		// check for
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
 
 						if (mCurrentWorkingText.toString().endsWith(") ")) {
 							CalculatorDecimalFragment.numberOfClosedParenthesis--;
 							CalculatorBinaryFragment.numberOfClosedParenthesis--;
 							CalculatorHexFragment.numberOfClosedParenthesis--;
 							CalculatorOctalFragment.numberOfClosedParenthesis--;
 						} else if (mCurrentWorkingText.toString().endsWith("(")
 								|| mCurrentWorkingText.toString()
 										.endsWith("( ")
 								|| mCurrentWorkingText.toString().endsWith(
 										" ( ")) {
 							CalculatorDecimalFragment.numberOfOpenParenthesis--;
 							CalculatorBinaryFragment.numberOfOpenParenthesis--;
 							CalculatorHexFragment.numberOfOpenParenthesis--;
 							CalculatorOctalFragment.numberOfOpenParenthesis--;
 						}
 
 						if (mCurrentWorkingText.toString().endsWith(" + ( ")
 								|| mCurrentWorkingText.toString().endsWith(
 										" - ( ")
 								|| mCurrentWorkingText.toString().endsWith(
 										" x ( ")
 								|| mCurrentWorkingText.toString().endsWith(
 										" / ( ")) {
 
 							// this deletes the last 2 char's
 							mCurrentWorkingText.delete(
 									mCurrentWorkingText.length() - 2,
 									mCurrentWorkingText.length());
 
 							mWorkingTextView
 									.setText(mWorkingTextView
 											.getText()
 											.toString()
 											.substring(
 													0,
 													mWorkingTextView.length() - 2));
 						}
 
 						else if (mCurrentWorkingText.toString().endsWith(" ( ")) {
 
 							// this deletes the last 3 char's
 							mCurrentWorkingText.delete(
 									mCurrentWorkingText.length() - 3,
 									mCurrentWorkingText.length());
 
 							mWorkingTextView
 									.setText(mWorkingTextView
 											.getText()
 											.toString()
 											.substring(
 													0,
 													mWorkingTextView.length() - 3));
 
 						}
 
 						else if (mCurrentWorkingText.toString().endsWith(
 								" AND ")
 								|| mCurrentWorkingText.toString().endsWith(
 										" NOR ")
 								|| mCurrentWorkingText.toString().endsWith(
 										" XOR ")) {
 
 							// this deletes the bitwise operation and spaces
 							mCurrentWorkingText.delete(
 									mCurrentWorkingText.length() - 5,
 									mCurrentWorkingText.length());
 
 							mWorkingTextView
 									.setText(mWorkingTextView
 											.getText()
 											.toString()
 											.substring(
 													0,
 													mWorkingTextView.length() - 5));
 						}
 
 						else if (mCurrentWorkingText.toString().endsWith(
 								" NAND ")
 								|| mCurrentWorkingText.toString().endsWith(
 										" XNOR ")) {
 
 							// this deletes the bitwise operation and spaces
 							mCurrentWorkingText.delete(
 									mCurrentWorkingText.length() - 6,
 									mCurrentWorkingText.length());
 
 							mWorkingTextView
 									.setText(mWorkingTextView
 											.getText()
 											.toString()
 											.substring(
 													0,
 													mWorkingTextView.length() - 6));
 						}
 
 						else if (mCurrentWorkingText.toString()
 								.endsWith(" OR ")) {
 
 							// this deletes the bitwise operation and spaces
 							mCurrentWorkingText.delete(
 									mCurrentWorkingText.length() - 4,
 									mCurrentWorkingText.length());
 
 							mWorkingTextView
 									.setText(mWorkingTextView
 											.getText()
 											.toString()
 											.substring(
 													0,
 													mWorkingTextView.length() - 4));
 						}
 
 						// we need to delete the spaces around the operators
 						// also, not just the last char added to the
 						// workingTextView
 						else if (mCurrentWorkingText.toString().endsWith(" + ")
 								|| mCurrentWorkingText.toString().endsWith(
 										" - ")
 								|| mCurrentWorkingText.toString().endsWith(
 										" x ")
 								|| mCurrentWorkingText.toString().endsWith(
 										" / ")
 								|| mCurrentWorkingText.toString()
 										.endsWith(") ")
 								|| mCurrentWorkingText.toString().endsWith(
 										" ( ")) {
 
 							if (mCurrentWorkingText.toString().endsWith(") x ")
 									|| mCurrentWorkingText.toString().endsWith(
 											") + ")
 									|| mCurrentWorkingText.toString().endsWith(
 											") - ")
 									|| mCurrentWorkingText.toString().endsWith(
 											") / ")) {
 
 								mCurrentWorkingText.delete(
 										mCurrentWorkingText.length() - 2,
 										mCurrentWorkingText.length());
 
 								mWorkingTextView.setText(mWorkingTextView
 										.getText()
 										.toString()
 										.substring(0,
 												mWorkingTextView.length() - 2));
 							} else {
 
 								// this deletes the last three char's
 								mCurrentWorkingText.delete(
 										mCurrentWorkingText.length() - 3,
 										mCurrentWorkingText.length());
 
 								mWorkingTextView.setText(mWorkingTextView
 										.getText()
 										.toString()
 										.substring(0,
 												mWorkingTextView.length() - 3));
 							}
 
 						} else if (mCurrentWorkingText.toString()
 								.endsWith("( ")) {
 							// only delete two chars if the user started
 							// with an
 							// open parenthesis
 							mCurrentWorkingText.delete(
 									mCurrentWorkingText.length() - 2,
 									mCurrentWorkingText.length());
 
 							mWorkingTextView
 									.setText(mWorkingTextView
 											.getText()
 											.toString()
 											.substring(
 													0,
 													mWorkingTextView.length() - 2));
 
 						} else {
 
 							mCurrentWorkingText.delete(
 									mCurrentWorkingText.length() - 1,
 									mCurrentWorkingText.length());
 
 							mWorkingTextView
 									.setText(mWorkingTextView
 											.getText()
 											.toString()
 											.substring(
 													0,
 													mWorkingTextView.length() - 1));
 						}
 					} else {
 						return;
 					}
 				}
 				// Log.d(TAG, "**Backspace, number of operators: "
 				// + numberOfOperators);
 				onPassData(mCurrentWorkingText.toString(), true);
 				mExpressions.updateExpressions(mCurrentWorkingText.toString());
 
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
 		clearAllButton.setText("AC");
 		clearAllButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// clear all the text in the working textView, AND maybe the
 				// computed textView as well?
 				// Also, might want to clear out the post fix expression stack
 				mWorkingTextView.setText("");
 				mCurrentWorkingText = new StringBuilder("");
 				mExpressions.clearAllExpressions();
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
 
 				CalculatorDecimalFragment.numberOfOperators = 0;
 				CalculatorBinaryFragment.numberOfOperators = 0;
 				CalculatorHexFragment.numberOfOperators = 0;
 				CalculatorOctalFragment.numberOfOperators = 0;
 
 				onPassData(mCurrentWorkingText.toString(), false);
 				mExpressions.updateExpressions(mCallback.toString());
 			}
 
 		});
 
 		ImageButton backspaceButton = (ImageButton) firstRow.getChildAt(3);
 		backspaceButton.setOnClickListener(backspaceButtonListener);
 
 		TableRow secondRow = (TableRow) tableLayout.getChildAt(1);
 
 		Button dButton = (Button) secondRow.getChildAt(0);
 		dButton.setText("D");
 		dButton.setOnClickListener(genericHexNumberButtonListener);
 
 		Button eButton = (Button) secondRow.getChildAt(1);
 		eButton.setText("E");
 		eButton.setOnClickListener(genericHexNumberButtonListener);
 
 		Button fButton = (Button) secondRow.getChildAt(2);
 		fButton.setText("F");
 		fButton.setOnClickListener(genericHexNumberButtonListener);
 
 		Button blankButton1 = (Button) secondRow.getChildAt(3);
 		blankButton1.setText("");
 		blankButton1.setOnClickListener(null);
 
 		TableRow thirdRow = (TableRow) tableLayout.getChildAt(2);
 		// the NOR button
 		Button aButton = (Button) thirdRow.getChildAt(0);
 		aButton.setText("A");
 		aButton.setOnClickListener(genericHexNumberButtonListener);
 		// XOR button
 		Button bButton = (Button) thirdRow.getChildAt(1);
 		bButton.setText("B");
 		bButton.setOnClickListener(genericHexNumberButtonListener);
 		// XNOR button
 		Button cButton = (Button) thirdRow.getChildAt(2);
 		cButton.setText("C");
 		cButton.setOnClickListener(genericHexNumberButtonListener);
 
 		Button blankButton2 = (Button) thirdRow.getChildAt(3);
 		blankButton2.setText("");
 		blankButton2.setOnClickListener(null);
 
 		TableRow fourthRow = (TableRow) tableLayout.getChildAt(3);
 		// button '1'
 		Button sevenButton = (Button) fourthRow.getChildAt(0);
 		sevenButton.setText("7");
 		sevenButton.setOnClickListener(genericHexNumberButtonListener);
 		// bitwise shift Left button
 		Button eightButton = (Button) fourthRow.getChildAt(1);
 		eightButton.setText("8");
 		eightButton.setOnClickListener(genericHexNumberButtonListener);
 		// bitwise shift Right button
 		Button nineButton = (Button) fourthRow.getChildAt(2);
 		nineButton.setText("9");
 		nineButton.setOnClickListener(genericHexNumberButtonListener);
 
 		Button divideButt = (Button) fourthRow.getChildAt(3);
 		divideButt.setText("/");
 		divideButt.setOnClickListener(genericOperatorButtonListener);
 
 		TableRow fifthRow = (TableRow) tableLayout.getChildAt(4);
 		// set the decimal button
 		Button fourButton = (Button) fifthRow.getChildAt(0);
 		fourButton.setText("4");
 		fourButton.setOnClickListener(genericHexNumberButtonListener);
 		// set the zero button
 		Button fiveButton = (Button) fifthRow.getChildAt(1);
 		fiveButton.setText("5");
 		fiveButton.setOnClickListener(genericHexNumberButtonListener);
 		// set the plus button
 		Button sixButton = (Button) fifthRow.getChildAt(2);
 		sixButton.setText("6");
 		sixButton.setOnClickListener(genericHexNumberButtonListener);
 		// set the equals button, it will have it's own separate listener to
 		// compute the inputed value
 
 		Button multButt = (Button) fifthRow.getChildAt(3);
 		multButt.setText("x");
 		multButt.setOnClickListener(genericOperatorButtonListener);
 
 		TableRow sixthRow = (TableRow) tableLayout.getChildAt(5);
 
 		Button oneButton = (Button) sixthRow.getChildAt(0);
 		oneButton.setText("1");
 		oneButton.setOnClickListener(genericHexNumberButtonListener);
 
 		Button twoButton = (Button) sixthRow.getChildAt(1);
 		twoButton.setText("2");
 		twoButton.setOnClickListener(genericHexNumberButtonListener);
 
 		Button threeButton = (Button) sixthRow.getChildAt(2);
 		threeButton.setText("3");
 		threeButton.setOnClickListener(genericHexNumberButtonListener);
 
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
 
 				if (mCurrentWorkingText.toString().endsWith("-")) {
 					Toast.makeText(getSherlockActivity(),
 							"That is not a valid expression.",
 							Toast.LENGTH_SHORT).show();
 
 					CalculatorDecimalFragment.numberOfOperators = 0;
 					CalculatorBinaryFragment.numberOfOperators = 0;
 					CalculatorHexFragment.numberOfOperators = 0;
 					CalculatorOctalFragment.numberOfOperators = 0;
 
 					return;
 				}
 
 				if (mCurrentWorkingText.toString().contains("N")
 						|| mCurrentWorkingText.toString().contains("O")) {
 					Toast.makeText(getSherlockActivity(),
 							"Bitwise expressions must be in binary.",
 							Toast.LENGTH_SHORT).show();
 
 					return;
 				}
 
 				// need to convert the mCurrentWorkingText (the current
 				// expression) to base10 before we do any evaluations.
 				StringTokenizer toke = new StringTokenizer(mCurrentWorkingText
 						.toString(), "x+-/)( \n\t", true);
 				StringBuilder builder = new StringBuilder();
 
 				while (toke.hasMoreElements()) {
 					String aToken = (String) toke.nextElement().toString();
 					if (aToken.equals("+") || aToken.equals("x")
 							|| aToken.equals("-") || aToken.equals("/")
 							|| aToken.equals("(") || aToken.equals(")")
 							|| aToken.equals("\t") || aToken.equals(" ")
 							|| aToken.equals("\n")) {
 
 						builder.append(aToken);
 
 					}
 					// if our token contains a "." in it then that means that we
 					// need to do some conversion trickery
 					else if (aToken.contains(".")) {
 						if (aToken.endsWith(".")) {
 							// don't do anything if a token ends with "." we
 							// don't want cases like ".5 + 5."
 							return;
 						}
 						// split the string around the "." delimiter.
 						String[] parts = aToken.split("\\.");
 						StringBuilder tempBuilder = new StringBuilder();
 
 						if (aToken.charAt(0) == '.') {
 							// so it doesn't break on cases like ".5"
 						} else {
 							// add the portion of the number to the left of the
 							// "."
 							// to our string, this doesn't need any conversion
 							// nonsense because it is a whole number.
 							tempBuilder.append(Long.toString(Long.parseLong(
 									parts[0], viewsRadix)));
 						}
 						// convert the fraction portion
 						String getRidOfZeroBeforePoint = null;
 
 						// convert just the fraction portion of the number to
 						// base10. This method doesn't take in the "." with the
 						// fraction.
 						getRidOfZeroBeforePoint = Fractions
 								.convertFractionPortionToDecimal(parts[1],
 										viewsRadix);
 
 						// the conversion returns just the fraction
 						// portion
 						// with
 						// a "0" to the left of the ".", so let's get
 						// rid of
 						// that extra zero.
 						getRidOfZeroBeforePoint = getRidOfZeroBeforePoint
 								.substring(1, getRidOfZeroBeforePoint.length());
 
 						tempBuilder.append(getRidOfZeroBeforePoint);
 
 						builder.append(tempBuilder.toString());
 					}// closes the "." case
 					else {
 						// if it's just a regular good ol' fashioned whole
 						// number, use java's parseInt method to convert to
 						// base10
 						builder.append(Long.parseLong(aToken, viewsRadix));
 					}
 				} // closes while() loop
 
 				// /Now convert the base10 expression into post-fix
 				String postfix = InfixToPostfix.convertToPostfix(
 						builder.toString(), getSherlockActivity());
 
 				if (postfix.equals("") || postfix.length() == 0) {
 					return;
 				}
 
 				// tokenize to see if the expression is in fact a valid
 				// expression, i.e contains an operator, contains the correct
 				// operand to operator ratio
 				StringTokenizer toker = new StringTokenizer(mCurrentWorkingText
 						.toString(), "+-/x )(");
 
 				// the number of operators should be one less than the number of
 				// operands/tokens
 				if (numberOfOperators != toker.countTokens() - 1
 						|| numberOfOperators == 0) {
 					Toast.makeText(getSherlockActivity(),
 							"That is not a valid expression.",
 							Toast.LENGTH_SHORT).show();
 
 					CalculatorDecimalFragment.numberOfOperators = 0;
 					CalculatorBinaryFragment.numberOfOperators = 0;
 					CalculatorHexFragment.numberOfOperators = 0;
 					CalculatorOctalFragment.numberOfOperators = 0;
 
 					return;
 				}
 
 				String theAnswerInDecimal = null;
 				if (postfix != null && postfix.length() > 0) {
 					if (!(postfix.contains("+") || postfix.contains("-")
 							|| postfix.contains("x") || postfix.contains("/"))) {
 						// don't evaluate if there is an expression with no
 						// operators
 						Toast.makeText(getSherlockActivity(),
 								"There are no operators in the expression.",
 								Toast.LENGTH_LONG).show();
 
 						CalculatorDecimalFragment.numberOfOperators = 0;
 						CalculatorBinaryFragment.numberOfOperators = 0;
 						CalculatorHexFragment.numberOfOperators = 0;
 						CalculatorOctalFragment.numberOfOperators = 0;
 
 						return;
 					} else if (numberOfOpenParenthesis != numberOfClosedParenthesis) {
 						// don't evaluate if the number of closed and open
 						// parenthesis aren't equal.
 						Toast.makeText(
 								getSherlockActivity(),
 								"The number of close parentheses is not equal to the number of open parentheses.",
 								Toast.LENGTH_LONG).show();
 
 						CalculatorDecimalFragment.numberOfOperators = 0;
 						CalculatorBinaryFragment.numberOfOperators = 0;
 						CalculatorHexFragment.numberOfOperators = 0;
 						CalculatorOctalFragment.numberOfOperators = 0;
 
 						return;
 					}
 					// Do the evaluation if it's safe to.
 					theAnswerInDecimal = PostfixEvaluator.evaluate(postfix);
 
 				} else {
 					// don't evaluate if the expression is null or empty
 					// Toast.makeText(getSherlockActivity(),
 					// "Error", Toast.LENGTH_LONG)
 					// .show();
 
 					CalculatorDecimalFragment.numberOfOperators = 0;
 					CalculatorBinaryFragment.numberOfOperators = 0;
 					CalculatorHexFragment.numberOfOperators = 0;
 					CalculatorOctalFragment.numberOfOperators = 0;
 
 					return;
 				}
 
 				// Log.d(TAG, "**Postfix: " + postfix + " AnswerInDecimal: "
 				// + theAnswerInDecimal);
 
 				// Now we need to convert the base10 answer into the correct
 				// base.
 				//
 				// This will require parsing around the "." (if it's there)
 				// because we need to give the fraction portion extra care.
 				//
 				// This will also require parsing out the negative sign (if it's
 				// there) because the negative sign will break the built in java
 				// base conversion function.
 				String[] answerParts = theAnswerInDecimal.split("\\.");
 				StringBuilder answerInCorrectBase = null;
 				if (answerParts[0].contains("-")) {
 					String[] parseOutNegativeSign = answerParts[0].split("-");
 					answerInCorrectBase = new StringBuilder(Long
 							.toHexString(Long
 									.parseLong(parseOutNegativeSign[1])));
 
 					// re-insert the '-' if it was even there
 					answerInCorrectBase.insert(0, "-");
 
 				} else {
 					answerInCorrectBase = new StringBuilder(Long
 							.toHexString(Long.parseLong(answerParts[0])));
 				}
 
 				String fractionPart = null;
 				if (answerParts.length > 1) {
 					fractionPart = Fractions.convertFractionPortionFromDecimal(
 							"." + answerParts[1], viewsRadix);
 					if (!fractionPart.equals("")) {
 						answerInCorrectBase.append("." + fractionPart);
 					}
 				}
 
 				// put new lines around our answer.
 				String answer = "\n" + "\t" + "\t"
 						+ answerInCorrectBase.toString() + "\n";
 
 				// mExpressions.add(answer);
 
 				// add the answer to the textView
 				mWorkingTextView.setText(mWorkingTextView.getText().toString()
 						.concat(answer.toUpperCase(Locale.getDefault())));
 
 				mExpressions.updateExpressions(answer);
 				// pass the data to the other fragments
 				onPassData(answer, false);
 
 				mCurrentWorkingText = new StringBuilder("");
 
 				// reset all of our counter variables
 				CalculatorDecimalFragment.numberOfOpenParenthesis = 0;
 				CalculatorBinaryFragment.numberOfOpenParenthesis = 0;
 				CalculatorHexFragment.numberOfOpenParenthesis = 0;
 				CalculatorOctalFragment.numberOfOpenParenthesis = 0;
 
 				CalculatorDecimalFragment.numberOfClosedParenthesis = 0;
 				CalculatorBinaryFragment.numberOfClosedParenthesis = 0;
 				CalculatorHexFragment.numberOfClosedParenthesis = 0;
 				CalculatorOctalFragment.numberOfClosedParenthesis = 0;
 
 				CalculatorDecimalFragment.numberOfOperators = 0;
 				CalculatorBinaryFragment.numberOfOperators = 0;
 				CalculatorHexFragment.numberOfOperators = 0;
 				CalculatorOctalFragment.numberOfOperators = 0;
 			}
 		});
 
 		Button zeroButton = (Button) lastRow.getChildAt(1);
 		zeroButton.setText("0");
 		zeroButton.setOnClickListener(genericHexNumberButtonListener);
 
 		Button decimalPointButton = (Button) lastRow.getChildAt(2);
 		decimalPointButton.setText(".");
 		decimalPointButton.setOnClickListener(new OnClickListener() {
 			// we can't put a "." up there if there has already been one in
 			// the current token (number)
 			@Override
 			public void onClick(View v) {
 
 				TextView textView = (TextView) v;
 
 				CharSequence textFromButton = textView.getText();
 				CharSequence newTextViewText = null;
 
 				StringBuilder textViewBuilder = new StringBuilder(
 						mWorkingTextView.getText());
 
 				// see if the workingTextView is empty, if so just add the '.'
 				if (mCurrentWorkingText.length() == 0) {
 
 					newTextViewText = (CharSequence) textViewBuilder
 							.append(textFromButton);
 
 					mWorkingTextView.setText(newTextViewText);
 					mCurrentWorkingText.append(textFromButton);
 
 				} else {
 
 					if (mCurrentWorkingText.length() <= 47) {
 						StringTokenizer toke = new StringTokenizer(
 								mCurrentWorkingText.toString(), "+-/x)(", true);
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
 						if (mCurrentWorkingText.toString().endsWith(".")
 								|| currentElement.contains(".")
 								|| mCurrentWorkingText.toString().contains("O")
 								|| mCurrentWorkingText.toString().contains("N")) {
 							// do nothing here so we don't end up with
 							// expressions
 							// like "2..2" or "2.3.22"
 						} else {
 							// otherwise we're all good and just add the ".' up
 							// there.
 							newTextViewText = (CharSequence) textViewBuilder
 									.append(textFromButton);
 
 							mWorkingTextView.setText(newTextViewText);
 							mCurrentWorkingText.append(textFromButton);
 						}
 					}
 				}
 
 				onPassData(mCurrentWorkingText.toString(), false);
 				mExpressions.updateExpressions(mCurrentWorkingText.toString());
 			}
 		});
 
 		Button plusButt = (Button) lastRow.getChildAt(3);
 		plusButt.setText("+");
 		plusButt.setOnClickListener(genericOperatorButtonListener);
 
 		return v;
 	}
 
 	public static SherlockFragment newInstance(int positionInViewPager,
 			int radix) {
 		CalculatorHexFragment hexFrag = new CalculatorHexFragment();
 		Bundle arg = new Bundle();
 		arg.putInt(KEY_VIEW_NUMBER, positionInViewPager);
 		arg.putInt(KEY_RADIX, radix);
 		hexFrag.setArguments(arg);
 		return hexFrag;
 	}
 
 	// method to save the state of the application during the activity life
 	// cycle. This is so we can preserve the values in the textViews upon screen
 	// rotation.
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		// Log.i(TAG, "onSaveInstanceState");
 		// outState.putString(KEY_WORKINGTEXTVIEW_STRING, mSavedStateString);
 		outState.putStringArrayList(KEY_WORKINGTEXTVIEW_STRING, mExpressions);
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
 	public void onPassData(String dataToBePassed, boolean cameFromBackspace) {
 		mCallback.onDataPassed(dataToBePassed, positionInPager, viewsRadix,
 				cameFromBackspace);
 	}
 
 	// method to receive the data from the activity/other-fragments and update
 	// the textViews accordingly
 	public void updateWorkingTextView(String dataToBePassed, int base,
 			boolean cameFromBackspace) {
 
 		if (base == viewsRadix)
 			return;
 
 		if (dataToBePassed.length() != 0 || cameFromBackspace) {
 			if (dataToBePassed.length() != 0) {
 
 				StringTokenizer toke = new StringTokenizer(dataToBePassed,
 						"x+-/)( \n\t", true);
 				StringBuilder builder = new StringBuilder();
 
 				while (toke.hasMoreElements()) {
 					String aToken = (String) toke.nextElement().toString();
 					if (aToken.equals("+") || aToken.equals("x")
 							|| aToken.equals("-") || aToken.equals("/")
 							|| aToken.equals("(") || aToken.equals(")")
 							|| aToken.equals(" ") || aToken.equals("\n")
 							|| aToken.equals("\t") || aToken.contains("N")
 							|| aToken.contains("O")) {
 
 						builder.append(aToken);
 
 					}
 					// if our token contains a "." in it then that means that we
 					// need to do some conversion trickery
 					else if (aToken.contains(".")) {
 						if (aToken.endsWith(".")) {
 							// don't do any conversions when the number is still
 							// being
 							// inputed and in the current state of something
 							// like
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
 							tempBuilder.append(Long.toHexString(Long.parseLong(
 									parts[0], base)));
 						}
 						// convert the fraction portion
 						String getRidOfZeroBeforePoint = null;
 
 						if (base == 10) {
 							String fractionWithRadixPoint = "." + parts[1];
 							String converted = Fractions
 									.convertFractionPortionFromDecimal(
 											fractionWithRadixPoint, viewsRadix);
 							parts = converted.split("\\.");
 							tempBuilder.append(".").append(parts[0]);
 						} else {
 
 							getRidOfZeroBeforePoint = Fractions
 									.convertFractionPortionToDecimal(parts[1],
 											base);
 
 							// the conversion returns just the fraction
 							// portion
 							// with
 							// a "0" to the left of the ".", so let's get
 							// rid of
 							// that extra zero.
 							getRidOfZeroBeforePoint = getRidOfZeroBeforePoint
 									.substring(1,
 											getRidOfZeroBeforePoint.length());
 							String partsAgain[] = getRidOfZeroBeforePoint
 									.split("\\.");
 
 							String converted = Fractions
 									.convertFractionPortionFromDecimal(
 											getRidOfZeroBeforePoint, viewsRadix);
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
 							String tokenInCorrectBase = Long.toHexString(Long
 									.parseLong(aToken, base));
 							builder.append(tokenInCorrectBase);
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
 					mCurrentWorkingText = safeUpperCase;
 				}
 			} else {
 				mCurrentWorkingText = new StringBuilder("");
 			}
 			mExpressions.updateExpressions(mCurrentWorkingText.toString());
 			if (mCurrentWorkingText.toString().contains("\n")) {
 				mCurrentWorkingText = new StringBuilder("");
 			}
 			mWorkingTextView.setText(mExpressions.printAllExpressions());
 		} else {
 			mExpressions.clearAllExpressions();
 			mCurrentWorkingText = new StringBuilder("");
 			mWorkingTextView.setText(mCurrentWorkingText);
 		}
 	}
 
 	// method to tell us if a string is a number or not
 	public static boolean isOperand(String s) {
 		double a = 0;
 		try {
 			a = Integer.parseInt(s, viewsRadix);
 		} catch (Exception ignore) {
 			return false;
 		}
 		return true;
 	}
 
 }
