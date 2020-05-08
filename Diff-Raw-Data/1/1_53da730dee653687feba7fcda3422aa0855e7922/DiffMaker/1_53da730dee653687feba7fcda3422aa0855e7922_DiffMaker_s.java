 package org.eclipse.emf.compare.diff.generic;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.diff.AddModelElement;
 import org.eclipse.emf.compare.diff.AddReferenceValue;
 import org.eclipse.emf.compare.diff.DiffFactory;
 import org.eclipse.emf.compare.diff.DiffModel;
 import org.eclipse.emf.compare.diff.MoveModelElement;
 import org.eclipse.emf.compare.diff.RemoveModelElement;
 import org.eclipse.emf.compare.diff.RemoveReferenceValue;
 import org.eclipse.emf.compare.diff.UpdateAttribute;
 import org.eclipse.emf.compare.diff.api.DiffEngine;
 import org.eclipse.emf.compare.match.Match2Elements;
 import org.eclipse.emf.compare.match.MatchModel;
 import org.eclipse.emf.compare.match.UnMatchElement;
 import org.eclipse.emf.compare.util.EFactory;
 import org.eclipse.emf.compare.util.FactoryException;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.resource.Resource;
 
 /**
  * This class is usefull when one want's to determine a diff from a matching
  * model
  * 
  * @author Cedric Brun <cedric.brun@obeo.fr>
  * 
  */
 public class DiffMaker implements DiffEngine {
 	/**
 	 * This hasmap is usefull to find the Match from any EObject instance
 	 */
 	private HashMap EObjectToMatch = new HashMap();
 
 	/**
 	 * Fill the EObjectToMatch hashmap to retrieve matchings from left or right
 	 * EObject
 	 * 
 	 */
 	private void updateEObjectToMatch(MatchModel match) {
 		Iterator rootElemIt = match.getMatchedElements().iterator();
 		while (rootElemIt.hasNext()) {
 			Match2Elements matchRoot = (Match2Elements) rootElemIt.next();
 			EObjectToMatch.put(matchRoot.getLeftElement(), matchRoot);
 			EObjectToMatch.put(matchRoot.getRightElement(), matchRoot);
 			TreeIterator matchElemIt = matchRoot.eAllContents();
 			while (matchElemIt.hasNext()) {
 				Match2Elements matchElem = (Match2Elements) matchElemIt.next();
 				EObjectToMatch.put(matchElem.getLeftElement(), matchElem);
 				EObjectToMatch.put(matchElem.getRightElement(), matchElem);
 			}
 		}
 
 	}
 
 	/**
 	 * Return the matched EObject from the one given.
 	 * 
 	 * @param from
 	 *            the original EObject
 	 * @return the matched EObject
 	 */
 	private EObject getMatchedEObject(EObject from) {
 		Match2Elements matchElem = (Match2Elements) EObjectToMatch.get(from);
 		if (matchElem == null)
 			return null;
 		if (from == matchElem.getLeftElement())
 			return matchElem.getRightElement();
 		return matchElem.getLeftElement();
 
 	}
 
 	/**
 	 * Return a diffmodel created using the match model. This implementation is
 	 * a generic and simple one.
 	 * 
 	 * @param match
 	 *            the matching model
 	 * @return the corresponding diff model
 	 * @throws FactoryException
 	 */
 	public DiffModel doDiff(MatchModel match) {
 
 		updateEObjectToMatch(match);
 
 		DiffModel result = DiffFactory.eINSTANCE.createDiffModel();
 		// we have to visit browse the model and create the corresponding
 		// operations
 		Resource leftModel = ((Match2Elements) match.getMatchedElements()
 				.get(0)).getLeftElement().eResource();
 		Resource rightModel = ((Match2Elements) match.getMatchedElements().get(
 				0)).getRightElement().eResource();
 		// iterate over the unmached elements end determine if they has been
 		// added or removed.
 		Iterator unMatched = match.getUnMatchedElements().iterator();
 		while (unMatched.hasNext()) {
 
 			UnMatchElement unMatchElement = (UnMatchElement) unMatched.next();
 			if (unMatchElement.getElement().eResource() == leftModel) {
 				// add remove model element
 				RemoveModelElement operation = DiffFactory.eINSTANCE
 						.createRemoveModelElement();
 				operation.setLeftElement(unMatchElement.getElement());
 				operation.setRightParent(getMatchedEObject(unMatchElement
 						.getElement().eContainer()));
 				result.getOwnedElements().add(operation);
 			}
 			if (unMatchElement.getElement().eResource() == rightModel) {
 				// add remove model element
 				AddModelElement operation = DiffFactory.eINSTANCE
 						.createAddModelElement();
 				operation.setRightElement(unMatchElement.getElement());
 				// TODOCBR check for the following case..
 				// if (unMatchElement.getElement().eContainer() != null
 				// &&
 				// getMatchedEObject(unMatchElement.getElement().eContainer())!=
 				// null )
 				EObject addedElement = unMatchElement.getElement();
 				EObject parent = addedElement.eContainer();
 				EObject targetParent = getMatchedEObject(parent);
 
 				operation.setLeftParent(targetParent);
 				result.getOwnedElements().add(operation);
 			}
 
 		}
 		// browsing the match model
 		TreeIterator it = ((EObject) match.getMatchedElements().get(0))
 				.eAllContents();
 		while (it.hasNext()) {
 			Match2Elements matchElement = (Match2Elements) it.next();
 			try {
 				checkAttributesUpdates(result, matchElement);
 				checkReferencesUpdates(result, matchElement);
 				checkForMove(result, matchElement);
 			} catch (FactoryException e) {
 				EMFComparePlugin.getDefault().log(e,false);
 			}
 			// checkForModelElementDiff(result, matchElement);
 		}
 		return result;// FIXME Do something
 	}
 
 	private void checkForMove(DiffModel result, Match2Elements matchElement) {
 		// TODOCBR check for moves in diffMaker
 		if (matchElement.getLeftElement().eContainer() != null
 				&& matchElement.getRightElement().eContainer() != null)
 			if (getMatchedEObject(matchElement.getLeftElement().eContainer()) != matchElement
 					.getRightElement().eContainer())
 			// if
 			// (!ETools.getURI(matchElement.getLeftElement()).equals(ETools.getURI(matchElement.getRightElement())))
 			{
				System.err.println("moved element");
 				MoveModelElement operation = DiffFactory.eINSTANCE
 						.createMoveModelElement();
 				operation.setRightElement(matchElement.getRightElement());
 				operation.setLeftElement(matchElement.getLeftElement());
 				operation.setLeftParent(matchElement.getLeftElement()
 						.eContainer());
 				operation.setRightParent(matchElement.getRightElement()
 						.eContainer());
 				result.getOwnedElements().add(operation);
 
 			}
 	}
 
 	/**
 	 * Check wether the attributes values have changed or not
 	 * 
 	 * @param log2
 	 * @param mapping
 	 * @throws FactoryException
 	 */
 	private void checkAttributesUpdates(DiffModel log2, Match2Elements mapping)
 			throws FactoryException {
 
 		EObject eclass = mapping.getLeftElement().eClass();
 
 		List eclassAttributes = new LinkedList();
 		if (eclass instanceof EClass)
 			eclassAttributes = ((EClass) eclass).getEAllAttributes();
 		// for each feature, compare the value
 		Iterator it = eclassAttributes.iterator();
 		while (it.hasNext()) {
 			EAttribute next = (EAttribute) it.next();
 			if (!next.isDerived()) {
 				String attributeName = next.getName();
 				if (EFactory.eGet(mapping.getLeftElement(), attributeName) != null)
 					if (!EFactory.eGet(mapping.getLeftElement(), attributeName)
 							.equals(
 									EFactory.eGet(mapping.getRightElement(),
 											attributeName))) {
 						UpdateAttribute operation = DiffFactory.eINSTANCE
 								.createUpdateAttribute();
 						operation.setRightElement(mapping.getRightElement());
 						operation.setLeftElement(mapping.getLeftElement());
 						operation.setAttribute(next);
 						log2.getOwnedElements().add(operation);
 					}
 			}
 		}
 	}
 
 	/**
 	 * Check wether the references values have changed or not
 	 * 
 	 * @param log2
 	 * @param mapping
 	 * @throws FactoryException
 	 */
 	private void checkReferencesUpdates(DiffModel log2, Match2Elements mapping)
 			throws FactoryException {
 
 		EObject eclass = mapping.getLeftElement().eClass();
 
 		List eclassReferences = new LinkedList();
 
 		if (eclass instanceof EClass)
 			eclassReferences = ((EClass) eclass).getEAllReferences();
 
 		// for each reference, compare the targets
 		boolean break_process = false;
 		Iterator it = eclassReferences.iterator();
 		while (it.hasNext()) {
 			EReference next = (EReference) it.next();
 			String referenceName = next.getName();
 			if (!next.isContainment() && !next.isDerived()
 					&& !next.isTransient()) {
 				List oldReferences = EFactory.eGetAsList(mapping
 						.getLeftElement(), referenceName);
 				List newReferences = EFactory.eGetAsList(mapping
 						.getRightElement(), referenceName);
 				List mappedOldReferences = new ArrayList();
 				List mappedNewReferences = new ArrayList();
 
 				if (oldReferences == null)
 					oldReferences = new ArrayList();
 				if (newReferences == null)
 					newReferences = new ArrayList();
 				// For each of the old reference
 				// if the linked element is not linked using the new references
 				// then a reference has been added
 				Iterator oldRef = oldReferences.iterator();
 				while (oldRef.hasNext()) {
 					Object curRef = oldRef.next();
 					if (curRef != null) {
 						EObject curMapping = getMatchedEObject((EObject) curRef);
 						if (curMapping == null) {
 							break_process = true;
 						}
 						mappedOldReferences.add(curMapping);
 					}
 				}
 				Iterator newRef = newReferences.iterator();
 				while (newRef.hasNext()) {
 					Object curRef = newRef.next();
 					if (curRef != null) {
 						EObject curMapping = getMatchedEObject((EObject) curRef);
 						if (curMapping == null) {
 							break_process = true;
 						}
 						mappedNewReferences.add(curMapping);
 					}
 				}
 				// new References is now added references
 				newReferences.removeAll(mappedOldReferences);
 				// old References is now removed references
 				oldReferences.removeAll(mappedNewReferences);
 				if (newReferences.size() + oldReferences.size() != 0) {
 					AddReferenceValue operation = DiffFactory.eINSTANCE
 							.createAddReferenceValue();
 					operation.setLeftElement(mapping.getLeftElement());
 					operation.setRightElement(mapping.getRightElement());
 					operation.setReference(next);
 					newRef = newReferences.iterator();
 					while (newRef.hasNext()) {
 						Object eobj = newRef.next();
 						operation.getRightAddedTarget().add((eobj));
 						if ((getMatchedEObject((EObject) eobj)) != null)
 							operation.getLeftAddedTarget().add(
 									(getMatchedEObject((EObject) eobj)));
 						// EFactory.eAdd(operation, "referencesOrigins",
 						// );
 					}
 					if (newReferences.size() > 0 && !break_process)
 						log2.getOwnedElements().add(operation);
 					// Remove references
 					RemoveReferenceValue deloperation = DiffFactory.eINSTANCE
 							.createRemoveReferenceValue();
 					deloperation.setRightElement((mapping.getRightElement()));
 					deloperation.setLeftElement(mapping.getLeftElement());
 					deloperation.setReference(next);
 					oldRef = oldReferences.iterator();
 					while (oldRef.hasNext()) {
 						Object eobj = oldRef.next();
 						// TODOCBR check that and fix
 						if ((getMatchedEObject((EObject) eobj)) != null) {
 							deloperation.getLeftRemovedTarget().add((eobj));
 							deloperation.getRightRemovedTarget().add(
 									getMatchedEObject((EObject) eobj));
 						}
 						// EFactory.eAdd(deloperation, "referencesTargets",
 						// ((Match2Elements)
 						// getMatchedEObject((EObject)eobj)).getRightElement());
 						// EFactory.eAdd(deloperation, "referencesOrigins",
 						// eobj);
 					}
 					if (oldReferences.size() > 0 && !break_process)
 						log2.getOwnedElements().add(deloperation);
 
 				}
 			}
 		}
 
 	}
 
 }
