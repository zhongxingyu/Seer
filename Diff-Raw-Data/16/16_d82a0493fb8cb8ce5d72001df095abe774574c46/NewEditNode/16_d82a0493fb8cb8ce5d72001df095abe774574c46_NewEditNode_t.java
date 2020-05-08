 /*******************************************************************************
  * Copyright (c) 15 dec. 2012 NetXForge.
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
 package com.netxforge.netxstudio.screens.f2.details;
 
 import java.util.Date;
 import java.util.Iterator;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.eclipse.core.databinding.conversion.IConverter;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.cdo.transaction.CDOTransaction;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.databinding.EMFDataBindingContext;
 import org.eclipse.emf.databinding.EMFUpdateValueStrategy;
 import org.eclipse.emf.databinding.FeaturePath;
 import org.eclipse.emf.databinding.IEMFValueProperty;
 import org.eclipse.emf.databinding.edit.EMFEditProperties;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.nebula.widgets.datechooser.DateChooserCombo;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.events.HyperlinkAdapter;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.events.IHyperlinkListener;
 import org.eclipse.ui.forms.widgets.Form;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ImageHyperlink;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.wb.swt.ResourceManager;
 
 import com.netxforge.netxstudio.generics.GenericsFactory;
 import com.netxforge.netxstudio.generics.GenericsPackage;
 import com.netxforge.netxstudio.generics.Lifecycle;
 import com.netxforge.netxstudio.geo.GeoPackage;
 import com.netxforge.netxstudio.geo.Location;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.library.NodeType;
 import com.netxforge.netxstudio.operators.Node;
 import com.netxforge.netxstudio.operators.OperatorsPackage;
 import com.netxforge.netxstudio.screens.AbstractDetailsScreen;
 import com.netxforge.netxstudio.screens.dialog.LocationFilterDialog;
 import com.netxforge.netxstudio.screens.dialog.NodeTypeFilterDialog;
 import com.netxforge.netxstudio.screens.editing.IEditingService;
 import com.netxforge.netxstudio.screens.editing.actions.WarningDeleteCommand;
 import com.netxforge.netxstudio.screens.editing.selector.IDataScreenInjection;
 import com.netxforge.netxstudio.screens.editing.selector.ScreenUtil;
 import com.netxforge.netxstudio.screens.editing.util.DateChooserComboObservableValue;
 import com.netxforge.netxstudio.screens.editing.util.ValidationService;
 import com.netxforge.netxstudio.screens.internal.ScreensActivator;
 
 /**
  * 
  * @author Christophe Bouhier
  * 
  */
 public class NewEditNode extends AbstractDetailsScreen implements
 		IDataScreenInjection {
 
 	private Node node;
 	private FormToolkit toolkit = new FormToolkit(Display.getCurrent());
 	private Text txtName;
 	private IEditingService editingService;
 	private Text txtNodeType;
 	private Text txtRoom;
 
 	private DateChooserCombo dcProposed;
 	private DateChooserCombo dcPlanned;
 	private DateChooserCombo dcConstruction;
 	private DateChooserCombo dcInService;
 	private DateChooserCombo dcOutOfService;
 	private ImageHyperlink roomRefHyperlink;
 
 	private ImageHyperlink nodeTypeHyperlink;
 	private Form parentForm;
 	private boolean readonly;
 	private int widgetStyle;
 
 	public NewEditNode(Form form, Composite cmpDetails, int style,
 			final IEditingService editingService) {
 		super(cmpDetails, style);
 
 		this.parentForm = form;
 		this.editingService = editingService;
 		addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				validationService.dispose();
 				validationService.removeValidationListener(NewEditNode.this);
 				toolkit.dispose();
 
 			}
 		});
 		toolkit.adapt(this);
 		toolkit.paintBordersFor(this);
 	}
 
 	public void injectData(Object owner, Object object) {
 		if (object instanceof Node) {
 			this.node = (Node) object;
 		} else {
 			return;
 		}
 		this.buildUI();
 
 		@SuppressWarnings("unused")
 		EMFDataBindingContext m_bindingContext = this.initDataBindings_();
 
 		// TODO, disable for now.
 		// if (!Screens.isReadOnlyOperation(getOperation())) {
 		// validationService.registerBindingContext(m_bindingContext);
 		// validationService.addValidationListener(this);
 		// }
 
 	}
 
 	public boolean isValid() {
 		return false;
 	}
 
 	private void buildUI() {
 
 		// Readonlyness.
 		readonly = ScreenUtil.isReadOnlyOperation(this.getOperation());
 		widgetStyle = readonly ? SWT.READ_ONLY : SWT.NONE;
 		buildInfoSection();
 		buildGeoSection();
 		buildLifeCycleSection();
 	}
 
 	private void buildLifeCycleSection() {
 		Section sctnLifecycle = toolkit.createSection(this, Section.TITLE_BAR
 				| Section.TWISTIE);
 		toolkit.paintBordersFor(sctnLifecycle);
 		sctnLifecycle.setText("Lifecycle");
 
 		Composite composite_1 = toolkit
 				.createComposite(sctnLifecycle, SWT.NONE);
 		toolkit.paintBordersFor(composite_1);
 		sctnLifecycle.setClient(composite_1);
 		composite_1.setLayout(new GridLayout(2, false));
 
 		Label lblProposed = toolkit.createLabel(composite_1, "Proposed:",
 				SWT.NONE);
 		lblProposed.setAlignment(SWT.RIGHT);
 		GridData gd_lblProposed = new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1);
 		gd_lblProposed.widthHint = 70;
 		lblProposed.setLayoutData(gd_lblProposed);
 
 		dcProposed = new DateChooserCombo(composite_1, SWT.BORDER | SWT.FLAT);
 		GridData gd_dcProposed = new GridData(SWT.LEFT, SWT.CENTER, false,
 				false, 1, 1);
 		gd_dcProposed.heightHint = 20;
 		dcProposed.setLayoutData(gd_dcProposed);
 		toolkit.adapt(dcProposed);
 		toolkit.paintBordersFor(dcProposed);
 
 		Label lblPlanned = toolkit.createLabel(composite_1, "Planned:",
 				SWT.NONE);
 		lblPlanned.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1));
 		lblPlanned.setAlignment(SWT.RIGHT);
 
 		dcPlanned = new DateChooserCombo(composite_1, SWT.BORDER | SWT.FLAT);
 		GridData gd_dcPlanned = new GridData(SWT.LEFT, SWT.CENTER, false,
 				false, 1, 1);
 		gd_dcPlanned.heightHint = 20;
 		dcPlanned.setLayoutData(gd_dcPlanned);
 		toolkit.adapt(dcPlanned);
 		toolkit.paintBordersFor(dcPlanned);
 
 		Label lblConstruction = toolkit.createLabel(composite_1,
 				"Construction:", SWT.NONE);
 		lblConstruction.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
 				false, false, 1, 1));
 
 		dcConstruction = new DateChooserCombo(composite_1, SWT.BORDER
 				| SWT.FLAT);
 		GridData gd_dcConstruction = new GridData(SWT.LEFT, SWT.CENTER, false,
 				false, 1, 1);
 		gd_dcConstruction.heightHint = 20;
 		dcConstruction.setLayoutData(gd_dcConstruction);
 		toolkit.adapt(dcConstruction);
 		toolkit.paintBordersFor(dcConstruction);
 
 		Label lblInService = toolkit.createLabel(composite_1, "In Service:",
 				SWT.NONE);
 		lblInService.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1));
 		lblInService.setAlignment(SWT.RIGHT);
 
 		dcInService = new DateChooserCombo(composite_1, SWT.BORDER | SWT.FLAT);
 		GridData gd_dcInService = new GridData(SWT.LEFT, SWT.CENTER, false,
 				false, 1, 1);
 		gd_dcInService.heightHint = 20;
 		dcInService.setLayoutData(gd_dcInService);
 		toolkit.adapt(dcInService);
 		toolkit.paintBordersFor(dcInService);
 
 		Label lblOutOfService = toolkit.createLabel(composite_1,
 				"Out of Service:", SWT.NONE);
 		GridData gd_lblOutOfService = new GridData(SWT.RIGHT, SWT.CENTER,
 				false, false, 1, 1);
 		gd_lblOutOfService.widthHint = 80;
 		lblOutOfService.setLayoutData(gd_lblOutOfService);
 
 		dcOutOfService = new DateChooserCombo(composite_1, SWT.BORDER
 				| SWT.FLAT);
 		GridData gd_dcOutOfService = new GridData(SWT.LEFT, SWT.CENTER, false,
 				false, 1, 1);
 		gd_dcOutOfService.heightHint = 20;
 		dcOutOfService.setLayoutData(gd_dcOutOfService);
 		dcOutOfService.setWeeksVisible(true);
 		toolkit.adapt(dcOutOfService);
 		toolkit.paintBordersFor(dcOutOfService);
 
 		if (readonly) {
 			dcProposed.setEditable(false);
 			dcPlanned.setEditable(false);
 			dcConstruction.setEditable(false);
 			dcInService.setEditable(false);
 			dcOutOfService.setEditable(false);
 		}
 	}
 
 	private void buildGeoSection() {
 		Section sctnGeo = toolkit.createSection(this, Section.TITLE_BAR
 				| Section.TWISTIE);
 
 		toolkit.paintBordersFor(sctnGeo);
 		sctnGeo.setText("Geo");
 
 		Composite cmpTolerances = toolkit.createComposite(sctnGeo, SWT.NONE);
 		toolkit.paintBordersFor(cmpTolerances);
 		sctnGeo.setClient(cmpTolerances);
 		cmpTolerances.setLayout(new GridLayout(5, false));
 
 		Label lblRoomsite = toolkit.createLabel(cmpTolerances, "Room/Site:",
 				SWT.NONE);
 		lblRoomsite.setAlignment(SWT.RIGHT);
 		GridData gd_lblRoomsite = new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1);
 		gd_lblRoomsite.widthHint = 80;
 		lblRoomsite.setLayoutData(gd_lblRoomsite);
 
 		txtRoom = toolkit.createText(cmpTolerances, "New Text", SWT.NONE
 				| SWT.READ_ONLY);
 		txtRoom.setText("");
 		GridData gd_txtRoom = new GridData(SWT.FILL, SWT.CENTER, true, false,
 				1, 1);
 		// gd_txtRoom.widthHint = 150;
 		txtRoom.setLayoutData(gd_txtRoom);
 
 		if (!readonly) {
 			roomRefHyperlink = toolkit.createImageHyperlink(cmpTolerances,
 					SWT.NONE);
 			GridData gd_imageHyperlink = new GridData(SWT.LEFT, SWT.CENTER,
 					false, false, 1, 1);
 			gd_imageHyperlink.widthHint = 18;
 			roomRefHyperlink.setLayoutData(gd_imageHyperlink);
 			roomRefHyperlink.addHyperlinkListener(new IHyperlinkListener() {
 				public void linkActivated(HyperlinkEvent e) {
 					Command set = new SetCommand(editingService
 							.getEditingDomain(), node,
 							OperatorsPackage.Literals.NODE__LOCATION_REF, null);
 					editingService.getEditingDomain().getCommandStack()
 							.execute(set);
 				}
 
 				public void linkEntered(HyperlinkEvent e) {
 				}
 
 				public void linkExited(HyperlinkEvent e) {
 				}
 			});
 
 			roomRefHyperlink.setImage(ResourceManager.getPluginImage(
 					"org.eclipse.ui", "/icons/full/etool16/delete.gif"));
 			toolkit.paintBordersFor(roomRefHyperlink);
 			roomRefHyperlink.setText("");
 
 			Button btnSelectRoom = toolkit.createButton(cmpTolerances,
 					"Select...", SWT.NONE);
 			new Label(cmpTolerances, SWT.NONE);
 			btnSelectRoom.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 
 					Resource nodeTypeResource = editingService
 							.getData(GeoPackage.Literals.COUNTRY);
 					LocationFilterDialog dialog = new LocationFilterDialog(
 							NewEditNode.this.getShell(), nodeTypeResource);
 					if (dialog.open() == IDialogConstants.OK_ID) {
 						Location room = (Location) dialog.getFirstResult();
 
 						Command sc = new SetCommand(editingService
 								.getEditingDomain(), node,
 								OperatorsPackage.Literals.NODE__LOCATION_REF,
 								room);
 						editingService.getEditingDomain().getCommandStack()
 								.execute(sc);
 					}
 				}
 			});
 		}
 	}
 
 	private void buildInfoSection() {
 		Section scnInfo = toolkit.createSection(this, Section.EXPANDED
 				| Section.TITLE_BAR);
 		toolkit.paintBordersFor(scnInfo);
 		scnInfo.setText("Info");
 
 		Composite composite = toolkit.createComposite(scnInfo, SWT.NONE);
 		toolkit.paintBordersFor(composite);
 		scnInfo.setClient(composite);
 		composite.setLayout(new GridLayout(4, false));
 
 		Label lblName = toolkit.createLabel(composite, "NE ID:", SWT.NONE);
 		lblName.setAlignment(SWT.RIGHT);
 		GridData gd_lblName = new GridData(SWT.RIGHT, SWT.CENTER, false, false,
 				1, 1);
 		gd_lblName.widthHint = 80;
 		lblName.setLayoutData(gd_lblName);
 
 		txtName = toolkit.createText(composite, "New Text", SWT.NONE
 				| widgetStyle);
 		txtName.setText("");
 		GridData gd_txtName = new GridData(SWT.LEFT, SWT.CENTER, false, false,
 				1, 1);
 		gd_txtName.widthHint = 150;
 		txtName.setLayoutData(gd_txtName);
 		new Label(composite, SWT.NONE);
 		new Label(composite, SWT.NONE);
 
 		Label lblType = toolkit.createLabel(composite, "Type:", SWT.NONE);
 		lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
 				1, 1));
 
 		txtNodeType = toolkit.createText(composite, "New Text", SWT.NONE
 				| SWT.READ_ONLY);
 		txtNodeType.setText("");
 		txtNodeType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
 				false, 1, 1));
 
 		if (!readonly) {
 			nodeTypeHyperlink = toolkit.createImageHyperlink(composite,
 					SWT.NONE);
 			nodeTypeHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
 				public void linkActivated(HyperlinkEvent e) {
 
 					final CompoundCommand cp = new CompoundCommand();
 					final NodeType nt = node.getNodeType();
 					if (nt != null) {
 						final Command dc = WarningDeleteCommand.create(
 								editingService.getEditingDomain(), nt);
 						cp.append(dc);
 					}
 					if (node.eIsSet(OperatorsPackage.Literals.NODE__ORIGINAL_NODE_TYPE_REF)) {
 						final SetCommand sc = new SetCommand(
 								editingService.getEditingDomain(),
 								node,
 								OperatorsPackage.Literals.NODE__ORIGINAL_NODE_TYPE_REF,
 								null);
 						cp.append(sc);
 					}
 
 					// We can't really do this, as our object will be dangling.
 					// Command c = new
 					// SetCommand(editingService.getEditingDomain(),
 					// node, OperatorsPackage.Literals.NODE__NODE_TYPE, null);
 					editingService.getEditingDomain().getCommandStack()
 							.execute(cp);
 				}
 			});
 			GridData gd_imageHyperlink_1 = new GridData(SWT.LEFT, SWT.CENTER,
 					false, false, 1, 1);
 			gd_imageHyperlink_1.widthHint = 18;
 			nodeTypeHyperlink.setLayoutData(gd_imageHyperlink_1);
 			nodeTypeHyperlink.setImage(ResourceManager.getPluginImage(
 					"org.eclipse.ui", "/icons/full/etool16/delete.gif"));
 			toolkit.paintBordersFor(nodeTypeHyperlink);
 			nodeTypeHyperlink.setText("");
 
 			Button btnSelectNodeType = toolkit.createButton(composite,
 					"Select...", SWT.NONE);
 			btnSelectNodeType.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					Resource nodeTypeResource = editingService
 							.getData(LibraryPackage.Literals.NODE_TYPE);
 					NodeTypeFilterDialog dialog = new NodeTypeFilterDialog(
 							NewEditNode.this.getShell(), nodeTypeResource);
 					if (dialog.open() == IDialogConstants.OK_ID) {
 						NodeType nt = (NodeType) dialog.getFirstResult();
 						// Ask the user if the node should be replaced,
 						// or simply set as the original node type.
 						boolean copyStructure = MessageDialog.openQuestion(
								NewEditNode.this.getShell(),"Network Element Type Option",
								" Choose to select parts from the original Network Element Type: "
										+ nt.getName() + "?\n" + 
								" When selecting \'No\' the structure of \'"
										+ node.getNodeID()
										+ "\' will be replaced by the original network "
										+ "Element Type structure\n (WARNING: Choosing \'No\' will replace the current structure, all editing will be lost)");
						if (!copyStructure) {
 							handleNodeTypeCopy(nt);
 						} else {
 							handleSetOriginalNodeType(nt);
 						}
 					}
 				}
 
 				private void handleSetOriginalNodeType(NodeType nt) {
 
 					Command setOriginalNodeTypeRef = new SetCommand(
 							editingService.getEditingDomain(),
 							node,
 							OperatorsPackage.Literals.NODE__ORIGINAL_NODE_TYPE_REF,
 							nt);
 					editingService.getEditingDomain().getCommandStack()
 							.execute(setOriginalNodeTypeRef);
 				}
 
 				private void handleNodeTypeCopy(NodeType nt) {
 
 					@SuppressWarnings("serial")
 					EcoreUtil.Copier nodeTypeCopier = new EcoreUtil.Copier() {
 
 						@Override
 						protected EObject createCopy(EObject eObject) {
 							EObject createCopy = super.createCopy(eObject);
 							if (createCopy instanceof Component) {
 
 								Lifecycle newLC = GenericsFactory.eINSTANCE
 										.createLifecycle();
 								newLC.setProposed(modelUtils
 										.toXMLDate(modelUtils.todayAndNow()));
 								((Component) createCopy).setLifecycle(newLC);
 
 							}
 							return createCopy;
 						}
 
 						/**
 						 * Our version of copy reference has a special treatment
 						 * for NetXResource object.
 						 */
 						@Override
 						protected void copyReference(EReference eReference,
 								EObject eObject, EObject copyEObject) {
 
 							if (eReference == LibraryPackage.Literals.COMPONENT__RESOURCE_REFS) {
 								copyResourceReference(eReference, eObject,
 										copyEObject);
 							} else {
 								super.copyReference(eReference, eObject,
 										copyEObject);
 							}
 						}
 
 						protected void copyResourceReference(
 								EReference eReference, EObject eObject,
 								EObject copyEObject) {
 							if (eObject.eIsSet(eReference)
 									&& eReference.isMany()) {
 								@SuppressWarnings("unchecked")
 								InternalEList<EObject> source = (InternalEList<EObject>) eObject
 										.eGet(eReference);
 								@SuppressWarnings("unchecked")
 								InternalEList<EObject> target = (InternalEList<EObject>) copyEObject
 										.eGet(getTarget(eReference));
 								if (source.isEmpty()) {
 									target.clear();
 								} else {
 									boolean isBidirectional = eReference
 											.getEOpposite() != null;
 									int index = 0;
 									for (Iterator<EObject> k = resolveProxies ? source
 											.iterator() : source
 											.basicIterator(); k.hasNext();) {
 										EObject referencedEObject = k.next();
 										EObject copyReferencedEObject = get(referencedEObject);
 										if (copyReferencedEObject == null) {
 											if (useOriginalReferences) {
 												// NetXResource is a bidi link,
 												// so
 												// make an actual copy (A copier
 												// within a copier... auch).
 												if (isBidirectional) {
 													EcoreUtil.Copier defaultCopier = new EcoreUtil.Copier();
 													EObject newEObject = defaultCopier
 															.copy(referencedEObject);
 
 													if (copyEObject instanceof Component) {
 
 														// String
 														// cdoResourcePath =
 														// null;
 														// try {
 														// cdoResourcePath =
 														// modelUtils
 														// .cdoCalculateResourceName(node);
 														// } catch
 														// (IllegalAccessException
 														// e) {
 														// if
 														// (ScreensActivator.DEBUG)
 														// {
 														// ScreensActivator.TRACE
 														// .trace(ScreensActivator.TRACE_SCREENS_OPTION,
 														// "Error creating CDO Resource name for node: "
 														// + node.getNodeID(),
 														// e);
 														// }
 														// }
 														//
 														// if (cdoResourcePath
 														// == null) {
 														// if
 														// (ScreensActivator.DEBUG)
 														// {
 														// ScreensActivator.TRACE
 														// .trace(ScreensActivator.TRACE_SCREENS_OPTION,
 														// "Error, CDO resource can't be determoned, should not occur!");
 														// }
 														// return; // Can't
 														// // calculate
 														// // path for
 														// // empty
 														// // names.
 														// } else {
 														// if
 														// (ScreensActivator.DEBUG)
 														// {
 														// ScreensActivator.TRACE
 														// .trace(ScreensActivator.TRACE_SCREENS_OPTION,
 														// "Creating CDO Resource "
 														// + cdoResourcePath);
 														// }
 														// }
 														// final Resource
 														// resourcesResource =
 														// editingService
 														// .getDataService()
 														// .getProvider()
 														// .getResource(
 														// editingService
 														// .getEditingDomain()
 														// .getResourceSet(),
 														// cdoResourcePath);
 
 														final Resource cdoResourceForNetXResource = modelUtils
 																.cdoResourceForNetXResource(
 																		node,
 																		(CDOTransaction) node
 																				.cdoView());
 														cdoResourceForNetXResource
 																.getContents()
 																.add(newEObject);
 													}
 
 													target.addUnique(index,
 															newEObject);
 													index++;
 												} else {
 													target.addUnique(index,
 															referencedEObject);
 													++index;
 												}
 											}
 										} else {
 
 											// This would actually do what?
 											if (isBidirectional) {
 												int position = target
 														.indexOf(copyReferencedEObject);
 												if (position == -1) {
 													target.addUnique(index,
 															copyReferencedEObject);
 												} else if (index != position) {
 													target.move(index,
 															copyReferencedEObject);
 												}
 											} else {
 												target.addUnique(index,
 														copyReferencedEObject);
 											}
 											++index;
 										}
 									}
 								}
 							}
 						}
 					};
 
 					NodeType copyOf = (NodeType) nodeTypeCopier.copy(nt);
 					nodeTypeCopier.copyReferences();
 
 					CompoundCommand cc = new CompoundCommand();
 					Command setOriginalNodeTypeRef = new SetCommand(
 							editingService.getEditingDomain(),
 							node,
 							OperatorsPackage.Literals.NODE__ORIGINAL_NODE_TYPE_REF,
 							nt);
 					Command setNodeTypeCommand = new SetCommand(editingService
 							.getEditingDomain(), node,
 							OperatorsPackage.Literals.NODE__NODE_TYPE, copyOf);
 
 					cc.append(setOriginalNodeTypeRef);
 					cc.append(setNodeTypeCommand);
 					editingService.getEditingDomain().getCommandStack()
 							.execute(cc);
 				}
 
 			});
 		}
 	}
 
 	public EMFDataBindingContext initDataBindings_() {
 		EMFDataBindingContext context = new EMFDataBindingContext();
 
 		// The Node valiation
 		EMFUpdateValueStrategy nodeTypeStrategy = ValidationService
 				.getStrategyfactory().strategyAfterGet(new IValidator() {
 					public IStatus validate(Object value) {
 						if (value instanceof String) {
 							return new Status(IStatus.OK,
 									ScreensActivator.PLUGIN_ID, "Hello node");
 							// return Status.OK_STATUS;
 						} else {
 							return new Status(IStatus.WARNING,
 									ScreensActivator.PLUGIN_ID,
 									"Original Network Element type is not set, can't add parts");
 						}
 					}
 
 				});
 
 		IObservableValue nameObservable = SWTObservables.observeDelayedValue(
 				400, SWTObservables.observeText(txtName, SWT.Modify));
 
 		IEMFValueProperty nodeIDProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				OperatorsPackage.Literals.NODE__NODE_ID);
 
 		context.bindValue(nameObservable, nodeIDProperty.observe(node), null,
 				null);
 
 		IObservableValue nodeTypeObservable = SWTObservables
 				.observeDelayedValue(400,
 						SWTObservables.observeText(txtNodeType, SWT.Modify));
 
 		IEMFValueProperty originalNodeTypeProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(), FeaturePath.fromList(
 						OperatorsPackage.Literals.NODE__ORIGINAL_NODE_TYPE_REF,
 						LibraryPackage.Literals.NODE_TYPE__NAME));
 
 		context.bindValue(nodeTypeObservable,
 				originalNodeTypeProperty.observe(node), null, nodeTypeStrategy);
 
 		IObservableValue roomObservable = SWTObservables.observeDelayedValue(
 				400, SWTObservables.observeText(txtRoom, SWT.Modify));
 
 		IEMFValueProperty roomProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(),
 				OperatorsPackage.Literals.NODE__LOCATION_REF);
 
 		EMFUpdateValueStrategy modelToTargetLocationStrategy = new EMFUpdateValueStrategy();
 		modelToTargetLocationStrategy.setConverter(new IConverter() {
 
 			public Object getFromType() {
 				return Location.class;
 			}
 
 			public Object getToType() {
 				return String.class;
 			}
 
 			public Object convert(Object fromObject) {
 				if (fromObject instanceof Location) {
 					return locationName((EObject) fromObject);
 				}
 				return null;
 			}
 
 			String locationName(EObject locationObject) {
 				String location = "";
 				if (locationObject.eContainer() instanceof Location
 						&& locationObject instanceof Location) {
 					String parentLocationName = this
 							.locationName(locationObject.eContainer());
 					return location = parentLocationName + "-->"
 							+ ((Location) locationObject).getName();
 				}
 
 				if (locationObject instanceof Location) {
 					location += ((Location) locationObject).getName();
 				}
 				return location;
 			}
 
 		});
 
 		context.bindValue(roomObservable, roomProperty.observe(node), null,
 				modelToTargetLocationStrategy);
 
 		// Lifecycle properties.
 		IObservableValue dcProposedObservable = new DateChooserComboObservableValue(
 				dcProposed, SWT.Modify);
 
 		IObservableValue dcPlannedObservable = new DateChooserComboObservableValue(
 				dcPlanned, SWT.Modify);
 
 		IObservableValue dcConstructionObservable = new DateChooserComboObservableValue(
 				dcConstruction, SWT.Modify);
 
 		IObservableValue dcInServiceObservable = new DateChooserComboObservableValue(
 				dcInService, SWT.Modify);
 
 		IObservableValue dcOutOfServiceObservable = new DateChooserComboObservableValue(
 				dcOutOfService, SWT.Modify);
 
 		IEMFValueProperty proposedProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(), FeaturePath.fromList(
 						OperatorsPackage.Literals.NODE__LIFECYCLE,
 						GenericsPackage.Literals.LIFECYCLE__PROPOSED));
 
 		IEMFValueProperty plannedProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(), FeaturePath.fromList(
 						OperatorsPackage.Literals.NODE__LIFECYCLE,
 						GenericsPackage.Literals.LIFECYCLE__PLANNED_DATE));
 
 		IEMFValueProperty constructionProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(), FeaturePath.fromList(
 						OperatorsPackage.Literals.NODE__LIFECYCLE,
 						GenericsPackage.Literals.LIFECYCLE__CONSTRUCTION_DATE));
 
 		IEMFValueProperty inServiceProperty = EMFEditProperties.value(
 				editingService.getEditingDomain(), FeaturePath.fromList(
 						OperatorsPackage.Literals.NODE__LIFECYCLE,
 						GenericsPackage.Literals.LIFECYCLE__IN_SERVICE_DATE));
 
 		IEMFValueProperty outOfServiceProperty = EMFEditProperties
 				.value(editingService.getEditingDomain(),
 						FeaturePath
 								.fromList(
 										OperatorsPackage.Literals.NODE__LIFECYCLE,
 										GenericsPackage.Literals.LIFECYCLE__OUT_OF_SERVICE_DATE));
 
 		EMFUpdateValueStrategy modelToTargetUpdateStrategy = new EMFUpdateValueStrategy();
 		modelToTargetUpdateStrategy.setConverter(new IConverter() {
 
 			public Object getFromType() {
 				return XMLGregorianCalendar.class;
 			}
 
 			public Object getToType() {
 				return Date.class;
 			}
 
 			public Object convert(Object fromObject) {
 				return modelUtils
 						.fromXMLDate((XMLGregorianCalendar) fromObject);
 			}
 		});
 
 		EMFUpdateValueStrategy targetToModelUpdateStrategy = new EMFUpdateValueStrategy();
 		targetToModelUpdateStrategy.setConverter(new IConverter() {
 
 			public Object getFromType() {
 				return Date.class;
 			}
 
 			public Object getToType() {
 
 				return XMLGregorianCalendar.class;
 			}
 
 			public Object convert(Object fromObject) {
 				return modelUtils.toXMLDate((Date) fromObject);
 			}
 		});
 
 		context.bindValue(dcProposedObservable, proposedProperty.observe(node),
 				targetToModelUpdateStrategy, modelToTargetUpdateStrategy);
 
 		context.bindValue(dcPlannedObservable, plannedProperty.observe(node),
 				targetToModelUpdateStrategy, modelToTargetUpdateStrategy);
 
 		context.bindValue(dcConstructionObservable,
 				constructionProperty.observe(node),
 				targetToModelUpdateStrategy, modelToTargetUpdateStrategy);
 
 		context.bindValue(dcInServiceObservable,
 				inServiceProperty.observe(node), targetToModelUpdateStrategy,
 				modelToTargetUpdateStrategy);
 
 		context.bindValue(dcOutOfServiceObservable,
 				outOfServiceProperty.observe(node),
 				targetToModelUpdateStrategy, modelToTargetUpdateStrategy);
 
 		return context;
 	}
 
 	@Override
 	public Form getScreenForm() {
 		return this.parentForm;
 	}
 
 }
