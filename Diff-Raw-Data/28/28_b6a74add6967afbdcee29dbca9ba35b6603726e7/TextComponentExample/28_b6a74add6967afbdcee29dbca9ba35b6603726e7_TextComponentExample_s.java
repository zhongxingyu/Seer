 /*
  * $Id$
  * Copyright 2000,2005 j-wingS development team.
  *
  * This file is part of j-wingS (http://www.j-wings.org).
  *
  * j-wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package wingset;
 
 import org.wings.*;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * @author <a href="mailto:mreinsch@to.com">Michael Reinsch</a>
  * @version $Revision$
  */
 public class TextComponentExample
         extends WingSetPane {
     private int i = 1;
 
     public SComponent createExample() {
         SPanel p = new SPanel();
         STextField textField = new STextField();
         textField.setName("textfield");
         p.add(createTextComponentExample(textField));
         p.add(new SSeparator());
 
         STextArea textArea = new STextArea();
         textArea.setName("textarea");
         p.add(createTextComponentExample(textArea));
         return p;
     }
 
     SForm createTextComponentExample(STextComponent textComp) {
         SForm f = new SForm();
 
         SGridLayout layout = new SGridLayout(2);
         f.setLayout(layout);
 
         SLabel desc = new SLabel("Enter some text: ");
        SLabel resultDesc = new SLabel("Entered text:");
         final SLabel result = new SLabel("");
         final SLabel buttonDesc = new SLabel("");
 
         /* TODO FIXME 
           textComp.addTextListener(new TextListener() {
             public void textValueChanged(TextEvent evt) {
                 result.setText(((STextComponent) evt.getSource()).getText());
                 buttonDesc.setText("button not pressed");
             }
         }); */
 
         SButton button = new SButton("Submit");
         button.setName("submit" + (i++));
         button.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                buttonDesc.setText("button pressed");
             }
         });
 
         f.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 buttonDesc.setText(buttonDesc.getText() + " + form event");
             }
         });
 
         f.add(desc);
         f.add(textComp);
         f.add(resultDesc);
         f.add(result);
         f.add(button);
         f.add(buttonDesc);
 
         return f;
     }
 }
 
 
