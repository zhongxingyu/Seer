 package br.com.findplaces.jpa.entity;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.LazyCollection;
 import org.hibernate.annotations.LazyCollectionOption;
 
 import br.com.findplaces.jpa.entity.spatial.PlaceSpatial;
 
 @NamedQueries({ @NamedQuery(name = "FindBySpatial", query = "SELECT p FROM Place p where p.spatial.id = :spatial_id"),
 	@NamedQuery(name = "findBysellerID", query = "SELECT p FROM Place p where p.seller.user.id = :id")})
 @Entity
 @Table(name = "TB_PLACE") //TODO Refact this, we have more than one entity here.
 public class Place extends BaseEntity implements Serializable {
 	
 	/** Default value included to remove warning. Remove or modify at will. **/
 	private static final long serialVersionUID = 1L;
 	
 	public static final String findBySpatial = "FindBySpatial";
 	
 	public static final String findBysellerID = "findBysellerID";
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private Long id;
 	
	@OneToMany(cascade=CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.FALSE)
     @JoinTable(name="TB_PLACE_COMMENTS", joinColumns={@JoinColumn(name="PLACE_ID", referencedColumnName="id")}, inverseJoinColumns={@JoinColumn(name="COMMENT_ID", referencedColumnName="id")})
 	private List<Coment> coments;
 	
 	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)//
     private PlaceSpatial spatial;
 	
 	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
 	private Facilities facilities;
 	
 	@ManyToOne()
 	@JoinColumn(name = "seller_id")
 	private Seller seller;
 
 	@Column
 	private String address;
 	
 //	@ManyToOne
 //	private City city;
 //
 //	@ManyToOne
 //	private Neighborhood neighborhood;
 //
 //	@ManyToOne
 //	private Street street;
 
 	@ManyToOne
 	private PlaceType type;		
 	
 	@Column
 	private String code;
 	
 	@Column
 	private Integer packTime;
 	
 	@Column
 	private Date startOffer;
 	
 	@Column
 	private Date endOffer;
 	
 	@Column
 	private String description;
 	
 	@Column
 	private Double price;
 	
 	@Column
 	private Double deposit;
 	
 	@Column
 	private Integer rentMonths;
 	
 	@Column
 	private Integer placeFloor;
 	
 	@Column
 	private Integer qtdPlaceFloor;
 	
 	@Column
 	private Double iptu;
 	
 	@Column
 	private Double condominiumPrice;
 	
 //	@Column
 //	private Double internet;
 //	
 //	@Column
 //	private Double tv;
 //	
 	@Column
 	private Double totalPrice;
 	
 	@Column
 	private Integer room;
 	
 	@Column
 	private Integer bathroom;
 	
 	@Column
 	private Integer bedroom;
 	
 	@Column
 	private Integer suite;
 	
 	@Column
 	private Double m2;
 	
 	@Column
 	private Integer garage;
 	
 	@OneToMany
 	private List<Image> photos;
 	
 	@ManyToMany
 	@LazyCollection(LazyCollectionOption.FALSE)
 	private List<SellType> sellType;
 	
 	//TODO PLEASE WE SHOULD REALLY REFACT THIS WHEN WE HAVE TIME
 	@Column
 	private String cellphone;
 	
 	@Column
 	private String cellphone2;
 	
 //	@Column
 //	private String cellphone3;
 	
 	//Campos do Paulo
 	
 	@Column
 	private Double rent;
 	
 	@Column
 	private Integer contract_time;
 	
 	@Column
 	private Double internet;
 	
 	@Column
 	private Double tv;
 	
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 //	public City getCity() {
 //		return city;
 //	}
 //
 //	public void setCity(City city) {
 //		this.city = city;
 //	}
 //
 //	public Neighborhood getNeighborhood() {
 //		return neighborhood;
 //	}
 //
 //	public void setNeighborhood(Neighborhood neighborhood) {
 //		this.neighborhood = neighborhood;
 //	}
 
 	public Date getStartOffer() {
 		return startOffer;
 	}
 
 	public void setStartOffer(Date startOffer) {
 		this.startOffer = startOffer;
 	}
 
 	public Integer getPlaceFloor() {
 		return placeFloor;
 	}
 
 	public void setPlaceFloor(Integer placeFloor) {
 		this.placeFloor = placeFloor;
 	}
 
 	public Integer getQtdPlaceFloor() {
 		return qtdPlaceFloor;
 	}
 
 	public void setQtdPlaceFloor(Integer qtdPlaceFloor) {
 		this.qtdPlaceFloor = qtdPlaceFloor;
 	}
 
 //	public Street getStreet() {
 //		return street;
 //	}
 //
 //	public void setStreet(Street street) {
 //		this.street = street;
 //	}
 
 	public PlaceType getType() {
 		return type;
 	}
 
 	public void setType(PlaceType type) {
 		this.type = type;
 	}
 	
 
 	public PlaceSpatial getSpatial() {
 		return spatial;
 	}
 
 	public void setSpatial(PlaceSpatial spatial) {
 		this.spatial = spatial;
 	}
 	
 	public String getCode() {
 		return code;
 	}
 
 	public Double getPrice() {
 		return price;
 	}
 
 
 	public Integer getRoom() {
 		return room;
 	}
 
 	public Integer getBathroom() {
 		return bathroom;
 	}
 
 	public Integer getBedroom() {
 		return bedroom;
 	}
 
 	public Integer getSuite() {
 		return suite;
 	}
 
 	public Double getM2() {
 		return m2;
 	}
 	
 	public void setCode(String code) {
 		this.code = code;
 	}
 
 	public void setPrice(Double price) {
 		this.price = price;
 	}	
 
 	public void setRoom(Integer room) {
 		this.room = room;
 	}
 
 	public void setBathroom(Integer bathroom) {
 		this.bathroom = bathroom;
 	}
 
 	public void setBedroom(Integer bedroom) {
 		this.bedroom = bedroom;
 	}
 
 	public void setSuite(Integer suite) {
 		this.suite = suite;
 	}
 
 	public Integer getGarage() {
 		return garage;
 	}
 
 	public void setGarage(Integer garage) {
 		this.garage = garage;
 	}
 
 	public Double getInternet() {
 		return internet;
 	}
 
 	public void setInternet(Double internet) {
 		this.internet = internet;
 	}
 
 	public Double getTv() {
 		return tv;
 	}
 
 	public void setTv(Double tv) {
 		this.tv = tv;
 	}
 
 	public void setM2(Double m2) {
 		this.m2 = m2;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
 	public Seller getSeller() {
 		return seller;
 	}
 
 	public void setSeller(Seller seller) {
 		this.seller = seller;
 	}
 
 	public List<Image> getPhotos() {
 		return photos;
 	}
 
 	public void setPhotos(List<Image> photos) {
 		this.photos = photos;
 	}
 
 	public List<SellType> getSellType() {
 		return sellType;
 	}
 
 	public void setSellType(List<SellType> sellType) {
 		this.sellType = sellType;
 	}
 
 	public List<Coment> getComents() {
 		return coments;
 	}
 
 	public void setComents(List<Coment> coments) {
 		this.coments = coments;
 	}
 
 	public Facilities getFacilities() {
 		return facilities;
 	}
 
 	public void setFacilities(Facilities facilities) {
 		this.facilities = facilities;
 	}
 
 	public Double getDeposit() {
 		return deposit;
 	}
 
 	public void setDeposit(Double deposit) {
 		this.deposit = deposit;
 	}
 
 	public Integer getRentMonths() {
 		return rentMonths;
 	}
 
 	public void setRentMonths(Integer rentMonths) {
 		this.rentMonths = rentMonths;
 	}
 
 	public Double getIptu() {
 		return iptu;
 	}
 
 	public void setIptu(Double iptu) {
 		this.iptu = iptu;
 	}
 
 	public Double getCondominiumPrice() {
 		return condominiumPrice;
 	}
 
 	public void setCondominiumPrice(Double condominiumPrice) {
 		this.condominiumPrice = condominiumPrice;
 	}
 
 //	public Double getInternet() {
 //		return internet;
 //	}
 //
 //	public void setInternet(Double internet) {
 //		this.internet = internet;
 //	}
 //
 //	public Double getTv() {
 //		return tv;
 //	}
 //
 //	public void setTv(Double tv) {
 //		this.tv = tv;
 //	}
 
 	public Double getTotalPrice() {
 		return totalPrice;
 	}
 
 	public void setTotalPrice(Double totalPrice) {
 		this.totalPrice = totalPrice;
 	}
 
 	public String getCellphone() {
 		return cellphone;
 	}
 
 	public void setCellphone(String cellphone) {
 		this.cellphone = cellphone;
 	}
 
 	public String getCellphone2() {
 		return cellphone2;
 	}
 
 	public void setCellphone2(String cellphone2) {
 		this.cellphone2 = cellphone2;
 	}
 
 //	public String getCellphone3() {
 //		return cellphone3;
 //	}
 //
 //	public void setCellphone3(String cellphone3) {
 //		this.cellphone3 = cellphone3;
 //	}
 
 	public Integer getPackTime() {
 		return packTime;
 	}
 
 	public void setPackTime(Integer packTime) {
 		this.packTime = packTime;
 	}
 
 	public Date getEndOffer() {
 		return endOffer;
 	}
 
 	public void setEndOffer(Date endOffer) {
 		this.endOffer = endOffer;
 	}
 
 	public Double getRent() {
 		return rent;
 	}
 
 	public void setRent(Double rent) {
 		this.rent = rent;
 	}
 
 	public Integer getContract_time() {
 		return contract_time;
 	}
 
 	public void setContract_time(Integer contract_time) {
 		this.contract_time = contract_time;
 	}
 
 	
 
 }
