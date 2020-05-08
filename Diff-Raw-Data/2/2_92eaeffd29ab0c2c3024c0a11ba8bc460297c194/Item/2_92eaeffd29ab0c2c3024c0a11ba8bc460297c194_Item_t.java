 package group;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import exceptions.ServiceLocatorException;
 import jdbc.DBConnectionFactory;
 public class Item {
 private String title="";
 private int id = 0;
 private String pictureName="";
 private String picturePath="";
 private int category;
 private String imageurl="";
 private String description="";
 private String postage="";
 private int rprice;
 private int sprice;
private int bincre = 1;
 private int currentBiddingPrice;
 private Timestamp ctime = new Timestamp(System.currentTimeMillis());
 private boolean halted;
 private int seller;
 private int currentBidder;
 
 public int getId() {
 	return id;
 }
 public void setId(int id) {
 	this.id = id;
 }
 public String getPictureName() {
 	return pictureName;
 }
 public void setPictureName(String pictureName) {
 	this.pictureName = pictureName;
 }
 public String getPicturePath() {
 	return picturePath;
 }
 public void setPicturePath(String picturePath) {
 	this.picturePath = picturePath;
 }
 public String getTitle() {
 	return title;
 }
 public void setTitle(String title) {
 	this.title = title;
 }
 public int getCategory() {
 	return category;
 }
 public void setCategory(int category) {
 	this.category = category;
 }
 public String getImageurl() {
 	return imageurl;
 }
 public void setImageurl(String imageurl) {
 	this.imageurl = imageurl;
 }
 public String getDescription() {
 	return description;
 }
 public void setDescription(String description) {
 	this.description = description;
 }
 public String getPostage() {
 	return postage;
 }
 public void setPostage(String postage) {
 	this.postage = postage;
 }
 public int getRprice() {
 	return rprice;
 }
 public void setRprice(int rprice) {
 	this.rprice = rprice;
 }
 public int getSprice() {
 	return sprice;
 }
 public void setSprice(int sprice) {
 	this.sprice = sprice;
 }
 public int getBincre() {
 	return bincre;
 }
 public void setBincre(int bincre) {
 	this.bincre = bincre;
 }
 public Timestamp getCtime() {
 	return ctime;
 }
 public void setCtime(Timestamp ctime) {
 	this.ctime = ctime;
 }
 public void setCurrentBiddingPrice(int currentBiddingPrice) {
 	this.currentBiddingPrice = currentBiddingPrice;
 }
 public int getCurrentBiddingPrice() {
 	return currentBiddingPrice;
 }
 public void setCurrentBidder(int currentBidder) {
 	this.currentBidder = currentBidder;
 }
 public int getCurrentBidder() {
 	return currentBidder;
 }
 public void setHalted(boolean halted) {
 	this.halted = halted;
 }
 public boolean getHalted() {
 	return halted;
 }
 
 public int getMinimumBid() {
 	return getCurrentBiddingPrice() + getBincre();
 }
 public boolean isClosed() {
     Timestamp now = new Timestamp(System.currentTimeMillis());
 	long milliseconds1 = now.getTime();
     long milliseconds2 = getCtime().getTime();
     if (milliseconds1 > milliseconds2) {
     	return true;
     }
 	return false;
 }
 public String getTimeLeft() {
     Timestamp now = new Timestamp(System.currentTimeMillis());
 	long milliseconds1 = now.getTime();
     long milliseconds2 = getCtime().getTime();
     
     if (milliseconds1 > milliseconds2) {
     	return "Ended";
     }
 	
 	long diff = milliseconds2 - milliseconds1;
 	long diffSeconds = diff / 1000;
 	long diffMinutes = diff / (60 * 1000);
 	long diffHours = diff / (60 * 60 * 1000);
 	long diffDays = diff / (24 * 60 * 60 * 1000);
 	
 	if (diffDays != 0) {
 		return diffDays + " day(s)";
 	} else if (diffHours != 0) {
 		return diffHours + " hour(s)";
 	} else if (diffMinutes != 0) {
 		return diffMinutes + " minute(s)";
 	} else if (diffSeconds != 0) {
 		return diffSeconds + " second(s)";
 	}
 
 	return "Unknown";
 }
 public boolean canAcceptReject(Connection conn, String username) throws SQLException {
 	if (!isClosed())
 		return false;
 	if (getCurrentBidder() == 0)
 		return false;
 	if (getCurrentBiddingPrice() >= getRprice())
 		return false;
 	PreparedStatement st = null;
 	ResultSet rs = null;
 	try {
 		UserBean user = UserBean.initializeFromUsername(conn, username);
 		if (user.getUserid() == getSeller()) {
 			return true;
 		} else {
 			return false;
 		}
 	} catch (Exception e) {
 		return false;
 	} finally {
 		if (st != null)
 			st.close();
 		if (rs != null)
 			rs.close();
 	}
 }
 public void Initialize(String Title) {
 
 	try {
 		Connection conn = DBConnectionFactory.getConnection();
 		if(conn!=null) System.out.println("connected");
 		String sqlQuery = "SELECT items.title, categories.category, items.picturepath, items.description, items.postagedetails,"+ 
                           "items.reserveprice, items.biddingstartprice, items.biddingincrements, items.closingtime, users.username"+
                           "FROM items"+
                           "join categories on items.category = categories.id"+
                           "join users on items.seller = users.id where items.title =  '"+Title+"'";
 		Statement st = conn.createStatement();
 		ResultSet rs = st.executeQuery(sqlQuery);
 		if(rs.next()){
         title= rs.getString(1);
         category = rs.getInt(2);
         imageurl = rs.getString(3);
         description = rs.getString(4);
         postage = rs.getString(5);
         rprice = rs.getInt(6);
         sprice = rs.getInt(7);
         bincre = rs.getInt(8);
         ctime  = rs.getTimestamp(9);
         seller = rs.getInt(10);
 		st.close();
 		rs.close();
 		}
 	}catch  (Exception e) {
 		e.printStackTrace();
 	}
 }
 public static Item initializeFromId(Connection conn, int id) throws SQLException {
 	PreparedStatement st = null;
 	ResultSet rs = null;
 	Item item = new Item();
 	try {
 		String sqlQuery = "SELECT * FROM Items WHERE id = ?";
 		st = conn.prepareStatement(sqlQuery);
 		st.setInt(1, id);
 		rs = st.executeQuery();
 		if (rs.next()){
 			item = makeItem(rs);
 		} else {
 			System.out.println("Item id "+id+" not found.");
 			throw new SQLException();
 		}
 		Item.updateCurrentBid(conn, item);
 	} finally {
 		if (st != null)
 			st.close();
 		if (rs != null)
 			rs.close();
 	}
 	return item;
 }
 public void Insert(String username) {
 
 	Connection conn;
 	try {
 		conn = DBConnectionFactory.getConnection();
 		if(conn!=null) System.out.println("connected");
 		String sqlQuery = "select id from users where username = '"+username+"'";
 		Statement st = conn.createStatement();
 		ResultSet rs = st.executeQuery(sqlQuery);
 		if(rs.next()) this.seller = rs.getInt(1);
 		st.close();
 		rs.close();
 		sqlQuery = "INSERT INTO items( title, category, picturepath, description, postagedetails,"+
                           "reserveprice, biddingstartprice, biddingincrements, closingtime,"+
                           "seller) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 		PreparedStatement pres = conn.prepareStatement(sqlQuery);
 		pres.setString(1, this.title);
 		pres.setInt(2, this.category);
 		pres.setString(3, this.imageurl);
 		pres.setString(4, this.description);
 		pres.setString(5, this.postage);
 		pres.setInt(6, this.rprice);
 		pres.setInt(7, this.sprice);
 		pres.setInt(8, this.bincre);
 		pres.setTimestamp(9, this.ctime);
 		pres.setInt(10, this.seller);
 		pres.executeUpdate();		
                           
 	} catch (ServiceLocatorException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	} catch (SQLException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 	
 }
 
 public static void updateCurrentBid(Connection conn, Item i) throws SQLException {
 	PreparedStatement st = null;
 	ResultSet rs = null;
 	try {
 //		String sqlQuery = "select max(bid) from Bids where item = ? group by item";
 		String sqlQuery = "select bidder, bid, id from Bids where bid in (select max(bid) from bids where item = ?) AND item = ?";
 		st = conn.prepareStatement(sqlQuery);
 		st.setInt(1, i.getId());
 		st.setInt(2, i.getId());
 		rs = st.executeQuery();
 		if (rs.next()) {
 			i.setCurrentBidder(rs.getInt(1));
 			i.setCurrentBiddingPrice(rs.getInt(2));
 			System.out.println("has bidder. id="+rs.getInt(3)+" price="+rs.getInt(2)+" when seaching for item.id="+i.getId());
 		} else {
 			System.out.println("no bidder");
 			i.setCurrentBiddingPrice(i.getSprice());
 		}
 	} catch (Exception e) {
 		e.printStackTrace();
 	} finally {
 		if (st != null)
 			st.close();
 		if (rs != null)
 			rs.close();
 	}
 }
 
 private static Item makeItem(ResultSet rs) throws SQLException {
 	Item item = new Item();
 	item.setId(rs.getInt(1));
 	item.setTitle(rs.getString(2));
 	item.setCategory(rs.getInt(3));
 	item.setPictureName(rs.getString(4));
 	item.setPicturePath(rs.getString(5));
 	item.setDescription(rs.getString(6));
 	item.setPostage(rs.getString(7));
 	item.setRprice(rs.getInt(8));
 	item.setSprice(rs.getInt(9));
 	item.setBincre(rs.getInt(10));
 	item.setCtime(rs.getTimestamp(11));
 	item.setSeller(rs.getInt(12));
 	return item;
 }
 public static List<Item> search(Connection conn, String searchItem, int category) throws SQLException {
 	List<Item> result = new ArrayList<Item>();
 	PreparedStatement st = null;
 	ResultSet rs = null;
 	try {
 		String sqlQuery = "select * from Items where title ILIKE ? AND halted = false ORDER BY closingtime";
 		if (category != 0)
 			sqlQuery = "select * from Items where title ILIKE ? AND category = ? AND halted = false ORDER BY closingtime";
 		//doesnt work?
 		st = conn.prepareStatement(sqlQuery);
 		st.setString(1, "%" + searchItem + "%");
 		if (category != 0)
 			st.setInt(2, category);
 		rs = st.executeQuery();
 		while (rs.next()) {
 			result.add(makeItem(rs));
 		}
 		st.close();
 		rs.close();
 
 		for (Item i : result) {
 			Item.updateCurrentBid(conn, i);
 		}
 	} catch (Exception e) {
 		e.printStackTrace();
 	} finally {
 		if (st != null)
 			st.close();
 		if (rs != null)
 			rs.close();
 	}
 	return result;
 }
 
 public static List<Item> getAuctionsFinishing(Connection conn, Timestamp time) throws SQLException {
 	PreparedStatement st = null;
 	ResultSet rs = null;
 	List<Item> result = new ArrayList<Item>();
 	try {
 		String sqlQuery = "select * from Items where closingtime <= ? AND halted = false";
 		st = conn.prepareStatement(sqlQuery);
 		st.setTimestamp(1, time);
 		rs = st.executeQuery();
 		while (rs.next()) {
 			result.add(makeItem(rs));
 		}
 	} catch (Exception e) {
 		e.printStackTrace();
 	} finally {
 		if (st != null)
 			st.close();
 		if (rs != null)
 			rs.close();
 	}
 	return result;
 }
 public boolean checkAndEndAuction(Connection conn) throws SQLException {
 	PreparedStatement st = null;
 	ResultSet rs = null;
 	try {
 		String sqlQuery = "select * from Items where id = ? AND halted = false";
 		st = conn.prepareStatement(sqlQuery);
 		st.setInt(1, getId());
 		rs = st.executeQuery();
 		if (rs.next()) {
 			st.close();
 			rs.close();
 			sqlQuery = "UPDATE Items SET halted = true WHERE id = ?";
 			st = conn.prepareStatement(sqlQuery);
 			st.setInt(1, getId());
 			st.executeUpdate();
 		} else {
 			return false;
 		}
 	} catch (Exception e) {
 		e.printStackTrace();
 	} finally {
 		if (st != null)
 			st.close();
 		if (rs != null)
 			rs.close();
 	}
 	return true;
 }
 
 private Hashtable<String, String> errors= new Hashtable<String, String>();
 public boolean validate() {
 	boolean okAll = true;
 	if(title == ""){
 		errors.put("title", "Enter Title");
 		okAll = false;
 		}
 	if(description == ""){
 		errors.put("description", "Enter Description");
 		okAll = false;
 		}
 	if(!title.matches("^\\W*(\\w+(\\W+|$)){1,10}$")){
 		errors.put("title","invalid input, only words allowed, 10 words max");
 		okAll = false;
 	}
 
 
 	if(!description.matches("^\\W*(\\w+(\\W+|$)){1,100}$")){
 		errors.put("description","invalid input, only words allowed, 100 words max");
 		okAll = false;
 	}
 
 	if(!postage.matches("[0-9a-zA-Z ,!?.-]+")) {
 		errors.put("postage", "invalid input, only numbers, letters ',' '.' '!' '?' allowed");
 		System.out.println("3");
 		okAll = false;
 	}
 	if(postage == ""){
 		errors.put("postage", "Enter Postage Details");
 		okAll = false;
 	}
 	if(rprice <= 0) {
 		errors.put("rprice", "invalid price");
 		System.out.println("4");
 		okAll = false;
 	}
 	if(sprice < 0){
 		errors.put("sprice", "invalid price");
 		System.out.println("5");
 		okAll = false;	
 			}
 	if(bincre <= 0 ) {
 		errors.put("bprice", "invalid price");
 		System.out.println("6");
 		okAll = false;
 	}
 	return okAll;
 }
 public static boolean HaltItem(Connection conn, int id) {
 	PreparedStatement st = null;
 	try{
 		conn = DBConnectionFactory.getConnection();
 		st = conn.prepareStatement("UPDATE items SET halted = true where id = ?");
 		st.setInt(1, id);
         st.executeUpdate();
 		st.close();
 		return true;
 		} catch (Exception e) {
 		e.printStackTrace();
 		return false;
 	} 
 
 }
 public void setErrorMsg(String err,String errMsg) {
 	if (err != null && errMsg !=null) {
 	errors.put(err, errMsg);
 	}
 	}
 	public String getErrorMsg(String err) {
 	Object message = (String)errors.get(err);
 	return (String) ((message == null) ? "" : message);
 	}
 	public int getSeller() {
 		return seller;
 	}
 	public void setSeller(int seller) {
 		this.seller = seller;
 	}
 }
