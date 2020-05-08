 
 package org.nsu.vectoreditor;
 
 public class Main {
     public static void main(String args[]) {
 
         Shape rect = new Rectangle(50, 100, 260, 200);
         Shape circle = new Circle(100, 150, 80);
         Shape triangle = new Triangle(250, 250, 400, 300, 100, 350);
         Shape dummy = new Circle(10, 10, 5);
 
         Scene scene = new Scene();
         scene.addShape(rect);
         scene.addShape(triangle);
        scene.addShapeBefore(dummy, triangle);
         scene.removeShape(dummy);
         scene.addShapeBefore(circle, triangle);
 
         MainWindow mainWindow = new MainWindow(scene);
         mainWindow.setSize(800, 600);
         mainWindow.setVisible(true);
     }
 }
 
