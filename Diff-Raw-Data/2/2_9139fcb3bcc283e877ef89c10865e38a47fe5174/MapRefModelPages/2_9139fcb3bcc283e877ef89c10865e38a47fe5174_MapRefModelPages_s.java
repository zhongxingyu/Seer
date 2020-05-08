 /*
 * Copyright (c) 2006 Eclipse.org
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Dmitry Stadnik - initial API and implementation
  */
 package org.eclipse.gmf.mappings.presentation;
 
 import java.util.List;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.gmf.gmfgraph.Canvas;
 import org.eclipse.gmf.gmfgraph.GMFGraphPackage;
 import org.eclipse.gmf.gmfgraph.util.Assistant;
 import org.eclipse.gmf.internal.common.ui.ComboElementSelectorExtension;
 import org.eclipse.gmf.internal.common.ui.CreateNewModelExtension;
 import org.eclipse.gmf.internal.common.ui.ElementSelectorExtension;
 import org.eclipse.gmf.internal.common.ui.ExtensibleModelSelectionPage;
 import org.eclipse.gmf.internal.common.ui.ListElementSelectorExtension;
 import org.eclipse.gmf.internal.common.ui.PredefinedModelExtension;
 import org.eclipse.gmf.internal.common.ui.ResourceLocationProvider;
 import org.eclipse.gmf.mappings.CanvasMapping;
 import org.eclipse.gmf.mappings.GMFMapPackage;
 import org.eclipse.gmf.mappings.Mapping;
 import org.eclipse.gmf.tooldef.GMFToolPackage;
 import org.eclipse.gmf.tooldef.Palette;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.wizard.Wizard;
 
 /**
  * @author dstadnik
  */
 public class MapRefModelPages {
 
 	private final boolean withSelectors;
 
 	private ResourceSet resourceSet;
 
 	private ExtensibleModelSelectionPage domainModelSelectionPage;
 
 	private ExtensibleModelSelectionPage graphModelSelectionPage;
 
 	private ExtensibleModelSelectionPage toolModelSelectionPage;
 
 	public MapRefModelPages(boolean withSelectors, ResourceSet resourceSet) {
 		this.withSelectors = withSelectors;
 		this.resourceSet = resourceSet;
 	}
 
 	protected ResourceSet getResourceSet() {
 		if (resourceSet == null) {
 			resourceSet = new ResourceSetImpl();
 		}
 		return resourceSet;
 	}
 
 	public ExtensibleModelSelectionPage getDomainModelSelectionPage() {
 		return domainModelSelectionPage;
 	}
 
 	public ExtensibleModelSelectionPage getGraphModelSelectionPage() {
 		return graphModelSelectionPage;
 	}
 
 	public ExtensibleModelSelectionPage getToolModelSelectionPage() {
 		return toolModelSelectionPage;
 	}
 
 	public void addPages(Wizard wizard, ISelection selection) {
 		ResourceLocationProvider rloc = new ResourceLocationProvider(selection);
 		addDomainPage(wizard, rloc);
 		addGraphPage(wizard, rloc);
 		addToolPage(wizard, rloc);
 	}
 
 	protected void addDomainPage(Wizard wizard, ResourceLocationProvider rloc) {
 		domainModelSelectionPage = new ExtensibleModelSelectionPage("domain", rloc, getResourceSet(), "ecore"); //$NON-NLS-1$ //$NON-NLS-2$
 		domainModelSelectionPage.setTitle("Select Domain Model");
 		domainModelSelectionPage.setDescription("Load domain model and select element for canvas mapping.");
 		List<URI> uris = rloc.getSelectedURIs("ecore"); //$NON-NLS-1$
 		uris.add(URI.createURI("platform:/plugin/org.eclipse.emf.ecore/model/Ecore.ecore")); //$NON-NLS-1$
 		domainModelSelectionPage.addExtension("prem", new PredefinedModelExtension(domainModelSelectionPage, uris)); //$NON-NLS-1$
 		if (withSelectors) {
 			domainModelSelectionPage.addExtension("domainModel", new ComboElementSelectorExtension() { //$NON-NLS-1$
 
 						protected String getModelElementName() {
 							return "Package:";
 						}
 
 						protected String getModelElementLabel(EObject modelElement) {
 							String name = ((EPackage) modelElement).getName();
 							if (name == null || name.trim().length() == 0) {
 								name = "<unnamed>";
 							}
 							return name;
 						}
 
 						protected EClass getModelElementClass() {
 							return EcorePackage.eINSTANCE.getEPackage();
 						}
 					});
 			domainModelSelectionPage.addExtension("domainElement", new ListElementSelectorExtension() { //$NON-NLS-1$
 
 						protected String getModelElementName() {
 							return "Class:";
 						}
 
 						protected String getModelElementLabel(EObject modelElement) {
 							String name = ((EClass) modelElement).getName();
 							if (name == null || name.trim().length() == 0) {
 								name = "<unnamed>";
 							}
 							return name;
 						}
 
 						protected EClass getModelElementClass() {
 							return EcorePackage.eINSTANCE.getEClass();
 						}
 					});
 		}
 		wizard.addPage(domainModelSelectionPage);
 	}
 
 	protected void addGraphPage(Wizard wizard, ResourceLocationProvider rloc) {
 		graphModelSelectionPage = new ExtensibleModelSelectionPage("graph", rloc, getResourceSet(), "gmfgraph"); //$NON-NLS-1$ //$NON-NLS-2$
 		graphModelSelectionPage.setTitle("Select Diagram Canvas");
 		graphModelSelectionPage.setDescription("Load graphical definition model and select diagram canvas for canvas mapping.");
 		List<URI> uris = rloc.getSelectedURIs("gmfgraph"); //$NON-NLS-1$
 		uris.add(Assistant.getBasicGraphDef());
 		graphModelSelectionPage.addExtension("prem", new PredefinedModelExtension(graphModelSelectionPage, uris)); //$NON-NLS-1$
 		if (withSelectors) {
 			graphModelSelectionPage.addExtension("canvas", new ComboElementSelectorExtension() { //$NON-NLS-1$
 
 						protected String getModelElementName() {
 							return "Diagram Canvas:";
 						}
 
 						protected String getModelElementLabel(EObject modelElement) {
 							String name = ((Canvas) modelElement).getName();
 							if (name == null || name.trim().length() == 0) {
 								name = "<unnamed>";
 							}
 							return name;
 						}
 
 						protected EClass getModelElementClass() {
 							return GMFGraphPackage.eINSTANCE.getCanvas();
 						}
 					});
 		}
 		wizard.addPage(graphModelSelectionPage);
 	}
 
 	protected void addToolPage(Wizard wizard, ResourceLocationProvider rloc) {
 		toolModelSelectionPage = new ExtensibleModelSelectionPage("tool", rloc, getResourceSet(), "gmftool") { //$NON-NLS-1$ //$NON-NLS-2$
 
 			public void validatePage() {
 				CreateNewModelExtension ext = (CreateNewModelExtension) getExtension("new"); //$NON-NLS-1$
 				if (ext != null) {
 					ext.validatePage();
 				} else {
 					super.validatePage();
 				}
 			}
 		};
 		toolModelSelectionPage.setTitle("Select Diagram Palette");
 		toolModelSelectionPage.setDescription("Load tooling definition model and select diagram palette for canvas mapping.");
 		if (withSelectors) {
 			toolModelSelectionPage.addExtension("palette", new ComboElementSelectorExtension() { //$NON-NLS-1$
 
 						protected String getModelElementName() {
 							return "Diagram Palette:";
 						}
 
 						protected String getModelElementLabel(EObject modelElement) {
 							String title = ((Palette) modelElement).getTitle();
 							if (title == null || title.trim().length() == 0) {
 								title = "<untitled>";
 							}
 							return title;
 						}
 
 						protected EClass getModelElementClass() {
 							return GMFToolPackage.eINSTANCE.getPalette();
 						}
 					});
 		}
 		wizard.addPage(toolModelSelectionPage);
 	}
 
 	public void allowNewToolingModel() {
 		toolModelSelectionPage.addExtension("new", new CreateNewModelExtension(toolModelSelectionPage)); //$NON-NLS-1$
 	}
 
 	public CreateNewModelExtension getCreateNewToolingModelExt() {
 		if (toolModelSelectionPage == null) {
 			return null;
 		}
 		return (CreateNewModelExtension) toolModelSelectionPage.getExtension("new"); //$NON-NLS-1$
 	}
 
 	public boolean shouldCreateNewToolingModel(boolean defaultValue) {
 		CreateNewModelExtension ext = getCreateNewToolingModelExt();
 		if (ext == null) {
 			return defaultValue;
 		}
 		return ext.shouldCreateNewModel();
 	}
 
 	protected ElementSelectorExtension getElementSelectorExtension(ExtensibleModelSelectionPage page, String selectorId) {
 		if (page == null) {
 			return null;
 		}
 		return (ElementSelectorExtension) page.getExtension(selectorId);
 	}
 
 	protected EObject getSelectedElement(ExtensibleModelSelectionPage page, String selectorId) {
 		ElementSelectorExtension ext = getElementSelectorExtension(page, selectorId);
 		if (ext == null) {
 			return null;
 		}
 		return ext.getModelElement();
 	}
 
 	public ElementSelectorExtension getDomainModelExt() {
 		return getElementSelectorExtension(domainModelSelectionPage, "domainModel"); //$NON-NLS-1$
 	}
 
 	public ElementSelectorExtension getDomainElementExt() {
 		return getElementSelectorExtension(domainModelSelectionPage, "domainElement"); //$NON-NLS-1$
 	}
 
 	public ElementSelectorExtension getCanvasExt() {
 		return getElementSelectorExtension(graphModelSelectionPage, "canvas"); //$NON-NLS-1$
 	}
 
 	public ElementSelectorExtension getPaletteExt() {
 		return getElementSelectorExtension(toolModelSelectionPage, "palette"); //$NON-NLS-1$
 	}
 
 	public EPackage getDomainModel() {
 		return (EPackage) getSelectedElement(domainModelSelectionPage, "domainModel"); //$NON-NLS-1$
 	}
 
 	public EClass getDomainElement() {
 		return (EClass) getSelectedElement(domainModelSelectionPage, "domainElement"); //$NON-NLS-1$
 	}
 
 	public Canvas getCanvas() {
 		return (Canvas) getSelectedElement(graphModelSelectionPage, "canvas"); //$NON-NLS-1$
 	}
 
 	public Palette getPalette() {
 		return (Palette) getSelectedElement(toolModelSelectionPage, "palette"); //$NON-NLS-1$
 	}
 
 	public Mapping createMapping() {
 		Mapping mapping = GMFMapPackage.eINSTANCE.getGMFMapFactory().createMapping();
 		CanvasMapping canvasMapping = GMFMapPackage.eINSTANCE.getGMFMapFactory().createCanvasMapping();
 		mapping.setDiagram(canvasMapping);
 		EPackage domainModel = getDomainModel();
 		if (domainModel != null) {
 			canvasMapping.setDomainModel(domainModel);
 		}
 		EClass domainElement = getDomainElement();
 		if (domainElement != null) {
 			canvasMapping.setDomainMetaElement(domainElement);
 		}
 		Canvas canvas = getCanvas();
 		if (canvas != null) {
 			canvasMapping.setDiagramCanvas(canvas);
 		}
 		Palette palette = getPalette();
 		if (palette != null) {
 			canvasMapping.setPalette(palette);
 		}
 		return mapping;
 	}
 }
