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
 
 package org.eclipse.mylyn.reviews.r4e.ui.model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EContextType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileContext;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EItem;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.core.utils.ResourceUtils;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewsVersionsIF.CommitDescriptor;
 import org.eclipse.mylyn.reviews.r4e.ui.Activator;
 import org.eclipse.mylyn.reviews.r4e.ui.navigator.ReviewNavigatorContentProvider;
 import org.eclipse.mylyn.reviews.r4e.ui.preferences.PreferenceConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.properties.general.ReviewItemProperties;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.UIUtils;
 import org.eclipse.mylyn.versions.core.ChangeSet;
 import org.eclipse.ui.views.properties.IPropertySource;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EUIReviewItem extends R4EUIModelElement {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Field fReviewItemFile.
 	 * (value is ""icons/obj16/revitm_obj.gif"")
 	 */
 	private static final String REVIEW_ITEM_ICON_FILE = "icons/obj16/revitm_obj.gif";
 	
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_NAME.
 	 * (value is ""Delete Review Item"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_NAME = "Disable Review Item";
 	
     /**
      * Field REMOVE_ELEMENT_ACTION_TOOLTIP.
      * (value is ""Remove this review item from its parent review"")
      */
     private static final String REMOVE_ELEMENT_COMMAND_TOOLTIP = "Disable (and Optionally Remove) this Review " +
     		"Item from its Parent Review";
 	
 	
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
     
 	/**
 	 * Field fItem.
 	 */
 	private final R4EItem fItem;
 	
 	/**
 	 * Field fType.
 	 */
 	private final int fType;
 	
 	/**
 	 * Field fFileContexts.
 	 */
 	private final List<R4EUIFileContext> fFileContexts;
 	
 	
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EUIReviewItem.
 	 * @param aParent IR4EUIModelElement
 	 * @param aItem R4EItem
 	 * @param aType int
 	 * @param aItemInfo Object
 	 * @param aFilename String
 
 	 */
 	public R4EUIReviewItem(IR4EUIModelElement aParent, R4EItem aItem, int aType, Object aItemInfo, String aFilename) {
 		super(aParent, getItemDisplayName(aType, aItemInfo, aFilename), getItemDisplayTooltip(aType, aItemInfo));
 		fItem = aItem;
 		fType = aType;
 		fFileContexts = new ArrayList<R4EUIFileContext>();
 		setImage(REVIEW_ITEM_ICON_FILE);
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
 		if (IR4EUIModelElement.class.equals(adapter)) return this;
 		if (IPropertySource.class.equals(adapter)) return new ReviewItemProperties(this);
 		return null;
 	}
 	
 	//Attributes
 	
 	/**
 	 * Method getType.
 	 * @return int
 	 */
 	public int getType() {
 		return fType;
 	}
 	
 	/**
 	 * Method getItemDisplayName.
 	 * @param aType int
 	 * @param aItemInfo Object
 	 * @param aFilename String
 	 * @return String
 	 */
 	private static String getItemDisplayName(int aType, Object aItemInfo, String aFilename) {
 		switch (aType) {
 			case R4EUIConstants.REVIEW_ITEM_TYPE_RESOURCE:
 			{
 				return "Resource: " + aFilename;
 			}
 			
 			case R4EUIConstants.REVIEW_ITEM_TYPE_COMMIT:
 			{
 				String commitId = "";
 				if (aItemInfo instanceof CommitDescriptor) {
 					commitId = ((CommitDescriptor)aItemInfo).getTitle();
 				} else if (aItemInfo instanceof ChangeSet) {
 					commitId = ((ChangeSet)aItemInfo).getMessage().substring(R4EUIConstants.START_STRING_INDEX, 
 							R4EUIConstants.END_STRING_NAME_INDEX) + "...";
 				}
 				return "Commit: " + commitId;
 			}
 			
 			default:
 				return "";   //should never happen
 		}
 	}
 
 	/**
 	 * Method getItemDisplayTooltip.
 	 * @param aType int
 	 * @param aItemInfo Object
 	 * @return String
 	 */
 	private static String getItemDisplayTooltip(int aType, Object aItemInfo) {
 		switch (aType) {
 			case R4EUIConstants.REVIEW_ITEM_TYPE_RESOURCE:
 			{
				return "Description: " + ((R4EItem)aItemInfo).getDescription();
 			}
 			
 			case R4EUIConstants.REVIEW_ITEM_TYPE_COMMIT:
 			{
				String message = "";
 				if (aItemInfo instanceof CommitDescriptor) {
 					message = ((CommitDescriptor)aItemInfo).getMessage();
 				} else if (aItemInfo instanceof ChangeSet) {
 					message = ((ChangeSet)aItemInfo).getMessage();
 				}
 				return "Description: " + message;
 			}
 			
 			default:
 				return "";   //should never happen
 		}
 	}
 	
 	/**
 	 * Method setReviewed.
 	 * @param aReviewed boolean
 	 * @throws ResourceHandlingException 
 	 * @throws OutOfSyncException 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#setReviewed(boolean)
 	 */
 	@Override
 	public void setReviewed(boolean aReviewed) throws ResourceHandlingException, OutOfSyncException {
 		fReviewed = aReviewed;
 		if (fReviewed) {
 			//Also set the children
 			final int length = fFileContexts.size();
 			for (int i = 0; i < length; i++) {
 				fFileContexts.get(i).setChildReviewed(aReviewed);
 			}
 		}
 		fireReviewStateChanged(this);
 	}
 	
 	/**
 	 * Method checkToSetReviewed.
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#checkToSetReviewed()
 	 */
 	@Override
 	public void checkToSetReviewed() {
 		boolean allChildrenReviewed = true;
 		final int length = fFileContexts.size();
 		for (int i = 0; i < length; i++) {
 			if (!(fFileContexts.get(i).isReviewed())) allChildrenReviewed = false;
 		}
 		//If all children are reviewed, mark the parent as reviewed as well
 		if (allChildrenReviewed) {
 			fReviewed = true;   
 			fireReviewStateChanged(this);
 		}
 	}
 	
 	/**
 	 * Method getItem.
 	 * @return R4EItem
 	 */
 	public R4EItem getItem() {
 		return fItem;
 		
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
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fItem, R4EUIModelController.getReviewer());
 		fItem.setEnabled(true);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		for (R4EUIFileContext file : fFileContexts) {
 			file.setEnabled(true);
 		}
 		R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 	}
 	
 	/**
 	 * Method isEnabled.
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#isEnabled()
 	 */
 	@Override
 	public boolean isEnabled() {
 		return fItem.isEnabled();
 	}
 	
 	
 	//Hierarchy
 	
 	/**
 	 * Method getChildren.
 	 * @return IR4EUIModelElement[]
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#getChildren()
 	 */
 	@Override
 	public IR4EUIModelElement[] getChildren() {
 		return fFileContexts.toArray(new R4EUIFileContext[fFileContexts.size()]);
 	}
 	
 	/**
 	 * Method hasChildren.
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#hasChildren()
 	 */
 	@Override
 	public boolean hasChildren() {
 		if (fFileContexts.size() > 0) return true;
 	    return false;
 	}
 	
 	/**
 	 * Close the model element (i.e. disable it)
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#close()
 	 */
 	@Override
 	public void close() {
 		//Remove all children references
 		R4EUIFileContext fileContext = null;
 		final int fileContextsSize = fFileContexts.size();
 		for (int i = 0; i < fileContextsSize; i++) {
 			
 			fileContext = fFileContexts.get(i);
 			fileContext.close();
 		}
 		fFileContexts.clear();
 		fOpen = false;
 		removeListener();
 	}
 	
 	/**
 	 * Method open.
 	 */
 	@Override
 	public void open() {
 		final EList<R4EFileContext> files = fItem.getFileContextList();
 		if (null != files) {	
 			R4EUIFileContext newFileContext = null;
 			final int filesSize = files.size();
 			R4EFileContext file = null;
 			for (int i = 0; i < filesSize; i++) {
 				file = files.get(i);
 				if (file.isEnabled() || Activator.getDefault().getPreferenceStore().
 						getBoolean(PreferenceConstants.P_SHOW_DISABLED)) {
 					newFileContext = new R4EUIFileContext(this, files.get(i));
 					addChildren(newFileContext);
 					newFileContext.open();
 				}
 			}
 			
 			try {
 				final R4EUIReviewBasic review = (R4EUIReviewBasic) getParent();
 				final R4EParticipant user = review.getParticipant(R4EUIModelController.getReviewer(), false);
 
 				if (null != user) {
 					//Check if the file contexts are part of the reviewed content
 					for (R4EUIFileContext uiFile : fFileContexts) {
 						if (user.getReviewedContent().contains(uiFile.getFileContext().getId())) {
 							uiFile.setReviewed(true);
 						}
 					}			
 				}
 			} catch (ResourceHandlingException e) {
 				UIUtils.displayResourceErrorDialog(e);
 
 			} catch (OutOfSyncException e) {
 				UIUtils.displaySyncErrorDialog(e);
 
 			}
 		}
 		fOpen = true;
 	}
 	
 	/**
 	 * Method addChildren.
 	 * @param aChildToAdd IR4EUIModelElement
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#addChildren(IR4EUIModelElement)
 	 */
 	@Override
 	public void addChildren(IR4EUIModelElement aChildToAdd) {
 		fFileContexts.add((R4EUIFileContext) aChildToAdd);
 		aChildToAdd.addListener((ReviewNavigatorContentProvider) R4EUIModelController.getNavigatorView().getTreeViewer().getContentProvider());
 		fireAdd(aChildToAdd);
 	}
 
 	/**
 	 * Method createReviewItem
 	 * @param aBaseTempFileVersion R4EFileVersion
 	 * @param aTargetTempFileVersion R4EFileVersion
 	 * @return aType R4EContextType
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException 
 	 */
 	public R4EUIFileContext createFileContext(R4EFileVersion aBaseTempFileVersion, R4EFileVersion aTargetTempFileVersion,
 			R4EContextType aType) throws ResourceHandlingException, OutOfSyncException  {
 	
 		final R4EFileContext fileContext = R4EUIModelController.FModelExt.createR4EFileContext(fItem);			
 		fileContext.setType(aType);
 		
 		//Get Base version from Version control system and set core model data
 		if (null != aBaseTempFileVersion) {
 			final R4EFileVersion rfileBaseVersion = R4EUIModelController.FModelExt.createR4EBaseFileVersion(fileContext);
 			CommandUtils.copyFileVersionData(rfileBaseVersion, aBaseTempFileVersion);
 			
 			//Add ProjectURI to the review item
 			final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fItem, R4EUIModelController.getReviewer());
 			final String projPlatformURI = ResourceUtils.toPlatformURIStr(rfileBaseVersion.getResource().getProject());
 			if (!fItem.getProjectURIs().contains(projPlatformURI)) {
 				fItem.getProjectURIs().add(projPlatformURI);
 			}
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		}
 	
 		//Get Target version from Version control system and set core model data
 		if (null != aTargetTempFileVersion) {
 			final R4EFileVersion rfileTargetVersion = R4EUIModelController.FModelExt.createR4ETargetFileVersion(fileContext);
 			CommandUtils.copyFileVersionData(rfileTargetVersion, aTargetTempFileVersion);
 			
 			//Add ProjectURI to the review item
 			final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fItem, R4EUIModelController.getReviewer());
 			final String projPlatformURI = ResourceUtils.toPlatformURIStr(rfileTargetVersion.getResource().getProject());
 			if (!fItem.getProjectURIs().contains(projPlatformURI)) {
 				fItem.getProjectURIs().add(projPlatformURI);
 			}
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		}
 		
 		final R4EUIFileContext uiFile = new R4EUIFileContext(this, fileContext);
 		addChildren(uiFile);
 		return uiFile;
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
 		final R4EUIFileContext removedElement = fFileContexts.get(fFileContexts.indexOf(aChildToRemove));
 		
 		//Also recursively remove all children 
 		removedElement.removeAllChildren(aFileRemove);
 		
 		/* TODO uncomment when core model supports hard-removing of elements
 		if (aFileRemove) removedElement.getFileContext().remove());
 		else */
 		final R4EFileContext modelFile = removedElement.getFileContext();
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelFile, R4EUIModelController.getReviewer());
 		modelFile.setEnabled(false);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 		//Remove element from UI if the show disabled element option is off
 		if (!(Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 			fFileContexts.remove(removedElement);
 			aChildToRemove.removeListener();
 			fireRemove(aChildToRemove);
 		} else {
 			R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
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
 		for (R4EUIFileContext file : fFileContexts) {
 			removeChildren(file, aFileRemove);
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
 		if (null != fFileContexts) {
 			R4EUIFileContext element = null;
 			for (final Iterator<R4EUIFileContext> iterator = fFileContexts.iterator(); iterator.hasNext();) {
 				element = iterator.next();
 				element.addListener(aProvider);
 			}
 		}
 	}
 	
 	/**
 	 * Method removeListener.
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#removeListener()
 	 */
 	@Override
 	public void removeListener() {
 		fListener = null;
 		if (null != fFileContexts) {
 			R4EUIFileContext element = null;
 			for (final Iterator<R4EUIFileContext> iterator = fFileContexts.iterator(); iterator.hasNext();) {
 				element = iterator.next();
 				element.removeListener();
 			}
 		}
 	}
 	
 	
 	//Commands
 	
 	/**
 	 * Method isChangeReviewStateCmd.
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#isChangeReviewStateCmd()
 	 */
 	@Override
 	public boolean isChangeReviewStateCmd() {
 		if (isEnabled() && !(R4EUIModelController.getActiveReview().isReviewed())) return true;
 		return false;
 	}
 	
 	/**
 	 * Method isRemoveElementCmd.
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#isRemoveElementCmd()
 	 */
 	@Override
 	public boolean isRemoveElementCmd() {
 		if (isEnabled()) return true;
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
 		if (isEnabled()) return false;
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
 }
