 package org.strangeforest.currencywatch.core;
 
 import java.util.*;
 
 import com.finsoft.util.*;
 
 public class ChainedCurrencyRateProvider extends BaseObservableCurrencyRateProvider implements Disposable {
 
 	private final UpdatableCurrencyRateProvider localProvider;
 	private final CurrencyRateProvider remoteProvider;
 	private final boolean isLocalProviderObservable;
 	private final boolean isRemoteProviderObservable;
 	private final CurrencyRateListener rateListener;
 
 	public ChainedCurrencyRateProvider(UpdatableCurrencyRateProvider localProvider, CurrencyRateProvider remoteProvider) {
 		super();
 		this.localProvider = localProvider;
 		this.remoteProvider = remoteProvider;
 
 		isLocalProviderObservable = localProvider instanceof ObservableCurrencyRateProvider;
 		isRemoteProviderObservable = remoteProvider instanceof ObservableCurrencyRateProvider;
		rateListener = new CurrencyRateListener() {
			@Override public void newRate(CurrencyRateEvent rateEvent) {
				notifyListeners(rateEvent);
			}
			@Override public void newRates(CurrencyRateEvent[] rateEvents) {
				notifyListeners(rateEvents);
			}
		};
 	}
 
 	@Override public void init() throws CurrencyRateException {
 		localProvider.init();
 		remoteProvider.init();
 	}
 
 	@Override public void dispose() {
 		unsubscribeFromLocalProvider();
 		localProvider.dispose();
 		unsubscribeFromRemoteProvider();
 		remoteProvider.dispose();
 	}
 
 	@Override public RateValue getRate(String symbolFrom, String symbolTo, Date date) throws CurrencyRateException {
 		RateValue rateValue = localProvider.getRate(symbolFrom, symbolTo, date);
 		if (rateValue == null) {
 			rateValue = remoteProvider.getRate(symbolFrom, symbolTo, date);
 			if (rateValue != null)
 				localProvider.setRate(symbolFrom, symbolTo, date, rateValue);
 				if (hasAnyListener() && !isRemoteProviderObservable)
 					notifyListeners(symbolFrom, symbolTo, date, rateValue);
 		}
 		else if (hasAnyListener() && !isLocalProviderObservable)
 			notifyListeners(symbolFrom, symbolTo, date, rateValue);
 		return rateValue;
 	}
 
 	@Override public Map<Date, RateValue> getRates(String symbolFrom, String symbolTo, Collection<Date> dates) throws CurrencyRateException {
 		Map<Date, RateValue> dateRates = localProvider.getRates(symbolFrom, symbolTo, dates);
 		Collection<Date> missingDates = new HashSet<>(dates);
 		if (!dateRates.isEmpty()) {
 			if (hasAnyListener() && !isLocalProviderObservable)
 				notifyListeners(symbolFrom, symbolTo, dateRates);
 			missingDates.removeAll(dateRates.keySet());
 		}
 		if (!missingDates.isEmpty()) {
 			Map<Date, RateValue> newDateRates = remoteProvider.getRates(symbolFrom, symbolTo, missingDates);
 			if (!newDateRates.isEmpty()) {
 				if (hasAnyListener() && !isRemoteProviderObservable)
 					notifyListeners(symbolFrom, symbolTo, newDateRates);
 				localProvider.setRates(symbolFrom, symbolTo, newDateRates);
 				dateRates = new HashMap<>(dateRates);
 				dateRates.putAll(newDateRates);
 			}
 		}
 		return dateRates;
 	}
 
 	@Override public void addListener(CurrencyRateListener listener) {
 		if (!hasAnyListener()) {
 			subscribeToLocalProvider();
 			subscribeToRemoteProvider();
 		}
 		super.addListener(listener);
 	}
 
 	private void subscribeToLocalProvider() {
 		if (isLocalProviderObservable)
 			((ObservableCurrencyRateProvider)localProvider).addListener(rateListener);
 	}
 
 	private void subscribeToRemoteProvider() {
 		if (isRemoteProviderObservable)
 			((ObservableCurrencyRateProvider)remoteProvider).addListener(rateListener);
 	}
 
 	@Override public void removeListener(CurrencyRateListener listener) {
 		super.removeListener(listener);
 		if (!hasAnyListener()) {
 			unsubscribeFromLocalProvider();
 			unsubscribeFromRemoteProvider();
 		}
 	}
 
 	private void unsubscribeFromLocalProvider() {
 		if (isLocalProviderObservable)
 			((ObservableCurrencyRateProvider)localProvider).removeListener(rateListener);
 	}
 
 	private void unsubscribeFromRemoteProvider() {
 		if (isRemoteProviderObservable)
 			((ObservableCurrencyRateProvider)remoteProvider).removeListener(rateListener);
 	}
 }
