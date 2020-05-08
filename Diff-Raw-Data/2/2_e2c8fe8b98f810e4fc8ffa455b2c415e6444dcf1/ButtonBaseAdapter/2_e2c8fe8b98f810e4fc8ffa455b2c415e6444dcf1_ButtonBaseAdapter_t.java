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
 package org.eclipse.jubula.rc.javafx.tester.adapter;
 
 import java.util.concurrent.Callable;
 
 import javafx.scene.control.ButtonBase;
 import javafx.scene.control.CheckBox;
 import javafx.scene.control.Toggle;
 
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.tester.adapter.interfaces.IButtonComponent;
 import org.eclipse.jubula.rc.javafx.driver.EventThreadQueuerJavaFXImpl;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 
 /**
  * Implementation of the button interface as an adapter which holds the
  * <code>javax.swing.AbstractButton</code>.
  *
  * @author BREDEX GmbH
  * @created 30.10.2013
  */
 public class ButtonBaseAdapter extends JavaFXComponentAdapter<ButtonBase>
         implements IButtonComponent {
 
     /**
      * Creates an object with the adapted Button.
      *
      * @param objectToAdapt
      *            this must be an object of the Type <code>ButtonBase</code>
      */
     public ButtonBaseAdapter(ButtonBase objectToAdapt) {
         super(objectToAdapt);
     }
 
     @Override
     public String getText() {
         String text = EventThreadQueuerJavaFXImpl.invokeAndWait("getText", //$NON-NLS-1$
                 new Callable<String>() {
 
                     @Override
                     public String call() throws Exception {
                         return getRealComponent().getText();
                     }
                 });
         return text;
     }
 
     @Override
     public boolean isSelected() {
         final ButtonBase real = getRealComponent();
        if (real instanceof Toggle) {
             return EventThreadQueuerJavaFXImpl.invokeAndWait(
                     "isSelected", new Callable<Boolean>() { //$NON-NLS-1$
 
                         @Override
                         public Boolean call() throws Exception {
                             return ((Toggle) real).isSelected();
                         }
                     });
         } else if (real instanceof CheckBox) {
             return EventThreadQueuerJavaFXImpl.invokeAndWait(
                     "isSelected", new Callable<Boolean>() { //$NON-NLS-1$
 
                         @Override
                         public Boolean call() throws Exception {
                             return ((CheckBox) real).isSelected();
                         }
                     });
         }
         throw new StepExecutionException(
                 "The Button is not a RadioButton and CheckBoxButton", //$NON-NLS-1$
                 EventFactory
                         .createActionError(
                                 TestErrorEvent.UNSUPPORTED_OPERATION_ERROR));
     }
 
 }
