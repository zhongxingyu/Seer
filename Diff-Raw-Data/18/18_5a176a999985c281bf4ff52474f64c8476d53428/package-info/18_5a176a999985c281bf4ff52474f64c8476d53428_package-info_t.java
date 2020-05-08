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
 /**
  * Provides quantitative properties or attributes of thing such as
  * mass, time, distance, heat, and angular separation.
  * Quantities of different kinds are represented by sub-types of the
  * {@link org.unitsofmeasurement.quantity.Quantity} interface, which can be
  * created by a new {@link org.eclipse.uomo.units.impl.BaseAmount} instance or its subclasses.
  *
  * <p> Only quantities defined in the <a href="http://en.wikipedia.org/wiki/International_System_of_Units">International System of Units</a>
  *     are provided here. Users can create their own quantity types by extending the {@link
  *     org.unitsofmeasurement.quantity.Quantity Quantity} interface.</p>
  *
  * <p> This package supports <cite>measurable</cite> quantities, which can be
  *     expressed as ({@link java.lang.Number}, {@link org.unitsofmeasurement.unit.Unit}) tuples.
  *     Those tuples are not expected to be used directly in numerically intensive code.
  *     They are more useful as metadata converted to the application internal representation
  *     (for example {@code double} primitive type with the requirement to provide values in
  *     metres) before the computation begin. For this purpose, the {@code Quantity} interface
  *     provides the {@code longValue(Unit<Q>)} and {@code doubleValue(Unit<Q>)} convenience
  *     methods. Example:[code]
  *        TimeAmount calculateTravelTime(Length distance, Velocity velocity) {
  *            double seconds = distance.doubleValue(METRE) /
  *                             velocity.doubleValue(METRE_PER_SECOND);
  *            return new TimeAmount(seconds, SECOND);
  *        }
  *     [/code]
  * </p>
  *
  * <p> Quantities sub-types are also used as parameterized type to characterize generic
  *     classes (and provide additional compile time check) as illustrated here.[code]
  *        Sensor<Temperature> sensor ... // Generic sensor.
  *        Temperature temp = sensor.getValue();
  *        MassAmount mass = new MassAmount(180, POUND); // Combination magnitude/precision/unit (measurement)
  *        Vector3D<Velocity> aircraftSpeed = new Vector3D(12.0, 34.0, -45.5, METRE_PER_SECOND);
  *     [/code]</p>
  *
  * <p> This package holds only the quantities required by the metric system.</p>
  *
  * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
  * @author  <a href="mailto:uomo@catmedia.us">Werner Keil</a>
  * @version 2.2, $Date: 2011-01-09 19:56:23 +0100 (So, 09 Jan 2011) $
  */
 package org.eclipse.uomo.units.impl.quantity;
 
