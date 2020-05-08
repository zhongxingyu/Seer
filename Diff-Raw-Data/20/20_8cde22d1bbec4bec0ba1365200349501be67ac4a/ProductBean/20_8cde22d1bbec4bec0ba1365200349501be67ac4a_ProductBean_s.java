 package beans;
 
 /**
  *
  * @author Alexander Fougner
  */
 public class ProductBean {
 
     private int id;
     private String title;
     private int price;
     private String description;
     private int qty;
     
     public ProductBean() {
     }
     
     public int getPrice() {
         return price;
     }
     
     public void setPrice(int _price) {
         price = _price;
     }
     
     public String getTitle() {
         return title;
     }
     
     public void setTitle(String _title) {
         title=_title;
     }
     
     public int getId() {
         return id;
     }
     
     public void setId( int _id) {
         id= _id;
     }
 
     public void setDescription(String _description) {
         description=_description;
     }
 
     public String getDescription() {
         return description;
     }
 
     public int getQuantity() {
       return qty;
     }
 
     public void setQuantity(int _qty) {
       qty = _qty;
     }
 
     public String getXml() {
 
 	// use a Stringbuffer (not String) to avoid multiple
 	// object creation
 
      StringBuffer xmlOut = new StringBuffer();
       
       xmlOut.append("<product>");
       xmlOut.append("<id>");
       xmlOut.append(id);
       xmlOut.append("</id>");      
       xmlOut.append("<title><![CDATA[");
       xmlOut.append(title);
       xmlOut.append("]]></title>");
       xmlOut.append("<price>");
       xmlOut.append(price);      
       xmlOut.append("</price>");
       xmlOut.append("<description><![CDATA[");
       xmlOut.append(description);      
       xmlOut.append("]]></description>");
       xmlOut.append("<quantity><![CDATA[");
       xmlOut.append(qty);      
       xmlOut.append("]]></quantity>");
       xmlOut.append("</product>");
       
       return xmlOut.toString();
     }   
 }
