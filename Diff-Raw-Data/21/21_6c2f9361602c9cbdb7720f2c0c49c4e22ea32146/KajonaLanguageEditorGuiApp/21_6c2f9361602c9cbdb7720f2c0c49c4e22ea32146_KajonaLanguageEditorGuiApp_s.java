 /*
  * Kajona Language File Editor Gui
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 2, or (at your option) any
  * later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA
  *
  * (c) MulchProductions, www.mulchprod.de, www.kajona.de
  *
  */
 
 package de.mulchprod.kajona.languageeditor.gui;
 
 import org.jdesktop.application.Application;
 import org.jdesktop.application.SingleFrameApplication;
 
 /**
  * The main class of the application.
  */
 public class KajonaLanguageEditorGuiApp extends SingleFrameApplication {
 
     /**
      * At startup create and show the main frame of the application.
      */
     @Override protected void startup() {
         show(new KajonaLanguageEditorGuiView(this));
     }
 
     /**
      * This method is to initialize the specified window by injecting resources.
      * Windows shown in our application come fully initialized from the GUI
      * builder, so this additional configuration is not needed.
      */
     @Override protected void configureWindow(java.awt.Window root) {
     }
 
     /**
      * A convenient static getter for the application instance.
      * @return the instance of KajonaLanguageEditorGuiApp
      */
     public static KajonaLanguageEditorGuiApp getApplication() {
         return Application.getInstance(KajonaLanguageEditorGuiApp.class);
     }
 
     /**
      * Main method launching the application.
      */
     public static void main(String[] args) {
         launch(KajonaLanguageEditorGuiApp.class, args);
     }
 }
