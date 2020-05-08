 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.common.business.filter;
 
 /**
  * This is an enumeration for the different values of record packing in an SRU
  * request.
  * 
  * @author SCHE
  */
 public enum RecordPacking {
     STRING("string"), XML("xml");
 
     private final String type;
 
     /**
      * Construct a new RecordPacking object.
      * 
      * @param type
      *            record packing type
      */
     RecordPacking(final String type) {
         this.type = type;
     }
 
     /**
      * Get the record packing type.
      * 
      * @return the record packing type
      */
     public String getType() {
         return type;
     }
 
     /**
      * Create a RecordPacking object from the given type.
      * 
      * @param type
      *            record packing type
      * 
      * @return RecordPacking object
      */
     public static RecordPacking fromType(final String type) {
         RecordPacking result = null;
 
         for (RecordPacking e : RecordPacking.values()) {
            if (e.type == type) {
                 result = e;
                 break;
             }
         }
         return result;
     }
 }
