 /*
  * Copyright 2014 Marc Sluiter
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
 
 package net.slintes.raspidroid;
 
 import com.leapmotion.leap.*;
 
 import java.net.URI;
 
 /**
  * Created by slintes on 10.02.14.
  */
 public class LeapListener extends Listener {
 
     private static final String RASPI_URL = "ws://192.168.0.102:8888";
     private WSClient wsClient;
 
     public LeapListener() {
         wsClient = new WSClient(URI.create(RASPI_URL));
         wsClient.connect();
     }
 
     @Override
     public void onConnect(Controller controller) {
         System.out.println("leapmotion controller connected");
         super.onConnect(controller);
     }
 
     @Override
     public void onFrame(Controller controller) {
         super.onFrame(controller);
 
         Frame frame = controller.frame();
         FingerList fingers = frame.fingers();
 //        System.out.println("nr of fingers: " + fingers.count());
 
         if (!fingers.isEmpty()) {
             Finger finger = fingers.get(0);
             Vector pos = finger.tipPosition();
             float posX = pos.getX();
             float posY = pos.getY();
             // x: -200 .. 200
             // y: 100 .. 300
             // we need 0..7
             int x = (int) ((posX + 200) / 400 * 8);
             int y = (int) ((posY - 100) / 200 * 8);
             x = Math.max(0, Math.min(7, x));
             y = Math.max(0, Math.min(7, y));
//            System.out.println("x: " + x + ", y: " + y);
             wsClient.send(new RDMessage(7 - y, x));
             try {
                 Thread.sleep(50);
             } catch (InterruptedException e) {
             }
 
         }
     }
 }
