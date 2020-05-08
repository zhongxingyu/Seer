 /*
  * Copyright (c) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.github.fhirschmann.clozegen.gui;
 
 import com.github.fhirschmann.clozegen.lib.register.ReaderRegisterEntry;
 import com.github.fhirschmann.clozegen.lib.register.Registers;
 import com.github.fhirschmann.clozegen.lib.register.WriterRegisterEntry;
 import java.io.File;
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileFilter;
 
 /**
  * {@link FileFilter} utility functions.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public final class FileFilterUtils {
 
     /**
      * Utility class.
      */
     private FileFilterUtils() {
     }
 
     /**
      * Sets the accepted files for a given {@code fileChooser} to the ones
      * supported by {@link Registers#reader()}.
      *
      * @param fileChooser the {@link JFileChooser}
      */
     public static void setInputFileFilter(final JFileChooser fileChooser) {
         fileChooser.setAcceptAllFileFilterUsed(false);
         for (final ReaderRegisterEntry entry : Registers.reader()) {
             fileChooser.addChoosableFileFilter(new FileFilter() {
                 @Override
                 public boolean accept(final File file) {
                     return (file.isDirectory()
                             || file.getName().endsWith("." + entry.getIdentifier()));
                 }
 
                 @Override
                 public String getDescription() {
                     return entry.getName() + " (*." + entry.getIdentifier() + ")";
                 }
             });
         }
     }
 
     /**
      * Sets the accepted files for a given {@code fileChooser} to the ones
      * supported by {@link Registers#writer()}.
      *
      * @param fileChooser the {@link JFileChooser}
      */
     public static void setOutputFileFilter(final JFileChooser fileChooser) {
         fileChooser.setAcceptAllFileFilterUsed(false);
         for (final WriterRegisterEntry entry : Registers.writer()) {
             fileChooser.addChoosableFileFilter(new FileFilter() {
                 @Override
                 public boolean accept(final File file) {
                     return (file.isDirectory()
                             || file.getName().endsWith("." + entry.getIdentifier()));
                 }
 
                 @Override
                 public String getDescription() {
                     return entry.getName() + " (*." + entry.getIdentifier() + ")";
                 }
             });
         }
     }
 }
