 
 package org.nsu.vectoreditor;
 
 public class Scene extends java.awt.Component {
 
     public void paint(java.awt.Graphics graphics) {
         Shape shape = first;
         while(shape != null) {
             shape.draw(graphics);
             shape = shape.next;
         }
     }
 
     public void addShape(Shape shape) {
 
         if(first == null) {
            // no items in the list
             first = shape;
             last = shape;
         } else {
             last.next = shape;
             shape.prev = last;
             last = shape;
         }
     }
 
 
     public void addShapeBefore(Shape s, Shape before) {
 
         Shape shape = first;
         while(shape != null) {
             if(shape == before) {
 
                 s.next = shape;
 
                 if(shape.prev != null) {
                     shape.prev.next = s;
                     s.prev = shape.prev;
                 } else {
                     first = s;
                 }
 
                 shape.prev = s;
                 break;
             }
 
             shape = shape.next;
         }
     }
 
     public void removeShape(Shape s) {
         Shape shape = first;
 
         while(shape != null) {
 
             if(shape == s) {
                 if(shape == first && shape == last) {
                     first = null;
                     last = null;
                 } else if(shape == first) {
                     first = shape.next;
                     shape.next.prev = null;
                 } else if(shape == last) {
                     last = shape.prev;
                     shape.prev.next = null;
                 } else {
                     shape.prev.next = shape.next;
                     shape.next.prev = shape.prev;
                 }
             }
 
             shape = shape.next;
         }
     }
 
 
     Shape first;
     Shape last;
 }
 
