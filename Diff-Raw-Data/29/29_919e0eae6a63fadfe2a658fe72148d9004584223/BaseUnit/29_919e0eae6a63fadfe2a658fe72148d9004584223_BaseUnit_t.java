 /**
  * Copyright (c) 2005, 2010, Werner Keil, Ikayzo and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil, Ikayzo and others - initial API and implementation
  */
 package org.eclipse.uomo.units.impl;
 
 import org.eclipse.uomo.units.AbstractConverter;
 import org.eclipse.uomo.units.AbstractUnit;
 import org.unitsofmeasurement.quantity.Quantity;
 import org.unitsofmeasurement.unit.Dimension;
 import org.unitsofmeasurement.unit.Unit;
 import org.unitsofmeasurement.unit.UnitConverter;
 
 /**
  * <p> This class represents the building blocks on top of which all others
  *     units are created. Base units are always unscaled metric units.</p>
  * 
  * <p> When using the {@linkplain Dimensional.Model#STANDARD standard} model
  *     (default), all seven base units are dimensionally independent.</p>
  *
  * @param <Q> The type of the quantity measured by this unit.
  *
  * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
  * @author  <a href="mailto:units@catmedia.us">Werner Keil</a>
  * @version 1.9 ($Revision: 212 $), $Date: 2010-09-13 23:50:44 +0200 (Mo, 13 Sep 2010) $
  * @see <a href="http://en.wikipedia.org/wiki/SI_base_unit">
  *       Wikipedia: SI base unit</a>
  */
 public class BaseUnit<Q extends Quantity<Q>> extends AbstractUnit<Q> {
     
     /**
      * Holds the symbol.
      */
     private final String symbol;
 
     /** The serialVersionUID */
     private static final long serialVersionUID = 1234567654321265167L;
 
     /**
      * Creates a base unit having the specified symbol.
      *
      * @param symbol the symbol of this base unit.
      * @throws IllegalArgumentException if the specified symbol is
      *         associated to a different unit.
      */
     public BaseUnit(String symbol, String name) {
         super(name);
     	this.symbol = symbol;
         // Checks if the symbol is associated to a different unit.
         synchronized (AbstractUnit.SYMBOL_TO_UNIT) {
             Unit<?> unit = AbstractUnit.SYMBOL_TO_UNIT.get(symbol);
             if (unit == null) {
                 AbstractUnit.SYMBOL_TO_UNIT.put(symbol, this);
                 return;
             }
             if (!(unit instanceof BaseUnit<?>))
                 throw new IllegalArgumentException("Symbol " + symbol + " is associated to a different unit");
         }
     }
     
     /**
      * Creates a base unit having the specified symbol.
      *
      * @param symbol the symbol of this base unit.
      * @throws IllegalArgumentException if the specified symbol is
      *         associated to a different unit.
      */
     public BaseUnit(String symbol) {
     	this(symbol, null);
     }
 	
     @Override
     public String getSymbol() {
         return symbol;
     }
     
 	@Override
 	public Unit<Q> getSystemUnit() {
 		return toMetric();
 	}
 	
     @Override
     public boolean equals(Object that) {
         if (this == that)
             return true;
         if (!(that instanceof BaseUnit<?>)) {
         	if (!(that instanceof Unit<?>)) {
         		return false;
         	} else {
         		if (that instanceof TransformedUnit<?>) {
         			if (((TransformedUnit<?>) that).getParentUnit() instanceof BaseUnit<?>) {        				
         				if(this.getSymbol().equals(((TransformedUnit<?>)that).getParentUnit().getSymbol())) {
         					for (UnitConverter comp : ((TransformedUnit<?>) that).toParentUnit().getCompoundConverters()) {
        						// FIXME evaluate factor 1 for TU (Bug 338334)
 //								System.out.println(comp.toString());
 							}
         				}
         			}
         		}
         		return false;
         	}
         }  else {        	
 	        BaseUnit<?> thatUnit = (BaseUnit<?>) that;
 	        return this.symbol.equals(thatUnit.symbol);
         }
     }
 
     @Override
     public int hashCode() {
         return symbol.hashCode();
     }
 
     @Override
     protected Unit<Q> toMetric() {
         return this;
     }
 
     @Override
     public UnitConverter getConverterToMetric() {
         return AbstractConverter.IDENTITY;
     }
 
     @Override
     public Dimension getDimension() {
         return DimensionImpl.getModel().getDimension(this);
     }
 
     @Override
     public UnitConverter getDimensionalTransform() {
         return DimensionImpl.getModel().getTransform(this);
     }
 }
