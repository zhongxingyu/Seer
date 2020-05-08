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
 
 package org.trypticon.hex.gui.formats;
 
 import org.trypticon.hex.HexViewer;
 import org.trypticon.hex.anno.Annotation;
 import org.trypticon.hex.anno.AnnotationCollection;
 import org.trypticon.hex.binary.Binary;
 import org.trypticon.hex.formats.Structure;
 import org.trypticon.hex.gui.HexFrame;
 import org.trypticon.hex.util.swingsupport.ActionException;
 import org.trypticon.hex.util.swingsupport.BaseAction;
 
import javax.swing.JOptionPane;
 import java.awt.event.ActionEvent;
 
 /**
  * Action which drops annotations for a structure at the selected position.
  *
  * @author trejkaz
  */
 public class DropStructureAction extends BaseAction {
 
     private final Structure structure;
 
     public DropStructureAction(Structure structure) {
         this.structure = structure;
 
         putValue(NAME, "Drop " + structure.getName());
     }
 
     @Override
     protected void doAction(ActionEvent event) throws Exception {
         HexFrame frame = HexFrame.findActiveFrame();
         if (frame == null || frame.getNotebookPane() == null) {
             throw new ActionException("To add a structure, focus must be on the hex viewer.");
         }
 
         HexViewer viewer = frame.getNotebookPane().getViewer();
 
         Binary binary = viewer.getBinary();
         AnnotationCollection annotations = viewer.getAnnotations();
         long position = viewer.getSelectionModel().getSelectionStart();
 
        try {
            Annotation annotation = structure.drop(binary, position);
            annotations.add(annotation);
        } catch (Exception e) {
            String message = "An error occurred trying to drop the structure onto the binary.  The most likely cause is that it isn't the structure you're looking for.";
            JOptionPane.showMessageDialog(frame, message, (String) getValue(NAME), JOptionPane.ERROR_MESSAGE);
        }
     }
 }
