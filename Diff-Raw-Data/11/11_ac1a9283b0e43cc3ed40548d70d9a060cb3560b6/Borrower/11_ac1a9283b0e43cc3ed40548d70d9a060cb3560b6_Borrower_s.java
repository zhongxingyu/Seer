 package tables;
 
 import java.sql.Connection;
<<<<<<< HEAD
 import java.sql.Date;
import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
=======
>>>>>>> 57df0c01f1188664e94da94a1ad8915feb7ac5b4
 import java.sql.SQLException;
 import java.sql.Statement;
import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.List;
 
 import users.Conn;
 
 public class Borrower implements Table {
 
 	private Integer bid;
 	private String password;
 	private String name;
 	private String address;
 	private String phone;
 	private String emailAddress;
 	private Integer sinOrStNum;
 	private Calendar expiryDate;
 	private Integer bookTimeLimit;
 	
 	private Connection con;
 	
     /**
      * Creates an empty Borrower
      */
     public Borrower()
     {
       con = Conn.getInstance().getConnection();
     }
 	
 	
 	@Override
 	public String[][] display() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void update() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	 /**
      * Deletes the tuple in the Borrower table whose primary key
      * corresponds to this Borrower's bid. All other attributes are
      * ignored.
      * @return true if the tuple was successfully deleted, otherwise false
      */
 	@Override
 	public boolean delete() throws SQLException {
         String sql = "DELETE FROM Borrower WHERE bid = " + bid;
         
         PreparedStatement ps = con.prepareStatement(sql);
         int numRowsDeleted = ps.executeUpdate();
         ps.close();
         return numRowsDeleted == 1;
         
 	}
 
 	@Override
 	public Collection<Table> getAll() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Table get() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public boolean insert(int bid, String password, String name, String address, String phone,
 			String email, int sinOrStNo, Calendar expiryDate, String type) throws SQLException {
 		
 		Date sqlDate = new Date(expiryDate.getTimeInMillis());
 		
 		try {
 			PreparedStatement ps = con.prepareStatement ("INSERT INTO Borrower " +
 			"(bid,password,name,address,phone,emailAddress,sinOrStNo,expiryDate,type) VALUES (?,?,?,?,?,?,?,?,?)"); 
 
 			ps.setInt(1, bid);
 			ps.setString(2, password);
 			ps.setString(3, name);
 			ps.setString(4, address);
 			ps.setString(5, phone);
 			ps.setString(6, email);
 			ps.setInt(7, sinOrStNo);
 			ps.setDate(8, sqlDate);
 			ps.setString(9, type);
 			ps.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 			return false;
 		}
 	}
 
   /**
    * @return the bid
    */
   public Integer getBid() {
     return bid;
   }
 
   /**
    * @param bid the bid to set
    */
   public void setBid(Integer bid) {
     this.bid = bid;
   }
 
   /**
    * @return the password
    */
   public String getPassword() {
     return password;
   }
 
   /**
    * @param password the password to set
    */
   public void setPassword(String password) {
     this.password = password;
   }
 
   /**
    * @return the name
    */
   public String getName() {
     return name;
   }
 
   /**
    * @param name the name to set
    */
   public void setName(String name) {
     this.name = name;
   }
 
   /**
    * @return the address
    */
   public String getAddress() {
     return address;
   }
 
   /**
    * @param address the address to set
    */
   public void setAddress(String address) {
     this.address = address;
   }
 
   /**
    * @return the phone
    */
   public String getPhone() {
     return phone;
   }
 
   /**
    * @param phone the phone to set
    */
   public void setPhone(String phone) {
     this.phone = phone;
   }
 
   /**
    * @return the emailAddress
    */
   public String getEmailAddress() {
     return emailAddress;
   }
 
   /**
    * @param emailAddress the emailAddress to set
    */
   public void setEmailAddress(String emailAddress) {
     this.emailAddress = emailAddress;
   }
 
   /**
    * @return the sinOrStNum
    */
   public Integer getSinOrStNum() {
     return sinOrStNum;
   }
 
   /**
    * @param sinOrStNum the sinOrStNum to set
    */
   public void setSinOrStNum(Integer sinOrStNum) {
     this.sinOrStNum = sinOrStNum;
   }
 
   /**
    * @return the expiryDate
    */
   public Calendar getExpiryDate() {
     return expiryDate;
   }
 
   /**
    * @param expiryDate the expiryDate to set
    */
   public void setExpiryDate(Calendar expiryDate) {
     this.expiryDate = expiryDate;
   }
 
   /**
    * @return the bookTimeLimit
    */
   public Integer getBookTimeLimit() {
     return bookTimeLimit;
   }
 
   /**
    * @param bookTimeLimit the bookTimeLimit to set
    */
   public void setBookTimeLimit(Integer bookTimeLimit) {
     this.bookTimeLimit = bookTimeLimit;
   }
 
   public List<Book> searchBookByTitle(String title) {
 	  try {
 		Statement stmt = con.createStatement();
 		ResultSet rs = stmt.executeQuery("SELECT Book.*, COUNT(status) FROM Book, BookCopy " +
				"WHERE Book.title=" + title + " AND status=in AND Book.callNumber=BookCopy.callNumber");
 		List<Book> lob;
 		
 		while(rs.next()) {
 			String callNo = rs.getString(1);
 			String isbn = rs.getString(2);
 			String title1 = rs.getString(3);
 			String mainAuthor = rs.getString(4);
 			String publisher = rs.getString(5);
 			int year = rs.getInt(6);
 			int copiesIn = rs.getInt(7);
 			
 			Book b = new Book(callNo,isbn,title1,mainAuthor,publisher,year,copiesIn);
 			lob.add(b);			
 		}
 		
 	} catch (SQLException e) {
 		System.out.println(e.getMessage());
 		e.printStackTrace();
 		return null;
 	}
 	  
 	return null;  
   }
 
   public List<Book> searchBookByAuthor(String title) {
 		return null;  
 	  }
   
   public List<Book> searchBookBySubject(String title) {
 		return null;  
 	  }
   
   public void checkAccount() {
 	  
   }
   
   public void placeHoldRequest(String isbn) {
 	  
   }
   
   public void payFine(Integer borid, Integer amountInCents) {
 	  
   }
 
 @Override
 public boolean insert() throws SQLException {
 	// TODO Auto-generated method stub
 	return false;
 }
 
 }
