 package org.pillarone.riskanalytics.application.ui.parameterization.view;
 
 import com.ulcjava.base.application.*;
 import com.ulcjava.base.application.border.ULCTitledBorder;
 import com.ulcjava.base.application.util.Color;
 import com.ulcjava.base.application.util.Font;
 import org.pillarone.riskanalytics.application.ui.parameterization.model.ParameterViewModel;
 import org.pillarone.riskanalytics.application.util.LocaleResources;
 import org.pillarone.riskanalytics.core.parameterization.validation.ParameterValidationError;
 
 import java.util.Collection;
 
 public class ErrorPane {
 
     private ULCBoxPane content;
     private ULCBoxPane container;
     private ParameterViewModel model;
 
     public ErrorPane(ParameterViewModel model) {
         this.model = model;
         content = new ULCBoxPane();
         container = new ULCBoxPane(1, 0);
         container.setBackground(Color.white);
 
         content.add(ULCBoxPane.BOX_EXPAND_EXPAND, new ULCScrollPane(container));
     }
 
     public void addError(ParameterValidationError error) {
         container.add(ULCBoxPane.BOX_EXPAND_TOP, createLabel(error));
     }
 
     public void addErrors(Collection<ParameterValidationError> errors) {
         for (ParameterValidationError error : errors) {
             addError(error);
         }
         container.add(ULCBoxPane.BOX_EXPAND_EXPAND, ULCFiller.createVerticalGlue());
     }
 
     public void clear() {
         container.removeAll();
     }
 
     private ULCComponent createLabel(ParameterValidationError error) {
         ULCBoxPane pane = new ULCBoxPane(1, 1);
         pane.setBackground(Color.white);
        final String errorPath = model.findNodeForPath(error.getPath()).getDisplayPath();
        final ULCTitledBorder border = BorderFactory.createTitledBorder(errorPath);
         border.setTitleFont(border.getTitleFont().deriveFont(Font.PLAIN));
         pane.setBorder(border);
 
         ULCLabel label = new ULCLabel();
         label.setForeground(Color.red);
         label.setText(error.getLocalizedMessage(LocaleResources.getLocale()));
         label.setFont(label.getFont().deriveFont(Font.PLAIN));
 
         pane.add(ULCBoxPane.BOX_EXPAND_TOP, label);
         return pane;
     }
 
     public ULCBoxPane getContent() {
         return content;
     }
 }
