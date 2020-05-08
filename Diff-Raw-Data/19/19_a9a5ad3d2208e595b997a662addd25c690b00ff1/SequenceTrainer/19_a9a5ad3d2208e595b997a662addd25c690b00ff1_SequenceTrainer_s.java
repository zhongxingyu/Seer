 package de.tum.in.lrr.hmm.cli;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectOutputStream;
 import java.io.Reader;
 import java.io.SequenceInputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 import java.util.zip.GZIPOutputStream;
 
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 import de.tum.in.lrr.hmm.Alphabet;
 import de.tum.in.lrr.hmm.BatchTrainer;
 import de.tum.in.lrr.hmm.Model;
 import de.tum.in.lrr.hmm.Sequence;
 import de.tum.in.lrr.hmm.gene.AbstractSequenceReader;
 import de.tum.in.lrr.hmm.gene.FileFormatException;
 import de.tum.in.lrr.hmm.gene.SequenceReader;
 
 /**
  * @author <a href="bauerb@in.tum.de">Bernhard Bauer</a>
  *
  */
 public class SequenceTrainer {
 
     private static final int DEFAULT_LINEAR_THRESHOLD = 1<<14 - 1;
 
     private static final double DEFAULT_EPSILON = 0.002;
 
     private static final int DEFAULT_MAX_DEPTH = 8;
 
     @Option(name = "--format", usage="file format (FASTA or EMBL)")
     protected SequenceReader.Format format = null;
 
     @Option(name = "--maxDepth", usage="maximum trie depth (default: "+DEFAULT_MAX_DEPTH+")")
     protected int maxDepth = DEFAULT_MAX_DEPTH;
 
     @Option(name = "--maxIterations", usage="maximum number of iterations for batch learning")
     protected int maxIterations = Integer.MAX_VALUE;
 
     @Option(name = "--epsilon", usage="threshold for parameter difference (default: "+DEFAULT_EPSILON+")")
     protected double parameterEpsilon = DEFAULT_EPSILON;
 
     @Option(name = "--linearThreshold", usage="threshold for linear training (default: "+DEFAULT_LINEAR_THRESHOLD+")")
     protected int linearThreshold = DEFAULT_LINEAR_THRESHOLD;
 
     @Argument
     protected List<String> arguments = new ArrayList<String>();
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         SequenceTrainer trainer = new SequenceTrainer();
         trainer.doMain(args);
 
     }
 
     /**
      * @param args
      */
     private void doMain(String[] args) {
         final CmdLineParser parser = new CmdLineParser(this);
         try {
             parser.parseArgument(args);
             if (arguments.size() < 1) {
                 throw new CmdLineException(parser, "No arguments given");
             }
         } catch (CmdLineException e) {
             System.err.println(e.getMessage());
             System.err.println("Usage: java "+SequenceTrainer.class.getName()+" <options> <HMM file> <sequence files...>");
             parser.printUsage(System.err);
             System.exit(1);
         }
 
         String hmmFileName = arguments.remove(0);
         Vector<InputStream> files = new Vector<InputStream>();
         final InputStream input;
         if (arguments.isEmpty()) {
             input = System.in;
         } else {
             for (String s : arguments) {
                 try {
                     files.add(new FileInputStream(s));
                 } catch (FileNotFoundException e) {
                     System.err.println("Warning: Couldn't find '" + s + "'!");
                 }
             }
             input = new SequenceInputStream(files.elements());
         }
         Reader r = new InputStreamReader(input);
 
         try {
             SequenceReader reader = AbstractSequenceReader.create(r, format);
             Sequence trainingSequence = reader.readSequence();
 
             if (trainingSequence == null) {
                 System.err.println("Warning: No sequences found");
                 return;
             }
             Alphabet alphabet = trainingSequence.getAlphabet();
             Model model = new Model(alphabet, Model.Variant.PARTIAL_BACKLINKS);
 
             final List<Sequence> trainingSequences = new ArrayList<Sequence>();
 
             int seqNo = 0;
             while (true) {
                 model.learn(trainingSequence, maxDepth, linearThreshold);
                 System.out.print("\r"+(++seqNo)+" sequences read");
                 trainingSequences.add(trainingSequence);
                 trainingSequence = reader.readSequence();
                 if (trainingSequence == null) {
                     break;
                 }
                 if (trainingSequence.getAlphabet() != alphabet) {
                     throw new FileFormatException("All sequences must be of the same type");
                 }
             }
 
             System.out.println();
 
             final BatchTrainer trainer = new BatchTrainer(model);
            Model oldModel = model;
 
             int iteration = 0;
 
            while (iteration < maxIterations) {
                 for (Sequence s : trainingSequences) {
                     trainer.learn(s, maxDepth, linearThreshold);
                 }
                 System.out.print("\rIteration "+ iteration);
                 final Model newModel = trainer.finishBatch();
 
                final double parameterDifference = oldModel.parameterDifference(newModel);

                if (parameterDifference < parameterEpsilon) {
                    break;
                }
                iteration++;
 
                oldModel = newModel;
            }
 
             System.out.println();
 
             final ObjectOutputStream output = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(hmmFileName)));
             output.writeObject(model);
             output.close();
 
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
 }
