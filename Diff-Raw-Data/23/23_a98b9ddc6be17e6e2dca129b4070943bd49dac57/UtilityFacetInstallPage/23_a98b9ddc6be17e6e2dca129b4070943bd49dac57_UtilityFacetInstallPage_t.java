 /******************************************************************************
  * Copyright (c) 2005 BEA Systems, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Konstantin Komissarchik - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.jst.j2ee.ui.project.facet;
 
import org.eclipse.jst.j2ee.internal.actions.IJ2EEUIContextIds;
 import org.eclipse.jst.j2ee.internal.wizard.J2EEModuleFacetInstallPage;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * @author <a href="mailto:kosta@bea.com">Konstantin Komissarchik</a>
  */
 public final class UtilityFacetInstallPage extends J2EEModuleFacetInstallPage {
 	public UtilityFacetInstallPage() {
		super("utility.facet.install.page"); //$NON-NLS-1$
 		setTitle(Resources.pageTitle);
 		setDescription(Resources.pageDescription);
 	}
 
 	private static final class Resources extends NLS {
 		public static String pageTitle;
 		public static String pageDescription;
 
 		static {
 			initializeMessages(UtilityFacetInstallPage.class.getName(), Resources.class);
 		}
 	}
 
 	protected String[] getValidationPropertyNames() {
 		return new String[]{EAR_PROJECT_NAME};
 	}
 
 	protected Composite createTopLevelComposite(Composite parent) {
 		final Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new GridLayout(1, false));
		setInfopopID(IJ2EEUIContextIds.NEW_UTILITY_WIZARD_P3);
 		//setupEarControl(composite);
 		return composite;
 	}
 }
