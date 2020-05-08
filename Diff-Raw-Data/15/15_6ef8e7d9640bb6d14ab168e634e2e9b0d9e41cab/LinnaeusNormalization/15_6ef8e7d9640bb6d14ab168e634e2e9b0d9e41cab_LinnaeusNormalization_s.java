 package txtfnnl.pipelines;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.uima.UIMAException;
 import org.apache.uima.resource.ExternalResourceDescription;
 import org.apache.uima.resource.ResourceInitializationException;
 
 import txtfnnl.uima.analysis_component.LinnaeusAnnotator;
 import txtfnnl.uima.collection.AnnotationLineWriter;
 import txtfnnl.uima.collection.OutputWriter;
 import txtfnnl.uima.collection.XmiWriter;
 import txtfnnl.uima.resource.QualifiedStringResource;
 
 /**
  * A pipeline to detect names in dictionaries and annotate them with their IDs.
  * 
  * @author Florian Leitner
  */
 public class LinnaeusNormalization extends Pipeline {
   private LinnaeusNormalization() {
     throw new AssertionError("n/a");
   }
 
   public static void main(String[] arguments) {
     final CommandLineParser parser = new PosixParser();
     final Options opts = new Options();
     CommandLine cmd = null;
    // standard pipeline options
    Pipeline.addLogHelpAndInputOptions(opts);
    Pipeline.addTikaOptions(opts);
    Pipeline.addOutputOptions(opts);
     opts.addOption("p", "property-file", true,
         "set the Linnaeus property file path with the configuration data");
     opts.addOption("m", "id-map", true,
         "a map of IDs to another that will be applied to all annotations");
     opts.addOption("n", "namespace", true, "a namespace to apply to all annotations");
     try {
       cmd = parser.parse(opts, arguments);
     } catch (final ParseException e) {
       System.err.println(e.getLocalizedMessage());
       System.exit(1); // == EXIT ==
     }
     final Logger l = Pipeline.loggingSetup(cmd, opts,
         "txtfnnl match [options] -p file.property <directory|files...>\n");
     // Taxon ID mapping resource
     ExternalResourceDescription taxIdMap = null;
    if (cmd.hasOption('L')) {
       try {
        taxIdMap = QualifiedStringResource.configure("file:" + cmd.getOptionValue('L')).create();
       } catch (ResourceInitializationException e) {
         l.severe(e.toString());
         System.err.println(e.getLocalizedMessage());
         e.printStackTrace();
         System.exit(1); // == EXIT ==
       }
     }
     // Linnaeus setup
     String namespace = cmd.hasOption('n') ? cmd.getOptionValue('n')
         : LinnaeusAnnotator.DEFAULT_NAMESPACE;
     File properties = null;
     try {
       properties = new File(cmd.getOptionValue('p'));
     } catch (NullPointerException e) {
       System.err.println("property file missing");
       System.exit(1); // == EXIT ==
     }
     LinnaeusAnnotator.Builder linnaeus = LinnaeusAnnotator
         .configure(properties).setIdMappingResource(taxIdMap)
         .setAnnotationNamespace(namespace);
     // output
     OutputWriter.Builder writer;
     if (Pipeline.rawXmi(cmd)) {
       writer = Pipeline.configureWriter(cmd,
           XmiWriter.configure(Pipeline.ensureOutputDirectory(cmd)));
     } else {
       writer = Pipeline.configureWriter(cmd, AnnotationLineWriter.configure())
           .setAnnotatorUri(LinnaeusAnnotator.URI).setAnnotationNamespace(namespace);
     }
     try {
       // 0:tika, 1:linnaeus
       final Pipeline pipe = new Pipeline(2);
       pipe.setReader(cmd);
       pipe.configureTika(cmd);
       pipe.set(1, Pipeline.textEngine(linnaeus.create()));
       pipe.setConsumer(Pipeline.textEngine(writer.create()));
       pipe.run();
       pipe.destroy();
     } catch (final UIMAException e) {
       l.severe(e.toString());
       System.err.println(e.getLocalizedMessage());
       e.printStackTrace();
       System.exit(1); // == EXIT ==
     } catch (final IOException e) {
       l.severe(e.toString());
       System.err.println(e.getLocalizedMessage());
       e.printStackTrace();
       System.exit(1); // == EXIT ==
     }
     System.exit(0);
   }
 }
