 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, explicitThisUsage
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
  * This class implements the Rule Violation element of the UI model
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
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.DRModelFactory;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRule;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleViolation;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.RuleInputDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorContentProvider;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.preferences.PreferenceConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.properties.general.RuleViolationProperties;
 import org.eclipse.ui.views.properties.IPropertySource;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EUIRuleViolation extends R4EUIModelElement {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field RULE_VIOLATION_ICON_FILE. (value is ""icons/obj16/ruleviolation_obj.gif"")
 	 */
 	public static final String RULE_VIOLATION_ICON_FILE = "icons/obj16/ruleviolation_obj.gif";
 
 	/**
 	 * Field NEW_CHILD_ELEMENT_COMMAND_NAME. (value is ""Add a New Rule"")
 	 */
 	private static final String NEW_CHILD_ELEMENT_COMMAND_NAME = "New Rule";
 
 	/**
 	 * Field NEW_CHILD_ELEMENT_COMMAND_TOOLTIP. (value is ""Add a New Rule to the Current Rule Violation"")
 	 */
 	private static final String NEW_CHILD_ELEMENT_COMMAND_TOOLTIP = "Add a New Rule to the Current Rule Violation";
 
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_NAME. (value is ""Disable Rule Violation"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_NAME = "Disable Rule Violation";
 
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_TOOLTIP. (value is ""Disable (and Optionally Remove) this Rule Violation from its
 	 * parent Rule Area"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_TOOLTIP = "Disable (and Optionally Remove) this Rule Violation"
 			+ " from its parent Rule Area";
 
 	/**
 	 * Field RESTORE_ELEMENT_COMMAND_NAME. (value is ""Restore Rule Violation"")
 	 */
 	private static final String RESTORE_ELEMENT_COMMAND_NAME = "Restore Rule Violation";
 
 	/**
 	 * Field RESTORE_ELEMENT_ACTION_TOOLTIP. (value is ""Restore this disabled Rule Violation"")
 	 */
 	private static final String RESTORE_ELEMENT_COMMAND_TOOLTIP = "Restore this disabled Rule Violation";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fGroup.
 	 */
 	protected R4EDesignRuleViolation fViolation;
 
 	/**
 	 * Field fRules.
 	 */
 	private final List<R4EUIRule> fRules;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EUIRuleViolation.
 	 * 
 	 * @param aParent
 	 *            IR4EUIModelElement
 	 * @param aViolation
 	 *            R4EDesignRuleViolation
 	 */
 	public R4EUIRuleViolation(IR4EUIModelElement aParent, R4EDesignRuleViolation aViolation) {
 		super(aParent, aViolation.getName());
 		fViolation = aViolation;
 		fRules = new ArrayList<R4EUIRule>();
 		setImage(RULE_VIOLATION_ICON_FILE);
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
 			return new RuleViolationProperties(this);
 		}
 		return null;
 	}
 
 	//Attributes
 
 	/**
 	 * Method getViolation.
 	 * 
 	 * @return R4EDesignRuleViolation
 	 */
 	public R4EDesignRuleViolation getViolation() {
 		return fViolation;
 	}
 
 	/**
 	 * Create a serialization model element object
 	 * 
 	 * @return the new serialization element object
 	 */
 	@Override
 	public ReviewComponent createChildModelDataElement() {
 		//Get comment from user and set it in model data
 		R4EDesignRule tempRule = null;
 		R4EUIModelController.setJobInProgress(true);
 		final RuleInputDialog dialog = new RuleInputDialog(R4EUIModelController.getNavigatorView(). // $codepro.audit.disable methodChainLength
 				getSite()
 				.getWorkbenchWindow()
 				.getShell());
 		final int result = dialog.open();
 		if (result == Window.OK) {
 			tempRule = DRModelFactory.eINSTANCE.createR4EDesignRule();
 			tempRule.setId(dialog.getIdValue());
 			tempRule.setTitle(dialog.getTitleValue());
 			tempRule.setDescription(dialog.getDescriptionValue());
 			tempRule.setRank(dialog.getRankValue());
 			tempRule.setClass(dialog.getClassValue());
 		} else if (result != Window.CANCEL) {
 			R4EUIModelController.setJobInProgress(false); //Enable commands in case of error
 		}
 		return tempRule;
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
 		return fRules.toArray(new R4EUIRule[fRules.size()]);
 	}
 
 	/**
 	 * Method getViolationList.
 	 * 
 	 * @return List<R4EUIRuleViolation>
 	 */
 	public List<R4EUIRule> getViolationList() {
 		return fRules;
 	}
 
 	/**
 	 * Method hasChildren.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#hasChildren()
 	 */
 	@Override
 	public boolean hasChildren() {
 		if (fRules.size() > 0) {
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
 		R4EUIRule rule = null;
 		final int ruleSize = fRules.size();
 		for (int i = 0; i < ruleSize; i++) {
 
 			rule = fRules.get(i);
 			rule.close();
 		}
 		fRules.clear();
 		fOpen = false;
 		removeListeners();
 	}
 
 	/**
 	 * Method open.
 	 */
 	@Override
 	public void open() {
 		final List<R4EDesignRule> rules = fViolation.getRules();
 		if (null != rules) {
 			R4EUIRule uiRule = null;
 			final int ruleSize = rules.size();
 			for (int i = 0; i < ruleSize; i++) {
 				if (rules.get(i).isEnabled()
 						|| R4EUIPlugin.getDefault()
 								.getPreferenceStore()
 								.getBoolean(PreferenceConstants.P_SHOW_DISABLED)) {
 					uiRule = new R4EUIRule(this, rules.get(i));
 					addChildren(uiRule);
 				}
 			}
 		}
 		fOpen = true;
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
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fViolation,
 				R4EUIModelController.getReviewer());
 		fViolation.setEnabled(true);
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
 		return fViolation.isEnabled();
 	}
 
 	/**
 	 * Method addChildren.
 	 * 
 	 * @param aChildToAdd
 	 *            IR4EUIModelElement
 	 */
 	@Override
 	public void addChildren(IR4EUIModelElement aChildToAdd) {
 		fRules.add((R4EUIRule) aChildToAdd);
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
 		final R4EDesignRule rule = R4EUIModelController.FModelExt.createR4EDesignRule(fViolation);
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(rule, R4EUIModelController.getReviewer());
 		rule.setId(((R4EDesignRule) aModelComponent).getId());
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		final R4EUIRule addedChild = new R4EUIRule(this, rule);
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
 
 		final R4EUIRule removedElement = fRules.get(fRules.indexOf(aChildToRemove));
 		/* TODO uncomment when core model supports hard-removing of elements
 		if (aFileRemove) removedElement.getRule().remove());
 		else */
 		final R4EDesignRule modelRule = removedElement.getRule();
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelRule,
 				R4EUIModelController.getReviewer());
 		modelRule.setEnabled(false);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 		//Remove element from UI if the show disabled element option is off
 		if (!(R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 			fRules.remove(removedElement);
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
 		for (R4EUIRule rule : fRules) {
 			removeChildren(rule, aFileRemove);
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
 		if (null != fRules) {
 			R4EUIRule element = null;
 			for (final Iterator<R4EUIRule> iterator = fRules.iterator(); iterator.hasNext();) {
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
 		if (null != fRules) {
 			R4EUIRule element = null;
 			for (final Iterator<R4EUIRule> iterator = fRules.iterator(); iterator.hasNext();) {
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
 	 * Method isRemoveElementCmd.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#isRemoveElementCmd()
 	 */
 	@Override
 	public boolean isRemoveElementCmd() {
 		if (isEnabled()) {
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
 		if (isEnabled()) {
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
 }
