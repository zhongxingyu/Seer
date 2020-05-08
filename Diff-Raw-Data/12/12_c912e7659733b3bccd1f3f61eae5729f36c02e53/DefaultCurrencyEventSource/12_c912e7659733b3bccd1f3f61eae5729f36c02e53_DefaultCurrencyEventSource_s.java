 package org.strangeforest.currencywatch.core;
 
 import java.util.*;
 
import com.finsoft.util.*;
 
 public class DefaultCurrencyEventSource implements CurrencyEventSource {
 
 	private final List<CurrencyEvent> EVENTS = Arrays.asList(
 		new CurrencyEvent("RSD", new GregorianCalendar(2012, 5, 6).getTime(), "Elections in Serbia", "SNS won elections in Serbia.")
 	);
 
 	@Override public CurrencyEvent getEvent(final String currency, final Date date) {
 		return Algorithms.find(EVENTS, new Predicate<CurrencyEvent>() {
			@Override public boolean evaluate(CurrencyEvent event) {
				return ObjectUtil.equal(event.getCurrency(), currency) && ObjectUtil.equal(event.getDate(), date);
 			}
 		});
 	}
 
 	public Iterable<CurrencyEvent> getEvents(final String currency, final Date fromDate, final Date toDate) {
 		return Algorithms.select(EVENTS, new Predicate<CurrencyEvent>() {
			@Override public boolean evaluate(CurrencyEvent event) {
				return ObjectUtil.equal(event.getCurrency(), currency) && new DateRange(fromDate, toDate).contains(event.getDate());
 			}
 		});
 	}
 }
