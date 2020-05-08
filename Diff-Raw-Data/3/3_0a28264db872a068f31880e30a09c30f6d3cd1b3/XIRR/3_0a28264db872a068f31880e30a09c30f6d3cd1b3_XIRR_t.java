 package de.tomsplayground.peanuts.domain.statistics;
 
 import java.math.BigDecimal;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.joda.time.Days;
 
 import com.google.common.collect.Lists;
 
 import de.tomsplayground.util.Day;
 
 public class XIRR {
 	
 	private final BigDecimal minDistance = new BigDecimal("0.0000000001");
 
 	private static class Entry {
 		final Day day;
 		BigDecimal cashflow;
 		int delta;
 		public Entry(Day day, BigDecimal cashflow) {
 			this.day = day;
 			this.cashflow = cashflow;
 		}
 	}
 
 	private final List<Entry> dates = Lists.newArrayList();
 
 	public void add(Day day, BigDecimal cashflow) {
 		if (cashflow.signum() != 0) {
 			dates.add(new Entry(day, cashflow));
 		}
 	}
 
 	private void caluculateDates() {
 		Collections.sort(dates, new Comparator<Entry>() {
 			@Override
 			public int compare(Entry o1, Entry o2) {
 				return o1.day.compareTo(o2.day);
 			}
 		});
 		Day minDate = dates.get(0).day;
 		for (Entry entry : dates) {
 			entry.delta = Days.daysBetween(minDate.getJodaDate(), entry.day.getJodaDate()).getDays();
 		}
 	}
 	
 	protected double entryVlaue(BigDecimal cashflow, int days, BigDecimal rate) {
 		return cashflow.doubleValue() / 
 			Math.pow(rate.add(BigDecimal.ONE).doubleValue(),
 				(double)days / (double)365);
 	}
 	
 	private void checkNegative() {
 		if (dates.get(dates.size()-1).cashflow.signum() == -1) {
 			for (Entry entry : dates) {
 				entry.cashflow = entry.cashflow.negate();
 			}
 		}
 	}
 	
 	public BigDecimal calculateValue() {
		if (dates.isEmpty()) {
			return BigDecimal.ZERO;
		}
 		caluculateDates();
 		checkNegative();
 		
 		BigDecimal irrGuess = new BigDecimal("0.5");
 		BigDecimal rate = irrGuess;
 		boolean wasHi = false;
 		boolean wasLo = false;
 		for (int i = 0; i < 100; i++) {
 			double v = 0;
 			for (Entry entry : dates) {
 				v += entryVlaue(entry.cashflow, entry.delta, rate);
 			}
 			if (Math.abs(v) < 0.01) {
 				break;
 			}
 			if (irrGuess.compareTo(minDistance) < 0) {
 				 break;
 			}
 			if (v > 0.0) {
 				if (wasHi) {
 					irrGuess = irrGuess.divide(new BigDecimal("2"));
 				}
 				rate = rate.add(irrGuess);
 				wasHi = false;
 				wasLo = true;
 			} else if (v < 0.0) {
 				if (wasLo) {
 					irrGuess = irrGuess.divide(new BigDecimal("2"));
 				}
 				rate = rate.subtract(irrGuess);
 				wasHi = true;
 				wasLo = false;
 			}
 		}
 		return rate;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder s = new StringBuilder();
 		for (Entry entry : dates) {
 			s.append(entry.day).append(' ').append(entry.cashflow).append('\n');
 		}
 		return s.toString();
 	}
 }
