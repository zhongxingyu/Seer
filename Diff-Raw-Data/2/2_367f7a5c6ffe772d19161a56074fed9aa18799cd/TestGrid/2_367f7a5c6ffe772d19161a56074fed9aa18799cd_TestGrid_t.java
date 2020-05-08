 /*
  * Ninja Trials is an old school style Android Game developed for OUYA & using
  * AndEngine. It features several minigames with simple gameplay.
  * Copyright 2013 Mad Gear Games <madgeargames@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.madgear.ninjatrials.test;
 
 import org.andengine.entity.Entity;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.util.adt.align.HorizontalAlign;
 
 import com.madgear.ninjatrials.managers.ResourceManager;
 
 public class TestGrid extends Entity {
     private final static float WIDTH = ResourceManager.getInstance().cameraWidth;
     private final static float HEIGHT = ResourceManager.getInstance().cameraHeight;
    private final static int COLS = 5;
     private final static float yGap = 100;
     private static float xGap =  WIDTH / COLS;
     private TestGridItem[] items;
     private int index = 0;
     private int numItems = 0;
     
     public TestGrid(int numItemsMax) {
         items = new TestGridItem[numItemsMax];
     }
     
     public void addItem(TestGridItem item) {
         items[numItems] = item;
         item.setTextPosition((numItems % COLS) * xGap + xGap/2,
                 HEIGHT - yGap - (numItems / COLS) * yGap) ;
         attachChild(item);
         numItems++;      
     }
     
     public void onActionPressed() {
         items[index].onAction();
     }
 
     public void selectItem(int i) {
         items[index].onDeselected();
         index = i;
         items[index].onSelected();
     }
     
     public void moveUp() {
         if (index >= COLS) {
             items[index].onDeselected();
             index=- COLS;
             items[index].onSelected();
         }
     }
     
     public void moveDown() {
         if (index < numItems - COLS) {
             items[index].onDeselected();
             index=+ COLS;
             items[index].onSelected();
         }
     }
     
     public void moveLeft() {
         if (index > 0) {
             items[index].onDeselected();
             index--;
             items[index].onSelected();
         }
     }
     
     public void moveRight() {
         if (index < numItems - 1) {
             items[index].onDeselected();
             index++;
             items[index].onSelected();
         }
     }
 }
