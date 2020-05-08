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
 import org.wings.style.CSSProperty;
 import org.wings.style.CSSStyleSheet;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Example for STabbedPane.
  *
  * @author <a href="mailto:andre@lison.de">Andre Lison</a>
  * @author <a href="mailto:B.Schmid@eXXcellent.de">Benjamin Schmid</a>
  */
 public class TabbedPaneExample extends WingSetPane {
     private final static int INITIAL_TAB_COUNT = 10;
     private final static SIcon JAVA_CUP_ICON = new SResourceIcon("org/wings/icons/JavaCup.gif");
     private final static SIcon SMALL_COW_ICON = new SURLIcon("../icons/cowSmall.gif");
     private final static Object[] COLORS = new Object[]{
         "Transparent", null,
         "Yellow", Color.yellow,
         "Green", Color.green,
         "Light blue", new Color(200, 200, 255),
         "Light gray", Color.lightGray,
         "Orange", Color.orange};
     private final static Object[] TAB_ALIGNMENTS = new Object[]{
         "Top", new Integer(SConstants.TOP),
         "Left", new Integer(SConstants.LEFT),
         "Right", new Integer(SConstants.RIGHT),
         "Bottom", new Integer(SConstants.BOTTOM)
     };
     private TabbedPaneControls controls;
     private STabbedPane tabbedPane;
     private STextArea textArea;
 
     protected SComponent createExample() {
         textArea = new STextArea(6, 60);
 
         // Create tabbed pane and tabulators
         tabbedPane = new STabbedPane();
         for (int i = 0; i < INITIAL_TAB_COUNT; ++i) {
            SPanel panel = new SPanel(new SGridLayout(1));
             panel.add(new SLabel("Tab # " + i));
             panel.add(textArea);
             panel.setVerticalAlignment(SConstants.TOP);
            panel.setHorizontalAlignment(SConstants.CENTER);
             tabbedPane.add("Tab " + i, panel);
         }
         tabbedPane.setShowAsFormComponent(false);
         tabbedPane.setIconAt(3, JAVA_CUP_ICON);      // decorate with icons
         tabbedPane.setIconAt(8, SMALL_COW_ICON);
         tabbedPane.setEnabledAt(1, false);          // disable a tab
         tabbedPane.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent ce) {
                 textArea.setText(textArea.getText() + "Changed to tab: " + tabbedPane.getSelectedIndex() + "\n");
             }
         });
 
 
         // Create left-sided legend panel
         final SPanel legend = new SPanel(new SFlowDownLayout());
 
         // Tab placement
         final SButtonGroup alignmentGroup = new SButtonGroup();
         final PlacementActionListener placementActionListener = new PlacementActionListener(tabbedPane);
         final SLabel placementLabel = new SLabel("Tab Placement:");
         placementLabel.setFont(new SFont(SFont.BOLD));
         legend.add(placementLabel);
         for (int i = 0; i < TAB_ALIGNMENTS.length; i += 2) {
             final SRadioButton button = new SRadioButton(TAB_ALIGNMENTS[i].toString());
             button.putClientProperty("placement", TAB_ALIGNMENTS[i + 1]);
             button.addActionListener(placementActionListener);
             button.setShowAsFormComponent(false);
             legend.add(button);
             alignmentGroup.add(button);
         }
 
         // Tab colouring
         final SButtonGroup colorGroup = new SButtonGroup();
         final SLabel colorLabel = new SLabel("\nContent area background:");
         colorLabel.setFont(new SFont(SFont.BOLD));
         legend.add(colorLabel);
         for (int i = 0; i < COLORS.length; i += 2) {
             SRadioButton button = new SRadioButton(COLORS[i].toString());
             button.putClientProperty("color", COLORS[i + 1]);
             button.addActionListener(new ColorActionListener(tabbedPane));
             button.setShowAsFormComponent(false);
             colorGroup.add(button);
             legend.add(button);
         }
         legend.setHorizontalAlignment(SConstants.CENTER);
 
 
         controls = new TabbedPaneControls();
         controls.addSizable(tabbedPane);
 
         final SForm form = new SForm(new SBorderLayout());
         form.add(controls, SBorderLayout.NORTH);
         form.add(legend, SBorderLayout.WEST);
         form.add(tabbedPane, SBorderLayout.CENTER);
 
         return form;
     }
 
     /**
      * Action listener on radio buttons for tab placement
      */
     private static class PlacementActionListener implements ActionListener {
         private final STabbedPane tpane;
 
         public PlacementActionListener(STabbedPane tpane) {
             this.tpane = tpane;
         }
 
         public void actionPerformed(ActionEvent ae) {
             SComponent source = (SComponent) ae.getSource();
             Integer placement = (Integer) source.getClientProperty("placement");
             tpane.setTabPlacement(placement.intValue());
         }
     }
 
     /**
      * Action listener on radio buttons for tab colouring
      */
     private static class ColorActionListener implements ActionListener {
         private final STabbedPane tabs;
 
         public ColorActionListener(STabbedPane tabs) {
             this.tabs = tabs;
         }
 
         public void actionPerformed(ActionEvent ae) {
             SComponent source = (SComponent) ae.getSource();
             Color color = (Color) source.getClientProperty("color");
             tabs.setAttribute(STabbedPane.SELECTOR_CONTENT,
                     CSSProperty.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
         }
     }
 
 
     /**
      * Extended component control for this wingset demo.
      */
     private class TabbedPaneControls extends ComponentControls {
         private int tabCount = INITIAL_TAB_COUNT;
 
         public TabbedPaneControls() {
             final SCheckBox showAsFormComponent = new SCheckBox("Show as Form Component");
             showAsFormComponent.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     tabbedPane.setShowAsFormComponent(showAsFormComponent.isSelected());
                 }
             });
             add(showAsFormComponent);
             final SButton addTab = new SButton("add a tab");
             addTab.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     addTab();
                 }
             });
             add(addTab);
             final SCheckBox removeCurrent = new SCheckBox("remove current tab");
             final SButton removeTab = new SButton("remove a tab");
             removeTab.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     removeTab(removeCurrent.isSelected());
                 }
             });
             add(removeTab);
             add(removeCurrent);
         }
 
         protected void removeTab(boolean removeCurrent) {
             if (removeCurrent) {
                 int index = tabbedPane.getSelectedIndex();
                 if (index != -1) {
                     tabCount--;
                     tabbedPane.removeTabAt(index);
                 }
             } else {
                 if (tabCount > 0) {
                     tabCount--;
                     tabbedPane.removeTabAt(tabCount);
                 }
             }
         }
 
         protected void addTab() {
             SPanel p = new SPanel(new SBorderLayout());
             p.add(new SLabel("Tab # " + tabCount), "North");
             p.add(textArea);
             tabbedPane.add("Tab " + tabCount, p);
             tabCount++;
         }
     }
 }
