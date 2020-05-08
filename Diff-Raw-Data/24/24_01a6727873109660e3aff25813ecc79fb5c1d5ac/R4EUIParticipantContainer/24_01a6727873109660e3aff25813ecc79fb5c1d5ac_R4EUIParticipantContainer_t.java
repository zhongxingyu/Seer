 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, explicitThisUsage
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
  * This class implements the Participant Container element of the UI model
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.frame.core.model.ReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IParticipantInputDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.R4EUIDialogFactory;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorContentProvider;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.preferences.PreferenceConstants;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EUIParticipantContainer extends R4EUIModelElement {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fSelectionContainerFile. (value is ""icons/obj16/partcont_obj.png"")
 	 */
 	public static final String PARTICIPANT_CONTAINER_ICON_FILE = "icons/obj16/partcont_obj.png";
 
 	/**
 	 * Field NEW_CHILD_ELEMENT_COMMAND_NAME. (value is ""Add a New Participant"")
 	 */
 	private static final String NEW_CHILD_ELEMENT_COMMAND_NAME = "New Participants";
 
 	/**
 	 * Field NEW_CHILD_ELEMENT_COMMAND_TOOLTIP. (value is ""Add a New Participant to the Current Review"")
 	 */
 	private static final String NEW_CHILD_ELEMENT_COMMAND_TOOLTIP = "Add a New Participant to the Current Review";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fSelections.
 	 */
 	private final List<R4EUIParticipant> fParticipants;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EUIParticipantContainer.
 	 * 
 	 * @param aParent
 	 *            IR4EUIModelElement
 	 * @param aName
 	 *            String
 	 */
 	public R4EUIParticipantContainer(IR4EUIModelElement aParent, String aName) {
 		super(aParent, aName);
 		fReadOnly = aParent.isReadOnly();
 		fParticipants = new ArrayList<R4EUIParticipant>();
 		setImage(PARTICIPANT_CONTAINER_ICON_FILE);
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	//Attributes
 
 	/**
 	 * Create a serialization model element object
 	 * 
 	 * @return the new serialization element object
 	 */
 	@Override
 	public List<ReviewComponent> createChildModelDataElement() {
 		//Get Participants from user and set them in model data
 		List<ReviewComponent> tempParticipants = new ArrayList<ReviewComponent>();
 		R4EUIModelController.setJobInProgress(true);
 		final IParticipantInputDialog dialog = R4EUIDialogFactory.getInstance().getParticipantInputDialog(true);
 		final int result = dialog.open();
 		if (result == Window.OK) {
 			for (R4EParticipant participant : dialog.getParticipants()) {
 				tempParticipants.add(participant);
 			}
 		}
 		R4EUIDialogFactory.getInstance().removeParticipantInputDialog();
 		R4EUIModelController.setJobInProgress(false); //Enable view
 		return tempParticipants;
 	}
 
 	//Hierarchy
 
 	/**
 	 * Method getChildren.
 	 * 
 	 * @return IR4EUIModelElement[]
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getChildren()
 	 */
 	@Override
 	public IR4EUIModelElement[] getChildren() {
 		return fParticipants.toArray(new R4EUIParticipant[fParticipants.size()]);
 	}
 
 	/**
 	 * Method getSelectionList.
 	 * 
 	 * @return List<R4EUIParticipant>
 	 */
 	public List<R4EUIParticipant> getParticipantList() {
 		return fParticipants;
 	}
 
 	/**
 	 * Method getSelectionList.
 	 * 
 	 * @param aParticipant
 	 *            R4EParticipant
 	 * @return R4EUIParticipant
 	 */
 	public R4EUIParticipant getParticipant(R4EParticipant aParticipant) {
 		for (R4EUIParticipant participant : fParticipants) {
 			if (participant.getParticipant().equals(aParticipant)) {
 				return participant;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Method hasChildren.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#hasChildren()
 	 */
 	@Override
 	public boolean hasChildren() {
 		if (fParticipants.size() > 0) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Close the model element (i.e. disable it)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#close()
 	 */
 	@Override
 	public void close() {
 		//Remove all children references
 		R4EUIParticipant participant = null;
 		final int participantsSize = fParticipants.size();
 		for (int i = 0; i < participantsSize; i++) {
 
 			participant = fParticipants.get(i);
 			participant.close();
 		}
 		fParticipants.clear();
 		fOpen = false;
 		removeListeners();
 	}
 
 	/**
 	 * Method open.
 	 */
 	@Override
 	public void open() {
 		final List<R4EParticipant> participants = ((R4EUIReviewBasic) getParent()).getParticipants();
 		if (null != participants) {
 			final int participantsSize = participants.size();
 			for (int i = 0; i < participantsSize; i++) {
 				if (participants.get(i).isEnabled()
 						|| R4EUIPlugin.getDefault()
 								.getPreferenceStore()
 								.getBoolean(PreferenceConstants.P_SHOW_DISABLED)) {
 					addChildren(new R4EUIParticipant(this, participants.get(i),
 							((R4EUIReviewBasic) getParent()).getReview().getType()));
 				}
 			}
 		}
 		fOpen = true;
 	}
 
 	/**
 	 * Method isEnabled.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isEnabled()
 	 */
 	@Override
 	public boolean isEnabled() {
 		return getParent().isEnabled();
 	}
 
 	/**
 	 * Method setReadOnly.
 	 * 
 	 * @param boolean
 	 */
 	public void setReadOnly(boolean aReadOnly) {
 		fReadOnly = aReadOnly;
 	}
 
 	/**
 	 * Method addChildren.
 	 * 
 	 * @param aChildToAdd
 	 *            IR4EUIModelElement
 	 */
 	@Override
 	public void addChildren(IR4EUIModelElement aChildToAdd) {
 		fParticipants.add((R4EUIParticipant) aChildToAdd);
 		((R4EUIParticipant) aChildToAdd).setParticipantDetails();
 		aChildToAdd.addListener((ReviewNavigatorContentProvider) R4EUIModelController.getNavigatorView()
 				.getTreeViewer()
 				.getContentProvider());
 		fireAdd(aChildToAdd);
 	}
 
 	/**
 	 * Method createChildren
 	 * 
 	 * @param aModelComponent
 	 *            - the serialization model component object
 	 * @return IR4EUIModelElement
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#createChildren(ReviewNavigatorContentProvider)
 	 */
 	@Override
 	public IR4EUIModelElement createChildren(ReviewComponent aModelComponent) throws ResourceHandlingException,
 			OutOfSyncException {
 		final R4EParticipant participant = R4EUIModelController.FModelExt.createR4EParticipant(
 				((R4EUIReviewBasic) getParent()).getReview(), ((R4EParticipant) aModelComponent).getId(),
 				((R4EParticipant) aModelComponent).getRoles());
 		final R4EUIParticipant addedChild = new R4EUIParticipant(this, participant,
 				((R4EUIReviewBasic) getParent()).getReview().getType());
 		addedChild.setModelData(aModelComponent);
 		addChildren(addedChild);
 		return addedChild;
 	}
 
 	/**
 	 * Method removeChildren.
 	 * 
 	 * @param aChildToRemove
 	 *            IR4EUIModelElement
 	 * @param aFileRemove
 	 *            - also remove from file (hard remove)
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#removeChildren(IR4EUIModelElement)
 	 */
 	@Override
 	public void removeChildren(IR4EUIModelElement aChildToRemove, boolean aFileRemove)
 			throws ResourceHandlingException, OutOfSyncException {
 
 		final R4EUIParticipant removedElement = fParticipants.get(fParticipants.indexOf(aChildToRemove));
 		/* TODO uncomment when core model supports hard-removing of elements
 		if (aFileRemove) removedElement.getParticipant().remove());
 		else */
 		final R4EParticipant modelParticipant = removedElement.getParticipant();
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelParticipant,
 				R4EUIModelController.getReviewer());
 		modelParticipant.setEnabled(false);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 		//Remove element from UI if the show disabled element option is off
 		if (!(R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 			fParticipants.remove(removedElement);
 			aChildToRemove.removeListeners();
 			fireRemove(aChildToRemove);
 		} else {
 			R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 		}
 	}
 
 	/**
 	 * Method removeAllChildren.
 	 * 
 	 * @param aFileRemove
 	 *            boolean
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#removeAllChildren(boolean)
 	 */
 	@Override
 	public void removeAllChildren(boolean aFileRemove) throws ResourceHandlingException, OutOfSyncException {
 		//Recursively remove all children
 		for (R4EUIParticipant participant : fParticipants) {
 			removeChildren(participant, aFileRemove);
 		}
 	}
 
 	//Listeners
 
 	/**
 	 * Method addListener.
 	 * 
 	 * @param aProvider
 	 *            ReviewNavigatorContentProvider
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#addListener(ReviewNavigatorContentProvider)
 	 */
 	@Override
 	public void addListener(ReviewNavigatorContentProvider aProvider) {
 		super.addListener(aProvider);
 		if (null != fParticipants) {
 			R4EUIParticipant element = null;
 			for (final Iterator<R4EUIParticipant> iterator = fParticipants.iterator(); iterator.hasNext();) {
 				element = iterator.next();
 				element.addListener(aProvider);
 			}
 		}
 	}
 
 	/**
 	 * Method removeListener.
 	 * 
 	 * @param aProvider
 	 *            ReviewNavigatorContentProvider
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#removeListener()
 	 */
 	@Override
 	public void removeListener(ReviewNavigatorContentProvider aProvider) {
 		super.removeListener(aProvider);
 		if (null != fParticipants) {
 			R4EUIParticipant element = null;
 			for (final Iterator<R4EUIParticipant> iterator = fParticipants.iterator(); iterator.hasNext();) {
 				element = iterator.next();
 				element.removeListener(aProvider);
 			}
 		}
 	}
 
 	//Commands
 
 	/**
 	 * Method isAddChildElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isNewChildElementCmd()
 	 */
 	@Override
 	public boolean isNewChildElementCmd() {
		if (!isReadOnly() && getParent().isEnabled()) {
			final R4EReviewPhase phase = ((R4EReviewState) R4EUIModelController.getActiveReview()
 					.getReview()
					.getState()).getState();
			if (!phase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Method getAddChildElementCmdName.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getNewChildElementCmdName()
 	 */
 	@Override
 	public String getNewChildElementCmdName() {
 		return NEW_CHILD_ELEMENT_COMMAND_NAME;
 	}
 
 	/**
 	 * Method getAddChildElementCmdTooltip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getNewChildElementCmdTooltip()
 	 */
 	@Override
 	public String getNewChildElementCmdTooltip() {
 		return NEW_CHILD_ELEMENT_COMMAND_TOOLTIP;
 	}
 
 	/**
 	 * Method isShowPropertiesCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isShowPropertiesCmd()
 	 */
 	@Override
 	public boolean isShowPropertiesCmd() {
 		return false;
 	}
 }
