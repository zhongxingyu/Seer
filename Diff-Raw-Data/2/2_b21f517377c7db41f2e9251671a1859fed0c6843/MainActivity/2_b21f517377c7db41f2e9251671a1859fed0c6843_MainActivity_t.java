 package com.ott.matt.bwc;
 
 import org.javia.arity.Symbols;
 import org.javia.arity.SyntaxException;
 
 import roboguice.activity.RoboFragmentActivity;
 import roboguice.inject.InjectView;
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.DisplayMetrics;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class MainActivity extends RoboFragmentActivity {
 
 	@InjectView(R.id.delete_button)
 	View deleteBtn;
 	@InjectView(R.id.display_view)
 	TextView displayView;
 	@InjectView(R.id.number_pager)
 	ViewPager nPager;
 
 	private Symbols mSymbols = new Symbols();
 	private String mCurText = "";
 	private String nums[] = new String[20];
 	private char ops[] = new char[20];
 	private int opIndex = 0;
 	private int numIndex = 0;
 	private int mRadix = 10;
 	private OnClickListener mListener;
 
 	private static final String STATE_CURRENT_VIEW = "state-current-view";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		mListener = new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				char selectedButtonText = ((Button) v).getText().charAt(0);
 				mCurText += selectedButtonText;
 				displayView.setText(mCurText);
 				enqueue(selectedButtonText);
 			}
 		};
 
 		if (nPager != null) {
 			nPager.setAdapter(new NumberPagerAdapter(nPager));
 		} else {
 			final TypedArray num_buttons = getResources().obtainTypedArray(
 					R.array.dec_buttons);
 			for (int i = 0; i < num_buttons.length(); i++) {
 				setOnClickListener(null, num_buttons.getResourceId(i, 0));
 			}
 			num_buttons.recycle();
 		}
 
 		if (nPager != null) {
 			nPager.setCurrentItem(savedInstanceState == null ? 0
 					: savedInstanceState.getInt(STATE_CURRENT_VIEW, 0));
 		}
 
 		nPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				switch (position) {
 				case (0):
 					mRadix = 10;
 					break;
 				case (1):
 					mRadix = 16;
 					break;
 				case (2):
 					mRadix = 8;
 					break;
 				case (3):
 					mRadix = 2;
 					break;
 				}
 			}
 		});
 
 		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
 		DisplayMetrics metrics = new DisplayMetrics();
 		wm.getDefaultDisplay().getMetrics(metrics);
 		int windowWidth = metrics.widthPixels;
 		int windowHeight = metrics.heightPixels;
 
 		nPager.setLayoutParams(new LinearLayout.LayoutParams(
 				windowWidth * 3 / 5, windowHeight * 15 / 25));
 		((View) deleteBtn.getParent())
 				.setLayoutParams(new LinearLayout.LayoutParams(
 						LayoutParams.MATCH_PARENT, windowHeight * 4 / 25));
 		displayView.setLayoutParams(new LinearLayout.LayoutParams(
 				LayoutParams.MATCH_PARENT, windowHeight / 5));
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 	}
 
 	public void setOnClickListener(View root, int id) {
 		final View target = root != null ? root.findViewById(id)
 				: findViewById(id);
 		target.setOnClickListener(mListener);
 	}
 
 	public void clickedOperator(View v) {
 		char selectedButtonText = ((Button) v).getText().charAt(0);
 		if (selectedButtonText == '<' || selectedButtonText == '>')
 			mCurText += Character.toString(selectedButtonText)
 					+ Character.toString(selectedButtonText);
 		else
 			mCurText += selectedButtonText;
 
 		displayView.setText(mCurText);
 		enqueue(selectedButtonText);
 	}
 
 	public void onDelete(View v) {
 		numIndex = opIndex = 0;
 		nums = new String[20];
 		ops = new char[20];
 		mCurText = "";
 		displayView.setText(mCurText);
 	}
 
 	public void onEquate(View v) {
 		if (mCurText.length() < 1)
 			return;
 		String result = "Error";
 		try {
 			result = eval();
 		} catch (SyntaxException e) {
 			e.printStackTrace();
 		}
 		displayView.setText(result);
 		mCurText = "";
 	}
 
 	public void enqueue(char input) {
 		if (isOperator(input)) {
 			ops[opIndex] = input;
 			opIndex += 1;
 			if (nums[0] != null)
 				numIndex += 1;
 		} else if (nums[numIndex] == null)
 			nums[numIndex] = Character.toString(input);
 		else
 			nums[numIndex] += input;
 	}
 
 	public String eval() throws SyntaxException {
 		String answer = null;
 		toDecimal();
 		if (numIndex == opIndex) {
 			answer = evaluateExpression(nums[0] + ops[0] + nums[1], ops[0]);
 			for (int i = 1; i < numIndex && answer != "Error"; i++)
 				answer = evaluateExpression(answer + ops[i] + nums[i + 1],
 						ops[i]);
 		} else {
 			answer = evaluateExpression(ops[0] + nums[0], ops[0]);
 			for (int i = 1; i <= numIndex && answer != "Error"; i++)
 				answer = evaluateExpression(answer + ops[i] + nums[i], ops[i]);
 		}
 		if (answer != "Error")
 			return convertToRadix(Double.parseDouble(answer));
 		else
 			return answer;
 	}
 
 	public void toDecimal() {
 		if (mRadix != 10)
 			for (int i = 0; i <= numIndex; i++)
 				if (nums[i] != null)
 					nums[i] = Integer.toString(Integer
 							.parseInt(nums[i], mRadix));
 	}
 
 	public boolean isOperator(char input) {
 		if (Character.isDigit(input) || Character.isLetter(input)
 				|| input == '.')
 			return false;
 		else
 			return true;
 	}
 
 	public boolean containsBitwise(String input) {
 		for (int i = 0; i < bitwise_operators.length; i++)
 			if (input.contains(Character.toString(bitwise_operators[i])))
 				return true;
 		return false;
 	}
 
 	public String evaluateExpression(String input, char operator)
 			throws SyntaxException {
 		if (containsBitwise(input))
 			if (input.contains("."))
 				return "Error";
 			else
 				return evaluateBitwise(input, operator);
 		else
 			return Double.toString(mSymbols.eval(input));
 	}
 
 	public String evaluateBitwise(String input, char operator) {
 		String parts[] = { "", "" };
 		String result = "";
 		switch (operator) {
 		case '<':
 			parts = input.split("<");
 			result = Integer.toString(
 					Integer.parseInt(parts[0]) << Integer.parseInt(parts[1]),
 					mRadix);
 			break;
 		case '>':
 			parts = input.split(">");
 			result = Integer.toString(
 					Integer.parseInt(parts[0]) >> Integer.parseInt(parts[1]),
 					mRadix);
 			break;
 		case '|':
			parts = input.split("\\|");
 			result = Integer.toString(
 					Integer.parseInt(parts[0]) | Integer.parseInt(parts[1]),
 					mRadix);
 			break;
 		case '&':
 			parts = input.split("\\&");
 			result = Integer.toString(
 					Integer.parseInt(parts[0]) & Integer.parseInt(parts[1]),
 					mRadix);
 			break;
 		case '^':
 			parts = input.split("\\^");
 			result = Integer.toString(
 					Integer.parseInt(parts[0]) ^ Integer.parseInt(parts[1]),
 					mRadix);
 			break;
 		case '~':
 			parts = input.split("\\~");
 			result = Integer.toString(~Integer.parseInt(parts[1]), mRadix);
 			break;
 		default:
 			break;
 		}
 		return result;
 	}
 
 	public String convertToRadix(double input) {
 		if (mRadix == 10)
 			return Double.toString(input);
 		int fixed = (int) input;
 		String result = Integer.toString(fixed, mRadix);
 		return result;
 	}
 
 	class NumberPagerAdapter extends PagerAdapter {
 		private View mNumber_dec;
 		private View mNumber_hex;
 		private View mNumber_oct;
 		private View mNumber_bin;
 
 		public NumberPagerAdapter(ViewPager parent) {
 			final LayoutInflater inflater = LayoutInflater.from(parent
 					.getContext());
 			final View number_dec = inflater.inflate(R.layout.dec_layout,
 					parent, false);
 			final View number_hex = inflater.inflate(R.layout.hex_layout,
 					parent, false);
 			final View number_oct = inflater.inflate(R.layout.oct_layout,
 					parent, false);
 			final View number_bin = inflater.inflate(R.layout.bin_layout,
 					parent, false);
 			mNumber_dec = number_dec;
 			mNumber_hex = number_hex;
 			mNumber_oct = number_oct;
 			mNumber_bin = number_bin;
 
 			final Resources res = getResources();
 
 			final TypedArray hex_buttons = res
 					.obtainTypedArray(R.array.hex_buttons);
 			for (int i = 0; i < hex_buttons.length(); i++) {
 				setOnClickListener(number_hex, hex_buttons.getResourceId(i, 0));
 			}
 			hex_buttons.recycle();
 
 			final TypedArray dec_buttons = res
 					.obtainTypedArray(R.array.dec_buttons);
 			for (int i = 0; i < dec_buttons.length(); i++) {
 				setOnClickListener(number_dec, dec_buttons.getResourceId(i, 0));
 			}
 			dec_buttons.recycle();
 
 			final TypedArray oct_buttons = res
 					.obtainTypedArray(R.array.oct_buttons);
 			for (int i = 0; i < oct_buttons.length(); i++) {
 				setOnClickListener(number_oct, oct_buttons.getResourceId(i, 0));
 			}
 			oct_buttons.recycle();
 
 			final TypedArray bin_buttons = res
 					.obtainTypedArray(R.array.bin_buttons);
 			for (int i = 0; i < bin_buttons.length(); i++) {
 				setOnClickListener(number_bin, bin_buttons.getResourceId(i, 0));
 			}
 			bin_buttons.recycle();
 
 		}
 
 		@Override
 		public int getCount() {
 			return 4;
 		}
 
 		@Override
 		public void startUpdate(View container) {
 		}
 
 		@Override
 		public Object instantiateItem(View container, int position) {
 			final View page;
 			switch (position) {
 			case (0):
 				page = mNumber_dec;
 				break;
 			case (1):
 				page = mNumber_hex;
 				break;
 			case (2):
 				page = mNumber_oct;
 				break;
 			case (3):
 				page = mNumber_bin;
 				break;
 			default:
 				page = mNumber_dec;
 				break;
 			}
 			((ViewGroup) container).addView(page);
 
 			return page;
 		}
 
 		@Override
 		public void destroyItem(View container, int position, Object object) {
 			((ViewGroup) container).removeView((View) object);
 		}
 
 		@Override
 		public void finishUpdate(View container) {
 		}
 
 		@Override
 		public boolean isViewFromObject(View view, Object object) {
 			return view == object;
 		}
 
 		@Override
 		public Parcelable saveState() {
 			return null;
 		}
 
 		@Override
 		public void restoreState(Parcelable state, ClassLoader loader) {
 		}
 	}
 
 	private char bitwise_operators[] = { '>', '<', '~', '^', '&', '|' };
 }
