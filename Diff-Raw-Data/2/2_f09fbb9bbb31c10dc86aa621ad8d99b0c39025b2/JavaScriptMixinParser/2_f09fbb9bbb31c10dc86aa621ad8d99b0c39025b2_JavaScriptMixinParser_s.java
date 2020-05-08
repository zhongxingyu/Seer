 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.core.mixin;
 
 import java.io.CharArrayReader;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.mixin.IMixinParser;
 import org.eclipse.dltk.core.mixin.IMixinRequestor;
 import org.eclipse.dltk.core.mixin.MixinModel;
 import org.eclipse.dltk.core.search.indexing.IIndexConstants;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.core.SourceField;
 import org.eclipse.dltk.internal.core.SourceMethod;
 import org.eclipse.dltk.internal.javascript.parser.JavaScriptModuleDeclaration;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.ReferenceResolverContext;
 import org.eclipse.dltk.internal.javascript.typeinference.HostCollection;
 import org.eclipse.dltk.internal.javascript.typeinference.IReference;
 import org.eclipse.dltk.internal.javascript.typeinference.TypeInferencer;
 
 import com.xored.org.mozilla.javascript.CompilerEnvirons;
 import com.xored.org.mozilla.javascript.ErrorReporter;
 import com.xored.org.mozilla.javascript.FunctionNode;
 import com.xored.org.mozilla.javascript.Parser;
 import com.xored.org.mozilla.javascript.ScriptOrFnNode;
 
 public class JavaScriptMixinParser implements IMixinParser,
 		IExecutableExtension {
 
 	private IMixinRequestor requestor;
 
 	private void reportRefs(boolean signature, ISourceModule module) {
 		char[] content = getContent(module);
 		CompilerEnvirons cenv = new CompilerEnvirons();
 		ErrorReporter reporter = new NullReporter();
 		JavaScriptModuleDeclaration moduleDeclaration = new JavaScriptModuleDeclaration(
 				content.length);
 
 		Parser parser = new Parser(cenv, reporter);
 		try {
 			ScriptOrFnNode parse = parser.parse(new CharArrayReader(content),
 					"", 0);
 			TypeInferencer interferencer = new TypeInferencer(null,
 					new ReferenceResolverContext(null, new HashMap()));
 			// interferencer.setRequestor(fRequestor);
 			interferencer.doInterferencing(parse, Integer.MAX_VALUE);
 			// fRequestor.enterModule();
 			processNode("", parse, signature, (IModelElement) module);
 			HostCollection collection = interferencer.getCollection();
 			// elements/
 			moduleDeclaration.setCollection(collection);
 			Collection sm = (Collection) collection.getReferences().values();
 			Iterator i = sm.iterator();
 			while (i.hasNext()) {
 				Object next = i.next();
 				if (next instanceof IReference) {
 					IReference ref = (IReference) next;
 					reportRef(ref, null, 0);
 				}
 			}
 			Map ms = interferencer.getFunctionMap();
 			moduleDeclaration.setFunctionMap(ms);
 		} catch (Exception e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private char[] getContent(ISourceModule module) {
 		try {
 			return module.getSourceAsCharArray();
 		} catch (ModelException e1) {
 			if (DLTKCore.DEBUG) {
 				e1.printStackTrace();
 			}
 			return new char[0];
 		}
 	}
 
 	private void reportRef(IReference ref, String sma, int level) {
 		Set sm = ref.getChilds(false);
 		String key = ref.getName();
 		if (sma != null)
 			key = sma + '.' + key;
 		Iterator i = sm.iterator();
 		while (i.hasNext()) {
 			Object next = i.next();
 			if (next instanceof IReference) {
 				IReference refa = (IReference) next;
 				reportRef(refa, key, level + 1);
 			}
 		}
 		IMixinRequestor.ElementInfo elementInfo = new IMixinRequestor.ElementInfo();
 		elementInfo.key = IIndexConstants.SEPARATOR
 				+ key.replace('.', IIndexConstants.SEPARATOR);
 		elementInfo.object = ref;
 		requestor.reportElement(elementInfo);
 	}
 
 	private void processNode(String parent, ScriptOrFnNode parse,
 			boolean signature, IModelElement parentElement) {
 		for (int a = 0; a < parse.getFunctionCount(); a++) {
 			FunctionNode functionNode = parse.getFunctionNode(a);
 			String functionName = functionNode.getFunctionName();
 			if (functionName.length() > 0) {
 				IMixinRequestor.ElementInfo elementInfo = new IMixinRequestor.ElementInfo();
 				String key = parent + MixinModel.SEPARATOR + functionName;
 				elementInfo.key = key;
 				SourceMethod ms = new SourceMethod(
 						(ModelElement) parentElement, functionName);
 				elementInfo.object = ms;
 				requestor.reportElement(elementInfo);
 				processNode(key, functionNode, signature, ms);
 			}
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
 		// lets report global variables
 		if (parent.length() == 0)
 			for (int i = params.length; i < paramsAndVars.length - of; i++) {
 				ISourceElementRequestor.FieldInfo fieldInfo = new ISourceElementRequestor.FieldInfo();
 				String var = paramsAndVars[i];
 				fieldInfo.name = var;
 				IMixinRequestor.ElementInfo elementInfo = new IMixinRequestor.ElementInfo();
 				String key = parent + MixinModel.SEPARATOR + var;
 				elementInfo.key = key;
 				elementInfo.object = new SourceField(
 						(ModelElement) parentElement, var);
 				requestor.reportElement(elementInfo);
 			}
 	}
 
 	public void setRequirestor(IMixinRequestor requestor) {
 		this.requestor = requestor;
 	}
 
 	public void setInitializationData(IConfigurationElement config,
 			String propertyName, Object data) throws CoreException {
 	}
 
 	public void parserSourceModule(boolean signature, ISourceModule module) {
 		reportRefs(signature, module);
 	}
 }
