 package org.strangeforest.currencywatch.rest;
 
 import java.util.*;
 import javax.xml.bind.annotation.*;
 
 @XmlRootElement(name = "rates")
 @XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rates", propOrder = {"rates"})
 public class RatesType {
 
 	@XmlElement(name = "rate", required=true) private List<RateType> rates;
 
 	public RatesType() {}
 
 	public RatesType(List<RateType> rates) {
 		this.rates = rates;
 	}
 
 	public List<RateType> getRates() {
 		return rates;
 	}
 }
