 /* The contents of this file are subject to the terms
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
 * $Id: FederationManagerCLIConstants.java,v 1.1 2007-05-31 19:39:32 cmwesley Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.cli;
 
 /**
  * <code>FederationManagerCLIConstants</code> contains strings for the required and 
  * optional arguments used in the fmadm CLI.
  */
 public interface FederationManagerCLIConstants {
     
     /**
      * Administrator ID argument/option  
      */
     String ARGUMENT_ADMIN_ID = "adminid";
     
     /**
      * Administrator ID short argument/option
      */
     String SHORT_ARGUMENT_ADMIN_ID = "u";
     
     /**
      * Password argument/option
      */
     String ARGUMENT_PASSWORD = "password";
     
     /**
      * Password short argument/option
      */
     String SHORT_ARGUMENT_PASSWORD = "w";
     
     /**
      * Password file argument/option
      */
     String ARGUMENT_PASSWORD_FILE = "password-file";
     
     /**
      * Password file short argument/option
      */
     String SHORT_ARGUMENT_PASSWORD_FILE = "f";
     
     /**
      * Realm argument/option
      */
     String REALM_ARGUMENT = "realm";
     
     /**
      * Realm short argument/option
      */
     String SHORT_REALM_ARGUMENT = "e";
     
     /**
      * Recursive argument/option
      */
     String RECURSIVE_ARGUMENT = "recursive";
     
     /**
      * Short recursive argument/option
      */
     String SHORT_RECURSIVE_ARGUMENT = "r";
     
     /**
      * Filter argument/option
      */
     String FILTER_ARGUMENT = "filter";
     
     /**
      * Short filter argument/option
      */
     String SHORT_FILTER_ARGUMENT = "x";
     
     /** 
      * Attribute names argument/option
      */
     String ATTRIBUTE_NAMES_ARGUMENT = "attributenames";
     
     /**
      * Short attribute names argument/option
      */
     String SHORT_ATTRIBUTE_NAMES_ARGUMENT = "a";
     
     /**
      * Idtype argument/option
      */
     String ID_TYPE_ARGUMENT = "idtype";
     
     /**
      * Short idtype argument/option
      */
     String SHORT_ID_TYPE_ARGUMENT = "t";
     
     /**
      * Idname argument/option
      */
     String ID_NAME_ARGUMENT = "idname";
     
     /**
      * Short idname argument/option
      */
     String SHORT_ID_NAME_ARGUMENT = "i";
 }
