 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.shell.internal.converters;
 
 import org.eclipse.virgo.kernel.shell.Converter;
 
 
 
 /**
  * <p>
  * LongConverter is registered in the service registry to provide a conversion service 
  * from both {@link Long} and <code>long</code> to and from Strings for use by the command 
  * 
  * resolver.
  * </p>
  *
  * <strong>Concurrent Semantics</strong><br />
  *
  * LongConverter is thread safe
  *
  */
 final public class LongConverter implements Converter {
 
     private static final String[] TYPES = new String[]{ Long.class.getName(), long.class.getName() };
     
     /**
      * Simple getter used when this class is created as a bean and placed in the service registry.
      * 
      * @return String or String[] of the types this converter can convert
      */
     public static String[] getTypes() {
        return TYPES.clone();
     }
 
     /** 
      * {@inheritDoc}
      */
     public Object convert(Class<?> desiredType, Object in) throws Exception {
         if(canConvert(desiredType)){
             try{
                 return Long.valueOf(in.toString());
             }catch (NumberFormatException e){
                 // no-op to just return null
             }
         }
         return null;
     }
 
     /** 
      * {@inheritDoc}
      */
     public CharSequence format(Object target, int level, Converter escape) throws Exception {
         if(canFormat(target)){
             return String.valueOf(target);
         }
         return null;
     }
     
     private boolean canConvert(Class<?> desiredType) {
         return Long.class.equals(desiredType) || long.class.equals(desiredType);
     }
     
     private boolean canFormat(Object target) {
         return target instanceof Long;
     }
 
 }
