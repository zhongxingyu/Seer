 /*
  * Copyright 2008-2010 Microarray Informatics Team, EMBL-European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  *
  * For further details of the Gene Expression Atlas project, including source code,
  * downloads and documentation, please see:
  *
  * http://gxa.github.com/gxa
  */
 
 package uk.ac.ebi.gxa.index.builder.service;
 
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.common.SolrInputDocument;
 import uk.ac.ebi.gxa.efo.Efo;
 import uk.ac.ebi.gxa.efo.EfoTerm;
 import uk.ac.ebi.gxa.index.GeneExpressionAnalyticsTable;
 import uk.ac.ebi.gxa.index.builder.IndexAllCommand;
 import uk.ac.ebi.gxa.index.builder.IndexBuilderException;
 import uk.ac.ebi.gxa.index.builder.UpdateIndexForExperimentCommand;
 import uk.ac.ebi.gxa.properties.AtlasProperties;
 import uk.ac.ebi.gxa.utils.ChunkedSublistIterator;
 import uk.ac.ebi.gxa.utils.EscapeUtil;
 import uk.ac.ebi.microarray.atlas.model.*;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * An {@link IndexBuilderService} that generates index documents from the genes in the Atlas database, and enriches the
  * data with expression values, links to EFO and other useful measurements.
  * <p/>
  * This is a heavily modified version of an original class first adapted to Atlas purposes by Pavel Kurnosov.
  * <p/>
  * Note that this implementation does NOT support updates - regardless of whether the update flag is set to true, this
  * will rebuild the index every time.
  *
  * @author Tony Burdett
  */
 public class GeneAtlasIndexBuilderService extends IndexBuilderService {
     private Map<String, Collection<String>> ontomap =
             new HashMap<String, Collection<String>>();
     private Efo efo;
     private AtlasProperties atlasProperties;
 
     public void setAtlasProperties(AtlasProperties atlasProperties) {
         this.atlasProperties = atlasProperties;
     }
 
     public void setEfo(Efo efo) {
         this.efo = efo;
     }
 
     @Override
     public void processCommand(IndexAllCommand indexAll, ProgressUpdater progressUpdater) throws IndexBuilderException {
         super.processCommand(indexAll, progressUpdater);    //To change body of overridden methods use File | Settings | File Templates.
 
         getLog().info("Indexing all genes...");
         indexGenes(progressUpdater, getAtlasDAO().getAllGenesFast());
     }
 
     @Override
     public void processCommand(UpdateIndexForExperimentCommand cmd, ProgressUpdater progressUpdater) throws IndexBuilderException {
         super.processCommand(cmd, progressUpdater);
 
         getLog().info("Indexing genes for experiment " + cmd.getAccession() + "...");
         indexGenes(progressUpdater, getAtlasDAO().getGenesByExperimentAccession(cmd.getAccession()));
     }
 
     private void indexGenes(final ProgressUpdater progressUpdater,
                             final List<Gene> genes) throws IndexBuilderException {
         java.util.Collections.shuffle(genes);
 
         final int total = genes.size();
         getLog().info("Found " + total + " genes to index");
 
         loadEfoMapping();
 
         final AtomicInteger processed = new AtomicInteger(0);
         final long timeStart = System.currentTimeMillis();
 
        final int fnothnum = 32; // TODO atlasProperties.getGeneAtlasIndexBuilderNumberOfThreads();
         final int chunksize = 500; // TODO atlasProperties.getGeneAtlasIndexBuilderChunksize();
         final int commitfreq = atlasProperties.getGeneAtlasIndexBuilderCommitfreq();
 
         getLog().info("Using " + fnothnum + " threads, " + chunksize + " chunk size, committing every " + commitfreq + " genes");
         ExecutorService tpool = Executors.newFixedThreadPool(fnothnum);
         List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>(genes.size());
 
         // index all genes in parallel
         for (final List<Gene> genelist : new Iterable<List<Gene>>() {
             public Iterator<List<Gene>> iterator() {
                 return new ChunkedSublistIterator<List<Gene>>(genes, chunksize);
             }
         }) {
             // for each gene, submit a new task to the executor
             tasks.add(new Callable<Boolean>() {
                 public Boolean call() throws IOException, SolrServerException {
                     try {
                         StringBuilder sblog = new StringBuilder();
                         long timeTaskStart = System.currentTimeMillis();
 
                         List<Long> geneids = new ArrayList<Long>(chunksize);
                         for (Gene gene : genelist) {
                             geneids.add(gene.getGeneID());
                         }
 
                         getAtlasDAO().getPropertiesForGenes(genelist);
                         Map<Long, List<ExpressionAnalysis>> eas = getAtlasDAO().getExpressionAnalyticsForGeneIDs(geneids);
 
                         int eascount = 0;
                         for (List<ExpressionAnalysis> easlist : eas.values())
                             eascount += easlist.size();
 
                         sblog.append("[ ").append(System.currentTimeMillis() - timeTaskStart)
                                 .append(" ] got ").append(eascount).append(" EA's for ")
                                 .append(geneids.size()).append(" genes.\n");
 
                         Iterator<Gene> geneiter = genelist.iterator();
                         List<SolrInputDocument> solrDocs = new ArrayList<SolrInputDocument>(genelist.size());
                         while (geneiter.hasNext()) {
                             final Gene gene = geneiter.next();
                             geneiter.remove();
 
                             SolrInputDocument solrInputDoc = createGeneSolrInputDocument(gene);
 
                             Set<String> designElements = new HashSet<String>();
                             for (DesignElement de : getAtlasDAO().getDesignElementsByGeneID(gene.getGeneID())) {
                                 designElements.add(de.getName());
                                 designElements.add(de.getAccession());
                             }
                             solrInputDoc.addField("property_designelement", designElements);
                             solrInputDoc.addField("properties", "designelement");
 
                             // add EFO counts for this gene
                             List<ExpressionAnalysis> eal = eas.get(gene.getGeneID());
                             if (eal == null)
                                 eal = Collections.emptyList();
                             if (eal.size() > 0) {
                                 addEfoCounts(solrInputDoc, new HashSet<ExpressionAnalysis>(eal));
                                 // finally, add the document
                                 solrDocs.add(solrInputDoc);
                             }
 
                             int processedNow = processed.incrementAndGet();
                             if (processedNow % commitfreq == 0 || processedNow == total) {
                                 long timeNow = System.currentTimeMillis();
                                 long elapsed = timeNow - timeStart;
                                 double speed = (processedNow / (elapsed / (double) commitfreq));  // (item/s)
                                 double estimated = (total - processedNow) / (speed * 60);
 
                                 getLog().info(
                                         String.format("Processed %d/%d genes %d%%, %.1f genes/sec overall, estimated %.1f min remaining",
                                                 processedNow, total, (processedNow * 100 / total), speed, estimated));
 
                                 progressUpdater.update(processedNow + "/" + total);
                             }
                         }
 
                         sblog.append("[ ").append(System.currentTimeMillis() - timeTaskStart).append(" ] adding genes to Solr index...\n");
                         getSolrServer().add(solrDocs);
                         sblog.append("[ ").append(System.currentTimeMillis() - timeTaskStart).append(" ] ... batch complete.\n");
                         getLog().info("Gene chunk done:\n" + sblog);
 
                         return true;
                     } catch (RuntimeException e) {
                         getLog().error("Runtime exception occurred: " + e.getMessage(), e);
                         return false;
                     }
                 }
             });
         }
 
         genes.clear();
 
         try {
             List<Future<Boolean>> results = tpool.invokeAll(tasks);
             Iterator<Future<Boolean>> iresults = results.iterator();
             while (iresults.hasNext()) {
                 Future<Boolean> result = iresults.next();
                 result.get();
                 iresults.remove();
             }
         } catch (InterruptedException e) {
             getLog().error("Indexing interrupted!", e);
         } catch (ExecutionException e) {
             throw new IndexBuilderException("Error in indexing!", e.getCause());
         } finally {
             // shutdown the service
             getLog().info("Gene index building tasks finished, cleaning up resources and exiting");
             tpool.shutdown();
         }
     }
 
     private void addEfoCounts(SolrInputDocument solrDoc, Iterable<ExpressionAnalysis> studies) {
         Map<String, UpDnSet> efoupdn = new HashMap<String, UpDnSet>();
         Map<String, UpDn> efvupdn = new HashMap<String, UpDn>();
         Set<Long> noexp = new HashSet<Long>();
         Set<Long> upexp = new HashSet<Long>();
         Set<Long> dnexp = new HashSet<Long>();
         Map<String, Set<String>> noefv = new HashMap<String, Set<String>>();
         Map<String, Set<String>> upefv = new HashMap<String, Set<String>>();
         Map<String, Set<String>> dnefv = new HashMap<String, Set<String>>();
 
         GeneExpressionAnalyticsTable expTable = new GeneExpressionAnalyticsTable();
 
         for (ExpressionAnalysis expressionAnalytic : studies) {
             Long experimentId = expressionAnalytic.getExperimentID();
             if (experimentId == 0) {
                 getLog().debug("Gene " + solrDoc.getField("id") + " references an experiment where " +
                         "experimentid=0, this design element will be excluded");
                 continue;
             }
 
             boolean isUp = expressionAnalytic.isUp();
             boolean isNo = expressionAnalytic.isNo();
             float pval = expressionAnalytic.getPValAdjusted();
             final String ef = expressionAnalytic.getEfName();
             final String efv = expressionAnalytic.getEfvName();
 
             Collection<String> accessions =
                     ontomap.get(experimentId + "_" + ef + "_" + efv);
 
             String efvid = EscapeUtil.encode(ef, efv); // String.valueOf(expressionAnalytic.getEfvId()); // TODO: is efvId enough?
             if (!efvupdn.containsKey(efvid)) {
                 efvupdn.put(efvid, new UpDn());
             }
             if (isNo) {
                 efvupdn.get(efvid).cno ++;
                 if (!noefv.containsKey(ef)) {
                     noefv.put(ef, new HashSet<String>());
                 }
                 noefv.get(ef).add(efv);
 
             } else if (isUp) {
                 efvupdn.get(efvid).cup++;
                 efvupdn.get(efvid).pup = Math.min(efvupdn.get(efvid).pup, pval);
                 if (!upefv.containsKey(ef)) {
                     upefv.put(ef, new HashSet<String>());
                 }
                 upefv.get(ef).add(efv);
             } else {
                 efvupdn.get(efvid).cdn++;
                 efvupdn.get(efvid).pdn = Math.min(efvupdn.get(efvid).pdn, pval);
                 if (!dnefv.containsKey(ef)) {
                     dnefv.put(ef, new HashSet<String>());
                 }
                 dnefv.get(ef).add(efv);
             }
 
             if (accessions != null) {
                 for (String acc : accessions) {
 
                     if (!efoupdn.containsKey(acc)) {
                         efoupdn.put(acc, new UpDnSet());
                     }
                     if (isNo) {
                         efoupdn.get(acc).no.add(experimentId);
                     } else if (isUp) {
                         efoupdn.get(acc).up.add(experimentId);
                         efoupdn.get(acc).minpvalUp =
                                 Math.min(efoupdn.get(acc).minpvalUp, pval);
                     } else {
                         efoupdn.get(acc).dn.add(experimentId);
                         efoupdn.get(acc).minpvalDn =
                                 Math.min(efoupdn.get(acc).minpvalDn, pval);
                     }
                 }
             }
 
             if (isUp) {
                 upexp.add(experimentId);
             } else {
                 dnexp.add(experimentId);
             }
 
             expressionAnalytic.setEfoAccessions(accessions != null ? accessions.toArray(new String[accessions.size()]) : new String[0]);
             expTable.add(expressionAnalytic);
         }
 
         solrDoc.addField("exp_info", expTable.serialize());
 
         for (String rootId : efo.getRootIds()) {
             calcChildren(rootId, efoupdn);
         }
 
         storeEfoCounts(solrDoc, efoupdn);
         storeExperimentIds(solrDoc, noexp, upexp, dnexp);
         storeEfvs(solrDoc, noefv, upefv, dnefv);
     }
 
     private void calcChildren(String currentId, Map<String, UpDnSet> efoupdn) {
         UpDnSet current = efoupdn.get(currentId);
         if (current == null) {
             current = new UpDnSet();
             efoupdn.put(currentId, current);
         } else if (current.processed) {
             return;
         }
 
         for (EfoTerm child : efo.getTermChildren(currentId)) {
             calcChildren(child.getId(), efoupdn);
             current.addChild(efoupdn.get(child.getId()));
         }
 
         current.processed = true;
     }
 
     private <T> Set<T> union(Set<T> a, Set<T> b) {
         Set<T> x = new HashSet<T>();
         if (a != null) {
             x.addAll(a);
         }
         if (b != null) {
             x.addAll(b);
         }
         return x;
     }
 
     private SolrInputDocument createGeneSolrInputDocument(final Gene gene) {
         // create a new solr document for this gene
         SolrInputDocument solrInputDoc = new SolrInputDocument();
         getLog().debug("Updating index with properties for " + gene.getIdentifier());
 
         // add the gene id field
         solrInputDoc.addField("id", gene.getGeneID());
         solrInputDoc.addField("species", gene.getSpecies());
         solrInputDoc.addField("name", gene.getName());
         solrInputDoc.addField("identifier", gene.getIdentifier());
 
         Set<String> propNames = new HashSet<String>();
         for (Property prop : gene.getProperties()) {
             String pv = prop.getValue();
             String p = prop.getName();
             if (pv == null)
                 continue;
             if (p.toLowerCase().contains("ortholog")) {
                 solrInputDoc.addField("orthologs", pv);
             } else {
                 getLog().trace("Updating index, gene property " + p + " = " + pv);
                 solrInputDoc.addField("property_" + p, pv);
                 propNames.add(p);
             }
         }
         if (!propNames.isEmpty())
             solrInputDoc.setField("properties", propNames);
 
         getLog().debug("Properties for " + gene.getIdentifier() + " updated");
 
         return solrInputDoc;
     }
 
     private void storeEfvs(SolrInputDocument solrDoc,
                            Map<String, Set<String>> noefv,
                            Map<String, Set<String>> upefv,
                            Map<String, Set<String>> dnefv) {
         /** TODO is this needed?
         for (Map.Entry<String, Set<String>> e : noefv.entrySet()) {
             for (String i : e.getValue()) {
                 solrDoc.addField("efvs_no_" + EscapeUtil.encode(e.getKey()), i);
             }
         }*/
 
         for (Map.Entry<String, Set<String>> e : upefv.entrySet()) {
             for (String i : e.getValue()) {
                 solrDoc.addField("efvs_up_" + EscapeUtil.encode(e.getKey()), i);
             }
         }
 
         for (Map.Entry<String, Set<String>> e : dnefv.entrySet()) {
             for (String i : e.getValue()) {
                 solrDoc.addField("efvs_dn_" + EscapeUtil.encode(e.getKey()), i);
             }
         }
 
         for (String factor : union(upefv.keySet(), dnefv.keySet())) {
             for (String i : union(upefv.get(factor), dnefv.get(factor))) {
                 solrDoc.addField("efvs_ud_" + EscapeUtil.encode(factor), i);
             }
         }
     }
 
     private void storeExperimentIds(SolrInputDocument solrDoc,
                                     Set<Long> noexp,
                                     Set<Long> upexp,
                                     Set<Long> dnexp) {
         for (Long i : noexp) {
             solrDoc.addField("exp_no_ids", i);
         }
         for (Long i : upexp) {
             solrDoc.addField("exp_up_ids", i);
         }
         for (Long i : dnexp) {
             solrDoc.addField("exp_dn_ids", i);
         }
 
         for (Long i : union(upexp, dnexp)) {
             solrDoc.addField("exp_ud_ids", i);
         }
     }
 
     private void storeEfoCounts(SolrInputDocument solrDoc,
                                 Map<String, UpDnSet> efoupdn) {
         for (Map.Entry<String, UpDnSet> e : efoupdn.entrySet()) {
             String accession = e.getKey();
             UpDnSet ud = e.getValue();
 
             ud.childrenUp.addAll(ud.up);
             ud.childrenDn.addAll(ud.dn);
             ud.childrenNo.addAll(ud.no);
 
             int cup = ud.childrenUp.size();
             int cdn = ud.childrenDn.size();
 
             if (cup + cdn > 0) {
                 solrDoc.addField("efos_ud", accession);
             }
         }
     }
 
 
     private void loadEfoMapping() {
         getLog().info("Fetching ontology mappings...");
 
         // we don't support enything else yet
         List<OntologyMapping> mappings = getAtlasDAO().getOntologyMappingsByOntology("EFO");
         for (OntologyMapping mapping : mappings) {
             String mapKey = mapping.getExperimentId() + "_" +
                     mapping.getProperty() + "_" +
                     mapping.getPropertyValue();
 
             if (ontomap.containsKey(mapKey)) {
                 // fetch the existing array and add this term
                 // fixme: should actually add ontology term accession
                 ontomap.get(mapKey).add(mapping.getOntologyTerm());
             } else {
                 // add a new array
                 Collection<String> values = new HashSet<String>();
                 // fixme: should actually add ontology term accession
                 values.add(mapping.getOntologyTerm());
                 ontomap.put(mapKey, values);
             }
         }
 
         getLog().info("Ontology mappings loaded");
     }
 
     private short shorten(float f) {
         f = f * 256;
         if (f > Short.MAX_VALUE) {
             return Short.MAX_VALUE;
         }
         if (f < Short.MIN_VALUE) {
             return Short.MIN_VALUE;
         }
         return (short) f;
     }
 
 
     private static class UpDnSet {
         Set<Long> up = new HashSet<Long>();
         Set<Long> dn = new HashSet<Long>();
         Set<Long> no = new HashSet<Long>();
         Set<Long> childrenUp = new HashSet<Long>();
         Set<Long> childrenDn = new HashSet<Long>();
         Set<Long> childrenNo = new HashSet<Long>();
         boolean processed = false;
         float minpvalUp = 1;
         float minpvalDn = 1;
         float minpvalChildrenUp = 1;
         float minpvalChildrenDn = 1;
 
         void addChild(UpDnSet child) {
             childrenUp.addAll(child.childrenUp);
             childrenDn.addAll(child.childrenDn);
             childrenNo.addAll(child.childrenNo);
             childrenUp.addAll(child.up);
             childrenDn.addAll(child.dn);
             childrenNo.addAll(child.no);
             minpvalChildrenDn =
                     Math.min(Math.min(minpvalChildrenDn, child.minpvalChildrenDn),
                             child.minpvalDn);
             minpvalChildrenUp =
                     Math.min(Math.min(minpvalChildrenUp, child.minpvalChildrenUp),
                             child.minpvalUp);
         }
     }
 
     private static class UpDn {
         int cup = 0;
         int cdn = 0;
         int cno = 0;
         float pup = 1;
         float pdn = 1;
     }
 
     @Override
     public void finalizeCommand(UpdateIndexForExperimentCommand updateIndexForExperimentCommand, ProgressUpdater progressUpdater) throws IndexBuilderException {
         commit(); // do not optimize
     }
 
     public String getName() {
         return "genes";
     }
 }
