 /*******************************************************************************
  * Copyright (c) May 9, 2011 NetXForge.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  You should have received a copy of the GNU Lesser General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>
  *
  * Contributors:
  *    Christophe Bouhier - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.screens.f3;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.observable.IObservable;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.map.IObservableMap;
 import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
 import org.eclipse.core.databinding.observable.set.IObservableSet;
 import org.eclipse.emf.cdo.eresource.CDOResource;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.databinding.EMFDataBindingContext;
 import org.eclipse.emf.databinding.FeaturePath;
 import org.eclipse.emf.databinding.IEMFListProperty;
 import org.eclipse.emf.databinding.edit.EMFEditProperties;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
 import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.events.HyperlinkAdapter;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.events.IHyperlinkListener;
 import org.eclipse.ui.forms.widgets.Form;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ImageHyperlink;
 import org.eclipse.wb.swt.ResourceManager;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.data.actions.ServerRequest;
 import com.netxforge.netxstudio.generics.GenericsPackage;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.Equipment;
 import com.netxforge.netxstudio.library.Function;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.library.NodeType;
 import com.netxforge.netxstudio.operators.EquipmentRelationship;
 import com.netxforge.netxstudio.operators.FunctionRelationship;
 import com.netxforge.netxstudio.operators.Network;
 import com.netxforge.netxstudio.operators.Node;
 import com.netxforge.netxstudio.operators.Operator;
 import com.netxforge.netxstudio.operators.OperatorsFactory;
 import com.netxforge.netxstudio.operators.OperatorsPackage;
 import com.netxforge.netxstudio.operators.Warehouse;
 import com.netxforge.netxstudio.scheduling.Job;
 import com.netxforge.netxstudio.scheduling.SchedulingPackage;
 import com.netxforge.netxstudio.screens.AbstractScreen;
 import com.netxforge.netxstudio.screens.actions.ExportHTMLAction;
 import com.netxforge.netxstudio.screens.actions.ExportXLSAction;
 import com.netxforge.netxstudio.screens.dialog.OperatorFilterDialog;
 import com.netxforge.netxstudio.screens.dialog.WarehouseFilterDialog;
 import com.netxforge.netxstudio.screens.editing.IDataServiceInjection;
 import com.netxforge.netxstudio.screens.editing.ScreenUtil;
 import com.netxforge.netxstudio.screens.editing.actions.BaseSelectionListenerAction;
 import com.netxforge.netxstudio.screens.editing.actions.SeparatorAction;
 import com.netxforge.netxstudio.screens.editing.actions.WizardUtil;
 import com.netxforge.netxstudio.screens.editing.filter.TreeSearchFilter;
 import com.netxforge.netxstudio.screens.f1.support.ScheduledReportSelectionWizard;
 import com.netxforge.netxstudio.screens.f2.NodeHistory;
 import com.netxforge.netxstudio.screens.f2.details.NewEditEquipmentLinkII;
 import com.netxforge.netxstudio.screens.f2.details.NewEditFunctionLinkII;
 import com.netxforge.netxstudio.screens.f2.details.NewEditNetwork;
 import com.netxforge.netxstudio.screens.f2.details.NewEditNode;
 import com.netxforge.netxstudio.screens.f2.details.NewEditNodeEquipment;
 import com.netxforge.netxstudio.screens.f2.details.NewEditNodeFunction;
 import com.netxforge.netxstudio.screens.f2.details.NewEditNodeType;
 import com.netxforge.netxstudio.screens.f3.support.NetworkTreeLabelProvider;
 import com.netxforge.netxstudio.screens.f4.NewEditJob;
 
 /**
  * @author Christophe Bouhier christophe.bouhier@netxforge.com
  * 
  */
 public class Networks extends AbstractScreen implements IDataServiceInjection {
 
 	private static final String MEM_KEY_NETWORKS_SELECTION_TREE = "MEM_KEY_NETWORKS_SELECTION_TREE";
 
 	/*
 	 * Remember the state for Details => Function.
 	 */
 	private static final String MEM_KEY_DETAILS_FUNCTION = "MEM_KEY_DETAILS_FUNCTION";
 	private static final String MEM_KEY_DETAILS_EQUIPMENT = "MEM_KEY_DETAILS_EQUIPMENT";
 	private static final String MEM_KEY_DETAILS_NODE = "MEM_KEY_DETAILS_NODE";
 	private static final String MEM_KEY_DETAILS_NODE_TYPE = "MEM_KEY_DETAILS_NODE_TYPE";
 	private static final String MEM_KEY_DETAILS_NETWORK = "MEM_KEY_DETAILS_NETWORK";
 	private static final String MEM_KEY_DETAILS_FUNCTION_LINK = "MEM_KEY_DETAILS_FUNCTION_LINK";
 	private static final String MEM_KEY_DETAILS_EQUIPMENT_LINK = "MEM_KEY_DETAILS_EQUIPMENT_LINK";
 
 	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
 	private Text txtFilterText;
 	@SuppressWarnings("unused")
 	private DataBindingContext bindingContext;
 	private Form frmNetworks;
 	private CDOResource operatorsResource;
 	private TreeViewer networkTreeViewer;
 	private Composite cmpDetails;
 	private SashForm sashForm;
 
 	@Inject
 	ServerRequest serverActions;
 
 	/**
 	 * Create the composite.
 	 * 
 	 * @param parent
 	 * @param style
 	 */
 	public Networks(Composite parent, int style) {
 		super(parent, style);
 
 		addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				// obm.dispose();
 				toolkit.dispose();
 			}
 		});
 		toolkit.adapt(this);
 		toolkit.paintBordersFor(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.data.IDataServiceInjection#injectData()
 	 */
 	public void injectData() {
 		operatorsResource = (CDOResource) editingService
 				.getData(OperatorsPackage.Literals.OPERATOR);
 		buildUI();
 		initDataBindings_();
 	}
 
 	private void buildUI() {
 		setLayout(new FillLayout(SWT.HORIZONTAL));
 
 		// Readonlyness.
 		boolean readonly = ScreenUtil.isReadOnlyOperation(this.getOperation());
 		int widgetStyle = readonly ? SWT.READ_ONLY : SWT.NONE;
 
 		frmNetworks = toolkit.createForm(this);
 
 		frmNetworks.setSeparatorVisible(true);
 		toolkit.paintBordersFor(frmNetworks);
 
 		frmNetworks.setText(getOperationText() + "Network");
 		frmNetworks.getBody().setLayout(new FillLayout(SWT.HORIZONTAL));
 
 		frmNetworks.addMessageHyperlinkListener(new HyperlinkAdapter());
 
 		sashForm = new SashForm(frmNetworks.getBody(), SWT.VERTICAL);
 		sashForm.setOrientation(SWT.HORIZONTAL);
 		toolkit.adapt(sashForm);
 		toolkit.paintBordersFor(sashForm);
 
 		Composite composite = toolkit.createComposite(sashForm, SWT.NONE);
 		toolkit.paintBordersFor(composite);
 		composite.setLayout(new GridLayout(3, false));
 
 		Label lblFilterLabel = toolkit.createLabel(composite, "Filter:",
 				SWT.NONE);
 		lblFilterLabel.setSize(64, 81);
 
 		txtFilterText = toolkit.createText(composite, "New Text", SWT.H_SCROLL
 				| SWT.SEARCH | SWT.CANCEL);
 		GridData gd_txtFilterText = new GridData(SWT.FILL, SWT.CENTER, true,
 				false, 1, 1);
 		// gd_txtFilterText.widthHint = 200;
 		txtFilterText.setLayoutData(gd_txtFilterText);
 		txtFilterText.setSize(64, 81);
 		txtFilterText.setText("");
 
 		txtFilterText.addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent ke) {
 				ViewerFilter[] filters = networkTreeViewer.getFilters();
 				for (ViewerFilter viewerFilter : filters) {
 					if (viewerFilter instanceof TreeSearchFilter) {
 						((TreeSearchFilter) viewerFilter)
 								.setSearchText(txtFilterText.getText());
 					}
 				}
 				networkTreeViewer.refresh();
 				networkTreeViewer.expandAll();
 			}
 		});
 
 		if (!readonly) {
 
 			mghprlnkNewImagehyperlink = toolkit.createImageHyperlink(composite,
 					SWT.NONE);
 			mghprlnkNewImagehyperlink
 					.addHyperlinkListener(new IHyperlinkListener() {
 						public void linkActivated(HyperlinkEvent e) {
 							OperatorFilterDialog dialog = new OperatorFilterDialog(
 									Networks.this.getShell(), operatorsResource);
 							int result = dialog.open();
 							if (result == Window.OK) {
 								Operator operator = (Operator) dialog
 										.getFirstResult();
 								Network newNetwork = OperatorsFactory.eINSTANCE
 										.createNetwork();
 								newNetwork.setName("<new network>");
 								Command add = AddCommand.create(
 										editingService.getEditingDomain(),
 										operator, null, newNetwork);
 								editingService.getEditingDomain()
 										.getCommandStack().execute(add);
 							}
 
 						}
 
 						public void linkEntered(HyperlinkEvent e) {
 						}
 
 						public void linkExited(HyperlinkEvent e) {
 						}
 					});
 			mghprlnkNewImagehyperlink.setImage(ResourceManager.getPluginImage(
 					"com.netxforge.netxstudio.models.edit",
 					"icons/full/ctool16/Network_E.png"));
 			mghprlnkNewImagehyperlink.setLayoutData(new GridData(SWT.RIGHT,
 					SWT.CENTER, false, false, 1, 1));
 			toolkit.paintBordersFor(mghprlnkNewImagehyperlink);
 			mghprlnkNewImagehyperlink.setText("New");
 
 		}
 
 		networkTreeViewer = new TreeViewer(composite, SWT.BORDER | SWT.VIRTUAL
 				| SWT.MULTI | widgetStyle);
 		// networkTreeViewer.setUseHashlookup(true);
 		// networkTreeViewer.setComparer(new CDOElementComparer());
 		networkTreeViewer.addFilter(new TreeSearchFilter(editingService));
 
 		// CB http://work.netxforge.com/issues/290
 		networkTreeViewer.setComparator(new NetworkViewerComparator());
 
 		networkTreeViewer
 				.addSelectionChangedListener(new ISelectionChangedListener() {
 					public void selectionChanged(SelectionChangedEvent event) {
 						ISelection s = event.getSelection();
 						if (s instanceof IStructuredSelection) {
 							IStructuredSelection ss = (IStructuredSelection) s;
 							final Object o = ss.getFirstElement();
 							Networks.this.getDisplay().asyncExec(
 									new Runnable() {
 										public void run() {
 											try {
 												handleDetailsSelection(o);
 												// testRevisionHandling(o);
 											} catch (Exception e) {
 												e.printStackTrace();
 											}
 										}
 										// CB TODO Remove later.
 										// private void testRevisionHandling(
 										// Object o) {
 										// CDOObject cdoObject = (CDOObject) o;
 										//
 										// System.out.println("");
 										// System.out.println("Object= "
 										// + cdoObject);
 										// Iterator<CDORevision> cdoRevisions =
 										// modelUtils
 										// .cdoRevisions(cdoObject);
 										//
 										// while (cdoRevisions.hasNext()) {
 										// CDORevision rev = cdoRevisions
 										// .next();
 										// System.out.println("Revision= " + rev
 										// + " @ "
 										// + new Date(rev.getTimeStamp()));
 										//
 										// CDOObject object = modelUtils
 										// .cdoObject(cdoObject,
 										// rev);
 										// if (object != null
 										// && object instanceof Node) {
 										// String printNodeStructure =
 										// modelUtils
 										// .printNodeStructure((Node) object);
 										// System.out
 										// .println(printNodeStructure);
 										// object.cdoView().close();
 										// }
 										// }
 										// }
 									});
 						}
 					}
 				});
 		Tree tree = networkTreeViewer.getTree();
 		GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 2);
 		gd_tree.widthHint = 396;
 		tree.setLayoutData(gd_tree);
 		tree.setSize(74, 81);
 		toolkit.paintBordersFor(tree);
 
 		cmpDetails = toolkit.createComposite(sashForm, SWT.NONE);
 		toolkit.paintBordersFor(cmpDetails);
 		cmpDetails.setLayout(new FillLayout());
 		sashForm.setWeights(new int[] { 3, 7 });
 
 	}
 
 	private final List<IAction> actions = Lists.newArrayList();
 
 	@Override
 	public IAction[] getActions() {
 
 		// Lazy init actions.
 		if (actions.isEmpty()) {
 			boolean readonly = ScreenUtil.isReadOnlyOperation(getOperation());
 
 			actions.add(new HistoryAction("History...", SWT.PUSH));
 			actions.add(new SeparatorAction());
 			if (!readonly) {
 				actions.add(new MoveToWarehouseAction("Decommission", SWT.PUSH));
 				actions.add(new SeparatorAction());
 			}
 			// actions.add(new ScheduleReportingJobAction(
 			// "Schedule Reporting Job...", SWT.PUSH));
 			actions.add(new ScheduleReportingJobAction(
 					"Schedule Reporting Job..."));
 			actions.add(new ReportNodeAction("Report Now"));
 			actions.add(new SeparatorAction());
 			actions.add(new ExportHTMLAction("Export to HTML"));
 			actions.add(new ExportXLSAction("Export to XLS"));
 			actions.add(new SeparatorAction());
 			// actions.add(new ExpressionsAction(this.getScreenService(),
 			// "Expressions for"));
 
 		}
 		return actions.toArray(new IAction[actions.size()]);
 	}
 
 	class HistoryAction extends BaseSelectionListenerAction {
 
 		public HistoryAction(String text, int style) {
 			super(text);
 		}
 
 		@Override
 		protected boolean updateSelection(IStructuredSelection selection) {
 			// boolean superResult = super.updateSelection(selection);
 			Object firstElement = selection.getFirstElement();
 			return firstElement instanceof Node;
 		}
 
 		@Override
 		public void run() {
 			IStructuredSelection structuredSelection = this
 					.getStructuredSelection();
 			Object object = structuredSelection.getFirstElement();
 			if (object instanceof Node) {
 				NodeHistory nodeHistoryScreen = new NodeHistory(
 						screenService.getScreenContainer(), SWT.NONE);
 				nodeHistoryScreen.setScreenService(screenService);
 				nodeHistoryScreen.setOperation(ScreenUtil.OPERATION_READ_ONLY);
 				nodeHistoryScreen.injectData(null, object);
 				screenService.setActiveScreen(nodeHistoryScreen);
 			}
 		}
 	}
 
 	class ScheduleReportingJobAction extends Action {
 		public ScheduleReportingJobAction(String text) {
 			super(text);
 		}
 
 		@Override
 		public void run() {
 			if (screenService != null) {
 				ISelection selection = getViewer().getSelection();
 				if (selection instanceof IStructuredSelection) {
 					Resource jobResource = editingService
 							.getData(SchedulingPackage.Literals.JOB);
 
 					ScheduledReportSelectionWizard wizard = new ScheduledReportSelectionWizard();
 					wizard.init(PlatformUI.getWorkbench(),
 							(IStructuredSelection) selection);
 
 					WizardDialog dialog = new WizardDialog(
 							Networks.this.getShell(), wizard);
 					dialog.open();
 					Job j = wizard.getJob();
 
 					NewEditJob newEditJob = new NewEditJob(
 							screenService.getScreenContainer(), SWT.NONE);
 					newEditJob.setOperation(wizard.getOperation());
 					newEditJob.setScreenService(screenService);
 					newEditJob.injectData(jobResource, j);
 					screenService.setActiveScreen(newEditJob);
 
 				}
 			}
 		}
 	}
 
 	class ReportNodeAction extends Action {
 		public ReportNodeAction(String text) {
 			super(text);
 		}
 
 		@Override
 		public void run() {
 			ISelection selection = getViewer().getSelection();
 			if (selection instanceof IStructuredSelection) {
 
 				WizardUtil.openWizard(
 						"com.netxforge.netxstudio.screens.reporting",
 						(IStructuredSelection) selection);
 			}
 		}
 	}
 
 	/**
 	 * Action to move objects to the ware house.
 	 * 
 	 * @author Christophe Bouhier
 	 * 
 	 */
 	class MoveToWarehouseAction extends BaseSelectionListenerAction {
 
 		public MoveToWarehouseAction(String text, int style) {
 			super(text);
 		}
 
 		@Override
 		protected boolean updateSelection(IStructuredSelection selection) {
 			@SuppressWarnings("rawtypes")
 			List list = selection.toList();
 			boolean result = true;
 			for (Object o : list) {
 				if (!(o instanceof Node) && !(o instanceof Equipment)) {
 					result = false;
 				}
 			}
 			return result;
 		}
 
 		@Override
 		public void run() {
 			IStructuredSelection s = this.getStructuredSelection();
 			@SuppressWarnings("unchecked")
 			List<Object> selectionList = s.toList();
 			Collection<Node> nodesToDecommission = Lists.newArrayList();
 			Collection<Equipment> equipmentsToDecommission = Lists
 					.newArrayList();
 			for (Object o : selectionList) {
 				if (o instanceof Node) {
 					nodesToDecommission.add((Node) o);
 				}
 				if (o instanceof Equipment) {
 					equipmentsToDecommission.add((Equipment) o);
 				}
 			}
 			if (nodesToDecommission.size() > 0
 					|| equipmentsToDecommission.size() > 0) {
 				Resource warehouseResource = editingService
 						.getData(OperatorsPackage.Literals.WAREHOUSE);
 				WarehouseFilterDialog dialog = new WarehouseFilterDialog(
 						Networks.this.getShell(), warehouseResource);
 				if (dialog.open() == IDialogConstants.OK_ID) {
 					Warehouse warehouse = (Warehouse) dialog.getFirstResult();
 
 					// Loop through the node and find cross references.
 
 					CompoundCommand cc = new CompoundCommand();
 					if (nodesToDecommission.size() > 0) {
 						Command c = new AddCommand(
 								editingService.getEditingDomain(),
 								warehouse.getNodes(), nodesToDecommission);
 						cc.append(c);
 					}
 					if (equipmentsToDecommission.size() > 0) {
 						Command c = new AddCommand(
 								editingService.getEditingDomain(),
 								warehouse.getEquipments(),
 								equipmentsToDecommission);
 						cc.append(c);
 					}
 
 					editingService.getEditingDomain().getCommandStack()
 							.execute(cc);
 				}
 			}
 		}
 	}
 
 	public EMFDataBindingContext initDataBindings_() {
 
 		EMFDataBindingContext bindingContext = new EMFDataBindingContext();
 
 		ObservableListTreeContentProvider cp = new ObservableListTreeContentProvider(
 				new NetworkTreeFactoryImpl(editingService.getEditingDomain()),
 				new NetworkTreeStructureAdvisorImpl());
 
 		networkTreeViewer.setContentProvider(cp);
 
 		IObservableSet set = cp.getKnownElements();
 
 		List<IObservableMap> observableMap = Lists.newArrayList();
 
 		observableMap.add(EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				GenericsPackage.Literals.COMPANY__NAME).observeDetail(set));
 
 		// CB propably not needed, as the factory will take care.
 		// observableMap.add(EMFEditProperties.value(
 		// editingService.getEditingDomain(),
 		// OperatorsPackage.Literals.OPERATOR__NETWORKS)
 		// .observeDetail(set));
 
 		observableMap.add(EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				OperatorsPackage.Literals.NETWORK__NAME).observeDetail(set));
 
 		// observableMap.add(EMFEditProperties.value(
 		// editingService.getEditingDomain(),
 		// OperatorsPackage.Literals.NETWORK__NODES).observeDetail(set));
 
 		observableMap.add(EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				OperatorsPackage.Literals.NODE__NODE_ID).observeDetail(set));
 
 		observableMap.add(EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				OperatorsPackage.Literals.NODE__NODE_TYPE).observeDetail(set));
 
 		observableMap.add(EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				OperatorsPackage.Literals.RELATIONSHIP__NAME)
 				.observeDetail(set));
 
 		// Relationship text changes.
 		observableMap.add(EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				OperatorsPackage.Literals.RELATIONSHIP__NODE_ID1_REF)
 				.observeDetail(set));
 
 		observableMap.add(EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				OperatorsPackage.Literals.RELATIONSHIP__NODE_ID2_REF)
 				.observeDetail(set));
 
 		observableMap.add(EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				OperatorsPackage.Literals.RELATIONSHIP__NAME)
 				.observeDetail(set));
 
 		observableMap.add(EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				LibraryPackage.Literals.NODE_TYPE__NAME).observeDetail(set));
 
 		observableMap.add(EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				LibraryPackage.Literals.COMPONENT__NAME).observeDetail(set));
 
 		// Notifiy LifeCycle Features.
 
 		observableMap.add(EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				FeaturePath.fromList(
 						LibraryPackage.Literals.COMPONENT__LIFECYCLE,
 						GenericsPackage.Literals.LIFECYCLE__PROPOSED))
 				.observeDetail(set));
 
 		observableMap.add(EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				FeaturePath.fromList(
 						LibraryPackage.Literals.COMPONENT__LIFECYCLE,
 						GenericsPackage.Literals.LIFECYCLE__PLANNED_DATE))
 				.observeDetail(set));
 
 		observableMap.add(EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				FeaturePath.fromList(
 						LibraryPackage.Literals.COMPONENT__LIFECYCLE,
 						GenericsPackage.Literals.LIFECYCLE__CONSTRUCTION_DATE))
 				.observeDetail(set));
 
 		observableMap.add(EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				FeaturePath.fromList(
 						LibraryPackage.Literals.COMPONENT__LIFECYCLE,
 						GenericsPackage.Literals.LIFECYCLE__IN_SERVICE_DATE))
 				.observeDetail(set));
 
 		observableMap
 				.add(EMFEditProperties
 						.value(editingService.getEditingDomain(),
 								FeaturePath
 										.fromList(
 												LibraryPackage.Literals.COMPONENT__LIFECYCLE,
 												GenericsPackage.Literals.LIFECYCLE__OUT_OF_SERVICE_DATE))
 						.observeDetail(set));
 
 		// FIXME, doesn't work.
 		// See Bug for JFace Databinding.
 		// observableMap.add(EMFEditProperties.value(editingService.getEditingDomain(),
 		// LibraryPackage.Literals.COMPONENT__METRIC_REFS).observeDetail(set));
 		//
 		// observableMap.add(EMFEditProperties.value(editingService.getEditingDomain(),
 		// LibraryPackage.Literals.COMPONENT__RESOURCE_REFS).observeDetail(set));
 
 		IObservableMap[] map = new IObservableMap[observableMap.size()];
 		observableMap.toArray(map);
 
 		networkTreeViewer.setLabelProvider(new NetworkTreeLabelProvider(
 				modelUtils, map));
 
 		IEMFListProperty networksResourceProperty = EMFEditProperties
 				.resource(editingService.getEditingDomain());
 
 		IObservableList networksObservableList = networksResourceProperty
 				.observe(operatorsResource);
 
 		networkTreeViewer.setInput(networksObservableList);
 
 		return bindingContext;
 	}
 
 	class NetworkTreeFactoryImpl implements IObservableFactory {
 
 		EditingDomain domain;
 
 		private IEMFListProperty operatorObservableProperty = EMFEditProperties
 				.list(domain, OperatorsPackage.Literals.OPERATOR__NETWORKS);
 
 		private IEMFListProperty networkObservableProperty = EMFEditProperties
 				.multiList(
 						domain,
 						OperatorsPackage.Literals.NETWORK__NODES,
 						OperatorsPackage.Literals.NETWORK__NETWORKS,
 						OperatorsPackage.Literals.NETWORK__EQUIPMENT_RELATIONSHIPS,
 						OperatorsPackage.Literals.NETWORK__FUNCTION_RELATIONSHIPS);
 
 		// private IEMFListProperty nodeObservableProperty = EMFEditProperties
 		// .list(domain, OperatorsPackage.Literals.NODE__NODE_TYPE);
 
 		private IEMFListProperty nodeObservableProperty = EMFEditProperties
 				.multiList(domain, FeaturePath.fromList(
 						OperatorsPackage.Literals.NODE__NODE_TYPE,
 						LibraryPackage.Literals.NODE_TYPE__FUNCTIONS),
 						FeaturePath.fromList(
 								OperatorsPackage.Literals.NODE__NODE_TYPE,
 								LibraryPackage.Literals.NODE_TYPE__EQUIPMENTS));
 
 		private IEMFListProperty functionsObservableProperty = EMFEditProperties
 				.list(domain, LibraryPackage.Literals.FUNCTION__FUNCTIONS);
 
 		private IEMFListProperty equipmentsObservableProperty = EMFEditProperties
 				.list(domain, LibraryPackage.Literals.EQUIPMENT__EQUIPMENTS);
 
 		NetworkTreeFactoryImpl(EditingDomain domain) {
 			this.domain = domain;
 		}
 
 		public IObservable createObservable(final Object target) {
 
 			IObservable ol = null;
 
 			if (target instanceof IObservableList) {
 				ol = (IObservable) target;
 			} else if (target instanceof Operator) {
 				ol = operatorObservableProperty.observe(target);
 			} else if (target instanceof Network) {
 				ol = networkObservableProperty.observe(target);
 			} else if (target instanceof Node) {
 				ol = nodeObservableProperty.observe(target);
 			}
 
 			// else if (target instanceof NodeType) {
 			// ol = nodeTypeObservableProperty.observe(target);
 			// }
 
 			else if (target instanceof Function) {
 				ol = functionsObservableProperty.observe(target);
 			} else if (target instanceof Equipment) {
 				ol = equipmentsObservableProperty.observe(target);
 			}
 			return ol;
 		}
 	}
 
 	class NetworkTreeStructureAdvisorImpl extends TreeStructureAdvisor {
 
 		/**
 		 * This is important and should return the correct parent as it is used
 		 * to set the selection!
 		 */
 		@Override
 		public Object getParent(Object element) {
 
 			if (element instanceof EObject) {
 				EObject eo = (EObject) element;
 				EObject eContainer = eo.eContainer();
 				if (eContainer != null) {
 					if (element instanceof Component
 							&& eContainer instanceof NodeType) {
 						return modelUtils.nodeFor((EObject) element);
 					}
 					return eContainer;
 				}
 			}
 
 			return null;
 		}
 
 		@Override
 		public Boolean hasChildren(Object element) {
 
 			if (element instanceof Operator) {
 				return ((Operator) element).getNetworks().size() > 0 ? Boolean.TRUE
 						: null;
 			} else if (element instanceof Network) {
 				Network net = (Network) element;
 				if (net.getNetworks().size() > 0 || net.getNodes().size() > 0
 						|| net.getEquipmentRelationships().size() > 0
 						|| net.getFunctionRelationships().size() > 0) {
 					return Boolean.TRUE;
 				} else {
 					return null;
 				}
 			} else if (element instanceof Node) {
 				Node n = (Node) element;
 				if (n.getNodeType() != null) {
 					return Boolean.TRUE;
 				} else {
 					return null;
 				}
 			} else
 
 			if (element instanceof NodeType) {
 				if (((NodeType) element).getFunctions().size() > 0
 						|| ((NodeType) element).getEquipments().size() > 0) {
 					return Boolean.TRUE;
 				}
 				return Boolean.FALSE;
 			} else if (element instanceof Function) {
 				if (((Function) element).getFunctions().size() > 0) {
 					return Boolean.TRUE;
 				} else {
 					return null;
 				}
 			} else if (element instanceof Equipment) {
 				if (((Equipment) element).getEquipments().size() > 0) {
 					return Boolean.TRUE;
 				} else {
 					return null;
 				}
 			}
 			return super.hasChildren(element);
 		}
 	}
 
 	/** The composite holding the current details */
 	Composite currentDetails;
 
 	private ImageHyperlink mghprlnkNewImagehyperlink;
 
 	/*
 	 * Show details for a selection.
 	 */
 	private void handleDetailsSelection(Object o) {
 
 		// Clear the form messages.
 		this.getScreenForm().getMessageManager().removeAllMessages();

 		if (currentDetails != null && !currentDetails.isDisposed()) {
 			this.saveDetailsState(currentDetails);
 			currentDetails.dispose();
 		}
 
 		if (o instanceof Network) {
 			NewEditNetwork nef = null;
 			nef = new NewEditNetwork(this.cmpDetails, SWT.NONE, editingService);
 			nef.setOperation(getOperation());
 			nef.setScreenService(screenService);
 			nef.setParentScreen(this);
 			nef.injectData(null, o);
 			this.currentDetails = nef;
 			this.restoreDetailsState(currentDetails);
 			sashForm.layout(true, true);
 		} else if (o instanceof Node) {
 			NewEditNode node = null;
 			node = new NewEditNode(this.getScreenForm(), this.cmpDetails,
 					SWT.NONE, editingService);
 			node.setOperation(getOperation());
 			node.setScreenService(screenService);
 			node.setParentScreen(this);
 			node.injectData(null, o);
 			this.currentDetails = node;
 			this.restoreDetailsState(currentDetails);
 			sashForm.layout(true, true);
 		} else if (o instanceof Function) {
 			NewEditNodeFunction screen = new NewEditNodeFunction(
 					this.cmpDetails, SWT.NONE, editingService);
 			screen.setScreenService(screenService);
 			screen.setOperation(getOperation());
 			screen.setParentScreen(this);
 			screen.injectData(null, o);
 			this.currentDetails = screen;
 			this.restoreDetailsState(currentDetails);
 			sashForm.layout(true, true);
 		} else if (o instanceof Equipment) {
 			NewEditNodeEquipment screen = null;
 			screen = new NewEditNodeEquipment(this.cmpDetails, SWT.NONE,
 					editingService);
 			screen.setOperation(getOperation());
 			screen.setScreenService(screenService);
 			screen.setParentScreen(this);
 			screen.injectData(null, o);
 			this.currentDetails = screen;
 			this.restoreDetailsState(currentDetails);
 			sashForm.layout(true, true);
 		} else if (o instanceof NodeType) {
 			NewEditNodeType nnt = new NewEditNodeType(this.cmpDetails,
 					SWT.NONE, editingService);
 			nnt.setOperation(getOperation());
 			nnt.setScreenService(screenService);
 			nnt.setParentScreen(this);
 			nnt.injectData(null, o);
 			this.currentDetails = nnt;
 			this.restoreDetailsState(currentDetails);
 			sashForm.layout(true, true);
 		} else if (o instanceof FunctionRelationship) {
 			NewEditFunctionLinkII linkScreen = new NewEditFunctionLinkII(
 					this.cmpDetails, SWT.NONE, editingService);
 			linkScreen.setOperation(getOperation());
 			linkScreen.setScreenService(screenService);
 			linkScreen.setParentScreen(this);
 			linkScreen.injectData(null, o);
 			this.currentDetails = linkScreen;
 			this.restoreDetailsState(currentDetails);
 			sashForm.layout(true, true);
 			return;
 		} else if (o instanceof EquipmentRelationship) {
 			NewEditEquipmentLinkII linkScreen = new NewEditEquipmentLinkII(
 					this.cmpDetails, SWT.NONE, editingService);
 			linkScreen.setOperation(getOperation());
 			linkScreen.setScreenService(screenService);
 			linkScreen.setParentScreen(this);
 			linkScreen.injectData(null, o);
 			this.currentDetails = linkScreen;
 			this.restoreDetailsState(currentDetails);
 			sashForm.layout(true, true);
 			return;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.common.ui.viewer.IViewerProvider#getViewer()
 	 */
 	public Viewer getViewer() {
 		return this.networkTreeViewer;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.screens.editing.selector.IScreen#isValid()
 	 */
 	public boolean isValid() {
 		return true;
 	}
 
 	public Form getScreenForm() {
 		return this.frmNetworks;
 	}
 
 	@Override
 	public String getScreenName() {
 		return "Networks";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.screens.AbstractScreenImpl#saveState(org.eclipse
 	 * .ui.IMemento)
 	 */
 	@Override
 	public void saveState(IMemento memento) {
 		mementoUtils.rememberStructuredViewerSelection(memento,
 				networkTreeViewer, MEM_KEY_NETWORKS_SELECTION_TREE);
 		this.saveDetailsState(currentDetails);
 
 	}
 
 	public void saveDetailsState(Composite currentDetails) {
 		IMemento memento = this.getScreenService().getAbsViewPart()
 				.getMemento();
 		String key = keyForComposite(currentDetails);
 		if (key != null) {
 			mementoUtils.rememberSectionsInComposite(memento, currentDetails,
 					key);
 		}
 	}

 	public void restoreDetailsState(Composite currentDetails) {
 		IMemento memento = this.getScreenService().getAbsViewPart()
 				.getMemento();
 
 		String key = keyForComposite(currentDetails);
 		if (key != null) {
 			mementoUtils.retrieveSectionsInComposite(memento, currentDetails,
 					key);
 		}
 	}
 
 	/**
 	 * Find the IMemento Key for a Composite, to remember UI Details state.
 	 * 
 	 * @param currentDetails
 	 * @return
 	 */
 	private String keyForComposite(Composite currentDetails) {
 		String key = null;
 
 		if (currentDetails instanceof NewEditNetwork) {
 			key = MEM_KEY_DETAILS_NETWORK;
 		} else if (currentDetails instanceof NewEditNode) {
 			key = MEM_KEY_DETAILS_NODE;
 		} else if (currentDetails instanceof NewEditNodeType) {
 			key = MEM_KEY_DETAILS_NODE_TYPE;
 		} else if (currentDetails instanceof NewEditNodeFunction) {
 			key = MEM_KEY_DETAILS_FUNCTION;
 		} else if (currentDetails instanceof NewEditNodeEquipment) {
 			key = MEM_KEY_DETAILS_EQUIPMENT;
 		} else if (currentDetails instanceof NewEditEquipmentLinkII) {
 			key = MEM_KEY_DETAILS_EQUIPMENT_LINK;
 		} else if (currentDetails instanceof NewEditFunctionLinkII) {
 			key = MEM_KEY_DETAILS_FUNCTION_LINK;
 		}
 		return key;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.screens.AbstractScreenImpl#init(org.eclipse.
 	 * ui.IMemento)
 	 */
 	@Override
 	public void restoreState(IMemento memento) {
 
 		if (memento != null) {
 			mementoUtils.retrieveStructuredViewerSelection(memento,
 					networkTreeViewer, MEM_KEY_NETWORKS_SELECTION_TREE,
 					this.operatorsResource.cdoView());
 		}
 	}
 
 }
