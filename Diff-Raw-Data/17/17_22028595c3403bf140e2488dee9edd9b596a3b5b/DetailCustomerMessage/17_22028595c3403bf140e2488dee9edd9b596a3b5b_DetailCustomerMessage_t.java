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
 package com.lolay.citygrid.content.places.detail;
 
 import java.io.Serializable;
 import java.net.URI;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlValue;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 import com.lolay.citygrid.UriAdapter;
 
 @XmlRootElement(name="customer_message")
 @XmlAccessorType(value=XmlAccessType.FIELD)
 public class DetailCustomerMessage implements Serializable {
 	private static final long serialVersionUID = 1L;
 	
 	@XmlAttribute(name="attribution_text")
 	private String attributionText = null;
 	@XmlAttribute(name="attribution_logo")
 	@XmlJavaTypeAdapter(value=UriAdapter.class)
 	private URI attributionLogo = null;
 	@XmlAttribute(name="attribution_source")
	private String attributionSource = null;
 	@XmlValue
 	private String customerMessage = null;
 	
 	public String getAttributionText() {
 		return attributionText;
 	}
 	public void setAttributionText(String attributionText) {
 		this.attributionText = attributionText;
 	}
 	public URI getAttributionLogo() {
 		return attributionLogo;
 	}
 	public void setAttributionLogo(URI attributionLogo) {
 		this.attributionLogo = attributionLogo;
 	}
	public String getAttributionSource() {
 		return attributionSource;
 	}
	public void setAttributionSource(String attributionSource) {
 		this.attributionSource = attributionSource;
 	}
 	public String getCustomerMessage() {
 		return customerMessage;
 	}
 	public void setCustomerMessage(String customerMessage) {
 		this.customerMessage = customerMessage;
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
