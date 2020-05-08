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
  * This class implements the Rule Area element of the UI model
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
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleArea;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleViolation;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.RuleViolationInputDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorContentProvider;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.preferences.PreferenceConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.properties.general.RuleAreaProperties;
 import org.eclipse.ui.views.properties.IPropertySource;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EUIRuleArea extends R4EUIModelElement {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field RULE_AREA_ICON_FILE. (value is ""icons/obj16/rulearea_obj.gif"")
 	 */
 	public static final String RULE_AREA_ICON_FILE = "icons/obj16/rulearea_obj.gif";
 
 	/**
 	 * Field NEW_CHILD_ELEMENT_COMMAND_NAME. (value is ""Add a New Rule Violation"")
 	 */
 	private static final String NEW_CHILD_ELEMENT_COMMAND_NAME = "New Rule Violation";
 
 	/**
 	 * Field NEW_CHILD_ELEMENT_COMMAND_TOOLTIP. (value is ""Add a New Rule Area to the Current Rule Set"")
 	 */
 	private static final String NEW_CHILD_ELEMENT_COMMAND_TOOLTIP = "Add a New Rule Violation to the Current Rule Area";
 
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_NAME. (value is ""Disable Rule Area"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_NAME = "Disable Rule Area";
 
 	/**
 	 * Field REMOVE_ELEMENT_ACTION_TOOLTIP. (value is ""Disable (and Optionally Remove) this Rule Area from its parent
 	 * Rule Set"")
 	 */
 	private static final String REMOVE_ELEMENT_COMMAND_TOOLTIP = "Disable (and Optionally Remove) this Rule Area"
 			+ " from its parent Rule Set";
 
 	/**
 	 * Field RESTORE_ELEMENT_COMMAND_NAME. (value is ""Restore Rule Area"")
 	 */
 	private static final String RESTORE_ELEMENT_COMMAND_NAME = "Restore Rule Area";
 
 	/**
 	 * Field RESTORE_ELEMENT_ACTION_TOOLTIP. (value is ""Restore this disabled Rule Area"")
 	 */
 	private static final String RESTORE_ELEMENT_COMMAND_TOOLTIP = "Restore this disabled Rule Area";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fGroup.
 	 */
 	protected R4EDesignRuleArea fArea;
 
 	/**
 	 * Field fViolations.
 	 */
 	private final List<R4EUIRuleViolation> fViolations;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EUIRuleArea.
 	 * 
 	 * @param aParent
 	 *            IR4EUIModelElement
 	 * @param aArea
 	 *            R4EDesignRuleArea
 	 */
 	public R4EUIRuleArea(IR4EUIModelElement aParent, R4EDesignRuleArea aArea) {
 		super(aParent, aArea.getName());
 		fArea = aArea;
 		fViolations = new ArrayList<R4EUIRuleViolation>();
 		setImage(RULE_AREA_ICON_FILE);
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
 			return new RuleAreaProperties(this);
 		}
 		return null;
 	}
 
 	//Attributes
 
 	/**
 	 * Method getArea.
 	 * 
 	 * @return R4EDesignRuleArea
 	 */
 	public R4EDesignRuleArea getArea() {
 		return fArea;
 	}
 
 	/**
 	 * Create a serialization model element object
 	 * 
 	 * @return the new serialization element object
 	 */
 	@Override
 	public ReviewComponent createChildModelDataElement() {
 		//Get comment from user and set it in model data
 		R4EDesignRuleViolation tempViolation = null;
 		R4EUIModelController.setJobInProgress(true);
 		final RuleViolationInputDialog dialog = new RuleViolationInputDialog(R4EUIModelController.getNavigatorView(). // $codepro.audit.disable methodChainLength
 				getSite()
 				.getWorkbenchWindow()
 				.getShell());
 		final int result = dialog.open();
 		if (result == Window.OK) {
 			tempViolation = DRModelFactory.eINSTANCE.createR4EDesignRuleViolation();
 			tempViolation.setName(dialog.getNameValue());
 		} else if (result != Window.CANCEL) {
 			R4EUIModelController.setJobInProgress(false); //Enable commands in case of error
 		}
 		return tempViolation;
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
 		return fViolations.toArray(new R4EUIRuleViolation[fViolations.size()]);
 	}
 
 	/**
 	 * Method getViolationList.
 	 * 
 	 * @return List<R4EUIRuleViolation>
 	 */
 	public List<R4EUIRuleViolation> getViolationList() {
 		return fViolations;
 	}
 
 	/**
 	 * Method hasChildren.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement#hasChildren()
 	 */
 	@Override
 	public boolean hasChildren() {
 		if (fViolations.size() > 0) {
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
 		R4EUIRuleViolation violation = null;
 		final int violationSize = fViolations.size();
 		for (int i = 0; i < violationSize; i++) {
 
 			violation = fViolations.get(i);
 			violation.close();
 		}
 		fViolations.clear();
 		fOpen = false;
 		removeListeners();
 	}
 
 	/**
 	 * Method open.
 	 */
 	@Override
 	public void open() {
 		final List<R4EDesignRuleViolation> violations = fArea.getViolations();
 		if (null != violations) {
 			R4EUIRuleViolation uiViolation = null;
 			final int violationSize = violations.size();
 			for (int i = 0; i < violationSize; i++) {
 				if (violations.get(i).isEnabled()
 						|| R4EUIPlugin.getDefault()
 								.getPreferenceStore()
 								.getBoolean(PreferenceConstants.P_SHOW_DISABLED)) {
 					uiViolation = new R4EUIRuleViolation(this, violations.get(i));
 					addChildren(uiViolation);
 					if (uiViolation.isEnabled()) {
 						uiViolation.open();
 					}
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
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(fArea, R4EUIModelController.getReviewer());
 		fArea.setEnabled(true);
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
 		return fArea.isEnabled();
 	}
 
 	/**
 	 * Method addChildren.
 	 * 
 	 * @param aChildToAdd
 	 *            IR4EUIModelElement
 	 */
 	@Override
 	public void addChildren(IR4EUIModelElement aChildToAdd) {
 		fViolations.add((R4EUIRuleViolation) aChildToAdd);
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
 		final R4EDesignRuleViolation violation = R4EUIModelController.FModelExt.createR4EDesignRuleViolation(fArea);
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(violation,
 				R4EUIModelController.getReviewer());
 		violation.setName(((R4EDesignRuleViolation) aModelComponent).getName());
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 		final R4EUIRuleViolation addedChild = new R4EUIRuleViolation(this, violation);
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
 
 		final R4EUIRuleViolation removedElement = fViolations.get(fViolations.indexOf(aChildToRemove));
 		/* TODO uncomment when core model supports hard-removing of elements
 		if (aFileRemove) removedElement.getViolation().remove());
 		else */
 		final R4EDesignRuleViolation modelViolation = removedElement.getViolation();
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelViolation,
 				R4EUIModelController.getReviewer());
 		modelViolation.setEnabled(false);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 		//Remove element from UI if the show disabled element option is off
 		if (!(R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 			fViolations.remove(removedElement);
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
 		for (R4EUIRuleViolation area : fViolations) {
 			removeChildren(area, aFileRemove);
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
 		if (null != fViolations) {
 			R4EUIRuleViolation element = null;
 			for (final Iterator<R4EUIRuleViolation> iterator = fViolations.iterator(); iterator.hasNext();) {
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
 		if (null != fViolations) {
 			R4EUIRuleViolation element = null;
 			for (final Iterator<R4EUIRuleViolation> iterator = fViolations.iterator(); iterator.hasNext();) {
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
		if (getParent().isEnabled()) {
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
