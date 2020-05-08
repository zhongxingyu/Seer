 package org.megatome;
 
 /*
  * Copyright 2007 Megatome Technologies 
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at 
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *      
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 import java.lang.reflect.InvocationTargetException;
 
 import javax.swing.*;
 
 import org.megatome.swing.MainGUI;
 
 public final class Main {
 
     public static void main(String[] args) {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
             System.err.println("Error setting system look and feel"); //$NON-NLS-1$
             e.printStackTrace();
         }
         System.setProperty("apple.laf.useScreenMenuBar", "true"); //$NON-NLS-1$ //$NON-NLS-2$
         try {
             SwingUtilities.invokeAndWait(new Runnable() {
				//@Override
 				public void run() {
 			        new MainGUI().setVisible(true);
 				}
             });
         } catch (InvocationTargetException ex) {
             ex.printStackTrace();
         } catch (InterruptedException ex) {
             ex.printStackTrace();
         }
     }
 }
