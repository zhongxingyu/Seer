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
 package mss.integratoren;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import mss.Observable;
 import mss.Observer;
 import mss.util.Notifications;
 import mss.util.Planet;
 import mss.util.Util;
 import mss.util.Vektor2D;
 
 /**
  *
  * @author Bernhard Sirlinger
  */
 public class Rechenmodul implements Observer, Observable, Runnable {
     private final HashMap<String, Observer> observers = new HashMap<>();
 
     private final ArrayList<Rechner> integratoren = new ArrayList<>();
     private ArrayList<Planet> data = new ArrayList<>();
     private Integratoren integrator;
     private double deltaT;
 
     public Rechenmodul(double deltaT) {
         this.integrator = Integratoren.RUNGE_KUTTA_KLASSISCH;
         this.deltaT = deltaT;
         this.addIntegratoren();
     }
 
     public Rechenmodul(Integratoren integrator, double deltaT) {
         this.integrator = integrator;
         this.deltaT = deltaT;
         this.addIntegratoren();
     }
 
     private void addIntegratoren() {
         this.integratoren.add(new Euler());
         this.integratoren.add(new Leapfrog());
         this.integratoren.add(new RungeKuttaKlassisch());
     }
 
     public void setData(ArrayList<Planet> startPlanets) {
         this.data = startPlanets;
     }
    
     public ArrayList<ArrayList<Planet>> computeUntilT(double deltaT, ArrayList<Planet> startPlanets) {
         this.deltaT = deltaT;
 
         ArrayList<ArrayList<Planet>> erg = new ArrayList<>(500);
         erg.add(startPlanets);
         erg.add(this.rechenschritt(startPlanets));
         for (int i = 1;; i++) {
             erg.add(this.rechenschritt(erg.get(i)));
 
             String collisions = Util.findCollisions(erg.get(i + 1));
             if (!collisions.isEmpty()) {
                 System.out.println(collisions);
                 return erg;
             }
             if(i > 1000/deltaT) {
                 return erg;
             }
         }
     }
 
     public ArrayList<Planet> rechenschritt(ArrayList<Planet> planeten) {
         int size = planeten.size();
         ArrayList<Planet> ergs = new ArrayList<>(size);
 
         //Durchlaufe alle Planeten
         for (int i = 0; i < size; i++) {
             Planet movingPlanet = planeten.get(i);
             Vektor2D currentV = movingPlanet.getV(),
                     deltaCoords,
                     deltaV = new Vektor2D(0, 0);
 
             for (int j = 0; j < size; j++) {
                 if (i != j) {
                     deltaV = Vektor2D.add(deltaV, this.getDeltaV(planeten.get(j), movingPlanet));
                 }
             }
             //System.out.println(deltaV.toString());
             //System.out.println(currentV.getX() + deltaV.getX());
             currentV = new Vektor2D(currentV.getX() + deltaV.getX(), currentV.getY() + deltaV.getY());
             //System.out.println("Current: " + currentV.toString());
             deltaCoords = this.getDeltaCoords(currentV);
             ergs.add(new Planet(movingPlanet.getLabel(), Vektor2D.add(movingPlanet.getCoords(), deltaCoords), movingPlanet.getMass(), movingPlanet.getRadix(), currentV, movingPlanet.getColor()));
         }
 
         return ergs;
     }
 
     private Vektor2D getDeltaCoords(Vektor2D v) {
         switch (this.integrator) {
             case EULER:
                 Euler euler = (Euler) this.integratoren.get(Integratoren.EULER.ordinal());
                 Vektor2D coordsE = euler.getNewX(v, this.deltaT);
                 return coordsE;
             case LEAPFROG:
                 Leapfrog leapfrog = (Leapfrog) this.integratoren.get(Integratoren.LEAPFROG.ordinal());
                 Vektor2D coordsL = leapfrog.getNewX(v, this.deltaT);
                 return coordsL;
             case RUNGE_KUTTA_KLASSISCH:
                 RungeKuttaKlassisch rungekutta = (RungeKuttaKlassisch) this.integratoren.get(Integratoren.RUNGE_KUTTA_KLASSISCH.ordinal());
                 Vektor2D coordsRKK = rungekutta.getNewX(v, this.deltaT);
                 return coordsRKK;
             default:
                 return null;
         }
     }
 
     private Vektor2D getDeltaV(Planet center, Planet moon) {
         switch (this.integrator) {
             case EULER:
                 Euler euler = (Euler) this.integratoren.get(Integratoren.EULER.ordinal());
                 Vektor2D vE = euler.getDeltaV(center.getCoords(), moon.getCoords(), center.getMass(), this.deltaT);
                 return vE;
             case LEAPFROG:
                 Leapfrog leapfrog = (Leapfrog) this.integratoren.get(Integratoren.LEAPFROG.ordinal());
                 Vektor2D vL = leapfrog.getDeltaV(center.getCoords(), moon.getCoords(), center.getMass(), this.deltaT);
                 return vL;
             case RUNGE_KUTTA_KLASSISCH:
                 RungeKuttaKlassisch rungekutta = (RungeKuttaKlassisch) this.integratoren.get(Integratoren.RUNGE_KUTTA_KLASSISCH.ordinal());
                 Vektor2D vRKK = rungekutta.getDeltaV(center.getCoords(), moon.getCoords(), center.getMass(), this.deltaT);
                 return vRKK;
             default:
                 return new Vektor2D();
         }
     }
 
     public void setIntegrator(Integratoren integrator) {
         this.integrator = integrator;
     }
 
     public void setDeltaT(double deltaT) {
         this.deltaT = deltaT;
     }
 
     @Override
     public void registerObserver(String key, Observer observer) {
         this.observers.put(key, observer);
     }
 
     @Override
     public void removeObserver(String key) {
         this.observers.remove(key);
     }
 
     @Override
     public void notifyObservers(Notifications type, String data) {
         Collection<Observer> values = this.observers.values();
         Object[] toArray = values.toArray();
         for (Object temp : toArray) {
             ((Observer) temp).notify(type, data);
         }
     }
 
     @Override
     public void run() {
         ArrayList<ArrayList<Planet>> computedValues = this.computeUntilT(this.deltaT, this.data);
         this.sendPlanetsToObservers(Notifications.RESULT, computedValues);
     }
 
     @Override
     public void notify(Notifications type, String data) {
         switch (type) {
             case DELTA_CHANGE:
                 this.setDeltaT(Double.parseDouble(data));
                 break;
         }
     }
 
     @Override
     public void sendPlanets(Notifications type, ArrayList<ArrayList<Planet>> planets) {
     }
 
     @Override
     public void sendPlanetsToObservers(Notifications type, ArrayList<ArrayList<Planet>> results) {
         Collection<Observer> values = this.observers.values();
         Object[] toArray = values.toArray();
 
         for (Object temp : toArray) {
             ((Observer) temp).sendPlanets(type, results);
         }
     }
 
     public Integratoren getIntegrator() {
         return this.integrator;
     }
 }
