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
  * Copyright 2009 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.
  * All rights reserved.  Use is subject to license terms.
  */
 
 package de.escidoc.core.sm.business.preprocessing;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author mih
  * 
  * Singleton for caching aggregationDefinitionIds.
  * used to write aggregation-tables synchronized.
  * 
  * @sm
  */
 public final class AggregationIdMapper {
 
    private static AggregationIdMapper instance = new AggregationIdMapper();
 
    private final Map<String, String> aggregationIdMap = new HashMap<String, String>();
 
     /**
      * private Constructor for Singleton.
      * 
      * @sm
      */
     private AggregationIdMapper() {
     }
 
     /**
      * Only initialize Object once. Check for old objects in cache.
      * 
      * @return AggregationIdMapper AggregationIdMapper
      * 
      * @sm
      */
    public static AggregationIdMapper getInstance() {
         return instance;
     }
 
     /**
      * get aggregationDefinitionId entry from hashmap.
      * This is done to synchronize access to the aggregation-tables.
      * 
      * @param aggregationDefinitionId
      *            id of the aggregation definition
      * @return String entry
      * 
      * @sm
      */
     public synchronized String getAggregationIdEntry(
                         final String aggregationDefinitionId) {
         if (!aggregationIdMap.containsKey(aggregationDefinitionId)) {
             aggregationIdMap.put(
                     aggregationDefinitionId, aggregationDefinitionId);
         }
         return aggregationIdMap.get(aggregationDefinitionId);
     }
 
 }
