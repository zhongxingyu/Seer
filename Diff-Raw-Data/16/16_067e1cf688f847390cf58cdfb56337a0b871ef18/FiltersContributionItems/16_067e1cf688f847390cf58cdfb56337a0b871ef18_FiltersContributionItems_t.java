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
  * This class implements contribution item that provide the filter commands
  * to the review navigator toolbar filters menu
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.internal.commands.filters;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorActionGroup;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.ui.actions.CompoundContributionItem;
 import org.eclipse.ui.menus.CommandContributionItem;
 import org.eclipse.ui.menus.CommandContributionItemParameter;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class FiltersContributionItems extends CompoundContributionItem {
 
 	/**
 	 * Method getContributionItems.
 	 * 
 	 * @return IContributionItem[]
 	 */
 	@Override
 	protected IContributionItem[] getContributionItems() {
 
 		final List<IContributionItem> list = new ArrayList<IContributionItem>();
 
 		CommandContributionItemParameter params = new CommandContributionItemParameter(
 				R4EUIModelController.getNavigatorView().getSite(), R4EUIConstants.REVIEWS_COMPLETED_FILTER_COMMAND,
 				R4EUIConstants.REVIEWS_COMPLETED_FILTER_COMMAND, null, null, null, null,
 				R4EUIConstants.REVIEWS_COMPLETED_FILTER_NAME, R4EUIConstants.REVIEWS_COMPLETED_FILTER_MNEMONIC,
 				R4EUIConstants.REVIEWS_COMPLETED_FILTER_TOOLTIP, CommandContributionItem.STYLE_CHECK, null, true);
 		list.add(new CommandContributionItem(params));
 
 		params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 				R4EUIConstants.REVIEWS_ONLY_FILTER_COMMAND, R4EUIConstants.REVIEWS_ONLY_FILTER_COMMAND, null, null,
 				null, null, R4EUIConstants.REVIEWS_ONLY_FILTER_NAME, R4EUIConstants.REVIEWS_ONLY_FILTER_MNEMONIC,
 				R4EUIConstants.REVIEWS_ONLY_FILTER_TOOLTIP, CommandContributionItem.STYLE_CHECK, null, true);
 		list.add(new CommandContributionItem(params));
 
 		params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 				R4EUIConstants.REVIEWS_MY_FILTER_COMMAND, R4EUIConstants.REVIEWS_MY_FILTER_COMMAND, null, null, null,
 				null, R4EUIConstants.REVIEWS_MY_FILTER_NAME, R4EUIConstants.REVIEWS_MY_FILTER_MNEMONIC,
 				R4EUIConstants.REVIEWS_MY_FILTER_TOOLTIP, CommandContributionItem.STYLE_CHECK, null, true);
 		list.add(new CommandContributionItem(params));
 
 		final String participant = ((ReviewNavigatorActionGroup) R4EUIModelController.getNavigatorView().getActionSet()).getFilterParticipant();
 		params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 				R4EUIConstants.REVIEWS_PARTICIPANT_FILTER_COMMAND, R4EUIConstants.REVIEWS_PARTICIPANT_FILTER_COMMAND,
 				null, null, null, null, R4EUIConstants.REVIEWS_PARTICIPANT_FILTER_NAME
 						+ (!participant.equals("") ? " (" + participant + ") " : ""),
 				R4EUIConstants.REVIEWS_PARTICIPANT_FILTER_MNEMONIC, R4EUIConstants.REVIEWS_PARTICIPANT_FILTER_TOOLTIP,
 				CommandContributionItem.STYLE_CHECK, null, true);
 		list.add(new CommandContributionItem(params));
 
 		params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 				R4EUIConstants.ANOMALIES_FILTER_COMMAND, R4EUIConstants.ANOMALIES_FILTER_COMMAND, null, null, null,
 				null, R4EUIConstants.ANOMALIES_FILTER_NAME, R4EUIConstants.ANOMALIES_FILTER_MNEMONIC,
 				R4EUIConstants.ANOMALIES_FILTER_TOOLTIP, CommandContributionItem.STYLE_CHECK, null, true);
 		list.add(new CommandContributionItem(params));
 
 		params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 				R4EUIConstants.ANOMALIES_MY_FILTER_COMMAND, R4EUIConstants.ANOMALIES_MY_FILTER_COMMAND, null, null,
 				null, null, R4EUIConstants.ANOMALIES_MY_FILTER_NAME, R4EUIConstants.ANOMALIES_MY_FILTER_MNEMONIC,
 				R4EUIConstants.ANOMALIES_MY_FILTER_TOOLTIP, CommandContributionItem.STYLE_CHECK, null, true);
 		list.add(new CommandContributionItem(params));
 
 		params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 				R4EUIConstants.REVIEWED_ELEMS_FILTER_COMMAND, R4EUIConstants.REVIEWED_ELEMS_FILTER_COMMAND, null, null,
 				null, null, R4EUIConstants.REVIEWED_ELEMS_FILTER_NAME, R4EUIConstants.REVIEWED_ELEMS_FILTER_MNEMONIC,
 				R4EUIConstants.REVIEWED_ELEMS_FILTER_TOOLTIP, CommandContributionItem.STYLE_CHECK, null, true);
 		list.add(new CommandContributionItem(params));
 
 		params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 				R4EUIConstants.HIDE_RULE_SETS_FILTER_COMMAND, R4EUIConstants.HIDE_RULE_SETS_FILTER_COMMAND, null, null,
 				null, null, R4EUIConstants.HIDE_RULE_SETS_FILTER_NAME, R4EUIConstants.HIDE_RULE_SETS_FILTER_MNEMONIC,
 				R4EUIConstants.HIDE_RULE_SETS_FILTER_TOOLTIP, CommandContributionItem.STYLE_CHECK, null, true);
 		list.add(new CommandContributionItem(params));
 
 		params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 				R4EUIConstants.HIDE_DELTAS_FILTER_COMMAND, R4EUIConstants.HIDE_DELTAS_FILTER_COMMAND, null, null, null,
 				null, R4EUIConstants.HIDE_DELTAS_FILTER_NAME, R4EUIConstants.HIDE_DELTAS_FILTER_MNEMONIC,
 				R4EUIConstants.HIDE_DELTAS_FILTER_TOOLTIP, CommandContributionItem.STYLE_CHECK, null, true);
 		list.add(new CommandContributionItem(params));
 
 		params = new CommandContributionItemParameter(R4EUIModelController.getNavigatorView().getSite(),
 				R4EUIConstants.REMOVE_ALL_FILTER_COMMAND, R4EUIConstants.REMOVE_ALL_FILTER_COMMAND, null, null, null,
 				null, R4EUIConstants.REMOVE_ALL_FILTER_NAME, R4EUIConstants.REMOVE_ALL_FILTER_MNEMONIC,
 				R4EUIConstants.REMOVE_ALL_FILTER_TOOLTIP, CommandContributionItem.STYLE_PUSH, null, true);
 		list.add(new CommandContributionItem(params));
 
 		return list.toArray(new IContributionItem[list.size()]);
 	}
 
 }
