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
 
 import org.wings.*;
 
 /**
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class RadioButtonExample
         extends WingSetPane {
     static final ClassLoader cl = WingSet.class.getClassLoader();
     static final SIcon sel =
             new SURLIcon("../icons/RadioButtonSelectedIcon.gif");
     static final SIcon nsel =
             new SURLIcon("../icons/RadioButtonIcon.gif");
     static final SIcon pressed =
             new SURLIcon("../icons/RadioButtonPressedIcon.gif");
     static final SIcon dissel =
             new SURLIcon("../icons/RadioButtonDisabledSelectedIcon.gif");
     static final SIcon disnsel =
             new SURLIcon("../icons/RadioButtonDisabledIcon.gif");
     static final SIcon rollsel =
             new SURLIcon("../icons/RadioButtonRolloverSelectedIcon.gif");
     static final SIcon rollnsel =
             new SURLIcon("../icons/RadioButtonRolloverIcon.gif");
 
 
     public SComponent createExample() {
         SPanel p = new SPanel(new SGridLayout(2));
         p.add(new SLabel("<html><h4>RadioButtons outside forms</h4>"));
         p.add(new SLabel("<html><h4>Image RadioButtons outside forms</h4>"));
         p.add(createRadioButtonExample());
         p.add(createImageRadioButtonExample());
 
         SForm form = new SForm();
         form.add(new SLabel("<html><h4>RadioButtons in a form</h4>"));
         form.add(createRadioButtonExample());
         form.add(new SLabel("<html><br />"));
         form.add(new SButton("submit"));
         p.add(form);
 
         form = new SForm();
         form.add(new SLabel("<html><h4>Image RadioButtons in a form</h4>"));
         form.add(createImageRadioButtonExample());
         form.add(new SLabel("<html><br />"));
         form.add(new SButton("submit"));
         p.add(form);
         return p;
     }
 
     SContainer createRadioButtonExample() {
         SPanel text = new SPanel();
 
         SButtonGroup group = new SButtonGroup();
         for (int i = 0; i < 3; i++) {
             SRadioButton b = new SRadioButton("text " + (i + 1));
             group.add(b);
             // b.setBorder(new SLineBorder(1));
             text.add(b);
         }
 
         return text;
     }
 
     SContainer createImageRadioButtonExample() {
         SButtonGroup group = new SButtonGroup();
 
         SRadioButton[] boxes = new SRadioButton[9];
         boxes[0] = new SRadioButton("testTL");
         boxes[1] = new SRadioButton("testTC");
         boxes[2] = new SRadioButton("testTR");
         boxes[3] = new SRadioButton("testCL");
         boxes[4] = new SRadioButton();
         boxes[5] = new SRadioButton("testCR");
         boxes[6] = new SRadioButton("testBL");
         boxes[7] = new SRadioButton("testBC");
         boxes[8] = new SRadioButton("testBR");
 
         for (int i = 0; i < boxes.length; i++) {
             group.add(boxes[i]);
             boxes[i].setToolTipText("RadioButton " + i);
             boxes[i].setIcon(nsel);
             boxes[i].setSelectedIcon(sel);
             boxes[i].setDisabledIcon(disnsel);
             boxes[i].setDisabledSelectedIcon(dissel);
             boxes[i].setRolloverIcon(rollnsel);
             boxes[i].setRolloverSelectedIcon(rollsel);
             boxes[i].setPressedIcon(pressed);
         }
 
         boxes[0].setVerticalTextPosition(SConstants.TOP);
         boxes[0].setHorizontalTextPosition(SConstants.LEFT);
 
         boxes[1].setVerticalTextPosition(SConstants.TOP);
         boxes[1].setHorizontalTextPosition(SConstants.CENTER);
 
         boxes[2].setVerticalTextPosition(SConstants.TOP);
         boxes[2].setHorizontalTextPosition(SConstants.RIGHT);
 
         boxes[3].setVerticalTextPosition(SConstants.CENTER);
         boxes[3].setHorizontalTextPosition(SConstants.LEFT);
         
        boxes[4].setHorizontalAlignment(SConstants.CENTER);
        boxes[4].setVerticalAlignment(SConstants.CENTER);
 
         boxes[5].setVerticalTextPosition(SConstants.CENTER);
         boxes[5].setHorizontalTextPosition(SConstants.RIGHT);
 
         boxes[6].setVerticalTextPosition(SConstants.BOTTOM);
         boxes[6].setHorizontalTextPosition(SConstants.LEFT);
 
         boxes[7].setSelected(true);
         boxes[7].setEnabled(false);
         boxes[7].setVerticalTextPosition(SConstants.BOTTOM);
         boxes[7].setHorizontalTextPosition(SConstants.CENTER);
 
         boxes[8].setSelected(false);
         boxes[8].setEnabled(false);
         boxes[8].setVerticalTextPosition(SConstants.BOTTOM);
         boxes[8].setHorizontalTextPosition(SConstants.RIGHT);
 
         SPanel erg = new SPanel(new SFlowDownLayout());
 
         SGridLayout grid = new SGridLayout(3, 3);
         grid.setBorder(1);
         SPanel b = new SPanel(grid);
 
         for (int i = 0; i < boxes.length; i++)
             b.add(boxes[i]);
 
         erg.add(b);
 
         return erg;
     }
 }
 
 
