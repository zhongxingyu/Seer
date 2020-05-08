 package network;
 
 import java.util.Vector;
 
 import app.model.User;
 
 public class NetworkNotification implements java.io.Serializable {
 
 	private static final long serialVersionUID = -2406982845299543963L;
 
 	private int action;
 	private String name;
 	private String product;
 	private String type;
 	private int price;
 	private byte[] productChunk;
 	
 	private User user;
 	private String ip;
 	private int port;
 	private Vector<String> products;
 
 	public NetworkNotification(int action, String name, String product,
 			int price) {
 		this.action = action;
 		this.product = product;
 		this.name = name;
 		this.price = price;
 	}
 	
 	public NetworkNotification(int action, User user) {
 		this.action = action;
 		
 		this.user = user;
 		
 		if (user != null) {
 			this.name = user.getName();
 			this.ip = user.getIp();
 			this.port = user.getPort();
 			this.products = user.getProducts();
 		}
 	}
 
 	public NetworkNotification(int action, String username, String password,
 			String type, String ip, int port, Vector<String> products) {
 		this.action = action;
 		this.name = username;
 		this.product = password;
 		this.type = type;
 		this.products = products;
 		this.ip = ip;
 		this.port = port;
 	}
 
 	public NetworkNotification(int action, String name, String product,
 			int price, byte[] chunk) {
 
 		this.action = action;
 		this.product = product;
 		this.name = name;
 		this.productChunk = chunk;
 	}
 
 	public String toString() {
 		return "[ " + this.action + " - " + this.name + " - " + this.product
 				+ " - " + this.price + " ]";
 	}
 
 	public int getAction() {
 		return action;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getProduct() {
 		return this.product;
 	}
 	
 	public void setProduct(String product) {
 		this.product = product;
 	}
 
 	public int getPrice() {
 		return price;
 	}
 	
 	public String getType() {
 		return type;
 	}
 	
 	public String getIp() {
 		return ip;
 	}
 	
 	public void setIp(String ip) {
 		this.ip = ip;
 	}
 	
 	public int getPort() {
 		return port;
 	}
 
 	public void setPort(int port) {
 		this.port = port;
 	}
 	
 	public User getUser() {
 		return user;
 	}
 	
 	public Vector<String> getProducts() {
 		return products;
 	}
 	
 	public void setProducts(Vector<String> products) {
 		this.products = products;
 	}
 	
 	public byte[] getChunk() {
 		return productChunk;
 	}
 }
