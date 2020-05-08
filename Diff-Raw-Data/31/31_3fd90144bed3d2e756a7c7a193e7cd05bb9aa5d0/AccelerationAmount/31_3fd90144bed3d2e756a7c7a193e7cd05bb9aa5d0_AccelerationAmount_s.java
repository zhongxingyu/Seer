 /**
 * Copyright (c) 2005, 2011, Werner Keil, Jean-Marie Dautelle and others.
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
 import org.unitsofmeasurement.quantity.Acceleration;
 import org.unitsofmeasurement.unit.Unit;
 
 
 /**
  * Represents the rate of change of velocity with respect to time.
  * The metric system unit for this quantity is "m/s²" (metre per square second).
  *
  * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
  * @author  <a href="mailto:uomo@catmedia.us">Werner Keil</a>
 * @version 1.4, ($Revision: 212 $), $Date: 2011-03-06 $
  */
public class AccelerationAmount extends BaseAmount<Acceleration> implements Acceleration {
 	
 	public AccelerationAmount(Number number, Unit<Acceleration> unit) {
 		super(number, unit);
 	}
 }
