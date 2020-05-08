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
  * Contributor(s):
  *   SymbolicTransformationContext contributed by Netymon Pty Ltd on behalf of
  *   The Australian Commonwealth Government under contract 4500507038.
  *
  * [NOTE: The text of this Exhibit A may differ slightly from the text
  * of the notices in the Source Code files of the Original Code. You
  * should use the text of this Exhibit A rather than the text found in the
  * Original Code Source Code for Your Modifications.]
  *
  */
 
 package org.mulgara.resolver;
 
 // Java 2 standard packages
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 
 // Third party packages
 import org.apache.log4j.Logger;
 import org.jrdf.graph.Node;
 import org.jrdf.graph.URIReference;
 
 // Local packages
 import org.mulgara.query.*;
 import org.mulgara.query.rdf.URIReferenceImpl;
 import org.mulgara.resolver.spi.DatabaseMetadata;
 import org.mulgara.resolver.spi.GlobalizeException;
 import org.mulgara.resolver.spi.LocalizeException;
 import org.mulgara.resolver.spi.Resolution;
 import org.mulgara.resolver.spi.Resolver;
 import org.mulgara.resolver.spi.ResolverFactory;
 import org.mulgara.resolver.spi.ResolverFactoryException;
 import org.mulgara.resolver.spi.SecurityAdapter;
 import org.mulgara.resolver.spi.Statements;
 import org.mulgara.resolver.spi.SymbolicTransformation;
 import org.mulgara.resolver.spi.SymbolicTransformationContext;
 import org.mulgara.resolver.spi.SystemResolver;
 import org.mulgara.resolver.spi.SystemResolverFactory;
 import org.mulgara.resolver.spi.TuplesWrapperStatements;
 import org.mulgara.resolver.view.ViewMarker;
 import org.mulgara.resolver.view.SessionView;
 import org.mulgara.store.nodepool.NodePool;
 import org.mulgara.store.tuples.Tuples;
 import org.mulgara.store.tuples.TuplesOperations;
 
 /**
  * Services provided by {@link DatabaseSession} to invocations of the
  * {@link Operation#execute} method.
  *
  * @created 2004-11-08
  * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
  * @version $Revision: 1.10 $
  * @modified $Date: 2005/05/02 20:07:56 $ by $Author: raboczi $
  * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
  * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
  *   Technology, Inc</a>
  * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
  */
 class DatabaseOperationContext implements OperationContext, SessionView, SymbolicTransformationContext {
  
   /** Logger. */
   private static final Logger logger = Logger.getLogger(DatabaseOperationContext.class.getName());
 
   /** Logger for {@link SymbolicTransformation} plugins. */
   private static final Logger symbolicLogger =
     Logger.getLogger(DatabaseOperationContext.class.getName() + "#symbolic");
 
   /**
    * The models from external resolvers which have been cached as temporary
    * models.
    *
    * Every model in this set can be manipulated by resolvers from the
    * {@link #temporaryResolverFactory}.
    */
   private final Set<LocalNode> cachedModelSet;
 
   /**
    * The models from external resolvers which have been cached as temporary
    * models and modified.
    *
    * Every model in this set can be manipulated by resolvers from the
    * {@link #temporaryResolverFactory}.
    */
   private final Set<LocalNode> changedCachedModelSet;
 
   /**
    * A map from {@link URI}s of models to {@link LocalNode}s representing the
    * localized type of the model.
    *
    * This is populated by {@link #findModelTypeURI} and cleared by
    * clear()
    */
   private final Map<LocalNode,URI> systemModelCacheMap = new WeakHashMap<LocalNode,URI>();
 
   /** Resolver used for accessing the system model (<code>#</code>).  */
   protected SystemResolverFactory systemResolverFactory;
   protected SystemResolver systemResolver;
 
   /** The transaction associated with these operations */
   private MulgaraTransaction transaction;
 
   // Immutable properties of the containing DatabaseSession
   private final Set<ResolverFactory> cachedResolverFactorySet;  // NOTE: Currently unused
   private final Map<ResolverFactory,Resolver> enlistedResolverMap;
   private final Map<String,ResolverFactory> externalResolverFactoryMap;
     // Hold a specific type of ResolverFactory to restrict what should be in there
   private final Map<URI,InternalResolverFactory> internalResolverFactoryMap;
   private final DatabaseMetadata   metadata;
   private final List<SecurityAdapter> securityAdapterList;
   private final URI                temporaryModelTypeURI;
   private final ResolverFactory    temporaryResolverFactory;
   /** Symbolic transformations this instance should apply. */
   private final List<SymbolicTransformation> symbolicTransformationList;
   private final boolean            isWriting;
 
   // Used as a set, all values are null.  Java doesn't provide a WeakHashSet.
   private WeakHashMap<TransactionalAnswer,Object> answers;
 
   DatabaseOperationContext(Set<ResolverFactory> cachedResolverFactorySet,
                            Map<String,ResolverFactory> externalResolverFactoryMap,
                            Map<URI,InternalResolverFactory> internalResolverFactoryMap,
                            DatabaseMetadata      metadata,
                            List<SecurityAdapter> securityAdapterList,
                            URI                   temporaryModelTypeURI,
                            ResolverFactory       temporaryResolverFactory,
                            List<SymbolicTransformation> symbolicTransformationList,
                            SystemResolverFactory systemResolverFactory,
                            boolean               isWriting)
   {
     assert cachedResolverFactorySet   != null;
     assert externalResolverFactoryMap != null;
     assert internalResolverFactoryMap != null;
     assert metadata                   != null;
     assert securityAdapterList        != null;
     assert temporaryModelTypeURI      != null;
     assert temporaryResolverFactory   != null;
     assert symbolicTransformationList != null;
     assert systemResolverFactory      != null;
 
     this.cachedResolverFactorySet   = cachedResolverFactorySet;
     this.externalResolverFactoryMap = externalResolverFactoryMap;
     this.internalResolverFactoryMap = internalResolverFactoryMap;
     this.metadata                   = metadata;
     this.securityAdapterList        = securityAdapterList;
     this.temporaryModelTypeURI      = temporaryModelTypeURI;
     this.temporaryResolverFactory   = temporaryResolverFactory;
     this.symbolicTransformationList = symbolicTransformationList;
     this.isWriting                  = isWriting;
     this.systemResolverFactory      = systemResolverFactory;
 
     this.cachedModelSet             = new HashSet<LocalNode>();
     this.changedCachedModelSet      = new HashSet<LocalNode>();
     this.enlistedResolverMap        = new HashMap<ResolverFactory,Resolver>();
     this.answers                    = new WeakHashMap<TransactionalAnswer,Object>();
   }
 
   //
   // Methods implementing OperationContext
   //
 
   public ResolverFactory findModelResolverFactory(long model) throws QueryException {
 
     if (logger.isDebugEnabled()) logger.debug("Finding resolver factory for model " + model);
 
     // See if the model is an internal one, with a model type
     try {
       URI modelTypeURI = findModelTypeURI(model);
       if (modelTypeURI != null) {
         // The model had a type recorded in the system model, so it's internal
         if (logger.isDebugEnabled()) {
           logger.debug("Model " + model + " type is " + modelTypeURI);
         }
         InternalResolverFactory internalResolverFactory = internalResolverFactoryMap.get(modelTypeURI);
 
         if (internalResolverFactory == null) {
           throw new QueryException("Unsupported model type for model " + model);        }
 
         return internalResolverFactory;
       } else {
         // This might be an external model or an aliased internal model.
         // get the model URI
         Node modelNode = systemResolver.globalize(model);
         if (!(modelNode instanceof URIReference)) {
           throw new QueryException(modelNode.toString() + " is not a valid Model");
         }
         URI modelURI = ((URIReference)modelNode).getURI();
 
         // check if this is really a reference to a local model, using a different server name
         Node aliasedNode = getCanonicalAlias(modelURI);
         if (aliasedNode != null) {
           long aliasedModel = systemResolver.localize(aliasedNode);
           return findModelResolverFactory(aliasedModel);
         }
 
         // test the model URI against the current server
         try {
           if (logger.isDebugEnabled()) {
             logger.debug("Comparing " + metadata.getURI().toString() + " to " + (new URI(modelURI.getScheme(),
                     modelURI.getSchemeSpecificPart(), null)).toString());
           }
 
           // Check all the hostname aliases to see if we're attempting to
           // contact the local server.
           URI tmpModelName = new URI(modelURI.getScheme(),
               modelURI.getSchemeSpecificPart(), null);
           String host = tmpModelName.getHost();
 
           // Ensure that the host name can be extracted - in case there's an
           // opaque hostname.
           if (tmpModelName.isOpaque()) {
            throw new QueryException("Unable to extract hostname from: " +
                tmpModelName);
           }
 
           // Do not test for locality if jar or file protocol
           if (!(modelURI.getScheme().startsWith("file")) &&
             !(modelURI.getScheme().startsWith("jar"))) {
 
             // Check that it's the same host name and server name.
             if ((metadata.getHostnameAliases().contains(host)) &&
                 (metadata.getServerName().equals(metadata.getServerName(modelURI)))) {
               // should be on the current server, but was not found here
               throw new QueryException(modelNode.toString() + " is not a Model");
             }
           }
         } catch (URISyntaxException use) {
           throw new QueryException("Internal error.  Model URI cannot be manipulated.");
         }
 
         // This is not a local model, get the protocol
         String modelProtocol = findProtocol(model);
         if (logger.isDebugEnabled()) {
           logger.debug("Model " + model + " protocol is " + modelProtocol);
         }
 
         // find the factory for this protocol
         ResolverFactory resolverFactory = externalResolverFactoryMap.get(modelProtocol);
         if (resolverFactory == null) {
           throw new QueryException(
               "Unsupported protocol for destination model (" +
               modelProtocol + ", " + model + " : '" + modelProtocol + "')");
         }
 
         // For the moment, not applying caching to any external models
         // TODO: add a method to ResolverFactory interface to test whether
         //       caching is appropriate for that particular implementation
         if (cachedResolverFactorySet.contains(resolverFactory)) {
           return new CacheResolverFactory(resolverFactory,
                                           temporaryResolverFactory,
                                           temporaryModelTypeURI,
                                           cachedModelSet,
                                           changedCachedModelSet);
         } else {
           return resolverFactory;
         }
       }
     } catch (GlobalizeException eg) {
       throw new QueryException("Unable to globalize modeltype", eg);
     } catch (LocalizeException el) {
       throw new QueryException("Unable to localize model", el);
     }
   }
 
   /**
    * Find a cached resolver factory for write back.
    *
    * @return a completely unwrapped resolver factory
    */
   // TODO: Common code with findModelResolverFactory should be consolidated.
   private ResolverFactory findResolverFactory(long model) throws QueryException {
 
     if (logger.isDebugEnabled()) logger.debug("Finding raw resolver factory for model " + model);
 
     try {
       // get the model URI
       Node modelNode = systemResolver.globalize(model);
       if (!(modelNode instanceof URIReference)) {
         throw new QueryException(modelNode.toString() + " is not a valid Model");
       }
       URI modelURI = ((URIReference)modelNode).getURI();
 
       // test the model URI against the current server
       try {
         if (logger.isDebugEnabled()) {
           logger.debug("Comparing " + metadata.getURI().toString() + " to "
               + (new URI(modelURI.getScheme(), modelURI.getSchemeSpecificPart(), null)).toString());
         }
         if (metadata.getURI().equals(
             new URI(modelURI.getScheme(), modelURI.getSchemeSpecificPart(), null))) {
           // should be on the current server, but was not found here
           throw new QueryException(modelNode.toString() + " is not a Model");
         }
       } catch (URISyntaxException use) {
         throw new QueryException("Internal error.  Model URI cannot be manipulated.");
       }
 
       // This is not a local model, get the protocol
       String modelProtocol = findProtocol(model);
       if (logger.isDebugEnabled()) {
         logger.debug("Model " + model + " protocol is " + modelProtocol);
       }
 
       // find the factory for this protocol
       ResolverFactory resolverFactory = externalResolverFactoryMap.get(modelProtocol);
       if (resolverFactory == null) {
         throw new QueryException(
             "Unsupported protocol for destination model (" +
             modelProtocol + ", " + model + " : '" + modelProtocol + "')");
       }
 
       return resolverFactory;
     } catch (GlobalizeException eg) {
       throw new QueryException("Unable to globalize modeltype", eg);
     }
   }
 
   public ResolverFactory findModelTypeResolverFactory(URI modelTypeURI) throws QueryException {
     return internalResolverFactoryMap.get(modelTypeURI);
   }
 
   public List<SecurityAdapter> getSecurityAdapterList() {
     return securityAdapterList;
   }
 
   public Resolver obtainResolver(ResolverFactory resolverFactory) throws QueryException {
 
     // Obtain a resolver
     Resolver resolver = enlistedResolverMap.get(resolverFactory);
     if (resolver != null) {
       return resolver;
     }
 
     try {
       resolver = resolverFactory.newResolver(isWriting, systemResolver, systemResolver);
 
       // FIXME: This is a kludge.  This should be done using a query rewriting
       //        hook in the ResolverFactory interface.  This hook is also
       //        required for efficient evaluation of XSD/Type constraints
       //        (specifically intervals), and distributed queries
       //        (specificially appended joins).
       if (resolver instanceof ViewMarker) {
         ((ViewMarker) resolver).setSession(this);
       }
     } catch (ResolverFactoryException e) {
       throw new QueryException("Unable to obtain resolver", e);
     }
 
     assert resolver != null;
 
     try {
       transaction.enlist(resolver);
     } catch (Exception e) {
       logger.warn("Failed to enlist resolver, aborting resolver");
       resolver.abort();
       throw new QueryException("Unable to enlist " + resolver + " into transaction", e);
     }
 
     enlistedResolverMap.put(resolverFactory, resolver);
 
     return resolver;
   }
 
 
   /**
    * Returns the canonical form of the model, leaving the model alone if it is recognised or unknown.
    *
    * @param model The model to check.
    * @return The new model node, or the current model if it is already canonical or unknown.
    */
   public long getCanonicalModel(long model) {
     // globalize to a URI
     try {
       Node modelNode = systemResolver.globalize(model);
       if (!(modelNode instanceof URIReference)) {
         logger.warn(modelNode.toString() + " is not a valid Model");
         return model;
       }
       URI modelURI = ((URIReference)modelNode).getURI();
 
       // check if this is really a reference to a local model, using a different server name
       Node aliasedNode = getCanonicalAlias(modelURI);
       if (aliasedNode != null) {
         return systemResolver.localize(aliasedNode);
       }
     } catch (Exception e) {
       // unable to get a canonical form, so leave this model alone
     }
     // model was not recognised as being on this server, so leave it alone
     return model;
   }
 
   //
   // Methods required by SymbolicTransformationContext
   //
 
   public URI mapToModelTypeURI(URI modelURI) throws QueryException {
     try {
       if (logger.isDebugEnabled()) logger.debug("Finding modelTypeURI for " + modelURI);
       long rawModel = systemResolver.localize(new URIReferenceImpl(modelURI));
       long canModel = getCanonicalModel(rawModel);
 
       URI modelTypeURI = findModelTypeURI(canModel);
 
       if (logger.isInfoEnabled()) {
         logger.info("Mapped " + modelURI + " via " + rawModel + ":" + canModel + " to ModelTypeURI: " + modelTypeURI);
       }
 
       return modelTypeURI;
     } catch (GlobalizeException eg) {
       throw new QueryException("Failed to map model to modelType", eg);
     } catch (LocalizeException el) {
       throw new QueryException("Failed to map model to modelType", el);
     }
   }
 
   //
   // Internal methods
   //
 
   /**
    * Find the type of a model.
    *
    * @param model  the local node of a model
    * @return the local node representing the type of the <var>model</var>, or
    *   {@link NodePool#NONE} if the <var>model</var> isn't stored within the
    *   system
    * @throws QueryException if the model type can't be determined
    */
   private URI findModelTypeURI(long model) throws QueryException, GlobalizeException {
     // If model is a query-node, model cannot exist in the system model so return null.
     if (model < 0) return null;
 
     // Check our cached version of the system model
     LocalNode modelLocalNode = new LocalNode(model);
     URI modelTypeURI = systemModelCacheMap.get(modelLocalNode);
     if (modelTypeURI != null) {
       return modelTypeURI;
     }
 
     // Query the system model for the type of the model
     Variable modelTypeVariable = new Variable("modelType");
     Constraint modelConstraint =
       new ConstraintImpl(new LocalNode(model),
                          new LocalNode(metadata.getRdfTypeNode()),
                          modelTypeVariable,
                          new LocalNode(metadata.getSystemModelNode()));
     Resolution resolution = systemResolver.resolve(modelConstraint);
     assert resolution != null;
 
     // Check the solution and extract the model type (if any) from it
     try {
       resolution.beforeFirst();
       if (resolution.next()) {
         long modelType = resolution.getColumnValue(
             resolution.getColumnIndex(modelTypeVariable));
 
         if (resolution.next()) {
           throw new QueryException("Model " + model +
               " has more than one type!");
         }
         Node modelNode = systemResolver.globalize(modelType);
         assert modelNode instanceof URIReferenceImpl;
         modelTypeURI = ((URIReferenceImpl) modelNode).getURI();
         systemModelCacheMap.put(modelLocalNode, modelTypeURI);
 
         return modelTypeURI;
       } else {
         return null;
       }
     } catch (TuplesException e) {
       throw new QueryException("Unable to determine model type of " + model, e);
     } finally {
       if ( resolution != null ) {
         try {
           resolution.close();
         } catch (TuplesException e) {
           logger.warn("Unable to close find model type resolution to model " + model, e);
         }
       }
     }
   }
 
   /**
    * @param n  the local node corresponding to the URI reference
    * @return  the scheme part of the <var>node</var>'s URI reference
    * @throws QueryException if the <var>node</var> can't be globalized or
    *   isn't a URI reference
    */
   private String findProtocol(long n) throws QueryException {
     try {
       // Globalize the node
       Node node = (Node) systemResolver.globalize(n);
       if (!(node instanceof URIReference)) {
         throw new QueryException(node + " is not a URI reference");
       }
       if (logger.isDebugEnabled()) {
         logger.debug("Model URI for model " + n + " is " + node);
       }
       // Return the protocol
       return ((URIReference) node).getURI().getScheme();
     } catch (GlobalizeException e) {
       throw new QueryException("Unable to globalize node " + n, e);
     }
   }
 
   /**
    * Check if the given model actually refers to a model on the local server.
    *
    * @param modelURI The URI of the model being searched for.
    * @return The Node for the local model, or <code>null</code> if not found.
    * @throws QueryException When the model URI cannot be manipulated.
    */
   private Node getCanonicalAlias(URI modelURI) throws QueryException {
     if (logger.isDebugEnabled()) {
       logger.debug("Checking for an alias on: " + modelURI);
     }
 
     // extract the host name
     String host = modelURI.getHost();
     if (host == null) {
       return null;
     }
     // Check if this host has been heard of before
     if (metadata.getHostnameAliases().contains(host)) {
       // this name is acceptable, so leave it alone
       return null;
     }
     // Check with a DNS server to see if this host is recognised
     InetAddress addr = null;
     try {
       addr = InetAddress.getByName(host);
     } catch (UnknownHostException uhe) {
       // The host was unknown, so allow resolution to continue as before
       return null;
     }
     // check the various names against known aliases
     if (metadata.getHostnameAliases().contains(addr.getHostName()) ||
         metadata.getHostnameAliases().contains(addr.getCanonicalHostName()) ||
         metadata.getHostnameAliases().contains(addr.getHostAddress())) {
       // change the host name to one that is recognised
       return getLocalURI(modelURI);
     }
 
     // not found, so return nothing
     return null;
   }
 
 
   /**
    * Convert a URI to a URIReference which refers to the canonical local machine name.
    *
    * @param uri The URI to update.
    * @return The URIReference representing the same URI as the parameter, with the host name updated.
    * @throws QueryException When the uri cannot be manipulated.
    */
   private Node getLocalURI(URI uri) throws QueryException {
     // use the system model to find the local host name
     String newHost = metadata.getSystemModelURI().getHost();
     // update the URI
     try {
       URI newModelURI = new URI(uri.getScheme(), newHost, uri.getPath(), uri.getFragment());
       logger.debug("Changing model URI from " + uri + " to " + newModelURI);
 
       return new URIReferenceImpl(newModelURI);
     } catch (URISyntaxException e) {
       throw new QueryException("Internal error.  Model URI cannot be manipulated.");
     }
   }
 
   /**
    * Resolve a localized constraint into the tuples which satisfy it.
    *
    * This method must be called within a transactional context.
    *
    * Will be made package-scope as soon as the View kludge is resolved.
    *
    * Deprecation warning removed to assist development.
    *
    * @param constraint  a localized constraint
    * @return the tuples satisfying the <var>constraint</var>
    * @throws IllegalArgumentException if <var>constraint</var> is
    *   <code>null</code>
    * @throws QueryException if the <var>constraint</var> can't be resolved
    */
   public Tuples resolve(Constraint constraint) throws QueryException {
     if (logger.isDebugEnabled()) {
       logger.debug("Resolving " + constraint);
     }
 
     // Validate "constraint" parameter
     if (constraint == null) {
       throw new IllegalArgumentException("Null \"constraint\" parameter");
     }
 
     ConstraintElement modelElem = constraint.getModel();
     if (modelElem instanceof Variable) {
       return resolveVariableModel(constraint);
     } else if (modelElem instanceof LocalNode) {
       long model = ((LocalNode) modelElem).getValue();
       long realModel = getCanonicalModel(model);
 
       // Make sure security adapters are satisfied
       for (SecurityAdapter securityAdapter: securityAdapterList) {
 
         // Lie to the user
         if (!securityAdapter.canSeeModel(realModel, systemResolver)) {
           try {
             throw new QueryException("No such model " + systemResolver.globalize(realModel));
           } catch (GlobalizeException e) {
             logger.warn("Unable to globalize model " + realModel);
             throw new QueryException("No such model");
           }
         }
       }
 
       for (SecurityAdapter securityAdapter: securityAdapterList) {
 
         // Tell a different lie to the user
         if (!securityAdapter.canResolve(realModel, systemResolver)) {
           return TuplesOperations.empty();
         }
       }
 
       // if the model was changed then update the constraint
       if (model != realModel) {
         constraint = ConstraintOperations.rewriteConstraintModel(new LocalNode(realModel), constraint);
       }
 
       // Evaluate the constraint
       Tuples result = obtainResolver(findModelResolverFactory(realModel)).resolve(constraint);
       assert result != null;
 
       return result;
     } else {
       throw new QueryException("Non-localized model in resolve: " + modelElem);
     }
   }
 
   /**
   * Resolve a {@link Constraint} in the case where the model isn't fixed.
   *
   * This is mostly relevant in the case where the <code>in</code> clause takes
   * a variable parameter.  It's tricky to resolve because external models may
   * be accessible to the system, but aren't known to it unless they're named.
   * The policy we take is to only consider internal models.
   *
   * @param constraint  a constraint with a {@link Variable}-valued model
   *   element, never <code>null</code>
   * @return the solutions to the <var>constraint</var> occurring in all
   *   internal models, never <code>null</code>
   * @throws QueryException if the solution can't be evaluated
   */
   private Tuples resolveVariableModel(Constraint constraint)
     throws QueryException
   {
     assert constraint != null;
     assert constraint.getElement(3) instanceof Variable;
 
     Tuples tuples = TuplesOperations.empty();
 
     // This is the alternate code we'd use if we were to consult external
     // models as well as internal models during the resolution of variable IN
     // clauses:
     //
     //Iterator i = resolverFactoryList.iterator();
 
     for (ResolverFactory resolverFactory: internalResolverFactoryMap.values()) {
       assert resolverFactory != null;
 
       // Resolve the constraint
       Resolver resolver = obtainResolver(resolverFactory);
       if (logger.isDebugEnabled()) {
         logger.debug("Resolving " + constraint + " against " + resolver);
       }
       Resolution resolution = resolver.resolve(constraint);
       assert resolution != null;
 
       try {
         // If this is a complete resolution of the constraint, we won't have to
         // consider any of the other resolvers
         if (resolution.isComplete()) {
           if (logger.isDebugEnabled()) {
             logger.debug("Returning complete resolution from " + resolver);
           }
           tuples.close();
 
           return resolution;
         } else {
           // Append the resolution to the overall solutions
           if (logger.isDebugEnabled()) {
             logger.debug("Appending " + resolver);
           }
           Tuples oldTuples = tuples;
           tuples = TuplesOperations.append(tuples, resolution);
           oldTuples.close();
         }
       } catch (TuplesException e) {
         throw new QueryException("Unable to resolve " + constraint, e);
       }
     }
 
     if (logger.isDebugEnabled()) {
       logger.debug("Resolved " + constraint + " to " +
           TuplesOperations.formatTuplesTree(tuples));
     }
 
     return tuples;
   }
 
   Tuples innerCount(Query query) throws QueryException {
     if (query == null) {
       throw new IllegalArgumentException("Null \"query\" parameter");
     }
 
     if (logger.isInfoEnabled()) {
       logger.info("Inner Count: " + query);
     }
     try {
       query = transform(query);
       LocalQueryResolver lq = new LocalQueryResolver(this, systemResolver);
       Tuples result = lq.resolveE(query);
       query.close();
 
       return result;
     } catch (QueryException eq) {
       throw eq;
     } catch (Exception e) {
       throw new QueryException("Failed to evaluate count", e);
     }
   }
 
   protected void doModify(URI modelURI, Statements statements, boolean insert) throws Throwable {
     long model = systemResolver.localize(new URIReferenceImpl(modelURI));
     model = getCanonicalModel(model);
 
     // Make sure security adapters are satisfied
     for (SecurityAdapter securityAdapter: securityAdapterList) {
 
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
     Resolver resolver = obtainResolver(findModelResolverFactory(model));
     assert resolver != null;
 
     if (logger.isDebugEnabled()) {
       logger.debug("Modifying " + modelURI + " using " + resolver);
     }
 
     resolver.modifyModel(model, statements, insert);
 
     if (logger.isDebugEnabled()) {
       logger.debug("Modified " + modelURI);
     }
   }
 
   public Answer doQuery(Query query) throws Exception {
     TransactionalAnswer result;
 
     query = transform(query);
 
     LocalQueryResolver localQuery = new LocalQueryResolver(this, systemResolver);
 
     // Complete the numerical phase of resolution
     Tuples tuples = localQuery.resolveE(query);
     result = new TransactionalAnswer(transaction, new SubqueryAnswer(this, systemResolver, tuples, query.getVariableList()));
     answers.put(result, null);
     tuples.close();
 
     return result;
   }
 
   /**
    * Apply the registered transformations to the query until we reach a
    * fixed-point.
    */
   Query transform(Query query) throws Exception {
     // Start with the symbolic phase of resolution
     if (symbolicLogger.isDebugEnabled()) {
       symbolicLogger.debug("Before transformation: " + query);
     }
 
     MutableLocalQueryImpl mutable = new MutableLocalQueryImpl(query);
     Iterator<SymbolicTransformation> i = symbolicTransformationList.iterator();
     while (i.hasNext()) {
       SymbolicTransformation symbolicTransformation = i.next();
       assert symbolicTransformation != null;
 
       symbolicTransformation.transform(this, mutable);
 
       // When a transformation succeeds, we rewind and start from the
       // beginning of the symbolicTransformationList again
       if (mutable.isModified()) {
         if (symbolicLogger.isDebugEnabled()) {
           symbolicLogger.debug("Symbolic transformation: " + mutable);
         }
 
         Query tmp = query;
         query = new Query(query, mutable.getConstraintExpression());
         tmp.close();
 
         mutable = new MutableLocalQueryImpl(query);
 
         // start again
         i = symbolicTransformationList.iterator();
       }
     }
 
     return query;
   }
 
   void clear() throws QueryException {
     Throwable error = null;
     try {
       for (TransactionalAnswer answer : answers.keySet()) {
         try {
           answer.sessionClose();
         } catch (Throwable th) {
           if (error == null) {
             error = th;
           }
         }
       }
       answers.clear();
     } finally {
       try {
         clearCache();
       } finally {
         systemResolver = null;
         systemModelCacheMap.clear();
         enlistedResolverMap.clear();
         if (error != null) {
           throw new QueryException("Error force-closing answers", error);
         }
       }
     }
   }
 
   public SystemResolver getSystemResolver() {
     return systemResolver;
   }
 
 
   /**
    * Clear the cache of temporary models.
    */
   private void clearCache() {
     // Clear the temporary models
     if (!cachedModelSet.isEmpty()) {
       try {
         Resolver temporaryResolver =
           temporaryResolverFactory.newResolver(true, systemResolver, systemResolver);
         for (Iterator<LocalNode> i = cachedModelSet.iterator(); i.hasNext();) {
           LocalNode modelLocalNode = i.next();
           long model = modelLocalNode.getValue();
 
           if (changedCachedModelSet.contains(modelLocalNode)) {
             // Write back the modifications to the original model
             try {
               Resolver resolver =
                 findResolverFactory(model).newResolver(true, systemResolver, systemResolver);
               Variable s = new Variable("s");
               Variable p = new Variable("p");
               Variable o = new Variable("o");
               resolver.modifyModel(model,
                 new TuplesWrapperStatements(temporaryResolver.resolve(
                     new ConstraintImpl(s, p, o, modelLocalNode)), s, p, o),
                 true  // insert the content
               );
             } catch (Exception e) {
               logger.error("Failed to write back cached model " + model +
                            " after transaction", e);
             }
             changedCachedModelSet.remove(modelLocalNode);
           }
 
           // Remove the cached model
           try {
             temporaryResolver.removeModel(model);
           } catch (Exception e) {
             logger.error("Failed to clear cached model " + model + " after transaction", e);
           }
           i.remove();
         }
       } catch (Exception e) {
         logger.error("Failed to clear cached models after transaction", e);
       }
     }
   }
 
   public void initiate(MulgaraTransaction transaction) throws QueryException {
     try {
       this.transaction = transaction;
       this.systemResolver = systemResolverFactory.newResolver(isWriting);
       transaction.enlist(systemResolver);
     } catch (Exception e) {
       throw new QueryException("Unable to enlist systemResolver:" + 
           systemResolver + " into transaction", e);
     }
   }
 }
