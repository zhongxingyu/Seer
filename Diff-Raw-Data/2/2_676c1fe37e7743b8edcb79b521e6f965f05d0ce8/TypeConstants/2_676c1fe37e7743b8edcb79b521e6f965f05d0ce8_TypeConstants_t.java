 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.core.internal.types;
 
 /**
  * @author cbateman
  *
  */
 public class TypeConstants 
 {
     /**
      * we overload "void" to represent null
      */
     public final static String  TYPE_NULL = "V";
     /**
      * unboxed boolean 
      */
     public final static String  TYPE_BOOLEAN = "Z";
     /**
      * string type
      */
     public final static String   TYPE_STRING = "Ljava.lang.String;";
     /**
      * big integer
      */
     public final static String   TYPE_BIG_INTEGER = "Ljava.math.BigInteger;";
     
     /**
      * big double
      */
    public final static String   TYPE_BIG_DOUBLE = "Ljava.math.BigDecimal;";
     
     /* boxed types */
     /**
      * Boxed byte
      */
     public final static String   TYPE_BOXED_BYTE = "Ljava.lang.Byte;";
     /**
      * Boxed short
      */
     public final static String   TYPE_BOXED_SHORT = "Ljava.lang.Short;";
     /**
      * Boxed int
      */
     public final static String   TYPE_BOXED_INTEGER = "Ljava.lang.Integer;";
     /**
      * Boxed long
      */
     public final static String   TYPE_BOXED_LONG = "Ljava.lang.Long;";
     /**
      * Boxed float
      */
     public final static String   TYPE_BOXED_FLOAT = "Ljava.lang.Float;";
     /**
      * Boxed double
      */
     public final static String   TYPE_BOXED_DOUBLE = "Ljava.lang.Double;";
     /**
      * Boxed boolean 
      */
     public final static String   TYPE_BOXED_BOOLEAN = "Ljava.lang.Boolean;";
     /**
      * Boxed char 
      */
     public final static String   SIGNATURE_BOXED_CHARACTER = "Ljava.lang.Character";
     /**
      * Map type
      */
     public final static String   TYPE_MAP = "Ljava.util.Map;";
     /**
      * Collection type
      */
     public final static String   TYPE_COLLECTION = "Ljava.util.Collection;";
     /**
      * Comparable type
      */
     public final static String   TYPE_COMPARABLE = "Ljava.lang.Comparable;";
 }
