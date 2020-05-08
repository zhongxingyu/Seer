 /**
  * Copyright 2007 Wei-ju Wu
  *
  * This file is part of TinyUML.
  *
  * TinyUML is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * TinyUML is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TinyUML; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.tinyuml.ui.diagram.commands;
 
 import javax.swing.undo.AbstractUndoableEdit;
 import org.tinyuml.draw.CompositeElement;
 import org.tinyuml.draw.DiagramElement;
 import org.tinyuml.util.Command;
 
 /**
  * This is an undoable creation command for a Connection.
  *
  * @author Wei-ju Wu
  * @version 1.0
  */
 public class AddConnectionCommand extends AbstractUndoableEdit
 implements Command {
 
   private DiagramEditorNotification notification;
   private DiagramElement element;
   private CompositeElement parent;
 
   /**
    * Constructor.
    * @param editorNotification a DiagramEditorNotification object
    * @param parent the parent component
    * @param elem the element created
    */
   public AddConnectionCommand(DiagramEditorNotification editorNotification,
     CompositeElement parent, DiagramElement elem) {
     this.parent = parent;
     element = elem;
     this.notification = editorNotification;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void undo() {
     super.undo();
     parent.removeChild(element);
     notification.notifyElementRemoved(element);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void redo() {
     super.redo();
     run();
   }
 
   /**
    * {@inheritDoc}
    */
   public void run() {
     parent.addChild(element);
     notification.notifyElementAdded(element);
   }
 }
