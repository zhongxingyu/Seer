 package ar.edu.itba.paw.grupo1.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class Property {
 
 	public enum PropertyType { HOUSE, FLAT }
 	public enum OperationType { SELLING, LEASING }
	public enum Services {CABLE, PHONE, POOL, LOUNGE, PADDLE, BARBECUE }	
 	private Integer id;
 	private PropertyType propertyType = PropertyType.HOUSE;
 	private OperationType operationType = OperationType.SELLING;
 	private String address;
 	private String neighbourhood;
 	private double price;
 	private int rooms;
 	private double indoorSpace;
 	private double outdoorSpace;
 	private String description; //Optional
 	private int antiquity;
 	private List<Services> services = new ArrayList<Services>();
 	private boolean published;
 	private int userId;
 	
 	public Property() {
 	
 	}
 	
 	public Property(Integer id, PropertyType propertyType, OperationType operationType, String address,
 			String neighbourhood, double price, int rooms,
 			double indoorSpace, double outdoorSpace, 
 			String description, int antiquity, List<Services> services,
 			boolean published, int userId) {
 		
 		this.id = id;
 		this.propertyType = propertyType;
 		this.operationType = operationType;
 		this.address = address;
 		this.neighbourhood = neighbourhood;
 		this.price = price;
 		this.rooms = rooms;
 		this.indoorSpace = indoorSpace;
 		this.outdoorSpace = outdoorSpace;
 		this.description = description;
 		this.antiquity = antiquity;
 		this.services = services;
 		this.published = published;
 		this.userId = userId;
 	}
 	
 	public Property(PropertyType propertyType, OperationType operationType, String address,
 			String neighbourhood, double price, int rooms,
 			double indoorSpace, double outdoorSpace, 
 			String description, int antiquity, List<Services> services, 
 			boolean published, int userId) {
 		
 		this.propertyType = propertyType;
 		this.operationType = operationType;
 		this.address = address;
 		this.neighbourhood = neighbourhood;
 		this.price = price;
 		this.rooms = rooms;
 		this.indoorSpace = indoorSpace;
 		this.outdoorSpace = outdoorSpace;
 		this.description = description;
 		this.antiquity = antiquity;
 		this.services = services;
 		this.published = published;
 		this.userId = userId;
 	}
 	
 	
 	public String getDescription() {
 		return description;
 	}
 	
 	public Integer getId() {
 		return id;
 	}
 	
 	public double getIndoorSpace() {
 		return indoorSpace;
 	}
 	
 	public String getNeighbourhood() {
 		return neighbourhood;
 	}
 	
 	public int getOperationType() {
 		return operationType.ordinal();
 	}
 	
 	public double getOutdoorSpace() {
 		return outdoorSpace;
 	}
 	
 	public double getPrice() {
 		return price;
 	}
 	
 	public int getPropertyType() {
 		return propertyType.ordinal();
 	}
 	
 	public int getRooms() {
 		return rooms;
 	}
 	
 	public boolean isBarbecue() {
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i) == Services.BARBECUE) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isCable() {
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i) == Services.CABLE) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isLounge() {
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i) == Services.LOUNGE) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isPaddle() {
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i) == Services.PADDLE) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isPhone() {
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i) == Services.PHONE) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isPool() {
 		for (int i = 0; i < services.size(); i++) {
 			if (services.get(i) == Services.POOL) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isPublished() {
 		return published;
 	}
 	
 	public int getUserId() {
 		return userId;
 	}
 
 	public String getAddress() {
 		return address;
 	}	
 	
 	public int getAntiquity() {
 		return antiquity;
 	}	
 	
 	public boolean isNew() {
 		return id == null;
 	}
 
 	public void publish() {
 		published = true;
 		return;
 	}
 	
 	public void unpublish() {
 		published = false;
 		return;
 	}	
 }
