 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, useForLoop, staticFieldSecurity, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
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
  * This class implements the model controller.  It is used to control the UI 
  * model data
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.NotEnabledException;
 import org.eclipse.core.commands.NotHandledException;
 import org.eclipse.core.commands.common.NotDefinedException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.mylyn.reviews.frame.core.model.Location;
 import org.eclipse.mylyn.reviews.frame.core.model.Topic;
 import org.eclipse.mylyn.reviews.notifications.core.NotificationsCore;
 import org.eclipse.mylyn.reviews.notifications.spi.NotificationsConnector;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomaly;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomalyTextPosition;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EContent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleCollection;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.RModelFactoryExt;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.ResourceUpdater;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.SerializeFactory;
 import org.eclipse.mylyn.reviews.r4e.ui.Activator;
 import org.eclipse.mylyn.reviews.r4e.ui.commands.ReviewState;
 import org.eclipse.mylyn.reviews.r4e.ui.navigator.ReviewNavigatorActionGroup;
 import org.eclipse.mylyn.reviews.r4e.ui.navigator.ReviewNavigatorView;
 import org.eclipse.mylyn.reviews.r4e.ui.preferences.PreferenceConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.UIUtils;
 import org.eclipse.ui.IPropertyListener;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EUIModelController {
 
 	
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Field MAIL_CONNECTOR_IDS.
 	 */
	//private static final String[] MAIL_CONNECTOR_IDS = { "reviews.r4e.mail.outlook.connector" };
	private static final String[] MAIL_CONNECTOR_IDS = { "reviews.r4e.mail.smtp.connector" };
 	//TODO outlook lookup is temporary.  Will be replaced by generic later
 
 	
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Field fRootElement.
 	 */
 	private static R4EUIRootElement FRootElement = null;
 	
 	/**
 	 * Field FIsDialogOpen.
 	 */
 	private static boolean FIsDialogOpen = false;
 	
 	/**
 	 * Field fActiveReview.
 	 */
 	private static R4EUIReviewBasic FActiveReview = null;
 	
 	/**
 	 * Field FReviewSourceProvider.
 	 */
 	private static ReviewState FReviewSourceProvider = null;
 	
 	/**
 	 * Field fView.
 	 */
 	private static ReviewNavigatorView FView = null;
 	
 	/**
 	 * Field FReviewer.
 	 */
 	private static String FReviewer = null;
 
 	/**
 	 * Field modelExt.
 	 */
 	public static RModelFactoryExt FModelExt;  	//Reference to the EMF serialization model
 	
 	/**
 	 * Field resourceUpdater.
 	 */
 	public static ResourceUpdater FResourceUpdater;  	//Reference to the EMF serialization model
 	
 	/**
 	 * Field fFileAnomalyMap.
 	 * 		Filname to Anomaly List map
 	 */
 	private static final Map<String,List<R4EAnomaly>> FFileAnomalyMap = new HashMap<String,List<R4EAnomaly>>(32, 0.75f); // $codepro.audit.disable constantNamingConvention
 	
 	/**
 	 * Field FElementStateListenerList.
 	 */
 	private static final List<IPropertyListener> FElementStateListenerList = new ArrayList<IPropertyListener>(); // $codepro.audit.disable constantNamingConvention
 	
 	/**
 	 * Field FMailConnector.
 	 */
 	private static NotificationsConnector FMailConnector = null;
 	
 	
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Method addElementStateListener.
 	 * @param aListener IPropertyListener
 	 */
 	public static void addElementStateListener(IPropertyListener aListener) {
 		FElementStateListenerList.add(aListener);
 	}
 	
 	/**
 	 * Method removeElementStateListener.
 	 * @param aListener IPropertyListener
 	 */
 	public static void removeElementStateListener(IPropertyListener aListener) {
 		FElementStateListenerList.remove(aListener);
 	}
 	
 	/**
 	 * Method setNavigatorView.
 	 * @param aView ReviewNavigatorView
 	 */
 	public static void setNavigatorView(ReviewNavigatorView aView) {
 		FView = aView;
 	}
 	
 	/**
 	 * Method getNavigatorView.
 	 * @return ReviewNavigatorView
 	 */
 	public static ReviewNavigatorView getNavigatorView() {
 		return FView;
 	}
 	
 	/**
 	 * Method setReviewCommandSourceProvider.
 	 * @param aSourceProvider ReviewState
 	 */
 	public static void setReviewCommandSourceProvider(ReviewState aSourceProvider) {
 		FReviewSourceProvider = aSourceProvider;
 	}
 	
 	/**
 	 * Get the model root element
 	 * @return the root R4E element
 	 */
 	public static IR4EUIModelElement getRootElement() {
 		return FRootElement;
 	}
 	
 	/**
 	 * Set the currently active review
 	 * @param aActiveReview ReviewElement - the currently open ReviewElement
 	 */
 	public static void setActiveReview(R4EUIReviewBasic aActiveReview) {
 		FActiveReview = aActiveReview;
 		if (null != FReviewSourceProvider) {
 			FReviewSourceProvider.setCurrentReview(FActiveReview);
 		}
 		//check to apply filters
 		try {
 			final ReviewNavigatorActionGroup actionGroup = (ReviewNavigatorActionGroup)FView.getActionSet();
 			if (actionGroup.isMyReviewFilterSet()) actionGroup.runReviewsMyFilterCommand(true);
 			if (actionGroup.isParticipantFilterSet()) { 
 				actionGroup.runReviewsParticipantFilterCommand(actionGroup.getFilterParticipant());
 			}
 		} catch (ExecutionException e) {
 			Activator.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 			Activator.getDefault().logError("Exception: " + e.toString(), e);
 		} catch (NotDefinedException e) {
 			Activator.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 			Activator.getDefault().logError("Exception: " + e.toString(), e);
 		} catch (NotEnabledException e) {
 			Activator.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 			Activator.getDefault().logError("Exception: " + e.toString(), e);
 		} catch (NotHandledException e) {
 			Activator.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 			Activator.getDefault().logError("Exception: " + e.toString(), e);
 		}
 	}
 	
 	/**
 	 * Set properties state based on the current dialog state
 	 * @param aIsDialogOpen boolean
 	 */
 	public static void setDialogOpen(boolean aIsDialogOpen) {
 		FIsDialogOpen = aIsDialogOpen;
 		for (IPropertyListener listener : FElementStateListenerList) {
 			listener.propertyChanged(null, 0);
 		}
 	}
 	
 	/**
 	 * Set properties state based on the selected element
 	 * @param aSelection the selected element
 	 */
 	public static void selectionChanged(IStructuredSelection aSelection) {
 		for (IPropertyListener listener : FElementStateListenerList) {
 			listener.propertyChanged(null, 0);  //TODO this is temporary and should be refined later
 		}
 	}
 	
 	/**
 	 * Refresh properties
 	 */
 	public static void propertyChanged() {
 		for (IPropertyListener listener : FElementStateListenerList) {
 			listener.propertyChanged(null, 0);  //TODO this is temporary and should be refined later
 		}
 	}
 	
 	/**
 	 * Get the currently active review
 	 * @return the currently open ReviewElement
 	 */
 	public static R4EUIReviewBasic getActiveReview() {
 		return FActiveReview;
 	}
 	
 	/**
 	 * Get the current dialog state
 	 * @return the current dialog state
 	 */
 	public static boolean isDialogOpen() {
 		return FIsDialogOpen;
 	}
 	
 	/**
 	 * Method peekReviewGroup.
 	 * @param filePath String
 	 * @return R4EReviewGroup
 	 * @throws ResourceHandlingException
 	 */
 	public static R4EReviewGroup peekReviewGroup(String filePath) throws ResourceHandlingException {
 		FModelExt = SerializeFactory.getModelExtension();
 		return FModelExt.openR4EReviewGroup(URI.createFileURI(filePath));
 	}
 	
 	/**
 	 * Method loadModel.
 	 * 		Load data from Serialized Model
 	 */
 	public static void loadModel() {
 		FModelExt = SerializeFactory.getModelExtension();
 		FResourceUpdater = SerializeFactory.getResourceUpdater();
 		
         //Set current user as reviewer
         setReviewer(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_USER_ID));
         
 		//Add root for Review Navigator
 		FRootElement = new R4EUIRootElement(null, "R4E");
 		
 		//Load Review Groups
 		final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		final String groupPaths = preferenceStore.getString(PreferenceConstants.P_GROUP_FILE_PATH);
 		final List<String> groupPathsList = UIUtils.parseStringList(groupPaths);
 		loadReviewGroups(groupPathsList);
 		final String ruleSetPaths = preferenceStore.getString(PreferenceConstants.P_RULE_SET_FILE_PATH);
 		final List<String> ruleSetPathsList = UIUtils.parseStringList(ruleSetPaths);
 		loadRuleSets(ruleSetPathsList);
 		
 		//Verify Mail Connectivity
 		FMailConnector = NotificationsCore.getFirstEnabled(MAIL_CONNECTOR_IDS);
 	}
 	
 	/**
 	 * Method loadReviewGroups.
 	 * @param aGroupPaths List<String>
 	 */
 	public static void loadReviewGroups(List<String> aGroupPaths) {
 		
 		boolean changePrefsPaths = false;
 		R4EReviewGroup reviewGroup = null;
 		final List<String> newGroupPaths = new ArrayList<String>();
 		newGroupPaths.addAll(aGroupPaths);
 		
         for (String groupPath : aGroupPaths) {
         	reviewGroup = null;
         	
         	//First try to open the review group file as entrered in the preferences
         	//If it fails, then create it
         	try {
         		reviewGroup = FModelExt.openR4EReviewGroup(URI.createFileURI(groupPath));
     			FRootElement.loadReviewGroup(reviewGroup);
         	} catch (ResourceHandlingException e) {
     			Activator.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 
         		//Review Group not found, ask user for review group creation
         		MessageDialog dialog = new MessageDialog( // $codepro.audit.disable variableDeclaredInLoop
         				null, "Missing Review Group", null, "Review Group file at location " + groupPath + 
         					" not found, remove it from Preferences?",
         				MessageDialog.WARNING,
         				new String[] {"Yes", "No"},
         				R4EUIConstants.DIALOG_NO); // no is the default
         		int result = dialog.open(); // $codepro.audit.disable variableDeclaredInLoop
 
         		if (result == R4EUIConstants.DIALOG_YES) {
         			changePrefsPaths = true;
         			newGroupPaths.remove(groupPath);
         		}      			
         	}
         }
         
         //Adjust review group paths in preferences
         if (changePrefsPaths) {
         	Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_GROUP_FILE_PATH, buildReviewGroupsStr(newGroupPaths));
         }
 	}
 	
 	/**
 	 * Method loadReviewGroups.
 	 * @param aRuleSetPaths List<String>
 	 */
 	public static void loadRuleSets(List<String> aRuleSetPaths) {
 		
 		boolean changePrefsPaths = false;
 		R4EDesignRuleCollection ruleSet = null;
 		final List<String> newRuleSetPaths = new ArrayList<String>();
 		newRuleSetPaths.addAll(aRuleSetPaths);
 		
         for (String ruleSetPath : aRuleSetPaths) {
         	ruleSet = null;
         	
         	//First try to open the review group file as entrered in the preferences
         	//If it fails, then create it
         	try {
         		ruleSet = FModelExt.openR4EDesignRuleCollection(URI.createFileURI(ruleSetPath));
     			FRootElement.loadRuleSet(ruleSet);
         	} catch (ResourceHandlingException e) {
     			Activator.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 
         		//Review Group not found, ask user for review group creation
         		MessageDialog dialog = new MessageDialog( // $codepro.audit.disable variableDeclaredInLoop
         				null, "Missing Rule Set", null, "Rule Set file at location " + ruleSetPath + 
         					" not found",
         				MessageDialog.WARNING,
         				new String[] {"Ok"},
         				R4EUIConstants.DIALOG_YES); // no is the default
         		int result = dialog.open(); // $codepro.audit.disable variableDeclaredInLoop
 
         		if (result == R4EUIConstants.DIALOG_YES) {
         			changePrefsPaths = true;
         			newRuleSetPaths.remove(ruleSetPath);
         		}      			
         	}
         }
         
         //Adjust review group paths in preferences
         if (changePrefsPaths) {
         	Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.
         			P_RULE_SET_FILE_PATH, buildReviewGroupsStr(newRuleSetPaths));
         }
 	}
 	
 	/**
 	 * Method buildReviewGroupsStr.
 	 * @param aReviewGroups String[]
 	 * @return String
 	 */
 	private static String buildReviewGroupsStr(List<String> aReviewGroups) {
 		final StringBuffer newPathsStr = new StringBuffer(R4EUIConstants.REVIEW_GROUP_PATHS_LENGTH);
         for (String group : aReviewGroups) {
         		newPathsStr.append(group + System.getProperty("line.separator"));
         }
         return newPathsStr.toString();
 	}
 	
 	/**
 	 * Method mapAnomalies.
 	 * 		Map anomalies to the files in which they are located
 	 * @param aReview R4EReview
 	 */
 	public static void mapAnomalies(R4EReview aReview) {
 		final EList<Topic> anomalies = aReview.getTopics();
 		Topic anomaly = null;
 		EList<Location> locations = null;
 		final int anomaliesSize = anomalies.size();
 		for (int i = 0; i < anomaliesSize; i++) {
 			anomaly = anomalies.get(i);
 			
 			locations = anomaly.getLocation();
 			for (Location location : locations) {
 				String targetFilePath = ((R4EAnomalyTextPosition)((R4EContent)location).getLocation()).getFile().getPlatformURI(); // $codepro.audit.disable variableDeclaredInLoop
 				if (FFileAnomalyMap.containsKey(targetFilePath)) {
 					//Add anomaly for this filename if not already present
 					List<R4EAnomaly> anomalyList = FFileAnomalyMap.get(targetFilePath); // $codepro.audit.disable variableDeclaredInLoop
 					if (!(anomalyList.contains(anomaly))) {
 						anomalyList.add((R4EAnomaly)anomaly);
 					}
 				} else {
 					List<R4EAnomaly> anomalyList = new ArrayList<R4EAnomaly>(); // $codepro.audit.disable variableDeclaredInLoop
 					anomalyList.add((R4EAnomaly)anomaly);
 					FFileAnomalyMap.put(targetFilePath, anomalyList);
 				}
 			}
 
 		}
 	}
 	
 	/**
 	 * Method clearAnomalyMap.
 	 */
 	public static void clearAnomalyMap() {
 		final Set<String> keys = FFileAnomalyMap.keySet();
 		for (String key: keys) {
 			List<R4EAnomaly> anomaliesList = FFileAnomalyMap.get(key);
 			anomaliesList.clear();
 		}
 		FFileAnomalyMap.clear();
 	}
 	
 	/**
 	 * Method getAnomaliesForFile.
 	 * 		Get list of anomalies for a given file
 	 * @param aFilePath String
 	 * @return List<R4EAnomaly>
 	 */
 	public static List<R4EAnomaly> getAnomaliesForFile(String aFilePath) {
 		return FFileAnomalyMap.get(aFilePath);
 	}
 	
 	/**
 	 * Method getReviewer.
 	 * @return R4EParticipant
 	 */
 	public static String getReviewer() {
 		return FReviewer;
 	}
 	
 	/**
 	 * Method setReviewer.
 	 * @param aReviewer R4EParticipant
 	 */
 	public static void setReviewer(String aReviewer) {
 		FReviewer = aReviewer;
 	}
 
 	/**
 	 * Method isUserQueryAvailable.
 	 * @return boolean\
 	 */
 	public static boolean isUserQueryAvailable() {
 		//Verify if the LDAP bundle is available
 		if (null != Platform.getBundle("org.eclipse.mylyn.reviews.ldap")) return true;
 		return false;
 	}
 	
 	/**
 	 * Method getMailConnector.
 	 * @return NotificationsConnector
 	 */
 	public static NotificationsConnector getMailConnector() {
 		return FMailConnector;
 	}
 }
