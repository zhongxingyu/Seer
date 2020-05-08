 package il.ac.huji.tipcalculator;
 
 import java.text.DecimalFormat;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class TipCalculatorActivity extends Activity {
 
 	private static final double TIP_PERCENTAGE = 0.12; 
 	private static final String TIP_RESULT_STRUCTURE = "Tip :$";
 	private static final DecimalFormat df= new DecimalFormat("#0.00");
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tip_calculator);
 
 		final CheckBox roundCheckBox = (CheckBox) findViewById(R.id.chkRound);
 		final TextView tipResText = (TextView) findViewById(R.id.txtTipResult);
 		final EditText edtBillEditText = (EditText) findViewById(R.id.edtBillAmount);
 		final Button clacButton = (Button) findViewById(R.id.btnCalculate);
 
 		clacButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 
 				String editBill = edtBillEditText.getText().toString();
 				if(editBill.length() > 0)
 				{
 					double num = (Double.parseDouble(editBill)) * TIP_PERCENTAGE;
 					int roundNum = (int)num;
 					if(roundCheckBox.isChecked())
 					{
 						if((Math.ceil(num) - num < 0.5) && (Math.ceil(num) - num > 0))
 						{
 							roundNum++;
 						}
 						tipResText.setText(TIP_RESULT_STRUCTURE+(double)roundNum); 
 					}
 					else
 					{
 						String numDecimalFormat= df.format(num);  
 						tipResText.setText(TIP_RESULT_STRUCTURE+Double.parseDouble(numDecimalFormat));
 					}
 				}else{
					tipResText.setText("Tip :$"+editBill);
 				}
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.tip_calculator, menu);
 		return true;
 	}
 
 }
