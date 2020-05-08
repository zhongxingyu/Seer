 /**
  * Copyright (c) 2013, Werner Keil and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil - initial API and implementation
  */
 package org.eclipse.uomo.examples.units.types;
 
 import org.eclipse.uomo.units.AbstractSystemOfUnits;
 import org.eclipse.uomo.examples.units.Messages;
 import org.eclipse.uomo.units.SI;
 import org.eclipse.uomo.units.impl.BaseUnit;
 import org.unitsofmeasurement.unit.SystemOfUnits;
 import org.unitsofmeasurement.unit.Unit;
 
 /**
  * @author <a href="mailto:uomo@catmedia.us">Werner Keil</a>
  * @version 0.1
  */
@SuppressWarnings("deprecation")
 public class Health extends AbstractSystemOfUnits {
 
 	private static final Unit<Heartbeat> BEAT = addUnit(new BaseUnit<Heartbeat>(
 			Messages.BEAT));
 	
 	// BPM.
 	@SuppressWarnings("unchecked")
 	public static final  Unit<HeartRate> BPM = addUnit((Unit<HeartRate>) BEAT.divide(SI.SECOND.multiply(60)),
 			Messages.BEAT);
 	
 	@Override
 	public String getName() {
 		return Health.class.getSimpleName();
 	}
 
     /**
      * Default constructor (prevents this class from being instantiated).
      */
     private Health() {
     }
 	
 	/**
 	 * The singleton instance of {@code Seismic}.
 	 */
 	private static final Health INSTANCE = new Health();
 
 	/**
 	 * Returns the singleton instance of this class.
 	 * 
 	 * @return the Seismic system instance.
 	 */
 	public static final SystemOfUnits getInstance() {
 		return INSTANCE;
 	}	
 }
