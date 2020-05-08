 /*
 *  Copyright 2012 Nate Drake
  * 
  *  Licensed to the Apache Software Foundation (ASF) under one or more
  *  contributor license agreements.  See the NOTICE file distributed with
  *  this work for additional information regarding copyright ownership.
  *  The ASF licenses this file to You under the Apache License, Version 2.0
  *  (the "License"); you may not use this file except in compliance with
  *  the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  */
 
 package net.slimeslurp.antblink;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import java.awt.Color;
 
 import thingm.blink1.*;
 
 /**
  * Ant Task that blinks a blink(1).
  *
  * @author ndrake
  *
  */
 public class Blink extends Task {
 
     private String color;
     private int red;
     private int green;
     private int blue;
     
     public Blink() {
                 
     }
 
     /**
      * Blink.
      *
      * @exception BuildException if something goes wrong with the build
      */
     public void execute() throws BuildException {
         
         Blink1 blink1 = new Blink1();
         int rc = blink1.open();
         if( rc != 0 ) { 
           System.err.println("uh oh, no Blink1 device found");
         }
         Color c = null;
         if(color != null && color.length() > 0) {
             c = new Color( 255,0,0 );
         } else {
             c = new Color( red, green, blue); 
         }
         rc = blink1.setRGB( c );
         System.out.println("result: " + rc);
         blink1.close();
     }
 
     public void setColor(String color) {
         this.color = color;
     }
     
     public void setRed(int red) {
         this.red = red;
     }
     
     public void setGreen(int green) {
         this.green = green;
     }
     
     public void setBlue(int blue) {
         this.blue = blue;
     }
     
 }
