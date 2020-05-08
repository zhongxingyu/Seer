 /*
  * Copyright 2011, MyCellar
  *
  * This file is part of MyCellar.
  *
  * MyCellar is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyCellar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MyCellar. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.peralta.mycellar.domain.wine;
 
 import java.io.Serializable;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.SequenceGenerator;
 import javax.validation.constraints.Pattern;
 
 import fr.peralta.mycellar.domain.shared.NamedEntity;
 import fr.peralta.mycellar.domain.shared.ValidationPattern;
 
 /**
  * @author speralta
  */
 @Entity
 @AttributeOverride(name = "name", column = @Column(name = "NAME", nullable = false, unique = true))
 @SequenceGenerator(name = "COUNTRY_ID_GENERATOR", allocationSize = 1)
 public class Country extends NamedEntity implements Serializable {
 
     private static final long serialVersionUID = 201010311742L;
 
     @Pattern(regexp = ValidationPattern.URL_PATTERN)
     @Column(name = "MAP_URL")
     private String mapUrl;
 
     @Column(name = "DESCRIPTION")
     private String description;
 
     @Id
     @GeneratedValue(generator = "COUNTRY_ID_GENERATOR")
     @Column(name = "ID", nullable = false, unique = true)
     private int id;
 
     /**
      * @param name
      */
     public Country(String name) {
         super(name);
     }
 
     /**
      * Needed by hibernate.
      */
     Country() {
         super();
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
 
 }
