 package org.genedb.crawl.elasticsearch.mappers;
 
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Pattern;
 
 
 import org.apache.log4j.Logger;
 import org.elasticsearch.action.search.SearchResponse;
 import org.elasticsearch.index.query.xcontent.QueryBuilders;
 import org.elasticsearch.search.SearchHit;
 import org.genedb.crawl.mappers.FeaturesMapper;
 import org.genedb.crawl.model.BlastPair;
 import org.genedb.crawl.model.Cvterm;
 import org.genedb.crawl.model.Feature;
 import org.genedb.crawl.model.HierarchyGeneFetchResult;
 import org.genedb.crawl.model.HierarchyRelation;
 import org.genedb.crawl.model.LocatedFeature;
 import org.genedb.crawl.model.Statistic;
 import org.springframework.stereotype.Component;
 
 @Component
 public class ElasticSearchFeaturesMapper extends ElasticSearchBaseMapper implements FeaturesMapper {
 	
 	private Logger logger = Logger.getLogger(ElasticSearchFeaturesMapper.class);
 	
 	
 	
 
 	
 	@Override
 	public List<HierarchyGeneFetchResult> getGeneForFeature(
 			List<String> features) {
 		
 		List<HierarchyGeneFetchResult> results = new ArrayList<HierarchyGeneFetchResult>();
 		
 		String[] fields = new String[]{"uniqueName", "parent"};
 		
 		for (String uniqueName : features) {
 			HierarchyGeneFetchResult res = new HierarchyGeneFetchResult();
 			results.add(res);
 			logger.info(uniqueName);
 			try {
 				logger.info(getFromElastic(connection.getIndex(), connection.getFeatureType(), uniqueName, fields));
 				LocatedFeature f = (LocatedFeature) jsonIzer.fromJson(this.getFromElastic(connection.getIndex(), connection.getFeatureType(), uniqueName, fields), LocatedFeature.class);
 				res.f = f.uniqueName;
 				res.ftype = f.type.name;
 				logger.info("parent?");
 				logger.info(f.parent);
 				if (f.parent != null) {
 					LocatedFeature f2 = (LocatedFeature) jsonIzer.fromJson(this.getFromElastic(connection.getIndex(), connection.getFeatureType(), f.parent, fields), LocatedFeature.class);
 					res.f2 = f2.uniqueName;
 					res.ftype2 = f2.type.name;
 					
 					logger.info("parent2?");
 					logger.info(f2.parent);
 					
 					if (f2.parent != null) {
 						
 						LocatedFeature f3 = (LocatedFeature) jsonIzer.fromJson(this.getFromElastic(connection.getIndex(), connection.getFeatureType(), f2.parent, fields), LocatedFeature.class);
 						res.f3 = f3.uniqueName;
 						res.ftype3 = f3.type.name;
 						
 						logger.info(">>");
 						logger.info(f3.uniqueName);
 					}
 				}
 				
 			} catch (Exception e) {
 				e.printStackTrace();
 			} 
 			
 		}
 		
 		
 		return results;
 	}
 
 	@Override
 	public List<HierarchyRelation> getRelationshipsParents(String feature,
 			List<Cvterm> relationships) {
 		
 		String[] fields = new String[]{"uniqueName", "parent", "type", "name"};
 		HierarchyRelation hr = new HierarchyRelation();
 		hr.feature = feature;
 		
 		logger.info("Searching for parent of " + feature);
 		
 		List<String> relationshipNames = new ArrayList<String>();
 		for (Cvterm rel : relationships) {
 			relationshipNames.add(rel.name);
 		}
 		
 		relationshipNames.add("parent");
 		
 		try {
 			LocatedFeature f = (LocatedFeature) jsonIzer.fromJson(this.getFromElastic(connection.getIndex(), connection.getFeatureType(), feature, fields), LocatedFeature.class);
 			logger.info(f);
 			logger.info(f.parent);
 			logger.info(f.parentRelationshipType);
 			if (f.parent == null || f.parentRelationshipType == null) {
 				return null;
 			}
 			
 			if (! relationshipNames.contains(f.parentRelationshipType.toLowerCase())) {
 				return null;
 			}
 			
 			hr.uniqueName = f.parent;
 			hr.relationship_type = f.parentRelationshipType;
 			
 			LocatedFeature p = (LocatedFeature) jsonIzer.fromJson(this.getFromElastic(connection.getIndex(), connection.getFeatureType(), f.parent, fields), LocatedFeature.class);
 			logger.info(p);
 			hr.type = p.type.name;
 			hr.name = p.name;
 			hr.relationship = "parent";
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		List<HierarchyRelation> hrs = new ArrayList<HierarchyRelation>();
 		hrs.add(hr);
 		
 		return hrs;
 	}
 
 	private static final String LUCENE_ESCAPE_CHARS = "[\\\\+\\-\\!\\(\\)\\:\\^\\]\\{\\}\\~\\*\\?]";
 	private static final Pattern LUCENE_PATTERN = Pattern.compile(LUCENE_ESCAPE_CHARS);
 	private static final String REPLACEMENT_STRING = "\\\\$0";
 
 	
 	@Override
 	public List<HierarchyRelation> getRelationshipsChildren(String feature,
 			List<Cvterm> relationships) {
 		
 		logger.info("Searching for child of " + feature);
 		List<HierarchyRelation> hrs = new ArrayList<HierarchyRelation>();
 		
 		try {
 			
 			
 			String escaped = LUCENE_PATTERN.matcher(feature).replaceAll(REPLACEMENT_STRING);
 			
 			// Using a standard term query was retrieving matches that had the same prefix
 			// SearchRequestBuilder srb = connection.getClient().prepareSearch(index).setQuery (QueryBuilders.fieldQuery("parent", escaped));
 			
 			// this is the closest I think I can get to an exact match query...
 			// by encapsulating the query in quotes, and making sure the phrase slop is 0
 			
 			String queryString = String.format("parent:\"%s\"", escaped);
 			logger.debug(queryString);
 			
 			SearchResponse response = 
 				connection.getClient()			
 				.prepareSearch(connection.getIndex())
 				.setTypes(connection.getFeatureType())
 				.setQuery (QueryBuilders.queryString(queryString).phraseSlop(0))
 				.execute()
 				.actionGet();
 			
 			
 			for (SearchHit hit : response.getHits()) {
 				
 				try {
 					LocatedFeature child = (LocatedFeature) jsonIzer.fromJson(hit.sourceAsString(), LocatedFeature.class);
 					
 					logger.info(" - " + child.uniqueName + " parent: " + child.parent);
 					
 					// make sure we only exact matches
 					if (! child.parent.equals(feature)) {
 						 logger.warn("       SKIPPING");
 						 continue;
 					}
 					
 					
 					HierarchyRelation hr = new HierarchyRelation();
 					hr.feature = feature;
 					
 					hr.uniqueName = child.uniqueName;
 					hr.name = child.name;
 					hr.type = child.type.name;
 					hr.relationship = "child";
 					hr.relationship_type = child.parentRelationshipType;
 					
 					hrs.add(hr);
 					
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			
 		} catch (Exception e) {
 			logger.error(e);
 		}
 		
 		
 		return hrs;
 	}
 
 	@Override
 	public List<Feature> coordinates(List<String> features, String region) {
 		return fetchAndCopy(connection.getIndex(), connection.getFeatureType(), features,  new String[]{"uniqueName", "coordinates"});
 	}
 
 	@Override
 	public List<Feature> synonyms(List<String> features, List<String> types) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	
 	/*
 	 * TODO this method does not filter on type!
 	 * @see org.gmod.cat.FeaturesMapper#properties(java.util.List, java.util.List)
 	 */
 	@Override
 	public List<Feature> properties(List<String> features, List<String> types) {
 		logger.debug("features");
 		logger.debug(features);
 		return fetchAndCopy(connection.getIndex(), connection.getFeatureType(), features,  new String[]{"uniqueName", "properties"});
 	}
 
 	@Override
 	public List<Feature> pubs(List<String> features) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<Feature> dbxrefs(List<String> features) {
 		return fetchAndCopy(connection.getIndex(), connection.getFeatureType(), features,  new String[]{"uniqueName", "dbxrefs"});
 	}
 
 	@Override
 	public List<Feature> terms(List<String> features, List<String> cvs) {
 		List<Feature> featureObjects = fetchAndCopy(connection.getIndex(), connection.getFeatureType(), features,  new String[]{"uniqueName", "terms"});
 		// currently there is no way to load terms from GFFs
 		// create empty terms arrays for web artemis
 		for (Feature feature : featureObjects) {
 			if (feature.terms == null) {
 				feature.terms = new ArrayList<Cvterm>();
 			}
 		}
 		return featureObjects;
 	}
 
 	@Override
 	public List<Feature> orthologues(List<String> features) {
 		return fetchAndCopy(connection.getIndex(), connection.getFeatureType(), features,  new String[]{"uniqueName", "orthologues"});
 	}
 
 	@Override
 	public List<Feature> clusters(List<String> features) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<Feature> synonymsLike(String term, Boolean regex, String region) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<Feature> featuresLike(String term, Boolean regex, String region) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<Feature> withproperty(String value, Boolean regex,
 			String region, String type) {
 		
 		//SearchRequestBuilder srb = connection.getClient().prepareSearch(index)
 		//.setQuery( QueryBuilders.fieldQuery("uniqueName", uniqueName));
 		
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<Feature> withterm(String cvterm, String cv, Boolean regex,
 			String region) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<Feature> timelastmodified(Date date, Integer organism_id, List<String> types, boolean exlude) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<BlastPair> blastPairs(String f1, int start1, int end1,
			String f2, int start2, int end2, Integer length, Integer normscore) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<Feature> annotationModified(Date date, Integer organism_id,
 			String region) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<Statistic> annotationModifiedStatistics(Date date,
 			Integer organism_id, String region) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
