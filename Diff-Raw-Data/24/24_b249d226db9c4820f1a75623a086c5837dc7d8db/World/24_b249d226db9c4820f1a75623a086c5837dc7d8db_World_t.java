 /*
  *  Copyright (C) 2010 Markus Echterhoff <tam@edu.uni-klu.ac.at>
  *
  *  This file is part of EvoPaint.
  *
  *  EvoPaint is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with EvoPaint.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package evopaint;
 
 import evopaint.interfaces.IChanging;
 import evopaint.interfaces.IChangeListener;
 import evopaint.pixel.Pixel;
 import evopaint.pixel.rulebased.RuleBasedPixel;
 
 import evopaint.util.mapping.ParallaxMap;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Markus Echterhoff <tam@edu.uni-klu.ac.at>
  */
 public class World extends ParallaxMap<Pixel> implements IChanging {
 
     private Configuration configuration;
     private final List<IChangeListener> pendingOperations = new ArrayList();
 
     public World(Pixel [] pixels, Configuration configuration) {
         super(pixels, configuration.dimension.width, configuration.dimension.height);
         this.configuration = configuration;
     }
 
     public void step() {
 
         if (pendingOperations.size() > 0) {
             synchronized(pendingOperations) {
                for (IChangeListener listener : pendingOperations) {
                    listener.changed();
                 }
                 pendingOperations.clear();
             }
         }
 
         if (configuration.runLevel != Configuration.RUNLEVEL_RUNNING) {
             // if painting, return so we can paint
             if (configuration.runLevel == Configuration.RUNLEVEL_PAINTING_ONLY) {
                 return;
             }
             // if stopped, sleep
             try {
                 Thread.sleep(500);
             } catch (InterruptedException ex) {
             }
             return;
         }
 
         if (configuration.operationMode == Configuration.OPERATIONMODE_AGENT_SIMULATION) {
             this.stepAgents();
         } else {
             this.stepCellularAutomaton();
         }
     }
 
     public void set(Pixel pixel) {
         super.set(pixel.getLocation().x, pixel.getLocation().y, pixel);
     }
 
     private void stepAgents() {
         int [] indices = getShuffledIndices(configuration.rng);
         
         for (int i = 0; i < indices.length; i++) {
             Pixel pixie = getUnclamped(indices[i]);
             if (pixie.isAlive()) {                  // only act when alive
                 pixie.act(this.configuration);
             }
             if (false == pixie.isAlive()) {     // immediate removal on death
                 set(indices[i], null);
             }
         }
     }
 
     private void stepCellularAutomaton() {
         Pixel [] currentData = getData();
         Pixel [] newData = new Pixel [currentData.length];
 
         for (int i = 0; i < currentData.length; i++) {
             Pixel pixie = currentData[i];
             if (pixie != null) {
                 RuleBasedPixel oldPixie = new RuleBasedPixel((RuleBasedPixel)pixie, false);
                 pixie.act(this.configuration);
                 newData[i] = pixie;
                 currentData[i] = oldPixie;
             }
         }
         
         setData(newData);
     }
 
     public void addChangeListener(IChangeListener subscriber) {
         synchronized(pendingOperations) {
             pendingOperations.add(subscriber);
         }
     }
 
     public void removeChangeListener(IChangeListener subscriber) {
         assert (false); // should not be called since pending operations are cleared automatically
     }
 }
