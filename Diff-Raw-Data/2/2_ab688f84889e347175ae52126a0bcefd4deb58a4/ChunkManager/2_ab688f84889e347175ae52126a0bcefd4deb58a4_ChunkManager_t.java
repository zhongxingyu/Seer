 package edu.unh.schwartz.epframework;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.concurrent.PriorityBlockingQueue;
 import java.util.List;
 import java.util.regex.Pattern;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Does all of the IO functions for the process. Can split an input file based
  * on a pattern and merge it back together in the same order.
  */
 public final class ChunkManager
 {
     private enum MergeMethod
     {
         DEFAULT,
         JAVA,
         EXTERNAL
     }
 
     /**
      * The Log.
      */
     private static final Log LOG = LogFactory.getLog(ChunkManager.class);
 
    /**
      * The default pattern for a manipulator.
      */
     private static final String DEFAULT_PATTERN = "^.*$";
 
    /**
      * The file name for the executable that will do an external merge if that
      * configuration was chosen.
      */
     private static final String EXTERNAL_MERGE_EXEC_NAME = "./merger";
 
     private final String rootDir = "/tmp/";
 
     /**
      * The pattern used to split the input file.
      */
     private Pattern pattern;
     
     /**
      * The chunks for the current input file.
      */
     private List<Chunk> chunks;
 
     /**
      * The number of header lines to put in the final output file.
      */
     private int headerLines;
 
     /**
      * Constructs a manipulator with the default pattern and 0 header lines.
      */
     public ChunkManager()
     {
         this(DEFAULT_PATTERN, 0);
     }
 
     /**
      * Constructs a manipulator with the default pattern and some number of 
      * header lines.
      *
      * @param headerLines - the number of header lines to take from output files
      */
     public ChunkManager(final int headerLines)
     {
         this(DEFAULT_PATTERN, headerLines);
     }
 
     /**
      * Constructs a manipulator with a custom pattern and some number of header
      * lines.
      *
      * @param regex - Regular expression to split on
      * @param headerLines - the number of header lines to take from output files
      */
     public ChunkManager(final String regex, final int headerLines)
     {
         this.pattern = Pattern.compile(regex);
         this.headerLines = headerLines;
     }
 
     /**
      * Splits the file and creates chunks.
      *
      * @param fileName - the name of the file
      * @throws IOException - when there is any kind of problem with the input
      */
     public void split(final String fileName) throws IOException 
     {
         this.chunks = new ArrayList<Chunk>();
 
         StringBuilder sb = new StringBuilder();
         
         final File f = new File(fileName);
         final BufferedReader reader = new BufferedReader(new FileReader(f));
         String line = reader.readLine();
         while (line != null)
         {
             sb.append(line);
             if (pattern.matcher(line).matches())
             {
                 // Save the chunk
                 final String content = sb.toString();
 
                 // Make a directory for that file
                 final File dir = new File(rootDir + content.hashCode());
                 dir.mkdir();
 
                 // Write the content to a file
                 final PrintWriter inWriter = 
                     new PrintWriter(dir.getAbsolutePath() + "/in");
                 inWriter.println(content);
                 inWriter.close();
 
                 this.chunks.add(new Chunk(content, dir));
 
                 sb = new StringBuilder();
             }
             else
             {
                 sb.append("\n");
             }
 
             line = reader.readLine();
         }
     }
 
     /**
      * Make chunks for the pre-split input files.
      * @param dir - the directory of pre-split files
      * @throws IOException if there is any problem with the files in the
      * directory
      */
     public void splitFiles(final String dir) throws IOException
     {
         this.chunks = new ArrayList<Chunk>();
 
         final File directory = new File(dir);
         for (File i : directory.listFiles())
         {
            final File d = new File(rootDir + i.hashCode());
             d.mkdir();
             this.chunks.add(new Chunk(i, d));
         }
     }
     
     /**
      * @return the chunks as a thread-safe queue
      */
     public PriorityBlockingQueue<Chunk> getChunks()
     {
         return new PriorityBlockingQueue<Chunk>(this.chunks);
     }
 
     /**
      * Merges the chunks back together. Prints the results of the work in the
      * order of the original chunks.
      * @param fileName - the name of the output file
      * @param mergeMethod - the type of merge the user specified
      */
     public void merge(final String fileName, final int mergeMethod)
     {
         switch (MergeMethod.values()[mergeMethod])
         {
             case DEFAULT:
                 defaultMerge(fileName);
                 break;
             case JAVA:
                 customMerge(fileName);
                 break;
             case EXTERNAL:
                 externalMerge(fileName);
                 break;
             default:
                 LOG.fatal("Illegal Merge Method");
                 assert(false);
         }
     }
 
     private void defaultMerge(final String fileName)
     {
         LOG.info("Using Default Merge");
         try(final PrintWriter writer = new PrintWriter(fileName))
         {
             writer.print(this.chunks.get(0).getHeader());
 
             for (final Chunk c : this.chunks)
             {
                 writer.print(c.getResult());
             }
         }
         catch (FileNotFoundException e)
         {
             LOG.fatal("merge: " + e.getMessage());
         }
     }
 
     private void customMerge(final String fileName)
     {
         LOG.info("Using Custom Merge");
         CustomMerge.merge(fileName, this.chunks);
     }
 
     private void externalMerge(final String fileName) 
     {
         LOG.info("Using External Merge");
 
         // Prepare the args
         final List<String> commands = new ArrayList<String>();
         commands.add(EXTERNAL_MERGE_EXEC_NAME);
         for (final Chunk c : this.chunks)
         {
             commands.add(c.getDirectory().getAbsolutePath());
         }
 
         try
         {
             // Create the process builder
             final ProcessBuilder pb = new ProcessBuilder(commands);
             pb.redirectErrorStream(true);
             pb.redirectOutput(new File(fileName));
 
             // Start the work and capture the time it takes to run
             LOG.info("Starting external merge");
             final Process proc = pb.start();
             proc.waitFor();
             LOG.info("Finished external merge");
         }
         catch (IOException|InterruptedException e)
         {
             LOG.fatal("externalMerge: " + e.getMessage());
         }
     }
     
     /**
      * Cleans up the chunks and temp out files and directories.
      */
     public void cleanUp()
     {
         for (final Chunk c : this.chunks)
         {
             c.clean();
         }
     }
 
     /**
      * Prints the statistics of the <code>Worker</code>s and the
      * <code>Chunk</code>s to a CSV file 'stats.csv' in the same output
      * directory as the program.
      * @param workerStats - the statstics of the Workers
      * @param outDir - the directory to output the file to
      */
     public void printStats(final String workerStats, final String outDir)
     {
         try(final PrintWriter statsOut = new PrintWriter(outDir + "/stats.csv"))
         {
             final String comma = ",";
             final String header = "Chunk #,Length,Runtime(ms)";
             statsOut.print(workerStats);
             LOG.debug(workerStats);
 
             statsOut.println(header);
             LOG.debug(header);
             for (int i = 0; i < this.chunks.size(); i++)
             {
                 final Chunk c = this.chunks.get(i);
                 final StringBuilder sb = new StringBuilder();
                 sb.append(c.hashCode()).append(comma).append(c.length());
                 sb.append(comma).append(c.getRuntime());
                 statsOut.println(sb.toString());
                 LOG.debug(sb.toString());
             }
         }
         catch (IOException e)
         {
             LOG.error("printStats: " + e.getMessage());
         }
     }
 }
