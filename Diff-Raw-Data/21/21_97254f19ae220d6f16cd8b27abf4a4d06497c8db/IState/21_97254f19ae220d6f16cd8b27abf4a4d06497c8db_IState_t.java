 /**
  * Copyright (c) 2005, 2011, Werner Keil, JScience and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil - initial API and implementation
  */
 package org.eclipse.uomo.units;
 
 import org.eclipse.uomo.core.IName;
import org.unitsofmeasurement.quantity.Quantity;
 import org.unitsofmeasurement.quantity.Time;
 
 /**
  * Groups a state name, value and timestamp.
  * The state itself is represented as a <type>Number</type> and the time is measured in IMeasure<Time>
  * A State object is immutable so that it may be easily shared.
  * 
  * @author <a href="mailto:uomo@catmedia.us">Werner Keil</a>
 * @version 1.1, $Date: 2011-04-07 02:02:02 +0430 $
  */
public interface IState<Q extends Quantity<Q>> extends IName {
	IMeasure<Time> time();
	IMeasure<Q> value(); // TODO get* vs. getter-less, also avoid value().value()
 }
