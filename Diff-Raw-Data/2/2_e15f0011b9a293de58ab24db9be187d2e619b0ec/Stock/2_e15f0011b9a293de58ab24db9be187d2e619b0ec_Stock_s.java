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
 package fr.peralta.mycellar.domain.stock;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import org.apache.commons.lang3.ObjectUtils;
 
 import fr.peralta.mycellar.domain.shared.IdentifiedEntity;
 
 /**
  * @author bperalta
  */
 @Entity
@Table(name = "STOCK", uniqueConstraints = @UniqueConstraint(columnNames = { "BOTTLE" }))
 @SequenceGenerator(name = "STOCK_ID_GENERATOR", allocationSize = 1)
 public class Stock extends IdentifiedEntity<Stock> {
 
     private static final long serialVersionUID = 201011111811L;
 
     @ManyToOne
     @JoinColumn(name = "BOTTLE", nullable = false)
     private Bottle bottle;
 
     @ManyToOne
     @JoinColumn(name = "CELLAR", nullable = false)
     private Cellar cellar;
 
     @Id
     @GeneratedValue(generator = "STOCK_ID_GENERATOR")
     @Column(name = "ID", nullable = false, unique = true)
     private Integer id;
 
     @Column(name = "QUANTITY")
     private Integer quantity;
 
     /**
      * @return the bottle
      */
     public Bottle getBottle() {
         return bottle;
     }
 
     /**
      * @return the cellar
      */
     public Cellar getCellar() {
         return cellar;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Integer getId() {
         return id;
     }
 
     /**
      * @return the quantity
      */
     public Integer getQuantity() {
         return quantity;
     }
 
     /**
      * @param bottle
      *            the bottle to set
      */
     public void setBottle(Bottle bottle) {
         this.bottle = bottle;
     }
 
     /**
      * @param cellar
      *            the cellar to set
      */
     public void setCellar(Cellar cellar) {
         this.cellar = cellar;
     }
 
     /**
      * @param quantity
      *            the quantity to set
      */
     public void setQuantity(Integer quantity) {
         this.quantity = quantity;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected boolean dataEquals(Stock other) {
         return ObjectUtils.equals(getBottle(), other.getBottle());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected Object[] getHashCodeData() {
         return new Object[] { getBottle() };
     }
 
 }
