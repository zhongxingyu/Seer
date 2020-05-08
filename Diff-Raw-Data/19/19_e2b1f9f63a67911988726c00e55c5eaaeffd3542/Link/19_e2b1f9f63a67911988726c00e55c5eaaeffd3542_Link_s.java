 package net.marioosh.spring.springonly.model.entities;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 import org.hibernate.validator.constraints.NotEmpty;
 
 public class Link {
 	private Integer id;
 
 	@NotEmpty(message="Address may not be empty")
 	private String address;
 
 	private String name = "";
 	
	private String description;
 	
 	private Date ldate;
 	
 	private Date dateMod;
 	
 	private int clicks;
 
 	public Link() {
 	}
 	
 	public Link(Integer id, String address, String name) {
 		super();
 		this.id = id;
 		this.address = address;
 		this.name = name;
 	}
 
 	public Link(String address, String name) {
 		super();
 		this.address = address;
 		this.name = name;
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getAddress() {
 		return address.startsWith("http://") || address.startsWith("https://") ? address : "http://"+address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address.startsWith("http://") || address.startsWith("https://") ? address : "http://"+address;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 	
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
 	public Date getLdate() {
 		return ldate;
 	}
 	
 	public void setLdate(Date ldate) {
 		this.ldate = ldate;
 	}
 	
 	public int getClicks() {
 		return clicks;
 	}
 	
 	public void setClicks(int clicks) {
 		this.clicks = clicks;
 	}
 	
 	public Date getDateMod() {
 		return dateMod;
 	}
 	
 	public void setDateMod(Date dateMod) {
 		this.dateMod = dateMod;
 	}
 	
 	public String getHostName() {
 		try {
 			return new URL(address).getHost();
 		} catch (MalformedURLException e) {
 			return "";
 		}
 	}
 }
