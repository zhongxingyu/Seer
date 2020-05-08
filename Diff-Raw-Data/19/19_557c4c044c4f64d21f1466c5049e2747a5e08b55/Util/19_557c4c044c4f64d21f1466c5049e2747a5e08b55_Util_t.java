 /*
  * The MIT License
  *
  * Copyright 2013 Bernhard Sirlinger.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package mss.util;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.util.ArrayList;
 import java.util.HashMap;
 import mss.integratoren.Integratoren;
 import org.lwjgl.util.Color;
 
 /**
  *
  * @author Bernhard Sirlinger
  */
 public class Util {
 
     /**
      *
      */
     public static final double G = 6.67384e-11;
 
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
         double skalar = Util.G * (m / (Math.pow(r, 2.0)));
 
         //Normalisierung des Vektors
         vektor.normalisieren();
 
         Vektor2D erg = new Vektor2D(skalar * vektor.getX(), skalar * vektor.getY());
         return erg;
     }
 
     public static HashMap<String, Object> getDataFromDataFile(File file) {
         HashMap<String, Object> erg = new HashMap<>(2);
         Gson gson = new Gson();
         String error = "";
 
         String data = null;
         try {
            data = new String(Files.readAllBytes(file.toPath()), "UTF-8");
 
             Project project = gson.fromJson(data, Project.class);
 
             String collisions = Util.findCollisions(project.planets);
             if (!collisions.isEmpty()) {
                 error += collisions;
             }
 
             if (error.isEmpty()) {
                 erg.put("Project", project);
             } else {
                 erg.put("Error", error);
             }
         } catch (JsonSyntaxException ex) {
             HashMap<String, Object> tempMap = new HashMap<>();
             ArrayList<Planet> planets = new ArrayList<>();
             String[] lines;
 
             if (data.contains("\r\n")) {
                 lines = data.split("\r\n");
             } else {
                 lines = data.split("[\n]|[\r]");
             }
             erg.put("Error", "");
             erg.put("Integrator", Integratoren.RUNGE_KUTTA_KLASSISCH);
 
             for (int i = 0; i < lines.length; i++) {
                 if (!lines[i].matches("^[ ]*$") && !lines[i].trim().startsWith("//")) {
                     lines[i] = lines[i].trim();
                     if (lines[i].startsWith("Integrator ")) {
                         String temp = lines[i].split(" ")[1];
                         try {
                             tempMap.put("Integrator", Integratoren.valueOf(temp));
                         } catch (IllegalArgumentException e) {
                             tempMap.put("Error", erg.get("Error") + " Invalid Integrator setting on line " + (i + 1) + "\n");
                         }
                     } else if (lines[i].startsWith("deltaT ")) {
                         String temp = lines[i].split(" ")[1];
                         tempMap.put("deltaT", Double.parseDouble(temp));
                     } else if (lines[i].startsWith("Body")) {
                         String[] temp = lines[i].split(" ");
                         planets.add(new Planet(temp[1], new Vektor2D(Double.parseDouble(temp[2]), Double.parseDouble(temp[3])), Double.parseDouble(temp[4]), Double.parseDouble(temp[5]), new Vektor2D(Double.parseDouble(temp[6]), Double.parseDouble(temp[7])), new Color(Integer.parseInt(temp[8]), Integer.parseInt(temp[9]), Integer.parseInt(temp[10]))));
                     } else {
                         tempMap.put("Error", erg.get("Error") + "Invalid line " + (i + 1) + "\n");
                     }
                 }
             }
             tempMap.put("Planets", planets);
             if (planets.isEmpty()) {
                 tempMap.put("Project", erg.get("Error") + "No Planets set in the File\n");
             }
             
             erg.put("Project", new Project(planets, (Integratoren)tempMap.get("Integrator"), (double)tempMap.get("deltaT")));
 
             if(!error.isEmpty()) {
                 error = "The file: " + file.getAbsolutePath() + " has an invalid Project Format.";
                 erg.put("Error", error);
             }
         } catch (IOException ex) {
             error = "The file: " + file.getAbsolutePath() + " could not be opened.";
             erg.put("Error", error);
         }
         return erg;
     }
 
     public static String findCollisions(ArrayList<Planet> planets) {
         String collisions = "";
 
         for (int i = 0; i < planets.size(); i++) {
             for (int j = 0; j < planets.size(); j++) {
                 if (i != j) {
                     if (Planet.areColliding(planets.get(i), planets.get(j))) {
                         collisions += "Die Planeten " + planets.get(i).getLabel() + " und " + planets.get(j).getLabel() + " kollidieren.\n";
                         planets.remove(i);
                         i = (i > 0) ? i -= 1 : i;
                         j = (j > 0) ? j -= 1 : j;
                     }
                 }
             }
         }
 
         return collisions;
     }
 }
