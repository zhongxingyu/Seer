 package org.genedb.crawl.elasticsearch.index.gff;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.genedb.crawl.elasticsearch.mappers.ElasticSearchFeatureMapper;
 import org.genedb.crawl.elasticsearch.mappers.ElasticSearchRegionsMapper;
 import org.genedb.crawl.model.Feature;
 import org.genedb.crawl.model.LocatedFeature;
 import org.genedb.crawl.model.Organism;
 import org.genedb.crawl.modelling.RegionFeatureBuilder;
 
 public class GFFAnnotatationExtractor {
 
     private static Logger logger = Logger.getLogger(GFFAnnotatationExtractor.class);
 
     public GFFAnnotatationExtractor(BufferedReader buf, String filePath, Organism organism, ElasticSearchFeatureMapper featureMapper, ElasticSearchRegionsMapper regionsMapper) throws IOException {
         
         List<RegionFeatureBuilder> sequences = new ArrayList<RegionFeatureBuilder>();
         
         try {
 
             boolean parsingAnnotations = true;
             LocatedFeature lastFeature = null;
             RegionFeatureBuilder sequence = null;
             String line = "";
 
             while ((line = buf.readLine()) != null) {
                 logger.debug(line);
 
                 if (line.startsWith("##sequence-region")) {
                     parsingAnnotations = true;
                 }
 
                 if (line.contains("##FASTA")) {
                     parsingAnnotations = false;
                 }
 
                 if (line.startsWith("#")) {
                     continue;
                 }
 
                 if (parsingAnnotations) {
 
                     LocatedFeature feature = new FeatureBeanFactory(organism, line).getFeature();
 
                     if (feature.type.name.equals("CDS")) {
                         logger.debug("changing type from CDS to exon");
                         feature.type.name = "exon";
                     }
                     
                     /*
                      * If the last feature has the same uniqueName, then add the extra 
                      * coordinates to the last one, else store this feature.
                      */
                     if (lastFeature != null && lastFeature.uniqueName.equals(feature.uniqueName)) {
                         if (feature.fmin != lastFeature.fmin || feature.fmax != lastFeature.fmax) {
 
                            logger.info(String.format("adding extra coordinates to %s : %s-%s", lastFeature.uniqueName, feature.coordinates.get(0).fmin, feature.coordinates.get(0).fmax));
 
                             lastFeature.coordinates.add(feature.coordinates.get(0));
                             createOrUpdate(lastFeature, featureMapper);
                         }
                     } else {
                         createOrUpdate(feature, featureMapper);
                         lastFeature = feature;
                     }
 
                 } else {
                     if (line.startsWith(">")) {
                         String sequenceName = line.substring(1);
 
                         /* we ignore everything after a space */
                         int spacePos = sequenceName.indexOf(" ");
                         if (spacePos != -1) {
                             sequenceName = sequenceName.substring(0, spacePos);
                         }
 
                         sequence = new RegionFeatureBuilder(sequenceName, organism.ID);
                         sequence.setSequenceFile(filePath);
                         
                         sequences.add(sequence);
 
                     } 
                 }
 
             }
             
             for (RegionFeatureBuilder regionBuilder : sequences) {
               Feature region = regionBuilder.getRegion();
               logger.info("Storing region : " + region.uniqueName);
               regionsMapper.createOrUpdate(region);
           }
 
         } finally {
             buf.close();
         }
 
     }
 
     private void createOrUpdate(LocatedFeature feature, ElasticSearchFeatureMapper featureMapper) {
         logger.info(info(feature));
         featureMapper.createOrUpdate(feature);
     }
 
     private String info(LocatedFeature f) {
         return (f.uniqueName + " " + f.fmin + "-" + f.fmax + " " + f.type.name);
     }
 
 }
