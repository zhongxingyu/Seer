 package edu.upenn.cis350.project;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.parse.ParseObject;
 
 public class DataBaser {
 	private static final DataBaser instance = new DataBaser();
 	HashMap<String,String> info;
 	ArrayList<Transaction> transactions;
 	HashMap<String,Integer> inventory1;
 	HashMap<String,Integer> inventory2;
 
 	private DataBaser() {
 		info = new HashMap<String,String>();
 		transactions = new ArrayList<Transaction>();
 		inventory1 = new HashMap<String,Integer>();
 		inventory2 = new HashMap<String,Integer>();
 	}
 
 	public static DataBaser getInstance() {
 		return instance;
 	}
 
 	public void databaseItThoroughly() {
 		//Save the generic information
 		ParseObject obj = new ParseObject("fruitStandInfo");
 		for(String k : info.keySet()) {
 			obj.add(k, info.get(k));
 		}
 		obj.add("preprocess_inventory", inventory1);
 		obj.add("postprocess_inventory", inventory2);
 		obj.saveInBackground();
 		
 		String school = info.get("school");
 		//Save each transaction
 		for(Transaction t : transactions) {
 			ParseObject obj_t = new ParseObject("fruitStandTransactions");
 			obj_t.add("school", school);
 			obj_t.add("age", t.age);
 			obj_t.add("gender", t.gender);
 			obj_t.add("purchases", t.purchases);
 			obj_t.add("payments", t.payments);
 			obj.saveInBackground();
 		}
 	}
 	
 	public void addInfo(String key, String value) {
 		info.put(key, value);
 	}
 	
 	public void savePreInventory(HashMap<String,Integer> inv) {
 		inventory1.putAll(inv);
 	}
 	
 	public void savePostInventory(HashMap<String,Integer> inv) {
 		inventory2.putAll(inv);
 	}
 	
 	public void addTransaction(HashMap<String, Integer> purchases,
 			HashMap<String, Integer> payments, String gender, String age) {
 		Transaction t = new Transaction(purchases, payments, gender, age);
 		transactions.add(t);
 	}
 	
 	public double getStartingCashboxAmount() {
 		if(info.containsKey("cashbox_starting_amount")) {
 			return Double.valueOf(info.get("cashbox_starting_amount"));
 		}
 		return 10.0;
 	}
 }
