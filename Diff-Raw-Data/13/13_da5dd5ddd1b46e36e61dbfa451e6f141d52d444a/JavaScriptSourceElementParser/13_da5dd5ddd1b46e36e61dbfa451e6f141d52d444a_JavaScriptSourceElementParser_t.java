 package org.eclipse.dltk.internal.javascript.parser;
 
 import java.io.CharArrayReader;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
import org.eclipse.dltk.compiler.problem.ProblemSeverities;
 import org.eclipse.dltk.compiler.problem.DLTKProblemReporter;
 import org.eclipse.dltk.compiler.problem.DefaultProblem;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.compiler.problem.ProblemSeverities;
 import org.eclipse.dltk.core.ISourceElementParser;
 import org.eclipse.dltk.core.ISourceModuleInfoCache.ISourceModuleInfo;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.ReferenceResolverContext;
 import org.eclipse.dltk.internal.javascript.typeinference.HostCollection;
 import org.eclipse.dltk.internal.javascript.typeinference.IReference;
 import org.eclipse.dltk.internal.javascript.typeinference.TypeInferencer;
 
 import com.xored.org.mozilla.javascript.CompilerEnvirons;
 import com.xored.org.mozilla.javascript.ErrorReporter;
 import com.xored.org.mozilla.javascript.EvaluatorException;
 import com.xored.org.mozilla.javascript.FunctionNode;
 import com.xored.org.mozilla.javascript.Parser;
 import com.xored.org.mozilla.javascript.ScriptOrFnNode;
 import com.xored.org.mozilla.javascript.ScriptOrFnNode.Position;
 
 public class JavaScriptSourceElementParser implements ISourceElementParser {
 
 	private ISourceElementRequestor fRequestor = null;
	private IProblemReporter fReporter = null;
 
 	/**
 	 * Python lexer handler helper.
 	 * 
 	 * @param problemReporter
 	 * 
 	 * @param enveronment
 	 */
 
 	public JavaScriptSourceElementParser(ISourceElementRequestor requestor,
 			IProblemReporter problemReporter) {
 		this.fRequestor = requestor;
		this.fReporter = problemReporter;
 	}
 
 	public ModuleDeclaration parseSourceModule(char[] contents, ISourceModuleInfo info ) {
 		String content = new String(contents);
 		CompilerEnvirons cenv = new CompilerEnvirons();
 		ErrorReporter reporter = new ErrorReporter() {
 
 			public void error(String arg0, String arg1, int arg2, String arg3,
 					int arg4) {
 				try {
 					if (fReporter != null)
 						fReporter.reportProblem(new DefaultProblem(arg1, arg0,
 								0, new String[] {}, ProblemSeverities.Error,
								arg4 - arg3.length(), arg4, arg2));
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 			}
 
 			public EvaluatorException runtimeError(String arg0, String arg1,
 					int arg2, String arg3, int arg4) {
 				// should never happen;
 				return null;
 			}
 
 			public void warning(String arg0, String arg1, int arg2,
 					String arg3, int arg4) {
 				try {
 					if (fReporter != null)
 						fReporter.reportProblem(new DefaultProblem(arg1, arg0,
 								0, new String[] {}, ProblemSeverities.Warning,
								arg4, arg4 + 1, arg2));
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 			}
 
 		};
 		JavaScriptModuleDeclaration moduleDeclaration = new JavaScriptModuleDeclaration(
 				content.length());
 
 		Parser parser = new Parser(cenv, reporter);
 		try {
 
 			ScriptOrFnNode parse = parser.parse(new CharArrayReader(contents),
 					"", 0);
 			TypeInferencer interferencer = new TypeInferencer(null,new ReferenceResolverContext(null,new HashMap()));
 			interferencer.setRequestor(fRequestor);
 			interferencer.doInterferencing(parse, Integer.MAX_VALUE);
 
 			fRequestor.enterModule();
 			processNode(parse);
 			HostCollection collection = interferencer.getCollection();
 			// elements/
 			moduleDeclaration.setCollection(collection);
 			Collection sm = (Collection) collection.getReferences().values();
 			Iterator i = sm.iterator();
 			while (i.hasNext()) {
 				Object next = i.next();
 				if (next instanceof IReference)
 				{
 				IReference ref = (IReference) next;
 				reportRef(ref, null, 0);
 				}
 			}
 			Map ms = interferencer.getFunctionMap();
 			moduleDeclaration.setFunctionMap(ms);
 			Iterator ia = ms.values().iterator();
 			while (ia.hasNext()) {
 				HostCollection next = (HostCollection) ia.next();
 				fRequestor.acceptFieldReference(("!!!" + next.getName())
 						.toCharArray(), 0);
 			}
 			fRequestor.exitModule(contents.length);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return moduleDeclaration;
 	}
 
 	private void reportRef(IReference ref, String sma, int level) {		
 		Set sm = ref.getChilds(false);
 		String key = ref.getName();
 		if (sma != null)
 			key = sma + '.' + key;
 		Iterator i = sm.iterator();
 		while (i.hasNext()) {
 			Object next = i.next();
 			if (next instanceof IReference)
 			{
 			IReference refa = (IReference) next;
 			reportRef(refa, key, level + 1);
 			}
 		}
 		// contibuting field to index
 		fRequestor.acceptFieldReference((key).toCharArray(), 0);
 	}
 
 	private void processNode(ScriptOrFnNode parse) {
 		for (int a = 0; a < parse.getFunctionCount(); a++) {
 			FunctionNode functionNode = parse.getFunctionNode(a);
 			functionNode.getFunctionName();
 			ISourceElementRequestor.MethodInfo methodInfo = new ISourceElementRequestor.MethodInfo();
 			String functionName = functionNode.getFunctionName();
 
 			if (functionName.length() == 0)
 				continue;
 			methodInfo.name = functionName;
 			methodInfo.declarationStart = functionNode.getEncodedSourceStart();
 			String[] paramsAndVars = functionNode.getParamAndVarNames();
 			String[] params = new String[functionNode.getParamCount()];
 			for (int i = 0; i < params.length; i++) {
 				params[i] = paramsAndVars[i];
 			}
 			methodInfo.parameterNames = params;
 			methodInfo.nameSourceStart = functionNode.nameStart;
 			methodInfo.nameSourceEnd = functionNode.nameEnd;
 			fRequestor.enterMethod(methodInfo);
 			processNode(functionNode);
 			fRequestor.exitMethod(functionNode.getEncodedSourceEnd());
 		}
 		String[] paramsAndVars = parse.getParamAndVarNames();
 		String[] params = new String[parse.getParamCount()];
 		for (int i = 0; i < params.length; i++) {
 			params[i] = paramsAndVars[i];
 		}
 		int of = 0;
 		if (parse instanceof FunctionNode) {
 			FunctionNode n = (FunctionNode) parse;
 			if (n.getType() != FunctionNode.FUNCTION_STATEMENT)
 				of = 1;
 		}
 		for (int i = params.length; i < paramsAndVars.length - of; i++) {
 			ISourceElementRequestor.FieldInfo fieldInfo = new ISourceElementRequestor.FieldInfo();
 			fieldInfo.name = paramsAndVars[i];
 			Position p = parse.getPosition(i);
 			fieldInfo.nameSourceStart = p.start;
 			fieldInfo.nameSourceEnd = p.start + fieldInfo.name.length();
 			fieldInfo.declarationStart = p.start;
 			fRequestor.enterField(fieldInfo);
 			fRequestor.exitField(fieldInfo.nameSourceEnd);
 		}
 	}
 
 	public void setRequirestor(ISourceElementRequestor requestor) {
 		this.fRequestor = requestor;
 	}
 }
