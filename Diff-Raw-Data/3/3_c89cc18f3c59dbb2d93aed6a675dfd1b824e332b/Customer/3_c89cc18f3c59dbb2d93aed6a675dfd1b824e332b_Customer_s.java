 package com.twobytes.model;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="customer")
 public class Customer extends BaseColumn implements Serializable {
 
 	private static final long serialVersionUID = -5384306818157531448L;
 	
 	private String customerID;
 	private CustomerType customerType;
 	private String name;
 	private String address;
 	private Subdistrict subdistrict;
 	private District district;
 	private Province province;
 	private Integer zipcode;
 	private String tel;
 	private String mobileTel;
 	private String email;
 	
 	@Id
     @Column(name="customerID")
    @GeneratedValue
 	public String getCustomerID() {
 		return customerID;
 	}
 	
 	public void setCustomerID(String customerID) {
 		this.customerID = customerID;
 	}
 	
 	@ManyToOne
 	@JoinColumn(name="customerTypeID")
 	public CustomerType getCustomerType() {
 		return customerType;
 	}
 
 	public void setCustomerType(CustomerType customerType) {
 		this.customerType = customerType;
 	}
 
 	@Column(name="name")
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	@Column(name="address")
 	public String getAddress() {
 		return address;
 	}
 	
 	public void setAddress(String address) {
 		this.address = address;
 	}
 	
 	@ManyToOne
 	@JoinColumn(name = "subdistrictID")
 	public Subdistrict getSubdistrict() {
 		return subdistrict;
 	}
 	
 	public void setSubdistrict(Subdistrict subdistrict) {
 		this.subdistrict = subdistrict;
 	}
 	
 	@ManyToOne
 	@JoinColumn(name = "districtID")
 	public District getDistrict() {
 		return district;
 	}
 	
 	public void setDistrict(District district) {
 		this.district = district;
 	}
 	
 	@ManyToOne
 	@JoinColumn(name = "provinceID")
 	public Province getProvince() {
 		return province;
 	}
 	
 	public void setProvince(Province province) {
 		this.province = province;
 	}
 	
 	@Column(name="zipcode")
 	public Integer getZipcode() {
 		return zipcode;
 	}
 
 	public void setZipcode(Integer zipcode) {
 		this.zipcode = zipcode;
 	}
 
 	@Column(name="tel")
 	public String getTel() {
 		return tel;
 	}
 	
 	public void setTel(String tel) {
 		this.tel = tel;
 	}
 	
 	@Column(name="mobileTel")
 	public String getMobileTel() {
 		return mobileTel;
 	}
 	
 	public void setMobileTel(String mobileTel) {
 		this.mobileTel = mobileTel;
 	}
 	
 	@Column(name="email")
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	
 }
