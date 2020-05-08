 package com.jgrue.vgpc.scrapers;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 import android.util.Log;
 
 import com.jgrue.vgpc.data.FullGame;
 import com.jgrue.vgpc.data.Price;
 import com.jgrue.vgpc.data.Store;
 
 public class GameScraper {
 	private static final String TAG = "GameScraper";
 	
 	public static FullGame getFullGame(String upc) {
 		return getFullGameInternal("http://videogames.pricecharting.com/search/?q=" + upc);
 	}
 	
 	public static FullGame getFullGame(String gameAlias, String consoleAlias) {
 		return getFullGameInternal("http://videogames.pricecharting.com/game/" + consoleAlias + "/" + gameAlias);
 	}
 	
 	private static FullGame getFullGameInternal(String urlString) {
 		FullGame game = new FullGame();
 		
 		try {
 			URL url = new URL(urlString);
 			Log.i(TAG, "Target URL: " + url.toString());
 			Document document = Jsoup.parse(url, 30000);
 			Log.i(TAG, "Retrieved URL: " + document.baseUri());
 			
 			// Check to see whether we found anything.
 			String results[] = document.baseUri().substring(36).split("/");
 			
 			if(!results[1].startsWith("no_hits")) {
 				game.setGameName(document.select("h1#product_name").get(0).text());
 				game.setGameAlias(document.baseUri().split("/")[5]);
 				game.setConsoleName(document.select("div#game-page h2.chart_title a").get(0).text());
 				game.setConsoleAlias(document.baseUri().split("/")[4]);
 				//game.setLastObservation(document.select("div#price_data div.prices_now p").get(0).html().substring(18).split("<")[0]);
 				try {
 					game.setVolume(document.select("a#volume_link_used").get(0).text());
 				} catch (IndexOutOfBoundsException e) {
 					Log.e(TAG, "Error parsing volume for " + game.getGameName() + ".");
 					game.setVolume("unknown");
 				}
 				try {
 					game.setUsedPrice(Float.parseFloat(document.select("td#used_price span.price").get(0).text().substring(1).replace(",", "")));
 				} catch (NumberFormatException e) {
 					Log.e(TAG, "Error parsing used price (" + document.select("td#used_price span.price").get(0).text().substring(1) + ") for " + game.getGameName() + ".");
 					game.setUsedPrice(0.0f);
 				} catch (IndexOutOfBoundsException e) {
 					Log.e(TAG, "Used price was not found.");
 				}
 				try {
 					game.setNewPrice(Float.parseFloat(document.select("td#new_price span.price").get(0).text().substring(1).replace(",", "")));
 				} catch (NumberFormatException e) {
 					Log.e(TAG, "Error parsing new price (" + document.select("td#new_price span.price").get(0).text().substring(1) + ") for " + game.getGameName() + ".");
 					game.setNewPrice(0.0f);
 				} catch (IndexOutOfBoundsException e) {
 					Log.e(TAG, "New price was not found.");
 				}
 			}
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return game;
 	}
 	
 	public static List<Store> getStores(String gameAlias, String consoleAlias) {
 		List<Store> storeList = new ArrayList<Store>();
 		
 		try {
 			URL url = new URL("http://videogames.pricecharting.com/game/" + consoleAlias + "/" + gameAlias);
 			Log.i(TAG, "Target URL: " + url.toString());
 			Document document = Jsoup.parse(url, 30000);
 			Log.i(TAG, "Retrieved URL: " + document.baseUri());
 			
			Elements tableRows = document.select("div#price_comparison table.used-prices tr");
			for(int i = 1; i < tableRows.size(); i++) {
 				Elements tableData = tableRows.get(i).select("td");
 				 
 				Store newStore = new Store();
 				newStore.setStoreName(tableData.get(0).text());
 				newStore.setStoreLink(tableData.get(2).select("a").first().attr("href"));
 
 				if(tableData.get(1).text().startsWith("$"))
 					newStore.setStorePrice(Float.parseFloat(tableData.get(1).text().substring(1).replace(",", "")));
 				else
 					newStore.setStorePrice(0.0f);
 				 
 				storeList.add(newStore);
 			}
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return storeList;
 	}
 	
 	public static List<Price> getPriceHistory(String gameAlias, String consoleAlias) {
 		List<Price> priceList = new ArrayList<Price>();
 		
 		try {
 			URL url = new URL("http://videogames.pricecharting.com/game/" + consoleAlias + "/" + gameAlias);
 			Log.i(TAG, "Target URL: " + url.toString());
 			Document document = Jsoup.parse(url, 30000);
 			Log.i(TAG, "Retrieved URL: " + document.baseUri());
 			
 			String javaScript = document.select("div.container div.content div.mid_col script").first().html();
 			Matcher matcher = Pattern.compile("\\[.*\\];").matcher(javaScript);
 			
 			if(matcher.find()) {
 				String[] jsPriceArray = matcher.group().substring(1, matcher.group().length() - 2).split("\\],\\[");
 				for(int i = 0; i < jsPriceArray.length; i++) {
 					if(i == 0 && jsPriceArray[i].length() == 0)
 						break;
 					
 					if(i == 0)
 						jsPriceArray[i] = jsPriceArray[i].substring(1).trim();
 					else if(i == jsPriceArray.length - 1)
 						jsPriceArray[i] = jsPriceArray[i].substring(0, jsPriceArray[i].length() - 1).trim();
 					else
 						jsPriceArray[i] = jsPriceArray[i].trim();
 					
 					String[] priceDate = jsPriceArray[i].split(", ");
 					String[] dateParts = priceDate[0].substring(9, priceDate[0].length() - 1).split(",");
 					Calendar priceCal = Calendar.getInstance();
 					priceCal.set(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]), 
 							Integer.parseInt(dateParts[2]), 0, 0, 0);
 					
 					Price newPrice = new Price();
 					newPrice.setPrice(Float.parseFloat(priceDate[1].replace(",", "")));
 					newPrice.setPriceDate(priceCal.getTime());
 					priceList.add(newPrice);
 				}
 			}
 			
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		Log.i(TAG, "Returning from getPriceHistory with " + priceList.size() + " items.");
 		return priceList;
 	}
 }
