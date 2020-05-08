 package com.blinz117.testapp;
 
 import java.math.BigDecimal;
 import java.util.Vector;
 
 public class TipManager {
 	
	static final BigDecimal ONE_HUNDRED = round(100.0, 2);
	
 	double mMinPercent;
 	double mMaxPercent;
 	double mBaseAmount;
 	
 	boolean mRoundTips;
 	
 	Vector<BigDecimal> mTipPercentages;
 	Vector<BigDecimal> mTipAmounts;
 	Vector<BigDecimal> mTotals;
 	
 	public TipManager(double minPercent, double maxPercent)
 	{
 		mMinPercent = minPercent;
 		mMaxPercent = maxPercent;
 		mBaseAmount = 0.00;
 		
 		mRoundTips = false;
 		
 		mTipPercentages = new Vector<BigDecimal>();
 		mTipAmounts = new Vector<BigDecimal>();
 		mTotals = new Vector<BigDecimal>();
 		
 		CalculateTips();
 	}
 	
 	public void CalculateTips()
 	{
 		mTipPercentages.clear();
 		mTipAmounts.clear();
 		mTotals.clear();
 		
 		boolean bMinMaxEqual = (mMaxPercent == mMinPercent);
 		
 		mTipPercentages.add(round(mMinPercent, 1));
 		// don't add same value twice
 		if (!bMinMaxEqual)
 			mTipPercentages.add(round(mMaxPercent, 1));
 		
 		for (int ndx = 0; ndx < mTipPercentages.size(); ndx++)
 		{
 			 double currTip = mTipPercentages.elementAt(ndx).doubleValue()/100.0 * mBaseAmount;
 			 mTipAmounts.add(round(currTip, 2));
 			 
 			 double currTotal = mBaseAmount + currTip;
 			 mTotals.add(round(currTotal, 2));
 		}
 		
		if (!bMinMaxEqual)
 		{
 			// the min total will be the first element in the totals array and max will be second
 			BigDecimal minTotal = mTotals.get(0);
 			BigDecimal maxTotal = mTotals.get(1);
 			BigDecimal nextDollar = minTotal.setScale(0, BigDecimal.ROUND_DOWN).add(BigDecimal.ONE);
 			
 			while (maxTotal.compareTo(nextDollar) > 0)
 			{
 				BigDecimal roundAmount = nextDollar.setScale(2);
 				// calculate tip and percentage
 				BigDecimal newTipAmount = roundAmount.subtract(round(mBaseAmount,2));
				BigDecimal newPercent = newTipAmount.multiply(ONE_HUNDRED);
				newPercent = newPercent.divide(roundAmount, 1, BigDecimal.ROUND_HALF_UP);
 				
 				mTipPercentages.add(newPercent);
 				mTipAmounts.add(newTipAmount);
 				mTotals.add(roundAmount);
 				
 				nextDollar = roundAmount.add(BigDecimal.ONE);
 			}
 		}
 	}
 	
 	public void UpdateBaseAmount(double newBaseAmount)
 	{
 		mBaseAmount = newBaseAmount;
		//CalculateTips();
 	}
 	
 	public void UpdatePercentMinMax(double newMin, double newMax)
 	{
 		// TODO: Add validation to min and max values
 		// Set min and max based on which is actually bigger, since there is no
 		// validation on the values right now
 		mMinPercent = Math.min(newMin, newMax);
 		mMaxPercent = Math.max(newMin, newMax);
 //		mMinPercent = newMin;
 //		mMaxPercent = newMax;
		//CalculateTips();
 	}
 	
 	public void UpdateRoundTips(boolean bUseRoundTips)
 	{
 		mRoundTips = bUseRoundTips;
 	}
 	
 	public Vector<BigDecimal> GetPercentages() { return mTipPercentages; }
 	
 	public Vector<BigDecimal> GetTips() { return mTipAmounts; }
 	
 	public Vector<BigDecimal> GetTotals() { return mTotals; }
 	
     public static BigDecimal round(double value, int places)
     {
         BigDecimal result = new BigDecimal(Double.toString(value));
         result = result.setScale(places, BigDecimal.ROUND_HALF_UP);       
         return result;
     }
 	
 }
