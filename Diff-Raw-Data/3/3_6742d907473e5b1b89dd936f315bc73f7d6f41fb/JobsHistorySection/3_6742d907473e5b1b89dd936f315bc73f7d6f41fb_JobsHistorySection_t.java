 /*******************************************************************************
  * Copyright (c) 2011 AGETO Service GmbH and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html.
  *
  * Contributors:
  *     Mike Tschierschke - initial API and implementation
  *******************************************************************************/
 package org.eclipse.gyrex.admin.ui.jobs.internal;
 
 import java.util.ArrayList;
 
 import org.eclipse.gyrex.admin.ui.internal.databinding.TrueWhenListSelectionNotEmptyConverter;
 import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutFactory;
 import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
 import org.eclipse.gyrex.context.IRuntimeContext;
 import org.eclipse.gyrex.context.internal.registry.ContextDefinition;
 import org.eclipse.gyrex.context.internal.registry.ContextRegistryImpl;
 import org.eclipse.gyrex.context.registry.IRuntimeContextRegistry;
 import org.eclipse.gyrex.jobs.history.IJobHistory;
 import org.eclipse.gyrex.jobs.history.IJobHistoryEntry;
 import org.eclipse.gyrex.jobs.manager.IJobManager;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.Section;
 
 import org.apache.commons.lang.time.DateFormatUtils;
 
 /**
  *
  */
 public class JobsHistorySection extends ViewerWithButtonsSectionPart {
 
 	private Button refrehButton;
 	private ListViewer dataList;
 	private Combo combo;
 	private final DataBindingContext bindingContext;
 	private IObservableValue selectedValue;
 
 	/**
 	 * Creates a new instance.
 	 * 
 	 * @param parent
 	 * @param page
 	 */
 	public JobsHistorySection(final Composite parent, final JobsConfigurationPage page) {
 		super(parent, page.getManagedForm().getToolkit(), ExpandableComposite.SHORT_TITLE_BAR);
 		bindingContext = page.getBindingContext();
 		final Section section = getSection();
 		section.setText("History");
 		section.setDescription("View history of recently executed jobs.");
 		createContent(section);
 	}
 
 	@Override
 	protected void createButtons(final Composite buttonsPanel) {
 		refrehButton = createButton(buttonsPanel, "Refresh", new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				refreshButtonPressed();
 			}
 		});
 	}
 
 	/**
 	 * @param contentPanel
 	 */
 	private void createCombo(final Composite contentPanel) {
 		combo = new Combo(contentPanel, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
 		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		combo.add("All registred contexts");
 		for (final ContextDefinition def : ((ContextRegistryImpl) getContextRegistry()).getDefinedContexts()) {
 			combo.add(def.getPath().toString());
 		}
 
 		combo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				setViewerInput();
 			}
 		});
 
 		combo.select(0);
 	}
 
 	@Override
 	protected void createContent(final Section section) {
 		section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
 
 		final Composite client = getToolkit().createComposite(section);
 		client.setLayout(FormLayoutFactory.createSectionClientGridLayout(false, 2));
 		client.setLayoutData(GridDataFactory.fillDefaults().create());
 
 		section.setClient(client);
 
 		createContentPanel(client);
 
 		createButtonPanel(client);
 
 		setViewerInput();
 	}
 
 	protected void createContentPanel(final Composite parent) {
 		final Composite contentPanel = getToolkit().createComposite(parent);
 		contentPanel.setLayoutData(GridDataFactory.fillDefaults().create());
 		contentPanel.setLayout(GridLayoutFactory.fillDefaults().create());
 
 		createCombo(contentPanel);
 
 		createViewer(contentPanel);
 	}
 
 	@Override
 	protected void createViewer(final Composite parent) {
 		dataList = new ListViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.V_SCROLL);
 
 		final List list = dataList.getList();
 		getToolkit().adapt(list, true, true);
 		final GridData layoutData = new GridData(GridData.FILL_BOTH);
 		layoutData.widthHint = 250;
 		layoutData.grabExcessHorizontalSpace = true;
 		list.setLayoutData(layoutData);
 
 		dataList.setContentProvider(new ArrayContentProvider());
 		dataList.setLabelProvider(new JobsLabelProvider());
 
 		selectedValue = ViewersObservables.observeSingleSelection(dataList);
 
 		dataList.addDoubleClickListener(new IDoubleClickListener() {
 
 			@Override
 			public void doubleClick(final DoubleClickEvent event) {
 				if (null != selectedValue) {
 					final JobLog log = (JobLog) selectedValue.getValue();
 					final String msg = String.format("Message: '%s'\nFinished with error: %s\nFinished with warning: %s", log.getId(), Boolean.toString(log.isError()), Boolean.toString(log.isWarning()));
 					MessageDialog.openInformation(parent.getShell(), "Running Details", msg);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Returns the bindingContext.
 	 * 
 	 * @return the bindingContext
 	 */
 	public DataBindingContext getBindingContext() {
 		return bindingContext;
 	}
 
 	private IRuntimeContextRegistry getContextRegistry() {
 		return JobsUiActivator.getInstance().getService(IRuntimeContextRegistry.class);
 	}
 
 	private java.util.List<JobLog> getJobLogs(final IRuntimeContext context) {
 		final IJobManager manager = context.get(IJobManager.class);
 
 		final java.util.List<JobLog> result = new ArrayList<JobLog>();
 		for (final String jobId : manager.getJobs()) {
 			final IJobHistory history = manager.getHistory(jobId);
 			for (final IJobHistoryEntry historyItem : history.getEntries()) {
 				final String msg = String.format("[%s] %s : %s", DateFormatUtils.format(historyItem.getTimeStamp(), "yyyy-MM-dd hh:mm:ssss"), jobId, historyItem.getResult());
				final int severity = historyItem.getResult().getSeverity();
				result.add(new JobLog(msg, severity == IStatus.ERROR, severity == IStatus.CANCEL));
 			}
 		}
 
 		return result;
 	}
 
 	JobLog getSelectedValue() {
 		return (JobLog) (null != selectedValue ? selectedValue.getValue() : null);
 	}
 
 	@Override
 	public void initialize(final IManagedForm form) {
 		super.initialize(form);
 
 		final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
 		modelToTarget.setConverter(new TrueWhenListSelectionNotEmptyConverter());
 		getBindingContext().bindValue(SWTObservables.observeEnabled(refrehButton), SWTObservables.observeSelection(dataList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
 	}
 
 	@Override
 	public void refresh() {
 		setViewerInput();
 		super.refresh();
 	}
 
 	void refreshButtonPressed() {
 		refresh();
 	}
 
 	/**
 	 *
 	 */
 	private void setViewerInput() {
 		final ArrayList<JobLog> logs = new ArrayList<JobLog>();
 
 		final IRuntimeContextRegistry contextRegistry = getContextRegistry();
 		if (combo.getSelectionIndex() == 0) {
 			for (final ContextDefinition def : ((ContextRegistryImpl) contextRegistry).getDefinedContexts()) {
 				logs.addAll(getJobLogs(contextRegistry.get(def.getPath())));
 			}
 		} else {
 			logs.addAll(getJobLogs(contextRegistry.get(new Path(combo.getText()))));
 		}
 
 		dataList.setInput(logs);
 	}
 }
