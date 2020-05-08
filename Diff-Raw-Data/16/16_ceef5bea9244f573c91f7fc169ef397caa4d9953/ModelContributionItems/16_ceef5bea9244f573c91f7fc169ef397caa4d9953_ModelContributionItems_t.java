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
  * This class implements contribution item that provide the model commands
  * to the review navigator context-sensitive menus
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   Jacques Bouthillier - Add contribution element for Report
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.internal.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.CompoundContributionItem;
 import org.eclipse.ui.menus.CommandContributionItem;
 import org.eclipse.ui.menus.CommandContributionItemParameter;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class ModelContributionItems extends CompoundContributionItem {
 
 	/**
 	 * Method getContributionItems.
 	 * 
 	 * @return IContributionItem[]
 	 */
 	@Override
 	protected IContributionItem[] getContributionItems() {
 
 		final List<IContributionItem> list = new ArrayList<IContributionItem>();
 		CommandContributionItemParameter params = null;
 		final IR4EUIModelElement element = (IR4EUIModelElement) ((IStructuredSelection) R4EUIModelController.getNavigatorView()
 				.getTreeViewer()
 				.getSelection()).getFirstElement();
 
 		if (null != element) {
 			if (element.isOpenElementCmd()) {
 
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.OPEN_ELEMENT_COMMAND, R4EUIConstants.OPEN_ELEMENT_COMMAND, null,
 						ImageDescriptor.createFromURL(R4EUIPlugin.getDefault()
 								.getBundle()
 								.getEntry(R4EUIConstants.OPEN_ELEMENT_ICON_FILE)), null, null,
 						element.getOpenElementCmdName(), R4EUIConstants.OPEN_ELEMENT_COMMAND_MNEMONIC,
 						element.getOpenElementCmdTooltip(), CommandContributionItem.STYLE_PUSH, null, true);
 				list.add(new CommandContributionItem(params));
 			}
 
 			if (element.isCloseElementCmd()) {
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.CLOSE_ELEMENT_COMMAND, R4EUIConstants.CLOSE_ELEMENT_COMMAND, null,
 						ImageDescriptor.createFromURL(R4EUIPlugin.getDefault()
 								.getBundle()
 								.getEntry(R4EUIConstants.CLOSE_ELEMENT_ICON_FILE)), null, null,
 						element.getCloseElementCmdName(), R4EUIConstants.CLOSE_ELEMENT_COMMAND_MNEMONIC,
 						element.getCloseElementCmdTooltip(), CommandContributionItem.STYLE_PUSH, null, true);
 				list.add(new CommandContributionItem(params));
 			}
 
 			if (element.isNextStateElementCmd()) {
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.NEXT_STATE_ELEMENT_COMMAND, R4EUIConstants.NEXT_STATE_ELEMENT_COMMAND, null,
 						ImageDescriptor.createFromURL(R4EUIPlugin.getDefault()
 								.getBundle()
 								.getEntry(R4EUIConstants.NEXT_STATE_ELEMENT_ICON_FILE)), null, null,
 						element.getNextStateElementCmdName(), R4EUIConstants.NEXT_STATE_ELEMENT_COMMAND_MNEMONIC,
 						element.getNextStateElementCmdTooltip(), CommandContributionItem.STYLE_PUSH, null, true);
 				list.add(new CommandContributionItem(params));
 			}
 
 			if (element.isPreviousStateElementCmd()) {
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.PREVIOUS_STATE_ELEMENT_COMMAND, R4EUIConstants.PREVIOUS_STATE_ELEMENT_COMMAND,
 						null, ImageDescriptor.createFromURL(R4EUIPlugin.getDefault()
 								.getBundle()
 								.getEntry(R4EUIConstants.PREVIOUS_STATE_ELEMENT_ICON_FILE)), null, null,
 						element.getPreviousStateElementCmdName(),
 						R4EUIConstants.PREVIOUS_STATE_ELEMENT_COMMAND_MNEMONIC,
 						element.getPreviousStateElementCmdTooltip(), CommandContributionItem.STYLE_PUSH, null, true);
 				list.add(new CommandContributionItem(params));
 			}
 
 			if (element.isNewChildElementCmd()) {
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.NEW_CHILD_ELEMENT_COMMAND, R4EUIConstants.NEW_CHILD_ELEMENT_COMMAND, null,
 						PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ADD),
 						null, null, element.getNewChildElementCmdName(),
 						R4EUIConstants.NEW_CHILD_ELEMENT_COMMAND_MNEMONIC, element.getNewChildElementCmdTooltip(),
 						CommandContributionItem.STYLE_PUSH, null, true);
 				list.add(new CommandContributionItem(params));
 			}
 
 			if (element.isRemoveElementCmd()) {
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.REMOVE_ELEMENT_COMMAND, R4EUIConstants.REMOVE_ELEMENT_COMMAND, null,
 						PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE),
 						null, null, element.getRemoveElementCmdName(), R4EUIConstants.REMOVE_ELEMENT_COMMAND_MNEMONIC,
 						element.getRemoveElementCmdTooltip(), CommandContributionItem.STYLE_PUSH, null, true);
 				list.add(new CommandContributionItem(params));
 			}
 
 			if (element.isAddLinkedAnomalyCmd()) {
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.NEW_LINKED_ANOMALY_COMMAND, R4EUIConstants.NEW_LINKED_ANOMALY_COMMAND, null,
 						PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ADD),
 						null, null, R4EUIConstants.NEW_LINKED_ANOMALY_NAME, R4EUIConstants.NEW_LINKED_ANOMALY_MNEMONIC,
 						R4EUIConstants.NEW_LINKED_ANOMALY_TOOLTIP, CommandContributionItem.STYLE_PUSH, null, true);
 				list.add(new CommandContributionItem(params));
 			}
 			if (element.isOpenEditorCmd() && R4EUIModelController.getNavigatorView().isEditorLinked()) {
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.OPEN_EDITOR_COMMAND, R4EUIConstants.OPEN_EDITOR_COMMAND, null,
 						ImageDescriptor.createFromURL(R4EUIPlugin.getDefault()
 								.getBundle()
 								.getEntry(R4EUIConstants.OPEN_EDITOR_ICON_FILE)), null, null,
 						R4EUIConstants.OPEN_EDITOR_COMMAND_NAME, R4EUIConstants.OPEN_EDITOR_COMMAND_MNEMONIC,
 						R4EUIConstants.OPEN_EDITOR_COMMAND_TOOLTIP, CommandContributionItem.STYLE_PUSH, null, true);
 				list.add(new CommandContributionItem(params));
 			}
 
 			if (element.isChangeUserReviewStateCmd()) {
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.CHANGE_REVIEW_STATE_COMMAND, R4EUIConstants.CHANGE_REVIEW_STATE_COMMAND, null,
 						ImageDescriptor.createFromURL(R4EUIPlugin.getDefault()
 								.getBundle()
 								.getEntry(R4EUIConstants.CHANGE_REVIEW_STATE_ICON_FILE)), null, null,
 						element.isUserReviewed()
 								? R4EUIConstants.UNMARK_REVIEW_STATE_COMMAND_NAME
 								: R4EUIConstants.MARK_REVIEW_STATE_COMMAND_NAME,
 						R4EUIConstants.CHANGE_REVIEW_STATE_COMMAND_MNEMONIC, element.isUserReviewed()
 								? R4EUIConstants.UNMARK_REVIEW_STATE_COMMAND_TOOLTIP
 								: R4EUIConstants.MARK_REVIEW_STATE_COMMAND_TOOLTIP, CommandContributionItem.STYLE_PUSH,
 						null, true);
 				list.add(new CommandContributionItem(params));
 			}
 
 			if (element.isRestoreElementCmd()) {
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.RESTORE_ELEMENT_COMMAND, R4EUIConstants.RESTORE_ELEMENT_COMMAND, null,
 						PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_UNDO),
 						null, null, element.getRestoreElementCmdName(),
 						R4EUIConstants.RESTORE_ELEMENT_COMMAND_MNEMONIC, element.getRestoreElementCmdTooltip(),
 						CommandContributionItem.STYLE_PUSH, null, true);
 				list.add(new CommandContributionItem(params));
 			}
 
 			if (element.isSendEmailCmd()) {
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.SEND_EMAIL_COMMAND, R4EUIConstants.SEND_EMAIL_COMMAND, null,
 						ImageDescriptor.createFromURL(R4EUIPlugin.getDefault()
 								.getBundle()
 								.getEntry(R4EUIConstants.SEND_EMAIL_ICON_FILE)), null, null,
 						R4EUIConstants.SEND_EMAIL_COMMAND_NAME, R4EUIConstants.SEND_EMAIL_COMMAND_MNEMONIC,
 						R4EUIConstants.SEND_EMAIL_COMMAND_TOOLTIP, CommandContributionItem.STYLE_PUSH, null, true);
 				list.add(new CommandContributionItem(params));
 			}
 
 			if (element.isImportPostponedCmd()) {
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.IMPORT_POSTPONED_COMMAND, R4EUIConstants.IMPORT_POSTPONED_COMMAND, null,
 						ImageDescriptor.createFromURL(R4EUIPlugin.getDefault()
 								.getBundle()
 								.getEntry(R4EUIConstants.IMPORT_POSTPONED_ICON_FILE)), null, null,
 						R4EUIConstants.IMPORT_POSTPONED_COMMAND_NAME, R4EUIConstants.IMPORT_POSTPONED_COMMAND_MNEMONIC,
 						R4EUIConstants.IMPORT_POSTPONED_COMMAND_TOOLTIP, CommandContributionItem.STYLE_PUSH, null, true);
 				list.add(new CommandContributionItem(params));
 			}
 
 			if (element.isReportElementCmd()) {
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.REPORT_ELEMENT_COMMAND, R4EUIConstants.REPORT_ELEMENT_COMMAND, null,
 						ImageDescriptor.createFromURL(R4EUIPlugin.getDefault()
 								.getBundle()
 								.getEntry(R4EUIConstants.REPORT_ELEMENT_ICON_FILE)), null, null,
 						element.getReportElementCmdName(), R4EUIConstants.REPORT_ELEMENT_COMMAND_MNEMONIC,
 						element.getReportElementCmdTooltip(), CommandContributionItem.STYLE_PUSH, null, true);
 				list.add(new CommandContributionItem(params));
 			}
 
 			if (element.isAssignToCmd()) {
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.ASSIGN_TO_COMMAND, R4EUIConstants.ASSIGN_TO_COMMAND, null,
 						ImageDescriptor.createFromURL(R4EUIPlugin.getDefault()
 								.getBundle()
 								.getEntry(R4EUIConstants.ASSIGN_TO_ICON_FILE)), null, null,
 						R4EUIConstants.ASSIGN_TO_COMMAND_NAME, R4EUIConstants.ASSIGN_TO_COMMAND_MNEMONIC,
 						R4EUIConstants.ASSIGN_TO_COMMAND_TOOLTIP, CommandContributionItem.STYLE_PUSH, null, true);
 				list.add(new CommandContributionItem(params));
 			}
 
 			if (element.isUnassignToCmd()) {
 				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 						R4EUIConstants.UNASSIGN_TO_COMMAND, R4EUIConstants.UNASSIGN_TO_COMMAND, null,
 						ImageDescriptor.createFromURL(R4EUIPlugin.getDefault()
 								.getBundle()
 								.getEntry(R4EUIConstants.UNASSIGN_TO_ICON_FILE)), null, null,
 						R4EUIConstants.UNASSIGN_TO_COMMAND_NAME, R4EUIConstants.UNASSIGN_TO_COMMAND_MNEMONIC,
 						R4EUIConstants.UNASSIGN_TO_COMMAND_TOOLTIP, CommandContributionItem.STYLE_PUSH, null, true);
 				list.add(new CommandContributionItem(params));
 			}
 		} else {
 			//When no element is selected, contribute add review group command
			if (R4EUIModelController.getNavigatorView().isDefaultDisplay()) {
				params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
						R4EUIConstants.NEW_CHILD_ELEMENT_COMMAND, R4EUIConstants.NEW_CHILD_ELEMENT_COMMAND, null,
						PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ADD),
						null, null, R4EUIConstants.NEW_REVIEW_GROUP_COMMAND_NAME,
						R4EUIConstants.NEW_CHILD_ELEMENT_COMMAND_MNEMONIC,
						R4EUIConstants.NEW_REVIEW_GROUP_COMMAND_TOOLTIP, CommandContributionItem.STYLE_PUSH, null, true);
				list.add(new CommandContributionItem(params));
			}
 		}
 		return list.toArray(new IContributionItem[list.size()]);
 	}
 }
