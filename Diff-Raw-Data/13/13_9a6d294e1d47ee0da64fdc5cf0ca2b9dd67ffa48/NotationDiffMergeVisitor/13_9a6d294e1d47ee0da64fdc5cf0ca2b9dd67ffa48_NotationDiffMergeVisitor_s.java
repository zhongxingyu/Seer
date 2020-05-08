 /*******************************************************************************
  * Copyright (c) 2009 Tobias Jaehnel.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Tobias Jaehnel - Bug#241385
  *******************************************************************************/
 
 package org.eclipse.emf.compare.ui.gmf.viewmodel;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.diff.merge.service.MergeService;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChange;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.UpdateAttribute;
 import org.eclipse.emf.compare.diff.metamodel.UpdateModelElement;
 import org.eclipse.emf.compare.diff.metamodel.util.DiffSwitch;
 import org.eclipse.emf.compare.match.metamodel.Match2Elements;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.ui.gmf.Constants;
 import org.eclipse.emf.compare.util.ModelUtils;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
 import org.eclipse.gmf.runtime.emf.core.resources.GMFResource;
 import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.Edge;
 import org.eclipse.gmf.runtime.notation.Node;
 import org.eclipse.gmf.runtime.notation.NotationFactory;
 import org.eclipse.gmf.runtime.notation.PropertiesSetStyle;
 import org.eclipse.gmf.runtime.notation.View;
 import org.w3c.dom.Notation;
 
 import com.sun.corba.se.spi.copyobject.CopierManager;
 
 /**
  * 
  * @author Tobias Jaehnel <tjaehnel@gmail.com>
  */
 public class NotationDiffMergeVisitor extends DiffSwitch<Object> {
 	HashMap<EObject, View> leftSemantic2notationMap = new HashMap<EObject, View>();
 	HashMap<EObject, View> rightSemantic2notationMap = new HashMap<EObject, View>();
 	HashMap<EObject, View> staticSemantic2notationMap = new HashMap<EObject, View>();
 	HashMap<EObject, EObject> right2leftSemanticMap = new HashMap<EObject, EObject>();
 	HashMap<View, View> right2leftNotationMap = new HashMap<View, View>();
 
 	Copier elementCopier;
 
 	EObject leftSemanticStaticModel;
 	Diagram leftNotationStaticModel;
 	EObject rightSemanticStaticModel;
 	Diagram rightNotationStaticModel;
 	
 	HashMap<EObject, EObject> merge2StaticSemanticMap;
 	HashMap<EObject, EObject> merge2StaticNotationMap;
 	
 	/**
 	 * This is the core of the GMF diff.
 	 * Merges the right to the left notation and semantic model for
 	 * the SemanticMergeViewer
 	 * Annotates the merged and the right model
 	 * Returns the annotated left model for the ContentMergeViewer,
 	 * the right model is not modified, only annotated and can directly
 	 * be used by the ContentMergeViewer 
 	 * 
 	 * @param semanticDiff
 	 * @param semanticMatch
 	 * @param leftNotationModel
 	 * @param rightNotationModel
 	 * @param leftSemanticModel
 	 * @param rightSemanticModel
 	 * @return the annotated left static model for ContentMergeViewer
 	 */
 	public Diagram doMergeAndAnnotate(DiffModel semanticDiff, MatchModel semanticMatch,
 			Diagram leftNotationModel, Diagram rightNotationModel,
 			EObject leftSemanticModel, EObject rightSemanticModel) {
 		rightSemanticStaticModel = rightSemanticModel;
 		rightNotationStaticModel = rightNotationModel;
 		
 		// make copies of left semantic an notation model
 		Copier semanticCopier = new Copier();
 		Copier notationCopier = new Copier();
 		leftSemanticStaticModel = semanticCopier.copy(leftSemanticModel);
 		semanticCopier.copyReferences();
 		merge2StaticSemanticMap = semanticCopier;
 		leftNotationStaticModel = (Diagram)notationCopier.copy(leftNotationModel);
 		notationCopier.copyReferences();
 		merge2StaticNotationMap = notationCopier;
 		// add copies to the resourceset
 		leftNotationModel.eResource().getContents().add(leftNotationStaticModel);
 		leftSemanticModel.eResource().getContents().add(leftSemanticStaticModel);
 		
 		// remap the references of the copied model
 		leftNotationStaticModel.setElement(leftSemanticStaticModel);
 		for (Iterator<EObject> iter = leftNotationStaticModel.eAllContents(); iter.hasNext();) {
 			EObject eObject = iter.next();
 			if (eObject instanceof View) {
 				View notationElement = (View)eObject;
 				if(notationElement.isSetElement()) {
 					EObject element = notationElement.getElement();
 					element = merge2StaticSemanticMap.get(element);
 					notationElement.setElement(element);
 					staticSemantic2notationMap.put(element, notationElement);
 				}
 			}
 		}
 
 		preCreateMappings(semanticDiff, semanticMatch, leftNotationModel, rightNotationModel, leftSemanticModel, rightSemanticModel);
 
 		// visit diff model
 		LinkedList<EObject> diffList = new LinkedList<EObject>();
 		for (Iterator<EObject> iter = semanticDiff.eAllContents(); iter.hasNext();) {
 			EObject eObject = iter.next();
 			diffList.add(eObject);
 		}
 		for (EObject eObject : diffList) {
 			doSwitch(eObject);			
 		}
 		
 		// remap all references in the notation model, which point to
 		// a right element
 		for (TreeIterator<EObject> iter = leftNotationModel.eAllContents(); iter.hasNext();) {
 			EObject element = iter.next();
 			remapElementRightToLeft(element);
 		}
 		
 		return leftNotationStaticModel;
 	}
 	
 	/**
 	 * In order to quickly navigate between corresponding semantic and notation
 	 * elements, as well as right and left elements, several maps are created
 	 * previously
 	 *  
 	 * @param semanticDiff
 	 * @param semanticMatch
 	 * @param leftNotationModel
 	 * @param rightNotationModel
 	 * @param leftSemanticModel
 	 * @param rightSemanticModel
 	 */
 	public void preCreateMappings(DiffModel semanticDiff, MatchModel semanticMatch,
 			Diagram leftNotationModel, View rightNotationModel,
 			EObject leftSemanticModel, EObject rightSemanticModel) {
 		// create left semantic to notation element map
 		leftSemantic2notationMap.put(leftNotationModel.getElement(), leftNotationModel);
 		for (TreeIterator<EObject> iter = leftNotationModel.eAllContents(); iter.hasNext();) {
 			EObject element = iter.next();
 			if (element instanceof View) {
 				View notationElement = (View)element;
 				if(!leftSemantic2notationMap.containsKey(notationElement.getElement()))
 					leftSemantic2notationMap.put(notationElement.getElement(),notationElement);
 			}
 		}
 		// create right semantic to notation element map
 		rightSemantic2notationMap.put(rightNotationModel.getElement(), rightNotationModel);
 		for (TreeIterator<EObject> iter = rightNotationModel.eAllContents(); iter.hasNext();) {
 			EObject element = iter.next();
 			if (element instanceof View) {
 				View notationElement = (View)element;
 				if(!rightSemantic2notationMap.containsKey(notationElement.getElement()))
 					rightSemantic2notationMap.put(notationElement.getElement(),notationElement);
 			}
 		}
 		// create right to left maps (semantic and notation)
 		// this is based on the sematic match-mode
 		for (TreeIterator<EObject> iter = semanticMatch.eAllContents(); iter.hasNext();) {
 			EObject element = iter.next();
 			if (element instanceof Match2Elements) {
 				Match2Elements matchElement = (Match2Elements)element;
 				right2leftSemanticMap.put(matchElement.getRightElement(), matchElement.getLeftElement());
 				
 				View rightNotation = rightSemantic2notationMap.get(matchElement.getRightElement());
 				View leftNotation = leftSemantic2notationMap.get(matchElement.getLeftElement());
 				if(rightNotation != null && leftNotation != null)
 					right2leftNotationMap.put(rightNotation, leftNotation);
 			}
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see org.eclipse.emf.compare.diff.metamodel.util.DiffSwitch#caseModelElementChangeRightTarget(org.eclipse.emf.compare.diff.metamodel.ModelElementChangeRightTarget)
 	 */
 	@Override
 	public Object caseModelElementChangeRightTarget(ModelElementChangeRightTarget object) {
 		if (!object.isRemote()) {
 			EObject rightElement = object.getRightElement();
 	
 			// find the correspondig notation element
 			View rightElementNotation = rightSemantic2notationMap.get(rightElement);
 			if(rightElementNotation == null) {
 				//TODO: resolve inconsistency between semantic and notation models
 				return null;
 			}
 	
 			// merge the right _semantic_ element to the left
 			if(elementCopier == null) {
 				elementCopier = MergeService.getCopier(object);
 				elementCopier.putAll(right2leftSemanticMap);			
 			}
 			if(object.eContainer() != null)
 				MergeService.merge(object, false);
 			
 			// merge the _notation_ element to left model add annotation
 			View newLeftElementNotation = notationElementRightToLeft(rightElementNotation);
 			newLeftElementNotation.setElement(elementCopier.get(rightElement));
 			annotateNotation(newLeftElementNotation, Constants.STYLE_STATE_VALUE_DELETED);
 			annotateNotation(rightElementNotation, Constants.STYLE_STATE_VALUE_DELETED); // _static_ (see below)
 			for (TreeIterator<EObject> iter = rightElement.eAllContents(); iter.hasNext();) {
 				EObject element = iter.next();
 				View notationElement = rightSemantic2notationMap.get(element);
 				if(notationElement == null) {
 					//TODO: resolve inconsistency between semantic and notation models
 					continue;
 				}
 				View leftNotationElement = notationElementRightToLeft(notationElement);
 				annotateNotation(leftNotationElement, Constants.STYLE_STATE_VALUE_DELETED);
 				leftNotationElement.setElement(elementCopier.get(element));
 				
 				// annotate the _static_ notation element for ContentMergeViewer (right model)
 				// this can be done in the same loop, because the right model is unmodified
 				annotateNotation(notationElement, Constants.STYLE_STATE_VALUE_DELETED);
 			}
			return object;
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see org.eclipse.emf.compare.diff.metamodel.util.DiffSwitch#caseModelElementChangeLeftTarget(org.eclipse.emf.compare.diff.metamodel.ModelElementChangeLeftTarget)
 	 */
 	@Override
 	public Object caseModelElementChangeLeftTarget(ModelElementChangeLeftTarget object) {
 		if (!object.isRemote()) {
 			EObject leftElement = object.getLeftElement();
 	
 			// find the correspondig notation element
 			View leftElementNotation = leftSemantic2notationMap.get(leftElement);
 			if(leftElementNotation == null) {
 				//TODO: resolve inconsistency between semantic and notation models
 				return null;
 			}
 	
 			// annotate notation element and all sub elements
 			annotateNotation(leftElementNotation, Constants.STYLE_STATE_VALUE_ADDED);
 			for (TreeIterator<EObject> iter = leftElement.eAllContents(); iter.hasNext();) {
 				EObject element = iter.next();
 				View notationElement = leftSemantic2notationMap.get(element);
 				if(notationElement == null) {
 					//TODO: resolve inconsistency between semantic and notation models
 					continue;
 				}
 				annotateNotation(notationElement, Constants.STYLE_STATE_VALUE_ADDED);
 			}
 	
 			// annotate the _static_ notation elements for ContentMergeViewer (left model)
 			EObject staticElement = merge2StaticSemanticMap.get(leftElement);
 			if(staticElement != null) {
 				View staticNotationElement = staticSemantic2notationMap.get(staticElement);
 				if(staticNotationElement != null)
 					annotateNotation(staticNotationElement,Constants.STYLE_STATE_VALUE_ADDED);
 				for (TreeIterator<EObject> iter = staticElement.eAllContents(); iter.hasNext();) {
 					EObject element = iter.next();
 					staticNotationElement = staticSemantic2notationMap.get(element);
 					if(staticNotationElement != null)
 						annotateNotation(staticNotationElement, Constants.STYLE_STATE_VALUE_ADDED);
 				}
 			}
			return object;
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see org.eclipse.emf.compare.diff.metamodel.util.DiffSwitch#caseUpdateAttribute(org.eclipse.emf.compare.diff.metamodel.UpdateAttribute)
 	 */
 	@Override
 	public Object caseUpdateAttribute(UpdateAttribute object) {
 		EObject leftElement = object.getLeftElement();
 		String leftValue = (String)leftElement.eGet(object.getAttribute());
 		EObject rightElement = object.getRightElement();
 		String rightValue = (String)rightElement.eGet(object.getAttribute());
 
 		// TODO: This workaround hard-codes the leftValue -> rightValue text in the
 		//       semantic model. Hence it is always displayed in this way.
 		//       Problem: The decorator is resposible for defining how to display
 		//       the differences and thus this should be also delegated to it
 		leftElement.eSet(object.getAttribute(), leftValue + " -> " + rightValue);
 		EObject leftChangedElement = EcoreUtil.copy(leftElement);
 
 		// annotate merged model
 		View mergedNotation = leftSemantic2notationMap.get(leftElement);
 		annotateNotation(mergedNotation, Constants.STYLE_STATE_VALUE_CHANGED);
 		// annotate split models
 		View splitnotation = (View)merge2StaticNotationMap.get(mergedNotation);
 		annotateNotation(splitnotation, Constants.STYLE_STATE_VALUE_CHANGED);
 		splitnotation = rightSemantic2notationMap.get(rightElement);
 		annotateNotation(splitnotation, Constants.STYLE_STATE_VALUE_CHANGED);
 		
 		return object;
 	}
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see org.eclipse.emf.compare.diff.metamodel.util.DiffSwitch#caseUpdateAttribute(org.eclipse.emf.compare.diff.metamodel.UpdateAttribute)
 	 */
 //	@Override
 //	public Object caseUpdateAttribute(UpdateAttribute object) {
 //		return object;
 //	}
 	
 	/**
 	 * Set the given annotation (one of the predefined
 	 * string constants Constants.STYLE_STATE_...)
 	 * to the given notation element
 	 * @param element
 	 * @param annotation
 	 */
 	protected void annotateNotation(View element, String annotation) {
 		PropertiesSetStyle propset = NotationFactory.eINSTANCE.createPropertiesSetStyle();
 		propset.setName(Constants.STYLE_ID);
 		propset.createProperty(Constants.STYLE_PROPERTY_STATE_ID, annotation);
 		element.getStyles().add(propset);
 	}
 	
 	/**
 	 * Copy the given notation element from the right to the left model
 	 * @param rightNotationElement
 	 */
 	protected View notationElementRightToLeft(View rightNotationElement) {
 		View copyRightNotationElem = right2leftNotationMap.get(rightNotationElement);
 		if(copyRightNotationElem != null)
 			return copyRightNotationElem;
 		
 		View rightNotationParentElem = (View)rightNotationElement.eContainer();
 		View leftNotationParentElem = getLeftNotation(rightNotationParentElem);
 		if(leftNotationParentElem == null)
 			// TODO: Resolve inconsistency between notation and sematic model
 			return null;
 		
 		copyRightNotationElem = (View)EcoreUtil.copy(rightNotationElement);
 		
 		if(rightNotationElement instanceof Edge)
 		{
 			((Edge)copyRightNotationElem).setSource(((Edge)rightNotationElement).getSource());
 			((Edge)copyRightNotationElem).setTarget(((Edge)rightNotationElement).getTarget());
 		}
 		EStructuralFeature feature = rightNotationElement.eContainingFeature();
 		if (feature.isMany()) {
 			((List<? super EObject>)leftNotationParentElem.eGet(feature)).add(0,copyRightNotationElem);
 		} else
 			leftNotationParentElem.eSet(feature, copyRightNotationElem);
 		right2leftNotationMap.put(rightNotationElement, copyRightNotationElem);
 		return copyRightNotationElem;
 	}
 	
 	/**
 	 * Get the left notation element, which corresponds to
 	 * the given right notation element. Take care of the View Type when trying to
 	 * find matching elements
 	 * Copy the element from the right if it does not exist yet.
 	 * 
 	 * @param rightNotation the notation element to which the left counterpart has to be found
 	 */
 	protected View getLeftNotation(View rightNotation) {
 		// recursion - go to the parent until a matching node is found 
 		View leftNotation = right2leftNotationMap.get(rightNotation);
 		if(leftNotation != null)
 			return leftNotation;
 		View leftNotationParent = getLeftNotation((View)rightNotation.eContainer()); 
 		if(leftNotationParent == null)
 			return null; // nothing found - this should not happen
 
 		// search for a element of correct type below the parent
 		String viewType = rightNotation.getType();
 		for (Object eObject : leftNotationParent.getChildren()) {
 			if (eObject instanceof View) {
 				if(viewType == ((View)eObject).getType()) {
 					leftNotation = (View)eObject;
 					right2leftNotationMap.put(rightNotation, leftNotation);
 					break;
 				}
 			}
 		}
 		// if nothing has been found, copy the element from the right
 		if(leftNotation == null)
 			leftNotation = notationElementRightToLeft(rightNotation);
 		
 		return leftNotation;
 	}
 	
 	/**
 	 * Make all references to right elements point to the 
 	 * corresponding left elements (By now notation elements only)
 	 * This is needet when at the time an element was copied from right
 	 * to left not all referenced elements were available
 	 * on the left side, yet.
 	 * 
 	 * @param element
 	 */
 	protected void remapElementRightToLeft(EObject element) {
 		// edges - remap source and target
 		if (element instanceof Edge) {
 			Edge edgeElement = (Edge)element;
 			View remapped;
 			remapped = right2leftNotationMap.get(edgeElement.getSource());
 			if(remapped != null)
 				edgeElement.setSource(remapped);
 			remapped = right2leftNotationMap.get(edgeElement.getTarget());
 			if(remapped != null)
 				edgeElement.setTarget(remapped);
 		}
 		// nodes - remap referenced edges
 		else if (element instanceof Node) {
 			Node nodeElement = (Node)element;
 			LinkedList<Edge> sourceEdgeList = new LinkedList<Edge>();
 			LinkedList<Edge> targetEdgeList = new LinkedList<Edge>();
 			EList sourceEdges = nodeElement.getSourceEdges();
 			EList targetEdges = nodeElement.getTargetEdges();
 			for (Object elem : sourceEdges) {
 				if(elem instanceof Edge)
 					sourceEdgeList.add((Edge)elem);
 			}
 			for (Object elem : targetEdges) {
 				if(elem instanceof Edge)
 					targetEdgeList.add((Edge)elem);
 			}
 			for (Edge edge : sourceEdgeList) {
 				View remapped = right2leftNotationMap.get(edge);
 				if(remapped != null) {
 					sourceEdges.remove(edge);
 					sourceEdges.add(remapped);
 				}
 			}
 			for (Edge edge : targetEdgeList) {
 				View remapped = right2leftNotationMap.get(edge);
 				if(remapped != null) {
 					targetEdges.remove(edge);
 					targetEdges.add(remapped);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * if we have notation and semantic in a single file there are diff elements,
 	 * which belong to notation elements - remove them
 	 * TODO: rewrite this to use the remove method of the iterator
 	 * 
 	 * @param diff
 	 */
 	public static void removeNotationDiffElements(DiffModel diff) {
 		LinkedList<DiffElement> diffList = new LinkedList<DiffElement>();		
 		for(TreeIterator<EObject> iter = diff.eAllContents(); iter.hasNext();) {
 			EObject element = iter.next();
 			if(element instanceof DiffElement)
 				diffList.add((DiffElement)element);
 		}
 			
 		for (DiffElement element : diffList) {
 			// TODO: make this for all possible element types in the diffModel
 			if (element instanceof ModelElementChangeRightTarget) {
 				ModelElementChangeRightTarget diffElem = (ModelElementChangeRightTarget)element;
 				if (diffElem.getLeftParent() instanceof View || diffElem.getRightElement() instanceof View)
 					EcoreUtil.delete(diffElem);
 			} else if (element instanceof ModelElementChangeLeftTarget) {
 				ModelElementChangeLeftTarget diffElem = (ModelElementChangeLeftTarget)element;
 				if(diffElem.getLeftElement() instanceof View || diffElem.getRightParent() instanceof View)
 					EcoreUtil.delete(diffElem);
 			} else if (element instanceof UpdateModelElement) {
 				UpdateModelElement diffElem = (UpdateModelElement)element;
 				if(diffElem.getLeftElement() instanceof View || diffElem.getRightElement() instanceof View)
 					EcoreUtil.delete(diffElem);
 			}
 		}
 	}
 }
