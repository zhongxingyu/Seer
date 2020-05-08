 package info.berryworks.photoorder.model;
 
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 /**
  * Model class which will store the Order Items
  * 
  * @author Darrell Dupas
  * 
  */
 @Entity
 public class POOrder {
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
	private String orderdate;
 	
 	private String customeremail;
 	private String photourl;
 	private String type;  
 	private String quantity;
 	private String price; 
 	private String albumid;
	
 	private String confirmeddate;
 	private String emailconfirmeddate;
 	private String paiddate;
 	private String finisheddate;
 
 	public POOrder(String customeremail, String photourl, String type,
 			String quantity, String albumid, String orderdate ) {
 
 		int iquantity = 0;
 		double costper = 0.5;
 		if ( 0 == type.compareTo("add to cd") ) {
 			costper = 0.5;
 		} else 
 		if ( 0 == type.compareTo("16 x 20 wall portrait") ) {
 			costper = 30;
 		} else 
 		if ( 0 == type.compareTo("11 x 14 wall portrait") ) {
 			costper = 20;
 			
 		} else 
 		if ( 0 == type.compareTo("8 x 10") ) {
 			costper = 10;
 		} else
 		if ( 0 == type.compareTo("5 x 7") ) {
 			costper = 5;
 		} else 
 		if ( 0 == type.compareTo("4 x 6") ) {
 			costper = 3;
 		} else
 		if ( 0 == type.compareTo("3.5 x 5") ) {
 				costper = 2.5;
 		} else
 		if ( 0 == type.compareTo("wallets (8)") ) {
 				costper = 10;
 		}
 		
 		iquantity =  Integer.parseInt(quantity);
 	
 		this.customeremail = customeremail; 
 		this.photourl = photourl; 
 		this.type = type;  
 		this.quantity = quantity; 
 		this.price = new java.text.DecimalFormat("0.00").format(iquantity * costper);
 		
 		this.orderdate = orderdate;
 		this.albumid = albumid;
 		
 		
 	}
 	
 	/*
 	private String nullltos(Long L) {
 		if (L == null) return "";
 		return ""+L;
 	}*/
 	private String nullstos(String S) {
 		if (S==null) return "";
 		return S;
 	}
 	
 	
 	public String toString() {
 		String out = "<order>" + "\n" +
 				"<id>" + this.id + "</id>" + "\n" +	
 		"<orderdate>" + this.orderdate + "</orderdate>" + "\n" +
 		"<confirmeddate>" + this.nullstos( confirmeddate) + "</confirmeddate>" + "\n" +
 		"<emailconfirmeddate>" + this.nullstos( emailconfirmeddate) +  "</emailconfirmeddate>" + "\n" +	
 		"<paiddate>" + this.nullstos(paiddate) + "</paiddate>" + "\n" +
 		"<finisheddate>" + this.nullstos(finisheddate) + "</finisheddate>" + "\n" +
 		"<customeremail>" + this.customeremail + "</customeremail>" + "\n" +
 		"<type>" + this.type + "</type>" + "\n" +
 		"<quantity>" + this.quantity + "</quantity>" + "\n" + 
 		"<price>" + this.price + "</price>" + "\n" +
 		"<album>" + this.albumid + "</album>" + "\n" +
 		"<photourl>" + this.photourl + "</photourl>" + "\n" +
 				"</order>";
 		return out;
 	}
 	public Long getId() {
 		return id;
 	}
 	
 	public String getAlbumid() {
 		return this.albumid;
 	}
 	public void setAlbumid(String aid) {
 		this.albumid = aid;
 	}
 	
 	public String getPrice() {
 		return this.price;
 	}
 	public void setPrice(String price) {
 		this.price = price;
 	}
 	
 	public String getQuantity() {
 		return this.quantity;
 	}
 	public void setQuantity(String quantity) {
 		this.quantity = quantity;
 	}
 	
 	public void setCustomeremail(String sid) {
 		this.customeremail = sid;
 	}
 	public String getCustomeremail() {
 		return customeremail;
 	}
 	
 	public String getPhotoid() {
 		return photourl;
 	}
 	public void setPhotoid( String pid) {
 		this.photourl = pid;
 	}
 	
 	public String getType() {
 		return this.type;
 	}
 	public void setType(String type) {
 		this.type = type;
 	}
 	
 	public String getOrderdate() {
 		return this.orderdate;
 	}
 	public void setOrderdate(String orderdate) {
 		this.orderdate = orderdate;
 	}
 	
 	public void setConfirmeddate(String cf) {
 		this.confirmeddate = cf;
 	}
 	public String getConfirmeddate(){
 		return this.confirmeddate;
 	}
 		
 	public String getEmailconfirmeddate(){
 		return this.emailconfirmeddate;
 	}
 	public void setEmailconfirmeddate(String ecd) {
 		this.emailconfirmeddate = ecd;
 	}
 	public String getPaiddate() {
 		return this.paiddate;
 	}
 	public void setPaiddate(String pd) {
 		this.paiddate = pd;
 	}
 	
 	public String getFinisheddate() {
 		return this.finisheddate;
 	}
 	public void setFinisheddate(String fd) {
 		this.finisheddate = fd;
 	}
 	
 	public String getStatusString() {
 		int is = getStatusInt();
 		
 		if ( is == 5) return "finished";
 		if ( is == 4) return "paid";
 		if ( is == 3) return "email confirmed";
 		if ( is == 2) return "confirmed";
 		if ( is == 1) return "ordered";
 		
 		return "error";
 	}
 	
 	public int getStatusInt() {
 		
 		String nullstring = "";
 		
 		String s = this.getFinisheddate();
 		if ( s != null && nullstring.compareTo(s) != 0 ) return 5;
 		
 		s = this.getPaiddate();
 		if ( s != null && nullstring.compareTo(s) != 0 ) return 4;
 		
 		s = this.getEmailconfirmeddate();
 		if ( s != null && nullstring.compareTo(s) != 0 ) return 3;
 		
 		s = this.getConfirmeddate();
 		if ( s != null && nullstring.compareTo(s) != 0 ) return 2;
 		
 		s = this.getOrderdate();
 		if ( s != null && nullstring.compareTo(s) != 0 ) return 1;
 		
 		return 0;
 	}
 	
 	
 	
 }
 	
 	
 
