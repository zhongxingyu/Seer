 package pl.wtopolski.android.polishnotation;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.text.Editable;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import pl.wtopolski.android.polishnotation.support.JniHelper;
 import pl.wtopolski.android.polishnotation.support.NotationUtil;
 import pl.wtopolski.android.polishnotation.support.exception.BracketException;
 import pl.wtopolski.android.polishnotation.support.model.CountResult;
 import pl.wtopolski.android.polishnotation.support.storage.Properties;
 import pl.wtopolski.android.polishnotation.support.task.CountListener;
 import pl.wtopolski.android.polishnotation.support.view.KeyBoard;
 
 public class MainActivity extends Activity implements CountListener {
     private static final Logger LOG = LoggerFactory.getLogger(NotationUtil.class);
 
     private NotationApplication app;
 
     private EditText edit;
     private TextView requestText;
     private TextView prefixText;
     private TextView postfixText;
     private KeyBoard keyboard;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         app = (NotationApplication) getApplication();
 
         keyboard = (KeyBoard) findViewById(R.id.keyboard);
         requestText = (TextView) findViewById(R.id.request);
         prefixText = (TextView) findViewById(R.id.prefix);
         postfixText = (TextView) findViewById(R.id.postfix);
 
         edit = (EditText) findViewById(R.id.edit);
         edit.setText("");
         edit.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 // Hide origin keyboard.
                 InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
 
                 // Update availability of keyboard buttons.
                 editSelectionUpdate();
             }
         });
 
         InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         app.setListener(this);
 
         String content = Properties.getEditValue();
         edit.setText(content);
 
         int position = Properties.getEditValuePosition();
         edit.setSelection(position);
 
         app.makeRequest(content);
 
         editSelectionUpdate();
     }
 
     public void editSelectionUpdate() {
         int position = edit.getSelectionStart();
         String content = edit.getText().toString();
         keyboard.onEditSelection(position, content);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         app.setListener(null);
 
         String content = edit.getText().toString();
         Properties.setEditValue(content);
 
         int position = edit.getSelectionStart();
         Properties.setEditValuePosition(position);
     }
 
     public void onKeyBoardButtonClick(View view) {
         // Handle button click.
         int buttonId = view.getId();
         keyboard.onClick(buttonId, edit);
 
         // Send request.
         String request = edit.getText().toString();
         app.makeRequest(request);
     }
 
     @Override
     public void onResolve(CountResult result) {
         if (result != null && result.getRequest().length() > 0) {
             onResolvePositive(result);
         } else {
             onResolveNegative();
         }
     }
 
     private void onResolvePositive(CountResult result) {
         String request = result.getRequest();
         String postfix = result.getPostfix();
         String prefix = result.getPrefix();
         double resultValue = result.getResult();
 
         String value = convertResult(resultValue);
 
         requestText.setText(request + " = " + value);
         requestText.setTextColor(getResources().getColor(R.color.blue_dark));
 
         prefixText.setText(prefix);
         postfixText.setText(postfix);
     }
 
     private static String convertResult(double resultValue) {
         String number = String.format("%f", resultValue).replace(",", ".");
         if (!number.contains(".")) {
             return number;
         }
        return number.replaceAll("\\.?0*$", "");
     }
 
     private void onResolveNegative() {
         requestText.setText(edit.getText().toString());
         requestText.setTextColor(getResources().getColor(R.color.red_dark));
 
         prefixText.setText("");
         postfixText.setText("");
     }
 }
