 package com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.diff;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.compare.rangedifferencer.RangeDifference;
 import org.eclipse.compare.rangedifferencer.RangeDifferencer;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.text.Position;
 
 import com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.LineComparator;
 import com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.common.Constants;
 import com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.dto.Complement;
 import com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.dto.LineAttrs;
 import com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.marker.MarkerFactory;
 import com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.marker.MarkerFactory.MarkerType;
 
 /**
  * Business logic to compute and render markers and annotations for differences between a complement.
  */
 public class Differ {
     
     /**
      * Computes all Markers and Annotations of a complement and renders them into the editor.
      * @param complement
      */
     public static void computeAndRenderAnnotations(Complement complement) {
         if (complement == null || complement.getSrcFile() == null || complement.getGeneratedFile() == null) {
             // no real complement
             return;
         }
         try {
             RangeDifference[] compareOnLevelLine = compareOnLevelLine(complement);
            if (compareOnLevelLine == null) {
                // no diffs
                return;
            }
 
             Map<Integer, LineAttrs> lines = complement.getLineMapOfSrcFile();
 //            System.out.println(printLineOffset(lines));
             /*
              * visited lines:
              * 1: these lines have annotations
              * 0: no annotations --> these are generated lines
              */
             Integer[] visitedLines = new Integer[lines.keySet().size()];
             Arrays.fill(visitedLines, new Integer(0));
             
             // mark changes
             for (int i = 0, diffindex = 0; i < compareOnLevelLine.length; i++) {
                 RangeDifference crt = compareOnLevelLine[i];
 
                 if (! lines.containsKey(crt.leftStart())) {
                     // doesn't know this line.
                     continue;
                 }
                 if (lines.containsKey(crt.leftStart())) {
                     if (lines.get(crt.leftStart()).isDoNotMarkMe()) {
                         diffindex++;
                         continue;
                     }
                 }
                 Position position = posByLineNumber(lines, crt.leftStart(), crt.leftEnd());
                 
                 MarkerType markerType = MarkerType.getByDiff(crt);
                 MarkerFactory.createMarkerByTextSelection(
                         complement, 
                         markerType, 
                         position, 
                         getAnnotationsMsg(crt.leftStart(), crt.leftEnd()),
                         diffindex);
                 
                 for (int v = crt.leftStart(); v <= (crt.leftEnd() - 1); v++) {
                     visitedLines[v] = 1;
                 }
                 diffindex++;
             }
             // last line is only a helper line
             visitedLines[visitedLines.length - 1] = -1;
             
             // mark generated
             for (int lineStart = 0; lineStart < visitedLines.length; lineStart++) {
                 if (visitedLines[lineStart] == 0) {
                     // start gen annotation
                     int lineEnd = lineStart + 1;
                     while (lineEnd < visitedLines.length && visitedLines[lineEnd] == 0) {
                         lineEnd++;
                     }
                     // end gen
                     Position position = posByLineNumber(lines, lineStart, lineEnd);
                     MarkerFactory.createMarkerByTextSelection(
                             complement, 
                             MarkerType.GENERATED, 
                             position, 
                             getAnnotationsMsg(lineStart, lineEnd),
                             0 /* Generated Code is equal to right; it is impossible to jump to a diff. */);
                     lineStart = lineEnd;
                 }
             }
             
         } catch (IOException e) {
             e.printStackTrace();
         } catch (CoreException e) {
             e.printStackTrace();
         }
     }
     
     /**
      * Maps a line-based input to an absolute position rage.
      * @param lines key: lineNumber - 0-based.
      * @param lineStart 0-based.
      * @param lineEnd 0-based.
      */
     private static Position posByLineNumber(Map<Integer, LineAttrs> lines, int lineStart, int lineEnd) {
         LineAttrs lineStartOffset = lines.get(lineStart);
         
         LineAttrs lineEndOffset = lines.get(lineEnd);
         
         Position position = new Position(lineStartOffset.getOffset());
         if (lineEndOffset != null) {
             int length = lineEndOffset.getOffset() - lineStartOffset.getOffset();
             if (length > 1) {
                 // ignore new line (otherwise: two markers at one line).
                 length--;
             }
             position.setLength(length);
         } else {
             System.err.println("Ui!");
             System.err.println("lines: " + lineStart + " -> " + lineEnd);
             System.err.println(printLineOffset(lines));
         }
         return position;
     }
     
     /**
      * Print-Helper.
      */
     private static String printLineOffset(Map<Integer, LineAttrs> lines) {
         int l = 0;
         String txt = "";
         while (lines.containsKey(l)) {
             LineAttrs lineOffset = lines.get(l);
             String lineText = lineOffset.getLineText();
             String toConsole = StringUtils.replace(lineText, "\n", "¶");
             txt +=
                     StringUtils.leftPad(l + "", 2) 
                     + " << " +  StringUtils.leftPad(lineOffset.getOffset() + "", 3) 
                     + ": " + toConsole
                     + "\n";
             l++;
         }
         return txt;
     }
     
     /**
      * Calls the internal Differencer.
      */
     private static RangeDifference[] compareOnLevelLine(Complement toCompare) {
         
         RangeDifference[] differences = null;
         try {
             InputStream leftInput = toCompare.getSrcFile().getContents();
             InputStream rightInput = toCompare.getGeneratedFile().getContents();
             LineComparator leftLineComparator = new LineComparator(leftInput, Constants.UTF_8 );
             LineComparator rightLineComparator = new LineComparator(rightInput, Constants.UTF_8 );
             
             differences = RangeDifferencer.findDifferences(leftLineComparator, rightLineComparator);
         } catch (CoreException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         toCompare.setDiffs(differences);
         return differences;
     }
     
     /**
      * Returns a human readable message for an annotation.
      * @param lineStart 0-based!
      * @param lineEnd 0-based!
      * @return human readable message for an annotation.
      */
     private static String getAnnotationsMsg(int lineStart, int lineEnd) {
         return ("Line " + (lineStart + 1) + " -> " + (lineEnd + 1) + " [" + (lineEnd - lineStart) + "]");
     }
 
 }
