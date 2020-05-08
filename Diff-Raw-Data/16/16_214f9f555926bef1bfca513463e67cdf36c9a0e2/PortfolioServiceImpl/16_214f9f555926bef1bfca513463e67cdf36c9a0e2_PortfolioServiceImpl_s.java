 package com.focustech.portfolio.service;
 
 import com.focustech.portfolio.domain.Portfolio;
 import com.focustech.portfolio.domain.PortfolioTotal;
 import com.csvreader.CsvReader;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 
 import org.apache.http.client.methods.*;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.HttpStatus;
 
 public class PortfolioServiceImpl implements PortfolioService {
 
 	public void savePortfolio(Portfolio portfolio) {
 		portfolio.setTicker(portfolio.getTicker().toUpperCase());
		double d = getCurrentPrice(portfolio.getTicker());
		portfolio.setCurrent_price(d);
 		portfolio.persist();
 
 	}
 
 	public PortfolioTotal getPortfolioTotal(List<Portfolio> portfoliolist) {
 		float pTotal = 0;
 		for (Portfolio portfolio : portfoliolist) {
 			pTotal += portfolio.getCurrent_price() * portfolio.getQuantity();
 		}
 		PortfolioTotal t = new PortfolioTotal();
 		t.setPortfolio_total(pTotal);
 		return t;
 	}
 
 	public double getCurrentPrice(String ticker) {
 		String uri = buildURI(ticker);
 		// doing the call
 		String responseBody = "";
 		try {
 			responseBody = doCall(uri);
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		// parse results
 		try {
 			HashMap h = extractValues(responseBody);
 
 			String price = (String) h.get(ticker.toUpperCase());
 			return (Double.parseDouble(price));
 
 		} catch (IOException e) {
 
 		}
 		return 0;
 	}
 
 	public void getCurrentPrices(List<Portfolio> portfoliolist) {
 
 		// creating the request URI
 		String uri = buildURI(portfoliolist);
 
 		System.out.println("calling :" + uri);
 
 		// doing the call
 		String responseBody = "";
 		try {
 			responseBody = doCall(uri);
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		// System.out.println(responseBody);
 
 		// parse results
 		try {
 			HashMap h = extractValues(responseBody);
 			for (Portfolio p : portfoliolist) {
 				String price = (String) h.get(p.getTicker());
 				p.setCurrent_price(Double.parseDouble(price));
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private HashMap extractValues(String responseBody) throws IOException {
 
 		HashMap<String, String> h = new HashMap<String, String>();
 		CsvReader csvReader = new CsvReader(new StringReader(responseBody));
 
 		// populating the lists
 		while (csvReader.readRecord()) {
 
 			String symbol = csvReader.get(0);
 			String price = csvReader.get(1);
 			h.put(symbol, price);
 
 		}
 
 		return h;
 	}
 
 	private String doCall(String uri) throws Exception {
 		HttpClient client = new DefaultHttpClient();
 		HttpGet method = new HttpGet(uri);
 
 		HttpResponse httpResponse = client.execute(method);
 		int statusCode = httpResponse.getStatusLine().getStatusCode();
 		if (statusCode == HttpStatus.SC_OK) {
 			InputStream stream = httpResponse.getEntity().getContent();
 			// do something with the input stream
 
 			try {
 
 				String responseText = responseToString(stream);
 
 				return responseText;
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 
 			}
 		}
 		return null;
 	}
 
 	private String responseToString(InputStream stream) throws IOException {
 		BufferedInputStream bi = new BufferedInputStream(stream);
 
 		StringBuilder sb = new StringBuilder();
 
 		byte[] buffer = new byte[1024];
 		int bytesRead = 0;
 		while ((bytesRead = bi.read(buffer)) != -1) {
 			sb.append(new String(buffer, 0, bytesRead));
 		}
 		return sb.toString();
 	}
 
 	private String buildURI(List<Portfolio> portfoliolist) {
 		StringBuilder uri = new StringBuilder();
 		uri.append("http://finance.yahoo.com/d/quotes.csv");
 		uri.append("?s=");
 		int i = 0;
 		while (i < portfoliolist.size()) {
 			Portfolio p = portfoliolist.get(i);
 			uri.append(p.getTicker());
 			i++;
 			if (i < portfoliolist.size()) {
 				uri.append("+");
 			}
 		}
 		uri.append("&f=sl1");
 
 		return uri.toString();
 	}
 
 	private String buildURI(String ticker) {
 		StringBuilder uri = new StringBuilder();
 		uri.append("http://finance.yahoo.com/d/quotes.csv");
 		uri.append("?s=");
 		uri.append(ticker);
 		uri.append("&f=sl1");
 		return uri.toString();
 
 	}
 
 }
