 /**
  * HomologySearchFactory.java
  *
  * 2011.10.10
  *
  * This file is part of the CheMet library
  *
  * The CheMet library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CheMet is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.ebi.mdk.tool.task.homology;
 
 import org.apache.log4j.Logger;
 import org.biojava3.core.sequence.ProteinSequence;
 import org.biojava3.core.sequence.io.FastaWriterHelper;
 import uk.ac.ebi.caf.utility.preference.type.FilePreference;
 import uk.ac.ebi.mdk.db.HomologyDatabaseManager;
 import uk.ac.ebi.mdk.domain.DomainPreferences;
 import uk.ac.ebi.mdk.domain.annotation.task.ExecutableParameter;
 import uk.ac.ebi.mdk.domain.annotation.task.FileParameter;
 import uk.ac.ebi.mdk.domain.annotation.task.Parameter;
 import uk.ac.ebi.mdk.domain.entity.GeneProduct;
 import uk.ac.ebi.mdk.domain.entity.ProteinProduct;
 import uk.ac.ebi.mdk.domain.identifier.basic.TaskIdentifier;
 import uk.ac.ebi.mdk.tool.task.RunnableTask;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.InvalidParameterException;
 import java.util.*;
 
 /**
  * @author johnmay
  * @author $Author$ (this version)
  * @version $Rev$ : Last Changed $Date$
  * @name HomologySearchFactory - 2011.10.10 <br>
  * Class description
  */
 public class HomologySearchFactory {
 
     private static final Logger LOGGER = Logger.getLogger(HomologySearchFactory.class);
 
     private HomologySearchFactory() {
     }
 
     public static HomologySearchFactory getInstance() {
         return HomologySearchFactoryHolder.INSTANCE;
     }
 
     private static class HomologySearchFactoryHolder {
 
         private static HomologySearchFactory INSTANCE = new HomologySearchFactory();
     }
 
     public RunnableTask getTabularSwissProtBLASTP(Collection<GeneProduct> products,
                                                   double expected,
                                                   int cpu,
                                                   int results) throws IOException, Exception {
         // should check for swiss prot and throw an expection if not
         return getBlastP(products, HomologyDatabaseManager.getInstance().getPath("SwissProt"), expected, cpu, results, 6);
     }
 
     /**
      * Returns a blastp task with output format '8'
      *
      * @param products
      * @param database
      * @param expected
      * @param cpu
      * @param results
      *
      * @return
      *
      * @throws IOException
      * @throws Exception
      */
     public RunnableTask getTabularBLASTP(Collection<GeneProduct> products,
                                          File database,
                                          double expected,
                                          int cpu,
                                          int results) throws IOException, Exception {
         return getBlastP(products, database, expected, cpu, results, 6);
     }
 
     /**
      * Builds a runnable task to perform a sequence homology search using Blast
      *
      * @param database The database to search against
      * @param expected The expected value to filter for
      * @param cpu      The number of cpus to allow
      * @param results  the number of results to allow
      * @param format   the blast format 7=xml, 8=tabular....
      *
      * @return A runnable task to perform the blast search externally
      */
     public RunnableTask getBlastP(Collection<GeneProduct> products,
                                   File database,
                                   double expected,
                                   int cpu,
                                   int results,
                                   int format) throws IOException, Exception {
 
         FilePreference blastpath = DomainPreferences.getInstance().getPreference("BLASTP_PATH");
         String blastp = blastpath.get().getAbsolutePath();
 
         if (blastp == null) {
             throw new InvalidParameterException("No path found for blastp, please configure the user preference");
         }
 
 
         Map<String, GeneProduct> accessionMap = new HashMap<String, GeneProduct>(); // we need an id map incase some names change
 
         Collection<ProteinSequence> sequences = new ArrayList<ProteinSequence>();
 
         for (GeneProduct p : products) {
             if (p instanceof ProteinProduct) {
                 ProteinProduct protein = (ProteinProduct) p;
                 if (protein.getSequences().size() > 1) {
                     LOGGER.info("Protein with multiple sequences");
                 }
 
                 ProteinSequence sequence = protein.getSequences().iterator().next();
                 sequence.setOriginalHeader(protein.getAccession()); // ensure the output has matching ids
                 if (accessionMap.containsKey(protein.getAccession())) {
                    throw new InvalidParameterException("Clashing protein accessions: " + protein.getAccession() + " sequence will not be used in search");
                 } else {
                     accessionMap.put(protein.getAccession(), protein);
                     sequences.add(sequence);
                 }
             }
         }
 
         File input = File.createTempFile("mnb-blast-input-", ".fasta");
 
         FastaWriterHelper.writeProteinSequence(input, sequences);
 
         RunnableTask task = new BLASTHomologySearch(accessionMap, new TaskIdentifier(UUID.randomUUID().toString()));
 
         // executable parameter
         task.addAnnotation(new ExecutableParameter("BLASTP", "BlastP executable", new File(blastp)));
 
         task.addAnnotation(new Parameter("Database", "The database to search", "-db", database.getAbsolutePath()));
         task.addAnnotation(new Parameter("Expected value", "Results above the specified threshold will be ignored", "-evalue", String.format("%.1e", expected)));
         task.addAnnotation(new Parameter("Threads", "The number of threads to split the task accross", "-num_threads", Integer.toString(cpu)));
         task.addAnnotation(new Parameter("Number of Descriptions", "The maximum number of descriptions to display", "-num_descriptions", Integer.toString(results)));
         task.addAnnotation(new Parameter("Number of Alignments", "The maximum number of alignments to display", "-num_alignments", Integer.toString(results)));
         task.addAnnotation(new Parameter("Output Format", "Format of blast output (4=tsv, 5=xml)", "-outfmt", Integer.toString(format)));
 
         task.addAnnotation(new FileParameter("Query File", "The query file", "-query", input));
         task.addAnnotation(new FileParameter("Output File", "The output file", "-out", File.createTempFile("blast", "")));
 
         return task;
     }
 }
