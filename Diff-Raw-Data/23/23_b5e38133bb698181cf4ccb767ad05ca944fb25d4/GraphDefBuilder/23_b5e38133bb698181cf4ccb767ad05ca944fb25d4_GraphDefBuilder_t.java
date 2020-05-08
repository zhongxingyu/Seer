 /*
  * Copyright (c) 2006, 2007 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Dmitry Stadnik - initial API and implementation
  */
 package org.eclipse.gmf.internal.bridge.wizards.pages.simple;
 
 import java.util.Iterator;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.gmf.gmfgraph.Canvas;
 import org.eclipse.gmf.gmfgraph.ChildAccess;
 import org.eclipse.gmf.gmfgraph.Connection;
 import org.eclipse.gmf.gmfgraph.DecorationFigure;
 import org.eclipse.gmf.gmfgraph.DiagramElement;
 import org.eclipse.gmf.gmfgraph.DiagramLabel;
 import org.eclipse.gmf.gmfgraph.FigureDescriptor;
 import org.eclipse.gmf.gmfgraph.FigureGallery;
 import org.eclipse.gmf.gmfgraph.GMFGraphFactory;
 import org.eclipse.gmf.gmfgraph.GMFGraphPackage;
 import org.eclipse.gmf.gmfgraph.Identity;
 import org.eclipse.gmf.gmfgraph.Label;
 import org.eclipse.gmf.gmfgraph.Node;
 import org.eclipse.gmf.gmfgraph.PolylineConnection;
 import org.eclipse.gmf.gmfgraph.RealFigure;
 import org.eclipse.gmf.gmfgraph.Rectangle;
 import org.eclipse.gmf.internal.bridge.wizards.WizardUtil;
 import org.eclipse.gmf.internal.bridge.wizards.pages.simple.ResolvedItem.Resolution;
 import org.eclipse.gmf.internal.common.IncrementalNamesDispenser;
 import org.eclipse.gmf.internal.common.NamesDispenser;
 
 /**
  * @author dstadnik
  */
 public class GraphDefBuilder {
 
 	protected final Canvas existingCanvas;
 
 	protected final GMFGraphFactory gmfGraphFactory = GMFGraphPackage.eINSTANCE.getGMFGraphFactory();
 
 	protected final NamesDispenser namesDispenser = new IncrementalNamesDispenser();
 
 	protected Canvas canvas;
 
 	protected FigureGallery fGallery;
 
 	public GraphDefBuilder(Canvas existingCanvas) {
 		this.existingCanvas = existingCanvas;
 	}
 
 	public static DiagramElement getDiagramElement(Iterator<EObject> source, EObject domainElement) {
 		String name = WizardUtil.getCapName(domainElement);
 		if (name == null) {
 			return null;
 		}
 		return getDiagramElement(source, name);
 	}
 
 	public static DiagramElement getDiagramElement(Iterator<EObject> source, String domainObjectName) {
 		while (source.hasNext()) {
 			Object next = source.next();
 			if (next instanceof DiagramElement && domainObjectName.equals(((DiagramElement) next).getName())) {
 				return (DiagramElement) next;
 			}
 		}
 		return null;
 	}
 
 	protected String getUniqueName(String semanticPart) {
 		return namesDispenser.get(semanticPart);
 	}
 
 	protected String getUniqueName(String semanticPart, String suffixPart) {
 		return namesDispenser.get(semanticPart, suffixPart);
 	}
 
 	protected void addExistingName(String name) {
 		if (name != null) {
 			namesDispenser.add(name);
 		}
 	}
 
 	protected void addExistingNames(Canvas canvas) {
 		for (Iterator<EObject> it = canvas.eAllContents(); it.hasNext();) {
 			EObject next = it.next();
 			if (next instanceof Identity) {
 				addExistingName(((Identity) next).getName());
 			}
 		}
 	}
 
 	public Canvas process(ResolvedItem item) {
 		if (existingCanvas == null) {
 			canvas = gmfGraphFactory.createCanvas();
 		} else {
 			canvas = existingCanvas;
 			addExistingNames(existingCanvas);
 		}
 		if (item != null) {
 			if (canvas != existingCanvas) {
 				EPackage ePackage = (EPackage) item.getDomainRef();
 				canvas.setName(getUniqueName(ePackage.getName()));
 			}
 			fGallery = null;
 			for (Object figure : canvas.getFigures()) {
 				if (figure instanceof FigureGallery) {
 					fGallery = (FigureGallery) figure;
 				}
 			}
 			if (fGallery == null) {
 				fGallery = gmfGraphFactory.createFigureGallery();
 				fGallery.setName(getUniqueName(Messages.GraphDefBuilder0));
 				canvas.getFigures().add(fGallery);
 			}
 			for (ResolvedItem child : item.getChildren()) {
 				process(child, null);
 			}
 		}
 		return canvas;
 	}
 
 	protected void process(ResolvedItem item, DiagramElement parent) {
 		DiagramElement newParent = getDiagramElement(canvas.eAllContents(), (EObject) item.getDomainRef());
 		if (item.isDisabled()) {
 			assert newParent != null : "For disabled item there should be a diagram element with the appropriate name"; //$NON-NLS-1$
 			// Process child items since there may be new labels for existing node
 		} else {
 			if (item.getDomainRef() instanceof EClass) {
 				EClass type = (EClass) item.getDomainRef();
 				if (item.getResolution() == Resolution.NODE) {
 					newParent = createNode(type);
 				} else if (item.getResolution() == Resolution.LINK) {
 					newParent = createLink(type);
 				}
 			} else if (item.getDomainRef() instanceof EReference) {
 				EReference ref = (EReference) item.getDomainRef();
 				if (item.getResolution() == Resolution.LINK) {
 					EClass containingClass = (EClass) item.getParent().getDomainRef();
 					newParent = createLink(ref, containingClass);
 				}
 			} else if (item.getDomainRef() instanceof EAttribute) {
 				EAttribute attr = (EAttribute) item.getDomainRef();
 				if (item.getResolution() == Resolution.LABEL) {
 					EClass containingClass = (EClass) item.getParent().getDomainRef();
 					newParent = createLabel(attr, containingClass, parent);
 				}
 			}
 		}
 		for (ResolvedItem next : item.getChildren()) {
 			process(next, newParent);
 		}
 	}
 
 	protected Node createNode(EClass type) {
 		String baseName = WizardUtil.getCapName(type);
 		Rectangle figure = gmfGraphFactory.createRectangle();
 		figure.setName(getUniqueName(baseName, Messages.GraphDefBuilder1));
 		Node node = gmfGraphFactory.createNode();
 		node.setFigure(newDescriptor(figure.getName(), figure));
 		fGallery.getDescriptors().add(node.getFigure());
 		node.setName(getUniqueName(baseName, null));
 		canvas.getNodes().add(node);
 		return node;
 	}
 
 	protected Connection createLink(EClass type) {
 		String baseName = WizardUtil.getCapName(type);
 		PolylineConnection figure = gmfGraphFactory.createPolylineConnection();
 		figure.setName(getUniqueName(baseName, Messages.GraphDefBuilder1));
 		Connection link = gmfGraphFactory.createConnection();
 		link.setFigure(newDescriptor(figure.getName(), figure));
 		fGallery.getDescriptors().add(link.getFigure());
 		link.setName(getUniqueName(baseName, null));
 		canvas.getConnections().add(link);
 		return link;
 	}
 
 	protected Connection createLink(EReference ref, EClass containingClass) {
 		String baseName = WizardUtil.getCapName(ref, containingClass);
 		PolylineConnection figure = gmfGraphFactory.createPolylineConnection();
 		figure.setName(getUniqueName(baseName, Messages.GraphDefBuilder1));
 		DecorationFigure decoration = gmfGraphFactory.createPolylineDecoration();
 		decoration.setName(getUniqueName(baseName, Messages.GraphDefBuilder6));
 		figure.setTargetDecoration(decoration);
 		fGallery.getFigures().add(decoration);
 		Connection link = gmfGraphFactory.createConnection();
 		link.setFigure(newDescriptor(figure.getName(), figure));
 		fGallery.getDescriptors().add(link.getFigure());
 		link.setName(getUniqueName(baseName, null));
 		canvas.getConnections().add(link);
 		return link;
 	}
 
 	protected DiagramLabel createLabel(EAttribute attr, EClass containingClass, DiagramElement parent) {
 		if (parent == null) {
 			return null; // makes no sense to define label without parent
 		}
 		String baseName = WizardUtil.getCapName(attr, containingClass);
 		Label figure = gmfGraphFactory.createLabel();
 		figure.setName(getUniqueName(baseName, Messages.GraphDefBuilder1));
 		figure.setText(Messages.GraphDefBuilder5);
 		assert parent.getFigure().getActualFigure() instanceof RealFigure : "We are creators of this gmfgraph; there should be nothing but figure"; //$NON-NLS-1$
		final RealFigure realFigure = (RealFigure) parent.getFigure().getActualFigure();
		if (parent instanceof Node) {
			// safety check, not sure whether labels for links are created here or not
			if (realFigure.getLayout() == null) {
				realFigure.setLayout(GMFGraphFactory.eINSTANCE.createFlowLayout());
			}
		}
		realFigure.getChildren().add(figure);
 		DiagramLabel label = gmfGraphFactory.createDiagramLabel();
 		label.setFigure(parent.getFigure());
 		label.setAccessor(newAccess(parent.getFigure(), figure));
 		label.setName(getUniqueName(baseName, null));
 		canvas.getLabels().add(label);
 		return label;
 	}
 
 	private static FigureDescriptor newDescriptor(String name, RealFigure fig) {
 		FigureDescriptor fd = GMFGraphFactory.eINSTANCE.createFigureDescriptor();
 		fd.setActualFigure(fig);
 		fd.setName(name);
 		return fd;
 	}
 	private static ChildAccess newAccess(FigureDescriptor fd, RealFigure child) {
 		ChildAccess ca = GMFGraphFactory.eINSTANCE.createChildAccess();
 		ca.setFigure(child);
 		fd.getAccessors().add(ca);
 		return ca;
 	}
 }
