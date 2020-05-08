 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, com.instantiations.assist.eclipse.analysis.mutabilityOfArrays
 /*******************************************************************************
  * Copyright (c) 2011 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the Postponed File element of the UI model
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.model;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.mylyn.reviews.frame.core.model.Location;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomaly;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomalyTextPosition;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EComment;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EContent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileContext;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.RFSRegistryFactory;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.ReviewsFileStorageException;
 import org.eclipse.mylyn.reviews.r4e.core.utils.ResourceUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorContentProvider;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.preferences.PreferenceConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.properties.general.PostponedFileProperties;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.team.core.history.IFileRevision;
 import org.eclipse.ui.views.properties.IPropertySource;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EUIPostponedFile extends R4EUIFileContext {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field POSTPONED_FILE_ICON_FILE. (value is ""icons/obj16/postfile_obj.gif"")
 	 */
 	public static final String POSTPONED_FILE_ICON_FILE = "icons/obj16/postfile_obj.gif";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fAnomalies.
 	 */
 	private final List<R4EUIPostponedAnomaly> fUiAnomalies;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EUIFileContext.
 	 * 
 	 * @param aParent
 	 *            IR4EUIModelElement
 	 * @param aFile
 	 *            R4EFileContext
 	 */
 	public R4EUIPostponedFile(IR4EUIModelElement aParent, R4EFileContext aFile) {
 		super(aParent, aFile);
 		fUiAnomalies = new ArrayList<R4EUIPostponedAnomaly>();
 		setImage(POSTPONED_FILE_ICON_FILE);
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
 		return "Original Review: " + fFile.getInfoAtt().get(R4EUIConstants.POSTPONED_ATTR_ORIG_REVIEW_NAME);
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
 			return new PostponedFileProperties(this);
 		}
 		return null;
 	}
 
 	/**
 	 * Method getBaseFileVersion. This is overriden to use the file version on which the anomaly was originally raised
 	 * 
 	 * @return R4EFileVersion
 	 */
 	@Override
 	public R4EFileVersion getBaseFileVersion() {
 		return fFile.getTarget(); //Target is actually the original file version
 	}
 
 	/**
 	 * Method getTargetFileVersion. This is overriden to use the file version in the current review
 	 * 
 	 * @return R4EFileVersion
 	 */
 	@Override
 	public R4EFileVersion getTargetFileVersion() {
 		final List<R4EUIReviewItem> items = R4EUIModelController.getActiveReview().getReviewItems();
 		for (R4EUIReviewItem item : items) {
 			List<R4EUIFileContext> files = item.getFileContexts();
 			for (R4EUIFileContext file : files) {
 				if (!(file instanceof R4EUIPostponedFile)
 						&& file.getTargetFileVersion().getPlatformURI().equals(fFile.getTarget().getPlatformURI())) {
 					return file.getTargetFileVersion();
 				}
 			}
 		}
 		return null;
 	}
 
 	//Hierarchy
 
 	/**
 	 * Method hasChildren.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#hasChildren()
 	 */
 	@Override
 	public boolean hasChildren() {
 		if (fUiAnomalies.size() > 0) {
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
 		R4EUIPostponedAnomaly anomaly = null;
 		final int anomaliesSize = fUiAnomalies.size();
 		for (int i = 0; i < anomaliesSize; i++) {
 
 			anomaly = fUiAnomalies.get(i);
 			anomaly.close();
 		}
 		fUiAnomalies.clear();
 		fOpen = false;
 		removeListeners();
 	}
 
 	/**
 	 * Method open. Load the serialization model data into UI model
 	 */
 	@Override
 	public void open() {
 
 		//Restore resource data in serialization model
 		IRFSRegistry revRegistry = null;
 		try {
 			revRegistry = RFSRegistryFactory.getRegistry(((R4EUIReviewBasic) this.getParent().getParent()).getReview());
 		} catch (ReviewsFileStorageException e1) {
 			R4EUIPlugin.Ftracer.traceInfo("Exception: " + e1.toString() + " (" + e1.getMessage() + ")");
 			R4EUIPlugin.getDefault().logInfo("Exception: " + e1.toString(), e1);
 		}
 
 		//Restore target file version
 		final R4EFileVersion targetFileVersion = fFile.getTarget();
 		if (null != targetFileVersion) {
 			try {
 				final IFile targetFile = ResourceUtils.toIFile(targetFileVersion.getPlatformURI());
 				targetFileVersion.setResource(targetFile);
 			} catch (FileNotFoundException e) {
 				R4EUIPlugin.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				targetFileVersion.setResource(null);
 			}
 
 			targetFileVersion.setFileRevision(null);
 			if (null != revRegistry) {
 				try {
 					final IFileRevision fileRev = revRegistry.getIFileRevision(null, targetFileVersion);
 					targetFileVersion.setFileRevision(fileRev);
 				} catch (ReviewsFileStorageException e) {
 					R4EUIPlugin.Ftracer.traceInfo("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 		}
 
 		//get anomalies that are specific to a file
 		R4EUIPostponedAnomaly uiAnomaly = null;
 		R4EUITextPosition position = null;
 		fAnomalies = R4EUIModelController.getAnomaliesForFile(fFile.getTarget().getLocalVersionID());
		int anomaliesSize = (null != fAnomalies) ? fAnomalies.size() : 0;
 		R4EAnomaly anomaly = null;
 		for (int i = 0; i < anomaliesSize; i++) {
 			anomaly = fAnomalies.get(i);
 			if (null == anomaly.getInfoAtt().get(R4EUIConstants.POSTPONED_ATTR_ORIG_ANOMALY_ID)) {
 				//This is a genuine anomaly, not a postponed one, so we ignore it
 				continue;
 			}
 			if (anomaly.isEnabled()
 					|| R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED)) {
 				//Do not set position for global EList<E>lies
 				position = null;
 				EList<Location> locations = anomaly.getLocation(); // $codepro.audit.disable variableDeclaredInLoop
 				if (null != locations) {
 					if (null != locations.get(0)) {
 						int locationsSize = locations.size(); // $codepro.audit.disable variableDeclaredInLoop
 						for (int j = 0; j < locationsSize; j++) {
 							position = new R4EUITextPosition(((R4EContent) anomaly.getLocation().get(j)).getLocation());
 							uiAnomaly = new R4EUIPostponedAnomaly(this, anomaly, position);
 							uiAnomaly.setName(R4EUIAnomalyExtended.getStateString(anomaly.getState()) + ": "
 									+ uiAnomaly.getName());
 
 							addChildren(uiAnomaly);
 							if (uiAnomaly.isEnabled()) {
 								uiAnomaly.open();
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Method getChildren.
 	 * 
 	 * @return IR4EUIModelElement[]
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getChildren()
 	 */
 	@Override
 	public IR4EUIModelElement[] getChildren() {
 		return fUiAnomalies.toArray(new R4EUIPostponedAnomaly[fUiAnomalies.size()]);
 	}
 
 	/**
 	 * Method addChildren.
 	 * 
 	 * @param aChildToAdd
 	 *            IR4EUIModelElement
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#addChildren(IR4EUIModelElement)
 	 */
 	@Override
 	public void addChildren(IR4EUIModelElement aChildToAdd) {
 		if (aChildToAdd instanceof R4EUIPostponedAnomaly) {
 			fUiAnomalies.add((R4EUIPostponedAnomaly) aChildToAdd);
 		}
 		aChildToAdd.addListener((ReviewNavigatorContentProvider) R4EUIModelController.getNavigatorView()
 				.getTreeViewer()
 				.getContentProvider());
 		fireAdd(aChildToAdd);
 	}
 
 	/**
 	 * Method createAnomaly
 	 * 
 	 * @param aPostponedAnomaly
 	 *            R4EFileVersion
 	 * @return R4EUIPostponedAnomaly
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException
 	 */
 	public R4EUIPostponedAnomaly createAnomaly(R4EAnomaly aPostponedAnomaly) throws ResourceHandlingException,
 			OutOfSyncException {
 
 		//Check if the creator of the postponed anomaly is a participant of the current review.  If not, it will be 
 		//created and disabled after the postponed anomaly is created
 		final R4EUIReviewBasic uiReview = R4EUIModelController.getActiveReview();
 		R4EParticipant participant = uiReview.getParticipant(aPostponedAnomaly.getUser().getId(), false);
 		boolean isParticipant = true;
 		if (null == participant) {
 			participant = uiReview.getParticipant(aPostponedAnomaly.getUser().getId(), true);
 			isParticipant = false;
 		}
 
 		//Copy anomaly information from postponed anomaly model element
 		final R4EAnomaly anomaly = R4EUIModelController.FModelExt.createR4EAnomaly(participant);
 		CommandUtils.copyAnomalyData(anomaly, aPostponedAnomaly);
 		final EMap<String, String> info = anomaly.getInfoAtt(); //We use the R4EAnomaly attribute map to store the original anomaly ID
 		info.put(R4EUIConstants.POSTPONED_ATTR_ORIG_ANOMALY_ID, aPostponedAnomaly.getId().getUserID()
 				+ R4EUIConstants.SEPARATOR + aPostponedAnomaly.getId().getSequenceID());
 
 		//Set position data
 		final R4EAnomalyTextPosition position = R4EUIModelController.FModelExt.createR4EAnomalyTextPosition(R4EUIModelController.FModelExt.createR4ETextContent(anomaly));
 		final R4EUITextPosition uiPosition = new R4EUITextPosition(CommandUtils.getAnomalyPosition(aPostponedAnomaly));
 
 		//Set File version data
 		final R4EFileVersion postponedAnomalyFileVersion = CommandUtils.getAnomalyParentFile(aPostponedAnomaly);
 		final R4EFileVersion anomalyFileVersion = R4EUIModelController.FModelExt.createR4EFileVersion(position);
 		Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(anomalyFileVersion,
 				R4EUIModelController.getReviewer());
 		CommandUtils.copyFileVersionData(anomalyFileVersion, postponedAnomalyFileVersion);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 		//Create and set UI model element
 		uiPosition.setPositionInModel(position);
 		final R4EUIPostponedAnomaly uiAnomaly = new R4EUIPostponedAnomaly(this, anomaly, uiPosition);
 		uiAnomaly.updateState(aPostponedAnomaly.getState());
 		addChildren(uiAnomaly);
 
 		//Disable original creator of the postponed anomaly since he is not part of this review
 		if (!isParticipant) {
 			bookNum = R4EUIModelController.FResourceUpdater.checkOut(participant, R4EUIModelController.getReviewer());
 			participant.setEnabled(false);
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		}
 		//Add R4EAnomaly to the list of current postponed anomalies
 		if (null == fAnomalies) {
 			fAnomalies = new ArrayList<R4EAnomaly>();
 		}
 		fAnomalies.add(anomaly);
 		return uiAnomaly;
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
 		if (aChildToRemove instanceof R4EUIPostponedAnomaly) {
 			final R4EUIPostponedAnomaly removedElement = fUiAnomalies.get(fUiAnomalies.indexOf(aChildToRemove));
 
 			final R4EComment modelAnomaly = removedElement.getAnomaly();
 			final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelAnomaly,
 					R4EUIModelController.getReviewer());
 			modelAnomaly.setEnabled(false);
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 			if (!(R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 				fUiAnomalies.remove(removedElement);
 				aChildToRemove.removeListeners();
 				fireRemove(aChildToRemove);
 			} else {
 				R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 			}
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
 		for (R4EUIPostponedAnomaly uiAnomaly : fUiAnomalies) {
 			removeChildren(uiAnomaly, aFileRemove);
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
 		if (null != fUiAnomalies) {
 			R4EUIPostponedAnomaly element = null;
 			for (final Iterator<R4EUIPostponedAnomaly> iterator = fUiAnomalies.iterator(); iterator.hasNext();) {
 				element = iterator.next();
 				element.addListener(aProvider);
 			}
 		}
 	}
 
 	/**
 	 * Method removeListener.
 	 * 
 	 * @param aProvider
 	 *            - the treeviewer content provider
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#removeListener()
 	 */
 	@Override
 	public void removeListener(ReviewNavigatorContentProvider aProvider) {
 		super.removeListener(aProvider);
 		if (null != fUiAnomalies) {
 			R4EUIPostponedAnomaly element = null;
 			for (final Iterator<R4EUIPostponedAnomaly> iterator = fUiAnomalies.iterator(); iterator.hasNext();) {
 				element = iterator.next();
 				element.removeListener(aProvider);
 			}
 		}
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
 		return false;
 	}
 }
