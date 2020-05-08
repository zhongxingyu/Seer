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
 
 package org.jledit.simple;
 
 import jline.Terminal;
 import jline.console.KeyMap;
 import org.fusesource.jansi.Ansi;
 import org.jledit.command.Command;
 import org.jledit.command.CommandNotFoundException;
 import org.jledit.command.editor.BackspaceCommand;
 import org.jledit.command.editor.DeleteCommand;
 import org.jledit.command.editor.FindCommand;
 import org.jledit.command.editor.FindNextCommand;
 import org.jledit.command.editor.FindPreviousCommand;
 import org.jledit.command.editor.GoToCommand;
 import org.jledit.command.editor.MoveCursorDownCommand;
 import org.jledit.command.editor.MoveCursorLeftCommand;
 import org.jledit.command.editor.MoveCursorRightCommand;
 import org.jledit.command.editor.MoveCursorToEndOfLineCommand;
 import org.jledit.command.editor.MoveCursorToStartOfLineCommand;
 import org.jledit.command.editor.MoveCursorUpCommand;
 import org.jledit.command.editor.NewLineCommand;
 import org.jledit.command.editor.PasteCommand;
 import org.jledit.command.editor.QuitCommand;
 import org.jledit.command.editor.TypeCommand;
 import org.jledit.command.file.FileCloseCommand;
 import org.jledit.command.file.FileOpenCommand;
 import org.jledit.command.file.FileSaveCommand;
 import org.jledit.command.undo.RedoCommand;
 import org.jledit.command.undo.UndoCommand;
 import org.jledit.AbstractConsoleEditor;
 import org.jledit.EditorOperation;
 import org.jledit.EditorOperationType;
 import org.jledit.utils.JlEditConsole;
 import org.jledit.utils.Strings;
 import org.jledit.utils.internal.KeyMaps;
 
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import static org.fusesource.jansi.Ansi.ansi;
 
 public class SimpleConsoleEditor extends AbstractConsoleEditor {
 
     private final Map<String, String> supportedOperations = new LinkedHashMap<String, String>();
     private final List<String> helpLines = new LinkedList<String>();
 
 
     public SimpleConsoleEditor(Terminal terminal) throws Exception {
         super(terminal);
         setKeys(createKeyMap());
         supportedOperations.put("^O", "Open");
         supportedOperations.put("^X", "Quit");
         supportedOperations.put("^S", "Save");
         supportedOperations.put("^Z", "Undo");
         supportedOperations.put("^R", "Redo");
         supportedOperations.put("^G", "Go To");
         supportedOperations.put("^F", "Find");
         supportedOperations.put("^N", "Next");
         supportedOperations.put("^P", "Previous");
         addHelpLines(helpLines);
         setFooterSize(helpLines.size() + 1);
     }
 
     public void redrawHeader() {
         saveCursorPosition();
         JlEditConsole.out.print(ansi().cursor(1, 1));
         Ansi style = ansi();
         if (getTheme().getHeaderBackground() != null) {
             style.bg(getTheme().getHeaderBackground());
         }
         if (getTheme().getHeaderForeground() != null) {
             style.fg(getTheme().getHeaderForeground());
         }
         String textCoords = "L:" + getLine() + " C:" + getColumn();
         int displayFileLength = getTerminal().getWidth() - getTitle().length() - textCoords.length() - 1;
         JlEditConsole.out.print(style.a(getTitle()).a(":").a(Strings.tryToTrimToSize(getDisplayAs(), displayFileLength)).a(isDirty() ? DIRTY_SIGN : "").eraseLine(Ansi.Erase.FORWARD));
         JlEditConsole.out.print(ansi().cursor(1, getTerminal().getWidth() - textCoords.length()));
         JlEditConsole.out.print(ansi().a(textCoords).reset());
         JlEditConsole.out.print(ansi().cursor(getTerminal().getHeight(), 1));
         restoreCursorPosition();
     }
 
     /**
      * Refreshes the footer that displays the current line and column.
      */
     public void redrawFooter() {
         saveCursorPosition();
         Ansi style = ansi();
         if (getTheme().getFooterBackground() != null) {
             style.bg(getTheme().getFooterBackground());
         }
         if (getTheme().getFooterForeground() != null) {
             style.fg(getTheme().getFooterForeground());
         }
         JlEditConsole.out.print(style);
         JlEditConsole.out.print(ansi().cursor(getTerminal().getHeight() + 1 - getFooterSize(), 1).eraseLine(Ansi.Erase.FORWARD));
         for (int i = 1; i <= helpLines.size(); i++) {
             String helpLine = helpLines.get(i - 1);
             int startColumn = (getTerminal().getWidth() - helpLine.length()) / 2;
             JlEditConsole.out.print(ansi().cursor(getTerminal().getHeight() + 1 - getFooterSize() + i, 1).eraseLine(Ansi.Erase.FORWARD));
             JlEditConsole.out.print(ansi().cursor(getTerminal().getHeight() + 1 - getFooterSize() + i, startColumn));
             JlEditConsole.out.print(helpLine);
         }
         JlEditConsole.out.print(ansi().reset());
         restoreCursorPosition();
     }
 
     /**
      * Redraws the cursor coordinates if the flavor supports that.
      */
     @Override
     public void redrawCoords() {
        saveCursorPosition();
        JlEditConsole.out.print(ansi().cursor(1, 1));
        Ansi style = ansi();
        if (getTheme().getHeaderBackground() != null) {
            style.bg(getTheme().getHeaderBackground());
        }
        if (getTheme().getHeaderForeground() != null) {
            style.fg(getTheme().getHeaderForeground());
        }

        String textCoords = "L:" + getLine() + " C:" + getColumn();
        JlEditConsole.out.print(style);
        JlEditConsole.out.print(ansi().cursor(1, getTerminal().getWidth() - textCoords.length()));
        JlEditConsole.out.print(ansi().a(textCoords).reset());
        JlEditConsole.out.print(ansi().cursor(getTerminal().getHeight(), 1).reset());
        restoreCursorPosition();
     }
 
     private void addHelpLines(List<String> helpLines) {
         helpLines.clear();
         StringBuilder sb = new StringBuilder();
         boolean startOfLine = true;
         for (Map.Entry<String, String> entry : supportedOperations.entrySet()) {
             String key = entry.getKey();
             String desc = entry.getValue();
             String txt;
             if (startOfLine) {
                 txt = key + " " + desc;
                 startOfLine = false;
             } else {
                 txt = "     " + key + " " + desc;
             }
             if (txt.length() + sb.length() > getTerminal().getWidth()) {
                 helpLines.add(sb.toString());
                 sb.delete(0, sb.length());
                 startOfLine = true;
             }
             sb.append(txt);
         }
         helpLines.add(sb.toString());
     }
 
     @Override
     public Command create(EditorOperation operation) throws CommandNotFoundException {
         switch (operation.getType()) {
             case TYPE:
                 return new TypeCommand(this, operation.getInput());
             case NEWLINE:
                 return new NewLineCommand(this);
             case BACKSAPCE:
                 return new BackspaceCommand(this);
             case DELETE:
                 return new DeleteCommand(this);
             case PASTE:
                 return new PasteCommand(this);
             //Cursor operations
             case HOME:
                 return new MoveCursorToStartOfLineCommand(this);
             case END:
                 return new MoveCursorToEndOfLineCommand(this);
             case UP:
                 return new MoveCursorUpCommand(this);
             case DOWN:
                 return new MoveCursorDownCommand(this);
             case LEFT:
                 return new MoveCursorLeftCommand(this);
             case RIGHT:
                 return new MoveCursorRightCommand(this);
             //File Operations
             case SAVE:
                 return new FileSaveCommand(this);
             case OPEN:
                 return new FileOpenCommand(this, null);
             case CLOSE:
                 return new FileCloseCommand(this);
             case QUIT:
                 return new QuitCommand(this);
             //UNDO & REDO
             case UNDO:
                 return new UndoCommand(this, getUndoContext());
             case REDO:
                 return new RedoCommand(getUndoContext());
             case FIND:
                 return new FindCommand(this);
             case FIND_NEXT:
                 return new FindNextCommand(this, null);
             case FIND_PREVIOUS:
                 return new FindPreviousCommand(this, null);
             case GOTO:
                 return new GoToCommand(this);
 
         }
         throw new CommandNotFoundException("Could not find command for Operation");
     }
 
     private KeyMap createKeyMap() {
         Object[] ctrl = new Object[]{
                 // Control keys.
                 null,                               /* Control-@ */
                 null,                               /* Control-A */
                 null,                               /* Control-B */
                 null,                               /* Control-C */
                 null,                               /* Control-D */
                 null,                               /* Control-E */
                 EditorOperationType.FIND,           /* Control-F */
                 EditorOperationType.GOTO,           /* Control-G */
                 EditorOperationType.BACKSAPCE,      /* Control-H */
                 null,                               /* Control-I */
                 EditorOperationType.NEWLINE,        /* Control-J */
                 null,                               /* Control-K */
                 null,                               /* Control-L */
                 EditorOperationType.NEWLINE,        /* Control-M */
                 EditorOperationType.FIND_NEXT,      /* Control-N */
                 EditorOperationType.OPEN,           /* Control-O */
                 EditorOperationType.FIND_PREVIOUS,  /* Control-P */
                 EditorOperationType.QUIT,           /* Control-Q */
                 EditorOperationType.REDO,           /* Control-R */
                 EditorOperationType.SAVE,           /* Control-S */
                 null,                               /* Control-T */
                 EditorOperationType.UNDO,           /* Control-U */
                 EditorOperationType.PASTE,          /* Control-V */
                 null,                               /* Control-W */
                 EditorOperationType.QUIT,           /* Control-X */
                 null,                               /* Control-Y */
                 EditorOperationType.UNDO,           /* Control-Z */
                 null,                               /* Control-[ */
                 null,                               /* Control-\ */
                 null,                               /* Control-] */
                 null,                               /* Control-^ */
                 null,                               /* Control-_ */
         };
 
         KeyMap simpleKeyMap = new KeyMap("simple", false);
         for (char c = 0; c < ctrl.length; c++) {
             simpleKeyMap.bind(Character.toString(c), ctrl[c]);
         }
         for (char c = 32; c < 256; c++) {
             simpleKeyMap.bind(Character.toString(c), EditorOperationType.TYPE);
         }
         simpleKeyMap.bind(Character.toString((char) 127), EditorOperationType.BACKSAPCE);
         KeyMaps.bindArrowKeys(simpleKeyMap);
         return simpleKeyMap;
     }
 
     @Override
     public void setDirty(Boolean dirty) {
         super.setDirty(dirty);
         redrawHeader();
     }
 
     @Override
     public void setReadOnly(boolean readOnly) {
         super.setReadOnly(readOnly);
         if (readOnly) {
             supportedOperations.remove("^S");
         } else {
             supportedOperations.put("^S", "Save");
         }
         addHelpLines(helpLines);
     }
 
     @Override
     public void setOpenEnabled(boolean openEnabled) {
         super.setOpenEnabled(openEnabled);
         if (!openEnabled) {
             supportedOperations.remove("^O");
         } else {
             supportedOperations.put("^O", "Open");
         }
         addHelpLines(helpLines);
     }
 }
