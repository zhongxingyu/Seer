 // net.vtst.ow.eclipse.less: An Eclipse module for LESS (http://lesscss.org)
 // (c) Vincent Simonet, 2011.  All rights reserved.
 
 package net.vtst.ow.eclipse.less.scoping;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.vtst.ow.eclipse.less.less.AtVariableDef;
 import net.vtst.ow.eclipse.less.less.AtVariableRefTarget;
 import net.vtst.ow.eclipse.less.less.Block;
 import net.vtst.ow.eclipse.less.less.BlockUtils;
 import net.vtst.ow.eclipse.less.less.HashOrClassRef;
 import net.vtst.ow.eclipse.less.less.ImportStatement;
 import net.vtst.ow.eclipse.less.less.LessPackage;
 import net.vtst.ow.eclipse.less.less.Mixin;
 import net.vtst.ow.eclipse.less.less.MixinParameter;
 import net.vtst.ow.eclipse.less.less.MixinSelectors;
 import net.vtst.ow.eclipse.less.less.MixinUtils;
 import net.vtst.ow.eclipse.less.less.StyleSheet;
 import net.vtst.ow.eclipse.less.less.TerminatedMixin;
 import net.vtst.ow.eclipse.less.less.ToplevelStatement;
 import net.vtst.ow.eclipse.less.less.VariableDefinition;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.xtext.naming.QualifiedName;
 import org.eclipse.xtext.resource.EObjectDescription;
 import org.eclipse.xtext.resource.IEObjectDescription;
 import org.eclipse.xtext.scoping.IScope;
 import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider;
 import org.eclipse.xtext.scoping.impl.MapBasedScope;
 import org.eclipse.xtext.util.IResourceScopeCache;
 import org.eclipse.xtext.util.Tuples;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 public class LessScopeProvider extends AbstractDeclarativeScopeProvider {
   
   private static final String ARGUMENTS_VARIABLE_NAME = "@arguments";
 
   // The cache contains pairs (LessScopeProvider.class, context) for variable scopes
   // and triples (LessScopeProvider.HashOrClassCrossReferenceclass, context, prefix) for mixin scopes.
   @Inject
   private IResourceScopeCache cache;
   
   @Inject
   private LessImportStatementResolver importStatementResolver;
   
   @Inject
   private LessMixinScopeProvider mixinScopeProvider;
       
   private Iterable<EObject> getStyleSheetStatements(StyleSheet styleSheet) {
     return styleSheet.eContents();
   }
 
   // **************************************************************************
   // Scoping of variables
 
   /** Entry point for the calculation of the scope of a cross-reference to
    * a VariableDefinitionIdent.
    */
   IScope scope_AtVariableRefTarget(EObject context, EReference ref) {
     if (MixinUtils.isBoundByMixinDefinitionParameter(context)) return IScope.NULLSCOPE;
     return computeVariableScope(context, ref);
   }
 
   /** Entry point for the calculation of the scope of a cross-reference to
    * a VariableDefinitionIdent.
    */
   // TODO: Delete
   IScope scope_VariableCrossReference(EObject context, EReference ref) {
     return computeVariableScope(context, ref);
   }
   
   /** Compute the scope of a context.  If the given context is a Block or a StyleSheet, call
    * computeVariableScopeOfStatements in order to lookup on the variables defined in this scope.
    * Otherwise, call the function on the container.
    * Results for Block and StyleSheet are cached.
    */
   public IScope computeVariableScope(final EObject context, EReference ref) {
     EObject container = context.eContainer();
     if (container == null) {
       return IScope.NULLSCOPE;
     } else if (container instanceof Block) {
       return computeVariableScopeOfStatements(container, BlockUtils.iterator((Block) container), ref);
     } else if (container instanceof StyleSheet) {
       return computeVariableScopeOfStatements(container, getStyleSheetStatements((StyleSheet) container), ref);
     } else if (container instanceof TerminatedMixin) {
       EStructuralFeature containingFeature = context.eContainingFeature();
       if (containingFeature.equals(LessPackage.eINSTANCE.getTerminatedMixin_Guards()) ||
           containingFeature.equals(LessPackage.eINSTANCE.getTerminatedMixin_Body())) {
         return computeVariableScopeOfMixinDefinition((TerminatedMixin) container, ref);
       }
     }
     return computeVariableScope(container, ref);
   }
     
   /** Compute the scope of a context, which contains the statements returned by iterable.
    */
   public IScope computeVariableScopeOfStatements(final EObject context, final Iterable<EObject> statements, final EReference ref) {
     return cache.get(Tuples.pair(LessScopeProvider.class, context), context.eResource(), new Provider<IScope>() {
       public IScope get() {
         List<IEObjectDescription> variableDefinitions = new ArrayList<IEObjectDescription>();
         // Go through the variables bound by the statements
         addVariableDefinitions(statements, variableDefinitions);
         return MapBasedScope.createScope(computeVariableScope(context, ref), variableDefinitions);
       }
     });
   }
   
   /**
    * Compute the scope of a mixin definition, binding the parameters of the definition.
    */
   public IScope computeVariableScopeOfMixinDefinition(final TerminatedMixin context, final EReference ref) {
     return cache.get(Tuples.pair(LessScopeProvider.class, context), context.eResource(), new Provider<IScope>() {
       public IScope get() {
         List<IEObjectDescription> variableDefinitions = new ArrayList<IEObjectDescription>();
         // Go through the variables bound by the container
         addVariableDefinitions(context, variableDefinitions);
         return MapBasedScope.createScope(computeVariableScope(context, ref), variableDefinitions);
       }
     });    
   }
   
   /** Add the variables defined by a set of statements.
    */
   private void addVariableDefinitions(Iterable<? extends EObject> statements, List<IEObjectDescription> variableDefinitions) {
     for (EObject statement: statements) {
       if (statement instanceof VariableDefinition) {
         variableDefinitions.add(getEObjectDescriptionFor(((VariableDefinition) statement).getLhs().getVariable()));
       } else if (statement instanceof ImportStatement) {
         Iterable<ToplevelStatement> importedStatements = importStatementResolver.getAllStatements((ImportStatement) statement);
         addVariableDefinitions(importedStatements, variableDefinitions);
       }
     }    
   }
   
   /** Add the variables defined by a mixin.
    */
   private void addVariableDefinitions(TerminatedMixin mixinDefinition, List<IEObjectDescription> variableDefinitions) {
     for (MixinParameter parameter: mixinDefinition.getParameters().getParameter()) {
       AtVariableRefTarget variable = MixinUtils.getVariableBoundByMixinParameter(parameter);
       if (variable != null) variableDefinitions.add(getEObjectDescriptionFor(variable));
     }
     variableDefinitions.add(EObjectDescription.create(QualifiedName.create(ARGUMENTS_VARIABLE_NAME), mixinDefinition));
   }
   
   /** Create the object description for a variable definition ident.
    */
   private IEObjectDescription getEObjectDescriptionFor(AtVariableRefTarget atVariable) {
     return EObjectDescription.create(QualifiedName.create(MixinUtils.getIdent(atVariable)), atVariable);
   }
 
   /** Create the object description for a variable definition ident.
    */
   private IEObjectDescription getEObjectDescriptionFor(AtVariableDef atVariable) {
     return EObjectDescription.create(QualifiedName.create(atVariable.getIdent()), atVariable);
   }
 
   
   // **************************************************************************
   // Scoping of mixins
   
   // Let's consider a call s_1 ... s_n and a definition d_1 ... d_m
   // s matches d if and only if one of the following condition holds:
   // * s_1 ... s_n is a subword of d_1 ... d_m
   // * m = 1 and s_1 = d_1 and s_2 ... s_n matches an element of d's block
   // Combinators are not considered.
   
   // TODO: Check that caching works.
   // TODO: Fix auto-complete.
   /** Entry point for the calculation of the scope of a cross-reference to
    * a HashOrClass.
    */
   IScope scope_HashOrClassRefTarget(EObject context, EReference ref) {
     // if (MixinUtils.isBoundByMixinDefinitionSelector(context)) return IScope.NULLSCOPE;
     EObject container = context.eContainer();
    if (!(container instanceof MixinSelectors)) return IScope.NULLSCOPE;
     MixinSelectors mixinSelectors = (MixinSelectors) container;
     EObject container2 = mixinSelectors.eContainer();
    if (!(container2 instanceof Mixin)) return IScope.NULLSCOPE;
     Mixin mixin = (Mixin) container2;
    int position = getHashOrClsasRefTargetPosition(mixinSelectors, context);
    if (position < 0) return IScope.NULLSCOPE;
     MixinScope scope = mixinScopeProvider.getScope(mixin);
     return scope.getScope(position);
   }
 
  private int getHashOrClsasRefTargetPosition(MixinSelectors mixinSelectors, EObject current) {
     int index = 0;
     for (HashOrClassRef item: mixinSelectors.getSelector()) {
       if (item == current) return index;
       ++index;
     }
     return -1;
   }
 }
