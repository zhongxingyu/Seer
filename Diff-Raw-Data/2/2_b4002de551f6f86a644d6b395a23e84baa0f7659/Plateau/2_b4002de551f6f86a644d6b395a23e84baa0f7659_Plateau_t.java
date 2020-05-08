 package com.n11.rovers.models;
 
 import com.n11.rovers.exception.OutOfPlateauException;
 import com.n11.rovers.exception.PlateauInitializationException;
 import com.n11.rovers.listener.ChangeEvent;
 import com.n11.rovers.listener.Listener;
 
 public class Plateau implements Listener{
 
     private boolean [][] surface;
 
     public Plateau(Coordinates maxCoordinates) throws PlateauInitializationException {
         super();
 
         if(maxCoordinates.getX() < 0 || maxCoordinates.getY() < 0)
             throw new PlateauInitializationException("Coordinates must be higher than zero!");
 
        this.surface = new boolean [maxCoordinates.getX() + 1][maxCoordinates.getY() + 1];
     }
 
     public boolean[][] getSurface() {
         return surface;
     }
 
     public void setSurface(boolean[][] surface) {
         this.surface = surface;
     }
 
     public void change(ChangeEvent event) throws OutOfPlateauException {
         Coordinates coords = event.getLocation();
         if(coords.getX() >= this.surface.length
                 || coords.getY() >= this.surface[0].length
                 || coords.getX() < 0
                 || coords.getY() < 0)
             throw new OutOfPlateauException();
 
         surface[coords.getX()][coords.getY()] = true;
     }
 }
