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
  *   Various modifications to this file copyright:
  *     The Australian Commonwealth Government
  *     Department of Defense
  *   Developed by Netymon Pty Ltd
  *   under contract 4500430665
  *   contributed to the Kowari Project under the
  *     Mozilla Public License version 1.1
  *   per clause 4.1.3 of the above contract.
  *
  *   Various modifications to this file copyright:
 *     2005-2006 Netymon Pty Ltd: mail@netymon.com
  *
  *   Various modifications to this file copyright:
 *     2005-2006 Andrae Muys: andrae@muys.id.au
  *
  *   getModel() contributed by Netymon Pty Ltd on behalf of
  *   The Australian Commonwealth Government under contract 4500507038.
  *   ConstraintLocalization contributed by Netymon Pty Ltd on behalf of
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
 import java.util.*;
 
 // Third party packages
 import org.apache.log4j.Logger;
 import org.jrdf.graph.Node;
 import org.jrdf.graph.URIReference;
 
 // Local packages
 import org.mulgara.query.*;
 import org.mulgara.query.rdf.BlankNodeImpl;
 import org.mulgara.query.rdf.LiteralImpl;
 import org.mulgara.query.rdf.URIReferenceImpl;
 import org.mulgara.resolver.spi.ConstraintBindingHandler;
 import org.mulgara.resolver.spi.ConstraintLocalization;
 import org.mulgara.resolver.spi.ConstraintModelRewrite;
 import org.mulgara.resolver.spi.ConstraintResolutionHandler;
 import org.mulgara.resolver.spi.GlobalizeException;
 import org.mulgara.resolver.spi.LocalizeException;
 import org.mulgara.resolver.spi.ModelResolutionHandler;
 import org.mulgara.resolver.spi.QueryEvaluationContext;
 import org.mulgara.resolver.spi.ResolverSession;
 import org.mulgara.store.tuples.RestrictPredicateFactory;
 import org.mulgara.store.tuples.Tuples;
 import org.mulgara.store.tuples.TuplesOperations;
 import org.mulgara.util.NVPair;
 
 /**
  * Localized version of a global {@link Query}.
  *
  * As well as providing coordinate transformation from global to local
  * coordinates, this adds methods to partially resolve the query.
  *
  * @created 2004-05-06
  * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
  * @version $Revision: 1.12 $
  * @modified $Date: 2005/05/16 11:07:07 $
  * @maintenanceAuthor $Author: amuys $
  * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
  * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
  *   Technology, Inc</a>
  * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
  */
 class LocalQueryResolver implements QueryEvaluationContext
 {
   /** Logger.  */
   private static final Logger logger = Logger.getLogger(LocalQueryResolver.class.getName());
 
   private LocalQuery localQuery;
 
   private ResolverSession resolverSession;
 
   //
   // Constructor
   //
 
   /**
    * Construct a database.
    *
    * @param localQuery  the query to localize
    * @param resolverSession  the database session to localize the
    *   <var>localQuery</var> against
    * @throws IllegalArgumentException if <var>query</var> or
    *   <var>resolverSession</var> are <code>null</code>
    * @throws LocalizeException if the <var>query</var> can't be localized
    */
   LocalQueryResolver(LocalQuery localQuery, ResolverSession resolverSession) {
     // Validate "query" parameter
     if (localQuery == null) {
       throw new IllegalArgumentException("Null 'localQuery' parameter");
     }
 
     // Initialize fields
     this.localQuery = localQuery;
     this.resolverSession = resolverSession;
   }
 
 
   static {
     ConstraintOperations.addModelResolutionHandlers(new NVPair[]
       {
         new NVPair(ModelUnion.class, new ModelResolutionHandler() {
           public Tuples resolve(QueryEvaluationContext context, ModelExpression modelExpr,
                                 Constraint constraint) throws Exception {
             return TuplesOperations.append(
                 ConstraintOperations.resolveModelExpression(context, ((ModelOperation)modelExpr).getLHS(), constraint),
                 ConstraintOperations.resolveModelExpression(context, ((ModelOperation)modelExpr).getRHS(), constraint));
           }
         }),
         new NVPair(ModelIntersection.class, new ModelResolutionHandler() {
           public Tuples resolve(QueryEvaluationContext context, ModelExpression modelExpr,
                                 Constraint constraint) throws Exception {
             return TuplesOperations.join(
                 ConstraintOperations.resolveModelExpression(context, ((ModelOperation)modelExpr).getLHS(), constraint),
                 ConstraintOperations.resolveModelExpression(context, ((ModelOperation)modelExpr).getRHS(), constraint));
           }
         }),
         new NVPair(ModelResource.class, new ModelResolutionHandler() {
           public Tuples resolve(QueryEvaluationContext context, ModelExpression modelExpr,
                                 Constraint constraint) throws Exception {
             return context.resolve((ModelResource)modelExpr, (Constraint)constraint);
           }
         })
       });
   }
 
 
   static {
     ConstraintOperations.addConstraintResolutionHandlers(new NVPair[]
       {
         new NVPair(ConstraintConjunction.class, new ConstraintResolutionHandler() {
           public Tuples resolve(QueryEvaluationContext context, ModelExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
             return TuplesOperations.join(
               context.resolveConstraintOperation(modelExpr, (ConstraintOperation)constraintExpr));
           }
         }),
         new NVPair(ConstraintDisjunction.class, new ConstraintResolutionHandler() {
           public Tuples resolve(QueryEvaluationContext context, ModelExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
             return TuplesOperations.append(
                 context.resolveConstraintOperation(modelExpr, (ConstraintOperation)constraintExpr));
           }
         }),
         new NVPair(ConstraintDifference.class, new ConstraintResolutionHandler() {
           public Tuples resolve(QueryEvaluationContext context, ModelExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
             List args = context.resolveConstraintOperation(modelExpr, (ConstraintOperation)constraintExpr);
             assert args.size() == 2;
             return TuplesOperations.subtract((Tuples)args.get(0), (Tuples)args.get(1));
           }
         }),
         new NVPair(ConstraintIs.class, new ConstraintResolutionHandler() {
           public Tuples resolve(QueryEvaluationContext context, ModelExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
             ConstraintIs constraint = (ConstraintIs)constraintExpr;
             return TuplesOperations.assign((Variable)context.localize(constraint.getVariable()),
                                            ((LocalNode)context.localize(constraint.getValueNode())).getValue());
           }
         }),
         new NVPair(ConstraintImpl.class, new ConstraintResolutionHandler() {
           public Tuples resolve(QueryEvaluationContext context, ModelExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
             ConstraintElement constraintElem =
               ((ConstraintImpl) constraintExpr).getModel();
             assert constraintElem != null;
             if (constraintElem.equals(Variable.FROM)) {
               return ConstraintOperations.resolveModelExpression(context, modelExpr, (Constraint)constraintExpr);
             }
             else if (constraintElem instanceof URIReference) {
               return ConstraintOperations.resolveModelExpression(context, new ModelResource(((URIReference)constraintElem).getURI()), (Constraint)constraintExpr);
             }
             else if (constraintElem instanceof LocalNode) {
               return context.resolve(null, (ConstraintImpl)constraintExpr);
             }
             else if (constraintElem instanceof Variable) {
               return context.resolve(null, (ConstraintImpl)constraintExpr);
             }
             else {
               throw new QueryException("Specified model not a URIReference/Variable: " + constraintElem +" is a " + constraintElem.getClass().getName() );
             }
           }
         }),
         new NVPair(ConstraintNegation.class, new ConstraintResolutionHandler() {
           public Tuples resolve(QueryEvaluationContext context, ModelExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
             if (((ConstraintNegation)constraintExpr).getModel().equals(Variable.FROM)) {
               return ConstraintOperations.resolveModelExpression(context, modelExpr, (Constraint)constraintExpr);
             } else {
               ConstraintElement constraintElem = ((ConstraintNegation)constraintExpr).getElement(3);
               if (constraintElem instanceof URIReference) {
                 return ConstraintOperations.resolveModelExpression(context, new ModelResource(((URIReference)constraintElem).getURI()), (Constraint)constraintExpr);
               } else {
                 throw new QueryException("Specified model not a URIReference: " + constraintElem +" is a " + constraintElem.getClass().getName() );
               }
             }
           }
         }),
         new NVPair(WalkConstraint.class, new ConstraintResolutionHandler() {
           public Tuples resolve(QueryEvaluationContext context, ModelExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
             return WalkFunction.walk(context, (WalkConstraint)constraintExpr, modelExpr, context.getResolverSession());
           }
         }),
         new NVPair(SingleTransitiveConstraint.class, new ConstraintResolutionHandler() {
           public Tuples resolve(QueryEvaluationContext context, ModelExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
             SingleTransitiveConstraint constraint = (SingleTransitiveConstraint)constraintExpr;
             if (constraint.isAnchored()) {
               return DirectTransitiveFunction.infer(context, constraint, modelExpr, context.getResolverSession());
             } else {
               return ExhaustiveTransitiveFunction.infer(context, constraint, modelExpr, context.getResolverSession());
             }
           }
         }),
         new NVPair(TransitiveConstraint.class, new ConstraintResolutionHandler() {
           public Tuples resolve(QueryEvaluationContext context, ModelExpression modelExpr, ConstraintExpression constraintExpr) throws Exception {
             return ExhaustiveTransitiveFunction.infer(context, (TransitiveConstraint)constraintExpr, modelExpr, context.getResolverSession());
           }
         }),
       });
   }
 
   static {
     ConstraintOperations.addConstraintBindingHandlers(new NVPair[]
       {
         new NVPair(ConstraintTrue.class, new ConstraintBindingHandler() {
           public ConstraintExpression bindVariables(Map bindings, ConstraintExpression constraintExpr) throws Exception {
             return constraintExpr;
           }
         }),
         new NVPair(ConstraintFalse.class, new ConstraintBindingHandler() {
           public ConstraintExpression bindVariables(Map bindings, ConstraintExpression constraintExpr) throws Exception {
             return constraintExpr;
           }
         }),
         new NVPair(ConstraintImpl.class, new ConstraintBindingHandler() {
           public ConstraintExpression bindVariables(Map bindings, ConstraintExpression constraintExpr) throws Exception {
             return ConstraintOperations.replace(bindings, (Constraint)constraintExpr);
           }
         }),
         new NVPair(ConstraintIs.class, new ConstraintBindingHandler() {
           public ConstraintExpression bindVariables(Map bindings, ConstraintExpression constraintExpr) throws Exception {
             return ConstraintOperations.replace(bindings, (Constraint)constraintExpr);
           }
         }),
         new NVPair(SingleTransitiveConstraint.class, new ConstraintBindingHandler() {
           public ConstraintExpression bindVariables(Map bindings, ConstraintExpression constraintExpr) throws Exception {
             return new SingleTransitiveConstraint(ConstraintOperations.replace(bindings, (Constraint)constraintExpr));
           }
         }),
         new NVPair(TransitiveConstraint.class, new ConstraintBindingHandler() {
           public ConstraintExpression bindVariables(Map bindings, ConstraintExpression constraintExpr) throws Exception {
             TransitiveConstraint tc = (TransitiveConstraint)constraintExpr;
             return new TransitiveConstraint(ConstraintOperations.replace(bindings, tc.getAnchoredConstraint()),
                                             ConstraintOperations.replace(bindings, tc.getUnanchoredConstraint()));
           }
         }),
         new NVPair(WalkConstraint.class, new ConstraintBindingHandler() {
           public ConstraintExpression bindVariables(Map bindings, ConstraintExpression constraintExpr) throws Exception {
             WalkConstraint wc = (WalkConstraint)constraintExpr;
             return new WalkConstraint(ConstraintOperations.replace(bindings, wc.getAnchoredConstraint()),
                                       ConstraintOperations.replace(bindings, wc.getUnanchoredConstraint()));
           }
         }),
         new NVPair(ConstraintNegation.class, new ConstraintBindingHandler() {
           public ConstraintExpression bindVariables(Map bindings, ConstraintExpression constraintExpr) throws Exception {
             return new ConstraintNegation(ConstraintOperations.replace(bindings, (Constraint)constraintExpr));
           }
         }),
         new NVPair(ConstraintConjunction.class, new ConstraintBindingHandler() {
           public ConstraintExpression bindVariables(Map bindings, ConstraintExpression constraintExpr) throws Exception {
             return new ConstraintConjunction(ConstraintOperations.replaceOperationArgs(bindings, (ConstraintOperation)constraintExpr));
           }
         }),
         new NVPair(ConstraintDisjunction.class, new ConstraintBindingHandler() {
           public ConstraintExpression bindVariables(Map bindings, ConstraintExpression constraintExpr) throws Exception {
             return new ConstraintDisjunction(ConstraintOperations.replaceOperationArgs(bindings, (ConstraintOperation)constraintExpr));
           }
         }),
         new NVPair(ConstraintDifference.class, new ConstraintBindingHandler() {
           public ConstraintExpression bindVariables(Map bindings, ConstraintExpression constraintExpr) throws Exception {
             List args = ConstraintOperations.replaceOperationArgs(bindings, (ConstraintOperation)constraintExpr);
             return new ConstraintDifference((ConstraintExpression)args.get(0), (ConstraintExpression)args.get(1));
           }
         }),
       });
   }
 
 
   static {
     ConstraintOperations.addConstraintModelRewrites(new NVPair[]
       {
         new NVPair(ConstraintImpl.class, new ConstraintModelRewrite() {
           public Constraint rewrite(ConstraintElement newModel, Constraint constraint) throws Exception {
             return new ConstraintImpl(constraint.getElement(0), constraint.getElement(1), constraint.getElement(2), newModel);
           }
         }),
         new NVPair(ConstraintNegation.class, new ConstraintModelRewrite() {
           public Constraint rewrite(ConstraintElement newModel, Constraint constraint) throws Exception {
             return new ConstraintNegation(new ConstraintImpl(constraint.getElement(0), constraint.getElement(1), constraint.getElement(2), newModel));
           }
         }),
       });
   }
 
   static {
     ConstraintOperations.addConstraintLocalizations(new NVPair[]
       {
         new NVPair(ConstraintImpl.class, new ConstraintLocalization() {
           public Constraint localize(QueryEvaluationContext context, Constraint constraint) throws Exception {
             return new ConstraintImpl(context.localize(constraint.getElement(0)),
                 context.localize(constraint.getElement(1)),
                 context.localize(constraint.getElement(2)),
                 context.localize(constraint.getElement(3)));
           }
         }),
         new NVPair(ConstraintNegation.class, new ConstraintLocalization() {
           public Constraint localize(QueryEvaluationContext context, Constraint constraint) throws Exception {
             return new ConstraintNegation(new ConstraintImpl(
                 context.localize(constraint.getElement(0)),
                 context.localize(constraint.getElement(1)),
                 context.localize(constraint.getElement(2)),
                 context.localize(constraint.getElement(3))));
           }
         }),
       });
   }
 
   public List resolveConstraintOperation(ModelExpression modelExpr,
                                          ConstraintOperation constraintOper)
       throws QueryException
   {
     List result = new ArrayList();
     Iterator i = constraintOper.getElements().iterator();
     while (i.hasNext()) {
       result.add(ConstraintOperations.resolveConstraintExpression(this, modelExpr, (ConstraintExpression)i.next()));
     }
 
     return result;
   }
 
 
   /**
    * Returns either a variable or the LocalNode local equivalent of the
    * ConstraintElement.
    *
    * @param constraintElement  a global constraint element
    * @return the localized equivalent to the global <var>constraintElement</var>
    */
   public ConstraintElement localize(ConstraintElement constraintElement)
     throws LocalizeException
   {
     if (constraintElement instanceof Node) {
       return new LocalNode(resolverSession.localize((Node)constraintElement));
     } else if (constraintElement instanceof Variable) {
       return constraintElement;
     } else if (constraintElement instanceof LocalNode) {
       return (LocalNode)constraintElement;
     } else {
       throw new IllegalArgumentException("Not a global constraint element: " + constraintElement +
                                          "::" + constraintElement.getClass());
     }
   }
 
   public ConstraintElement globalize(ConstraintElement constraintElement)
     throws GlobalizeException
   {
     Node node;
     if (constraintElement instanceof LocalNode) {
       node = resolverSession.globalize(((LocalNode)constraintElement).getValue());
       if (node instanceof URIReferenceImpl ||
           node instanceof LiteralImpl ||
           node instanceof BlankNodeImpl) {
         return (Value)node;
       } else {
         throw new GlobalizeException(((LocalNode)constraintElement).getValue(),
             "Globalize of non-internal Nodes not supported by LocalQueryResolver: " + constraintElement + " -> " + node);
       }
     } else {
       return constraintElement;  // Either Variable or GlobalNode
     }
   }
 
 
   /**
    * Localize and resolve the leaf node of the <code>FROM</code> and
    * <code>WHERE</code> clause product.
    *
    * @param modelResource  the <code>FROM<code> clause to resolve, never
    *   <code>null</codE>
    * @param constraint  the <code>WHERE</code> clause to resolve, which must
    *   have {@link Variable#FROM} as its fourth element, and never be
    *   <code>null</code>
    * @throws QueryException if resolution can't be obtained
    */
   public Tuples resolve(ModelResource modelResource, Constraint constraint) throws QueryException
   {
     assert modelResource != null || !constraint.getModel().equals(Variable.FROM);
     assert constraint != null;
 
     // Delegate constraint resolution back to the database session
     try {
       Constraint localized = ConstraintOperations.localize(this, constraint);
 
       if (localized.getModel().equals(Variable.FROM)) {
         localized = ConstraintOperations.rewriteConstraintModel(
             localize(new URIReferenceImpl(modelResource.getURI())), localized);
       }
 
       Tuples result = localQuery.resolve(localized);
 
       return result;
     } catch (LocalizeException e) {
       throw new QueryException("Unable to resolve FROM " + modelResource +
                                " WHERE " + constraint, e);
     } catch (QueryException eq) {
       throw new QueryException("Error resolving " + constraint + " from " + modelResource, eq);
     } catch (Exception e) {
       throw new QueryException("Unexpected error resolving " + constraint + " from " + modelResource, e);
     }
   }
 
 
   public Tuples resolve(ModelExpression modelExpression, ConstraintExpression constraintExpression) throws QueryException {
     return localQuery.resolve(modelExpression, constraintExpression);
   }
 
 
   public ResolverSession getResolverSession() {
     return resolverSession;
   }
 }
