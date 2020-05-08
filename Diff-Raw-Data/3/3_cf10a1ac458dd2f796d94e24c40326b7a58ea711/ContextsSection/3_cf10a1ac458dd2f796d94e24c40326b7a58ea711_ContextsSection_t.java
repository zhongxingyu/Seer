 /**
  * Copyright (c) 2011 Gunnar Wagenknecht and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Gunnar Wagenknecht - initial API and implementation
  */
 package org.eclipse.gyrex.admin.ui.context.internal;
 
 import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;
 import org.eclipse.gyrex.admin.ui.internal.databinding.TrueWhenListSelectionNotEmptyConverter;
 import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
 import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
 import org.eclipse.gyrex.context.internal.ContextActivator;
 import org.eclipse.gyrex.context.internal.registry.ContextDefinition;
 import org.eclipse.gyrex.context.internal.registry.ContextRegistryImpl;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.Section;
 
 /**
  *
  */
 public class ContextsSection extends ViewerWithButtonsSectionPart {
 
 	private final ConfigurationPage page;
 
 	private Button addButton;
 	private Button removeButton;
 	private ListViewer contextsList;
 	protected IViewerObservableValue selectedValue;
 
 	/**
 	 * Creates a new instance.
 	 * 
 	 * @param parent
 	 * @param page
 	 */
 	public ContextsSection(final Composite parent, final ConfigurationPage page) {
 		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
 		this.page = page;
 		final Section section = getSection();
 		section.setText("Available Contexts");
 		section.setDescription("Define the available contexts.");
 		createContent(section);
 	}
 
 	void addButtonPressed() {
 		final AddContextDialog dialog = new AddContextDialog(SwtUtil.getShell(addButton), getContextRegistry());
 		if (dialog.open() == Window.OK) {
 			markStale();
 		}
 	}
 
 	@Override
 	protected void createButtons(final Composite buttonsPanel) {
 		addButton = createButton(buttonsPanel, "Add...", new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				addButtonPressed();
 			}
 		});
 		removeButton = createButton(buttonsPanel, "Remove...", new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				removeButtonPressed();
 			}
 		});
 	}
 
 	@Override
 	protected void createViewer(final Composite parent) {
 		contextsList = new ListViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
 
 		final List list = contextsList.getList();
 		getToolkit().adapt(list, true, true);
 		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
 
 		contextsList.setContentProvider(new ArrayContentProvider());
 		contextsList.setLabelProvider(new ContextUiLabelProvider());
 
		selectedValue = ViewersObservables.observeSingleSelection(contextsList);
 	}
 
 	private DataBindingContext getBindingContext() {
 		return page.getBindingContext();
 	}
 
 	private ContextRegistryImpl getContextRegistry() {
 		return ContextActivator.getInstance().getContextRegistryImpl();
 	}
 
 	private ContextDefinition getSelectedContext() {
 		return (ContextDefinition) (null != selectedValue ? selectedValue.getValue() : null);
 	}
 
 	@Override
 	public void initialize(final IManagedForm form) {
 		super.initialize(form);
 
 		final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
 		modelToTarget.setConverter(new TrueWhenListSelectionNotEmptyConverter());
 		getBindingContext().bindValue(SWTObservables.observeEnabled(removeButton), SWTObservables.observeSelection(contextsList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
 	}
 
 	@Override
 	public void refresh() {
 		contextsList.setInput(getContextRegistry().getDefinedContexts());
 		super.refresh();
 	}
 
 	void removeButtonPressed() {
 		final ContextDefinition contextDefinition = getSelectedContext();
 		if (contextDefinition == null) {
 			return;
 		}
 
 		if (!MessageDialog.openQuestion(SwtUtil.getShell(getSection()), "Remove Context", "Do you really want to delete the context?")) {
 			return;
 		}
 
 		getContextRegistry().removeDefinition(contextDefinition);
 		markStale();
 	}
 
 }
