 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.newdawn.controllers;
 
 import com.newdawn.model.system.Satellite;
 import com.newdawn.model.system.Planet;
 import com.newdawn.model.system.Star;
 import com.newdawn.model.system.StellarSystem;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.ext.DefaultHandler2;
 
 /**
  *
  * @author Pierrick Puimean-Chieze
  */
 public class SystemHandler extends DefaultHandler2 {
 
     public StellarSystem createdSystem;
     private Planet currentPlanet;
     private Satellite currentSatellite;
     private Star currentStar;
     private InitialisationController initialisationController;
     private final String defaultDeltaValue;
 
     public SystemHandler(InitialisationController initialisationController, String defaultDeltaValue) {
         this.initialisationController = initialisationController;
         this.defaultDeltaValue = defaultDeltaValue;
     }
 
     @Override
     public void startDocument() throws SAXException {
         super.startDocument();
         System.out.println("document Started");
     }
 
     @Override
     public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         super.startElement(uri, localName, qName, attributes);
         switch (qName) {
             case "system":
                 initSystem(attributes);
                 break;
             case "star":
                 initCurrentStar(attributes);
                 break;
             case "planet":
                 initCurrentPlanet(attributes);
                 break;
             case "satellite":
                 initCurrentSatellite(attributes);
                 break;
             default:
                 throw new AssertionError("Unknow Element : " + qName);
         }
 
     }
 
     @Override
     public void endElement(String uri, String localName, String qName) throws SAXException {
         super.endElement(uri, localName, qName);
         switch (qName) {
             case "system":
 
                 break;
             case "star":
                 currentStar = null;
                 break;
             case "planet":
                 currentPlanet = null;
                 break;
             case "satellite":
                 currentSatellite = null;
                 break;
             default:
                 throw new AssertionError("Unknow Element : " + qName);
         }
     }
 
     public StellarSystem getCreatedSystem() {
         return createdSystem;
     }
 
     private void initSystem(Attributes attributes) {
         createdSystem = new StellarSystem();
         createdSystem.setName(attributes.getValue("name"));
     }
 
     private void initCurrentStar(Attributes attributes) {
         Star.SpectralClass spectralClass = Star.SpectralClass.valueOf(attributes.
                 getValue("spectralClass").toUpperCase());
         long diameter = Long.parseLong(attributes.getValue("diameter"));
        String name = attributes.getValue("name");
        currentStar = initialisationController.addStarToSystem(createdSystem, name, spectralClass, diameter);
     }
 
     private void initCurrentPlanet(Attributes attributes) {
         Planet.PlanetaryClass planetaryClass = Planet.PlanetaryClass.valueOf(attributes.
                 getValue("planetaryClass").toUpperCase());
         long orbitalRadius = Long.parseLong(attributes.getValue("orbitalRadius"));
         long diameter = Long.parseLong(attributes.getValue("diameter"));
         String name = attributes.getValue("name");
         Long orbitalPeriod = null;
         String orbitalPeriodStr = attributes.getValue("orbitalPeriod");
         if (orbitalPeriodStr != null) {
             orbitalPeriod = Long.parseLong(orbitalPeriodStr);
         }
         //TODO handling the illegal argument exception from parseDelta
         double delta = 0.0;
         delta = parseDelta(attributes.getValue("delta"));
         currentPlanet = initialisationController.addNewPlanetToStar(currentStar, name, planetaryClass, orbitalRadius, diameter, orbitalPeriod, delta);
     }
 
     private void initCurrentSatellite(Attributes attributes) {
         long orbitalRadius = Long.parseLong(attributes.getValue("orbitalRadius"));
         long diameter = Long.parseLong(attributes.getValue("diameter"));
         String name = attributes.getValue("name");
         Long orbitalPeriod = null;
         String orbitalPeriodStr = attributes.getValue("orbitalPeriod");
         if (orbitalPeriodStr != null) {
             orbitalPeriod = Long.parseLong(orbitalPeriodStr);
         }
         currentSatellite = initialisationController.addNewSatelliteToPlanet(currentPlanet, name, orbitalRadius, diameter, orbitalPeriod);
     }
 
     private double parseDelta(String deltaStr) {
         //If the delta String is null
         if (deltaStr == null) {
             //We fall back on the default value 
             deltaStr = defaultDeltaValue;
         }
 
         Matcher piPatternMatcher = Pattern.compile("(\\d+(\\.\\d+)?)?π").matcher(deltaStr);
 
 
         //Check if a random value is asked and return it
         if ("random".equalsIgnoreCase(deltaStr)) {
             return Math.random() * 2 * Math.PI;
         }
         //Check and parse if the delta asked is in the form "Xπ" and return the correct value
         if (piPatternMatcher.matches()) {
             double piCoef = 1;
             String piCoefStr =
                     piPatternMatcher.group(1);
             if (piCoefStr != null && !piCoefStr.isEmpty()) {
                 piCoef = Double.parseDouble(piCoefStr);
             }
             return piCoef * Math.PI;
         }
         //Check and parse if the delta asked is in numerical form and return the parsed value
         Matcher doublePatternMatcher = Pattern.compile("\\d+(\\.\\d+)?").matcher(deltaStr);
         if (doublePatternMatcher.matches()) {
             return Double.parseDouble(deltaStr);
         }
 
         throw new IllegalArgumentException("Incorrect String format for a Delta");
     }
 }
