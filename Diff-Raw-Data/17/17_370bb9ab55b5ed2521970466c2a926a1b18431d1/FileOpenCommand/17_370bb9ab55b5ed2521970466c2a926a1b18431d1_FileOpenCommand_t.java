 /**
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
 
 package org.jledit.command.file;
 
 import org.jledit.command.Command;
 import org.jledit.command.undo.UndoContext;
 import org.jledit.command.undo.UndoContextAware;
 import org.jledit.ConsoleEditor;
 
 import java.io.File;
 import java.io.IOException;
 
 public class FileOpenCommand implements Command, UndoContextAware {
 
     private final ConsoleEditor editor;
     private final String fileName;
     private UndoContext undoContext;
 
     public FileOpenCommand(ConsoleEditor editor, String fileName) {
         this.editor = editor;
         this.fileName = fileName;
     }
 
     @Override
     public void execute() {
         try {
             if (editor.isDirty()) {
                 boolean doOpen = editor.readBoolean("You have unsaved changes. Do you want to open a new file without saving? [y/N]", false);
                 if (!doOpen) {
                     return;
                 }
             }
 
             if (fileName == null || fileName.isEmpty()) {
                 String f = editor.readLine("Open file:");
                 undoContext.clear();
                 editor.open(new File(f));
                editor.redrawText();
                editor.redrawHeader();
                editor.redrawFooter();
             } else {
                 undoContext.clear();
                 editor.open(new File(fileName));
             }
         } catch (IOException e) {
             //noop
         }
     }
 
     /**
      * Sets the {@link org.jledit.command.undo.UndoContext}.
      *
      * @param undoContext
      */
     @Override
     public void setUndoContext(UndoContext undoContext) {
         this.undoContext = undoContext;
     }
 }
