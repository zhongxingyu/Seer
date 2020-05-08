 /*******************************************************************************
  * Copyright (c) 2011 Sam Hanes and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sam Hanes - initial API and implementation
  *******************************************************************************/
 
 package com.maltera.mylyn.internal.rt.ui.wizard;
 
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
 import org.eclipse.swt.widgets.Composite;
 
 import com.maltera.mylyn.internal.rt.core.RtCorePlugin;
 
 /**
  * @author Sam Hanes
  *
  */
 public class RtRepositorySettingsPage extends AbstractRepositorySettingsPage {
 
 	private static final String TITLE = Messages.RtRepositorySettingsPage_RT_Repository_Settings;
 
 	private static final String DESCRIPTION = Messages.RtRepositorySettingsPage_EXAMPLE_BESTPRACTICAL;
 	
 	public RtRepositorySettingsPage(TaskRepository taskRepository) {
 		super(TITLE, DESCRIPTION, taskRepository);
 		setNeedsCertAuth(true);
 		setNeedsAnonymousLogin(true);
 		setNeedsEncoding(false);
 		setNeedsTimeZone(false);
 	}
 
 	@Override
 	public String getConnectorKind() {
 		return RtCorePlugin.CONNECTOR_KIND;
 	}
 
 	@Override
 	protected void createAdditionalControls(Composite parent) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected boolean isValidUrl(String url) {
		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	protected Validator getValidator(TaskRepository repository) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
