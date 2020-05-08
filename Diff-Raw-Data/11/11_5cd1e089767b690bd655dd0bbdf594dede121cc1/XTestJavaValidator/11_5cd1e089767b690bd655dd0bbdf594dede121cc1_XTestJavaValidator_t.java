 package org.xtest.validation;
 
 import static com.google.common.collect.Maps.newHashMap;
 import static org.eclipse.xtext.xbase.validation.IssueCodes.INCOMPATIBLE_RETURN_TYPE;
 import static org.eclipse.xtext.xbase.validation.IssueCodes.INVALID_USE_OF_TYPE;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xtend.core.validation.IssueCodes;
 import org.eclipse.xtend.core.validation.TypeErasedSignature;
 import org.eclipse.xtend.core.xtend.XtendImport;
 import org.eclipse.xtend.core.xtend.XtendPackage;
 import org.eclipse.xtend.core.xtend.XtendParameter;
 import org.eclipse.xtext.CrossReference;
 import org.eclipse.xtext.common.types.JvmField;
 import org.eclipse.xtext.common.types.JvmGenericType;
 import org.eclipse.xtext.common.types.JvmIdentifiableElement;
 import org.eclipse.xtext.common.types.JvmOperation;
 import org.eclipse.xtext.common.types.JvmType;
 import org.eclipse.xtext.common.types.JvmTypeReference;
 import org.eclipse.xtext.common.types.TypesPackage;
 import org.eclipse.xtext.common.types.util.TypeConformanceComputer;
 import org.eclipse.xtext.common.types.util.TypeReferences;
 import org.eclipse.xtext.naming.QualifiedName;
 import org.eclipse.xtext.nodemodel.ICompositeNode;
 import org.eclipse.xtext.nodemodel.INode;
 import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
 import org.eclipse.xtext.resource.IEObjectDescription;
 import org.eclipse.xtext.scoping.IScope;
 import org.eclipse.xtext.util.CancelIndicator;
 import org.eclipse.xtext.validation.CancelableDiagnostician;
 import org.eclipse.xtext.validation.Check;
 import org.eclipse.xtext.validation.CheckType;
 import org.eclipse.xtext.validation.ValidationMessageAcceptor;
 import org.eclipse.xtext.xbase.XAssignment;
 import org.eclipse.xtext.xbase.XBlockExpression;
 import org.eclipse.xtext.xbase.XExpression;
 import org.eclipse.xtext.xbase.XbasePackage;
 import org.eclipse.xtext.xbase.typing.ITypeProvider;
 import org.xtest.RunType;
 import org.xtest.XTestEvaluationException;
 import org.xtest.XTestRunner;
 import org.xtest.XTestRunner.DontRunCheck;
 import org.xtest.jvmmodel.XtestJvmModelAssociator;
 import org.xtest.preferences.PerFilePreferenceProvider;
 import org.xtest.preferences.RuntimePref;
 import org.xtest.results.XTestResult;
 import org.xtest.xTest.Body;
 import org.xtest.xTest.XAssertExpression;
 import org.xtest.xTest.XMethodDef;
 import org.xtest.xTest.XTestPackage;
 
 import com.google.common.base.Function;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Multiset;
 import com.google.common.collect.TreeMultiset;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 /**
  * Validator for xtest expression models. Validates that:
  * <ul>
  * <li>assert expressions have boolean return type
  * <li>assert/throws types are subclasses are throwable
  * <li><b>All unit tests pass</b> </ol>
  * 
  * @author Michael Barry
  */
 @Singleton
 @SuppressWarnings("restriction")
 public class XTestJavaValidator extends AbstractXTestJavaValidator {
     private static final int TEST_RUN_FAILURE_INDEX = Integer.MAX_VALUE;
     @Inject
     private XtestJvmModelAssociator associations;
     private final ThreadLocal<CancelIndicator> cancelIndicators = new ThreadLocal<CancelIndicator>();
     @Inject
     private PerFilePreferenceProvider preferenceProvider;
     @Inject
     private XTestRunner runner;
     @Inject
     private TypeErasedSignature.Provider signatureProvider;
     @Inject
     private TypeConformanceComputer typeConformanceComputer;
     @Inject
     private ITypeProvider typeProvider;
     @Inject
     private TypeReferences typeReferences;
 
     private final XtendValidations xtendUtils;
 
     /**
      * FOR GUICE USE ONLY
      * 
      * @param xtendUtil
      *            Xtend validator delegate to hook up to this validators message acceptor
      */
     @Inject
     public XTestJavaValidator(XtendValidations xtendUtil) {
         this.xtendUtils = xtendUtil;
         xtendUtils.setDelagate(this);
     }
 
     /**
      * Verifies that the "throws" type is a subclass of throwable or that the assert expression has
      * boolean return type.
      * 
      * @param assertExpression
      *            The assert expression to check
      */
     @Check
     public void checkAssertExpression(XAssertExpression assertExpression) {
         JvmTypeReference throws1 = assertExpression.getThrows();
         if (throws1 != null) {
             JvmTypeReference expected = typeReferences.getTypeForName(Throwable.class,
                     assertExpression);
             if (!typeConformanceComputer.isConformant(expected, throws1)) {
                 error("Throws expression must be a subclass of Throwable",
                         XTestPackage.Literals.XASSERT_EXPRESSION__THROWS);
             }
         } else {
             XExpression actual = assertExpression.getActual();
             if (actual != null) {
                 JvmTypeReference returnType = typeProvider.getCommonReturnType(actual, true);
                 JvmTypeReference expected = typeReferences.getTypeForName(Boolean.class,
                         assertExpression);
 
                 if (!typeConformanceComputer.isConformant(expected, returnType)) {
                     error("Assert expression must return a boolean",
                             XTestPackage.Literals.XASSERT_EXPRESSION__ACTUAL);
                 }
             }
         }
     }
 
     @Override
     @Check
     public void checkAssignment(XAssignment assignment) {
         JvmIdentifiableElement assignmentFeature = assignment.getFeature();
         if (!(assignmentFeature instanceof JvmField && ((JvmField) assignmentFeature).isFinal())) {
             super.checkAssignment(assignment);
         }
     }
 
     /**
      * Validate imports for an Xtest file
      * 
      * From Xtend 2.3
      * 
      * @param file
      *            The Xtest file
      */
     @Check
     public void checkImports(Body file) {
         final Map<JvmType, XtendImport> imports = newHashMap();
         final Map<JvmType, XtendImport> staticImports = newHashMap();
         final Map<String, JvmType> importedNames = newHashMap();
 
         for (XtendImport imp : file.getImports()) {
             if (imp.getImportedNamespace() != null) {
                 warning("The use of wildcard imports is deprecated.", imp, null,
                         IssueCodes.IMPORT_WILDCARD_DEPRECATED);
             } else {
                 JvmType importedType = imp.getImportedType();
                 if (importedType != null && !importedType.eIsProxy()) {
                     Map<JvmType, XtendImport> map = imp.isStatic() ? staticImports : imports;
                     if (map.containsKey(importedType)) {
                         warning("Duplicate import of '" + importedType.getSimpleName() + "'.", imp,
                                 null, IssueCodes.IMPORT_DUPLICATE);
                     } else {
                         map.put(importedType, imp);
                         if (!imp.isStatic()) {
                             JvmType currentType = importedType;
                             String currentSuffix = currentType.getSimpleName();
                             JvmType collidingImport = importedNames
                                     .put(currentSuffix, importedType);
                             if (collidingImport != null) {
                                 error("The import '" + importedType.getIdentifier()
                                         + "' collides with the import '"
                                         + collidingImport.getIdentifier() + "'.", imp, null,
                                         IssueCodes.IMPORT_COLLISION);
                             }
                             while (currentType.eContainer() instanceof JvmType) {
                                 currentType = (JvmType) currentType.eContainer();
                                 currentSuffix = currentType.getSimpleName() + "$" + currentSuffix;
                                 JvmType collidingImport2 = importedNames.put(currentSuffix,
                                         importedType);
                                 if (collidingImport2 != null) {
                                     error("The import '" + importedType.getIdentifier()
                                             + "' collides with the import '"
                                             + collidingImport2.getIdentifier() + "'.", imp, null,
                                             IssueCodes.IMPORT_COLLISION);
                                 }
                             }
                         }
                     }
                 }
             }
         }
         EList<XExpression> expressions = file.getExpressions();
         for (XExpression expression : expressions) {
             ICompositeNode node = NodeModelUtils.findActualNodeFor(expression);
             if (node != null) {
                 for (INode n : node.getAsTreeIterable()) {
                     if (n.getGrammarElement() instanceof CrossReference) {
                         EClassifier classifier = ((CrossReference) n.getGrammarElement()).getType()
                                 .getClassifier();
                         if (classifier instanceof EClass
                                 && (TypesPackage.Literals.JVM_TYPE
                                         .isSuperTypeOf((EClass) classifier) || TypesPackage.Literals.JVM_CONSTRUCTOR
                                         .isSuperTypeOf((EClass) classifier))) {
                             String simpleName = n.getText().trim();
                             // handle StaticQualifier Workaround (see Xbase grammar)
                             if (simpleName.endsWith("::")) {
                                 simpleName = simpleName.substring(0, simpleName.length() - 2);
                             }
                             if (importedNames.containsKey(simpleName)) {
                                 JvmType type = importedNames.remove(simpleName);
                                 imports.remove(type);
                             } else {
                                 while (simpleName.contains("$")) {
                                     simpleName = simpleName.substring(0,
                                             simpleName.lastIndexOf('$'));
                                     if (importedNames.containsKey(simpleName)) {
                                         imports.remove(importedNames.remove(simpleName));
                                         break;
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         for (XtendImport imp : imports.values()) {
             warning("The import '" + imp.getImportedType().getQualifiedName() + "' is never used.",
                     imp, null, IssueCodes.IMPORT_UNUSED);
         }
     }
 
     /**
      * Checks that method parameter names don't collide with eachother or other local variables
      * 
      * @param def
      *            The method def
      */
     @Check
     public void checkMethodNameDoesntShadowVariable(XMethodDef def) {
         if (!def.isStatic()) {
             checkDeclaredVariableName(def, def, XtendPackage.Literals.XTEND_FUNCTION__NAME);
         }
     }
 
     /**
      * Checks that method parameter names don't collide with eachother or other local variables
      * 
      * @param def
      *            The method def
      */
     @Check
     public void checkMethodParametersUnique(XMethodDef def) {
         if (!def.isStatic()) {
             for (XtendParameter parameter : def.getParameters()) {
                 checkDeclaredVariableName(def, parameter,
                         XtendPackage.Literals.XTEND_PARAMETER__NAME);
             }
         }
         checkParameterNames(def.getParameters(), XtendPackage.Literals.XTEND_PARAMETER__NAME);
     }
 
     /**
      * Checks that method parameter types are not void
      * 
      * @param param
      *            The method def parameter
      */
     @Check
     public void checkMethodParemeterNotVoid(XtendParameter param) {
         if (typeReferences.is(param.getParameterType(), Void.TYPE)) {
             error("Argument type cannot be void", param.getParameterType(), null,
                     INVALID_USE_OF_TYPE);
         }
     }
 
     /**
      * Checks that the declared return type of a method matches the actual return type, if specified
      * 
      * @param def
      *            The method def
      */
     @Check
     public void checkMethodReturnType(XMethodDef def) {
         JvmTypeReference declaredReturnType = def.getReturnType();
         if (declaredReturnType != null && !typeReferences.is(declaredReturnType, Void.TYPE)) {
             JvmTypeReference commonReturnType = typeProvider.getCommonReturnType(
                     def.getExpression(), true);
             if (!typeConformanceComputer.isConformant(declaredReturnType, commonReturnType)) {
                 error("Incompatible return type. Declared " + getNameOfTypes(declaredReturnType)
                         + " but was " + canonicalName(commonReturnType), def,
                         XtendPackage.Literals.XTEND_FUNCTION__EXPRESSION,
                         ValidationMessageAcceptor.INSIGNIFICANT_INDEX, INCOMPATIBLE_RETURN_TYPE);
             }
         }
     }
 
     /**
      * Checks that method def type parameter names are unique
      * 
      * @param def
      *            The method def
      */
     @Check
     public void checkMethodTypeParametersUnique(XMethodDef def) {
         checkParameterNames(def.getTypeParameters(), TypesPackage.Literals.JVM_TYPE_PARAMETER__NAME);
     }
 
     /**
      * Checks that static method def signatures are unique.
      * 
      * Don't need to check local methods. Like javascript functions they can shadow each other based
      * on scoping rules.
      * 
      * @param body
      *            The body of the test file
      */
     @Check
     public void checkStaticMethodSignaturesUnique(Body body) {
         final TreeIterator<EObject> allContents = body.eResource().getAllContents();
         Iterable<EObject> objects = new Iterable<EObject>() {
             @Override
             public java.util.Iterator<EObject> iterator() {
                 return allContents;
             };
         };
         Iterable<XMethodDef> filter = Iterables.filter(objects, XMethodDef.class);
         Multimap<Object, JvmOperation> signatureToOperation = HashMultimap.create();
         for (XMethodDef def : filter) {
             if (def.isStatic()) {
                 for (JvmOperation operation : associations.getJvmOperations(def)) {
                     TypeErasedSignature signature = signatureProvider.get(operation);
                     signatureToOperation.put(signature, operation);
                 }
             }
         }
         JvmGenericType inferredType = associations.getInferredType(body);
         if (inferredType != null) {
             xtendUtils.doCheckDuplicateExecutables(inferredType, signatureToOperation);
         }
     }
 
     @Check
     @Override
     public void checkTypeReferenceIsNotVoid(XExpression expression) {
         // only type reference is return type, that can be void
         if (!(expression instanceof XMethodDef)) {
             super.checkTypeReferenceIsNotVoid(expression);
         }
     }
 
     /**
      * Checks that var-arg parameter is last, if present
      * 
      * @param def
      *            The method def
      */
     @Check
     public void checkVarArgIsLast(XMethodDef def) {
         EList<XtendParameter> parameters = def.getParameters();
         List<XtendParameter> reversedParams = Lists.reverse(parameters);
         boolean first = true;
         for (XtendParameter parameter : reversedParams) {
             if (first) {
                 first = false;
             } else if (parameter.isVarArg()) {
                 error("Only last paremeter can be var arg", parameter, null, 0);
             }
         }
     }
 
     /**
      * Runs the unit test as long as the {@link CheckType} is not {@link DontRunCheck} and marks any
      * failed expressions.
      * 
      * @param main
      *            The xtest expression model to run.
      */
     @Check(CheckType.EXPENSIVE)
     public void doMagic(Body main) {
         RunType weight = getCheckMode().shouldCheck(CheckType.FAST) ? RunType.LIGHTWEIGHT
                 : RunType.HEAVYWEIGHT;
         if (!(getCheckMode() instanceof XTestRunner.DontRunCheck)) {
             CancelIndicator indicator = cancelIndicators.get();
             if (indicator == null) {
                 indicator = CancelIndicator.NullImpl;
             }
             XTestResult result = runner.run(main, weight, indicator);
             markErrorsFromTest(result);
 
             if (preferenceProvider.get(main, RuntimePref.MARK_UNEXECUTED)) {
                 Set<XExpression> unexecutedExpressions = runner.getUnexecutedExpressions(main);
                 markUnexecuted(main, unexecutedExpressions);
             }
             getContext().put(XTestResult.KEY, result);
         }
     }
 
     @Override
     protected void checkDeclaredVariableName(EObject nameDeclarator, EObject attributeHolder,
             EAttribute attr) {
         super.checkDeclaredVariableName(nameDeclarator, attributeHolder, attr);
         if (nameDeclarator.eContainer() != null
                 && attr.getEContainingClass().isInstance(attributeHolder)) {
             String name = (String) attributeHolder.eGet(attr);
             if (name != null) {
                 int idx = 0;
                 if (nameDeclarator.eContainer() instanceof XBlockExpression) {
                     idx = ((XBlockExpression) nameDeclarator.eContainer()).getExpressions()
                             .indexOf(nameDeclarator);
                 }
                 IScope scope = getScopeProvider().createSimpleFeatureCallScope(
                         nameDeclarator.eContainer(),
                         XbasePackage.Literals.XABSTRACT_FEATURE_CALL__FEATURE,
                         nameDeclarator.eResource(), true, idx);
                 Iterable<IEObjectDescription> elements = scope.getElements(QualifiedName
                         .create(name));
                 for (IEObjectDescription desc : elements) {
                     EObject eObjectOrProxy = desc.getEObjectOrProxy();
                     if (eObjectOrProxy != nameDeclarator && eObjectOrProxy instanceof JvmOperation
                             && !(nameDeclarator instanceof XMethodDef)
                             && !((JvmOperation) eObjectOrProxy).isStatic()) {
                         error("Local variable '" + name + "' shadows local method",
                                 attributeHolder,
                                 attr,
                                 org.eclipse.xtext.xbase.validation.IssueCodes.VARIABLE_NAME_SHADOWING);
                     }
                 }
             }
         }
     }
 
     /**
      * Checks that a parameter name is only used once
      * 
      * @param typeParameters
      *            List of parameters
      * @param nameAttr
      *            Attribute containing the name
      */
     protected void checkParameterNames(EList<? extends EObject> typeParameters,
             final EAttribute nameAttr) {
         Multiset<String> typeParamNames = TreeMultiset.create(Iterables.transform(typeParameters,
                 new Function<EObject, String>() {
                     @Override
                     public String apply(EObject input) {
                         return (String) input.eGet(nameAttr);
                     }
                 }));
         for (EObject typeParam : typeParameters) {
             String name = (String) typeParam.eGet(nameAttr);
             if (typeParamNames.count(name) > 1) {
                 error("Duplicate type parameter name '" + name + "'", typeParam, nameAttr,
                         org.eclipse.xtext.xbase.validation.IssueCodes.VARIABLE_NAME_SHADOWING);
             }
         }
     }
 
     @Override
     protected boolean isResponsible(Map<Object, Object> context, EObject eObject) {
         cancelIndicators.set((CancelIndicator) context
                 .get(CancelableDiagnostician.CANCEL_INDICATOR));
         return super.isResponsible(context, eObject);
     }
 
     @Override
    protected boolean isValueExpectedRecursive(XExpression expr) {
        EObject eContainer = expr.eContainer();
        return eContainer != null && eContainer instanceof XAssertExpression
                || super.isValueExpectedRecursive(expr);
    }

    @Override
     protected boolean supportsCheckedExceptions() {
         // No need to check exceptions in xtest, they will be flagged as errors if thrown
         return false;
     }
 
     /**
      * Marks the errors from the test
      * 
      * @param run
      *            The test result
      */
     private void markErrorsFromTest(XTestResult run) {
         if (run != null) {
             for (String error : run.getErrorMessages()) {
                 error(run.getQualifiedName() + ": " + error, run.getEObject(), null,
                         TEST_RUN_FAILURE_INDEX);
             }
             markEvaluationExceptions(run);
             for (XTestResult test : run.getSubTests()) {
                 markErrorsFromTest(test);
             }
         }
     }
 
     /**
      * Marks the evaluation exception on the line that generated it
      * 
      * @param run
      *            The test that failed
      */
     private void markEvaluationExceptions(XTestResult run) {
         Collection<XTestEvaluationException> exceptions = run.getEvaluationException();
         for (XTestEvaluationException exception : exceptions) {
             Throwable cause = exception.getCause();
             XExpression expression = exception.getExpression();
             StringBuilder builder = new StringBuilder(run.getQualifiedName() + ": "
                     + cause.toString());
             for (StackTraceElement trace : cause.getStackTrace()) {
                 builder.append("\n");
                 builder.append(trace.toString());
             }
             error(builder.toString(), expression, null, TEST_RUN_FAILURE_INDEX);
         }
     }
 
     /**
      * Finds all expressions in {@code main} that are not contained in {@code executedExpressions}
      * 
      * @param main
      *            The top-level expression object
      * @param executedExpressions
      *            The set of evaluated expressions
      */
     private void markUnexecuted(Body main, Set<XExpression> unexecuted) {
         for (XExpression expression : unexecuted) {
             warning("Expression never reached", expression, null, 10);
         }
     }
 }
