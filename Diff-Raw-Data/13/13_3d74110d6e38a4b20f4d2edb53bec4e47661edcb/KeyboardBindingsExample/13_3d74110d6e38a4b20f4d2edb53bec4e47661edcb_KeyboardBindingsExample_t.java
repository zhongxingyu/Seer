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
 
 import org.wings.SComponent;
 import org.wings.SFlowDownLayout;
 import org.wings.SForm;
 import org.wings.SLabel;
 import org.wings.STextField;
 
 import javax.swing.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.ActionEvent;
 
 /**
  * @author <a href="mailto:hengels@mercatis.de">Holger Engels</a>
  * @version $Revision$
  */
 public class KeyboardBindingsExample extends WingSetPane {
     private final SLabel label = new SLabel();
     private final STextField textField = new STextField();
     private final SForm form = new SForm(new SFlowDownLayout());
 
     public KeyboardBindingsExample() {
         final InputMap inputMap = new InputMap();
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false), "F1");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false), "F2");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false), "F3");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, false), "F4");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false), "F5");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0, false), "F6");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, false), "F7");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0, false), "F8");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, false), "F9");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0, false), "F10");
 
         final InputMap formInputMap = new InputMap();
         formInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, KeyEvent.SHIFT_DOWN_MASK, false), "Shift F1");
         formInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.SHIFT_DOWN_MASK, false), "Shift F2");
         formInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK, false), "Shift F3");
         formInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.SHIFT_DOWN_MASK, false), "Shift F4");
         formInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.SHIFT_DOWN_MASK, false), "Shift F5");
         formInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.SHIFT_DOWN_MASK, false), "Shift F6");
         formInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.SHIFT_DOWN_MASK, false), "Shift F7");
         formInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.SHIFT_DOWN_MASK, false), "Shift F8");
         formInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, KeyEvent.SHIFT_DOWN_MASK, false), "Shift F9");
         formInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK, false), "Shift F10");
 
         final Action action = new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 label.setText(e.getActionCommand());
             }
         };
 
         final ActionMap actionMap = new ActionMap();
         actionMap.put("F1", action);
         actionMap.put("F2", action);
         actionMap.put("F3", action);
         actionMap.put("F4", action);
         actionMap.put("F5", action);
         actionMap.put("F6", action);
         actionMap.put("F7", action);
         actionMap.put("F8", action);
         actionMap.put("F9", action);
         actionMap.put("F10", action);
         actionMap.put("Shift F1", action);
         actionMap.put("Shift F2", action);
         actionMap.put("Shift F3", action);
         actionMap.put("Shift F4", action);
         actionMap.put("Shift F5", action);
         actionMap.put("Shift F6", action);
         actionMap.put("Shift F7", action);
         actionMap.put("Shift F8", action);
         actionMap.put("Shift F9", action);
         actionMap.put("Shift F10", action);
 
         textField.setInputMap(inputMap);
         textField.setActionMap(actionMap);
 
         form.setInputMap(formInputMap);
         form.setActionMap(actionMap);
 
        form.add(new SLabel("<html>&nbsp;"));
         form.add(new SLabel("Try ALT-Left and ALT-Right to cycle inside the WingSet application between the different tabs"));
        form.add(new SLabel("<html>&nbsp;"));
         form.add(new SLabel("<html>The keys F1 through F10 are captured by the STextField<br/>" +
                 "Shift F1 through Shift F10 are bubbling up to the containing SForm"));
         form.add(textField);
         form.add(label);
 
         form.setHorizontalAlignment(CENTER);
     }
 
     public SComponent createExample() {
         return form;
     }
 }
