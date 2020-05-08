 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jsnative.global;
 
 import org.eclipse.vjet.dsf.jsnative.anno.Constructor;
 import org.eclipse.vjet.dsf.jsnative.anno.Function;
 import org.eclipse.vjet.dsf.jsnative.anno.JsSupport;
 import org.eclipse.vjet.dsf.jsnative.anno.JsVersion;
 import org.eclipse.vjet.dsf.jsnative.anno.OverLoadFunc;
 import org.eclipse.vjet.dsf.jsnative.anno.Property;
 import org.eclipse.vjet.dsf.jsnative.anno.Static;
 
 /**
  * 
  * Represents JavaScript native Number object
  *
  */
 @JsSupport( {JsVersion.MOZILLA_ONE_DOT_ONE, JsVersion.JSCRIPT_ONE_DOT_ZERO})
 public interface Number extends Object {
 	
 	@Constructor void Number();
 	
 	@Constructor void Number(Number number);
 	
 	@Constructor void Number(String number);
 		
 	/**
 	 * The maximum numeric value representable in JavaScript. 
 	 */
 	@Property @Static Number getMAX_VALUE();
 	
 	/**
 	 * The smallest positive numeric value representable in JavaScript. 
 	 */
 	@Property @Static Number getMIN_VALUE();
 
 	/**
 	 * Special value representing negative infinity; returned on overflow.
 	 */
 	@Property @Static Number getNEGATIVE_INFINITY();
 	
 	/**
 	 * Special value representing infinity; returned on overflow.
 	 */
 	@Property @Static Number getPOSITIVE_INFINITY();
 	
 	/**
 	 * A value representing Not-A-Number.
 	 */
 	@Property @Static Number getNaN();
 	
 	/**
 	 * Returns a string representing the number in exponential notation.
 	 */
 	@OverLoadFunc String toExponential();
 	
 	/**
 	 * Returns a string representing the number in exponential notation.
 	 */
 	@OverLoadFunc String toExponential(Number num);
 	
 	/**
 	 * Returns a string representing the number in exponential notation.
 	 */
 	@OverLoadFunc String toExponential(String num);
 	
 	/**
 	 * Returns a string representing the number in fixed-point notation.
 	 */
 	@OverLoadFunc String toFixed();
 	
 	/**
 	 * Returns a string representing the number in fixed-point notation.
 	 */
 	@OverLoadFunc String toFixed(Number num);
 	
 	/**
 	 * Returns a string representing the number in fixed-point notation.
 	 */
 	@OverLoadFunc String toFixed(String num);
 	
 	/**
 	 * Returns a string representing the number to a specified precision 
 	 * in fixed-point notation.
 	 */
 	@OverLoadFunc String toPrecision();
 	
 	/**
 	 * Returns a string representing the number to a specified precision 
 	 * in fixed-point notation.
 	 */
 	@OverLoadFunc String toPrecision(Number num);
 	
 	/**
 	 * Returns a string representing the number to a specified precision 
 	 * in fixed-point notation.
 	 */
 	@OverLoadFunc String toPrecision(String num);
 	
 	/**
 	 * Returns a string representing the specified object.
 	 */
 	@OverLoadFunc String toString(Number radix);
 	
 	/**
 	 * Returns a string representing the specified object.
 	 */
 	@OverLoadFunc String toString(String radix);
 	
 	/**
 	 * Returns a string representing the specified object.
 	 */
 	@OverLoadFunc java.lang.String toString();
 	
 	/**
 	 * Returns the primitive value of the specified object.
 	 */
 	@Function Number valueOf();
 }
