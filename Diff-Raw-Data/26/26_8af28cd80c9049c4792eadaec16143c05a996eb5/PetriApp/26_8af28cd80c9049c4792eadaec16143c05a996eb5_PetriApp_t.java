 // @formatter:off
 // CHECKSTYLE:OFF
 /******************************************************************************* 
  * Copyright (c) 2011 Tim Steinbach
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated 
  * documentation files (the "Software"), to deal in the 
  * Software without restriction, including without limitation 
  * the rights to use, copy, modify, merge, publish, distribute, 
  * sublicense, and/or sell copies of the Software, and to permit 
  * persons to whom the Software is furnished to do so, subject 
  * to the following conditions:
  * 
  * The above copyright notice and this permission notice shall 
  * be included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY 
  * OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
  * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS 
  * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO 
  * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
  * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN 
  * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
  * OR OTHER DEALINGS IN THE SOFTWARE.
  * 
  ******************************************************************************/
 // @formatter:on
 // CHECKSTYLE:ON
 
 package com.nequissimus.university.k1584;
 
 import java.awt.EventQueue;
 
 import javax.swing.JOptionPane;
 
 import com.nequissimus.library.data.Singleton;
 import com.nequissimus.library.os.MacMenuBar;
 import com.nequissimus.library.os.MacWindow;
 import com.nequissimus.library.os.OSystem;
 import com.nequissimus.university.k1584.logic.PetriConfig;
 
 /**
  * This is the application launcher which causes the editor to be created and
  * started.
  * @author Tim Steinbach
  */
 public final class PetriApp {
 
     // TODO: Allow changing size of token visualization
     // TODO: Load/Save markings in PNML (need to customize EPNML 1.1 standard;
     // maybe introduce <markings> under <net>)
 
     /**
      * <net>... <markings> <marking id="abc123" name="Basic"> <place id="abc"
      * tokens="2" /> <place id="bcd" tokens="1" /> </marking> <marking
      * id="abc234" name="Advanced"> ... </marking> </markings> </net>
      */
 
     /**
      * Hide constructor.
      */
     private PetriApp() {
     }
 
     /**
      * Point of entry for executing the application.
      * @param args Command line arguments
      */
     public static void main(final String[] args) {
 
         final PetriConfig config = new PetriConfig();
         Singleton.addObject(config);
 
         final OSystem os = OSystem.getCurrentOs();
 
         if (os == OSystem.MACOSX) {
 
             // This is needed for Mac OS X only - It changes the application
             // name in the menu bar from the fully-qualified class name to the
             // given value

             MacMenuBar.setApplicationName(config.getApplicationName());
 
             // Disallow the resize box to interfere with the actual interface.
             MacWindow.allowGrowboxIntrusion(false);
 
         }
 
         try {
 
             EventQueue.invokeLater(PetriController.getInstance());
 
         } catch (final Error e) {
 
             // At least show the user why the application is not starting
 
             JOptionPane.showMessageDialog(null, e.getCause().getMessage(),
                 null, JOptionPane.ERROR_MESSAGE);
 
             System.exit(0);
 
         }
 
     }
 
 }
