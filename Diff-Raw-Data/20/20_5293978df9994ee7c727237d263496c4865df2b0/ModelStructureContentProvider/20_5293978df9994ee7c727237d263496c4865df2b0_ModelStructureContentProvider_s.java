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
 package org.eclipse.emf.compare.ui.contentprovider;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.IStreamContentAccessor;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.compare.ResourceNode;
 import org.eclipse.compare.structuremergeviewer.ICompareInput;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.compare.diff.generic.DiffMaker;
 import org.eclipse.emf.compare.diff.metamodel.DiffFactory;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.diff.metamodel.ModelInputSnapshot;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.match.service.MatchService;
 import org.eclipse.emf.compare.ui.EMFCompareException;
 import org.eclipse.emf.compare.ui.util.EMFCompareConstants;
 import org.eclipse.emf.compare.util.ModelUtils;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * Structure viewer used by the
  * {@link org.eclipse.emf.compare.ui.structuremergeviewer.ModelStructureMergeViewer ModelStructureMergeViewer}.
  * Assumes that its input is a {@link DiffModel}.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public class ModelStructureContentProvider implements ITreeContentProvider {
 	private CompareConfiguration configuration;
 
 	private EObject leftModel;
 
 	private EObject rightModel;
 
 	private ModelInputSnapshot snapshot;
 
 	private DiffModel diffInput;
 
 	/**
 	 * Instantiates a content provider given the {@link CompareConfiguration} to use.
 	 * 
 	 * @param compareConfiguration
 	 *            {@link CompareConfiguration} used for this comparison.
 	 */
 	public ModelStructureContentProvider(CompareConfiguration compareConfiguration) {
 		configuration = compareConfiguration;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ITreeContentProvider#getChildren(Object)
 	 */
 	public Object[] getChildren(Object parentElement) {
 		Object[] children = null;
 		if (parentElement instanceof EObject) {
 			children = ((EObject)parentElement).eContents().toArray();
 		}
 		return children;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ITreeContentProvider#getParent(Object)
 	 */
 	public Object getParent(Object element) {
 		Object parent = null;
 		if (element instanceof EObject) {
 			parent = ((EObject)element).eContainer();
 		}
 		return parent;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ITreeContentProvider#hasChildren(Object)
 	 */
 	public boolean hasChildren(Object element) {
 		boolean hasChildren = false;
 		if (element instanceof EObject) {
 			hasChildren = !((EObject)element).eContents().isEmpty();
 		}
 		return hasChildren;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
 	 */
 	public Object[] getElements(Object inputElement) {
 		Object[] elements = null;
 		if (inputElement instanceof DiffModel) {
 			elements = ((DiffModel)inputElement).getOwnedElements().toArray();
 		} else {
			elements = diffInput.getOwnedElements().toArray();
 		}
 		return elements;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	 */
 	public void dispose() {
 		// Nothing needs to be disposed of here.
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
 	 */
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		((TreeViewer)viewer).getTree().clearAll(true);
 		if (newInput instanceof ICompareInput) {
 			final ITypedElement left = ((ICompareInput)newInput).getLeft();
 			final ITypedElement right = ((ICompareInput)newInput).getRight();
 
 			try {
 				final ResourceSet modelResourceSet = new ResourceSetImpl();
 				if (left instanceof ResourceNode && right instanceof ResourceNode) {
 					leftModel = ModelUtils.load(((ResourceNode)left).getResource().getFullPath(),
 							modelResourceSet);
 					rightModel = ModelUtils.load(((ResourceNode)right).getResource().getFullPath(),
 							modelResourceSet);
 				} else if (left instanceof IStreamContentAccessor && right instanceof IStreamContentAccessor) {
 					// this is the case of SVN/CVS comparison, we invert left and right.
 					rightModel = ModelUtils.load(((IStreamContentAccessor)left).getContents(),
 							left.getName(), modelResourceSet);
 					leftModel = ModelUtils.load(((IStreamContentAccessor)right).getContents(), right
 							.getName(), modelResourceSet);
 					final String leftLabel = configuration.getRightLabel(rightModel);
 					configuration.setRightLabel(configuration.getLeftLabel(leftModel));
 					configuration.setLeftLabel(leftLabel);
 					configuration.setProperty(EMFCompareConstants.PROPERTY_LEFT_IS_REMOTE, true);
 				}
 				if (leftModel != null && rightModel != null) {
 					final Date start = Calendar.getInstance().getTime();
 					PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
 							new IRunnableWithProgress() {
 								public void run(IProgressMonitor monitor) throws InvocationTargetException,
 										InterruptedException {
 									final MatchModel match = new MatchService().doMatch(leftModel,
 											rightModel, monitor);
 									final DiffModel diff = new DiffMaker().doDiff(match);
 
 									snapshot = DiffFactory.eINSTANCE.createModelInputSnapshot();
 									snapshot.setDiff(diff);
 									snapshot.setMatch(match);
 								}
 							});
 					final Date end = Calendar.getInstance().getTime();
					configuration.setProperty(EMFCompareConstants.PROPERTY_COMPARISON_TIME, end.getTime() - start.getTime());
					
 					// prints comparison time
//					System.out.println(EMFCompareEObjectUtils.computeObjectName(leftModel) + 
//							" and " + EMFCompareEObjectUtils.computeObjectName(rightModel) + //$NON-NLS-1$
//							" compared in : " + (end.getTime() - start.getTime()) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
 
 					diffInput = snapshot.getDiff();
 				}
 			} catch (IOException e) {
 				throw new EMFCompareException(e.getMessage());
 			} catch (CoreException e) {
 				throw new EMFCompareException(e.getMessage());
 			} catch (InterruptedException e) {
 				throw new EMFCompareException(e.getMessage());
 			} catch (InvocationTargetException e) {
 				throw new EMFCompareException(e.getMessage());
 			}
 		} else if (newInput instanceof ModelInputSnapshot) {
 			snapshot = (ModelInputSnapshot)newInput;
 			diffInput = snapshot.getDiff();
 		}
 	}
 
 	/**
 	 * Returns this content provider's input.
 	 * 
 	 * @return This content provider's input.
 	 */
 	public ModelInputSnapshot getSnapshot() {
 		return snapshot;
 	}
 }
