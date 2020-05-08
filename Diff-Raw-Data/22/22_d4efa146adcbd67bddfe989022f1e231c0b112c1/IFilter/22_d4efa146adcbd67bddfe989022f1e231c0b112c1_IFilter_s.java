 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.filters.experimental;
 
 /**
  * Interface for any type of filters
  * 
  * @author Pechko_E
  * @since 1.0.0
  */
 public interface IFilter {
     
     public boolean accept(Object value);
 
 //    public void setProperty(String property);
 //
//    public String getProperty();
 
 }
