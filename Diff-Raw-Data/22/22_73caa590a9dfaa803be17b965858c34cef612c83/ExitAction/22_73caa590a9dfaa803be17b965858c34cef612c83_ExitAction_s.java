 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009-2010  Trejkaz, Hex Project
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.trypticon.hex.gui;
 
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.prefs.Preferences;
 import javax.swing.AbstractAction;
 import javax.swing.SwingUtilities;
 
 import org.trypticon.hex.gui.prefs.WorkspaceStateTracker;
 import org.trypticon.hex.gui.util.Callback;
 
 /**
  * Action to exit the application.
  *
  * @author trejkaz
  */
 class ExitAction extends AbstractAction {
     ExitAction() {
         putValue(NAME, "Exit");
         putValue(MNEMONIC_KEY, (int) 'x');
     }
 
     @Override
     public void actionPerformed(ActionEvent event) {
         tryToExit(new Callback<Boolean>() {
             @Override
             public void execute(Boolean okToExit) {
                 if (okToExit) {
                     System.exit(0);
                 }
             }
         });
     }
 
     /**
      * Tries to exit the application.
      *
      * @param okToExitCallback a callback which is called with {@code true} if it's OK to exit
      * and {@code false} if it's not OK.
      */
     public void tryToExit(final Callback<Boolean> okToExitCallback) {
         WorkspaceStateTracker.create().save();
 
         final List<HexFrame> frames = HexFrame.findAllFrames();
         if (frames.isEmpty()) {
             // No frames, can exit immediately.
             okToExitCallback.execute(true);
             return;
         }
 
         prepareForExit(frames, new Callback<Boolean>() {
             @Override
             public void execute(Boolean okToExit) {
                 if (okToExit) {
                     for (Frame frame : frames) {
                         frame.dispose();
                     }
 
                     // Depending on the platform, the dialogs may have been modeless, so the user might have opened
                     // new frames while we were prompting them to close the existing ones.
                     SwingUtilities.invokeLater(new Runnable() {
                         @Override
                         public void run() {
                            tryToExit(okToExitCallback);
                         }
                     });
                 } else {
                     okToExitCallback.execute(false);
                 }
             }
         });
     }
 
     /**
      * Prepares for exiting the application. Recursively calls itself for each frame.
      *
      * @param frames the list of frames.
      * @param okToExitCallback a callback which is called with {@code true} if all frames said it's okay to close
      * or {@code false} if one of them said it wasn't.
      */
     private void prepareForExit(List<HexFrame> frames, final Callback<Boolean> okToExitCallback) {
         if (frames.isEmpty()) {
             // Every frame said it was OK to close.
             okToExitCallback.execute(true);
             return;
         }
 
         HexFrame firstFrame = frames.get(0);
         final List<HexFrame> remainingFrames = frames.subList(1, frames.size());
 
         firstFrame.prepareForClose(new Callback<Boolean>() {
             @Override
             public void execute(Boolean okToClose) {
                 if (okToClose) {
                     // Reducing the risk of a StackOverflowError if there are a large number of frames open.
                     SwingUtilities.invokeLater(new Runnable() {
                         @Override
                         public void run() {
                             prepareForExit(remainingFrames, okToExitCallback);
                         }
                     });
                 } else {
                     okToExitCallback.execute(false);
                 }
             }
         });
     }
 }
