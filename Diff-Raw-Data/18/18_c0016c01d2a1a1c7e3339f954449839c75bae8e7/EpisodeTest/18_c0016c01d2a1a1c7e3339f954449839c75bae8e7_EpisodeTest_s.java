 package uk.co.mdjcox.model;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.TestCase.fail;
 
 /** 
 * Episode Tester. 
 * 
 * @author <Authors name> 
 * @since <pre>Apr 19, 2013</pre> 
 * @version 1.0 
 */ 
 public class EpisodeTest { 
 
 @Before
 public void before() throws Exception { 
 } 
 
 @After
 public void after() throws Exception { 
 }
 
   @Test
   public void testCreateNullSourceId() {
     try {
       new Episode(null, "id", "programmeTitle", "episodeTitle", "series",
                                     "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                     "airTime", "channel", "category");
     } catch (NullPointerException e) {
       return;
     }
 
     fail("Should have thrown NullPointerException");
 
   }
 
   @Test
   public void testCreateNullId() {
     try {
       new Episode("sourceId", null, "programmeTitle", "episodeTitle", "series",
                                     "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                     "airTime", "channel", "category");
     } catch (NullPointerException e) {
       return;
     }
 
     fail("Should have thrown NullPointerException");
 
   }
 
   @Test
   public void testCreateNullProgrammeTitle() {
     try {
       new Episode("sourceId", "id", null, "episodeTitle", "series",
                                     "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                     "airTime", "channel", "category");
     } catch (NullPointerException e) {
       return;
     }
 
     fail("Should have thrown NullPointerException");
 
   }
 
 
   @Test
   public void testCreateNullEpisodeTitle() {
     try {
        new Episode("sourceId", "id", "programmeTitle", null, "series",
                                     "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                     "airTime", "channel", "category");
     } catch (NullPointerException e) {
       return;
     }
 
     fail("Should have thrown NullPointerException");
 
   }
 
   @Test
   public void testCreateNullSeries() {
     try {
       new Episode("sourceId", "id", "programmeTitle", "episodeTitle", null,
                                     "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                     "airTime", "channel", "category");
     } catch (NullPointerException e) {
       return;
     }
 
     fail("Should have thrown NullPointerException");
 
   }
 
   @Test
   public void testCreateNullEpisode() {
     try {
       new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                     null, "descripton", "iconUrl", "serviceUrl", "airDate",
                                     "airTime", "channel", "category");
     } catch (NullPointerException e) {
       return;
     }
 
     fail("Should have thrown NullPointerException");
 
   }
 
   @Test
   public void testCreateNullDescription() {
     try {
       new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                     "episode", null, "iconUrl", "serviceUrl", "airDate",
                                     "airTime", "channel", "category");
     } catch (NullPointerException e) {
       return;
     }
 
     fail("Should have thrown NullPointerException");
 
   }
 
   @Test
   public void testCreateNullIconUrl() {
     try {
       new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                     "episode", "descripton", null, "serviceUrl", "airDate",
                                     "airTime", "channel", "category");
     } catch (NullPointerException e) {
       return;
     }
 
     fail("Should have thrown NullPointerException");
 
   }
 
   @Test
   public void testCreateNullServiceUrl() {
     try {
       new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                     "episode", "descripton", "iconUrl", null, "airDate",
                                     "airTime", "channel", "category");
     } catch (NullPointerException e) {
       return;
     }
 
     fail("Should have thrown NullPointerException");
 
   }
 
   @Test
   public void testCreateNullAirDate() {
     try {
       new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                     "episode", "descripton", "iconUrl", "serviceUrl", null,
                                     "airTime", "channel", "category");
     } catch (NullPointerException e) {
       return;
     }
 
     fail("Should have thrown NullPointerException");
 
   }
 
   @Test
   public void testCreateNullAirTime() {
     try {
        new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                     "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                     null, "channel", "category");
     } catch (NullPointerException e) {
       return;
     }
 
     fail("Should have thrown NullPointerException");
 
   }
 
   @Test
   public void testCreateNullChannel() {
     try {
       new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                     "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                     "airTime", null, "category");
     } catch (NullPointerException e) {
       return;
     }
 
     fail("Should have thrown NullPointerException");
 
   }
 
   @Test
   public void testCreateNullCategory() {
     try {
       new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                     "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                     "airTime", "channel", null);
     } catch (NullPointerException e) {
       return;
     }
 
     fail("Should have thrown NullPointerException");
 
   }
 
   @Test
   public void testCreate() throws Exception {
     Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                   "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                   "airTime", "channel", "category");
     assertEquals("getSourceId", "sourceId", episode.getSourceId());
     assertEquals("getId", "id", episode.getId());
     assertEquals("getProgrammeTitle", "programmeTitle", episode.getProgrammeTitle());
     assertEquals("getEpisodeTitle", "episodeTitle", episode.getEpisodeTitle());
     assertEquals("getSeries", "series", episode.getSeries());
     assertEquals("getEpisode", "episode", episode.getEpisode());
     assertEquals("getDescription", "descripton", episode.getDescription());
     assertEquals("getIconUrl", "iconUrl", episode.getIconUrl());
     assertEquals("getServiceUrl", "serviceUrl", episode.getServiceUrl());
     assertEquals("getAirDate", "airDate", episode.getAirDate());
     assertEquals("getAirTime", "airTime", episode.getAirTime());
     assertEquals("getChannel", "channel", episode.getChannel());
     assertEquals("getCategory", "category", episode.getCategory());
   }
 
 /** 
 * 
 * Method: getSourceId() 
 * 
 */ 
 @Test
 public void testGetSourceId() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
     assertEquals("getSourceId", "sourceId", episode.getSourceId());
 
 }
 
 /** 
 * 
 * Method: setSourceId(String sourceId) 
 * 
 */ 
 @Test
 public void testSetSourceId() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   episode.setSourceId("sourceId2");
   assertEquals("setSourceId", "sourceId2", episode.getSourceId());
 
   try {
     episode.setSourceId(null);
   } catch (NullPointerException e) {
     return;
   }
 
   fail("Should have thrown NullPointerException");
 } 
 
 /** 
 * 
 * Method: getId() 
 * 
 */ 
 @Test
 public void testGetId() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   assertEquals("getId", "id", episode.getId());
 } 
 
 /** 
 * 
 * Method: setId(String id) 
 * 
 */ 
 @Test
 public void testSetId() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   episode.setId("id2");
   assertEquals("setId", "id2", episode.getId());
 
   try {
     episode.setId(null);
   } catch (NullPointerException e) {
     return;
   }
 
   fail("Should have thrown NullPointerException");
 } 
 
 /** 
 * 
 * Method: getAirDate() 
 * 
 */ 
 @Test
 public void testGetAirDate() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   assertEquals("getAirDate", "airDate", episode.getAirDate());
 }
 
 /** 
 * 
 * Method: setAirDate(String airDate) 
 * 
 */ 
 @Test
 public void testSetAirDate() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   episode.setAirDate("airDate2");
   assertEquals("setAirDate", "airDate2", episode.getAirDate());
 
   try {
     episode.setAirDate(null);
   } catch (NullPointerException e) {
     return;
   }
 
   fail("Should have thrown NullPointerException");
 } 
 
 /** 
 * 
 * Method: getAirTime() 
 * 
 */ 
 @Test
 public void testGetAirTime() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   assertEquals("getAirTime", "airTime", episode.getAirTime());
 } 
 
 /** 
 * 
 * Method: setAirTime(String airTime) 
 * 
 */ 
 @Test
 public void testSetAirTime() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   episode.setAirTime("airTime2");
   assertEquals("setAirTime", "airTime2", episode.getAirTime());
 
   try {
     episode.setAirTime(null);
   } catch (NullPointerException e) {
     return;
   }
 
   fail("Should have thrown NullPointerException");
 } 
 
 /** 
 * 
 * Method: getProgrammeTitle() 
 * 
 */ 
 @Test
 public void testGetProgrammeTitle() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   assertEquals("getProgrammeTitle", "programmeTitle", episode.getProgrammeTitle());
 } 
 
 /** 
 * 
 * Method: setProgrammeTitle(String programmeTitle) 
 * 
 */ 
 @Test
 public void testSetProgrammeTitle() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   episode.setProgrammeTitle("programmeTitle2");
   assertEquals("testSetProgrammeTitle", "programmeTitle2", episode.getProgrammeTitle());
 
   try {
     episode.setProgrammeTitle(null);
   } catch (NullPointerException e) {
     return;
   }
 
   fail("Should have thrown NullPointerException");
 } 
 
 /** 
 * 
 * Method: setEpisodeTitle(String episodeTitle) 
 * 
 */ 
 @Test
 public void testSetEpisodeTitle() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   episode.setEpisodeTitle("episodeTitle2");
   assertEquals("setEpisodeTitle", "episodeTitle2", episode.getEpisodeTitle());
 
   try {
     episode.setEpisodeTitle(null);
   } catch (NullPointerException e) {
     return;
   }
 
   fail("Should have thrown NullPointerException");
 } 
 
 /** 
 * 
 * Method: setDescription(String description) 
 * 
 */ 
 @Test
 public void testSetDescription() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   episode.setDescription("descripton2");
   assertEquals("setDescription", "descripton2", episode.getDescription());
 
   try {
     episode.setDescription(null);
   } catch (NullPointerException e) {
     return;
   }
 
   fail("Should have thrown NullPointerException");
 } 
 
 /** 
 * 
 * Method: setIconUrl(String iconUrl) 
 * 
 */ 
 @Test
 public void testSetIconUrl() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   episode.setIconUrl("iconUrl2");
   assertEquals("setIconUrl", "iconUrl2", episode.getIconUrl());
 
   try {
     episode.setIconUrl(null);
   } catch (NullPointerException e) {
     return;
   }
 
   fail("Should have thrown NullPointerException");
 } 
 
 /** 
 * 
 * Method: setServiceUrl(String serviceUrl) 
 * 
 */ 
 @Test
 public void testSetServiceUrl() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   episode.setServiceUrl("serviceUrl2");
   assertEquals("setServiceUrl", "serviceUrl2", episode.getServiceUrl());
 
   try {
     episode.setServiceUrl(null);
   } catch (NullPointerException e) {
     return;
   }
 
   fail("Should have thrown NullPointerException");
 } 
 
 /** 
 * 
 * Method: getIconUrl() 
 * 
 */ 
 @Test
 public void testGetIconUrl() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   assertEquals("getIconUrl", "iconUrl", episode.getIconUrl());
 } 
 
 /** 
 * 
 * Method: getEpisodeTitle() 
 * 
 */ 
 @Test
 public void testGetEpisodeTitle() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   assertEquals("getEpisodeTitle", "episodeTitle", episode.getEpisodeTitle());
 } 
 
 /** 
 * 
 * Method: getServiceUrl() 
 * 
 */ 
 @Test
 public void testGetServiceUrl() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   assertEquals("getServiceUrl", "serviceUrl", episode.getServiceUrl());
 } 
 
 /** 
 * 
 * Method: getDescription() 
 * 
 */ 
 @Test
 public void testGetDescription() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   assertEquals("getDescription", "descripton", episode.getDescription());
 } 
 
 /** 
 * 
 * Method: getChannel() 
 * 
 */ 
 @Test
 public void testGetChannel() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   assertEquals("getChannel", "channel", episode.getChannel());
 } 
 
 /** 
 * 
 * Method: setChannel(String channel) 
 * 
 */ 
 @Test
 public void testSetChannel() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   episode.setChannel("channel2");
   assertEquals("setChannel", "channel2", episode.getChannel());
 
   try {
     episode.setChannel(null);
   } catch (NullPointerException e) {
     return;
   }
 
   fail("Should have thrown NullPointerException");
 } 
 
 /** 
 * 
 * Method: getCategory() 
 * 
 */ 
 @Test
 public void testGetCategory() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   assertEquals("getCategory", "category", episode.getCategory());
 } 
 
 /** 
 * 
 * Method: setCategory(String category) 
 * 
 */ 
 @Test
 public void testSetCategory() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   episode.setCategory("category2");
   assertEquals("setCategory", "category2", episode.getCategory());
 
   try {
     episode.setCategory(null);
   } catch (NullPointerException e) {
     return;
   }
 
   fail("Should have thrown NullPointerException");
 } 
 
 /** 
 * 
 * Method: getSeries() 
 * 
 */ 
 @Test
 public void testGetSeries() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   assertEquals("getSeries", "series", episode.getSeries());
 } 
 
 /** 
 * 
 * Method: setSeries(String series) 
 * 
 */ 
 @Test
 public void testSetSeries() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   episode.setSeries("series2");
   assertEquals("setSeries", "series2", episode.getSeries());
 
   try {
     episode.setSeries(null);
   } catch (NullPointerException e) {
     return;
   }
 
   fail("Should have thrown NullPointerException");
 } 
 
 /** 
 * 
 * Method: getEpisode() 
 * 
 */ 
 @Test
 public void testGetEpisode() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   assertEquals("getEpisode", "episode", episode.getEpisode());
 } 
 
 /** 
 * 
 * Method: setEpisode(String episode) 
 * 
 */ 
 @Test
 public void testSetEpisode() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   episode.setEpisode("episode2");
   assertEquals("setEpisode", "episode2", episode.getEpisode());
 
   try {
     episode.setEpisode(null);
   } catch (NullPointerException e) {
     return;
   }
 
   fail("Should have thrown NullPointerException");
 } 
 
 /** 
 * 
 * Method: getPodcastTitle() 
 * 
 */ 
 @Test
 public void testGetPodcastTitle() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   assertEquals("getPodcastTitle", "episodeTitle - series - episode", episode.getPodcastTitle());
 
   episode = new Episode("sourceId", "id", "programmeTitle", "", "series",
                         "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                         "airTime", "channel", "category");
   assertEquals("getPodcastTitle", "programmeTitle - series - episode", episode.getPodcastTitle());
 
   episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "",
                         "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                         "airTime", "channel", "category");
   assertEquals("getPodcastTitle", "episodeTitle - episode", episode.getPodcastTitle());
 
   episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                         "", "descripton", "iconUrl", "serviceUrl", "airDate",
                         "airTime", "channel", "category");
   assertEquals("getPodcastTitle", "episodeTitle - series", episode.getPodcastTitle());
 }
 
 /** 
 * 
 * Method: toString() 
 * 
 */ 
 @Test
 public void testToString() throws Exception {
   Episode episode = new Episode("sourceId", "id", "programmeTitle", "episodeTitle", "series",
                                 "episode", "descripton", "iconUrl", "serviceUrl", "airDate",
                                 "airTime", "channel", "category");
   assertEquals("toString", "episodeTitle", episode.toString());
 } 
 
 
 } 
