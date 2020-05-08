 package org.silix.the9ull.microbit.control;
 
 import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.silix.the9ull.microbit.model.PersistenceUtility;
 import org.silix.the9ull.microbit.model.SingletonSessionFactory;
 import org.stringtree.json.JSONReader;
 import org.stringtree.json.JSONValidatingReader;
 
 
 /* Example:
 {
   "result":"success",
   "return":{
     "high":{
       "value":"13.83000",
       "value_int":"1383000",
       "display":"$13.83000",
       "display_short":"$13.83",
       "currency":"USD"},
       "low":{
         "value":"13.40400",
         "value_int":"1340400",
         "display":"$13.40400",
         "display_short":"$13.40",
         "currency":"USD"},
       "avg":{
         "value":"13.61070",
         "value_int":"1361070",
         "display":"$13.61070",
         "display_short":"$13.61",
         "currency":"USD"},
       "vwap":{
         "value":"13.61479",
         "value_int":"1361479",
         "display":"$13.61479",
         "display_short":"$13.61",
         "currency":"USD"},
       "vol":{
         "value":"45864.73597595",
         "value_int":"4586473597595",
         "display":"45,864.73597595\u00a0BTC",
         "display_short":"45,864.74\u00a0BTC",
         "currency":"BTC"},
       "last_local":{
         "value":"13.66001",
         "value_int":"1366001",
         "display":"$13.66001",
         "display_short":"$13.66",
         "currency":"USD"},
       "last":{
         "value":"13.66001",
         "value_int":"1366001",
         "display":"$13.66001",
         "display_short":"$13.66",
         "currency":"USD"},
       "last_orig":{
         "value":"13.66001",
         "value_int":"1366001",
         "display":"$13.66001",
         "display_short":"$13.66",
         "currency":"USD"},
       "last_all":{
         "value":"13.66001",
         "value_int":"1366001",
         "display":"$13.66001",
         "display_short":"$13.66",
         "currency":"USD"},
       "buy":{
         "value":"13.66001",
         "value_int":"1366001",
         "display":"$13.66001",
         "display_short":"$13.66",
         "currency":"USD"},
       "sell":{
         "value":"13.69010",
         "value_int":"1369010",
         "display":"$13.69010",
         "display_short":"$13.69",
         "currency":"USD"}
       }
     }
  */
 
 public class MtGoxInfo {
 	// https://mtgox.com/api/1/BTCEUR/ticker
 	// https://mtgox.com/api/1/BTCUSD/ticker
 	
 	private BigDecimal eur, usd;
 	private long cache; // ms
 	private Session session;
 	
 	public MtGoxInfo(int cache /* s */) {
 		this.cache = cache*1000;
 		session = SingletonSessionFactory.getSession(); // used to load cache
 	}
 	
 	@SuppressWarnings("deprecation")
 	public String download(String url){
 		URL url1;
 		InputStream is = null;
 		DataInputStream dis;
 		String line;
 		String buffer = "";
 
 		try {
 		    url1 = new URL(url);
 		    is = url1.openStream();  // throws an IOException
 		    dis = new DataInputStream(new BufferedInputStream(is));
 		    
 		    while ((line = dis.readLine()) != null) {
 		    	buffer += line;
 		    }
 		} catch (MalformedURLException mue) {
 		     mue.printStackTrace();
 		} catch (IOException ioe) {
 		     ioe.printStackTrace();
 		} finally {
 		    try {
 		        is.close();
 		    } catch (IOException ioe) {
 		        // nothing to see here
 		    }
 		}
 		return buffer;
 	}
 	
 	public static Object jsonToJava(String json) {
 		//debug
 		JSONReader reader = new JSONValidatingReader();
 		Object result = reader.read(json);
 		return result;
 	}
 	
 	public boolean update(String currency) {
 		
 		BigDecimal value;
 		
 		Transaction tx = session.beginTransaction();//needed by newUser()
 		String updated = PersistenceUtility.dictGet("valueBTC"+currency+"time", session);
 		
 		System.out.println(">> "+updated);
 		System.out.println(" long updated  "+new Long(updated));
 		System.out.println(" date  "+new Date().getTime());
 		System.out.println(" cache  "+cache);
 		
 		if(updated!=null && new Date().getTime() - new Long(updated) < cache ){
 			System.out.println("update: Reading from cache");
 			value = new BigDecimal(PersistenceUtility.dictGet("valueBTC"+currency, session));
 			tx.commit();
 			if(currency=="EUR")
 				eur = value;
 			else if(currency=="USD")
 				usd = value;
 			
 			return true;
 		}
 		
 		String json = download("https://mtgox.com/api/1/BTC"+currency+"/ticker");
 		
 		@SuppressWarnings("unchecked")
 		Map<String,Object> o = 
 			(Map<String,Object>) jsonToJava(json);
 		
 		if(o.get("result").equals("success")){
 			@SuppressWarnings("unchecked")
 			Map<String,Object> oo = (Map<String,Object>) o.get("return");
 			@SuppressWarnings("unchecked")
 			Map<String,Object> ooo = (Map<String,Object>) oo.get("vwap");
 			value = new BigDecimal((String) ooo.get("value"));
 			System.out.println(value);
 			
 			// Update cache
 			PersistenceUtility.dictSet("valueBTC"+currency+"time", "" + new Date().getTime(), session);
 			PersistenceUtility.dictSet("valueBTC"+currency+"humantime", "" + new Date(), session);
 			PersistenceUtility.dictSet("valueBTC"+currency,""+value,session);
 			tx.commit();
 			
 			if(currency=="EUR"){
 				eur = value;
 				return true;
 			}
 			if(currency=="USD"){
 				usd = value;
 				return true;
 			}
 			
 			System.out.println("Wrong parameter");
 			assert(false);
 			
 		}
 		return false;
 	}
 	
 	
 	public BigDecimal getEur() {
 		return eur;
 	}
 
 	public BigDecimal getUsd() {
 		return usd;
 	}
 
	public void setEur(BigDecimal eur) {
		this.eur = eur;
	}

	public void setUsd(BigDecimal usd) {
		this.usd = usd;
	}

 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		MtGoxInfo mtgox = new MtGoxInfo(10*60);
 		
 		//mtgox.proxy.call("api/1/BTCEUR/ticker");
 		mtgox.update("EUR");
 		mtgox.update("USD");
 		mtgox.getEur();
 		mtgox.getUsd();
 	}
 
 }
