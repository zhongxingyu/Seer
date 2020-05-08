 package edu.upenn.cis.fruity;
 
 import edu.upenn.cis.fruity.database.DatabaseHandler;
 import edu.upenn.cis.fruity.database.FruitStand;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Allows the user to provide input for calculated costs, net profit, and final cash box and to
  * check answers
  */
 public class ProfitCalculationsActivity extends Activity{
 	
 	public static final int ProfitCalculationsActivity_ID = 14;
 	private static Intent intent;
 	private double totalRev;
 	private double donations = 0.0;
 	private double expectedTotalCosts, expectedNetProfit, expectedFinalCashBox;
 	private FruitStand currentStand;
 
 	private int numEquations = 3;
 	private int numCorrect = 0;
 	private double precision = 0.001;
 	private int correctColor = android.R.attr.editTextBackground;
 	private int incorrectColor = Color.YELLOW;
 	
 	ParseInputData parser = new ParseInputData();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_calculations_profit);
 		
 		intent = getIntent();
 
 		// Set total revenue from prior screen
 		if (intent != null && intent.getExtras() != null) {
 			totalRev = Double.valueOf((String)intent.getExtras().get("totalRevenue"));
 		} else {
 			totalRev= 0.0;
 		}
 		
 		DatabaseHandler dh = DatabaseHandler.getInstance(this);
 		currentStand = dh.getCurrentFruitStand();
 		long id = currentStand.id;
 
 		SQLiteDatabase db = dh.getReadableDatabase();
 		Cursor c = db.rawQuery("SELECT sum(amount_cash) FROM Purchase " +
 				"WHERE fruit_stand_id ="+id + " and item_name = 'donation'", null);

		if(c!=null && c.moveToFirst()){
			donations = c.getDouble(0);
 		}
		c.close();

 		TextView donationsText = (TextView)findViewById(R.id.calc_donations);
 		donationsText.setText(parser.convertToCurrency(donations));		

 		expectedTotalCosts = currentStand.stand_cost + currentStand.smoothie_cost + currentStand.additional_cost;
 		expectedNetProfit = totalRev - expectedTotalCosts;
 		expectedFinalCashBox = currentStand.initial_cash + expectedNetProfit + donations;
 
 		TextView fruitStandCost = (TextView)findViewById(R.id.calc_fruitStandCost);
 		fruitStandCost.setText(parser.convertToCurrency(currentStand.stand_cost));
 		
 		TextView smoothieCost = (TextView)findViewById(R.id.calc_smoothieCost);
 		smoothieCost.setText(parser.convertToCurrency(currentStand.smoothie_cost));
 		
 		TextView additionalCosts = (TextView)findViewById(R.id.calc_additionalCosts);
 		additionalCosts.setText(parser.convertToCurrency(currentStand.additional_cost));
 
 		TextView totalRevenue = (TextView)findViewById(R.id.calc_totalRevenueProfitEq);
 		totalRevenue.setText(parser.convertToCurrency(totalRev));
 		
 		TextView initCashBox = (TextView)findViewById(R.id.calc_initCashBox);
 		initCashBox.setText(parser.convertToCurrency(currentStand.initial_cash));
 
 		updateValuesInEquation(R.id.calc_totalCost, R.id.calc_totalCostProfitEq);
 		updateValuesInEquation(R.id.calc_profit, R.id.calc_profitFromEq);
 
 		TextView numCorrectDisplay = (TextView)findViewById(R.id.num_correct_profit_calculations);
 		numCorrectDisplay.setText("0/"+numEquations);
 	}
 	
 	public void onBackPressed() {
 		//back not allowed
 	}
 
 	
 	/**
 	 * Updates a TextView in an equation based on the user's input into an EditText box
 	 * in a previous equation that it depends on
 	 * @param inputId: id associated with the EditText box the user is editing in an equation
 	 * @param updateId: id associated with the TextView that is dependent on the input associated with inputId
 	 */
 	public void updateValuesInEquation(int inputId, final int updateId){
 		final EditText equationInput= (EditText)findViewById(inputId);
 		equationInput.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void afterTextChanged(Editable arg0) {
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {				
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				TextView updatedText = (TextView)findViewById(updateId);
 				updatedText.setText(parser.convertToCurrency(parser.parseItemPrice(equationInput)));	
 			}
 			});
 	}
 	
 	public void onCheckProfitCalculationsButtonClick(View v){		
 		numCorrect = 0;
 		checkAnswerToEquation(R.id.calc_totalCost, expectedTotalCosts); // total costs equation
 		checkAnswerToEquation(R.id.calc_profit, expectedNetProfit); // net profit equation
 		checkAnswerToEquation(R.id.calc_finalCashBox, expectedFinalCashBox); // cash box equation
 
 		TextView numCorrectDisplay = (TextView)findViewById(R.id.num_correct_profit_calculations);
 		numCorrectDisplay.setText("" + numCorrect +"/"+numEquations);
 		
 		if(numCorrect < numEquations){
 			Toast toast = Toast.makeText(getApplicationContext(),
 					"Incorrect calculations are highlighted in yellow.", Toast.LENGTH_SHORT);
 			toast.show();
 		}
 	}
 	
 	/**
 	 * Check if the answers to the equations input by the user match the expected results
 	 * @param inputId: id associated with EditText input for equation
 	 * @param expectedValue: expected answer to equation
 	 * @return: number of correct answers to equations
 	 */
 	public void checkAnswerToEquation(int inputId, double expectedValue){
 		EditText inputText = (EditText)findViewById(inputId);
 		double inputValue = parser.parseItemPrice(inputText);
 		
 		if(Math.abs(expectedValue - inputValue) < precision){
 			numCorrect++;
 			inputText.setBackgroundColor(correctColor);
 		}
 		else{
 			inputText.setBackgroundColor(incorrectColor);
 		}
 	}
 	
 	// Save revenue, costs, and profit in database and return to main activity screen
 	public void onCheckFinishButtonClick(View v){
 		DatabaseHandler dh = DatabaseHandler.getInstance(this);
 		FruitStand currentStand = dh.getCurrentFruitStand();
 		currentStand.addTotals(this, expectedTotalCosts, totalRev, expectedFinalCashBox);
 		
 		Intent i = new Intent(this, FinalSalesSummaryActivity.class);
 		startActivityForResult(i, ProfitCalculationsActivity_ID);
 	}
 }
