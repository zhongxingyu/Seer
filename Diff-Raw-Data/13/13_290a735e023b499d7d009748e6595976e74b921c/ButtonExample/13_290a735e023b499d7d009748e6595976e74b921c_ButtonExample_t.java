 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package wingset;
 
 import org.wings.SAbstractButton;
 import org.wings.SButton;
 import org.wings.SCheckBox;
 import org.wings.SComponent;
 import org.wings.SConstants;
 import org.wings.SContainer;
import org.wings.SDimension;
 import org.wings.SForm;
 import org.wings.SGridLayout;
 import org.wings.SIcon;
 import org.wings.SLabel;
 import org.wings.SPanel;
 import org.wings.SURLIcon;
 import org.wings.border.SEmptyBorder;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Iterator;
 
 /**
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class ButtonExample extends WingSetPane {
     // icons
     private static final SIcon icon = new SURLIcon("../icons/ButtonIcon.gif");
     private static final SIcon disabledIcon = new SURLIcon("../icons/ButtonDisabledIcon.gif");
     private static final SIcon pressedIcon = new SURLIcon("../icons/ButtonPressedIcon.gif");
     private static final SIcon rolloverIcon = new SURLIcon("../icons/ButtonRolloverIcon.gif");
 
     // pressed label & handler
     private final SLabel buttonPressedLabel = new SLabel("No button pressed");
     private final ActionListener reportButtonPressedAction = new ActionListener() {
         public void actionPerformed(ActionEvent e) {
             buttonPressedLabel.setText("<html>Button <b>'" + e.getActionCommand() + "'</b> pressed");
         }
     };
 
     // button control itself
     private ButtonControls controls;
 
     public SComponent createExample() {
         controls = new ButtonControls();
         controls.getApplyButton().addActionListener(reportButtonPressedAction);
 
         final SForm form = new SForm(new SGridLayout(1));
         final SContainer buttonExamplePanel = createButtonExample(form);
 
         form.add(controls);
         form.add(buttonExamplePanel);
        form.setPreferredSize(SDimension.FULLWIDTH);
         return form;
     }
 
     private SContainer createButtonExample(SForm containingForm) {
         final SButton[] buttons = new SButton[9];
         final int[] textHPos = new int[] {SConstants.LEFT, SConstants.CENTER, SConstants.RIGHT};
         final int[] textVPos = new int[] {SConstants.TOP, SConstants.CENTER, SConstants.BOTTOM};
 
         for (int i = 0; i < buttons.length; i++) {
             final String buttonName = "Text " + (i + 1);
             buttons[i] = new SButton(buttonName);
             buttons[i].setActionCommand(buttons[i].getText());
             if (i != 4) {
                 buttons[i].setIcon(icon);
                 buttons[i].setDisabledIcon(disabledIcon);
                 buttons[i].setRolloverIcon(rolloverIcon);
                 buttons[i].setPressedIcon(pressedIcon);
             }
 
             buttons[i].setToolTipText("Button " + (i + 1));
             buttons[i].setName("button" + (i + 1));
             buttons[i].setShowAsFormComponent(false);
             buttons[i].setVerticalTextPosition(textVPos[(i / 3)% 3]);
             buttons[i].setHorizontalTextPosition(textHPos[i % 3]);
             buttons[i].setActionCommand(buttonName);
             controls.addSizable(buttons[i]);
         }
 
         final SGridLayout grid = new SGridLayout(3);
         final SPanel buttonGrid = new SPanel(grid);
         grid.setBorder(1);
         grid.setHgap(10);
         grid.setVgap(10);
         buttonGrid.setHorizontalAlignment(CENTER);
         buttonGrid.setBorder(new SEmptyBorder(10, 0, 10, 0));
 
         for (int i = 0; i < buttons.length; i++) {
             buttons[i].addActionListener(reportButtonPressedAction);
             buttonGrid.add(buttons[i]);
         }
 
         // default button handling ==> if enter is pressed
         final SButton defaultButton = new SButton();
         defaultButton.addActionListener(reportButtonPressedAction);
         defaultButton.setActionCommand("Default Button (Enter key)");
         containingForm.setDefaultButton(defaultButton);
 
         final SPanel panel = new SPanel(new SGridLayout(1));
         final SLabel description = new SLabel("Click on the buttons and see how The label changes\n" +
                 "Click into a input box and press apply or enter to see the effect of the enter button capturing.");
         panel.setBorder(new SEmptyBorder(10, 10, 10, 10));
         panel.add(description);
         panel.add(buttonGrid);
         panel.add(buttonPressedLabel);
 
         return panel;
     }
 
     private class ButtonControls extends ComponentControls {
         public ButtonControls() {
             final SCheckBox showAsFormComponent = new SCheckBox("Show as Form Component");
             showAsFormComponent.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     for (Iterator iterator = components.iterator(); iterator.hasNext();) {
                         SComponent component = (SComponent) iterator.next();
                         component.setShowAsFormComponent(showAsFormComponent.isSelected());
                     }
                 }
             });
             add(showAsFormComponent);
 
             final SCheckBox useImages = new SCheckBox("Use Icons");
             useImages.setSelected(true);
             useImages.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     boolean use = useImages.isSelected();
 
                     for (Iterator iterator = components.iterator(); iterator.hasNext();) {
                         SAbstractButton component = (SAbstractButton) iterator.next();
                         component.setIcon(use ? icon : null);
                         component.setDisabledIcon(use ? disabledIcon : null);
                         component.setRolloverIcon(use ? rolloverIcon : null);
                         component.setPressedIcon(use ? pressedIcon : null);
                     }
                 }
             });
             add(useImages);
         }
 
         public SButton getApplyButton() {
             return super.applyButton;
         }
     }
 }
