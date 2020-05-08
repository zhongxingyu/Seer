 /*******************************************************************************
  * Copyright (c) Jul 11, 2011 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.screens.f2;
 
 import java.text.DecimalFormat;
 import java.util.Date;
 
 import org.eclipse.core.databinding.conversion.IConverter;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.cdo.eresource.CDOResource;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.databinding.EMFDataBindingContext;
 import org.eclipse.emf.databinding.EMFUpdateValueStrategy;
 import org.eclipse.emf.databinding.FeaturePath;
 import org.eclipse.emf.databinding.IEMFValueProperty;
 import org.eclipse.emf.databinding.edit.EMFEditProperties;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.Form;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 
 import com.netxforge.netxstudio.generics.Value;
 import com.netxforge.netxstudio.library.BaseResource;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.library.NetXResource;
 import com.netxforge.netxstudio.library.NodeType;
 import com.netxforge.netxstudio.library.Unit;
 import com.netxforge.netxstudio.metrics.Metric;
 import com.netxforge.netxstudio.metrics.MetricsPackage;
 import com.netxforge.netxstudio.operators.Node;
 import com.netxforge.netxstudio.operators.OperatorsPackage;
 import com.netxforge.netxstudio.screens.AbstractScreen;
 import com.netxforge.netxstudio.screens.dialog.ComponentFilterDialog;
 import com.netxforge.netxstudio.screens.dialog.MetricFilterDialog;
 import com.netxforge.netxstudio.screens.dialog.UnitFilterDialog;
 import com.netxforge.netxstudio.screens.editing.IDataScreenInjection;
 import com.netxforge.netxstudio.screens.editing.ScreenUtil;
 import com.netxforge.netxstudio.screens.internal.ScreensActivator;
 
 public class NewEditResource extends AbstractScreen implements
 		IDataScreenInjection {
 
 	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
 	private Text txtShortName;
 	private Text txtLongName;
 	private Text txtExpressionName;
 	private Text txtUnit;
 	private BaseResource res;
 
 	private Form frmResource;
 	private Resource owner;
 
 	private Text txtComponent;
 
 	private Text txtNode;
 
 	private Text txtMetric;
 
 	private Label lblNode;
 
 	// CB 20-02-2012 disable viewing values in this screen.
 	// @Inject
 	// private ValueComponent valueComponent;
 	// private boolean valuesVisible = false;
 
 	/**
 	 * Create the composite.
 	 * 
 	 * @param parent
 	 * @param style
 	 */
 	public NewEditResource(Composite parent, int style) {
 		super(parent, style);
 		addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				toolkit.dispose();
 			}
 		});
 		toolkit.adapt(this);
 		toolkit.paintBordersFor(this);
 		// buildUI();
 		// buildValuesUI();
 	}
 
 	private void buildUI() {
 
 		// Readonlyness.
 		boolean readonly = ScreenUtil.isReadOnlyOperation(this.getOperation());
 		int widgetStyle = readonly ? SWT.READ_ONLY : SWT.NONE;
 
 		setLayout(new FillLayout(SWT.HORIZONTAL));
 
 		frmResource = toolkit.createForm(this);
 		frmResource.setSeparatorVisible(true);
 		toolkit.paintBordersFor(frmResource);
 		frmResource.setText(this.getOperationText() + "Resource");
 		frmResource.getBody().setLayout(new GridLayout(1, false));
 
 		buildInfoSection(readonly, widgetStyle);
 
 	}
 
 	private void buildInfoSection(boolean readonly, int widgetStyle) {
 
 		Section sctnInfo = toolkit.createSection(frmResource.getBody(),
 				Section.EXPANDED | Section.TITLE_BAR);
 		sctnInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
 				1, 1));
 		toolkit.paintBordersFor(sctnInfo);
 		sctnInfo.setText("Info");
 
 		Composite composite = toolkit.createComposite(sctnInfo, SWT.NONE);
 		toolkit.paintBordersFor(composite);
 		sctnInfo.setClient(composite);
 		composite.setLayout(new GridLayout(3, false));
 
 		lblNode = toolkit.createLabel(composite, "<Dynamic>:", SWT.NONE);
 		lblNode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
 				1, 1));
 
 		txtNode = toolkit.createText(composite, "New Text", SWT.READ_ONLY);
 		txtNode.setText("");
 		GridData gd_txtNode = new GridData(SWT.LEFT, SWT.CENTER, true, false,
 				2, 1);
 		gd_txtNode.widthHint = 300;
 		txtNode.setLayoutData(gd_txtNode);
 
 		Label lblComponent = toolkit.createLabel(composite, "Component:",
 				SWT.NONE);
 		lblComponent.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1));
 		lblComponent.setAlignment(SWT.RIGHT);
 
 		txtComponent = toolkit.createText(composite, "New Text", SWT.READ_ONLY);
 		GridData gd_txtComponent = new GridData(SWT.LEFT, SWT.CENTER, false,
 				false, 1, 1);
 		gd_txtComponent.widthHint = 300;
 		txtComponent.setLayoutData(gd_txtComponent);
 		txtComponent.setText("");
 
 		Button btnSelectComponent = toolkit.createButton(composite,
 				"Select...", SWT.PUSH);
 		btnSelectComponent.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 
 				CDOResource operatorsResource = (CDOResource) editingService
 						.getData(OperatorsPackage.Literals.OPERATOR);
 
 				ComponentFilterDialog dialog = new ComponentFilterDialog(
 						NewEditResource.this.getShell(), operatorsResource,
 						modelUtils);
 				if (dialog.open() == IDialogConstants.OK_ID) {
 
 					final Component component = (Component) dialog
 							.getFirstResult();
 					final CompoundCommand cc = new CompoundCommand();
 
 					final CDOResource cdoResource = res.cdoResource();
 
					String computedPath = null;
 					try {
						computedPath = "/Node_/"
								+ modelUtils.cdoResourceName(component);
 					} catch (IllegalAccessException e1) {
 						if (ScreensActivator.DEBUG) {
 							ScreensActivator.TRACE.trace(
 									ScreensActivator.TRACE_SCREENS_OPTION,
 									"Attempt to deduce a name with invalid object: "
 											+ component, e1);
 						}
 					}
 
 					// Check to see if, we need to move the target to another
 					// CDO Resource when the calculated name is different.
					String path = cdoResource.getPath();
					if (computedPath != null && !computedPath.equals(path)) {
						
						// Make sure we use an existing transaction, 
						// if not we might get it through a CDOView which is read-only. 
						// This can happen when the resource was already loaded by the 
						// DisconnectedResourceScreen for example. (We are a sub-screen, 
						// of this). DisconnectedResourceScreen uses a SQL query handler 
						// which is read-only.
 						final Resource emfNetxResource = editingService
 								.getDataService().getProvider()
								.getResource(cdoResource.cdoView(), computedPath);
						
						
 						final Command moveResource = new AddCommand(
 								editingService.getEditingDomain(),
 								emfNetxResource.getContents(),
 								(NetXResource) res);
 						cc.append(moveResource);
 					}
 
 					final Command refBidiCommand = new AddCommand(
 							editingService.getEditingDomain(), component
 									.getResourceRefs(), (NetXResource) res);
 
 					cc.append(refBidiCommand);
 
 					editingService.getEditingDomain().getCommandStack()
 							.execute(cc);
 				}
 
 			}
 		});
 
 		Label lblShortName = toolkit.createLabel(composite, "Short Name:",
 				SWT.NONE);
 		lblShortName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1));
 
 		txtShortName = toolkit.createText(composite, "New Text", SWT.NONE
 				| widgetStyle);
 		GridData gd_txtShortName = new GridData(SWT.LEFT, SWT.CENTER, true,
 				false, 2, 1);
 		gd_txtShortName.widthHint = 300;
 		txtShortName.setLayoutData(gd_txtShortName);
 		txtShortName.setText("");
 
 		Label lblLongName = toolkit.createLabel(composite, "Long Name:",
 				SWT.NONE);
 		lblLongName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1));
 
 		txtLongName = toolkit.createText(composite, "New Text", SWT.NONE
 				| widgetStyle);
 		txtLongName.setText(" ");
 		GridData gd_txtLongName = new GridData(SWT.LEFT, SWT.CENTER, true,
 				false, 2, 1);
 		gd_txtLongName.widthHint = 300;
 		txtLongName.setLayoutData(gd_txtLongName);
 
 		Label lblNameInExpression = toolkit.createLabel(composite,
 				"Name In Expression:", SWT.NONE);
 		lblNameInExpression.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
 				false, false, 1, 1));
 
 		txtExpressionName = toolkit.createText(composite, "New Text", SWT.NONE
 				| widgetStyle);
 		GridData gd_txtExpressionName = new GridData(SWT.LEFT, SWT.CENTER,
 				false, false, 2, 1);
 		gd_txtExpressionName.widthHint = 300;
 		txtExpressionName.setLayoutData(gd_txtExpressionName);
 		txtExpressionName.setText("");
 
 		// Metric Reference, only supported when the resource is of type
 		// NetXResource
 
 		Label lblMetric = toolkit.createLabel(composite, "Metric:", SWT.NONE);
 		lblMetric.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1));
 		lblMetric.setAlignment(SWT.RIGHT);
 
 		txtMetric = toolkit.createText(composite, "", SWT.READ_ONLY);
 		GridData gd_txtMetric = new GridData(SWT.LEFT, SWT.CENTER, false,
 				false, 1, 1);
 		gd_txtMetric.widthHint = 300;
 		txtMetric.setLayoutData(gd_txtMetric);
 		txtMetric.setText("");
 
 		Button btnSelectMetric = toolkit.createButton(composite, "Select...",
 				SWT.PUSH);
 		btnSelectMetric.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				Resource metriceResource = editingService
 						.getData(MetricsPackage.Literals.METRIC);
 
 				MetricFilterDialog dialog = new MetricFilterDialog(
 						NewEditResource.this.getShell(), metriceResource);
 				if (dialog.open() == IDialogConstants.OK_ID) {
 					Object[] metricResult = dialog.getResult();
 					if (res instanceof NetXResource) {
 
 						Command setCommand = SetCommand.create(
 								editingService.getEditingDomain(),
 								res,
 								LibraryPackage.Literals.NET_XRESOURCE__METRIC_REF,
 								metricResult[0]);
 						editingService.getEditingDomain().getCommandStack()
 								.execute(setCommand);
 					}
 				}
 			}
 		});
 
 		Label lblUnit = toolkit.createLabel(composite, "Unit:", SWT.NONE);
 		lblUnit.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
 				1, 1));
 
 		txtUnit = toolkit.createText(composite, "New Text", SWT.READ_ONLY);
 		txtUnit.setText("");
 		GridData gd_txtUnit = new GridData(SWT.FILL, SWT.CENTER, false, false,
 				1, 1);
 		gd_txtUnit.widthHint = 50;
 		txtUnit.setLayoutData(gd_txtUnit);
 
 		Button btnSelectUnit = toolkit.createButton(composite, "Select...",
 				SWT.NONE);
 		btnSelectUnit.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				Resource unitResource = editingService
 						.getData(LibraryPackage.Literals.UNIT);
 				UnitFilterDialog dialog = new UnitFilterDialog(
 						NewEditResource.this.getShell(), unitResource);
 				if (dialog.open() == IDialogConstants.OK_ID) {
 					Unit u = (Unit) dialog.getFirstResult();
 
 					SetCommand cmd = new SetCommand(editingService
 							.getEditingDomain(), res,
 							LibraryPackage.Literals.BASE_RESOURCE__UNIT_REF, u);
 					editingService.getEditingDomain().getCommandStack()
 							.execute(cmd);
 				}
 			}
 		});
 
 		// This section is optional, if the resource is actually part of a
 		// component, from
 		// real node.
 
 		if (readonly) {
 			btnSelectUnit.setEnabled(false);
 			btnSelectComponent.setEnabled(false);
 		}
 	}
 
 	class NetXResourceValueLabelProvider extends LabelProvider implements
 			ITableLabelProvider {
 
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 
 		public String getColumnText(Object element, int columnIndex) {
 			if (element instanceof Value) {
 				Value v = (Value) element;
 				switch (columnIndex) {
 				case 0: {
 					Date d = modelUtils.fromXMLDate(v.getTimeStamp());
 					String ts = new String(modelUtils.date(d) + " @ "
 							+ modelUtils.time(d));
 					return ts;
 				}
 				case 1: {
 					double value = v.getValue();
 					DecimalFormat numberFormatter = new DecimalFormat(
 							"###,###,##0.00");
 					numberFormatter.setDecimalSeparatorAlwaysShown(true);
 					return numberFormatter.format(value);
 				}
 
 				}
 			}
 			return null;
 		}
 	}
 
 	class NetXResourceValueContentProvider implements
 			IStructuredContentProvider {
 
 		public void dispose() {
 		}
 
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		}
 
 		public Object[] getElements(Object inputElement) {
 			if (inputElement instanceof Object[]) {
 				return (Object[]) inputElement;
 			}
 			return null;
 		}
 
 	}
 
 	// private List<Value> values(int targetInterval) {
 	// if (res instanceof NetXResource) {
 	// NetXResource resource = (NetXResource) res;
 	//
 	// if (targetInterval == CAPACITIES) {
 	// return modelUtils.sortByTimeStampAndReverse(resource
 	// .getCapacityValues());
 	// }
 	//
 	// if (targetInterval == UTILIZATION) {
 	// return modelUtils.sortByTimeStampAndReverse(resource
 	// .getUtilizationValues());
 	// }
 	//
 	// for (MetricValueRange mvr : resource.getMetricValueRanges()) {
 	//
 	// if (mvr.getIntervalHint() == targetInterval) {
 	// return modelUtils.sortByTimeStampAndReverse(mvr
 	// .getMetricValues());
 	// }
 	// }
 	// // DEBUG.
 	// System.out.println("Target interval: " + targetInterval
 	// + " not found for resource: " + resource.getShortName());
 	//
 	// } else {
 	// throw new java.lang.IllegalArgumentException(
 	// "Expected a NetXResource");
 	// }
 	// return null;
 	// }
 
 	public EMFDataBindingContext initDataBindings_() {
 		EMFDataBindingContext context = new EMFDataBindingContext();
 
 		// Widget observables.
 
 		IObservableValue componentTargetObservable = SWTObservables
 				.observeText(this.txtComponent, SWT.Modify);
 
 		IObservableValue nodeTargetObservable = SWTObservables.observeText(
 				this.txtNode, SWT.Modify);
 
 		IObservableValue metricTargetObservable = SWTObservables.observeText(
 				this.txtMetric, SWT.Modify);
 
 		IObservableValue shortNameTargetObservable = SWTObservables
 				.observeDelayedValue(400, SWTObservables.observeText(
 						this.txtShortName, SWT.Modify));
 		IObservableValue longNameTargetObservable = SWTObservables
 				.observeDelayedValue(400, SWTObservables.observeText(
 						this.txtLongName, SWT.Modify));
 		IObservableValue expressionNameTargetObservable = SWTObservables
 				.observeDelayedValue(400, SWTObservables.observeText(
 						this.txtExpressionName, SWT.Modify));
 		IObservableValue unitTargetObservable = SWTObservables
 				.observeDelayedValue(400,
 						SWTObservables.observeText(this.txtUnit, SWT.Modify));
 
 		IEMFValueProperty componentProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				LibraryPackage.Literals.NET_XRESOURCE__COMPONENT_REF);
 
 		IEMFValueProperty metricProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				LibraryPackage.Literals.NET_XRESOURCE__METRIC_REF);
 
 		IEMFValueProperty shortNameProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				LibraryPackage.Literals.BASE_RESOURCE__SHORT_NAME);
 		IEMFValueProperty longNameProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				LibraryPackage.Literals.BASE_RESOURCE__LONG_NAME);
 
 		IEMFValueProperty expressionNameProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				LibraryPackage.Literals.BASE_RESOURCE__EXPRESSION_NAME);
 
 		IEMFValueProperty unitProperty = EMFEditProperties.value(editingService
 				.getEditingDomain(), FeaturePath.fromList(
 				LibraryPackage.Literals.BASE_RESOURCE__UNIT_REF,
 				LibraryPackage.Literals.UNIT__CODE));
 
 		EMFUpdateValueStrategy nodeToTargetStrategy = new EMFUpdateValueStrategy();
 		nodeToTargetStrategy.setConverter(new IConverter() {
 
 			public Object getFromType() {
 				return Component.class;
 			}
 
 			public Object getToType() {
 				return String.class;
 			}
 
 			public Object convert(Object fromObject) {
 				Component c = (Component) fromObject;
 				NodeType nt = modelUtils.resolveParentNodeType((EObject) c);
 				if (nt != null) {
 					Node n = null;
 					if ((n = modelUtils.nodeFor(nt)) != null) {
 						lblNode.setText("NE Instance:");
 						return n.getNodeID();
 					} else {
 						lblNode.setText("NE Type:");
 						return nt.getName();
 					}
 				} else {
 					return "";
 				}
 			}
 		});
 
 		EMFUpdateValueStrategy componentToTargetStrategy = new EMFUpdateValueStrategy();
 		componentToTargetStrategy.setConverter(new IConverter() {
 
 			public Object getFromType() {
 				return Component.class;
 			}
 
 			public Object getToType() {
 				return String.class;
 			}
 
 			public Object convert(Object fromObject) {
 				return modelUtils.componentName((Component) fromObject);
 			}
 
 		});
 
 		EMFUpdateValueStrategy metricToTargetStrategy = new EMFUpdateValueStrategy();
 		metricToTargetStrategy.setConverter(new IConverter() {
 
 			public Object getFromType() {
 				return Metric.class;
 			}
 
 			public Object getToType() {
 				return String.class;
 			}
 
 			public Object convert(Object fromObject) {
 				if (fromObject instanceof Metric) {
 					return ((Metric) fromObject).getName();
 				}
 				return fromObject;
 			}
 		});
 
 		context.bindValue(nodeTargetObservable, componentProperty.observe(res),
 				null, nodeToTargetStrategy);
 		context.bindValue(componentTargetObservable,
 				componentProperty.observe(res), null, componentToTargetStrategy);
 
 		context.bindValue(shortNameTargetObservable,
 				shortNameProperty.observe(res), null, null);
 		context.bindValue(longNameTargetObservable,
 				longNameProperty.observe(res), null, null);
 		context.bindValue(expressionNameTargetObservable,
 				expressionNameProperty.observe(res), null, null);
 
 		context.bindValue(metricTargetObservable, metricProperty.observe(res),
 				null, metricToTargetStrategy);
 
 		context.bindValue(unitTargetObservable, unitProperty.observe(res),
 				null, null);
 
 		return context;
 	}
 
 	public void disposeData() {
 		// N/A
 	}
 
 	public void injectData(Object owner, Object object) {
 		injectData(owner, null, object);
 	}
 
 	public void injectData(Object owner, Object whoRefers, Object object) {
 		if (owner instanceof Resource) {
 			this.owner = (Resource) owner;
 		}
 		if (object instanceof BaseResource) {
 			res = (BaseResource) object;
 		}
 
 		if (res instanceof NetXResource
 				&& !res.eIsSet(LibraryPackage.Literals.NET_XRESOURCE__COMPONENT_REF)
 				&& whoRefers != null) {
 			res.eSet(LibraryPackage.Literals.NET_XRESOURCE__COMPONENT_REF,
 					whoRefers);
 		}
 
 		// this.referingComponent = ((NetXResource) res).getComponentRef();
 		// } else {
 		// // Determine the ownership if not a resource.
 		// if (whoRefers != null && whoRefers instanceof Component) {
 		// this.referingComponent = (Component) whoRefers;
 		// }
 		// }
 
 		buildUI();
 		this.initDataBindings_();
 
 	}
 
 	public void addData() {
 		if (ScreenUtil.isNewOperation(getOperation()) && owner != null) {
 			// If new, we have been operating on an object not added yet.
 			CompoundCommand c = new CompoundCommand();
 
 			if (res instanceof NetXResource) {
 
 				Command addResource = new AddCommand(
 						editingService.getEditingDomain(), owner.getContents(),
 						(NetXResource) res);
 
 				c.append(addResource);
 				//
 				// Command refBidiCommand = new AddCommand(
 				// editingService.getEditingDomain(),
 				// ((Component) referingComponent).getResourceRefs(),
 				// (NetXResource) res);
 				//
 				// c.append(refBidiCommand);
 
 			}
 			editingService.getEditingDomain().getCommandStack().execute(c);
 
 		} else if (ScreenUtil.isEditOperation(getOperation())) {
 			// If edit, we have been operating on a copy of the object, so we
 			// have to replace. However if our original object is invalid, this
 			// will
 			// cause invalidity, so the action will not occure in case the
 			// original is
 			// invalid, and we should cancel the action and warn the user.
 			if (res.cdoInvalid()) {
 				MessageDialog
 						.openWarning(Display.getDefault().getActiveShell(),
 								"Conflict",
 								"There is a conflict with another user. Your changes can't be saved.");
 				return;
 			}
 			// System.out.println(res.cdoID() + "" + res.cdoState());
 
 		}
 		// After our edit, we shall be dirty
 		if (editingService.isDirty()) {
 			editingService.doSave(new NullProgressMonitor());
 		}
 
 	}
 
 	public Viewer getViewer() {
 		return null;
 		// CB disable viewing values. 20-02-2012.
 		// if (valuesVisible) {
 		// return valueComponent.getValuesTableViewer();
 		// } else {
 		// return null;
 		// }
 	}
 
 	@Override
 	public boolean isValid() {
 		return true;
 	}
 
 	public Form getScreenForm() {
 		return this.frmResource;
 	}
 
 	public String getScreenName() {
 		return "Resource";
 	}
 
 	// CB Disable viewing values. 20-02-2012
 	// private final List<IAction> actions = Lists.newArrayList();
 	// @Override
 	// public IAction[] getActions() {
 	// // Lazy init actions.
 	// if (actions.isEmpty()) {
 	// actions.add(valueComponent.new MonitorAction("Monitoring Chart...",
 	// SWT.PUSH));
 	// }
 	// return actions.toArray(new IAction[actions.size()]);
 	// }
 
 }
