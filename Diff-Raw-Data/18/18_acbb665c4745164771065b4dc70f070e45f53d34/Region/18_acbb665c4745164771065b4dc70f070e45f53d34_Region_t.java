 package fr.peralta.mycellar.domain.wine;
 
 import java.io.Serializable;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
 import javax.validation.Valid;
 import javax.validation.constraints.Pattern;
 
 import fr.peralta.mycellar.domain.shared.NamedEntity;
 import fr.peralta.mycellar.domain.shared.ValidationPattern;
 
 @Entity
@Table(name = "REGION", uniqueConstraints = @UniqueConstraint(columnNames = {
        "NAME", "COUNTRY" }))
@AttributeOverride(name = "name", column = @Column(name = "NAME", nullable = false))
 @SequenceGenerator(name = "REGION_ID_GENERATOR", allocationSize = 1)
 public class Region extends NamedEntity implements Serializable {
 
     private static final long serialVersionUID = 201010311741L;
 
     @Pattern(regexp = ValidationPattern.URL_PATTERN)
     @Column(name = "MAP_URL")
     private String mapUrl;
 
     @Column(name = "DESCRIPTION")
     private String description;
 
     @Valid
     @ManyToOne
     @JoinColumn(name = "COUNTRY", nullable = false)
     private Country country;
 
     @Id
    @GeneratedValue(generator = "REGION_ID_GENERATOR")
     @Column(name = "ID", nullable = false, unique = true)
     private int id;
 
     /**
      * @param name
      * @param country
      */
     public Region(String name, Country country) {
         super(name);
         this.country = country;
     }
 
     /**
      * Needed by Hibernate.
      */
     Region() {
     }
 
     /**
      * @return the id
      */
     @Override
     public int getId() {
         return id;
     }
 
     /**
      * @return the mapUrl
      */
     public String getMapUrl() {
         return mapUrl;
     }
 
     /**
      * @param mapUrl
      *            the mapUrl to set
      */
     public void setMapUrl(String mapUrl) {
         this.mapUrl = mapUrl;
     }
 
     /**
      * @return the description
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * @param description
      *            the description to set
      */
     public void setDescription(String description) {
         this.description = description;
     }
 
     /**
      * @return the country
      */
     public Country getCountry() {
         return country;
     }
 
     /**
      * @param country
      *            the country to set
      */
     public void setCountry(Country country) {
         this.country = country;
     }
 
 }
