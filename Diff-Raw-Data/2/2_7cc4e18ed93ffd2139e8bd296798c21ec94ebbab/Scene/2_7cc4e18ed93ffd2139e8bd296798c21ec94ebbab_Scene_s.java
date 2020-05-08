 
 package org.nsu.vectoreditor;
 
 public class Scene extends java.awt.Component {
 
     public Scene() {
         shapes = new ShapeList();
     }
 
     public void paint(java.awt.Graphics graphics) {
         ShapeListItem item = shapes.getFirst();
         while(!item.isEnd()) {
             item.getShape().draw(graphics);
             item = item.getNext();
         }
     }
 
     public void addShape(Shape shape) {
         shapes.add(shape);
     }
 
 
     public void addShapeBefore(Shape s, Shape before) {
 
         ShapeListItem item = shapes.getFirst();
         while(!item.isEnd()) {
             if(item.getShape() == before) {
                 shapes.addBefore(s, item);
                 break;
             }
 
             item = item.getNext();
         }
     }
 
     public void removeShape(Shape s) {
 
         ShapeListItem item = shapes.getFirst();
        while(item.isEnd()) {
             if(item.getShape() == s) {
                 shapes.remove(item);
             }
 
             item = item.getNext();
         }
     }
 
     ShapeList shapes;
 }
 
