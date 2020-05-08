 /*
  * Licensed to Lolay, Inc. under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  Lolay, Inc. licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://github.com/lolay/citygrid/raw/master/LICENSE
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package com.lolay.citygrid.profile;
 
 import java.io.Serializable;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 @XmlRootElement(name="address")
 @XmlAccessorType(value=XmlAccessType.FIELD)
 public class ProfileAddress implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@XmlElement(name="street")
 	private String street = null;
 	@XmlElement(name="city")
 	private String city = null;
 	@XmlElement(name="state")
 	private String state = null;
	@XmlElement(name="postal_code")
 	private String postalCode = null;
 	@XmlElement(name="street")
 	private String deliveryPoint = null;
 	@XmlElement(name="cross_street")
 	private String crossStreet = null;
 	@XmlElement(name="latitude")
 	private Double latitude = null;
 	@XmlElement(name="longitude")
 	private Double longitude = null;
 	
 	public String getStreet() {
 		return street;
 	}
 	public void setStreet(String street) {
 		this.street = street;
 	}
 	public String getCity() {
 		return city;
 	}
 	public void setCity(String city) {
 		this.city = city;
 	}
 	public String getState() {
 		return state;
 	}
 	public void setState(String state) {
 		this.state = state;
 	}
 	public String getPostalCode() {
 		return postalCode;
 	}
 	public void setPostalCode(String postalCode) {
 		this.postalCode = postalCode;
 	}
 	public String getDeliveryPoint() {
 		return deliveryPoint;
 	}
 	public void setDeliveryPoint(String deliveryPoint) {
 		this.deliveryPoint = deliveryPoint;
 	}
 	public String getCrossStreet() {
 		return crossStreet;
 	}
 	public void setCrossStreet(String crossStreet) {
 		this.crossStreet = crossStreet;
 	}
 	public Double getLatitude() {
 		return latitude;
 	}
 	public void setLatitude(Double latitude) {
 		this.latitude = latitude;
 	}
 	public Double getLongitude() {
 		return longitude;
 	}
 	public void setLongitude(Double longitude) {
 		this.longitude = longitude;
 	}
 
 	@Override
 	public int hashCode() {
 		return HashCodeBuilder.reflectionHashCode(this);
 	}
 	@Override
 	public boolean equals(Object obj) {
 	   return EqualsBuilder.reflectionEquals(this, obj);
 	}
 	@Override
 	public String toString() {
 	   return ToStringBuilder.reflectionToString(this);
 	}
 }
