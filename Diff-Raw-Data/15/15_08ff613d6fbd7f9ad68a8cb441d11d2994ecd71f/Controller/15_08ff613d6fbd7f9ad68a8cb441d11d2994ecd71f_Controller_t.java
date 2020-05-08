 package controller;
 
 import gui.KundGUI;
 
 import java.util.ArrayList;
 
 import db.Customer;
 import db.Database;
 import db.ReceiptArticle;
 
 public class Controller {
 	private Database db;
 	private KundGUI gui;
 
 	public Controller(KundGUI gui) {
 		this.gui = gui;
 
 		this.db = new Database();
 		if (db.openConnection("db/db", "SA", "")) System.out.println("CONTROLLER: Connection Established");
 		else System.out.println("CONTROLLER: False");
 		// DatabaseManager.threadedDBM();
 
 	}
 
 	public ArrayList<Integer> getPaidReceipts() {
 		return db.getPaidReceipts();
 	}
 
 	public ArrayList<Integer> getUnPaidReceipts() {
 		return db.getUnPaidReceipts();
 	}
 
 	public Customer getCustomerInfo(String receiptId) {
 		return db.getCustomerInfo(receiptId);
 	}
 
 	public String getArticlesFromReceipt(String receiptId) {
 		//FIXME Kolla om priset p en produkt slutar med jmt tiotal ren, dvs 0.5 ska bli 0.50
 		ArrayList<ReceiptArticle> articles = db.getReceiptInfo(receiptId).getReceiptItems();
 		String articleList = "";
		String priceSingle, priceTotal;
		
 		for (ReceiptArticle article : articles) {
			priceSingle = addTrailingZero("" + article.getPrice());
			priceTotal = addTrailingZero("" + (article.getPrice()*article.getAmount()));
			
			articleList = articleList + article.getArticleName() + "\t" + article.getAmount() + "st\t" + priceSingle + ":-/st\t" + priceTotal + ":- \n";
 		}
 
 		return articleList;
 	}
	
	private String addTrailingZero(String price){
		String fixedNumber = price.replace('.', ',');
		if((price.length() - 1) - price.indexOf(".") == 1) fixedNumber += "0";
		return fixedNumber;
	}
 
 	public Boolean isPaid(String receiptId) {
 		return db.isPaid(receiptId);
 	}
 
 	public void receiptIsPaidFor(String receiptId, boolean isPaidFor) {
 		db.receiptIsPaidFor(receiptId, isPaidFor);
 	}
 	
 	public void closeDBConnection(){
 		db.closeConnection();
 	}
 	
 }
