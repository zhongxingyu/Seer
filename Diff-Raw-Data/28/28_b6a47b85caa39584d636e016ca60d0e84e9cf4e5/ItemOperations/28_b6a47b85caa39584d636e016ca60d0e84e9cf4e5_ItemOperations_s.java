 
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 
 import javax.swing.event.EventListenerList;
 
 public class ItemOperations extends AbstractTableOperations{
 
 	boolean insert(String upc, String title, String type, String category, String company, String year, Double price, Integer stock){
 		try {
 			ps = con.prepareStatement("INSERT INTO item VALUES (?,?,?,?,?,?,?,?)");
 
 			ps.setString(1,upc);
 
 			if (title != null){
 				ps.setString(2,title);
 			}
 			else {
 				ps.setString(2,null);
 			}
 			if (type!=null){
 				ps.setString(3,type);
 			}
 			else{
 				ps.setString(3,null);
 			}
 			if (category!=null){
 				ps.setString(4,category);
 			}
 			else{
 				ps.setString(4,null);
 			}
 			if (company!=null){
 				ps.setString(5,company);
 			}
 			else {
 				ps.setString(5,null);
 			}
 			if (year!= null){
 				ps.setString(6,year);
 			}
 			else {
 				ps.setString(6,null);
 			}
 			if (price!=null){
 				ps.setDouble(7,price.doubleValue());
 			}
 			else {
 				ps.setNull(7, Types.DOUBLE);
 			}
 
 			ps.setInt(8, stock.intValue());
 			ps.executeUpdate();
 			con.commit();
 			System.out.println("New Item added");
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
 	boolean updateItem(String upc, Integer stock, Double price){	
 		try{
 			ps = con.prepareStatement("UPDATE item SET price = ?, stock = stock + ? WHERE upc = ?");
 
 			if (price != null) {
 				ps.setDouble(1, price.doubleValue());
 			}
 			else {
 				ps.setNull(1, Types.DOUBLE);
 			}
 			ps.setInt(2, stock.intValue());
 			ps.setString(3, upc);
 
 			ps.executeUpdate();
 			con.commit();
 			System.out.println("item updated");
 			return true;
 		}
 		catch(SQLException ex){
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 
 			try	{
 				con.rollback();
 				return false; 
 			}
 			catch (SQLException ex2){
 				event = new ExceptionEvent(this, ex2.getMessage());
 				fireExceptionGenerated(event);
 				return false; 
 			}
 		}
 	}
 
 	boolean delete(String upc){
 		try {
 			ps = con.prepareStatement("DELETE FROM item WHERE upc = ?");
 			ps.setString(1, upc);
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
 
 	void display(){
 
 	}
 	//TODO: Fairly straightforward ManagerAddItem, see comment below. 
 	//if upc null, return false
 	//check if upc exists in item, if it does, add quantity to current (modify price if input)
 	//if upc doesn't exist, insert all fields to Item
 	public boolean managerAddItem(String upc, Integer stock, Double price ){
 		ResultSet rs;
 
 		try {
 			// finds tuple with given upc
 			ps = con.prepareStatement("SELECT * FROM ITEM WHERE upc = ?");
 			ps.setString(1, upc);
 			rs = ps.executeQuery();
 
 			if (rs.next()) {
 				System.out.println("upc exists. now updating");
 				//rs.getString()
 				updateItem(upc, stock, price);
 				return true;
 			}
 			else if (!rs.next()) {
 				insert(upc, "", "", "", "", "", price, stock );
 				return true;
 			}
 			else return false;
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
 
 	public boolean dailySalesReport (String date) {
 		String upc;
 		String category;
 		Double price;
 		Integer units;
 		Double tValue;
 		ResultSet rs;
 
 		Double totalPrice = 0.0;
 		Integer totalQuantity = 0;
 		int qClassical, qCountry, qInstrumental, qNewAge, qPop, qRap, qRock;
 		qClassical = qCountry = qInstrumental = qNewAge = qPop = qRap = qRock = 0;
 		Double tClassical, tCountry, tInstrumental, tNewAge, tPop, tRap, tRock;
 		tClassical = tCountry = tInstrumental = tNewAge = tPop = tRap = tRock = 0.0;
 		
 		System.out.println("Executing query");
 		try {
 			ps = con.prepareStatement("WITH sq1 AS (SELECT * FROM 	(SELECT DISTINCT(i.upc), category, price FROM item i, purchase p, purchaseitem pi WHERE i.upc = pi.upc AND pi.receiptid = p.receiptid ORDER BY category)), sq2 AS (SELECT upc, sum(quantity) AS units FROM purchase p, purchaseitem pi WHERE p.receiptid = pi.receiptid AND pdate >= ? and pdate <= ? GROUP BY upc ORDER BY units) SELECT sq1.upc, category, sq1.price, sq2.units, (sq1.price * sq2.units) AS total_value FROM sq1, sq2 WHERE sq1.upc = sq2.upc ORDER BY category",
 					ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 
 			ps.setString(1, date);
 			ps.setString(2, date);
 			rs = ps.executeQuery();
 
 			System.out.println("Query executed");
 
 			// get info on ResultSet
 			ResultSetMetaData rsmd = rs.getMetaData();
 
 			// get number of columns
 			int numCols = rsmd.getColumnCount();
 
 			System.out.println(" ");
 			// print the names of each column
 			for (int i = 0; i < numCols; i++) {
 				System.out.printf("%-10s", rsmd.getColumnName(i+1));
 			}
 			System.out.println(" ");
 
 			while (rs.next()) {
 
 				upc = rs.getString("upc");
 				System.out.printf("%-10.10s", upc);
 
 				category = rs.getString("category");
 				if (rs.wasNull()) {
 					System.out.printf("%-10.10s", " ");
 				}
 				else {
 					System.out.printf("%-10.10s", category);
 					switch (category) {
 					}
 				}
 
 				price = rs.getDouble("price");
 				if (rs.wasNull()){
 					System.out.printf("%-10.10s", " ");
 				}
 				else {
 					System.out.printf("%-10.10s", price + " ");
 					totalPrice += price;
 				}
 
 				units = rs.getInt("units");
 				if (rs.wasNull()) {
 					System.out.printf("%-10.10s", " ");
 				}
 				else {
 					System.out.printf("%-10.10s", units + " ");
 			
 				} 
 
 				tValue = rs.getDouble("total_value");
 				if (rs.wasNull()){
 					System.out.printf("%-10.10s\n", " ");
 				}
 				else {
 					System.out.printf("%-10.10s\n", tValue + " ");
 				} 
 				switch (category) {
 					case "Classical":		
 						qClassical += units;
 						tClassical += price;
 						break;
 					case "Country":			
 						qCountry += units;
 						tCountry += price;
 						break;
 					case "Instrumental":	
 						qInstrumental += units;
 						tInstrumental =+ price;
 						break;
 					case "New Age":			
 						qNewAge += units;
 						tNewAge += price;
 						break;
 					case "Pop":				
 						qPop += units;
 						tPop += price;
 						break;
 					case "Rap":				
 						qRap += units;
 						tRap += price;
 						break;
 					case "Rock":			
 						qRock += units;
 						tRock += price;
 						break;
 					default:				
 						break; 
 				}
 				totalQuantity += units;
 				
 			}
 			System.out.println("---------------------------------------------------");
 			if (qClassical != 0) {
 				System.out.println("Classical subtotal: " + qClassical + " units $ " + tClassical);
 			}
 			if (qCountry != 0) {
 				System.out.println("Country subtotal: " + qCountry + " units $ " + tCountry);
 			}
 			if (qInstrumental != 0) {
 				System.out.println("Instrumental subtotal: " + qInstrumental + " units $ " + tInstrumental);
 			}
 			if (qNewAge != 0) {
 				System.out.println("New Age subtotal: " + qNewAge + " units $ " + tNewAge);
 			}
 			if (qPop != 0) {
 				System.out.println("Pop subtotal: " + qPop + " units $ " + tPop);
 			}
 			if (qRap != 0) {
 				System.out.println("Rap subtotal: " + qRap + " units $ " + tRap);
 			}
 			if (qRock != 0) {
 				System.out.println("Rock subtotal: " + qRock + " units $ " + tRock);
 			}
 			System.out.println("Total daily sales: " + totalQuantity + " units $" + totalPrice);
 			
 			ps.close();
 
 			return true;
 		}
 		catch (SQLException ex) {
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 			System.out.println(ex.getMessage());
 
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
 
 
 
 	public boolean topItems(String date, Integer n){
 
 		String title;
 		String company;
 		Integer stock;
 		Integer sold;
 		ResultSet rs;
 		ResultSetMetaData rsmd;
 
 		try{
 
 			ps = con.prepareStatement("WITH sq1 AS (select * from (select upc,  sum(quantity) as Sold from purchaseitem pi, purchase p where p.receiptid = pi.receiptid and pdate >= ? and pdate <= ? group by upc order by sold desc) where rownum <= ?), sq2 AS (select distinct (pi.upc), ititle, stock, company from item i, purchase p, purchaseitem pi where i.upc = pi.upc and pi.receiptid = p.receiptid) SELECT ititle as Title, company, stock, Sold FROM sq1, sq2 WHERE  sq1.upc = sq2.upc ORDER BY Sold desc",
 					ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 
 			//			if (date != null)
 			//			{
 			ps.setString(1, date);
 			ps.setString(2, date);
 			ps.setInt(3, n);
 			//			}
 			//			else
 			//			{
 			//				ps.setString(1,null);
 			//				ps.setString(2,null);
 			//			}
 
 			//			if (n != null)
 			//			{
 			//				ps.setInt(3, n);
 			//			}
 			//			else ps.setNull(3,Types.INTEGER);
 
 			System.out.println("about to execute");
 
 			rs = ps.executeQuery();
 
 			System.out.println("after execute");
 
 
 			// get info on ResultSet
 			rsmd = rs.getMetaData();
 
 			// get number of columns
 			int numCols = rsmd.getColumnCount();
 
 			//			String nc = toString(numCols);
 
 			System.out.println(" ");
 
 			// display column names;
 			for (int i = 0; i < numCols; i++)
 			{
 				// get column name and print it
 
 				System.out.printf("%-30s", rsmd.getColumnName(i+1));    
 			}
 
 			System.out.println(" ");
 
 			while(rs.next())
 			{
 				// for display purposes get everything from Oracle 
 				// as a string
 
 				// simplified output formatting; truncation may occur
 
 				title = rs.getString("title");
 				System.out.printf("%-30.30s", title);
 
 				company = rs.getString("company");
 				if (rs.wasNull())
 				{
 					System.out.printf("%-30.30s", " ");
 				}
 				else
 				{
 					System.out.printf("%-30.30s", company);
 				}
 
 				stock = rs.getInt("stock");
 				if (rs.wasNull())
 				{
 					System.out.printf("%-30.30s", " ");
 				}
 				else
 				{
 					System.out.printf("%-30.30s", stock + " ");
 				}
 
 				sold = rs.getInt("sold");
 				if (rs.wasNull())
 				{
 					System.out.printf("%-30.30s\n", " ");
 				}
 				else
 				{
 					System.out.printf("%-30.30s\n", sold + " ");
 				} 
 
 			}
 			return true;
 		}
 		catch(SQLException ex){
 			ExceptionEvent event = new ExceptionEvent(this, ex.getMessage());
 			fireExceptionGenerated(event);
 			System.out.println(ex.getMessage());
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
 
 	public boolean reduceStockForPurchase(ArrayList<String> items){
 		try {
 			ps = con.prepareStatement("UPDATE item SET stock = stock - ? WHERE upc = ?");
 			String upc;
 			Integer qty;
 			for (int i = 0; i<MainFrame.getNumberInstorePurchaseItems();i++){
 				upc = items.get(0);
 				qty = Integer.parseInt(items.get(1));
 				
 				if (upc!=null){
 					ps.setString(2, upc);
 				}
 				else return false;
 				if (qty!=null&&qty!=0){
 					ps.setInt(1,qty);
 				}
 				else return false;
 				ps.executeUpdate();
 				items.remove(0);
 				items.remove(0);
 			}
			
			
			
			
			
			
			
 			con.commit();
			System.out.println("Stocked reduced by purchase quantity");
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
 	
 	public static void main(String args[])
 	{
 
 		System.out.println("test");
 		//
 		AMSOracleConnection oCon = AMSOracleConnection.getInstance();
 		oCon.connect("ora_o0g6", "a40493058");
 //		oCon.connect("ora_h5n8", "a44140028");
 		//
 		ItemOperations item = new ItemOperations();
 
 
 
 		//		item.insert("999888", "test", "CD", "test", "test", "test", 999.99, 99);
 		//		item.insert("123457", "cpsc", "CD", "instrumental", "company", "year", 111.12, 27);
 
 		//		//item.displayItem();
 		//		item.delete("999888");
 		//		item.delete("999999");
 		//		item.delete("123457");
 		//
 		item.dailySalesReport("18-mar-03");
 		
 		System.out.println("test done");
 
 
 		
 		
 
 		//item.topItems("25-may-13", 4);
 
 	} 
 
 
 }
