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
 
 import org.trypticon.hex.HexViewer;
 import org.trypticon.hex.anno.OverlappingAnnotationException;
 import org.trypticon.hex.anno.SimpleMutableGroupAnnotation;
 import org.trypticon.hex.util.swingsupport.ActionException;
 import org.trypticon.hex.util.swingsupport.BaseAction;
 
 import java.awt.event.ActionEvent;
 
 /**
  * Action to add a sub-region.
  *
  * @author trejkaz
  */
 public class AddSubRegionAction extends BaseAction {
     public AddSubRegionAction() {
         putValue(NAME, "Add Sub-Region");
     }
 
     @Override
     protected void doAction(ActionEvent event) throws Exception {
         HexFrame frame = HexFrame.findActiveFrame();
         if (frame == null || frame.getNotebookPane() == null) {
             throw new ActionException("To add a sub-region, focus must be on the hex viewer.");
         }
 
         HexViewer viewer = frame.getNotebookPane().getViewer();
 
         // TODO: Check that it's legal, or have the model throw an exception if it isn't.
 
         long position = viewer.getSelectionModel().getSelectionStart();
        int length = (int) (viewer.getSelectionModel().getSelectionEnd() - position) + 1;
 
         try {
             viewer.getAnnotations().add(new SimpleMutableGroupAnnotation(position, length, null));
         } catch (OverlappingAnnotationException e) {
             throw new ActionException("You cannot create annotations which overlap each other.", e);
         }
     }
 }
