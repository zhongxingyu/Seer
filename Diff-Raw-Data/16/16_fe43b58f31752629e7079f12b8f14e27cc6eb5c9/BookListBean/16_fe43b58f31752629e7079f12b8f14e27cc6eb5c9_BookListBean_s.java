 /*
  * BookListBean.java
  *
  */
 package beans;
 
 import java.util.*;
 import java.sql.*;
 import java.io.*;
 /**
  *
 * @author  Fredrik lund, Olle Eriksson
  */
 public class BookListBean {
     
     private Collection bookList;
     private String url=null;
 
     // this constructor is not really used in the application
     // but is here for testing purpose
 
     public BookListBean() throws Exception{
       this(
           "jdbc:mysql://localhost/olle?user=olle&password=QEYIXYTY");
     }
     
     /** Creates a new instance of BookListBean */
 
     public BookListBean(String _url) throws Exception {
         url=_url;
         Connection conn =null;
         Statement stmt = null;
         ResultSet rs = null;
         bookList = new ArrayList();    // a list
         try{
             
 	    // get a database connection and load the JDBC-driver
 
             Class.forName("com.mysql.jdbc.Driver");
             conn=DriverManager.getConnection(url);
             
 	    // create SQL statements to load the books into the list
 	    // each book is a BookBean object
 
             stmt = conn.createStatement();
             String sql="SELECT BOOK_ID, TITLE, NAME AS AUTHOR_NAME, ";
 	    sql += "SURNAME AS AUTHOR_SURNAME, ";
             sql += "PRICE, PAGES, DESCRIPTION FROM BOOKS,";
 	    sql += "AUTHORS WHERE BOOKS.AUTHOR_ID=AUTHORS.AUTHOR_ID";
             rs= stmt.executeQuery(sql);
             
 	    // analyze the result set
 
             while(rs.next()){
                 
                 BookBean bb = new BookBean();
                 
                 bb.setId(rs.getInt("BOOK_ID"));
                 bb.setTitle(rs.getString("TITLE"));
                 bb.setAuthorName(rs.getString("AUTHOR_NAME"));
                 bb.setAuthorSurname(rs.getString("AUTHOR_SURNAME"));
                 bb.setPrice(rs.getInt("PRICE"));
                 bb.setPages(rs.getInt("PAGES"));
                 bb.setDescription(rs.getString("DESCRIPTION"));
                 bookList.add(bb);
                 
             }
         
         }
         catch(SQLException sqle){
             throw new Exception(sqle);
         }
 
 	// note the we always try to close all services
 	// even if one or more fail to close
 	
         finally{
  	    try{
               rs.close();
             }
             catch(Exception e) {}
             try{
               stmt.close();
             }
 	    catch(Exception e) {}
             try {
               conn.close();
             }
             catch(Exception e){}
         }
     }
     
     // return the booklist
     
     java.util.Collection getProduktLista() {
         return bookList;
     }
     
     // create an XML document from the booklist
 
     public String getXml() {
         
         BookBean bb=null;
         Iterator iter = bookList.iterator();
         StringBuffer buff = new StringBuffer();
         
         buff.append("<booklist>");
         while(iter.hasNext()){
             bb=(BookBean)iter.next();
             buff.append(bb.getXml());
         }
         buff.append("</booklist>");        
         
         return buff.toString();
     }
     
 
     // search for a book by book ID
 
     public BookBean getById(int id) {
 	BookBean bb = null;
 	Iterator iter = bookList.iterator();
         
 	while(iter.hasNext()){
 	    bb=(BookBean)iter.next();
 	    if(bb.getId()== id){
                 return bb;
 	    }
 	}
 	return null;
     }
     
     // a main used for testing, remember that a bean can be run
     // without a container
 
     public static void main(String[] args){
         try{
 	    BookListBean blb = new BookListBean();
 	    System.out.println(blb.getXml());
         }
         catch(Exception e){
 	    System.out.println(e.getMessage());
         }
     }
 }
 
