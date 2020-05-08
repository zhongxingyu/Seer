 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009  Trejkaz, Hex Project
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.trypticon.hex.gui.notebook;
 
 import java.net.URL;
 import java.io.StringWriter;
 import java.io.StringReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 
 import org.junit.Test;
 import org.junit.Ignore;
 import static org.junit.Assert.assertEquals;
 
 import org.trypticon.hex.anno.AnnotationCollection;
 import org.trypticon.hex.anno.SimpleMutableAnnotation;
import org.trypticon.hex.anno.NullInterpretor;
 import org.trypticon.hex.anno.primitive.PrimitiveInterpretors;
 
import com.sun.xml.internal.bind.v2.TODO;

 /**
  * Tests for {@link NotebookStorage}.
  *
  * @author trejkaz
  */
 public class TestNotebookStorage {
     private NotebookStorage storage = new NotebookStorage();
 
     @Test
     public void testRoundTrip() throws Exception {
         Notebook notebook = new Notebook(new URL("http://example.com/biscuits.dat.xml"));
         AnnotationCollection annotations = notebook.getAnnotations();
         annotations.add(new SimpleMutableAnnotation(5, new NullInterpretor(4), "Test"));
         annotations.add(new SimpleMutableAnnotation(9, PrimitiveInterpretors.UINT32_LE, null));
 
         StringWriter writer = new StringWriter();
         storage.write(notebook, writer);
 
         String content = writer.toString();
 
         //System.out.println(content);
 
         StringReader reader = new StringReader(content);
         Notebook churned = storage.read(reader);
 
         assertEquals("Wrong binary location", notebook.getBinaryLocation(), churned.getBinaryLocation());
         assertEquals("Wrong annotations", notebook.getAnnotations().getAll(), churned.getAnnotations().getAll());
     }
 
     @Test
     public void testFileStorage() throws Exception {
         File tmpFile = File.createTempFile("temp", ".xml");
         try {
             URL tmpFileURL = tmpFile.toURI().toURL();
             Notebook notebook = new Notebook(new URL("http://example.com/biscuits.dat.xml"));
 
             storage.write(notebook, tmpFileURL);
 
             Notebook churned = storage.read(tmpFileURL);
             assertEquals("Wrong binary location", notebook.getBinaryLocation(), churned.getBinaryLocation());
         } finally {
             if (!tmpFile.delete()) {
                 System.err.println("Error deleting temp file: " + tmpFile);
             }
         }
     }
 
     @Test(expected=IOException.class)
     public void testIOExceptionOnReading() throws Exception {
         storage.read(new BrokenReader());
     }
 
     @Test(expected=IOException.class)
     @Ignore("requires a fix to JvYAML: https://jvyaml.dev.java.net/issues/show_bug.cgi?id=15")
     public void testIOExceptionOnWriting() throws Exception {
         Notebook notebook = new Notebook(new URL("http://example.com/biscuits.dat.xml"));
         storage.write(notebook, new BrokenWriter());
     }
 
     private static class BrokenReader extends Reader {
         public int read(char[] chars, int i, int i1) throws IOException {
             throw new IOException("Broken");
         }
 
         public void close() throws IOException {
             throw new IOException("Broken");
         }
     }
 
     private static class BrokenWriter extends Writer {
         public void write(char[] chars, int i, int i1) throws IOException {
             throw new IOException("Broken");
         }
 
         public void flush() throws IOException {
             throw new IOException("Broken");
         }
 
         public void close() throws IOException {
             throw new IOException("Broken");
         }
     }
 }
