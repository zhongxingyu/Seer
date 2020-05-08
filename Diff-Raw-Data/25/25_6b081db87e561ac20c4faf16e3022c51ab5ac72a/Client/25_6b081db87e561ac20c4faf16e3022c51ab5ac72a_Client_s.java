 package com.pharmasynth.model;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  * Class that represents the client
  * @author jonataschagas
  *
  */
 @Entity
 @Table(name = TableConstants.TABLE_CLIENT)
 public class Client 
 {
 	
 	private Long id;
 	private String name;
 	private String country;
 	private String city;
 	private String address;
 	private String billingAddress;
 	private String telephone;
 	private String email;
 	private String acquisitionType;
 	private String clientType;
 	private String industryType;
 	private String description;
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@Basic
 	@Column(nullable = false)
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	@Basic
 	@Column(nullable = false)
 	public String getCountry() {
 		return country;
 	}
 	public void setCountry(String country) {
 		this.country = country;
 	}
 	
 	@Basic
 	@Column(nullable = true)
 	public String getCity() {
 		return city;
 	}
 	public void setCity(String city) {
 		this.city = city;
 	}
 	
 	@Basic
 	@Column(nullable = true)
 	public String getAddress() {
 		return address;
 	}
 	public void setAddress(String address) {
 		this.address = address;
 	}
 	
 	@Basic
 	@Column(nullable = true,name = "billing_address" )
 	public String getBillingAddress() {
 		return billingAddress;
 	}
 	public void setBillingAddress(String billingAddress) {
 		this.billingAddress = billingAddress;
 	}
 	
 	@Basic
 	@Column(nullable = true)
 	public String getTelephone() {
 		return telephone;
 	}
 	public void setTelephone(String telephone) {
 		this.telephone = telephone;
 	}
 	
 	@Basic
 	@Column(nullable = true)
 	public String getEmail() {
 		return email;
 	}
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	
 	@Basic
 	@Column(nullable = true, name="acquisition_type")
 	public String getAcquisitionType() {
 		return acquisitionType;
 	}
 	public void setAcquisitionType(String acquisitionType) {
 		this.acquisitionType = acquisitionType;
 	}
 	
 	@Basic
 	@Column(nullable = true, name="client_type")
 	public String getClientType() {
 		return clientType;
 	}
 	public void setClientType(String clientType) {
 		this.clientType = clientType;
 	}
 	
 	@Basic
 	@Column(nullable = true, name="industry_type")
 	public String getIndustryType() {
 		return industryType;
 	}
 	public void setIndustryType(String industryType) {
 		this.industryType = industryType;
 	}
 	
 	@Basic
 	@Column(nullable = true)
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((acquisitionType == null) ? 0 : acquisitionType.hashCode());
 		result = prime * result + ((address == null) ? 0 : address.hashCode());
 		result = prime * result
 				+ ((billingAddress == null) ? 0 : billingAddress.hashCode());
 		result = prime * result + ((city == null) ? 0 : city.hashCode());
 		result = prime * result
 				+ ((clientType == null) ? 0 : clientType.hashCode());
 		result = prime * result + ((country == null) ? 0 : country.hashCode());
 		result = prime * result
 				+ ((description == null) ? 0 : description.hashCode());
 		result = prime * result + ((email == null) ? 0 : email.hashCode());
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result
 				+ ((industryType == null) ? 0 : industryType.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result
 				+ ((telephone == null) ? 0 : telephone.hashCode());
 		return result;
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Client other = (Client) obj;
 		if (acquisitionType == null) {
 			if (other.acquisitionType != null)
 				return false;
 		} else if (!acquisitionType.equals(other.acquisitionType))
 			return false;
 		if (address == null) {
 			if (other.address != null)
 				return false;
 		} else if (!address.equals(other.address))
 			return false;
 		if (billingAddress == null) {
 			if (other.billingAddress != null)
 				return false;
 		} else if (!billingAddress.equals(other.billingAddress))
 			return false;
 		if (city == null) {
 			if (other.city != null)
 				return false;
 		} else if (!city.equals(other.city))
 			return false;
 		if (clientType == null) {
 			if (other.clientType != null)
 				return false;
 		} else if (!clientType.equals(other.clientType))
 			return false;
 		if (country == null) {
 			if (other.country != null)
 				return false;
 		} else if (!country.equals(other.country))
 			return false;
 		if (description == null) {
 			if (other.description != null)
 				return false;
 		} else if (!description.equals(other.description))
 			return false;
 		if (email == null) {
 			if (other.email != null)
 				return false;
 		} else if (!email.equals(other.email))
 			return false;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (industryType == null) {
 			if (other.industryType != null)
 				return false;
 		} else if (!industryType.equals(other.industryType))
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (telephone == null) {
 			if (other.telephone != null)
 				return false;
 		} else if (!telephone.equals(other.telephone))
 			return false;
 		return true;
 	}
 	@Override
 	public String toString() {
 		return "Client [id=" + id + ", name=" + name + ", country=" + country
 				+ ", city=" + city + ", address=" + address
 				+ ", billingAddress=" + billingAddress + ", telephone="
 				+ telephone + ", email=" + email + ", acquisitionType="
 				+ acquisitionType + ", clientType=" + clientType
 				+ ", industryType=" + industryType + ", description="
 				+ description + "]";
 	}
 	
 	
 	
 	
 }
