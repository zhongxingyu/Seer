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
 
 package org.jledit.internal;
 
 
 import org.jledit.Editor;
 import org.jledit.utils.internal.Charsets;
 import org.jledit.utils.Files;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.LinkedList;
 
 /**
  * An {@link org.jledit.Editor} implementation for {@link String} objects.
  */
 public class StringEditor implements Editor<String> {
 
     private File file;
     private Charset charset = Charset.defaultCharset();
     private int line = 1;
     private int column = 1;
     private Boolean dirty = false;
     private final LinkedList<String> lines = new LinkedList<String>();
 
     public StringEditor() {
         lines.add("");
     }
 
     public StringEditor(String content) {
         String[] contentLines = content.split("\n|\r");
         lines.addAll(Arrays.asList(contentLines));
     }
 
     @Override
     public synchronized int getLine() {
         return line;
     }
 
     @Override
     public synchronized int getColumn() {
         return column;
     }
 
     /**
      * Moves the index to the specified line and column.
      * If the requested line is greater that the actual lines number and exception is thrown.
      * If the requested column is greater than the line width, the index is moved to the end of the line.
      *
      *
      * @param line
      * @param column
      * @throws IndexOutOfBoundsException When the requested line exceeds the number of lines in the content.
      */
     @Override
     public synchronized void move(int line, int column) {
         if (lines() < line) {
             this.column = 1;
         } else {
             String targetLine = lines.get(line - 1);
             if (targetLine.length() < column) {
                 this.column = targetLine.length() + 1;
             } else {
                 this.column = column;
             }
         }
         this.line = line;
     }
 
     /**
      * Moves the cursors to the start of the line.
      */
     @Override
     public synchronized void home() {
         move(line, 1);
     }
 
     /**
      * Moves cursor to the end of the line.
      */
     @Override
     public synchronized void end() {
         move(line, getContent(line).length());
     }
 
     @Override
     public synchronized void put(String str) {
         while (lines() < line) {
             lines.add("");
         }
         String currentLine = lines.remove(line - 1);
         String beforePut = currentLine.substring(0, column - 1);
         String afterPut = column - 1 < currentLine.length() ? currentLine.substring(column - 1) : "";
         String[] modifiedContent = (beforePut + str + afterPut).split("\n|\r");
         if (modifiedContent.length == 1) {
             lines.add(line - 1, modifiedContent[0]);
             column += str.length();
         } else {
             String lastLine = null;
             for (String l : modifiedContent) {
                 lines.add(line++ - 1, l);
                 lastLine = l;
             }
             column = lastLine.length() - afterPut.length();
         }
     }
 
     @Override
     public synchronized String delete() {
         if (lines() < line) {
             this.column = 1;
             return "\n";
         }
         String currentLine = lines.remove(line - 1);
         if (column - 1 == currentLine.length()) {
             String nextLine = "";
             if (lines() >= line) {
                 nextLine = lines.remove(line - 1);
             }
             lines.add(line - 1, currentLine + nextLine);
             return "\n";
         } else if (column - 1 < currentLine.length()) {
             String deleted = currentLine.substring(column - 1, column);
             lines.add(line - 1, currentLine.substring(0, column - 1) + currentLine.substring(column));
             return deleted;
         } else {
             return "\n";
         }
     }
 
     @Override
     public synchronized String backspace() {
         if (line == 1 && column == 1) {
             return "";
         } else if (lines() < line) {
             return "";
         } else if (column == 1) {
             String currentLine = lines.remove(line - 1);
             String previousLine = lines.remove(line - 2);
             lines.add(line - 2, previousLine + currentLine);
             line--;
             column = previousLine.length();
             return "\n";
         } else {
             String currentLine = lines.remove(line - 1);
             String deleted = currentLine.substring(column - 2, column - 1);
             lines.add(line - 1, currentLine.substring(0, column - 2) + currentLine.substring(column - 1));
             column--;
             return deleted;
         }
     }
 
     @Override
     public synchronized void newLine() {
         while (lines() < line) {
             lines.add("");
         }
 
         if (column == 1) {
             lines.add(line - 1, "");
         } else {
             String currentLine = lines.remove(line - 1);
             //The character under the cursor should just move to the next line.
             String beforeNewLine = currentLine.substring(0, column - 1);
             String afterNewLine = currentLine.substring(column - 1);
             lines.add(line - 1, afterNewLine);
             lines.add(line - 1, beforeNewLine);
         }
         line++;
         column = 1;
     }
 
     @Override
     public synchronized void mergeLine() {
         if (line < lines.size()) {
             String currentLine = lines.remove(line - 1);
             String nextLine = lines.remove(line - 1);
             lines.add(line - 1, currentLine + nextLine);
         }
     }
 
     /**
      * Finds the next appearance of the String.
      *
      * @param str
      */
     @Override
     public synchronized void findNext(String str) {
         boolean found = false;
         int startLine = line;
         int startColumn = column + 1; //We always start one char after the cursor position.
 
         while (!found && startLine <= lines.size()) {
             String currentLine = getContent(startLine);
             String linePart = currentLine.length() > startColumn ? currentLine.substring(startColumn - 1) : "";
             if (linePart.contains(str)) {
                 column = startColumn + linePart.indexOf(str);
                 line = startLine;
                 found = true;
             } else {
                 startLine++;
                 startColumn = 1;
             }
         }
     }
 
     /**
      * Returns the number of lines.
      *
      * @return
      */
     @Override
     public synchronized int lines() {
         return lines.size();
     }
 
     /**
      * Finds the next appearance of the String.
      *
      * @param str
      */
     @Override
     public synchronized void findPrevious(String str) {
         boolean found = false;
         int startLine = line;
         int startColumn = column;
 
         while (!found && startLine > 0) {
             String currentLine = getContent(startLine);
             String linePart = currentLine.substring(0, startColumn - 1);
             if (linePart.contains(str)) {
                 column = linePart.indexOf(str, 0) + 1;
                 line = startLine;
                 found = true;
             } else {
                 startLine--;
                 currentLine = getContent(startLine);
                 startColumn = currentLine.length() + 1;
             }
         }
     }
 
     @Override
     public synchronized void open(File source) throws IOException {
         this.file = source;
         this.charset = Charsets.detect(source);
         lines.clear();
         if (source.exists()) {
             String[] contentLines = Files.toString(source, charset).split("\n|\r");
             lines.addAll(Arrays.asList(contentLines));
         }
     }
 
     @Override
     public synchronized void save(File target) throws IOException {
         if (target != null) {
             this.file = target;
         }
 
         if (file != null) {
             Files.writeToFile(file, getContent(), charset);
         } else {
             throw new IOException("No file specified for saving.");
         }
     }
 
     @Override
     public synchronized void close() throws IOException {
         this.file = null;
         this.charset = null;
         lines.clear();
     }
 
 
     @Override
     public synchronized String getContent() {
         StringBuilder contentBuilder = new StringBuilder();
         for (String l : lines) {
             contentBuilder.append(l).append("\n");
         }
         return contentBuilder.toString();
     }
 
     @Override
     public String getContent(int line) {
         if (line <= lines.size()) {
             return lines.get(line - 1);
         } else {
             return "";
         }
     }
 
     /**
      * Returns the {@link java.io.File} being edited.
      *
      * @return
      */
     @Override
     public File getFile() {
         return file;
     }
 
     public Boolean isDirty() {
         return dirty;
     }
 
     public void setDirty(Boolean dirty) {
         this.dirty = dirty;
     }
 }
