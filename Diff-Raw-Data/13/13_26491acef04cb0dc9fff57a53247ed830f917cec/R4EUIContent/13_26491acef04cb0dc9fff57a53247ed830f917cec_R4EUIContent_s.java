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
  * This class implements the Content element of the UI model
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.model;
 
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EDelta;
import org.eclipse.mylyn.reviews.r4e.core.model.R4EFormalReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EItem;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.properties.general.ContentsProperties;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.ui.views.properties.IPropertySource;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public abstract class R4EUIContent extends R4EUIModelElement {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_NAME. (value is ""Delete Content"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_NAME = "Disable Content";
 
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_TOOLTIP. (value is ""Disable (and Optionally Remove) this Content" +
 	 * " from its Parent Container"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_TOOLTIP = "Disable (and Optionally Remove) this Content"
 			+ " from its Parent Container";
 
 	/**
 	 * Field RESTORE_ELEMENT_COMMAND_NAME. (value is ""Restore Content"")
 	 */
 	private static final String RESTORE_ELEMENT_COMMAND_NAME = "Restore Content";
 
 	/**
 	 * Field RESTORE_ELEMENT_ACTION_TOOLTIP. (value is ""Restore this disabled Content"")
 	 */
 	private static final String RESTORE_ELEMENT_COMMAND_TOOLTIP = "Restore this disabled Content";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fDelta.
 	 */
 	private final R4EDelta fContent;
 
 	/**
 	 * Field fPosition.
 	 */
 	private final IR4EUIPosition fPosition;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EUIContent.
 	 * 
 	 * @param aParent
 	 *            IR4EUIModelElement
 	 * @param aDelta
 	 *            R4EDelta
 	 * @param aPosition
 	 *            IR4EUIPosition
 	 */
 	protected R4EUIContent(IR4EUIModelElement aParent, R4EDelta aDelta, IR4EUIPosition aPosition) {
 		super(aParent, aPosition.toString());
 		fContent = aDelta;
 		fPosition = aPosition;
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method getToolTip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getToolTip()
 	 */
 	@Override
 	public String getToolTip() {
 		return R4EUIConstants.AUTHOR_LABEL + ((R4EItem) fContent.eContainer().eContainer()).getAddedBy().getId();
 	}
 
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
 			return new ContentsProperties(this);
 		}
 		return null;
 	}
 
 	//Attributes
 
 	/**
 	 * Method getContent.
 	 * 
 	 * @return R4EDelta
 	 */
 	public R4EDelta getContent() {
 		return fContent;
 	}
 
 	/**
 	 * Method getPosition.
 	 * 
 	 * @return IR4EPosition
 	 */
 	public IR4EUIPosition getPosition() {
 		return fPosition;
 	}
 
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
 		if (fUserReviewed != aReviewed) { //Reviewed state is changed
 			fUserReviewed = aReviewed;
 			if (fUserReviewed) {
 				//Add delta to the reviewedContent for this user
 				addContentReviewed();
 
 				//Check to see if we should mark the parent reviewed as well
 				getParent().getParent().checkToSetUserReviewed();
 			} else {
 				//Remove delta from the reviewedContent for this user
 				removeContentReviewed();
 
 				//Remove check on parent, since at least one children is not set anymore
 				getParent().getParent().setUserReviewed(fUserReviewed);
 			}
 			fireUserReviewStateChanged(this, R4EUIConstants.CHANGE_TYPE_REVIEWED_STATE);
 		}
 	}
 
 	/**
 	 * Method setChildrenReviewed.
 	 * 
 	 * @param aReviewed
 	 *            boolean
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#setChildUserReviewed(boolean)
 	 */
 	@Override
 	public void setChildUserReviewed(boolean aReviewed) throws ResourceHandlingException, OutOfSyncException {
 		if (fUserReviewed != aReviewed) { //Reviewed state is changed
 			fUserReviewed = aReviewed;
 			if (aReviewed) {
 				//Add delta to the reviewedContent for this user
 				addContentReviewed();
 			} else {
 				//Remove delta from the reviewedContent for this user
 				removeContentReviewed();
 			}
 			fUserReviewed = aReviewed;
 			fireUserReviewStateChanged(this, R4EUIConstants.CHANGE_TYPE_REVIEWED_STATE);
 		}
 	}
 
 	/**
 	 * Method addContentReviewed.
 	 * 
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 */
 	private void addContentReviewed() throws ResourceHandlingException, OutOfSyncException {
 		//First get the current user
 		final R4EUIReviewBasic review = (R4EUIReviewBasic) getParent().getParent().getParent().getParent(); // $codepro.audit.disable methodChainLength
 		final R4EParticipant user = review.getParticipant(R4EUIModelController.getReviewer(), true);
 
 		//Add this content to the reviewed contents for this user
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(user, user.getId());
 		user.getReviewedContent().add(fContent.getId());
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 	}
 
 	/**
 	 * Method removeContentReviewed.
 	 * 
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 */
 	private void removeContentReviewed() throws ResourceHandlingException, OutOfSyncException {
 		//First get the current user
 		final R4EUIReviewBasic review = (R4EUIReviewBasic) getParent().getParent().getParent().getParent(); // $codepro.audit.disable methodChainLength
 		final R4EParticipant user = review.getParticipant(R4EUIModelController.getReviewer(), false);
 
 		if (null != user) {
 			//Remove this content from the reviewed contents for this user
 			final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(user, user.getId());
 			user.getReviewedContent().remove(fContent.getId());
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		}
 	}
 
 	/**
 	 * Method setEnabled.
 	 * 
 	 * @param aEnabled
 	 *            boolean
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#setUserReviewed(boolean)
 	 */
 	@Override
 	public void setEnabled(boolean aEnabled) throws ResourceHandlingException, OutOfSyncException {
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fContent,
 				R4EUIModelController.getReviewer());
 		fContent.setEnabled(true);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 	}
 
 	/**
 	 * Method isEnabled.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isEnabled()
 	 */
 	@Override
 	public boolean isEnabled() {
 		return fContent.isEnabled();
 	}
 
 	//Commands
 
 	/**
 	 * Method isAddLinkedAnomalyCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isAddLinkedAnomalyCmd()
 	 */
 	@Override
 	public boolean isAddLinkedAnomalyCmd() {
		//If this is a formal review, we need to be in the preparation or decision phase
		if (R4EUIModelController.getActiveReview().getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_FORMAL)) {
			final R4EReviewPhase phase = ((R4EFormalReview) R4EUIModelController.getActiveReview().getReview()).getCurrent()
					.getType();
			if (!phase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION)
					&& !phase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_DECISION)) {
				return false;
			}
		}
 		if (isEnabled()
 				&& !(((R4EReviewState) R4EUIModelController.getActiveReview().getReview().getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED))) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method isOpenEditorCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isOpenEditorCmd()
 	 */
 	@Override
 	public boolean isOpenEditorCmd() {
 		if (isEnabled() && null != ((R4EUIFileContext) getParent().getParent()).getTargetFileVersion()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method isChangeReviewStateCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isChangeUserReviewStateCmd()
 	 */
 	@Override
 	public boolean isChangeUserReviewStateCmd() {
 		if (isEnabled()
 				&& !(((R4EReviewState) R4EUIModelController.getActiveReview().getReview().getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED))) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method isRemoveElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isRemoveElementCmd()
 	 */
 	@Override
 	public boolean isRemoveElementCmd() {
 		if (isEnabled()
 				&& !(((R4EReviewState) R4EUIModelController.getActiveReview().getReview().getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED))) {
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
 	 * Method isRestoreElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#iisRestoreElementCmd()
 	 */
 	@Override
 	public boolean isRestoreElementCmd() {
 		if (!(getParent().getParent().isEnabled())) {
 			return false;
 		}
 		if (isEnabled()
 				|| ((R4EReviewState) R4EUIModelController.getActiveReview().getReview().getState()).getState().equals(
 						R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 			return false;
 		}
 		return true;
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
 		return true;
 	}
 }
