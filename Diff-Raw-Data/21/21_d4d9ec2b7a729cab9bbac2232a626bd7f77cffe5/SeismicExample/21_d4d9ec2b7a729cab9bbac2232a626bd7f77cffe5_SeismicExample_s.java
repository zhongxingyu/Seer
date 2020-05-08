 /**
  * Copyright (c) 2005, 2011, Werner Keil, Ikayzo and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil - initial API and implementation
  */
 package org.eclipse.uomo.examples.units.console.sandbox;
 
 import org.eclipse.uomo.examples.units.types.Seismic;
 import org.eclipse.uomo.units.impl.quantity.EnergyAmount;
import org.unitsofmeasurement.quantity.Energy;
 import com.ibm.icu.util.Measure;
 
 /**
  * @author Werner Keil
  *
  */
 public class SeismicExample {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Measure e = new EnergyAmount(8.3, Seismic.RICHTER_MAGNITUDE);
 		System.out.println(e);
 	}
 
 }
