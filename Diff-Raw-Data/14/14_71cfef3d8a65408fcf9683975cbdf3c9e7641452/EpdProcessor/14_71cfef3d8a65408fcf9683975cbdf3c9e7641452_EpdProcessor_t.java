 package sf.pnr.tests;
 
 import sf.pnr.base.Board;
 import sf.pnr.base.StringUtils;
 import sf.pnr.base.Utils;
 
 import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.UndeclaredThrowableException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 /**
  */
 public class EpdProcessor {
 
     private final Class resourceBaseClass;
 
     public EpdProcessor() {
         this(EpdProcessor.class);
     }
 
     public EpdProcessor(final Class resourceBaseClass) {
         this.resourceBaseClass = resourceBaseClass;
     }
 
     public void process(final List<String> testFiles, final EpdProcessorTask task)
             throws IOException {
         process(testFiles, task, -1);
     }
 
     public void process(final List<String> testFiles, final EpdProcessorTask task, final long rndSeed)
             throws IOException {
         final Random rnd;
         if (rndSeed == -1) {
             rnd = null;
         } else if (rndSeed == 0) {
             final long seed = System.currentTimeMillis();
             System.out.println("Using random seed: " + seed);
             rnd = new Random(seed);
         } else {
             rnd = new Random(rndSeed);
         }
 
         final long globalStartTime = System.currentTimeMillis();
         final Map<String, List<Fen>> fensToProcess = getFensToProcess(testFiles);
         int remainingFens = 0;
         for (String fileName: fensToProcess.keySet()) {
             remainingFens += fensToProcess.get(fileName).size();
         }
         if (task instanceof SearchTask) {
             ((SearchTask) task).resetCounters();
         }
         String prevFileName = null;
         for (;remainingFens > 0; remainingFens--) {
             int index = 0;
             if (rnd != null) {
                 index = rnd.nextInt(remainingFens);
             }
             String fileName = "";
             Fen fen = null;
             for (Map.Entry<String, List<Fen>> entry: fensToProcess.entrySet()) {
                 final List<Fen> fens = entry.getValue();
                 if (fens.size() > index) {
                     fileName = entry.getKey();
                    fen = fens.remove(index);
                     break;
                 }
                 index -= fens.size();
             }
             if (rnd == null && !fileName.equals(prevFileName)) {
                 System.out.printf("Processing file '%s'\r\n", fileName);
                 prevFileName = fileName;
             }
             int fenCount = 0;
             final Board board = StringUtils.fromFen(fen.getFen());
             final Set<String> problems = Utils.checkBoard(board);
             if (!problems.isEmpty()) {
                 System.out.printf("Skipping FEN '%s' because of the following problem(s):\r\n", fen);
                 for (String problem: problems) {
                     System.out.println("  - " + problem);
                 }
                 continue;
             }
             final Map<String, String> commands = fen.getCommands();
             fenCount++;
             try {
                 task.run(fileName, board, commands);
             } catch (Exception e) {
                 throw new UndeclaredThrowableException(e, "Task failed on FEN: " + fen);
             } catch (Error e) {
                 System.out.printf("Task failed on FEN #%d: %s\r\n", fenCount, fen);
                 throw e;
             }
             if (fenCount % 1000 == 0) {
                 System.out.printf("Processed %d FENs in %.1fs.\r\n", fenCount,
                     ((double) System.currentTimeMillis() - globalStartTime) / 1000);
                 if (task instanceof SearchTask) {
                     final SearchTask searchTask = (SearchTask) task;
                     final int testCount = searchTask.getTestCount();
                     System.out.printf(" Pass ratio is %.2f%%\r\n",
                         ((double)(testCount - searchTask.getFailureCount()) * 100) / testCount);
                 }
             }
         }
         task.completed();
         System.out.printf("Processed all files in %.1fs\r\n",
             ((double) System.currentTimeMillis() - globalStartTime) / 1000);
         if (task instanceof SearchTask) {
             final SearchTask searchTask = (SearchTask) task;
             final int testCount = searchTask.getTestCount();
             System.out.printf(" Pass ratio is %.2f%%\r\n",
                 ((double)(testCount - searchTask.getFailureCount()) * 100) / testCount);
         }
     }
 
     public Map<String, List<Fen>> getFensToProcess(final List<String> testFiles) throws IOException {
         final Set<String> fens = new HashSet<String>(5000);
         final Map<String, List<Fen>> fensToProcess = new LinkedHashMap<String, List<Fen>>();
         for (String fileName: testFiles) {
            final File file = new File(fileName);
            final InputStream is;
            if (file.isAbsolute()) {
                is = new FileInputStream(file);
            } else {
                is = resourceBaseClass.getResourceAsStream("res/" + fileName);
            }
             final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
             try {
                 for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                     line = line.trim();
                     if (line.startsWith("#")) {
                         continue;
                     }
                     final String[] parts = line.split(";");
                     final String[] firstSegments = parts[0].split(" ");
                     final String fen = firstSegments[0] + " " + firstSegments[1] + " " + firstSegments[2] + " " +
                         firstSegments[3] + " 0 1";
                     if (!fens.add(fen)) {
                         continue;
                     }
                     final Board board = StringUtils.fromFen(fen);
                     final Set<String> problems = Utils.checkBoard(board);
                     if (!problems.isEmpty()) {
                         System.out.printf("Skipping FEN '%s' because of the following problem(s):\r\n", fen);
                         for (String problem: problems) {
                             System.out.println("  - " + problem);
                         }
                         continue;
                     }
                     final Map<String, String> commands = new HashMap<String, String>();
                     if (firstSegments.length >= 6) {
                         commands.put(firstSegments[firstSegments.length - 2], firstSegments[firstSegments.length - 1]);
                     } else {
                         commands.put(firstSegments[firstSegments.length - 1], "");
                     }
                     for (int i = 1, partsLength = parts.length; i < partsLength; i++) {
                         final String part = parts[i];
                         final String[] segments = part.trim().split(" ", 2);
                         final String command = segments[0];
                         final String parameter;
                         if (segments.length == 2) {
                             parameter = segments[1];
                         } else {
                             parameter = "";
                         }
                         commands.put(command, parameter);
                     }
                     List<Fen> fensInFile = fensToProcess.get(fileName);
                     if (fensInFile == null) {
                         fensInFile = new ArrayList<Fen>();
                         fensToProcess.put(fileName, fensInFile);
                     }
                     fensInFile.add(new Fen(fen, commands));
                 }
             } finally {
                 reader.close();
             }
         }
         return fensToProcess;
     }
 
     private static class Fen {
         private final String fen;
         private final Map<String, String> commands;
 
         private Fen(final String fen, final Map<String, String> commands) {
             this.fen = fen;
             this.commands = commands;
         }
 
         public String getFen() {
             return fen;
         }
 
         public Map<String, String> getCommands() {
             return commands;
         }
 
         @Override
         public String toString() {
             return fen;
         }
     }
 }
