 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.tools.utils;
 
 import java.util.Iterator;
 import java.util.List;
 
/** Dieser Iterator teilt eine Liste in mehrere Sublisten mit maximaler
 * Gre von 1000 Eintrgen Wird gebraucht fr "in (...)" bedingungen in HSQL
 * weil dort maximal 1000 Eintrge auftreten drfen
 * @author BREDEX GmbH
 *
  */
 public class ValueListIterator implements Iterator {
     
     /**
      * max DB in (...) size
      */
     public static final int MAX_DB_VALUE_LIST = 1000;
     
     /**
      * original list
      */
     private List m_values;
     
     /**
      * current index
      */
     private int m_idx;
     
     /**
      * @param values List to be divided into sublists
      */
     public ValueListIterator(List values) {
         m_values = values;
     }
     
     /**
      * @return is there another sublist
      */
     public boolean hasNext() {
         return m_idx < m_values.size();
     }
     
     /**
      * returns the next object
      * @return Object from Iterator interface
      */
     public Object next() {
         Object ret = m_values.subList(
             m_idx, Math.min(m_idx + MAX_DB_VALUE_LIST, m_values.size()));
         m_idx += MAX_DB_VALUE_LIST;
         return ret;
     }
     
     /**
      * for easy usage
      * @return returns the next list 
      */
     public List nextList() {
         return (List)next();
     }
     
     /**
      * remove method from iterator interface
      */
     public void remove() {
         throw new UnsupportedOperationException();
     }
 }
