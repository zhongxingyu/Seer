 package pl.edu.agh.iosr.brokers;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class BankierStockIndexProvider extends AbstractStockIndexProvider {
 	
 	private static final String BANKIER_URL = "http://www.bankier.pl/inwestowanie/notowania/indeksy.html";
 
 	static Logger logger = Logger.getLogger(BankierStockIndexProvider.class);
 	
 	int interval;
 	List<StockIndex> indices;
 	Date lastDate = new Date(0);
 
 	private Document doc;
 
 	private SimpleDateFormat longFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	private SimpleDateFormat shortFormatter = new SimpleDateFormat("MM-dd\u00a0HH:mm");
 	
 	
 	public BankierStockIndexProvider(int pollingInterval, List<StockIndex> indices) {
 		this.interval = pollingInterval;
 		this.indices = indices;
 	}
 	
 	@Override
 	public void run() {
 		try {
 			while (!Thread.interrupted()) {
 				checkForUpdates();
 				Thread.sleep(interval);
 			}
 		} catch (InterruptedException e) {
 			// OK
 		}
 	}
 	
 	private void checkForUpdates() {
 		try {
 			System.getProperties().put( "proxySet", "true" );
 			System.getProperties().put( "proxyHost", "wwwgate0.mot.com");
 			System.getProperties().put( "proxyPort", "1080");
 			
 			doc = Jsoup.connect(BANKIER_URL).get();
 			String updateKey = "Aktualizacja:";
 			Date date = longFormatter.parse(doc.getElementsContainingOwnText(updateKey).text().replace(updateKey, "").trim());
 			//if(date.after(lastDate)){
				System.out.println(date);
 				lastDate = date;
 				dispatchAll();
 			//}
 		} catch (IOException e) {
 			logger.error("Couldnt connect with: " + BANKIER_URL);
 		} catch (ParseException e) {
 			logger.error("Unsupported page format: " + e.getMessage());
 		}
 	}
 	
 	private void dispatchAll() throws ParseException {		
 		for (int i=0; i<indices.size(); i++) {
 			StockIndex index = indices.get(i);
 			index = updateStockIndex(index);
 			dispatch(index);
 			indices.set(i, index);
 		}
 	}
 
 	private StockIndex updateStockIndex(StockIndex index) throws ParseException {
 		Elements rows = doc.select("tr#noto:has(td:eq(0) > a[title=" + index.getName() + "])");
 		if (rows.size() != 1)
 			throw new ParseException("Could not find index " + index.getName(), 0);
 		Element row = rows.get(0);
 		BigDecimal value = new BigDecimal(row.child(1).text());
		BigDecimal change = value.subtract(index.getValue()).add(index.getChange());
		
		
 		Date date = shortFormatter.parse(row.child(7).text());	
 		return new StockIndex(index.getKey(), index.getName(), value, change, date.getTime());
 	}
 }
