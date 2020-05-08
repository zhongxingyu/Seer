 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.cdo.handlers;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IIntentLogger.LogType;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IntentLogger;
 import org.eclipse.mylyn.docs.intent.collab.common.repository.IntentRepositoryInitializer;
 
 /**
  * Handler that print a widget allowing the user to select the Intent element to open.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class InitializeContent extends AbstractHandler {
 
 	/**
 	 * constructor.
 	 */
 	public InitializeContent() {
 		super();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
 	 */
 	public Object execute(ExecutionEvent event) throws ExecutionException {
		IntentRepositoryInitializer.initializeContent("cdo:/localhost:2036/intent-server", "Document {}");
 		IntentLogger.getInstance().log(LogType.INFO, "Content correctly initialized.");
 		return null;
 	}
 }
