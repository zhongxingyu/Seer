 /*******************************************************************************
  * Copyright (c) 2012 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation 
  *******************************************************************************/
 package org.eclipse.jubula.rc.swt.uiadapter;
 
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.uiadapter.interfaces.IButtonAdapter;
import org.eclipse.jubula.rc.swt.utils.SwtUtils;
 import org.eclipse.swt.widgets.Button;
 /**
  * Implements the Button interface for adapting a <code>SWT.Button</code>
  * 
  *  @author BREDEX GmbH
  */
 public class ButtonAdapter extends WidgetAdapter implements IButtonAdapter {
     
     /** the Button from the AUT */
     private Button m_button;
     
     /**
      * 
      * @param objectToAdapt graphics component which will be adapted
      */
     public ButtonAdapter(Object objectToAdapt) {
         super(objectToAdapt);
         m_button = (Button) objectToAdapt;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getText() {
         
         return (String)getEventThreadQueuer()
                 .invokeAndWait("getText", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                        return SwtUtils.removeMnemonics(m_button.getText()); 
                            // see findBugs;
                     }
                 });
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isSelected() {
         Boolean actual = (Boolean)getEventThreadQueuer()
                 .invokeAndWait("isSelected", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                         return m_button.getSelection() 
                             ? Boolean.TRUE : Boolean.FALSE; // see findBugs;
                     }
                 });
         return actual.booleanValue();
     }
     
     /**
      * {@inheritDoc}
      * 
      */
     public String readValue(String variable) {
         
         return getText();
     }
 }
