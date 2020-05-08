 package net.abesto.jstuki.io;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.EnumMap;
 import net.abesto.jstuki.io.JStukiIOException.IncompleteTextSyntaxException;
 import net.abesto.jstuki.io.JStukiIOException.SyntaxFileException;
 
 /**
  * Describes a set of templates, that define a syntax for text output
  * Examples: pseudo-code, python
  *
  * @author Nagy Zoltán (abesto0@gmail.com)
  */
 public class TextSyntax {
 
     /**
      * The text templates used
      */
     public enum Template {
         INDENT, PROCEDURE, ENDPROCEDURE, LOOP, ENDLOOP, IF, ELSEIF, ELSE, ENDIF, NULL
     };
     protected static EnumMap<TextSyntax.Template, String> templates;
 
     static {
         templates = new EnumMap<TextSyntax.Template, String>(TextSyntax.Template.class);
     }
 
     /**
      * Load syntax description from file
      *
      * @param file The file to use
      * @throws FileNotFoundException
      * @throws IOException
      * @throws net.abesto.jstuki.io.JStukiIOException.SyntaxFileException
      * @throws net.abesto.jstuki.io.JStukiIOException.IncompleteTextSyntaxException
      */
     public void load(File file) throws FileNotFoundException, IOException, SyntaxFileException, IncompleteTextSyntaxException {
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(file));
             String line = null;
             while ((line = reader.readLine()) != null) {
                 parse(line);
             }
             checkComplete();
         } catch (JStukiIOException.IncompleteTextSyntaxException e) {
             e.setF(file);
             throw e;
         } finally {
             if (reader != null) {
                 reader.close();
             }
         }
     }
 
     /**
      * Check whether all the templates defined in @ref TextSyntax.Template are
      * assigned a template string.
      *
      * @throws net.abesto.jstuki.io.JStukiIOException.IncompleteTextSyntaxException if not
      */
     protected void checkComplete() throws IncompleteTextSyntaxException {
         for (Template t : Template.values()) {
             if (getTemplate(t) == null) {
                 throw new JStukiIOException.IncompleteTextSyntaxException(t);
             }
         }
     }
 
     public String getTemplate(Template t) {
         return templates.get(t);
     }
 
     protected void setTemplate(Template t, String s) {
         templates.put(t, s);
     }
 
     public void parse(String line) throws SyntaxFileException {
         if (line.trim().equals("")) {
             return;
         }
         line = line.replaceAll("\\\\n", "\n");
         for (Template t : TextSyntax.Template.values()) {
             if (line.startsWith(t.toString())) {
                 String s = line.substring(t.toString().length());
                 // Template can have empty-string value, the separating space is not required there
                 if (s.length() > 0) {
                     s = s.substring(1);
                 }
                 setTemplate(t, s);
                 return;
             }
         }
         throw new JStukiIOException.SyntaxFileException(line);
     }
 }
