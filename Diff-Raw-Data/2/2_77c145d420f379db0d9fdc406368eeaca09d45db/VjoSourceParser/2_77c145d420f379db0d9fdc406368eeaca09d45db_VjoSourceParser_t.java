 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.eclipse.internal.parser;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.mod.ast.declarations.Argument;
 import org.eclipse.dltk.mod.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.mod.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.mod.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.mod.ast.parser.AbstractSourceParser;
 import org.eclipse.dltk.mod.ast.references.SimpleReference;
 import org.eclipse.dltk.mod.ast.references.TypeReference;
 import org.eclipse.dltk.mod.ast.references.VariableKind;
 import org.eclipse.dltk.mod.ast.references.VariableReference;
 import org.eclipse.dltk.mod.ast.references.VjoTypeReference;
 import org.eclipse.dltk.mod.compiler.problem.DefaultProblem;
 import org.eclipse.dltk.mod.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.mod.compiler.problem.ProblemSeverities;
 import org.eclipse.dltk.mod.core.DLTKCore;
 import org.eclipse.vjet.dsf.jsgen.shared.ids.VjoSyntaxProbIds;
 import org.eclipse.vjet.dsf.jsgen.shared.jstvalidator.DefaultJstProblem;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.IScriptProblem;
 import org.eclipse.vjet.dsf.jst.JstSource;
 import org.eclipse.vjet.dsf.jst.ProblemSeverity;
 import org.eclipse.vjet.dsf.jst.declaration.JstArg;
 import org.eclipse.vjet.dsf.jst.declaration.JstBlock;
 import org.eclipse.vjet.dsf.jst.declaration.JstVar;
 import org.eclipse.vjet.dsf.jst.declaration.JstVars;
 import org.eclipse.vjet.dsf.jst.expr.ArrayAccessExpr;
 import org.eclipse.vjet.dsf.jst.expr.AssignExpr;
 import org.eclipse.vjet.dsf.jst.expr.BoolExpr;
 import org.eclipse.vjet.dsf.jst.expr.FieldAccessExpr;
 import org.eclipse.vjet.dsf.jst.expr.InfixExpr;
 import org.eclipse.vjet.dsf.jst.expr.MtdInvocationExpr;
 import org.eclipse.vjet.dsf.jst.expr.ParenthesizedExpr;
 import org.eclipse.vjet.dsf.jst.expr.PostfixExpr;
 import org.eclipse.vjet.dsf.jst.expr.PrefixExpr;
 import org.eclipse.vjet.dsf.jst.stmt.CatchStmt;
 import org.eclipse.vjet.dsf.jst.stmt.ForStmt;
 import org.eclipse.vjet.dsf.jst.stmt.RtnStmt;
 import org.eclipse.vjet.dsf.jst.stmt.SwitchStmt;
 import org.eclipse.vjet.dsf.jst.stmt.SwitchStmt.CaseStmt;
 import org.eclipse.vjet.dsf.jst.stmt.ThrowStmt;
 import org.eclipse.vjet.dsf.jst.stmt.WhileStmt;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jst.token.IExpr;
 import org.eclipse.vjet.dsf.jst.token.IIfStmt;
 import org.eclipse.vjet.dsf.jst.token.IInitializer;
 import org.eclipse.vjet.dsf.jst.token.ILHS;
 import org.eclipse.vjet.dsf.jst.token.IStmt;
 import org.eclipse.vjet.dsf.jst.token.ITryStmt;
 import org.eclipse.vjet.dsf.ts.event.ISourceEventCallback;
 import org.eclipse.vjet.dsf.ts.event.type.AddTypeEvent;
 import org.eclipse.vjet.dsf.ts.event.type.ModifyTypeEvent;
 import org.eclipse.vjet.dsf.ts.event.type.RemoveTypeEvent;
 import org.eclipse.vjet.dsf.ts.type.TypeName;
 import org.eclipse.vjet.eclipse.ast.declarations.VjoArgument;
 import org.eclipse.vjet.eclipse.ast.declarations.VjoFieldDeclaration;
 import org.eclipse.vjet.eclipse.ast.declarations.VjoMethodDeclaration;
 import org.eclipse.vjet.eclipse.ast.declarations.VjoTypeDeclaration;
 import org.eclipse.vjet.eclipse.ast.references.VjoQualifiedNameReference;
 import org.eclipse.vjet.eclipse.codeassist.CodeassistUtils;
 import org.eclipse.vjet.eclipse.core.VjetPlugin;
 import org.eclipse.vjet.eclipse.core.builder.VjetSourceModuleBuildCtx;
 import org.eclipse.vjet.eclipse.core.parser.VjoParserToJstAndIType;
 import org.eclipse.vjet.eclipse.core.validation.ValidationEntry;
 import org.eclipse.vjet.eclipse.core.validation.utils.ProblemUtility;
 import org.eclipse.vjet.eclipse.internal.core.util.Util;
 import org.eclipse.vjet.vjo.tool.codecompletion.CodeCompletionUtils;
 import org.eclipse.vjet.vjo.tool.typespace.SourceTypeName;
 import org.eclipse.vjet.vjo.tool.typespace.TypeSpaceMgr;
 
 /**
  * @author MPeleshchyshyn
  * 
  */
 public class VjoSourceParser extends AbstractSourceParser{
 
 //	IScriptUnit scriptUnit;
 
 	TypeSpaceMgr tsm = TypeSpaceMgr.getInstance();
 	String typeName;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.dltk.mod.ast.parser.ISourceParser#parse(char[], char[],
 	 *      org.eclipse.dltk.mod.compiler.problem.IProblemReporter)
 	 */
 	public ModuleDeclaration parse(char[] fileName, char[] source,
 			IProblemReporter reporter, VjetSourceModuleBuildCtx context) {
 		ModuleDeclaration moduleDeclaration = new ModuleDeclaration(
 				source.length);
 		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		String strPath = new String(fileName).replace(File.separatorChar, '/');
 		IPath path = workspaceRoot.getFullPath().append(strPath);
 		IFile file = workspaceRoot.getFile(path);
 		String groupName = file.getProject().getName();
 		// TODO doesn't check src directories
 		String substring = strPath.substring(strPath.indexOf(groupName)+groupName.length(), strPath.lastIndexOf('/'));
 		if(substring.contains(".")){
 //			System.out.println("file path contains unsupported character \".\" dot : "+ strPath);
 			String[] str ={};
 			reporter.reportProblem(ProblemUtility.reportProblem(new DefaultJstProblem(str,VjoSyntaxProbIds.TypeHasIllegalToken, "file path contains unsupported character \".\" dot : "+ substring ,fileName, 0,0, 0, 0, ProblemSeverity.error), ProblemSeverities.Error));  // );
 			return moduleDeclaration;
 			
 		}
 		String typeName = CodeassistUtils.getClassName(file);
 
 		VjoParserToJstAndIType parser = new VjoParserToJstAndIType(reporter);
 		try {
 			if(VjetPlugin.TRACE_PARSER){
 				System.out.println("parsing for " + getClass().getName());
 			}
 			// TODO disable full build (parse,resolve,validate)
 			IJstType scriptUnit = parser.parse(groupName, typeName,
 					new String(source));
 			typeName = scriptUnit.getName();
 			
 			if(context==null){
 				//if disable all the validations (syntax and semantic)
 				if ( ValidationEntry.isEnableVjetValidation()) {
 					
 				// deal with problems
 				List<DefaultProblem> dproblems = null;
 				List<IScriptProblem> problems = scriptUnit.getProblems();
 				// if there are no syntax errors in script unit
				if (problems!=null && !problems.isEmpty()  ) {
 					dproblems = ProblemUtility.reportProblems(problems);
 				}else{
 					dproblems = ValidationEntry.validator(scriptUnit);
 				}
 				
 				if (dproblems != null) {
 					parser.reportProblems(dproblems, reporter);
 				}
 				}
 			}
 			
 			// Register type to typeSpace when type is not exist in type space
 			// and package path is same with OS path
 			
 			if(scriptUnit!=null  && 
 					!scriptUnit.getName().equals(typeName)){
 				typeName = scriptUnit.getName();
 			}
 			
 			if (scriptUnit != null) {
 				// TODO this appears to be done too many times
 				// it should only be done during incremental build
 				// or onsave events
 				reRegestierType(source, file, groupName, typeName, scriptUnit);
 				// create DLTK ui model from jst
 				processType(scriptUnit, moduleDeclaration);
 			}
 			if(context!=null)
 				context.setUnit(scriptUnit);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			DLTKCore.error(e.getMessage(), e);
 		}
 
 		return moduleDeclaration;
 	}
 	
 	@Override
 	public ModuleDeclaration parse(char[] fileName, char[] source,
 			IProblemReporter reporter) {
 		// TODO Auto-generated method stub
 		return parse(fileName, source, reporter, null);
 	}
 
 	/**
 	 * Register type to typeSpace when type is not exist in type space and
 	 * package path is same with OS path
 	 * 
 	 * @param source
 	 *            char[]
 	 * @param file
 	 *            {@link IFile}
 	 * @param groupName
 	 *            String
 	 * @param typeName
 	 *            String
 	 * @param unit
 	 *            {@link IScriptUnit}
 	 */
 	private void reRegestierType(char[] source, IFile file, String groupName,
 			String typeName, IJstType unit) {
 		SourceTypeName name = new SourceTypeName(groupName, typeName,
 				new String(source));
 		if (!tsm.existType(name)) {
 			IJstType actualType = unit;
 			String actualName = null;
 			if (actualType != null) {
 				actualName = actualType.getName();
 				if (actualName != null) {
 					if(file==null || file.getLocation()==null){
 						return;
 					}
 					
 					String filePath = file.getLocation().toOSString();
 					if (!filePath.endsWith(".js"))
 						return;
 					if (typeName.equals(actualName)) {
 						doProcessType(name, null, actualType);
 					}
 				}
 			}
 		}
 	}
 	
 	private void reRegestierType2(char[] source, IFile file, String groupName,
 			String typeName, IJstType unit) {
 		SourceTypeName name = new SourceTypeName(groupName, typeName,
 				new String(source));
 		if(tsm.existType(name)){
 			name.setAction(SourceTypeName.CHANGED);
 		}else{
 			name.setAction(SourceTypeName.ADDED);
 		}
 		
 			IJstType actualType = unit;
 			
 			doProcessType(name, null, actualType);
 					
 				
 			
 		
 	}
 
 	/**
 	 * Returns true if changed type not exist in type space.
 	 * 
 	 * @param name
 	 *            {@link SourceTypeName} object.
 	 * @return true if changed type not exist in type space.
 	 */
 	private boolean isChangedTypeNotExist(SourceTypeName name) {
 		return name.getAction() == SourceTypeName.CHANGED
 				&& !tsm.existType(name);
 	}
 
 	/**
 	 * Process type changes with specified source type name. if type is added
 	 * the send {@link AddTypeEvent}, changed {@link ModifyTypeEvent}, removed
 	 * {@link RemoveTypeEvent}.
 	 * 
 	 * 
 	 * @param name
 	 *            {@link SourceTypeName} object.
 	 * @param callback
 	 *            {@link ISourceEventCallback} object.
 	 */
 	private void doProcessType(SourceTypeName name,
 			ISourceEventCallback<IJstType> callback, IJstType jstType) {
 
 		TypeName typeName = new TypeName(name.groupName(), name.typeName());
 
 		int action = name.getAction();
 
 		if (isChangedTypeNotExist(name)) {
 			action = SourceTypeName.ADDED;
 		}
 
 		switch (action) {
 		case SourceTypeName.ADDED:
 			AddTypeEvent addEvent = new AddTypeEvent(name, jstType);
 			tsm.processEvent(addEvent, callback);
 			break;
 		case SourceTypeName.CHANGED:
 			ModifyTypeEvent event = new ModifyTypeEvent(name.groupName(), name
 					.typeName(), name.source());
 			tsm.processEvent(event, callback);
 			break;
 		case SourceTypeName.REMOVED:
 			RemoveTypeEvent removeEvent = new RemoveTypeEvent(typeName);
 			tsm.processEvent(removeEvent, callback);
 			break;
 		default:
 			break;
 		}
 	}
 
 	public void processType(IJstType type, ModuleDeclaration moduleDeclaration) {
 		String typeName = type.getSimpleName();
 		JstSource typeSource = type.getSource();
 
 		TypeDeclaration typeDeclaration;
 		if (typeSource != null) {
 			typeDeclaration = new VjoTypeDeclaration(typeName, typeSource
 					.getStartOffSet(), typeSource.getEndOffSet() + 1,
 					typeSource.getStartOffSet(), moduleDeclaration.sourceEnd());
 		} else {
 			typeDeclaration = new VjoTypeDeclaration(typeName, 0, 0, 0, 0);
 		}
 
 		moduleDeclaration.addStatement(typeDeclaration);
 
 		processInheritsAndImplements(type, typeDeclaration);
 		processFields(type, typeDeclaration);
 		processConstructor(type, typeDeclaration);
 		processMethods(type, typeDeclaration);
 	}
 
 	private void processConstructor(IJstType type,
 			TypeDeclaration typeDeclaration) {
 		IJstMethod constructor = type.getConstructor();
 		if (constructor != null && !CodeCompletionUtils.isSynthesizedElement(constructor)) {
 			JstSource nameSource = constructor.getName().getSource();
 			processMethod(constructor, typeDeclaration, nameSource);
 		}
 	}
 
 	private void processInheritsAndImplements(IJstType type,
 			TypeDeclaration typeDeclaration) {
 		// extended types
 		List<? extends IJstType> types = type.getExtends();
 		processTypes(types, typeDeclaration, type);
 		// implemented interfaces
 		types = type.getSatisfies();
 		processTypes(types, typeDeclaration, type);
 	}
 
 	private void processTypes(List<? extends IJstType> types,
 			TypeDeclaration typeDeclaration, IJstType focusType) {
 		for (IJstType type : types) {
 			typeDeclaration.addSuperClass(createTypeReference(type));
 		}
 	}
 
 	private void processFields(IJstType type, TypeDeclaration typeDeclaration) {
 		// static fields
 		Collection<IJstProperty> fields = type.getStaticProperties();
 		processFields(fields, typeDeclaration);
 		// instance fields
 		fields = type.getInstanceProperties();
 		processFields(fields, typeDeclaration);
 	}
 
 	private void processFields(Collection<IJstProperty> fields,
 			TypeDeclaration typeDeclaration) {
 		for (IJstProperty field : fields) {
 			if (CodeCompletionUtils.isSynthesizedElement(field)) {
 				continue;
 			}
 			String name = field.getName().getName();
 			JstSource nameSource = field.getName().getSource();
 			int startOffSet = 0;
 			int endOffset = 0;
 			if (nameSource != null) {
 				startOffSet = nameSource.getStartOffSet();
 				endOffset = nameSource.getEndOffSet() + 1;
 
 			}
 			VjoFieldDeclaration fieldDeclaration = new VjoFieldDeclaration(
 					name, startOffSet, endOffset, startOffSet, endOffset, null);
 
 			int modifiers = Util.getModifiers(field.getModifiers());
 			fieldDeclaration.setModifiers(modifiers);
 			typeDeclaration.getFieldList().add(fieldDeclaration);
 
 			// type reference
 			IJstType fieldType = field.getType();
 			if (fieldType != null) {
 				fieldDeclaration.type = createTypeReference(fieldType);
 			}
 		}
 	}
 
 	private static TypeReference createTypeReference(IJstType type) {
 		// should we use full name instead???
 		VjoTypeReference typeRef = null;
 		if (type != null) {
 			String typeName = type.getSimpleName();
 			JstSource typeSource = ((IJstType) type).getSource();
 			if (typeSource != null) {
 				typeRef = new VjoTypeReference(typeSource.getStartOffSet(),
 						typeSource.getEndOffSet() + 1, typeName);
 				if (type.getPackage() != null) {
 					typeRef.setPackageName(type.getPackage().getName());
 				}
 			}
 		}
 		return typeRef;
 	}
 
 	private void processMethods(IJstType type, TypeDeclaration typeDeclaration) {
 		Collection<? extends IJstMethod> methods = type.getStaticMethods();
 		processMethods(methods, typeDeclaration);
 		methods = type.getInstanceMethods();
 		processMethods(methods, typeDeclaration);
 	}
 
 	private void processMethods(Collection<? extends IJstMethod> methods,
 			TypeDeclaration typeDeclaration) {
 		for (IJstMethod method : methods) {
 			if (method.getName().getSource() == null) {
 				continue;
 			}
 			JstSource nameSource = method.getName().getSource();
 			processMethod(method, typeDeclaration, nameSource);
 			List<IJstMethod> overloadedMethods = method.getOverloaded();
 			if (overloadedMethods.size() > 0) {
 				for (IJstMethod m : overloadedMethods) {
 					processMethod(m, typeDeclaration, nameSource);
 				}
 			}
 		}
 	}
 
 	private void processMethod(IJstMethod method,
 			TypeDeclaration typeDeclaration, JstSource nameSource) {
 
 		if(nameSource==null){
 			nameSource = new JstSource(0, 0, 0, 0, 0, 0);
 		}
 		VjoMethodDeclaration methodDeclaration = new VjoMethodDeclaration(
 				method.getName().getName(), nameSource.getStartOffSet(),
 				nameSource.getEndOffSet() + 1, nameSource.getStartOffSet(),
 				nameSource.getEndOffSet() + 1);
 		methodDeclaration.setDeclaringTypeName(typeDeclaration.getName());
 		methodDeclaration
 				.setModifiers(Util.getModifiers(method.getModifiers()));
 		typeDeclaration.getMethodList().add(methodDeclaration);
 
 		// rtn type
 		IJstType retType = method.getRtnType();
 		if (retType != null) {
 			methodDeclaration.type = createTypeReference(retType);
 		}
 
 		// args
 		List<JstArg> args = method.getArgs();
 		for (JstArg arg : args) {
 			// add source info to arg??
 			JstSource argSource = arg.getSource();
 			if (argSource != null && arg.getName() != null) {
 				VjoArgument argDecl = new VjoArgument(new SimpleReference(
 						argSource.getStartOffSet(),
 						argSource.getEndOffSet() + 1, arg.getName()), argSource
 						.getStartOffSet(), null, 0);
 				IJstType argType = arg.getType();
 				if (argType != null) {
 					argDecl.type = createTypeReference(argType);
 				}
 				methodDeclaration.addArgument(argDecl);
 			}
 
 		}
 		// vars and methods refs
 		processStatements(method.getBlock(), methodDeclaration);
 	}
 
 	private void processStatements(JstBlock block,
 			MethodDeclaration methodDelcaration) {
 		if (block != null) {
 			List<IStmt> statements = block.getStmts();
 			for (IStmt statement : statements) {
 				processStatement(statement, methodDelcaration);
 			}
 		}
 	}
 
 	private void processStatement(IStmt statement,
 			MethodDeclaration methodDelcaration) {
 		// TODO other types of expressions
 		if (statement instanceof AssignExpr) {
 			processAssignExpr((AssignExpr) statement, methodDelcaration);
 		} else if (statement instanceof ForStmt) {
 			ForStmt forStatement = (ForStmt) statement;
 			// initializers first
 			IInitializer initializer = forStatement.getInitializers();
 			if (initializer != null) {
 				List<AssignExpr> initializers = initializer.getAssignments();
 				for (AssignExpr assignExpr : initializers) {
 					processAssignExpr(assignExpr, methodDelcaration);
 				}
 			}
 
 			// condition next
 			processExpression(forStatement.getCondition(), methodDelcaration);
 
 			// updaters
 			List<IExpr> updaters = forStatement.getUpdaters();
 			for (IExpr updater : updaters) {
 				processExpression(updater, methodDelcaration);
 			}
 			// block last
 			processStatements(forStatement.getBody(), methodDelcaration);
 		} else if (statement instanceof WhileStmt) {
 			WhileStmt whileStmt = (WhileStmt) statement;
 			// condition first
 			processExpression(whileStmt.getCondition(), methodDelcaration);
 			// body next
 			processStatements(whileStmt.getBody(), methodDelcaration);
 		} else if (statement instanceof ITryStmt) {
 			ITryStmt tryStmt = (ITryStmt) statement;
 			// try block
 			processStatements(tryStmt.getBody(), methodDelcaration);
 			// catch statements
 			if(tryStmt.getCatchBlock()!=null){
 				List<IStmt> catchStmts = tryStmt.getCatchBlock().getStmts();
 				for (IStmt catchStmt : catchStmts) {
 					processStatement(catchStmt, methodDelcaration);
 				}
 				// finally block
 				processStatements(tryStmt.getFinallyBlock(), methodDelcaration);
 			}
 		} else if (statement instanceof CatchStmt) {
 			CatchStmt catchStmt = (CatchStmt) statement;
 			processJstVar(catchStmt.getException(), methodDelcaration);
 
 			processStatements(catchStmt.getBody(), methodDelcaration);
 		} else if (statement instanceof SwitchStmt) {
 			SwitchStmt switchStmt = (SwitchStmt) statement;
 			processExpression(switchStmt.getExpr(), methodDelcaration);
 
 			List<IStmt> statements = switchStmt.getBody().getStmts();
 			if (statements != null) {
 				for (IStmt stmt : statements) {
 					processStatement(stmt, methodDelcaration);
 				}
 			}
 		} else if (statement instanceof CaseStmt) {
 			processExpression(((CaseStmt) statement).getExpr(),
 					methodDelcaration);
 		} else if (statement instanceof IIfStmt) {
 			IIfStmt ifStmt = (IIfStmt) statement;
 			// condition
 			processExpression(ifStmt.getCondition(), methodDelcaration);
 			// if block
 			processStatements(ifStmt.getBody(), methodDelcaration);
 			// else-if statements
 			if(ifStmt.getElseIfBlock()!=null){
 				List<IStmt> elseIfStmts = ifStmt.getElseIfBlock().getStmts();
 				if (elseIfStmts != null) {
 					for (IStmt elseIfStmt : elseIfStmts) {
 						processStatement(elseIfStmt, methodDelcaration);
 					}
 				}
 			}
 			// else block
 			processStatements(ifStmt.getElseBlock(), methodDelcaration);
 		} else if (statement instanceof ThrowStmt) {
 			ThrowStmt throwStmt = (ThrowStmt) statement;
 			processExpression(throwStmt.getExpression(), methodDelcaration);
 		} else if (statement instanceof RtnStmt) {
 			RtnStmt returnStmt = (RtnStmt) statement;
 			processExpression(returnStmt.getExpression(), methodDelcaration);
 		} else if (statement instanceof MtdInvocationExpr) {
 			processExpression((IExpr) statement, methodDelcaration);
 		} else if (statement instanceof JstVars) {
 			processJstVars((JstVars) statement, methodDelcaration);
 		}
 	}
 
 	private void processJstVars(JstVars vars,
 			MethodDeclaration methodDelcaration) {
 		JstIdentifier localVar = (JstIdentifier) vars.getAssignments().get(0)
 				.getLHS();
 		JstSource source = localVar.getSource();
 		IJstType type = vars.getType();
 		String typeName = "Object";
 		if (type != null) {
 			typeName = type.getName();
 		}
 		// TODO ... new TypeReference(0, 0, typeName)
 		VjoFieldDeclaration var = new VjoFieldDeclaration(localVar.getName(),
 				source.getStartOffSet(), source.getEndOffSet() + 1, source
 						.getStartOffSet(), source.getEndOffSet() + 1,
 				new TypeReference(0, 0, typeName));
 		var.setModifiers(var.getModifiers());
 		methodDelcaration.getBody().addStatement(var);
 
 	}
 
 	private void processJstVar(JstVar jstVar,
 			MethodDeclaration methodDelcaration) {
 		JstSource source = jstVar.getSource();
 		IJstType type = jstVar.getType();
 		String typeName = "Object";
 		if (type != null) {
 			typeName = type.getName();
 		}
 		// TODO ... new TypeReference(0, 0, typeName)
 		VjoFieldDeclaration var = new VjoFieldDeclaration(jstVar.getName(),
 				source.getStartOffSet(), source.getEndOffSet() + 1, source
 						.getStartOffSet(), source.getEndOffSet() + 1,
 				new TypeReference(0, 0, typeName));
 		var.setModifiers(var.getModifiers());
 		methodDelcaration.getBody().addStatement(var);
 	}
 
 //	private void processArithExpr(ArithExpr updater,
 //			MethodDeclaration methodDelcaration) {
 //		// TODO Auto-generated method stub
 //	}
 
 	private void processAssignExpr(AssignExpr assignExpr,
 			MethodDeclaration methodDelcaration) {
 		ILHS lhs = assignExpr.getLHS();
 		if (lhs instanceof IExpr) {
 			processExpression((IExpr) lhs, methodDelcaration);
 		} else if (lhs instanceof JstVar) {
 			// local var declaration
 			processJstVar((JstVar) lhs, methodDelcaration);
 		}
 
 		processExpression(assignExpr.getExpr(), methodDelcaration);
 	}
 
 	private void processExpression(IExpr expression,
 			MethodDeclaration methodDelcaration) {
 		if (expression == null) {
 			return;
 		}
 		if (expression instanceof JstIdentifier) {
 			JstIdentifier identifier = (JstIdentifier) expression;
 			if ("this".equals(identifier.getName())
 					|| "self".equals(identifier.getName())) {
 				return;
 			}
 			VariableKind kind = VariableKind.UNKNOWN;
 			List localVars = methodDelcaration.getBody().getStatements();
 			for (Object statement : localVars) {
 				if (statement instanceof VjoFieldDeclaration
 						&& ((VjoFieldDeclaration) statement).getName().equals(
 								identifier.getName())) {
 					kind = VariableKind.LOCAL;
 					break;
 				}
 			}
 			if (kind.equals(VariableKind.UNKNOWN)) {
 				List<Argument> args = methodDelcaration.getArguments();
 				for (Argument argument : args) {
 					if (argument.getName().equals(identifier.getName())) {
 						kind = VariableKind.ARGUMENT;
 						break;
 					}
 				}
 			}
 			JstSource source = (identifier.getSource());
 			VariableReference varRef = new VariableReference(source
 					.getStartOffSet(), source.getEndOffSet() + 1, identifier
 					.getName(), kind);
 			methodDelcaration.getBody().addStatement(varRef);
 		} else if (expression instanceof MtdInvocationExpr
 				|| expression instanceof FieldAccessExpr) {
 			VjoQualifiedNameReference qref = new VjoQualifiedNameReference(-1,
 					-1, expression.toExprText(), typeName, expression);
 			methodDelcaration.getBody().addStatement(qref);
 		} else if (expression instanceof BoolExpr) {
 			BoolExpr boolExpr = (BoolExpr) expression;
 			processExpression(boolExpr.getLeft(), methodDelcaration);
 			processExpression(boolExpr.getRight(), methodDelcaration);
 		} else if (expression instanceof PostfixExpr) {
 			processExpression(((PostfixExpr) expression).getIdentifier(),
 					methodDelcaration);
 		} else if (expression instanceof InfixExpr) {
 			InfixExpr infixExpr = (InfixExpr) expression;
 			processExpression(infixExpr.getLeft(), methodDelcaration);
 			processExpression(infixExpr.getRight(), methodDelcaration);
 		} else if (expression instanceof ParenthesizedExpr) {
 			processExpression(((ParenthesizedExpr) expression).getExpression(),
 					methodDelcaration);
 		} else if (expression instanceof PrefixExpr) {
 			processExpression(((PrefixExpr) expression).getIdentifier(),
 					methodDelcaration);
 		} else if (expression instanceof ArrayAccessExpr) {
 			processExpression(((ArrayAccessExpr) expression).getExpr(),
 					methodDelcaration);
 			processExpression(((ArrayAccessExpr) expression).getIndex(),
 					methodDelcaration);
 		}
 	}
 
 
 }
