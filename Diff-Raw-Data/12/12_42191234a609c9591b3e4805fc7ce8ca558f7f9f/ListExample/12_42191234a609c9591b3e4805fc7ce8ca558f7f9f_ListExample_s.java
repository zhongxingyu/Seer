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
 
 import javax.swing.*;
 import javax.swing.event.ListDataListener;
 import java.awt.*;
 
 /**
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class ListExample
         extends WingSetPane {
     static final SResourceIcon javaCup = new SResourceIcon("org/wings/icons/JavaCup.gif");
     private ComponentControls controls;
 
     public SComponent createExample() {
         controls = new ComponentControls();
 
         SPanel p = new SPanel(new SGridLayout(2, 2));
         p.add(createListSingleSelExample());
         p.add(createListMultSelExample());
         p.add(createComboBoxExample());
         p.add(createAnchorListExample());
 
         SForm form = new SForm(new SBorderLayout());
         form.add(controls, SBorderLayout.NORTH);
         form.add(p, SBorderLayout.CENTER);
         form.add(new SButton("SUBMIT"), SBorderLayout.SOUTH);
         return form;
     }
 
     public SContainer createListSingleSelExample() {
         SContainer cont = new SPanel(new SFlowDownLayout());
         cont.add(new SLabel("List with single selection"));
        SList sinlgeSelectionList = new SList();
        sinlgeSelectionList.setName("single");
        sinlgeSelectionList.setSelectionMode(SList.SINGLE_SELECTION);
        addListElements(sinlgeSelectionList);
        cont.add(sinlgeSelectionList);
        controls.addSizable(sinlgeSelectionList);
 
         return cont;
     }
 
     public SContainer createListMultSelExample() {
         SContainer cont = new SPanel(new SFlowDownLayout());
         cont.add(new SLabel("List with multiple selection"));
         SList multiSelectionList = new SList();
         multiSelectionList.setName("multiple");
         multiSelectionList.setSelectionMode(SList.MULTIPLE_SELECTION);
         addListElements(multiSelectionList);
         cont.add(multiSelectionList);
         controls.addSizable(multiSelectionList);
 
         return cont;
     }
 
     public SContainer createComboBoxExample() {
         SContainer cont = new SPanel(new SFlowDownLayout());
         cont.add(new SLabel("ComboBox"));
         SComboBox comboBox = new SComboBox();
         comboBox.setName("combo");
         addComboBoxElements(comboBox);
         cont.add(comboBox);
 
         return cont;
     }
 
     public SContainer createAnchorListExample() {
         SContainer cont = new SPanel(new SFlowDownLayout());
         cont.add(new SLabel("List with showAsFormComponent = false"));
         SList anchorList = new SList();
         anchorList.setName("noform");
         anchorList.setShowAsFormComponent(false);
         anchorList.setSelectionMode(SList.SINGLE_SELECTION);
         addAnchorElements(anchorList);
         cont.add(anchorList);
         controls.addSizable(anchorList);
 
         return cont;
     }
 
     public void addListElements(SList list) {
         list.setListData(createElements());
     }
 
     public void addComboBoxElements(SComboBox comboBox) {
         comboBox.setModel(new DefaultComboBoxModel(createElements()));
     }
 
     public static Object[] createElements() {
         SLabel color = new SLabel("");
         color.setForeground(Color.green);
         color.setText(Color.green.toString());
 
         Object[] values = {
             "element1",
             color,
             "element3",
             "element4",
             "element5",
             "element6"
         };
 
         return values;
     }
 
     public static ListModel createListModel() {
         final SLabel img =
                 new SLabel(javaCup);
 
         final SLabel color = new SLabel("");
         color.setForeground(Color.green);
         color.setText(Color.green.toString());
 
         ListModel listModel = new ListModel() {
             Object[] values = {
                 "element1",
                 color,
                 img,
                 "element4",
                 "element5",
                 "element6"
             };
 
             public int getSize() {
                 return values.length;
             }
 
             public Object getElementAt(int i) {
                 return values[i];
             }
 
             public void addListDataListener(ListDataListener l) {
             }
 
             public void removeListDataListener(ListDataListener l) {
             }
         };
 
         return listModel;
     }
 
     private final ListModel listModel = createListModel();
 
     public void addAnchorElements(SList list) {
         final SLabel img = new SLabel(javaCup);
 
         final SLabel color = new SLabel("");
         color.setForeground(Color.green);
         color.setText(Color.green.toString());
 
         list.setModel(listModel);
         list.setType(SList.ORDER_TYPE_NORMAL);
     }
 }
