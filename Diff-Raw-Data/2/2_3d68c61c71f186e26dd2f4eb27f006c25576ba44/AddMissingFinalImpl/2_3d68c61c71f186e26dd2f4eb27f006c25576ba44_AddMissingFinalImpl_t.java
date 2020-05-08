 package sk.mka.app.finalizer.addfinal;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import sk.mka.app.finalizer.AbstractAction;
 import sk.mka.app.finalizer.IAction;
 import sk.mka.app.finalizer.Utils;
 
 public final class AddMissingFinalImpl extends AbstractAction implements
         IAction {
 
     @Override
     public void parseFile(final File file) {
         BufferedReader reader = null;
         final StringBuffer stringBuffer = new StringBuffer();
         final StringBuffer paramsTemporaryBuffer = new StringBuffer();
         boolean doModification = false;
 
         try {
 
             String extension = null;
             String fileName = file.getName();
             int i = fileName.lastIndexOf(Utils.DOT);
             if (i > 0) {
                 extension = fileName.substring(i + 1);
             }
             if (extension != null && extension.equals(Utils.JAVA)) {
                 System.out.println(file);
                 doModification = true;
 
                 reader = new BufferedReader(new FileReader(file));
                 String line;
 
                 while ((line = reader.readLine()) != null) {
 
                     if (line.contains(Utils.COMMENT)) { // skip commented code
 
                         int indexOfSlash = line.indexOf(Utils.COMMENT);
 
                         if (line.contains(Utils.OPEN_PARENTHES_OPENING)) {
                             int indexOfBeginingBracket = line
                                     .indexOf(Utils.OPEN_PARENTHES_OPENING);
                             if (indexOfSlash < indexOfBeginingBracket) {
                                 appendLine(stringBuffer, line);
                             } else {
                                 modify(stringBuffer, paramsTemporaryBuffer,
                                         line);
                             }
                         } else
                             appendLine(stringBuffer, line);
                     } else
                         modify(stringBuffer, paramsTemporaryBuffer, line);
                 }
 
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 if (reader != null) {
                     reader.close();
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         if (doModification) {
             String absolutePath = file.getAbsolutePath();
             final int lastIndexOfDot = absolutePath.lastIndexOf(".");
             absolutePath = absolutePath.substring(0, lastIndexOfDot);
             absolutePath += ".java";
             writeToFile(absolutePath, stringBuffer.toString());
         }
     }
 
     @Override
     protected void modify(StringBuffer stringBuffer,
                           StringBuffer paramsTemporaryBuffer, String line) {
         if (line.contains(Utils.PRIVATE) || line.contains(Utils.PUBLIC)
                 || line.contains("static") || line.contains(Utils.PROTECTED)) {
            if (!line.contains(Utils.NEW) && !line.contains(".class")) {
 
                 if (line.contains(Utils.OPEN_PARENTHES_OPENING)) {
 
                     int indexOfStart = line
                             .indexOf(Utils.OPEN_PARENTHES_OPENING);
                     final String beg = line.substring(0, indexOfStart + 1);
 
                     if (line.contains(Utils.OPEN_PARENTHESIS_CLOSING)) {
                         int endOfStart = line
                                 .indexOf(Utils.OPEN_PARENTHESIS_CLOSING);
                         final String middle = line.substring(indexOfStart + 1,
                                 endOfStart);
                         final String end = line.substring(endOfStart);
                         boolean isWhitespace = middle.matches("^\\s*$");
                         if (!isWhitespace) {
                             appendFinalToParams(stringBuffer, beg, middle, end);
                         } else {
                             appendLine(stringBuffer, line);
                         }
                     } else
                         paramsTemporaryBuffer.append(line);
                 } else {
                     appendLine(stringBuffer, line);
                 }
             } else
                 appendLine(stringBuffer, line);
         } else {
             if (paramsTemporaryBuffer.length() > 0) {
 
                 if (line.contains(Utils.OPEN_PARENTHESIS_CLOSING)) {
                     paramsTemporaryBuffer.append(line);
                     final int indexOfStartBracket = paramsTemporaryBuffer
                             .indexOf(Utils.OPEN_PARENTHES_OPENING);
                     final int indexOfEndOfStart = paramsTemporaryBuffer
                             .indexOf(Utils.OPEN_PARENTHESIS_CLOSING);
 
                     final String beggining = paramsTemporaryBuffer.substring(0,
                             indexOfStartBracket + 1);
                     final String middle = paramsTemporaryBuffer.substring(
                             indexOfStartBracket + 1, indexOfEndOfStart);
                     final String end = paramsTemporaryBuffer
                             .substring(indexOfEndOfStart);
 
                     final StringBuffer tempStringBuffer = new StringBuffer();
                     appendFinalToParams(tempStringBuffer, beggining, middle,
                             end);
                     stringBuffer.append(tempStringBuffer.toString());
                     paramsTemporaryBuffer.delete(0,
                             paramsTemporaryBuffer.length());
 
                 } else
                     paramsTemporaryBuffer.append(line);
 
             } else
                 appendLine(stringBuffer, line);
         }
     }
 
     private void appendFinalToParams(StringBuffer stringBuffer,
                                      final String beg, final String middle, final String end) {
         final String[] split = middle.split(",");
         for (int i = 0; i < split.length; i++) {
             if (i == 0) {
                 stringBuffer.append(beg);
             }
 
             if (!split[i].contains(Utils.FINAL))
                 stringBuffer.append(Utils.FINAL + Utils.SPACE);
 
             stringBuffer.append(split[i]);
             if (i < split.length - 1)
                 stringBuffer.append(Utils.COMMA);
 
             if (i == split.length - 1) {
                 stringBuffer.append(end);
             }
 
         }
         stringBuffer.append(Utils.NEWLINE);
     }
 
     private void appendLine(StringBuffer stringBuffer, String line) {
         stringBuffer.append(line);
         stringBuffer.append(Utils.NEWLINE);
     }
 
     private void writeToFile(final String filename, final String output) {
         try {
             final BufferedWriter out = new BufferedWriter(new FileWriter(
                     filename));
             out.write(output);
             out.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
 }
