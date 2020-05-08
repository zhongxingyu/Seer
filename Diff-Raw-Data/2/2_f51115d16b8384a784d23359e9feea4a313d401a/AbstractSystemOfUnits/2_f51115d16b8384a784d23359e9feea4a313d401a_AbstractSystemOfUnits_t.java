 /**
  * Copyright (c) 2005, 2011, Werner Keil and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil - initial API and implementation
  */
 package org.eclipse.uomo.units;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.uomo.core.IName;
 import org.unitsofmeasurement.quantity.Quantity;
 import org.unitsofmeasurement.unit.Dimension;
 import org.unitsofmeasurement.unit.SystemOfUnits;
 import org.unitsofmeasurement.unit.Unit;
 
 /**
  * <p> An abstract base class for unit systems.</p>
  *
  * @author <a href="mailto:uomo@catmedia.us">Werner Keil</a>
  * @version 1.2, $Date: 2011-09-11 14:52:11 +0200 (So, 11 Sep 2011) $
  */
 public abstract class AbstractSystemOfUnits implements SystemOfUnits, IName {
 	/**
 	 * Holds collection of units.
 	 */
 	protected static final Set<Unit<?>> UNITS = new HashSet<Unit<?>>();
 
 	// ///////////////////
 	// Collection View //
 	// ///////////////////
 	/**
 	 * Returns a read only view over the units defined in this class.
 	 * 
 	 * @return the collection of units.
 	 */
 	public Set<Unit<?>> getUnits() {
 		return Collections.unmodifiableSet(UNITS);
 	}
 	
 	@Override
 	public Set<Unit<?>> getUnits(Dimension dimension) {
 		return Helper.getUnitsOfDimension(UNITS, dimension);
 	}
 
 	@Override
	public <T extends Quantity<T>> Unit<T> getUnit(Class<T> quantityType) {
 		return QuantityFactory.getInstance(quantityType).getMetricUnit();
 	}
 	
 	/**
 	 * Adds a new named unit to the collection.
 	 * 
 	 * @param unit the unit being added.
 	 * @param name the name of the unit.
 	 * @return <code>unit</code>.
 	 */
 	@SuppressWarnings("unchecked")
 	protected static <U extends Unit<?>> U addUnit(U unit, String name) {
 		if (name != null && unit instanceof AbstractUnit) {
 			AbstractUnit<?> aUnit = (AbstractUnit<?>)unit;
 			aUnit.setName(name);
 			UNITS.add(aUnit);
 			return (U) aUnit;
 		}
 		UNITS.add(unit);
 		return unit;
 	}
 
 	/**
 	 * Adds a new unit to the collection.
 	 * 
 	 * @param unit
 	 *            the unit being added.
 	 * @return <code>unit</code>.
 	 */
 	protected static <U extends Unit<?>> U addUnit(U unit) {
 		UNITS.add(unit);
 		return unit;
 	}
 	static class Helper {
 		static Set<Unit<?>> getUnitsOfDimension(final Set<Unit<?>> units, 
 				Dimension dimension) {
 			if (dimension != null) {
 				Set<Unit<?>>dimSet = new HashSet<Unit<?>>();
 				for (Unit<?> u : units) {
 					if (dimension.equals(u.getDimension())) {
 						dimSet.add(u);
 					}
 				}
 				return dimSet;
 			}
 			return null;
 		}
 	}
 }
