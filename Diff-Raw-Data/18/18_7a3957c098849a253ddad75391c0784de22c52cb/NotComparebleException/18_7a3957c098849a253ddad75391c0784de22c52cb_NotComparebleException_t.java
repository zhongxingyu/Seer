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
 
 package org.amanzi.neo.services.filters.exceptions;
 
 /**
  * TODO Purpose of 
  * <p>
  *
  * </p>
  * @author Sasha
  * @since 1.0.0
  */
public class NotComparebleException extends RuntimeException{
    
     /** long serialVersionUID field */
     private static final long serialVersionUID = 3884623639107628988L;
     private String exceptionMessage = "value or propertyValue is not compareble type";
     
     public NotComparebleException() {
     }
     public String toString(){
         return "Exception: "+ exceptionMessage;
     }
 
 }
