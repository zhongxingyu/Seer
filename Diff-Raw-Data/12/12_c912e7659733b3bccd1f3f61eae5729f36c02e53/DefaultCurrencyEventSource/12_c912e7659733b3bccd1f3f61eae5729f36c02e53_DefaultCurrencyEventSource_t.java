 package org.strangeforest.currencywatch.core;
 
 import java.util.*;
 
import org.strangeforest.util.*;
 
 public class DefaultCurrencyEventSource implements CurrencyEventSource {
 
 	private final List<CurrencyEvent> EVENTS = Arrays.asList(
 		new CurrencyEvent("RSD", new GregorianCalendar(2012, 5, 6).getTime(), "Elections in Serbia", "SNS won elections in Serbia.")
 	);
 
 	@Override public CurrencyEvent getEvent(final String currency, final Date date) {
 		return Algorithms.find(EVENTS, new Predicate<CurrencyEvent>() {
			@Override public boolean test(CurrencyEvent event) {
				return Objects.equals(event.getCurrency(), currency) && Objects.equals(event.getDate(), date);
 			}
 		});
 	}
 
 	public Iterable<CurrencyEvent> getEvents(final String currency, final Date fromDate, final Date toDate) {
 		return Algorithms.select(EVENTS, new Predicate<CurrencyEvent>() {
			@Override public boolean test(CurrencyEvent event) {
				return Objects.equals(event.getCurrency(), currency) && new DateRange(fromDate, toDate).contains(event.getDate());
 			}
 		});
 	}
 }
