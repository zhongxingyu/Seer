 package org.genedb.crawl.dao.backend;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.log4j.Logger;
 import org.genedb.crawl.CrawlException;
 import org.genedb.crawl.mappers.FeaturesMapper;
 import org.genedb.crawl.mappers.OrganismsMapper;
 import org.genedb.crawl.mappers.RegionsMapper;
 import org.genedb.crawl.mappers.TermsMapper;
 import org.genedb.crawl.model.Cvterm;
 import org.genedb.crawl.model.Feature;
 import org.genedb.crawl.model.LocatedFeature;
 import org.genedb.crawl.model.LocationBoundaries;
 import org.genedb.crawl.model.Organism;
 import org.genedb.crawl.model.Sequence;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class RegionsDAO extends BaseDAO implements org.genedb.crawl.dao.RegionsDAO {
     
     private Logger logger = Logger.getLogger(RegionsDAO.class);
     
     @Autowired
     RegionsMapper regionsMapper;
     
     @Autowired
     TermsMapper termsMapper;
     
     @Autowired
     FeaturesMapper featuresMapper;
     
     @Autowired
     OrganismsMapper organismsMapper;
     
     private boolean cacheRegionsOnStartup = false;
     private Map<String, List<Feature>> organismRegionMap = new HashMap<String, List<Feature>>();
     
     public void setCacheRegionsOnStartup(boolean cacheRegionsOnStartup) {
         this.cacheRegionsOnStartup = cacheRegionsOnStartup;
     }
     
     @PostConstruct
     void setup() throws CrawlException {
         if (! cacheRegionsOnStartup) {
             return;
         }
         for (Organism o : organismsMapper.list()) {
             List<Feature> r = regionsMapper.inorganism( o.ID, null, null, null );
             Collections.sort(r, new FeatureUniqueNameSorter());
             organismRegionMap.put(String.valueOf(o.ID), r);
             logger.info(String.format("Cached %s.", o.common_name));
         }
     }
     
     
     
     /* (non-Javadoc)
      * @see org.genedb.crawl.dao.backend.RegionsDAO#locations(java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.Boolean, java.util.List)
      */
     @Override
     
     public List<LocatedFeature> locations(
             String region, 
             Integer start, 
             Integer end, 
             Boolean exclude,
             List<String> types
             ) throws CrawlException {
         
         // the JAX-WS endpoint won't think to use default value, so we must assign them manually
         if (exclude == null)
             exclude = true;
         
         if (start == null) 
             start = 0;
         
         if (end == null) 
             end = regionsMapper.sequence(region).dna.length();
         
         
         // logger.info(String.format("Getting locations for %s.", region));
         // trying to speed up the boundary query by determining the types in advance
         // String[] geneTypes = new String[] {"gene", "pseudogene"};
         
         Set<String> geneTypes = new HashSet<String>();
         
         if (types != null) {
             geneTypes.addAll(types);
         }
         
         // boundary calculations must include genes or pseudogenes, so we clone the set
         Set<String> boundaryTypes = new HashSet<String>(geneTypes);
         
         // let's never exclude gene or pseudogenes from boundary calculations, and always include them 
         if (exclude) {
             boundaryTypes.remove("gene");
             boundaryTypes.remove("pseudogene");
         } else {
             boundaryTypes.add("gene");
             boundaryTypes.add("pseudogene");
         }
         
         
         
         
         logger.info(String.format("%s %d-%d %s", region, start,end,exclude));
         logger.info("Gene Types " + geneTypes);
         
         int actualStart = start;
         int actualEnd = end;
         
         LocationBoundaries expandedBoundaries = regionsMapper.locationsMinAndMaxBoundaries(region, start, end, exclude, new ArrayList<String>(boundaryTypes));
        logger.debug(expandedBoundaries.start);
        logger.debug(expandedBoundaries.end);
         if (expandedBoundaries != null) {
             if (expandedBoundaries.start != null && expandedBoundaries.start < start) {
                 actualStart = expandedBoundaries.start;
             }
             if (expandedBoundaries.end != null &&expandedBoundaries.end > end) {
                 actualEnd = expandedBoundaries.end;
             }
         }
         
         logger.debug( String.format("Locating on %s : %s-%s (%s)", region, actualStart, actualEnd, exclude));
         
         return regionsMapper.locations(region, actualStart, actualEnd, exclude, new ArrayList<String>(geneTypes));
 //      results.actual_end = actualEnd;
 //      results.actual_start = actualStart;
 //      
 //      return results;
 
     }
     
 //  @RequestMapping(method=RequestMethod.GET, value={"/locations_paged", "/locations_paged.*"})
 //  @ResourceDescription("Returns features and their locations on a region of interest, paged by limit and offset.")
 //  public ResultsRegions locationsPaged(
 //          @RequestParam("region") String region, 
 //          @RequestParam("limit") int limit, 
 //          @RequestParam("offset") int offset, 
 //          @RequestParam(value="exclude", defaultValue="true") boolean exclude,
 //          @RequestParam(value="types", required=false) @ResourceDescription("A list of features types to exclude or include.") List<String> types
 //          ) throws CrawlException {
 //      
 //      
 //      logger.info(String.format("Getting locations for %s.", region));
 //              
 //      // trying to speed up the boundary query by determining the types in advance
 //        List<Integer> geneTypes = termsMapper.getCvtermIDs("sequence", new String[] {"gene", "pseudogene"});
 //        
 //        logger.info("Gene Types " + geneTypes);
 //        
 //      logger.info( String.format("Locating paged on %s : %s-%s (%s)", region, limit, offset, exclude));
 //      
 //      results.locations = regionsMapper.locationsPaged(region, limit, offset, exclude, types);
 //      
 //      return results;
 //
 //  }
     
 //  @RequestMapping(method=RequestMethod.GET, value="/sequence")
 //  @ResourceDescription("Returns the sequence on a region.")
 //  public List<Sequence>  sequenceInfo(
 //          @RequestParam("region") String region, 
 //          @RequestParam(value="metadata_only", required=false, defaultValue="false") boolean metadataOnly) {
 //      
 //      List<Sequence> sequences = new ArrayList<Sequence>();
 //      Sequence sequence = regionsMapper.sequenceTrimmed(region, start, end);
 //      
 //  }
     
     /* (non-Javadoc)
      * @see org.genedb.crawl.dao.backend.RegionsDAO#sequenceLength(java.lang.String)
      */
     @Override
     public List<Sequence>  sequenceLength(String region) {
         
         List<Sequence> sequences = new ArrayList<Sequence>();
         Sequence sequence = regionsMapper.sequenceLength(region);
         sequences.add(sequence);
         return sequences;
     }
     
     /* (non-Javadoc)
      * @see org.genedb.crawl.dao.backend.RegionsDAO#sequence(java.lang.String, java.lang.Integer, java.lang.Integer)
      */
     @Override
     public List<Sequence>  sequence(
             String region, 
             Integer start, 
             Integer end) {
         
         List<Sequence> sequences = new ArrayList<Sequence>();
         
         if (start == null && end == null) {
             Sequence sequence = regionsMapper.sequence(region);
             sequence.start = 1;
             sequence.end = sequence.length; 
             sequences.add(sequence);
         } else {
             Sequence sequence = regionsMapper.sequenceTrimmed(region, start, end);
             sequences.add(sequence);
         }
         
         
         
         
         //results.sequences = sequences;
 //      
 //      String sequenceResidues = sequence.dna;
 //      
 //      int length = (sequence.length == null) ? sequenceResidues.length() : sequence.length;
 //      if (length == 0) {
 //          return sequences;
 //      }
 //      
 //      // if it's a simple case of no start or end position, just return what we've got
 //      if (start == null && end == null) {
 //          
 //          if (metadataOnly) {
 //              sequence.dna = null;
 //          }
 //          sequence.start = 0;
 //          sequence.end = length -1;
 //          sequence.region = region;
 //          
 //          return sequences;
 //      }
 //      
 //      
 //      if (start == null) {
 //          start = 0;
 //      }
 //      
 //      if (end == null) {
 //          end = length;
 //      }
 //      
 //      int lastResiduePosition = length -1;
 //      int actualStart = start -1;
 //      int actualEnd = end -1;
 //      
 //      if (actualStart > lastResiduePosition || actualStart > actualEnd) {
 //          return sequences;
 //      }
 //      
 //      if (actualEnd > lastResiduePosition) {
 //          actualEnd = lastResiduePosition;
 //      }
 //      
 //      if (! metadataOnly) {
 //          sequence.dna = sequenceResidues.substring(actualStart, actualEnd);
 //      } else {
 //          sequence.dna = null;
 //      }
         
 //      sequence.start = start;
 //      sequence.end = end;
 //      sequence.length = length;
 //      sequence.region = region;
         
         return sequences;
     }
     
     /* (non-Javadoc)
      * @see org.genedb.crawl.dao.backend.RegionsDAO#getInfo(java.lang.String, java.lang.String, java.lang.String)
      */
     @Override
     public Feature getInfo( 
             String uniqueName, 
             String name,
             String organism) throws CrawlException {
         
         Integer organism_id = null;
         if (organism != null) {
             Organism o = util.getOrganism(organism);
             if (o != null)
                 organism_id = o.ID;
         }
         
         return regionsMapper.getInfo(uniqueName, name, organism_id);
 
     }
     
     /* (non-Javadoc)
      * @see org.genedb.crawl.dao.backend.RegionsDAO#inorganism(java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String)
      */
     @Override
     public List<Feature> inorganism( 
             String organism,
             Integer limit, 
             Integer offset,
             String type) throws CrawlException {
         
         Organism o = util.getOrganism(organism);
         
         List<Feature> r = null;
         if (organismRegionMap.containsKey(o.ID)) {
             r = organismRegionMap.get(o.ID);
         } else {
             r = regionsMapper.inorganism( o.ID, limit, offset, type);
             Collections.sort(r, new FeatureUniqueNameSorter());
             organismRegionMap.put(String.valueOf(o.ID), r);
         }
         
         //results.regions = r;
         
         return r;
     }
     
     /* (non-Javadoc)
      * @see org.genedb.crawl.dao.backend.RegionsDAO#typesInOrganism(java.lang.String)
      */
     @Override
     public List<Feature> typesInOrganism(String organism) throws CrawlException {
         
         Organism o = util.getOrganism(organism);
         
         List<Cvterm> regionTypes = regionsMapper.typesInOrganism( o.ID );
         List<Feature> regions = new ArrayList<Feature>();
         
         for (Cvterm regionType : regionTypes) {
             Feature region = new Feature();
             region.type = regionType;
             regions.add(region);
         }
         
         //results.regions = regions;
         
         return regions;
     }
     
     class FeatureUniqueNameSorter implements Comparator<Feature> {
         @Override
         public int compare(Feature f1, Feature f2) {
             return f1.uniqueName.compareTo(f2.uniqueName);
         }
     }
 }
