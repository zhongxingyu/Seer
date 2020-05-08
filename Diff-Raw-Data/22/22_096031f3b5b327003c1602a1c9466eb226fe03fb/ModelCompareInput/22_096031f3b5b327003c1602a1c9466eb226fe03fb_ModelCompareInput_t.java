 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ui;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.compare.structuremergeviewer.ICompareInput;
 import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.match.metamodel.Match2Elements;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.merge.api.AbstractMerger;
 import org.eclipse.emf.compare.merge.api.MergeFactory;
 import org.eclipse.emf.compare.ui.util.EMFCompareConstants;
 import org.eclipse.emf.compare.ui.util.EMFCompareEObjectUtils;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * Input to be used for a 2 or 3-way comparison in a
  * {@link org.eclipse.emf.compare.ui.contentmergeviewer.ModelContentMergeViewer ModelContentMergeViewer}.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public class ModelCompareInput implements ICompareInput {
 	private final List<ICompareInputChangeListener> inputChangeListeners = new ArrayList<ICompareInputChangeListener>();
 
 	private DiffModel diff;
 
 	private MatchModel match;
 
 	/**
 	 * Creates a CompareInput given the resulting {@link org.eclipse.emf.compare.match.MatchModel match} and
 	 * {@link org.eclipse.emf.compare.diff.DiffModel diff} of the comparison.
 	 * 
 	 * @param matchModel
 	 *            {@link org.eclipse.emf.compare.match.MatchModel match} of the comparison.
 	 * @param diffModel
 	 *            {@link org.eclipse.emf.compare.diff.DiffModel diff} of the comparison.
 	 */
 	public ModelCompareInput(MatchModel matchModel, DiffModel diffModel) {
 		match = matchModel;
 		diff = diffModel;
 	}
 
 	/**
 	 * Returns this ModelCompareInput's DiffModel.
 	 * 
 	 * @return This ModelCompareInput's DiffModel.
 	 */
 	public DiffModel getDiff() {
 		return diff;
 	}
 
 	/**
 	 * Returns this ModelCompareInput's MatchModel.
 	 * 
 	 * @return This ModelCompareInput's MatchModel.
 	 */
 	public MatchModel getMatch() {
 		return match;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ICompareInput#addCompareInputChangeListener(ICompareInputChangeListener)
 	 */
 	public void addCompareInputChangeListener(ICompareInputChangeListener listener) {
 		inputChangeListeners.add(listener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ICompareInput#removeCompareInputChangeListener(ICompareInputChangeListener)
 	 */
 	public void removeCompareInputChangeListener(ICompareInputChangeListener listener) {
 		inputChangeListeners.remove(listener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ICompareInput#copy(boolean)
 	 */
 	public void copy(boolean leftToRight) {
 		for (final DiffElement aDiff : getDiffAsList()) {
			final AbstractMerger merger = MergeFactory.createMerger(aDiff);
 			if (leftToRight && merger.canUndoInTarget()) {
 				merger.undoInTarget();
 			} else if (!leftToRight && merger.canApplyInOrigin()) {
 				merger.applyInOrigin();
 			}
 		}
 		fireCompareInputChanged();
 	}
 
 	/**
 	 * Returns the {@link DiffElement} of the input {@link DiffModel} as a list. Doesn't take
 	 * {@link DiffGroup}s into account.
 	 * 
 	 * @return The {@link DiffElement} of the input {@link DiffModel} as a list.
 	 */
 	public List<DiffElement> getDiffAsList() {
 		final List<DiffElement> diffs = new LinkedList<DiffElement>();
 		for (final TreeIterator iterator = getDiff().eAllContents(); iterator.hasNext(); ) {
 			final DiffElement aDiff = (DiffElement)iterator.next();
 			if (!(aDiff instanceof DiffGroup))
 				diffs.add(aDiff);
 		}
 		return diffs;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ICompareInput#getAncestor()
 	 */
 	public ITypedElement getAncestor() {
 		// TODO LGT three way support
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ICompareInput#getImage()
 	 */
 	public Image getImage() {
 		Image image = null;
 
 		if (getMatch() != null)
 			image = EMFCompareEObjectUtils.computeObjectImage(getMatch());
 		else if (getDiff() != null)
 			image = EMFCompareEObjectUtils.computeObjectImage(getDiff());
 
 		return image;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ICompareInput#getName()
 	 */
 	public String getName() {
 		String name = null;
 
 		if (getMatch() != null)
 			name = EMFCompareEObjectUtils.computeObjectName(getMatch());
 		else if (getDiff() != null)
 			name = EMFCompareEObjectUtils.computeObjectName(getDiff());
 
 		return name;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ICompareInput#getKind()
 	 */
 	public int getKind() {
 		return EMFCompareConstants.NO_CHANGE;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ICompareInput#getLeft()
 	 */
 	public ITypedElement getLeft() {
 		ITypedElement element = null;
 
 		if (getMatch().getMatchedElements().get(0) instanceof Match2Elements)
 			element = new TypedElementWrapper(((Match2Elements)getMatch().getMatchedElements().get(0))
 					.getLeftElement());
 
 		return element;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ICompareInput#getRight()
 	 */
 	public ITypedElement getRight() {
 		ITypedElement element = null;
 
 		if (getMatch().getMatchedElements().get(0) instanceof Match2Elements)
 			element = new TypedElementWrapper(((Match2Elements)getMatch().getMatchedElements().get(0))
 					.getRightElement());
 
 		return element;
 	}
 
 	/**
 	 * Fetches the {@link DiffElement diff} associated to the given {@link Match2Elements match}.
 	 * 
 	 * @param aMatch
 	 *            Match element for which we seek the diff.
 	 * @return the {@link DiffElement diff} associated to the given {@link Match2Elements match}.
 	 */
 	public DiffElement findDiffFromMatch(Match2Elements aMatch) {
 		DiffElement result = null;
 		final EObject leftMatch = aMatch.getLeftElement();
 		final EObject rightMatch = aMatch.getRightElement();
 
 		for (final DiffElement target : getDiffAsList()) {
 			final EObject leftDiff = EMFCompareEObjectUtils.getLeftElement(target);
 			final EObject rightDiff = EMFCompareEObjectUtils.getRightElement(target);
 
 			if ((leftDiff != null && leftDiff.equals(leftMatch))
 					|| (rightDiff != null && rightDiff.equals(rightMatch))) {
 				result = target;
 			}
 		}
 		return result;
 	}
 
 	protected void fireCompareInputChanged() {
 		for (ICompareInputChangeListener listener : inputChangeListeners) {
 			listener.compareInputChanged(this);
 		}
 	}
 }
