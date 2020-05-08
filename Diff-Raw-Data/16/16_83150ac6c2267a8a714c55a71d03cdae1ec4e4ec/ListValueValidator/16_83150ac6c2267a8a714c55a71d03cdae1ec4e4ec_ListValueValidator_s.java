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
 * $Id: ListValueValidator.java,v 1.2 2007-12-17 19:42:50 veiming Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.common.configuration;
 
 import com.sun.identity.sm.ServiceAttributeValidator;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * Validates list value in Agent Properties. e.g.
  * <code>com.sun.identity.agents.config.response.attribute.mapping[]=</code>
  */
 public class ListValueValidator implements ServiceAttributeValidator {
     public ListValueValidator() {
     }
 
     /**
      * Returns <code>true</code> if values are of list typed.
      * 
      * @param values the set of values to be validated
      * @return <code>true</code> if values are of list typed.
      */
     public boolean validate(Set values) {
         boolean valid = true;
 
         if ((values != null) && !values.isEmpty()) {
             for (Iterator i = values.iterator(); (i.hasNext() && valid);) {
                 String str = (String)i.next();
                 if (str.length() > 0) {
                    if (str.charAt(0) != '[') {
                        valid = false;
                    } else {
                        valid = (str.indexOf("]=") != -1);
                    }
                 }
             }
         }
         return valid;
     }
 }
