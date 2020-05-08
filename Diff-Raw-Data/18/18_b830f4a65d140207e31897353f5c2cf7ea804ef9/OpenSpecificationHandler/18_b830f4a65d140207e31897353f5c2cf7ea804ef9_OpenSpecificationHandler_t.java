 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.handlers.open;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.ui.businessprocess.UINodeBP;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 
 /**
  * @author BREDEX GmbH
  * @created Jul 29, 2010
  */
 public class OpenSpecificationHandler extends AbstractOpenHandler {
     /** {@inheritDoc} */
     public Object execute(ExecutionEvent event) {
         ISelection sel = HandlerUtil.getCurrentSelection(event);
         if (!(sel instanceof IStructuredSelection)) {
             return null;
         }
         openSpecNode((IStructuredSelection)sel);
         return null;
     }
 
     /**
      * Search for the SpecTC of an ExecTC and opens the corresponding editor if
      * possible.
      * 
      * @param structuredSel
      *            the current selection
      */
     private void openSpecNode(IStructuredSelection structuredSel) {
         ISpecTestCasePO specTc = UINodeBP.getSpecTC(structuredSel);
         openEditorForSpecTC(specTc);
     }
 }
