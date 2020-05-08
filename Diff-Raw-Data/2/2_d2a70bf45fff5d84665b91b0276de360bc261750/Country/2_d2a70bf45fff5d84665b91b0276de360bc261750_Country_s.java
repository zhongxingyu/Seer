 package com.aciertoteam.geo.entity;
 
 import com.aciertoteam.common.entity.AbstractEntity;
 import com.aciertoteam.common.utils.ContractEqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.codehaus.jackson.annotate.JsonAutoDetect;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.UniqueConstraint;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Bogdan Nechyporenko
  */
 @Entity
 @JsonAutoDetect
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class Country extends AbstractEntity {
 
     private static final long serialVersionUID = 1L;
 
     @Column(unique = true, nullable = false)
     private String name;
 
     @ManyToMany(fetch = FetchType.EAGER)
     @JoinTable(name = "COUNTRY_LANGUAGES", joinColumns = @JoinColumn(name = "COUNTRY_ID", referencedColumnName = "ID"), inverseJoinColumns = @JoinColumn(name = "LANGUAGE_ID", referencedColumnName = "ID"), uniqueConstraints = @UniqueConstraint(name = "UNIQUE_COUNTRY_LANG", columnNames = {
             "COUNTRY_ID", "LANGUAGE_ID" }))
     @Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE })
     private Set<Language> languages = new HashSet<Language>();
 
     // TODO fix this, doesn't belong here
     private transient String ipAddress;
 
     Country() {
         //hibernate
     }
 
     public Country(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getIpAddress() {
         return ipAddress;
     }
 
     public void setIpAddress(String ipAddress) {
         this.ipAddress = ipAddress;
     }
 
     public Set<Language> getLanguages() {
         return languages;
     }
 
    void setLanguages(Set<Language> languages) {
         this.languages = languages;
     }
 
     public void addLanguages(Set<Language> languages) {
         this.languages.addAll(languages);
     }
 
     @Override
     public final boolean equals(Object obj) {
         return ContractEqualsBuilder.isEquals(this, obj, "name");
     }
 
     @Override
     public final int hashCode() {
         return new HashCodeBuilder().append(name).toHashCode();
     }
 
     @Override
     public String toString() {
         return "Country{" + "name='" + name + '\'' + '}';
     }
 
 }
