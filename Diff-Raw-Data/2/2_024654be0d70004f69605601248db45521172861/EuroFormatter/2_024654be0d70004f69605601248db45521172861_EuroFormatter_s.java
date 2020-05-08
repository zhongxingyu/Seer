 package org.fhw.asta.kasse.client.common;
 
 import org.fhw.asta.kasse.shared.common.EuroAmount;
 
 import com.google.gwt.i18n.client.NumberFormat;
 
 public class EuroFormatter {
 
 	private static final NumberFormat EURO_NUMBER_FORMAT = NumberFormat.getCurrencyFormat("EUR");
 	
 	public static String format(EuroAmount euroAmount) {
		return EURO_NUMBER_FORMAT.format(euroAmount.getCentAmount());
 	}
 	
 }
