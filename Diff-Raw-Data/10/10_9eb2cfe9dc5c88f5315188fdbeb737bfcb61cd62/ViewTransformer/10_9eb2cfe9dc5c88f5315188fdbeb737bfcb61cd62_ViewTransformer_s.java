 /*******************************************************************************
  * Copyright (c) 2008-2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.codegen.core.initializer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenAnnotation;
 import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.UniqueEList;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.eef.toolkits.Widget;
 import org.eclipse.emf.eef.views.Category;
 import org.eclipse.emf.eef.views.Container;
 import org.eclipse.emf.eef.views.ElementEditor;
 import org.eclipse.emf.eef.views.View;
 import org.eclipse.emf.eef.views.ViewElement;
 import org.eclipse.emf.eef.views.ViewsFactory;
 import org.eclipse.emf.eef.views.ViewsRepository;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class ViewTransformer extends AbstractTransformer {
 
 	private Map<String, EObject> toolkits;
 
 	private Map<EObject, List<ViewElement>> workingResolvTemp;
 
 	private ViewsRepository repository;
 
 	/* ===== Constructor ===== */
 
 	public ViewTransformer(Map<String, EObject> toolkits) {
 		this.toolkits = toolkits;
 		workingResolvTemp = new HashMap<EObject, List<ViewElement>>();
 	}
 
 	/* ===== Getters/Setters ===== */
 	public Map<EObject, List<ViewElement>> getWorkingResolvTemp() {
 		return workingResolvTemp;
 	}
 
 	public void addElementToEObject(EObject source, ViewElement element) {
 		if (workingResolvTemp.get(source) != null)
 			workingResolvTemp.get(source).add(element);
 		else {
 			List<ViewElement> list = new UniqueEList<ViewElement>();
 			list.add(element);
 			workingResolvTemp.put(source, list);
 		}
 	}
 
 	/* ===== Transformation ===== */
 
 	public ViewsRepository genPackage2ViewsRepository(GenPackage genPackage, String repositoryKind) {
 
 		repository = ViewsFactory.eINSTANCE.createViewsRepository();
 		repository.getRepositoryKind().add("SWT");
 		repository.getRepositoryKind().add("Form");
 		repository.setName(genPackage.getEcorePackage().getName());
 		repository.setDocumentation("Views repository for " + genPackage.getEcorePackage().getName()
 				+ " GenPackage");
 		Category views = ViewsFactory.eINSTANCE.createCategory();
 		views.setName(genPackage.getEcorePackage().getName());
 		repository.getCategories().add(views);
 		for (GenClass genClass : genPackage.getGenClasses()) {
 			if (!genClass.getEcoreClass().isAbstract()) {
 				views.getViews().addAll(genClass2Views(genClass));
 			}
 		}
 
 		return repository;
 
 	}
 
 	public List<View> genClass2Views(GenClass genClass) {
 		List<View> result = new ArrayList<View>();
 		View view = ViewsFactory.eINSTANCE.createView();
 		view.setExplicit(true);
 		View additionalView = genClass2AdditionalView(genClass);
 		view.setName(genClass.getName());
 		Map<String, List<EStructuralFeature>> groups = genClassGroups(genClass);
 		for (String name : groups.keySet()) {
 			if (!name.equals("view")) {
 				Container container = ViewsFactory.eINSTANCE.createContainer();
 				container.setName(name);
 				container.setRepresentation(getWidget("Group"));
 				List<EStructuralFeature> features = groups.get(name);
 				for (EStructuralFeature structuralFeature : features) {
 					if (!structuralFeature.isDerived() && !isUnmanagedReference(structuralFeature)) {
 						ElementEditor editor = eStructuralFeature2ViewElement(structuralFeature);
						container.getElements().add(editor);
 					}
 				}
 				view.getElements().add(container);
 			}
 		}
 		if (groups.get("view") != null) {
 			List<EStructuralFeature> features = groups.get("view");
 			for (EStructuralFeature structuralFeature : features) {
 				if (!structuralFeature.isDerived()) {
 					ElementEditor editor = eStructuralFeature2ViewElement(structuralFeature);
					view.getElements().add(editor);
 				}
 			}
 		}
 		result.add(view);
 		addElementToEObject(genClass.getEcoreClass(), view);
 		if (additionalView != null) {
 			result.add(additionalView);
 			addElementToEObject(genClass.getEcoreClass(), additionalView);
 		}
 		return result;
 	}
 
 	public View genClass2AdditionalView(GenClass genClass) {
 		View newView = null;
 		GenAnnotation genAnnotation = genClass.getGenAnnotation("component");
 		if (genAnnotation != null) {
 			newView = ViewsFactory.eINSTANCE.createCustomView();
 			String name = genAnnotation.getDetails().get("name");
 			if (name != null) {
 				newView.setName(genClass.getName() + name);
 			}
 		}
 		return newView;
 
 	}
 
 	public ElementEditor eStructuralFeature2ViewElement(EStructuralFeature feature) {
 		ElementEditor result = ViewsFactory.eINSTANCE.createElementEditor();
 		result.setName(feature.getName());
 		if (feature instanceof EAttribute) {
 			if (feature.isMany()) {
 				result.setRepresentation(getWidget("MultiValuedEditor"));
 			} else {
 				if (feature.getEType().getName().equals("EBoolean")
 						|| feature.getEType().getName().equals("EBool")
 						|| feature.getEType().getName().equalsIgnoreCase("Boolean")) {
 					result.setRepresentation(getWidget("Checkbox"));
 				} else if (EcorePackage.eINSTANCE.getEEnum().isInstance(feature.getEType())) {
 					result.setRepresentation(getWidget("EMFComboViewer"));
 				}
 				// FIXME: HACK
 				else if ("documentation".equals(feature.getName())) {
 					result.setRepresentation(getWidget("Textarea"));
 				} else {
 					result.setRepresentation(getWidget("Text"));
 				}
 			}
 		} else if (feature instanceof EReference) {
 			EReference reference = (EReference)feature;
 			if (reference.isContainment()) {
 				if (reference.isMany())
 					result.setRepresentation(getWidget("AdvancedTableComposition"));
 				else {
					// I don't now what is it for the moment !
 				}
 			} else {
 				if (reference.isMany())
 					result.setRepresentation(getWidget("AdvancedReferencesTable"));
 				else
 					result.setRepresentation(getWidget("EObjectFlatComboViewer"));
 			}
 		}
 		addElementToEObject(feature, result);
 		return result;
 	}
 
 	/* ===== Widgets management ===== */
 
 	protected Widget getWidget(String name) {
 		if (name != null) {
 			for (String key : toolkits.keySet()) {
 				EObject toolkit = toolkits.get(key);
 				TreeIterator<EObject> iter = toolkit.eAllContents();
 				while (iter.hasNext()) {
 					EObject next = iter.next();
 					if (next instanceof Widget && name.equals(((Widget)next).getName()))
 						return (Widget)next;
 				}
 			}
 		}
 		return null;
 	}
 
 	/* ===== GenConstraint management ===== */
 
 	private Map<String, List<EStructuralFeature>> genClassGroups(GenClass genClass) {
 		Map<String, List<EStructuralFeature>> result = new HashMap<String, List<EStructuralFeature>>();
 		for (EStructuralFeature feature : genClass.getEcoreClass().getEAllStructuralFeatures()) {
 			if (isSignificant(feature) && getViewName(feature) == null) {
 				String groupConstraint = genConstraint(feature, "group");
 				if (groupConstraint != null) {
 					addToGroup(result, feature, groupConstraint);
 				} else {
 					// FIXME: HACK
 					String docConstraint = genConstraint(feature, "documentation");
 					if ("true".equals(docConstraint))
 						addToGroup(result, feature, "view");
 					else
 						addToGroup(result, feature, "properties");
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/* ===== Misc utilities ===== */
 
 	private void addToGroup(Map<String, List<EStructuralFeature>> result, EStructuralFeature feature,
 			String genConstraint) {
 		if (result.get(genConstraint) != null)
 			result.get(genConstraint).add(feature);
 		else {
 			List<EStructuralFeature> list = new ArrayList<EStructuralFeature>();
 			list.add(feature);
 			result.put(genConstraint, list);
 		}
 	}
 
 }
