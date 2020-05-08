 /**********************************************************************
 Copyright (c) 2009 Andy Jefferson and others. All rights reserved.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
 Contributors :
  ...
 ***********************************************************************/
 package org.datanucleus.store.hbase.query;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.datanucleus.exceptions.NucleusException;
 import org.datanucleus.query.evaluator.JPQLEvaluator;
 import org.datanucleus.query.evaluator.JavaQueryEvaluator;
 import org.datanucleus.store.ExecutionContext;
 import org.datanucleus.store.hbase.HBaseManagedConnection;
 import org.datanucleus.store.query.AbstractJPQLQuery;
 import org.datanucleus.util.NucleusLogger;
 
 /**
  * Implementation of JPQL for HBase datastores.
  */
 public class JPQLQuery extends AbstractJPQLQuery
 {
     /**
      * Constructs a new query instance that uses the given persistence manager.
      * @param ec Execution Context
      */
     public JPQLQuery(ExecutionContext ec)
     {
         this(ec, (JPQLQuery) null);
     }
 
     /**
      * Constructs a new query instance having the same criteria as the given query.
      * @param ec Execution Context
      * @param q The query from which to copy criteria.
      */
     public JPQLQuery(ExecutionContext ec, JPQLQuery q)
     {
         super(ec, q);
     }
 
     /**
      * Constructor for a JPQL query where the query is specified using the "Single-String" format.
      * @param ec Execution Context
      * @param query The query string
      */
     public JPQLQuery(ExecutionContext ec, String query)
     {
         super(ec, query);
     }
 
     protected Object performExecute(Map parameters)
     {
         HBaseManagedConnection mconn = (HBaseManagedConnection) ec.getStoreManager().getConnection(ec);
         try
         {
             long startTime = System.currentTimeMillis();
             if (NucleusLogger.QUERY.isDebugEnabled())
             {
                 NucleusLogger.QUERY.debug(LOCALISER.msg("021046", "JPQL", getSingleStringQuery(), null));
             }
             List candidates = null;
             if (candidateCollection == null)
             {
                 candidates = HBaseQueryUtils.getObjectsOfCandidateType(ec, mconn, candidateClass, subclasses,
                     ignoreCache, getFetchPlan());
             }
             else
             {
                 candidates = new ArrayList(candidateCollection);
             }
 
             // Apply any result restrictions to the results
             JavaQueryEvaluator resultMapper = new JPQLEvaluator(this, candidates, compilation, 
                 parameters, ec.getClassLoaderResolver());
             Collection results = resultMapper.execute(true, true, true, true, true);
 
             if (NucleusLogger.QUERY.isDebugEnabled())
             {
                 NucleusLogger.QUERY.debug(LOCALISER.msg("021074", "JPQL", 
                     "" + (System.currentTimeMillis() - startTime)));
             }
 
             if (type == BULK_DELETE)
             {
                 Iterator iter = results.iterator();
                 while (iter.hasNext())
                 {
                     Object obj = iter.next();
                    ec.deleteObjectInternal(obj);
                 }
                 return Long.valueOf(results.size());
             }
             else if (type == BULK_UPDATE)
             {
                 throw new NucleusException("Bulk Update is not yet supported");
             }
             else
             {
                 return results;
             }
         }
         finally
         {
             mconn.release();
         }
     }
 }
