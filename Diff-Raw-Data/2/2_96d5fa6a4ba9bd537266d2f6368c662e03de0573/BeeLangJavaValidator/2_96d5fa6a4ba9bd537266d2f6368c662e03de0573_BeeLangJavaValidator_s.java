 package org.eclipse.b3.validation;
 
 import java.lang.reflect.Type;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.b3.backend.core.IB3LvalProvider;
 import org.eclipse.b3.backend.core.IStringProvider;
 import org.eclipse.b3.backend.core.datatypes.TypePattern;
 import org.eclipse.b3.backend.core.exceptions.B3AmbiguousFunctionSignatureException;
 import org.eclipse.b3.backend.core.exceptions.B3NoSuchFunctionException;
 import org.eclipse.b3.backend.core.exceptions.B3NoSuchFunctionSignatureException;
 import org.eclipse.b3.backend.evaluator.B3ConstantEvaluator;
 import org.eclipse.b3.backend.evaluator.B3ConstantEvaluator.ConstantEvaluationResult;
 import org.eclipse.b3.backend.evaluator.Pojo;
 import org.eclipse.b3.backend.evaluator.b3backend.B3JavaImport;
 import org.eclipse.b3.backend.evaluator.b3backend.B3MetaClass;
 import org.eclipse.b3.backend.evaluator.b3backend.B3ParameterizedType;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage;
 import org.eclipse.b3.backend.evaluator.b3backend.BAssignmentExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BAtExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BCallFeature;
 import org.eclipse.b3.backend.evaluator.b3backend.BCallFunction;
 import org.eclipse.b3.backend.evaluator.b3backend.BCallNamedFunction;
 import org.eclipse.b3.backend.evaluator.b3backend.BCreateExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BDefValue;
 import org.eclipse.b3.backend.evaluator.b3backend.BExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BFeatureExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BFunction;
 import org.eclipse.b3.backend.evaluator.b3backend.BFunctionConcernContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BIfExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BLiteralExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BLiteralListExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BLiteralMapExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BMapEntry;
 import org.eclipse.b3.backend.evaluator.b3backend.BParameterDeclaration;
 import org.eclipse.b3.backend.evaluator.b3backend.BProceedExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BWithExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.IFunction;
 import org.eclipse.b3.backend.evaluator.b3backend.impl.BBinaryOpExpressionImpl;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 import org.eclipse.b3.backend.inference.FunctionUtils;
 import org.eclipse.b3.backend.inference.ITypeInfo;
 import org.eclipse.b3.backend.inference.ITypeProvider;
 import org.eclipse.b3.backend.inference.InferenceException;
 import org.eclipse.b3.backend.inference.InferenceExceptions;
 import org.eclipse.b3.build.B3BuildPackage;
 import org.eclipse.b3.build.BeeModel;
 import org.eclipse.b3.build.Branch;
 import org.eclipse.b3.build.BuildSet;
 import org.eclipse.b3.build.BuildUnit;
 import org.eclipse.b3.build.Builder;
 import org.eclipse.b3.build.BuilderConcernContext;
 import org.eclipse.b3.build.FirstFoundUnitProvider;
 import org.eclipse.b3.build.PathVector;
 import org.eclipse.b3.build.RepoOption;
 import org.eclipse.b3.build.Repository;
 import org.eclipse.b3.build.RepositoryUnitProvider;
 import org.eclipse.b3.build.core.iterators.PathIterator;
 import org.eclipse.b3.build.core.runtime.RepositoryValidation;
 import org.eclipse.b3.build.repository.IRepositoryValidator;
 import org.eclipse.b3.build.repository.IRepositoryValidator.IOption;
 import org.eclipse.b3.scoping.DeclarativeVarScopeProvider;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.xtext.resource.IEObjectDescription;
 import org.eclipse.xtext.scoping.IScope;
 import org.eclipse.xtext.validation.Check;
 
 import com.google.common.base.ReferenceType;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimaps;
 import com.google.common.collect.Sets;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.internal.Lists;
 import com.google.inject.internal.Maps;
 
 public class BeeLangJavaValidator extends AbstractBeeLangJavaValidator implements IBeeLangDiagnostic {
 
 	@Inject
 	private Injector injector;
 
 	@Inject
 	private IStringProvider stringProvider;
 
 	@Inject
 	private ITypeProvider typer;
 
 	@Inject
 	private IB3LvalProvider lvalProvider;
 
 	@Check
 	public void checkAssignment(BAssignmentExpression expr) {
 		FunctionUtils funcUtils = injector.getInstance(FunctionUtils.class);
 		ITypeProvider typer = injector.getInstance(ITypeProvider.class);
 		ITypeInfo tinfo = typer.doGetTypeInfo(expr.getLeftExpr());
 		Type lhsType = tinfo.getType(); // typer.doGetInferredType(expr.getLeftExpr());
 
 		// Check that it is an LValue
 		if(!tinfo.isLValue()) {
 			error(
 				"The left-hand side of an assignment must be a variable", expr.getLeftExpr(),
 				B3backendPackage.BASSIGNMENT_EXPRESSION__LEFT_EXPR);
 			// Additional errors are meaningless
 			return;
 
 		}
 		if(!tinfo.isSettable()) {
 			error(
 				"The left hand side of the assignment can not be immutable", expr.getLeftExpr(),
 				B3backendPackage.BASSIGNMENT_EXPRESSION__LEFT_EXPR);
 			// it is worth continuing with additional errors as it may provide a clue
 			// to what is wrong.
 		}
 		BExpression rhs = expr.getRightExpr();
 		Type rhsType = typer.doGetInferredType(expr.getRightExpr());
 		boolean isNull = rhs instanceof BLiteralExpression && ((BLiteralExpression) rhs).getValue() == null;
 		String fName = expr.getFunctionName();
 		String lhsName = stringProvider.doToString(lhsType);
 		String rhsName = stringProvider.doToString(rhsType);
 		// straight assignment
 		if(fName == null || "".equals(fName) || "=:".contains(fName)) {
 			if(!isNull) {
 				if(!(((tinfo.isEObject() && TypeUtils.isESettableFrom(lhsType, rhsType)) || TypeUtils.isAssignableFrom(
 					lhsType, rhsType)))) {
 					error(
 						"Type mismatch: Cannot convert from " + rhsName + " to " + lhsName, expr,
 						B3backendPackage.BASSIGNMENT_EXPRESSION__RIGHT_EXPR);
 					return;
 				}
 			}
 		}
 		else {
 			// This is an assigned call (i.e. += etc, validate function call)
 			String fn = fName.endsWith("=")
 					? fName.substring(0, fName.length() - 1)
 					: fName;
 			Type[] types = new Type[] { lhsType, rhsType };
 			try {
 				List<IFunction> effective = funcUtils.findEffectiveFunctions(expr, fn, lhsType);
 				// used for side effect of exceptions
 				IFunction f = funcUtils.selectFunction(fn, effective, types);
 				Type returnType = null;
 				if(f.getTypeCalculator() != null) {
 					returnType = f.getTypeCalculator().getSignature(types).getReturnTypeForParameterTypes(types);
 				}
 				else
 					returnType = f.getReturnType();
 				if(returnType == null)
 					returnType = Object.class;
 				// check for IntegerVariable += StringValue etc.
 				if(!TypeUtils.isAssignableFrom(lhsType, returnType)) {
 					error("Type operator mismatch: Cannot convert from " + stringProvider.doToString(returnType) +
 							" to " + lhsName, expr, B3backendPackage.BASSIGNMENT_EXPRESSION__RIGHT_EXPR);
 					return;
 				}
 			}
 			catch(B3NoSuchFunctionSignatureException e) {
 				error("Operator va. type mismatch: operator " + fn + " not applicable to types " + lhsName + " and " +
 						rhsName, expr, B3backendPackage.BASSIGNMENT_EXPRESSION__FUNCTION_NAME);
 			}
 			catch(B3NoSuchFunctionException e) {
 				error("Illegal operator (internal error)", expr, B3backendPackage.BASSIGNMENT_EXPRESSION__FUNCTION_NAME);
 			}
 			catch(B3AmbiguousFunctionSignatureException e) {
 				error(
 					"Ambigous operation - sevaral choices for types " + lhsName + " and " + rhsName, expr,
 					B3backendPackage.BASSIGNMENT_EXPRESSION__FUNCTION_NAME);
 			}
 		}
 		// check that value is mutable
 
 	}
 
 	@Check
 	public void checkAtOperation(BAtExpression o) {
 		Type objType = null;
 		if(!lvalProvider.doIsIndexLValType(objType = typer.doGetInferredType(o.getObjExpr()))) {
 			error(
 				"Non indexed type: " + stringProvider.doToString(objType) + " can not be used with [] operator",
 				o.getObjExpr(), B3backendPackage.BEXPRESSION);
 		}
 	}
 
 	@Check
 	public void checkBeeModel(BeeModel beeModel) {
 		// mark all but the first repository as being in error.
 		int count = 0;
 		for(FirstFoundUnitProvider provider : beeModel.getProviders()) {
 			count++;
 			if(count > 1)
 				error(
 					"A build unit may only have one 'resolution' declaration.", provider,
 					B3BuildPackage.BEE_MODEL__PROVIDERS, ISSUE_BEEMODEL__MULTIPLE_RESOLUTIONS);
 		}
 		// check for unique names and versions of units
 		//
 		HashMultimap<String, BuildUnit> umap = Multimaps.newHashMultimap();
 		Set<BuildUnit> offenders = Sets.newIdentityHashSet(ReferenceType.STRONG);
 		for(BuildUnit u : beeModel.getBuildUnits()) {
 			if(u.getName() == null)
 				continue; // handled by BuildUnit validation
 			for(BuildUnit u2 : umap.get(u.getName())) {
 				if((u.getVersion() == null && u2.getVersion() == null) || (u.getVersion().equals(u2.getVersion()))) {
 					offenders.add(u);
 					offenders.add(u2);
 				}
 			}
 			umap.put(u.getName(), u);
 		}
 		for(BuildUnit u : offenders)
 			error("Duplicate unit name/version", u, B3BuildPackage.BUILD_UNIT);
 
 		// check for unique signatures of functions
 		HashMultimap<String, IFunction> fmap = Multimaps.newHashMultimap();
 		Set<IFunction> funcOffenders = Sets.newIdentityHashSet(ReferenceType.STRONG);
 		for(IFunction f : beeModel.getFunctions()) {
 			if(f.getName() == null)
 				continue; // handled elsewhere
 			for(IFunction f2 : fmap.get(f.getName())) {
 				if(TypeUtils.hasEqualSignature(f, f2)) {
 					funcOffenders.add(f);
 					funcOffenders.add(f2);
 				}
 			}
 			fmap.put(f.getName(), f);
 		}
 		for(IFunction f : funcOffenders)
 			error("Duplicate function signature", f, B3backendPackage.B3_FUNCTION__NAME);
 
 	}
 
 	/**
 	 * A proceed expression can only occur in expressions that are going to be used for weaving.
 	 * These are specified in a BFunctionConcernContext, or a BuilderConcernContext.
 	 */
 	@Check
 	public void checkBProceedExpression(BProceedExpression proceed) {
 		EObject container = proceed.eContainer();
 		while(container != null) {
 			if(container instanceof BFunctionConcernContext || container instanceof BuilderConcernContext)
 				return; // ok
 			container = container.eContainer();
 		}
 		error(
 			"A proceed expression can only appear inside a function or builder context in a concern", proceed,
 			B3backendPackage.BPROCEED_EXPRESSION);
 	}
 
 	@Check
 	public void checkBranch(Branch branch) {
 		// If branchpoint type is timestamp
 		// If branchpoint type is other
 	}
 
 	@Check
 	public void checkBuilderMustReturnSomething(Builder builder) {
 		if(builder.getOutput() == null && builder.getInput() == null && builder.getSource() == null &&
 				builder.getFuncExpr() == null) {
 			error(
 				"A builder must have one of: input, source, output or expression returning BuildResult.", builder,
 				B3BuildPackage.BUILDER__NAME);
 			return;
 		}
 		if(builder.getFuncExpr() != null) {
 			Type returnType = typer.doGetInferredType(builder.getFuncExpr());
 			if(returnType == null || !TypeUtils.isAssignableFrom(BuildSet.class, returnType))
 				error(
 					"Can not convert a " + stringProvider.doToString(returnType) + " to a BuildSet.", builder,
 					B3BuildPackage.BUILDER__FUNC_EXPR);
 		}
 	}
 
 	@Check
 	public void checkBuildUnit(BuildUnit buildUnit) {
 		// mark all but the first repository as being in error.
 		int count = 0;
 		for(FirstFoundUnitProvider provider : buildUnit.getProviders()) {
 			count++;
 			if(count > 1)
 				error(
 					"A build unit may only have one 'resolution' declaration.", provider,
 					B3BuildPackage.BUILD_UNIT__PROVIDERS, ISSUE_BUILD_UNIT__MULTIPLE_RESOLUTIONS);
 		}
 		// make sure all "implements" are unique
 		// make sure all "implements" are BuildUnit interfaces
 		List<Type> seenTypes = Lists.newArrayList();
 		for(Type t : buildUnit.getImplements()) {
 			if(t instanceof B3ParameterizedType && ((B3ParameterizedType) t).getRawType() == null) {
 				error("Incomplete list of 'is'", (EObject) t, B3BuildPackage.BUILD_UNIT__IMPLEMENTS, null);
 				continue; // not meaningful to test with null type
 			}
 			if(seenTypes.contains(t))
 				error("Duplicate declaration of unit type", t instanceof EObject
 						? (EObject) t
 						: buildUnit, B3BuildPackage.BUILD_UNIT__IMPLEMENTS, ISSUE_BUILD_UNIT__DUPLICATE_IS);
 			else
 				seenTypes.add(t);
 			if(t instanceof B3ParameterizedType && ((B3ParameterizedType) t).getRawType() instanceof B3JavaImport &&
 					((B3JavaImport) ((B3ParameterizedType) t).getRawType()).getType() == null) {
 				error("Invalid type", t instanceof EObject
 						? (EObject) t
 						: buildUnit, B3BuildPackage.BUILD_UNIT__IMPLEMENTS, null);
 				continue;
 			}
 			if(!TypeUtils.isAssignableFrom(BuildUnit.class, t))
 				error("A unit type must be a specialization of BuildUnit", t instanceof EObject
 						? (EObject) t
 						: buildUnit, B3BuildPackage.BUILD_UNIT__IMPLEMENTS, ISSUE_BUILD_UNIT__NOT_UNIT_INTERFACE);
 		}
 	}
 
 	@Check
 	public void checkFeatureCallCanBeMade(BCallFeature cexpr) {
 		FunctionUtils funcUtils = injector.getInstance(FunctionUtils.class);
 		ITypeProvider typer = injector.getInstance(ITypeProvider.class);
 
 		BExpression lhs = cexpr.getFuncExpr();
 		String name = cexpr.getName();
 		Type type = typer.doGetInferredType(lhs);
 		if(type == null) {
 			error(
 				"The type of the expression is not known or inferable.", cexpr,
 				B3backendPackage.BCALL_FEATURE__FUNC_EXPR);
 			return;
 		}
 		if(name == null || name.length() < 1) {
 			error("The name of the feature is null or empty.", cexpr, B3backendPackage.BCALL_FEATURE__NAME);
 			return;
 		}
 		if(!cexpr.isCall()) {
 			// this is a feature expression
 			// (this type of feature expression will not have a null lhs)
 
 			Pojo.Feature resultingFeature = new Pojo.Feature(TypeUtils.getRaw(type), name);
 
 			if(!resultingFeature.isGetable()) {
 				error(
 					"The feature '" + name + "' is not a feature found in type '" + type + "'.", cexpr,
 					B3backendPackage.BCALL_FEATURE__NAME);
 			}
 			return;
 		}
 		// this is a feature call
 		//
 		Type[] tparameters = null;
 		try {
 			tparameters = funcUtils.asTypeArray(cexpr, true);
 			tparameters[0] = type;
 		}
 		catch(InferenceExceptions e) {
 			// mark all erroneous parameters
 			for(InferenceException e2 : e.getExceptions())
 				error("The type of the parameter is not known or inferable.", e2.getSource(), e2.getFeature());
 			return; // not meaningful to continue
 		}
 
 		try {
 			List<IFunction> effective = funcUtils.findEffectiveFunctions(cexpr, name, type);
 			// used for side effect of exceptions
 			funcUtils.selectFunction(name, effective, tparameters);
 		}
 		catch(B3NoSuchFunctionSignatureException e) {
 			error(
 				"No function matching used parameter types found.", cexpr,
 				B3backendPackage.BCALL_FEATURE__PARAMETER_LIST);
 		}
 		catch(B3NoSuchFunctionException e) {
 			error("Function name not found.", cexpr, B3backendPackage.BCALL_FEATURE__NAME);
 		}
 		catch(B3AmbiguousFunctionSignatureException e) {
 			error("Used parameter types leads to ambiguous call", cexpr, B3backendPackage.BCALL_FEATURE__PARAMETER_LIST);
 		}
 	}
 
 	@Check
 	public void checkFeatureExists(BFeatureExpression fexpr) {
 		EObject objE = fexpr.getObjExpr();
 
 		// TODO: Ugly, it expects to find "special engine var 'this'" in runtime == a created instance
 		// when the object expression is null.
 		if(objE == null) {
 			EObject container = fexpr;
 			while(container.eContainer() != null && !(container instanceof BCreateExpression))
 				container = container.eContainer();
 			objE = container;
 		}
 		String fname = fexpr.getFeatureName();
 		// B3BuildTypeProvider typer = new B3BuildTypeProvider();
 		Type type = typer.doGetInferredType(objE);
 
 		Pojo.Feature resultingFeature = new Pojo.Feature(TypeUtils.getRaw(type), fname);
 
 		if(!resultingFeature.isGetable()) {
 			error(
 				"The feature '" + fname + "' is not a feature found in type '" + type + "'.", fexpr,
 				B3backendPackage.BFEATURE_EXPRESSION__FEATURE_NAME);
 		}
 	}
 
 	@Check
 	public void checkFunctionCallCanBeMade(BCallNamedFunction o) {
 		FunctionUtils funcUtils = injector.getInstance(FunctionUtils.class);
 		// ITypeProvider typer = injector.getInstance(ITypeProvider.class);
 		String fName = o.getFuncRef() == null
 				? o.getName()
 				: o.getFuncRef().getName();
 
 		Type[] tparameters = null;
 		try {
 			tparameters = funcUtils.asTypeArray(o, false);
 		}
 		catch(InferenceExceptions e) {
 			// mark all erroneous parameters
 			for(InferenceException e2 : e.getExceptions())
 				error("The type of the parameter is not known or inferable.", e2.getSource(), e2.getFeature());
 			return; // not meaningful to continue
 		}
 
 		try {
 			List<IFunction> effective = funcUtils.findEffectiveFunctions(o, fName, tparameters.length > 0
 					? tparameters[0]
 					: null);
 			// used for side effect of exceptions
 			funcUtils.selectFunction(fName, effective, tparameters);
 		}
 		catch(B3NoSuchFunctionSignatureException e) {
 			error(
 				"No function matching used parameter types found.", o, B3backendPackage.BCALL_FUNCTION__PARAMETER_LIST);
 		}
 		catch(B3NoSuchFunctionException e) {
 			error("Function name not found.", o, B3backendPackage.BCALL_FUNCTION__NAME);
 		}
 		catch(B3AmbiguousFunctionSignatureException e) {
 			error("Used parameter types leads to ambiguous call", o, B3backendPackage.BCALL_FUNCTION__PARAMETER_LIST);
 		}
 
 	}
 
 	/**
 	 * Validate that the entered parameter pattern is compilable.
 	 * 
 	 * @param fcc
 	 */
 	@Check
 	public void checkFunctionConcernContext(BFunctionConcernContext fcc) {
 		try {
 			TypePattern.compile(fcc.getParameters());
 		}
 		catch(Throwable t) {
 			error(t.getMessage(), fcc, B3backendPackage.BFUNCTION_CONCERN_CONTEXT__PARAMETERS);
 		}
 	}
 
 	@Check
 	public void checkFunctionExpression(BCallFunction o) {
 		FunctionUtils funcUtils = injector.getInstance(FunctionUtils.class);
 		ITypeProvider typer = injector.getInstance(ITypeProvider.class);
 		Type lhsType = typer.doGetInferredType(o.getFuncExpr());
 		Type[] tparameters = null;
 		try {
 			tparameters = funcUtils.asTypeArray(o, false);
 		}
 		catch(InferenceExceptions e) {
 			// mark all erroneous parameters
 			for(InferenceException e2 : e.getExceptions())
 				error("The type of the parameter is not known or inferable.", e2.getSource(), e2.getFeature());
 			return; // not meaningful to continue
 		}
 
 		if(lhsType instanceof B3MetaClass) {
 			if(tparameters.length != 1)
 				error("Attempt to cast multiple instances", o, B3backendPackage.BCALL_FUNCTION__PARAMETER_LIST);
 		}
 		else if(!TypeUtils.isAssignableFrom(BFunction.class, lhsType))
 			error(
 				"Type mismatch: can not convert " + stringProvider.doToString(lhsType) + " to function or type", o,
 				B3backendPackage.BCALL_FUNCTION__FUNC_EXPR);
 
 	}
 
 	/**
 	 * Validate that import references a class that can be loaded.
 	 */
 	@Check
 	public void checkImportability(B3JavaImport jimport) {
 		try {
 			if(jimport.getType() == null)
 				error("Could not import type", jimport, B3backendPackage.B3_JAVA_IMPORT__QUALIFIED_NAME);
 
 		}
 		catch(Throwable t) {
 			error(t.getMessage(), jimport, B3backendPackage.B3_JAVA_IMPORT__QUALIFIED_NAME);
 		}
 	}
 
 	@Check
 	public void checkParameterNamesAreUnique(IFunction func) {
 		Map<String, Boolean> seen = Maps.newHashMap();
 		List<BParameterDeclaration> culprits = Lists.newArrayList();
 		for(BParameterDeclaration decl : func.getParameters()) {
 			if(seen.get(decl.getName()) != null)
 				culprits.add(decl);
 			seen.put(decl.getName(), Boolean.TRUE);
 		}
 		for(BParameterDeclaration d : culprits)
 			error("Duplicate parameter name", d, B3backendPackage.BPARAMETER_DECLARATION__NAME);
 	}
 
 	/**
 	 * Validate that path vectors are consistent
 	 */
 	@Check
 	public void checkPathVector(PathVector pathVector) {
 		try {
 			PathIterator pvItor = new PathIterator(pathVector);
 			while(pvItor.hasNext())
 				pvItor.next();
 		}
 		catch(Throwable t) {
 			error(t.getMessage(), pathVector, B3BuildPackage.PATH_VECTOR);
 		}
 	}
 
 	/**
 	 * Produce warnings:
 	 * - if a repository handler name is unknown to the configuration.
 	 * - if a repository option is for an unknown repository type
	 * ProducesExpr errors:
 	 * - the remote URI of a repository is not set
 	 * - the option is not valid for the repository
 	 * 
 	 * @param repoHandler
 	 */
 	@Check
 	public void checkRepository(Repository repoHandler) {
 		if(!RepositoryValidation.isNameRegistered(repoHandler.getHandlerType()))
 			warning("The repository type '" + repoHandler.getHandlerType() +
 					"' is unknown to the b3 editing environment.", repoHandler, B3BuildPackage.REPOSITORY__HANDLER_TYPE);
 		// TODO: currently all repoHandlers require an address, but there could be those that do not, make it optional
 		if(repoHandler.getAddress() == null) {
 			error(
 				"The repository must have a 'connection'", repoHandler, B3BuildPackage.REPOSITORY__HANDLER_TYPE,
 				ISSUE_REPOSITORY__NO_CONNECTION);
 		}
 	}
 
 	@Check
 	public void checkRepositoryOption(RepoOption option) {
 		EObject container = option.eContainer();
 		if(container instanceof Repository) {
 			Repository repo = (Repository) container;
 			IRepositoryValidator validator = RepositoryValidation.getValidator(repo.getHandlerType());
 			if(validator == null) {
 				warning(
 					"Unable to validate option for unknown repository type  '" + repo.getHandlerType(), option,
 					B3BuildPackage.REPO_OPTION__NAME);
 			}
 			else {
 				Map<String, IOption<Repository>> optionData = validator.getRepositoryOptions();
 				IOption<Repository> opt = optionData.get(option.getName());
 				if(opt == null)
 					error(
 						"The option '" + option.getName() + "' is not a valid option for repository type '" +
 								repo.getHandlerType() + "'.", option, B3BuildPackage.REPO_OPTION__NAME,
 						ISSUE_REPO_OPTION__INVALID_OPTION);
 				else {
 					// TODO: check type compatibility (complicated)
 				}
 			}
 		}
 		else if(container instanceof RepositoryUnitProvider) {
 			RepositoryUnitProvider provider = (RepositoryUnitProvider) container;
 			Repository repo = provider.getRepository();
 			IRepositoryValidator validator = repo != null
 					? RepositoryValidation.getValidator(repo.getHandlerType())
 					: null;
 			if(validator == null) {
 				warning(
 					"Unable to validate option for unknown repository type  '" + repo.getHandlerType(), option,
 					B3BuildPackage.REPO_OPTION__NAME);
 			}
 			else {
 				Map<String, IOption<RepositoryUnitProvider>> optionData = validator.getResolverOptions();
 				IOption<RepositoryUnitProvider> opt = optionData.get(option.getName());
 				if(opt == null)
 					error(
 						"The option '" + option.getName() +
 								"' is not a valid option for a resolver using a repository of type '" +
 								repo.getHandlerType() + "'.", option, B3BuildPackage.REPO_OPTION__NAME,
 						ISSUE_REPO_OPTION__INVALID_OPTION);
 				else {
 					// TODO: check type compatibility (complicated)
 				}
 			}
 
 		}
 	}
 
 	@Check
 	public void checkTypeCompliance(BLiteralListExpression expr) {
 		Type listType = typer.doGetInferredType(expr);
 		Type entryType = TypeUtils.getElementType(listType);
 		if(entryType == Object.class)
 			return; // Meaningless to test
 		String lhsName = stringProvider.doToString(entryType);
 		for(BExpression e : expr.getEntries()) {
 			Type actualType = typer.doGetInferredType(e);
 			if(!TypeUtils.isAssignableFrom(entryType, actualType))
 				error(
 					"Type mismatch: Cannot convert from " + stringProvider.doToString(actualType) + " to " + lhsName,
 					e, B3backendPackage.BEXPRESSION);
 		}
 	}
 
 	@Check
 	public void checkTypeCompliance(BLiteralMapExpression expr) {
 		Type mapType = typer.doGetInferredType(expr);
 		Type[] typeArgs = TypeUtils.getTypeParameters(mapType);
 		if(typeArgs == null || typeArgs.length != 2)
 			return; // Meaningless to continue (Object:Object or some error)
 
 		Type keyType = typeArgs[0];
 		Type valueType = typeArgs[1];
 		if(keyType == null || valueType == null)
 			return;
 		String keyName = stringProvider.doToString(keyType);
 		String valueName = stringProvider.doToString(valueType);
 		for(BMapEntry e : expr.getEntries()) {
 			Type actualKeyType = typer.doGetInferredType(e.getKey());
 			Type actualValType = typer.doGetInferredType(e.getValue());
 			boolean keyError = !TypeUtils.isAssignableFrom(keyType, actualKeyType);
 			boolean valError = !TypeUtils.isAssignableFrom(valueType, actualValType);
 			if(keyError && valError)
 				error("Type mismatch: Cannot convert from Entry<" + stringProvider.doToString(actualKeyType) + ", " +
 						stringProvider.doToString(actualValType) + "> " + " to Entry<" + keyName + ", " + valueName +
 						">", e, B3backendPackage.BEXPRESSION);
 			else if(keyError)
 				error("Type mismatch: Cannot convert from key type " + stringProvider.doToString(actualKeyType) +
 						" to " + keyName, e, B3backendPackage.BMAP_ENTRY__KEY);
 			else if(valError)
 				error("Type mismatch: Cannot convert from value type " + stringProvider.doToString(actualValType) +
 						" to " + valueName, e, B3backendPackage.BMAP_ENTRY__VALUE);
 		}
 	}
 
 	@Check
 	public void checkUnreachableIf(BIfExpression o) {
 		B3ConstantEvaluator constEvaluator = injector.getInstance(B3ConstantEvaluator.class);
 		B3ConstantEvaluator.ConstantEvaluationResult result = constEvaluator.doEvalConstant(o.getConditionExpr());
 		if(!result.isConstant())
 			return;
 		if(result.getResult() instanceof Boolean && result.getResult().equals(Boolean.TRUE))
 			error("Unreachable: condition is constant and true", o.getElseExpr(), B3backendPackage.BEXPRESSION);
 		else
 			error("Unreachable: condition is constant and false", o.getThenExpr(), B3backendPackage.BEXPRESSION);
 	}
 
 	@Check
 	public void checkValueDefinition(BDefValue expr) {
 		// Check type compatibility
 		ITypeProvider typer = injector.getInstance(ITypeProvider.class);
 		Type lhsType = typer.doGetInferredType(expr);
 		Type rhsType = typer.doGetInferredType(expr.getValueExpr());
 		BExpression rhs = expr.getValueExpr();
 		boolean isNull = rhs instanceof BLiteralExpression && ((BLiteralExpression) rhs).getValue() == null;
 
 		if(!isNull && !TypeUtils.isAssignableFrom(lhsType, rhsType)) {
 			String lhsName = stringProvider.doToString(lhsType);
 			String rhsName = stringProvider.doToString(rhsType);
 			error(
 				"Type mismatch: Cannot convert from " + rhsName + " to " + lhsName, expr,
 				B3backendPackage.BDEF_VALUE__VALUE_EXPR);
 			// return;
 		}
 		// Use a scope provider to find named values (i.e. variables visible in the scope)
 		//
 		DeclarativeVarScopeProvider scopeProvider = new DeclarativeVarScopeProvider();
 		for(IScope scope = scopeProvider.doGetVarScope(expr.eContainer(), expr); scope != IScope.NULLSCOPE; scope = scope.getOuterScope())
 			for(IEObjectDescription desc : scope.getContents()) {
 				// if the expr was found
 				if(desc.getEObjectOrProxy() == expr)
 					continue;
 
 				// only check BDefValues
 				EClass eclass = desc.getEClass();
 				if(!(eclass == B3backendPackage.Literals.BDEF_VALUE || eclass == B3backendPackage.Literals.BPARAMETER_DECLARATION))
 					continue;
 				EObject other = desc.getEObjectOrProxy();
 				if(other.eIsProxy())
 					other = EcoreUtil.resolve(other, expr.eResource().getResourceSet());
 				// if name is equal
 				String n = desc.getName();
 				if(n != null && n.equals(expr.getName())) {
 					// if in the same container
 					if(typer.doGetVarScope(expr) == typer.doGetVarScope(other)) {
 						error("Duplicate name", expr, B3backendPackage.INAMED_VALUE__NAME);
 						error("Duplicate name", other, B3backendPackage.INAMED_VALUE__NAME);
 					}
 
 					if(!other.eIsProxy()) {
 						if(eclass == B3backendPackage.Literals.BDEF_VALUE && ((BDefValue) other).isFinal())
 							error("Can not redefined a final variable", expr, B3backendPackage.BDEF_VALUE__NAME);
 						else if(eclass == B3backendPackage.Literals.BPARAMETER_DECLARATION &&
 								((BParameterDeclaration) other).isFinal())
 							error(
 								"Can not redefined a final parameter", expr,
 								B3backendPackage.BPARAMETER_DECLARATION__NAME);
 					}
 				}
 			}
 	}
 
 	@Check
 	public void checkWithClauseIsNotEmpty(BWithExpression withClause) {
 		if(withClause.getReferencedAdvice().size() == 0 && withClause.getConcerns().size() == 0 &&
 				withClause.getPropertySets().size() == 0) {
 			error(
 				"with-clause must have at least one of: references, properties, or concern", withClause,
 				B3backendPackage.BWITH_EXPRESSION);
 		}
 	}
 
 	@Override
 	protected List<EPackage> getEPackages() {
 		List<EPackage> result = super.getEPackages();
 		result.add(B3backendPackage.eINSTANCE);
 		result.add(B3BuildPackage.eINSTANCE);
 		return result;
 
 	}
 
 	@Check
 	void checkBuilderUnitParameter(Builder builder) {
 		if(builder.eContainer() instanceof BuildUnit && builder.getExplicitUnitType() != null)
 			error(
 				"Can not override implied unit parameter for builder declared in a unit.",
 				builder.getExplicitUnitType(), B3BuildPackage.UNIT_PARAMETER_DECLARATION__NAME);
 	}
 
 	@Check
 	void checkDivByZero(BBinaryOpExpressionImpl o) {
 		if(!o.getFunctionName().equals("/"))
 			return;
 		B3ConstantEvaluator constEval = injector.getInstance(B3ConstantEvaluator.class);
 		ConstantEvaluationResult result = constEval.doEvalConstant(o.getRightExpr());
 		if(!result.isConstant())
 			return;
 		Object value = result.getResult();
 		if(value instanceof Number && ((Number) value).longValue() == 0)
 			error("Division by 0", o, B3backendPackage.BBINARY_OP_EXPRESSION__RIGHT_EXPR);
 	}
 }
