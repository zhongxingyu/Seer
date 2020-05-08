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
 
 		//TODO : move data to external file
 		client.prepareIndex("vascan", "taxon", "951")
 		        .setSource(jsonBuilder()
 		                    .startObject()
 		                        .field("taxonname", "Carex")
 		                        .field("sortname", "Carex")
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
                         .field("sortname", "Carex feta")
                         .field("status", "accepted")
                         .field("namehtml", "<em>Carex feta</em>")
                         .field("namehtmlauthor", "<em>Carex feta</em> L.H. Bailey")
                         .field("rankname", "species")
                     .endObject()
                   )
         .execute()
         .actionGet();
 		
 		//add taxon with genus = epithet
 		client.prepareIndex("vascan", "taxon", "26305")
         .setSource(jsonBuilder()
                     .startObject()
                         .field("taxonname", "Carex Carex bigelowii × Carex saxatilis var. rhomalea")
                         .field("sortname", "Carex Carex bigelowii × Carex saxatilis var. rhomalea")
                         .field("status", "synonym")
                         .field("namehtml", "<em>Carex Carex bigelowii × Carex saxatilis var. rhomalea</em>")
                         .field("namehtmlauthor", "<em>Carex Carex bigelowii × Carex saxatilis var. rhomalea</em>")
                         .field("rankname", "variety")
                     .endObject()
                   )
         .execute()
         .actionGet();
 		
 		//Add a hybrid
 		client.prepareIndex("vascan", "taxon", "23238")
         .setSource(jsonBuilder()
                     .startObject()
                         .field("taxonname", "×Achnella")
                         .field("sortname", "×Achnella")
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
                         .field("sortname", "Carex straminea var. mixta")
                         .field("status", "synonym")
                         .field("namehtml", "<em>Carex straminea</em> var. <em>mixta</em>")
                         .field("namehtmlauthor", "<em>Carex straminea</em> var. <em>mixta</em> L.H. Bailey")
                         .field("parentid", 864)
                         .field("parentnamehtml", "<em>Carex feta</em>")
                     .endObject()
                   )
         .execute()
         .actionGet();
 		
 		client.prepareIndex("vascan", "taxon", "5064")
         .setSource(jsonBuilder()
                     .startObject()
                         .field("taxonname", "Carex sabulosa")
                         .field("sortname", "Carex sabulosa")
                         .field("status", "accepted")
                         .field("namehtml", "<em>Carex sabulosa</em>")
                         .field("namehtmlauthor", "<em>Carex sabulosa</em> Turczaninow ex Kunth")
                         .field("parentid", 2096)
                         .field("parentnamehtml", "<em>Carex</em> sect. <em>Racemosae</em>")
                     .endObject()
                   )
         .execute()
         .actionGet();
 		
 		//add a synonym with 2 parents, not common but legit
 		client.prepareIndex("vascan", "taxon", "101010")
         .setSource(jsonBuilder()
                     .startObject()
                         .field("taxonname", "SynonymWithTwoParents")
                         .field("sortname", "SynonymWithTwoParents")
                         .field("status", "synonym")
                         .field("namehtml", "<em>SynonymWithTwoParents</em>")
                         .field("namehtmlauthor", "<em>SynonymWithTwoParents</em> Turczaninow ex Kunth")
                         .array("parentid", 2096,2097)
                         .array("parentnamehtml", "<em>Carex</em> sect. <em>Racemosae</em>","<em>Carex</em> sect. <em>Racemosae</em>")
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
                         .field("sortname", "épinette")
                     .endObject()
                   )
         .execute()
         .actionGet();
 		
 		client.prepareIndex("vascan", "vernacular", "9583")
         .setSource(jsonBuilder()
                     .startObject()
                         .field("taxonid", 4912)
                         .field("taxonnamehtml", "<em>Carex illota</em>")
                         .field("vernacularname", "carex sali")
                         .field("sortname", "carex sali")
                     .endObject()
                   )
         .execute()
         .actionGet();
 		
 		client.prepareIndex("vascan", "vernacular", "25445")
         .setSource(jsonBuilder()
                     .startObject()
                         .field("taxonid", 9208)
                         .field("taxonnamehtml", "<em>Acer palmatum</em>")
                         .field("vernacularname", "Japanese maple")
                         .field("sortname", "Japanese maple")
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
 		assertEquals("Ascii Folding, search vernacular with accent",new Integer(7174), nameModeListLR.getRows().get(0).getTaxonId());
 		
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
 		//make sure carex the genus get the higher score
 		assertTrue(nameModeListLR.getRows().get(0).getScore() > nameModeListLR.getRows().get(1).getScore());
		//same test with searchTaxon
		nameModeList = nameDAO.searchTaxon("carex");
		System.out.println("Score1."+nameModeList.get(0).getScore());
		System.out.println("Score1."+nameModeList.get(1).getScore());
		assertTrue(nameModeList.get(0).getScore() > nameModeList.get(1).getScore());
 		
 		//Search for carex feta using the genus first letter
 		nameModeListLR = nameDAO.search("C. feta");
 		assertEquals(new Integer(4864), nameModeListLR.getRows().get(0).getTaxonId());
 		//same test with searchTaxon
 		nameModeList = nameDAO.searchTaxon("C. feta");
 		assertEquals(new Integer(4864), nameModeList.get(0).getTaxonId());
 		
 		//Search using the epithet
 		nameModeListLR = nameDAO.search("sabulosa");
 		assertEquals(new Integer(5064), nameModeListLR.getRows().get(0).getTaxonId());
 		//same test with searchTaxon
 		nameModeList = nameDAO.searchTaxon("sabulosa");
 		assertEquals(new Integer(5064), nameModeList.get(0).getTaxonId());
 		
 		//Test hybrids
 		//should work without the multiply sign
 		nameModeListLR = nameDAO.search("Achnella");
 		assertEquals("Search hybrid without symbol",new Integer(23238), nameModeListLR.getRows().get(0).getTaxonId());
 		//should also work with the multiply sign
 		nameModeListLR = nameDAO.search("×Achnella");
 		assertEquals("Search hybrid with symbol",new Integer(23238), nameModeListLR.getRows().get(0).getTaxonId());
 		
 		//Test searching for a vernacular on taxon index
 		nameModeList = nameDAO.searchTaxon("epi");
 		assertEquals(0,nameModeList.size());
 		
 		//Test vernacular
 		nameModeListLR = nameDAO.search("japanese m");
 		assertEquals(1,nameModeListLR.getRows().size());
 		nameModeListLR = nameDAO.search("maple");
 		assertEquals(1,nameModeListLR.getRows().size());
 		
 		//Test to make sure that taxon and vernacular are sorted correctly
 		nameModeListLR = nameDAO.search("carex");
 		int idx=0;
 		int carexSabulosaIdx=0;
 		int carexSaliIdx=0;
 		for(NameConceptModelIF currName : nameModeListLR.getRows()){
 			if("Carex sabulosa".equals(currName.getName())){
 				carexSabulosaIdx = idx;
 			}
 			else if("carex sali".equals(currName.getName())){
 				carexSaliIdx = idx;
 			}
 			idx++;
 		}
 		assertTrue("taxon and vernacular order",carexSabulosaIdx < carexSaliIdx);
 		
 		//test a synonym with 2 parents
 		nameModeListLR = nameDAO.search("SynonymWithTwoParents");
 		assertEquals(new Integer(101010), nameModeListLR.getRows().get(0).getTaxonId());
 		assertFalse(((NameConceptTaxonModel)nameModeListLR.getRows().get(0)).hasSingleParent());
 		
 		//Test with paging, do this one last since we change the behavior of the nameDAO
 		//We should not use setPageSize outside testing
 		((ElasticSearchNameDAO)nameDAO).setPageSize(1);
 		nameModeListLR = nameDAO.search("care",0);
 		assertEquals(1,nameModeListLR.getRows().size());
 		nameModeListLR = nameDAO.search("care",1);
 		assertEquals(1,nameModeListLR.getRows().size());
 	}
 }
