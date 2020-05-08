 package org.strangeforest.currencywatch.db4o;
 
 import java.io.*;
 import java.util.*;
 
 import org.strangeforest.currencywatch.core.*;
 
 import com.db4o.*;
 import com.db4o.config.*;
 
 public class Db4oCurrencyRateProvider extends BaseCurrencyRateProvider implements UpdatableCurrencyRateProvider {
 
 	private final EmbeddedObjectContainer db;
 
 	public Db4oCurrencyRateProvider(String dbFileName) {
 		super();
 		new File(dbFileName).getParentFile().mkdirs();
 		EmbeddedConfiguration dbConfig = Db4oEmbedded.newConfiguration();
 		ObjectClass currencyRateConfig = dbConfig.common().objectClass(CurrencyRateObject.class);
 		currencyRateConfig.objectField("symbolFrom").indexed(true);
 		currencyRateConfig.objectField("symbolTo").indexed(true);
 		currencyRateConfig.cascadeOnUpdate(true);
 		currencyRateConfig.cascadeOnDelete(true);
 		db = Db4oEmbedded.openFile(dbConfig, dbFileName);
 	}
 
	@Override public void close() {
 		db.close();
 		super.close();
 	}
 
 	private CurrencyRateObject getCurrencyRate(ObjectContainer db, String symbolFrom, String symbolTo) {
 		CurrencyRateObject template = new CurrencyRateObject(symbolFrom, symbolTo);
 		ObjectSet<CurrencyRateObject> rates = db.queryByExample(template);
 		switch (rates.size()) {
 			case 0: return null;
 			case 1: return rates.get(0);
 			default: throw new IllegalStateException(String.format("Multiple currency rates found for %1$s.", template));
 		}
 	}
 
 	@Override public RateValue getRate(final String symbolFrom, final String symbolTo, final Date date) {
 		return queryDb4o(new Db4oQueryCallback<RateValue>() {
 			@Override public RateValue queryDb4o(ObjectContainer db) {
 				CurrencyRateObject rate = getCurrencyRate(db, symbolFrom, symbolTo);
 				return rate != null ? rate.getRate(date) : null;
 			}
 		});
 	}
 
 	@Override public Map<Date, RateValue> getRates(final String symbolFrom, final String symbolTo, final Collection<Date> dates) {
		return queryDb4o(new Db4oQueryCallback<Map<Date, RateValue>>() {
 			@Override public Map<Date, RateValue> queryDb4o(ObjectContainer db) {
 				Map<Date, RateValue> dateRates = new HashMap<>();
 				CurrencyRateObject rate = getCurrencyRate(db, symbolFrom, symbolTo);
 				if (rate != null) {
 					dateRates.putAll(rate.getRates());
 					dateRates.keySet().retainAll(dates);
 				}
 				return dateRates;
 			}
 		});
 	}
 
 	@Override public void setRate(final String symbolFrom, final String symbolTo, final Date date, final RateValue rateValue) {
 		doInDb4o(new Db4oCallback() {
 			@Override public void doInDb4o(ObjectContainer db) {
 				CurrencyRateObject rate = getCurrencyRate(db, symbolFrom, symbolTo);
 				if (rate == null)
 					rate = new CurrencyRateObject(symbolFrom, symbolTo);
 				rate.setRate(date, rateValue);
 				db.store(rate);
 			}
 		});
 	}
 
 	@Override public void setRates(final String symbolFrom, final String symbolTo, final Map<Date, RateValue> dateRates) {
 		doInDb4o(new Db4oCallback() {
 			@Override public void doInDb4o(ObjectContainer db) {
 				CurrencyRateObject rate = getCurrencyRate(db, symbolFrom, symbolTo);
 				if (rate == null)
 					rate = new CurrencyRateObject(symbolFrom, symbolTo);
 				rate.setRates(dateRates);
 				db.store(rate);
 			}
 		});
 	}
 
 	private synchronized void doInDb4o(Db4oCallback callback) {
 		try {
 			callback.doInDb4o(db);
 			db.commit();
 		}
 		catch (Throwable th) {
 			db.rollback();
 			throw th;
 		}
 	}
 
 	private synchronized <T> T queryDb4o(Db4oQueryCallback<T> callback) {
		return callback.queryDb4o(db);
 	}
 
 	private static interface Db4oCallback {
 		public void doInDb4o(ObjectContainer db);
 	}
 
 	private static interface Db4oQueryCallback<T> {
 		public T queryDb4o(ObjectContainer db);
 	}
 }
