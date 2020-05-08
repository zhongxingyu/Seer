 /**
 * Copyright (c) 2005, 2013, Werner Keil and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
 *    Werner Keil, Jean-Marie Dautelle - initial API and implementation
  */
 package org.eclipse.uomo.units.impl.quantity;
 
 import org.eclipse.uomo.units.impl.BaseAmount;
 import org.unitsofmeasurement.quantity.Energy;
 import org.unitsofmeasurement.unit.Unit;
 
 /**
  * Represents the capacity of a physical system to do work.
  * The metric system unit for this quantity "J" (Joule).
  *
  * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
  * @author  <a href="mailto:uomo@catmedia.us">Werner Keil</a>
 * @version 1.3, $Date: 2013-05-20 $
  */
public final class EnergyAmount extends BaseAmount<Energy> {
 
 	public EnergyAmount(Number number, Unit<Energy> unit) {
 		super(number, unit);
 	}
 
 }
