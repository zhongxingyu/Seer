 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, com.instantiations.assist.eclipse.analysis.mutabilityOfArrays
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the Review Item element of the UI model
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.model;
 
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EContextType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileContext;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EItem;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.RFSRegistryFactory;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.ReviewsFileStorageException;
 import org.eclipse.mylyn.reviews.r4e.core.utils.ResourceUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.properties.general.ReviewItemProperties;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.team.core.history.IFileRevision;
 import org.eclipse.ui.views.properties.IPropertySource;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EUIReviewItem extends R4EUIFileContainer {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fReviewItemFile. (value is ""icons/obj16/revitm_obj.gif"")
 	 */
 	public static final String REVIEW_ITEM_ICON_FILE = "icons/obj16/revitm_obj.gif";
 
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_NAME. (value is ""Delete Review Item"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_NAME = "Disable Review Item";
 
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_TOOLTIP. (value is ""Remove this review item from its parent review"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_TOOLTIP = "Disable (and Optionally Remove) this Review "
 			+ "Item from its Parent Review";
 
 	/**
 	 * Field RESTORE_ELEMENT_COMMAND_NAME. (value is ""Restore Review Item"")
 	 */
 	private static final String RESTORE_ELEMENT_COMMAND_NAME = "Restore Review Item";
 
 	/**
 	 * Field RESTORE_ELEMENT_ACTION_TOOLTIP. (value is ""Restore this disabled Review Item"")
 	 */
 	private static final String RESTORE_ELEMENT_COMMAND_TOOLTIP = "Restore this disabled Review Item";
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EUIReviewItem.
 	 * 
 	 * @param aParent
 	 *            IR4EUIModelElement
 	 * @param aItem
 	 *            R4EItem
 	 * @param aName
 	 *            String
 	 * @param aTooltip
 	 *            String
 	 */
 	public R4EUIReviewItem(IR4EUIModelElement aParent, R4EItem aItem, String aName) {
 		super(aParent, aItem, aName);
 		setImage(REVIEW_ITEM_ICON_FILE);

		//Remove check on parent, since at least one children is not set anymore
		try {
			getParent().setUserReviewed(false);
		} catch (OutOfSyncException e) {
			R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
			R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
		} catch (ResourceHandlingException e) {
			R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
			R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
		}
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method getAdapter.
 	 * 
 	 * @param adapter
 	 *            Class
 	 * @return Object
 	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
 	 */
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes")
 	Class adapter) {
 		if (IR4EUIModelElement.class.equals(adapter)) {
 			return this;
 		}
 		if (IPropertySource.class.equals(adapter)) {
 			return new ReviewItemProperties(this);
 		}
 		return null;
 	}
 
 	//Attributes
 
 	/**
 	 * Method setReviewed.
 	 * 
 	 * @param aReviewed
 	 *            boolean
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#setUserReviewed(boolean)
 	 */
 	@Override
 	public void setUserReviewed(boolean aReviewed) throws ResourceHandlingException, OutOfSyncException {
 		fUserReviewed = aReviewed;
 		if (fUserReviewed) {
 			//Also set the children
 			final int length = fFileContexts.size();
 			for (int i = 0; i < length; i++) {
 				fFileContexts.get(i).setChildUserReviewed(aReviewed);
 			}
 
 			//Check to see if we should mark the parent reviewed as well
 			getParent().checkToSetUserReviewed();
 		} else {
 			//Remove check on parent, since at least one children is not set anymore
 			getParent().setUserReviewed(fUserReviewed);
 		}
 		fireUserReviewStateChanged(this, R4EUIConstants.CHANGE_TYPE_REVIEWED_STATE);
 	}
 
 	/**
 	 * Method checkToSetReviewed.
 	 * 
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#checkToSetUserReviewed()
 	 */
 	@Override
 	public void checkToSetUserReviewed() throws ResourceHandlingException, OutOfSyncException {
 		boolean allChildrenReviewed = true;
 		final int length = fFileContexts.size();
 		for (int i = 0; i < length; i++) {
 			if (!(fFileContexts.get(i).isUserReviewed())) {
 				allChildrenReviewed = false;
 			}
 		}
 		//If all children are reviewed, mark the parent as reviewed as well
 		if (allChildrenReviewed) {
 			fUserReviewed = true;
 			getParent().checkToSetUserReviewed();
 			fireUserReviewStateChanged(this, R4EUIConstants.CHANGE_TYPE_REVIEWED_STATE);
 		}
 	}
 
 	//Hierarchy
 
 	/**
 	 * Method createReviewItem
 	 * 
 	 * @param aBaseTempFileVersion
 	 *            R4EFileVersion
 	 * @param aTargetTempFileVersion
 	 *            R4EFileVersion
 	 * @param aType
 	 *            R4EContextType
 	 * @return R4EUIFileContext
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 */
 	public R4EUIFileContext createFileContext(R4EFileVersion aBaseTempFileVersion,
 			R4EFileVersion aTargetTempFileVersion, R4EContextType aType) throws ResourceHandlingException,
 			OutOfSyncException {
 
 		IRFSRegistry revRegistry = null;
 		try {
 			revRegistry = RFSRegistryFactory.getRegistry(((R4EUIReviewBasic) this.getParent()).getReview());
 		} catch (ReviewsFileStorageException e1) {
 			R4EUIPlugin.Ftracer.traceInfo("Exception: " + e1.toString() + " (" + e1.getMessage() + ")");
 		}
 
 		final R4EFileContext fileContext = R4EUIModelController.FModelExt.createR4EFileContext(fItem);
 		fileContext.setType(aType);
 
 		//Get Base version from Version control system and set core model data
 		if (null != aBaseTempFileVersion) {
 			final R4EFileVersion rfileBaseVersion = R4EUIModelController.FModelExt.createR4EBaseFileVersion(fileContext);
 			Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(rfileBaseVersion,
 					R4EUIModelController.getReviewer());
 			CommandUtils.copyFileVersionData(rfileBaseVersion, aBaseTempFileVersion);
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 			//Add IFileRevision info
 			if (null != revRegistry) {
 				try {
 					final IFileRevision fileRev = revRegistry.getIFileRevision(null, rfileBaseVersion);
 					rfileBaseVersion.setFileRevision(fileRev);
 				} catch (ReviewsFileStorageException e) {
 					R4EUIPlugin.Ftracer.traceInfo("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			//Add ProjectURI to the review item
 			//TODO:  temporary solution.  We need to have an Iproject member varaible in R4EFileVersion
 			if (null != rfileBaseVersion.getResource()) {
 				bookNum = R4EUIModelController.FResourceUpdater.checkOut(fItem, R4EUIModelController.getReviewer());
 				final String projPlatformURI = ResourceUtils.toPlatformURIStr(rfileBaseVersion.getResource()
 						.getProject());
 				if (!fItem.getProjectURIs().contains(projPlatformURI)) {
 					fItem.getProjectURIs().add(projPlatformURI);
 				}
 				R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 			}
 		}
 
 		//Get Target version from Version control system and set core model data
 		if (null != aTargetTempFileVersion) {
 			final R4EFileVersion rfileTargetVersion = R4EUIModelController.FModelExt.createR4ETargetFileVersion(fileContext);
 			Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(rfileTargetVersion,
 					R4EUIModelController.getReviewer());
 			CommandUtils.copyFileVersionData(rfileTargetVersion, aTargetTempFileVersion);
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 			//Add IFileRevision info
 			if (null != revRegistry) {
 				try {
 					final IFileRevision fileRev = revRegistry.getIFileRevision(null, rfileTargetVersion);
 					rfileTargetVersion.setFileRevision(fileRev);
 				} catch (ReviewsFileStorageException e) {
 					R4EUIPlugin.Ftracer.traceInfo("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			//Add ProjectURI to the review item
 			if (null != rfileTargetVersion.getResource()) {
 				bookNum = R4EUIModelController.FResourceUpdater.checkOut(fItem, R4EUIModelController.getReviewer());
 				final String projPlatformURI = ResourceUtils.toPlatformURIStr(rfileTargetVersion.getResource()
 						.getProject());
 				if (!fItem.getProjectURIs().contains(projPlatformURI)) {
 					fItem.getProjectURIs().add(projPlatformURI);
 				}
 				R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 			}
 		}
 
 		final R4EUIFileContext uiFile = new R4EUIFileContext(this, fileContext);
 		addChildren(uiFile);
 		return uiFile;
 	}
 
 	//Commands
 
 	/**
 	 * Method isChangeReviewStateCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isChangeUserReviewStateCmd()
 	 */
 	@Override
 	public boolean isChangeUserReviewStateCmd() {
 		if (isEnabled()
 				&& !(((R4EReviewState) ((R4EUIReviewBasic) getParent()).getReview().getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED))) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method getRemoveElementCmdName.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getRemoveElementCmdName()
 	 */
 	@Override
 	public String getRemoveElementCmdName() {
 		return REMOVE_ELEMENT_COMMAND_NAME;
 	}
 
 	/**
 	 * Method getRemoveElementCmdTooltip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getRemoveElementCmdTooltip()
 	 */
 	@Override
 	public String getRemoveElementCmdTooltip() {
 		return REMOVE_ELEMENT_COMMAND_TOOLTIP;
 	}
 
 	/**
 	 * Method getRestoreElementCmdName.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getRestoreElementCmdName()
 	 */
 	@Override
 	public String getRestoreElementCmdName() {
 		return RESTORE_ELEMENT_COMMAND_NAME;
 	}
 
 	/**
 	 * Method getRestoreElementCmdTooltip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getRestoreElementCmdTooltip()
 	 */
 	@Override
 	public String getRestoreElementCmdTooltip() {
 		return RESTORE_ELEMENT_COMMAND_TOOLTIP;
 	}
 
 	/**
 	 * Method isSendEmailCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isSendEmailCmd()
 	 */
 	@Override
 	public boolean isSendEmailCmd() {
 		if (isEnabled()) {
 			return true;
 		}
 		return false;
 	}
 }
