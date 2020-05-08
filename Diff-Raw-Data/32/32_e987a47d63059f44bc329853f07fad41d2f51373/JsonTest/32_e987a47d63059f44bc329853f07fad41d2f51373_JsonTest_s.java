 package test.unit.torrent;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import torrent.Json;
 import torrent.linkStation.json.Torrent;
 import torrent.linkStation.json.TorrentList;
 
 import com.google.gson.Gson;
 
 public class JsonTest
 {
 
 	@Before
 	public void setUp() throws Exception
 	{
 	}
 
 	@After
 	public void tearDown() throws Exception
 	{
 	}
 
 	@Test
 	public void test_serialise()
 	{
 		Torrent torrent = new Torrent();
 		torrent.setCaption("test");
 		torrent.setDone(50);
 		
 		Json json = new Json();
 		
 		String jsonString = json.serialise(torrent);
 		
 		System.out.println(jsonString);	
 	}
 
 	@Test
 	public void test_Torrent_deserialise()
 	{
 		Gson gson = new Gson();
 		
 		String json = "{\"Caption\":\"test\",\"dl_rate\":0,\"Done\":50,\"Max_Connections\":0,\"Max_dl_rate\":0,\"Max_ul_rate\":0,\"Max_uploads\":0,\"Payload_download\":0,\"Payload_upload\":0,\"Peers_connected\":0,\"peers_total\":0,\"Seeds_connected\":0,\"Seeds_total\":0,\"Size\":0,\"Stopped\":0,\"Ul_rate\":0}";
 		
 		Torrent torrent = gson.fromJson(json, Torrent.class);
 		
 		Assert.assertTrue(torrent.getDone() == 50);
 		Assert.assertTrue(torrent.getCaption().equals("test"));
 	}
 	
 	@Test
 	public void test_TorrentList_deserialise()
 	{
 		Gson gson = new Gson();
 		
 		String json = "{\"torrents\" : [{\"caption\" : \"test\", \"distributed_copies\" : \"4.983838\", \"dl_rate\" : 3294, \"done\" : 2689301, \"hash\" : \"fe312ecd846c88a2148ad0be2c464b3b22b1e354\", \"max_connections\" : 50, \"max_dl_rate\" : -1, \"max_ul_rate\" : -1, \"max_uploads\" : 4, \"payload_download\" : 3797521, \"payload_upload\" : 3684140, \"peers_connected\" : 49, \"peers_total\" : 1257, \"private\" : 0, \"seeds_connected\" : 4, \"seeds_total\" : 97, \"size\" : 1264994828, \"state\" : \"downloading\", \"stopped\" : 0, \"ul_rate\" : 0}]}";
 		
 		TorrentList torrent = gson.fromJson(json, TorrentList.class);
 		
 		Assert.assertTrue(torrent.getTorrents().length == 1);
 		Assert.assertTrue(torrent.getTorrents()[0].getCaption().equals("test"));
 	}
 	
 	@Test
 	public void test_TorrentListWithTwoItems_deserialise()
 	{
 		Gson gson = new Gson();
 		
 		String json = "{\"torrents\" : [{\"caption\" : \"BBC.Rise.of.the.Continents.4of4.Eurasia.PDTV.XviD.AC3.MVGroup.org.avi\", \"distributed_copies\" : \"8.988456\", \"dl_rate\" : 137, \"done\" : 10905, \"hash\" : \"796634716da38d131dcf5c24e0479edd531f0cb4\", \"max_connections\" : 50, \"max_dl_rate\" : -1, \"max_ul_rate\" : -1, \"max_uploads\" : 4, \"payload_download\" : 13494, \"payload_upload\" : 0, \"peers_connected\" : 28, \"peers_total\" : 327, \"private\" : 0, \"seeds_connected\" : 8, \"seeds_total\" : 77, \"size\" : 725760748, \"state\" : \"downloading\", \"stopped\" : 0, \"ul_rate\" : 0}, {\"caption\" : \"Dexter.S08E01.720p.HDTV.x264-EVOLVE.mkv\", \"distributed_copies\" : \"3.850808\", \"dl_rate\" : 1886, \"done\" : 3826372, \"hash\" : \"fe312ecd846c88a2148ad0be2c464b3b22b1e354\", \"max_connections\" : 50, \"max_dl_rate\" : -1, \"max_ul_rate\" : -1, \"max_uploads\" : 4, \"payload_download\" : 5493835, \"payload_upload\" : 6520604, \"peers_connected\" : 48, \"peers_total\" : 3684, \"private\" : 0, \"seeds_connected\" : 3, \"seeds_total\" : 279, \"size\" : 1264994828, \"state\" : \"downloading\", \"stopped\" : 0, \"ul_rate\" : 646}]}";
 		
 		TorrentList torrent = gson.fromJson(json, TorrentList.class);
 		
 		Assert.assertTrue(torrent.getTorrents().length == 2);
 	}
 }
