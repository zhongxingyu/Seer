 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.model;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.riena.core.util.VariableManagerUtil;
 import org.eclipse.riena.navigation.IAssemblerProvider;
 import org.eclipse.riena.navigation.IForEachExtension;
 import org.eclipse.riena.navigation.IGenericNavigationAssembler;
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleGroupNodeExtension;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.IModuleNodeExtension;
 import org.eclipse.riena.navigation.INavigationAssembler;
 import org.eclipse.riena.navigation.INavigationAssemblyExtension;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.INodeExtension;
 import org.eclipse.riena.navigation.ISubApplicationNode;
 import org.eclipse.riena.navigation.ISubApplicationNodeExtension;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.ISubModuleNodeExtension;
 import org.eclipse.riena.navigation.NavigationArgument;
 import org.eclipse.riena.navigation.NavigationNodeId;
 
 /**
  * The GenericNavigationAssembler can handle the hierarchical definition of
  * navigation assemblies as defined by the extension point
  * 'org.eclipse.riena.navigation.assemblies'. Variables used for labels of
  * modules and submodules would be substituted. There are some predefined
  * variables:
  * <ul>
  * <li>riena.navigation.nodeid
 * <li>riena.navigation.parameter
  * </ul>
  * These variables reference the NavigationNodeId and
  * NavigationArgument.getParameter() objects, resp. The user is required to
  * provide a parameter pointing to the desired property. For example to access
  * the instanceId property of the current NavigationNodeId one would write:
  * 
  * <pre>
  * ${riena.navigation.nodeid:instanceId}
  * </pre>
  */
 public class GenericNavigationAssembler implements IGenericNavigationAssembler {
 
 	/** dynamic variable referencing navigation node id */
 	public static final String VAR_NAVIGATION_NODEID = "riena.navigation.nodeid"; //$NON-NLS-1$
 
 	/** dynamic variable referencing navigation node id */
 	public static final String VAR_NAVIGATION_NODECONTEXT = "riena.navigation.nodecontext"; //$NON-NLS-1$
 
 	/** dynamic variable referencing navigation parameter */
 	public static final String VAR_NAVIGATION_PARAMETER = "riena.navigation.parameter"; //$NON-NLS-1$
 
 	private static final String VARIABLE_START = "${"; //$NON-NLS-1$
 	private static final char VARIABLE_ARG = ':';
 	private static final char VARIABLE_END = '}';
 
 	// the node definition as read from extension point
 	private INavigationAssemblyExtension assembly;
 
 	// the IAssemblyProvider that can be used to resolve assembly references
 	private IAssemblerProvider assemblerProvider;
 
 	private Set<String> acceptedTargetIds = null;
 
 	/**
 	 * @see org.eclipse.riena.navigation.IGenericNavigationAssembler#getAssembly()
 	 */
 	public INavigationAssemblyExtension getAssembly() {
 		return assembly;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.IGenericNavigationAssembler#setAssembly(org.eclipse.riena.navigation.INavigationAssemblyExtension)
 	 */
 	public void setAssembly(INavigationAssemblyExtension nodeDefinition) {
 		this.assembly = nodeDefinition;
 	}
 
 	public void setAssemblerProvider(IAssemblerProvider assemblyProvider) {
 		this.assemblerProvider = assemblyProvider;
 	}
 
 	public IAssemblerProvider getAssemblerProvider() {
 		return assemblerProvider;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationAssembler#buildNode(org.eclipse.riena.navigation.NavigationNodeId,
 	 *      org.eclipse.riena.navigation.NavigationArgument)
 	 */
 	public INavigationNode<?> buildNode(NavigationNodeId targetId, NavigationArgument navigationArgument) {
 
 		if (assembly != null) {
 			Map<String, Object> context = new HashMap<String, Object>();
 			// build module group if it exists
 			ISubApplicationNodeExtension subapplicationDefinition = assembly.getSubApplicationNode();
 			if (subapplicationDefinition != null) {
 				return build(subapplicationDefinition, targetId, navigationArgument, context);
 			}
 			// build module group if it exists
 			IModuleGroupNodeExtension groupDefinition = assembly.getModuleGroupNode();
 			if (groupDefinition != null) {
 				return build(groupDefinition, targetId, navigationArgument, context);
 			}
 			// otherwise try module
 			IModuleNodeExtension moduleDefinition = assembly.getModuleNode();
 			if (moduleDefinition != null) {
 				return build(moduleDefinition, targetId, navigationArgument, context);
 			}
 			// last resort is submodule
 			ISubModuleNodeExtension submoduleDefinition = assembly.getSubModuleNode();
 			if (submoduleDefinition != null) {
 				return build(submoduleDefinition, targetId, navigationArgument, context);
 			}
 		}
 
 		throw new ExtensionPointFailure(
 				"'subapplication', 'modulegroup', 'module' or 'submodule' element expected. ID=" + targetId.getTypeId()); //$NON-NLS-1$
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationAssembler#acceptsTargetId(java
 	 *      .lang.String)
 	 */
 	public boolean acceptsToBuildNode(NavigationNodeId nodeId, NavigationArgument argument) {
 
 		return getAcceptedTargetIds().contains(nodeId.getTypeId());
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationAssembler#getAcceptedNodeIds()
 	 */
 	public Collection<String> getAcceptedTargetIds() {
 
 		if (acceptedTargetIds == null) {
 			acceptedTargetIds = new HashSet<String>();
 			initializeAcceptedTargetIds();
 			acceptedTargetIds = Collections.unmodifiableSet(acceptedTargetIds);
 		}
 
 		return new HashSet<String>(acceptedTargetIds);
 	}
 
 	protected ISubApplicationNode build(ISubApplicationNodeExtension subapplicationDefinition,
 			NavigationNodeId targetId, NavigationArgument navigationArgument, Map<String, Object> context) {
 
 		Assert.isNotNull(subapplicationDefinition, "Error building subapplication. Subapplication cannot be null"); //$NON-NLS-1$
 		Assert.isNotNull(subapplicationDefinition.getViewId(),
 				"Error building subapplication. Attribute 'view' cannot be null for subapplication = " //$NON-NLS-1$
 						+ subapplicationDefinition.getTypeId());
 
 		// a module group can only contain modules
 		ISubApplicationNode subapplication = new SubApplicationNode(createNavigationNodeIdFromTemplate(targetId,
 				subapplicationDefinition, navigationArgument), subapplicationDefinition.getLabel());
 		subapplication.setIcon(subapplicationDefinition.getIcon());
 		updateContext(subapplication, navigationArgument);
 
 		Map<IConfigurationElement, Object> elementMap = createElementMap(subapplicationDefinition);
 
 		for (IConfigurationElement element : subapplicationDefinition.getConfigurationElement().getChildren()) {
 			if (elementMap.get(element) != null) {
 				if (elementMap.get(element) instanceof IModuleGroupNodeExtension) {
 					subapplication.addChild(build((IModuleGroupNodeExtension) elementMap.get(element), targetId,
 							navigationArgument, copy(context)));
 				} else if (elementMap.get(element) instanceof INavigationAssemblyExtension) {
 					INavigationAssemblyExtension assemblyref = (INavigationAssemblyExtension) elementMap.get(element);
 					if (assemblyref != null) {
 						INavigationAssembler assembler = getAssemblerProvider().getNavigationAssembler(
 								assemblyref.getRef());
 						if (assembler != null && assembler.getAssembly().getModuleGroupNode() != null) {
 							subapplication.addChild(build(assembler.getAssembly().getModuleGroupNode(), targetId,
 									navigationArgument, copy(context)));
 						}
 					}
 				}
 			}
 		}
 
 		return subapplication;
 	}
 
 	protected IModuleGroupNode build(IModuleGroupNodeExtension groupDefinition, NavigationNodeId targetId,
 			NavigationArgument navigationArgument, Map<String, Object> context) {
 
 		// a module group can only contain modules
 		IModuleGroupNode moduleGroup = new ModuleGroupNode(createNavigationNodeIdFromTemplate(targetId,
 				groupDefinition, navigationArgument));
 		updateContext(moduleGroup, navigationArgument);
 
 		Map<IConfigurationElement, Object> elementMap = createElementMap(groupDefinition);
 
 		for (IConfigurationElement element : groupDefinition.getConfigurationElement().getChildren()) {
 			if (elementMap.get(element) != null) {
 				if (elementMap.get(element) instanceof IModuleNodeExtension) {
 					moduleGroup.addChild(build((IModuleNodeExtension) elementMap.get(element), targetId,
 							navigationArgument, copy(context)));
 				} else if (elementMap.get(element) instanceof INavigationAssemblyExtension) {
 					INavigationAssemblyExtension assemblyref = (INavigationAssemblyExtension) elementMap.get(element);
 					if (assemblyref != null) {
 						INavigationAssembler assembler = getAssemblerProvider().getNavigationAssembler(
 								assemblyref.getRef());
 						if (assembler != null && assembler.getAssembly().getModuleNode() != null) {
 							moduleGroup.addChild(build(assembler.getAssembly().getModuleNode(), targetId,
 									navigationArgument, copy(context)));
 						}
 					}
 				}
 			}
 		}
 
 		return moduleGroup;
 	}
 
 	protected IModuleNode build(IModuleNodeExtension moduleDefinition, NavigationNodeId targetId,
 			NavigationArgument navigationArgument, Map<String, Object> context) {
 
 		IModuleNode module = null;
 		Map<String, Object> mapping = createMapping(targetId, navigationArgument);
 		try {
 			startVariableResolver(mapping);
 			// create module node with label (and icon)
 			module = new ModuleNode(createNavigationNodeIdFromTemplate(targetId, moduleDefinition, navigationArgument),
 					moduleDefinition.getLabel());
 			module.setIcon(moduleDefinition.getIcon());
 			module.setClosable(!moduleDefinition.isUnclosable());
 			updateContext(module, navigationArgument);
 
 			Map<IConfigurationElement, Object> elementMap = createElementMap(moduleDefinition);
 
 			for (IConfigurationElement element : moduleDefinition.getConfigurationElement().getChildren()) {
 				if (elementMap.get(element) != null) {
 					if (elementMap.get(element) instanceof ISubModuleNodeExtension) {
 						module.addChild(build((ISubModuleNodeExtension) elementMap.get(element), targetId,
 								navigationArgument, copy(context)));
 					} else if (elementMap.get(element) instanceof INavigationAssemblyExtension) {
 						INavigationAssemblyExtension assemblyref = (INavigationAssemblyExtension) elementMap
 								.get(element);
 						if (assemblyref != null) {
 							INavigationAssembler assembler = getAssemblerProvider().getNavigationAssembler(
 									assemblyref.getRef());
 							if (assembler != null && assembler.getAssembly().getSubModuleNode() != null) {
 								module.addChild(build(assembler.getAssembly().getSubModuleNode(), targetId,
 										navigationArgument, copy(context)));
 							}
 						}
 					}
 				}
 			}
 
 		} finally {
 			cleanupVariableResolver();
 		}
 
 		return module;
 	}
 
 	protected ISubModuleNode build(ISubModuleNodeExtension submoduleDefinition, NavigationNodeId targetId,
 			NavigationArgument navigationArgument, Map<String, Object> context) {
 
 		ISubModuleNode submodule = null;
 		Map<String, Object> mapping = createMapping(targetId, navigationArgument);
 		mapping.put(VAR_NAVIGATION_NODECONTEXT, context);
 
 		try {
 			startVariableResolver(mapping);
 			// create submodule node with label (and icon)
 			submodule = new SubModuleNode(createNavigationNodeIdFromTemplate(targetId, submoduleDefinition,
 					navigationArgument), submoduleDefinition.getLabel());
 			submodule.setIcon(submoduleDefinition.getIcon());
 			updateContext(submodule, navigationArgument);
 
 			Map<IConfigurationElement, Object> elementMap = createElementMap(submoduleDefinition);
 
 			for (IConfigurationElement element : submoduleDefinition.getConfigurationElement().getChildren()) {
 				if (elementMap.get(element) != null) {
 					if (elementMap.get(element) instanceof ISubModuleNodeExtension) {
 						// process nested submodule
 						submodule.addChild(build((ISubModuleNodeExtension) elementMap.get(element), targetId,
 								navigationArgument, copy(context)));
 					} else if (elementMap.get(element) instanceof INavigationAssemblyExtension) {
 						// process assembly reference
 						INavigationAssemblyExtension assemblyref = (INavigationAssemblyExtension) elementMap
 								.get(element);
 						if (assemblyref != null) {
 							INavigationAssembler assembler = getAssemblerProvider().getNavigationAssembler(
 									assemblyref.getRef());
 							if (assembler != null && assembler.getAssembly().getSubModuleNode() != null) {
 								submodule.addChild(build(assembler.getAssembly().getSubModuleNode(), targetId,
 										navigationArgument, copy(context)));
 							}
 						}
 					} else if (elementMap.get(element) instanceof IForEachExtension) {
 						// process foreach loop element
 						processForeachLoop(targetId, navigationArgument, context, submodule,
 								(IForEachExtension) elementMap.get(element));
 					}
 				}
 			}
 
 		} finally {
 			cleanupVariableResolver();
 		}
 
 		return submodule;
 	}
 
 	private void processForeachLoop(NavigationNodeId targetId, NavigationArgument navigationArgument,
 			Map<String, Object> context, ISubModuleNode submodule, IForEachExtension foreach) {
 
 		String loopVar = foreach.getElement();
 		String in = foreach.getIn();
 		String variable = getVariable(in);
 		String argument = getArgument(in);
 		if (variable != null) {
 			try {
 				Object bean = resolveCoreVariable(variable, navigationArgument);
 				if (bean != null) {
 					Object obj = bean;
 					if (argument != null) {
 						obj = new PropertyUtilsBean().getNestedProperty(bean, argument);
 					}
 					if (obj != null) {
 						if (obj instanceof Collection<?>) {
 							for (Object each : (Collection<?>) obj) {
 								Map<String, Object> nodecontext = copy(context);
 								nodecontext.put(loopVar, each);
 								for (ISubModuleNodeExtension nestedSubmoduleDefinition : foreach.getSubModuleNodes()) {
 									submodule.addChild(build(nestedSubmoduleDefinition, targetId, navigationArgument,
 											nodecontext));
 								}
 							}
 						} else if (obj.getClass().isArray()) {
 							for (Object each : (Object[]) obj) {
 								Map<String, Object> nodecontext = copy(context);
 								nodecontext.put(loopVar, each);
 								for (ISubModuleNodeExtension nestedSubmoduleDefinition : foreach.getSubModuleNodes()) {
 									submodule.addChild(build(nestedSubmoduleDefinition, targetId, navigationArgument,
 											nodecontext));
 								}
 							}
 						}
 					}
 				}
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	protected Map<String, Object> copy(Map<String, Object> context) {
 
 		return new HashMap<String, Object>(context);
 	}
 
 	private void initializeAcceptedTargetIds() {
 
 		if (assembly != null) {
 			// build module group if it exists
 			ISubApplicationNodeExtension subapplicationDefinition = assembly.getSubApplicationNode();
 			if (subapplicationDefinition != null) {
 				resolveTargetIds(subapplicationDefinition);
 			}
 			// build module group if it exists
 			IModuleGroupNodeExtension groupDefinition = assembly.getModuleGroupNode();
 			if (groupDefinition != null) {
 				resolveTargetIds(groupDefinition);
 			}
 			// otherwise try module
 			IModuleNodeExtension moduleDefinition = assembly.getModuleNode();
 			if (moduleDefinition != null) {
 				resolveTargetIds(moduleDefinition);
 			}
 			// last resort is submodule
 			ISubModuleNodeExtension submoduleDefinition = assembly.getSubModuleNode();
 			if (submoduleDefinition != null) {
 				resolveTargetIds(submoduleDefinition);
 			}
 		}
 	}
 
 	private void resolveTargetIds(ISubApplicationNodeExtension subapplicationDefinition) {
 
 		updateAcceptedTargetIds(subapplicationDefinition);
 		for (IModuleGroupNodeExtension groupDefinition : subapplicationDefinition.getModuleGroupNodes()) {
 			resolveTargetIds(groupDefinition);
 		}
 	}
 
 	private void resolveTargetIds(IModuleGroupNodeExtension groupDefinition) {
 
 		updateAcceptedTargetIds(groupDefinition);
 		for (IModuleNodeExtension moduleDefinition : groupDefinition.getModuleNodes()) {
 			resolveTargetIds(moduleDefinition);
 		}
 	}
 
 	private void resolveTargetIds(IModuleNodeExtension moduleDefinition) {
 
 		updateAcceptedTargetIds(moduleDefinition);
 		for (ISubModuleNodeExtension submoduleDefinition : moduleDefinition.getSubModuleNodes()) {
 			resolveTargetIds(submoduleDefinition);
 		}
 	}
 
 	private void resolveTargetIds(ISubModuleNodeExtension submoduleDefinition) {
 
 		updateAcceptedTargetIds(submoduleDefinition);
 		for (ISubModuleNodeExtension nestedDefinition : submoduleDefinition.getSubModuleNodes()) {
 			resolveTargetIds(nestedDefinition);
 		}
 	}
 
 	private void updateAcceptedTargetIds(INodeExtension nodeDefinition) {
 
 		if (nodeDefinition.getTypeId() != null) {
 			acceptedTargetIds.add(nodeDefinition.getTypeId());
 		}
 	}
 
 	protected void updateContext(INavigationNode<?> node, NavigationArgument navigationArgument) {
 
 		if (navigationArgument != null) {
 			node.setContext(NavigationArgument.CONTEXT_KEY_PARAMETER, navigationArgument.getParameter());
 		}
 
 	}
 
 	protected NavigationNodeId createNavigationNodeIdFromTemplate(NavigationNodeId template,
			INodeExtension nodeExtension, NavigationArgument navigationArgument) {
 
		String typeId = nodeExtension.getTypeId();
		String instanceId = nodeExtension.getInstanceId() == null ? template.getInstanceId() : nodeExtension
 				.getInstanceId();
 
 		return new NavigationNodeId(typeId, instanceId);
 	}
 
 	protected String resolveVariables(String string) {
 
 		try {
 			return VariableManagerUtil.substitute(string);
 		} catch (CoreException ex) {
 			ex.printStackTrace();
 			return string;
 		}
 	}
 
 	protected Map<String, Object> createMapping(NavigationNodeId targetId, NavigationArgument navigationArgument) {
 
 		Map<String, Object> mapping = new HashMap<String, Object>();
 		mapping.put(VAR_NAVIGATION_NODEID, targetId);
 		if (navigationArgument != null) {
 			mapping.put(VAR_NAVIGATION_PARAMETER, navigationArgument.getParameter());
 		}
 
 		return mapping;
 	}
 
 	protected void startVariableResolver(Map<String, Object> mapping) {
 		ThreadLocalMapResolver.configure(mapping);
 	}
 
 	protected void cleanupVariableResolver() {
 		ThreadLocalMapResolver.cleanup();
 	}
 
 	private Map<IConfigurationElement, Object> createElementMap(INodeExtension nodeDefinition) {
 
 		Map<IConfigurationElement, Object> elementMap = new HashMap<IConfigurationElement, Object>();
 		// add child node definitions
 		for (INodeExtension childNodeDefinition : nodeDefinition.getChildNodes()) {
 			elementMap.put(childNodeDefinition.getConfigurationElement(), childNodeDefinition);
 		}
 		// add assembly references
 		for (INavigationAssemblyExtension assemblyref : nodeDefinition.getAssemblies()) {
 			elementMap.put(assemblyref.getConfigurationElement(), assemblyref);
 		}
 		// add foreach definition if present and the node is a submodule
 		if (nodeDefinition instanceof ISubModuleNodeExtension) {
 			IForEachExtension foreachDefinition = ((ISubModuleNodeExtension) nodeDefinition).getForeach();
 			if (foreachDefinition != null) {
 				elementMap.put(foreachDefinition.getConfigurationElement(), foreachDefinition);
 			}
 		}
 
 		return elementMap;
 	}
 
 	/*
 	 * variableDef has the form '${variablename:argument}'
 	 */
 	private String getVariable(String variableDef) {
 
 		int start = 0;
 		if (variableDef.startsWith(VARIABLE_START)) {
 			start += VARIABLE_START.length();
 		}
 		int variableArgumentSeparator = variableDef.indexOf(VARIABLE_ARG);
 		if (variableArgumentSeparator > 0) {
 			return variableDef.substring(start, variableArgumentSeparator);
 		} else {
 			int end = variableDef.length();
 			if (variableDef.charAt(end - 1) == VARIABLE_END) {
 				end--;
 			}
 			return variableDef.substring(start, end);
 		}
 	}
 
 	/*
 	 * variableDef has the form '${variablename:argument}'
 	 */
 	private String getArgument(String variableDef) {
 
 		int end = variableDef.length();
 		if (variableDef.charAt(end - 1) == VARIABLE_END) {
 			end--;
 		}
 		int variableArgumentSeparator = variableDef.indexOf(VARIABLE_ARG);
 		if (variableArgumentSeparator > 0) {
 			return variableDef.substring(variableArgumentSeparator + 1, end);
 		} else {
 			// assume variable without argument
 			return null;
 		}
 	}
 
 	protected Object resolveCoreVariable(String variable, NavigationArgument navigationArgument) {
 
 		if (NavigationArgument.CONTEXT_KEY_PARAMETER.equals(variable)) {
 			return navigationArgument.getParameter();
 		}
 
 		return null;
 	}
 }
