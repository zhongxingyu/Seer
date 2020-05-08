 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: ValidateResourceResult.java,v 1.3 2009-05-13 21:55:29 veiming Exp $
  */
 
 package com.sun.identity.entitlement;
 
 /**
  * This class has an error code which indicates why the resource name is
  * valid or invalid; and also a message.
  */
 public class ValidateResourceResult {
     public static int VALID_CODE_VALID = 0;
     public static int VALID_CODE_INVALID = 1;
    public static int VALID_CODE_DOES_NOT_MATCH_VALID_RESOURCES = 2;
 
     private int validCode;
     private String message;
 
     /**
      * Constructor.
      *
      * @param validCode valid code.
      * @param message Message.
      */
     public ValidateResourceResult(int validCode, String message) {
         this.validCode = validCode;
         this.message = message;
     }
 
     /**
      * Returns message.
      *
      * @return message.
      */
     public String getMessage() {
         return message;
     }
 
     /**
      * Returns valid code.
      *
      * @return valid code.
      */
     public int getValidCode() {
         return validCode;
     }
 
     /**
      * Returns <code>true</code> if it is valid.
      *
      * @return <code>true</code> if it is valid.
      */
     public boolean isValid() {
         return (validCode == VALID_CODE_VALID);
     }
 }
