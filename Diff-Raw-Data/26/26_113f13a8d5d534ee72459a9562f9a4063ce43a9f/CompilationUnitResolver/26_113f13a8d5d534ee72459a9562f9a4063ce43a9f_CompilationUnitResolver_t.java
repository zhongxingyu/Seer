 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package descent.core.dom;
 
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 
 import descent.core.IJavaProject;
 import descent.core.IProblemRequestor;
 import descent.core.JavaCore;
 import descent.core.JavaModelException;
 import descent.core.WorkingCopyOwner;
 import descent.core.compiler.IProblem;
 import descent.internal.compiler.ICompilerRequestor;
 import descent.internal.compiler.IErrorHandlingPolicy;
 import descent.internal.compiler.IProblemFactory;
 import descent.internal.compiler.env.INameEnvironment;
 import descent.internal.compiler.impl.CompilerOptions;
 import descent.internal.compiler.parser.Module;
 import descent.internal.compiler.parser.Parser;
 import descent.internal.compiler.parser.SemanticContext;
 import descent.internal.core.util.Util;
 
 public class CompilationUnitResolver extends descent.internal.compiler.Compiler {
 	
	private final static boolean RESOLVE = false;
 	private final static boolean SYSOUT = false;
 	
 	public static class ParseResult {
 		public Module module;
 		public PublicScanner scanner;
 		public ParseResult(Module module, PublicScanner scanner) {
 			this.module = module;
 			this.scanner = scanner;
 		}
 	}
 	
 	boolean hasCompilationAborted;
 	
 	//private IProgressMonitor monitor;
 	
 	/**
 	 * Answer a new CompilationUnitVisitor using the given name environment and compiler options.
 	 * The environment and options will be in effect for the lifetime of the compiler.
 	 * When the compiler is run, compilation results are sent to the given requestor.
 	 *
 	 *  @param environment descent.internal.compiler.api.env.INameEnvironment
 	 *      Environment used by the compiler in order to resolve type and package
 	 *      names. The name environment implements the actual connection of the compiler
 	 *      to the outside world (for example, in batch mode the name environment is performing
 	 *      pure file accesses, reuse previous build state or connection to repositories).
 	 *      Note: the name environment is responsible for implementing the actual classpath
 	 *            rules.
 	 *
 	 *  @param policy descent.internal.compiler.api.problem.IErrorHandlingPolicy
 	 *      Configurable part for problem handling, allowing the compiler client to
 	 *      specify the rules for handling problems (stop on first error or accumulate
 	 *      them all) and at the same time perform some actions such as opening a dialog
 	 *      in UI when compiling interactively.
 	 *      @see descent.internal.compiler.DefaultErrorHandlingPolicies
 	 * 
 	 *	@param compilerOptions The compiler options to use for the resolution.
 	 *      
 	 *  @param requestor descent.internal.compiler.api.ICompilerRequestor
 	 *      Component which will receive and persist all compilation results and is intended
 	 *      to consume them as they are produced. Typically, in a batch compiler, it is 
 	 *      responsible for writing out the actual .class files to the file system.
 	 *      @see descent.internal.compiler.CompilationResult
 	 *
 	 *  @param problemFactory descent.internal.compiler.api.problem.IProblemFactory
 	 *      Factory used inside the compiler to create problem descriptors. It allows the
 	 *      compiler client to supply its own representation of compilation problems in
 	 *      order to avoid object conversions. Note that the factory is not supposed
 	 *      to accumulate the created problems, the compiler will gather them all and hand
 	 *      them back as part of the compilation unit result.
 	 */
 	public CompilationUnitResolver(
 		INameEnvironment environment,
 		IErrorHandlingPolicy policy,
 		CompilerOptions compilerOptions,
 		ICompilerRequestor requestor,
 		IProblemFactory problemFactory,
 		IProgressMonitor monitor) {
 
 		super(environment, policy, compilerOptions, requestor, problemFactory);
 		this.hasCompilationAborted = false;
 		//this.monitor =monitor;
 	}
 	
 	public static ParseResult parse(int apiLevel,
 			descent.internal.compiler.env.ICompilationUnit sourceUnit, 
 			Map options, 
 			boolean statementsRecovery) {
 		
 		return parse(apiLevel, sourceUnit.getContents(), options, statementsRecovery);
 	}
 	
 	public static ParseResult parse(int apiLevel,
 			char[] source, 
 			Map options, 
 			boolean statementsRecovery) {
 		
 		descent.internal.compiler.parser.Parser parser;
 		if (options != null) {
 			String taskTags = (String) options.get(JavaCore.COMPILER_TASK_TAGS);
 			if (taskTags != null) {
 				parser = new Parser(apiLevel, source, 0, source.length, 
 						Util.toCharArrays(taskTags.split(",")),
 						Util.toCharArrays(((String) options.get(JavaCore.COMPILER_TASK_PRIORITIES)).split(",")),
 						JavaCore.ENABLED.equals(options.get(JavaCore.COMPILER_TASK_CASE_SENSITIVE))
 						);
 			} else {
 				parser = new Parser(apiLevel, source, 0, source.length);
 			}
 		} else {
 			parser = new Parser(apiLevel, source, 0, source.length);
 		}
 		
 		PublicScanner scanner = new PublicScanner(true, true, true, true, apiLevel);
 		scanner.setLexerAndSource(parser, source);
 		
 		Module module = parser.parseModuleObj();
 		module.setSourceRange(0, source.length);
 		
 		return new ParseResult(module, scanner);
 	}
 	
 	public static ParseResult resolve(int apiLevel,
 			descent.internal.compiler.env.ICompilationUnit sourceUnit,
 			IJavaProject javaProject,
 			Map options,
 			WorkingCopyOwner owner,
 			boolean statementsRecovery,
 			IProgressMonitor monitor) throws JavaModelException {
 		
 		ParseResult result = parse(apiLevel, sourceUnit, options, statementsRecovery);
 		resolve(result.module);
 		return result;
 	}
 	
 	public static void resolve(final Module module) {
 		if (!RESOLVE) return;
 		
 		module.semantic(new SemanticContext(new IProblemRequestor() {
 			public void acceptProblem(IProblem problem) {
 				module.problems.add(problem);
 			}
 			public void beginReporting() {
 			}
 			public void endReporting() {
 			}
 			public boolean isActive() {
 				return true;
 			}
 		}, module.apiLevel));
 		
 		if (SYSOUT) {
 			System.out.println(module);
 		}
 	}
 	
 	public static CompilationUnit convert(AST ast, ParseResult parseResult, IProgressMonitor monitor) {
 		int savedDefaultNodeFlag = ast.getDefaultNodeFlag();
 		ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
 		
 		ASTConverter converter = new ASTConverter(monitor);
 		converter.setAST(ast);
 		CompilationUnit result = converter.convert(parseResult.module);
 		result.setLineEndTable(parseResult.module.lineEnds);
 		result.problems = parseResult.module.problems;
 		result.initCommentMapper(parseResult.scanner);
 		
 		ast.setOriginalModificationCount(ast.modificationCount());
 		ast.setDefaultNodeFlag(savedDefaultNodeFlag);
 		
 		return result;
 	}
 
 	public static ASTNode convert(AST ast, descent.internal.compiler.parser.Initializer initializer) {
 		int savedDefaultNodeFlag = ast.getDefaultNodeFlag();
 		ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
 		
 		ASTConverter converter = new ASTConverter(null);
 		converter.setAST(ast);
 		Initializer init = converter.convert(initializer);
 		
 		ast.setOriginalModificationCount(ast.modificationCount());
 		ast.setDefaultNodeFlag(savedDefaultNodeFlag);
 		
 		return init;
 	}
 	
 	public static ASTNode convert(AST ast, descent.internal.compiler.parser.Expression expression) {
 		int savedDefaultNodeFlag = ast.getDefaultNodeFlag();
 		ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
 		
 		ASTConverter converter = new ASTConverter(null);
 		converter.setAST(ast);
 		Expression exp = converter.convert(expression);
 		
 		ast.setOriginalModificationCount(ast.modificationCount());
 		ast.setDefaultNodeFlag(savedDefaultNodeFlag);
 		
 		return exp;
 	}
 	
 	public static ASTNode convert(AST ast, descent.internal.compiler.parser.Statement statement) {
 		int savedDefaultNodeFlag = ast.getDefaultNodeFlag();
 		ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
 		
 		ASTConverter converter = new ASTConverter(null);
 		converter.setAST(ast);
 		Statement stm = converter.convert(statement);
 		
 		ast.setOriginalModificationCount(ast.modificationCount());
 		ast.setDefaultNodeFlag(savedDefaultNodeFlag);
 		
 		return stm;
 	}
 
 }
