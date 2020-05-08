 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * shterev
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.views.historybrowserview;
 
 import org.eclipse.emf.emfstore.internal.client.ui.views.scm.SCMLabelProvider;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.HistoryInfo;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.LogMessage;
 import org.eclipse.jface.viewers.TreeNode;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * Column LabelProvider to show info from the log message, such as commiter, date, etc.
  * 
  * @author shterev
  */
 public class LogMessageColumnLabelProvider extends SCMLabelProvider {
 
 	/**
 	 * Default constructor.
 	 */
 	public LogMessageColumnLabelProvider() {
 		super();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getText(Object element) {
 		if (element instanceof TreeNode && !(((TreeNode) element).getValue() instanceof HistoryInfo)) {
			final String toolTipText = super.getToolTipText(element);
 			return toolTipText;
 		} else if (element instanceof HistoryInfo) {
 			LogMessage logMessage = null;
			final HistoryInfo historyInfo = (HistoryInfo) element;
			final StringBuilder builder = new StringBuilder();
			if (historyInfo.getPrimarySpec() != null && historyInfo.getPrimarySpec().getIdentifier() == -1) {
				builder.append(""); //$NON-NLS-1$
			} else if (historyInfo.getLogMessage() != null) {
 				logMessage = historyInfo.getLogMessage();
 			} else if (historyInfo.getChangePackage() != null && historyInfo.getChangePackage().getLogMessage() != null) {
 				logMessage = historyInfo.getChangePackage().getLogMessage();
 			}
 			if (logMessage != null) {
 				builder.append(logMessage.getMessage());
 			}
 			return builder.toString();
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Image getImage(Object element) {
 		return null;
 	}
 
 	@Override
 	public String getToolTipText(Object element) {
 		return getText(element);
 	}
 
 }
