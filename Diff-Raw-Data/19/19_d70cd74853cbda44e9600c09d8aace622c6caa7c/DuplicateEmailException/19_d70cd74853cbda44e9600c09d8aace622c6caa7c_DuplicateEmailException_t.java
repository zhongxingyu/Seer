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
 package org.jtalks.common.service.exceptions;
 
 /**
  * Exception for cases when email already exists in system.
  *
  * @author Eugeny Batov
  */
 public class DuplicateEmailException extends DuplicateException {
 
     /**
      * Default constructor.
      */
     public DuplicateEmailException() {
         super();
     }
 
     /**
     * Create exception with specific email.
      *
     * @param email email that causes an exception
      */
    public DuplicateEmailException(String email) {
        this(email, "");
     }
 
     /**
      * This constructor creates {@link DuplicateEmailException} with the specified email and error message
      *
     * @param email   Email that causes an exception
      * @param message Error message
      * @since 0.8
      */
     public DuplicateEmailException(String email, String message) {
        super("Email is duplicated [" + email + "]. " +  message);
     }
 }
