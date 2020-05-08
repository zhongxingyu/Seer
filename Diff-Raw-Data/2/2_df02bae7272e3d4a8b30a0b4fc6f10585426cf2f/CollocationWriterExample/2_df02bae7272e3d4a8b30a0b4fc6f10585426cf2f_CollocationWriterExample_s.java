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
 package com.github.fhirschmann.clozegen.lib.examples;
 
 import com.github.fhirschmann.clozegen.lib.components.CollocationWriter;
 import com.github.fhirschmann.clozegen.lib.constraints.resources.PrepositionConstraintResource;
 import com.github.fhirschmann.clozegen.lib.pipeline.Pipeline;
 import de.tudarmstadt.ukp.dkpro.core.api.resources.DKProContext;
 import de.tudarmstadt.ukp.dkpro.core.io.imscwb.ImsCwbReader;
 import de.tudarmstadt.ukp.dkpro.teaching.corpus.BrownCorpusReader;
 import org.apache.uima.analysis_engine.AnalysisEngineDescription;
 import org.apache.uima.collection.CollectionReader;
 import org.uimafit.factory.CollectionReaderFactory;
 import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
 import static org.uimafit.factory.ExternalResourceFactory.createExternalResourceDescription;
 
 /**
  * This example demonstrates the usage of {@link CollocationWriterExample}.
  *
  * <p>
  * The brown_tei corpus is expected in $DKPRO_HOME.
  * </p>
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class CollocationWriterExample {
     public static void main(String[] args) throws Exception {
         Pipeline pipeline = new Pipeline();
 
         CollectionReader cr = CollectionReaderFactory.createCollectionReader(
                 BrownCorpusReader.class,
                 BrownCorpusReader.PARAM_PATH,
                 DKProContext.getContext().getWorkspace("brown_tei").getAbsolutePath(),
                 BrownCorpusReader.PARAM_PATTERNS, new String[] {"[+]*.xml"});
 
         CollectionReader wacky = CollectionReaderFactory.createCollectionReader(
                 ImsCwbReader.class,
                 BrownCorpusReader.PARAM_PATH,
                 DKProContext.getContext().getWorkspace("wacky/uk/x").getAbsolutePath(),
                 ImsCwbReader.PARAM_TAGGER_TAGSET, "en",
                 ImsCwbReader.PARAM_PATTERNS, new String[] {"[+]*.xml.gz"});
 
 
         AnalysisEngineDescription trigrams = createPrimitiveDescription(
                 CollocationWriter.class,
                 CollocationWriter.CONSTRAINT_KEY,
                 createExternalResourceDescription(PrepositionConstraintResource.class),
                 CollocationWriter.PARAM_OUTPUT_PATH, "target/test.txt");
 
         pipeline.addStep(trigrams);
        pipeline.run(wacky);
     }
 
 }
