 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mss.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.util.ArrayList;
 import java.util.HashMap;
 import org.lwjgl.util.Color;
 
 /**
  *
  * @author Bernhard Sirlinger
  */
 public class Util {
     /**
      *
      */
     public static final double G  = 6.67384e-11;
     
     /**
      *
      * @param m
      * @param vektor Der Vektor zwischen den beiden Planeten
      * @return
      */
     public static Vektor2D getA(double m, Vektor2D vektor) {
         //Der Betrag des Vektors zwischen den Planeten entspricht dem Radius r
         double r = vektor.betrag();
         
         //Berechnung des skalaren Wertes nach Newtons Gravitations Formel F = G * m/r² * rVec/r;
         double skalar = Util.G * (m/(Math.pow(r, 2.0)));
         
         //Normalisierung des Vektors
         vektor.normalisieren();
         
         Vektor2D erg = new Vektor2D(skalar * vektor.getX(), skalar * vektor.getY());
         return erg;
 		}
     
     public static HashMap<String, Object> getDataFromDataFile(File file) {
         HashMap<String, Object> erg = new HashMap<>();
         ArrayList<Planet> planets = new ArrayList<>();
         String data;
         try {
             data = new String(Files.readAllBytes(file.toPath()));
         } catch (IOException ex) {
             erg.put("Error", "The file: " + file.getAbsolutePath() + " could not be opened.");
             return erg;
         }
         String[] lines;
         
         if(data.contains("\r\n")) {
             lines = data.split("\r\n");
         } else {
             lines = data.split("[\n]|[\r]");
         }
         erg.put("Error", "");
         for(int i = 0; i < lines.length; i++) {
             if(!lines[i].matches("^[ ]*$") && !lines[i].trim().startsWith("//")) {
                 if(lines[i].trim().matches("^(deltaT) [0-9]+[\\\\.]{0,1}[0-9]*$")) {
                     String[] temp = lines[i].split(" ");
                     erg.put("deltaT", Double.parseDouble(temp[1]));
                 } else if (lines[i].trim().startsWith("Body")) {
                     String[] temp =  lines[i].split(" ");
                     planets.add(new Planet(temp[1], new Vektor2D(Double.parseDouble(temp[2]), Double.parseDouble(temp[3])), Double.parseDouble(temp[4]), Double.parseDouble(temp[5]), new Vektor2D(Double.parseDouble(temp[6]), Double.parseDouble(temp[7])), new Color(Integer.parseInt(temp[8]), Integer.parseInt(temp[9]), Integer.parseInt(temp[10]))));
                 } else {
                     erg.put("Error", erg.get("Error") + "Invalid line " + (i+1) + "\n");
                 }
             }
         }
         erg.put("Planets", planets);
         if(planets.isEmpty()) {
             erg.put("Error", erg.get("Error") + "No Planets set in the File\n");
         }
         String collisions = Util.findCollisions(planets);
         if(!collisions.isEmpty()) {
             erg.put("Error", erg.get("Error") + collisions);
         }
         
         return erg;
     }
     
     public static String findCollisions(ArrayList<Planet> planets) {
        int size = planets.size();
         String collisions = "";
         
        for(int i = 0; i < size; i++) {  
            for(int j = 0; j < size; j++) {
                 if(i != j) {
                     if(Planet.areColliding(planets.get(i), planets.get(j))) {
                         collisions += "Die Planeten " + planets.get(i).getLabel() + " und " + planets.get(j).getLabel() + " kollidieren.\n";
                     }
                 }
             }
         }
         
         return collisions;
     }
 }
