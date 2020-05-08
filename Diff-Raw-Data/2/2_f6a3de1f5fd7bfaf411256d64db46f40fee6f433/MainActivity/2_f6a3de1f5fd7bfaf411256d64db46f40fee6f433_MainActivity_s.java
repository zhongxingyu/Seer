 package com.ott.matt.bwc;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	private String mCurText = "";
 	private TextView tV;
 
 	// initialize the radix to binary
 	private int radix = 2;
 
 	// allows new operators to be called on operands
 	private boolean hasOperator = false;
 
 	private ArrayAdapter<CharSequence> opAdapter;
 	private ArrayAdapter<CharSequence> spAdapter;
 	private NumbersAdapter numAdapter;
 	private ArrayAdapter<CharSequence> radixAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// inflate the view
 		setContentView(R.layout.activity_main);
 		// initialize the grid and text views
 		final GridView oV = (GridView) findViewById(R.id.operator_view);
 		final GridView nV = (GridView) findViewById(R.id.numbers_view);
 		final GridView sV = (GridView) findViewById(R.id.special_view);
 		final Spinner radixSpinner = (Spinner) findViewById(R.id.radix_spinner);
 		tV = (TextView) findViewById(R.id.display_view);
 
 		// initialize the arrayadapters
 		opAdapter = ArrayAdapter.createFromResource(this,
 				R.array.operators_array, R.layout.operation_layout);
 		spAdapter = ArrayAdapter.createFromResource(this,
 				R.array.special_array, R.layout.special_layout);
 		numAdapter = NumbersAdapter.createFromResource(this,
 				R.array.numbers_array, R.layout.numbers_layout);
 		numAdapter.setRadix(radix);
 		radixAdapter = ArrayAdapter.createFromResource(this,
 				R.array.radix_array, android.R.layout.simple_spinner_item);
 
 		// initialize the click and select listeners
 		OnItemClickListener opClickListener = new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 				String selected = oV.getItemAtPosition(position).toString();
				if (!hasOperator) {
 					mCurText = onOperationPressed(selected);
 				}
 				tV.setText(mCurText);
 			}
 		};
 		OnItemClickListener spClickListener = new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 				String selected = sV.getItemAtPosition(position).toString();
 				if (selected.startsWith("DEL")) {
 					mCurText = onDeletePressed();
 				} else if (mCurText.length() > 1) {
 					mCurText = onCalculatePressed();
 				}
 				tV.setText(mCurText);
 			}
 		};
 		OnItemClickListener numClickListener = new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 				String selected = nV.getItemAtPosition(position).toString();
 				mCurText = onNumberPressed(selected);
 				tV.setText(mCurText);
 			}
 		};
 
 		OnItemSelectedListener spinnerListener = new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View v,
 					int position, long id) {
 				String selected = radixSpinner.getSelectedItem().toString();
 				((TextView) parent.getChildAt(0)).setTextColor(getResources()
 						.getColor(android.R.color.white));
 
 				if (selected.startsWith("BIN")) {
 					radix = 2;
 				} else if (selected.startsWith("OCT")) {
 					radix = 8;
 				} else if (selected.startsWith("DEC")) {
 					radix = 10;
 				} else if (selected.startsWith("HEX")) {
 					radix = 16;
 				}
 				updateDataSet(radix);
 
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				radix = 2;
 			}
 
 		};
 		// bind the adapters to the views
 		oV.setAdapter(opAdapter);
 		oV.setOnItemClickListener(opClickListener);
 		nV.setAdapter(numAdapter);
 		nV.setOnItemClickListener(numClickListener);
 		sV.setAdapter(spAdapter);
 		sV.setOnItemClickListener(spClickListener);
 		radixAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		radixSpinner.setAdapter(radixAdapter);
 		radixSpinner.setOnItemSelectedListener(spinnerListener);
 	}
 
 	/**
 	 * method: onSaveInstanceState(Bundle outState) saves user input
 	 */
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putString("curText", mCurText);
 	}
 
 	/***
 	 * method: updateDataSet(Integer[] newResources)
 	 * 
 	 * @param newResources
 	 *            : the resource array to replace in event of a radix change
 	 */
 	public void updateDataSet(int new_radix) {
 		numAdapter.setRadix(new_radix);
 		for (int position = 0; position < numAdapter.getCount(); position++) {
 			numAdapter.isEnabled(position);
 		}
 		mCurText = "";
 		// allow a new operator to be called
 		hasOperator = false;
 		tV.setText(mCurText);
 		numAdapter.notifyDataSetChanged();
 	}
 
 	/***
 	 * method: onDeletePressed()
 	 * 
 	 * 
 	 * @return the string that will be put back into the textview
 	 */
 	public String onDeletePressed() {
 		if (mCurText.length() > 1) {
 			if (mCurText.charAt(mCurText.length() - 1) == '<'
 					|| mCurText.charAt(mCurText.length() - 1) == '>') {
 				hasOperator = false;
 				return (String) mCurText.subSequence(0, mCurText.length() - 2);
 			} else {
 				if (mCurText.charAt(mCurText.length() - 1) == '|'
 						|| mCurText.charAt(mCurText.length() - 1) == '&'
 						|| mCurText.charAt(mCurText.length() - 1) == '^'
 						|| mCurText.charAt(mCurText.length() - 1) == '~')
 					hasOperator = false;
 				return (String) mCurText.subSequence(0, mCurText.length() - 1);
 			}
 		} else {
 			hasOperator = false;
 			return "";
 		}
 	}
 
 	/**
 	 * method: onCalculatePressed()
 	 * 
 	 * @return the answer based on the delimiting operator
 	 */
 	public String onCalculatePressed() {
 		String[] parts;
 		int val, bitmask;
 		boolean beginsWithNumber = String.valueOf(mCurText.charAt(0)).matches(
 				"[0-9a-zA-Z]");
 		boolean endsWithNumber = String.valueOf(
 				mCurText.charAt(mCurText.length() - 1)).matches("[0-9a-zA-Z]");
 
 		if (beginsWithNumber && mCurText.contains("<<") && endsWithNumber) {
 			parts = mCurText.split("<<");
 			val = Integer.parseInt(parts[0], radix);
 			bitmask = Integer.parseInt(parts[1], radix);
 			bitwiseShiftLeft(val, bitmask);
 			hasOperator = false;
 		} else if (beginsWithNumber && mCurText.contains(">>")
 				&& endsWithNumber) {
 			parts = mCurText.split(">>");
 			val = Integer.parseInt(parts[0], radix);
 			bitmask = Integer.parseInt(parts[1], radix);
 			bitwiseShiftRight(val, bitmask);
 			hasOperator = false;
 		} else if (beginsWithNumber && mCurText.contains("|") && endsWithNumber) {
 			parts = mCurText.split("\\|");
 			val = Integer.parseInt(parts[0], radix);
 			bitmask = Integer.parseInt(parts[1], radix);
 			bitwiseOr(val, bitmask);
 			hasOperator = false;
 		} else if (beginsWithNumber && mCurText.contains("&") && endsWithNumber) {
 			parts = mCurText.split("&");
 			val = Integer.parseInt(parts[0], radix);
 			bitmask = Integer.parseInt(parts[1], radix);
 			bitwiseAnd(val, bitmask);
 			hasOperator = false;
 		} else if (beginsWithNumber && mCurText.contains("^") && endsWithNumber) {
 			parts = mCurText.split("\\^");
 			val = Integer.parseInt(parts[0], radix);
 			bitmask = Integer.parseInt(parts[1], radix);
 			bitwiseXor(val, bitmask);
 			hasOperator = false;
 		} else if (mCurText.startsWith("~") && endsWithNumber) {
 			val = Integer.parseInt(mCurText.subSequence(1, mCurText.length())
 					.toString(), radix);
 			bitwiseComplement(val);
 			hasOperator = false;
 		}
 		return mCurText;
 	}
 
 	/**
 	 * method onOperationPressed(String keyText)
 	 * 
 	 * @param keyText
 	 *            - the value of the operator that was pressed
 	 * @return the old text plus the operator
 	 */
 	public String onOperationPressed(String keyText) {
 		if (hasOperator) {
 			return mCurText;
 		} else {
 			hasOperator = true;
 			return mCurText + keyText;
 		}
 	}
 
 	/**
 	 * method: onNumberPressed(String keyText)
 	 * 
 	 */
 	public String onNumberPressed(String keyText) {
 		return mCurText + keyText;
 	}
 
 	/**
 	 * method: bitwiseAnd(int val, int bitmask)
 	 * 
 	 * @param val
 	 * @param bitmask
 	 */
 	public void bitwiseAnd(int val, int bitmask) {
 		mCurText = Integer.toString(val & bitmask, radix);
 	}
 
 	/**
 	 * method: bitwiseOr(int val, int bitmask)
 	 * 
 	 * @param val
 	 * @param bitmask
 	 */
 	public void bitwiseOr(int val, int bitmask) {
 		mCurText = Integer.toString(val | bitmask, radix);
 	}
 
 	/**
 	 * method: bitwiseXor(int val, int bitmask)
 	 * 
 	 * @param val
 	 * @param bitmask
 	 */
 	public void bitwiseXor(int val, int bitmask) {
 		mCurText = Integer.toString(val ^ bitmask, radix);
 	}
 
 	/**
 	 * method: bitwiseShiftLeft(int val, int bitmask)
 	 * 
 	 * @param val
 	 * @param bitmask
 	 */
 	public void bitwiseShiftLeft(int val, int bitmask) {
 		mCurText = Integer.toString(val << bitmask, radix);
 	}
 
 	/**
 	 * method: bitwiseShiftRight(int val, int bitmask)
 	 * 
 	 * @param val
 	 * @param bitmask
 	 */
 	public void bitwiseShiftRight(int val, int bitmask) {
 		mCurText = Integer.toString(val >> bitmask, radix);
 	}
 
 	/**
 	 * method: bitwiseComplement(int val)
 	 * 
 	 * @param val
 	 */
 	public void bitwiseComplement(int val) {
 		mCurText = Integer.toString(~val, radix);
 	}
 }
