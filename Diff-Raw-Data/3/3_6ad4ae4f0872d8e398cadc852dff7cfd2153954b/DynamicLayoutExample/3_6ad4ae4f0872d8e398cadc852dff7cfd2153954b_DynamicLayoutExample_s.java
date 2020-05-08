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
 import org.wings.border.SBorder;
 import org.wings.border.SLineBorder;
 
 import java.awt.*;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 /**
  * A quickhack example to show the capabilities of the dynamic wings layout managers.
  *
  * @author bschmid
  */
 public class DynamicLayoutExample extends WingSetPane {
     private final SForm panel = new SForm();
     private final SPanel[] demoPanels = {new BorderLayoutDemoPanel(), new FlowLayoutDemoPanel(), new GridBagDemoPanel(), new GridLayoutDemoPanel(), new BoxLayoutDemoPanel()};
     private final static String[] demoManagerNames = {"SBorderLayout", "SFlowLayout", "SGridBagLayout", "SGridLayout", "SBoxLayout"};
     private final SComboBox selectLayoutManager = new SComboBox(demoManagerNames);
 
     protected SComponent createExample() {
         selectLayoutManager.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent e) {
                 panel.remove(1);
                 panel.add(demoPanels[selectLayoutManager.getSelectedIndex()]);
             }
         });
 
         panel.add(selectLayoutManager);
         panel.add(demoPanels[0]);
 
         return panel;
     }
 
     private static void addDummyLabels(final SPanel panel, final int amount) {
         for (int i = 0; i < amount; i++) {
             panel.add(createDummyLabel(i));
         }
     }
 
     private static SLabel createDummyLabel(int i) {
         final String[] texts = {"<html><nobr>[%] A very short component (TopLeft)</nobr>",
                                 "<html><nobr>[%] A much longer, unbreakable label for wrapping demo (Default)</nobr>",
                                 "<html><nobr>[%] And again a short one (RightBottom, red)</nobr>",
                                 "<html><nobr>[%] A 2-line</nobr><br/><nobr>label (CenterCenter, bold-italic)</nobr>"};
         final SFont boldItalic = new SFont(null, SFont.BOLD + SFont.ITALIC, SFont.DEFAULT_SIZE);
         final SBorder greenLineBorder = new SLineBorder();
         final SLabel label = new SLabel(texts[i % 4].replace('%', Integer.toString((i + 1)).charAt(0)));
         greenLineBorder.setColor(Color.green);
         label.setBorder(greenLineBorder);
         if (i % texts.length == 0) {
             label.setVerticalAlignment(TOP);
             label.setHorizontalAlignment(LEFT);
         }
         if (i % texts.length == 1) ;
         if (i % texts.length == 2) {
             label.setForeground(Color.RED);
             label.setHorizontalAlignment(RIGHT);
             label.setVerticalAlignment(BOTTOM);
         }
         if (i % texts.length == 3) {
             label.setHorizontalAlignment(CENTER);
             label.setVerticalAlignment(CENTER);
             label.setFont(boldItalic);
         }
         return label;
     }
 
     private static SPanel createPanel(SLayoutManager layout, int amount) {
         final SPanel panel = new SPanel(layout);
         panel.setBackground(new Color(210, 210, 210));
         addDummyLabels(panel, amount);
         return panel;
     }
 
     private static class BorderLayoutDemoPanel extends SPanel {
         int i = 0;
         public BorderLayoutDemoPanel() {
             super(new SFlowDownLayout());
             add(new SLabel("Default"));
             SPanel borderDemoPanel1 = new SPanel(new SBorderLayout());
             borderDemoPanel1.add(wrap(createDummyLabel(0)), SBorderLayout.NORTH);
             borderDemoPanel1.add(wrap(createDummyLabel(1)), SBorderLayout.SOUTH);
             borderDemoPanel1.add(wrap(createDummyLabel(2)), SBorderLayout.EAST);
             borderDemoPanel1.add(wrap(createDummyLabel(3)), SBorderLayout.WEST);
             borderDemoPanel1.add(wrap(createDummyLabel(4)), SBorderLayout.CENTER);
             add(borderDemoPanel1);
             borderDemoPanel1.setPreferredSize(new SDimension(800,200));
             borderDemoPanel1.setBackground(new Color(210, 210, 210));
 
             add(new SLabel("Spacing and Border"));
             SPanel borderDemoPanel2 = new SPanel(new SBorderLayout(10, 1));
             borderDemoPanel2.add(wrap(createDummyLabel(0+3)), SBorderLayout.NORTH);
             borderDemoPanel2.add(wrap(createDummyLabel(1+3)), SBorderLayout.SOUTH);
             borderDemoPanel2.add(wrap(createDummyLabel(2+3)), SBorderLayout.EAST);
             borderDemoPanel2.add(wrap(createDummyLabel(3+3)), SBorderLayout.WEST);
             borderDemoPanel2.add(wrap(createDummyLabel(4+3)), SBorderLayout.CENTER);
             add(borderDemoPanel2);
             borderDemoPanel2.setPreferredSize(new SDimension(800,200));
             borderDemoPanel2.setBackground(new Color(210, 210, 210));
         }
 
         private SPanel wrap(SComponent c) {
             final Color[] colors = { Color.red, Color.green, Color.pink, Color.magenta, Color.gray, Color.cyan };
             SPanel p = new SPanel();
             p.add(c);
             p.setPreferredSize(new SDimension("100%","100%"));
             p.setVerticalAlignment(c.getVerticalAlignment());
             p.setHorizontalAlignment(c.getHorizontalAlignment());
             p.setBackground(colors[i++ % colors.length]);
             return p;
         }
     }
 
     private static class BoxLayoutDemoPanel extends SPanel {
         public BoxLayoutDemoPanel() {
             add(new SLabel("Horizontal box layout with padding & border"));
             SBoxLayout horizontalLayout = new SBoxLayout(SBoxLayout.HORIZONTAL);
             horizontalLayout.setCellPadding(10);
             horizontalLayout.setBorder(1);
             add(createPanel(horizontalLayout, 5));
 
             add(new SLabel("Vertical vanilla box layout"));
             SBoxLayout verticalLayout = new SBoxLayout(SBoxLayout.VERTICAL);
             add(createPanel(verticalLayout, 5));
         }
     }
 
     private static class FlowLayoutDemoPanel extends SPanel {
         public FlowLayoutDemoPanel() {
             super(new SFlowDownLayout());
             add(new SLabel("Horizontal, centered FlowLayout"));
             add(createPanel(new SFlowLayout(SFlowLayout.CENTER), 4));
             add(new SLabel("Horizontal, left-aligned FlowLayout"));
             add(createPanel(new SFlowLayout(SFlowLayout.LEFT), 4));
             add(new SLabel("Horizontal, right-aligned FlowLayout"));
             add(createPanel(new SFlowLayout(SFlowLayout.RIGHT), 4));
             add(new SLabel("<html>&nbsp;"));
             add(new SLabel("Vertical, centered FlowLayout"));
             add(createPanel(new SFlowDownLayout(SConstants.CENTER), 4));
             add(new SLabel("Vertical, left-aligned FlowLayout"));
             add(createPanel(new SFlowDownLayout(), 3));
             add(new SLabel("Vertical, right-aligned FlowLayout"));
             add(createPanel(new SFlowDownLayout(SConstants.RIGHT), 4));
         }
     }
 
     private static class GridLayoutDemoPanel extends SPanel {
         public GridLayoutDemoPanel() {
             add(new SLabel("Grid Layout panel with 3 colums, border, padding & spacing"));
             SGridLayout layout1 = new SGridLayout(3);
             layout1.setBorder(1);
             layout1.setCellPadding(10);
             layout1.setCellSpacing(20);
 
             add(createPanel(layout1, 12));
         }
     }
 
     private static class GridBagDemoPanel extends SPanel {
         public GridBagDemoPanel() {
             SFlowDownLayout flowLayout = new SFlowDownLayout();
             setLayout(flowLayout);
             setHorizontalAlignment(CENTER);
 
             add(new SLabel("Horizontal adding using REMAINDER"));
             SGridBagLayout layout = new SGridBagLayout();
             layout.setBorder(1);
             SPanel p = new SPanel(layout);
             p.setPreferredSize(new SDimension(300, 100));
             p.setBackground(Color.red);
             add(p);
 
             GridBagConstraints c = new GridBagConstraints();
             c.gridwidth = GridBagConstraints.REMAINDER;
             p.add(new SLabel("1"), c);
             c.gridwidth = 1;
 
             p.add(new SLabel("2"), c);
             c.gridwidth = GridBagConstraints.REMAINDER;
             p.add(new SLabel("3"), c);
             c.gridwidth = 1;
 
             p.add(new SLabel("4"), c);
             p.add(new SLabel("5"), c);
             c.gridwidth = GridBagConstraints.REMAINDER;
             p.add(new SLabel("6"), c);
             c.gridwidth = 1;
 
             p.add(new SLabel("7"), c);
             p.add(new SLabel("8"), c);
             p.add(new SLabel("9"), c);
             c.gridwidth = GridBagConstraints.REMAINDER;
             p.add(new SLabel("10"), c);
 
 
             add(new SLabel("Vertical adding using pre-defined gridx"));
             layout = new SGridBagLayout();
             layout.setBorder(1);
             p = new SPanel(layout);
             add(p);
 
             c = new GridBagConstraints();
             c.gridx = 0;
             c.gridheight = GridBagConstraints.REMAINDER;
             p.add(new SLabel("1"), c);
             c.gridheight = 1;
 
             c.gridx = 1;
             p.add(new SLabel("2"), c);
             c.gridheight = GridBagConstraints.REMAINDER;
             p.add(new SLabel("3"), c);
             c.gridheight = 1;
 
             c.gridx = 2;
             p.add(new SLabel("4"), c);
             p.add(new SLabel("5"), c);
             c.gridheight = GridBagConstraints.REMAINDER;
             p.add(new SLabel("6"), c);
             c.gridheight = 1;
 
             c.gridx = 3;
             p.add(new SLabel("7"), c);
             p.add(new SLabel("8"), c);
             p.add(new SLabel("9"), c);
             c.gridheight = GridBagConstraints.REMAINDER;
             p.add(new SLabel("10"), c);
 
             add(new SLabel("Random adding with pre-defined gridx+gridy"));
             layout = new SGridBagLayout();
             layout.setBorder(1);
             p = new SPanel(layout);
             add(p);
 
             c = new GridBagConstraints();
             c.gridx = 4;
             c.gridy = 0;
             p.add(new SLabel("1"), c);
             c.gridx = 3;
             c.gridy = 1;
             p.add(new SLabel("2"), c);
             c.gridx = 2;
             c.gridy = 2;
             p.add(new SLabel("3"), c);
             c.gridx = 1;
             c.gridy = 3;
             p.add(new SLabel("4"), c);
             c.gridx = 0;
             c.gridy = 4;
             p.add(new SLabel("5"), c);
 
 
             add(new SLabel("Using weight"));
             layout = new SGridBagLayout();
             layout.setBorder(1);
             p = new SPanel(layout);
             add(p);
             p.setPreferredSize(new SDimension(500, 500));
 
             c = new GridBagConstraints();
             c.gridx = 0;
             c.gridy = 0;
             c.weightx = 0;
             c.weighty = 0;
             p.add(new SLabel("1"), c);
             c.gridx = 1;
             c.gridy = 0;
             c.weightx = 1;
             c.weighty = 0;
             p.add(new SLabel("2"), c);
             c.gridx = 2;
             c.gridy = 0;
             c.weightx = 2;
             c.weighty = 0;
             p.add(new SLabel("3"), c);
             c.gridx = 3;
             c.gridy = 0;
             c.weightx = 1;
             c.weighty = 0;
             p.add(new SLabel("4"), c);
             c.gridx = 4;
             c.gridy = 0;
             c.weightx = 0;
             c.weighty = 0;
             p.add(new SLabel("5"), c);
 
             c.gridx = 0;
             c.gridy = 1;
             c.weightx = 0;
             c.weighty = 1;
             p.add(new SLabel("6"), c);
             c.gridx = 1;
             c.gridy = 1;
             c.weightx = 1;
             c.weighty = 1;
             p.add(new SLabel("7"), c);
             c.gridx = 2;
             c.gridy = 1;
             c.weightx = 2;
             c.weighty = 1;
             p.add(new SLabel("8"), c);
             c.gridx = 3;
             c.gridy = 1;
             c.weightx = 1;
             c.weighty = 1;
             p.add(new SLabel("9"), c);
             c.gridx = 4;
             c.gridy = 1;
             c.weightx = 0;
             c.weighty = 1;
             p.add(new SLabel("10"), c);
 
             c.gridx = 0;
             c.gridy = 2;
             c.weightx = 0;
             c.weighty = 2;
             p.add(new SLabel("11"), c);
             c.gridx = 1;
             c.gridy = 2;
             c.weightx = 1;
             c.weighty = 2;
             p.add(new SLabel("12"), c);
             c.gridx = 2;
             c.gridy = 2;
             c.weightx = 2;
             c.weighty = 2;
             p.add(new SLabel("13"), c);
             c.gridx = 3;
             c.gridy = 2;
             c.weightx = 1;
             c.weighty = 2;
             p.add(new SLabel("14"), c);
             c.gridx = 4;
             c.gridy = 2;
             c.weightx = 0;
             c.weighty = 2;
             p.add(new SLabel("15"), c);
             c.gridx = 0;
             c.gridy = 3;
             c.weightx = 0;
             c.weighty = 1;
             p.add(new SLabel("16"), c);
             c.gridx = 1;
             c.gridy = 3;
             c.weightx = 1;
             c.weighty = 1;
             p.add(new SLabel("17"), c);
             c.gridx = 2;
             c.gridy = 3;
             c.weightx = 2;
             c.weighty = 1;
             p.add(new SLabel("18"), c);
             c.gridx = 3;
             c.gridy = 3;
             c.weightx = 1;
             c.weighty = 1;
             p.add(new SLabel("19"), c);
             c.gridx = 4;
             c.gridy = 3;
             c.weightx = 0;
             c.weighty = 1;
             p.add(new SLabel("20"), c);
             c.gridx = 0;
             c.gridy = 4;
             c.weightx = 0;
             c.weighty = 0;
             p.add(new SLabel("21"), c);
             c.gridx = 1;
             c.gridy = 4;
             c.weightx = 1;
             c.weighty = 0;
             p.add(new SLabel("22"), c);
             c.gridx = 2;
             c.gridy = 4;
             c.weightx = 2;
             c.weighty = 0;
             p.add(new SLabel("23"), c);
             c.gridx = 3;
             c.gridy = 4;
             c.weightx = 1;
             c.weighty = 0;
             p.add(new SLabel("24"), c);
             c.gridx = 4;
             c.gridy = 4;
             c.weightx = 0;
             c.weighty = 0;
             p.add(new SLabel("25"), c);
 
 
             add(new SLabel("Adding with gridwidth=RELATIVE"));
             layout = new SGridBagLayout();
             layout.setBorder(1);
             p = new SPanel(layout);
             add(p);
             c = new GridBagConstraints();
             p.add(new SLabel("1"), c);
             p.add(new SLabel("2"), c);
             p.add(new SLabel("3"), c);
             p.add(new SLabel("4"), c);
             c.gridwidth = GridBagConstraints.RELATIVE;
             p.add(new SLabel("5"), c);
             c.gridwidth = 1;
             p.add(new SLabel("end #1"), c);
 
             p.add(new SLabel("6"), c);
             p.add(new SLabel("7"), c);
             p.add(new SLabel("8"), c);
             c.gridwidth = GridBagConstraints.RELATIVE;
             p.add(new SLabel("9"), c);
             c.gridwidth = 1;
             p.add(new SLabel("end #2"), c);
 
             p.add(new SLabel("10"), c);
             p.add(new SLabel("11"), c);
             c.gridwidth = GridBagConstraints.RELATIVE;
             p.add(new SLabel("12"), c);
             c.gridwidth = 1;
             p.add(new SLabel("end #3"), c);
 
             p.add(new SLabel("13"), c);
             c.gridwidth = GridBagConstraints.RELATIVE;
             p.add(new SLabel("14"), c);
             c.gridwidth = 1;
             p.add(new SLabel("end #4"), c);
 
             c.gridwidth = GridBagConstraints.RELATIVE;
             p.add(new SLabel("15"), c);
             c.gridwidth = 1;
             p.add(new SLabel("end #5"), c);
 
 
             add(new SLabel("Vertical adding with gridheight=RELATIVE"));
             layout = new SGridBagLayout();
             layout.setBorder(1);
             p = new SPanel(layout);
             add(p);
             c = new GridBagConstraints();
 
             c.gridx = 0;
             p.add(new SLabel("1"), c);
             p.add(new SLabel("2"), c);
             p.add(new SLabel("3"), c);
             p.add(new SLabel("4"), c);
             c.gridheight = GridBagConstraints.RELATIVE;
             p.add(new SLabel("5"), c);
             c.gridheight = 1;
             p.add(new SLabel("end #1"), c);
 
             c.gridx = 1;
             p.add(new SLabel("6"), c);
             p.add(new SLabel("7"), c);
             p.add(new SLabel("8"), c);
             c.gridheight = GridBagConstraints.RELATIVE;
             p.add(new SLabel("9"), c);
             c.gridheight = 1;
             p.add(new SLabel("end #2"), c);
 
             c.gridx = 2;
             p.add(new SLabel("10"), c);
             p.add(new SLabel("11"), c);
             c.gridheight = GridBagConstraints.RELATIVE;
             p.add(new SLabel("12"), c);
             c.gridheight = 1;
             p.add(new SLabel("end #3"), c);
 
             c.gridx = 3;
             p.add(new SLabel("13"), c);
             c.gridheight = GridBagConstraints.RELATIVE;
             p.add(new SLabel("14"), c);
             c.gridheight = 1;
             p.add(new SLabel("end #4"), c);
 
             c.gridx = 4;
             c.gridheight = GridBagConstraints.RELATIVE;
             p.add(new SLabel("15"), c);
             c.gridheight = 1;
             p.add(new SLabel("end #5"), c);
         }
 
     }
 
 
 }
