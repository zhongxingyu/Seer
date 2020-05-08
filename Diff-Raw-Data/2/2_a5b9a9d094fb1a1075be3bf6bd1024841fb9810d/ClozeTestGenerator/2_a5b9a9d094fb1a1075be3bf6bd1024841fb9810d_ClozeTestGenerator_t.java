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
 package com.github.fhirschmann.clozegen.lib;
 
 import static com.google.common.base.Objects.firstNonNull;
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import org.apache.uima.UIMAException;
 import org.apache.uima.analysis_engine.AnalysisEngineDescription;
 import org.apache.uima.collection.CollectionReader;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.resource.ResourceInitializationException;
 
 import com.github.fhirschmann.clozegen.lib.components.GapAnnotator;
 import com.github.fhirschmann.clozegen.lib.pipeline.Pipeline;
 import com.github.fhirschmann.clozegen.lib.pipeline.PipelineFactory;
 import com.github.fhirschmann.clozegen.lib.register.AnnotatorRegister;
 import com.github.fhirschmann.clozegen.lib.register.Registers;
 import com.google.common.base.Charsets;
 import com.google.common.base.Joiner;
 import com.google.common.base.Objects;
 import com.google.common.base.Objects.ToStringHelper;
 import com.google.common.io.Files;
 
 /**
  * Cloze Test Generator.
  *
  * <p>
  * This is the main entry point for the cloze test generation process. You should
  * never call one of the {@code run} methods twice.
  * </p>
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class ClozeTestGenerator {
     /**
      * The pipeline.
      */
     private Pipeline pipeline;
 
     /**
      * Indicates whether this instance of {@link ClozeTestGenerator} is clean.
      * An instance is not clean if one of the {@code run} methods has been
      * called in the past.
      */
     private boolean clean;
 
     /**
      * The logger for this class.
      */
     public static final Logger LOGGER = Logger.
             getLogger(ClozeTestGenerator.class.getName());
 
     /**
      * Creates a new {@link ClozeTestGenerator} instance.
      */
     public ClozeTestGenerator() {
         clean = true;
     }
 
     /**
      * Runs the cloze test generation process.
      *
      * <p>
      * The pipeline will start at the reader associated with {@code input}
      * and write to {@code output}.
      * </p>
      *
      * @param input the input file
      * @param output the output file
      * @param languageCode the language
      * @throws UIMAException on UIMA errors
      * @throws IOException on errors reading or writing files
      */
     public void run(final URL input, final URL output, final String languageCode)
             throws UIMAException, IOException {
         checkArgument(clean, "You cannot call run twice.");
         checkNotNull(input);
         checkNotNull(output);
         CollectionReader reader = Registers.reader().
                 getReaderForFile(input, languageCode);
         run(reader, output);
     }
 
     /**
      * Runs the cloze test generation process and returns
      * the result (intermediate format).
      *
      * <p>
      * The pipeline will start at the reader associated with {@code input}
      * and write to {@code output}.
      * </p>
      *
      * @param input the input file
      * @param languageCode the language
      * @return the intermediate format
      * @throws UIMAException on UIMA errors
      * @throws IOException on errors reading or writing files
      */
     public String run(final URL input, final String languageCode)
             throws IOException, UIMAException {
         checkArgument(clean, "You cannot call run twice.");
         File file = File.createTempFile("clozegen", ".clz");
         run(input, file.toURI().toURL(), languageCode);
         List<String> lines = Files.readLines(file, Charsets.UTF_8);
 
         return Joiner.on(System.getProperty("line.separator")).join(lines);
     }
 
     /**
      * Runs the cloze test generation process.
      *
      * <p>
      * The pipeline will start at the reader associated with {@code input}
      * and write to {@code output}.
      * </p>
      *
      * @param reader the collection reader
      * @param output the output file
      * @throws UIMAException on UIMA errors
      * @throws IOException on errors reading or writing files
      * @throws ResourceInitializationException on errors during initialization
      */
     public void run(final CollectionReader reader, final URL output)
             throws ResourceInitializationException, UIMAException, IOException {
         checkArgument(clean, "You cannot call run twice.");
         AnalysisEngineDescription writer = Registers.writer().getWriterFor(output);
         getPipeline().add(writer);
         run(reader);
     }
 
     /**
      * Runs the cloze test generation process.
      *
      * <p>
      * The pipeline will start at the given {@link JCas} and write to {@code output}.
      * </p>
      *
      * @param jcas JCas the {@link JCas}
      * @param output the output file
      * @throws UIMAException on UIMA errors
      * @throws IOException on errors reading or writing files
      * @throws ResourceInitializationException on errors during initialization
      */
     public void run(final JCas jcas, final URL output)
             throws ResourceInitializationException, UIMAException, IOException {
         checkArgument(clean, "You cannot call run twice.");
         AnalysisEngineDescription writer = Registers.writer().getWriterFor(output);
         getPipeline().add(writer);
         run(jcas);
     }
 
     /**
      * Runs the cloze test generation process.
      *
      * <p>
      * The pipeline will start at the given CAS.
      * </p>
      *
      * @param jCas the CAS to start the pipeline off with
      * @throws UIMAException on errors produced by UIMA
      * @throws IOException on errors when reading a file
      */
     public void run(final JCas jCas) throws UIMAException, IOException {
         getPipeline().run(jCas);
     }
 
     /**
      * Runs the cloze test generation process.
      *
      * <p>
      * The pipeline will start at the given CollectionReader.
      * </p>
      *
      * @param reader the Collection Reader to start the pipeline off with
      * @throws UIMAException when UIMA errors occur
      * @throws IOException when errors occur while reading from a file
      */
     public void run(final CollectionReader reader) throws UIMAException, IOException {
         getPipeline().run(reader);
     }
 
     /**
      * Activate the given {@code generators}.
      *
      * <p>
      * The argument to this function is a map with entries which map
      * from an identifier in {@link AnnotatorRegister} to an Integer
      * (the answer count).
      * </p>
      *
      * @param generators the generators to activate
      * @throws ResourceInitializationException on errors during initialization
      */
     public void activate(final Map<String, Integer> generators)
             throws ResourceInitializationException {
         for (Entry<String, Integer> entry : generators.entrySet()) {
             activate(entry.getKey(), entry.getValue());
         }
     }
 
     /**
      * Activate the given {@code generator}.
      *
      * @param generatorIdentifier the generator to activate
      * @param answerCount the answer options to generate
      * @throws ResourceInitializationException on errors during initialization
      */
     public void activate(final String generatorIdentifier, final int answerCount)
             throws ResourceInitializationException {
        checkArgument(Registers.annotator().containsIdentifier(generatorIdentifier),
                "Unknown Generator: " + generatorIdentifier);
         getPipeline().add(Registers.annotator().
                 get(generatorIdentifier).getDescription(
                 GapAnnotator.PARAM_ANSWER_COUNT, answerCount));
     }
 
     /**
      * Returns the pipeline of the generation process. You can safely add
      * your own analysis engines to the pipeline.
      *
      * <p>
      * If no register has been set, a new default one will be created.
      * </p>
      *
      * @return the pipeline
      * @throws ResourceInitializationException on errors during initialization
      */
     public Pipeline getPipeline() throws ResourceInitializationException {
         pipeline = firstNonNull(pipeline, PipelineFactory.createDefaultPipeline());
         return pipeline;
     }
 
     /**
      * @param pipeline the pipeline to set
      */
     public void setPipeline(final Pipeline pipeline) {
         this.pipeline = pipeline;
     }
 
     @Override
     public String toString() {
         final ToStringHelper str = Objects.toStringHelper(this);
         str.add("pipeline", pipeline.toString());
 
         return str.toString();
     }
 }
