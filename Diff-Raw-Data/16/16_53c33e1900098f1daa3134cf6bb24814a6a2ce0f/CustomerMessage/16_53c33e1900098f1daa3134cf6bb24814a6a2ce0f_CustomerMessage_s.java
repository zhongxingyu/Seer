 package com.lolay.citygrid;
 
 import java.io.Serializable;
 import java.net.URI;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 @XmlRootElement(name="customer_message")
 @XmlAccessorType(value=XmlAccessType.FIELD)
 public class CustomerMessage implements Serializable {
 	private static final long serialVersionUID = 1L;
 	
	@XmlElement(name="attribution_text")
 	private String attributionText = null;
	@XmlElement(name="attribution_logo")
 	private URI attributionLogo = null;
	@XmlElement(name="attribution_source")
 	private Integer attributionSource = null;
//	@XmlValue FIXME
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
 	public Integer getAttributionSource() {
 		return attributionSource;
 	}
 	public void setAttributionSource(Integer attributionSource) {
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
