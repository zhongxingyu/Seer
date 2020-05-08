 package org.jessies.mathdroid;
 
 import android.app.*;
 import android.content.*;
 import android.content.res.*;
 import android.graphics.*;
 import android.os.Bundle;
 import android.preference.*;
 import android.text.*;
 import android.text.style.*;
 import android.util.Log;
 import android.view.*;
 import android.view.inputmethod.*;
 import android.widget.*;
 import java.util.*;
 import org.jessies.calc.Calculator;
 import org.jessies.calc.CalculatorError;
 import org.jessies.calc.CalculatorPlotData;
 import org.jessies.calc.CalculatorPlotter;
 import org.jessies.calc.Node;
 import org.jessies.calc.StringNode;
 import org.jessies.calc.UnitsConverter;
 
 public class Mathdroid extends Activity implements AdapterView.OnItemClickListener, CalculatorPlotter, TextView.OnEditorActionListener, View.OnClickListener {
     private static final String TAG = "Mathdroid";
 
     // Constants for the transcript context menu items.
     private static final int CONTEXT_MENU_RETYPE_SELECTED = 0;
     private static final int CONTEXT_MENU_COPY_SELECTED = 1;
     private static final int CONTEXT_MENU_COPY_ALL  = 2;
     private static final int CONTEXT_MENU_FORGET_SELECTED = 3;
     private static final int CONTEXT_MENU_FORGET_ALL  = 4;
 
     // Constants identifying dialogs.
     private static final int DIALOG_PLOT = 0;
 
     private Calculator calculator;
 
     private CalculatorPlotData plotData;
 
     private final HashMap<Integer, String> buttonMap = new HashMap<Integer, String>();
 
     private HistoryAdapter history;
 
     private boolean continuationMode;
     private boolean hapticFeedback;
 
     // Called when the activity is first created or recreated.
     @Override protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Compatibility compatibility = Compatibility.get();
         compatibility.configureActionBar(this);
         if (!compatibility.isTablet(this)) {
             // If we hide the title bar on modern releases, that hides the ActionBar,
             // and then the user has no way to get to the settings or help.
             if (!compatibility.hasActionBar()) {
                 // Hide the title bar if we're on a small screen and don't want to waste space.
                 // We can't do this in the manifest because we want it conditional on screen size
                 // and OS version.
                 requestWindowFeature(Window.FEATURE_NO_TITLE);
             }
             // On a small screen, we want the system keyboard to overlap ours, not cause resizing.
             getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
         }
 
         calculator = new Calculator();
         calculator.setPlotter(this);
 
         setContentView(R.layout.main);
 
         final EditText queryView = (EditText) findViewById(R.id.q);
         queryView.setOnEditorActionListener(this);
         queryView.addTextChangedListener(new TextWatcher() {
             public void afterTextChanged(Editable s) {
                 onQueryTextChanged(queryView);
             }
             public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
             public void onTextChanged(CharSequence s, int start, int before, int count) { }
         });
         queryView.requestFocus();
 
         // Prevent the soft keyboard from appearing unless the user presses our keyboard button.
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 
         // Android doesn't count the _soft_ keyboard appearing/disappearing as a configChange.
         // This is the stackoverflow-approved hack for detecting soft keyboard visibility changes.
         // http://stackoverflow.com/questions/2150078/how-to-check-visibility-of-software-keyboard-in-android
         final View activityRootView = findViewById(android.R.id.content);
         activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
           @Override public void onGlobalLayout() {
             int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
            findViewById(R.id.on_screen_keyboard).setVisibility((heightDiff < 200) ? View.VISIBLE : View.GONE);
           }
         });
 
         initButtonMap();
 
         initButtonClickListener(R.id.del);
         initButtonClickListener(R.id.exe);
         initButtonClickListener(R.id.kbd);
         initButtonClickListener(R.id.left);
         initButtonClickListener(R.id.less);
         initButtonClickListener(R.id.more);
         initButtonClickListener(R.id.right);
 
         for (int id : buttonMap.keySet()) {
             initButtonClickListener(id);
         }
 
         this.history = new HistoryAdapter(this);
 
         ListView transcriptView = transcriptView();
         registerForContextMenu(transcriptView);
         transcriptView.setAdapter(history);
         transcriptView.setOnItemClickListener(this);
 
         try {
             loadState();
         } catch (Exception ex) {
             // Without code like this, it's impossible to recover from bad state: Mathdroid will just crash every time you start it.
             ex.printStackTrace();
         }
 
         onConfigurationChanged(getResources().getConfiguration());
     }
 
     private void initButtonClickListener(int id) {
         // Not all buttons will be present in all layouts.
         final View button = findViewById(id);
         if (button != null) {
             button.setOnClickListener(this);
         }
     }
 
     private void initButtonMap() {
         buttonMap.put(R.id.acos,   "acos()");
         buttonMap.put(R.id.ans,    "Ans");
         buttonMap.put(R.id.asin,   "asin()");
         buttonMap.put(R.id.atan,   "atan()");
         buttonMap.put(R.id.ceil,   "ceil()");
         buttonMap.put(R.id.close,  ")");
         buttonMap.put(R.id.comma,  ",");
         buttonMap.put(R.id.cos,    "cos()");
         buttonMap.put(R.id.digit0, "0");
         buttonMap.put(R.id.digit1, "1");
         buttonMap.put(R.id.digit2, "2");
         buttonMap.put(R.id.digit3, "3");
         buttonMap.put(R.id.digit4, "4");
         buttonMap.put(R.id.digit5, "5");
         buttonMap.put(R.id.digit6, "6");
         buttonMap.put(R.id.digit7, "7");
         buttonMap.put(R.id.digit8, "8");
         buttonMap.put(R.id.digit9, "9");
         buttonMap.put(R.id.divide, "\u00f7");
         buttonMap.put(R.id.dot,    ".");
         buttonMap.put(R.id.e,      "e");
         buttonMap.put(R.id.eng,    "E");
         buttonMap.put(R.id.floor,  "floor()");
         buttonMap.put(R.id.log10,  "Log10()");
         buttonMap.put(R.id.logE,   "LogE()");
         buttonMap.put(R.id.minus,  "-");
         buttonMap.put(R.id.ncr,    "nCr()");
         buttonMap.put(R.id.npr,    "nPr()");
         buttonMap.put(R.id.open,   "(");
         buttonMap.put(R.id.pi,     "\u03c0");
         buttonMap.put(R.id.pling,  "!");
         buttonMap.put(R.id.plus,   "+");
         buttonMap.put(R.id.pow,    "^");
         buttonMap.put(R.id.rand,   "rand()");
         buttonMap.put(R.id.sin,    "sin()");
         buttonMap.put(R.id.sqrt,   "\u221a");
         buttonMap.put(R.id.tan,    "tan()");
         buttonMap.put(R.id.times,  "\u00d7");
         buttonMap.put(R.id.x,      "x");
     }
 
     @Override public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.options, menu);
         return true;
     }
 
     @Override public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_clear:
             clear((EditText) findViewById(R.id.q));
             return true;
         case R.id.menu_help:
             showHelp();
             return true;
         case R.id.menu_settings:
             showSettings();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     @Override protected Dialog onCreateDialog(int id) {
         switch (id) {
         case DIALOG_PLOT:
             return createPlotDialog();
         default:
             return null;
         }
     }
 
     @Override protected void onPrepareDialog(int id, Dialog dialog) {
         switch (id) {
         case DIALOG_PLOT:
             final PlotView plotView = (PlotView) dialog.findViewById(R.id.plot);
             plotView.preparePlot(calculator, plotData);
             //plotData = null;
             return;
         default:
             return;
         }
     }
 
     private Dialog createPlotDialog() {
         final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
         final View layout = inflater.inflate(R.layout.plot, (ViewGroup) findViewById(R.id.plot));
 
         final AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setView(layout);
         builder.setCancelable(true);
 
         final Dialog dialog = builder.create();
         return dialog;
     }
 
     private HistoryItem selectedHistoryItem(ContextMenu.ContextMenuInfo menuInfo) {
         if (!(menuInfo instanceof AdapterView.AdapterContextMenuInfo)) {
             return null;
         }
         final int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
         return history.getItem(position);
     }
 
     @Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         if (history.getCount() == 0) {
             // If there's no transcript, there's nothing to copy, so no reason to show a menu.
             return;
         }
 
         super.onCreateContextMenu(menu, v, menuInfo);
         menu.setHeaderTitle("History");
         final HistoryItem historyItem = selectedHistoryItem(menuInfo);
         if (historyItem != null) {
             menu.add(0, CONTEXT_MENU_RETYPE_SELECTED, 0, "Retype '" + historyItem.question + "'");
             menu.add(0, CONTEXT_MENU_COPY_SELECTED, 0, "Copy '" + historyItem.question + " = " + historyItem.answer + "'");
         }
         menu.add(0, CONTEXT_MENU_COPY_ALL,  0, "Copy all");
         if (historyItem != null) {
             menu.add(0, CONTEXT_MENU_FORGET_SELECTED, 0, "Forget '" + historyItem.question + " = " + historyItem.answer + "'");
         }
         menu.add(0, CONTEXT_MENU_FORGET_ALL,  0, "Forget all");
     }
 
     @Override public boolean onContextItemSelected(MenuItem item) {
         final int id = item.getItemId();
         final ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
         final HistoryItem historyItem = selectedHistoryItem(menuInfo);
         switch (id) {
         case CONTEXT_MENU_RETYPE_SELECTED:
             final EditText queryView = (EditText) findViewById(R.id.q);
             queryView.setText(historyItem.question);
             queryView.setSelection(queryView.length());
             return true;
         case CONTEXT_MENU_COPY_SELECTED:
             return copyToClipboard(historyItem.question + " = " + historyItem.answer);
         case CONTEXT_MENU_COPY_ALL:
             return copyToClipboard(history.toString());
         case CONTEXT_MENU_FORGET_SELECTED:
             history.remove(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
             return true;
         case CONTEXT_MENU_FORGET_ALL:
             history.clear();
             return true;
         default:
             return super.onContextItemSelected(item);
         }
     }
 
     @SuppressWarnings("deprecation") // Honeycomb replaces the text-only ClipboardManager.
     private boolean copyToClipboard(String text) {
         final android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
         clipboard.setText(text);
         return true;
     }
 
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         final EditText queryView = (EditText) findViewById(R.id.q);
         final HistoryItem historyItem = history.getItem(position);
         queryView.setText(historyItem.question);
         queryView.setSelection(queryView.length());
     }
 
     @Override protected void onPause() {
         super.onPause();
         saveState();
     }
 
     @Override public void onResume() {
         super.onResume();
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 
         String angleMode = settings.getString("angleMode", "Radians");
         calculator.setDegreesMode(angleMode.equals("Degrees"));
 
         String outputBase = settings.getString("outputBase", "10");
         calculator.setOutputBase(Integer.parseInt(outputBase));
 
         this.continuationMode = settings.getBoolean("continuationMode", false);
         this.hapticFeedback = settings.getBoolean("hapticFeedback", false);
 
         final EditText queryView = (EditText) findViewById(R.id.q);
         queryView.selectAll();
     }
 
     private void performHapticFeedback(View view) {
         if (!hapticFeedback) {
             return;
         }
         int HapticFeedbackConstants_VIRTUAL_KEY = 1; // HapticFeedbackConstants.VIRTUAL_KEY not available until API 5.
         int SDK_INT = Integer.parseInt(android.os.Build.VERSION.SDK); // SDK_INT not available until API 4.
         int type = SDK_INT >= 5 ? HapticFeedbackConstants.LONG_PRESS : HapticFeedbackConstants_VIRTUAL_KEY;
         view.performHapticFeedback(type, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
     }
 
     @Override public void onClick(View view) {
         performHapticFeedback(view);
         final EditText queryView = (EditText) findViewById(R.id.q);
         final int id = view.getId();
         switch (id) {
         case R.id.transcript:
             break;
         case R.id.kbd:
             showSoftKeyboard(queryView);
             break;
         case R.id.left:
             moveCaret(queryView, -1);
             break;
         case R.id.right:
             moveCaret(queryView, 1);
             break;
         case R.id.del:
             del(queryView);
             break;
         case R.id.exe:
             exe(queryView);
             break;
         case R.id.more:
         case R.id.less:
             ((ViewFlipper) findViewById(R.id.flipper)).showNext();
             break;
         default:
             buttonPressed(queryView, id);
         }
     }
 
     // Invoked if the user hits the physical "return" key while the EditText has focus.
     public boolean onEditorAction(TextView queryView, int actionId, KeyEvent event) {
         if (event != null && event.getAction() == KeyEvent.ACTION_UP) {
             // We already handled the ACTION_DOWN event, and don't want to repeat the work.
             return true;
         }
         exe((EditText) queryView);
         return true;
     }
 
     private void buttonPressed(EditText queryView, int id) {
         // Insert the new text (by replacing the entire content, which is our only option).
         // If there's a selection, we overwrite it.
         final String newText = buttonMap.get(id);
         final String existingText = queryView.getText().toString();
         final int startOffset = queryView.getSelectionStart();
         final int endOffset = queryView.getSelectionEnd();
         queryView.setText(existingText.substring(0, startOffset) + newText + existingText.substring(endOffset));
         // Put the caret back in the right place.
         int newCaretOffset;
         if (newText.length() > 1 && newText.endsWith(")")) {
             // For functions, we automatically insert both parentheses, so we need to move the caret back between them.
             newCaretOffset = startOffset + newText.length() - 1;
         } else {
             newCaretOffset = startOffset + newText.length();
         }
         queryView.setSelection(newCaretOffset, newCaretOffset);
     }
 
     private void clear(EditText queryView) {
         queryView.setText("");
         history.clear();
     }
 
     private void showSoftKeyboard(EditText queryView) {
         InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.showSoftInput(queryView, 0);
     }
 
     private void del(EditText queryView) {
         int startOffset = queryView.getSelectionStart();
         int endOffset = queryView.getSelectionEnd();
         final String existingText = queryView.getText().toString();
         if (existingText.length() == 0) {
             return;
         }
         if (startOffset != endOffset) {
             // Remove the selection.
             queryView.setText(existingText.substring(0, startOffset) + existingText.substring(endOffset));
         } else {
             // Remove the character before the caret.
             if (startOffset == 0) {
                 // There is no character before the caret.
                 return;
             }
             --startOffset;
             queryView.setText(existingText.substring(0, startOffset) + existingText.substring(endOffset));
         }
         queryView.setSelection(startOffset, startOffset);
     }
 
     private void moveCaret(EditText queryView, int delta) {
         int offset = (delta > 0) ? queryView.getSelectionEnd() : queryView.getSelectionStart();
         offset += delta;
         offset = Math.max(Math.min(offset, queryView.length()), 0);
         queryView.setSelection(offset);
     }
 
     private void onQueryTextChanged(final EditText queryView) {
         if (!continuationMode) {
             return;
         }
         // Implement continuation mode, where we insert "Ans" before an operator at the start of an expression.
         // Note that this is ambiguous in the case of '-' which may be part of a numeric literal starting a new expression, or subtraction, indicating a continuation.
         // This is the price people who want this feature have to pay.
         final String newInput = queryView.getText().toString();
         if (isContinuationOperator(newInput)) {
             // We could actually include the text of the previous expression, but you can just click the history item to get that behavior.
             // String lastInput = history.getItem(history.getCount() - 1).question;
             String lastInput = "Ans";
             final String replacement = lastInput + newInput;
             queryView.post(new Runnable() {
                 public void run() {
                     // We have to do this later on the UI thread, or the selection change doesn't happen.
                     queryView.setText(replacement);
                     queryView.setSelection(replacement.length());
                 }
             });
         }
     }
 
     private static boolean isContinuationOperator(String s) {
         return s.length() == 1 && "-=+*\u00d7^/\u00f7%<>&|".indexOf(s.charAt(0)) != -1;
     }
 
     private void exe(EditText queryView) {
         final String queryText = queryView.getText().toString().trim();
         if (queryText.length() == 0) {
             // Nothing to do. Finger flub.
             return;
         }
         if (continuationMode) {
             // Clear the input; we may put it back depending on what the user types next...
             queryView.setText("");
         } else {
             // Select the input to make it easy to replace while allowing the possibility of further editing.
             queryView.selectAll();
         }
         // Adding to the history automatically updates the display.
         history.add(new HistoryItem(queryText, computeAnswer(queryText)));
     }
 
     private Node computeAnswer(String query) {
         try {
             Node answer = null;
             // TODO: integrate units conversion with calculation.
             // Convert units.
             String conversion = UnitsConverter.convert(query);
             if (conversion != null) {
               answer = new StringNode(conversion);
             }
             if (answer == null) {
                 // Evaluate mathematical expressions.
                 answer = calculator.evaluate(query);
             }
             return answer;
         } catch (CalculatorError ex) {
             return new StringNode("Error: " + ex.getMessage());
         } catch (Exception ex) {
             ex.printStackTrace();
             return new StringNode("Internal error: " + ex.getMessage());
         }
     }
 
     private void loadState() {
         final SharedPreferences state = getPreferences(MODE_PRIVATE);
         final int version = state.getInt("version", 0);
         if (version != 3) {
             // We've never been run before, or the last run was an incompatible version.
             return;
         }
 
         final EditText queryView = (EditText) findViewById(R.id.q);
         final String oldQuery = state.getString("query", "");
         queryView.setText(oldQuery);
         queryView.selectAll();
 
         final String serializedHistory = state.getString("transcript", "");
         history.fromString(serializedHistory);
 
         final String serializedPlotData = state.getString("plotData", "");
         if (serializedPlotData.length() > 0) {
             plotData = CalculatorPlotData.fromString(serializedPlotData);
         }
     }
 
     private void saveState() {
         final String serializedHistory = history.toString();
 
         final EditText queryView = (EditText) findViewById(R.id.q);
 
         final SharedPreferences.Editor state = getPreferences(MODE_PRIVATE).edit();
         state.putInt("version", 3);
         state.putString("query", queryView.getText().toString());
         state.putString("transcript", serializedHistory);
         state.putString("plotData", (plotData != null) ? plotData.toString() : "");
         state.commit();
     }
 
     private ListView transcriptView() {
         return (ListView) findViewById(R.id.transcript);
     }
 
     private void showHelp() {
         startActivity(new Intent(this, MathdroidHelp.class));
     }
 
     private void showSettings() {
         startActivity(new Intent(this, MathdroidSettings.class));
     }
 
     public void showPlot(CalculatorPlotData plotData) {
         this.plotData = plotData;
         showDialog(DIALOG_PLOT);
     }
 }
