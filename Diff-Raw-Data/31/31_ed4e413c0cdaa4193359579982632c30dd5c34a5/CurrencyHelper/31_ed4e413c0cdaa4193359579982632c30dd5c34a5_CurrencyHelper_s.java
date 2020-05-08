 //big changes have been made to this file
 package com.lab117.exchangecam.currency;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.util.Log;
 import java.util.Currency;
 import java.util.List;
 import java.math.*;
 import java.lang.StringBuffer;
 
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.graphics.Rect;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 
 
 import com.lab117.exchangecam.CaptureActivity;
 import com.lab117.exchangecam.OcrResult;
 import com.lab117.exchangecam.OcrResultText;
 import com.lab117.exchangecam.PreferencesActivity;
 import com.lab117.exchangecam.R;
 
 /*
  * Utility Class that implements currency related operations.
  */
 
 public class CurrencyHelper {
 	public static final String TAG = CurrencyHelper.class.getSimpleName();
 
 	public static final boolean DETECT_SMALL_DECIMAL = true;
 
 	/**
 	 * Private constructor for utility class
 	 */
 	private CurrencyHelper() {
 		throw new AssertionError();
 	}
 
 	/**
 	 * Map ISO-4217 Currency Code to Name
 	 */
 	public static String getCurrencyName(Context context, String currencyCode) {
 		Resources res = context.getResources();
 		String[] currencyCodes = res.getStringArray(R.array.currencycodes);
 		String[] currencyNames = res.getStringArray(R.array.currencynames);
 
 		for (int i = 0; i < currencyCodes.length; i++) {
 			if (currencyCodes[i].equals(currencyCode)) {
 				Log.d(TAG, "getCurrencyName: " + currencyCode + "->" + currencyNames[i]);
 				return currencyNames[i];
 			}
 		}
 		Log.d(TAG, "currencyCode: Could not find currency name for " + currencyCode);
 		return currencyCode;
 	}
 
 
 	/**
 	 * Retrieve Correct number of significant figures
 	 */
 	public static int getCurrencyPrecision(String currencyCode) {
 		Currency currency = Currency.getInstance(currencyCode);
 		return currency.getDefaultFractionDigits();
 	}
 
 	/**
 	 * Get the Correct Symbol
 	 */
 	public static String getCurrencySymbol(String currencyCode) {
 		Currency currency = Currency.getInstance(currencyCode);
 		return currency.getSymbol();
 	}
 
 	public static String convert(Context context, String sourceAmount) {
 		/*
 		 * Get the conversion rate from shared preferences
 		 */
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		String conversionRate = prefs.getString(PreferencesActivity.KEY_EXCHANGE_RATE_PREFERENCE, CaptureActivity.DEFAULT_EXCHANGE_RATE);
 		String targetCurrencyCode = prefs.getString(PreferencesActivity.KEY_TARGET_CURRENCY_PREFERENCE, CaptureActivity.DEFAULT_TARGET_CURRENCY);
 
 		//remove all non numeric or "." characters
 		sourceAmount = sourceAmount.replaceAll("[^0-9\\.]", "");
 
 		if (isNumeric(sourceAmount)) {
 			/*
 			 * Calculate the converted value as sourceAmount * conversionRate
 			 */
 			//Are there any exceptions to be caught here?
 			double sourceAmountDbl = Double.parseDouble(sourceAmount);
 			double conversionRateDbl = Double.parseDouble(conversionRate);
 			Log.d(TAG, "Conversion Rate Double: " + conversionRateDbl);
 			String convertedValue = Double.toString(sourceAmountDbl * conversionRateDbl);
 			return formatPrice(convertedValue, targetCurrencyCode);
 		}
 		return "0";	
 	}
 
 	/**
 	 * Checks if the string passed in follows format of a price
 	 * e.g. 15.99 or $14,50
 	 * @param price
 	 * @param sourceCurrencyCode
 	 * @return true if price otherwise false. 
 	 */
 	public static Boolean isPrice(Context context, String price) {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		String sourceCurrencyCode = prefs.getString(PreferencesActivity.KEY_SOURCE_CURRENCY_PREFERENCE, CaptureActivity.DEFAULT_SOURCE_CURRENCY);
 
 		int precision = getCurrencyPrecision(sourceCurrencyCode);
 		//Remove all whitespace from price.
 		price = price.replaceAll("\\s", "");
 		//Remove all non-numeric or "," or "." characters from string
 		price = price.replaceAll("[^\\d.,]", "");
 		Log.d(TAG, "isPrice Stripped String: " + price);
 		//Match price pattern
 		boolean result = price.matches("([0-9]+)(,[0-9]{3})*((\\.|,)([0-9]{" + precision + "}))?$");
 		Log.d(TAG, "isPrice: " + result);
 		return result;
 	}
 
 	public static String formatPrice(String price, String currencyCode) {
 
 		//Remove all whitespace from price.
 		price = price.replaceAll("\\s", "");
 		//Remove all non-numeric or "," or "." characters from string
 		price = price.replaceAll("[^\\d.,]", "");
 		price = adjustDecimal(price, currencyCode);
 		price = getCurrencySymbol(currencyCode) + " " + price;
 
 		return price;
 	}
 
 	private static String adjustDecimal(String price, String currencyCode) {
 		if (price != null && isNumeric(price)) {
 			int currencyPrecision = getCurrencyPrecision(currencyCode);
 			//String contains a comma
 			if (price.indexOf(",") != -1) { 
 				//look for comma used instead of decimal
 				if (price.matches(".*(,[\\d]{" + currencyPrecision + "})")) { 
 					//replace last comma with a deciaml
 					StringBuilder b = new StringBuilder(price);
 					b.replace(price.lastIndexOf(","), price.lastIndexOf(",")+1, ".");
 					price = b.toString();
 				}
 			}
 			//remove all remaining commas
 			price = price.replace(",", "");
 
 			BigDecimal roundVal = new BigDecimal(price);
 			roundVal = roundVal.setScale(currencyPrecision, BigDecimal.ROUND_HALF_UP);
 			return roundVal.toString();
 		}
 		return "0";
 	}
 
 	public static boolean isNumeric(String str) {
 		return str.matches("-?\\d+(.\\d+)?");
 	}
 
 	public static String[] extractPrices(OcrResultText resultText, int priceIndex, Context context) {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		String sourceCurrencyCode = prefs.getString(PreferencesActivity.KEY_SOURCE_CURRENCY_PREFERENCE, CaptureActivity.DEFAULT_SOURCE_CURRENCY);
 
 		String[] words = resultText.getText().replace("\n"," ").split(" ");
 		String prices[] = {"0", "0"};
 
 		if (priceIndex < words.length) {
 			prices[0] = correctSuperscriptDecimal(words[priceIndex], resultText, sourceCurrencyCode);
			prices[1] = CurrencyHelper.convert(context, words[priceIndex]);				
 		}
 		Log.d(TAG, "format, price: " + prices[0]);
 		prices[0] = CurrencyHelper.formatPrice(prices[0], sourceCurrencyCode);
 		return prices;
 	}
 
 	public static String correctSuperscriptDecimal(String price, OcrResultText resultText, String currencyCode) {
 		Log.d(TAG, "correctSmallDecimal()");
		//Scan through character boxes looking for matching price string.
 		int precision = getCurrencyPrecision(currencyCode);
 		String text = resultText.getText().replaceAll("\\s", "");
 		List<Rect> characterBoundingBoxes = resultText.getCharacterBoundingBoxes();
 		int priceIndex = -1;
 		/*
 		 * h, height of first character of price string
 		 * d, height of decimal character
 		 * a, average height of decimal characters
 		 */
 		try {
 			priceIndex = matchPrice(text, price);
 			Log.d(TAG, "priceIndex: " + priceIndex + " characterBoundingBoxes: " + characterBoundingBoxes.size());
 			if (priceIndex != -1 && price.length() > precision) {
 				if (priceIndex < characterBoundingBoxes.size()) {
					int h = characterBoundingBoxes.get(priceIndex).height();
					double d = 0;
					for (int i = 0; i < precision; i++) {
						d += characterBoundingBoxes.get(priceIndex+price.length()-1-i).height();
					}
					if (d != 0 && precision != 0) {
						double a = d / precision;
						Log.d(TAG, "a: " + a + " h: " + h);
						if (a < h * 0.6 && price.indexOf(".") == -1) {
							StringBuffer sb = new StringBuffer(price);
							price = sb.insert(price.length()-precision, ".").toString();
							Log.d(TAG, "Corrected Decimal!: " + price);							
						}
 					}
 				}
 			}
 		}
 		catch (IndexOutOfBoundsException e) {
 			Log.e(TAG, e.getMessage() + "\\n" + e.toString());
 		}
 		catch (NullPointerException e) {
 			Log.e(TAG, e.getMessage());
 		}
 		return price;
 	}
 
 	private static int matchPrice(String text, String price) {
 		for (int i = 0; i < text.length(); i++) {
 			int j = 0;
 			while ( j < price.length() && text.charAt(i + j) == price.charAt(j) ) j++;
 			if (j == price.length())
 				return i;
 		}
 		return -1;
 	}
 
 	/**
 	 * Formats a string for display as a price adding the currency symbol and 
 	 * rounding to the correct number of digits.
 	 * 
 	 * @param price; unformatted price string
 	 * @param targetCurrencyCode; CC to be used for symbol
 	 * @return formatted price; string
 	 */
 
 
 
 
 }
