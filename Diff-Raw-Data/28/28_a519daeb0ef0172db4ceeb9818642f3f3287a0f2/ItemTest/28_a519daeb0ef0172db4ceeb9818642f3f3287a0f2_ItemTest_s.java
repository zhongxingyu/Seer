 package gov.usgs.cida.coastalhazards.model;
 
 import gov.usgs.cida.coastalhazards.jpa.ItemManager;
 import gov.usgs.cida.coastalhazards.session.io.SessionIOException;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 import org.junit.Ignore;
 
 /**
  *
  * @author jordan
  */
 public class ItemTest {
     
    String itemJSON =  "{\"id\":\"abc123\"," +
 "            \"metadata\": \"testMetaUrl\"," +
 "            \"wfsService\": {\n" +
 "                    \"endpoint\": \"http://cida.usgs.gov/qa/DSASweb/geoserver/published/wfs\",\n" +
 "                    \"typeName\": \"published:KauaiE_shorelines\"\n" +
 "                },\n" +
 "            \"wmsService\": {\n" +
 "                    \"endpoint\": \"http://cida.usgs.gov/qa/DSASweb/geoserver/published/wms\",\n" +
 "                    \"layers\": \"KauaiE_shorelines\"\n" +
 "                }," +
 "            \"name\": \"Linear Regression Rate of shorelines in eastern Kauai, HI\",\n" +
 "            \"type\": \"historical\",\n" +
 "            \"attr\": \"LRR\",\n" +
 "            \"bbox\": [-159.35, 21.96, -159.29, 22.17],\n" +
 "            \"summary\": {" +
 "               \"tiny\": {" +
 "                   \"text\": \"Linear Regression Rate of shorelines in eastern Kauai, HI\"" +
 "               }," +
 "               \"medium\": {" +
 "                   \"title\": \"LRR for Kauai, HI\"," +
 "                   \"text\": \"This dataset includes shorelines ranging from 1927 to 2008 in the Kauai East coastal region from Pilaa to Nawiliwili. Linear Regression Rate is a shoreline change metric calculated using the Digital Shoreline Analysis System. Data sources: aerial photographs, coastal survey maps\"" +
 "               },\n" +
 "               \"full\": {\n" +
 "                   \"id\": \"124\"," +
 "                   \"title\": \"The Category 3 inundation element of National Assessment of Hurricane-Induced Coastal Erosion Hazards: Southeast Atlantic\"," +
 "                   \"text\": \"These data sets contain information on the probabilities of hurricane-induced erosion (collision, inundation and overwash) for each 1-km section of the Southeast Atlantic coast for category 1-5 hurricanes. The analysis is based on a storm-impact scaling model that uses observations of beach morphology combined with sophisticated hydrodynamic models to predict how the coast will respond to the direct landfall of category 1-5 hurricanes. Hurricane-induced water levels, due to both surge and waves, are compared to beach and dune elevations to determine the probabilities of three types of coastal change: collision (dune erosion), overwash, and inundation. Data on dune morphology (dune crest and toe elevation) and hydrodynamics (storm surge, wave setup and runup) are also included in this data set. As new beach morphology observations and storm predictions become available, this analysis will be updated to describe how coastal vulnerability to storms will vary in the future. The data presented here include the dune morphology observations, as derived from lidar surveys.\",\n" +
 "                   \"publications\": {" +
 "                       \"data\": [" +
 "                           {" +
 "                               \"id\": 23," +
 "                               \"title\": \"pub title\"," +
 "                               \"link\": \"pub link\"" +
 "                           }" +
 "                       ]" +
 "                   }," +
 "                   \"keywords\": \"this|is|just|a|pipe|delimited|list|of|things\"" +
 "               }" +
 "            }\n" +             
 "         }";
     
     @Test
     @Ignore //need to figure out how to do local db test
    public void testDB() throws SessionIOException {
         ItemManager manager = new ItemManager();
		String id;
		
		id = manager.persist(itemJSON);
		assertNotNull(id);
		
		Item item = manager.load(id);
         assertNotNull(item);
     }
 
     /**
      * Test of fromJSON method, of class Item.
      */
     @Test
     public void testFromJSON() {
        Item item = Item.fromJSON(itemJSON);
        assertThat(item.getId(), is(equalTo("abc123")));
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void testTypeEnum() {
         Item.ItemType type1 = Item.ItemType.valueOf("aggregation");
         assertThat(type1, is(equalTo(Item.ItemType.aggregation)));
         Item.ItemType type2 = Item.ItemType.valueOf("data");
         assertThat(type2, is(equalTo(Item.ItemType.data)));
         Item.ItemType type4 = Item.ItemType.valueOf("foo");
         assertThat(type4, is(not(equalTo(Item.ItemType.aggregation))));
     }
 }
