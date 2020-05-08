 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, com.instantiations.assist.eclipse.analysis.instanceFieldSecurity, com.instantiations.assist.eclipse.analysis.mutabilityOfArrays
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
  * This class implements the Review element of the UI model
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.model;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.mylyn.reviews.frame.core.model.Item;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EDecision;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFormalReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EItem;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewDecision;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUserReviews;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUserRole;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.RModelFactoryExt;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.core.utils.ResourceUtils;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewVersionsException;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewsVersionsIF;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewsVersionsIF.CommitDescriptor;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewsVersionsIFFactory;
 import org.eclipse.mylyn.reviews.r4e.ui.Activator;
 import org.eclipse.mylyn.reviews.r4e.ui.navigator.ReviewNavigatorContentProvider;
 import org.eclipse.mylyn.reviews.r4e.ui.preferences.PreferenceConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.properties.general.ReviewBasicProperties;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.UIUtils;
 import org.eclipse.ui.views.properties.IPropertySource;
 
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EUIReviewBasic extends R4EUIModelElement {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Field REVIEW_ICON_FILE.
 	 * (value is ""icons/obj16/review_obj.gif"")
 	 */
 	private static final String REVIEW_ICON_FILE = "icons/obj16/review_obj.gif";
 	
 	/**
 	 * Field REVIEW_ICON_FILE.
 	 * (value is ""icons/obj16/revclsd_obj.gif"")
 	 */
 	private static final String REVIEW_CLOSED_ICON_FILE = "icons/obj16/revclsd_obj.gif";
 	
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_NAME.
 	 * (value is ""Delete Review"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_NAME = "Disable Review";
         
     /**
      * Field REMOVE_ELEMENT_ACTION_TOOLTIP.
      * (value is ""Remove this review from its parent review group"")
      */
     private static final String REMOVE_ELEMENT_COMMAND_TOOLTIP = "Disable (and Optionally Remove) this Review from " +
     		"its Parent Review Group";
 	
 	/**
 	 * Field REVIEW_PHASE_STARTED.
 	 * (value is ""PLANNING"")
 	 */
 	private static final String REVIEW_PHASE_STARTED = "STARTED";
 	
 	/**
 	 * Field REVIEW_PHASE_COMPLETED.
 	 * (value is ""COMPLETED"")
 	 */
 	protected static final String REVIEW_PHASE_COMPLETED = "COMPLETED";
 		
 	/**
 	 * Field FBasicPhaseValues.
 	 */
 	private static final String[] BASIC_PHASE_VALUES = { REVIEW_PHASE_STARTED, REVIEW_PHASE_COMPLETED };
 
 	/**
 	 * Field EXIT_DECISION_NONE.
 	 * (value is ""No Decision"")
 	 */
 	private static final String EXIT_DECISION_NONE = "No Decision";
 	/**
 	 * Field EXIT_DECISION_ACCEPTED.
 	 * (value is ""Accepted"")
 	 */
 	private static final String EXIT_DECISION_ACCEPTED = "Accepted";		
 	/**
 	 * Field EXIT_DECISION_ACCEPTED_FOLLOWUP.
 	 * (value is ""Accepted with Follow-up"")
 	 */
 	private static final String EXIT_DECISION_ACCEPTED_FOLLOWUP = "Accepted with Follow-up";
 	/**
 	 * Field EXIT_DECISION_REJECTED.
 	 * (value is ""Rejected"")
 	 */
 	private static final String EXIT_DECISION_REJECTED = "Rejected";
 	
 	/**
 	 * Field decisionValues.
 	 */
 	private static final String[] DECISION_VALUES = { EXIT_DECISION_NONE, EXIT_DECISION_ACCEPTED,
 		EXIT_DECISION_ACCEPTED_FOLLOWUP, EXIT_DECISION_REJECTED };  //NOTE: This has to match R4EDecision in R4E core plugin
 
 	
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
     
 	/**
 	 * Field fReview.
 	 */
 	protected R4EReview fReview;
 	
 	/**
 	 * Field fReviewName.
 	 */
 	protected final String fReviewName;
 	
 	/**
 	 * Field fItems.
 	 */
 	protected final List<R4EUIReviewItem> fItems;
 	
 	/**
 	 * Field fParticipantsContainer.
 	 */
 	protected R4EUIParticipantContainer fParticipantsContainer;
 	
 	/**
 	 * Field fAnomalyContainer.
 	 */
 	protected R4EUIAnomalyContainer fAnomalyContainer = null;
 	
 	
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Constructor for R4EUIReview.
 	 * @param aParent R4EUIReviewGroup
 	 * @param aReview R4EReview
 	 * @param aType R4EReviewType
 	 * @param aOpen boolean
 	 * @throws ResourceHandlingException
 	 */
 	//TODO have different icons for all review types
 	public R4EUIReviewBasic(R4EUIReviewGroup aParent, R4EReview aReview, R4EReviewType aType, boolean aOpen) throws ResourceHandlingException {
 		super(aParent, getReviewDisplayName(aReview.getName(), aType), aReview.getExtraNotes());
 		fReview = aReview;
 		fReviewName = aReview.getName();
 		fParticipantsContainer = new R4EUIParticipantContainer(this, R4EUIConstants.PARTICIPANTS_LABEL_NAME);
 		fAnomalyContainer = new R4EUIAnomalyContainer(this, R4EUIConstants.GLOBAL_ANOMALIES_LABEL_NAME);
 		fItems = new ArrayList<R4EUIReviewItem>();
 		if (aOpen) {
 			//Open the new review and make it the active one (close any other that is open)
 			setImage(REVIEW_ICON_FILE);
 			fOpen = true;
 			final List<R4EUserRole> role = new ArrayList<R4EUserRole>(1);
 			role.add(R4EUserRole.R4E_ROLE_LEAD);
 			final R4EParticipant participant = R4EUIModelController.FModelExt.createR4EParticipant(fReview, R4EUIModelController.getReviewer(), role);
 			fParticipantsContainer.addChildren(new R4EUIParticipant(fParticipantsContainer, participant));
 			final R4EUIReviewBasic activeReview = R4EUIModelController.getActiveReview();
 			if (null != activeReview ) activeReview.close();
 			R4EUIModelController.setActiveReview(this);
 		} else {
 			setImage(REVIEW_CLOSED_ICON_FILE);
 			fOpen = false;
 		}
 	}
 	
 	
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Method getAdapter.
 	 * @param adapter Class
 	 * @return Object
 	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
 	 */
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
 		if (IR4EUIModelElement.class.equals(adapter)) {
 			return this;
 		}
 		if (IPropertySource.class.equals(adapter)) {
 			return new ReviewBasicProperties(this);
 		}
 		return null;
 	}
 	
 	
 	//Attributes
 	
 	/**
 	 * Method getReviewDisplayName.
 	 * @param aName String
 	 * @param aType R4EReviewType
 	 * @return String
 	 */
 	private static String getReviewDisplayName(String aName, R4EReviewType aType) {
 		if (aType.equals(R4EReviewType.R4E_REVIEW_TYPE_INFORMAL)) {
 			return R4EUIConstants.REVIEW_TYPE_INFORMAL + ": " + aName;
 		} else if (aType.equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 			return R4EUIConstants.REVIEW_TYPE_BASIC + ": " + aName;		
 		} else {
 			//No change.  For formal review the name is set in the subclass
 			return aName;
 		}
 	}
 	
 	/**
 	 * Set serialization model data by copying it from the passed-in object
 	 * @param aModelComponent - a serialization model element to copy information from
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#setModelData(R4EReviewComponent)
 	 */
 	@Override
 	public void setModelData(R4EReviewComponent aModelComponent) throws ResourceHandlingException, OutOfSyncException {
     	//Set data in model element
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fReview, 
 				R4EUIModelController.getReviewer());
     	fReview.setExtraNotes(((R4EReview)aModelComponent).getExtraNotes());
     	fReview.setType(((R4EReview)aModelComponent).getType());
 
     	//Optional properties
     	fReview.setProject(((R4EReview)aModelComponent).getProject());
 		fReview.getComponents().addAll(((R4EReview)aModelComponent).getComponents());
 		fReview.setEntryCriteria(((R4EReview)aModelComponent).getEntryCriteria());
     	fReview.setObjectives(((R4EReview)aModelComponent).getObjectives());
     	fReview.setReferenceMaterial(((R4EReview)aModelComponent).getReferenceMaterial());
     	R4EUIModelController.FResourceUpdater.checkIn(bookNum);
     }
 	
 	/**
 	 * Method getReview.
 	 * @return R4EReview
 	 */
 	public R4EReview getReview() {
 		return fReview;
 	}
 	
 	/**
 	 * Add a new participant
 	 * @param aParticipant - the new particpant name/ID
 	 * @param aCreate boolean
 	 * @return R4EParticipant
 	 * @throws ResourceHandlingException
 	 */
 	public R4EParticipant getParticipant(String aParticipant, boolean aCreate) throws ResourceHandlingException {
 		R4EParticipant participant = null;
 		if (isParticipant(aParticipant)) {
 			participant = (R4EParticipant)fReview.getUsersMap().get(aParticipant);
 		} else {
 			if (aCreate) {
 				final List<R4EUserRole> role = new ArrayList<R4EUserRole>(1);
 				role.add(R4EUserRole.R4E_ROLE_REVIEWER);
 				participant = R4EUIModelController.FModelExt.createR4EParticipant(fReview, aParticipant, null);
 				fParticipantsContainer.addChildren(new R4EUIParticipant(fParticipantsContainer, participant));
 			}
 		}
 		return participant;
 	}
 	
 	/**
 	 * Checks whether the participant is in the participants list for this review
 	 * @param aParticipant - the participant to look for
 	 * @return true/false
 	 */
 	public boolean isParticipant(String aParticipant) {
 		if (null != fReview) {
 			if (isOpen()) {
 				if (null != ((R4EParticipant)fReview.getUsersMap().get(aParticipant))) {
 					return true;
 				}
 			} else {
 				final R4EUserReviews userReviews = ((R4EUIReviewGroup)getParent()).getReviewGroup().getUserReviews().get(aParticipant);
				if (null != userReviews && userReviews.getInvitedToMap().containsValue(fReview)) return true;
 			}
 		}
 		return false;
 	}
 	
 	
 	/**
 	 * Method getParticipants.
 	 * @return List<R4EParticipant>
 	 */
 	public List<R4EParticipant> getParticipants() {
 		final Object[] users = fReview.getUsersMap().values().toArray();
 		
 		//Cast list to R4EParticipants
 		final List<R4EParticipant> participants = new ArrayList<R4EParticipant>();
 		for (Object user : users) {
 			participants.add((R4EParticipant) user);
 		}
 		return participants;
 	}
 	
 	/**
 	 * Method getParticipantIDs.
 	 * @return List<String>
 	 */
 	public List<String> getParticipantIDs() {
 		final Object[] users = fReview.getUsersMap().values().toArray();
 		
 		//Cast list to R4EParticipants
 		final List<String> participantIDs = new ArrayList<String>();
 		for (Object user : users) {
 			participantIDs.add(((R4EParticipant)user).getId());
 		}
 		return participantIDs;
 	}
 	
 	/**
 	 * Method setReviewed.
 	 * @param aReviewed boolean
 	 * @throws ResourceHandlingException 
 	 * @throws OutOfSyncException 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#setReviewed(boolean)
 	 */
 	@Override
 	public void setReviewed(boolean aReviewed) throws ResourceHandlingException, OutOfSyncException { // $codepro.audit.disable emptyMethod, unnecessaryExceptions
 		final R4EParticipant participant = getParticipant(R4EUIModelController.getReviewer(), true);
 		if (null != participant) {
 			final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fReview, R4EUIModelController.getReviewer());
 			participant.setReviewCompleted(aReviewed);
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		}
 		fReviewed = aReviewed;
 		
 		if (fReviewed) {
 			//Also set the children
 			final int length = fItems.size();
 			for (int i = 0; i < length; i++) {
 				fItems.get(i).setReviewed(aReviewed);
 			}
 		}
 		fireReviewStateChanged(this);
 		R4EUIModelController.propertyChanged();
 	}
 	
 	/**
 	 * Close the model element (i.e. disable it)
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#close()
 	 */
 	@Override
 	public void close() {
 		//Remove all children references
 		R4EUIReviewItem reviewItem = null;
 		final int itemsSize = fItems.size();
 		for (int i = 0; i < itemsSize; i++) {
 			
 			reviewItem = fItems.get(i);
 			reviewItem.close();
 		}
 		fParticipantsContainer.close();
 		fAnomalyContainer.close();
 		
 		fItems.clear();
 		fOpen = false;
 		R4EUIModelController.FModelExt.closeR4EReview(fReview);   //Notify model
 		R4EUIModelController.clearAnomalyMap();
 		fImage = UIUtils.loadIcon(REVIEW_CLOSED_ICON_FILE);
 		fireReviewStateChanged(this);
 	}
 	
 	/**
 	 * Open the model element (i.e. enable it)
 	 * @throws ResourceHandlingException
 	 * @throws ReviewVersionsException 
 	 * @throws FileNotFoundException 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#open()
 	 */
 	@Override
 	public void open() throws ResourceHandlingException, ReviewVersionsException, FileNotFoundException {
 		fReview = R4EUIModelController.FModelExt.openR4EReview(((R4EUIReviewGroup)getParent()).getReviewGroup(), fReviewName);
 		
 		final EList<Item> items = fReview.getReviewItems();
 		if (null != items) {
 		
 			R4EUIReviewItem uiItem = null;
 			R4EUIModelController.mapAnomalies(fReview);
 			final int itemsSize = items.size();
 			R4EItem item = null;
 			for (int i = 0; i < itemsSize; i++) {
 				//TODO This is a temporary fix to be able to distinguish between various review item types
 				item = (R4EItem)items.get(i);
 				if (item.isEnabled() || Activator.getDefault().getPreferenceStore().
 						getBoolean(PreferenceConstants.P_SHOW_DISABLED)) {
 					if (null == item.getFileContextList().get(0).getBase()) {
 						//Assume resource
 						uiItem = new R4EUIReviewItem(this, item, R4EUIConstants.REVIEW_ITEM_TYPE_RESOURCE, item,
 								item.getFileContextList().get(0).getTarget().getName());
 					} else {
 						//commit
 						IProject project = ResourceUtils.toIProject(item.getProjectURIs().get(0));
 						ReviewsVersionsIF versionsIf = ReviewsVersionsIFFactory.instance.getVersionsIF(project);
 						CommitDescriptor descriptor = versionsIf.getCommitInfo(project, item.getRepositoryRef());
 						uiItem = new R4EUIReviewItem(this, item, R4EUIConstants.REVIEW_ITEM_TYPE_COMMIT, descriptor, null);
 					}
 
 					uiItem.open();
 					addChildren(uiItem);
 				}
 			}
 		}
 		
 		fAnomalyContainer.open();
 		fParticipantsContainer.open();
 		
 		fOpen = true;
 		fImage = UIUtils.loadIcon(REVIEW_ICON_FILE);
 		fireReviewStateChanged(this);
 	}
 	
 	/**
 	 * Method setEnabled.
 	 * @param aEnabled boolean
 	 * @throws ResourceHandlingException 
 	 * @throws OutOfSyncException 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#setReviewed(boolean)
 	 */
 	@Override
 	public void setEnabled(boolean aEnabled) throws ResourceHandlingException, OutOfSyncException {
 		fReview = R4EUIModelController.FModelExt.openR4EReview(((R4EUIReviewGroup) getParent()).getReviewGroup(),
 				fReviewName);
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fReview, R4EUIModelController.getReviewer());
 		fReview.setEnabled(aEnabled);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		R4EUIModelController.FModelExt.closeR4EReview(fReview);
 		R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 	}
 	
 	/**
 	 * Method isEnabled.
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#isEnabled()
 	 */
 	@Override
 	public boolean isEnabled() {
 		return fReview.isEnabled();
 	}
 	
 	//Hierarchy
 	
 	/**
 	 * Method hasChildren
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#hasChildren()
 	 */
 	@Override
 	public boolean hasChildren() {
 		if (isOpen()) {
 			if (fItems.size() > 0 || null != fAnomalyContainer || null != fParticipantsContainer) return true;
 		}
 	    return false;
 	}
 	
 	/**
 	 * Method getChildren.
 	 * @return IR4EUIModelElement[]
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#getChildren()
 	 */
 	@Override
 	public IR4EUIModelElement[] getChildren() {
 		final List<IR4EUIModelElement> newList = new ArrayList<IR4EUIModelElement>();
 		newList.addAll(fItems);
 		newList.add(fAnomalyContainer);
 		newList.add(fParticipantsContainer);
 		return newList.toArray(new IR4EUIModelElement[newList.size()]);
 	}
 	
 	/**
 	 * Method getReviewItems.
 	 * @return List<R4EUIReviewItem>
 	 */
 	public List<R4EUIReviewItem> getReviewItems() {
 		//Get review items only
 		final IR4EUIModelElement[] reviewChildren = getChildren();
 		final List<R4EUIReviewItem> reviewItems = new ArrayList<R4EUIReviewItem>();
 		for (IR4EUIModelElement child : reviewChildren) {
 			if (child instanceof R4EUIReviewItem) {
 				reviewItems.add((R4EUIReviewItem)child);
 			}
 		}
 		return reviewItems;
 	}
 	
 	/**
 	 * Method addChildren.
 	 * @param aChildToAdd IR4EUIModelElement
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#addChildren(IR4EUIModelElement)
 	 */
 	@Override
 	public void addChildren(IR4EUIModelElement aChildToAdd) {
 		if (aChildToAdd instanceof R4EUIReviewItem) {
 			fItems.add((R4EUIReviewItem) aChildToAdd);
 		} else if (aChildToAdd instanceof R4EUIAnomalyContainer) {
 			fAnomalyContainer = (R4EUIAnomalyContainer) aChildToAdd;
 		} else if (aChildToAdd instanceof R4EUIParticipantContainer) {
 			fParticipantsContainer = (R4EUIParticipantContainer) aChildToAdd;
 		} 
 		aChildToAdd.addListener((ReviewNavigatorContentProvider) R4EUIModelController.getNavigatorView().getTreeViewer().getContentProvider());
 		fireAdd(aChildToAdd);
 	}
 	
 	/**
 	 * Method createReviewItem
 	 * @param aTargetFile - the target file used for this review item
 	 * @return R4EUIReviewItem
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException 
 	 */
 	public R4EUIReviewItem createReviewItem(IFile aTargetFile) throws ResourceHandlingException, OutOfSyncException  {
 	
 		//Create and set review item model element
 		final R4EParticipant participant = getParticipant(R4EUIModelController.getReviewer(), true);
 		
 		final R4EItem reviewItem = R4EUIModelController.FModelExt.createR4EItem(participant);
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(reviewItem, R4EUIModelController.getReviewer());
 		reviewItem.getProjectURIs().add(ResourceUtils.toPlatformURIStr(aTargetFile.getProject()));
 		reviewItem.setDescription("");
 		reviewItem.setRepositoryRef(aTargetFile.getFullPath().toOSString());
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		
 		//Create and set UI model element
 		final R4EUIReviewItem uiReviewItem = new R4EUIReviewItem(this, reviewItem, 
 				R4EUIConstants.REVIEW_ITEM_TYPE_RESOURCE, reviewItem, aTargetFile.getName());
 		addChildren(uiReviewItem);	
 		return uiReviewItem;
 	}
 	
 	/**
 	 * Method removeChildren.
 	 * @param aChildToRemove IR4EUIModelElement
 	 * @param aFileRemove - also remove from file (hard remove)
 	 * @throws OutOfSyncException 
 	 * @throws ResourceHandlingException 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#removeChildren(IR4EUIModelElement)
 	 */
 	@Override
 	public void removeChildren(IR4EUIModelElement aChildToRemove, boolean aFileRemove) throws ResourceHandlingException, OutOfSyncException {
 		if (aChildToRemove instanceof R4EUIReviewItem) {
 			final R4EUIReviewItem removedElement = fItems.get(fItems.indexOf(aChildToRemove));
 			
 			//Also recursively remove all children 
 			removedElement.removeAllChildren(aFileRemove);
 			
 			/* TODO uncomment when core model supports hard-removing of elements
 			if (aFileRemove) removedElement.getItem().remove());
 			else */ 
 			final R4EItem modelItem = removedElement.getItem();
 			final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelItem, R4EUIModelController.getReviewer());
 			modelItem.setEnabled(false);
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 			//Remove element from UI if the show disabled element option is off
 			if (!(Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 				fItems.remove(removedElement);
 				aChildToRemove.removeListener();
 				fireRemove(aChildToRemove);
 			} else {
 				R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 			}
 		} else if (aChildToRemove instanceof R4EUIAnomalyContainer) {
 			fAnomalyContainer.removeAllChildren(aFileRemove);
 			if (!(Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 				fAnomalyContainer = null;
 				aChildToRemove.removeListener();
 				fireRemove(aChildToRemove);
 			} else {
 				R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 			}
 		} else if (aChildToRemove instanceof R4EUIParticipantContainer) {
 			fParticipantsContainer.removeAllChildren(aFileRemove);
 			if (!(Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 				fParticipantsContainer = null;
 				aChildToRemove.removeListener();
 				fireRemove(aChildToRemove);
 			} else {
 				R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 			}
 		}
 	}
 	
 	/**
 	 * Method removeAllChildren.
 	 * @param aFileRemove boolean
 	 * @throws OutOfSyncException 
 	 * @throws ResourceHandlingException 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#removeAllChildren(boolean)
 	 */
 	@Override
 	public void removeAllChildren(boolean aFileRemove) throws ResourceHandlingException, OutOfSyncException {
 		//Recursively remove all children
 		for (R4EUIReviewItem item : fItems) {
 			removeChildren(item, aFileRemove);
 		}
 	}
 	
 	
 	//Listeners
 	
 	/**
 	 * Method addListener.
 	 * @param aProvider ReviewNavigatorContentProvider
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#addListener(ReviewNavigatorContentProvider)
 	 */
 	@Override
 	public void addListener(ReviewNavigatorContentProvider aProvider) {
 		fListener = aProvider;
 		if (null != fItems) {
 			R4EUIReviewItem element = null;
 			for (final Iterator<R4EUIReviewItem> iterator = fItems.iterator(); iterator.hasNext();) {
 			    element = iterator.next();
 				element.addListener(aProvider);
 			}
 		}
 		if (null != fAnomalyContainer) fAnomalyContainer.addListener(aProvider);
 		if (null != fParticipantsContainer) fParticipantsContainer.addListener(aProvider);	
 	}
 	
 	/**
 	 * Method removeListener.
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#removeListener()
 	 */
 	@Override
 	public void removeListener() {
 		fListener = null;
 		if (null != fItems) {
 			R4EUIReviewItem element = null;
 			for (final Iterator<R4EUIReviewItem> iterator = fItems.iterator(); iterator.hasNext();) {
 				element = iterator.next();
 				element.removeListener();
 			}
 		}
 		if (null != fAnomalyContainer) fAnomalyContainer.removeListener();
 		if (null != fParticipantsContainer) fParticipantsContainer.removeListener();
 	}
 	
 	
 	//Commands
 	
 	/**
 	 * Method isChangeReviewStateCmd.
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#isChangeReviewStateCmd()
 	 */
 	@Override
 	public boolean isChangeReviewStateCmd() {
 		if (isEnabled() && isOpen()) return true;
 		return false;
 	}
 	
 	/**
 	 * Method isOpenElementCmd.
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#isOpenElementCmd()
 	 */
 	@Override
 	public boolean isOpenElementCmd() {
 		if (!isEnabled() || isOpen()) return false;
 		return true;
 	}
 	
 	/**
 	 * Method isCloseElementCmd.
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#isCloseElementCmd()
 	 */
 	@Override
 	public boolean isCloseElementCmd() {
 		if (isOpen()) return true;
 		return false;
 	}
 	
 	/**
 	 * Method isRemoveElementCmd.
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#isRemoveElementCmd()
 	 */
 	@Override
 	public boolean isRemoveElementCmd() {
 		if (!isOpen() && isEnabled()) return true;
 		return false;
 	}
 	
 	/**
 	 * Method isRestoreElementCmd.
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#iisRestoreElementCmd()
 	 */
 	@Override
 	public boolean isRestoreElementCmd() {
 		if (!(getParent().isEnabled())) return false;
 		if (isOpen() || isEnabled()) return false;
 		return true;
 	}
 	
 	/**
 	 * Method getRemoveElementCmdName.
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#getRemoveElementCmdName()
 	 */
 	@Override
 	public String getRemoveElementCmdName() {
 		return REMOVE_ELEMENT_COMMAND_NAME;
 	}
 	
 	/**
 	 * Method getRemoveElementCmdTooltip.
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#getRemoveElementCmdTooltip()
 	 */
 	@Override
 	public String getRemoveElementCmdTooltip() {
 		return REMOVE_ELEMENT_COMMAND_TOOLTIP;
 	}
 	
 	
 	//Phase Management
 	
 	/**
 	 * Method setDate.
 	 * @param aNewPhase R4EReviewPhase
 	 * @throws OutOfSyncException 
 	 * @throws ResourceHandlingException 
 	 */
 	public void setDate(R4EReviewPhase aNewPhase) throws ResourceHandlingException, OutOfSyncException {
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fReview, R4EUIModelController.getReviewer());
 		if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION)) {
 			((R4EFormalReview)fReview).getCurrent().setStartDate(new Date(new Date().getTime()));
 		} else if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_DECISION)) {
 			((R4EFormalReview)fReview).getCurrent().setStartDate(new Date(new Date().getTime()));
 		} else if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_REWORK)) {
 			((R4EFormalReview)fReview).getCurrent().setStartDate(new Date(new Date().getTime()));
 		} else if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 			fReview.setEndDate(new Date(new Date().getTime()));
 		} else {
 			fReview.setEndDate(null);
 		}
 		
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 	}
 	
 	/**
 	 * Method updatePhase.
 	 * @param aNewPhase R4EReviewPhase
 	 * @throws OutOfSyncException 
 	 * @throws ResourceHandlingException 
 	 */
 	public void updatePhase(R4EReviewPhase aNewPhase) throws ResourceHandlingException, OutOfSyncException {
 		//Update review state
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fReview, 
 				R4EUIModelController.getReviewer());
 		((R4EReviewState)fReview.getState()).setState(aNewPhase);
     	R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 	}
 	
 	/**
 	 * Method getPhaseString.
 	 * @param aNewPhase R4EReviewPhase
  	 * @return String
 	 */
 	public String getPhaseString(R4EReviewPhase aNewPhase) {
 		if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_STARTED)) {
 			return REVIEW_PHASE_STARTED;
 		} else if (aNewPhase.equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 			return REVIEW_PHASE_COMPLETED;
 		} else return "";
 	}
 	
 	/**
 	 * Method getStateFromString.
 	 * @param aNewPhase String
 	 * @return R4EReviewPhase
 	 */
 	public R4EReviewPhase getPhaseFromString(String aNewPhase) {
 		if (aNewPhase.equals(REVIEW_PHASE_STARTED)) {
 			return R4EReviewPhase.R4E_REVIEW_PHASE_STARTED;
 		} else if (aNewPhase.equals(REVIEW_PHASE_COMPLETED)) {
 			return R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED;
 		} else return null;   //should never happen
 	}
 	
 	/**
 	 * Method getPhases.
 	 * @return String[]
 	 */
 	public String[] getPhases() {
 		return BASIC_PHASE_VALUES;
 	}
 	
 	/**
 	 * Method getAvailablePhases.
 	 * @return String[]
 	 */
 	public String[] getAvailablePhases() {
 		//Peek state machine to get available states
 		final R4EReviewPhase[] phases = getAllowedPhases(((R4EReviewState)getReview().getState()).getState());
 		final List<String> phaseStrings = new ArrayList<String>();
 		for (R4EReviewPhase phase : phases) {
 			phaseStrings.add(getPhaseString(phase));
 		}
 		return phaseStrings.toArray(new String[phaseStrings.size()]);
 	}
 	
 	/**
 	 * Method mapPhaseToIndex.
 	 * @param aPhase R4EReviewPhase
 	 * @return int
 	 */
 	public int mapPhaseToIndex(R4EReviewPhase aPhase) {
 		//Peek state machine to get available states
 		final R4EReviewPhase[] phases = getAllowedPhases(((R4EReviewState)getReview().getState()).getState());
 		for (int i = 0; i < phases.length; i++) {
 			if (phases[i].getValue() == aPhase.getValue()) return i;		
 		}
 		return R4EUIConstants.INVALID_VALUE;   //should never happen
 	}
 	
 	/**
 	 * Method getAllowedPhases.
 	 * @param aCurrentPhase R4EReviewPhase
 	 * @return R4EReviewPhase[]
 	 */
 	protected R4EReviewPhase[] getAllowedPhases(R4EReviewPhase aCurrentPhase) {
 		final List<R4EReviewPhase> phases = new ArrayList<R4EReviewPhase>();
 		
 		switch (aCurrentPhase.getValue()) {
 			case R4EReviewPhase.R4E_REVIEW_PHASE_STARTED_VALUE:
 				phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_STARTED);
 				phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED);
 				break;
 
 			case R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED_VALUE:
 				phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED);
 				phases.add(R4EReviewPhase.R4E_REVIEW_PHASE_STARTED);
 				break;
 
 		default:
 			//should never happen
 		}
 
 		return phases.toArray(new R4EReviewPhase[phases.size()]);
 	}
 	
 	/**
 	 * Method validatePhaseChange.
 	 * @param aNextPhase - R4EReviewPhase
 	 * @param aErrorMessage - AtomicReference<String>
 	 * @return R4EReviewDecision
 	 */
 	public boolean validatePhaseChange(R4EReviewPhase aNextPhase, AtomicReference<String> aErrorMessage) { // $codepro.audit.disable booleanMethodNamingConvention
 				
 		switch (aNextPhase.getValue()) {	
 			case R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED_VALUE:
 				if (!checkCompletionStatus()) {
 					aErrorMessage.set("Phase cannot be changed to " + REVIEW_PHASE_COMPLETED + 
 										" as some anomalies are not in the proper state");
 					return false;
 				}
 				break;
 				
 			default:
 				//Nothing to do
 		}
 		return true;
 	}
 	
 	/**
 	 * Method checkCompletionStatus.
 	 * @return boolean
 	 */
 	public boolean checkCompletionStatus() { // $codepro.audit.disable booleanMethodNamingConvention
 		if (!(fReview.getType().equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC))) {
 			if (null == fReview.getDecision() || null == fReview.getDecision().getValue()) return false;
 			if (fReview.getDecision().getValue().equals(R4EDecision.R4E_REVIEW_DECISION_NONE)) return false;	
 			if (fReview.getDecision().getValue().equals(R4EDecision.R4E_REVIEW_DECISION_REJECTED)) return true;
 			
 			//Check global anomalies state
 			if (!(fAnomalyContainer.checkCompletionStatus())) return false;
 			
 			for (R4EUIReviewItem item : fItems) {
 				R4EUIFileContext[] contexts = (R4EUIFileContext[]) item.getChildren();
 				for (R4EUIFileContext context : contexts) {
 					R4EUIAnomalyContainer container = (R4EUIAnomalyContainer) context.getAnomalyContainerElement();
 					if (null != container && !(container.checkCompletionStatus())) return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Method isExitDecisionEnabled.
 	 * @return boolean
 	 */
 	public boolean isExitDecisionEnabled() {
 		if (((R4EReviewState)fReview.getState()).getState().equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Method getExitDecisionValues.
 	 * @return String[]
 	 */
 	public String[] getExitDecisionValues() {
 		return DECISION_VALUES;
 	}
 	
 	/**
 	 * Method getDecisionValueFromString.
 	 * @param aDecision - String
 	 * @return R4EReviewDecision
 	 */
 	public static R4EReviewDecision getDecisionValueFromString(String aDecision) {
 		final R4EReviewDecision reviewDecision = RModelFactoryExt.eINSTANCE.createR4EReviewDecision();
 		if (aDecision.equals(EXIT_DECISION_ACCEPTED)) {
 			reviewDecision.setValue(R4EDecision.R4E_REVIEW_DECISION_ACCEPTED);
 		} else if (aDecision.equals(EXIT_DECISION_ACCEPTED_FOLLOWUP)) {
 			reviewDecision.setValue(R4EDecision.R4E_REVIEW_DECISION_ACCEPTED_FOLLOWUP);
 		} else if (aDecision.equals(EXIT_DECISION_REJECTED)) {
 			reviewDecision.setValue(R4EDecision.R4E_REVIEW_DECISION_REJECTED);
 		} else {
 			reviewDecision.setValue(R4EDecision.R4E_REVIEW_DECISION_NONE);
 		}
 		return reviewDecision;
 	}
 }
