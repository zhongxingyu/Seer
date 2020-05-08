 /*
  * Copyright (c) 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.graphdef.codegen;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.gmf.gmfgraph.Canvas;
 import org.eclipse.gmf.gmfgraph.Compartment;
 import org.eclipse.gmf.gmfgraph.Connection;
 import org.eclipse.gmf.gmfgraph.CustomFigure;
 import org.eclipse.gmf.gmfgraph.DiagramLabel;
 import org.eclipse.gmf.gmfgraph.Figure;
 import org.eclipse.gmf.gmfgraph.FigureAccessor;
 import org.eclipse.gmf.gmfgraph.FigureGallery;
import org.eclipse.gmf.gmfgraph.FigureHandle;
 import org.eclipse.gmf.gmfgraph.GMFGraphFactory;
 import org.eclipse.gmf.gmfgraph.GMFGraphPackage;
 import org.eclipse.gmf.gmfgraph.Node;
 import org.eclipse.gmf.gmfgraph.util.FigureQualifiedNameSwitch;
 import org.eclipse.gmf.graphdef.codegen.NamingStrategy;
 import org.eclipse.gmf.graphdef.codegen.StandaloneGenerator.Config;
 import org.eclipse.gmf.graphdef.codegen.StandaloneGenerator.Processor;
 import org.eclipse.gmf.graphdef.codegen.StandaloneGenerator.ProcessorCallback;
 
 public class CanvasProcessor extends Processor {
 	private final DiagramElementsCopier myElementCopier;
 	private ProcessorCallback myCallback;
 	final Canvas myInput;
 	private Canvas myOutcome;
 	private FigureGallery myOutcomeGallery; 
 
 	public CanvasProcessor(Canvas input) {
 		assert input != null;
 		myInput = input;
 		myElementCopier = new DiagramElementsCopier();
 	}
 
 	public Canvas getOutcome() {
 		return myOutcome;
 	}
 
 	public void go(ProcessorCallback callback, Config config) throws InterruptedException {
 		myCallback = callback;
 		myOutcomeGallery = GMFGraphFactory.eINSTANCE.createFigureGallery();
 		myOutcomeGallery.setName(myInput.getFigures().size() == 1 ? ((FigureGallery) myInput.getFigures().get(0)).getName() : "GeneratedGallery");
 		// TODO respect implementation from original FigureGallery, see (#x#) 
 		myOutcomeGallery.setImplementationBundle(config.getPluginID());
 		handleNodes();
 		handleLinks();
 		handleCompartments();
 		handleLabels();
 		// can't use 
 		// = (Canvas) diagramElementCopier.copy(myInput);
 		// here because Copier.copy doesn't respect already copied elements
 		myOutcome = GMFGraphFactory.eINSTANCE.createCanvas();
 		myOutcome.setName(myInput.getName());
 		myOutcome.getFigures().add(myOutcomeGallery);
 
 		myOutcome.getCompartments().addAll(myElementCopier.copyAll(myInput.getCompartments()));
 		myOutcome.getLabels().addAll(myElementCopier.copyAll(myInput.getLabels()));
 		myOutcome.getNodes().addAll(myElementCopier.copyAll(myInput.getNodes()));
 		myOutcome.getConnections().addAll(myElementCopier.copyAll(myInput.getConnections()));
 
 		if (!myOutcome.eContents().isEmpty()) {
 			myElementCopier.copyReferences();
 		}
 		myCallback = null;
 	}
 
 	public String[] getRequiredBundles(FigureQualifiedNameSwitch fqnSwitch) {
 		ArrayList/*<String>*/ rv = new ArrayList();
 		for (Iterator galleries = myInput.getFigures().iterator(); galleries.hasNext();) {
 			FigureGallery next = (FigureGallery) galleries.next();
 			rv.addAll(Arrays.asList(fqnSwitch.getDependencies(next)));
 		}
 		return (String[]) rv.toArray(new String[rv.size()]);
 	}
 
 	private void handleNodes() throws InterruptedException {
 		for (Iterator it = myInput.getNodes().iterator(); it.hasNext();) {
 			Node next = (Node) it.next();
 			handleFigure(next.getNodeFigure());
 		}
 	}
 
 	private void handleLinks() throws InterruptedException {
 		for (Iterator it = myInput.getConnections().iterator(); it.hasNext();) {
 			Connection next = (Connection) it.next();
 			handleFigure(next.getConnectionFigure());
 		}
 	}
 
 	private void handleCompartments() throws InterruptedException {
 		for (Iterator it = myInput.getCompartments().iterator(); it.hasNext();) {
 			Compartment next = (Compartment) it.next();
			FigureHandle nextFigure = next.getFigure();
			if (nextFigure == null){
				throw new NullPointerException("Compartment without figure : " + next);
			}
			if (nextFigure instanceof Figure) {
				handleFigure((Figure) nextFigure);
 			} else {
 				throw new IllegalStateException("Don't support accessors for compartments yet");
 			}
 		}
 	}
 
 	private void handleLabels() throws InterruptedException {
 		for (Iterator it = myInput.getLabels().iterator(); it.hasNext();) {
 			DiagramLabel next = (DiagramLabel) it.next();
 			if (next.getFigure() instanceof FigureAccessor) {
 				assert myElementCopier.containsKey(next.getFigure()) : "Should be copied as part of previously referenced CustomFigure";
 			} else {
 				assert next.getFigure() instanceof Figure;
 				Figure f = (Figure) next.getFigure(); 
 				if (isInsideProcessedFigure(f)) {
 					// obviously, fact we got here means f is !getReferencingElements().isEmpty()
 					// feedback.findAccessorFor(f)
 					FigureAccessor accessor = GMFGraphFactory.eINSTANCE.createFigureAccessor();
 					accessor.setAccessor(NamingStrategy.INSTANCE.getChildFigureGetterName(f));
 					myElementCopier.put(f, accessor);
 					// find closest ancestor figure
 					/* XXX assume there's no cases like
 					 * Node1 -->   Rect1
 					 * Node2 -->     |- Rect2
 					 * Label -->          |- gef.Label
 					 * and the Label we process is from Node1. 
 					 * With the current approach, we'll get mirrored Rect2 instead of mirrored Rect1.
 					 */
 					Figure parent = f;
 					do {
 						parent = parent.getParent();
 						// parent can't be null, as we checked isInsideProcessedFigure prior to that.
 					} while (!myElementCopier.containsKey(parent));
 					assert myElementCopier.get(parent) instanceof CustomFigure : "We used to keep custom figures only in the mirrored gallery";
 					((CustomFigure) myElementCopier.get(parent)).getCustomChildren().add(accessor);
 				} else {
 					handleFigure(f);
 				}
 				
 			}
 		}
 	}
 
 	private boolean isInsideProcessedFigure(Figure f) {
 		return EcoreUtil.isAncestor(myElementCopier.keySet(), f);
 	}
 
 	private void handleFigure(Figure figure) throws InterruptedException {
 		if (figure instanceof CustomFigure && isPlainBareCustomFigure((CustomFigure) figure)) {
 			// XXX an implementationBundle might be an issue here (#x#),
 			// since myOutcomeGallery gonna get one we generate, while the original CustomFigure
 			// may have one specified in the ownining FigureGallery. 
 			myOutcomeGallery.getFigures().add(myElementCopier.copy(figure));
 		} else {
 			String fqn = myCallback.visitFigure(figure);
 			myElementCopier.registerSubstitution(figure, createCustomFigure(figure, fqn));
 		}
 	}
 
 	/**
 	 * FIXME diplicates {@link org.eclipse.gmf.bridge.genmodel.InnerClassViewmapProducer#isBareInstance}
 	 * Should be merged somehow.
 	 */
 	private static boolean isPlainBareCustomFigure(CustomFigure figure) {
 		if (!figure.getChildren().isEmpty()) {
 			return false;
 		}
 		final Collection featuresToCheck = new LinkedList(figure.eClass().getEAllStructuralFeatures());
 		featuresToCheck.remove(GMFGraphPackage.eINSTANCE.getIdentity_Name());
 		featuresToCheck.remove(GMFGraphPackage.eINSTANCE.getFigure_Children());
 		featuresToCheck.remove(GMFGraphPackage.eINSTANCE.getFigureMarker_Parent());
 		featuresToCheck.remove(GMFGraphPackage.eINSTANCE.getFigureHandle_ReferencingElements());
 
 		featuresToCheck.remove(GMFGraphPackage.eINSTANCE.getCustomClass_BundleName());
 		featuresToCheck.remove(GMFGraphPackage.eINSTANCE.getCustomClass_QualifiedClassName());
 		featuresToCheck.remove(GMFGraphPackage.eINSTANCE.getCustomFigure_CustomChildren());
 
 		for(Iterator it = featuresToCheck.iterator(); it.hasNext();) {
 			final EStructuralFeature next = (EStructuralFeature) it.next();
 			if (next.isDerived()) {
 				continue;
 			}
 			if (figure.eIsSet(next)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private CustomFigure createCustomFigure(Figure original, String fqn) {
 		CustomFigure cf = DiagramElementsCopier.createCustomFigure(original);
 		cf.setName(original.getName());
 		cf.setQualifiedClassName(fqn);
 		myOutcomeGallery.getFigures().add(cf);
 		return cf;
 	}
 }
