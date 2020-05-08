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
 
 package org.eclipse.jst.jsf.designtime.el;
 
 import org.eclipse.jst.jsf.context.symbol.ISymbol;
 
 /**
  * Super-class of all design time property resolvers
  * 
  * @author cbateman
  *
  */
 public abstract class AbstractDTPropertyResolver 
 {
     /**
      * Returns a symbol encapsulating the property on base with the name
      * properyId.  Note that getProperty may return *more* property symbols
      * for the same 'base' because some ISymbol's have unconstrained type
      * descriptors.  For example, a bean that implements java.util.Map,
     * may have any number of possible properties add at runtime that cannot
      * be determined at design time.  It is up to implementers of this interface
      * to decide how (whether) to return these objects at design time 
      * 
      * @param base
      * @param propertyId
      * @return the symbol for the named propertyId or null if not found
      */
     public abstract ISymbol getProperty(ISymbol base, Object propertyId);
     
     /**
      * @param base
      * @param offset
      * @return the symbol for the property referred to by the offset 
      * into base when it is treated as either an array or a list.
      * The symbol returned is *not* found in the getAllProperties list.
      */
     public abstract ISymbol getProperty(ISymbol base, int offset);
     
     /**
      * @param base
      * @return all properties of base that can be determined concretely
      * at designtime.  Note that getProperty(base, x) may return non-null
      * for objects not found in the return from this method. @see getProperty
      */
     public abstract ISymbol[] getAllProperties(ISymbol base);
 }
