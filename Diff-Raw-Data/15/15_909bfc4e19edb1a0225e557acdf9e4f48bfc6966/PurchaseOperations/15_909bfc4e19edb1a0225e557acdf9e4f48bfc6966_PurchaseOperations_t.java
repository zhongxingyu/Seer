 import java.util.ArrayList;
 import java.util.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class PurchaseOperations extends AbstractTableOperations {
 	
 	//TODO test 
 	boolean insert(java.sql.Date date,String cid,String cardno, String expdate, java.sql.Date expected,java.sql.Date delivered){
 		try{
 			ps=con.prepareStatement("INSERT INTO purchase VALUES (purchase_receiptId.nextval,?,?,?,?,?,?)");
 			if (date!=null){
 				ps.setDate(1, date);
 			}
 			else{
 				ps.setDate(1,null);
 			}
 			if(cid!=null){
 				ps.setString(2,cid);
 			}
 			else{
 				ps.setString(2,null);
 			}
 			if (cardno!=null){
 				ps.setString(3,cardno);
 			}
 			else{
 				ps.setString(3, null);
 			}
 			if(expdate!=null){
 				ps.setString(4,expdate);
 			}
 			else{
 				ps.setString(4,null);
 			}
 			if(expected!=null){
 				ps.setDate(5, expected);
 			}
 			else{
 				ps.setDate(5,null);
 			}
 			if(delivered!=null){
 				ps.setDate(6,delivered);
 			}
 			else{
 				ps.setDate(6,null);
 			}
 			ps.executeUpdate();
 			con.commit();
 			return true;
 		}
 		catch (SQLException ex){
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 
 			try
 			{
 				con.rollback();
 				return false; 
 			}
 			catch (SQLException ex2)
 			{
 				event = new ExceptionEvent(this, ex2.getMessage());
 				fireExceptionGenerated(event);
 				return false; 
 			}
 		}
 	}
 
 	boolean updateDeliveryDate(Integer receiptId,String stringdate){
 		try{
 			ps = con.prepareStatement("UPDATE purchase SET delivereddate = ? WHERE receiptId = ?");
 			if(receiptId!=null){
 				ps.setInt(2, receiptId);
 			}
 			else ps.setNull(2,Types.INTEGER);
 			if(stringdate!=null){
 				SimpleDateFormat fm = new SimpleDateFormat("dd/MM/yy");
 				java.util.Date utilDate = fm.parse(stringdate);
 				java.sql.Date sqldate = new java.sql.Date(utilDate.getTime());
 				ps.setDate(1,sqldate);
 			}
 			else{
 				ps.setDate(1,null);
 			}
 			ps.executeUpdate();
 			con.commit();
 			System.out.println("Delivery Date Set");
 			return true;
 		}
 		catch(SQLException | ParseException ex){
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 
 			try {
 				con.rollback();
 				return false; 
 			}
 			catch (SQLException ex2) {
 				event = new ExceptionEvent(this, ex2.getMessage());
 				fireExceptionGenerated(event);
 				return false; 
 			}
 		}
 	}
 
 
 	//TODO test 
 	boolean delete(Integer receiptId, Integer upc){
 		try {
 			ps = con.prepareStatement("DELETE FROM purchase WHERE receiptId = ? AND upc = ?");
 			ps.setInt(1, receiptId);
 			ps.setInt(2, upc);
 			ps.executeUpdate();
 			con.commit();
 			return true;
 		}
 		catch (SQLException ex) {
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 
 			try {
 				con.rollback();
 				return false;
 			}
 			catch (SQLException ex2) {
 				event = new ExceptionEvent(this, ex2.getMessage());
 				fireExceptionGenerated(event);
 				return false;
 			}
 		}
 	}
 
 	/*
 	 * Display a read-only set of tuples from ReturnItem table
 	 */
 	public ResultSet display(){
 		try {
 			ps = con.prepareStatement("SELECT * FROM purchase", 
 					ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 
 			ResultSet rs = ps.executeQuery();
 
 			return rs; 
 		}
 		catch (SQLException ex) {
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 			// no need to commit or rollback since it is only a query
 
 			return null; 
 		}
 	}
 	/*	
 	 * Given a receipt number, this query lists all the items purchased, their 
 	 * quantities and their prices. Total amount for the purchase to be calculated in Java code.
 	 * */
 	public void listItems(int receiptId){
 		String q = "SELECT ititle as Title, I.upc, quantity, price FROM Item I, PurchaseItem PI WHERE I.upc=PI.upc AND receiptId=?";
 //		int totalQty = 0; //uncomment for other uses
 		double subtotal = 0.0;
 		double totalPrice;
 		double tax = 1.12;
 		try {
 			ps = con.prepareStatement(q);
 
 			ps.setInt(1, receiptId);
 
 			ResultSet rs = ps.executeQuery();
 			System.out.println("Item    " + "\t" + "Quantity" + "\t" + "Price");
 			System.out.println("-----------" + "\t" + "--------" + "\t" + "-----");
 			while (rs.next()) {
 
 				String item = rs.getString("Title");
 				int qty = rs.getInt("quantity");
 //				totalQty += qty; //uncomment for other uses
 				double price = rs.getDouble("price");
 				subtotal += price;
 				System.out.printf(item + "%-12.12s" +  qty + "%-6.6s" + price + "\n", " ", " " );
 				//System.out.println(item + "\t" + qty + "\t" + price);          // ************ item, qty, price OUTPUT TO GUI
 			}
 			System.out.println("\n\nSubtotal: "  + subtotal);                      // ************ subtotal OUTPUT TO GUI
 			totalPrice = subtotal*tax; 
 			DecimalFormat twoDForm = new DecimalFormat("#.##");
 			totalPrice = (double) Double.valueOf(twoDForm.format(totalPrice)); // ************ totalPrice OUTPUT TO GUI
 			System.out.println("Total: "  + totalPrice + "\n");
 			System.out.println("Thank you for shoopping at AMS!\n");
 
 		}
 		catch (SQLException ex) {
 			System.out.println("exception");
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 			// no need to commit or rollback since it is only a query
 
 
 		}
 	}
 	
 	/*	
 	 * Given a receipt number, finds purchase date, and last 5 digits of the card's number if exists
 	 */	
 	public void getDateCard(int receiptId){
 		String cardno;
 		String q =  "SELECT pdate as PurchaseDate, 'xxxxxxxxxxx' || SUBSTR(cardno, 12,5) as CardNumber " +
 					"FROM Purchase " +
 					"WHERE receiptId = ? AND cardno IS NOT NULL " +
 					"UNION " +
 					"SELECT pdate, cardno " +
 					"FROM Purchase " +
 					"WHERE receiptId = ? AND cardno IS NULL";
 
 		try {
 			ps = con.prepareStatement(q);
 
 			ps.setInt(1, receiptId);
 			ps.setInt(2, receiptId);
 
 			ResultSet rs = ps.executeQuery();
 			
 
 
 			if(rs.next()){ // if cardno is null, don't assign cardno
 				cardno = rs.getString("CardNumber"); // assign cardno string
 			} else {
 				cardno = rs.getString("CardNumber"); // assign cardno string
 				 }
 			
 			
 			Date pdate = rs.getDate("PurchaseDate");
 
 
 			if(cardno == null){
 				System.out.println("Receipt Number" + "\t" + "Purchase Date");
 			System.out.println("--------------" + "\t" + "-------------");
 				System.out.printf(receiptId + "%-12.12s" +  pdate + "\n\n", " " );  // ************ receiptId, pdate, cardno OUTPUT TO GUI
 		}else {
 				System.out.println("Receipt Number" + "\t" + "Purchase Date" + "\t" + "Credit Card");
 			System.out.println("--------------" + "\t" + "-------------" + "\t" + "-----------------");
 				System.out.printf(receiptId + "%-12.12s" +  pdate + "%-6.6s" + cardno + "\n\n", " ", " " );}  // ************ receiptId, pdate, cardno OUTPUT TO GUI
 
 
 			//System.out.println(receiptId + "\t" +  pdate + "\t" + cardno );  // ************ receiptId, pdate, cardno OUTPUT TO GUI
 
 
 
 		}
 		catch (SQLException ex) {
 			System.out.println("exception");
 			System.out.println(ex.getMessage());
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 			// no need to commit or rollback since it is only a query
 
 
 		}
 	}
 	
 	/*	Prints out receipt number, the date, 
 	a list with the items purchased, their quantities and their prices, and the total 
 	amount for the purchase.  If the customer pays by a credit card, the receipt should 
 	show the last 5 digits of the card's number.
 	 */	
 	public void printReceipt(int receiptId){
 		getDateCard(receiptId);
 		listItems(receiptId);
 	}
 	
 	//This method checks whether the items requested for purchase
 	//also adds it to the array list in mainframe
 	boolean isInStock (String upc, Integer qty){
 		
 		try{
 			
 			ps = con.prepareStatement("SELECT upc, price  FROM item where upc = ? AND stock >= ?");
 			String rsupc;
 			Double rsprice;
 			String displayItem;
 			
 			if (upc != null)
 			{
 				ps.setString(1, upc);
 			}
 			else
 			{
 				ps.setString(1, null);
 			}
 
 			if(qty!=null){
 				ps.setInt(2, qty);
 			}
 			else ps.setNull(2,Types.INTEGER);
 
 			System.out.println("About to execute query for instore purchase");
 			
 			ResultSet rs = ps.executeQuery();
 			System.out.println("Just executed query and about to insert to GUI ItemList");
 		
 			while (rs.next())
 			{
 				rsupc = rs.getString(1);
 				System.out.println(rsupc);
 				rsprice = rs.getDouble(2);
 				System.out.println(rsprice);
 				displayItem = "                    "+rsupc+"               "+rsprice.toString()+"               "+qty.toString();
 				MainFrame.addInstoreItemToPurchase(displayItem);
 				MainFrame.savePurchaseItem(rsupc);
 				MainFrame.savePurchaseItem(qty.toString());
 			}
 			return true;
 		}
 		catch(SQLException ex){
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 
 			try {
 				con.rollback();
 				return false; 
 			}
 			catch (SQLException ex2) {
 				event = new ExceptionEvent(this, ex2.getMessage());
 				fireExceptionGenerated(event);
 				return false; 
 			}
 		}
 	}
  public void addItemToShoppingCart(String upc){
 	 ArrayList<String> searchItems = MainFrame.getOnlineSearchItems();
 	 int index = searchItems.indexOf(upc);
 	 
 	 String theupc = searchItems.get(index);
 //	 String title = searchItems.get(index+1);
 //	 String category = searchItems.get(index+2);
 //	 String leadsinger = searchItems.get(index+3);
 	 Double price = Double.parseDouble(searchItems.get(index+4));
 	 Integer qty = Integer.parseInt(searchItems.get(index+5));
 	 
 	 MainFrame.clearSearchResults();
 	 //save upc, price, qty as strings to onlinePurchaseItems
 	 MainFrame.saveOnlinePurchaseItem(theupc);
 	 MainFrame.saveOnlinePurchaseItem(price.toString());
 	 MainFrame.saveOnlinePurchaseItem(qty.toString());
 	 
 	 //indicate there is 1 purchaseitem in basket
 	 MainFrame.incOnlinePurchaseCount();
 	 
 	 //save these values in GUI shopping basket text area
 	 MainFrame.appendShoppingBasketTextArea("  "+theupc+"  ");
 	 MainFrame.appendShoppingBasketTextArea(price.toString()+"  ");
 	 MainFrame.appendShoppingBasketTextArea(qty.toString());
 	 
  }
 //TODO: IAN FINISH THIS METHOD
 	
 	public boolean onlinePurchaseItemSearch(String title, String category,String leadsinger, Integer qty){
 		try {
 			String rsupc;
 			String rstitle;
 			String rscategory;
 			String rsleadsinger;
 			Double rsprice;
 			
 			String qtitle = "";
 			String qcat = "";
 			String qls= "";
 			String qqty = "";
 			
 			if(qty==null||qty==0)return false;
 			else qqty = "AND i.stock >= "+qty.toString();
 					System.out.println(qqty);
 			if (title != null && !title.isEmpty()){
 				qtitle = " AND i.ititle = '"+title+"'";
 			}
 			System.out.println(qtitle);
 			if (category != null && !category.isEmpty()){
 				qcat = " OR i.category = '"+category+"'";
 			}
 			System.out.println(qcat);
 			if (leadsinger != null && !leadsinger.isEmpty()){
 				qls = " OR l.sname = '"+leadsinger+"'";
 			}
 			System.out.println(qls);
 			
 			//String statement = "SELECT i.upc, ititle, category, sname, price FROM Item i, LeadSinger l WHERE i.upc = l.upc "+qqty+qtitle+qcat+qls;
 
 			String statement = "SELECT i.upc, ititle, category, sname, price FROM Item i, LeadSinger l WHERE i.upc = l.upc "+qqty+qtitle+qcat+qls;
 			//String statement = "SELECT * FROM Item i, LeadSinger l WHERE i.upc=l.upc";
 			//String statement = "SELECT i.upc, i.ititle, i.category, l.sname, i.price FROM Item i inner join LeadSinger l on i.upc = l.upc WHERE "+qqty+qtitle+qcat+qls;
 
 			
 			
 			
 			System.out.println(statement);
 			ps = con.prepareStatement(statement);
 			//,ResultSet.TYPE_SCROLL_INSENSITIVE,
 			//ResultSet.CONCUR_READ_ONLY
 			
 			System.out.println("About to Execute Online Search");
 			
 			ResultSet rs = ps.executeQuery();
 	
 			System.out.println("Just Executed Online Item Search Query");
 //			String searchItems = "";
 			System.out.println("\nEntering loop...");
 			while(rs.next()){
				
 				System.out.println(MainFrame.getSearchResultItemCount());
 				String searchItem = "";
 				
 				rsupc = rs.getString(1);
 				System.out.println("Checking out rsupc value: "+rsupc);
 				searchItem += (rsupc);
 //				MainFrame.addSearchItem(rsupc);
 				
 				rstitle = rs.getString(2);
 				System.out.println("Checking out rstitle value: "+rstitle);
 				searchItem += ("\t" + rstitle); //TODO: fix output format (low priority)
 //				MainFrame.addSearchItem(rstitle);
 				
 				rscategory = rs.getString(3);
 				System.out.println("Checking out rscat value: "+rscategory);
 				searchItem += ("\t" + rscategory);
 //				MainFrame.addSearchItem(rscategory);
 				
 				rsleadsinger = rs.getString(4);
 				System.out.println("Checking out rsls value: "+rsleadsinger);
 				searchItem += ("\t" + rsleadsinger);
 //				MainFrame.addSearchItem(rsleadsinger);
 			
 				
 				rsprice = rs.getDouble(5);
 				System.out.println("Checking out rsprice value: "+rsprice);
 				searchItem += ("\t" + rsprice);
 //				MainFrame.addSearchItem(rsprice.toString());
 				
 //				MainFrame.addSearchItem(qty.toString());
 				MainFrame.addSearchItem(searchItem + "\t" + qty.toString() + "\n");
 				MainFrame.incSearchResultCount();
 //				searchItems += searchItem;
 				System.out.println("\n");
 			}
 			System.out.println("Exiting loop...\n");
 			ArrayList<String> searchresults = MainFrame.getOnlineSearchItems();
 			String s= "" ;
 			for (int j=0; j<MainFrame.getSearchResultItemCount();j++){
 				s += searchresults.get(j)+" ";
 			}
 			MainFrame.showSearchResults(s);
 			System.out.println(s);
 
 			return true;
 			
 		} catch (SQLException ex) {
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 
 			try {
 				con.rollback();
 				return false; 
 			}
 			catch (SQLException ex2) {
 				event = new ExceptionEvent(this, ex2.getMessage());
 				fireExceptionGenerated(event);
 				return false; 
 			}
 		}
 	
 
 }
 	// expected date not null for online purchases
 	//for purchase, require receiptid.nextval, upc, cardno, expdate
 	// for purchaseitem inserts, require receiptid.currentval,upc,qty
 	
 	public boolean completePurchase(String cardno, String cardexpdate){
 		String upc;
 		Integer qty;
 		ArrayList<String> items = MainFrame.getPurchaseItems();
 		java.util.Date pdate = new Date();
 		java.sql.Date sqlpdate = new java.sql.Date(pdate.getTime());
 			
 		try {
 			
 			ps = con.prepareStatement("INSERT INTO purchase VALUES (purchase_receiptId.nextval, ?, ?, ?, ?, ? ,?)");
 			ps.setDate(1, sqlpdate);
 			ps.setString(2,null);
 			if (cardno!=null&&!cardno.isEmpty()&&cardexpdate!=null&&!cardexpdate.isEmpty()){
 				ps.setString(3,cardno);  //this is a card purchase
 				ps.setString(4,cardexpdate);
 			}
 			else{
 				ps.setString(3, null);  //this means this is a cash purchase
 				ps.setString(4,null);
 			}
 			ps.setDate(5, null); // expecteddate
 			ps.setDate(6,null); // deliverydate
 
 			ps.executeUpdate();
 			System.out.println("Insertion to Purchase Was Successful");
 			
 			for(int i=0; i<MainFrame.getNumberInstorePurchaseItems();i++){
 				System.out.println("entered for loop");
 				upc = items.get(0);
 				System.out.println("UPC is:" + upc);
 				qty = Integer.parseInt(items.get(1));
 				System.out.println("Quantity is:" + qty.toString() );
 				ps = con.prepareStatement("INSERT INTO PurchaseItem VALUES (purchase_receiptId.currval,?,?)");
 				items.remove(0); 
 				items.remove(0);
 				ps.setString(1,upc);
 				ps.setInt(2,qty);
 				
 				ps.executeUpdate();
 				System.out.println("An Insertion To PurchaseItem Was Successful");
 			}
 						con.commit();
 			return true;
 			
 		} catch (SQLException ex) {
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 
 			try {
 				con.rollback();
 				return false; 
 			}
 			catch (SQLException ex2) {
 				event = new ExceptionEvent(this, ex2.getMessage());
 				fireExceptionGenerated(event);
 				return false; 
 			}
 		}
 	}
 	
 	public boolean completeOnlinePurchase(String cardno, String cardexpdate,String cid){
 		String upc;
 		Integer qty;
 		Double price;
 		ArrayList<String> items = MainFrame.getOnlinePurchaseItems();
 		java.util.Date pdate = new Date();
 		DateFormat formatter = new SimpleDateFormat("dd/MM/yy");
 		java.sql.Date sqlpdate = new java.sql.Date(pdate.getTime());
 			
 		try {
 			
 			ps = con.prepareStatement("INSERT INTO purchase VALUES (purchase_receiptId.nextval, ?, ?, ?, ?, ? ,?)");
 			ps.setDate(1, sqlpdate);
 			ps.setString(2,cid); //insert customerid
 			if (cardno!=null&&!cardno.isEmpty()&&cardexpdate!=null&&!cardexpdate.isEmpty()){
 				ps.setString(3,cardno);  //this is a card purchase
 				ps.setString(4,cardexpdate);
 			}
 			else{
 			return false;
 			}
 			//We are calculating a date 10 days in the future for expected date
 			java.util.Date expdate = new Date();
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(expdate);
 			cal.add(Calendar.DATE,10);
 			expdate = cal.getTime();
 			java.sql.Date date = new java.sql.Date(expdate.getTime());
 			ps.setDate(5, date); // expecteddate
 			ps.setDate(6,null); // deliverydate
 
 			ps.executeUpdate();
 			System.out.println("Insertion to Purchase Was Successful");
 			
 			for(int i=0; i<MainFrame.getNumberInstorePurchaseItems();i++){
 				System.out.println("entered for loop");
 				upc = items.get(0);
 				System.out.println("UPC is:" + upc);
 				
 				price = Double.parseDouble(items.get(1));
 				System.out.println("Price is:" + price.toString() );
 				
 				qty = Integer.parseInt(items.get(2));
 				System.out.println("Quantity is:" + qty.toString() );
 				
 				ps = con.prepareStatement("INSERT INTO PurchaseItem VALUES (purchase_receiptId.currval,?,?)");
 				items.remove(0); 
 				items.remove(0);
 				ps.setString(1,upc);
 				ps.setInt(2,qty);
 				
 				ps.executeUpdate();
 				System.out.println("An Insertion To PurchaseItem Was Successful");
 			}
 						con.commit();
 			return true;
 			
 		} catch (SQLException ex) {
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 
 			try {
 				con.rollback();
 				return false; 
 			}
 			catch (SQLException ex2) {
 				event = new ExceptionEvent(this, ex2.getMessage());
 				fireExceptionGenerated(event);
 				return false; 
 			}
 		}
 	}
 	
 	//TODO Figure out how to get most recent receiptId in order to print receipt via Daniel's methods
 	public int getReceiptId(){
 		int receiptId = -1;
 		
 		try {
 			Statement st = con.createStatement();
 			ResultSet rs = st.executeQuery("SELECT purchase_receiptId.currval FROM purchase");
 			if(rs.next()){
 				receiptId = rs.getInt(1);
 			}
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return receiptId;
 		
 	}
 //	public static void main(String args[])
 //	{
 //		
 //	// Receipt tests:
 //	AMSOracleConnection oCon = AMSOracleConnection.getInstance();
 //
 //	oCon.connect("ora_n7o8", "a36421089");
 //	PurchaseOperations po = new PurchaseOperations();
 //	System.out.println("test******** receipt w/card");
 //	po.printReceipt(1001);
 //	System.out.println("test******** receipt w/o card");
 //	po.printReceipt(1004);
 	
 //		System.out.println("test");
 //		
 //		AMSOracleConnection oCon = AMSOracleConnection.getInstance();
 ////		oCon.connect("ora_o0g6", "a40493058");
 //		oCon.connect("ora_h5n8", "a44140028");
 //		
 //		PurchaseOperations po = new PurchaseOperations();
 //		
 //
 //		@SuppressWarnings("deprecation")
 //		java.sql.Date date = new java.sql.Date(2013, 10, 10);
 //
 
 	
 //		
 //
 //		
 ////		po.insert(date, "joe123", "666", "555", null, null);
 ////		po.insert(date, "joe123", "667", "555", null, null);
 //		po.insert(date, "joe123", "test", "test", null, null);
 //		
 //		
 //
 //	} 
 	
 	
 	
 }
 
 
