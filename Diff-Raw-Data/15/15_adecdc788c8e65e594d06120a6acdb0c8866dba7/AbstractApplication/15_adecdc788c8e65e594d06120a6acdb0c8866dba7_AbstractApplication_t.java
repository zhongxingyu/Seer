 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.application;
 
 import java.util.SortedSet;
 import java.util.TreeSet;
 
import org.osgi.service.log.LogService;

 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 import org.eclipse.equinox.log.Logger;

 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.injector.Inject;
 import org.eclipse.riena.internal.navigation.ui.Activator;
 import org.eclipse.riena.navigation.ApplicationNodeManager;
 import org.eclipse.riena.navigation.IApplicationNode;
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationAssembler;
 import org.eclipse.riena.navigation.INavigationAssemblyExtension;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.INavigationNodeProvider;
 import org.eclipse.riena.navigation.INodeExtension;
 import org.eclipse.riena.navigation.ISubApplicationNode;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.model.ApplicationNode;
 import org.eclipse.riena.navigation.model.NavigationNodeProvider;
 import org.eclipse.riena.navigation.model.NavigationNodeProviderAccessor;
 import org.eclipse.riena.navigation.ui.login.ILoginDialogViewDefinition;
 import org.eclipse.riena.ui.core.resource.IIconManager;
 import org.eclipse.riena.ui.core.uiprocess.ProgressProviderBridge;
 
 /**
  * Abstract application defining the basic structure of a Riena application
  */
 public abstract class AbstractApplication implements IApplication {
 
 	private final static Logger LOGGER = Log4r.getLogger(Activator.getDefault(), AbstractApplication.class);
 	protected ILoginDialogViewDefinition loginDialogViewDefinition;
 
 	public Object start(IApplicationContext context) throws Exception {
 
 		Object result = initializePerformLogin(context);
 		if (!EXIT_OK.equals(result)) {
 			return result;
 		}
 
 		IApplicationNode node = createModel();
 		if (node == null) {
 			throw new RuntimeException(
 					"Application did not return an ApplicationModel in method 'createModel' but returned NULL. Cannot continue"); //$NON-NLS-1$
 		}
 		createStartupsFromExtensions(node);
 		ApplicationNodeManager.registerApplicationNode(node);
 		initializeNode(node);
 		setProgressProviderBridge();
 		return createView(context, node);
 	}
 
 	private void setProgressProviderBridge() {
 		ProgressProviderBridge bridge = ProgressProviderBridge.instance();
 		Job.getJobManager().setProgressProvider(bridge);
 		bridge.setVisualizerFactory(new VisualizerFactory());
 	}
 
 	/**
 	 * Overwrite to create own application model
 	 * 
 	 * @return IApplicationModelProvider - root of the configured application
 	 *         model
 	 */
 	protected IApplicationNode createModel() {
 		IApplicationNode applicationModel = new ApplicationNode(new NavigationNodeId(
 				ApplicationNode.DEFAULT_APPLICATION_TYPEID));
 		return applicationModel;
 	}
 
 	protected void createStartupsFromExtensions(IApplicationNode applicationNode) {
 
 		SortedSet<StartupSortable> startups = new TreeSet<StartupSortable>();
 
 		INavigationNodeProvider nnp = NavigationNodeProviderAccessor.getNavigationNodeProvider();
 		for (INavigationAssembler assembler : ((NavigationNodeProvider) nnp).getNavigationAssemblers()) {
 			Integer sequence = getAutostartSequence(assembler.getAssembly());
 			if (sequence != null) {
 				StartupSortable startupSortable = createStartupSortable(assembler.getAssembly(), sequence);
 				if (startupSortable != null) {
 					startups.add(startupSortable);
 				}
 			}
 		}
 
 		for (StartupSortable startup : startups) {
 			String message = "creating startup module %s [level=%s sequence=%d]"; //$NON-NLS-1$
 			LOGGER.log(LogService.LOG_INFO, String.format(message, startup.id, startup.level, startup.sequence));
 			applicationNode.create(new NavigationNodeId(startup.id));
 		}
 	}
 
 	private StartupSortable createStartupSortable(final INavigationAssemblyExtension assembly, final Integer sequence) {
 		String id = getTypeId(assembly.getSubApplicationNode());
 		if (id != null) {
 			return new StartupSortable(StartupLevel.SUBAPPLICATION, sequence, id);
 		}
 		id = getTypeId(assembly.getModuleGroupNode());
 		if (id != null) {
 			return new StartupSortable(StartupLevel.MODULEGROUP, sequence, id);
 		}
 		id = getTypeId(assembly.getModuleNode());
 		if (id != null) {
 			return new StartupSortable(StartupLevel.MODULE, sequence, id);
 		}
 		id = getTypeId(assembly.getSubModuleNode());
 		if (id != null) {
 			return new StartupSortable(StartupLevel.SUBMODULE, sequence, id);
 		}
 		id = assembly.getId();
 		if (id != null) {
 			return new StartupSortable(StartupLevel.CUSTOM, sequence, id);
 		}
 		return null;
 	}
 
 	protected void initializeNode(IApplicationNode model) {
 		initializeModelDefaults(model);
 	}
 
 	protected void initializeModelDefaults(IApplicationNode model) {
 		initializeNodeDefaults(model);
 	}
 
 	protected void initializeNodeDefaults(IApplicationNode node) {
 
 		for (ISubApplicationNode child : node.getChildren()) {
 			initializeNodeDefaults(child);
 		}
 	}
 
 	protected void initializeNodeDefaults(ISubApplicationNode node) {
 
 		for (IModuleGroupNode child : node.getChildren()) {
 			initializeNodeDefaults(child);
 		}
 	}
 
 	protected void initializeNodeDefaults(IModuleGroupNode node) {
 
 		for (IModuleNode child : node.getChildren()) {
 			initializeNodeDefaults(child);
 		}
 	}
 
 	protected void initializeNodeDefaults(IModuleNode node) {
 
 		initializeNodeDefaultIcon(node);
 		//		for (ISubModuleNode child : node.getChildren()) {
 		//			initializeNodeDefaults(child);
 		//		}
 	}
 
 	//	protected void initializeNodeDefaults(ISubModuleNode node) {
 	//
 	//		initializeNodeDefaultIcon(node);
 	//		for (ISubModuleNode child : node.getChildren()) {
 	//			initializeNodeDefaults(child);
 	//		}
 	//	}
 
 	protected void initializeNodeDefaultIcon(INavigationNode<?> node) {
 
 		if (node.getIcon() == null) {
 			node.setIcon(IIconManager.DEFAULT_ICON);
 		}
 	}
 
 	abstract protected Object createView(IApplicationContext context, IApplicationNode pNode) throws Exception;
 
 	protected Object initializePerformLogin(IApplicationContext context) throws Exception {
 
 		initializeLoginViewDefinition();
 
 		if (isDialogLogin(context)) {
 			return performLogin(context);
 		} else {
 			return EXIT_OK;
 		}
 	}
 
 	protected boolean isDialogLogin(IApplicationContext context) {
 		return loginDialogViewDefinition != null;
 	}
 
 	protected boolean isSplashLogin(IApplicationContext context) {
 		return false;
 	}
 
 	protected Object doPerformLogin(IApplicationContext context) {
 
 		return EXIT_OK;
 	}
 
 	protected Object doPerformSplashLogin(IApplicationContext context) {
 
 		return EXIT_OK;
 	}
 
 	protected Object performLogin(IApplicationContext context) throws Exception {
 
 		if (isSplashLogin(context)) {
 			return doPerformSplashLogin(context);
 		} else {
 			return doPerformLogin(context);
 		}
 	}
 
 	public void update(ILoginDialogViewDefinition[] data) {
 
 		if (data.length > 0) {
 			loginDialogViewDefinition = data[0];
 		}
 	}
 
 	protected void initializeLoginViewDefinition() {
 
 		Inject.extension(ILoginDialogViewDefinition.EP_TYPE).useType(ILoginDialogViewDefinition.class).into(this)
 				.andStart(Activator.getDefault().getContext());
 	}
 
 	private Integer getAutostartSequence(INavigationAssemblyExtension assembly) {
 
 		try {
 			return assembly.getAutostartSequence();
 		} catch (Exception ignore) {
 			// does not seem to be an integer, assume no autostart
 			return null;
 		}
 	}
 
 	private String getTypeId(INodeExtension extension) {
 
 		if (extension == null) {
 			return null;
 		} else {
 			return extension.getTypeId();
 		}
 	}
 
 	enum StartupLevel {
 		SUBAPPLICATION, MODULEGROUP, MODULE, SUBMODULE, CUSTOM
 	}
 
 	static class StartupSortable implements Comparable<StartupSortable> {
 
 		private final StartupLevel level;
 		private final int sequence;
 		private final String id;
 
 		public StartupSortable(StartupLevel level, Integer sequence, String id) {
 			this.level = level;
 			this.sequence = sequence;
 			this.id = id;
 		}
 
 		public int compareTo(StartupSortable o) {
			if (sequence == o.sequence) {
 				return level.compareTo(o.level);
 			} else {
 				return sequence - o.sequence;
 			}
			//			if (level.compareTo(o.level) != 0) {
			//				return level.compareTo(o.level);
			//			} else {
			//				return sequence - o.sequence;
			//			}
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((level == null) ? 0 : level.hashCode());
 			result = prime * result + sequence;
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj) {
 				return true;
 			}
 			if (obj == null) {
 				return false;
 			}
 			if (getClass() != obj.getClass()) {
 				return false;
 			}
 			StartupSortable other = (StartupSortable) obj;
 			if (level == null) {
 				if (other.level != null) {
 					return false;
 				}
 			} else if (!level.equals(other.level)) {
 				return false;
 			}
 			if (sequence != other.sequence) {
 				return false;
 			}
 			return true;
 		}
 
 	}
 }
