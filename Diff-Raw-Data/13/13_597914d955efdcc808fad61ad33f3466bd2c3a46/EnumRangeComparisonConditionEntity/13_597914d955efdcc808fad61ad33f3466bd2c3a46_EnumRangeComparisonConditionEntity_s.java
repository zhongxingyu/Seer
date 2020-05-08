 package org.motechproject.carereporting.domain;
 
 import com.fasterxml.jackson.annotation.JsonTypeName;
 import com.fasterxml.jackson.annotation.JsonView;
 import org.motechproject.carereporting.domain.views.ComplexConditionJsonView;
 import org.motechproject.carereporting.domain.views.IndicatorJsonView;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 import java.util.Set;
 
 @Entity
 @Table(name = "enum_range_comparison")
 @AttributeOverrides({
         @AttributeOverride(name = "id", column = @Column(name = "enum_range_comparison_id"))
 })
 @JsonTypeName(value = "enumRange")
 public class EnumRangeComparisonConditionEntity extends ConditionEntity {
 
     @NotNull
    @OneToMany(mappedBy = "condition")
     @JsonView({ IndicatorJsonView.IndicatorDetails.class, ComplexConditionJsonView.ComplexConditionDetails.class })
     private Set<EnumRangeComparisonConditionValueEntity> values;
 
     public EnumRangeComparisonConditionEntity() {
         super();
     }
 
     public EnumRangeComparisonConditionEntity(EnumRangeComparisonConditionEntity conditionEntity) {
         super(conditionEntity);
 
         this.values = conditionEntity.getValues();
     }
 
     public Set<EnumRangeComparisonConditionValueEntity> getValues() {
         return values;
     }
 
     public void setValues(Set<EnumRangeComparisonConditionValueEntity> values) {
         this.values = values;
     }
 }
