 /**
  * Copyright (c) 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    bblajer - initial API and implementation
  */
 package org.eclipse.gmf.runtime.lite.parts;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.workspace.WorkspaceEditingDomainFactory;
 import org.eclipse.gef.EditDomain;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.commands.CommandStackEvent;
 import org.eclipse.gef.commands.CommandStackEventListener;
 import org.eclipse.gef.editparts.ZoomManager;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.ui.actions.ActionRegistry;
 import org.eclipse.gef.ui.actions.SelectionAction;
 import org.eclipse.gef.ui.actions.StackAction;
 import org.eclipse.gef.ui.actions.WorkbenchPartAction;
 import org.eclipse.gef.ui.views.palette.PalettePage;
 import org.eclipse.gmf.internal.runtime.lite.Activator;
 import org.eclipse.gmf.runtime.lite.services.DefaultDiagramLayouter;
 import org.eclipse.gmf.runtime.lite.services.IDiagramLayouter;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.PageBook;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 
 /**
  * This class serves as the base class for the generated diagram view parts. 
  * @author bblajer
  */
 public abstract class DiagramViewPart extends ViewPart implements IDiagramManager {
 	private DiagramDisplayer myDiagramDisplayer;
 	private PageBook myBook;
 	private Control myUninitializedControl;
 
 	private CommandStackEventListener mySaveListener = new CommandStackEventListener() {
 		public void stackChanged(CommandStackEvent event) {
 			assert event.getSource() == myDiagramDisplayer.getCommandStack();
 			if (event.isPostChangeEvent()) {
 				try {
 					myDiagramDisplayer.save(new NullProgressMonitor());
 					myDiagramDisplayer.getCommandStack().markSaveLocation();
 				} catch (CoreException e) {
 					Activator.getDefault().getLog().log(e.getStatus());
 				}
 			}
 		}
 	};
 
 	@Override
 	public void init(IViewSite site) throws PartInitException {
 		super.init(site);
 	}
 
 	@Override
 	public void dispose() {
 		if (myDiagramDisplayer != null) {
 			disposeDisplayer(myDiagramDisplayer);
 		}
 		myDiagramDisplayer = null;
 		super.dispose();
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		myBook = new PageBook(parent, SWT.NONE);
 		myUninitializedControl = createUninitializedControl(myBook);
 		myBook.showPage(myUninitializedControl);
 	}
 
 	@Override
 	public void setFocus() {
 		if (myDiagramDisplayer != null) {
 			myDiagramDisplayer.setFocus();
 			return;
 		}
 		myBook.setFocus();
 	}
 
 	/**
 	 * Returns the editing domain to be used for the new diagram if no editing domain was specified in {@link #showDiagram(TransactionalEditingDomain, URI)}. 
 	 * Subclasses may reimplement or extend.
 	 */
 	protected TransactionalEditingDomain createEditingDomain() {
 		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainFactory.INSTANCE.createEditingDomain();
 		editingDomain.getResourceSet().eAdapters().add(new AdapterFactoryEditingDomain.EditingDomainProvider(editingDomain));
 		return editingDomain;
 	}
 
 	protected abstract void createActions();
 
 	/**
 	 * Shows the given diagram in the viewer. 
 	 * @param editingDomain the editing domain that should be used for loading the diagram or <code>null</code> if the viewer should create {@link #createEditingDomain() a default instance}.
 	 * @param diagramURI the URI to load the diagram from.
 	 */
 	public final boolean showDiagram(TransactionalEditingDomain editingDomain, URI diagramURI) {
 		DiagramDisplayer oldDiagramDisplayer = myDiagramDisplayer;
 		boolean result = false;
 		if (diagramURI == null) {
 			if (oldDiagramDisplayer == null) {
 				return true;
 			}
 			myBook.showPage(myUninitializedControl);
 		} else {
 			if (editingDomain == null) {
 				editingDomain = createEditingDomain();
 			}
 			myDiagramDisplayer = new DiagramDisplayer(this, createEditDomain(), editingDomain);
 			initDisplayer(myDiagramDisplayer);
 			try {
 				setInput(diagramURI);
 				myDiagramDisplayer.createViewer(myBook);
 				createActions();
 				myBook.showPage(myDiagramDisplayer.getTopLevelControl());
 				result = true;
 			} catch (CoreException e) {
 				Activator.getDefault().getLog().log(e.getStatus());
 				ErrorDialog.openError(getSite().getShell(), "Error", "Failed to open diagram", e.getStatus());
 				if (myDiagramDisplayer.getTopLevelControl() != null && !myDiagramDisplayer.getTopLevelControl().isDisposed()) {
 					disposeDisplayer(myDiagramDisplayer);
 					myDiagramDisplayer = oldDiagramDisplayer;
 					//prevent navigation from the old diagram.
 					oldDiagramDisplayer = null;
 				}
 			}
 		}
 		if (oldDiagramDisplayer != null) {
 			disposeDisplayer(oldDiagramDisplayer);
 		}
 		updateActionBars();
 		return result;
 	}
 
 	protected void initDisplayer(DiagramDisplayer diagramDisplayer) {
 		diagramDisplayer.getCommandStack().addCommandStackEventListener(mySaveListener);
 	}
 
 	protected void disposeDisplayer(DiagramDisplayer diagramDisplayer) {
 		if (diagramDisplayer.getTopLevelControl() != null) {
 			diagramDisplayer.getTopLevelControl().dispose();
 		}
 		diagramDisplayer.getCommandStack().removeCommandStackEventListener(mySaveListener);
 		diagramDisplayer.dispose();
 	}
 
 	protected abstract void updateActionBars();
 
 	public void configureGraphicalViewer() {
 		getGraphicalViewer().getControl().setBackground(ColorConstants.listBackground);
 	}
 
 	public abstract void configurePalette(PaletteRoot paletteRoot);
 
 	/**
 	 * Sets the input for the diagram. Implementors should load the contents from the given URI and initialize the diagram.
 	 * @throws CoreException if the diagram with the specified URI cannot be loaded for any reason.
 	 */
 	protected abstract void setInput(URI diagramURI) throws CoreException;
 
 	/**
 	 * Creates edit domain that will be used for this viewer. This method is called once for each diagram being displayed in the editor.
 	 * Subclasses may extend.
 	 */
 	protected EditDomain createEditDomain() {
 		EditDomain domain = new EditDomain();
 		return domain;
 	}
 
	public final TransactionalEditingDomain getEditingDomain() {
 		if (myDiagramDisplayer == null) {
 			return null;
 		}
 		return myDiagramDisplayer.getEditingDomain();
 	}
 
 	protected final EditDomain getEditDomain() {
 		if (myDiagramDisplayer == null) {
 			return null;
 		}
 		return myDiagramDisplayer.getEditDomain();
 	}
 
 	protected final CommandStack getCommandStack() {
 		if (myDiagramDisplayer == null) {
 			return null;
 		}
 		return getEditDomain().getCommandStack();
 	}
 
 	protected final ZoomManager getZoomManager() {
 		if (myDiagramDisplayer == null) {
 			return null;
 		}
 		return myDiagramDisplayer.getZoomManager();
 	}
 
 	protected final ActionRegistry getActionRegistry() {
 		if (myDiagramDisplayer == null) {
 			return null;
 		}
 		return myDiagramDisplayer.getActionRegistry();
 	}
 
 	protected final GraphicalViewer getGraphicalViewer() {
 		if (myDiagramDisplayer == null) {
 			return null;
 		}
 		return myDiagramDisplayer.getGraphicalViewer();
 	}
 
 	protected IPropertySheetPage getPropertySheetPage() {
 		if (myDiagramDisplayer == null) {
 			return null;
 		}
 		return myDiagramDisplayer.getPropertySheetPage();
 	}
 
 	protected IDiagramLayouter getDiagramLayouter() {
 		if (myDiagramDisplayer == null) {
 			return null;
 		}
 		return new DefaultDiagramLayouter();
 	}
 
 	@Override
 	public Object getAdapter(Class type) {
 		if (type == IPropertySheetPage.class) {
 			return getPropertySheetPage();
 		} else if (type == IContentOutlinePage.class) {
 //			return getOutlinePage();
 			return null;
 		} else if (type == ZoomManager.class) {
 			return getZoomManager();
 		} else if (type == IDiagramLayouter.class) {
 			return getDiagramLayouter();
 		} else if (type == PalettePage.class) {
 			return myDiagramDisplayer.getPalettePage();
 		} else if (type == GraphicalViewer.class) {
 			return getGraphicalViewer();
 		} else if (type == CommandStack.class) {
 			return getCommandStack();
 		} else if (type == ActionRegistry.class) {
 			return getActionRegistry();
 		} else if (type == EditPart.class && getGraphicalViewer() != null) {
 			return getGraphicalViewer().getRootEditPart();
 		} else if (type == IFigure.class && getGraphicalViewer() != null) {
 			return ((GraphicalEditPart)getGraphicalViewer().getRootEditPart()).getFigure();
 		}
 		return super.getAdapter(type);
 	}
 
 	/**
 	 * Adds an action to this editor's <code>ActionRegistry</code>. 
 	 * 
 	 * @param action
 	 *            the action to add.
 	 */
 	protected void addAction(IAction action) {
 		myDiagramDisplayer.addAction(action);
 	}
 
 	/**
 	 * Adds an editor action to this editor.
 	 * 
 	 * <p>
 	 * Editor actions are actions that depend and work on the editor.
 	 * 
 	 * @param action
 	 *            the editor action
 	 */
 	protected void addEditorAction(WorkbenchPartAction action) {
 		myDiagramDisplayer.addEditorAction(action);
 	}
 
 	/**
 	 * Adds an <code>EditPart</code> action to this editor.
 	 * 
 	 * <p>
 	 * <code>EditPart</code> actions are actions that depend and work on the
 	 * selected <code>EditPart</code>s.
 	 * 
 	 * @param action
 	 *            the <code>EditPart</code> action
 	 */
 	protected void addEditPartAction(SelectionAction action) {
 		myDiagramDisplayer.addEditPartAction(action);
 	}
 
 	/**
 	 * Adds an <code>CommandStack</code> action to this editor.
 	 * 
 	 * <p>
 	 * <code>CommandStack</code> actions are actions that depend and work on
 	 * the <code>CommandStack</code>.
 	 * 
 	 * @param action
 	 *            the <code>CommandStack</code> action
 	 */
 	protected void addStackAction(StackAction action) {
 		myDiagramDisplayer.addStackAction(action);
 	}
 
 	/**
 	 * Returns the control this viewer displays when no input has been set.
 	 * Subclasses may reimplement.
 	 */
 	protected Control createUninitializedControl(Composite parent) {
 		Label result = new Label(parent, SWT.NONE);
 		result.setText("There is no diagram to display or the diagram failed to load");
 		return result;
 	}
 
 	public abstract AdapterFactory getDomainAdapterFactory();
 
 	public abstract void initializeGraphicalViewer();
 
 	public abstract boolean isFlyoutPalette();
 }
