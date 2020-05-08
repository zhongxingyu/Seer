 package tables;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import users.Conn;
 
 /**
  * This class represents the BookCopy table in the database.
  * 
  * @author Christiaan Fernando
  * 
  */
 
 public class BookCopy implements Table {
 
 	private String copyNo;
 	private String status; // TODO This needs to be clarified
         /*
          * status can be "in", "out", "on-hold", or "overdue"
          * see requirements
          */
 	private Book b;
 
 	private Connection c;
 
 	/**
 	 * Default Constructor
 	 */
 	public BookCopy() {
 		c = Conn.getInstance().getConnection();
 	}
 
 	/**
 	 * BookCopy Constructor
 	 * 
 	 * This constructor creates a BookCopy object with the given copyNo, status,
 	 * and b.
 	 * 
 	 * @param copyNo
 	 *            The copyNo of the BookCopy
 	 * @param status
 	 *            The status of the BookCopy
 	 * @param b
 	 *            The Book that shares the callNo of the BookCopy object
 	 */
 	public BookCopy(String copyNo, String status, Book b) {
 		this.copyNo = copyNo;
 		this.status = status;
 		this.b = b;
 
 		c = Conn.getInstance().getConnection();
 	}
 
 	@Override
 	public String[][] display() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * Updates the SQL table.
 	 * 
 	 * This updates this BookCopy item in the BookCopy table. This assumes the
 	 * item already exists.
 	 */
 	@Override
 	public void update() throws SQLException {
 		PreparedStatement ps;
 
		ps = c.prepareStatement("UPDATE bookCopy SET copyNo = '?', status = '?' WHERE callNo = '?'");
 
 		ps.setString(3, b.getCallNumber());
 		ps.setString(1, copyNo);
 		ps.setString(2, status);
 		int rowCount = ps.executeUpdate();
 		if (rowCount == 0) {
 			// Throw Exception
 		}
 		c.commit();
 		ps.close();
 	}
 
 	/**
 	 * Deletes from the SQL table.
 	 * 
 	 * This deletes the BookCopy item from the BookCopy table.
 	 */
 	@Override
 	public boolean delete() throws SQLException {
 		PreparedStatement ps;
 
		ps = c.prepareStatement("DELETE FROM bookCopy WHERE callNo = '?'");
 
 		ps.setString(1, b.getCallNumber());
 
 		int rowCount = ps.executeUpdate();
 		if (rowCount == 0) {
 			// throw exception
 		}
 
 		c.commit();
 		ps.close();
 
 		return false;
 	}
 
 	/**
 	 * Inserts into the SQL table.
 	 * 
 	 * This inserts the HoldRequest item into the HoldRequest table. This
 	 * assumes the item doesn't already exist.
 	 */
 	@Override
 	public boolean insert() throws SQLException {
 		PreparedStatement ps;
 
		ps = c.prepareStatement("UPDATE bookCopy SET copyNo = '?', status = '?' WHERE callNo = '?'");
 
 		ps.setString(3, b.getCallNumber());
 		ps.setString(1, copyNo);
 		ps.setString(2, status);
 
 		int rowCount = ps.executeUpdate();
 		if (rowCount == 0) {
 			// Throw Exception
 		}
 
 		c.commit();
 		ps.close();
 
 		return false;
 	}
 
 	/**
 	 * Get all BookCopy objects in the BookCopy table.
 	 * 
 	 * @throws SQLException
 	 */
 	@Override
 	public Collection<Table> getAll() throws SQLException {
 		PreparedStatement ps;
 		ResultSet rs;
 
 		Collection<Table> bc = new ArrayList<Table>();
 		ps = c.prepareStatement("SELECT * FROM BookCopy");
 
 		rs = ps.executeQuery();
 
 		while (rs.next()) {
 			Book b = new Book();
 			b.setCallNumber(rs.getString(1));
 			b = (Book) b.get();
 			bc.add(new BookCopy(rs.getString(2), rs.getString(3), b));
 		}
 
 		return bc;
 	}
 
 	/**
 	 * Get all BookCopy objects in the BookCopy table that match the given Book.
 	 * 
 	 * Given a Book, find all the BookCopy objects in the BookCopy table that
 	 * have the Book's callNo.
 	 * 
 	 * @param b
 	 *            The Book that shares the callNo of the BookCopy object
 	 * @return
 	 * @throws SQLException
 	 */
 	public Collection<Table> getAll(Book b) throws SQLException {
 		PreparedStatement ps;
 		ResultSet rs;
 
 		Collection<Table> bc = new ArrayList<Table>();
		ps = c.prepareStatement("SELECT * FROM BookCopy WHERE callNo = '?'");
 		ps.setString(1, b.getCallNumber());
 
 		rs = ps.executeQuery();
 
 		while (rs.next()) {
 			bc.add(new BookCopy(rs.getString(2), rs.getString(3), b));
 		}
 
 		return bc;
 	}
 
 	/**
 	 * Return the BookCopy object corresponding with the set Book and copyNo.
 	 * 
 	 * Given a BookCopy object with an initialized Book and copyNo field, this
 	 * returns the BookCopy object with those id fields that exists in the SQL
 	 * database. This is used if either the default constructor was called and
 	 * the parameters are required, or if some of the parameters are changed and
 	 * the user wants the database object.
 	 * 
 	 * @throws SQLException
 	 * 
 	 */
 	@Override
 	public Table get() throws SQLException {
 		PreparedStatement ps;
 		ResultSet rs;
 
		ps = c.prepareStatement("SELECT * FROM BookCopy WHERE copyNo = '?', callNo = '?'");
 		ps.setString(1, this.b.getCallNumber());
 		ps.setString(2, this.copyNo);
 
 		rs = ps.executeQuery();
 
 		while (rs.next()) {
 			BookCopy bc = new BookCopy();
 			bc.setB(b);
 			bc.setCopyNo(copyNo);
 			bc.setStatus(rs.getString(3));
 			return bc;
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the BookCopy object corresponding with the given Book and copyNo.
 	 * 
 	 * Given a Book and a copyNo, this returns the BookCopy object in the SQL
 	 * database that match the given fields.
 	 * 
 	 * @param copyNo
 	 *            The copy number of the BookCopy object
 	 * @param b
 	 *            The Book that shares the callNo of the BookCopy object
 	 * @return
 	 * @throws SQLException
 	 */
 	public Table get(String copyNo, Book b) throws SQLException {
 		PreparedStatement ps;
 		ResultSet rs;
 
		ps = c.prepareStatement("SELECT * FROM BookCopy WHERE copyNo = '?', callNo = '?'");
 		ps.setString(1, b.getCallNumber());
 		ps.setString(2, copyNo);
 
 		rs = ps.executeQuery();
 
 		while (rs.next()) {
 			BookCopy bc = new BookCopy();
 			bc.setB(b);
 			bc.setCopyNo(copyNo);
 			bc.setStatus(rs.getString(3));
 			return bc;
 		}
 		return null;
 	}
 
 	/**
 	 * @return the copyNo
 	 */
 	public String getCopyNo() {
 		return copyNo;
 	}
 
 	/**
 	 * @param copyNo
 	 *            the copyNo to set
 	 */
 	public void setCopyNo(String copyNo) {
 		this.copyNo = copyNo;
 	}
 
 	/**
 	 * @return the status
 	 */
 	public String getStatus() {
 		return status;
 	}
 
 	/**
 	 * @param status
 	 *            the status to set
 	 */
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 	/**
 	 * @return the b
 	 */
 	public Book getB() {
 		return b;
 	}
 
 	/**
 	 * @param b
 	 *            the b to set
 	 */
 	public void setB(Book b) {
 		this.b = b;
 	}
 
 }
