 package org.genedb.crawl.elasticsearch.mappers;
 
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 
 import org.apache.log4j.Logger;
 import org.elasticsearch.action.count.CountResponse;
 import org.elasticsearch.action.search.SearchResponse;
 import org.elasticsearch.client.action.search.SearchRequestBuilder;
 import org.elasticsearch.common.xcontent.ToXContent;
 import org.elasticsearch.common.xcontent.json.JsonXContent;
 import org.elasticsearch.index.query.xcontent.BoolQueryBuilder;
 import org.elasticsearch.index.query.xcontent.FieldQueryBuilder;
 import org.elasticsearch.index.query.xcontent.QueryBuilders;
 import org.elasticsearch.index.query.xcontent.RangeQueryBuilder;
 import org.elasticsearch.search.SearchHit;
 import org.elasticsearch.search.SearchHitField;
 import org.elasticsearch.search.SearchHits;
 import org.elasticsearch.search.sort.SortBuilders;
 import org.genedb.crawl.mappers.RegionsMapper;
 import org.genedb.crawl.model.Coordinates;
 import org.genedb.crawl.model.Cvterm;
 import org.genedb.crawl.model.Feature;
 import org.genedb.crawl.model.FeatureProperty;
 import org.genedb.crawl.model.LocatedFeature;
 import org.genedb.crawl.model.LocationBoundaries;
 import org.genedb.crawl.model.Sequence;
 import org.springframework.stereotype.Component;
 
 @Component
 public class ElasticSearchRegionsMapper extends ElasticSearchBaseMapper implements RegionsMapper {
 
 	private Logger logger = Logger.getLogger(ElasticSearchRegionsMapper.class);
 	
 	private int getTotalInRegion(String region) {
 		FieldQueryBuilder regionQuery = QueryBuilders.fieldQuery("region", region);
 		
 		logger.debug(String.format("Index %s, Type %s", connection.getIndex(), connection.getFeatureType()));
 		
 		CountResponse cr = connection.getClient()
 		 	.prepareCount()
 		 	.setIndices(connection.getIndex())
 		 	.setTypes(connection.getFeatureType())
 		 	.setQuery(regionQuery)
 		 	.execute()
 	        .actionGet();
 		
 		long count = cr.count();
 		
 		
 		logger.debug(String.format("Count in %s : %s", region, count));
 		
 		return (int) count;
 	}
 	
 	private int getTotalRegionsInOrganism(int organism_id) {
 		FieldQueryBuilder regionQuery = QueryBuilders.fieldQuery("organism_id", organism_id);
 		
 		CountResponse cr = connection.getClient()
 		 	.prepareCount(connection.getIndex())
 		 	.setTypes(connection.getRegionType())
 		 	.setQuery(regionQuery)
 		 	.execute()
 	        .actionGet();
 		
 		long count = cr.count();
 		
 		logger.debug(String.format("Count in organism %s : %s", organism_id, count));
 		
 		return (int) count;
 	}
 	
 	
 	private BoolQueryBuilder isOverlap(String region, int start, int end, boolean exclude, List<String> types) {
 		
 		RangeQueryBuilder startLowerThanRequested = 
 			QueryBuilders.rangeQuery("fmin")
 				.lte(start);
 		
 		RangeQueryBuilder endHigherThanRequested = 
 			QueryBuilders.rangeQuery("fmax")
 				.gte(end);
 		
 		// (fmin <= start) && (end <= fmax)
 		BoolQueryBuilder spansBothSides = 
 			QueryBuilders.boolQuery()
 				.must(startLowerThanRequested)
 				.must(endHigherThanRequested);
 		
 		RangeQueryBuilder startInRange = 
 			QueryBuilders.rangeQuery("fmin")
 				.from(start)
 				.to(end);
 		
 		RangeQueryBuilder endInRange = 
 			QueryBuilders.rangeQuery("fmax")
 				.from(start)
 				.to(end);
 		
 		// (start <= fmin <= end) || (start <= fmax <= end) 
 		BoolQueryBuilder isInsideRange = 
 			QueryBuilders.boolQuery()
 				.should(startInRange)
 				.should(endInRange);
 		
 		
 		
 		BoolQueryBuilder isOverlap = 
 			QueryBuilders.boolQuery()
 				.should(spansBothSides)
 				.should(isInsideRange);
 		
 		
 		FieldQueryBuilder regionQuery = 
 			QueryBuilders.fieldQuery("region", region);
 		
 		BoolQueryBuilder isOverlapOnRegion =
 			QueryBuilders.boolQuery()
 			.must(isOverlap)
 			.must(regionQuery);
 		
 		if (types != null) {
 			
 			BoolQueryBuilder typesQuery = QueryBuilders.boolQuery();
 			
 			for (String type : types) {
 				FieldQueryBuilder excludeQuery = 
 					QueryBuilders.fieldQuery("type.name", type);
 				if (exclude) {
 					typesQuery.mustNot(excludeQuery);
 				} else {
 					typesQuery.should(excludeQuery);
 				}
 			}
 			
 			isOverlapOnRegion.must(typesQuery);
 		}
 		
 		return isOverlapOnRegion;
 	}
 	
 	public static String toString(ToXContent tmp) {
 	       try {
 	           return
 	           tmp.toXContent(JsonXContent.unCachedContentBuilder(),
 	        		   ToXContent.EMPTY_PARAMS).
 	                   prettyPrint().
 	                   string();
 	       } catch (Exception ex) {
 	           return "<ERROR:" + ex.getMessage() + ">";
 	       }
 	   }
 	
 	@Override
 	public LocationBoundaries locationsMinAndMaxBoundaries(String region,
 			int start, int end, List<String> types) {
 		
 		BoolQueryBuilder isOverlap = isOverlap(region, start, end, false, types);
 		
 		SearchRequestBuilder builder = 
 			connection
 			.getClient()
 			.prepareSearch(connection.getIndex())
 			.setTypes(connection.getFeatureType());
 		
 		
 		SearchResponse response = builder
 			.setQuery(isOverlap)
 			.setExplain(true)
 			.setSize(getTotalInRegion(region))
 			.execute()
 			.actionGet();
 	
 		logger.info(toString(builder.internalBuilder()));
 		
 		LocationBoundaries lb = new LocationBoundaries();
 		lb.start = start;
 		lb.end = end;
 		
 		for (SearchHit hit : response.getHits()) {
 			
 			String source = hit.sourceAsString();
 			
 			//logger.debug(source);
 			
 			LocatedFeature feature = this.getFeatureFromJson(source);
 			
 			
 			if (feature != null) {
 				
 				
 				if (feature.fmax != null && feature.fmin != null && feature.region != null) {
 					if (feature.region.equals(region)) {
 						
 						if (feature.fmin < lb.start) {
 							lb.start = feature.fmin;
 						} 
 						
 						if (feature.fmax > lb.end) {
 							lb.end = feature.fmax;
 						} 
 					}
 				}
 				
 				else if (feature.coordinates != null) {
 					for (Coordinates co : feature.coordinates) {
 						if (co.region.equals(region)) {
 							
 							if (co.fmin < lb.start) {
 								lb.start = co.fmin;
 							} 
 							
 							if (co.fmax > lb.end) {
 								lb.end = co.fmax;
 							} 
 								
 							break;
 						}
 					}
 				}
 				
 				
 				
 			}
 		}
 		
 		
 		logger.debug(String.format("Actual start: %s. Actual end %s", lb.start, lb.end));
 		
 		return lb;
 		
 	}
 	
 	
 //	@Override
 //	public List<LocatedFeature> locationsPaged(String region, int limit,
 //			int offset,boolean exclude, List<String> types) {
 //		
 //		SearchRequestBuilder builder = connection.getClient()
 //			.prepareSearch(connection.getIndex())
 //			.setTypes(connection.getFeatureType())
 //			.addSort(SortBuilders.fieldSort("fmin"))
 //			.addSort(SortBuilders.fieldSort("fmax"));
 //		
 //		FieldQueryBuilder regionQuery = 
 //			QueryBuilders.fieldQuery("region", region);
 //		
 //		RangeQueryBuilder rangeQuery = 
 //			QueryBuilders.rangeQuery("fmin").from(0);
 //		
 //		BoolQueryBuilder locations =
 //			QueryBuilders.boolQuery()
 //				.must(rangeQuery)
 //				.must(regionQuery);
 //		
 //		SearchResponse response = builder
 //			.setQuery(locations)
 //			.setExplain(true)
 //			.setSize(limit)
 //			.setFrom(offset)
 //			.execute()
 //			.actionGet();
 //		
 //		return parseLocations(region, response);
 //	}
 	
 	
 	
 	@Override
 	public List<LocatedFeature> locations(String region, int start, int end, boolean exclude,
 			List<String> types) {
 		
 		BoolQueryBuilder isOverlap = isOverlap(region, start, end, exclude, types);
 		
 		SearchRequestBuilder builder = connection.getClient()
 			.prepareSearch(connection.getIndex())
 			.setTypes(connection.getFeatureType())
 			.addSort(SortBuilders.fieldSort("fmin"))
 			.addSort(SortBuilders.fieldSort("fmax"));
 		
 		SearchResponse response = builder
 			.setQuery(isOverlap)
 			.setExplain(true)
 			.setSize(getTotalInRegion(region))
 			.execute()
 			.actionGet();
 		
 		logger.info(toString(builder.internalBuilder()));
 		
 		
 		return parseLocations(region, response);
 		
 	}
 	
 	private List<LocatedFeature> parseLocations(String region, SearchResponse response) {
 		List<LocatedFeature> features = new ArrayList<LocatedFeature>();
 		
 		String[] fieldNames = new String[] {"uniqueName", "fmin", "fmax", "isObsolete", "parent", "phase", "type", "strand", "region", "properties"};
 		
 		for (SearchHit hit : response.getHits()) {
 		
 			String source = hit.sourceAsString();
 			//logger.debug(source);
 			
 			LocatedFeature feature = this.getFeatureFromJson(source);
 			
 			// we only want to return colour
 			List<FeatureProperty> fps = new ArrayList<FeatureProperty>();
 			if (feature.properties != null) {
 				for (FeatureProperty prop : feature.properties) {
 					if (prop.name.equals("colour")) {
 						fps.add(prop);
 					}
 				}
 			}
 			feature.properties=fps;
 			
 			if (feature != null) {
 				
 				
 				if (feature.fmax != null && feature.fmin != null && feature.region != null) {
 					if (feature.region.equals(region)) {
 						try {
 							features.add(copy(feature, fieldNames, LocatedFeature.class));
 						} catch (InstantiationException e) {
 							logger.error(e);
 							e.printStackTrace();
 						} catch (IllegalAccessException e) {
 							logger.error(e);
 							e.printStackTrace();
 						}
 					}
 				}
 				else if (feature.coordinates != null) {
 					for (Coordinates co : feature.coordinates) {
 						if (co.region.equals(region)) {
 							
 							try {
 								features.add(copy(feature, fieldNames, LocatedFeature.class));
 							} catch (InstantiationException e) {
 								logger.error(e);
 								e.printStackTrace();
 							} catch (IllegalAccessException e) {
 								logger.error(e);
 								e.printStackTrace();
 							}
 							break;
 						}
 					}
 				}
 				
 			}
 		}
 		
 		return features;
 	}
 
 	@Override
 	public Sequence sequence(String region) {
 		
 		Sequence sequence = new Sequence();
 		sequence.dna = "";
 		sequence.length = 0;
 		sequence.organism_id = -1;
 		
 		try {
 			
 			logger.info(String.format("%s %s %s %s %s",connection.getIndex(), connection.getRegionType(), region, connection, connection.getClient()));
 			String json = connection
 							.getClient()
 							.prepareGet()
 							.setIndex(connection.getIndex())
 							.setType(connection.getRegionType())
 							.setId(region)
 							.execute()
 							.actionGet()
 							.sourceAsString();
 			
 			
 			
 			Feature regionFeature = (Feature) jsonIzer.fromJson(json, Feature.class);
 			
 			sequence.dna = regionFeature.residues;
 			sequence.length = regionFeature.residues.length();
 			sequence.organism_id = regionFeature.organism_id;
 			
 			
 		} catch (Exception e) {
 			logger.error(e);
 		} 
 		
 		return sequence;
 		
 	}
 	
 	@Override
 	public Sequence sequenceLength(String region) {
 		return sequence(region);
 	}
 
 	@Override
 	public Sequence sequenceTrimmed(String region, Integer start, Integer end) {
 		Sequence s = sequence(region);
 		
 		// if it's a simple case of no start or end position, just return what we've got
 		if (start == null && end == null) {
 			
 			s.start = 0;
 			s.end = s.length -1;
 			s.region = region;
 			
 			return s;
 		}
 		
 		int actualStart = start -1;
 		int actualEnd = end -1;
 		
 		s.dna = s.dna.substring(actualStart, actualEnd);
		s.length = s.dna.length();
 		
 		return s;
 	}
 	
 
 	@Override
 	public List<Feature> inorganism(int organismid, Integer limit, Integer offset, String type_name) {
 		
 		logger.debug(String.format("%s %s %s", "sequences", "organism_id", String.valueOf(organismid)));
 		
 		BoolQueryBuilder regionInOrganismQuery = QueryBuilders.boolQuery();
 		
 		FieldQueryBuilder organismQuery = 
 			QueryBuilders.fieldQuery("organism_id", organismid);
 		
 		regionInOrganismQuery.must(organismQuery);
 			
 		if (type_name != null) {
 			FieldQueryBuilder typeQuery =
 				QueryBuilders.fieldQuery("type.name", type_name);
 			regionInOrganismQuery.must(typeQuery);
 		}
 		
 		SearchRequestBuilder srb = connection.getClient()
 			.prepareSearch(connection.getIndex())
 			.setTypes(connection.getRegionType())
 			.setQuery(regionInOrganismQuery)
 			.addFields(new String[] {"organism_id", "type.name"});
 		
 		if (limit == null) {
 			limit = getTotalRegionsInOrganism(organismid); 
 		}
 		
 		srb.setSize(limit);
 		
 		if (offset != null) {
 			srb.setFrom(offset);
 		}
 		
 		logger.info(toString(srb.internalBuilder()));
 		
 		SearchResponse response = srb.execute()
 			.actionGet();
 		
 		logger.info(response);
 		
 		SearchHits hits = response.getHits();
 		
 		List<Feature> regions = new ArrayList<Feature>();
 		
 		for (SearchHit hit : hits) {
 			
 			logger.info(hit.id());
 			
 			for (Entry<String, SearchHitField> fieldEntry : hit.getFields().entrySet()) {
 				logger.debug("-" + fieldEntry.getKey());
 				
 				SearchHitField field = fieldEntry.getValue();
 				logger.debug("---" + field.getValue());
 				
 			}
 			
 			Feature region = new Feature();
 			region.uniqueName = hit.getId();
 			region.type = new Cvterm();
 			region.type.name = (String) hit.field("type.name").getValue(); 
 			region.organism_id = (Integer) hit.field("organism_id").getValue();
 			
 			regions.add(region);
 			
 		}
 		
 		return regions;
 
 	}
 	
 //	QueryBuilder regionInOrganismQuery(int organismid) {
 //		FieldQueryBuilder organismQuery = 
 //			QueryBuilders.fieldQuery("organism_id", organismid);
 //		
 ////		FieldQueryBuilder regionQuery =
 ////			QueryBuilders.fieldQuery("topLevel", true);
 //		
 ////		BoolQueryBuilder regionInOrganismQuery = 
 ////			QueryBuilders.boolQuery()
 ////				.must(organismQuery)
 ////				.must(regionQuery);
 //		
 //		return organismQuery;
 //	}
 
 	@Override
 	public List<Cvterm> typesInOrganism(int organismid) {
 		FieldQueryBuilder organismQuery = 
 			QueryBuilders.fieldQuery("organism_id", organismid);
 		
 		SearchResponse response = 
 			connection.getClient()
 			.prepareSearch()
 			.setQuery(organismQuery).execute().actionGet();
 		
 		List<Feature> regions = this.getAllMatches(response, Feature.class);
 		Set<Cvterm> terms = new HashSet<Cvterm>();
 		for (Feature region : regions) {
 			terms.add(region.type);
 		}
 		
 		return new ArrayList<Cvterm>(terms);
 	}
 	
 	public void createOrUpdate(Feature feature) {
 		this.createOrUpdate(connection.getIndex(), connection.getRegionType(), feature.uniqueName, feature);
 	}
 
 
 
 
 	
 //	public static String getIndex() {
 //		return "regions";
 //	}
 //
 //
 //	
 //	public static String getType() {
 //		return "Region";
 //	}
 
 
 	
 
 }
