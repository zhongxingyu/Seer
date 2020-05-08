 package com.rightmove.es.service.impl;
 
 import com.rightmove.es.dao.PropertyDao;
 import com.rightmove.es.domain.Property;
 import com.rightmove.es.service.PropertyService;
 import com.rightmove.es.utils.StretchyUtils;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
 import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
 import org.elasticsearch.action.bulk.BulkRequestBuilder;
 import org.elasticsearch.action.index.IndexRequest;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.search.SearchHit;
 import org.elasticsearch.search.SearchHits;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 
 import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
 
 @Component
 public class PropertyServiceImpl implements PropertyService {
 
 	@Autowired
 	private PropertyDao propertyDao;
 
     @Autowired
     private Client client;
 
 	@Override
 	public void indexProperty(Property property) {
 		try {
 			client.prepareIndex("property-search-index", "rm-property", String.valueOf(property.getId()))
 					.setSource(new ObjectMapper().writeValueAsBytes(property))
 					.execute().actionGet();
 		} catch (IOException e) {
 			throw new RuntimeException("Cannot index property", e);
 		}
 	}
 
 	@Override
 	public void indexProperties(Collection<Property> properties) {
 
 		BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
 		ObjectMapper mapper = new ObjectMapper();
 
 		for(Property property : properties) {
 			try {
 				bulkRequestBuilder.add(new IndexRequest("property-search-index", "rm-property", String.valueOf(property.getId()))
 				.source(mapper.writeValueAsBytes(property)));
 			} catch (IOException e) {
 				throw new RuntimeException("Error converting Property: " + property.getId() + " to JSON.", e);
 			}
 		}
 
 		if(bulkRequestBuilder.execute().actionGet().hasFailures()) {
 			throw new RuntimeException("Failed to bulk index regions.");
 		}
 	}
 
 	@Override
 	public void createIndex() {
 		try {
			String mapping = jsonBuilder().startObject().startObject("rm-property").startObject("properties")
 					.startObject("_boost")
 					.field("name", "boost")
 					.field("null_value", 1.0)
 					.endObject()
 					.endObject().endObject().string();
 
 			client.admin().indices().delete(new DeleteIndexRequest("property-search-index")).actionGet().isAcknowledged();
 			client.admin().indices().create(new CreateIndexRequest("property-search-index")).actionGet().isAcknowledged();
 			client.admin().indices().preparePutMapping("property-search-index").setType("rm-property").setSource(mapping).execute().actionGet().isAcknowledged();
 
 		} catch (Exception e) {
 			throw new RuntimeException("Could not set up property index.", e);
 		}
 	}
 
 	@Override
 	public void indexAllProperties() {
 		for(File jsonFile : StretchyUtils.getAllJSONFiles()) {
 			indexProperties(propertyDao.listAllByFile(jsonFile));
 		}
 	}
 
 	@Override
     public Collection<Property> extractProperties(SearchHits searchHits) {
 
         Collection<Property> propertyCollection = new LinkedHashSet<Property>();
 
         for (SearchHit searchHitFields : searchHits) {
             ObjectMapper objectMapper = new ObjectMapper();
             try {
                 Property property = objectMapper.readValue(searchHitFields.getSourceAsString(), Property.class);
                 propertyCollection.add(property);
             }
             catch (Exception e) {
 
             }
         }
 
         return propertyCollection;
     }
 
 }
