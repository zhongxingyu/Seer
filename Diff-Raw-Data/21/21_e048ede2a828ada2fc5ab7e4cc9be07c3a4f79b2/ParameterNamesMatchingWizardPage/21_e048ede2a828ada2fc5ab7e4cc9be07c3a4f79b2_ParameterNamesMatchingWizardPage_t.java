 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.wizards.search.refactor.pages;
 
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.widgets.Composite;
 /**
  * 
  * @author BREDEX GmbH
  *
  */
 public class ParameterNamesMatchingWizardPage extends WizardPage {
 
     /**
      * 
      * @param pageName
      *            the page name
      */
     public ParameterNamesMatchingWizardPage(String pageName) {
         super(pageName, Messages.ReplaceTCRWizard_matchComponentNames_title,
                 null);
         setPageComplete(false);
     }
     /**
      * {@inheritDoc}
      */
     public void createControl(Composite parent) {
         ScrolledComposite comp = new ScrolledComposite(parent, SWT.V_SCROLL
                 | SWT.H_SCROLL);
         setControl(comp);
     }
 
     /**
      * Show help contend attached to wizard after selecting the ? icon,
      * or pressing F1 on Windows / Shift+F1 on Linux / Help on MAC.
      * {@inheritDoc}
      */
     public void performHelp() {
         Plugin.getHelpSystem().displayHelp(ContextHelpIds
                .SEARCH_REFACTOR_REPLACE_EXECUTION_TEST_CASE_WIZARD);
     }
 
 }
