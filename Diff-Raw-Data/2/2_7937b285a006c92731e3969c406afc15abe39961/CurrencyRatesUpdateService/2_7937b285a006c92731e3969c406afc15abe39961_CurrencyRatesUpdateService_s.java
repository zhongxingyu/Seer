 package si.kcclass.currencyconverter.services;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.log4j.Logger;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 
 import si.kcclass.currencyconverter.domain.ForeignCurrency;
 import si.kcclass.currencyconverter.domain.ForeignCurrencyToEuroRate;
 
 @Service
 public class CurrencyRatesUpdateService {
 
	protected static Logger logger = Logger.getLogger("service");
 
 	private Set<String> getSupportedCurrencies() throws IOException {
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpGet httpGet = new HttpGet("http://currencies.apps.grandtrunk.net/currencies");
 		ResponseHandler<String> responseHandler = new BasicResponseHandler();
 		String responseBody = httpclient.execute(httpGet, responseHandler);
 		StringTokenizer responseTokenizer = new StringTokenizer(responseBody);
 		Set<String> supportedCurrencies = new HashSet<String>();
 		while (responseTokenizer.hasMoreTokens()) {
 			String currencySymbol = responseTokenizer.nextToken();
 			supportedCurrencies.add(currencySymbol);
 		}
 		httpclient.getConnectionManager().shutdown();
 		return supportedCurrencies;
 	}
 	
 	private double convert(String currencyFrom, String currencyTo) throws IOException {
 		HttpClient httpclient = new DefaultHttpClient();
 		String conversionUrl = String.format("http://currencies.apps.grandtrunk.net/getlatest/%s/%s", 
 				currencyFrom, currencyTo);
 		HttpGet httpGet = new HttpGet(conversionUrl);
 		ResponseHandler<String> responseHandler = new BasicResponseHandler();
 		String responseBody = httpclient.execute(httpGet, responseHandler);
 		httpclient.getConnectionManager().shutdown();
 		return Double.parseDouble(responseBody);
 	}
 
 	@Scheduled(fixedRate = 24 * 3600 * 1000)
 	public void getCurrencyRates() {
 		logger.debug("Start currency rates update service.");
 		try {
 			Set<String> supportedCurrencies = getSupportedCurrencies();
 			List<ForeignCurrency> currencies = ForeignCurrency.findAllForeignCurrencys();
 			Date updateTimestamp = new Date();
 			for (ForeignCurrency currency: currencies) {
 				if (supportedCurrencies.contains(currency.getSymbol())) {
 					double toEurConversionRate = convert(currency.getSymbol(), "EUR");
 					logger.debug(String.format("%s to EUR conversion rate: %f",
 							currency.getSymbol(), toEurConversionRate));
 					ForeignCurrencyToEuroRate toEurRate = new ForeignCurrencyToEuroRate();
 					toEurRate.setCurrency(currency);
 					toEurRate.setDateOfConversion(updateTimestamp);
 					toEurRate.setConversionRate(toEurConversionRate);
 					toEurRate.persist();
 				}
 			}
 		} catch (IOException e) {
 			logger.error(String.format("Conversion from %s to %s failed", "USD", "EUR"), e);
 			e.printStackTrace();
 		}
 		logger.debug("End currency rates update service.");
 	}
 
 }
