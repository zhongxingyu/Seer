 /*
  * BookListBean.java
  *
  */
 package beans;
 
 import java.util.*;
 import java.sql.*;
 
 /**
  * 
  * @author Fredrik �lund, Olle Eriksson
  */
 public class BookListBean {
 
 	private Collection<BookBean> bookList;
 	private String url = null;
 	private Connection conn = null;
 	private Statement stmt = null;
 	private ResultSet rs = null;
 	public int q;
 
 	// this constructor is not really used in the application
 	// but is here for testing purpose
 	public BookListBean() {
 	}
 	
 	
 
 	public void initBeans() throws Exception {
 		bookList = new ArrayList<BookBean>(); // a list
 		try {
 
 			// get a database connection and load the JDBC-driver
 
 			Class.forName("com.mysql.jdbc.Driver");
 			conn = DriverManager.getConnection(url);
 
 			// create SQL statements to load the books into the list
 			// each book is a BookBean object
 
 			stmt = conn.createStatement();
 			String sql = "SELECT B.BOOK_ID, B.TITLE, B.DESCRIPTION, AV.FINAL_PRICE AS PRICE, ";
 			sql += "B.PROFIT, B.VISIBLE, B.COM_ID, B.NAME, B.SURNAME, ";
 			sql += "B.IN_STOCK AS QTY, B.C_PRICE, B.QTY AS NEEDED, AV.AVAILABILITY ";
 			sql += "FROM NEEDED AS B, AVAILABILITY AS AV WHERE ";
 			sql += "AV.BOOK_ID = B.BOOK_ID ORDER BY BOOK_ID";
 			rs = stmt.executeQuery(sql);
 
 			// analyze the result set
 			int bid = -1;
 			BookBean bb = null;
 			while (rs.next()) {
 				if (rs.getInt("BOOK_ID") != bid) {
 					bb = new BookBean();
 					bid = rs.getInt("BOOK_ID");
 					bb.setId(rs.getInt("BOOK_ID"));
 					bb.setProduct(rs.getString("TITLE"));
 					bb.setManufacturer(rs.getInt("COM_ID"),
 							rs.getString("NAME"));
 					bb.setType(rs.getInt("COM_ID"),
 							rs.getString("SURNAME"));
 					bb.setComponentQuantity(rs.getInt("COM_ID"),
 							rs.getInt("NEEDED"));
 					bb.setPrice(rs.getInt("PRICE"));
 					bb.setProfit(rs.getInt("PROFIT"));
 					bb.setDescription(rs.getString("DESCRIPTION"));
 					bb.setVisible(rs.getBoolean("VISIBLE"));
 					bb.setAvailability(rs.getInt("AVAILABILITY"));
 					bookList.add(bb);
 				} else {
 					bb.setManufacturer(rs.getInt("COM_ID"),
 							rs.getString("NAME"));
 					bb.setType(rs.getInt("COM_ID"),
 							rs.getString("SURNAME"));
 					bb.setComponentQuantity(rs.getInt("COM_ID"),
 							rs.getInt("NEEDED"));
 				}
 			}
 
 		} catch (SQLException sqle) {
 			throw new Exception(sqle);
 		} // note the we always try to close all services
 			// even if one or more fail to close
 		finally {
 			try {
 				rs.close();
 			} catch (Exception e) {
 			}
 			try {
 				stmt.close();
 			} catch (Exception e) {
 			}
 			try {
 				conn.close();
 			} catch (Exception e) {
 			}
 		}
 
 	}
 
 	/**
 	 * Creates a new instance of BookListBean
 	 */
 	public BookListBean(String _url) throws Exception {
 		this.url = _url;
 		initBeans();
 		
 	}
 	public BookListBean(Collection<BookBean> c, String _url) {
 		this.url = _url;
 		this.bookList=c;
 	}
 	
 	public void add(BookBean c) {
 		//this.url = _url;
 		this.bookList.add(c);
 	}
 
 	// return the booklist
 	java.util.Collection<BookBean> getProduktLista() {
 		return bookList;
 	}
 
 	// create an XML document from the booklist
 	public String getXml() throws Exception {
 		//initBeans();
 		BookBean bb = null;
 		Iterator<BookBean> iter = bookList.iterator();
 		StringBuffer buff = new StringBuffer();
 
 		buff.append("<productlist>");
 		while (iter.hasNext()) {
 			bb = iter.next();
			if ((bb.getVisible() && (bb.getAvailability() > 0))|| bb.q > 0) {
 				buff.append(bb.getXml());
 			}
 		}
 		buff.append("</productlist>");
 
 		return buff.toString();
 	}
 	
 	// create an XML document from the booklist
 		
 
 	// search for a book by book ID
 	public BookBean getById(int id) {
 		BookBean bb = null;
 		Iterator<BookBean> iter = bookList.iterator();
 
 		while (iter.hasNext()) {
 			bb = iter.next();
 			if (bb.getId() == id) {
 				return bb;
 			}
 		}
 		return null;
 	}
 
 	// a main used for testing, remember that a bean can be run
 	// without a container
 	public static void main(String[] args) {
 		try {
 			BookListBean blb = new BookListBean();
 			System.out.println(blb.getXml());
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 }
