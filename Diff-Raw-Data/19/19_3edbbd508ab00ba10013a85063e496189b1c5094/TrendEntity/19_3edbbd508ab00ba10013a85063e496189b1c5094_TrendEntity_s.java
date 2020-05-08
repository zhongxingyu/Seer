 package org.motechproject.carereporting.domain;
 
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import java.math.BigDecimal;
 
 @Entity
 @Table(name = "trend")
 @AttributeOverrides({
         @AttributeOverride(name = "id", column = @Column(name = "trend_id"))
 })
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class TrendEntity extends AbstractEntity {
 
     @Column(name = "positive_diff")
     private BigDecimal positiveDiff;
 
     @Column(name = "negative_diff")
     private BigDecimal negativeDiff;
 
     @OneToOne(mappedBy = "trend")
     private IndicatorEntity indicator;
 
     public TrendEntity(BigDecimal positiveDiff, BigDecimal negativeDiff) {
         this.positiveDiff = positiveDiff;
         this.negativeDiff = negativeDiff;
     }
 
     public TrendEntity() {
     }
 
     public BigDecimal getPositiveDiff() {
         return positiveDiff;
     }
 
     public void setPositiveDiff(BigDecimal positiveDiff) {
         this.positiveDiff = positiveDiff;
     }
 
     public BigDecimal getNegativeDiff() {
         return negativeDiff;
     }
 
     public void setNegativeDiff(BigDecimal negativeDiff) {
         this.negativeDiff = negativeDiff;
     }
 
     public IndicatorEntity getIndicator() {
         return indicator;
     }
 
     public void setIndicator(IndicatorEntity indicator) {
         this.indicator = indicator;
     }
 }
