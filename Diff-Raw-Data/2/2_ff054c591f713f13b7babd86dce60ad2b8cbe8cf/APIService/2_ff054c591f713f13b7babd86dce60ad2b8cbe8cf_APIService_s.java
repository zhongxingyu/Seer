 package com.testdroid.api.um.model;
 
 import com.testdroid.api.formatter.CurrencyFormatter;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author kajdus
  */
 @XmlRootElement(name = "umApiService", namespace = "cloud.testdroid.api.um.model")
 @XmlType(name="umApiService", namespace = "cloud.testdroid.api.um.model")
 public class APIService {
     
     @XmlType(name="umApiServiceUnit", namespace = "cloud.testdroid.um.api.model")
     public static enum Unit {
         SECOND, MINUTE, HOUR, DAY, MONTH, YEAR, RUN, PROJECT;
     }
     
     @XmlType(name="umApiServiceType", namespace = "cloud.testdroid.um.api.model")
     public static enum Type {
         RECORDER, CLOUD;
     }
     
     private Long id;
     private String name;
     private String description;
     private boolean autoRenew;
     private Type type;
     private Integer centPrice;
     private Integer unitCount;
     private Unit unit;
     private boolean quantityAppliable;
     private String braintreeId;
 
     public APIService() {}
     
     public APIService(Long id, String name, String description, boolean autoRenew, Type type, Integer centPrice, Integer unitCount, Unit unit, 
             boolean quantityAppliable, String braintreeId) {
         this.id = id;
         this.name = name;
         this.description = description;
         this.autoRenew = autoRenew;
         this.type = type;
         this.centPrice = centPrice;
         this.unitCount = unitCount;
         this.unit = unit;
         this.quantityAppliable = quantityAppliable;
         this.braintreeId = braintreeId;
     }
 
     public boolean isAutoRenew() {
         return autoRenew;
     }
 
     public Integer getCentPrice() {
         return centPrice;
     }
 
     public Long getId() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public Type getType() {
         return type;
     }
 
     public Unit getUnit() {
         return unit;
     }
     @JsonIgnore
     public String getPaypalUnit() {
         switch(unit) {
             case YEAR: return "Y";
             case DAY: return "D";
             case MONTH: return "M";
             default: return "";
         }
     }
 
     public Integer getUnitCount() {
         return unitCount;
     }
 
     public boolean isQuantityAppliable() {
         return quantityAppliable;
     }
 
     public String getBraintreeId() {
         return braintreeId;
     }
 
     public void setBraintreeId(String braintreeId) {
         this.braintreeId = braintreeId;
     }
 
     public void setAutoRenew(boolean autoRenew) {
         this.autoRenew = autoRenew;
     }
 
     public void setCentPrice(Integer centPrice) {
         this.centPrice = centPrice;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
 
     public void setUnit(Unit unit) {
         this.unit = unit;
     }
 
     public void setUnitCount(Integer unitCount) {
         this.unitCount = unitCount;
     }
 
     public void setQuantityAppliable(boolean quantityAppliable) {
         this.quantityAppliable = quantityAppliable;
     }
    @JsonIgnore
     public String getPriceString() {
         return CurrencyFormatter.format(centPrice);
     }
     @JsonIgnore
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
     @JsonIgnore
     @Override
     public boolean equals(Object object) {
 	if(this == object) return true;
         if (!(object instanceof APIService)) {
             return false;
         }
         APIService other = (APIService) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
     
 }
