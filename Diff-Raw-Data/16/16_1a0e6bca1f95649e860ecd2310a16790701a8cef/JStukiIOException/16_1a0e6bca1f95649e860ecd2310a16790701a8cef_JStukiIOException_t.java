 package net.abesto.jstuki.io;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Custom IO exceptions
  *
  * @author Nagy Zoltán (abesto0@gmail.com)
  */
 public class JStukiIOException {
 
     /**
      * Thrown when a @ref TextSyntax instance has one of its templates missing.
      */
     public static class IncompleteTextSyntaxException extends IOException {
 
         private TextSyntax.Template t;
         private File f;
 
         /**
          * @param _t The undefined template
          */
         public IncompleteTextSyntaxException(TextSyntax.Template _t) {
             t = _t;
             f = null;
         }
 
         /**
          * @param f The file that was being read when the exception was thrown
          */
         public void setF(File f) {
             this.f = f;
         }
 
         public String getMessage() {
             StringBuilder s = new StringBuilder("Required template ");
             s.append(t.toString());
             s.append(" not defined");
             if (f != null) {
                 s.append(" in file ");
                 s.append(f.getAbsoluteFile());
             }
             return s.toString();
         }
     }
 
     /**
      * Thrown when a syntax error occurs while parsing a syntax descriptor file
      */
     public static class SyntaxFileException extends IOException {
 
         public SyntaxFileException(String msg) {
             super(msg);
         }
     }
 }
