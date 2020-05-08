 package com.anotherbrick.inthewall;
 
 import java.util.ArrayList;
 
 import processing.core.PApplet;
 
 import com.anotherbrick.inthewall.Config.MyColorEnum;
 
 public class VizList extends VizPanel implements TouchEnabled {
 
   private int numOfRows = 10;
   private ArrayList<Object> selectedObjects = new ArrayList<Object>();
 
   public static enum SelectionMode {
     NOT_SELECTABLE, MULTIPLE, SINGLE, SINGLE_OR_WHOLE_CATEGORY
   };
 
   private SelectionMode selectionMode;
   private String name;
 
   public VizList(float x0, float y0, float width, float height, VizPanel parent) {
     super(x0, y0, width, height, parent);
   }
 
   public ArrayList<Object> getSelected() {
     return selectedObjects;
   }
 
   public void setSelected(ArrayList<Object> selectedObjects) {
     this.selectedObjects = selectedObjects;
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public boolean touch(float x, float y, boolean down, TouchTypeEnum touchType) {
     if (!isVisible())
       return false;
     if (selectionMode == SelectionMode.NOT_SELECTABLE)
       return true;
     setToRedraw();
     if (!propagateTouch(x, y, down, touchType)) {
       if (!down) {
         Object element = null;
        int row = (int) (((y - getY0Absolute()) / getHeight()) * numOfRows);
         if (isNested) {
           // get the right elements
           int index = 0;
           boolean isCategory = false;
           Object category = null;
           for (Object xx : elements) {
             boolean isCategoryTemp = true;
             for (Object yy : (ArrayList<Object>) xx) {
               if (index == (row + startIndex)) {
                 element = yy;
                 isCategory = isCategoryTemp;
                 category = xx;
               }
               index++;
               isCategoryTemp = false;
             }
           }
           if (isCategory) {
             if (selectedObjects.contains(((ArrayList<Object>) category).get(1))) {
               boolean first = true;
               for (Object yy : (ArrayList<Object>) category) {
                 if (!first) {
                   deselectElement(
                       yy,
                       selectionMode == SelectionMode.SINGLE_OR_WHOLE_CATEGORY ? SelectionMode.MULTIPLE
                           : selectionMode);
                 }
                 first = false;
               }
             } else {
               boolean first = true;
               if (selectionMode == SelectionMode.SINGLE_OR_WHOLE_CATEGORY)
                 selectedObjects.clear();
               for (Object yy : (ArrayList<Object>) category) {
                 if (!first) {
                   selectElement(
                       yy,
                       selectionMode == SelectionMode.SINGLE_OR_WHOLE_CATEGORY ? SelectionMode.MULTIPLE
                           : selectionMode);
                 }
                 first = false;
               }
             }
           } else {
             toggleElement(element, selectionMode);
           }
         } else {
           element = elements.get(row + startIndex);
           toggleElement(element, selectionMode);
         }
       }
       return true;
     }
     return true;
   }
 
   private void selectElement(Object element, SelectionMode s) {
     if (!selectedObjects.contains(element)) {
       if (s == SelectionMode.SINGLE || s == SelectionMode.SINGLE_OR_WHOLE_CATEGORY) {
         selectedObjects.clear();
       }
       selectedObjects.add(element);
     }
 
   }
 
   private void deselectElement(Object element, SelectionMode s) {
     if (s == SelectionMode.SINGLE_OR_WHOLE_CATEGORY)
       selectedObjects.clear();
     if (selectedObjects.contains(element)) {
       selectedObjects.remove(element);
     }
   }
 
   private void toggleElement(Object element, SelectionMode s) {
     if (selectedObjects.contains(element)) {
       deselectElement(element, s);
     } else {
       selectElement(element, s);
     }
   }
 
   public void setListName(String name) {
     this.name = name;
   }
 
   public String getListName() {
     return this.name;
   }
 
   public String toString() {
     return "VizList (" + name + ")";
   }
 
   private MyColorEnum oddRowsColor, evenRowsColor;
   public MyColorEnum strokeColor = MyColorEnum.BLACK;
   public float strokeWeight = 0;
   private ArrayList<? extends Object> elements;
   private int startIndex, stopIndex;
   private float rowHeight;
   private VizSlider slider;
   private boolean isNested;
   private int expandedSize;
   public int cropAtNChars = -1;
 
   @SuppressWarnings("unchecked")
   public void setup(MyColorEnum oddRowsColor, MyColorEnum evenRowsColor, int numOfRows,
       ArrayList<? extends Object> elements, boolean isNested, SelectionMode selectionMode) {
     this.selectionMode = selectionMode;
     this.evenRowsColor = evenRowsColor;
     this.oddRowsColor = oddRowsColor;
     this.elements = elements;
     this.isNested = isNested;
     startIndex = 0;
     stopIndex = numOfRows;
     this.numOfRows = numOfRows;
     this.rowHeight = (getHeight() - numOfRows) / stopIndex;
 
     if (isNested) {
       expandedSize = 0;
       for (Object x : elements) {
         expandedSize += ((ArrayList<Object>) x).size();
       }
     }
 
     slider = new VizSlider(getWidth() - 20, 0, 20, getHeight(), this);
     slider.setup();
     addTouchSubscriber(slider);
 
   }
 
   public ArrayList<? extends Object> getElements() {
     return elements;
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public boolean draw() {
     if (!startDraw())
       return false;
     pushStyle();
     noStroke();
     background(MyColorEnum.MEDIUM_GRAY);
     //
     if (isNested) {
       for (int i = startIndex, j = 0; i < stopIndex && i < expandedSize && j < numOfRows; i++, j++) {
         VizRow row = new VizRow(5, rowHeight * j + 5, getWidth() - 30, rowHeight, this);
         row.cropAtNChars = cropAtNChars;
         // get the right elements
         int index = 0;
         boolean isCategory = false;
         Object obj = null;
         for (Object x : elements) {
           boolean isCategoryTemp = true;
           for (Object y : (ArrayList<Object>) x) {
             if (index == i) {
               obj = y;
               isCategory = isCategoryTemp;
             }
             index++;
             isCategoryTemp = false;
           }
         }
         // and use it..
         if (selectedObjects.contains(obj)) {
           row.selected = true;
         }
         if (isCategory) {
           row.backgroundColor = MyColorEnum.RED;
         } else {
           if (i % 2 == 0) {
             row.backgroundColor = evenRowsColor;
           } else {
             row.backgroundColor = oddRowsColor;
           }
         }
         slider.draw();
         row.backgroundColorSelected = MyColorEnum.LIGHT_BLUE;
         row.drawBackground();
         row.drawStrings(c.translate(obj.toString()));
       }
     } else {
       for (int i = startIndex, j = 0; i < stopIndex && i < elements.size() && j < numOfRows; i++, j++) {
         VizRow row = new VizRow(5, rowHeight * j + 5, getWidth() - 30, rowHeight, this);
         row.cropAtNChars = cropAtNChars;
         if (selectedObjects.contains(elements.get(i))) {
           row.selected = true;
         }
         if (i % 2 == 0) {
           row.backgroundColor = evenRowsColor;
         } else {
           row.backgroundColor = oddRowsColor;
         }
         slider.draw();
         row.backgroundColorSelected = MyColorEnum.LIGHT_BLUE;
         row.drawBackground();
         row.drawStrings(c.translate(elements.get(i).toString()));
       }
     }
     noFill();
     if (strokeWeight > 0) {
       stroke(strokeColor);
       strokeWeight(strokeWeight);
     } else {
       noStroke();
     }
     rect(0, -1, getWidth(), getHeight() + 2);
     popStyle();
     return endDraw(slider.handle.moving);
   }
 
   private class Handle extends VizPanel {
 
     public boolean moving;
 
     public Handle(float x0, float y0, VizPanel parent) {
       super(x0, y0, 20, 20, parent);
       moving = false;
     }
 
     @Override
     public boolean draw() {
       pushStyle();
       fill(MyColorEnum.LIGHT_GRAY);
       rect(2, 2, 16, 16, 5, 5, 5, 5);
       popStyle();
       return false;
     }
 
     @Override
     public void setup() {
     }
 
   }
 
   private class VizSlider extends VizPanel implements TouchEnabled {
 
     private Handle handle;
 
     public VizSlider(float x0, float y0, float width, float height, VizPanel parent) {
       super(x0, y0, width, height, parent);
       handle = new Handle(0, 0, this);
     }
 
     @Override
     public boolean draw() {
       pushStyle();
       noStroke();
       background(MyColorEnum.DARK_GRAY);
       handle.draw();
       popStyle();
       updateHandlePosition();
       return false;
     }
 
     @Override
     public boolean touch(float x, float y, boolean down, TouchTypeEnum touchType) {
       if (down) {
         if (handle.containsPoint(x, y)) {
           handle.moving = true;
           System.out.println("Handle touched");
         }
         setModal(true);
         return true;
       } else if (!down) {
         handle.moving = false;
         setModal(false);
         println("drop!!");
         return true;
       }
       return false;
     }
 
     public void updateHandlePosition() {
       if (handle.moving) {
         handle.modifyPositionWithAbsoluteValue(getX0Absolute(),
             costrain(m.touchY - 11, getY0Absolute() + getHeight() - 20, getY0Absolute()));
         float start = PApplet.map(handle.getY0(), 0, getHeight(), 0, isNested ? expandedSize
             : elements.size());
         setStartIndex(start);
         setStopIndex(start + numOfRows);
       }
     }
 
     private float costrain(float value, float maxValue, float minValue) {
       return Math.min(Math.max(value, minValue), maxValue);
     }
 
     @Override
     public void setup() {
     }
 
   }
 
   public void setStopIndex(float f) {
     stopIndex = (int) f;
   }
 
   public void setStartIndex(float f) {
     startIndex = (int) f;
   }
 
   @Override
   public void setup() {
     // TODO Auto-generated method stub
 
   }
 
 }
