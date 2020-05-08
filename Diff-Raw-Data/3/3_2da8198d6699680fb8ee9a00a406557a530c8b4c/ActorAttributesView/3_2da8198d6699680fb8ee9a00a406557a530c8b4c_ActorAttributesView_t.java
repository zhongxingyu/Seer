 package com.isencia.passerelle.workbench.model.editor.ui.views;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.gef.commands.CommandStackEvent;
 import org.eclipse.gef.commands.CommandStackEventListener;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.internal.help.WorkbenchHelpSystem;
 import org.eclipse.ui.part.ViewPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ptolemy.actor.Director;
 import ptolemy.data.expr.Parameter;
 import ptolemy.kernel.util.Attribute;
 import ptolemy.kernel.util.IllegalActionException;
 import ptolemy.kernel.util.NamedObj;
 import ptolemy.kernel.util.StringAttribute;
 import ptolemy.vergil.kernel.attributes.TextAttribute;
 
 import com.isencia.passerelle.actor.Actor;
 import com.isencia.passerelle.editor.common.utils.ParameterUtils;
 import com.isencia.passerelle.workbench.model.editor.ui.Activator;
 import com.isencia.passerelle.workbench.model.editor.ui.HelpUtils;
 import com.isencia.passerelle.workbench.model.editor.ui.PreferenceConstants;
 import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
 import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.DeleteAttributeHandler;
 import com.isencia.passerelle.workbench.model.editor.ui.editpart.AbstractBaseEditPart;
 import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
 import com.isencia.passerelle.workbench.model.editor.ui.properties.NamedObjComparator;
 import com.isencia.passerelle.workbench.model.ui.GeneralAttribute;
 import com.isencia.passerelle.workbench.model.ui.command.AttributeCommand;
 import com.isencia.passerelle.workbench.model.ui.command.RenameCommand;
 import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;
 import com.isencia.passerelle.workbench.model.utils.ModelUtils;
 
 /**
  * Optional replacement for PropertiesView which renders the actor properties
  * more simply but with more customizable rules.
  * 
  * @author gerring
  * 
  */
 public class ActorAttributesView extends ViewPart implements
 		ISelectionListener, CommandStackEventListener {
 
 	@Override
 	public void setSite(IWorkbenchPartSite site) {
 		// TODO Auto-generated method stub
 		super.setSite(site);
 	}
 
 	private NamedObj dialogActor;
 
 	public void setActor(NamedObj actor) {
 		this.dialogActor = actor;
 	}
 
 	private static Logger logger = LoggerFactory.getLogger(ActorAttributesView.class);
 
 	public static final String ID = "com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesView"; //$NON-NLS-1$
 
 	private TableViewer viewer;
 	private NamedObj actor;
 	private boolean addedListener = false;
 	private IWorkbenchPart part;
 
 	private VariableEditingSupport valueColumnEditor;
 
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		if (part instanceof PasserelleModelMultiPageEditor) {
 			this.part = part;
 			if (updateSelection(selection)) return;
 			clear();
 		}
 	}
 
 	protected boolean updateSelection(final ISelection selection) {
 
 		if (!(selection instanceof StructuredSelection)) return false;
 
 		final Object sel = ((StructuredSelection) selection).getFirstElement();
 		if (sel instanceof AbstractBaseEditPart) {
 			final List<Attribute> parameterList = new ArrayList<Attribute>();
 			if (this.dialogActor == null)
 				this.actor = (NamedObj) ((AbstractBaseEditPart) sel).getModel();
 			else
 				this.actor = this.dialogActor;
 
 			if (this.actor instanceof NamedObj) {
 
 				if (!addedListener && part instanceof PasserelleModelMultiPageEditor) {
 					// ((PasserelleModelMultiPageEditor)part).getEditor().getEditDomain().getCommandStack().addCommandStackEventListener(this);
 					// addedListener = true;
 				}
 				Class filter = null;
 				if (this.actor instanceof TextAttribute) {
 					filter = StringAttribute.class;
 				} else {
 					filter = Parameter.class;
 				}
 				Iterator parameterIterator = actor.attributeList(filter).iterator();
 				boolean expert = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EXPERT);
 				while (parameterIterator.hasNext()) {
 					Attribute parameter = (Attribute) parameterIterator.next();
 					
 					if (!(parameter instanceof Parameter) || (ParameterUtils.isVisible(actor,(Parameter) parameter,expert))) {
 						parameterList.add(parameter);
 					}
 				}
 
 			}
 			createTableModel(parameterList);
 			return true;
 		}
 		return false;
 	}
 
 	private void createTableModel(final List<Attribute> parameterList) {
 
 		if (parameterList != null)
 			Collections.sort(parameterList, new NamedObjComparator());
 		try {
 			viewer.setContentProvider(new IStructuredContentProvider() {
 				public void dispose() {
 
 				}
 
 				public void inputChanged(Viewer viewer, Object oldInput,
 						Object newInput) {
 				}
 
 				public Object[] getElements(Object inputElement) {
 					if (parameterList == null) return new Parameter[] {};
 					final List<Object> ret = new ArrayList<Object>(parameterList.size() + 1);
 					
 					final Director director = actor instanceof Actor
 					                        ? (Director)((Actor)actor).getDirector()
 					                        : null;
 					
 					boolean expert = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EXPERT);
 					if (actor instanceof Actor && expert)
 						ret.add(new GeneralAttribute( GeneralAttribute.ATTRIBUTE_TYPE.TYPE,PaletteBuilder.getInstance().getType(actor.getClass())));
 
 					if (actor instanceof Actor && director!=null && expert)
 						ret.add(new GeneralAttribute(GeneralAttribute.ATTRIBUTE_TYPE.CLASS, actor.getClass().getName()));
 
 					ret.add(new GeneralAttribute(GeneralAttribute.ATTRIBUTE_TYPE.NAME,PaletteBuilder.getInstance().getType(actor.getName())));
 					ret.addAll(parameterList);
 					return ret.toArray(new Object[ret.size()]);
 				}
 			});
 
 			viewer.setInput(new Object());
 			viewer.refresh();
 		} catch (Exception e) {
             logger.error("Cannot set input", e);
 		}
 	}
 
 	public void clear() {
 		this.actor = null;
 		if (part != null && part instanceof PasserelleModelMultiPageEditor) {
 			((PasserelleModelMultiPageEditor) part).getEditor().getEditDomain()
 					.getCommandStack().removeCommandStackEventListener(this);
 		}
 		this.part = null;
 		this.addedListener = false;
 		createTableModel(null);
 	}
 
 	/**
 	 * Create contents of the view part.
 	 * 
 	 * @param parent
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
 
 		this.viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL
 				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
 		
 
 		viewer.getTable().setLinesVisible(true);
 		viewer.getTable().setHeaderVisible(true);
 
 		createColumns(viewer);
 		viewer.setUseHashlookup(true);
 		viewer.setColumnProperties(new String[] { "Property", "Value" });
 
 		createActions();
 		createPopupMenu();
 		if (getSite() != null)
 			getSite().getWorkbenchWindow().getSelectionService()
 					.addSelectionListener(this);
 
 		viewer.getTable().addKeyListener(new KeyListener() {
 
 			public void keyReleased(KeyEvent e) {
 			}
 
 			public void keyPressed(KeyEvent e) {
 				if (e.keyCode == SWT.F1) {
 					try {
 						showHelpSelectedParameter();
 					} catch (IllegalActionException e1) {
 
 					}
 				}
 				if (e.character == SWT.DEL) {
 					try {
 						deleteSelectedParameter();
 					} catch (IllegalActionException e1) {
 						logger.error("Cannot delete ", e1);
 					}
 				}
 			}
 		});
		
		// Required for documentation to work
		getSite().setSelectionProvider(viewer);
 
 		try {
 			this.part = EclipseUtils.getActivePage().getActiveEditor();
 			updateSelection(EclipseUtils.getActivePage().getSelection());
 
 		} catch (Throwable ignored) {
 			// There might not be a selection or page.
 		}
 	}
 
 	private void createColumns(final TableViewer viewer) {
 
 		final TableViewerColumn name = new TableViewerColumn(viewer, SWT.LEFT, 0);
 
 		name.getColumn().setText("Property");
 		name.getColumn().setWidth(200);
 		name.setLabelProvider(new PropertyLabelProvider());
 		final TableViewerColumn value = new TableViewerColumn(viewer, SWT.LEFT, 1);
 
 		value.getColumn().setText("Value");
 		value.getColumn().setWidth(700);
 		value.setLabelProvider(new VariableLabelProvider(this));
 		this.valueColumnEditor = new VariableEditingSupport(this, viewer);
 		value.setEditingSupport(valueColumnEditor);
 	}
 	
 	public boolean canEditAttribute(final Object attribute) {
 		return valueColumnEditor.canEdit(attribute);
 	}
 
 	/**
 	 * Create the actions.
 	 */
 	private void createActions() {
 		// Create the actions
 	}
 
 	/**
 	 * Initialize the menu.
 	 */
 	private void createPopupMenu() {
 		MenuManager menuMan = new MenuManager();
 
 		menuMan.add(new Action("Delete Attribute", Activator.getImageDescriptor("icons/delete_obj.gif")) {
 			public void run() {
 				(new DeleteAttributeHandler()).run(null);
 			}
 		});
 		menuMan.add(new Separator());
 		menuMan.add(new Action("Help", Activator.getImageDescriptor("icons/help.gif")) {
 			public void run() {
 				try {
 					showHelpSelectedParameter();
 				} catch (IllegalActionException e) {
 				}
 			}
 		});
 		menuMan.add(new Action("Help Contents", Activator.getImageDescriptor("icons/help.gif")) {
 			public void run() {
 				WorkbenchHelpSystem.getInstance().displayHelp();
 			}
 		});
 
 		viewer.getControl().setMenu(menuMan.createContextMenu(viewer.getControl()));
 	}
 
 	@Override
 	public void setFocus() {
 		if (!viewer.getTable().isDisposed()) {
 			viewer.getTable().setFocus();
 		}
 	}
 
 	public void dispose() {
 		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
 		if (part != null && part instanceof PasserelleModelMultiPageEditor) {
 			((PasserelleModelMultiPageEditor) part).getEditor().getEditDomain().getCommandStack().removeCommandStackEventListener(this);
 		}
 		super.dispose();
 	}
 
 	public void stackChanged(CommandStackEvent event) {
 		viewer.refresh();
 	}
 	
 	public void refresh() {
 		viewer.refresh();
 	}
 
 	public String getActorName() {
 		return actor.getName();
 	}
 
 	public Class getActorClass() {
 		if (actor == null) {
 			return null;
 		}
 		return actor.getClass();
 	}
 
 	public void setActorName(final GeneralAttribute element, String name) {
 		if (ModelUtils.isNameLegal(name)) {
 			element.setValue(name);
 			final PasserelleModelMultiPageEditor ed = (PasserelleModelMultiPageEditor) this.part;
 			try {
 				final RenameCommand cmd = new RenameCommand(viewer,actor,element);
 				ed.getEditor().getEditDomain().getCommandStack().execute(cmd);
 				ed.refreshActions();
 			} catch (Exception ne) {
 				MessageDialog.openError(Display.getCurrent().getActiveShell(),
 						"Invalid Name", ne.getMessage());
 			}
 		} else {
 			MessageDialog.openError(Display.getCurrent().getActiveShell(),
 					"Invalid Name", "The name '" + name
 							+ "' is not allowed.\n\n"
 							+ "Names should not contain '.'");
 		}
 
 	}
 
 	public void deleteSelectedParameter() throws IllegalActionException {
 
 		final ISelection sel = viewer.getSelection();
 		if (sel != null && sel instanceof StructuredSelection) {
 			final StructuredSelection s = (StructuredSelection) sel;
 			final Object o = s.getFirstElement();
 			if (o instanceof String)
 				return; // Cannot delete name
 			if (o instanceof Attribute) {
 				setAttributeValue(o, null);
 			}
 		}
 	}
 
 	public void showHelpSelectedParameter() throws IllegalActionException {
 
 		final ISelection sel = viewer.getSelection();
 		if (sel != null && sel instanceof StructuredSelection) {
 			final StructuredSelection s = (StructuredSelection) sel;
 			final Object o = s.getFirstElement();
 			String contextId = HelpUtils.getContextId(o);
 			if (contextId != null) {
 // TODO revert this when using context specific help				
 //				WorkbenchHelp.displayHelp(contextId);
 				WorkbenchHelpSystem.getInstance().displayHelpResource(contextId);
 			}
 
 		}
 	}
 
 	public void setAttributeValue(Object element, Object value)
 			throws IllegalActionException {
 
 		final PasserelleModelMultiPageEditor ed = (PasserelleModelMultiPageEditor) this.part;
 		final AttributeCommand cmd = new AttributeCommand(viewer, element,
 				value);
 		ed.getEditor().getEditDomain().getCommandStack().execute(cmd);
 		ed.refreshActions();
     ed.getEditor().refresh();
 	}
 
 	public ColumnViewer getViewer() {
 		return this.viewer;
 	}
 }
