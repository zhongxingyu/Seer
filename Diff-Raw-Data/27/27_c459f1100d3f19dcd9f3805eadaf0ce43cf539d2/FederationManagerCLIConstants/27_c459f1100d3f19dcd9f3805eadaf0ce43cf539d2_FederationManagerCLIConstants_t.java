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
 * $Id: FederationManagerCLIConstants.java,v 1.11 2008-08-21 20:22:14 srivenigan Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.cli;
 
 /**
  * <code>FederationManagerCLIConstants</code> contains strings for the required 
  * and optional arguments used in the ssoadm CLI.
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
     
     /**
      * Attributevalues argument/option
      */
     String ATTRIBUTE_VALUES_ARGUMENT = "attributevalues";
     
     /**
      * Short attributevalues argument/option
      */
     String SHORT_ATTRIBUTE_VALUES_ARGUMENT = "a";
     
     /**
      * Datafile argument/option
      */
     String DATA_FILE_ARGUMENT = "datafile";
     
     /**
      * Short datafile argument/option
      */
     String SHORT_DATA_FILE_ARGUMENT = "D";
     
     /**
      * Idnames argument/option
      */
     String ID_NAMES_ARGUMENT = "idnames";
     
     /**
      * Memberidname argument/option
      */
     String MEMBER_ID_NAME_ARGUMENT = "memberidname";
     
     /**
      * Short memberidname argument/option
      */
     String SHORT_MEMBER_ID_NAME_ARGUMENT = "m";
     
     /**
      * Memberidtype argument/option
      */
     String MEMBER_ID_TYPE_ARGUMENT = "memberidtype";
     
     /**
      * Short memberidtype argument/option
      */
     String SHORT_MEMBER_ID_TYPE_ARGUMENT = "y";
     
     /**
      * Membershipidtype argument/option
      */
     String MEMBERSHIP_ID_TYPE_ARGUMENT = "membershipidtype";
     
     /**
      * Short membershipidtype argument/option
      */
     String SHORT_MEMBERSHIP_ID_TYPE_ARGUMENT = "m";
     
     /**
      * Servicename argument/option
      */
     String SERVICENAME_ARGUMENT = "servicename";
     
     /**
      * Short servicename argument/option
      */
     String SHORT_SERVICENAME_ARGUMENT = "s";
     
     /**
      * Attributename argument/option
      */
     String ATTRIBUTE_NAME_ARGUMENT = "attributename"; 
     
     /**
      * Authtype argument/option
      */
     String AUTHTYPE_ARGUMENT = "authtype";
     
     /**
      * Short authtype argument/option
      */
     String SHORT_AUTHTYPE_ARGUMENT = "t";
     
     /**
      * Name argument/option
      */
     String NAME_ARGUMENT = "name";
     
     /**
      * Names argument/option
      */
     String NAMES_ARGUMENT = "names";
     
     /**
      * Short names argument/option
      */
     String SHORT_NAMES_ARGUMENT = "m";
     
     /**
      * Append argument/option
      */
     String APPEND_ARGUMENT = "append";
     
     /**
      * Short append argument/option
      */
     String SHORT_APPEND_ARGUMENT = "p";
     
     /**
      * Revision number argument/option
      */
     String REVISION_NO_ARGUMENT = "revisionnumber";
 
     /**
     * Short revision number argument/option
      */
     String SHORT_REVISION_NO_ARGUMENT = "r";
     
     /**
     * Xml File argument/option
     */
    String XML_FILE_ARGUMENT = "xmlfile";
    
    /**
     * Short xml file argument/option
     */
    String SHORT_XML_FILE_ARGUMENT = "X";
    
    /**
      * agenttype argument/option
      */
     String AGENTTYPE_ARGUMENT = "agenttype";
     
     /**
      * Short agenttype argument/option
      */
     String SHORT_AGENTTYPE_ARGUMENT = "t";
 
     /**
      * agenttype argument/option
      */
     String AGENTNAME_ARGUMENT = "agentname";
     
     /**
      * Short agenttype argument/option
      */
     String SHORT_AGENTNAME_ARGUMENT = "b";
     
     /**
      * agenttype argument/option
      */
     String AGENTNAMES_ARGUMENT = "agentnames";
     
     /**
      * Short agenttype argument/option
      */
     String SHORT_AGENTNAMES_ARGUMENT = "s";
 
    /**
     * Continue argument/option
     */
    String CONTINUE_ARGUMENT = "continue";
    
    /**
     * Short continue argument/option
     */
    String SHORT_CONTINUE_ARGUMENT = "c";

 }
