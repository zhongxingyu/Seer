 package de.tud.stg.tigerseye.eclipse.core.codegeneration;
 
 import static de.tud.stg.tigerseye.util.Utils.*;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.annotation.CheckForNull;
 import javax.annotation.Nullable;
 
 import org.eclipse.core.runtime.Assert;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.tud.stg.parlex.core.Category;
 import de.tud.stg.parlex.core.Grammar;
 import de.tud.stg.parlex.core.ICategory;
 import de.tud.stg.parlex.core.IGrammar;
 import de.tud.stg.parlex.core.IRule;
 import de.tud.stg.parlex.core.IRuleAnnotation;
 import de.tud.stg.parlex.core.Rule;
 import de.tud.stg.parlex.core.groupcategories.WaterCategory;
 import de.tud.stg.parlex.core.ruleannotations.AbsolutePriorityAnnotation;
 import de.tud.stg.parlex.core.ruleannotations.AssociativityAnnotation;
 import de.tud.stg.parlex.core.ruleannotations.AvoidAnnotation;
 import de.tud.stg.parlex.core.ruleannotations.PreferAnnotation;
 import de.tud.stg.parlex.core.ruleannotations.RejectAnnotation;
 import de.tud.stg.parlex.core.ruleannotations.RelativePriorityAnnotation;
 import de.tud.stg.popart.builder.core.annotations.DSLMethod;
 import de.tud.stg.popart.builder.core.annotations.DSLMethod.Associativity;
 import de.tud.stg.popart.builder.core.annotations.DSLMethod.DslMethodType;
 import de.tud.stg.popart.builder.core.annotations.DSLMethod.PreferencePriority;
 import de.tud.stg.tigerseye.eclipse.core.api.DSLDefinition;
 import de.tud.stg.tigerseye.eclipse.core.codegeneration.extraction.ClassDSLInformation;
 import de.tud.stg.tigerseye.eclipse.core.codegeneration.extraction.DSLInformationDefaults;
 import de.tud.stg.tigerseye.eclipse.core.codegeneration.extraction.MethodDSLInformation;
 import de.tud.stg.tigerseye.eclipse.core.codegeneration.extraction.MethodProductionConstants.ProductionElement;
 import de.tud.stg.tigerseye.eclipse.core.codegeneration.extraction.MethodProductionElement;
 import de.tud.stg.tigerseye.eclipse.core.codegeneration.extraction.MethodProductionScanner;
 import de.tud.stg.tigerseye.eclipse.core.codegeneration.extraction.ParameterDSLInformation;
 import de.tud.stg.tigerseye.eclipse.core.codegeneration.extraction.ParameterElement;
 import de.tud.stg.tigerseye.eclipse.core.codegeneration.extraction.WhitespaceElement;
 import de.tud.stg.tigerseye.eclipse.core.codegeneration.grammars.CategoryNames;
 import de.tud.stg.tigerseye.eclipse.core.codegeneration.grammars.HostLanguageGrammar;
 import de.tud.stg.tigerseye.eclipse.core.codegeneration.typeHandling.ConfigurationOptions;
 import de.tud.stg.tigerseye.eclipse.core.codegeneration.utils.WhitespaceCategoryDefinition;
 
 /**
  * {@link GrammarBuilder} builds the grammar for given classes implementing the
  * DSL interface.
  * 
  * @author Kamil Erhard
  * @author Leo Roos
  * 
  */
 public class GrammarBuilder {
 
     private static final Logger logger = LoggerFactory.getLogger(GrammarBuilder.class);
 
     private final AtomicInteger parameterCounter = new AtomicInteger();
 
     /*
      * Only used as left hand side rule or grammar category
      */
     private final ICategory<String> statement = new Category(CategoryNames.STATEMENT_CATEGORY, false);
     private final ICategory<String> statements = new Category(CategoryNames.STATEMENTS_CATEGORY, false);
 
     private final UnicodeLookupTable unicodeLookupTable;
 
     public final HashMap<String, MethodOptions> methodAliases = new HashMap<String, MethodOptions>();
 
     public GrammarBuilder(UnicodeLookupTable ult) {
 	this.unicodeLookupTable = ult;
     }
 
     private void setWaterEnabled(boolean enabled, Grammar grammar) {
 	ICategory<String> anything = new WaterCategory();
 	grammar.addCategory(anything);
 
 	Rule rAnyStatement = new Rule(this.statement, anything);
 	grammar.addRule(rAnyStatement);
 
 	grammar.addWaterRule(rAnyStatement);
     }
 
     private void setupGeneralGrammar(Grammar grammar) {
 	Category program = new Category(CategoryNames.PROGRAM_CATEGORY, false);
 
 	Rule startRule = new Rule(program, this.statements);
 
 	Rule rStatements = new Rule(this.statements, newList(this.statement)
 		.add(WhitespaceCategoryDefinition.getAndSetOptionalWhitespace(grammar)).add(this.statements).toList());
 
 	Rule rStatement = new Rule(this.statements, this.statement);
 
 	grammar.addCategory(program);
 	grammar.addCategory(this.statement);
 	grammar.addCategory(this.statements);
 
 	grammar.setStartRule(startRule);
 	grammar.addRule(rStatement);
 	grammar.addRule(rStatements);
     }
 
     // XXX(Leo Roos;Aug 16, 2011) Untested
     public IGrammar<String> buildGrammarFromDefinitions(List<DSLDefinition> dsls) {
 	ArrayList<Class<? extends de.tud.stg.popart.dslsupport.DSL>> clazzes = new ArrayList<Class<? extends de.tud.stg.popart.dslsupport.DSL>>(
 		dsls.size());
 	for (DSLDefinition dsl : dsls) {
 	    Class<? extends de.tud.stg.popart.dslsupport.DSL> loadClass = dsl.getDSLClassChecked();
 	    clazzes.add(loadClass);
 	}
 	return buildGrammar(clazzes);
     }
 
     /**
      * @param clazzes
      * @return the grammar for DSL classes
      * @deprecated use type safe version {@link #buildGrammar(List)} instead
      */
     @Deprecated
     public IGrammar<String> buildGrammar(Class<? extends de.tud.stg.popart.dslsupport.DSL>... clazzes) {
 	List<Class<? extends de.tud.stg.popart.dslsupport.DSL>> clazzess = Arrays.asList(clazzes);
 	return buildGrammar(clazzess);
     }
 
     public IGrammar<String> buildGrammar(List<Class<? extends de.tud.stg.popart.dslsupport.DSL>> clazzes) {
 
 	List<ClassDSLInformation> exInfos = extractClassesInformation(clazzes);
 
 	Grammar grammar = createCombinedGrammar(exInfos);
 
 	return grammar;
     }
 
     private List<ClassDSLInformation> extractClassesInformation(
 	    List<Class<? extends de.tud.stg.popart.dslsupport.DSL>> clazzes) {
 	ArrayList<ClassDSLInformation> result = new ArrayList<ClassDSLInformation>(clazzes.size());
 	for (Class<? extends de.tud.stg.popart.dslsupport.DSL> aClass : clazzes) {
 	    result.add(loadClassInformation(aClass));
 	}
 	return result;
     }
 
     private ClassDSLInformation loadClassInformation(Class<? extends de.tud.stg.popart.dslsupport.DSL> aClass) {
 	ClassDSLInformation classInfo = new ClassDSLInformation(aClass);
 	classInfo.load(DSLInformationDefaults.DEFAULT_CONFIGURATIONOPTIONS_MAP);
 	return classInfo;
     }
 
     private Grammar createCombinedGrammar(List<ClassDSLInformation> classesInfos) {
 
 	// Collect all Categories and rules from the DSL definitions
 
 	LinkedList<GrammarCollectionBox> collectionCollector = new LinkedList<GrammarCollectionBox>();
 
 	for (ClassDSLInformation classInfo : classesInfos) {
 
 	    List<MethodDSLInformation> validMinfs = filterOnlyValidMethodInformations(classInfo);
 
 	    for (MethodDSLInformation methodInfo : validMinfs) {
 
 		GrammarCollectionBox box = new GrammarCollectionBox(methodInfo);
 
 		DslMethodType dslType = methodInfo.getDSLType();
 		switch (dslType) {
 
 		case Literal:
 		    this.handleLiteral(methodInfo, box, getTypeHandlerConfigurationOnGrammar(classesInfos, box));
 		    break;
 
 		case Operation:
 		    this.handleNonLiteral(methodInfo, box, getTypeHandlerConfigurationOnGrammar(classesInfos, box));
 		    break;
 
 		case AbstractionOperator:
 		    throw new UnsupportedOperationException("Functionality not yet implemented for " + dslType);
 		default:
 		    throw illegalForArg(dslType);
 		}
 		collectionCollector.add(box);
 	    }
 	}
 
 	/* Add rule annotations where necessary */
 
 	// build lookup list for relative priorization lookup
 	Map<String, GrammarCollectionBox> uidLookupList = new HashMap<String, GrammarCollectionBox>();
 	for (GrammarCollectionBox box : collectionCollector) {
 	    uidLookupList.put(box.methodInfo.getUniqueIdentifier(), box);
 	}
 	uidLookupList = Collections.unmodifiableMap(uidLookupList);
 	for (GrammarCollectionBox grammarCollectionBox : collectionCollector) {
 	    addRuleAnnotations(grammarCollectionBox, uidLookupList);
 	}
 
 	/*
 	 * Assemble actual grammar from collected and configured rules and
 	 * categories
 	 */
 	Grammar grammar = new Grammar();
 	this.setupGeneralGrammar(grammar);
 
 	boolean waterSupported = isWaterSupported(classesInfos);
 	if (waterSupported) {
 	    this.setWaterEnabled(waterSupported, grammar);
 	}
 
 	for (ClassDSLInformation classInfo : classesInfos) {
 	    this.setupHostLanguageRules(classInfo.getHostLanguageRules(), grammar);
 	}
 
 	for (GrammarCollectionBox box : collectionCollector) {
 	    addBoxContentToGrammar(box, grammar);
 	}
 
 	return grammar;
     }
 
     private ITypeHandler getTypeHandlerConfigurationOnGrammar(List<ClassDSLInformation> exannos,
 	    IGrammar<String> grammar) {
 	TypeHandlerDispatcher typeHandler = new TypeHandlerDispatcher(grammar);
 	for (ClassDSLInformation classInfo : exannos) {
 	    typeHandler.addAdditionalTypeRules(classInfo.getTypeRules());
 	}
 	for (ClassDSLInformation classDSLInformation : exannos) {
 	    typeHandler.configurationOptions(classDSLInformation.getConfigurationOptions());
 	}
 	return typeHandler;
     }
 
     // TODO(Leo_Roos;Sep 1, 2011) should be a functionality of
     // ClassDSLInformation to provide only the valid ones if queried
     private List<MethodDSLInformation> filterOnlyValidMethodInformations(ClassDSLInformation classInfo) {
 	List<MethodDSLInformation> validMinfs = new ArrayList<MethodDSLInformation>();
 	List<MethodDSLInformation> invalidMinfs = new ArrayList<MethodDSLInformation>();
 	for (MethodDSLInformation minf : classInfo.getMethodsInformation()) {
 	    if (minf.isValid())
 		validMinfs.add(minf);
 	    else
 		invalidMinfs.add(minf);
 	}
 
 	if (invalidMinfs.size() > 0) {
 	    logger.info("Ignoring following invalid method configurations: {}", invalidMinfs);
 	}
 	return validMinfs;
     }
 
     private void addRuleAnnotations(GrammarCollectionBox box, Map<String, GrammarCollectionBox> lookupList) {
 	addAbsolutePriorityIfNonDefault(box);
 
 	addPreferencePriorityIfNonDefault(box);
 
 	addAssociativityIfNonDefault(box);
 
 	addPriorityHigherThanAnnotationToRulesIfNonDefault(lookupList, box);
 
 	addBoxAslowerPriorityToPriorityLowerThanRulesIfNonDefault(box, lookupList);
 
     }
 
     private MethodDSLInformation addAbsolutePriorityIfNonDefault(GrammarCollectionBox box) {
 	MethodDSLInformation context = box.methodInfo;
 	int absolutePriority = context.getAbsolutePriority();
 	if (absolutePriority != defaultDSLM.absolutePriority()) {
 	    IRuleAnnotation absolutePriorityAnnotation = new AbsolutePriorityAnnotation(absolutePriority);
 	    addAnnotationTo(box.rules, absolutePriorityAnnotation);
 	}
 	return context;
     }
 
     private void addBoxAslowerPriorityToPriorityLowerThanRulesIfNonDefault(GrammarCollectionBox box,
 	    Map<String, GrammarCollectionBox> lookupList) {
 	String priorityLowerThan = box.methodInfo.getPriorityLowerThan();
 	if (!priorityLowerThan.equals(defaultDSLM.priorityLowerThan())) {
 	    GrammarCollectionBox hasHigherPrio = lookupList.get(priorityLowerThan);
 	    if (hasHigherPrio != null) {
 		RelativePriorityAnnotation relp = newRelativePriorityWithLowerPriorityFor(box);
 		for (IRule<String> iOtherRule : hasHigherPrio.rules) {
 		    iOtherRule.addAnnotation(relp);
 		}
 	    }
 	}
     }
 
     private void addPriorityHigherThanAnnotationToRulesIfNonDefault(Map<String, GrammarCollectionBox> lookupList,
 	    GrammarCollectionBox box) {
 	String priorityHigherThan = box.methodInfo.getPriorityHigherThan();
 	if (!priorityHigherThan.equals(defaultDSLM.priorityHigherThan())) {
 	    GrammarCollectionBox hasLowerPrio = lookupList.get(priorityHigherThan);
 	    if (hasLowerPrio != null) {
 		RelativePriorityAnnotation relp = newRelativePriorityWithLowerPriorityFor(hasLowerPrio);
 		addAnnotationTo(box.rules, relp);
 	    }
 	}
     }
 
     private void addAnnotationTo(List<IRule<String>> rules, @Nullable IRuleAnnotation annotations) {
 	if (annotations == null)
 	    return;
 	addAnnotationsTo(rules, single(annotations));
     }
 
     private void addAnnotationsTo(List<IRule<String>> rules, List<IRuleAnnotation> annotations) {
 	for (IRuleAnnotation iRuleAnnotation : annotations) {
 	    for (IRule<String> rule : rules) {
 		rule.addAnnotation(iRuleAnnotation);
 	    }
 	}
     }
 
     private RelativePriorityAnnotation newRelativePriorityWithLowerPriorityFor(GrammarCollectionBox hasLowerPrio) {
 	RelativePriorityAnnotation relp = new RelativePriorityAnnotation();
 	relp.setLowerPriorityRules(new LinkedHashSet<IRule<String>>(hasLowerPrio.rules));
 	return relp;
     }
 
     private void addBoxContentToGrammar(GrammarCollectionBox box, Grammar grammar) {
 	for (ICategory<String> iterable_element : box.category) {
 	    grammar.addCategory(iterable_element);
 	}
 	for (IRule<String> rule : box.rules) {
 	    grammar.addRule(rule);
 	}
     }
 
     private void addPreferencePriorityIfNonDefault(GrammarCollectionBox box) {
 	PreferencePriority preferencePriority = box.methodInfo.getPreferencePriority();
 	IRuleAnnotation result = null;
 	if (!preferencePriority.equals(defaultDSLM.preferencePriority())) {
 	    switch (preferencePriority) {
 	    case Avoid:
 		result = (new AvoidAnnotation());
 		break;
 	    case Prefer:
 		result = (new PreferAnnotation());
 		break;
 	    case Reject:
 		result = (new RejectAnnotation());
 		break;
 	    default:
 		// nothing
 		break;
 	    }
 	}
 	addAnnotationTo(box.rules, result);
     }
 
     private void addAssociativityIfNonDefault(GrammarCollectionBox box) {
 	Associativity associativity = box.methodInfo.getAssociativity();
 	IRuleAnnotation result = null;
 	switch (associativity) {
 	case LEFT:
 	    result = new AssociativityAnnotation(
 		    de.tud.stg.parlex.core.ruleannotations.AssociativityAnnotation.Associativity.LEFT);
 	    break;
 	case RIGHT:
 	    result = new AssociativityAnnotation(
 		    de.tud.stg.parlex.core.ruleannotations.AssociativityAnnotation.Associativity.RIGHT);
 	    break;
 	default:
 	    break;
 	}
 	addAnnotationTo(box.rules, result);
     }
 
     private final DSLMethod defaultDSLM = DSLInformationDefaults.DEFAULT_DSLMethod;
 
     /*
      * Water is supported as long as every involved DSL supports water.
      * Otherwise it is not supported
      */
     private boolean isWaterSupported(List<ClassDSLInformation> exannos) {
 	for (ClassDSLInformation annos : exannos) {
 	    if (!annos.isWaterSupported()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     private void setupHostLanguageRules(Set<Class<? extends HostLanguageGrammar>> hostLanguageRules, Grammar grammar) {
 	for (Class<? extends HostLanguageGrammar> clazz : hostLanguageRules) {
 	    setupHostLanguage(grammar, clazz);
 	}
     }
 
     private void setupHostLanguage(Grammar grammar, Class<? extends HostLanguageGrammar> clazz) {
 	Constructor<? extends HostLanguageGrammar> constructor = getNullaryConstructor(clazz);
 	if (constructor == null) {
 	    logger.error("Ignoring host language grammar for {}", clazz);
 	    return;
 	}
 	try {
 	    HostLanguageGrammar newInstance = constructor.newInstance();
 	    newInstance.applySpecificGrammar(grammar);
 	} catch (IllegalArgumentException e) {
 	    logger.error("Unexpected Problem, thought I have loaded nullary constructor", e);
 	} catch (InvocationTargetException e) {
 	    logger.error("Underlying constructor threw exception", e);
 	} catch (InstantiationException e) {
 	    logger.error("Class can not be instantiated", e);
 	} catch (IllegalAccessException e) {
 	    logger.error("Access to class has been denied", e);
 	}
     }
 
     private @CheckForNull
     Constructor<? extends HostLanguageGrammar> getNullaryConstructor(Class<? extends HostLanguageGrammar> clazz) {
 	try {
 	    Constructor<? extends HostLanguageGrammar> constructor = clazz.getConstructor();
 	    return constructor;
 	} catch (SecurityException e) {
 	    logger.error("Unexpected Problem. Will not load {}", clazz, e);
 	} catch (NoSuchMethodException e) {
 	    logger.warn("Hostlanguage class has no nullary constructor {}. Can not load it", clazz, e);
 	}
 	return null;
     }
 
     /**
      * collects all rules and categories in order s.t. they can be post
      * processed in a consistent fashion<br>
      * 
      * the rhsMethodCategory is only for type NonLiteral interesting
      * 
      * @author Leo_Roos
      * 
      */
     private static class GrammarCollectionBox implements IGrammar<String> {
 
 	List<ICategory<String>> rhsMethodCategory = new LinkedList<ICategory<String>>();
 	List<ICategory<String>> category = new LinkedList<ICategory<String>>();
 	List<IRule<String>> rules = new LinkedList<IRule<String>>();
 	public final MethodDSLInformation methodInfo;
 
 	public GrammarCollectionBox(MethodDSLInformation methodInfo) {
 	    Assert.isNotNull(methodInfo);
 	    this.methodInfo = methodInfo;
 	}
 
 	@Override
 	public void addCategory(ICategory<String> category) {
 	    this.category.add(category);
 	}
 
 	@Override
 	public void addRule(IRule<String> rule) {
 	    this.rules.add(rule);
 	}
 
 	@Override
 	public void addRules(IRule<String>... rules) {
 	    Collections.addAll(this.rules, rules);
 	}
 
 	@Override
 	public void addCategories(ICategory<String>... categories) {
 	    Collections.addAll(this.category, categories);
 	}
 
 	@Override
 	public void setCategories(Set<ICategory<String>> categories) {
 	    throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Set<ICategory<String>> getCategories() {
 	    throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void setRules(Set<IRule<String>> rules) {
 	    throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Set<IRule<String>> getRules() {
 	    throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void setStartRule(IRule<String> startRule) {
 	    throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public IRule<String> getStartRule() {
 	    throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void addWaterRule(IRule<String> rule) {
 	    throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Set<IRule<String>> getWaterRules() {
 	    throw new UnsupportedOperationException();
 	}
 
     }
 
     private void handleLiteral(MethodDSLInformation extractedMethod, GrammarCollectionBox box, ITypeHandler iTypeHandler) {
 
 	Method method = extractedMethod.getMethod();
 	Map<ConfigurationOptions, String> methodOptions = extractedMethod.getConfigurationOptions();
 
 	String literal = extractedMethod.getProduction();
 
 	ICategory<String> literalCategory = new Category(literal, true);
 	box.addCategory(literalCategory);
 
 	Rule literalRule = new Rule(this.statement, literalCategory);
 	box.addRule(literalRule);
 
 	Class<?> returnType = method.getReturnType();
 	ICategory<String> returnTypeCategory = iTypeHandler.handle(returnType, methodOptions);
 	box.addCategory(returnTypeCategory);
 
 	Rule returnTypeRule = new Rule(returnTypeCategory, literalCategory);
 	box.addRule(returnTypeRule);
 
 	this.methodAliases.put(literal,
 		new MethodOptions(method.getName(), new LinkedList<Integer>(), method.getDeclaringClass()));
     }
 
     public static class MethodOptions {
 
 	private final List<Integer> parameterIndices;
 	private final String methodCallName;
 	private final Class<?> parentClass;
 
 	public MethodOptions(String methodCallName, List<Integer> parameterIndices, Class<?> clazz) {
 	    this.methodCallName = methodCallName;
 	    this.parameterIndices = parameterIndices;
 	    this.parentClass = clazz;
 	}
 
 	public List<Integer> getParamaterIndices() {
 	    return this.parameterIndices;
 	}
 
 	public String getMethodCallName() {
 	    return this.methodCallName;
 	}
 
 	public Class<?> getParentClass() {
 	    return this.parentClass;
 	}
     }
 
     private void handleNonLiteral(MethodDSLInformation methodInfo, GrammarCollectionBox box, ITypeHandler typeHandler) {
 
 	Method method = methodInfo.getMethod();
 
 	String methodProduction = methodInfo.getProduction();
 
 	Map<ConfigurationOptions, String> configurationOptions = methodInfo.getConfigurationOptions();
 
 	String parameterEscape = configurationOptions.get(ConfigurationOptions.PARAMETER_ESCAPE);
 	String whitespaceEscape = configurationOptions.get(ConfigurationOptions.WHITESPACE_ESCAPE);
 
 	MethodProductionScanner mps = new MethodProductionScanner();
 	mps.setParameterEscape(parameterEscape);
 	mps.setWhitespaceEscape(whitespaceEscape);
 
 	mps.startScan(methodProduction);
 
 	if (!mps.hasNext())
 	    return;
 
 	int index = 0;
 	List<Integer> parameterIndices = new ArrayList<Integer>();
 
 	while (mps.hasNext()) {
 	    MethodProductionElement next = mps.next();
 	    ProductionElement productionElementType = next.getProductionElementType();
 	    switch (productionElementType) {
 	    case Keyword:
 		handleProductionElementKeyword(box, next);
 		break;
 	    case Parameter:
 		ParameterElement pe = (ParameterElement) next;
 		ParameterDSLInformation parameterInfo = methodInfo.getParameterInfo(pe.getParsedParameterNumber());
 		if (parameterInfo == null) {
 		    logger.error("Could not determine parameter of index {}. Will ignore it and build grammar without it"
 			    + index);
 		    // TODO(Leo_Roos;Sep 1, 2011) perhaps better to throw
 		    // exception?
 		    break;
 		}
 		handleProductionElementParameter(box, typeHandler, parameterInfo);
 		parameterIndices.add(index);
 		break;
 	    case Whitespace:
 		handleProductionElementWhitespace(box, (WhitespaceElement) next);
 		break;
 	    default:
 		throwIllegalArgFor(productionElementType);
 	    }
 	    index++;
 	}
 
 	int methodCounter = this.parameterCounter.getAndIncrement();
 
 	String indexedMethodProduction = "M" + methodCounter + "{[" + methodProduction + "]" + method.getName() + "}";
 	MethodOptions value = new MethodOptions(method.getName(), parameterIndices, method.getDeclaringClass());
 	this.methodAliases.put(indexedMethodProduction, value);
 
 	ICategory<String> methodCategory = new Category(indexedMethodProduction, false);
 	box.addCategory(methodCategory);
 
 	boolean toplevel = methodInfo.isToplevel();
 	if (toplevel) {
 	    Rule methodRule = new Rule(this.statement, methodCategory);
 	    box.addRule(methodRule);
 	}
 
 	Rule rule = new Rule(methodCategory, box.rhsMethodCategory);
 	box.addRule(rule);
 
 	// XXX(Leo_Roos;Sep 1, 2011) i leave it shortly to see if it all works
 	for (ICategory<String> c : box.rhsMethodCategory) {
 	    box.addCategory(c);
 	}
 
 	if (methodInfo.hasReturnValue()) {
 	    ICategory<String> returnTypeCategory = typeHandler.handle(method.getReturnType(), configurationOptions);
 	    box.addCategory(returnTypeCategory);
 
 	    ICategory<String> typeCategory = new Category(CategoryNames.RETURNTYPE_CATEGORY, false);
 	    Rule typeToMethod = new Rule(typeCategory, methodCategory);
 	    box.addRule(typeToMethod);
 
 	    Rule returnTypeToMethod = new Rule(returnTypeCategory, methodCategory);
 	    box.addRule(returnTypeToMethod);
 	}
 
     }
 
     private void handleProductionElementKeyword(GrammarCollectionBox box, MethodProductionElement next) {
 	String keyword = next.getCapturedString();
 	keyword = getUnicodeRepresentationOrKeyword(keyword);
 	box.rhsMethodCategory.add(new Category(keyword, true));
 	// this.keywords.add(keyword);
     }
 
     private void handleProductionElementWhitespace(GrammarCollectionBox box, WhitespaceElement we) {
 	ICategory<String> whitespaceCategory;
 	List<Rule> wsRules;
 	if (we.isOptional()) {
 	    whitespaceCategory = WhitespaceCategoryDefinition.getNewOptionalWhitespaceCategory();
 	    wsRules = WhitespaceCategoryDefinition.getOptionalWhitespaceRules();
 	} else {
 	    whitespaceCategory = WhitespaceCategoryDefinition.getNewRequiredWhitespaceCategory();
 	    wsRules = WhitespaceCategoryDefinition.getRequiredWhitespaceRules();
 	}
 	box.rhsMethodCategory.add(whitespaceCategory);
 	box.category.add(whitespaceCategory);
 	box.rules.addAll(wsRules);
     }
 
     private String getUnicodeRepresentationOrKeyword(String keyword) {
 	String uniChar = unicodeLookupTable.nameToUnicode(keyword);
 	if (uniChar == null) {
 	    return keyword;
 	} else {
 	    logger.trace("found unicode representation [{}] for [{}]", uniChar, keyword);
 	    return uniChar;
 	}
     }
 
     private void handleProductionElementParameter(GrammarCollectionBox box, ITypeHandler typeHandler,
 	    ParameterDSLInformation parameterInfo) {
 
 	// The following string is actually built for use in the ATerm framework
 	String param = "P" + parameterInfo.getIndex() + "{" + this.parameterCounter.getAndIncrement() + "}";
 	ICategory<String> parameterCategory = new Category(param, false);
 	box.rhsMethodCategory.add(parameterCategory);
 
 	Map<ConfigurationOptions, String> parameterOptions = parameterInfo.getConfigurationOptions();
 	ICategory<String> parameterMapping = typeHandler.handle(parameterInfo.getType(), parameterOptions);
 	Rule rule = new Rule(parameterCategory, parameterMapping);
 
 	ICategory<String> typeCategory = new Category(CategoryNames.PARAMETERTYPE_CATEGORY, false);
 	Rule typeRule = new Rule(parameterMapping, typeCategory);
 
 	box.addRule(rule);
 	box.addRule(typeRule);
 	box.addCategory(parameterMapping);
 
     }
 
     public Map<String, MethodOptions> getMethodOptions() {
 	return Collections.unmodifiableMap(methodAliases);
     }
 
 //@formatter:off
 // TODO(Leo_Roos;Sep 1, 2011) delete when sure that no lost treasure is buried somewhere down there ...
 // ======================================================================================================
 /*
     // private void handleConstructor(Constructor<?> constructor, String
     // methodParameterEscape,
     // String methodWhitespaceEscape) {
     // String methodProduction = this.getMethodProduction(constructor,
     // constructor.getName());
     //
     // Pattern[] pattern = this.getPattern(methodParameterEscape,
     // methodWhitespaceEscape);
     //
     // this.methodAliases.put(methodProduction, new Pair<String,
     // Pattern[]>(constructor.getName(), pattern));
     //
     // Class<?>[] parameters = constructor.getParameterTypes();
     //
     // this.handleNonLiteral(methodProduction, constructor.getClass(),
     // parameters, pattern);
     // }
 
     // private static final String DEFAULT_STRING_QUOTATION =
     // "([\\w_]+|(\".*?\"))";
      
 //From handleProductionElementParameter
  	/*
 	 * categories.add(parameterCategory);
 	 * 
 	 * DSL parameterDSLAnnotation = null; for (Annotation a : pAnnotations)
 	 * { if (a instanceof DSL) { parameterDSLAnnotation = (DSL) a; break; }
 	 * }
 	 * 
 	 * Map<ConfigurationOptions, String> parameterOptions =
 	 * getAnnotationParameterOptionsOverInitialMap( parameterDSLAnnotation,
 	 * methodOptions);
 	 * 
 	 * ICategory<String> parameterMapping = typeHandler.handle(
 	 * parameterType, parameterOptions); Rule rule = new
 	 * Rule(parameterCategory, parameterMapping);
 	 */
     
 //  /*
 //  * assigns value with key to resultMap if the value is neither null nor
 //  * equal to the UNASSIGNED constant.
 //  */
 // private static Map<ConfigurationOptions, String> putIfValid(Map<ConfigurationOptions, String> resultMap,
 //	    ConfigurationOptions confOption, String value) {
 //	Assert.isNotNull(value);
 //	if (value.equals(AnnotationConstants.UNASSIGNED))
 //	    return resultMap;
 //	else {
 //	    resultMap.put(confOption, value);
 //	    return resultMap;
 //	}
 // }
  
     // Never used; still necessary to save the
     // resulting set of keywords?
     //private final Set<String> keywords = new LinkedHashSet<String>();
 
 //@formatter:on
     // ===================================
 
 }
