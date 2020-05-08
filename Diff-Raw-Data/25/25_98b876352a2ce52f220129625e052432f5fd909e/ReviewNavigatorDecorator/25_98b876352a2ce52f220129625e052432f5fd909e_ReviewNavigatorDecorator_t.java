 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, useForLoop, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010, 2012 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements a label decorator for the Review Navigator View.  
  * It is used to modify the label text and icons based on various criteria
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.navigator;
 
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.IColorDecorator;
 import org.eclipse.jface.viewers.IFontDecorator;
 import org.eclipse.jface.viewers.ILabelDecorator;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyExtended;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.OverlayImageIcon;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class ReviewNavigatorDecorator implements ILabelDecorator, IFontDecorator, IColorDecorator {
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method addListener.
 	 * 
 	 * @param listener
 	 *            ILabelProviderListener
 	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
 	 */
 	public void addListener(ILabelProviderListener listener) { // $codepro.audit.disable emptyMethod
 	}
 
 	/**
 	 * Method dispose.
 	 * 
 	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
 	 */
 	public void dispose() { // $codepro.audit.disable emptyMethod
 	}
 
 	/**
 	 * Method isLabelProperty.
 	 * 
 	 * @param element
 	 *            Object
 	 * @param property
 	 *            String
 	 * @return boolean
 	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(Object, String)
 	 */
 	public boolean isLabelProperty(Object element, String property) {
 		return false;
 	}
 
 	/**
 	 * Method removeListener.
 	 * 
 	 * @param listener
 	 *            ILabelProviderListener
 	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
 	 */
 	public void removeListener(ILabelProviderListener listener) { // $codepro.audit.disable emptyMethod
 	}
 
 	/**
 	 * Method decorateImage.
 	 * 
 	 * @param aBaseImage
 	 *            Image
 	 * @param aElement
 	 *            Object
 	 * @return Image
 	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(Image, Object)
 	 */
 	public Image decorateImage(Image aBaseImage, Object aElement) { // $codepro.audit.disable
 
 		//If the image is not already loaded, do it here
 		Image currentOverlayImage;
 		if (null != aBaseImage) {
 			currentOverlayImage = aBaseImage;
 		} else {
 			currentOverlayImage = ((IR4EUIModelElement) aElement).getImage(((IR4EUIModelElement) aElement).getImageLocation());
 		}
 
 		OverlayImageIcon overlayIcon = null;
 		//Disabled element decorator
 		if (!((IR4EUIModelElement) aElement).isEnabled()) {
 			overlayIcon = new OverlayImageIcon(currentOverlayImage, ((IR4EUIModelElement) aElement).getDisabledImage(),
 					OverlayImageIcon.BOTTOM_RIGHT);
			currentOverlayImage = overlayIcon.getImage();
			return currentOverlayImage; //No need to check for other decorators
 		} else if (((IR4EUIModelElement) aElement).isUserReviewed()
 				|| (aElement instanceof R4EUIAnomalyExtended && ((R4EUIAnomalyExtended) aElement).isTerminalState())) {
 			//Completed element decorator
 			overlayIcon = new OverlayImageIcon(currentOverlayImage,
 					((IR4EUIModelElement) aElement).getUserReviewedImage(), OverlayImageIcon.BOTTOM_RIGHT);
			currentOverlayImage = overlayIcon.getImage();
 		}
 
 		//Added, Removed or Modified file
 		if (aElement instanceof R4EUIFileContext) {
 			if (null == ((R4EUIFileContext) aElement).getBaseFileVersion()
 					&& null != ((R4EUIFileContext) aElement).getTargetFileVersion()) {
 				//Only target present, file was added
 				overlayIcon = new OverlayImageIcon(currentOverlayImage, ((R4EUIFileContext) aElement).getAddedImage(),
 						OverlayImageIcon.BOTTOM_LEFT);
				currentOverlayImage = overlayIcon.getImage();
 			} else if (null != ((R4EUIFileContext) aElement).getBaseFileVersion()
 					&& null == ((R4EUIFileContext) aElement).getTargetFileVersion()) {
 				//Only base present, file was removed
 				overlayIcon = new OverlayImageIcon(currentOverlayImage,
 						((R4EUIFileContext) aElement).getRemovedImage(), OverlayImageIcon.BOTTOM_LEFT);
				currentOverlayImage = overlayIcon.getImage();
 			} //else modified file
 		}
 
 		//Read-Only
 		if (((IR4EUIModelElement) aElement).isReadOnly()) {
 			overlayIcon = new OverlayImageIcon(currentOverlayImage, ((IR4EUIModelElement) aElement).getReadOnlyImage(),
 					OverlayImageIcon.TOP_RIGHT);
			currentOverlayImage = overlayIcon.getImage();
 		}
 
 		//Due date passed
 		if (((IR4EUIModelElement) aElement).isDueDatePassed()) {
 			overlayIcon = new OverlayImageIcon(currentOverlayImage,
 					((IR4EUIModelElement) aElement).getDueDatePassedImage(), OverlayImageIcon.TOP_LEFT);
			currentOverlayImage = overlayIcon.getImage();
 		}
		return currentOverlayImage;
 	}
 
 	/**
 	 * Method decorateText.
 	 * 
 	 * @param aText
 	 *            String
 	 * @param aElement
 	 *            Object
 	 * @return String
 	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(String, Object)
 	 */
 	public String decorateText(String aText, Object aElement) {
 		if (aElement instanceof R4EUIFileContext) {
 			if (!CommandUtils.useWorkspaceResource(((R4EUIFileContext) aElement).getTargetFileVersion())) {
 				//File is not in sync with file present in workspace
 				return "> " + aText;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Method decorateFont.
 	 * 
 	 * @param aElement
 	 *            Object
 	 * @return Font
 	 * @see org.eclipse.jface.viewers.IFontDecorator#decorateFont(Object)
 	 */
 	public Font decorateFont(Object aElement) { // $codepro.audit.disable
 		if (null != R4EUIModelController.getActiveReview() && R4EUIModelController.getActiveReview().equals(aElement)) {
 			return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
 		}
 		if (isMyReview((IR4EUIModelElement) aElement)) {
 			return JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
 		}
 		return null;
 	}
 
 	/**
 	 * Verifies whether this element in a Review element and, if so, if we are part of it
 	 * 
 	 * @param aElement
 	 *            - the model element
 	 * @return - true if this is a review and we are part of it, false otherwise
 	 */
 	private boolean isMyReview(IR4EUIModelElement aElement) {
 
 		IR4EUIModelElement currentElement = aElement;
 		while (null != currentElement) {
 			if (currentElement instanceof R4EUIReviewBasic) {
 				if (((R4EUIReviewBasic) currentElement).isParticipant(R4EUIModelController.getReviewer())) {
 					return true;
 				}
 			}
 			currentElement = currentElement.getParent();
 		}
 		return false;
 	}
 
 	/**
 	 * Method decorateBackground
 	 * 
 	 * @param aElement
 	 *            Object
 	 * @return Color
 	 */
 	public Color decorateBackground(Object aElement) {
 		return null;
 	}
 
 	/**
 	 * Method decorateForeground
 	 * 
 	 * @param aElement
 	 *            Object
 	 * @return Color
 	 */
 	public Color decorateForeground(Object aElement) {
 		if (aElement instanceof R4EUIReviewBasic) {
 			if (((R4EReviewState) ((R4EUIReviewBasic) aElement).getReview().getState()).getState().equals(
 					R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 				return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
 			}
 		}
 
 		if (aElement instanceof IR4EUIModelElement) {
 			if (((IR4EUIModelElement) aElement).isDueDatePassed()) {
 				return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);
 			} else if (((IR4EUIModelElement) aElement).isReadOnly()) {
 				return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
 			}
 		}
 		return null;
 	}
 }
