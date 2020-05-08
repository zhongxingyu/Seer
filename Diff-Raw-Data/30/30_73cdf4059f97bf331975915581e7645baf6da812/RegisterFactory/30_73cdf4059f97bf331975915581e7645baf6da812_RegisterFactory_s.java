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
 import com.github.fhirschmann.clozegen.lib.constraints.resources.PrepositionConstraintResource;
 import com.github.fhirschmann.clozegen.lib.imf.IntermediateFormatWriter;
 import com.google.common.collect.Sets;
 import de.tudarmstadt.ukp.dkpro.core.io.pdf.PdfReader;
 import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
 import java.util.logging.Logger;
 import org.apache.uima.resource.ResourceInitializationException;
 import static org.uimafit.factory.ExternalResourceFactory.createExternalResourceDescription;
 
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
     public static AnnotatorRegister createDefaultAnnotatorRegister()
             throws ResourceInitializationException {
         AnnotatorRegister register = new AnnotatorRegister();
 
         AnnotatorRegisterEntry entry =
                    new AnnotatorRegisterEntry("prepositions",
                 GapAnnotator.class,
                 GapAnnotator.ADAPTER_KEY,
                 createExternalResourceDescription(
                 CollocationAdapter.class,
                 CollocationAdapter.PARAM_PATH,
                 "frequencies/en/prepositions/trigrams.txt"),
                 GapAnnotator.CONSTRAINT_KEY,
                 createExternalResourceDescription(
                 PrepositionConstraintResource.class));
 
        entry.setName("Preposition Gap Generator");
         entry.setSupportedLanguages(Sets.newHashSet("en"));
         register.add(entry);
 
         return register;
     }
 
     /**
      * Creates a new {@link WriterRegister} prefilled with known descriptions
      * for exporting cloze tests.
      *
      * @return a new {@link WriterRegister}
      */
     public static WriterRegister createDefaultWriterRegister() {
         WriterRegister register = new WriterRegister();
 
         WriterRegisterEntry entry = new WriterRegisterEntry(
                 "clz", IntermediateFormatWriter.class);
         entry.setName("Intermediate Format Writer");
         register.add(entry);
 
         return register;
     }
 
     /**
      * Creates a new {@link ReaderRegister} prefilled with known descriptions
      * for creating input readers.
      *
      * @return a new reader register
      */
     public static ReaderRegister createDefaultReaderRegister() {
         ReaderRegister register = new ReaderRegister();
 
         ReaderRegisterEntry txt = new ReaderRegisterEntry(TextReader.class);
         txt.setName("Plain-Text Reader");
         register.put("txt", txt);
         register.put("text", txt);
 
         ReaderRegisterEntry pdf = new ReaderRegisterEntry(PdfReader.class);
         pdf.setName("PDF Reader");
         register.put("pdf", pdf);
 
         return register;
     }
 }
