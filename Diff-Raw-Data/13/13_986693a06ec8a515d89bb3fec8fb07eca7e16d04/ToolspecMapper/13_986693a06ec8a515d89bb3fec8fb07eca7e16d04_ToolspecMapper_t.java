 package eu.scape_project.pt.mapred;
 
 import eu.scape_project.pt.proc.Processor;
 import eu.scape_project.pt.proc.StreamProcessor;
 import eu.scape_project.pt.proc.ToolProcessor;
 import eu.scape_project.pt.repo.Repository;
 import eu.scape_project.pt.repo.ToolRepository;
 import eu.scape_project.pt.tool.Operation;
 import eu.scape_project.pt.tool.Tool;
 import eu.scape_project.pt.util.CmdLineParser;
 import eu.scape_project.pt.util.Command;
 import eu.scape_project.pt.util.PipedArgsParser;
 import eu.scape_project.pt.util.PropertyNames;
 import eu.scape_project.pt.util.fs.Filer;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 
 /**
  * The Toolspec executor.
  *
  * @author Rainer Schmidt [rschmidt13]
  * @author Matthias Rella [myrho]
  */
 public class ToolspecMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
 
     private final Log LOG = LogFactory.getLog(getClass());
     private static final String SEP = " ";
 
     private CmdLineParser parser;
     private Repository repo;
     private Tool tool;
     private Operation operation;
 
     /**
      * Sets up toolspec repository and parser.
      */
     @Override
     public void setup(Context context) throws IOException {
         Configuration conf = context.getConfiguration();
         String strRepo = conf.get(PropertyNames.REPO_LOCATION);
         Path fRepo = new Path(strRepo);
         FileSystem fs = FileSystem.get(conf);
         this.repo = new ToolRepository(fs, fRepo);
 
         // create parser of command line input arguments
         parser = new PipedArgsParser();
     }
 
     /**
      * The map gets a key and value, the latter being a line describing
      * stdin and stdout file references and (a pipe of) tool/action pair(s) with
      * parameters.
      *
      * 1. Parse the input command-line and read parameters and arguments.
      * 2. Localize input and output file references and input stream.
      * 3. Chain piped commands to a simple-chained list of processors and run them.
      * 4. "De-localize" output files and output streams
      *
      * @param key offset in control file
      * @param value line describing the (piped) command(s) and stdin/out file refs
      * @param context Job context
      */
     @Override
     public void map(LongWritable key, Text value, Context context ) throws IOException {
         LOG.info("Mapper.map key:" + key.toString() + " value:" + value.toString());
 
         Text text = null;
         try {
             // parse input line for stdin/out file refs and tool/action commands
             parser.parse(value.toString());
 
             final Command[] commands = parser.getCommands();
             final String strStdinFile = parser.getStdinFile();
             final String strStdoutFile = parser.getStdoutFile();
 
             Processor firstProcessor = null;
             ToolProcessor lastProcessor = null; 
 
             Map<String, String>[] mapOutputFileParameters = new HashMap[commands.length];
 
             for(int c = 0; c < commands.length; c++ ) {
                 Command command = commands[c];
 
                 this.tool = repo.getTool(command.getTool());
 
                 lastProcessor = new ToolProcessor(this.tool);
 
                 this.operation = lastProcessor.findOperation(command.getAction());
                 if( this.operation == null )
                     throw new IOException(
                             "operation " + command.getAction() + " not found");
 
                 lastProcessor.setOperation(this.operation);
 
                 lastProcessor.initialize();
 
                 lastProcessor.setParameters(command.getPairs());
                 lastProcessor.setWorkingDir(workingDir());
 
                 // get parameters accepted by the lastProcessor.
                 Map<String, String> mapInputFileParameters = lastProcessor.getInputFileParameters(); 
                 mapOutputFileParameters[c] = lastProcessor.getOutputFileParameters(); 
 
                 // copy parameters to temporal map
                 Map<String, String> mapTempInputFileParameters = 
                     new HashMap<String, String>(mapInputFileParameters);
 
                 // localize parameters
                 for( Entry<String, String> entry : mapInputFileParameters.entrySet()) {
                     LOG.debug("input = " + entry.getValue());
                     String localFileRefs = localiseFileRefs(entry.getValue());
                     mapTempInputFileParameters.put( entry.getKey(), localFileRefs.substring(1));
                 }
 
                 Map<String, String> mapTempOutputFileParameters = 
                         new HashMap<String, String>(mapOutputFileParameters[c]);
                 for( Entry<String, String> entry : mapOutputFileParameters[c].entrySet()) {
                     LOG.debug("output = " + entry.getValue());
                     String localFileRefs = localiseFileRefs(entry.getValue());
                     mapTempOutputFileParameters.put( entry.getKey(), localFileRefs.substring(1));
                 }
 
                 // feed processor with localized parameters
                 lastProcessor.setInputFileParameters(mapTempInputFileParameters);
                 lastProcessor.setOutputFileParameters(mapTempOutputFileParameters);
 
                 // chain processor
                 if(firstProcessor == null )
                     firstProcessor = lastProcessor;
                 else {
                     Processor help = firstProcessor;  
                     while(help.next() != null ) help = help.next();
                     help.next(lastProcessor);
                 }
             }
 
             // Processors for stdin and stdout
             StreamProcessor streamProcessorIn = createStreamProcessorIn(strStdinFile);
             if( streamProcessorIn != null ) {
                 streamProcessorIn.next(firstProcessor);
                 firstProcessor = streamProcessorIn;
             } 
 
             OutputStream oStdout = createStdOut(strStdoutFile);
             StreamProcessor streamProcessorOut = new StreamProcessor(oStdout);
             lastProcessor.next(streamProcessorOut);
             
             firstProcessor.execute();
 
             delocalizeOutputParameters(mapOutputFileParameters);
 
             text = convertToResult(oStdout, strStdoutFile);
 
         } catch (Exception ex) {
            LOG.error("error during mapping", ex);
             text = convertToResult(ex);
         } finally {
             writeMappingResult(key, text, context);
         }
     }
 
     private String localiseFileRefs(String localFile) throws IOException {
         String[] remoteFileRefs = localFile.split(SEP);
         String localFileRefs = "";
         String workingDir = workingDir();
         for( int i = 0; i < remoteFileRefs.length; i++ ){
             Filer filer = Filer.create(remoteFileRefs[i]);
             filer.setWorkingDir(workingDir);
             filer.localize();
             localFileRefs = localFileRefs + SEP + filer.getRelativeFileRef();
         }
         return localFileRefs;
     }
 
     private Text convertToResult(OutputStream oStdout, final String strStdoutFile) {
         if( oStdout instanceof ByteArrayOutputStream )
             return  new Text( ((ByteArrayOutputStream)oStdout).toByteArray() );
         return new Text( strStdoutFile );
     }
 
     private Text convertToResult(Exception ex) {
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        return new Text( "ERROR: " + writer.toString() );
     }
 
     private OutputStream createStdOut(final String strStdoutFile) throws IOException {
         if( strStdoutFile != null ) 
             return Filer.create(strStdoutFile).getOutputStream();
         // default: output to bytestream
         return new ByteArrayOutputStream();
     }
 
     private StreamProcessor createStreamProcessorIn(final String strStdinFile) throws IOException {
         if( strStdinFile != null ) {
             InputStream iStdin = Filer.create(strStdinFile).getInputStream();
             return new StreamProcessor(iStdin);
         }
         return null;
     }
 
     private void delocalizeOutputParameters(Map<String, String>[] mapOutputFileParameters) throws IOException {
         for(int i = 0; i < mapOutputFileParameters.length; i++ ) {
             Map<String, String> outputFileParameters = mapOutputFileParameters[i];
             delocalizeOutputParameters(outputFileParameters);
         }
     }
 
     private void delocalizeOutputParameters(Map<String, String> outputFileParameters) throws IOException {
         String workingDir = workingDir();
         for( String strFile : outputFileParameters.values())
         {
             String[] localFileRefs = strFile.split(SEP);
             for( int j = 0; j < localFileRefs.length; j++ ){
                 Filer filer = Filer.create(localFileRefs[j]);
                 filer.setWorkingDir(workingDir);
                 filer.delocalize();
             }
         }
     }
 
     private String workingDir() {
         return System.getProperty("user.dir");
     }
 
     private void writeMappingResult(LongWritable key, Text text, Context context) throws IOException {
         try {
             context.write( key, text);
         } catch (InterruptedException ex) {
            LOG.error("error during write", ex);
             throw new IOException(ex);
         }
     }
 }
