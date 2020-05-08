 package org.motechproject.carereporting.domain;
 
 import com.fasterxml.jackson.annotation.JsonView;
 import org.motechproject.carereporting.domain.views.BaseView;
 import org.motechproject.carereporting.domain.views.QueryJsonView;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "grouped_by")
 @AttributeOverrides({
         @AttributeOverride(name = "id", column = @Column(name = "grouped_by_id"))
 })
 public class GroupedByEntity extends AbstractEntity implements Cloneable {
 
    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
     @JoinColumn(name = "computed_field_id", referencedColumnName = "computed_field_id")
     @JsonView({ BaseView.class })
     private ComputedFieldEntity computedField;
 
     @ManyToOne(cascade = CascadeType.ALL)
     @JoinColumn(name = "having_id", referencedColumnName = "having_id")
     @JsonView({ QueryJsonView.EditForm.class })
     private HavingEntity having;
 
     public GroupedByEntity() {
 
     }
 
     public GroupedByEntity(GroupedByEntity groupedBy) {
         this.computedField = groupedBy.getComputedField();
         this.having = groupedBy.getHaving() != null ? new HavingEntity(groupedBy.getHaving()) : null;
     }
 
     public ComputedFieldEntity getComputedField() {
         return computedField;
     }
 
     public void setComputedField(ComputedFieldEntity computedField) {
         this.computedField = computedField;
     }
 
     public HavingEntity getHaving() {
         return having;
     }
 
     public void setHaving(HavingEntity having) {
         this.having = having;
     }
 
     @Override
     protected Object clone() throws CloneNotSupportedException {
         GroupedByEntity groupedByEntity = new GroupedByEntity();
 
         groupedByEntity.setComputedField(this.getComputedField());
         if (this.getHaving() != null) {
             groupedByEntity.setHaving((HavingEntity) this.getHaving().clone());
         }
 
         return groupedByEntity;
     }
 }
