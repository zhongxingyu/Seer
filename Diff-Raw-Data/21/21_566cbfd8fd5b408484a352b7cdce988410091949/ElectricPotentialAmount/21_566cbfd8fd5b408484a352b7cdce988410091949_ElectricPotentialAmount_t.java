 /**
 * Copyright (c) 2005, 2013, Werner Keil and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil, Ikayzo and others - initial API and implementation
  */
 package org.eclipse.uomo.units.impl.quantity;
 
 import org.eclipse.uomo.units.impl.BaseAmount;
 import org.unitsofmeasurement.quantity.ElectricPotential;
 import org.unitsofmeasurement.unit.Unit;
 
 /**
  * Represents an amount of electric potential or electromotive force.
  * The metric system unit for this quantity is "V" (Volt).
  * 
  * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
  * @author  <a href="mailto:uomo@catmedia.us">Werner Keil</a>
 * @version 1.2, $Date: 2013-05-13 $
  */
public final class ElectricPotentialAmount extends BaseAmount<ElectricPotential> {
 
 	public ElectricPotentialAmount(Number number, Unit<ElectricPotential> unit) {
 		super(number, unit);
 	}
 }
