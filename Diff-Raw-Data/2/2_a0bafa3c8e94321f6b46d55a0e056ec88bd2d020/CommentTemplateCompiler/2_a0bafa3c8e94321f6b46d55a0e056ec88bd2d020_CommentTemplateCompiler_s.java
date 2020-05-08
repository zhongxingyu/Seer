 /*******************************************************************************
  * Copyright (c) 2006-2012
  * Software Technology Group, Dresden University of Technology
  * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany;
  *   DevBoost GmbH - Berlin, Germany
  *      - initial API and implementation
  ******************************************************************************/
 package de.devboost.commenttemplate.compiler;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.emftext.commons.layout.LayoutInformation;
 import org.emftext.language.java.annotations.AnnotationAttributeSetting;
 import org.emftext.language.java.annotations.AnnotationInstance;
 import org.emftext.language.java.annotations.AnnotationParameter;
 import org.emftext.language.java.annotations.AnnotationParameterList;
 import org.emftext.language.java.annotations.AnnotationValue;
 import org.emftext.language.java.annotations.SingleAnnotationParameter;
 import org.emftext.language.java.classifiers.ConcreteClassifier;
 import org.emftext.language.java.commons.Commentable;
 import org.emftext.language.java.containers.CompilationUnit;
 import org.emftext.language.java.expressions.Expression;
 import org.emftext.language.java.imports.ClassifierImport;
 import org.emftext.language.java.imports.Import;
 import org.emftext.language.java.instantiations.InstantiationsFactory;
 import org.emftext.language.java.instantiations.NewConstructorCall;
 import org.emftext.language.java.members.ClassMethod;
 import org.emftext.language.java.members.Constructor;
 import org.emftext.language.java.members.Field;
 import org.emftext.language.java.members.Member;
 import org.emftext.language.java.members.Method;
 import org.emftext.language.java.modifiers.AnnotableAndModifiable;
 import org.emftext.language.java.modifiers.AnnotationInstanceOrModifier;
 import org.emftext.language.java.references.IdentifierReference;
 import org.emftext.language.java.references.MethodCall;
 import org.emftext.language.java.references.Reference;
 import org.emftext.language.java.references.ReferenceableElement;
 import org.emftext.language.java.references.ReferencesFactory;
 import org.emftext.language.java.references.StringReference;
 import org.emftext.language.java.resource.java.IJavaTextResource;
 import org.emftext.language.java.resource.java.IJavaTextScanner;
 import org.emftext.language.java.resource.java.IJavaTextToken;
 import org.emftext.language.java.resource.java.mopp.JavaMetaInformation;
 import org.emftext.language.java.resource.java.mopp.JavaPrinter2;
 import org.emftext.language.java.resource.java.util.JavaLayoutUtil;
 import org.emftext.language.java.statements.Conditional;
 import org.emftext.language.java.statements.ExpressionStatement;
 import org.emftext.language.java.statements.ForEachLoop;
 import org.emftext.language.java.statements.ForLoop;
 import org.emftext.language.java.statements.LocalVariableStatement;
 import org.emftext.language.java.statements.Return;
 import org.emftext.language.java.statements.Statement;
 import org.emftext.language.java.statements.StatementListContainer;
 import org.emftext.language.java.statements.StatementsFactory;
 import org.emftext.language.java.types.ClassifierReference;
 import org.emftext.language.java.types.Type;
 import org.emftext.language.java.types.TypeReference;
 import org.emftext.language.java.types.TypesFactory;
 import org.emftext.language.java.variables.LocalVariable;
 import org.emftext.language.java.variables.Variable;
 import org.emftext.language.java.variables.VariablesFactory;
 
 import de.devboost.commenttemplate.CommentTemplate;
 import de.devboost.commenttemplate.CommentTemplatePlugin;
 import de.devboost.commenttemplate.LineBreak;
 import de.devboost.commenttemplate.ReplacementRule;
 import de.devboost.commenttemplate.VariableAntiQuotation;
 
 /**
  * The {@link CommentTemplateCompiler} is used to transform Java classes that 
  * contain methods with the <code>CommentTemplate</code> annotations (i.e.,
  * code generator classes) to Java classes where the comments in the template
  * methods are replaced by StringBuilders.
  */
 public class CommentTemplateCompiler {
 	
 	public static final String SRC_GEN_FOLDER = "src-gen-comment-template";
 	public static final String SOURCE_SUFFIX = "Source";
 	public static final String DEFAULT_LINE_BREAK = "\n";
 
 	private static final String LINE_BREAK_REGEX = "(\\\r\\\n|\\\r|\\\n)";
 
 	private interface AddStatementOperation {
 		public void execute();
 	}
 	
 	/**
 	 * A {@link CommentUnit} is a list of comments together with the statement
 	 * before these comments appear in the source code. The statement is 
 	 * required because we need to insert a call to the append() method of the
 	 * StringBuilder in front of the statement.
 	 */
 	private class CommentUnit {
 
 		private List<String> comments;
 		private StatementListContainer statementListContainer;
 		private EObject statement;
 
 		public CommentUnit(Commentable commentable, List<String> comments) {
 			this.comments = comments;
 			findParents(commentable);
 		}
 
 		public List<String> getComments() {
 			return comments;
 		}
 
 		public StatementListContainer getStatementListContainer() {
 			return statementListContainer;
 		}
 		
 		private void findParents(Commentable commentable) {
 			this.statement = null;
 			EObject eContainer = commentable;
 			while (eContainer != null && !(eContainer instanceof StatementListContainer)) {
 				statement = eContainer;
 				eContainer = eContainer.eContainer();
 			}
 			if (eContainer instanceof StatementListContainer) {
 				this.statementListContainer = (StatementListContainer) eContainer;
 			}
 		}
 
 		public EObject getStatement() {
 			return statement;
 		}
 		
 		@Override
 		public String toString() {
 			return comments.toString();
 		}
 	}
 
 	private ConcreteClassifier commentTemplateAnnotation;
 	private ConcreteClassifier replacementRuleAnnotation;
 	private ConcreteClassifier variableAntiQuotationAnnotation;
 	private ConcreteClassifier lineBreakAnnotation;
 	private String lineBreak;
 
 	public CommentTemplateCompiler() {
 		super();
 	}
 	
 	public Resource compileAndSave(URI sourceURI, ResourceSet resourceSet) {
 		Resource resource = null;
 		try {
 			resource = resourceSet.getResource(sourceURI, true);	
 		} catch (Exception e) {
 			CommentTemplatePlugin.logError("Exception while compiling template class.", e);
 		}
 		
 		if (resource == null) {
 			return null;
 		}
 		return compileAndSave(resource, new LinkedHashSet<String>());
 	}
 	
 	public Resource compileAndSave(Resource resource, Set<String> brokenVariableReferences) {
 		boolean success = compile(resource, brokenVariableReferences);
 		Resource compiledResource = null;
 		if (success) {
 			compiledResource = save(resource);
 		}
 		return compiledResource;
 	}
 
 	public boolean compile(Resource resource, Set<String> brokenVariableReferences) {
 		if (resource.getContents().isEmpty()) {
 			return false;
 		}
 		if (!(resource.getContents().get(0) instanceof CompilationUnit)) {
 			return false;
 		}
 		CompilationUnit cu = (CompilationUnit) resource.getContents().get(0);
 		
 		getAnnotationClasses(cu);
 		
 		ConcreteClassifier classifier = cu.getClassifiers().get(0);
 
 		Set<AnnotationInstance> annotationsToRemove = new LinkedHashSet<AnnotationInstance>();
 		boolean containsCommentTemplateMethods = false;
 		for (Method method : classifier.getMethods()) {
 			containsCommentTemplateMethods |= compileMethod(method, annotationsToRemove, brokenVariableReferences);
 		}
 		
 		for (AnnotationInstance annotationToRemove : annotationsToRemove) {
 			EcoreUtil.remove(annotationToRemove);
 		}
 		removeImports(cu);
 		return containsCommentTemplateMethods;
 	}
 
 	private void getAnnotationClasses(CompilationUnit cu) {
 		commentTemplateAnnotation = 
 				cu.getConcreteClassifier(CommentTemplate.class.getName());
 		
 		replacementRuleAnnotation = 
 				cu.getConcreteClassifier(ReplacementRule.class.getName());
 		
 		variableAntiQuotationAnnotation = 
 				cu.getConcreteClassifier(VariableAntiQuotation.class.getName());
 		
 		lineBreakAnnotation = 
 				cu.getConcreteClassifier(LineBreak.class.getName());
 	}
 
 	private void removeImports(CompilationUnit cu) {
 		for (Iterator<Import> i = cu.getImports().iterator(); i.hasNext(); ) {
 			Import imp = i.next();
 			if (imp instanceof ClassifierImport) {
 				ConcreteClassifier importedClassifier = ((ClassifierImport) imp).getClassifier();
 				if (importedClassifier.equals(commentTemplateAnnotation)) {
 					i.remove();
 				} else if (importedClassifier.equals(replacementRuleAnnotation)) {
 					i.remove();
 				} else if (importedClassifier.equals(variableAntiQuotationAnnotation)) {
 					i.remove();
 				} else if (importedClassifier.equals(lineBreakAnnotation)) {
 					i.remove();
 				}
 			}
 		}
 	}
 
 	private Resource save(Resource resource) {
 		String compiledClassName = getNameForCompilationResult(resource);
 		URI compiledURI = resource.getURI().trimSegments(1).appendSegment(compiledClassName).appendFileExtension("java");
 		compiledURI = URI.createURI(compiledURI.toString().replaceFirst("/src/", "/" + SRC_GEN_FOLDER + "/")); //TODO needs to be more precise
 		Resource compiledResource = resource.getResourceSet().createResource(compiledURI);
 		compiledResource.getContents().addAll(resource.getContents());
 		try {
 			CompilationUnit cu = (CompilationUnit) compiledResource.getContents().get(0);
 			List<ConcreteClassifier> classifiers = cu.getClassifiers();
 			if (!classifiers.isEmpty()) {
 				ConcreteClassifier mainClassifier = classifiers.get(0);
 				renameClassifier(mainClassifier, compiledClassName);
 			}
 			
 			compiledResource.save(null);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return compiledResource;
 	}
 
 	private void renameClassifier(ConcreteClassifier classifier,
 			String newClassName) {
 		// fix the name of the compilation result (class must be renamed)
 		classifier.setName(newClassName);
 		// fix the names of the constructors (must match the new class name)
 		List<Constructor> constructors = classifier.getConstructors();
 		for (Constructor constructor : constructors) {
 			constructor.setName(newClassName);
 		}
 	}
 
 	private String getNameForCompilationResult(Resource resource) {
 		String compiledClassName = resource.getURI().trimFileExtension().lastSegment();
 		compiledClassName = compiledClassName.substring(0, compiledClassName.length() - SOURCE_SUFFIX.length());
 		return compiledClassName;
 	}
 
 	private boolean compileMethod(Method method, Set<AnnotationInstance> annotationsToRemove, Set<String> brokenVariableReferences) {
 		setLineBreak(method, annotationsToRemove);
 		
 		AnnotationInstance commentTemplateAnnotationInstance = getAnnotationInstance(method, commentTemplateAnnotation);
 		if (commentTemplateAnnotationInstance != null && method instanceof ClassMethod) {
 			List<AnnotationInstance> replacementRules = getReplacementRules(method);
 			List<AnnotationInstance> variableAntiQuotations = getVariableAntiQuotations(method);
 			AnnotationInstance variableAntiQuotation = null;
 			if (!variableAntiQuotations.isEmpty()) {
 				variableAntiQuotation = variableAntiQuotations.get(0);
 				// TODO add warning if there is multiple VariableAntiQuotation annotations
 			}
 			compileCommentTemplateMethod((ClassMethod) method, replacementRules, variableAntiQuotation, brokenVariableReferences);
 
 			// we schedule the annotations for removal, but we not remove them 
 			// right away, because the annotations that apply to this method may
 			// stem from the enclosing class and apply to other methods too.
 			// thus, we cannot remove them before all methods have been 
 			// processed.
 			annotationsToRemove.add(commentTemplateAnnotationInstance);
 			annotationsToRemove.addAll(variableAntiQuotations);
 			annotationsToRemove.addAll(replacementRules);
 			
 			return true;
 		}
 		return false;
 	}
 
 	private void setLineBreak(Method method, Set<AnnotationInstance> annotationsToRemove) {
 		this.lineBreak = DEFAULT_LINE_BREAK;
 		AnnotationInstance lineBreakAnnotationInstance = getAnnotationInstance(method, lineBreakAnnotation);
 		if (lineBreakAnnotationInstance == null) {
 			lineBreakAnnotationInstance = getAnnotationInstance(method.getContainingConcreteClassifier(), lineBreakAnnotation);
 		}
 		if (lineBreakAnnotationInstance != null) {
 			annotationsToRemove.add(lineBreakAnnotationInstance);
 			String lineBreakCharacters = getStringValue(lineBreakAnnotationInstance);
 			if (lineBreakCharacters != null) {
 				this.lineBreak = lineBreakCharacters;
 			}
 		}
 	}
 
 	/**
 	 * Searches for all replacement rule annotations that apply to the given
 	 * method.
 	 * 
 	 * @param method
 	 * @return
 	 */
 	private List<AnnotationInstance> getReplacementRules(Method method) {
 		return getAnnotationInstances(method, replacementRuleAnnotation);
 	}
 
 	private List<AnnotationInstance> getVariableAntiQuotations(Method method) {
 		return getAnnotationInstances(method, variableAntiQuotationAnnotation);
 	}
 
 	private List<AnnotationInstance> getAnnotationInstances(Method method, ConcreteClassifier annotationType) {
 		List<AnnotationInstance> instances = new ArrayList<AnnotationInstance>();
 
 		EObject next = method;
 		while (next != null) {
 			if (next instanceof AnnotableAndModifiable) {
 				AnnotableAndModifiable annotableAndModifiable = (AnnotableAndModifiable) next;
 				AnnotationInstance annotationInstance = getAnnotationInstance(annotableAndModifiable, annotationType);
 				if (annotationInstance != null) {
 					instances.add(annotationInstance);
 				}
 			}
 			next = next.eContainer();
 		}
 		return instances;
 	}
 
 	private void compileCommentTemplateMethod(ClassMethod m, List<AnnotationInstance> replacementRules, AnnotationInstance variableAntiQuotation, Set<String> brokenVariableReferences) {
 		ConcreteClassifier sbClass = m.getConcreteClassifier(StringBuilder.class.getName());
 		
 		LocalVariableStatement lvs = StatementsFactory.eINSTANCE.createLocalVariableStatement();
 		LocalVariable lv = VariablesFactory.eINSTANCE.createLocalVariable();
 		
 		lv.setTypeReference(createTypeReference(sbClass));
 		lv.setName("__content");
 		
 		NewConstructorCall ncc = InstantiationsFactory.eINSTANCE.createNewConstructorCall();
 		ncc.setTypeReference(createTypeReference(sbClass));
 		lv.setInitialValue(ncc);
 		
 		lvs.setVariable(lv);
 		m.getStatements().add(0, lvs);
 		
 		convertCommentsToStrings(m, lv, replacementRules, variableAntiQuotation, brokenVariableReferences);
 		
 		Statement lastStatement = m.getStatements().get(m.getStatements().size() - 1);
 		if (!(lastStatement instanceof Return)) {
 			return;
 		}
 		
 		Return returnStatement = (Return)  lastStatement;
 		
 		IdentifierReference ir = createReference(lv);
 		MethodCall mc = createMethodCall((Method) sbClass.getMembersByName("toString").get(0));
 		ir.setNext(mc);
 		returnStatement.setReturnValue(ir);
 	}
 
 	private void convertCommentsToStrings(ClassMethod method, LocalVariable stringBuilder, List<AnnotationInstance> replacementRules, AnnotationInstance variableAntiQuotation, Set<String> brokenVariableReferences) {
 		List<AddStatementOperation> operations = new ArrayList<CommentTemplateCompiler.AddStatementOperation>();
 	 	
 		List<Variable> stringFields = getFields(method);
 		List<Variable> stringVariables = getLocalStringVariables(method);
 		List<Variable> visibleStringVariables = new ArrayList<Variable>();
 		visibleStringVariables.addAll(stringFields);
 		visibleStringVariables.addAll(stringVariables);
 		
 		// find leading tabs for first comment
 		int leadingTabs = determineTabsBeforeFirstComment(method);
 		
 		boolean endedWithLineBreak = true;
 		
 		List<CommentUnit> orderedCommentUnits = getOrderedCommentables(method);
 		orderedCommentUnits = removeEmptyUnits(orderedCommentUnits);
 		for (CommentUnit commentUnit : orderedCommentUnits) {
 			StatementListContainer container = commentUnit.getStatementListContainer();
 			if (container == null) {
 				continue;
 			}
 			endedWithLineBreak = computeCompilationOperationsForCommentUnit(
 					stringBuilder,
 					replacementRules, 
 					variableAntiQuotation,
 					operations, 
 					visibleStringVariables, 
 					leadingTabs,
 					endedWithLineBreak, 
 					commentUnit,
 					brokenVariableReferences);
 		}
 		
 		for (AddStatementOperation op : operations) {
 			op.execute();
 		}
 	}
 
 	/**
 	 * Removes all comment units that do not contain actual comments.
 	 */
 	private List<CommentUnit> removeEmptyUnits(List<CommentUnit> units) {
 		List<CommentUnit> nonEmptyUnits = new ArrayList<CommentUnit>();
 		for (CommentUnit unit : units) {
 			if (!unit.getComments().isEmpty()) {
 				nonEmptyUnits.add(unit);
 			}
 		}
 		return nonEmptyUnits;
 	}
 
 	/**
 	 * Returns all comments that are attached to the given element or its 
 	 * children. The comments are returned in the order in which they appear in
 	 * the textual representation of the given model (i.e., the Java source 
 	 * code).
 	 */
 	private List<CommentUnit> getOrderedCommentables(EObject element) {
 		final List<CommentUnit> units = new ArrayList<CommentUnit>();
 		// we use a modified version of the printer to retrieve all comments in
 		// the exact order they appear in the source code. while this may sound
 		// crazy, the printer is only entity that is smart enough for this job.
 		new JavaLayoutUtil().transferAllLayoutInformationFromModel(element);
 		JavaPrinter2 printer = new JavaPrinter2(new ByteArrayOutputStream(), (IJavaTextResource) element.eResource()) {
 			
 			public void printSmart(java.io.PrintWriter writer) throws IOException {
 				for (JavaPrinter2.PrintToken token : tokenOutputStream) {
 					if (token.getTokenName() != null) {
 						// skip all visible tokens
 						continue;
 					}
 					String text = token.getText();
 					List<String> multiLineComments = splitTextToComments(text);
 					EObject container = token.getContainer();
 					if (container != null && container instanceof Commentable) {
 						Commentable commentable = (Commentable) container;
 						units.add(new CommentUnit(commentable, multiLineComments));
 					}
 				}
 			}
 		};
 		try {
 			printer.print(element);
 		} catch (IOException ioe) {
 			CommentTemplatePlugin.logError("IOException while printing template fragment.", ioe);
 		}
 		return units;
 	}
 
 	private boolean computeCompilationOperationsForCommentUnit(
 			final LocalVariable stringBuilder,
 			List<AnnotationInstance> replacementRules,
 			AnnotationInstance variableAntiQuotation,
 			List<AddStatementOperation> operations,
 			List<Variable> visibleStringVariables, int leadingTabs,
 			boolean endedWithLineBreak, 
 			CommentUnit commentUnit,
 			Set<String> brokenVariableReferences) {
 		
 		
 		List<String> comments = commentUnit.getComments();
 		final StatementListContainer container = commentUnit.getStatementListContainer();
 		
 		int leadingTabsForContainer = leadingTabs + countDepth(container);
 		for (int c = 0; c < comments.size(); c++) {
 			String comment = comments.get(c);
 			
 			int tabsToRemove = leadingTabsForContainer;
 			final List<Expression> stringExpressions = convertCommentToStringExpressions(container, comment, visibleStringVariables, replacementRules, variableAntiQuotation, tabsToRemove, endedWithLineBreak);
 			findBrokenReferences(stringExpressions, variableAntiQuotation, brokenVariableReferences);
 			endedWithLineBreak = endsWithLineBreak(comment);
 			final EObject theElement = commentUnit.getStatement();
 			operations.add(new AddStatementOperation() {
 				@Override
 				public void execute() {
 					for (Expression stringExpression : stringExpressions) {
 						final Statement statement = createAppendCall(stringBuilder, stringExpression);
 						if (theElement == null) {
 							container.getStatements().add(statement);
 						} else {
 							int idx = container.getStatements().indexOf(theElement);
 							container.getStatements().add(idx, statement);
 						}
 					}
 				}
 			});
 		}
 		return endedWithLineBreak;
 	}
 
 	/**
 	 * Counts the number of tab characters before the first multi-line comment
 	 * in the given method. This number is important, because it determines the
 	 * left border for all subsequent comments.
 	 */
 	private int determineTabsBeforeFirstComment(ClassMethod m) {
 		int leadingTabs = 0;
 		EList<Commentable> commentables = m.getChildrenByType(Commentable.class);
 		for (Commentable commentable : commentables) {
 			List<String> comments = getRawMLComments(commentable, false);
 			if (comments.isEmpty()) {
 				continue;
 			}
 			String comment = comments.get(0);
 			comment = removeLeadingBreaks(comment);
 			leadingTabs = countLeadingTabsInComment(comment);
 			break;
 		}
 		return leadingTabs;
 	}
 
 	private List<Variable> getLocalStringVariables(ClassMethod m) {
 		List<Variable> result = new ArrayList<Variable>();
 		TreeIterator<EObject> eAllContents = m.eAllContents();
 		while (eAllContents.hasNext()) {
 			EObject eObject = (EObject) eAllContents.next();
 			if (eObject instanceof Variable) {
 				Variable variable = (Variable) eObject;
 				if (m.getStringClass().equals(variable.getTypeReference().getTarget())) {
 					result.add(variable);
 				}
 			}
 		}
 		return result;
 	}
 
 	private boolean endsWithLineBreak(String comment) {
 		int endIndex = comment.indexOf("*/");
 		for (int i = endIndex - 1; i >= 0; i--) {
 			char charAtI = comment.charAt(i);
 			if ('\t' == charAtI || ' ' == charAtI) {
 				continue;
 			} else if ('\r' == charAtI) {
 				return true;
 			} else if ('\n' == charAtI) {
 				return true;
 			} else {
 				break;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Counts the nesting depth of the given {@link StatementListContainer}. 
 	 * This number is important, because for each nesting level a tab character
 	 * must be removed from the comments as this level.
 	 */
 	private int countDepth(StatementListContainer container) {
 		int depth = 0;
 		EObject parent = container;
 		while (parent != null && !(parent instanceof Method)) {
 			if (parent instanceof ForLoop ||
 				parent instanceof ForEachLoop ||
 				parent instanceof Conditional) {
 				depth++;
 			}
 			parent = parent.eContainer();
 		}
 		return depth;
 	}
 
 	/**
	 * Returns all field that are provided by the classifier that contains the
 	 * given method. These fields are accessible from within templates.
 	 */
 	private List<Variable> getFields(ClassMethod method) {
 		ConcreteClassifier enclosingClass = method.getParentByType(ConcreteClassifier.class);
 		List<Field> fields = enclosingClass.getFields();
 		List<Variable> result = new ArrayList<Variable>();
 		for (Field field : fields) {
 			result.add(field);
 		}
 		return result;
 	}
 
 	private List<Expression> convertCommentToStringExpressions(
 			StatementListContainer container,
 			String comment, 
 			List<Variable> declaredStringVariables, 
 			List<AnnotationInstance> replacementRules,
 			AnnotationInstance variableAntiQuotation, 
 			int leadingTabs, boolean endedWithLineBreak) {
 		//UNICODE ESCAPE (see issue #1833)
 		comment = comment.replaceAll("\\\\u", "\\u");
 		//
 		
 		List<Expression> result = new ArrayList<Expression>();
 		
 		List<String> linesToPrint = getLinesToPrint(comment, leadingTabs, endedWithLineBreak);
 		Iterator<String> iterator = linesToPrint.iterator();
 		while (iterator.hasNext()) {
 			String lineToPrint = iterator.next();
 			if (iterator.hasNext()) {
 				lineToPrint = lineToPrint + lineBreak;
 			}
 
 			lineToPrint = applyReplacementRules(lineToPrint, replacementRules);
 			StringReference stringReference = ReferencesFactory.eINSTANCE.createStringReference();
 			stringReference.setValue(lineToPrint);
 			result.add(stringReference);
 		}
 		
 		//reference
 		Iterator<Expression> i = result.iterator();
 		while (i.hasNext()) {
 			Expression exp = i.next();
 			if (exp instanceof StringReference) {
 				StringReference stringReference2 = (StringReference) exp;
 				String commentPart = stringReference2.getValue();
 				for (Variable variable : declaredStringVariables) {
 					String variableName = variable.getName();
 					variableName = applyVariableAntiQuotationPattern(variableName, variableAntiQuotation);
 					int idx = commentPart.indexOf(variableName);
 					if (idx != -1) {
 						StringReference stringReference1 = ReferencesFactory.eINSTANCE.createStringReference();
 						
 						// determine part of line before the variable
 						String textBeforeVariable = commentPart.substring(0, idx);
 						stringReference1.setValue(textBeforeVariable);
 						
 						int tabsInFront = countLeadingTabs(textBeforeVariable, textBeforeVariable.length());
 
 						// remove last line break of variable content
 						
 						IdentifierReference variableReference = ReferencesFactory.eINSTANCE.createIdentifierReference();
 						variableReference.setTarget(variable);
 						Reference variableAccess = createReplaceMethodCall(container, variableReference, "replaceAll", lineBreak.replace("\n", "\\n").replace("\r", "\\r") + "\\z", "");
 						if (tabsInFront > 0) {
 							// add more tabs
 							StringBuilder tabs = new StringBuilder();
 							for (int t = 0; t < tabsInFront; t++) {
 								tabs.append("\t");
 							}
 							String replacement = lineBreak + tabs.toString();
 							variableAccess = createReplaceMethodCall(container, variableAccess, "replace", lineBreak, replacement);
 						}
 						
 						//split string
 						String textAfterVariable = commentPart.substring(idx + variableName.length());
 						stringReference2.setValue(textAfterVariable);
 						
 						//compose
 						int currentIdx = result.indexOf(exp);
 						result.add(currentIdx, variableReference);
 						result.add(currentIdx, stringReference1);
 						
 						//new iterator
 						i = result.iterator();
 						break;
 					}
 				}
 			}
 		}
 		return result;	
 	}
 
 	private Reference createReplaceMethodCall(StatementListContainer container,
 			Reference reference,
 			String methodName,
 			String pattern, String replacement) {
 		Reference variableAccess;
 		StringReference patternReference = ReferencesFactory.eINSTANCE.createStringReference();
 		patternReference.setValue(pattern);
 		StringReference replacementReference = ReferencesFactory.eINSTANCE.createStringReference();
 		replacementReference.setValue(replacement);
 		
 		EList<Expression> arguments = new BasicEList<Expression>();
 		arguments.add(patternReference);
 		arguments.add(replacementReference);
 		
 		// temporary add string references to resource set.
 		// otherwise the String class cannot be resolved
 		container.eResource().getContents().add(patternReference);
 		container.eResource().getContents().add(replacementReference);
 		variableAccess = createMethodCall(reference, container.getStringClass(), methodName, arguments);
 		if (variableAccess == null) {
 			return reference;
 		}
 		container.eResource().getContents().remove(patternReference);
 		container.eResource().getContents().remove(replacementReference);
 		return variableAccess;
 	}
 
 	// TODO move to JaMoPP metamodel
 	private Reference createMethodCall(
 			Reference reference,
 			Type type, 
 			String methodName,
 			List<Expression> arguments) {
 
 		MethodCall methodCall = ReferencesFactory.eINSTANCE.createMethodCall();
 		methodCall.getArguments().addAll(arguments);
 		
 		reference.setNext(methodCall);
 
 		if (type instanceof ConcreteClassifier) {
 			ConcreteClassifier classifier = (ConcreteClassifier) type;
 			EList<Member> members = classifier.getMembersByName(methodName);
 			for (Member member : members) {
 				if (member instanceof ClassMethod) {
 					ClassMethod classMethod = (ClassMethod) member;
 					boolean matches = classMethod.isMethodForCall(methodCall, false);
 					if (matches) {
 						methodCall.setTarget(classMethod);
 						return methodCall;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	// TODO move to separate class?
 	public List<String> getLinesToPrint(String comment, int leadingTabs, boolean endedWithLineBreak) {
 		List<String> result = new ArrayList<String>();
 		// TODO check whether this is a comment that must be translated?
 		
 		// first, remove everything before the comment opener
 		comment = removeLeadingBreaks(comment);
 		comment = removeCommentDelimiters(comment);
 		
 		List<String> commentLines = split(comment);
 		for (int i = 0; i < commentLines.size(); i++) {
 			String commentLine = commentLines.get(i);
 			commentLine = removeLeadingTabs(commentLine, leadingTabs, endedWithLineBreak || i > 0);
 			result.add(commentLine);
 		}
 		
 		return result;
 	}
 
 	private String removeCommentDelimiters(String comment) {
 		int startIdx = comment.indexOf("/*");
 		comment = comment.substring(0, startIdx) + comment.substring(startIdx + 2);
 		int endIdx = comment.lastIndexOf("*/");
 		if (endIdx >= 0) {
 			comment = comment.substring(0, endIdx);
 		}
 		return comment;
 	}
 
 	public List<String> split(String comment) {
 		List<String> lines = new ArrayList<String>();
 		// TODO create constant
 		Pattern pattern = Pattern.compile(LINE_BREAK_REGEX);
 		Matcher matcher = pattern.matcher(comment);
 		int lastEnd = 0;
 		while (matcher.find()) {
 			int start = matcher.start();
 			int end = matcher.end();
 			lines.add(comment.substring(lastEnd, start));
 			lastEnd = end;
 		}
 		if (lastEnd < comment.length()) {
 			lines.add(comment.substring(lastEnd, comment.length()));
 		} else if (lastEnd == comment.length()) {
 			lines.add("");
 		}
 		return lines;
 	}
 
 	private String removeLeadingBreaks(String comment) {
 		int leadingBreak = comment.lastIndexOf("\n", comment.indexOf("/*"));
 		comment = comment.substring(leadingBreak + 1);
 		return comment;
 	}
 
 	private int countLeadingTabsInComment(String comment) {
 		return countLeadingTabs(comment, comment.indexOf("/*"));
 	}
 
 	private int countLeadingTabs(String comment, int endIndex) {
 		int count = 0;
 		for (int i = 0; i < endIndex; i++) {
 			if (new Character('\t').equals(comment.charAt(i))) {
 				count++;
 			} else {
 				break;
 			}
 		}
 		return count;
 	}
 
 	private String removeLeadingTabs(String commentLine, int leadingTabs, boolean endedWithLineBreak) {
 		int lastTabIndex = 0;
 		int foundTabs = 0;
 		for (int i = 0; i < commentLine.length(); i++) {
 			if (foundTabs >= leadingTabs && endedWithLineBreak) {
 				break;
 			}
 			char charAtI = commentLine.charAt(i);
 			if ('\t' == charAtI) {
 				lastTabIndex = i + 1;
 				foundTabs++;
 			} else if (' ' == charAtI) {
 				// ignore white spaces that are mixed with the tabs
 				continue;
 			} else {
 				break;
 			}
 		}
 		return commentLine.substring(lastTabIndex);
 	}
 
 	private String applyVariableAntiQuotationPattern(String variableName,
 			AnnotationInstance variableAntiQuotation) {
 		if (variableAntiQuotation == null) {
 			return variableName;
 		}
 		String textValue = getStringValue(variableAntiQuotation);
 		textValue = String.format(textValue, variableName);
 		return textValue;
 	}
 
 	private String getStringValue(AnnotationInstance annotationInstance) {
 		AnnotationParameter parameter = annotationInstance.getParameter();
 		if (parameter instanceof SingleAnnotationParameter) {
 			SingleAnnotationParameter singleAnnotationParameter = (SingleAnnotationParameter) parameter;
 			AnnotationValue value = singleAnnotationParameter.getValue();
 			if (value instanceof StringReference) {
 				StringReference stringValue = (StringReference) value;
 				return stringValue.getValue();
 			}
 		}
 		return null;
 	}
 
 	private String applyReplacementRules(String commentLine, List<AnnotationInstance> replacementRules) {
 		for (AnnotationInstance replacementRule : replacementRules) {
 			AnnotationParameter parameter = replacementRule.getParameter();
 			if (parameter instanceof AnnotationParameterList) {
 				AnnotationParameterList annotationParameterList = (AnnotationParameterList) parameter;
 				String pattern = getValue(annotationParameterList, "pattern");
 				String replacement = getValue(annotationParameterList, "replacement");
 				if (pattern == null || replacement == null) {
 					continue;
 				}
 				commentLine = commentLine.replace(pattern, replacement);
 			}
 		}
 		return commentLine;
 	}
 
 	private String getValue(AnnotationParameterList annotationParameterList, String name) {
 		List<AnnotationAttributeSetting> settings = annotationParameterList.getSettings();
 		for (AnnotationAttributeSetting annotationAttributeSetting : settings) {
 			String attributeName = annotationAttributeSetting.getAttribute().getName();
 			if (name.equals(attributeName)) {
 				AnnotationValue value = annotationAttributeSetting.getValue();
 				if (value instanceof StringReference) {
 					StringReference stringValue = (StringReference) value;
 					return stringValue.getValue();
 				}
 			}
 		}
 		return null;
 	}
 
 	// TODO move to JaMoPP metamodel?
 	private IdentifierReference createReference(ReferenceableElement element) {
 		IdentifierReference ref = ReferencesFactory.eINSTANCE.createIdentifierReference();
 		ref.setTarget(element);
 		return ref;
 	}
 
 	// TODO move to JaMoPP metamodel
 	private MethodCall createMethodCall(Method method) {
 		MethodCall ref = ReferencesFactory.eINSTANCE.createMethodCall();
 		ref.setTarget(method);
 		return ref;
 	}
 
 	// TODO add a setType() method to JaMoPP metamodel
 	private TypeReference createTypeReference(ConcreteClassifier concreteClassifier) {
 		ClassifierReference ref = TypesFactory.eINSTANCE.createClassifierReference();
 		ref.setTarget(concreteClassifier);
 		return ref;
 	}
 
 	private Statement createAppendCall(LocalVariable stringBuilder, Expression stringExpression) {
 		ConcreteClassifier sbClass = (ConcreteClassifier) stringBuilder.getTypeReference().getTarget();
 		
 		ExpressionStatement es = StatementsFactory.eINSTANCE.createExpressionStatement();
 		
 		IdentifierReference ir = createReference(stringBuilder);
 		MethodCall mc = createMethodCall((Method) sbClass.getMembersByName("append").get(0));
 		mc.getArguments().add(stringExpression);
 		ir.setNext(mc);
 
 		es.setExpression(ir);
 		
 		return es;
 	}
 
 	// TODO move to JaMoPP metamodel
 	private AnnotationInstance getAnnotationInstance(AnnotableAndModifiable element,
 			ConcreteClassifier annotationType) {
 		for (AnnotationInstanceOrModifier aiom : element.getAnnotationsAndModifiers()) {
 			if (aiom instanceof AnnotationInstance) {
 				AnnotationInstance ai = (AnnotationInstance) aiom;
 				InternalEObject annotation = (InternalEObject) ai.getAnnotation();
 				if (annotation.equals(annotationType) ||
 						(annotation.eIsProxy() && annotation.eProxyURI().fragment().endsWith("_" + annotationType.getName()))) {
 					return ai;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns all multi-line comments for the given element in their raw form.
 	 * If requested (clearComment = true), the comments are removed afterwards.
 	 */
 	private List<String> getRawMLComments(Commentable element, boolean clearComment) {
 		List<String> comments = new ArrayList<String>();
 		for (LayoutInformation layoutInformation : element.getLayoutInformations()) {
 			String text = layoutInformation.getHiddenTokenText();
 			List<String> multiLineComments = splitTextToComments(text);
 			comments.addAll(multiLineComments);
 			if (!multiLineComments.isEmpty() && clearComment) {
 				layoutInformation.setHiddenTokenText("");
 			}
 		}
 		return comments;
 	}
 
 	/**
 	 * Splits the given (hidden token) text into into individual comments. This
 	 * is required, because the multiple hidden tokens that appear after each
 	 * other are currently merged by the layout plug-in.
 	 * 
 	 * Note that whitespace characters (e.g., line breaks and tabs) before each
 	 * comment are preserved.
 	 */
 	public List<String> splitTextToComments(String text) {
 		List<String> comments = new ArrayList<String>();
 		
 		IJavaTextScanner scanner = new JavaMetaInformation().createLexer();
 		scanner.setText(text);
 		// retrieve all tokens from scanner
 		IJavaTextToken nextToken = scanner.getNextToken();
 		StringBuilder commentAndWhitespaceBefore = new StringBuilder();
 		while (nextToken != null) {
 			String tokenText = nextToken.getText();
 			if (tokenText.startsWith("/*")) {
 				comments.add(commentAndWhitespaceBefore.toString() + tokenText);
 			} else {
 				commentAndWhitespaceBefore.append(tokenText);
 			}
 			nextToken = scanner.getNextToken();
 		}
 		return comments;
 	}
 	
 	/**
 	 * Finds the names of variable references (using the defined VariableAntiQuotation)
 	 * that persist Strings in the compiled template.
 	 */
 	private void findBrokenReferences(List<Expression> stringExpressions, AnnotationInstance variableAntiQuotation, Set<String> brokenVariableReferences) {
 		if (variableAntiQuotation == null) {
 			return;
 		}
 		String regex = getStringValue(variableAntiQuotation);
 		regex = "(" + String.format("\\Q" + regex + "\\E", "\\E\\w*\\Q") + ")";
 		Pattern pattern = Pattern.compile(regex);
 		
 		for (Expression expression : stringExpressions) {
 			if (expression instanceof StringReference) {
 				StringReference stringReference = (StringReference) expression;
 				Matcher matcher = pattern.matcher(stringReference.getValue());
 				while (matcher.find()) {
 					if (matcher.groupCount() == 1) {
 					    String s = matcher.group(0);
 					    brokenVariableReferences.add(s);
 					}
 				}
 			}
 		}
 	}
 }
