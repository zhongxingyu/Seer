 /*******************************************************************************
  * <copyright>
  *
 * Copyright (c) 2011, 2013 Modular Mind, Ltd.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    pjpaulin - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.editor;
 
 import java.util.ArrayList;
 import java.util.EventObject;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.gef.ContextMenuProvider;
 import org.eclipse.gef.DefaultEditDomain;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.commands.CommandStackListener;
 import org.eclipse.gef.ui.actions.ActionBarContributor;
 import org.eclipse.gef.ui.actions.ActionRegistry;
 import org.eclipse.gef.ui.actions.DeleteAction;
 import org.eclipse.gef.ui.actions.PrintAction;
 import org.eclipse.gef.ui.actions.RedoAction;
 import org.eclipse.gef.ui.actions.SelectAllAction;
 import org.eclipse.gef.ui.actions.UndoAction;
 import org.eclipse.gef.ui.actions.UpdateAction;
 import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
 import org.eclipse.gef.ui.parts.SelectionSynchronizer;
 import org.eclipse.gef.ui.properties.UndoablePropertySheetPage;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.ISelectionService;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.actions.ActionFactory;
 
 /**
  * Based on the original GEF GraphicalEditor class, this is a composite that
  * supports graphical editing.
  * 
  * @since 0.10
  */
 @SuppressWarnings("rawtypes")
 public abstract class GraphicalComposite extends Composite implements CommandStackListener,
         ISelectionListener {
 
     @SuppressWarnings("serial")
     private static class ActionIDList extends ArrayList {
 
         @SuppressWarnings("unchecked")
         public boolean add(Object o) {
             if (o instanceof IAction) {
                 try {
                     IAction action = (IAction) o;
                     o = action.getId();
                     throw new IllegalArgumentException(
                             "Action IDs should be added to lists, not the action: " + action); //$NON-NLS-1$
                 }
                 catch(IllegalArgumentException exc) {
                     exc.printStackTrace();
                 }
             }
             return super.add(o);
         }
     }
 
     private DefaultEditDomain editDomain;
     private GraphicalViewer graphicalViewer;
     private ActionRegistry actionRegistry;
     private SelectionSynchronizer synchronizer;
     private List selectionActions = new ActionIDList();
     private List stackActions = new ActionIDList();
     private List propertyActions = new ActionIDList();
     private IWorkbenchPart parentPart;
 
     /**
      * Constructs the editor part
      */
     public GraphicalComposite(IWorkbenchPart parentPart, Composite parent, int style) {
         super(parent, style);
         this.parentPart = parentPart;
     }
 
     /**
      * When the command stack changes, the actions interested in the command stack are updated.
      * 
      * @param event
      *            the change event
      */
     public void commandStackChanged(EventObject event) {
         updateActions(stackActions);
     }
 
     /**
      * Called to configure the graphical viewer before it receives its contents. This is where the
      * root editpart should be configured. Subclasses should extend or override this method as
      * needed.
      */
     protected void configureGraphicalViewer() {
         getGraphicalViewer().getControl().setBackground(ColorConstants.listBackground);
     }
 
     /**
      * Creates actions for this editor. Subclasses should override this method to create and
      * register actions with the {@link ActionRegistry}.
      */
     @SuppressWarnings("unchecked")
     protected void createActions() {
         ActionRegistry registry = getActionRegistry();
         IAction action;
 
         action = new UndoAction(this.parentPart);
         registry.registerAction(action);
         getStackActions().add(action.getId());
 
         action = new RedoAction(this.parentPart);
         registry.registerAction(action);
         getStackActions().add(action.getId());
 
         action = new SelectAllAction(this.parentPart);
         registry.registerAction(action);
 
         action = new DeleteAction(this.parentPart);
         registry.registerAction(action);
         getSelectionActions().add(action.getId());
 
         registry.registerAction(new PrintAction(this.parentPart));
     }
 
     /**
      * Creates the GraphicalViewer on the specified <code>Composite</code>.
      * 
      * @param parent
      *            the parent composite
      */
     protected void createGraphicalViewer() {
         GraphicalViewer viewer = new ScrollingGraphicalViewer();
         viewer.createControl(this);
         setGraphicalViewer(viewer);
         configureGraphicalViewer();
         hookGraphicalViewer();
         initializeGraphicalViewer();
     }
 
     /**
      * Realizes the Editor by creating it's Control.
      * <P>
      * WARNING: This method may or may not be called by the workbench prior to {@link #dispose()}.
      * 
      * @param parent
      *            the parent composite
      */
     public void createControl() {
         createGraphicalViewer();
     }
 
     /**
      * @see org.eclipse.ui.IWorkbenchPart#dispose()
      */
     public void dispose() {
         getCommandStack().removeCommandStackListener(this);
         this.parentPart.getSite().getWorkbenchWindow().getSelectionService()
                 .removeSelectionListener(this);
         getEditDomain().setActiveTool(null);
         getActionRegistry().dispose();
         super.dispose();
     }
 
     /**
      * Does nothing be default. This method should be overridden if {@link #isSaveAsAllowed()} has
      * been overridden to return <code>true</code>.
      * 
      * @see org.eclipse.ui.ISaveablePart#doSaveAs()
      */
     public void doSaveAs() {
         throw new RuntimeException("doSaveAs must be overridden"); //$NON-NLS-1$
     }
 
     /**
      * @see org.eclipse.ui.part.WorkbenchPart#firePropertyChange(int)
      */
     protected void firePropertyChange(int property) {
         /* TODO Need to communicate this to part somehow */
         // super.firePropertyChange(property);
         updateActions(propertyActions);
     }
 
     /**
      * Lazily creates and returns the action registry.
      * 
      * @return the action registry
      */
     protected ActionRegistry getActionRegistry() {
         if (actionRegistry == null)
             actionRegistry = new ActionRegistry();
         return actionRegistry;
     }
 
     /**
      * Returns the adapter for the specified key.
      * 
      * <P>
      * <EM>IMPORTANT</EM> certain requests, such as the property sheet, may be made before or after
      * {@link #createPartControl(Composite)} is called. The order is unspecified by the Workbench.
      * 
      * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
      */
     public Object getAdapter(Class type) {
         if (type == org.eclipse.ui.views.properties.IPropertySheetPage.class) {
             return new UndoablePropertySheetPage(getCommandStack(), getActionRegistry().getAction(
                     ActionFactory.UNDO.getId()), getActionRegistry().getAction(
                     ActionFactory.REDO.getId()));
         }
         if (type == GraphicalViewer.class)
             return getGraphicalViewer();
         if (type == CommandStack.class)
             return getCommandStack();
         if (type == ActionRegistry.class)
             return getActionRegistry();
         if (type == EditPart.class && getGraphicalViewer() != null)
             return getGraphicalViewer().getRootEditPart();
         if (type == IFigure.class && getGraphicalViewer() != null)
             return ((GraphicalEditPart) getGraphicalViewer().getRootEditPart()).getFigure();
         return null;
     }
 
     /**
      * Returns the command stack.
      * 
      * @return the command stack
      */
     protected CommandStack getCommandStack() {
         return getEditDomain().getCommandStack();
     }
 
     /**
      * Returns the edit domain.
      * 
      * @return the edit domain
      */
     public DefaultEditDomain getEditDomain() {
         return editDomain;
     }
 
     /**
      * Returns the graphical viewer.
      * 
      * @return the graphical viewer
      */
     public GraphicalViewer getGraphicalViewer() {
         return graphicalViewer;
     }
 
     /**
      * Returns the list of {@link IAction IActions} dependant on property changes in the Editor.
      * These actions should implement the {@link UpdateAction} interface so that they can be updated
      * in response to property changes. An example is the "Save" action.
      * 
      * @return the list of property-dependant actions
      */
     protected List getPropertyActions() {
         return propertyActions;
     }
 
     /**
      * Returns the list of <em>IDs</em> of Actions that are dependant on changes in the workbench's
      * {@link ISelectionService}. The associated Actions can be found in the action registry. Such
      * actions should implement the {@link UpdateAction} interface so that they can be updated in
      * response to selection changes.
      * 
      * @see #updateActions(List)
      * @return the list of selection-dependant action IDs
      */
     protected List getSelectionActions() {
         return selectionActions;
     }
 
     /**
      * Returns the selection syncronizer object. The synchronizer can be used to sync the selection
      * of 2 or more EditPartViewers.
      * 
      * @return the syncrhonizer
      */
     protected SelectionSynchronizer getSelectionSynchronizer() {
         if (synchronizer == null)
             synchronizer = new SelectionSynchronizer();
         return synchronizer;
     }
 
     /**
      * Returns the list of <em>IDs</em> of Actions that are dependant on the CommmandStack's state.
      * The associated Actions can be found in the action registry. These actions should implement
      * the {@link UpdateAction} interface so that they can be updated in response to command stack
      * changes. An example is the "undo" action.
      * 
      * @return the list of stack-dependant action IDs
      */
     protected List getStackActions() {
         return stackActions;
     }
 
     /**
      * Hooks the GraphicalViewer to the rest of the Editor. By default, the viewer is added to the
      * SelectionSynchronizer, which can be used to keep 2 or more EditPartViewers in sync. The
      * viewer is also registered as the ISelectionProvider for the Editor's PartSite.
      */
     protected void hookGraphicalViewer() {
         getSelectionSynchronizer().addViewer(getGraphicalViewer());
         this.parentPart.getSite().setSelectionProvider(getGraphicalViewer());
     }
 
     protected void init() {
         getCommandStack().addCommandStackListener(this);
         this.parentPart.getSite().getWorkbenchWindow().getSelectionService()
                 .addSelectionListener(this);
         initializeActionRegistry();
         this.createControl();
     }
 
     /**
      * Initializes the ActionRegistry. This registry may be used by {@link ActionBarContributor
      * ActionBarContributors} and/or {@link ContextMenuProvider ContextMenuProviders}.
      * <P>
      * This method may be called on Editor creation, or lazily the first time
      * {@link #getActionRegistry()} is called.
      */
     protected void initializeActionRegistry() {
         createActions();
         updateActions(propertyActions);
         updateActions(stackActions);
     }
 
     /**
      * Override to set the contents of the GraphicalViewer after it has been created.
      * 
      * @see #createGraphicalViewer(Composite)
      */
     protected void initializeGraphicalViewer() {
         /* may be overridden */
     }
 
     /**
      * Returns <code>true</code> if the command stack is dirty
      * 
      * @see org.eclipse.ui.ISaveablePart#isDirty()
      */
     public boolean isDirty() {
         return getCommandStack().isDirty();
     }
 
     /**
      * Returns <code>false</code> by default. Subclasses must return <code>true</code> to allow
      * {@link #doSaveAs()} to be called.
      * 
      * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
      */
     public boolean isSaveAsAllowed() {
         return false;
     }
 
     /**
      * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
      */
     public void selectionChanged(IWorkbenchPart part, ISelection selection) {
         // If not the active part, ignore selection changed.
         if (this.parentPart.equals(this.parentPart.getSite().getPage().getActivePart()))
             updateActions(selectionActions);
     }
 
     /**
      * Sets the ActionRegistry for this EditorPart.
      * 
      * @param registry
      *            the registry
      */
     protected void setActionRegistry(ActionRegistry registry) {
         actionRegistry = registry;
     }
 
     /**
      * Sets the EditDomain for this EditorPart.
      * 
      * @param ed
      *            the domain
      */
     protected void setEditDomain(DefaultEditDomain ed) {
         this.editDomain = ed;
     }
 
     public boolean setFocus() {
         return getGraphicalViewer().getControl().setFocus();
     }
 
     /**
      * Sets the graphicalViewer for this EditorPart.
      * 
      * @param viewer
      *            the graphical viewer
      */
     protected void setGraphicalViewer(GraphicalViewer viewer) {
         getEditDomain().addViewer(viewer);
         this.graphicalViewer = viewer;
     }
 
     /**
      * A convenience method for updating a set of actions defined by the given List of action IDs.
      * The actions are found by looking up the ID in the {@link #getActionRegistry() action
      * registry}. If the corresponding action is an {@link UpdateAction}, it will have its
      * <code>update()</code> method called.
      * 
      * @param actionIds
      *            the list of IDs to update
      */
     protected void updateActions(List actionIds) {
         ActionRegistry registry = getActionRegistry();
         Iterator iter = actionIds.iterator();
         while (iter.hasNext()) {
             IAction action = registry.getAction(iter.next());
             if (action instanceof UpdateAction)
                 ((UpdateAction) action).update();
         }
     }
 
     protected IWorkbenchPart getParentPart() {
         return this.parentPart;
     }
 }
