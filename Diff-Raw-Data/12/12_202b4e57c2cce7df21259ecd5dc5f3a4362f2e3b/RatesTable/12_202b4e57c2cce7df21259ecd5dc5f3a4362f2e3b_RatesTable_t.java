 /*
  * Created on Oct 31, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package net.sf.jmoney.stocks;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * This class contains a table of rates that can be used to calculate
  * commissions, taxes etc.
  * <P>
  * This is an immutable class.  If you want to modify a rates table,
  * you must create a new rates table and set the new table as the
  * property value.  (If you were to be allowed to modify a rates table
  * then there would be no easy means of ensuring listeners are notified
  * of the changes).
  * 
  * @author Nigel Westbury
  */
 public class RatesTable {
 	/*
 	 * An immutable class containing details of a single band in a rates
 	 * table.
 	 */
 	static class Band {
 		private long bandStart;
 		private double percentage;
 
 		public Band(long bandStart, double percentage) {
 			this.bandStart = bandStart;
 			this.percentage = percentage;
 		}
 
 		public long getBandStart() {
 			return bandStart;
 		}
 
 		public double getPercentage() {
 			return percentage;
 		}
 	}
 	
 	private long fixedAmount = 0;
 	
 	private ArrayList<Band> bands;
 	
 	// Construct a new rates table.
 	public RatesTable() {
 		bands = new ArrayList<Band>();
 	}
 	
 	// Construct a rates table from a String object.
 	// All classes created by plug-ins to contain JMoney properties 
 	// must have both a toString() method and a constructor from
 	// String that re-constructs an object from the string.
 	public RatesTable(String stringValue) {
 		bands = new ArrayList<Band>();
 
 		String[] numbers = stringValue.split(";");
         
         fixedAmount = new Long(numbers[0]).longValue();
 
         int i = 1;
         while (i < numbers.length) {
         	long bandStart = new Long(numbers[i++]).longValue();
         	double percentage = new Double(numbers[i++]).doubleValue();
         	Band band = new Band(bandStart, percentage);
         	bands.add(band);
         }
 	}
 	
 	public RatesTable(long fixedAmount, ArrayList<Band> bands) {
 		this.fixedAmount = fixedAmount;
 		this.bands = bands;
 	}
 
 	@Override
 	public String toString() {
 		String result = new Long(fixedAmount).toString();
 		for (int i = 0; i < bands.size(); i++) {
 			Band band = bands.get(i);
 			result += ";" + band.bandStart;
 			result += ";" + band.percentage;
 		}
 		return result;
 	}
 	
 	public long calculateRate(long amount) {
		double total = fixedAmount;
 		int i = 1;
 		while (i < bands.size() && amount > bands.get(i).getBandStart()) {
 			total += (bands.get(i).getBandStart() - bands.get(i-1).getBandStart()) * bands.get(i-1).getPercentage();
 			i++;
 		}
 		
 		total += (amount - bands.get(i-1).getBandStart()) * bands.get(i-1).getPercentage();
 
 		return (long)total;
 	}
 	
 	public long getFixedAmount() {
 		return fixedAmount;
 	}
 	
 	public List<Band> getBands() {
 		return Collections.unmodifiableList(bands);
 	}
 }
