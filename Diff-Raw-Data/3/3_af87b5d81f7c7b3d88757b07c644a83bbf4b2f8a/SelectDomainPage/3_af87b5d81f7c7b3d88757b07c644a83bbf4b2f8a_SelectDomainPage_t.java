 /*******************************************************************************
  * This file is protected by Copyright. 
  * Please refer to the COPYRIGHT file distributed with this source distribution.
  *
  * This file is part of REDHAWK IDE.
  *
  * All rights reserved.  This program and the accompanying materials are made available under 
  * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package gov.redhawk.product.sca_explorer.wizard;
 
 import gov.redhawk.model.sca.DomainConnectionException;
 import gov.redhawk.model.sca.DomainConnectionState;
 import gov.redhawk.model.sca.RefreshDepth;
 import gov.redhawk.model.sca.ScaDomainManager;
 import gov.redhawk.model.sca.ScaDomainManagerRegistry;
 import gov.redhawk.model.sca.ScaWaveform;
 import gov.redhawk.model.sca.commands.ScaModelCommand;
 import gov.redhawk.model.sca.provider.ScaItemProviderAdapterFactory;
 import gov.redhawk.model.sca.provider.ScaWaveformsContainerItemProvider;
 import gov.redhawk.product.sca_explorer.Activator;
 import gov.redhawk.sca.ScaPlugin;
import gov.redhawk.sca.ui.parts.FormFilteredTree;
 import gov.redhawk.sca.ui.preferences.DomainEntryWizard;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.dialogs.PatternFilter;
 import org.eclipse.ui.statushandlers.StatusManager;
 
 public class SelectDomainPage extends WizardPage {
 	private FormFilteredTree domainList = null;
 	private ComposedAdapterFactory adapterFactory;
 	private final StartupWizard parent;
 	private ScaDomainManager mgr;
 	private MyPatternFilter patternFilter;
 
 	public SelectDomainPage(final String pageName, final StartupWizard parent) {
 		super(pageName);
 		setTitle(pageName);
 		setDescription("Select the Domain to connect and click 'Next'.");
 		setPageComplete(false);
 		this.parent = parent;
 	}
 
 	public void createControl(final Composite parent) {
 		final Composite container = new Composite(parent, SWT.NULL);
 		final GridLayout layout = new GridLayout(3, false);
 		container.setLayout(layout);
 
 		this.patternFilter = new MyPatternFilter();
 		this.domainList = new FormFilteredTree(container, SWT.SINGLE | SWT.V_SCROLL, this.patternFilter);
 		this.domainList.setBackground(container.getBackground());
 		this.domainList.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(2, 1).create());
 		this.domainList.getViewer().setContentProvider(new AdapterFactoryContentProvider(getAdapterFactory()));
 		this.domainList.getViewer().setLabelProvider(new AdapterFactoryLabelProvider(getAdapterFactory()) {
 			@Override
 			public String getText(final Object object) {
 				String text = super.getText(object);
 				if (object instanceof ScaDomainManager) {
 					text += " (" + ((ScaDomainManager) object).getState().getLiteral().toUpperCase() + ")";
 				}
 				return text;
 			}
 		});
 		this.domainList.getViewer().setFilters(createDomainViewerFilter());
 		this.domainList.getViewer().setInput(ScaPlugin.getDefault().getDomainManagerRegistry());
 		this.domainList.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(final SelectionChangedEvent event) {
 				boolean domainSelected = false;
 				boolean appsRunning = false;
 				final StructuredSelection selection = (StructuredSelection) event.getSelection();
 				if (!selection.isEmpty() && selection.getFirstElement() instanceof ScaDomainManager) {
 					if (connect(selection)) {
 						domainSelected = true;
 						try {
 							appsRunning = SelectDomainPage.this.mgr.applications().length > 0;
 						} catch (final Exception e) {
 							// PASS - You can connect to a domain that isn't up but is in the NameService
 						}
 					}
 				}
 				SelectDomainPage.this.setPageComplete(domainSelected);
 				SelectDomainPage.this.parent.appsRunning(appsRunning);
 			}
 		});
 
 		final Composite buttonBox = new Composite(container, SWT.NULL);
 		buttonBox.setLayoutData(GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL).grab(false, true).create());
 		final GridLayout l = new GridLayout();
 		l.marginWidth = 0;
 		buttonBox.setLayout(l);
 		final Button newButton = new Button(buttonBox, SWT.PUSH);
 		newButton.setText(" New... ");
 		newButton.setLayoutData(GridDataFactory.fillDefaults().create());
 		newButton.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(final SelectionEvent e) {
 				widgetSelected(e);
 			}
 
 			public void widgetSelected(final SelectionEvent e) {
 				final ScaDomainManagerRegistry registry = ScaPlugin.getDefault().getDomainManagerRegistry();
 				final DomainEntryWizard wizard = new DomainEntryWizard();
 				wizard.setShowExtraSettings(false);
 				wizard.setRegistry(registry);
 				wizard.setWindowTitle("Add Domain Manager");
 				final WizardDialog dialog = new WizardDialog(getShell(), wizard);
 				if (dialog.open() == IStatus.OK) {
 					final Map<String, String> connectionProperties = Collections.singletonMap(ScaDomainManager.NAMING_SERVICE_PROP,
 					        wizard.getNameServiceInitRef());
 					ScaModelCommand.execute(registry, new ScaModelCommand() {
 						public void execute() {
 							registry.createDomain(wizard.getDomainName(), false, connectionProperties);
 						}
 					});
 				}
 			}
 		});
 
 		setControl(container);
 	}
 
 	public boolean connect(final StructuredSelection selection) {
 		final IStatus[] status = new IStatus[1];
 		status[0] = null;
 		this.mgr = (ScaDomainManager) selection.getFirstElement();
 		try {
 			getContainer().run(true, true, new IRunnableWithProgress() {
 				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 					monitor.beginTask("Connecting to the domain", 2);
 					try {
 						if (SelectDomainPage.this.mgr.getState().getValue() != DomainConnectionState.CONNECTED_VALUE) {
 							SelectDomainPage.this.mgr.connect(new NullProgressMonitor(), RefreshDepth.CHILDREN);
 						}
 					} catch (final DomainConnectionException e) {
 						status[0] = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to connect to Domain: " + SelectDomainPage.this.mgr.getName());
 					}
 					monitor.worked(1);
 
 					monitor.done();
 				}
 			});
 		} catch (final InvocationTargetException e) {
 			status[0] = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to connect to Domain: " + this.mgr.getName());
 		} catch (final InterruptedException e) {
 			status[0] = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to connect to Domain: " + this.mgr.getName());
 		}
 		if (status[0] != null) {
 			setErrorMessage("Unable to connect to Domain: " + this.mgr.getName());
 			this.domainList.getViewer().setSelection(new StructuredSelection());
 			StatusManager.getManager().handle(status[0]);
 		} else {
 			setErrorMessage(null);
 			if (this.mgr.getState().getValue() == DomainConnectionState.CONNECTED_VALUE) {
 				setPageComplete(true);
 				getContainer().updateButtons();
 			}
 		}
 
 		this.domainList.getViewer().refresh();
 
 		return (status[0] == null);
 	}
 
 	/**
 	 * Gets the adapter factory.
 	 * 
 	 * @return the adapter factory
 	 */
 	private AdapterFactory getAdapterFactory() {
 		if (this.adapterFactory == null) {
 			this.adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 			this.adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
 			this.adapterFactory.addAdapterFactory(new ScaItemProviderAdapterFactory());
 			this.adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
 		}
 		return this.adapterFactory;
 	}
 
 	/**
 	 * Creates the domain viewer filter.
 	 * 
 	 * @return the viewer filter[]
 	 */
 	private ViewerFilter[] createDomainViewerFilter() {
 		return new ViewerFilter[] { new ViewerFilter() {
 			@Override
 			public Object[] filter(final Viewer viewer, final Object parent, final Object[] elements) {
 				final List<Object> elems = new ArrayList<Object>();
 				for (final Object obj : elements) {
 					ScaDomainManager dommgr = null;
 					String name = "";
 					if (obj instanceof ScaDomainManager) {
 						dommgr = (ScaDomainManager) obj;
 						name = dommgr.getName();
 					} else if (obj instanceof ScaWaveform) {
 						dommgr = (ScaDomainManager) ((ScaWaveform) obj).eContainer();
 						name = ((ScaWaveform) obj).getName();
 					} else if (obj instanceof ScaWaveformsContainerItemProvider) {
 						dommgr = (ScaDomainManager) ((ScaWaveformsContainerItemProvider) obj).getTarget();
 						name = dommgr.getName();
 					}
 					if ((dommgr != null) && SelectDomainPage.this.patternFilter.wordMatches(name)) {
 						elems.add(obj);
 					}
 				}
 
 				return elems.toArray();
 			}
 
 			@Override
 			public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
 				return element instanceof ScaDomainManager || element instanceof ScaWaveform || element instanceof ScaWaveformsContainerItemProvider;
 			}
 		} };
 	}
 
 	/**
 	 * This returns the selected Domain Manager
 	 * 
 	 * @return the Domain Manager to connect to
 	 */
 	public ScaDomainManager getDomainManager() {
 		return (ScaDomainManager) ((StructuredSelection) this.domainList.getViewer().getSelection()).getFirstElement();
 	}
 
 	private class MyPatternFilter extends PatternFilter {
 		@Override
 		public boolean wordMatches(final String text) {
 			return super.wordMatches(text);
 		}
 	}
 }
