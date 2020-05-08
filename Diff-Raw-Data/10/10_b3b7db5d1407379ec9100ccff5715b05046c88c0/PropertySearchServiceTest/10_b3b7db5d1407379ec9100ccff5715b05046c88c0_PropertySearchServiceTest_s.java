 package com.rightmove.es.service;
 
 import com.rightmove.es.domain.Property;
 import com.rightmove.es.domain.PropertyFilter;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
 import org.springframework.data.elasticsearch.core.FacetedPage;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:/applicationContext.xml")
 public class PropertySearchServiceTest {
 
 	@Autowired
 	private PropertySearchService propertySearchService;
 
 	@Autowired
 	private PropertyService propertyService;
 
 	@Autowired
 	private ElasticsearchTemplate esTemplate;
 
 	@Test
 	public void getSearchResultFilteredWithFacets() {
 		
		indexProperty(1L, "London", "NW1");
		indexProperty(2L, "London", "NW1");
		indexProperty(3L, "London", "NW2");
		indexProperty(4L, "London", "NW3");
		indexProperty(5L, "London", "NW4");
 		
 		PropertyFilter propertyFilter = new PropertyFilter();
 		propertyFilter.addFilter("nw1","outcode");
 		FacetedPage<Property> results = propertySearchService.search("summary test", propertyFilter).getProperties();
 		assertEquals(2, results.getNumberOfElements());
 		for (Property property : results) {
 			System.out.println(property);
 		}
 
 		System.out.println();
 
 		propertyFilter.addFilter("nw2","outcode");
 		results = propertySearchService.search("summary test", propertyFilter).getProperties();
 		assertEquals(3, results.getNumberOfElements());
 		for (Property property : results) {
 			System.out.println(property);
 		}
 		
 		assertEquals(1, results.getFacets().size());
 	}
 	
 	@Test
 	public void weight() {
 		
 		indexProperty(1L, "Milton Keynes", "AB1", "DE2", "summary test", null);
 		indexProperty(2L, "Milton Keynes", "AB1", "DE3", "this property has 2 staircases", Arrays.asList("walls"));
 		indexProperty(3L, "NW1", "NW2");
 		indexProperty(4L, "NW1", "NW1");
 		indexProperty(5L, "London", "NW3");
 		
 		FacetedPage<Property> results = propertySearchService.search("AB1 walls").getProperties();
 		//assertEquals(2, results.getNumberOfElements());
 		for (Property property : results) {
 			System.out.println(property);
 		}
 	}
 
 	public void indexProperty(Long id, String city, String incode) {
 		indexProperty(id, city, "OUT", incode, "summary test", null);
 	}
 	
 	public void indexProperty(Long id, String city, String outcode, String incode, String summary, List<String> features) {
 		Property property = new Property();
 		property.setId(id);
 		property.setIncode(incode);
 		property.setOutcode(outcode);
 		property.setCity(city);
 		property.setSummary(summary);
 		property.setFeatures(features);
 		propertyService.indexProperty(property);
 	}
 }
