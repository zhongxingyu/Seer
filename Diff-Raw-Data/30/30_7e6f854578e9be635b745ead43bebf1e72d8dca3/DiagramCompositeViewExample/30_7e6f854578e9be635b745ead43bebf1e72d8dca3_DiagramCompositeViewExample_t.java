 /*******************************************************************************
  * <copyright>
  *
 * Copyright (c) 2013 SRC
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    pjpaulin - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.examples.composite;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.graphiti.ui.editor.DiagramComposite;
 import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.part.ViewPart;
 
 public class DiagramCompositeViewExample extends ViewPart {
 
     private DiagramComposite diagramComposite;
 
     @Override
     public void createPartControl(Composite parent) {
         this.diagramComposite = new DiagramComposite(this, parent, SWT.NONE);
         this.diagramComposite.setLayout(new FillLayout());
         this.diagramComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         
 		URI uri = URI.createURI("platform:/plugin/org.eclipse.graphiti.examples.composite/test.diagram#/0");
         this.diagramComposite.setInput(new DiagramEditorInput(uri,
                 "org.eclipse.graphiti.examples.tutorial.diagram.TutorialDiagramTypeProvider"));
     }
 
     @Override
     public void setFocus() {
         this.diagramComposite.setFocus();
     }
     
     public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
         Object returnObj = null;
 
         if (this.diagramComposite != null && !this.diagramComposite.isDisposed()) 
             returnObj = this.diagramComposite.getAdapter(type);
         
         if (returnObj != null)
             return returnObj;
         
         return super.getAdapter(type);
     }
 }
