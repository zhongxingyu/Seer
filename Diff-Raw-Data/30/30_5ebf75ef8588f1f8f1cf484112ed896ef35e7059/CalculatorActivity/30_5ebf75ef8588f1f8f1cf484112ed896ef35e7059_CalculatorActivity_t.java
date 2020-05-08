 package roboguice.calculator.activity;
 
 import roboguice.calculator.R;
 import roboguice.calculator.util.RpnStack;
 import roboguice.calculator.view.TickerTapeView;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 
 public class CalculatorActivity extends Activity {
     TickerTapeView tapeView;
     Button enterButton;
     Button deleteButton;
     Button plusButton;
     Button minusButton;
     Button multiplyButton;
     Button divideButton;
 
     RpnStack stack;
     SharedPreferences prefs;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         tapeView = (TickerTapeView) findViewById(R.id.tape);
         enterButton = (Button) findViewById(R.id.enter);
         deleteButton = (Button) findViewById(R.id.delete);
         plusButton = (Button) findViewById(R.id.plus);
         minusButton = (Button) findViewById(R.id.minus);
         multiplyButton = (Button) findViewById(R.id.multiply);
         divideButton = (Button) findViewById(R.id.divide);
 
        stack = new RpnStack();
         prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
         tapeView.setStack(stack);
 
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         for( int i=0; prefs.contains(String.valueOf(i)); ++i)
             stack.insertElementAt(new BigDecimal(prefs.getString(String.valueOf(i), null)), i);
        refreshDisplay();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         final SharedPreferences.Editor edit = prefs.edit();
 
         edit.clear();
 
         for( int i=0; i< stack.size(); ++i )
             edit.putString(String.valueOf(i), stack.get(i).toString());
 
         edit.commit();
     }
 
     public void onDigitClicked( View digit ) {
         stack.appendToDigitAccumulator( ((TextView) digit).getText() );
         refreshDisplay();
     }
     
     public void onOperationClicked( View operation ) {
 
         // Any operation will automatically push the current digits onto the stack as if the user hit 'enter'
         stack.pushDigitAccumulatorOnStack();
 
         BigDecimal tmp;
         switch( operation.getId() ) {
             case R.id.plus:
                 if( stack.size()<2 ) break;
                 stack.push(stack.pop().add(stack.pop()));
                 break;
 
             case R.id.minus:
                 if( stack.size()<2 ) break;
                 tmp = stack.pop();
                 stack.push( stack.pop().subtract(tmp) );
                 break;
 
             case R.id.multiply:
                 if( stack.size()<2 ) break;
                 stack.push(stack.pop().multiply( stack.pop()));
                 break;
 
             case R.id.divide:
                 if( stack.size()<2 ) break;
                 tmp = stack.pop();
                 stack.push(stack.pop().divide(tmp, MathContext.DECIMAL32 ));
                 break;
             
             case R.id.delete:
                 if( stack.size()>=1 )
                     stack.pop();
                 break;
             
         }
 
         refreshDisplay();
     }
 
     protected void refreshDisplay() {
         tapeView.refresh();
 
         boolean linesHasAtLeastTwoItems = tapeView.getText().toString().split("\n").length >= 2;
         int digitAccumulatorLength = stack.getDigitAccumulator().length();
 
         enterButton.setEnabled( digitAccumulatorLength > 0);
         deleteButton.setEnabled(stack.size() > 0 || digitAccumulatorLength > 0);
         plusButton.setEnabled(linesHasAtLeastTwoItems);
         minusButton.setEnabled(linesHasAtLeastTwoItems);
         divideButton.setEnabled(linesHasAtLeastTwoItems);
         multiplyButton.setEnabled(linesHasAtLeastTwoItems);
     }
 
 
 
 
 
 }
 
 
