 /*
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  *
  * The Original Code is the Kowari Metadata Store.
  *
  * The Initial Developer of the Original Code is Plugged In Software Pty
  * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
  * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
  * Plugged In Software Pty Ltd. All Rights Reserved.
  *
  * Contributor(s): N/A.
  *
  * [NOTE: The text of this Exhibit A may differ slightly from the text
  * of the notices in the Source Code files of the Original Code. You
  * should use the text of this Exhibit A rather than the text found in the
  * Original Code Source Code for Your Modifications.]
  *
  */
 
 package org.mulgara.resolver;
 
 // Java 2 standard packages
 import java.io.*;
 import java.net.URI;
 import java.util.*;
 
 // Third party packages
 import org.apache.log4j.Logger;
 import org.jrdf.graph.Triple;
 
 // Local packages
 import org.mulgara.query.*;
 import org.mulgara.query.rdf.URIReferenceImpl;
 import org.mulgara.resolver.spi.*;
 import org.mulgara.server.Session;
 
 /**
  * An {@link Operation} that implements the {@link Session#insert} and
  * {@link Session#delete} methods.
  *
  * The operation is really two closely-related operations combined into one
  * class: modification by a specified set of {@link Triple}s, and modification
  * by the result of a specified {@link Query}.
  *
  * @created 2004-11-24
  *
  * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
  *
  * @version $Revision: 1.8 $
  *
  * @modified $Date: 2005/01/05 04:58:24 $ by $Author: newmana $
  *
  * @maintenanceAuthor $Author: newmana $
  *
  * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
  *   Technology, Inc</a>
  *
  * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
  */
 class ModifyModelOperation implements Operation
 {
   /**
    * Logger.
    *
    * This is named after the class.
    */
   private static final Logger logger =
     Logger.getLogger(ModifyModelOperation.class.getName());
 
   /**
    * The URI of the model to be removed.
    */
   private final URI modelURI;
 
   /**
    * The statements whose occurence is to be modified.
    *
    * If this field is not <code>null</code>, {@link #query} will be
    * <code>null</code>
    */
   private final Set tripleSet;
 
   /**
    * The query generating the statements whose occurence is to be modified.
    */
   private final Query query;
 
   /**
    * Whether to insert or delete statements from the model.
    */
   private final boolean insert;
 
   private final DatabaseSession databaseSession;
 
   //
   // Constructor
   //
 
   /**
    * Construct a modification of a literal set of statements.
    *
    * @param modelURI  the {@link URI} of the model to be removed, never
    *   <code>null</code>
    * @param tripleSet  the statements to be modified, never <code>null</code>
    * @param insert  whether to insert or delete the <var>tripleSet</var> from
    *   the model
    * @throws IllegalArgumentException if <var>modelURI</var> or
    *   <var>tripleSet</var> are <code>null</code>
    */
   ModifyModelOperation(URI modelURI, Set tripleSet, boolean insert)
   {
     // Validate "modelURI" parameter
     if (modelURI == null) {
       throw new IllegalArgumentException("Null \"modelURI\" parameter");
     }
 
     // Validate "statements" parameter
     if (tripleSet == null) {
       throw new IllegalArgumentException("Null \"tripleSet\" parameter");
     }
 
     // Initialize fields
     this.modelURI  = modelURI;
     this.tripleSet = tripleSet;
     this.query     = null;
     this.insert    = insert;
     this.databaseSession = null;
   }
 
   /**
    * Construct a modification of a query-generated set of statements.
    *
    * @param modelURI  the {@link URI} of the model to be removed, never
    *   <code>null</code>
    * @param query  the query used to generate the statements to be modified,
    *   never <code>null</code>
    * @param insert  whether to insert or delete statements from the model
    * @throws IllegalArgumentException if <var>modelURI</var> or
    *   <var>query</var> are <code>null</code>
    * @throws QueryException if <var>query</var> doesn't have exactly three
    *   variables in its <code>SELECT</code> clause
    */
   ModifyModelOperation(URI modelURI, Query query, boolean insert,
                        DatabaseSession databaseSession)
     throws QueryException
   {
     // Validate "modelURI" parameter
     if (modelURI == null) {
       throw new IllegalArgumentException("Null \"modelURI\" parameter");
     }
 
     // Validate "query" parameter
     if (query == null) {
       throw new IllegalArgumentException("Null \"query\" parameter");
     }
     if (query.getVariableList().size() != 3) {
       throw new QueryException(
           "Invalid select clause in insert/select.  Exactly 3 terms required");
     }
 
     // Validate "databaseSession" parameter
     if (databaseSession == null) {
       throw new IllegalArgumentException("Null \"databaseSession\" parameter");
     }
 
     // Initialize fields
     this.modelURI  = modelURI;
     this.tripleSet = null;
     this.query     = query;
     this.insert    = insert;
     this.databaseSession = databaseSession;
   }
 
   //
   // Methods implementing Operation
   //
 
   public void execute(OperationContext       operationContext,
                       SystemResolver         systemResolver,
                       ResolverSessionFactory resolverSessionFactory,
                       DatabaseMetadata       metadata) throws Exception
   {
     Statements statements;
     if (tripleSet != null) {
       assert query == null;
 
       statements = new TripleSetWrapperStatements(
         tripleSet, systemResolver, TripleSetWrapperStatements.PERSIST
       );
     } else {
       assert query != null;
 
       Answer answer = operationContext.doQuery(query);
       try {
         Variable[] vars = answer.getVariables();
         assert vars.length == 3;
         statements = new TuplesWrapperStatements(
            new LocalizedTuples(systemResolver, answer, true),
             vars[0], vars[1], vars[2]);
       } finally {
         answer.close();
       }
     }
     assert statements != null;
 
     try {
       doModify(operationContext, systemResolver, modelURI, statements, insert);
     } finally {
       statements.close();
     }
   }
 
 
   protected void doModify(OperationContext operationContext,
                           SystemResolver   systemResolver,
                           URI              modelURI,
                           Statements       statements,
                           boolean          insert) throws Exception
   {
     long model = systemResolver.localize(new URIReferenceImpl(modelURI));
     model = operationContext.getCanonicalModel(model);
 
     // Make sure security adapters are satisfied
     for (Iterator i = operationContext.getSecurityAdapterList().iterator();
          i.hasNext(); )
     {
       SecurityAdapter securityAdapter = (SecurityAdapter) i.next();
 
       // Lie to the user
       if (!securityAdapter.canSeeModel(model, systemResolver)) {
         throw new QueryException("No such model " + modelURI);
       }
 
       // Tell the truth to the user
       if (!securityAdapter.canModifyModel(model, systemResolver)) {
         throw new QueryException("You aren't allowed to modify " + modelURI);
       }
     }
 
     // Obtain a resolver for the destination model type
     Resolver resolver = operationContext.obtainResolver(
                           operationContext.findModelResolverFactory(model));
     assert resolver != null;
 
     if (logger.isDebugEnabled()) {
       logger.debug("Modifying " + modelURI + " using " + resolver);
     }
 
     resolver.modifyModel(model, statements, insert);
 
     if (logger.isDebugEnabled()) {
       logger.debug("Modified " + modelURI);
     }
   }
 
   /**
    * @return <code>true</code>
    */
   public boolean isWriteOperation()
   {
     return true;
   }
 }
