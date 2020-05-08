 /*
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
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.SBoxLayout;
 import org.wings.SButton;
 import org.wings.SComponent;
 import org.wings.SConstants;
 import org.wings.SDimension;
 import org.wings.SGridLayout;
 import org.wings.SIcon;
 import org.wings.SLabel;
 import org.wings.SPanel;
 import org.wings.SURLIcon;
 import org.wings.dnd.DragSource;
 import org.wings.dnd.DropTarget;
 import org.wings.event.SComponentDropListener;
 import org.wings.style.CSSProperty;
 
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * example for showing the drag and drop capabilities of wingS.
  * And "it's got wingS ;)" 
  * @author ole
  *
  */
 public class DragAndDropExample extends WingSetPane {
     private final static Log log = LogFactory.getLog(DragAndDropExample.class);
 
     private static final SURLIcon ICON_BEE1 = new SURLIcon("../icons/bee_1.jpg", 100, 100);
     private static final SURLIcon ICON_BEE2 = new SURLIcon("../icons/bee_2.jpg", 100, 100);
     private static final SURLIcon ICON_BEE3 = new SURLIcon("../icons/bee_3.jpg", 100, 100);
     private static final SURLIcon ICON_BEE4 = new SURLIcon("../icons/bee_4.jpg", 100, 100);
     private static final SURLIcon ICON_BEE5 = new SURLIcon("../icons/bee_5.jpg", 100, 100);
     private static final SURLIcon ICON_BEE6 = new SURLIcon("../icons/bee_6.jpg", 100, 100);
     private static final SURLIcon ICON_BEE7 = new SURLIcon("../icons/bee_7.jpg", 100, 100);
     private static final SURLIcon ICON_BEE8 = new SURLIcon("../icons/bee_8.jpg", 100, 100);
     private static final SURLIcon ICON_BEE9 = new SURLIcon("../icons/bee_9.jpg", 100, 100);
 
     private static final SURLIcon ICON_BEE1_SMALL = new SURLIcon("../icons/bee_1.jpg", 30, 30);
     private static final SURLIcon ICON_BEE2_SMALL = new SURLIcon("../icons/bee_2.jpg", 30, 30);
     private static final SURLIcon ICON_BEE3_SMALL = new SURLIcon("../icons/bee_3.jpg", 30, 30);
     private static final SURLIcon ICON_BEE4_SMALL = new SURLIcon("../icons/bee_4.jpg", 30, 30);
     private static final SURLIcon ICON_BEE5_SMALL = new SURLIcon("../icons/bee_5.jpg", 30, 30);
     private static final SURLIcon ICON_BEE6_SMALL = new SURLIcon("../icons/bee_6.jpg", 30, 30);
     private static final SURLIcon ICON_BEE7_SMALL = new SURLIcon("../icons/bee_7.jpg", 30, 30);
     private static final SURLIcon ICON_BEE8_SMALL = new SURLIcon("../icons/bee_8.jpg", 30, 30);
     private static final SURLIcon ICON_BEE9_SMALL = new SURLIcon("../icons/bee_9.jpg", 30, 30);
 
     private final SDragLabel dragIconOne = new SDragLabel();
     private final SDragLabel dragIconTwo = new SDragLabel();
     private final SDragLabel dragIconThree = new SDragLabel();
     private final SDragLabel dragIconFour = new SDragLabel();
     private final SDragLabel dragIconFive = new SDragLabel();
     private final SDragLabel dragIconSix = new SDragLabel();
     private final SDragLabel dragIconSeven = new SDragLabel();
     private final SDragLabel dragIconEight = new SDragLabel();
     private final SDragLabel dragIconNine = new SDragLabel();
 
     private final SDropLabel dropIconOne = new SDropLabel();
     private final SDropLabel dropIconTwo = new SDropLabel();
     private final SDropLabel dropIconThree = new SDropLabel();
     private final SDropLabel dropIconFour = new SDropLabel();
     private final SDropLabel dropIconFive = new SDropLabel();
     private final SDropLabel dropIconSix = new SDropLabel();
     private final SDropLabel dropIconSeven = new SDropLabel();
     private final SDropLabel dropIconEight = new SDropLabel();
     private final SDropLabel dropIconNine = new SDropLabel();
 
     private final SIcon[] beeIcons = new SIcon[] {ICON_BEE1, ICON_BEE2, ICON_BEE3, ICON_BEE4, ICON_BEE5, ICON_BEE6, ICON_BEE7, ICON_BEE8, ICON_BEE9};
     private final SIcon[] beeIconsSmall = new SIcon[] {ICON_BEE1_SMALL, ICON_BEE2_SMALL, ICON_BEE3_SMALL, ICON_BEE4_SMALL, ICON_BEE5_SMALL, ICON_BEE6_SMALL, ICON_BEE7_SMALL, ICON_BEE8_SMALL, ICON_BEE9_SMALL};
     private final SDragLabel[] dragIcons = new SDragLabel[] {dragIconOne, dragIconTwo, dragIconThree, dragIconFour, dragIconFive, dragIconSix, dragIconSeven, dragIconEight, dragIconNine};
     private final SDropLabel[] dropIcons = new SDropLabel[] {dropIconOne, dropIconTwo, dropIconThree, dropIconFour, dropIconFive, dropIconSix, dropIconSeven, dropIconEight, dropIconNine};
 
     private int[] shuffleTable = new int[] {0,1,2,3,4,5,6,7,8};
 
     private int piecesRight;
     private final SLabel statusLabel = new SLabel();
 
     private ComponentControls controls;
 
     protected SComponent createControls() {
         controls = new Controls();
         return controls;
     }
 
     protected SComponent createExample() {
         final SPanel container = new SPanel(new SBoxLayout(SBoxLayout.HORIZONTAL));
        final SPanel puzzleContainer = new SPanel(new SBoxLayout(SBoxLayout.VERTICAL));
 
         // initialize the drag components
         for (int i = 0; i < dragIcons.length; i++) {
             dragIcons[i].setDragEnabled(true);
             dragIcons[i].setPreferredSize(new SDimension(30,30));
         }
 
         // init the shuffle transformation table
         Random random = new Random();
         for (int i = 0; i < shuffleTable.length; i++) {
             int swapWith = random.nextInt(shuffleTable.length);
             int oldVal = shuffleTable[i];
             shuffleTable[i] = shuffleTable[swapWith];
             shuffleTable[swapWith] = oldVal;
         }
 
         // initialize the drop components
         for (int i = 0; i < dropIcons.length; i++) {
             final int position = i;
             final SDropLabel dropIcon = dropIcons[i];
             dropIcon.setPreferredSize(new SDimension(100,100));
             dropIcon.addComponentDropListener(new SComponentDropListener() {
 
                 // the drag and drop magic
                 public boolean handleDrop(SComponent dragSource) {
                     if (dragIcons[shuffleTable[position]].equals(dragSource)) {
                         dragIcons[shuffleTable[position]].setIcon(null);
                         dropIcon.setIcon(beeIcons[position]);
                         piecesRight++;
                         if (piecesRight == 9) {
                             statusLabel.setText("Look, it's got wingS ;) !");
                             statusLabel.setAttribute(CSSProperty.FONT_WEIGHT, "600");
                             statusLabel.setAttribute(CSSProperty.COLOR, "red");
                         } else {
                             statusLabel.setText("You have " + piecesRight + " pieces right!\nCan you guess what it is?");
                         }
                         return true;
                     }
                     statusLabel.setText("That piece doesn't belong there!\nWhat are you thinking?");
                     return false;
                 }
                 
             });
         }
         resetPuzzle();
         
         // build the puzzle
         SGridLayout gridLayout = new SGridLayout(3,3);
         gridLayout.setBorder(1);
         final SPanel puzzle = new SPanel(gridLayout);
         for (int i = 0; i < dropIcons.length; i++) {
             puzzle.add(dropIcons[i]);
         }
         
         // build the pieces area
         gridLayout = new SGridLayout(9);
         gridLayout.setBorder(1);
         final SPanel pieces = new SPanel(gridLayout);
         for (int i = 0; i < dragIcons.length; i++) {
             pieces.add(dragIcons[i]);
         }
 
         puzzleContainer.add(puzzle);
         puzzleContainer.add(pieces);
 
         statusLabel.setPreferredSize(new SDimension("400px", null));
         
         container.add(puzzleContainer);
         container.add(statusLabel);
         
         return container;
     }
 
     protected void resetPuzzle() {
         piecesRight = 0;
         statusLabel.setText("Try to solve the puzzle.");
         statusLabel.setAttribute(CSSProperty.FONT_WEIGHT, "normal");
         statusLabel.setAttribute(CSSProperty.COLOR, "black");
 
         // initialize the drag components
         for (int i = 0; i < dragIcons.length; i++) {
             SDragLabel dragIcon = dragIcons[shuffleTable[i]];
             dragIcon.setIcon(beeIconsSmall[i]);
         }
         // initialize the drop components
         for (int i = 0; i < dropIcons.length; i++) {
             dropIcons[i].setIcon(null);
         }
     }
 
     /**
      * This class extends the SLabel class with Drag functionality.
      * @author ole
      *
      */
     private static class SDragLabel extends SLabel implements DragSource {
 
         private boolean dragEnabled;
 
         public boolean isDragEnabled() {
             return dragEnabled;
         }
 
         public void setDragEnabled(boolean dragEnabled) {
             this.dragEnabled = dragEnabled;
             if (dragEnabled) {
                 getSession().getDragAndDropManager().registerDragSource(this);
             } else {
                 getSession().getDragAndDropManager().deregisterDragSource(this);
             }
         }
         
     }
     
     
     /**
      * This class extends the SLabel class with Drop functionality.
      * @author ole
      *
      */
     private static class SDropLabel extends SLabel implements DropTarget {
         private ArrayList componentDropListeners = new ArrayList();
 
         /* (non-Javadoc)
          * @see org.wings.dnd.DropTarget#addComponentDropListener(org.wings.event.SComponentDropListener)
          */
         public void addComponentDropListener(SComponentDropListener listener) {
             componentDropListeners.add(listener);
             getSession().getDragAndDropManager().registerDropTarget(this);
         }
 
         /* (non-Javadoc)
          * @see org.wings.dnd.DropTarget#getComponentDropListeners()
          */
         public List getComponentDropListeners() {
             return componentDropListeners;
         }        
     }
 
     class Controls extends ComponentControls {
         public Controls() {
             globalControls.setVisible(false);
 
             final SButton resetButton = new SButton("Reset");
             resetButton.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     resetPuzzle();
                 }
             });
             resetButton.setHorizontalAlignment(SConstants.LEFT_ALIGN);
 
             addControl(resetButton);
         }
     }
 }
