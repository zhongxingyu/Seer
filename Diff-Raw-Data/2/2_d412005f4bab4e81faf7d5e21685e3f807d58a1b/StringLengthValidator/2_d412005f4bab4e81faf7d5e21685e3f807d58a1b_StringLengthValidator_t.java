 /*
  * StringLengthValidator.java
  *
  * Created on July 16, 2007, 5:29 PM
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.totsp.gwittir.client.validator;
 
 import com.totsp.gwittir.client.validator.ValidationException;
 import com.totsp.gwittir.client.validator.Validator;
 
 
 /**
  *
  * @author <a href="mailto:cooper@screaming-penguin.com">Robert "kebernet" Cooper</a>
  */
 public class StringLengthValidator implements Validator {
     int max;
     int min;
 
     /** Creates a new instance of StringLengthValidator */
     public StringLengthValidator(int minCharacters, int maxCharacters) {
         this.min = minCharacters;
         this.max = maxCharacters;
     }
 
     public Object validate(Object value) throws ValidationException {
        if((value == null) || (value.toString().length() < min) || (value.toString().length() > max)) {
             throw new ValidationException("Value must be at least " + min +
                 "and no more than " + max + " characters.",
                 StringLengthValidator.class);
         }
 
         return value;
     }
 }
