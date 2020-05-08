 /*
  * Copyright 2013 maxstrauch
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
 package simulation.traffic;
 
 import javax.swing.JComponent;
 import javax.swing.JTabbedPane;
 
 import simulation.traffic.highway.HighwaySimulationGui;
 import simulation.traffic.motorway.MotorwaySimulationGui;
 
 public class TrafficSimulation {
 
     public static JComponent getGUI() {
         // Create and set up the content pane
         JTabbedPane tab = new JTabbedPane();
         
         HighwaySimulationGui hiWa = new HighwaySimulationGui();
         hiWa.setOpaque(false);
        tab.addTab("Highway", hiWa);
         
         MotorwaySimulationGui moWa = new MotorwaySimulationGui();
         moWa.setOpaque(false);
         tab.addTab("Motorway", moWa);
        
        return tab;
     }
     
     public static String getName() {
     	return "Traffic simulation";
     }
     
 }
