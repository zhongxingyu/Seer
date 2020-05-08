 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.parser;
 
 import java.io.CharArrayReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.eclipse.dltk.ast.declarations.FieldDeclaration;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.parser.AbstractSourceParser;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.ReferenceResolverContext;
 import org.eclipse.dltk.internal.javascript.typeinference.TypeInferencer;
 
 import com.xored.org.mozilla.javascript.CompilerEnvirons;
 import com.xored.org.mozilla.javascript.ErrorReporter;
 import com.xored.org.mozilla.javascript.EvaluatorException;
 import com.xored.org.mozilla.javascript.FunctionNode;
 import com.xored.org.mozilla.javascript.Parser;
 import com.xored.org.mozilla.javascript.ScriptOrFnNode;
 import com.xored.org.mozilla.javascript.ScriptOrFnNode.Position;
 
 public class JavaScriptSourceParser extends AbstractSourceParser {
 	
 	private final class ReferenceRecordingRequestor implements
 			ISourceElementRequestor {
 		
 
 		ArrayList references=new ArrayList();
 		
 		public void acceptFieldReference(char[] fieldName,
 				int sourcePosition) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void acceptMethodReference(char[] methodName,
 				int argCount, int sourcePosition, int sourceEndPosition) {
 			references.add(new SimpleReference(sourcePosition,sourceEndPosition,new String(methodName)));
 		}
 
 		public void acceptPackage(int declarationStart,
 				int declarationEnd, char[] name) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void acceptTypeReference(char[][] typeName,
 				int sourceStart, int sourceEnd) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void acceptTypeReference(char[] typeName,
 				int sourcePosition) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void enterField(FieldInfo info) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public boolean enterFieldCheckDuplicates(FieldInfo info) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public boolean enterFieldWithParentType(FieldInfo info,
 				String parentName, String delimiter) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public void enterMethod(MethodInfo info) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void enterMethodRemoveSame(MethodInfo info) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public boolean enterMethodWithParentType(MethodInfo info,
 				String parentName, String delimiter) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public void enterModule() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void enterModuleRoot() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void enterType(TypeInfo info) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public boolean enterTypeAppend(String fullName, String delimiter) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public void exitField(int declarationEnd) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void exitMethod(int declarationEnd) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void exitModule(int declarationEnd) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void exitModuleRoot() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void exitType(int declarationEnd) {
 			// TODO Auto-generated method stub
 			
 		}
 	}
 
 	ModelElement element;
 	public JavaScriptSourceParser(IModelElement parent) {
 		this.element=(ModelElement) parent;
 	}
 	
 	public JavaScriptSourceParser() {
 
 	}
 
 	public ModuleDeclaration parse(char[] fileName, char[] content, IProblemReporter r) {
 		JavaScriptModuleDeclaration moduleDeclaration = new JavaScriptModuleDeclaration(content
 				.length);
 		
 		CompilerEnvirons cenv=new CompilerEnvirons();
 		ErrorReporter reporter=new ErrorReporter(){
 
 			public void error(String arg0, String arg1, int arg2, String arg3,
 					int arg4) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			public EvaluatorException runtimeError(String arg0, String arg1,
 					int arg2, String arg3, int arg4) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			public void warning(String arg0, String arg1, int arg2,
 					String arg3, int arg4) {
 				// TODO Auto-generated method stub
 				
 			}
 		
 		};
 		Parser parser = new Parser(cenv, reporter);
 		try {
 
 			ScriptOrFnNode parse = parser.parse(new CharArrayReader(content),
 					"", 0);
 			TypeInferencer interferencer = new TypeInferencer(element,new ReferenceResolverContext(null,new HashMap()));
 			ReferenceRecordingRequestor referenceRecordingRequestor = new ReferenceRecordingRequestor();
 			interferencer.setRequestor(referenceRecordingRequestor);
 			interferencer.doInterferencing(parse,Integer.MAX_VALUE);
 			moduleDeclaration.setCollection(interferencer.getCollection());
 			moduleDeclaration.setFunctionMap(interferencer.getFunctionMap());
 			moduleDeclaration.setReferences(referenceRecordingRequestor.references);
 			processNode(parse,moduleDeclaration);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return moduleDeclaration;
 	}
 	
 	private void processNode(ScriptOrFnNode parse, ModuleDeclaration moduleDeclaration) {
 		for (int a = 0; a < parse.getFunctionCount(); a++) {
 			FunctionNode functionNode = parse.getFunctionNode(a);
 			functionNode.getFunctionName();
 			ISourceElementRequestor.MethodInfo methodInfo = new ISourceElementRequestor.MethodInfo();
 			String functionName = functionNode.getFunctionName();
 
 			if (functionName.length() == 0)
 				continue;
 			methodInfo.name = functionName;
 			String functionComments = functionNode.getFunctionComments();
 			int declarationStart = functionNode.getEncodedSourceStart();
 			String[] paramsAndVars = functionNode.getParamAndVarNames();
 			String[] params = new String[functionNode.getParamCount()];
 			for (int i = 0; i < params.length; i++) {
 				params[i] = paramsAndVars[i];
 			}
 			String[] parameterNames = params;
 			int nameSourceStart = functionNode.nameStart;
 			int nameSourceEnd = functionNode.nameEnd;			
 			processNode(functionNode,null);
 			MethodDeclaration decl=new MethodDeclaration(functionName,nameSourceStart,nameSourceEnd,declarationStart,functionNode.getEncodedSourceEnd());
 			// TODO set decl.getArguments()
 			decl.setComments(functionComments);
 			if (moduleDeclaration!=null)
 				moduleDeclaration.addStatement(decl);
 		}
 		String[] paramsAndVars = parse.getParamAndVarNames();
 		String[] params = new String[parse.getParamCount()];
 		for (int i = 0; i < params.length; i++) {
 			params[i] = paramsAndVars[i];
 		}
 		int of = 0;
 		if (parse instanceof FunctionNode) {
 			FunctionNode n = (FunctionNode) parse;
			if (n.getFunctionType() != FunctionNode.FUNCTION_STATEMENT)
 				of = 1;
 		}
 		for (int i = params.length; i < paramsAndVars.length - of; i++) {
 			String name = paramsAndVars[i];
 			Position p=parse.getPosition(i);
 			FieldDeclaration decl=new FieldDeclaration(name,p.start,p.start + name.length(),p.start,p.start + name.length());
 			decl.setComments(parse.getParamComments(name));
 			//fRequestor.enterField(fieldInfo);
 			//fRequestor.exitField(fieldInfo.nameSourceEnd);
 			if (moduleDeclaration!=null)
 				moduleDeclaration.addStatement(decl);
 		}
 		
 	}
 }
