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
 
 package org.jledit.command.editor;
 
 import org.jledit.ConsoleEditor;
 
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 
 public class PasteCommand extends AbstractUndoableCommand {
 
     private final String clipboardContent;
 
     public PasteCommand(ConsoleEditor editor) {
         super(editor);
         this.clipboardContent = getClipboardContent();
     }
 
     @Override
     public void execute() {
        if (!getEditor().isReadOnly()) {
             if (!clipboardContent.isEmpty()) {
                 getEditor().setDirty(true);
                 getEditor().put(clipboardContent);
             }
             super.execute();
         }
     }
 
     @Override
     public void undo() {
         if (!getEditor().isReadOnly()) {
             super.undo();
             for (int i = 0; i < clipboardContent.length(); i++) {
                 getEditor().backspace();
             }
         }
     }
 
     public final String getClipboardContent() {
         String result = "";
         Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         try {
             result = (String) clipboard.getData(DataFlavor.stringFlavor);
         } catch (Exception ex) {
             //noop
         }
         return result;
     }
 }
