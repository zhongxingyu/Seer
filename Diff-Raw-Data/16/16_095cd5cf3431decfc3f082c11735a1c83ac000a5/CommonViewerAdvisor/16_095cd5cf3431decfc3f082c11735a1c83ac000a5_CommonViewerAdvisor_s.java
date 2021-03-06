 /*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.team.internal.ui.mapping;
 
 import java.util.*;
 
 import org.eclipse.core.resources.mapping.*;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.action.*;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.team.core.TeamException;
 import org.eclipse.team.core.mapping.*;
 import org.eclipse.team.internal.ui.TeamUIPlugin;
 import org.eclipse.team.internal.ui.Utils;
 import org.eclipse.team.internal.ui.synchronize.AbstractTreeViewerAdvisor;
 import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;
 import org.eclipse.team.ui.TeamUI;
 import org.eclipse.team.ui.mapping.*;
 import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
 import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant;
 import org.eclipse.ui.*;
 import org.eclipse.ui.actions.ActionContext;
 import org.eclipse.ui.navigator.*;
 import org.eclipse.ui.navigator.resources.ResourceDragAdapterAssistant;
 import org.eclipse.ui.part.IPageSite;
 
 /**
  * Provides a Common Navigator based viewer for use by a {@link ModelSynchronizePage}.
  */
 public class CommonViewerAdvisor extends AbstractTreeViewerAdvisor implements INavigatorContentServiceListener, IEmptyTreeListener, IPropertyChangeListener {
 
 	public static final class NavigableCommonViewer extends CommonViewer implements ITreeViewerAccessor {
 		private final IEmptyTreeListener listener;
 		private boolean empty;
 		private NavigableCommonViewer(String id, Composite parent, int style, IEmptyTreeListener listener) {
 			super(id, parent, style);
 			this.listener = listener;
 		}
 		public void createChildren(TreeItem item) {
 			super.createChildren(item);
 		}
 		public void openSelection() {
 			fireOpen(new OpenEvent(this, getSelection()));
 		}
 		protected void internalRefresh(Object element, boolean updateLabels) {
 			TreePath[] expanded = getVisibleExpandedPaths();
 			super.internalRefresh(element, updateLabels);
 			setExpandedTreePaths(expanded);
 			checkForEmptyViewer();
 		}
 		protected void internalRemove(Object parent, Object[] elements) {
 			super.internalRemove(parent, elements);
 			if (parent == getInput())
 				checkForEmptyViewer();
 		}
 		protected void internalRemove(Object[] elements) {
 			super.internalRemove(elements);
 			checkForEmptyViewer();
 		}
 		protected void internalAdd(Widget widget, Object parentElement, Object[] childElements) {
 			super.internalAdd(widget, parentElement, childElements);
 			if (empty) {
 				empty = false;
 				listener.notEmpty(this);
 			}
 				
 		}
 		protected void inputChanged(Object input, Object oldInput) {
 			super.inputChanged(input, oldInput);
 			checkForEmptyViewer();
 		}
 		private void checkForEmptyViewer() {
 			Object input = getInput();
 			if (input != null) {
 				Widget w = findItem(input);
 				Item[] children = getChildren(w);
 				if (children.length == 0) {
 					if (!empty) {
 						empty = true;
 						listener.treeEmpty(this);
 					}
 					return;
 				}
 			}
 			empty = false;
 			if (listener != null)
 				listener.notEmpty(this);
 		}
 		public boolean isEmpty() {
 			return empty;
 		}
 		
 		protected void initDragAndDrop() {
 			getNavigatorContentService().getDnDService().bindDragAssistant(new ResourceDragAdapterAssistant());
 			super.initDragAndDrop();
 		}
 	    /**
 	     * Gets the expanded elements that are visible to the user. An expanded
 	     * element is only visible if the parent is expanded.
 	     * 
 	     * @return the visible expanded elements
 	     * @since 2.0
 	     */
 	    public TreePath[] getVisibleExpandedPaths() {
 	        ArrayList v = new ArrayList();
 	        internalCollectVisibleExpanded(v, getControl());
 	        return (TreePath[]) v.toArray(new TreePath[v.size()]);
 	    }
 
 	    private void internalCollectVisibleExpanded(ArrayList result, Widget widget) {
 	        Item[] items = getChildren(widget);
 	        for (int i = 0; i < items.length; i++) {
 	            Item item = items[i];
 	            if (getExpanded(item)) {
 	            	TreePath path = getTreePathFromItem(item);
 	                if (path != null) {
 						result.add(path);
 	                }
 	                //Only recurse if it is expanded - if
 	                //not then the children aren't visible
 	                internalCollectVisibleExpanded(result, item);
 	            }
 	        }
 	    }
 	}
 
 	/**
 	 * Subclass of SubActionBars that manages the contributions from the common action service
 	 */
 	private class CommonSubActionBars extends SubActionBars {
 
 		public CommonSubActionBars(IActionBars parent) {
 			super(parent);
 		}
 		
 		public void setGlobalActionHandler(String actionID, IAction handler) {
 			if (handler == null) {
 				// Only remove the handler if it was set
 				if (getGlobalActionHandler(actionID) != null) {
 					getParent().setGlobalActionHandler(actionID, null);
 					super.setGlobalActionHandler(actionID, null);
 				}
 			} else {
 				// Only set the action handler if the parent doesn't
 				if (getParent().getGlobalActionHandler(actionID) != null) {
 					TeamUIPlugin.log(new TeamException(NLS.bind("Conflicting attempt to set action id {0} detected", actionID))); //$NON-NLS-1$
 					return;
 				}
 				super.setGlobalActionHandler(actionID, handler);
 			}
 		}
 		
 		public void clearGlobalActionHandlers() {
 			// When cleared, also remove the ids from the parent
 			Map handlers = getGlobalActionHandlers();
 			if (handlers != null) {
 				Set keys = handlers.keySet();
 				Iterator iter = keys.iterator();
 				while (iter.hasNext()) {
 					String actionId = (String) iter.next();
 					getParent().setGlobalActionHandler(actionId,
 							null);
 				}
 			}
 			super.clearGlobalActionHandlers();
 		}
 		
 		public void updateActionBars() {
 			// On update, push all or action handlers into our parent
 			Map newActionHandlers = getGlobalActionHandlers();
 			if (newActionHandlers != null) {
 				Set keys = newActionHandlers.entrySet();
 				Iterator iter = keys.iterator();
 				while (iter.hasNext()) {
 					Map.Entry entry = (Map.Entry) iter.next();
 					getParent().setGlobalActionHandler((String) entry.getKey(),
 							(IAction) entry.getValue());
 				}
 			}
 			super.updateActionBars();
 		}
 		
 	}
 	
 	public static final String TEAM_NAVIGATOR_CONTENT = "org.eclipse.team.ui.navigatorViewer"; //$NON-NLS-1$
 
 	private static final String PROP_ACTION_SERVICE_ACTION_BARS = "org.eclipse.team.ui.actionServiceActionBars"; //$NON-NLS-1$
 	
 	private Set extensions = new HashSet();
 	
 	private NavigatorActionService actionService;
 
 	private IEmptyTreeListener emptyTreeListener;
 	
 	/**
 	 * Create a common viewer
 	 * @param parent the parent composite of the common viewer
 	 * @param configuration the configuration for the viewer
 	 * @return a newly created common viewer
 	 */
 	private static CommonViewer createViewer(Composite parent, final ISynchronizePageConfiguration configuration, IEmptyTreeListener listener) {
 		final CommonViewer v = new NavigableCommonViewer(configuration.getViewerId(), parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, listener);
 		v.setSorter(new CommonViewerSorter());
 		v.setSorter(new TeamViewerSorter((CommonViewerSorter)v.getSorter()));
 		ISynchronizationScope scope = getScope(configuration);
 		bindTeamContentProviders(v);
 		scope.addScopeChangeListener(new ISynchronizationScopeChangeListener() {
 			public void scopeChanged(final ISynchronizationScope scope,
 					ResourceMapping[] newMappings, ResourceTraversal[] newTraversals) {
 				enableContentProviders(v, configuration);
 				Utils.asyncExec(new Runnable() {			
 					public void run() {
 						v.refresh();
 					}
 				
 				}, v);
 			}
 		});
 		enableContentProviders(v, configuration);
 		configuration.getSite().setSelectionProvider(v);
 		return v;
 	}
 
 	private static void enableContentProviders(CommonViewer v, ISynchronizePageConfiguration configuration) {
 		v.getNavigatorContentService().getActivationService().activateExtensions(getEnabledContentProviders(configuration), true);
 	}
 
 	private static String[] getEnabledContentProviders(ISynchronizePageConfiguration configuration) {
 		String visibleModel = (String)configuration.getProperty(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER);
 		if (visibleModel != null && !visibleModel.equals(ModelSynchronizeParticipant.ALL_MODEL_PROVIDERS_VISIBLE)) {
 			ITeamContentProviderDescriptor desc = TeamUI.getTeamContentProviderManager().getDescriptor(visibleModel);
 			if (desc != null && desc.isEnabled())
 				return new String[] { desc.getContentExtensionId() };
 		}
 		configuration.setProperty(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER, ModelSynchronizeParticipant.ALL_MODEL_PROVIDERS_VISIBLE);
 		ModelSynchronizeParticipant participant = (ModelSynchronizeParticipant)configuration.getParticipant();
 		ModelProvider[] providers = participant.getEnabledModelProviders();
 		Set result = new HashSet();
 		Object property = configuration.getProperty(ITeamContentProviderManager.PROP_PAGE_LAYOUT);
 		boolean isFlatLayout = property != null && property.equals(ITeamContentProviderManager.FLAT_LAYOUT);
 		for (int i = 0; i < providers.length; i++) {
 			ModelProvider provider = providers[i];
 			ITeamContentProviderDescriptor desc = TeamUI.getTeamContentProviderManager().getDescriptor(provider.getId());
 			if (desc != null && desc.isEnabled() && (!isFlatLayout || desc.isFlatLayoutSupported()))
 				result.add(desc.getContentExtensionId());
 		}
 		return (String[]) result.toArray(new String[result.size()]);
 	}
 
 	private static void bindTeamContentProviders(CommonViewer v) {
 		ITeamContentProviderManager teamContentProviderManager = TeamUI.getTeamContentProviderManager();
 		ITeamContentProviderDescriptor[] descriptors = teamContentProviderManager.getDescriptors();
 		Set toBind = new HashSet();
 		for (int i = 0; i < descriptors.length; i++) {
 			ITeamContentProviderDescriptor descriptor = descriptors[i];
 			toBind.add(descriptor.getContentExtensionId());
 		}
 		v.getNavigatorContentService().bindExtensions((String[]) toBind.toArray(new String[toBind.size()]), true);
 	}
 	
 	private static ISynchronizationScope getScope(ISynchronizePageConfiguration configuration) {
 		return (ISynchronizationScope)configuration.getProperty(ITeamContentProviderManager.P_SYNCHRONIZATION_SCOPE);
 	}
 
 	/**
 	 * Create the advisor using the given configuration
 	 * @param parent the parent
 	 * @param configuration the configuration
 	 */
 	public CommonViewerAdvisor(Composite parent, ISynchronizePageConfiguration configuration) {
 		super(configuration);
 		final CommonViewer viewer = CommonViewerAdvisor.createViewer(parent, configuration, this);
 		TeamUI.getTeamContentProviderManager().addPropertyChangeListener(this);
 		configuration.addPropertyChangeListener(this);
 		GridData data = new GridData(GridData.FILL_BOTH);
 		viewer.getControl().setLayoutData(data);
         viewer.getNavigatorContentService().addListener(this);
         initializeViewer(viewer);
 		IBaseLabelProvider provider = viewer.getLabelProvider();
 		if (provider instanceof DecoratingLabelProvider) {
 			DecoratingLabelProvider dlp = (DecoratingLabelProvider) provider;
 			ILabelDecorator decorator = ((SynchronizePageConfiguration)configuration).getLabelDecorator();
 			if (decorator != null) {
 				ILabelProvider lp = dlp.getLabelProvider();
 				dlp = new DecoratingLabelProvider(
 						new DecoratingLabelProvider(lp, decorator),
 						PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator());
 				viewer.setLabelProvider(dlp);
 			}
 			DecorationContext decorationContext = new DecorationContext();
 			decorationContext.putProperty(SynchronizationStateTester.PROP_TESTER, new SynchronizationStateTester() {
 				public boolean isStateDecorationEnabled() {
 					return false;
 				}
 			});
 			dlp.setDecorationContext(decorationContext);
 		}
 	}
 	
 	public void setInitialInput() {
 		CommonViewer viewer = (CommonViewer)getViewer();
         viewer.setInput(getInitialInput());
         viewer.expandToLevel(2);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.team.internal.ui.synchronize.StructuredViewerAdvisor#initializeViewer(org.eclipse.jface.viewers.StructuredViewer)
 	 */
 	public void initializeViewer(StructuredViewer viewer) {
 		createActionService((CommonViewer)viewer, getConfiguration());
 		super.initializeViewer(viewer);
 	}
 	
 	private void createActionService(CommonViewer viewer, ISynchronizePageConfiguration configuration) {
 		ICommonViewerSite commonSite = createCommonViewerSite(viewer, configuration);
 		actionService = new NavigatorActionService(commonSite, viewer, viewer.getNavigatorContentService());
 	}
 
 	private ICommonViewerSite createCommonViewerSite(CommonViewer viewer, ISynchronizePageConfiguration configuration) {
 		IWorkbenchSite site = configuration.getSite().getWorkbenchSite();
 		if (site instanceof IEditorSite) {
 			IEditorSite es = (IEditorSite) site;
 			return CommonViewerSiteFactory.createCommonViewerSite(es);
 		}
 		if (site instanceof IViewSite) {
 			IViewSite vs = (IViewSite) site;
 			return CommonViewerSiteFactory.createCommonViewerSite(vs);
 		}
 		if (site instanceof IPageSite) {
 			IPageSite ps = (IPageSite) site;
 			return CommonViewerSiteFactory.createCommonViewerSite(configuration.getViewerId(), ps);
 		}
 		return CommonViewerSiteFactory.createCommonViewerSite(configuration.getViewerId(), viewer, configuration.getSite().getShell());
 	}
 
 	private Object getInitialInput() {
 		String visible = (String)getConfiguration().getProperty(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER);
 		if (visible != null && !visible.equals(ModelSynchronizeParticipant.ALL_MODEL_PROVIDERS_VISIBLE)) {
 			try {
 				IModelProviderDescriptor desc = ModelProvider.getModelProviderDescriptor(visible);
 				if (desc != null)
 					return desc.getModelProvider();
 			} catch (CoreException e) {
 				TeamUIPlugin.log(e);
 			}
 		}
 		return getConfiguration().getProperty(ITeamContentProviderManager.P_SYNCHRONIZATION_CONTEXT);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentServiceListener#onLoad(org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension)
 	 */
 	public void onLoad(INavigatorContentExtension anExtension) {
 		extensions.add(anExtension);
 		ISynchronizationContext context = getParticipant().getContext();
 		anExtension.getStateModel().setProperty(ITeamContentProviderManager.P_SYNCHRONIZATION_SCOPE, context.getScope());
 		anExtension.getStateModel().setProperty(ITeamContentProviderManager.P_SYNCHRONIZATION_PAGE_CONFIGURATION, getConfiguration());
 		anExtension.getStateModel().setProperty(ITeamContentProviderManager.P_SYNCHRONIZATION_CONTEXT, context);
 	}
 
 	private ModelSynchronizeParticipant getParticipant() {
 		return (ModelSynchronizeParticipant)getConfiguration().getParticipant();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.team.internal.ui.synchronize.StructuredViewerAdvisor#getContextMenuId(org.eclipse.jface.viewers.StructuredViewer)
 	 */
 	protected String getContextMenuId(StructuredViewer viewer) {
 		return ((CommonViewer)viewer).getNavigatorContentService().getViewerDescriptor().getPopupMenuId();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.team.internal.ui.synchronize.StructuredViewerAdvisor#registerContextMenu(org.eclipse.jface.viewers.StructuredViewer, org.eclipse.jface.action.MenuManager)
 	 */
 	protected void registerContextMenu(StructuredViewer viewer, MenuManager menuMgr) {
 		actionService.prepareMenuForPlatformContributions(menuMgr,
 				viewer, false);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.team.internal.ui.synchronize.StructuredViewerAdvisor#fillContextMenu(org.eclipse.jface.viewers.StructuredViewer, org.eclipse.jface.action.IMenuManager)
 	 */
 	protected void fillContextMenu(StructuredViewer viewer, IMenuManager manager) {
 		// Clear any handlers from the menu
 		if (manager instanceof CommonMenuManager) {
 			CommonMenuManager cmm = (CommonMenuManager) manager;
 			cmm.clearHandlers();
 		}
 		
 		// Add the actions from the service (which willal so add the groups)
 		ISelection selection = getViewer().getSelection();
 		actionService.setContext(new ActionContext(selection));
 		actionService.fillContextMenu(manager);
 		
 		// Add any programmatic menu items
 		super.fillContextMenu(viewer, manager);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.team.internal.ui.synchronize.StructuredViewerAdvisor#dispose()
 	 */
 	public void dispose() {
 		TeamUI.getTeamContentProviderManager().removePropertyChangeListener(this);
 		getConfiguration().removePropertyChangeListener(this);
 		actionService.dispose();
 		super.dispose();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.team.internal.ui.synchronize.StructuredViewerAdvisor#updateActionBars(org.eclipse.jface.viewers.IStructuredSelection)
 	 */
 	protected void updateActionBars(IStructuredSelection selection) {
 		super.updateActionBars(selection);
 		if (!getConfiguration().getSite().isModal()) {
 			actionService.setContext(new ActionContext(selection));
 			// This is non-standard behavior that is required by the common navigator framework (see bug 122808)
 			SubActionBars subActionBars = (SubActionBars)getConfiguration().getProperty(PROP_ACTION_SERVICE_ACTION_BARS);
 			if (subActionBars == null) {
 				subActionBars = new CommonSubActionBars(getConfiguration().getSite().getActionBars());
 				getConfiguration().setProperty(PROP_ACTION_SERVICE_ACTION_BARS, subActionBars);
 			}
 			actionService.fillActionBars(subActionBars);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.team.internal.ui.synchronize.StructuredViewerAdvisor#createContextMenuManager(java.lang.String)
 	 */
 	protected MenuManager createContextMenuManager(String targetID) {
 		return new CommonMenuManager(targetID);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.team.internal.ui.synchronize.StructuredViewerAdvisor#addContextMenuGroups(org.eclipse.jface.action.IMenuManager)
 	 */
 	protected void addContextMenuGroups(IMenuManager manager) {
 		// Don't do anything. The groups will be added by the action service
 	}
 
 	public void addEmptyTreeListener(IEmptyTreeListener emptyTreeListener) {
 		this.emptyTreeListener = emptyTreeListener;
 	}
 
 	public void treeEmpty(TreeViewer viewer) {
 		if (emptyTreeListener != null)
 			emptyTreeListener.treeEmpty(viewer);
 	}
 
 	public void notEmpty(TreeViewer viewer) {
 		if (emptyTreeListener != null)
 			emptyTreeListener.notEmpty(viewer);
 	}
 
 	public void propertyChange(final PropertyChangeEvent event) {
 		if (event.getProperty().equals(ITeamContentProviderManager.PROP_ENABLED_MODEL_PROVIDERS)) {
 			enableContentProviders((CommonViewer)getViewer(), getConfiguration());
 		} else if (event.getProperty().equals(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER)) {
 			enableContentProviders((CommonViewer)getViewer(), getConfiguration());
 			final Viewer viewer = getViewer();
 			Utils.syncExec(new Runnable() {
 				public void run() {
 					Object viewerInput = ModelSynchronizePage.getViewerInput(getConfiguration(), (String)event.getNewValue());
 					if (viewer != null && viewerInput != null) {
 						viewer.setInput(viewerInput);
 					}
 				}
 			}, (StructuredViewer)viewer);
 		} else if (event.getProperty().equals(ITeamContentProviderManager.PROP_PAGE_LAYOUT)) {
 			// TODO
 			enableContentProviders((CommonViewer)getViewer(), getConfiguration());
 		}
 	}
 	
 	protected boolean handleDoubleClick(StructuredViewer viewer, DoubleClickEvent event) {
 		if (isOpenable(event.getSelection())) {
 			return true;
 		}
 		return super.handleDoubleClick(viewer, event);
 	}
 
 	private boolean isOpenable(ISelection selection) {
 		IStructuredSelection ss = (IStructuredSelection) selection;
 		Object object = ss.getFirstElement();
 		if (object == null)
 			return false;
 		return getParticipant().hasCompareInputFor(object);
 	}
 	
 	protected void expandToNextDiff(Object element) {
 		((TreeViewer)getViewer()).expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);
 	}
 
 }
