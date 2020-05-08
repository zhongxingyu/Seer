 /*
  * Copyright (c) 2006, 2009 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.graphdef.codegen;
 
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.gmf.common.UnexpectedBehaviourException;
 import org.eclipse.gmf.gmfgraph.Figure;
 import org.eclipse.gmf.gmfgraph.FigureDescriptor;
 import org.eclipse.gmf.internal.common.codegen.TextEmitter;
 import org.eclipse.gmf.internal.graphdef.codegen.Activator;
 import org.eclipse.gmf.internal.xpand.BufferOutput;
 import org.eclipse.gmf.internal.xpand.ResourceManager;
 import org.eclipse.gmf.internal.xpand.XpandFacade;
import org.eclipse.gmf.internal.xpand.model.AmbiguousDefinitionException;
import org.eclipse.gmf.internal.xpand.model.EvaluationException;
 import org.eclipse.gmf.internal.xpand.model.Variable;
 import org.eclipse.gmf.internal.xpand.util.ContextFactory;
 
 public class FigureGenerator implements TextEmitter {
 
 	private static final String VAR_MM_ACCESS = "mapModeAccessor";
 	private static final String VAR_RT_TOKEN = "runtimeToken";
 
 	private final ResourceManager resourceManager;
 
 	private final StringBuilder result = new StringBuilder();
 
 	private final ArrayList<Variable> globals = new ArrayList<Variable>();
 
 	private final boolean myIsInnerClassCode;
 
 	private String packageStatement;
 
 
 	/**
 	 * XXX consider using enum for runtimeToken
 	 * @param runtimeToken either "full" or null to indicate full GMF runtime use, any other value is to be processed by custom templates 
 	 * @param asInnerClass
 	 */
 	public FigureGenerator(String runtimeToken, String packageStmt, boolean asInnerClass) {
 		this(runtimeToken, packageStmt, MapModeCodeGenStrategy.DYNAMIC, "getMapMode().", asInnerClass);
 	}
 
 	public FigureGenerator(String runtimeToken, String packageStmt, MapModeCodeGenStrategy mapModeStrategy, String mapModeAccessor, boolean asInnerClass) {
 		this(runtimeToken, packageStmt, mapModeStrategy, mapModeAccessor, asInnerClass, null);
 	}
 
 	public FigureGenerator(String runtimeToken, String packageStmt, MapModeCodeGenStrategy mapModeStrategy, String mapModeAccessor, boolean asInnerClass, URL[] dynamicTemplates) {
 		myIsInnerClassCode = asInnerClass;
 		this.packageStatement = packageStmt;
 		if (mapModeStrategy == MapModeCodeGenStrategy.STATIC) {
 			if (mapModeAccessor != null && mapModeAccessor.trim().length() > 0) {
 				throw new IllegalArgumentException("Can't use map mode accessor with identity map mode");
 			}
 		}
 		if (mapModeStrategy == MapModeCodeGenStrategy.DYNAMIC) {
 			globals.add(new Variable(VAR_MM_ACCESS, EcorePackage.eINSTANCE.getEString(), mapModeAccessor == null ? "" : mapModeAccessor));
 		}
 		if (runtimeToken != null) {
 			globals.add(new Variable(VAR_RT_TOKEN, EcorePackage.eINSTANCE.getEString(), runtimeToken));
 		}
 		resourceManager = Activator.createResourceEngine(mapModeStrategy, dynamicTemplates);
 	}
 
 	public String getPackageName() {
 		return packageStatement;
 	}
 
 	public String fqnSwitch(Figure figure) {
		try {
			xpandFacade().evaluate("Runtime::fqn", figure, null);
		} catch (AmbiguousDefinitionException e) {
			throw new EvaluationException(e);
		}
 		return result.toString();
 	}
 	
 	/**
 	 * @param packageStmt can be null if asInnerClass was true
 	 * @param figure
 	 */
 	public String go(FigureDescriptor figure) {
		try {
			if (myIsInnerClassCode) {
				xpandFacade().evaluate("top::Descriptor::Inner", figure, null);
			} else {
				xpandFacade().evaluate("top::Descriptor::Top", figure, new Object[] { packageStatement });
			}
		} catch (AmbiguousDefinitionException e) {
			throw new EvaluationException(e);
 		}
 		return result.toString();
 	}
 
 	private XpandFacade xpandFacade() {
 		result.setLength(0);
 		BufferOutput bufferOutput = new BufferOutput(result);
 
 		return new XpandFacade(ContextFactory.createXpandContext(resourceManager, bufferOutput, globals));
 	}
 
 	public String generate(IProgressMonitor monitor, Object[] arguments) throws InterruptedException, InvocationTargetException, UnexpectedBehaviourException {
 		if (arguments == null || arguments.length != 1 || false == arguments[0] instanceof FigureDescriptor) {
 			throw new UnexpectedBehaviourException("(FigureDescriptor) expected as arguments, not " + arguments);
 		}
 		return go((FigureDescriptor) arguments[0]);
 	}
 }
