 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.debug.ui.display;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.dltk.debug.ui.display.internal.ConsoleDropDownAction;
 import org.eclipse.dltk.debug.ui.display.internal.ConsoleWorkbenchPart;
 import org.eclipse.dltk.debug.ui.display.internal.EvaluateConsoleFactoryManager;
 import org.eclipse.dltk.debug.ui.display.internal.OpenConsoleAction;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.IBasicPropertyConstants;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.console.AbstractConsole;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleConstants;
 import org.eclipse.ui.console.IConsolePageParticipant;
 import org.eclipse.ui.console.IConsoleView;
 import org.eclipse.ui.contexts.IContextActivation;
 import org.eclipse.ui.contexts.IContextService;
 import org.eclipse.ui.internal.console.ConsoleMessages;
 import org.eclipse.ui.internal.console.PinConsoleAction;
 import org.eclipse.ui.internal.console.ShowConsoleAction;
 import org.eclipse.ui.part.IPage;
 import org.eclipse.ui.part.IPageBookViewPage;
 import org.eclipse.ui.part.MessagePage;
 import org.eclipse.ui.part.PageBook;
 import org.eclipse.ui.part.PageBookView;
 import org.eclipse.ui.part.PageSwitcher;
 import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
 
 public class ScriptDisplayView extends PageBookView implements IConsoleView,
 		IPropertyChangeListener, IPartListener2 {
 
 	/**
 	 * Whether this console is pinned.
 	 */
 	private boolean fPinned = false;
 
 	/**
 	 * Stack of consoles in MRU order
 	 */
 	private List<IConsole> fStack = new ArrayList<IConsole>();
 
 	/**
 	 * The console being displayed, or <code>null</code> if none
 	 */
 	private IConsole fActiveConsole = null;
 
 	/**
 	 * Map of consoles to dummy console parts (used to close pages)
 	 */
 	private Map<IConsole, ConsoleWorkbenchPart> fConsoleToPart;
 
 	/**
 	 * Map of consoles to array of page participants
 	 */
 	private Map<IConsole, ListenerList> fConsoleToPageParticipants;
 
 	/**
 	 * Map of parts to consoles
 	 */
 	private Map<ConsoleWorkbenchPart, IConsole> fPartToConsole;
 
 	/**
 	 * Whether this view is active
 	 */
 	private boolean fActive = false;
 
 	/**
 	 * 'In Console View' context
 	 */
 	private IContextActivation fActivatedContext;
 
 	// actions
 	private PinConsoleAction fPinAction = null;
 	private ConsoleDropDownAction fDisplayConsoleAction = null;
 
 	private OpenConsoleAction fOpenConsoleAction = null;
 
 	private boolean fScrollLock;
 
 	private boolean isAvailable() {
 		return getPageBook() != null && !getPageBook().isDisposed();
 	}
 
 	public boolean getWordWrap() {
 		return false;
 	}
 
	@Override
 	public void setWordWrap(boolean wordWrap) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse
 	 * .jface.util.PropertyChangeEvent)
 	 */
 	public void propertyChange(PropertyChangeEvent event) {
 		Object source = event.getSource();
 		if (source instanceof IConsole
 				&& event.getProperty().equals(IBasicPropertyConstants.P_TEXT)) {
 			if (source.equals(getConsole())) {
 				updateTitle();
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
 	 */
 	public void partClosed(IWorkbenchPart part) {
 		super.partClosed(part);
 		fPinAction.update();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.internal.ui.console.IConsoleView#getConsole()
 	 */
 	public IConsole getConsole() {
 		return fActiveConsole;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.part.PageBookView#showPageRec(org.eclipse.ui.part.PageBookView
 	 * .PageRec)
 	 */
 	protected void showPageRec(PageRec pageRec) {
 		// don't show the page when pinned, unless this is the first console to
 		// be added
 		// or its the default page
 		if (fActiveConsole != null && pageRec.page != getDefaultPage()
 				&& fPinned && fConsoleToPart.size() > 1) {
 			IConsole console = fPartToConsole.get(pageRec.part);
 			if (!fStack.contains(console)) {
 				fStack.add(console);
 			}
 			return;
 		}
 
 		IConsole recConsole = fPartToConsole.get(pageRec.part);
 		if (recConsole != null && recConsole.equals(fActiveConsole)) {
 			return;
 		}
 
 		super.showPageRec(pageRec);
 		fActiveConsole = recConsole;
 		IConsole tos = null;
 		if (!fStack.isEmpty()) {
 			tos = fStack.get(0);
 		}
 		if (tos != null && !tos.equals(fActiveConsole)) {
 			deactivateParticipants(tos);
 		}
 		if (fActiveConsole != null && !fActiveConsole.equals(tos)) {
 			fStack.remove(fActiveConsole);
 			fStack.add(0, fActiveConsole);
 			activateParticipants(fActiveConsole);
 		}
 		updateTitle();
 		updateHelp();
 		// update console actions
 		if (fPinAction != null) {
 			fPinAction.update();
 		}
 		IPage page = getCurrentPage();
 		// if (page instanceof IOConsolePage) {
 		// ((IOConsolePage) page).setAutoScroll(!fScrollLock);
 		// }
 	}
 
 	/**
 	 * Activates the participants for the given console, if any.
 	 * 
 	 * @param console
 	 */
 	private void activateParticipants(IConsole console) {
 		// activate
 		if (console != null && fActive) {
 			final ListenerList listeners = getParticipants(console);
 			if (listeners != null) {
 				Object[] participants = listeners.getListeners();
 				for (int i = 0; i < participants.length; i++) {
 					final IConsolePageParticipant participant = (IConsolePageParticipant) participants[i];
 					SafeRunner.run(new ISafeRunnable() {
 						public void run() throws Exception {
 							participant.activated();
 						}
 
 						public void handleException(Throwable exception) {
 							DLTKDebugUIPlugin.log(exception);
 							listeners.remove(participant);
 						}
 					});
 				}
 			}
 		}
 	}
 
 	/**
 	 * Returns a stack of consoles in the view in MRU order.
 	 * 
 	 * @return a stack of consoles in the view in MRU order
 	 */
 	public List<IConsole> getConsoleStack() {
 		return fStack;
 	}
 
 	/**
 	 * Updates the view title based on the active console
 	 */
 	protected void updateTitle() {
 		IConsole console = getConsole();
 		if (console == null) {
 			setContentDescription(ConsoleMessages.ConsoleView_0);
 		} else {
 			String newName = console.getName();
 			String oldName = getContentDescription();
 			if (newName != null && !(newName.equals(oldName))) {
 				setContentDescription(console.getName());
 			}
 		}
 	}
 
 	protected void updateHelp() {
 		IConsole console = getConsole();
 		String helpContextId = null;
 		if (console instanceof AbstractConsole) {
 			AbstractConsole abs = (AbstractConsole) console;
 			helpContextId = abs.getHelpContextId();
 		}
 		if (helpContextId == null) {
 			// helpContextId = IConsoleHelpContextIds.CONSOLE_VIEW;
 		}
 		// PlatformUI.getWorkbench().getHelpSystem()
 		// .setHelp(getPageBook().getParent(), helpContextId);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.IWorkbenchPart
 	 * , org.eclipse.ui.part.PageBookView.PageRec)
 	 */
 	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
 		IConsole console = fPartToConsole.get(part);
 
 		// dispose page participants
 		ListenerList listeners = fConsoleToPageParticipants.remove(console);
 		if (listeners != null) {
 			Object[] participants = listeners.getListeners();
 			for (int i = 0; i < participants.length; i++) {
 				final IConsolePageParticipant participant = (IConsolePageParticipant) participants[i];
 				SafeRunner.run(new ISafeRunnable() {
 					public void run() throws Exception {
 						participant.dispose();
 					}
 
 					public void handleException(Throwable exception) {
 						DLTKDebugUIPlugin.log(exception);
 					}
 				});
 			}
 		}
 
 		IPage page = pageRecord.page;
 		page.dispose();
 		pageRecord.dispose();
 		console.removePropertyChangeListener(this);
 
 		if (console instanceof AbstractConsole)
 			((AbstractConsole) console).destroy();
 
 		// empty cross-reference cache
 		fPartToConsole.remove(part);
 		fConsoleToPart.remove(console);
 		if (fPartToConsole.isEmpty()) {
 			fActiveConsole = null;
 		}
 
 		// update console actions
 		fPinAction.update();
 	}
 
 	/**
 	 * Returns the page participants registered for the given console, or
 	 * <code>null</code>
 	 * 
 	 * @param console
 	 * @return registered page participants or <code>null</code>
 	 */
 	private ListenerList getParticipants(IConsole console) {
 		return fConsoleToPageParticipants.get(console);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.IWorkbenchPart
 	 * )
 	 */
 	protected PageRec doCreatePage(IWorkbenchPart dummyPart) {
 		ConsoleWorkbenchPart part = (ConsoleWorkbenchPart) dummyPart;
 		final IConsole console = part.getConsole();
 		final IPageBookViewPage page = console.createPage(this);
 		initPage(page);
 		page.createControl(getPageBook());
 		console.addPropertyChangeListener(this);
 
 		// initialize page participants
 		final ListenerList participants = new ListenerList();
 		IConsolePageParticipant[] consoleParticipants = getPageParticipants(console);
 		for (int i = 0; i < consoleParticipants.length; i++) {
 			participants.add(consoleParticipants[i]);
 		}
 		fConsoleToPageParticipants.put(console, participants);
 		Object[] listeners = participants.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			final IConsolePageParticipant participant = (IConsolePageParticipant) listeners[i];
 			SafeRunner.run(new ISafeRunnable() {
 				public void run() throws Exception {
 					participant.init(page, console);
 				}
 
 				public void handleException(Throwable exception) {
 					DLTKDebugUIPlugin.log(exception);
 					participants.remove(participant);
 				}
 			});
 		}
 
 		PageRec rec = new PageRec(dummyPart, page);
 		return rec;
 	}
 
 	private IConsolePageParticipant[] getPageParticipants(IConsole console) {
 		// TODO Auto-generated method stub
 		return new IConsolePageParticipant[0];
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.part.PageBookView#isImportant(org.eclipse.ui.IWorkbenchPart
 	 * )
 	 */
 	protected boolean isImportant(IWorkbenchPart part) {
 		return part instanceof ConsoleWorkbenchPart;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
 	 */
 	public void dispose() {
 		IViewSite site = getViewSite();
 		if (site != null) {
 			site.getPage().removePartListener((IPartListener2) this);
 		}
 		super.dispose();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part
 	 * .PageBook)
 	 */
 	protected IPage createDefaultPage(PageBook book) {
 		MessagePage page = new MessagePage();
 		page.createControl(getPageBook());
 		initPage(page);
 		return page;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.console.IConsoleListener#consolesAdded(org.eclipse.ui.
 	 * console.IConsole[])
 	 */
 	public void addConsole(final IEvaluateConsole console) {
 		if (isAvailable()) {
 			ConsoleWorkbenchPart part = new ConsoleWorkbenchPart(console,
 					getSite());
 			fConsoleToPart.put(console, part);
 			fPartToConsole.put(part, console);
 			partActivated(part);
 			for (Object listener : consoleListeners.getListeners()) {
 				((IEvaluateConsoleListener) listener).consoleAdded(console);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.console.IConsoleListener#consolesRemoved(org.eclipse.ui
 	 * .console.IConsole[])
 	 */
 	public void consolesRemoved(final IConsole[] consoles) {
 		if (isAvailable()) {
 			Runnable r = new Runnable() {
 				public void run() {
 					for (int i = 0; i < consoles.length; i++) {
 						if (isAvailable()) {
 							IConsole console = consoles[i];
 							fStack.remove(console);
 							ConsoleWorkbenchPart part = fConsoleToPart
 									.get(console);
 							if (part != null) {
 								partClosed(part);
 							}
 							if (getConsole() == null) {
 								IConsole[] available = {}; // FIXME
 								// getConsoleManager().getConsoles();
 								if (available.length > 0) {
 									display(available[available.length - 1]);
 								}
 							}
 						}
 					}
 				}
 			};
 			asyncExec(r);
 		}
 	}
 
 	/**
 	 * Constructs a console view
 	 */
 	public ScriptDisplayView() {
 		super();
 		fConsoleToPart = new LinkedHashMap<IConsole, ConsoleWorkbenchPart>();
 		fPartToConsole = new HashMap<ConsoleWorkbenchPart, IConsole>();
 		fConsoleToPageParticipants = new HashMap<IConsole, ListenerList>();
 
 	}
 
 	protected void createActions() {
 		fPinAction = new PinConsoleAction(this);
 		fDisplayConsoleAction = new ConsoleDropDownAction(this);
 		IEvaluateConsoleFactory[] extensions = EvaluateConsoleFactoryManager
 				.getManager().getInstances();
 		if (extensions.length > 0) {
 			fOpenConsoleAction = new OpenConsoleAction(this, extensions);
 		}
 	}
 
 	protected void configureToolBar(IToolBarManager mgr) {
 		mgr.add(new Separator(IConsoleConstants.LAUNCH_GROUP));
 		mgr.add(new Separator(IConsoleConstants.OUTPUT_GROUP));
 		mgr.add(new Separator("fixedGroup")); //$NON-NLS-1$
 		mgr.add(fPinAction);
 		mgr.add(fDisplayConsoleAction);
 		if (fOpenConsoleAction != null) {
 			mgr.add(fOpenConsoleAction);
 			if (mgr instanceof ToolBarManager) {
 				ToolBarManager tbm = (ToolBarManager) mgr;
 				final ToolBar tb = tbm.getControl();
 				tb.addMouseListener(new MouseAdapter() {
 					public void mouseDown(MouseEvent e) {
 						ToolItem ti = tb.getItem(new Point(e.x, e.y));
 						if (ti != null) {
 							if (ti.getData() instanceof ActionContributionItem) {
 								ActionContributionItem actionContributionItem = (ActionContributionItem) ti
 										.getData();
 								IAction action = actionContributionItem
 										.getAction();
 								if (action == fOpenConsoleAction) {
 									Event event = new Event();
 									event.widget = ti;
 									event.x = e.x;
 									event.y = e.y;
 									action.runWithEvent(event);
 								}
 							}
 						}
 					}
 				});
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.console.IConsoleView#display(org.eclipse.ui.console.IConsole
 	 * )
 	 */
 	public void display(IConsole console) {
 		if (fPinned && fActiveConsole != null) {
 			return;
 		}
 		if (console.equals(fActiveConsole)) {
 			return;
 		}
 		ConsoleWorkbenchPart part = fConsoleToPart.get(console);
 		if (part != null) {
 			partActivated(part);
 		}
 	}
 
 	/*
 	 * /* (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.console.IConsoleView#pin(org.eclipse.ui.console.IConsole)
 	 */
 	public void setPinned(boolean pin) {
 		fPinned = pin;
 		if (fPinAction != null) {
 			fPinAction.update();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.console.IConsoleView#isPinned()
 	 */
 	public boolean isPinned() {
 		return fPinned;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.part.PageBookView#getBootstrapPart()
 	 */
 	protected IWorkbenchPart getBootstrapPart() {
 		return null;
 	}
 
 	/**
 	 * Registers the given runnable with the display associated with this view's
 	 * control, if any.
 	 * 
 	 * @see org.eclipse.swt.widgets.Display#asyncExec(java.lang.Runnable)
 	 */
 	public void asyncExec(Runnable r) {
 		if (isAvailable()) {
 			getPageBook().getDisplay().asyncExec(r);
 		}
 	}
 
 	/**
 	 * Creates this view's underlying viewer and actions. Hooks a pop-up menu to
 	 * the underlying viewer's control, as well as a key listener. When the
 	 * delete key is pressed, the <code>REMOVE_ACTION</code> is invoked. Hooks
 	 * help to this view. Subclasses must implement the following methods which
 	 * are called in the following order when a view is created:
 	 * <ul>
 	 * <li><code>createViewer(Composite)</code> - the context menu is hooked to
 	 * the viewer's control.</li>
 	 * <li><code>createActions()</code></li>
 	 * <li><code>configureToolBar(IToolBarManager)</code></li>
 	 * <li><code>getHelpContextId()</code></li>
 	 * </ul>
 	 * 
 	 * @see IWorkbenchPart#createPartControl(Composite)
 	 */
 	public void createPartControl(Composite parent) {
 		super.createPartControl(parent);
 		createActions();
 		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
 		configureToolBar(tbm);
 		updateForExistingConsoles();
 		getViewSite().getActionBars().updateActionBars();
 		// PlatformUI.getWorkbench().getHelpSystem()
 		// .setHelp(parent, IConsoleHelpContextIds.CONSOLE_VIEW);
 		getViewSite().getPage().addPartListener((IPartListener2) this);
 		initPageSwitcher();
 		createDebugConsole();
 	}
 
 	private void createDebugConsole() {
 		addConsole(new DebugConsole(Messages.ScriptDisplayView_consoleName,
 				DebugConsole.class.getName(), new DebugScriptInterpreter(this)));
 	}
 
 	/**
 	 * Initialize the PageSwitcher.
 	 */
 	@SuppressWarnings("unused")
 	private void initPageSwitcher() {
 		new PageSwitcher(this) {
 			public void activatePage(Object page) {
 				ShowConsoleAction.showConsole((IConsole) page,
 						ScriptDisplayView.this);
 			}
 
 			public ImageDescriptor getImageDescriptor(Object page) {
 				return ((IConsole) page).getImageDescriptor();
 			}
 
 			public String getName(Object page) {
 				return ((IConsole) page).getName();
 			}
 
 			public Object[] getPages() {
 				return getConsoles();
 			}
 
 			public int getCurrentPageIndex() {
 				IConsole currentConsole = getConsole();
 				IConsole[] consoles = getConsoles();
 				for (int i = 0; i < consoles.length; i++) {
 					if (consoles[i].equals(currentConsole))
 						return i;
 				}
 				return super.getCurrentPageIndex();
 			}
 		};
 	}
 
 	public IConsole[] getConsoles() {
 		return fConsoleToPart.keySet().toArray(
 				new IConsole[fConsoleToPart.size()]);
 	}
 
 	/**
 	 * Initialize for existing consoles
 	 */
 	private void updateForExistingConsoles() {
 		// IConsoleManager manager = getConsoleManager();
 		// // create pages for consoles
 		// IConsole[] consoles = manager.getConsoles();
 		// consolesAdded(consoles);
 		// // add as a listener
 		// manager.addConsoleListener(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.console.IConsoleView#warnOfContentChange(org.eclipse.ui
 	 * .console.IConsole)
 	 */
 	public void warnOfContentChange(IConsole console) {
 		IWorkbenchPart part = fConsoleToPart.get(console);
 		if (part != null) {
 			IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) part
 					.getSite().getAdapter(IWorkbenchSiteProgressService.class);
 			if (service != null) {
 				service.warnOfContentChange();
 			}
 		}
 	}
 
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
 		Object adpater = super.getAdapter(key);
 		if (adpater == null) {
 			IConsole console = getConsole();
 			if (console != null) {
 				ListenerList listeners = getParticipants(console);
 				// an adapter can be asked for before the console participants
 				// are created
 				if (listeners != null) {
 					Object[] participants = listeners.getListeners();
 					for (int i = 0; i < participants.length; i++) {
 						IConsolePageParticipant participant = (IConsolePageParticipant) participants[i];
 						adpater = participant.getAdapter(key);
 						if (adpater != null) {
 							return adpater;
 						}
 					}
 				}
 			}
 		}
 		return adpater;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.
 	 * IWorkbenchPartReference)
 	 */
 	public void partActivated(IWorkbenchPartReference partRef) {
 		if (isThisPart(partRef)) {
 			fActive = true;
 			IContextService contextService = (IContextService) getSite()
 					.getService(IContextService.class);
 			if (contextService != null) {
 				fActivatedContext = contextService
 						.activateContext(IConsoleConstants.ID_CONSOLE_VIEW);
 				activateParticipants(fActiveConsole);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.
 	 * IWorkbenchPartReference)
 	 */
 	public void partBroughtToTop(IWorkbenchPartReference partRef) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.
 	 * IWorkbenchPartReference)
 	 */
 	public void partClosed(IWorkbenchPartReference partRef) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.
 	 * IWorkbenchPartReference)
 	 */
 	public void partDeactivated(IWorkbenchPartReference partRef) {
 		if (isThisPart(partRef)) {
 			fActive = false;
 			IContextService contextService = (IContextService) getSite()
 					.getService(IContextService.class);
 			if (contextService != null) {
 				contextService.deactivateContext(fActivatedContext);
 				deactivateParticipants(fActiveConsole);
 			}
 		}
 	}
 
 	/**
 	 * Returns if the specified part reference is to this view part (if the part
 	 * reference is the console view or not)
 	 * 
 	 * @param partRef
 	 * @return true if the specified part reference is the console view
 	 */
 	protected boolean isThisPart(IWorkbenchPartReference partRef) {
 		if (partRef instanceof IViewReference) {
 			IViewReference viewRef = (IViewReference) partRef;
 			if (getViewSite() != null
 					&& viewRef.getId().equals(getViewSite().getId())) {
 				String secId = viewRef.getSecondaryId();
 				String mySec = null;
 				if (getSite() instanceof IViewSite) {
 					mySec = ((IViewSite) getSite()).getSecondaryId();
 				}
 				if (mySec == null) {
 					return secId == null;
 				}
 				return mySec.equals(secId);
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Deactivates participants for the given console, if any.
 	 * 
 	 * @param console
 	 *            console to deactivate
 	 */
 	private void deactivateParticipants(IConsole console) {
 		// deactivate
 		if (console != null) {
 			final ListenerList listeners = getParticipants(console);
 			if (listeners != null) {
 				Object[] participants = listeners.getListeners();
 				for (int i = 0; i < participants.length; i++) {
 					final IConsolePageParticipant participant = (IConsolePageParticipant) participants[i];
 					SafeRunner.run(new ISafeRunnable() {
 						public void run() throws Exception {
 							participant.deactivated();
 						}
 
 						public void handleException(Throwable exception) {
 							DLTKDebugUIPlugin.log(exception);
 							listeners.remove(participant);
 						}
 					});
 				}
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.
 	 * IWorkbenchPartReference)
 	 */
 	public void partOpened(IWorkbenchPartReference partRef) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.
 	 * IWorkbenchPartReference)
 	 */
 	public void partHidden(IWorkbenchPartReference partRef) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.
 	 * IWorkbenchPartReference)
 	 */
 	public void partVisible(IWorkbenchPartReference partRef) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.
 	 * IWorkbenchPartReference)
 	 */
 	public void partInputChanged(IWorkbenchPartReference partRef) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.console.IConsoleView#setScrollLock(boolean)
 	 */
 	public void setScrollLock(boolean scrollLock) {
 		fScrollLock = scrollLock;
 
 		IPage page = getCurrentPage();
 		// if (page instanceof IOConsolePage) {
 		// ((IOConsolePage) page).setAutoScroll(!scrollLock);
 		// }
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.console.IConsoleView#getScrollLock()
 	 */
 	public boolean getScrollLock() {
 		return fScrollLock;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.console.IConsoleView#pin(org.eclipse.ui.console.IConsole)
 	 */
 	public void pin(IConsole console) {
 		if (console == null) {
 			setPinned(false);
 		} else {
 			if (isPinned()) {
 				setPinned(false);
 			}
 			display(console);
 			setPinned(true);
 		}
 	}
 
 	private final ListenerList consoleListeners = new ListenerList();
 
 	public void addConsoleListener(IEvaluateConsoleListener listener) {
 		consoleListeners.add(listener);
 	}
 
 	public void removeConsoleListener(IEvaluateConsoleListener listener) {
 		consoleListeners.remove(listener);
 	}
 }
