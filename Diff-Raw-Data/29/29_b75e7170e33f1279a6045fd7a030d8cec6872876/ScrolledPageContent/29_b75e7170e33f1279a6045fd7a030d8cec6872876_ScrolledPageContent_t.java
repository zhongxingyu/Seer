 /*******************************************************************************
  * Copyright (c) 2000, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Oracle - updated for JSF tools
  *******************************************************************************/
 package org.eclipse.jst.jsf.ui.internal.validation;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.SharedScrolledComposite;
 
 
 /**
  * Customized SharedScrolledComposite
  */
 /*package*/ class ScrolledPageContent extends SharedScrolledComposite {
 
     private FormToolkit fToolkit;
     
     /**
      * @param parent
      */
     public ScrolledPageContent(Composite parent) {
         this(parent, SWT.V_SCROLL | SWT.H_SCROLL);
     }
     
     /**
      * @param parent
      * @param style
      */
     public ScrolledPageContent(Composite parent, int style) {
         super(parent, style);
         
         setFont(parent.getFont());
         
        initDialogsFormToolkit();
         
         setExpandHorizontal(true);
         setExpandVertical(true);
         
         Composite body= new Composite(this, SWT.NONE);
         body.setFont(parent.getFont());
         setContent(body);
     }
     
    private FormToolkit initDialogsFormToolkit()
    {
        if (fToolkit == null) {
            FormColors colors= new FormColors(Display.getCurrent());
            colors.setBackground(null);
            colors.setForeground(null); 
            fToolkit= new FormToolkit(colors);
        }
        return fToolkit;
    }
     
     /**
      * @param childControl
      */
     public void adaptChild(Control childControl) {
         fToolkit.adapt(childControl, true, true);
     }
     
     /**
      * @return the content composite
      */
     public Composite getBody() {
         return (Composite) getContent();
     }
 
 }
