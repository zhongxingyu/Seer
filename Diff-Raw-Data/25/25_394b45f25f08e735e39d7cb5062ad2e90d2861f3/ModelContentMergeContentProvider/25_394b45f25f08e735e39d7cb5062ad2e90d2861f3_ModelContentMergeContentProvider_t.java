 /*******************************************************************************
  * Copyright (c) 2006, 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ui.viewer.content;
 
 import java.io.IOException;
 import java.util.Collections;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
 import org.eclipse.compare.structuremergeviewer.ICompareInput;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.ui.ModelCompareInput;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * Content provider for our {@link org.eclipse.compare.contentmergeviewer.ContentMergeViewer}.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public class ModelContentMergeContentProvider implements IMergeViewerContentProvider {
 	/**
 	 * {@link CompareConfiguration} controls various aspect of the GUI elements. This will keep track of the
 	 * one used to created this compare editor.
 	 */
 	private final CompareConfiguration configuration;
 
 	/**
 	 * Instantiates a content provider for our
 	 * {@link org.eclipse.compare.contentmergeviewer.ContentMergeViewer} given its
 	 * {@link CompareConfiguration}.
 	 * 
 	 * @param cc
 	 *            {@link CompareConfiguration} used by this
 	 *            {@link org.eclipse.compare.contentmergeviewer.ContentMergeViewer}.
 	 */
 	public ModelContentMergeContentProvider(CompareConfiguration cc) {
 		configuration = cc;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	 */
 	public void dispose() {
 		// Nothing needs disposal here
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#getAncestorContent(java.lang.Object)
 	 */
 	public Object getAncestorContent(Object element) {
 		Object content = null;
 		if (element instanceof ModelCompareInput)
 			content = ((ModelCompareInput)element).getAncestorResource();
 		else if (element instanceof ICompareInput)
 			content = ((ICompareInput)element).getAncestor();
 		return content;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#getAncestorImage(java.lang.Object)
 	 */
 	public Image getAncestorImage(Object element) {
 		return configuration.getAncestorImage(element);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#getAncestorLabel(java.lang.Object)
 	 */
 	public String getAncestorLabel(Object element) {
 		return configuration.getAncestorLabel(element);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#getLeftContent(java.lang.Object)
 	 */
 	public Object getLeftContent(Object element) {
 		Object content = null;
 		if (element instanceof ModelCompareInput) {
			content = ((ModelCompareInput)element).getLeftResource();
			// final Resource res = ((ModelCompareInput)element).getLeftResource();
			// if (res != null && res.getResourceSet() != null)
			// content = res.getResourceSet();
			// else
			// content = res;
 		} else if (element instanceof ICompareInput)
 			content = ((ICompareInput)element).getLeft();
 		return content;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#getLeftImage(java.lang.Object)
 	 */
 	public Image getLeftImage(Object element) {
 		return configuration.getLeftImage(element);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#getLeftLabel(java.lang.Object)
 	 */
 	public String getLeftLabel(Object element) {
 		return configuration.getLeftLabel(element);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#getRightContent(java.lang.Object)
 	 */
 	public Object getRightContent(Object element) {
 		Object content = null;
 		if (element instanceof ModelCompareInput) {
			content = ((ModelCompareInput)element).getRightResource();
			// final Resource res = ((ModelCompareInput)element).getRightResource();
			// if (res != null && res.getResourceSet() != null)
			// content = res.getResourceSet();
			// else
			// content = res;
 		} else if (element instanceof ICompareInput)
 			content = ((ICompareInput)element).getRight();
 		return content;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#getRightImage(java.lang.Object)
 	 */
 	public Image getRightImage(Object element) {
 		return configuration.getRightImage(element);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#getRightLabel(java.lang.Object)
 	 */
 	public String getRightLabel(Object element) {
 		return configuration.getRightLabel(element);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
 	 *      java.lang.Object, java.lang.Object)
 	 */
 	public void inputChanged(Viewer v, Object o1, Object o2) {
 		// we don't need this
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#isLeftEditable(java.lang.Object)
 	 */
 	public boolean isLeftEditable(Object element) {
 		return configuration.isLeftEditable();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#isRightEditable(java.lang.Object)
 	 */
 	public boolean isRightEditable(Object element) {
 		return configuration.isRightEditable();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#saveLeftContent(java.lang.Object,
 	 *      byte[])
 	 */
 	public void saveLeftContent(Object element, byte[] bytes) {
 		// FIXME save whole resource Set
 		// FIXME automatic saves
 		if (element instanceof ModelCompareInput) {
 			final ModelCompareInput input = (ModelCompareInput)element;
 			if (input.getLeftResource() != null) {
 				try {
 					input.getLeftResource().save(Collections.EMPTY_MAP);
 				} catch (IOException e) {
 					EMFComparePlugin.log(e.getMessage(), false);
 				}
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#saveRightContent(java.lang.Object,
 	 *      byte[])
 	 */
 	public void saveRightContent(Object element, byte[] bytes) {
 		// FIXME save whole resource Set
 		// FIXME automatic saves
 		if (element instanceof ModelCompareInput) {
 			final ModelCompareInput input = (ModelCompareInput)element;
 			if (input.getRightResource() != null) {
 				try {
 					input.getRightResource().save(Collections.EMPTY_MAP);
 				} catch (IOException e) {
 					EMFComparePlugin.log(e.getMessage(), false);
 				}
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#showAncestor(java.lang.Object)
 	 */
 	public boolean showAncestor(Object element) {
 		if (element instanceof ICompareInput)
 			return true;
 		return false;
 	}
 }
