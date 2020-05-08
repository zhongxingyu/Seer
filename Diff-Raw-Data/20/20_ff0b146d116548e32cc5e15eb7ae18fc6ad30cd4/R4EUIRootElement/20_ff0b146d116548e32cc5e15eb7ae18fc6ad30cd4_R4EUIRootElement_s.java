 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
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
  * This class implements the root R4E element of the UI model
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.frame.core.model.ReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.core.model.RModelFactory;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.DRModelFactory;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleCollection;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.ReviewGroupInputDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.RuleSetInputDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorContentProvider;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.preferences.PreferenceConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EUIRootElement extends R4EUIModelElement {
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fReviewGroups.
 	 */
 	private final List<R4EUIReviewGroup> fReviewGroups;
 
 	/**
 	 * Field fRuleSets.
 	 */
 	private final List<R4EUIRuleSet> fRuleSets;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EElement.
 	 * 
 	 * @param aParent
 	 *            IR4EUIModelElement
 	 * @param aName
 	 *            String
 	 */
 	public R4EUIRootElement(IR4EUIModelElement aParent, String aName) {
 		super(aParent, aName);
 		fReviewGroups = new ArrayList<R4EUIReviewGroup>();
 		fRuleSets = new ArrayList<R4EUIRuleSet>();
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Create a serialization model element object
 	 * 
 	 * @return the new serialization element object
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 */
 	@Override
 	public ReviewComponent createChildModelDataElement() {
 		//Get comment from user and set it in model data
 		R4EReviewGroup tempReviewGroup = null;
 		R4EUIModelController.setJobInProgress(true);
 		final ReviewGroupInputDialog dialog = new ReviewGroupInputDialog(R4EUIModelController.getNavigatorView(). // $codepro.audit.disable methodChainLength
 				getSite()
 				.getWorkbenchWindow()
 				.getShell(), this);
 		dialog.create();
 		final int result = dialog.open();
 		if (result == Window.OK) {
 			tempReviewGroup = RModelFactory.eINSTANCE.createR4EReviewGroup();
 			tempReviewGroup.setName(dialog.getGroupNameValue());
 			tempReviewGroup.setDescription(dialog.getGroupDescriptionValue());
 			tempReviewGroup.setFolder(dialog.getGroupFolderValue());
 			for (String project : dialog.getAvailableProjectsValues()) {
 				tempReviewGroup.getAvailableProjects().add(project);
 			}
 			for (String component : dialog.getAvailableComponentsValues()) {
 				tempReviewGroup.getAvailableComponents().add(component);
 			}
 			for (String ruleSetLocation : dialog.getRuleSetValues()) {
 				tempReviewGroup.getDesignRuleLocations().add(ruleSetLocation);
 			}
 			tempReviewGroup.setDefaultEntryCriteria(dialog.getDefaultEntryCriteriaValue());
 		} else if (result != Window.CANCEL) {
 			R4EUIModelController.setJobInProgress(false); //Enable commands in case of error
 		}
 		return tempReviewGroup;
 	}
 
 	/**
 	 * Create a serialization model Rule Set element object
 	 * 
 	 * @return the new serialization element object
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 */
 	public ReviewComponent createRuleSetElement() {
 		//Get comment from user and set it in model data
 		R4EDesignRuleCollection tempRuleSet = null;
 		R4EUIModelController.setJobInProgress(true);
 		final RuleSetInputDialog dialog = new RuleSetInputDialog(R4EUIModelController.getNavigatorView(). // $codepro.audit.disable methodChainLength
 				getSite()
 				.getWorkbenchWindow()
 				.getShell());
 		dialog.create();
 		final int result = dialog.open();
 		if (result == Window.OK) {
 			tempRuleSet = DRModelFactory.eINSTANCE.createR4EDesignRuleCollection();
 			tempRuleSet.setVersion(dialog.getVersionValue());
 			tempRuleSet.setFolder(dialog.getFolderValue());
 			tempRuleSet.setName(dialog.getNameValue());
 		} else if (result != Window.CANCEL) {
 			R4EUIModelController.setJobInProgress(false); //Enable commands in case of error
 		}
 		return tempRuleSet;
 	}
 
 	/**
 	 * Close the model element (i.e. disable it)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#close()
 	 */
 	@Override
 	public void close() {
 		//Remove all children references
 		R4EUIReviewGroup reviewGroup = null;
 		final int reviewGroupsSize = fReviewGroups.size();
 		for (int i = 0; i < reviewGroupsSize; i++) {
 
 			reviewGroup = fReviewGroups.get(i);
 			if (!reviewGroup.isOpen()) {
 				continue; //skip reviews groups that are already closed
 			}
 			reviewGroup.close();
 			reviewGroup.removeListeners();
 			fireRemove(reviewGroup);
 		}
 		fReviewGroups.clear();
 
 		R4EUIRuleSet ruleSet = null;
 		final int ruleSetSize = fRuleSets.size();
 		for (int i = 0; i < ruleSetSize; i++) {
 
 			ruleSet = fRuleSets.get(i);
 			if (!ruleSet.isOpen()) {
 				continue; //skip rule sets that are already closed
 			}
 			ruleSet.close();
 			ruleSet.removeListeners();
 			fireRemove(ruleSet);
 		}
 		fRuleSets.clear();
 		fOpen = false;
 	}
 
 	/**
 	 * Method getRuleSets
 	 * 
 	 * @return List<R4EUIRuleSet>
 	 */
 	public List<R4EUIRuleSet> getRuleSets() {
 		return fRuleSets;
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
 		final List<IR4EUIModelElement> newList = new ArrayList<IR4EUIModelElement>();
 		for (R4EUIReviewGroup group : fReviewGroups) {
 			newList.add(group);
 		}
 		for (R4EUIRuleSet ruleSet : fRuleSets) {
 			newList.add(ruleSet);
 		}
 		return newList.toArray(new IR4EUIModelElement[newList.size()]);
 	}
 
 	/**
 	 * Method getGroups.
 	 * 
 	 * @return R4EUIReviewGroup[]
 	 */
 	public R4EUIReviewGroup[] getGroups() {
 		return fReviewGroups.toArray(new R4EUIReviewGroup[fReviewGroups.size()]);
 	}
 
 	/**
 	 * Method hasChildren.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#hasChildren()
 	 */
 	@Override
 	public boolean hasChildren() {
 		if (0 == fReviewGroups.size() && 0 == fRuleSets.size()) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Method loadReviewGroup.
 	 * 
 	 * @param aGroup
 	 *            R4EReviewGroup
 	 * @throws ResourceHandlingException
 	 */
 	public void loadReviewGroup(R4EReviewGroup aGroup) {
 		if (aGroup.isEnabled()
 				|| R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED)) {
 			final R4EUIReviewGroup addedChild = new R4EUIReviewGroup(this, aGroup, false);
 			addChildren(addedChild);
 		}
 	}
 
 	/**
 	 * Method loadRuleSet.
 	 * 
 	 * @param aRuleSet
 	 *            R4EDesignRuleCollection
 	 * @throws ResourceHandlingException
 	 */
 	public void loadRuleSet(R4EDesignRuleCollection aRuleSet) {
 		if (aRuleSet.isEnabled()
 				|| R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED)) {
 			final R4EUIRuleSet addedChild = new R4EUIRuleSet(this, aRuleSet, false);
 			addChildren(addedChild);
 			addedChild.close();
 		}
 	}
 
 	/**
 	 * Method addChildren.
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
 
 		if (aModelComponent instanceof R4EReviewGroup) {
 			final String groupName = ((R4EReviewGroup) aModelComponent).getName();
 			//Check if group already exists.  If so it cannot be recreated
 			for (R4EUIReviewGroup group : fReviewGroups) {
 				if (group.getReviewGroup().getName().equals(groupName)) {
 					final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 							"Error while creating new Review Group ", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID,
 									0, "Review Group " + groupName + " already exists", null), IStatus.ERROR);
 					dialog.open();
 					return null;
 				}
 			}
 
 			final R4EReviewGroup reviewGroup = R4EUIModelController.FModelExt.createR4EReviewGroup(
 					URI.createFileURI(((R4EReviewGroup) aModelComponent).getFolder()), groupName);
 			final R4EUIReviewGroup addedChild = new R4EUIReviewGroup(this, reviewGroup, true);
 			addedChild.setModelData(aModelComponent);
 			addChildren(addedChild);
 
 			final IPreferenceStore preferenceStore = R4EUIPlugin.getDefault().getPreferenceStore();
 			final String newGroup = reviewGroup.eResource().getURI().toFileString();
 			final String prefGroupsStr = preferenceStore.getString(PreferenceConstants.P_GROUP_FILE_PATH);
 			final String[] prefGroups = prefGroupsStr.split(System.getProperty("line.separator"));
 			for (String prefGroup : prefGroups) {
 				if (prefGroup.equals(newGroup + ";")) {
 					return addedChild; //Do not put group reference in preferences if it is already there
 				}
 			}
 
 			preferenceStore.setValue(
 					PreferenceConstants.P_GROUP_FILE_PATH,
 					preferenceStore.getString(PreferenceConstants.P_GROUP_FILE_PATH)
 							+ System.getProperty("line.separator") + newGroup);
 			return addedChild;
 		} else if (aModelComponent instanceof R4EDesignRuleCollection) {
 			final String ruleSetName = ((R4EDesignRuleCollection) aModelComponent).getName();
 			//Check if group already exists.  If so it cannot be recreated
 			for (R4EUIRuleSet ruleSet : fRuleSets) {
 				if (ruleSet.getRuleSet().getName().equals(ruleSetName)) {
 					final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 							"Error while creating new Rule Set  ", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
 									"Rule Set " + ruleSetName + " already exists", null), IStatus.ERROR);
 					dialog.open();
 					return null;
 				}
 			}
 
 			final R4EDesignRuleCollection ruleSet = R4EUIModelController.FModelExt.createR4EDesignRuleCollection(
 					URI.createFileURI(((R4EDesignRuleCollection) aModelComponent).getFolder()), ruleSetName);
 			final R4EUIRuleSet addedChild = new R4EUIRuleSet(this, ruleSet, true);
 			addedChild.setModelData(aModelComponent);
 			addChildren(addedChild);
 
 			final IPreferenceStore preferenceStore = R4EUIPlugin.getDefault().getPreferenceStore();
 			final String newSet = ruleSet.eResource().getURI().toFileString();
 			final String prefSetsStr = preferenceStore.getString(PreferenceConstants.P_RULE_SET_FILE_PATH);
 			final String[] prefSets = prefSetsStr.split(System.getProperty("line.separator"));
 			for (String prefSet : prefSets) {
 				if (prefSet.equals(newSet)) {
 					return addedChild; //Do not put group reference in preferences if it is already there
 				}
 			}
 
 			preferenceStore.setValue(
 					PreferenceConstants.P_RULE_SET_FILE_PATH,
 					preferenceStore.getString(PreferenceConstants.P_RULE_SET_FILE_PATH)
 							+ System.getProperty("line.separator") + newSet);
 			return addedChild;
 		} else {
 			return null; //should never happen
 		}
 
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
 		if (aChildToAdd instanceof R4EUIReviewGroup) {
 			fReviewGroups.add((R4EUIReviewGroup) aChildToAdd);
 		} else if (aChildToAdd instanceof R4EUIRuleSet) {
 			fRuleSets.add((R4EUIRuleSet) aChildToAdd);
 		} else {
 			return;
 		}
 		aChildToAdd.addListener((ReviewNavigatorContentProvider) R4EUIModelController.getNavigatorView()
 				.getTreeViewer()
 				.getContentProvider());
 		fireAdd(aChildToAdd);
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
 		if (aChildToRemove instanceof R4EUIReviewGroup) {
 			final R4EUIReviewGroup removedElement = fReviewGroups.get(fReviewGroups.indexOf(aChildToRemove));
 
 			//Also recursively remove all children 
 			removedElement.removeAllChildren(aFileRemove);
 
 			/* TODO uncomment when core model supports hard-removing of elements
 			if (aFileRemove) removedElement.getReviewGroup().remove());
 			else */
 			removedElement.setEnabled(false);
 
 			//Remove element from UI if the show disabled element option is off
 			if (!(R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 				fReviewGroups.remove(removedElement);
 				aChildToRemove.removeListeners();
 				fireRemove(aChildToRemove);
 			}
 		} else if (aChildToRemove instanceof R4EUIRuleSet) {
 			final R4EUIRuleSet removedElement = fRuleSets.get(fRuleSets.indexOf(aChildToRemove));
 
 			//Verify if the rule set is referred to by any review group
 			for (R4EUIReviewGroup group : getGroups()) {
 				if (group.getRuleSets().contains(removedElement)) {
 					final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 							"Rule Set cannot be removed because it is being used", new Status(IStatus.ERROR,
 									R4EUIPlugin.PLUGIN_ID, "Rule Set used in Group " + group.getName()), IStatus.ERROR);
 					dialog.open();
 					return;
 				}
 			}
 
 			//Also recursively remove all children 
 			removedElement.removeAllChildren(aFileRemove);
 
 			/* TODO uncomment when core model supports hard-removing of elements
 			if (aFileRemove) removedElement.getRuleSet().remove());
 			else */
 			removedElement.setEnabled(false);
 
 			//Remove element from UI if the show disabled element option is off
 			if (!(R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 				fRuleSets.remove(removedElement);
 				aChildToRemove.removeListeners();
 				fireRemove(aChildToRemove);
 			}
 		}
 	}
 
 	/**
 	 * Method removeChildrenFromUI.
 	 * 
 	 * @param aChildToRemove
 	 *            IR4EUIModelElement
 	 * @throws OutOfSyncException
 	 * @throws ResourceHandlingException
 	 */
 	public void removeChildrenFromUI(IR4EUIModelElement aChildToRemove) throws ResourceHandlingException,
 			OutOfSyncException {
 		if (aChildToRemove instanceof R4EUIReviewGroup) {
 			final R4EUIReviewGroup removedElement = fReviewGroups.get(fReviewGroups.indexOf(aChildToRemove));
			removedElement.removeAllChildren(false);
 			fReviewGroups.remove(removedElement);
 		} else {
 			final R4EUIRuleSet removedElement = fRuleSets.get(fRuleSets.indexOf(aChildToRemove));
			removedElement.removeAllChildren(false);
 			fRuleSets.remove(removedElement);
 		}
 		aChildToRemove.removeListeners();
 		fireRemove(aChildToRemove);
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
 		for (R4EUIReviewGroup group : fReviewGroups) {
 			removeChildren(group, aFileRemove);
 		}
 		for (R4EUIRuleSet ruleSet : fRuleSets) {
 			removeChildren(ruleSet, aFileRemove);
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
 		if (null != fReviewGroups) {
 			R4EUIReviewGroup element = null;
 			for (final Iterator<R4EUIReviewGroup> iterator = fReviewGroups.iterator(); iterator.hasNext();) {
 				element = iterator.next();
 				element.addListener(aProvider);
 			}
 		}
 		if (null != fRuleSets) {
 			R4EUIRuleSet element = null;
 			for (final Iterator<R4EUIRuleSet> iterator = fRuleSets.iterator(); iterator.hasNext();) {
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
 		if (null != fReviewGroups) {
 			R4EUIReviewGroup element = null;
 			for (final Iterator<R4EUIReviewGroup> iterator = fReviewGroups.iterator(); iterator.hasNext();) {
 				element = iterator.next();
 				element.removeListener(aProvider);
 			}
 		}
 		if (null != fRuleSets) {
 			R4EUIRuleSet element = null;
 			for (final Iterator<R4EUIRuleSet> iterator = fRuleSets.iterator(); iterator.hasNext();) {
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
 		if (isEnabled()) {
 			return true;
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
 		return R4EUIConstants.NEW_CHILD_ELEMENT_COMMAND_NAME;
 	}
 
 	/**
 	 * Method getAddChildElementCmdTooltip.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#getNewChildElementCmdTooltip()
 	 */
 	@Override
 	public String getNewChildElementCmdTooltip() {
 		return R4EUIConstants.NEW_CHILD_ELEMENT_COMMAND_TOOLTIP;
 	}
 }
