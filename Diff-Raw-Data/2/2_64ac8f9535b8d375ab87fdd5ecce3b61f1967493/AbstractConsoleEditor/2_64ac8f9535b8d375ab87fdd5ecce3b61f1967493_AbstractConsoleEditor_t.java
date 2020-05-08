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
 
 package org.vial.editor;
 
 
 import jline.Terminal;
 import jline.TerminalFactory;
 import jline.WindowsTerminal;
 import jline.console.KeyMap;
 import jline.console.Operation;
 import jline.internal.Configuration;
 import jline.internal.InputStreamReader;
 import jline.internal.NonBlockingInputStream;
 import org.fusesource.jansi.Ansi;
 import org.vial.collection.RollingStack;
 import org.vial.command.Command;
 import org.vial.command.CommandFactory;
 import org.vial.command.undo.UndoContext;
 import org.vial.command.undo.UndoContextAware;
 import org.vial.command.undo.UndoableCommand;
 import org.vial.editor.internal.StringEditor;
 import org.vial.terminal.VialTerminalFactory;
 import org.vial.theme.DefaultTheme;
 import org.vial.theme.Theme;
 import org.vial.utils.Closeables;
 import org.vial.utils.VialConsole;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.nio.charset.Charset;
 import java.util.LinkedList;
 import java.util.Stack;
 
 import static org.fusesource.jansi.Ansi.Erase;
 import static org.fusesource.jansi.Ansi.ansi;
 
 /**
  * An {@link org.vial.editor.Editor} which delegates to an other {@link org.vial.editor.Editor} implementation and displays the outcome to the console.
  */
 public abstract class AbstractConsoleEditor implements ConsoleEditor, CommandFactory {
 
     public static final String EDITOR_NAME = "Vial";
     public static final String DIRTY_SIGN = "*";
     public static final int ESCAPE = 27;
     public static final int DEFAULT_ESCAPE_TIMEOUT = 100;
     public static final int READ_EXPIRED = -2;
 
     private final UndoContext undoContext = new UndoContext();
     private final RollingStack<Coordinates> cursorPositions = new RollingStack<Coordinates>();
 
     //The line inside the scrolling frame.
     //Minimum value = 1 and maximum value = terminal height - getHeaderSize() - getFooterSize().
     private int frameLine = 1;
     private int frameColumn = 1;
     private String encoding;
     private final Terminal terminal;
     private KeyMap keys;
 
     private boolean running = false;
 
     private NonBlockingInputStream in;
     private long escapeTimeout;
     private Reader reader;
 
     private File file;
 
     private int headerSize = 1;
     private int footerSize = 1;
 
     private String highLight;
 
     private Editor<String> delegate = new StringEditor();
     private Theme theme = new DefaultTheme();
 
     public AbstractConsoleEditor(final Terminal term) throws Exception {
         this.encoding = encoding != null ? encoding : Configuration.getEncoding();
         this.terminal = VialTerminalFactory.get(term);
     }
 
     public final void init() throws Exception {
         this.escapeTimeout = DEFAULT_ESCAPE_TIMEOUT;
         boolean nonBlockingEnabled =
                 escapeTimeout > 0L
                         && terminal.isSupported()
                         && in != null;
 
         /*
          * If we had a non-blocking thread already going, then shut it down
          * and start a new one.
          */
         if (this.in != null) {
             this.in.shutdown();
         }
 
         final InputStream wrapped = terminal.wrapInIfNeeded(System.in);
 
         this.in = new NonBlockingInputStream(wrapped, nonBlockingEnabled);
         this.reader = new InputStreamReader(this.in, Charset.defaultCharset());
         this.terminal.reset();
     }
 
     /**
      * Starts the editor.
      * This methods actually creates the {@link Reader}.
      */
     public void start() {
         VialConsole.systemInstall();
         running = true;
         try {
             init();
             show();
             while (running) {
                 EditorOperation operation = readOperation();
                 if (operation != null) {
                     Command cmd = create(operation);
                     onCommand(cmd);
                 }
             }
         } catch (Exception e) {
             //noop.
         }
     }
 
     /**
      * Stops the editor.
      * The methods clears the editor screen and also closes in/out and {@link Reader}.
      */
     public void stop() {
         hide();
         VialConsole.systemUninstall();
         running = false;
         Closeables.closeQuitely(reader);
         Closeables.closeQuitely(in);
     }
 
     /**
      * Shows the editor screen.
      */
     public void show() {
         repaintScreen();
         frameLine = 1;
         frameColumn = 1;
         delegate.move(1, 1);
         flush();
     }
 
     /**
      * Hides the editor screen and restore the {@link Terminal}.
      */
     public void hide() {
         VialConsole.out.print("\33[" + 1 + ";" + terminal.getHeight() + ";r");
         VialConsole.out.print(ansi().eraseScreen());
         VialConsole.out.print(ansi().cursor(1, 1));
         flush();
         try {
             terminal.restore();
         } catch (Exception e) {
             //noop
         }
     }
 
     /**
      * Reads a character from the user input.
      *
      * @return
      * @throws java.io.IOException
      */
     @Override
     public int read() throws IOException {
         return reader.read();
     }
 
     /**
      * Reads a boolean from the user input.
      * The mapping of the user input to a boolean value is specified by the implementation.
      *
      * @return
      */
     @Override
     public boolean readBoolean() throws IOException {
         return readBoolean("", false);
     }
 
     /**
      * Displays a message and reads a boolean from the user input.
      * The mapping of the user input to a boolean value is specified by the implementation.
      *
      * @param message
      * @param defaultValue
      * @return
      */
     @Override
     public boolean readBoolean(String message, Boolean defaultValue) throws IOException {
         saveCursorPosition();
         Ansi style = ansi();
         if (getTheme().getPromptBackground() != null) {
             style.bg(getTheme().getPromptBackground());
         }
         if (getTheme().getPromptForeground() != null) {
             style.fg(getTheme().getPromptForeground());
         }
         for (int i = 1; i <= getFooterSize(); i++) {
             VialConsole.out.print(ansi().cursor(terminal.getHeight() - getFooterSize() + i, 1));
             VialConsole.out.print(style.eraseLine(Ansi.Erase.FORWARD));
         }
         VialConsole.out.print(ansi().cursor(terminal.getHeight(), 1));
         VialConsole.out.print(style.a(message).bold().eraseLine(Ansi.Erase.FORWARD));
         restoreCursorPosition();
         flush();
         try {
             EditorOperation operation;
             while (true) {
                 operation = readOperation();
                 switch (operation.getType()) {
                     case NEWLINE:
                         return defaultValue;
                     case TYPE:
                         if ("y".equals(operation.getInput()) || "Y".equals(operation.getInput())) {
                             return true;
                         } else if ("n".equals(operation.getInput()) || "N".equals(operation.getInput())) {
                             return false;
                         }
                 }
             }
         } finally {
             refreshFooter();
         }
     }
 
     /**
      * Reads a line from the user input.
      *
      * @return
      * @throws java.io.IOException
      */
     @Override
     public String readLine() throws IOException {
         StringBuilder lineBuilder = new StringBuilder();
         EditorOperation operation;
         while (true) {
             operation = readOperation();
             switch (operation.getType()) {
                 case BACKSAPCE:
                 case DELETE:
                     if (lineBuilder.length() > 0) {
                         lineBuilder.delete(lineBuilder.length() - 1, lineBuilder.length());
                         VialConsole.out.print(Ansi.ansi().cursorLeft(1));
                         VialConsole.out.print(" ");
                         VialConsole.out.print(Ansi.ansi().cursorLeft(1));
                     }
                     break;
                 case NEWLINE:
                     return lineBuilder.toString();
                 case TYPE:
                     VialConsole.out.print(operation.getInput());
                     lineBuilder.append(operation.getInput());
                     break;
             }
             flush();
         }
     }
 
     public String readLine(String message) throws IOException {
         String result = null;
         saveCursorPosition();
         Ansi style = ansi();
         if (getTheme().getPromptBackground() != null) {
             style.bg(getTheme().getPromptBackground());
         }
         if (getTheme().getPromptForeground() != null) {
             style.fg(getTheme().getPromptForeground());
         }
         for (int i = 1; i <= getFooterSize(); i++) {
             VialConsole.out.print(ansi().cursor(terminal.getHeight() - getFooterSize() + i, 1));
             VialConsole.out.print(style.eraseLine(Ansi.Erase.FORWARD));
         }
         VialConsole.out.print(ansi().cursor(terminal.getHeight(), 1));
         VialConsole.out.print(ansi().cursor(terminal.getHeight(), 1));
         VialConsole.out.print(style.a(message).bold().eraseLine(Ansi.Erase.FORWARD));
         flush();
         try {
             result = readLine();
         } finally {
             VialConsole.out.print(ansi().reset());
             restoreCursorPosition();
             refreshFooter();
         }
         return result;
     }
 
 
     private EditorOperation readOperation() throws IOException {
         StringBuilder sb = new StringBuilder();
         Stack<Character> pushBackChar = new Stack<Character>();
         while (true) {
             int c = pushBackChar.isEmpty() ? reader.read() : pushBackChar.pop();
             if (c == -1) {
                 return null;
             }
             sb.append((char) c);
 
             Object o = keys.getBound(sb);
             if (o == Operation.DO_LOWERCASE_VERSION) {
                 sb.setLength(sb.length() - 1);
                 sb.append(Character.toLowerCase((char) c));
                 o = keys.getBound(sb);
             }
 
             if (o instanceof KeyMap) {
                 if (c == ESCAPE
                         && pushBackChar.isEmpty()
                         && in.isNonBlockingEnabled()
                         && in.peek(escapeTimeout) == READ_EXPIRED) {
                     o = ((KeyMap) o).getAnotherKey();
                     if (o == null || o instanceof KeyMap) {
                         continue;
                     }
                     sb.setLength(0);
                 } else {
                     continue;
                 }
             }
 
             while (o == null && sb.length() > 0) {
                 c = sb.charAt(sb.length() - 1);
                 sb.setLength(sb.length() - 1);
                 Object o2 = keys.getBound(sb);
                 if (o2 instanceof KeyMap) {
                     o = ((KeyMap) o2).getAnotherKey();
                     if (o == null) {
                         continue;
                     } else {
                         pushBackChar.push((char) c);
                     }
                 }
             }
 
             if (o instanceof EditorOperationType) {
                 EditorOperationType op = (EditorOperationType) o;
                 return new EditorOperation(op, sb.toString());
             }
             return null;
         }
     }
 
     public void onCommand(Command command) {
         try {
             if (UndoContextAware.class.isAssignableFrom(command.getClass())) {
                 ((UndoContextAware) command).setUndoContext(undoContext);
             } else if (UndoableCommand.class.isAssignableFrom(command.getClass())) {
                 undoContext.undoPush((UndoableCommand) command);
             }
             command.execute();
             flush();
         } catch (Exception ex) {
             //noop.
         }
     }
 
 
     /**
      * Repaints the whole screen.
      */
     void repaintScreen() {
         int repaintLine = 1;
         VialConsole.out.print(ansi().eraseScreen(Erase.ALL));
         VialConsole.out.print(ansi().cursor(1, 1));
         VialConsole.out.print("\33[" + (getHeaderSize() + 1) + ";" + (terminal.getHeight() - getFooterSize()) + ";r");
         refreshHeader();
         refreshFooter();
         LinkedList<String> linesToDisplay = new LinkedList<String>();
         int l = 1;
         while (linesToDisplay.size() < terminal.getHeight() - getFooterSize()) {
             String currentLine = getContent(l++);
             linesToDisplay.addAll(toDisplayLines(currentLine));
         }
 
         for (int i = 0; i < terminal.getHeight() - getHeaderSize() - getFooterSize(); i++) {
             VialConsole.out.print(ansi().cursor(repaintLine + getHeaderSize(), 1));
             displayText(linesToDisplay.get(i));
             repaintLine++;
         }
         VialConsole.out.print(ansi().cursor(2, 1));
     }
 
     /**
      * Redraws the rest of the multi line.
      */
     void redrawRestOfLine() {
         //The number of lines to reach the end of the frame.
         int maxLinesToRepaint = terminal.getHeight() - getFooterSize() - frameLine;
         LinkedList<String> toRepaintLines = new LinkedList<String>();
         String currentLine = getContent(getLine());
         toRepaintLines.addAll(toDisplayLines(currentLine));
         //Remove already shown lines
         int remainingLines = (getColumn() - 1) / terminal.getWidth();
         for (int r = 0; r < remainingLines; r++) {
             toRepaintLines.removeFirst();
         }
 
         saveCursorPosition();
         for (int l = 0; l < Math.min(maxLinesToRepaint, toRepaintLines.size()); l++) {
             VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize() + l, 1));
             VialConsole.out.print(ansi().eraseLine(Erase.FORWARD));
             displayText(toRepaintLines.get(l));
         }
         restoreCursorPosition();
     }
 
     /**
      * Redraws content from the current line to the end of the frame.
      */
     void redrawRestOfScreen() {
         int linesToRepaint = terminal.getHeight() - getFooterSize() - frameLine;
         LinkedList<String> toRepaintLines = new LinkedList<String>();
         String currentLine = getContent(getLine());
         toRepaintLines.addAll(toDisplayLines(currentLine));
         //Remove already shown lines
         int remainingLines = Math.max(0, getColumn() - 1) / terminal.getWidth();
         for (int r = 0; r < remainingLines; r++) {
             toRepaintLines.removeFirst();
         }
 
         boolean eof = false;
         for (int l = 1; toRepaintLines.size() < linesToRepaint && !eof; l++) {
             try {
                 toRepaintLines.addAll(toDisplayLines(getContent(getLine() + l)));
             } catch (Exception e) {
                 eof = true;
             }
         }
 
         saveCursorPosition();
         for (int l = 0; l < linesToRepaint; l++) {
             VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize() + l, 1));
             VialConsole.out.print(ansi().eraseLine(Erase.FORWARD));
             if (toRepaintLines.size() > l) {
                 displayText(toRepaintLines.get(l));
             } else {
                 displayText("");
             }
         }
         restoreCursorPosition();
     }
 
     public void refreshText() {
         int startLine = getLine();
         int startColum = getColumn();
         //Go to the start of the screen (1,1)
         while (frameLine > 1) {
             moveUp(1);
         }
         while (frameColumn > 1) {
             moveLeft(1);
         }
         redrawRestOfScreen();
         move(startLine, startColum);
     }
 
     @Override
     public int getLine() {
         return delegate.getLine();
     }
 
     @Override
     public int getColumn() {
         return delegate.getColumn();
     }
 
     @Override
     public void move(int line, int column) {
         if (line <= 0) {
             throw new IndexOutOfBoundsException("Minimum valid line is 1.");
         }
 
         int verticalOffset = line - getLine();
         moveVertical(verticalOffset);
         int horizontalOffset = column - getColumn();
         moveHorizontally(horizontalOffset);
         refreshHeader();
         refreshFooter();
     }
 
     /**
      * Moves the cursor vertically and scroll if needed.
      *
      * @param offset
      */
     private void moveVertical(int offset) {
         if (offset < 0) {
             moveUp(Math.abs(offset));
         } else if (offset > 0) {
             moveDown(offset);
         }
     }
 
     public void moveUp(int offset) {
         LinkedList<String> toDisplayLines = new LinkedList<String>();
         for (int i = 0; i < offset; i++) {
             toDisplayLines.clear();
             String currentLine = getContent(getLine());
             toDisplayLines.addAll(toDisplayLines(currentLine));
             //Remove already shown lines
             int remainingLines = toDisplayLines.size() - getColumn() / terminal.getWidth();
             for (int r = 0; r < remainingLines; r++) {
                 toDisplayLines.removeLast();
             }
             delegate.move(getLine() - 1, getColumn());
             currentLine = getContent(getLine());
             toDisplayLines.addAll(toDisplayLines(currentLine));
             for (int l = toDisplayLines.size() - 1; l >= 0; l--) {
                 frameLine--;
                 if (frameLine <= 0) {
                     frameLine = 1;
                     scrollDown(1);
                     VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), 1));
                     displayText(toDisplayLines.get(l));
                     VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), getColumn()));
                 }
 
                 int actualColumn = getColumn();
                 while (actualColumn > terminal.getWidth()) {
                     actualColumn -= terminal.getWidth();
                 }
                 frameColumn = actualColumn;
                 VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), frameColumn));
             }
         }
     }
 
     public void moveDown(int offset) {
         LinkedList<String> toDisplayLines = new LinkedList<String>();
         for (int i = 0; i < offset; i++) {
             toDisplayLines.clear();
             String currentLine = getContent(getLine());
             toDisplayLines.addAll(toDisplayLines(currentLine));
             //Remove already shown lines
             int remainingLines = getColumn() / terminal.getWidth() + 1;
             for (int r = 0; r < remainingLines; r++) {
                 toDisplayLines.removeFirst();
             }
             delegate.move(getLine() + 1, getColumn());
             currentLine = getContent(getLine());
             toDisplayLines.add(toDisplayLines(currentLine).getFirst());
             for (int l = 0; l < toDisplayLines.size(); l++) {
                 frameLine++;
                 if (frameLine >= terminal.getHeight() - getFooterSize()) {
                     frameLine = terminal.getHeight() - getHeaderSize() - getFooterSize();
                     scrollUp(1);
                     VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), 1));
                     displayText(toDisplayLines.get(l));
                     VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), getColumn()));
                 }
 
                 int actualColumn = getColumn();
                 while (actualColumn > terminal.getWidth()) {
                     actualColumn -= terminal.getWidth();
                 }
                 frameColumn = actualColumn;
                 VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), frameColumn));
             }
         }
     }
 
     /**
      * Moves the cursor horizontally.
      *
      * @param offset
      */
     private void moveHorizontally(int offset) {
         if (offset < 0) {
             moveLeft(Math.abs(offset));
         } else if (offset > 0) {
             moveRight(offset);
         }
     }
 
     public void moveLeft(int offset) {
         for (int i = 0; i < offset; i++) {
             frameColumn--;
             //If cursor is at the left edge of the screen.
             if (frameColumn <= 0) {
                 String currentLine = getContent(getLine());
                 int currentLineLength = currentLine.length();
 
                 //If we are not at the first line of line projected in multiple lines:
                 if (currentLineLength > terminal.getWidth() && getColumn() > 1) {
                     if (frameLine != 1) {
                         frameLine--;
                         frameColumn = terminal.getWidth();
                         delegate.move(getLine(), getColumn() - 1);
                     } else {
                         frameLine = 1;
                         frameColumn = 1;
                         delegate.move(getLine(), getColumn() - 1);
                     }
                 } else {
                     //Setting minimum value just in case move vertical exceeds bounds.
                     frameColumn = 1;
                     String previousLine = getContent(getLine() - 1);
                     moveUp(1);
                     //Moving up will place the cursor possibly at the start of the line.
                     frameColumn = previousLine.length() + 1;
                     while (frameColumn > terminal.getWidth()) {
                         frameColumn -= terminal.getWidth();
                         frameLine++;
                     }
                     delegate.move(getLine(), previousLine.length() + 1);
                 }
             } else {
                 delegate.move(getLine(), getColumn() - 1);
             }
         }
         VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), frameColumn));
     }
 
     public void moveRight(int offset) {
         for (int i = 0; i < offset; i++) {
             int actualContentLength = getContent(getLine()).length();
             frameColumn++;
             //Check if we need to move to the next line of the file.
             if (frameColumn > actualContentLength + 1 || getColumn() > actualContentLength) {
                 frameColumn = 1;
                 moveDown(1);
                 home();
                 //Check if the current line is displayed using more lines and we need to move to the next one.
             } else if (frameColumn > terminal.getWidth()) {
                 String currentLine = getContent(getLine());
                 frameColumn = 1;
                 LinkedList<String> toDisplayLines = toDisplayLines(currentLine);
                 int remainingLines = toDisplayLines.size() - getColumn() / terminal.getWidth();
                 for (int r = 0; r < remainingLines; r++) {
                     toDisplayLines.removeFirst();
                 }
                 frameLine++;
                 if (frameLine >= terminal.getHeight() - getFooterSize()) {
                     frameLine = terminal.getHeight() - getHeaderSize() - getFooterSize();
                     scrollUp(1);
                     VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), 1));
                     displayText(toDisplayLines.get(0));
                     VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), getColumn()));
                 }
                 delegate.move(getLine(), getColumn() + 1);
             } else {
                 //Just move to the next character.
                 delegate.move(getLine(), getColumn() + 1);
             }
         }
         VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), frameColumn));
     }
 
     /**
      * Moves cursor to the end of the current line.
      */
     public void end() {
         String currentLine = getContent(getLine());
         LinkedList<String> toDisplayLines = toDisplayLines(currentLine);
         int remainingLines = getColumn() / terminal.getWidth() + 1;
         for (int r = 0; r < remainingLines; r++) {
             toDisplayLines.removeFirst();
         }
         frameColumn = currentLine.length();
         delegate.end();
         for (int l = 0; l < toDisplayLines.size(); l++) {
             frameLine++;
             frameColumn -= terminal.getWidth();
             if (frameLine >= terminal.getHeight() - getFooterSize()) {
                 frameLine = terminal.getHeight() - getHeaderSize() - getFooterSize();
                 scrollUp(1);
                 VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), 1));
                 displayText(toDisplayLines.get(l));
                 VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), getColumn()));
             }
         }
         VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), frameColumn));
         refreshHeader();
         refreshFooter();
     }
 
     /**
      * Moves cursor to the end of the current line.
      */
     public void home() {
         String currentLine = getContent(getLine());
         LinkedList<String> toDisplayLines = toDisplayLines(currentLine);
         int remainingLines = toDisplayLines.size() - getColumn() / terminal.getWidth();
         for (int r = 0; r < remainingLines; r++) {
             toDisplayLines.removeLast();
         }
         frameColumn = 1;
         delegate.home();
         for (int l = toDisplayLines.size() - 1; l >= 0; l--) {
             frameLine--;
             if (frameLine <= 0) {
                 frameLine = 1;
                 scrollDown(1);
                 VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), 1));
                 displayText(toDisplayLines.get(l));
                 VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), getColumn()));
             }
         }
         VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), frameColumn));
         refreshHeader();
         refreshFooter();
     }
 
 
     @Override
     public void put(String str) {
         if (str.contains(NEW_LINE)) {
             String[] lines = str.split(NEW_LINE);
             for (int i = 0; i < lines.length; i++) {
                 if (i != 0) {
                     newLine();
                 }
                 put(lines[i]);
             }
             if (str.endsWith(NEW_LINE)) {
                 newLine();
             }
 
         } else {
             int startingFromColumn = getColumn();
             delegate.put(str);
             String modifiedLine = getContent(getLine());
             LinkedList<String> toDisplayLines = toDisplayLines(modifiedLine);
 
             //We need to check if we exceed the boundaries of the line.
             frameColumn += str.length();
             if (frameColumn > terminal.getWidth()) {
                 int current = (startingFromColumn - 1) / terminal.getWidth();
                 VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), 1));
                 VialConsole.out.print(ansi().eraseLine(Erase.FORWARD));
                 displayText(toDisplayLines.get(current));
                 frameLine += frameColumn / terminal.getWidth();
                 frameColumn -= str.length();
                 while (frameLine > terminal.getHeight() - getHeaderSize() - getFooterSize()) {
                     frameLine = terminal.getHeight() - getHeaderSize() - getFooterSize();
                     scrollUp(1);
                 }
             }
 
             if (toDisplayLines.size() > 1) {
                 redrawRestOfScreen();
             } else {
                 redrawRestOfLine();
             }
 
             frameColumn = getColumn();
             while (frameColumn > terminal.getWidth()) {
                 frameColumn -= terminal.getWidth();
             }
 
             VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), frameColumn));
             refreshHeader();
             refreshFooter();
         }
     }
 
     @Override
     public String delete() {
         VialConsole.out.print(ansi().eraseLine(Erase.FORWARD));
         String r = delegate.delete();
         if (r.equals(NEW_LINE) || r.equals(CARRIEGE_RETURN)) {
             redrawRestOfScreen();
         } else {
             redrawRestOfLine();
         }
         refreshHeader();
         refreshFooter();
         return r;
     }
 
     @Override
     public String backspace() {
         String b = null;
         //Check if we need to merge with previous line.
         if (getColumn() == 1) {
             moveUp(1);
             end();
             //end() will just move the cursor to the last char. We want to move it one more char to the right.
             delegate.move(getLine(), getColumn() + 1);
             mergeLine();
             redrawRestOfScreen();
             return "\n";
             //Check if cursor is at the edge of a line display in multiple terminal lines.
         } else if (frameColumn == 1) {
             frameLine--;
             frameColumn = terminal.getWidth();
             b = delegate.backspace();
             String currentLine = getContent(getLine());
             LinkedList<String> toDisplayLines = toDisplayLines(currentLine);
             int multiLineNumber = getColumn() / terminal.getWidth();
             if (frameLine == 0) {
                 frameLine = 1;
                 scrollDown(1);
             }
             //Redraw previous line
             VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), 1));
             VialConsole.out.print(ansi().eraseLine(Erase.FORWARD));
             displayText(toDisplayLines.get(multiLineNumber - 1));
             //Redraw current line
             VialConsole.out.print(ansi().cursor(frameLine + 2, 1));
             VialConsole.out.print(ansi().eraseLine(Erase.FORWARD));
             displayText(toDisplayLines.get(multiLineNumber));
             VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), frameColumn));
 
             redrawRestOfScreen();
         } else {
             b = delegate.backspace();
             String currentLine = getContent(getLine());
             frameColumn--;
             //If we have a a simple line.
             if (currentLine.length() < terminal.getWidth()) {
                 VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), getColumn()));
                 VialConsole.out.print(ansi().eraseLine(Erase.FORWARD));
                 String modifiedLine = getContent(getLine());
                 displayText(modifiedLine.substring(getColumn() - 1));
                 //Line is multi line and we will need to swift chars.
             } else {
                 redrawRestOfScreen();
             }
             VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), frameColumn));
         }
         refreshHeader();
         refreshFooter();
         return b;
     }
 
     @Override
     public void newLine() {
         delegate.newLine();
         VialConsole.out.print(ansi().eraseLine(Erase.FORWARD));
         frameColumn = 1;
         frameLine++;
         if (frameLine > terminal.getHeight() - getHeaderSize() - getFooterSize()) {
             frameLine = terminal.getHeight() - getHeaderSize() - getFooterSize();
             scrollUp(1);
 
         }
         redrawRestOfScreen();
         VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), frameColumn));
         refreshHeader();
         refreshFooter();
     }
 
     @Override
     public void mergeLine() {
         String currentLine = getContent(getLine());
         frameColumn = currentLine.length() % terminal.getWidth() + 1;
         if (frameColumn == terminal.getWidth()) {
             frameColumn = 1;
             frameLine++;
         }
         delegate.mergeLine();
         redrawRestOfScreen();
         VialConsole.out.print(ansi().cursor(frameLine + getHeaderSize(), frameColumn));
         refreshHeader();
         refreshFooter();
     }
 
     /**
      * Finds the next appearance of the String.
      *
      * @param str
      */
     @Override
     public void findNext(String str) {
         int startLine = getLine();
         int startColumn = getColumn();
         highLight(str);
         delegate.findNext(str);
         int targetLine = getLine();
         int targetColumn = getColumn();
         //Rest the actual pointer
         delegate.move(startLine, startColumn);
         int verticalOffset = targetLine - getLine();
         moveVertical(verticalOffset);
         int horizontalOffset = targetColumn - getColumn();
         moveHorizontally(horizontalOffset);
         refreshText();
         refreshHeader();
         refreshFooter();
 
     }
 
     /**
      * Finds the next appearance of the String.
      *
      * @param str
      */
     @Override
     public void findPrevious(String str) {
         int startLine = getLine();
         int startColumn = getColumn();
         highLight(str);
         delegate.findPrevious(str);
         int targetLine = getLine();
         int targetColumn = getColumn();
         //Rest the actual pointer
         delegate.move(startLine, startColumn);
         int verticalOffset = targetLine - getLine();
         moveVertical(verticalOffset);
         int horizontalOffset = targetColumn - getColumn();
         moveHorizontally(horizontalOffset);
         refreshText();
         refreshHeader();
         refreshFooter();
     }
 
     protected void scrollUp(int rows) {
         //Windows Terminals don't support scrolling.
         if (WindowsTerminal.class.isAssignableFrom(terminal.getClass())) {
             refreshText();
         } else {
             VialConsole.out.print(ansi().scrollUp(rows));
         }
     }
 
     protected void scrollDown(int rows) {
         //Windows Terminals don't support scrolling.
         if (WindowsTerminal.class.isAssignableFrom(terminal.getClass())) {
             refreshText();
         } else {
             VialConsole.out.print(ansi().scrollDown(rows));
         }
     }
 
     /**
      * Displays Text highlighting the text that was marked as highlighted.
      *
      * @param text
      */
     protected void displayText(String text) {
         if (highLight != null && !highLight.isEmpty() && text.contains(highLight)) {
            String highLightedText = text.replaceAll(highLight, ansi().bold().bg(theme.getHighLightBackground()).fg(theme.getHighLightForeground()).a(highLight).boldOff().reset().toString());
             VialConsole.out.print(highLightedText);
         } else {
             VialConsole.out.print(text);
         }
     }
 
     /**
      * Clears the current line.
      * The purpose of this method is to cover cases where erase line doesn't respect background color (e.g some Windows).
      */
     protected void clearLine() {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < terminal.getWidth(); i++) {
             sb.append(" ");
         }
         VialConsole.out.print(sb.toString());
     }
 
     @Override
     public void saveCursorPosition() {
         cursorPositions.push(new Coordinates(frameLine, frameColumn));
     }
 
     @Override
     public void restoreCursorPosition() {
         Coordinates coordinates = cursorPositions.pop();
         if (coordinates != null) {
             VialConsole.out.print(ansi().cursor(coordinates.getLine() + getHeaderSize(), coordinates.getColumn()));
         }
     }
 
     public void flush() {
         VialConsole.out.flush();
     }
 
     protected void highLight(String text) {
         this.highLight = text;
     }
 
     @Override
     public void open(File source) throws IOException {
         this.file = source;
         delegate.open(source);
     }
 
     @Override
     public void save(File target) throws IOException {
         if (target != null) {
             this.file = target;
             delegate.save(target);
         } else {
             delegate.save(this.file);
         }
         setDirty(false);
         refreshHeader();
         refreshFooter();
     }
 
 
     @Override
     public void close() throws IOException {
         try {
             delegate.close();
             file = null;
         } catch (Exception e) {
             throw new IOException(e);
         }
     }
 
     /**
      * Returns the number of lines.
      *
      * @return
      */
     @Override
     public int lines() {
         return delegate.lines();
     }
 
     @Override
     public String getContent() {
         return delegate.getContent();
     }
 
     @Override
     public String getContent(int line) {
         return delegate.getContent(line);
     }
 
     public File getFile() {
         return file;
     }
 
     /**
      * Creates a list of lines that represent how the line will be displayed on screen.
      *
      * @param line
      * @return
      */
     private LinkedList<String> toDisplayLines(String line) {
         LinkedList<String> displayLines = new LinkedList<String>();
         if (line.length() <= terminal.getWidth()) {
             displayLines.add(line);
         } else {
             int total = Math.max(0, line.length() - 1) / terminal.getWidth() + 1;
             int startIndex = 0;
             for (int l = 0; l < total; l++) {
                 displayLines.add(line.substring(startIndex, Math.min(startIndex + terminal.getWidth(), line.length())));
                 startIndex += terminal.getWidth();
             }
         }
         return displayLines;
     }
 
 
     public Terminal getTerminal() {
         return terminal;
     }
 
     public Editor<String> getDelegate() {
         return delegate;
     }
 
     @Override
     public Boolean isDirty() {
         return file == null || delegate.isDirty();
     }
 
     @Override
     public void setDirty(Boolean dirty) {
         delegate.setDirty(dirty);
         refreshHeader();
     }
 
     public int getHeaderSize() {
         return headerSize;
     }
 
     public void setHeaderSize(int headerSize) {
         this.headerSize = headerSize;
     }
 
     public int getFooterSize() {
         return footerSize;
     }
 
     public void setFooterSize(int footerSize) {
         this.footerSize = footerSize;
     }
 
     public KeyMap getKeys() {
         return keys;
     }
 
     public void setKeys(KeyMap keys) {
         this.keys = keys;
     }
 
     public Theme getTheme() {
         return theme;
     }
 
     public void setTheme(Theme theme) {
         this.theme = theme;
     }
 
     public UndoContext getUndoContext() {
         return undoContext;
     }
 }
