 package abrackadabra.io;
 
 import java.io.*;
 
 /**
  * Created with IntelliJ IDEA.
  * Author: Abrackadabra
  * Date: 23/11/12
  * Time: 09:12
  */
 public class OutputWriter extends PrintWriter {
    static final String DEFAULT_SEPARATOR = " ";
 
     public OutputWriter(String fileName) throws FileNotFoundException {
         super(fileName);
     }
 
     public OutputWriter(OutputStream outputStream) {
         super(outputStream);
     }
 
     public OutputWriter(Writer writer) {
         super(writer);
     }
 
     public void printSeparated(Object... objects) {
        printSeparated(DEFAULT_SEPARATOR, objects);
    }

    public void printSeparated(String separator, Object... objects) {
         for (int i = 0; i < objects.length; i++) {
             if (i > 0) {
                 print(separator);
             }
             print(objects[i]);
         }
     }
 }
