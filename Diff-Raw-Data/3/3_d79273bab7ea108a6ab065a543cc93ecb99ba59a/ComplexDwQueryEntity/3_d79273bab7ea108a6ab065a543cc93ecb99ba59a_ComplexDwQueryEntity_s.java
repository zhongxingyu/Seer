 package org.motechproject.carereporting.domain;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 import java.util.Set;
 
 @Entity
 @Table(name = "complex_dw_query")
 @AttributeOverrides({
         @AttributeOverride(name = "id", column = @Column(name = "complex_dw_query_id"))
 })
 public class ComplexDwQueryEntity extends DwQueryEntity {
 
     @Column(name = "dimension")
     private String dimension;
 
     @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     @JoinTable(name = "fact_complex_dw_query", joinColumns = { @JoinColumn(name = "complex_dw_query_id") },
             inverseJoinColumns = { @JoinColumn(name = "fact_id") })
     private Set<FactEntity> facts;
 
     @Column(name = "dimension_key")
     private String dimensionKey;
 
     @Column(name = "fact_key")
     private String factKey;
 
     public String getDimension() {
         return dimension;
     }
 
     public void setDimension(String dimension) {
         this.dimension = dimension;
     }
 
     public Set<FactEntity> getFacts() {
         return facts;
     }
 
     public void setFacts(Set<FactEntity> facts) {
         this.facts = facts;
     }
 
     public String getDimensionKey() {
         return dimensionKey;
     }
 
     public void setDimensionKey(String dimensionKey) {
         this.dimensionKey = dimensionKey;
     }
 
     public String getFactKey() {
         return factKey;
     }
 
     public void setFactKey(String factKey) {
         this.factKey = factKey;
     }
 }
