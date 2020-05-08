 package org.openengsb.labs.endtoend.karaf.output;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.CharBuffer;
 import java.util.concurrent.Callable;
 
 class ResponseWorker implements Callable<String> {
     private static final int DEFAULT_BUFFER_SIZE = 1024;
 
     private final KarafPromptRecognizer promptRecognizer;
     private final InputStreamReader in;
 
     private final StringBuilder out = new StringBuilder();
 
     public ResponseWorker(InputStreamReader in, KarafPromptRecognizer promptRecognizer) {
         this.in = in;
         this.promptRecognizer = promptRecognizer;
     }
 
     public String getOutput() {
         return this.out.toString();
     }
 
     @Override
     public String call() {
         CharBuffer buf = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
         try {
             while (true) {
                 while (!this.in.ready()) {
                     try {
                         Thread.sleep(50);
                     } catch (InterruptedException e) {
                     }
                 }
                 while (this.in.ready()) {
                     this.in.read(buf);
                     out.append(buf.flip());
                     buf.clear();
                 }
 
                 if (this.promptRecognizer.recognize(out.toString())) {
                    out.delete(out.length() - this.promptRecognizer.getPrompt().length(), out.length());
                     return out.toString();
                 }
             }
         } catch (IOException e) {
             // Stream closed.
             return out.toString();
         }
     }
 }
