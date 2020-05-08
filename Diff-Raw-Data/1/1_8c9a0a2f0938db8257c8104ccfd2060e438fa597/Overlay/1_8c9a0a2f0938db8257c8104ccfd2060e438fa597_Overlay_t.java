 package org.linesofcode.antfarm.sceneObjects;
 
 import controlP5.ControlP5;
 import controlP5.Slider;
 import org.linesofcode.antfarm.AntFarm;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Dominik Eckelmann
  */
 public class Overlay {
 
     private static final float OVERLAY_KEY_BLOCK = 0.2f;
     private static final char OVERLAY_KEY = 'o';
 
     private final ControlP5 controlP5;
     private final AntFarm antFarm;
 
     private final Map<String, Slider> sliders;
     private int sliderCount;
     private float block;
 
     private boolean visible;
 
     public Overlay(final AntFarm antFarm) {
         this.antFarm = antFarm;
         controlP5 = new ControlP5(antFarm);
         sliders = new HashMap<String, Slider>();
         sliderCount = 0;
         visible = false;
         block = 0;
     }
 
     public Slider addSlider(final String name, final float min, final float max, final float defaultValue) {
         final Slider slider = controlP5.addSlider(name, min, max, defaultValue, 10, sliderCount, 100, 10);
         if (!visible) {
             slider.hide();
         }
         sliders.put(name, slider);
         sliderCount += 15;
         return slider;
     }
 
     public void draw() {
         if (!visible) {
             return;
         }
 
         antFarm.fill(0, 0, 0, 88);
        antFarm.stroke(0, 0, 0, 88);
         antFarm.strokeWeight(0);
         antFarm.rect(0, 0, antFarm.width, antFarm.height);
 
     }
 
     public void update(final float delta) {
         block += delta;
         if (block > OVERLAY_KEY_BLOCK && antFarm.keyPressed) {
             if (antFarm.key == OVERLAY_KEY) {
                 visible = !visible;
                 if (visible) {
                     showSlider();
                 } else {
                     hideSlider();
                 }
             }
             block = 0;
         }
     }
 
     private void showSlider() {
         for (final Slider slider : sliders.values()) {
             slider.show();
         }
     }
 
     private void hideSlider() {
         for (final Slider slider : sliders.values()) {
             slider.hide();
         }
     }
 }
