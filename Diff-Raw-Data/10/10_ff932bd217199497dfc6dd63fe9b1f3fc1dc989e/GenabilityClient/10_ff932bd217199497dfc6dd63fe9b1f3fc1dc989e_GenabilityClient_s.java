 package com.tendril.cleanweb.client;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.ISODateTimeFormat;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 import com.tendril.cleanweb.domain.genability.CalculateInput;
 import com.tendril.cleanweb.domain.genability.CalculatedCost;
 import com.tendril.cleanweb.domain.genability.Tariff;
 import com.tendril.cleanweb.domain.genability.TariffInput;
 import com.tendril.cleanweb.domain.genability.TariffList;
 
 public class GenabilityClient {
 
 	private static final String APP_ID = "b9c201aa";
 	private static final String APP_KEY = "3ffccf70d6caa98d680c673fff878af1";
 
 	public List<Tariff> getTariffs(String zipCode) {
 		List<Tariff> results = new ArrayList<Tariff>();
 
 		WebResource webResource = Client.create().resource(
 				"http://api.genability.com/rest/public/tariffs");
 
 		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
 		params.add("appId", APP_ID);
 		params.add("appKey", APP_KEY);
 		params.add("zipCode", zipCode);
 		params.add("customerClasses", "RESIDENTIAL");
 		params.add("tariffTypes", "DEFAULT");
 
 		TariffList tariffList = webResource.queryParams(params).get(
 				TariffList.class);
 
 		if (tariffList.getCount() > 0) {
 			for (Tariff tariff : tariffList.getResults()) {
 				if (tariff.getIsActive())
 					results.add(tariff);
 			}
 		}
 
 		return results;
 	}
 
 	public double getCost(Long tariffId, List<TariffInput> tariffInputs) {
 		double result = 0.0d;
 
 		WebResource webResource = Client.create().resource(
 				"http://api.genability.com/rest/alpha/calculate/" + tariffId);
 
 		DateTime fromDate = DateTime.now();
 		DateTime toDate = DateTime.now().minusYears(10);
 		for (TariffInput tariffInput : tariffInputs) {
 			DateTime newFromDate = DateTime
 					.parse(tariffInput.getFromDateTime());
 			fromDate = fromDate.isBefore(newFromDate) ? fromDate : newFromDate;
 			DateTime newToDate = DateTime.parse(tariffInput.getToDateTime());
 			toDate = toDate.isAfter(newToDate) ? toDate : newToDate;
 		}
 
 		CalculateInput calculateInput = new CalculateInput();
 		calculateInput.setAppId(APP_ID);
 		calculateInput.setAppKey(APP_KEY);
		calculateInput.setFromDateTime(ISODateTimeFormat
				.basicDateTimeNoMillis().print(fromDate));
		calculateInput.setToDateTime(ISODateTimeFormat.basicDateTimeNoMillis()
				.print(toDate));
 		calculateInput.setDetailLevel("TOTAL");
 
 		calculateInput.setTariffInputs(tariffInputs);
 
 		CalculatedCost calculatedCost = webResource.type(
 				MediaType.APPLICATION_JSON_TYPE).post(CalculatedCost.class,
 				calculateInput);
 
 		if (calculatedCost.getResults() != null
 				&& calculatedCost.getResults().size() > 0) {
 			result = calculatedCost.getResults().get(0).getTotalCost();
 		}
 
 		return result;
 	}
 }
