 /*******************************************************************************
  * Copyright (c) 2010 BSI Business Systems Integration AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BSI Business Systems Integration AG - initial API and implementation
  ******************************************************************************/
 package org.eclipse.scout.rt.ui.rap.ext;
 
 import org.eclipse.scout.rt.ui.rap.basic.comp.CLabelEx;
 import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 
 /**
  * <p>
  * Contains a label to use on top of a field. The label actually is a {@link StyledText} in order to support line wrap.
  * </p>
  * <p>
  * Compared to {@link StatusLabelEx} which uses a {@link CLabelEx} the text won't be shortened but wrapped instead (if
  * style is set to SWT.WRAP). Additionally the place of the status icon is different. It is located left to the text and
  * not at the right side.
  * </p>
  */
 public class StatusLabelTop extends StatusLabelEx {
 
   private static final long serialVersionUID = 1L;
 
   public StatusLabelTop(Composite parent, int style) {
     super(parent, style);
   }
 
   @Override
   protected void createLayout() {
     GridLayout containerLayout = new GridLayout(2, false);
     containerLayout.horizontalSpacing = 0;
     containerLayout.marginHeight = 0;
     containerLayout.marginWidth = 0;
     containerLayout.verticalSpacing = 0;
     containerLayout.marginBottom = 0;
 
     //A little margin on the left so it is vertically aligned with the LABEL_POSITION_LEFT-labels
     containerLayout.marginLeft = 1;
 
     setLayout(containerLayout);
   }
 
   @Override
   protected void createContent(Composite parent, int style) {
     setStatusLabel(new Label(parent, SWT.NONE));
     getUiEnvironment().getFormToolkit().getFormToolkit().adapt(getStatusLabel(), false, false);
 
     style |= SWT.WRAP | SWT.MULTI | SWT.NO_FOCUS;
     StyledText label = getUiEnvironment().getFormToolkit().createStyledText(parent, style);
     label.setEnabled(false);
     setLabel(label);
 
     //Set the status icon to the top left corner
     GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);
     getStatusLabel().setLayoutData(data);
 
     //Make sure the label composite fills the cell so that horizontal alignment of the text works well
     data = new GridData(SWT.FILL, SWT.FILL, true, true);
     label.setLayoutData(data);
   }
 
   @Override
   protected void setLabelText(String text) {
     if (getLabel() instanceof StyledText) {
       ((StyledText) getLabel()).setText(text);
     }
   }
 
   @Override
   protected String getLabelText() {
     if (getLabel() instanceof StyledText) {
       return ((StyledText) getLabel()).getText();
     }
 
     return null;
   }
 
 }
