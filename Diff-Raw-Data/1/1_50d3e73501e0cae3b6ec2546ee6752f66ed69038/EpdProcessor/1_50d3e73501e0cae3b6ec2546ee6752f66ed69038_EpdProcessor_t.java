 package sf.pnr.base;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.UndeclaredThrowableException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  */
 public class EpdProcessor {
 
     public void process(final List<String> testFiles, final EpdProcessorTask task) throws IOException {
         final long globalStartTime = System.currentTimeMillis();
         final Set<String> fens = new HashSet<String>(5000);
         for (String fileName: testFiles) {
             System.out.printf("Processing file '%s'\r\n", fileName);
             if (task instanceof SearchTask) {
                 ((SearchTask) task).resetCounters();
             }
             final long startTime = System.currentTimeMillis();
             final InputStream is = EpdProcessor.class.getResourceAsStream("res/" + fileName);
             final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
             int fenCount = 0;
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
 //                        System.out.printf("Skipping duplicate FEN in file '%s': %s\r\n", fileName, fen);
                         continue;
                     }
                     final Board board = StringUtils.fromFen(fen);
                     final Set<String> problems = StringUtils.checkBoard(board);
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
                         System.out.printf("Processed %d FENs\r\n", fenCount);
                     }
                 }
             } finally {
                 reader.close();
             }
             System.out.printf("Processed %d FENs in %.1fs.", fenCount,
                 ((double) System.currentTimeMillis() - startTime) / 1000);
             if (task instanceof SearchTask) {
                 final SearchTask searchTask = (SearchTask) task;
                 final int testCount = searchTask.getTestCount();
                 System.out.printf(" Pass ratio is %.2f%%",
                     ((double)(testCount - searchTask.getFailureCount()) * 100) / testCount);
             }
             System.out.println();
         }
        task.completed();
         System.out.printf("Processed all files in %.1fs\r\n",
             ((double) System.currentTimeMillis() - globalStartTime) / 1000);
     }
 }
