 package com.example.proceeds;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import java.util.Calendar;
 import java.util.Date;
 
 public class Results extends Activity {
 
 	/* Called when the activity is first created. */
 
 	@Override
 	public void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		setContentView(R.layout.results);
 		Bundle extras = getIntent().getExtras();
 		if (extras == null) {
 			return;
 		}
 		
 		//Calc & set commission
 		float commissionRate = extras.getFloat("commissionRate");
 		float sellingPrice = extras.getFloat("sellingPrice");
 		float commission = commissionRate * sellingPrice;
 		TextView text1 = (TextView) findViewById(R.id.res_commission_decimal);
 		commission = roundMoney(commission);
 		final String commissionOut = Float.toString(commission);
 		text1.setText(commissionOut);
 		
 		//Calc & set conveyance fee
 		/* Easy example */ 
 		String county = extras.getString("county");
 			TextView conveyance= (TextView) findViewById(R.id.res_conveyance_fee_decimal);
 		float countyRate = getCountyRate(county);
 		float conveyanceFee = getConveyanceFee(countyRate, sellingPrice);
 		conveyanceFee = roundMoney(conveyanceFee);
 		conveyance.setText(Float.toString(conveyanceFee));
 		
 		//Calc & set title premium
 		TextView homeTitle = (TextView) findViewById(R.id.res_homeowner_title_decimal);
 		float titleCost = getTitleCost(sellingPrice);
 		titleCost = roundMoney(titleCost);
 		homeTitle.setText(Float.toString(titleCost));
 		
 		// Calc & set prorated taxes
 		TextView proTax = (TextView) findViewById(R.id.res_prorated_taxes_decimal);
 		//float proratedTax = getProratedTax(annualTax, paidThrough,);
 		String paidThrough = extras.getString("paidThrough");
 		float taxRate = extras.getFloat("annualTax");
 		int year = extras.getInt("year");
 		int month = extras.getInt("month");
 		int day = extras.getInt("day");
 		int daysBetween = getDaysBetween(paidThrough, year, month, day);
 		System.out.println("Days between is " + daysBetween);
 		float taxAmt = getTaxAmt(daysBetween, taxRate);
 		System.out.println("Tax amt is " + taxAmt);
 		proTax.setText(Float.toString(taxAmt));
 		
 		
 		
 		String hoaFreq = extras.getString("hoaFreq");
 		String hoaFee = extras.getString("hoaFee");
 		String closingDate = extras.getString("closingDate");
 		String FirstMortgage = extras.getString("FirstMortgage");
 		String SecondMortgage = extras.getString("SecondMortgage");
 		String otherLiens = extras.getString("otherLines");
 		String otherRealtor = extras.getString("otherRealtor");
 		String gasLine = extras.getString("gasLine");
 		String homeWarranty = extras.getString("homeWarranty");
 		
 
 	}
 	
 	public float getCountyRate(String county){
 		/* Get the county rates, as per the email sent to me */
 
 		float rate = (float) 0.0;
 		if (county.equals("Delaware")){
 			rate = (float) 0.30;
 		}
 		else if (county.equals("Franklin") || county.equals("Licking") || county.equals("Union")){
 			rate = (float) 0.20;
 		}
 		else if (county.equals("Fairfield") || county.equals("Other")){
 			rate = (float) 0.40;
 		}
 		return rate;
 			
 	}
 	
 	public float getConveyanceFee(float countyRate, float sellingPrice){
 		/* For every hundred of sale price, charge the county rate */
 		float tempPrice = Math.round(sellingPrice / 100) * 100;
 		if (sellingPrice > tempPrice){
 			tempPrice = tempPrice + 100;
 		}
 		tempPrice = tempPrice / 100;
 		return countyRate * tempPrice;
 		
 	}
 	
 	/* Could easily just do a round to 10^n function here instead */
 	public float roundThou(float input){
 		/* round to the nearest thousand and always up */
 		float temp = Math.round(input / 1000) * 1000;
 		if (input > temp){
 			temp = temp + 1000;
 		}
 		return temp;
 	}
 	
 	public float roundHun(float input){
 		/* round to nearest hundred and always up */
 		float temp = Math.round(input / 100) * 100;
 		if (input > temp){
 			temp = temp + 100;
 		}
 		return temp;
 	}
 	
 	public float getTitleCost(float sellingPrice){
 
 		
 		float price = (float) 0.0;
 		float sellPrice = roundThou(sellingPrice) / 1000;
 		float baseRate = (float) 6.6125;
 		float secondRate = (float) 5.175;
 		float thirdRate = (float) 4.025;
 		float fourthRate = (float) 3.1625;
 		float fifthRate = (float) 2.5875;
 
 		// If under 150k, simple and return
 		if (sellPrice <= 150.0){
			price = (float) (baseRate* 150.0);
 			return price;
 		}
 		
 		// If > 150k, just add that to price
 		if (sellPrice > 150.0){
 			price = (float) (baseRate * 150.0);
 		}
 		// Get price if in the middle of the next bracket
 		if (sellPrice > 150 && sellPrice < 250){
 			price += (float) (secondRate * (sellPrice - 150));
 		}
 		// Get price if at or beyond the bracket
 		else if (sellPrice >= 250){ 
 			price += (float) (secondRate * 100.0);
 		}
 		// So on...
 		if (sellPrice < 500 && sellPrice > 250){
 			price += (float) (thirdRate * (sellPrice - 250));
 		}
 		else if (sellPrice >= 500){
 			price += (float) (thirdRate * 250);
 		}
 		if (sellPrice > 500 && sellPrice < 1000){
 			price += (float) (fourthRate * (sellPrice - 500));
 		}
 		else if (sellPrice >= 1000){
 			price += (float) (fourthRate * 9500);
 		}
 		if (sellPrice > 1000){
 			price += (float) (fifthRate * (sellPrice - 1000));
 		}
 		return price;
 	}
 
     public float roundMoney(float input){
     	input = input * 100;
     	input = Math.round(input);
     	input = input / 100;
     	return input;
     }
 
     public int getDaysBetween(String paidThrough, int year, int month, int day){
     	String[] temp = paidThrough.split(" ");
     	int startYear = Integer.valueOf(temp[2]);
     	int startMonth = 1;
     	int startDay = 1;
     	
     	//July 2nd is the 183 day, there are 182 days on each side.
     	// What to use if paid through first half of year
     	if (temp[0].equals("First")){
     		startMonth = 6; // 6 since Calendar is 0 indexed
     		startDay = 2;
     	}
     	// Dec 31 is final day in year
     	else{
     		startDay = 31;
     		startMonth = 11; // Month is 11 since Calendar is 0 indexed
     	}
     	
     	// Get difference between two dates
     	Calendar first = Calendar.getInstance();
     	Calendar second = Calendar.getInstance();
     	first.set(year, month, day);
     	second.set(startYear, startMonth, startDay);
     	long fTime = first.getTimeInMillis();
     	long sTime = second.getTimeInMillis();
     	fTime = fTime - sTime;
     	//Return in days
     	return (int) (fTime / (24 * 60 * 60 * 1000));
     }
 
     public float getTaxAmt(int daysBetween, float taxRate){
     	return (float) (daysBetween * (taxRate / 365.0));
     }
 };
