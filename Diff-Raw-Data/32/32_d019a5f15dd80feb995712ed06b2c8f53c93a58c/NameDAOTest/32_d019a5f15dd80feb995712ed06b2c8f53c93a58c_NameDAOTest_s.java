 package net.canadensys.dataportal.vascan.dao;
 
 import static org.elasticsearch.client.Requests.refreshRequest;
 import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.util.List;
 
 import net.canadensys.databaseutils.ElasticSearchTestInstance;
 import net.canadensys.dataportal.vascan.dao.impl.ElasticSearchNameDAO;
 import net.canadensys.dataportal.vascan.model.NameConceptModelIF;
 import net.canadensys.dataportal.vascan.model.NameConceptTaxonModel;
 import net.canadensys.query.LimitedResult;
 
 import org.apache.commons.io.FileUtils;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.indices.IndexMissingException;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  * Test Coverage : 
  * -Search different names on ElasticSearch server
  * @author canadensys
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "/test-spring.xml" })
 public class NameDAOTest {
 	
 	@Autowired
 	private ElasticSearchTestInstance elasticSearchTestInstance;
 	
 	@Autowired
 	private Client client;
 	
 	@Autowired
 	private NameDAO nameDAO;
 	
 	@Before
 	public void setupES() throws Exception{
 		try{
 			client.admin().indices().prepareDelete("vascan").execute().actionGet();
 		}
 		catch(IndexMissingException imEx){}//ignore
 		
 		client.admin().indices().prepareCreate("vascan")
 		    .setSource(FileUtils.readFileToString(new File("script/vascan/vascan_index_creation.txt")))
 		    .execute()
 		    .actionGet();
 
 		client.prepareIndex("vascan", "taxon", "951")
 		        .setSource(jsonBuilder()
 		                    .startObject()
 		                        .field("taxonname", "Carex")
 		                        .field("status", "accepted")
 		                        .field("namehtml", "<em>Carex</em>")
 		                        .field("namehtmlauthor", "<em>Carex</em> Linnaeus")
 		                        .field("rankname", "genus")
 		                    .endObject()
 		                  )
 		        .execute()
 		        .actionGet();
 
 		client.prepareIndex("vascan", "taxon", "4864")
         .setSource(jsonBuilder()
                     .startObject()
                         .field("taxonname", "Carex feta")
                         .field("status", "accepted")
                         .field("namehtml", "<em>Carex feta</em>")
                         .field("namehtmlauthor", "<em>Carex feta</em> L.H. Bailey")
                         .field("rankname", "species")
                     .endObject()
                   )
         .execute()
         .actionGet();
 		
 		//Add a hybrid
 		client.prepareIndex("vascan", "taxon", "23238")
         .setSource(jsonBuilder()
                     .startObject()
                         .field("taxonname", "×Achnella")
                         .field("status", "accepted")
                         .field("namehtml", "<em>×Achnella</em>")
                         .field("namehtmlauthor", "<em>×Achnella</em> Barkworth")
                         .field("parentid", 746)
                         .field("parentnamehtml", "<em>Stipinae</em>")
                     .endObject()
                   )
         .execute()
         .actionGet();
 		
 		client.prepareIndex("vascan", "taxon", "1941")
         .setSource(jsonBuilder()
                     .startObject()
                         .field("taxonname", "Carex straminea var. mixta")
                         .field("status", "synonym")
                         .field("namehtml", "<em>Carex straminea</em> var. <em>mixta</em>")
                         .field("namehtmlauthor", "<em>Carex straminea</em> var. <em>mixta</em> L.H. Bailey")
                         .field("parentid", 864)
                         .field("parentnamehtml", "<em>Carex feta</em>")
                     .endObject()
                   )
         .execute()
         .actionGet();
 		
 		client.prepareIndex("vascan", "vernacular", "3")
         .setSource(jsonBuilder()
                     .startObject()
                         .field("taxonid", 7174)
                         .field("taxonnamehtml", "<em>Picea mariana</em>")
                         .field("vernacularname", "épinette")
                     .endObject()
                   )
         .execute()
         .actionGet();
 		//refresh the index
 		client.admin().indices().refresh(refreshRequest()).actionGet();
 	}
 	
 	@Test
 	public void testSearch(){
 		//Test asciifolding filter
 		LimitedResult<List<NameConceptModelIF>> nameModeListLR = nameDAO.search("epi");
 		assertEquals(1,nameModeListLR.getRows().size());
 		assertEquals(1,nameModeListLR.getTotal_rows());
 		assertEquals(new Integer(7174), nameModeListLR.getRows().get(0).getTaxonId());
 		
 		//Test search using carex f
 		List<NameConceptModelIF> nameModeList = nameDAO.searchTaxon("carex f");
 		//make sure Carex feta is the first element
 		assertEquals("<em>Carex feta</em> L.H. Bailey",((NameConceptTaxonModel)nameModeList.get(0)).getNamehtmlauthor());
 		//make sure Carex alone (951) is not there
 		boolean carexFound = false;
 		for(NameConceptModelIF curr : nameModeList){
 			if(curr.getTaxonId().intValue() == 951){
 				carexFound = true;
 			}
 		}
 		assertFalse(carexFound);
 		
 		//Test search carex
 		nameModeListLR = nameDAO.search("carex");
 		assertEquals(new Integer(951), nameModeListLR.getRows().get(0).getTaxonId());
 		//make sure other carex are returned (carex feta)
 		assertTrue(nameModeListLR.getRows().size() > 1);
 		
 		//Test hybrids
 		//should work without the multiply sign
 		nameModeListLR = nameDAO.search("Achnella");
 		assertEquals(new Integer(23238), nameModeListLR.getRows().get(0).getTaxonId());
 		//should also work with the multiply sign
 		nameModeListLR = nameDAO.search("×Achnella");
 		assertEquals(new Integer(23238), nameModeListLR.getRows().get(0).getTaxonId());
 		
 		//Test with paging
 		//We should not use setPageSize outside testing
 		((ElasticSearchNameDAO)nameDAO).setPageSize(1);
 		nameModeListLR = nameDAO.search("care",0);
 		assertEquals(1,nameModeListLR.getRows().size());
 		nameModeListLR = nameDAO.search("care",1);
 		assertEquals(1,nameModeListLR.getRows().size());
 		
 		//Test searching for a vernacular on taxon index
 		nameModeList = nameDAO.searchTaxon("epi");
 		assertEquals(0,nameModeList.size());
 	}
 }
