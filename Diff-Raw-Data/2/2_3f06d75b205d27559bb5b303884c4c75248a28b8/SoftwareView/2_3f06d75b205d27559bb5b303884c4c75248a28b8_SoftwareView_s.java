 /*******************************************************************************
  * Copyright (c) 2009 EclipseSource Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the 
  * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v1.0 
  * which accompanies this distribution. The Eclipse Public License is available at 
  * http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution License 
  * is available at http://www.eclipse.org/org/documents/edl-v10.php.
  *
  * Contributors: 
  *     EclipseSource Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.examples.toast.backend.rap;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
 import org.eclipse.examples.toast.backend.data.IVehicle;
 import org.eclipse.examples.toast.backend.provisioning.IProvisioner;
 import org.eclipse.examples.toast.internal.backend.rap.bundle.Component;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.ISelectionService;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.part.ViewPart;
 
 public class SoftwareView extends ViewPart {
 
 	public static final String ID = "org.eclipse.examples.toast.backend.rap.softwareView";
 	private IVehicle vehicle = null;
 	private ListViewer viewer;
 
 	// private IProvisioner provisioner;
 	public void createPartControl(final Composite parent) {
 		GridLayout parentLayout = LayoutUtil.createGridLayout(3, false, 10, 10);
 		parent.setLayout(parentLayout);
 		viewer = new ListViewer(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
 		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		viewer.setLabelProvider(new LabelProvider() {
 			public String getText(Object element) {
 				return (((IInstallableUnit) element).getProperty(IInstallableUnit.PROP_NAME));
 			}
 		});
 
 		viewer.setContentProvider(new ArrayContentProvider());
 		Composite comp = new Composite(parent, SWT.NONE);
 		RowLayout layout = new RowLayout(SWT.VERTICAL);
 		layout.fill = true;
 		comp.setLayout(layout);
 		Button addButton = new Button(comp, SWT.PUSH);
 		addButton.setText("Add");
 		addButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				final ProvisioningDialog dialog = new ProvisioningDialog(parent.getShell());
 				Collection ius = getAvailablePackages(vehicle);
 				dialog.setInput(new ArrayList(ius));
 				dialog.open();
 				List selectedPackages = dialog.getSelectedPackages();
 				if (selectedPackages != null) {
 					IProvisioner provisioner = Component.getProvisioner();
 					IInstallableUnit iu = (IInstallableUnit) selectedPackages.get(0);
 					provisioner.install(vehicle.getName(), iu.getId(), null);
 				}
 				viewer.setInput(getInstalledPackages(vehicle));
 			}
 		});
 
 		Button removeButton = new Button(comp, SWT.PUSH);
 		removeButton.setText("Remove");
 		removeButton.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(SelectionEvent e) {
 				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
 				IProvisioner provisioner = Component.getProvisioner();
 				IInstallableUnit iu = (IInstallableUnit) selection.getFirstElement();
 				provisioner.uninstall(vehicle.getName(), iu.getId(), null);
 				viewer.setInput(getInstalledPackages(vehicle));
 			}
 		});
 		Button confButton = new Button(comp, SWT.PUSH);
 		confButton.setText("Configure");
 		createSelectionListener();
 	}
 
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
 	private Collection getAvailablePackages(IVehicle vehicle) {
 		IProvisioner provisioner = Component.getProvisioner();
 		if (provisioner == null)
 			return new ArrayList();
		return provisioner.getAvailableFeatures(vehicle.getName());
 	}
 
 	private Collection getInstalledPackages(IVehicle vehicle) {
 		IProvisioner provisioner = Component.getProvisioner();
 		if (provisioner == null)
 			return new ArrayList();
 		return provisioner.getInstalled(vehicle.getName());
 	}
 
 	private void createSelectionListener() {
 		IWorkbenchWindow window = getSite().getWorkbenchWindow();
 		ISelectionService selectionService = window.getSelectionService();
 		selectionService.addSelectionListener(new ISelectionListener() {
 
 			public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
 				if (selection != null) {
 					IStructuredSelection sselection = (IStructuredSelection) selection;
 					IVehicle vehicle = (IVehicle) sselection.getFirstElement();
 					if (vehicle != null) {
 						System.out.println("vehicle " + vehicle);
 						SoftwareView.this.vehicle = vehicle;
 						viewer.setInput(getInstalledPackages(vehicle));
 					}
 				}
 			}
 		});
 	}
 }
