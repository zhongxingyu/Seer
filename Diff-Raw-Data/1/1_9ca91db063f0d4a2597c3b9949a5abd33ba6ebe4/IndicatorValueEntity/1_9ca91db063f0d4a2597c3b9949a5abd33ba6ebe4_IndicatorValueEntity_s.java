 package org.motechproject.carereporting.domain;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonView;
 import org.motechproject.carereporting.domain.dto.IndicatorValueDto;
 import org.motechproject.carereporting.domain.views.DashboardJsonView;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 @Entity
 @Table(name = "indicator_value")
 @AttributeOverrides({
         @AttributeOverride(name = "id", column = @Column(name = "indicator_value_id"))
 })
 public class IndicatorValueEntity extends AbstractEntity {
 
     @NotNull
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "indicator_id")
     private IndicatorEntity indicator;
 
     @NotNull
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "area_id")
     private AreaEntity area;
 
     @NotNull
     @Column(name = "numerator")
     private BigDecimal numerator;
 
     @NotNull
     @Column(name = "denominator")
     private BigDecimal denominator;
 
     @NotNull
     @Column(name = "value")
     @JsonView({ DashboardJsonView.class })
     private BigDecimal value;
 
     @NotNull
     @ManyToOne
     @JoinColumn(name = "frequency_id")
     private FrequencyEntity frequency;
 
     @NotNull
     @Column(name = "date")
     @JsonView({ DashboardJsonView.class })
     private Date date;
 
     @Column(name = "category")
     private String category;
 
     public IndicatorValueEntity() {
 
     }
 
     public IndicatorValueEntity(IndicatorEntity indicator, AreaEntity area, BigDecimal numerator, BigDecimal denominator, BigDecimal value, FrequencyEntity frequency, Date date) {
         this.indicator = indicator;
         this.area = area;
         this.numerator = numerator;
         this.denominator = denominator;
         this.value = value;
         this.frequency = frequency;
         this.date = date;
     }
 
     public BigDecimal getNumerator() {
         return numerator;
     }
 
     public void setNumerator(BigDecimal numerator) {
         this.numerator = numerator;
     }
 
     public BigDecimal getDenominator() {
         return denominator;
     }
 
     public void setDenominator(BigDecimal denominator) {
         this.denominator = denominator;
     }
 
     public FrequencyEntity getFrequency() {
         return frequency;
     }
 
     public void setFrequency(FrequencyEntity frequency) {
         this.frequency = frequency;
     }
 
     @JsonIgnore
     public IndicatorEntity getIndicator() {
         return indicator;
     }
 
     public Integer getIndicatorId() {
         return indicator.getId();
     }
 
     public void setIndicator(IndicatorEntity indicator) {
         this.indicator = indicator;
     }
 
     @JsonIgnore
     public AreaEntity getArea() {
         return area;
     }
 
     public Integer getAreaId() {
         return area.getId();
     }
 
     public void setArea(AreaEntity area) {
         this.area = area;
     }
 
     public BigDecimal getValue() {
         return value;
     }
 
     public void setValue(BigDecimal value) {
         this.value = value;
     }
 
     public Date getDate() {
         return date;
     }
 
     public String getDateString() {
         return new SimpleDateFormat("yyyy-MM-dd").format(getDate());
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public String getCategory() {
         return category;
     }
 
     public void setCategory(String category) {
         this.category = category;
     }
 
     public IndicatorValueDto toDto() {
         return new IndicatorValueDto(date, numerator, denominator, value);
     }
 }
