 /**
  * Copyright (c) 2008 Borland Software Corp.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Alexander Shatalin (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.xpand.migration;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.gmf.internal.xpand.ResourceManager;
 import org.eclipse.gmf.internal.xpand.ast.AbstractDefinition;
 import org.eclipse.gmf.internal.xpand.ast.Advice;
 import org.eclipse.gmf.internal.xpand.ast.Definition;
 import org.eclipse.gmf.internal.xpand.ast.ErrorStatement;
 import org.eclipse.gmf.internal.xpand.ast.ExpandStatement;
 import org.eclipse.gmf.internal.xpand.ast.ExpressionStatement;
 import org.eclipse.gmf.internal.xpand.ast.FileStatement;
 import org.eclipse.gmf.internal.xpand.ast.ForEachStatement;
 import org.eclipse.gmf.internal.xpand.ast.IfStatement;
 import org.eclipse.gmf.internal.xpand.ast.ImportDeclaration;
 import org.eclipse.gmf.internal.xpand.ast.LetStatement;
 import org.eclipse.gmf.internal.xpand.ast.NamespaceImport;
 import org.eclipse.gmf.internal.xpand.ast.Statement;
 import org.eclipse.gmf.internal.xpand.ast.Template;
 import org.eclipse.gmf.internal.xpand.expression.AnalysationIssue;
 import org.eclipse.gmf.internal.xpand.expression.ExecutionContext;
 import org.eclipse.gmf.internal.xpand.expression.SyntaxConstants;
 import org.eclipse.gmf.internal.xpand.expression.ast.DeclaredParameter;
 import org.eclipse.gmf.internal.xpand.expression.ast.Expression;
 import org.eclipse.gmf.internal.xpand.expression.ast.Identifier;
 import org.eclipse.gmf.internal.xpand.expression.ast.SyntaxElement;
 import org.eclipse.gmf.internal.xpand.migration.MigrationException.Type;
 import org.eclipse.gmf.internal.xpand.model.XpandAdvice;
 import org.eclipse.gmf.internal.xpand.model.XpandDefinition;
 import org.eclipse.gmf.internal.xpand.model.XpandResource;
 import org.eclipse.gmf.internal.xpand.util.CompositeXpandResource;
 import org.eclipse.gmf.internal.xpand.xtend.ast.XtendResource;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.text.edits.InsertEdit;
 import org.eclipse.text.edits.MalformedTreeException;
 import org.eclipse.text.edits.MultiTextEdit;
 import org.eclipse.text.edits.ReplaceEdit;
 
 public class XpandMigrationFacade {
 
 	private ResourceManager resourceManager;
 
 	private String resourceName;
 
 	private Document document;
 
 	private MigrationExecutionContext ctx;
 
 	private MultiTextEdit edit;
 
 	private ModelManager modelManager;
 
 	private TypeManager typeManager;
 
 	private OclKeywordManager oclKeywordManager;
 
 	private MigrationExecutionContext rootExecutionContext;
 
 	public XpandMigrationFacade(ResourceManager resourceManager, String xtendResourceName, MigrationExecutionContext executionContext) {
 		this(resourceManager, xtendResourceName);
 		rootExecutionContext = executionContext;
 	}
 
 	public XpandMigrationFacade(ResourceManager resourceManager, String xtendResourceName) {
 		this.resourceManager = resourceManager;
 		resourceName = xtendResourceName;
 	}
 
 	public String migrateXpandResource() throws MigrationException {
 		StringBuilder originalContent = new StringBuilder();
 		try {
 			Reader[] readers = resourceManager.resolveMultiple(resourceName, XpandResource.TEMPLATE_EXTENSION);
 			assert readers.length > 0;
 			Reader mainReader = readers[0];
 			for (int ch = mainReader.read(); ch != -1; ch = mainReader.read()) {
 				originalContent.append((char) ch);
 			}
 		} catch (IOException e) {
 			throw new MigrationException(Type.RESOURCE_NOT_FOUND, resourceName, "Unable to load resource: " + resourceName);
 		}
 
 		XpandResource xpandResource = resourceManager.loadXpandResource(resourceName);
 		if (xpandResource == null) {
 			throw new MigrationException(Type.RESOURCE_NOT_FOUND, resourceName, "Unable to load resource: " + resourceName);
 		}
		ctx = rootExecutionContext != null ? rootExecutionContext : (MigrationExecutionContext) new MigrationExecutionContextImpl(resourceManager).cloneWithResource(xpandResource);
 		Set<AnalysationIssue> issues = new HashSet<AnalysationIssue>();
 		xpandResource.analyze(ctx, issues);
 		if (MigrationException.hasErrors(issues)) {
 			throw new MigrationException(issues, resourceName);
 		}
 		Template xpandTemplate = getFirstTemplate(xpandResource);
 		document = new Document(originalContent.toString());
 		edit = new MultiTextEdit();
 
 		migrate(xpandTemplate);
 		try {
 			edit.apply(document);
 		} catch (MalformedTreeException e) {
 			throw new MigrationException(Type.UNABLE_TO_APPLY_EDIT, resourceName, e.getMessage());
 		} catch (BadLocationException e) {
 			throw new MigrationException(Type.UNABLE_TO_APPLY_EDIT, resourceName, e.getMessage());
 		}
 		return document.get();
 	}
 	
 	private Template getFirstTemplate(XpandResource xpandResource) throws MigrationException {
 		// TODO: there should be more generic way to get first definition..
 		while (xpandResource instanceof CompositeXpandResource) {
 			xpandResource = ((CompositeXpandResource) xpandResource).getFirstDefinition();
 		}
 		if (false == xpandResource instanceof Template) {
 			throw new MigrationException(Type.UNSUPPORTED_XPAND_RESOURCE, resourceName, "Only Template instances are supported, but loaded: " + xpandResource);
 		}
 		return (Template) xpandResource;
 	}
 
 	private void migrate(Template xpandTemplate) throws MigrationException {
 		StandardLibraryImports stdLibImportsManager = new StandardLibraryImports(getStdLibImportsPosition(xpandTemplate));
 		oclKeywordManager = new OclKeywordManager();
 		modelManager = new ModelManager(stdLibImportsManager, oclKeywordManager);
 		modelManager.registerSelfAlias(ExecutionContext.IMPLICIT_VARIABLE);
 		typeManager = new TypeManager(oclKeywordManager);
 
 		for (NamespaceImport namespaceImport : xpandTemplate.getImports()) {
 			migrateExpression(namespaceImport.getStringLiteral(), EcorePackage.eINSTANCE.getEString(), Collections.<String, EClassifier> emptyMap(), new VariableNameDispatcher());
 		}
 
 		for (XpandDefinition definition : xpandTemplate.getDefinitions()) {
 			assert definition instanceof AbstractDefinition;
 			migrateDefinition((AbstractDefinition) definition);
 		}
 
 		for (XpandAdvice advice : xpandTemplate.getAdvices()) {
 			assert advice instanceof Advice;
 			migrateDefinition((Advice) advice);
 		}
 
 		injectStdlibImports(stdLibImportsManager, getAdditionalLibraries(xpandTemplate));
 	}
 
 	// TODO: use RangeMarker instead?
 	private int getStdLibImportsPosition(Template xpandTemplate) {
 		int offset = 0;
 		if (xpandTemplate.getExtensions().length > 0) {
 			ImportDeclaration[] extensions = xpandTemplate.getExtensions();
 			offset = extensions[extensions.length - 1].getEndOffset();
 		} else if (xpandTemplate.getImports().length > 0) {
 			NamespaceImport[] imports = xpandTemplate.getImports();
 			offset = imports[imports.length - 1].getEndOffset();
 		}
 		if (offset > 0) {
 			try {
 				for (; !"".equals(document.get(offset, 1)); offset++) {
 				}
 				offset++;
 			} catch (BadLocationException e) {
 				offset = 0;
 			}
 		}
 		return offset;
 	}
 
 	private List<String> getAdditionalLibraries(Template xpandTemplate) {
 		List<String> result = new ArrayList<String>();
 		for (ImportDeclaration extension : xpandTemplate.getExtensions()) {
 			XtendResource xtendResource = resourceManager.loadXtendResource(extension.getImportString().getValue());
 			if (xtendResource != null) {
 				result.addAll(getReexportedExtensions(xtendResource));
 			}
 		}
 		return result;
 	}
 
 	private List<String> getReexportedExtensions(XtendResource xtendResource) {
 		List<String> result = new ArrayList<String>();
 		for (String extension : xtendResource.getImportedExtensions()) {
 			if (xtendResource.isReexported(extension)) {
 				result.add(extension);
 				XtendResource extensionResource = resourceManager.loadXtendResource(extension);
 				result.addAll(getReexportedExtensions(extensionResource));
 			}
 		}
 		return result;
 	}
 
 	private void injectStdlibImports(StandardLibraryImports stdLibImportsManager, List<String> list) {
 		list.addAll(Arrays.asList(stdLibImportsManager.getLibraries()));
 		if (list.isEmpty()) {
 			return;
 		}
 		StringBuilder sb = new StringBuilder();
 		if (stdLibImportsManager.getPlaceholderIndex() > 0) {
 			sb.append(ExpressionMigrationFacade.LF);
 		}
 		for (int i = 0; i < list.size(); i++) {
 			if (i > 0) {
 				sb.append(ExpressionMigrationFacade.LF);
 			}
 			sb.append("EXTENSION ");
 			sb.append(list.get(i));
 			sb.append("");
 		}
 
 		if (stdLibImportsManager.getPlaceholderIndex() == 0) {
 			sb.append(ExpressionMigrationFacade.LF);
 		}
 		insert(stdLibImportsManager.getPlaceholderIndex(), sb);
 	}
 
 	private void migrateDefinition(AbstractDefinition definition) throws MigrationException {
 		assert definition instanceof Definition || definition instanceof Advice;
 		migrateIdentifier(definition instanceof Definition ? ((Definition) definition).getDefName() : ((Advice) definition).getPointCut());
 
 		Map<String, EClassifier> envVariables = new HashMap<String, EClassifier>();
 		for (DeclaredParameter parameter : definition.getParams()) {
 			envVariables.put(parameter.getName().getValue(), migrateParameter(parameter));
 		}
 
 		Identifier targetType = definition.getType();
 		EClassifier qvtType = ctx.getTypeForName(targetType.getValue());
 		replace(targetType, typeManager.getQvtFQName(qvtType));
 		envVariables.put(ExecutionContext.IMPLICIT_VARIABLE, qvtType);
 
 		VariableNameDispatcher variableNameDispatcher = new VariableNameDispatcher(definition);
 		for (Statement statement : definition.getBody()) {
 			migrateStatement(statement, variableNameDispatcher, envVariables);
 		}
 	}
 
 	private void migrateIdentifier(Identifier definitionName) {
 		if (oclKeywordManager.isOclKeyword(definitionName)) {
 			replace(definitionName, oclKeywordManager.getValidIdentifierValue(definitionName));
 		}
 	}
 
 	private void migrateStatement(Statement statement, VariableNameDispatcher variableNameDispatcher, Map<String, EClassifier> envVariables) throws MigrationException {
 		if (statement instanceof ExpressionStatement) {
 			ExpressionStatement expressionStatement = (ExpressionStatement) statement;
 			migrateExpression(expressionStatement.getExpression(), EcorePackage.eINSTANCE.getEString(), envVariables, variableNameDispatcher);
 		} else if (statement instanceof ErrorStatement) {
 			ErrorStatement errorStatement = (ErrorStatement) statement;
 			migrateExpression(errorStatement.getMessage(), EcorePackage.eINSTANCE.getEString(), envVariables, variableNameDispatcher);
 		} else if (statement instanceof ExpandStatement) {
 			ExpandStatement expandStatement = (ExpandStatement) statement;
 			migrateExpandStatementDefinition(expandStatement);
 			ExpressionAnalyzeTrace trace = ctx.getTraces().get(expandStatement);
 			assert trace instanceof ExpandAnalyzeTrace;
 			ExpandAnalyzeTrace expTrace = (ExpandAnalyzeTrace) trace;
 
 			for (Expression parameter : expandStatement.getParameters()) {
 				migrateExpression(parameter, expTrace.getParameterType(parameter), envVariables, variableNameDispatcher);
 			}
 			if (expandStatement.getTarget() != null) {
 				migrateExpression(expandStatement.getTarget(), expTrace.getResultType(), envVariables, variableNameDispatcher);
 			}
 			if (expandStatement.getSeparator() != null) {
 				migrateExpression(expandStatement.getSeparator(), expTrace.getSeparatorType(), envVariables, variableNameDispatcher);
 			}
 		} else if (statement instanceof FileStatement) {
 			FileStatement fileStatement = (FileStatement) statement;
 			migrateExpression(fileStatement.getTargetFileName(), EcorePackage.eINSTANCE.getEString(), envVariables, variableNameDispatcher);
 			for (Statement bodyStatement : fileStatement.getBody()) {
 				migrateStatement(bodyStatement, variableNameDispatcher, envVariables);
 			}
 		} else if (statement instanceof ForEachStatement) {
 			ForEachStatement forEach = (ForEachStatement) statement;
 			ExpressionAnalyzeTrace trace = ctx.getTraces().get(forEach);
 			assert trace instanceof ForEachAnalyzeTrace;
 			ForEachAnalyzeTrace forEachTrace = (ForEachAnalyzeTrace) trace;
 			migrateExpression(forEach.getTarget(), forEachTrace.getResultType(), envVariables, variableNameDispatcher);
 			if (forEach.getSeparator() != null) {
 				migrateExpression(forEach.getSeparator(), forEachTrace.getSeparatorType(), envVariables, variableNameDispatcher);
 			}
 			for (Statement bodyStatement : forEach.getBody()) {
 				migrateStatement(bodyStatement, variableNameDispatcher, envVariables);
 			}
 		} else if (statement instanceof IfStatement) {
 			IfStatement ifStatement = (IfStatement) statement;
 			if (ifStatement.getCondition() != null) {
 				ExpressionAnalyzeTrace trace = ctx.getTraces().get(ifStatement);
 				migrateExpression(ifStatement.getCondition(), trace.getResultType(), envVariables, variableNameDispatcher);
 			}
 			for (Statement thenStatement : ifStatement.getThenPart()) {
 				migrateStatement(thenStatement, variableNameDispatcher, envVariables);
 			}
 			if (ifStatement.getElseIf() != null) {
 				migrateStatement(ifStatement.getElseIf(), variableNameDispatcher, envVariables);
 			}
 		} else if (statement instanceof LetStatement) {
 			LetStatement letStatement = (LetStatement) statement;
 			migrateIdentifier(letStatement.getVarName());
 			ExpressionAnalyzeTrace trace = ctx.getTraces().get(letStatement);
 			migrateExpression(letStatement.getVarValue(), trace.getResultType(), envVariables, variableNameDispatcher);
 			envVariables.put(letStatement.getVarName().getValue(), trace.getResultType());
 			try {
 				for (Statement bodyStatement : letStatement.getBody()) {
 					migrateStatement(bodyStatement, variableNameDispatcher, envVariables);
 				}
 			} finally {
 				envVariables.remove(letStatement.getVarName().getValue());
 			}
 		}
 	}
 
 	private void migrateExpandStatementDefinition(ExpandStatement expandStatement) {
 		Identifier definition = expandStatement.getDefinition();
 		String fullQualifiedDefinitionName = definition.getValue();
 		int lastSeparatorIndex = fullQualifiedDefinitionName.lastIndexOf(SyntaxConstants.NS_DELIM);
 		if (lastSeparatorIndex == -1) {
 			migrateIdentifier(definition);
 			return;
 		}
 		// fullName
 		String namePrefix = fullQualifiedDefinitionName.substring(0, lastSeparatorIndex);
 		String shortName = fullQualifiedDefinitionName.substring(lastSeparatorIndex + SyntaxConstants.NS_DELIM.length());
 		if (oclKeywordManager.isOclKeyword(shortName)) {
 			replace(definition, namePrefix + SyntaxConstants.NS_DELIM + oclKeywordManager.getValidIdentifierValue(shortName));
 		}
 	}
 
 	private EClassifier migrateParameter(DeclaredParameter parameter) throws MigrationException {
 		EClassifier parameterType = ctx.getTypeForName(parameter.getType().getValue());
 		replace(parameter, oclKeywordManager.getValidIdentifierValue(parameter.getName()) + " : " + typeManager.getQvtFQName(parameterType));
 		return parameterType;
 	}
 
 	private void migrateExpression(Expression expression, EClassifier expectedExpressionType, Map<String, EClassifier> envVariables, VariableNameDispatcher variableNameDispatcher) throws MigrationException {
 		ExpressionMigrationFacade expressionMF = new ExpressionMigrationFacade(expression, expectedExpressionType, envVariables, typeManager, modelManager, variableNameDispatcher, ctx, resourceName);
 		StringBuilder result = expressionMF.migrate();
 		replace(expression, result.toString());
 	}
 
 	private void replace(SyntaxElement syntaxElement, CharSequence replacement) {
 		ReplaceEdit replaceEdit = new ReplaceEdit(syntaxElement.getStartOffset(), syntaxElement.getEndOffset() + 1 - syntaxElement.getStartOffset(), replacement.toString());
 		edit.addChild(replaceEdit);
 	}
 
 	private void insert(int position, CharSequence text) {
 		InsertEdit insertEdit = new InsertEdit(position, text.toString());
 		edit.addChild(insertEdit);
 	}
 
 }
