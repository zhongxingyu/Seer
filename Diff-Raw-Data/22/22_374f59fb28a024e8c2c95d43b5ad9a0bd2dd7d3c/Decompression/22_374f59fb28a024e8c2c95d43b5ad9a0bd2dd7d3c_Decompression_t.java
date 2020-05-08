 package semanticcompression;
 
 import weka.core.converters.AbstractSaver;
 import weka.core.converters.ArffSaver;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Package: semanticcompression
  * Class: Decompression
  * Description: Used to decompress data that has been compressed.
  */
 public class Decompression {
 
     DecompressionStream _inStream;
     File _outputFile;
 
     public Decompression(String inputFolder, String outputFile) {
         _inStream = new DecompressionStream(inputFolder);
         _outputFile = new File(outputFile);
     }
 
     public void run() {
         ArffSaver saver = new ArffSaver();
         try {
             saver.setFile(_outputFile);
            saver.setRetrieval(AbstractSaver.INCREMENTAL);
            saver.setStructure(_inStream.getHeader());
            System.out.println("Writing output to output file...");
             // the DecompressionStream does all the work
            while (_inStream.hasMoreInstances()) {
                 saver.writeIncremental(_inStream.nextInstance());
            }
            saver.writeIncremental(null);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
