 /*
  * Copyright (c) 2005 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.codegen.wizards.pages;
 
 import java.util.Arrays;
 import java.util.Iterator;
 
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.gmf.mappings.FeatureSeqInitializer;
 import org.eclipse.gmf.mappings.FeatureValueSpec;
 import org.eclipse.gmf.mappings.GMFMapFactory;
 import org.eclipse.gmf.mappings.LinkMapping;
 import org.eclipse.gmf.mappings.Mapping;
 import org.eclipse.gmf.mappings.MappingEntry;
 import org.eclipse.gmf.mappings.NodeMapping;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.ui.dialogs.ListDialog;
 
 /**
  * @author artem
  *
  */
 public class EntriesPage extends WizardPage {
 	private final WizardInput myHolder;
 
 	public EntriesPage(WizardInput input) {
 		super("entriesPage");
 		this.myHolder = input;
 	}
 
 	protected Mapping getMapInstance() {
 		return myHolder.getMapping();
 	}
 
 	protected WizardInput getHolder() {
 		return myHolder;
 	}
 
 	public void createControl(Composite parent) {
 		setControl(new PageControl(parent));
 	}
 
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 		if (visible) {
 			((PageControl) getControl()).populate();
 		}
 	}
 
 	private class PageControl extends Composite {
 
 		private Group group = null;
 		private List nodesList = null;
 		private Group group1 = null;
 		private List linksList = null;
 		private Composite detailsPart = null;
 		private Group groupStructure = null;
 		private Group groupEdit = null;
 		private Group groupVisual = null;
 		private Composite composite2 = null;
 		private Composite composite = null;
 		private Button asNodeButton = null;
 		private Button asLinkButton = null;
 		private Button removeButton = null;
 		private Button changeDetailsButton = null;
 		private Button restoreButton = null;
 		private Group groupConstaints = null;
 		private Label specLabel = null;
 		private Label initLabel = null;
 		private Label editFeatureLabel = null;
 		private Label displayFeatureLabel = null;
 		private Label diagramElementLabel = null;
 		private Label metaElementLabel;
 		private Label containmentLabel;
 		private Label linkMetaFeatureLabel;
 
 		private MappingEntry selectedEntry;
 		private final ILabelProvider myLabelProvider = new LabelProvider() {
 			public String getText(Object element) {
 				if (element instanceof LinkMapping) {
 					LinkMapping next = (LinkMapping) element;
 					StringBuffer sb = new StringBuffer();
 					if (next.getDomainMetaElement() == null) {
 						sb.append(next.getLinkMetaFeature() == null ? "Link" : next.getLinkMetaFeature().getName());
 					} else {
 						sb.append(next.getDomainMetaElement().getName());
 					}
 					sb.append(" (");
 					if (next.getDiagramLink() != null) {
 						sb.append(next.getDiagramLink().getName());
 						if (next.getContainmentFeature() != null) {
 							sb.append(";  ");
 						}
 					}
 					if (next.getContainmentFeature() != null) {
 						sb.append(next.getContainmentFeature().getName());
 					}
 					sb.append(")");
 					return sb.toString();
 				} else {
 					NodeMapping next = (NodeMapping) element;
 					StringBuffer sb = new StringBuffer();
 					sb.append(next.getDomainMetaElement() == null ? "Node" : next.getDomainMetaElement().getName());
 					sb.append(" (");
 					if (next.getDiagramNode() != null) {
 						sb.append(next.getDiagramNode().getName());
 						if (next.getContainmentFeature() != null) {
 							sb.append(";  ");
 						}
 					}
 					if (next.getContainmentFeature() != null) {
 						sb.append(next.getContainmentFeature().getName());
 					}
 					sb.append(")");
 					return sb.toString();
 				}
 			}
 		};
 
 		private SelectionListener myListListener = new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				final boolean nodeSelected = e.widget == nodesList;
 				asLinkButton.setEnabled(nodeSelected);
 				removeButton.setEnabled(true);
 				changeDetailsButton.setEnabled(true);
 				restoreButton.setEnabled(true);
 				if (nodeSelected) {
 					asNodeButton.setEnabled(false);
 					assert nodesList.getSelectionIndex() != -1;
 					selectedEntry = (NodeMapping) getMapInstance().getNodes().get(nodesList.getSelectionIndex());
 					refreshNodeDetails();
 					linksList.deselectAll();
 				} else {
 					// e.widget == linksList
 					assert linksList.getSelectionIndex() != -1;
 					selectedEntry =(LinkMapping) getMapInstance().getLinks().get(linksList.getSelectionIndex());
 					asNodeButton.setEnabled(selectedEntry.getDomainMetaElement() != null);
 					refreshLinkDetails();
 					nodesList.deselectAll();
 				}
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		};
 
 		public PageControl(Composite parent) {
 			super(parent, SWT.NONE);
 			initialize();
 		}
 
 		public void populate() {
 			populateNodesList();
 			populateLinksList();
 		}
 
 		private void populateNodesList() {
 			String[] items = new String[getMapInstance().getNodes().size()];
 			int i = 0;
 			for (Iterator it = getMapInstance().getNodes().iterator(); it.hasNext(); i++) {
 				items[i] = myLabelProvider.getText(it.next());
 			}
 			nodesList.setItems(items);
 		}
 
 		private void populateLinksList() {
 			String[] items = new String[getMapInstance().getLinks().size()];
 			int i = 0;
 			for (Iterator it = getMapInstance().getLinks().iterator(); it.hasNext(); i++) {
 				items[i] = myLabelProvider.getText(it.next());
 			}
 			linksList.setItems(items);
 		}
 
 		private void initialize() {
 			GridLayout gridLayout = new GridLayout();
 			gridLayout.numColumns = 3;
 			this.setLayout(gridLayout);
 //			setSize(new org.eclipse.swt.graphics.Point(990,612));
 			createNodesList();
 			createButtonsPane();
 			createLinksList();
 			createDetailsPart();
 		}
 
 		private void createNodesList() {
 			GridData gridData = new GridData();
 			gridData.horizontalAlignment = GridData.FILL;
 			gridData.grabExcessHorizontalSpace = true;
 			gridData.grabExcessVerticalSpace = true;
 			gridData.verticalAlignment = GridData.FILL;
 			group = new Group(this, SWT.NONE);
 			group.setLayout(new FillLayout());
 			group.setLayoutData(gridData);
 			group.setText("Nodes");
 			nodesList = new List(group, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
 			nodesList.addSelectionListener(myListListener);
 		}
 
 		private void createLinksList() {
 			GridData gridData1 = new GridData();
 			gridData1.grabExcessHorizontalSpace = true;
 			gridData1.horizontalAlignment = GridData.FILL;
 			gridData1.verticalAlignment = GridData.FILL;
 			gridData1.grabExcessVerticalSpace = true;
 			group1 = new Group(this, SWT.NONE);
 			group1.setLayout(new FillLayout());
 			group1.setLayoutData(gridData1);
 			group1.setText("Links");
 			linksList = new List(group1, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
 			linksList.addSelectionListener(myListListener);
 		}
 
 		private void createDetailsPart() {
 			GridData gridData3 = new GridData();
 			gridData3.horizontalSpan = 5;
 			gridData3.verticalAlignment = GridData.FILL;
 			gridData3.grabExcessHorizontalSpace = true;
 			gridData3.grabExcessVerticalSpace = false;
 			gridData3.horizontalAlignment = GridData.FILL;
 			detailsPart = new Composite(this, SWT.NONE);
 			detailsPart.setLayoutData(gridData3);
 			GridLayout gridLayout1 = new GridLayout();
 			gridLayout1.numColumns = 7;
 			gridLayout1.makeColumnsEqualWidth = true;
 			detailsPart.setLayout(gridLayout1);
 			createStructureGroup();
 			createEditGroup();
 			changeDetailsButton = new Button(detailsPart, SWT.NONE);
 			changeDetailsButton.setText("Change...");
 			changeDetailsButton.addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					MessageDialog.openInformation(getShell(), "Mapping Details", "Please use EMF-generated editor to modify values for a while...");
 				}
 			});
 			GridData gridData8 = new GridData();
 			gridData8.grabExcessHorizontalSpace = true;
 			gridData8.verticalAlignment = GridData.CENTER;
 			gridData8.verticalSpan = 2;
 			gridData8.horizontalAlignment = GridData.CENTER;
 			changeDetailsButton.setLayoutData(gridData8);
 			createVisualGroup();
 			createConstraintsGroup();
 		}
 
 		private void createStructureGroup() {
 			groupStructure = new Group(detailsPart, SWT.SHADOW_OUT);
 			groupStructure.setText("Structure");
 			groupStructure.setLayoutData(newDetailGroupConstraint());
 			groupStructure.setLayout(newDetailGroupLayout());
 			Label l = new Label(groupStructure, SWT.NONE);
 			l.setText("Element:");
 			metaElementLabel = new Label(groupStructure, SWT.NONE);
 			metaElementLabel.setLayoutData(newDetailLabelConstraint());
 			l = new Label(groupStructure, SWT.NONE);
 			l.setText("Containment:");
 			containmentLabel = new Label(groupStructure, SWT.NONE);
 			containmentLabel.setLayoutData(newDetailLabelConstraint());
 			l = new Label(groupStructure, SWT.NONE);
 			l.setText("Target Feature:");
 			linkMetaFeatureLabel = new Label(groupStructure, SWT.NONE);
 			linkMetaFeatureLabel.setLayoutData(newDetailLabelConstraint());
 			
 		}
 
 		private void createEditGroup() {
 			groupEdit = new Group(detailsPart, SWT.NONE);
 			groupEdit.setText("Edit");
 			groupEdit.setLayout(newDetailGroupLayout());
 			groupEdit.setLayoutData(newDetailGroupConstraint());
 			Label l = new Label(groupEdit, SWT.NONE);
 			l.setText("In-place edit:");
 			editFeatureLabel = new Label(groupEdit, SWT.NONE);
 			editFeatureLabel.setLayoutData(newDetailLabelConstraint());
 			l = new Label(groupEdit, SWT.NONE);
 			l.setText("Display feature:");
 			displayFeatureLabel = new Label(groupEdit, SWT.NONE);
 			displayFeatureLabel.setLayoutData(newDetailLabelConstraint());
 		}
 
 		private void createVisualGroup() {
 			groupVisual = new Group(detailsPart, SWT.NONE);
 			groupVisual.setText("Visual");
 			groupVisual.setLayoutData(newDetailGroupConstraint());
 			groupVisual.setLayout(newDetailGroupLayout());
 			Label l = new Label(groupVisual, SWT.NONE);
 			l.setText("Diagram Element:");
 			diagramElementLabel = new Label(groupVisual, SWT.NONE);
 			diagramElementLabel.setLayoutData(newDetailLabelConstraint());
 		}
 
 		private void createButtonsPane() {
 			GridData gridData2 = new GridData();
 			gridData2.horizontalAlignment = GridData.FILL;
 			gridData2.verticalAlignment = GridData.CENTER;
 			composite2 = new Composite(this, SWT.NONE);
 			composite2.setLayout(new FillLayout());
 			composite2.setLayoutData(gridData2);
 			createComposite();
 		}
 
 		private void createComposite() {
 			RowLayout rowLayout = new RowLayout();
 			rowLayout.type = org.eclipse.swt.SWT.VERTICAL;
 			rowLayout.justify = true;
 			rowLayout.marginHeight = 0;
 			rowLayout.marginWidth = 0;
 			rowLayout.pack = false;
 			rowLayout.spacing = 6;
 			rowLayout.marginLeft = 10;
 			rowLayout.marginRight = 10;
 			rowLayout.fill = false;
 			composite = new Composite(composite2, SWT.NONE);
 			composite.setLayout(rowLayout);
 			asNodeButton = new Button(composite, SWT.NONE);
 			asNodeButton.setText("As node <--");
 			asNodeButton.setEnabled(false);
 			asNodeButton.addListener(SWT.Selection, new Listener() {
 				public void handleEvent(Event event) {
 					NodeMapping nm = GMFMapFactory.eINSTANCE.createNodeMapping();
 					nm.setDomainMetaElement(selectedEntry.getDomainMetaElement());
 					nm.setContainmentFeature(selectedEntry.getContainmentFeature());
 					nm.setDomainInitializer(selectedEntry.getDomainInitializer());
 					nm.setDomainSpecialization(selectedEntry.getDomainSpecialization());
 					final LinkMapping linkMapping = (LinkMapping) selectedEntry;
 					nm.setEditFeature(linkMapping.getLabelEditFeature());
 					nm.setTool(linkMapping.getTool());
 					nm.setContextMenu(linkMapping.getContextMenu());
 					nm.setAppearanceStyle(linkMapping.getAppearanceStyle());
 					getMapInstance().getNodes().add(nm);
 					linksList.remove(linksList.getSelectionIndex());
 					nodesList.add(myLabelProvider.getText(nm));
 					nodesList.select(nodesList.getItemCount() - 1);
 				}
 			});
 			asLinkButton = new Button(composite, SWT.NONE);
 			asLinkButton.setText("As link  -->");
 			asLinkButton.setEnabled(false);
 			asLinkButton.addListener(SWT.Selection, new Listener() {
 				public void handleEvent(Event event) {
 					LinkMapping lm = GMFMapFactory.eINSTANCE.createLinkMapping();
 					lm.setDomainMetaElement(selectedEntry.getDomainMetaElement());
 					lm.setContainmentFeature(selectedEntry.getContainmentFeature());
 					lm.setDomainInitializer(selectedEntry.getDomainInitializer());
 					lm.setDomainSpecialization(selectedEntry.getDomainSpecialization());
 					final NodeMapping nodeMapping = (NodeMapping) selectedEntry;
 					lm.setLabelEditFeature(nodeMapping.getEditFeature());
 					lm.setTool(nodeMapping.getTool());
 					lm.setContextMenu(nodeMapping.getContextMenu());
 					lm.setAppearanceStyle(nodeMapping.getAppearanceStyle());
 					getMapInstance().getLinks().add(lm);
 					nodesList.remove(nodesList.getSelectionIndex());
 					linksList.add(myLabelProvider.getText(lm));
 					linksList.select(linksList.getItemCount() - 1);
 				}
 			});
 			removeButton = new Button(composite, SWT.NONE);
 			removeButton.setText("Remove");
 			removeButton.setEnabled(false);
 			removeButton.addListener(SWT.Selection, new Listener() {
 				public void handleEvent(Event event) {
 					if (nodesList.getSelectionIndex() != -1) {
 						int i = nodesList.getSelectionIndex();
 						nodesList.remove(i);
 						getMapInstance().getNodes().remove(i);
 						if (i == nodesList.getItemCount() && i > 0) {
 							i--;
 						}
 						nodesList.select(i);
 					}
 					if (linksList.getSelectionIndex() != -1) {
 						int i = linksList.getSelectionIndex();
 						linksList.remove(i);
 						getMapInstance().getLinks().remove(i);
 						if (i == linksList.getItemCount() && i > 0) {
 							i--;
 						}
 						linksList.select(i);
 					}
 				}
 			});
 			restoreButton = new Button(composite, SWT.NONE);
 			restoreButton.setText("Restore...");
 			restoreButton.setEnabled(false);
 			restoreButton.addListener(SWT.Selection, new Listener() {
 				public void handleEvent(Event event) {
 					ListDialog d = new ListDialog(getShell());
 					d.setContentProvider(new IStructuredContentProvider() {
 						public Object[] getElements(Object inputElement) {
 							return (Object[]) inputElement;
 						}
 						public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 						}
 						public void dispose() {
 						}
 					});
 					d.setLabelProvider(PageControl.this.myLabelProvider);
 					final boolean isNodeMap = selectedEntry instanceof NodeMapping;
 					if (isNodeMap) {
 						d.setInput(getHolder().nodeCandidates());
 					} else {
 						d.setInput(getHolder().linkCandidates());
 					}
 					if (d.open() == ListDialog.OK) {
 						if (isNodeMap) {
 							getMapInstance().getNodes().addAll(Arrays.asList(d.getResult()));
 							nodesList.removeAll();
 							populateNodesList();
 						} else {
 							getMapInstance().getLinks().addAll(Arrays.asList(d.getResult()));
 							linksList.removeAll();
 							populateLinksList();
 						}
 					}
 				}
 			});
 		}
 
 		private void createConstraintsGroup() {
 			groupConstaints = new Group(detailsPart, SWT.NONE);
 			groupConstaints.setText("Constraints");
 			groupConstaints.setLayout(newDetailGroupLayout());
 			groupConstaints.setLayoutData(newDetailGroupConstraint());
 
 			Label label = new Label(groupConstaints, SWT.NONE);
 			label.setText("Specialization:");
 			specLabel = new Label(groupConstaints, SWT.NONE);
 			specLabel.setLayoutData(newDetailLabelConstraint());
 
 			label = new Label(groupConstaints, SWT.NONE);
 			label.setText("Initializer:");
 			initLabel = new Label(groupConstaints, SWT.NONE);
 			initLabel.setLayoutData(newDetailLabelConstraint());
 			// TODO link creation constraints
 		}
 
 		private GridLayout newDetailGroupLayout() {
 			GridLayout gridLayout = new GridLayout();
 			gridLayout.numColumns = 3;
 			gridLayout.makeColumnsEqualWidth = true;
 			return gridLayout;
 		}
 
 		private GridData newDetailGroupConstraint() {
 			GridData groupGridData = new GridData();
 			groupGridData.horizontalAlignment = GridData.FILL;
 			groupGridData.grabExcessHorizontalSpace = true;
 			groupGridData.grabExcessVerticalSpace = true;
 			groupGridData.horizontalSpan = 3;
 			groupGridData.verticalAlignment = GridData.FILL;
 			return groupGridData;
 		}
 
 		private GridData newDetailLabelConstraint() {
 			GridData labelGridData = new GridData();
 			labelGridData.horizontalSpan = 2;
 			labelGridData.grabExcessHorizontalSpace = true;
 			labelGridData.horizontalAlignment = GridData.FILL;
 			return labelGridData;
 		}
 
 		private void refreshCommonDetails() {
 			affix(metaElementLabel, selectedEntry.getDomainMetaElement());
 			affix(containmentLabel, selectedEntry.getContainmentFeature());
 			refreshDomainSpecialization();
 			refreshDomainInitializer();
 		}
 
 		private void refreshDomainSpecialization() {
 			if (selectedEntry.getDomainSpecialization() == null) {
 				specLabel.setText("");
 				return;
 			}
 			specLabel.setText(selectedEntry.getDomainSpecialization().getBody());
 		}
 
 		private void refreshDomainInitializer() {
 			if (selectedEntry.getDomainInitializer() == null || false == selectedEntry.getDomainInitializer() instanceof FeatureSeqInitializer) {
 				initLabel.setText("");
 				return;
 			}
 			FeatureSeqInitializer fsi = (FeatureSeqInitializer) selectedEntry.getDomainInitializer();
 			StringBuffer sb = new StringBuffer();
 			for (Iterator it = fsi.getInitializers().iterator(); it.hasNext();) {
 				FeatureValueSpec next = (FeatureValueSpec) it.next();
 				sb.append(next.getFeature().getName());
 				sb.append("; ");
 			}
 			initLabel.setText(sb.toString());
 		}
 
 		private void affix(Label l, ENamedElement el) {
 			if (el != null) {
 				l.setText(el.getName());
 			} else {
 				l.setText("");
 			}
 		}
 
 		private void refreshNodeDetails() {
 			refreshCommonDetails();
 			NodeMapping m = (NodeMapping) selectedEntry;
 			if (m.getDiagramNode() != null) {
 				diagramElementLabel.setText(m.getDiagramNode().getName());
 			} else {
 				diagramElementLabel.setText("");
 			}
 			affix(editFeatureLabel, m.getEditFeature());
 			linkMetaFeatureLabel.setText("");
 		}
 
 		private void refreshLinkDetails() {
 			refreshCommonDetails();
 			LinkMapping l = (LinkMapping) selectedEntry;
 			if (l.getDiagramLink() != null) {
 				diagramElementLabel.setText(l.getDiagramLink().getName());
 			} else {
 				diagramElementLabel.setText("");
 			}
 			affix(editFeatureLabel, l.getLabelEditFeature());
 			affix(displayFeatureLabel, l.getLabelDisplayFeature());
 			affix(linkMetaFeatureLabel, l.getLinkMetaFeature());
 		}
 	}
 }
