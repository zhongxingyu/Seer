 package org.genedb.crawl.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.jws.WebMethod;
 import javax.jws.WebService;
 
 import org.apache.log4j.Logger;
 import org.genedb.crawl.CrawlException;
 import org.genedb.crawl.annotations.ResourceDescription;
 import org.genedb.crawl.bam.BioDataFileStore;
 import org.genedb.crawl.bam.BioDataFileStoreInitializer;
 import org.genedb.crawl.mappers.FeatureMapper;
 import org.genedb.crawl.mappers.RegionsMapper;
 import org.genedb.crawl.mappers.OrganismsMapper;
 import org.genedb.crawl.model.LocatedFeature;
 import org.genedb.crawl.model.LocationBoundaries;
 import org.genedb.crawl.model.MappedSAMSequence;
 import org.genedb.crawl.model.MappedVCFRecord;
 import org.genedb.crawl.model.Organism;
 import org.genedb.crawl.model.Sequence;
 import org.genedb.crawl.model.Variant;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import org.genedb.crawl.model.Gene;
 
 import uk.ac.sanger.artemis.components.variant.VariantFilterOption;
 import uk.ac.sanger.artemis.components.variant.VariantFilterOptions;
 import uk.ac.sanger.artemis.util.OutOfRangeException;
 
 @Controller
 @ResourceDescription("Provides methods for VCF/BCF variant querying.")
 @RequestMapping("/variants")
 @WebService(serviceName="variants")
 public class VariantController extends BaseQueryController {
 	
 	private static Logger logger = Logger.getLogger(VariantController.class);
 	
 	private BioDataFileStore<Variant> variantStore;
 	
 	@Autowired
 	private RegionsMapper regionsMapper;
 	
 	@Autowired
 	private OrganismsMapper organismsMapper;
 	
 	@Autowired
 	private FeatureMapper featureMapper;
 	
 	@Autowired
 	@WebMethod(exclude=true)
 	public void setInitializer(BioDataFileStoreInitializer initializer) {
 		variantStore=initializer.getVariants();
 	}
 	
 	@ResourceDescription("Returns the complete list of variant files.")
 	@RequestMapping(method=RequestMethod.GET, value={"/list", "/list.*"})
 	public List<Variant> list() throws IOException {
 		return variantStore.getFiles();
 	}
 	
 	@ResourceDescription("Returns a list of sequences in a variant file.")
 	@RequestMapping(method=RequestMethod.GET, value={"/sequences", "/sequences.*"})
 	public List<MappedSAMSequence> sequences(
 			@RequestParam("fileID") int fileID) throws IOException {
 		return variantStore.getSequences(fileID);
 	}
 	
 	@ResourceDescription("Returns a list of variant files for a particular organism.")
 	@RequestMapping(method=RequestMethod.GET, value={"/listfororganism", "/listfororganism.*"})
 	public List<Variant> listfororganism( 
 			@RequestParam("organism") String organism) throws IOException {
 		
 		Organism mappedOrganism = getOrganism(organismsMapper, organism);
 		return variantStore.listfororganism(mappedOrganism.common_name);
 	}
 	
 	@ResourceDescription("Returns a list of variant files for a particular sequence.")
 	@RequestMapping(method=RequestMethod.GET, value={"/listforsequence", "/listforsequence.*"})
 	public List<Variant> listforsequence( 
 			@RequestParam("sequence") String sequence) throws Exception {
 		return variantStore.listforsequence(sequence);
 	}
 	
	
 	
 	@ResourceDescription("Queries a region of a variant file.")
 	@RequestMapping(method=RequestMethod.GET, value={"/query", "/query.*"})
 	public List<MappedVCFRecord> query(
 			@RequestParam("fileID") int fileID, 
 			@RequestParam("sequence") String sequence, 
 			@RequestParam("start") int start, 
 			@RequestParam("end") int end,
 			@RequestParam(value="filter", required=false) Integer filter,
 			@RequestParam(value="filters", required=false) List<String> filters) throws IOException, CrawlException, OutOfRangeException {
 	    
 	    if (filter == null && filters != null) {
 	        if (filters.size() > 0) {
 	            filter = 0;
 	            for (String f : filters) {
 	                VariantFilterOption option = VariantFilterOption.valueOf(f);
 	                filter += option.index();           
 	            }
 	        }
 	    }
 	    
 		return doQuery(fileID,sequence,start,end,filter);
 	}
 	
	
 	private List<MappedVCFRecord> doQuery(int fileID, String sequence, int start, int end, Integer filter) throws IOException, OutOfRangeException {
 		VariantFilterOptions options = new VariantFilterOptions(filter);
 		
 		logger.info(String.format("Filter %d, values: %s ", filter, options.toString()));
 		
 		String alignmentName = variantStore.getAlignmentFromName(sequence);
 		String referenceName = variantStore.getReferenceFromName(sequence);
 		
 		logger.info(String.format("sequence name supplied: %s, alignment sequence name used: %s, reference sequence name used: %s", sequence, alignmentName, referenceName));
 		
 		Sequence regionSequence = regionsMapper.sequence(referenceName);
 		List<LocatedFeature> geneFeatures = getExons(referenceName, start, end, regionsMapper, featureMapper);
 		
 		return variantStore.getFile(fileID).getReader().query(
 		        alignmentName, 
 		        start, 
 		        end, 
 		        variantStore.getFile(fileID).getReader().makeCDSFeatures(geneFeatures, regionSequence), 
 		        options);
 	}
 	
 	
	// we need to get the gene and pseudo gene features in this request to make sure that the proper boundaries are calculated
	// they will then be ignored by makeCDSFeatures()
    private static final List<String> geneTypes = Arrays.asList(new String[]{"gene", "pseudogene", "exon"});
    
	@WebMethod(exclude=true)
 	public static List<LocatedFeature> getExons(String sequence, int start, int end, RegionsMapper regionsMapper, FeatureMapper featureMapper) {
 	    LocationBoundaries boundaries = regionsMapper.locationsMinAndMaxBoundaries(sequence, start, end, false, geneTypes);
 	    List<LocatedFeature> features = regionsMapper.locations(sequence, boundaries.start, boundaries.end, false, geneTypes);
 	    return features;
 	}
 	
 //	@WebMethod(exclude=true)
 //	public static List<Gene> getGenesAt(String sequence, int start, int end, RegionsMapper regionsMapper, FeatureMapper featureMapper) {
 //		
 //		List<Gene> geneFeatures = new ArrayList<Gene>(); 
 //		
 //		LocationBoundaries boundaries = regionsMapper.locationsMinAndMaxBoundaries(sequence, start, end, false, geneTypes);
 //		
 //		if (boundaries == null) {
 //			return geneFeatures;
 //		}
 //		
 //		
 //		logger.info(sequence + " : " + boundaries.start + " --- " + boundaries.end);
 //		
 //		List<LocatedFeature> features = regionsMapper.locations(sequence, boundaries.start, boundaries.end, false, geneTypes);
 //		
 //		if (features == null) {
 //			return geneFeatures;
 //		}
 //		
 //		for (LocatedFeature feature : features) {
 //			
 //			Gene gene = new Gene();
 //			gene.fmin = feature.fmin;
 //			gene.fmax = feature.fmax;
 //			gene.strand = feature.strand;
 //			gene.phase = feature.phase;
 //			gene.region = feature.region;
 //			
 //			gene.uniqueName = feature.uniqueName;
 //			gene.transcripts = featureMapper.transcripts(gene, true);
 //			
 //			geneFeatures.add(gene);
 //		}
 //		return geneFeatures;
 //	}
 }
