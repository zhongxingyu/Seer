 package com.danielwellman.jschallenge.java1;
 
 
 import com.danielwellman.jschallenge.java1.javaio.JavaFileSystemFacade;
 import com.danielwellman.jschallenge.java1.javaio.JavaSystemConsoleOut;
 
 public class Main {
    private final JavaFileSystemFacade fileSystemFacade;
     private final Encoder encoder;
     private final OutputWriter output;
 
    public Main(JavaFileSystemFacade fileSystemFacade, Encoder encoder, OutputWriter out) {
         this.fileSystemFacade = fileSystemFacade;
         this.encoder = encoder;
         this.output = out;
     }
 
     public void convert(String inputFilename, String outputFilename) {
         final ConverterApplication converter = new ConverterApplication(encoder);
         converter.addListener(new ConsoleAppendingDecoderListener(output));
         converter.addListener(new FileAppenderDecoderListener(fileSystemFacade.createFile(outputFilename)));
 
         converter.convert(fileSystemFacade.openFileForRead(inputFilename));
     }
 
     public static void main(String... args) {
         final String inputFilename = args[0];
         final String outputFilename = args[1];
         new Main(new JavaFileSystemFacade(), new Rot13Encoder(), new JavaSystemConsoleOut()).convert(inputFilename, outputFilename);
     }
 }
