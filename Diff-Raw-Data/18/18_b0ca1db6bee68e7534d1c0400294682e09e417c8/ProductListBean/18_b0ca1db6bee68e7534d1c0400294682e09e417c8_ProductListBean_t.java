 /*
  * ProductListBean.java
  *
  */
 package beans;
 
 import java.util.*;
 import java.sql.*;
 import java.io.*;
 /**
  *
  * @author  Elham Roshan, Darina Buhcheva
  */
 public class ProductListBean {
     
     private Collection productList;
     private String url=null;
 
     // this constructor is not really used in the application
     // but is here for testing purpose
 
     public ProductListBean() throws Exception{
       this(
          "jdbc:mysql://localhost/camerashop?user=root&password=");
     }
     
     /** Creates a new instance of BookListBean */
 
     public ProductListBean(String _url) throws Exception {
         url=_url;
         Connection conn =null;
         Statement stmt = null;
         ResultSet rs = null;
         productList = new ArrayList();    // a list
         try{
             
 	    // get a database connection and load the JDBC-driver
 
             Class.forName("com.mysql.jdbc.Driver");
             conn=DriverManager.getConnection(url);
             
 	    // create SQL statements to load the products into the list
 	    // each product is a ProductBean object
 
             stmt = conn.createStatement();
            String sql="SELECT products.ID, products.Name, products.Description, sum( components.price ) AS price " +
                        "FROM products " +
                        "JOIN combination " +
                        "JOIN components ON products.ID = combination.product_ID " +
                        "AND combination.component_ID = components.ID group by products.ID";
 
             rs= stmt.executeQuery(sql);
             
 	    // analyze the result set
 
             while(rs.next()){
                 
                 ProductBean pb = new ProductBean();
                 
                 pb.setId(rs.getInt("ID"));
                 pb.setName(rs.getString("NAME"));
                 pb.setDescription(rs.getString("DESCRIPTION"));
                pb.setPrice(rs.getInt("price"));
                 productList.add(pb);
                 // add price maybe*?
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
     
     java.util.Collection getProductList() {
         return productList;
     }
     
     // create an XML document from the booklist
 
     public String getXml() {
         
         ProductBean pb=null;
         Iterator iter = productList.iterator();
         StringBuffer buff = new StringBuffer();
         
         buff.append("<productlist>");
         while(iter.hasNext()){
             pb=(ProductBean)iter.next();
             buff.append(pb.getXml());
         }
         buff.append("</productlist>");        
         
         return buff.toString();
     }
     
 
     // search for a book by book ID
 
     public ProductBean getById(int id) {
 	ProductBean pb = null;
 	Iterator iter = productList.iterator();
         
 	while(iter.hasNext()){
 	    pb=(ProductBean)iter.next();
 	    if(pb.getId()== id){
                 return pb;
 	    }
 	}
 	return null;
     }
     
     // a main used for testing, remember that a bean can be run
     // without a container
 
     public static void main(String[] args){
         try{
 	    ProductListBean plb = new ProductListBean();
 	    System.out.println(plb.getXml());
         }
         catch(Exception e){
 	    System.out.println(e.getMessage());
         }
     }
 }
 
