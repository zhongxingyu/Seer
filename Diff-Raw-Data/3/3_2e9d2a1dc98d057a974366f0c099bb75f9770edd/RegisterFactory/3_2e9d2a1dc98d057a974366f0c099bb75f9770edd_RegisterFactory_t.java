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
 package com.github.fhirschmann.clozegen.lib.register;
 
 import com.github.fhirschmann.clozegen.lib.components.GapAnnotator;
 import com.github.fhirschmann.clozegen.lib.adapters.CollocationAdapter;
 import com.google.common.collect.Sets;
 import de.tudarmstadt.ukp.dkpro.core.io.pdf.PdfReader;
 import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.uima.collection.CollectionReader;
 import org.apache.uima.resource.ResourceInitializationException;
 import static org.uimafit.factory.ExternalResourceFactory.createExternalResourceDescription;
 import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkArgument;
 import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
 import java.io.File;
 import java.net.URI;
 import java.util.Arrays;
 import org.apache.uima.collection.CollectionReader_ImplBase;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public final class RegisterFactory {
     public static final Logger LOGGER = Logger.getLogger(RegisterFactory.class.getName());
 
     /** Utility class cannot be called. */
     private RegisterFactory() {
     }
 
     /**
      * Creates a new {@link DescriptionRegister} prefilled with known descriptions
      * for gap generation.
      *
      * @return a new {@link DescriptionRegister}
      * @throws ResourceInitializationException on errors
      */
     public static DescriptionRegister createDefaultDescriptionRegister()
             throws ResourceInitializationException {
         DescriptionRegister register = new DescriptionRegister();
 
         DescriptionRegisterEntry entry = new DescriptionRegisterEntry("prepositions",
                 GapAnnotator.class,
                 GapAnnotator.ADAPTER_KEY,
                 createExternalResourceDescription(
                 CollocationAdapter.class,
                CollocationAdapter.PARAM_PATH,
                "frequencies/en/prepositions/trigrams.txt"));
 
         entry.setName("Preposition Gap Generator");
         entry.setSupportedLanguages(Sets.newHashSet("en"));
         register.add(entry);
 
         return register;
     }
 
     /**
      * Convenience method for creating {@link CollectionReader} patterns which
      * should work on only one input file.
      *
      * @param file the file in question
      * @return a new pattern
      */
     public static String[] createPatterns(final File file) {
         String[] patterns = new String[] {String.format("[+]%s",
                 checkNotNull(file.getName()))};
         return patterns;
     }
 
     /**
      * Creates a new standard {@link CollectionReader} based upon the input
      * parameters.
      *
      * @param clazz the collection reader class
      * @param file the input file
      * @param languageCode the language code
      * @return a new collection reader
      * @throws ResourceInitializationException on errors during initialization
      */
     public static CollectionReader createDefaultReader(
             final Class<? extends CollectionReader_ImplBase> clazz,
             final File file, final String languageCode)
             throws ResourceInitializationException {
         return createCollectionReader(
             clazz,
             ResourceCollectionReaderBase.PARAM_LANGUAGE, languageCode,
             ResourceCollectionReaderBase.PARAM_PATH, file.getParent(),
             ResourceCollectionReaderBase.PARAM_PATTERNS, createPatterns(file));
     }
 
     /**
      * Creates a new standard {@link ReaderRegisterEntry}.
      *
      * @param clazz the collection reader class
      * @return a new register entry
      */
     public static ReaderRegisterEntry createDefaultReaderRegisterEntry(
             final Class<? extends CollectionReader_ImplBase> clazz) {
         return new ReaderRegisterEntry() {
             @Override
             public CollectionReader get(final File file, final String languageCode) {
                 try {
                     return createDefaultReader(clazz, file, languageCode);
                 } catch (ResourceInitializationException ex) {
                     LOGGER.log(Level.SEVERE, null, ex);
                     return null;
                 }
             }
         };
     }
 
     public static ReaderRegister createDefaultReaderRegister() {
         ReaderRegister register = new ReaderRegister();
 
         ReaderRegisterEntry txt = createDefaultReaderRegisterEntry(TextReader.class);
         register.put("txt", txt);
         register.put("text", txt);
 
         ReaderRegisterEntry pdf = createDefaultReaderRegisterEntry(PdfReader.class);
         register.put("pdf", pdf);
 
         return register;
     }
 }
