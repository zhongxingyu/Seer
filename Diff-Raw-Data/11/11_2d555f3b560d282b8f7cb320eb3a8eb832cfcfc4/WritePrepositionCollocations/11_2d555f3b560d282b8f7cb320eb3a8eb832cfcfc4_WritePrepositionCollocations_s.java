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
 package com.github.fhirschmann.clozegen.lib.tools;
 
 import com.github.fhirschmann.clozegen.lib.io.CollocationsExtractor;
import com.github.fhirschmann.clozegen.lib.io.NewCollocationsExtractor;
 import com.github.fhirschmann.clozegen.lib.pipeline.Pipeline;
 import de.tudarmstadt.ukp.dkpro.teaching.corpus.BrownCorpusReader;
 import java.io.IOException;
 import org.apache.uima.UIMAException;
 import org.apache.uima.collection.CollectionReader;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.uimafit.factory.CollectionReaderFactory;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class WritePrepositionCollocations {
     public static void main(String[] args)
             throws ResourceInitializationException, UIMAException, IOException {
         Pipeline pipeline = new Pipeline();
 
         CollectionReader cr = CollectionReaderFactory.createCollectionReader(
                 BrownCorpusReader.class,
                 BrownCorpusReader.PARAM_PATH, "src/main/resources/corpora/brown_tei",
                 BrownCorpusReader.PARAM_PATTERNS, new String[] {"[+]*.xml"});
 
        pipeline.addStep(NewCollocationsExtractor.class);
         pipeline.run(cr);
     }
 
 }
