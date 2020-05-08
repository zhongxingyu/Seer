 /*
  * BookBean.java
  *
  */
 package beans;
 
 /**
  *
 * @author Olle Eriksson
  */
 public class BookBean {
 
     // describe a book
     
     private int id;
     private String title;
     private int price;
     private String authorName;
     private String authorSurname;
     private int pages;
     private String description;
     
     
     /** Creates a new instance of BookBean */
     public BookBean() {
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
 
     public String getAuthorName() {
         return authorName;
     }
     
     public void setAuthorName(String _authorName) {
         authorName=_authorName;
     }
 
     public String getAuthorSurname() {
         return authorSurname;
     }
     
     public void setAuthorSurname(String _authorSurname) {
         authorSurname=_authorSurname;
     }
     
     public int getId() {
         return id;
     }
     
     public void setId( int _id) {
         id= _id;
         
     }
     public int getPages() {
         return pages;
     }
     
     public void setPages( int _pages) {
         pages = _pages;
     }
 
     public void setDescription(String _description) {
         description=_description;
     }
 
     public String getDescription() {
         return description;
     }
 
     // create an XML document describing the book
     
     public String getXml() {
 
 	// use a Stringbuffer (not String) to avoid multiple
 	// object creation
 
      StringBuffer xmlOut = new StringBuffer();
       
       xmlOut.append("<book>");
       xmlOut.append("<id>");
       xmlOut.append(id);
       xmlOut.append("</id>");      
       xmlOut.append("<title><![CDATA[");
       xmlOut.append(title);
       xmlOut.append("]]></title>");
       xmlOut.append("<authorname><![CDATA[");
       xmlOut.append(authorName);
       xmlOut.append("]]></authorname>");
       xmlOut.append("<authorsurname><![CDATA[");
       xmlOut.append(authorSurname);
       xmlOut.append("]]></authorsurname>");
       xmlOut.append("<price>");
       xmlOut.append(price);      
       xmlOut.append("</price>");
       xmlOut.append("<pages>");
       xmlOut.append(pages);
       xmlOut.append("</pages>");
       xmlOut.append("<description><![CDATA[");
       xmlOut.append(description);      
       xmlOut.append("]]></description>");   
       xmlOut.append("</book>");
       
       return xmlOut.toString();
     
         
     }   
 }
