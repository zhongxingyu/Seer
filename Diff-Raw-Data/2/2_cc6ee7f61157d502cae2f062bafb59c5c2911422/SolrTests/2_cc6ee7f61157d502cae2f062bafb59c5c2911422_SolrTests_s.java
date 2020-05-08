 
 import junit.framework.TestCase;
 import org.junit.Assert;
 
 public class SolrTests extends TestCase{
 
 	public void test_basic()
 	{
 		Assert.assertEquals(1,1);
 	}
 	
 	public void test_GetDocFromSolrService_UnitTest()
 	{
 		SolrService solrService = new SolrService();
 		String searchString = "pink floyd";
 		SolrMasterArtist solrmasterArtist = solrService.searchMasterArtist(searchString);
 		Assert.assertNotNull(solrmasterArtist);
 	}
 	
 	public void test_GetDocFromSolrService_AcceptanceTest()
 	{
 		SolrService solrService = new SolrService();
 		String searchString = "pink floyd";
 		SolrMasterArtist solrmasterArtist = solrService.searchMasterArtist(searchString);
 		Assert.assertEquals("ma_34_447", solrmasterArtist.id);
 	}
 	
 	public void test_GetDocFromSolrService_mapsName_AcceptanceTest()
 	{
 		SolrService solrService = new SolrService();
 		String searchString = "pink floyd";
 		SolrMasterArtist solrmasterArtist = solrService.searchMasterArtist(searchString);
		Assert.assertEquals("pink floyd", solrmasterArtist.masterArtistName);
 	}
 	
 	public void test_GetDocFromSolrService_mapsSortName_AcceptanceTest()
 	{
 		SolrService solrService = new SolrService();
 		String searchString = "pink floyd";
 		SolrMasterArtist solrmasterArtist = solrService.searchMasterArtist(searchString);
 		Assert.assertEquals("pink floyd", solrmasterArtist.masterArtistSortName);
 	}
 	
 	public void test_GetDocFromSolrService_mapsUrl_AcceptanceTest()
 	{
 		SolrService solrService = new SolrService();
 		String searchString = "pink floyd";
 		SolrMasterArtist solrmasterArtist = solrService.searchMasterArtist(searchString);
 		Assert.assertEquals("pink-floyd", solrmasterArtist.masterArtistUrl);
 	}
 	
 	public void test_GetTop10DocumentsFromSolrService_AcceptanceTest()
 	{
 		SolrService solrService = new SolrService();
 		String searchString = "pink floyd";
 		SolrMasterArtist[] solrmasterArtist = solrService.searchMasterArtists(searchString);
 		Assert.assertEquals("ma_34_447", solrmasterArtist[0].id);
 		Assert.assertEquals("pink-floyd", solrmasterArtist[0].masterArtistUrl);
 		Assert.assertNotNull(solrmasterArtist[9].id);
 	}
 }
