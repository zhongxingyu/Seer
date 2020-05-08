 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib.multiset;
 
 import com.google.common.collect.ImmutableMultiset;
 import com.google.common.collect.Multiset;
 import com.google.common.collect.Multisets;
 import java.io.*;
 
 /**
  * Utility functions to write {@link Multiset}s.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public final class WriteMultisets {
     /** Constructor in utility class should not be called. */
     private WriteMultisets() {
     }
 
     /**
      * Writes a multiset to a file. The elements will be written to a tab-separated
      * file with the string representation of the elements on the left-hand side and
      * the corresponding counts on the right-hand side.
      *
      * @param multiset the multiset to write from
      * @param minFrequency the minimum frequency a word must have in order to be written
      * @param file the file to write to
      * @throws IOException on errors opening/writing to the file
      */
     public static void writeMultiSet(final Multiset<String> multiset,
             final int minFrequency, final File file)
             throws IOException {
         final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
 
         for (Multiset.Entry<String> entry : multiset.entrySet()) {
             if (entry.getCount() >= minFrequency) {
                 writeMultiSetLine(bufferedWriter, entry.getElement().toString(),
                         entry.getCount());
             }
         }
         bufferedWriter.close();
     }
 
     /**
      * Writes a multiset to a file. The elements will be written to a tab-separated
      * file with the string representation of the elements on the left-hand side and
      * the corresponding counts on the right-hand side.
      *
      * @param multiset the multiset to write from
      * @param file the file to write to
      * @throws IOException on errors opening/writing to the file
      */
     public static void writeMultiSet(final Multiset<String> multiset, final File file)
             throws IOException {
         writeMultiSet(multiset, 1, file);
     }
 
 
     /**
      * Writes a line (entry and frequency) to a {@link Writer}. Does not close
      * the writer.
      *
      * @param writer the writer to write to
      * @param str the string to write
      * @param count the frequency of the string
      * @throws IOException on errors writing to file
      */
     public static void writeMultiSetLine(final Writer writer,
             final String str, final int count) throws IOException {
             writer.write(String.format("%s\t%d%n", str, count));
     }
 
     /**
      * Sorts a Multiset by its counts before writing it to a file using
      * {@link MultisetUtils#writeMultiSet}.
      *
      * @param multiset the multiset to write
      * @param file the file to write to
      * @throws IOException on errors opening/writing to the file
      */
     public static void writeSortedMultiSet(final Multiset<String> multiset,
            final File file) throws IOException {
         final ImmutableMultiset<String> im = Multisets.copyHighestCountFirst(multiset);
        writeMultiSet(im, file);
     }
 }
