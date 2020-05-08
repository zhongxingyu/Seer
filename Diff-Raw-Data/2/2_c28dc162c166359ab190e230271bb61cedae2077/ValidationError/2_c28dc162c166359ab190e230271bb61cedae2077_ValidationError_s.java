 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.validation;
 
 /**
  * Designates a constraint violation found while validating a bean using JSR-303
  * annotations. It has two fields - the field which constraint is violated and
  * its corresponding code of error message.
  * 
  * @author Alexey Grigorev
  */
 public class ValidationError {
 
     private final String fieldName;
     private final String errorMessageCode;
 
     /**
     * Constructs a validaion error with two fields - one is field which
      * constraint is violated and its corresponding error message
      * 
      * @param fieldName
      * @param errorMessageCode
      */
     public ValidationError(String fieldName, String errorMessageCode) {
         this.fieldName = fieldName;
         this.errorMessageCode = errorMessageCode;
     }
 
     /**
      * @return the fieldName
      */
     public String getFieldName() {
         return fieldName;
     }
 
     /**
      * @return the errorMessageCode
      */
     public String getErrorMessageCode() {
         return errorMessageCode;
     }
 
 }
