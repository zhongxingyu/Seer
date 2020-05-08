 package org.pepit.p3.maths.additclassique;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.LightingColorFilter;
 import android.graphics.Point;
 import android.util.TypedValue;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ExerciseView {
 
 	// Text size
 	private int _textSize = 45;
 	// Current model
 	private ExerciseModel _m = null;
 	// Current context
 	private Context _ctx = null;
 	// Current Layout
 	private LinearLayout _l = null;
 	// Number of columns needed to display all the problem lines
 	private int _nbCols = 0;
 	// Number of rows needed to display all the problem lines
 	private int _nbRows = 0;
 	// TextView matrix used to display all the problem lines
 	private TextView[][] _tv = null;
 	// Current position in the answer
 	private int _cursorResultX = 0;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param m
 	 *            ExerciseModel
 	 * @param ctx
 	 *            Context
 	 * @param nbCols
 	 *            Number of columns for the TextView matrix
 	 * @param nbRows
 	 *            Number of rows for the TextView matrix
 	 */
 	public ExerciseView(ExerciseModel m, Context ctx, int nbCols, int nbRows) {
 		this._m = m;
 		this._ctx = ctx;
 		this._nbCols = nbCols;
 		this._nbRows = nbRows;
 		this._cursorResultX = nbCols - 1;
 	}
 
 	public LinearLayout getLayout() {
 		return (this._l);
 	}
 
 	public ExerciseModel getModel() {
 		return (this._m);
 	}
 
 	/**
 	 * Display a message toaster.
 	 * 
 	 * @param msg
 	 *            Message to display.
 	 * @param color
 	 *            Message color.
 	 * 
 	 */
 	private void _displayToast(String msg, int color) {
 		Toast t = Toast.makeText(this._ctx, msg, Toast.LENGTH_SHORT);
 		t.setGravity(Gravity.CENTER, 0, 0);
 		LinearLayout l = (LinearLayout) t.getView();
 		View child = l.getChildAt(0);
 		TextView msgTv = (TextView) child;
 		msgTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, this._textSize);
 		msgTv.setGravity(Gravity.CENTER);
 		msgTv.setTextColor(color);
 		t.show();
 	}
 
 	/**
 	 * Display the message for a bad answer
 	 * 
 	 */
 	public void displayWrongAnswer() {
 		this._displayToast("MAUVAISE REPONSE", Color.RED);
 	}
 
 	/**
 	 * Display the message for a good answer
 	 * 
 	 */
 	public void displayRightAnswer() {
 		this._displayToast("OPERATION REUSSIE", Color.GREEN);
 	}
 
 	/**
 	 * Update the answer text and move the cursor.
 	 * 
 	 * @param s
 	 *            Key pressed.
 	 */
 	private void _updateAnswerText(String s) {
 		int x = this._cursorResultX;
 		if (x >= 0) {
 			if (this._m.checkAnswerPart(s, x)) {
 				this._m.updateCurrentAnswer(x, s);
 				this._cursorResultX = x - 1;
 			} else {
 				this.displayWrongAnswer();
 			}
 		}
 	}
 
 	/**
 	 * Return a button for the pseudo numeric keyboard.
 	 * 
 	 * @param txt
 	 *            Button text.
 	 * @return Button.
 	 */
 	private Button _btnNumKey(final String txt) {
 		Context ctx = this._ctx;
 		Button btn = new Button(ctx);
 		btn.setText(txt);
 		btn.getBackground().setColorFilter(
 				new LightingColorFilter(0xFFFFFFFF, 0xFFFFFFFF));
 		btn.setTextColor(Color.BLACK);
 		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
 				ViewGroup.LayoutParams.MATCH_PARENT,
 				ViewGroup.LayoutParams.WRAP_CONTENT);
 		btn.setLayoutParams(params);
 		btn.setTag(this);
 		btn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				ExerciseView v = (ExerciseView) view.getTag();
 				v._updateAnswerText(txt);
 				v._refreshAnswer();
 				v._refreshCarry();
 			}
 		});
 		return (btn);
 	}
 
 	/**
 	 * Set the TextView to display a string.
 	 * 
 	 * @param tv
 	 *            TextView
 	 * @param s
 	 *            String
 	 */
 	private void _setStringTextView(TextView tv, String s) {
 		tv.setText(s);
 		tv.setTextColor(0xFF000000);
 	}
 
 	/**
 	 * Set the TextView to display a cursor.
 	 * 
 	 * @param tv
 	 *            TextView
 	 */
 	private void _setCursorTextView(TextView tv) {
 		tv.setText("\u2588"); // 0x2588 Unicode for full box
 		tv.setTextColor(0xFF99FF00);
 	}
 
 	/**
 	 * Set the TextView to display a separator.
 	 * 
 	 * @param tv
 	 *            TextView
 	 */
 	private void _setSeparatorTextView(TextView tv) {
 		tv.setText("\u2014"); // 0x2014 Unicode for long dash
 		tv.setPadding(0, 0, 0, 0);
 	}
 
 	/**
 	 * Set the TextView to display a carry.
 	 * 
 	 * @param tv
 	 *            TextView
 	 * @param s
 	 *            String
 	 */
 	private void _setCarryTextView(TextView tv, String s) {
 		tv.setText(s);
 		tv.setTextColor(0xFF990066);
 		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, this._textSize / s.length());
 	}
 
 	/**
 	 * Refresh the answer line from the model.
 	 * 
 	 */
 	private void _refreshAnswer() {
 		String currentAnswer = this._m.getCurrentAnswer();
 		TextView[][] tvMatrix = this._tv;
 		int sizex = this._nbCols;
 		for (int x = 0; x < sizex; x++) {
 			TextView tv = tvMatrix[x][0];
 			if (x == this._cursorResultX && this._cursorResultX >= 0) {
 				this._setCursorTextView(tv);
 			} else {
 				char c = currentAnswer.charAt(x);
 				this._setStringTextView(tv, String.valueOf(c));
 			}
 			tvMatrix[x][0] = tv;
 		}
 		this._tv = tvMatrix;
 	}
 
 	/**
 	 * Refresh the carry line from the model.
 	 * 
 	 */
 	private void _refreshCarry() {
 		int[] carry = this._m.getCarry();
 		TextView[][] tvMatrix = this._tv;
 		int sizex = this._nbCols - 1;
 		int y = this._nbRows - 1;
 		TextView tv = null;
 		String s = "";
 		// if the current answer is empty, reset the carry line
 		if (this._m.currentAnswerIsEmpty()) {
 			for (int x = 0; x < sizex; x++) {
 				tv = tvMatrix[x][y];
 				this._setStringTextView(tv, s);
 				tvMatrix[x][y] = tv;
 			}
 		}
 		// else refresh the carry line and stop at the cursor position
 		else if (carry != null) {
 			for (int x = sizex; x > 0 && x >= this._cursorResultX; x--) {
 				int c = carry[x];
 				if (c != 0) {
 					tv = tvMatrix[x][y];
 					s = String.valueOf(c);
 					if (c > 0) {
 						s = "+" + s;
 					} else {
 						s = "-" + s;
 					}
 					this._setCarryTextView(tv, s);
 					tvMatrix[x][y] = tv;
 				}
 			}
 			this._tv = tvMatrix;
 		}
 	}
 
 	/**
 	 * Refresh the TextView[][] from the model.
 	 * 
 	 */
 	public void refresh() {
 		int[] numbers = this._m.getNumbers();
 		String operator = this._m.getOperator();
 		TextView[][] tvMatrix = this._tv;
 		int sizex = this._nbCols;
 		int sizey = this._nbRows;
 		// if the current answer is empty, reset the cursor
 		if (this._m.currentAnswerIsEmpty()) {
 			this._cursorResultX = this._nbCols - 1;
 		}
 		// refresh the answer line
 		this._refreshAnswer();
 		// refresh the optional carry line
 		this._refreshCarry();
 		// refresh everything else
 		for (int x = 0; x < sizex; x++) {
 			for (int y = 1; y < sizey; y++) {
 				TextView tv = tvMatrix[x][y];
 				if (y == 1) { // separator line
 					this._setSeparatorTextView(tv);
 				} else if (y > 1 && y < (sizey - 1)) { // numbers
 					if (x == 0) {
 						if (y < (sizey - 2)) {
 							this._setStringTextView(tv, operator);
 						}
 					} else {
 						int n = numbers[y - 2];
 						String f = "%0" + (sizex - 1) + "d";
 						String s = String.format(f, n);
 						char c = s.charAt(x - 1);
 						this._setStringTextView(tv, String.valueOf(c));
 					}
 				}
 				tvMatrix[x][y] = tv;
 			}
 		}
 		this._tv = tvMatrix;
 	}
 
 	/**
 	 * Build an empty TextView[][].
 	 * 
 	 */
 	private void _initTvMatrix() {
 		int sizex = this._nbCols;
 		int sizey = this._nbRows;
 		Context ctx = this._ctx;
 		TextView[][] tvMatrix = new TextView[sizex][sizey];
 		for (int x = 0; x < sizex; x++) {
 			for (int y = 0; y < sizey; y++) {
 				TextView tv = new TextView(ctx);
 				tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, this._textSize);
 				tv.setGravity(Gravity.CENTER_HORIZONTAL);
 				tv.setPadding(5, 5, 5, 5);
 				this._setStringTextView(tv, "");
 				tvMatrix[x][y] = tv;
 			}
 		}
 		this._tv = tvMatrix;
 	}
 
 	/**
 	 * Build a TableLayout from a TextView[][]
 	 * 
 	 * @param tl
 	 *            TableLayout for the rows.
 	 */
 	private void _tableCalc(TableLayout tabCalc) {
 		TextView[][] tvMatrix = this._tv;
 		Context ctx = this._ctx;
 
 		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.WRAP_CONTENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 		tabCalc.setLayoutParams(params);
 
 		tabCalc.setGravity(Gravity.CENTER);
 		tabCalc.setBackgroundColor(Color.WHITE);
 
 		int sizex = tvMatrix.length;
 		int sizey = 0;
 		if (sizex > 0) {
 			sizey = tvMatrix[0].length;
 		}
 		if (sizex > 0 && sizey > 0) {
 			for (int y = sizey - 1; y >= 0; y--) {
 				TableRow row = new TableRow(ctx);
 				for (int x = 0; x < sizex; x++) {
 					TextView tv = tvMatrix[x][y];
 					row.addView(tv);
 				}
 				tabCalc.addView(row);
 			}
 		}
 		// add a padding proportional to the display width
 		WindowManager wm = (WindowManager) this._ctx
 				.getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		int width = size.x;
 		int pad = (int) Math.round(width * 0.2);
 		tabCalc.setPadding(pad, 0, pad, 0);
 	}
 
 	/**
 	 * Build the TableLayout for the numeric keyboard
 	 * 
 	 * @return TableLayout
 	 */
 	private TableLayout _tableNumericKeyboard() {
 		Context ctx = this._ctx;
 		TableLayout.LayoutParams paramsTable = new TableLayout.LayoutParams(
 				TableLayout.LayoutParams.MATCH_PARENT,
 				TableLayout.LayoutParams.WRAP_CONTENT);
 		paramsTable.gravity = Gravity.CENTER_HORIZONTAL;
		paramsTable.setMargins(5, 5, 5, 5);
 
 		TableRow.LayoutParams paramsRow = new TableRow.LayoutParams(
 				TableRow.LayoutParams.MATCH_PARENT,
 				TableRow.LayoutParams.WRAP_CONTENT);
 		paramsRow.gravity = Gravity.CENTER_HORIZONTAL;
		paramsRow.setMargins(5, 5, 5, 5);
 		// to have buttons with equal width
 		paramsRow.weight = 1;
 		paramsRow.width = 0;
 
 		TableLayout tab = new TableLayout(ctx);
 		// first button row
 		TableRow row1 = new TableRow(ctx);
 		for (Integer i = 0; i < 5; i++) {
 			Button btn = this._btnNumKey(i.toString());
 			row1.addView(btn, paramsRow);
 		}
 		tab.addView(row1, paramsTable);
 		// second button row
 		TableRow row2 = new TableRow(ctx);
 		for (Integer i = 5; i < 10; i++) {
 			Button btn = this._btnNumKey(i.toString());
 			row2.addView(btn, paramsRow);
 		}
 		tab.addView(row2, paramsTable);
 		return (tab);
 	}
 
 	/**
 	 * Initialize the game board.
 	 */
 	public void init() {
 		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 		params.gravity = Gravity.CENTER_HORIZONTAL;
 		params.setMargins(5, 5, 5, 5);
 
 		LinearLayout lil = new LinearLayout(this._ctx);
 		lil.setOrientation(LinearLayout.VERTICAL);
 		lil.setPadding(0, 15, 0, 0);
 		lil.setBackgroundColor(0xFF99CC66);
 
 		this._initTvMatrix();
 
 		LinearLayout lilCalc = new LinearLayout(this._ctx);
 		lilCalc.setPadding(5, 5, 5, 5);
 		lilCalc.setOrientation(LinearLayout.HORIZONTAL);
 		LinearLayout.LayoutParams paramsLilCalc = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.MATCH_PARENT);
 		lilCalc.setLayoutParams(paramsLilCalc);
 		lilCalc.setBackgroundColor(0xFF003300);
 		lilCalc.setGravity(Gravity.CENTER);
 
 		TableLayout tabCalc = new TableLayout(this._ctx);
 		this._tableCalc(tabCalc);
 		lilCalc.addView(tabCalc);
 		lil.addView(lilCalc, params);
 
 		TableLayout tabNumKey = this._tableNumericKeyboard();
 		lil.addView(tabNumKey, params);
 
 		// put the layout in a scroll
 		LinearLayout.LayoutParams paramsL = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.MATCH_PARENT);
 		ScrollView sc = new ScrollView(this._ctx);
 		sc.addView(lil, paramsL);
 
 		LinearLayout l = new LinearLayout(this._ctx);
 		paramsL.gravity = Gravity.CENTER_HORIZONTAL;
 		l.addView(sc, paramsL);
 
 		this._l = l;
 	}
 
 }
