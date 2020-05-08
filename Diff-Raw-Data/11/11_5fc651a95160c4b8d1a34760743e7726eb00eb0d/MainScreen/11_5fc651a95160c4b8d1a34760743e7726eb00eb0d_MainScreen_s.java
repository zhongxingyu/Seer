 package com.sth.petsimulator;
 
 import sofia.graphics.Color;
 import sofia.graphics.ShapeView;
 import android.widget.ProgressBar;
 import sofia.app.ShapeScreen;
 import sofia.graphics.RectangleShape;
 import sofia.app.Screen;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class MainScreen extends Screen{
 
     private Pet pet;
     private RectangleShape petImage;
 
     private ShapeView shapeView;
     private RectangleShape hungerBar;
 

     public void initialize()
     {
         pet = new Pet();
         pet.addObserver(this); // update graphics based on changes here
 
         hungerBar = new RectangleShape(0, shapeView.getHeight() - 20, pet.getHunger(), 20);
         hungerBar.setFillColor(Color.red);
         shapeView.add(hungerBar);
 
     }
 
     public void changeWasObserved(Pet pet)
     {
         hungerBar.setRightBottom(pet.getHunger(), 20);
         petImage.setImage(pet.getAnimationString());
 
     }
 
     public void feedClicked()
     {
         pet.feed();
     }
 
     public void exerciseClicked()
     {
         pet.exercise();
     }
 
 }
