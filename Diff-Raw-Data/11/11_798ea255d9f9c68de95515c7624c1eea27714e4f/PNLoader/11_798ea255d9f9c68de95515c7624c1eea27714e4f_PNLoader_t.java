 package org.faur.loader;
 
 import org.faur.api.loader.ILoader;
 import org.faur.api.model.IPlace;
 import org.faur.api.model.ITransition;
 import org.faur.model.Place;
 import org.faur.model.Transition;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * (C) Copyright 2013 Faur Ioan-Aurel.
  * <p/>
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl-2.1.html
  * <p/>
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  * <p/>
  * Created with IntelliJ IDEA.
  * User: faur
  * Date: 10/19/13
  * Time: 6:45 PM
  */
 public class PNLoader implements ILoader {
     private Map<String, IPlace> places;
     private Map<String, ITransition> transitions;
     private File petriFile;
     private Properties properties;
 
     public PNLoader(File petriFile) {
         this.petriFile = petriFile;
         this.places = new HashMap<String, IPlace>();
         this.transitions = new HashMap<String, ITransition>();
         this.properties = new Properties();
         loadPetriNet();
     }
 
     @Override
     public List<IPlace> getPlaces() {
         return new ArrayList<IPlace>(this.places.values());
     }
 
     @Override
     public List<ITransition> getTransitions() {
         return new ArrayList<ITransition>(this.transitions.values());
     }
 
     @Override
     public void setPetriNetLocation(File petriLocation) {
         this.petriFile = petriLocation;
     }
 
     @Override
     public void loadPetriNet() {
         try {
             properties.load(new FileInputStream(this.petriFile));
             String locations = properties.getProperty("locations");
             String transitions = properties.getProperty("transitions");
             String inputLocations = properties.getProperty("inputLocations");
             String outputLocations = properties.getProperty("outputLocations");
 
             this.places = getPlacesFromString(locations);
             this.transitions = getTransitionsFromString(transitions);
             setInputLocationsFromString(inputLocations);
             setOutputLocationsFromString(outputLocations);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public IPlace getPlace(String name) {
         return this.places.get(name);
     }
 
     @Override
     public ITransition getTransition(String name) {
         return this.transitions.get(name);
     }
 
     private Map<String, IPlace> getPlacesFromString(String locations) {
         String[] locs = locations.split(",");
         Map<String, IPlace> places = new HashMap<String, IPlace>();
         for (String location : locs) {
            String tmp = location.substring(1,location.length() - 1);
             String[] data = tmp.split(" ");
             places.put(data[0], new Place(data[0], Integer.valueOf(data[1])));
         }
         return places;
     }
 
     private Map<String, ITransition> getTransitionsFromString(String transitionsString) {
         String[] trans = transitionsString.split(",");
         Map<String, ITransition> toBeReturned = new HashMap<String, ITransition>();
         for (String transition : trans) {
            String tmp = transition.substring(1, transition.length() - 1);
             String[] data = tmp.split(" ");
             Transition t = new Transition(data[0]);
             t.setTime(Integer.valueOf(data[1]), Integer.valueOf(data[2]));
             toBeReturned.put(data[0], t);
         }
         return toBeReturned;
     }
 
     private void setInputLocationsFromString(String inputLocations) {
         String[] splittedData = inputLocations.split(",");
         for (String data : splittedData) {
            String tmp = data.substring(1, data.length() - 1);
             String[] inputLocs = tmp.split(" ");
             // search for the transition
             ITransition transition = getTransition(inputLocs[0]);
             if (transition != null) {
                 List<IPlace> iPlaces = new ArrayList<IPlace>(inputLocs.length);
                 for (int i = 1; i < inputLocs.length; i++) {
                     iPlaces.add(this.places.get(inputLocs[i]));
                 }
                 transition.setInputPlaces(iPlaces);
             }
 
         }
     }
 
     private void setOutputLocationsFromString(String outputLocations) {
         String[] splittedData = outputLocations.split(",");
         for (String data : splittedData) {
            String tmp = data.substring(1, data.length() - 1);
             String[] outputLocs = tmp.split(" ");
             // search for the transition
             ITransition transition = getTransition(outputLocs[0]);
             if (transition != null) {
                 List<IPlace> iPlaces = new ArrayList<IPlace>(outputLocs.length);
                 for (int i = 1; i < outputLocs.length; i++) {
                     iPlaces.add(this.places.get(outputLocs[i]));
                 }
                 transition.setOutputPlaces(iPlaces);
             }
 
         }
     }
 }
