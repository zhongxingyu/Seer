 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.text;
 
 import static org.eclipse.emf.compare.ide.utils.ResourceUtil.saveAllResources;
 
 import com.google.common.collect.ImmutableMap;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
 import org.eclipse.compare.structuremergeviewer.ICompareInput;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.rcp.ui.internal.EMFCompareConstants;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class EMFCompareTextMergeViewerContentProvider implements IMergeViewerContentProvider {
 
 	public static final char ANCESTOR_CONTRIBUTOR = 'A';
 
 	public static final char RIGHT_CONTRIBUTOR = 'R';
 
 	public static final char LEFT_CONTRIBUTOR = 'L';
 
 	private CompareConfiguration fCompareConfiguration;
 
 	private String fAncestorError;
 
 	private String fLeftError;
 
 	private String fRightError;
 
 	public EMFCompareTextMergeViewerContentProvider(CompareConfiguration cc) {
 		fCompareConfiguration = cc;
 	}
 
 	private boolean hasError() {
 		return fAncestorError != null || fLeftError != null || fRightError != null;
 	}
 
 	public void dispose() {
 		// empty default implementation
 	}
 
 	public void inputChanged(Viewer v, Object o1, Object o2) {
 		// we are not interested since we have no state
 	}
 
 	// ---- ancestor
 
 	public void setAncestorError(String errorMessage) {
 		fAncestorError = errorMessage;
 	}
 
 	public String getAncestorLabel(Object element) {
 		if (fAncestorError != null) {
 			return fAncestorError;
 		}
 		return fCompareConfiguration.getAncestorLabel(element);
 	}
 
 	public Image getAncestorImage(Object element) {
 		if (fAncestorError != null) {
 			return null;
 		}
 		return fCompareConfiguration.getAncestorImage(element);
 	}
 
 	public Object getAncestorContent(Object element) {
 		if (element instanceof ICompareInput) {
 			ITypedElement ancestor = ((ICompareInput)element).getAncestor();
 			return ancestor;
 		}
 		return null;
 	}
 
 	public boolean showAncestor(Object element) {
 		if (element instanceof ICompareInput) {
 			return true; // fix for #45239: Show ancestor for incoming and outgoing changes
 		}
 		// return (((ICompareInput)element).getKind() & Differencer.DIRECTION_MASK) ==
 		// Differencer.CONFLICTING;
 		return false;
 	}
 
 	// ---- left
 
 	public void setLeftError(String errorMessage) {
 		fLeftError = errorMessage;
 	}
 
 	public String getLeftLabel(Object element) {
 		if (fLeftError != null) {
 			return fLeftError;
 		}
 		return fCompareConfiguration.getLeftLabel(element);
 	}
 
 	public Image getLeftImage(Object element) {
 		if (fLeftError != null) {
 			return null;
 		}
 		return fCompareConfiguration.getLeftImage(element);
 	}
 
 	public Object getLeftContent(Object element) {
 		if (element instanceof ICompareInput) {
 			ITypedElement left = ((ICompareInput)element).getLeft();
 			return left;
 		}
 		return null;
 	}
 
 	public boolean isLeftEditable(Object element) {
 		if (hasError()) {
 			return false;
 		}
 		return fCompareConfiguration.isLeftEditable();
 	}
 
 	public void saveLeftContent(Object element, byte[] bytes) {
 		EList<Match> matches = getComparison().getMatches();
 		EObject leftEObject = null;
 		for (Match match : matches) {
 			leftEObject = match.getLeft();
 			if (leftEObject != null) {
 				break;
 			}
 		}
 		if (leftEObject != null) {
 			Resource eResource = leftEObject.eResource();
 			if (eResource != null) {
 				ResourceSet resourceSet = eResource.getResourceSet();
 				saveAllResources(resourceSet, ImmutableMap.of(Resource.OPTION_SAVE_ONLY_IF_CHANGED,
 						Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER));
 			}
 		}
 	}
 
 	// ---- right
 
 	public void setRightError(String errorMessage) {
 		fRightError = errorMessage;
 	}
 
 	public String getRightLabel(Object element) {
 		if (fRightError != null) {
 			return fRightError;
 		}
 		return fCompareConfiguration.getRightLabel(element);
 	}
 
 	public Image getRightImage(Object element) {
 		if (fRightError != null) {
 			return null;
 		}
 		return fCompareConfiguration.getRightImage(element);
 	}
 
 	public Object getRightContent(Object element) {
 		if (element instanceof ICompareInput) {
 			ITypedElement right = ((ICompareInput)element).getRight();
 			return right;
 		}
 		return null;
 	}
 
 	public boolean isRightEditable(Object element) {
 		if (hasError()) {
 			return false;
 		}
 		return fCompareConfiguration.isRightEditable();
 	}
 
 	public void saveRightContent(Object element, byte[] bytes) {
 		EList<Match> matches = getComparison().getMatches();
		EObject leftEObject = null;
 		for (Match match : matches) {
			leftEObject = match.getLeft();
			if (leftEObject != null) {
 				break;
 			}
 		}
		if (leftEObject != null) {
			Resource eResource = leftEObject.eResource();
 			if (eResource != null) {
 				ResourceSet resourceSet = eResource.getResourceSet();
 				saveAllResources(resourceSet, ImmutableMap.of(Resource.OPTION_SAVE_ONLY_IF_CHANGED,
 						Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER));
 			}
 		}
 	}
 
 	/**
 	 * Returns the comparison object.
 	 * 
 	 * @return the comparison.
 	 */
 	public Comparison getComparison() {
 		return (Comparison)fCompareConfiguration.getProperty(EMFCompareConstants.COMPARE_RESULT);
 	}
 }
