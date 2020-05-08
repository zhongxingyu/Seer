 package com.pmf.entities;
 
 import java.io.Serializable;
 import javax.persistence.*;
 
 import com.pmf.util.EjbConstants;
 
 @Entity
 @Table(name=EjbConstants.TABLE_CONTACTS)
 @NamedQueries({@NamedQuery(name = "contact.findAll", query = "SELECT d FROM Contact d where d.status='1'")})
 public class Contact implements Serializable{
 	private static final long serialVersionUID = 1L;
 	
 	@Id
     @Column(name="id")
     @GeneratedValue(strategy=GenerationType.IDENTITY)
 	private int id;
 	@Column(name="country")
 	private String country;		 
 	
 	@Column(name="city")
 	private String city;
 	
 	@Column(name="sector")
 	private String sector;
 	
 	@Column(name="street")
 	private String street;
 	
 	@Column(name="number")
 	private String number;
 	
 	@Column(name="apto")
 	private String apto;
 	
 	@Column(name="telephone")
 	private String telephone;
 	
 	@Column(name="cell")
 	private String cell;
 	
 	@Column(name="bbpin")
 	private String BBPin;
 	
 	@Column(name="email")
 	private String email;
 	
 	@Column(name="status")
 	private int status;
 	
     @JoinTable(
             name="UserContactMap",
             joinColumns = {@JoinColumn(name="id",nullable=false)},
           inverseJoinColumns = {@JoinColumn(name="id",nullable=false)})
 	private User user;
 
 	public Contact(){
 		super();
 	}
 
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getSector() {
 		return sector;
 	}
 	public void setSector(String sector) {
 		this.sector = sector;
 	}
 
 	public String getApto() {
 		return apto;
 	}
 	public void setApto(String apto) {
 		this.apto = apto;
 	}
 
 	public int getStatus() {
 		return status;
 	}
 	public void setStatus(int status) {
 		this.status = status;
 	}
 	
 	public User getUser() {
 		return user;
 	}
 	public void setUser(User user) {
 		this.user = user;
 	}
 	public String getCountry() {
 		return country;
 	}
 	public void setCountry(String country) {
 		this.country = country;
 	}
 	public String getCity() {
 		return city;
 	}
 	public void setCity(String city) {
 		this.city = city;
 	}
 	public String getStreet() {
 		return street;
 	}
 	public void setStreet(String street) {
 		this.street = street;
 	}
 	public String getNumber() {
 		return number;
 	}
 	public void setNumber(String number) {
 		this.number = number;
 	}
 	public String getTelephone() {
 		return telephone;
 	}
 	public void setTelephone(String telephone) {
 		this.telephone = telephone;
 	}
 	public String getCell() {
 		return cell;
 	}
 	public void setCell(String cell) {
 		this.cell = cell;
 	}
 	public String getBBPin() {
 		return BBPin;
 	}
 	public void setBBPin(String bBPin) {
 		BBPin = bBPin;
 	}
 	public String getEmail() {
 		return email;
 	}
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	
 }
