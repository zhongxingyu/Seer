 package org.strangeforest.currencywatch.rest;
 
 import java.math.*;
 import java.util.*;
 import javax.xml.bind.annotation.*;
 
 import org.strangeforest.currencywatch.core.*;
 
 @XmlRootElement(name = "rate")
 @XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rate", propOrder = {"date", "bid", "ask", "middle"})
 public class RateType {
 
 	@XmlAttribute(required=true) private Date date;
 	@XmlAttribute(required=true) private BigDecimal bid;
 	@XmlAttribute(required=true) private BigDecimal ask;
 	@XmlAttribute(required=true) private BigDecimal middle;
 
 	public RateType() {}
 
 	public RateType(Date date, RateValue rate) {
 		this.date = date;
 		this.bid = rate.getBid();
 		this.ask = rate.getAsk();
 		this.middle = rate.getMiddle();
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public BigDecimal getBid() {
 		return bid;
 	}
 
 	public BigDecimal getAsk() {
 		return ask;
 	}
 
 	public BigDecimal getMiddle() {
 		return middle;
 	}
 }
