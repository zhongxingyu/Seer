 package net.sourceforge.eclipseccase.views;
 
 import java.util.*;
 import net.sourceforge.eclipseccase.*;
 import net.sourceforge.eclipseccase.ui.ClearCaseUI;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.team.core.Team;
 import org.eclipse.ui.*;
 import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
 import org.eclipse.ui.views.navigator.ResourceComparator;
 import org.eclipse.ui.views.navigator.ResourceNavigator;
 
 /**
  * The Checkouts view
  */
 @SuppressWarnings("deprecation")
 public class CheckoutsView extends ResourceNavigator implements IResourceStateListener, IResourceChangeListener {
 	protected static final CoreException IS_AFFECTED_EX = new CoreException(Status.CANCEL_STATUS);
 
 	private static final String SETTING_HIDE_CHECKOUTS = "hideCheckouts";
 
 	private static final String SETTING_HIDE_NEW_ELEMENTS = "hideNewElements";
 
 	private static final String SETTING_HIDE_HIJACKED_ELEMENTS = "hideHijackedElements";
 
 	private static final String DIALOG_SETTINGS_STORE = "CheckoutsView";
 
 	/** the dialog settings */
 	private IDialogSettings settings;

 	private Boolean inClearCaseRefresh = false;
 
 	private CheckoutsViewContentProvider contentProvider;
 
 	protected boolean initialized = false;
 
 	private CheckoutsViewRoot myRoot;
 
 	private boolean needsRefresh;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sourceforge.eclipseccase.views.ClearCaseViewPart#shouldAdd(org.eclipse
 	 * .core.resources.IResource)
 	 */
 	boolean shouldAdd(IResource resource) {
 		ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
 
 		if (null == provider)
 			return false;
 
 		if (ClearCaseUI.DEBUG_VIEWPRIV) {
 			ClearCaseUI.trace(ClearCaseUI.VIEWPRIV, "shouldAdd? " + resource.getFullPath()); //$NON-NLS-1$
 		}
 		// don't show ignored resources
 		if (Team.isIgnoredHint(resource)) {
 			if (ClearCaseUI.DEBUG_VIEWPRIV) {
 				ClearCaseUI.trace(ClearCaseUI.VIEWPRIV, " no, ignored"); //$NON-NLS-1$
 			}
 			return false;
 		}
 
 		// don't show resources with unknown state
 		if (provider.isUnknownState(resource)) {
 			if (ClearCaseUI.DEBUG_VIEWPRIV) {
 				ClearCaseUI.trace(ClearCaseUI.VIEWPRIV, " no, unknown state"); //$NON-NLS-1$
 			}
 			return false;
 		}
 
 		// optimize: query the cache only once:
 		StateCache state = provider.getCache(resource);
 
 		// show checkouts if enabled
 		if (!hideCheckouts() && state.isCheckedOut()) {
 			if (ClearCaseUI.DEBUG_VIEWPRIV) {
 				ClearCaseUI.trace(ClearCaseUI.VIEWPRIV, " yes, checked out"); //$NON-NLS-1$
 			}
 			return true;
 		}
 
 		// show new elements if enabled
 		if (!hideNewElements() && state.isViewprivate()) {
 			if (ClearCaseUI.DEBUG_VIEWPRIV) {
 				ClearCaseUI.trace(ClearCaseUI.VIEWPRIV, " yes, viewpriv"); //$NON-NLS-1$
 			}
 			return true;
 		}
 
 		// show Hijacked files if enabled
 		if (!hideHijackedElements() && state.isHijacked()) {
 			if (ClearCaseUI.DEBUG_VIEWPRIV) {
 				ClearCaseUI.trace(ClearCaseUI.VIEWPRIV, " yes, hijacked"); //$NON-NLS-1$
 			}
 			return true;
 		}
 
 		// hide all other
 		if (ClearCaseUI.DEBUG_VIEWPRIV) {
 			ClearCaseUI.trace(ClearCaseUI.VIEWPRIV, " no"); //$NON-NLS-1$
 		}
 		return false;
 	}
 
 	/**
 	 * Indicates if checkouts should not be shown.
 	 * 
 	 * @return
 	 */
 	public boolean hideCheckouts() {
 		return settings.getBoolean(SETTING_HIDE_CHECKOUTS);
 	}
 
 	/**
 	 * @param hide
 	 */
 	public void setHideCheckouts(boolean hide) {
 		if (hideCheckouts() != hide) {
 			settings.put(SETTING_HIDE_CHECKOUTS, hide);
 			if (!hide) {
 				// do a full refresh if we show additional info
 				refreshFromClearCase();
 			}
 			refresh();
 		}
 	}
 
 	/**
 	 * Indicates if new elements should not be shown.
 	 * 
 	 * @return
 	 */
 	public boolean hideNewElements() {
 		return settings.getBoolean(SETTING_HIDE_NEW_ELEMENTS);
 	}
 
 	/**
 	 * @param hide
 	 */
 	public void setHideNewElements(boolean hide) {
 		if (hideNewElements() != hide) {
 			settings.put(SETTING_HIDE_NEW_ELEMENTS, hide);
 			if (!hide) {
 				// do a full refresh if we show additional info
 				refreshFromClearCase();
 			}
 			refresh();
 		}
 	}
 
 	/**
 	 * Indicates if hijacked should not be shown.
 	 * 
 	 * @return
 	 */
 	public boolean hideHijackedElements() {
 		return settings.getBoolean(SETTING_HIDE_HIJACKED_ELEMENTS);
 	}
 
 	/**
 	 * @param hide
 	 */
 	public void setHideHijackedElements(boolean hide) {
 		if (hideHijackedElements() != hide) {
 			settings.put(SETTING_HIDE_HIJACKED_ELEMENTS, hide);
 			if (!hide) {
 				// do a full refresh if we show additional info
 				refreshFromClearCase();
 			}
 			refresh();
 		}
 	}
 
 	/**
 	 * Creates a new instance.
 	 */
 	public CheckoutsView() {
 		super();
 		IDialogSettings dialogSettings = ClearCaseUI.getInstance().getDialogSettings();
 		settings = dialogSettings.getSection(DIALOG_SETTINGS_STORE);
 		if (null == settings) {
 			settings = dialogSettings.addNewSection(DIALOG_SETTINGS_STORE);
 		}
 		// add defaults for hide/show state
 		if (settings.get(SETTING_HIDE_NEW_ELEMENTS) == null)
 			settings.put(SETTING_HIDE_NEW_ELEMENTS, true);
 
 		try {
 			// makes no sense to call refreshFromClearCase() here, as
 			// ClearCaseProvider.getUsedViewNames() always returns empty list in
 			// this stage at IDE startup. It's helpful if the view is opened
 			// sometime later in the session, when the caches are initialized
 			// etc.
 //			refreshFromClearCase();
 		} catch (Exception e) {
 		}
 
 		// TODO: need a way to refresh at first display of the VP view,
 		// if it is open when the IDE starts up (because it was open at last
 		// shutdown)
 
 	}
 
 	/**
 	 * Collects checked out, hijacked, view private files by calling a cleartool
 	 * subprocess to get the actual listing. Each found element is checked for a
 	 * corresponding IResource in the workspace. If found, that resource is
 	 * updated in the StateCache, which in turn triggers a StateChanged event
 	 * which updates the actual view.
 	 * 
 	 * All this may be very long operation, several minutes in huge CC views. It
 	 * is done in a newly created job.
 	 * 
 	 */
 	public void refreshFromClearCase() {
		synchronized (inClearCaseRefresh) {
 			// guard against second refresh while first one is not finished yet
 			if (inClearCaseRefresh) {
 				return;
 			}
 			inClearCaseRefresh = true;
 		}
 		String[] views = ClearCaseProvider.getUsedViewNames();
 		List<IResource> dirlist = new ArrayList<IResource>();
 		for (String v : views) {
 			IContainer dir = ClearCaseProvider.getViewFolder(v);
 			if (dir != null) {
 				dirlist.add(dir);
 			}
 		}
 		if (dirlist.size() > 0) {
 			final IResource[] array = dirlist.toArray(new IResource[dirlist.size()]);
 			final ViewPrivCollector collector = new ViewPrivCollector(array);
 			collector.setFindCheckedouts(!hideCheckouts());
 			collector.setFindOthers(!hideNewElements());
 			collector.setFindHijacked(!hideHijackedElements());
 			Job gatherViewPrivateStuff = new Job("Gathering view-private files") { //$NON-NLS-1$
 
 				@Override
 				protected IStatus run(IProgressMonitor monitor) {
 					try {
 						collector.collectElements(monitor);
 					} finally {
						synchronized (inClearCaseRefresh) {
 							inClearCaseRefresh = false;
 						}
 					}
 					return Status.OK_STATUS;
 				}
 			};
 			gatherViewPrivateStuff.setPriority(Job.LONG);
 			gatherViewPrivateStuff.schedule();
 		} else {
			synchronized (inClearCaseRefresh) {
 				inClearCaseRefresh = false;
 			}
 		}
 	}
 
 	/**
 	 * @see org.eclipse.team.internal.ccvs.ui.repo.RemoteViewPart#getContentProvider()
 	 */
 	protected CheckoutsViewContentProvider getContentProvider() {
 		if (contentProvider == null) {
 			contentProvider = new CheckoutsViewContentProvider();
 		}
 		return contentProvider;
 	}
 
 	@Override
 	protected void initContentProvider(TreeViewer viewer) {
 		viewer.setContentProvider(getContentProvider());
 		StateCacheFactory.getInstance().addStateChangeListerer(this);
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
 	}
 
 	@Override
 	protected void initLabelProvider(TreeViewer viewer) {
 		viewer.setLabelProvider(new DecoratingLabelProvider(new CheckoutsViewLabelProvider(), getPlugin().getWorkbench().getDecoratorManager().getLabelDecorator()));
 	}
 
 	@Override
 	public void setComparator(ResourceComparator comparator) {
 		super.setComparator(new ResourceComparator(comparator.getCriteria()) {
 
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see
 			 * org.eclipse.ui.views.navigator.ResourceSorter#compare(org.eclipse
 			 * .jface.viewers.Viewer, java.lang.Object, java.lang.Object)
 			 */
 			@Override
 			public int compare(Viewer viewer, Object o1, Object o2) {
 				// have to deal with non-resources in navigator
 				// if one or both objects are not resources, returned a
 				// comparison
 				// based on class.
 				if (!(o1 instanceof IResource && o2 instanceof IResource))
 					return compareClass(o1, o2);
 				IResource r1 = (IResource) o1;
 				IResource r2 = (IResource) o2;
 
 				int typeres = compareClearCaseTypes(r1, r2);
 				if (typeres != 0) {
 					return typeres;
 				}
 				if (getCriteria() == NAME)
 					return compareNames(r1, r2);
 				else if (getCriteria() == TYPE)
 					return compareTypes(r1, r2);
 				else
 					return 0;
 			}
 
 			@SuppressWarnings("unchecked")
 			@Override
 			protected int compareNames(IResource resource1, IResource resource2) {
 				return getComparator().compare(resource1.getFullPath().toString(), resource2.getFullPath().toString());
 			}
 
 			protected int compareClearCaseTypes(IResource resource1, IResource resource2) {
 				// TODO: optimize this, how can the repeated lookups be
 				// eliminated?
 				StateCache c1 = StateCacheFactory.getInstance().get(resource1);
 				StateCache c2 = StateCacheFactory.getInstance().get(resource2);
 				// sort checkedout files first
 				if (c1.isCheckedOut()) {
 					if (c2.isCheckedOut()) {
 						return 0;
 					} else {
 						return -1;
 					}
 				} else if (c2.isCheckedOut()) {
 					return 1;
 				} else {
 					// no CO
 					// sort hijacked files second
 					if (c1.isHijacked()) {
 						if (c2.isHijacked()) {
 							return 0;
 						} else {
 							return -1;
 						}
 					} else if (c2.isHijacked()) {
 						return 1;
 					} else {
 						// both files are neither CO nor hijacked
 						return 0;
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	protected IAdaptable getInitialInput() {
 		return getRoot();
 	}
 
 	protected CheckoutsViewRoot getRoot() {
 		if (null == myRoot) {
 			myRoot = new CheckoutsViewRoot(this);
 		}
 
 		return myRoot;
 	}
 
 	@Override
 	public void setWorkingSet(IWorkingSet workingSet) {
 		getContentProvider().setWorkingSet(workingSet);
 		super.setWorkingSet(workingSet);
 	}
 
 	@Override
 	public void updateTitle() {
 		// do nothing
 	}
 
 	@Override
 	protected void makeActions() {
 		setActionGroup(new CheckoutsViewActionGroup(this));
 	}
 
 	/**
 	 * Refreshes the viewer.
 	 */
 	public void refresh() {
 		if (getViewer() == null)
 			return;
 		getContentProvider().cancelJobs(getRoot());
 		getViewer().refresh();
 		initialized = true;
 	}
 
 	public void refreshInGuiThread() {
 		try {
 			getViewer().getControl().getDisplay().asyncExec(new Runnable() {
 				public void run() {
 					refreshFromClearCase();
 					refresh();
 				}
 			});
 		} catch (NullPointerException e) {
 			// if the getViewer() etc is not yet initialized, ignore that...
 		}
 	}
 
 	@Override
 	public void dispose() {
 		StateCacheFactory.getInstance().removeStateChangeListerer(this);
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 		super.dispose();
 	}
 
 	public void resourceStateChanged(final IResource[] resources) {
 		if (!initialized) {
 			refreshInGuiThread();
 			return;
 		}
 
 		for (int i = 0; i < resources.length; i++) {
 			final IResource resource = resources[i];
 
 			// filter out ignored resources
 			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
 			if (null == provider || provider.isIgnored(resource))
 				return;
 
 			// do not add non existent resources
 			final boolean shouldAdd = resource.exists() && shouldAdd(resource);
 
 			if (null != getViewer() && null != getViewer().getControl() && !getViewer().getControl().isDisposed()) {
 				getViewer().getControl().getDisplay().syncExec(new Runnable() {
 
 					public void run() {
 						if (null != getViewer() && null != getViewer().getControl() && !getViewer().getControl().isDisposed()) {
 							// we remove in every case
 							getViewer().remove(resource);
 
 							// only add if desired
 							if (shouldAdd) {
 								getViewer().add(getRoot(), resource);
 							}
 						}
 					}
 				});
 			}
 		}
 	}
 
 	public void resourceChanged(IResourceChangeEvent event) {
 		if (!initialized) {
 			refreshInGuiThread();
 			return;
 		}
 		IResourceDelta rootDelta = event.getDelta();
 		if (null != rootDelta) {
 
 			final Set<IResource> toRemove = new HashSet<IResource>();
 			needsRefresh = false;
 
 			try {
 				rootDelta.accept(new IResourceDeltaVisitor() {
 
 					public boolean visit(IResourceDelta delta) throws CoreException {
 
 						IResource resource = delta.getResource();
 						switch (delta.getKind()) {
 
 						case IResourceDelta.ADDED:
 							// do nothing
 							return false;
 
 						case IResourceDelta.REMOVED:
 							toRemove.add(delta.getResource());
 							return true;
 
 						case IResourceDelta.CHANGED:
 							if (null != resource) {
 								// filter out non clear case projects
 								if (resource.getType() == IResource.PROJECT) {
 									if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
 										if (((IProject) resource).isOpen()) {
 											// project changed from closed to
 											// open
 											if (null != ClearCaseProvider.getClearCaseProvider(resource)) {
 												// it's a CC project
 												needsRefresh = true;
 												return true;
 											} else {
 												return false;
 											}
 										} else {
 											// project changed from open to
 											// closed, we can't know now whether
 											// it was associated with CC, so do
 											// a refresh always :-(
 											needsRefresh = true;
 										}
 									} else if ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
 										needsRefresh = true;
 										return null != ClearCaseProvider.getClearCaseProvider(resource);
 									} else {
 										return null != ClearCaseProvider.getClearCaseProvider(resource);
 									}
 								}
 								return true;
 							}
 							return false;
 
 						default:
 							if (null != resource) {
 								// filter out non clear case projects
 								if (resource.getType() == IResource.PROJECT)
 									return null != ClearCaseProvider.getClearCaseProvider(delta.getResource());
 								return true;
 							}
 							return false;
 						}
 					}
 				});
 			} catch (CoreException ex) {
 				// refresh on exception
 				if (IS_AFFECTED_EX == ex) {
 					refresh();
 				}
 			}
 
 			if (null != getViewer() && null != getViewer().getControl() && !getViewer().getControl().isDisposed()) {
 				getViewer().getControl().getDisplay().syncExec(new Runnable() {
 
 					public void run() {
 						if (null != getViewer() && null != getViewer().getControl() && !getViewer().getControl().isDisposed()) {
 							// remove resources
 							getViewer().remove(toRemove.toArray());
 						}
 					}
 				});
 			}
 			if (needsRefresh) {
 				refreshInGuiThread();
 			}
 		}
 	}
 
 	@Override
 	public void init(IViewSite site, IMemento memento) throws PartInitException {
 		super.init(site, memento);
 		IWorkbenchSiteProgressService progressService = getProgressService();
 		if (null != progressService) {
 			progressService.showBusyForFamily(ClearCasePlugin.FAMILY_CLEARCASE_OPERATION);
 		}
 		refreshFromClearCase();
 	}
 
 	/**
 	 * Returns the IWorkbenchSiteProgressService for the receiver.
 	 * 
 	 * @return IWorkbenchSiteProgressService (maybe <code>null</code>)
 	 */
 	protected IWorkbenchSiteProgressService getProgressService() {
 		return (IWorkbenchSiteProgressService) getSite().getAdapter(IWorkbenchSiteProgressService.class);
 	}
 
 	@Override
 	protected void handleDoubleClick(DoubleClickEvent event) {
 		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 		Object element = selection.getFirstElement();
 
 		TreeViewer viewer = getTreeViewer();
 		if (viewer.isExpandable(element)) {
 			viewer.setExpandedState(element, !viewer.getExpandedState(element));
 		} else if (selection.size() == 1 && (element instanceof IResource)) {
 			// OpenFileAction ofa = new OpenFileAction(getSite().getPage());
 			// ofa.selectionChanged((IStructuredSelection)
 			// viewer.getSelection());
 			// if (ofa.isEnabled()) {
 			// ofa.run();
 			// }
 		}
 
 	}
 
 	public boolean isRefreshActive() {
 		return inClearCaseRefresh;
 	}
 }
