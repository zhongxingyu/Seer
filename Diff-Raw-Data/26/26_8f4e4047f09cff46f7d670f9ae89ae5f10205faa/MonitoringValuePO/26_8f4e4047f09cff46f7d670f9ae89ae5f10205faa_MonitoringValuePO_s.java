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
 package org.eclipse.jubula.client.core.model;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Embeddable;
 
 import org.eclipse.jubula.tools.objects.MonitoringValue;
 
 /**
  * This class represent a monitoring value
  * @author BREDEX GmbH
  * @created 20.08.2010
  */
 @Embeddable
 public class MonitoringValuePO extends MonitoringValue {
 
     /** default */
     public MonitoringValuePO() {
         super();
     }
 
     /**
      * @param value the value to set
      * @param type the type to set
      */
     public MonitoringValuePO(String value, String type) {
         super(value, type);
 
     }
 
     /**
      * 
      * @param value the value to set              
      * @param type the type to set                 
      * @param category the category to set, which will be displayed in
      * the properties view
      *            
      */
     public MonitoringValuePO(String value, String type, String category) {
         super(value, type, category);
 
     }
 
     /**
      * 
      * @param value the value to set              
      * @param type the type to set                 
      * @param category the category to set, which will be displayed in
      * the properties view
      * @param isSignificant the significant value. This value will be displayed
      * in the TestResultSummaryView
      *            
      */
     public MonitoringValuePO(String value, String type, String category,
             boolean isSignificant) {
         super(value, type, category, isSignificant);
 
     }
 
     /**
      * @return the value
      */
     @Basic
     public String getValue() {
         return super.getValue();
     }
 
     /**
      * @return the type
      */
     @Basic
     public String getType() {
         return super.getType();
     }
 
     /**
      * {@inheritDoc}
      */
     @Basic
     public String getCategory() {
         return super.getCategory();
     }
 
     /**
      * {@inheritDoc}
      */
     public void setCategory(String category) {
         super.setCategory(category);
     }
 
     /**
      * @param value the value to set
      */

     public void setValue(String value) {
         super.setValue(value);
     }
 
     /**
      * @param type the type to set
      */
     public void setType(String type) {
         super.setType(type);
     }
 
     /**
      * {@inheritDoc}
      */

     public void setSignificant(Boolean significant) {
         super.setSignificant(significant);
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Basic
    @Column(name = "isSignificant")
     public Boolean isSignificant() {
         return super.isSignificant();
 
     }
 
 }
