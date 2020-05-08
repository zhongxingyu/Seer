 package org.linesofcode.antfarm.sceneObjects;
 
 import controlP5.*;
 import org.linesofcode.antfarm.AntFarm;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Dominik Eckelmann
  */
 public class Overlay {
 
     private static final float OVERLAY_KEY_BLOCK = 0.2f;
     private static final char OVERLAY_KEY = 'o';
 
     private final ControlP5 controlP5;
     private final AntFarm antFarm;
 
     private final Set<Controller> controlls;
     private final Set<Tab> tabs;
     private float block;
 
     private boolean visible;
 
     public Overlay(final AntFarm antFarm) {
         this.antFarm = antFarm;
         controlP5 = new ControlP5(antFarm);
         controlls = new HashSet<Controller> ();
         tabs = new HashSet<Tab>();
         visible = false;
         block = 0;
         setupGeneral();
         setupAntTab();
         setupStatic();
         hideSlider();
     }
 
     private void setupGeneral() {
         // controlP5.addSlider(name, min, max, defaultValue, x, y, width, heigth)
         Tab defaultTab = controlP5.tab("default");
         defaultTab.setLabel("General");
 
         int col = 10;
 
        final Slider timeLapse = controlP5.addSlider("Time lapse", 0.25f, 8, AntFarm.TIME_LAPSE, 10, col+=30, 100, 20);
        timeLapse.moveTo(defaultTab);
        timeLapse.addListener(new ControlListener() {
             @Override
             public void controlEvent(final ControlEvent controlEvent) {
                AntFarm.TIME_LAPSE = timeLapse.value();
             }
         });
        controlls.add(timeLapse);
 
         final Slider foodCount = controlP5.addSlider("Food count", 0.25f, 8, AntFarm.FOOD_COUNT, 10, col+=30, 100, 20);
         foodCount.moveTo(defaultTab);
         foodCount.addListener(new ControlListener() {
             @Override
             public void controlEvent(final ControlEvent controlEvent) {
                 AntFarm.FOOD_COUNT = (int) foodCount.value();
             }
         });
         controlls.add(foodCount);
 
         CheckBox viewDirection = controlP5.addCheckBox("view direction", 10, col += 30);
         final Toggle viewDirectionButton = viewDirection.addItem("Show view direction", 1);
         viewDirectionButton.addListener(new ControlListener() {
             @Override
             public void controlEvent(ControlEvent controlEvent) {
                 AntFarm.drawViewDirection = viewDirectionButton.value() == 1;
             }
         });
         controlls.add(viewDirectionButton);
     }
 
     private void setupStatic() {
 // controlP5.addSlider(name, min, max, defaultValue, x, y, width, heigth)
         Tab staticTAb = controlP5.tab("Static Objects");
         staticTAb.setLabel("Static Objects");
 
         int col = 10;
 
         final Slider foodSize = controlP5.addSlider("Food Size", 10, 40, Food.SIZE, 10, col+=30, 100, 20);
         foodSize.moveTo(staticTAb);
         foodSize.addListener(new ControlListener() {
             @Override
             public void controlEvent(final ControlEvent controlEvent) {
                 Food.SIZE = (int) foodSize.value();
             }
         });
         controlls.add(foodSize);
 
         final Slider maxFoodCount = controlP5.addSlider("Max food Count", 1, 50, Food.MAX_COUNT, 10, col+=30, 100, 20);
         maxFoodCount.moveTo(staticTAb);
         maxFoodCount.addListener(new ControlListener() {
             @Override
             public void controlEvent(final ControlEvent controlEvent) {
                 Food.MAX_COUNT = (int) maxFoodCount.value();
             }
         });
         controlls.add(maxFoodCount);
 
         final Slider hiveSize = controlP5.addSlider("Hive Size", 5, 30, Hive.SIZE, 10, col+=30, 100, 20);
         hiveSize.moveTo(staticTAb);
         hiveSize.addListener(new ControlListener() {
             @Override
             public void controlEvent(final ControlEvent controlEvent) {
                 Hive.SIZE = (int) hiveSize.value();
             }
         });
         controlls.add(hiveSize);
     }
 
     private void setupAntTab() {
         Tab antTab = controlP5.tab("Ants");
         antTab.setLabel("Ants");
 
         int col1 = 10;
 
         final Slider antSize = controlP5.addSlider("Ant size", 0, 10, Ant.SIZE, 10, col1+=30, 100, 20);
         antSize.moveTo(antTab);
         antSize.addListener(new ControlListener() {
             @Override
             public void controlEvent(final ControlEvent controlEvent) {
                 Ant.SIZE = antSize.value();
             }
         });
         controlls.add(antSize);
 
         final Slider antTrailInterval = controlP5.addSlider("Pheromone trail interval", .01f, .75f, Ant.TRAIL_INTERVAL, 10, col1+=30, 100, 20);
         antTrailInterval.moveTo(antTab);
         antSize.addListener(new ControlListener() {
             @Override
             public void controlEvent(final ControlEvent controlEvent) {
                 Ant.TRAIL_INTERVAL = antTrailInterval.value();
             }
         });
         controlls.add(antTrailInterval);
 
         final Slider antMaxTTL = controlP5.addSlider("Max TTL", 200, 500, Ant.MAX_TIME_TO_LIVE, 10, col1+=30, 100, 20);
         antMaxTTL.moveTo(antTab);
         antMaxTTL.addListener(new ControlListener() {
             @Override
             public void controlEvent(final ControlEvent controlEvent) {
                 Ant.MAX_TIME_TO_LIVE = antMaxTTL.value();
             }
         });
         controlls.add(antMaxTTL);
 
         final Slider antMinTTL = controlP5.addSlider("Min TTL", 50, 200, Ant.MIN_TIME_TO_LIVE, 10, col1+=30, 100, 20);
         antMinTTL.moveTo(antTab);
         antMinTTL.addListener(new ControlListener() {
             @Override
             public void controlEvent(final ControlEvent controlEvent) {
                 Ant.MIN_TIME_TO_LIVE = antMinTTL.value();
             }
         });
         controlls.add(antMinTTL);
 
         final Slider antMinWanderingTime = controlP5.addSlider("Min Wandering time", 30, 90, Ant.MIN_WANDERING_TIME, 10, col1+=30, 100, 20);
         antMinWanderingTime.moveTo(antTab);
         antMinWanderingTime.addListener(new ControlListener() {
             @Override
             public void controlEvent(final ControlEvent controlEvent) {
                 Ant.MIN_WANDERING_TIME = antMinWanderingTime.value();
             }
         });
         controlls.add(antMinWanderingTime);
 
         final Slider antMaxWanderingTime = controlP5.addSlider("Max Wandering time", 30, 90, Ant.MAX_WANDERING_TIME, 10, col1+=30, 100, 20);
         antMaxWanderingTime.moveTo(antTab);
         antMaxWanderingTime.addListener(new ControlListener() {
             @Override
             public void controlEvent(final ControlEvent controlEvent) {
                 Ant.MAX_WANDERING_TIME = antMaxWanderingTime.value();
             }
         });
         controlls.add(antMaxWanderingTime);
 
         final Slider antIdle = controlP5.addSlider("Idle time", 0, 20, Ant.MAX_IDLE_TIME, 10, col1+=30, 100, 20);
         antIdle.moveTo(antTab);
         antIdle.addListener(new ControlListener() {
             @Override
             public void controlEvent(final ControlEvent controlEvent) {
                 Ant.MAX_IDLE_TIME = antIdle.value();
             }
         });
         controlls.add(antIdle);
 
         final Slider antMovementRate = controlP5.addSlider("Movement Rate", 20, 50, Ant.MOVEMENT_RATE, 10, col1+=30, 100, 20);
         antMovementRate.moveTo(antTab);
         antMovementRate.addListener(new ControlListener() {
             @Override
             public void controlEvent(final ControlEvent controlEvent) {
                 Ant.MOVEMENT_RATE = antMovementRate.value();
             }
         });
         controlls.add(antMovementRate);
 
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
         controlP5.show();
     }
 
     private void hideSlider() {
         controlP5.hide();
     }
 }
