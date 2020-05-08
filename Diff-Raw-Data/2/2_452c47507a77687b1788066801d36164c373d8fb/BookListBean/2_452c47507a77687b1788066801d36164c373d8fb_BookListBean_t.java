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
 
 	// this constructor is not really used in the application
 	// but is here for testing purpose
 	public BookListBean() {
 	}
 
 	public void refreshBean() throws Exception {
 		try {
 			bookList = new ArrayList<BookBean>();
 			// get a database connection and load the JDBC-driver
 			conn = DriverManager.getConnection(url);
 
 			// create SQL statements to load the books into the list
 			// each book is a BookBean object
 			stmt = conn.createStatement();
			String sql = "SELECT B.BOOK_ID, B.TITLE, B.DESCRIPTION, AV.FINAL_PRICE AS PRICE, ";
 			sql += "B.PROFIT, B.VISIBLE, A.AUTHOR_ID, A.NAME, A.SURNAME, ";
 			sql += "A.QTY, A.C_PRICE, C.QTY AS NEEDED, AV.AVAILABILITY ";
 			sql += "FROM BOOKS AS B, AUTHORS AS A, COMPOSITION AS C, AVAILABILITY AS AV WHERE ";
 			sql += "AV.BOOK_ID = B.BOOK_ID AND B.BOOK_ID = C.EL_ID AND ";
 			sql += "A.AUTHOR_ID = C.COM_ID ORDER BY BOOK_ID";
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
 					bb.setManufacturer(rs.getInt("AUTHOR_ID"),
 							rs.getString("NAME"));
 					bb.setType(rs.getInt("AUTHOR_ID"),
 							rs.getString("SURNAME"));
 					bb.setComponentQuantity(rs.getInt("AUTHOR_ID"),
 							rs.getInt("NEEDED"));
 					bb.setPrice(rs.getInt("PRICE"));
 					bb.setProfit(rs.getInt("PROFIT"));
 					bb.setDescription(rs.getString("DESCRIPTION"));
 					bb.setVisible(rs.getBoolean("VISIBLE"));
 					bb.setAvailability(rs.getInt("AVAILABILITY"));
 					bookList.add(bb);
 				} else {
 					bb.setManufacturer(rs.getInt("AUTHOR_ID"),
 							rs.getString("NAME"));
 					bb.setType(rs.getInt("AUTHOR_ID"),
 							rs.getString("SURNAME"));
 					bb.setComponentQuantity(rs.getInt("AUTHOR_ID"),
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
 		url = _url;
 		bookList = new ArrayList<BookBean>(); // a list
 		try {
 
 			// get a database connection and load the JDBC-driver
 
 			Class.forName("com.mysql.jdbc.Driver");
 			conn = DriverManager.getConnection(url);
 
 			// create SQL statements to load the books into the list
 			// each book is a BookBean object
 
 			stmt = conn.createStatement();
 			String sql = "SELECT BOOKS.BOOK_ID, BOOKS.TITLE, NAME AS AUTHOR_NAME, ";
 			sql += "SURNAME AS AUTHOR_SURNAME, COM_ID, ";
 			sql += "AVAILABILITY.FINAL_PRICE AS PRICE , DESCRIPTION, AVAILABILITY, VISIBLE, PROFIT FROM AVAILABILITY, BOOKS,";
 			sql += "AUTHORS, COMPOSITION WHERE AVAILABILITY.BOOK_ID = EL_ID AND AUTHORS.AUTHOR_ID = COM_ID AND BOOKS.BOOK_ID = EL_ID";
 			sql += " ORDER BY BOOK_ID";
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
 							rs.getString("AUTHOR_NAME"));
 					bb.setType(rs.getInt("COM_ID"),
 							rs.getString("AUTHOR_SURNAME"));
 					bb.setPrice(rs.getInt("PRICE"));
 					bb.setProfit(rs.getInt("PROFIT"));
 					bb.setDescription(rs.getString("DESCRIPTION"));
 					bb.setVisible(rs.getBoolean("VISIBLE"));
 					bb.setAvailability(rs.getInt("AVAILABILITY"));
 					bookList.add(bb);
 				} else {
 					bb.setManufacturer(rs.getInt("COM_ID"),
 							rs.getString("AUTHOR_NAME"));
 					bb.setType(rs.getInt("COM_ID"),
 							rs.getString("AUTHOR_SURNAME"));
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
 
 	// return the booklist
 	java.util.Collection<BookBean> getProduktLista() {
 		return bookList;
 	}
 
 	// create an XML document from the booklist
 	public String getXml() throws Exception {
 		refreshBean();
 		BookBean bb = null;
 		Iterator<BookBean> iter = bookList.iterator();
 		StringBuffer buff = new StringBuffer();
 
 		buff.append("<productlist>");
 		while (iter.hasNext()) {
 			bb = iter.next();
 			if (bb.getVisible() && (bb.getAvailability() > 0)) {
 				buff.append(bb.getXml());
 			}
 		}
 		buff.append("</productlist>");
 
 		return buff.toString();
 	}
 
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
