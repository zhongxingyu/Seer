 package org.atlasapi.feeds.lakeview;
 
 import static org.junit.Assert.*;
 import nu.xom.Element;
 import nu.xom.Elements;
 
 import org.atlasapi.feeds.xml.XMLNamespace;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Policy.Platform;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableSet;
 
 public class LakeviewFeedCompilerTest {
 
     private static final XMLNamespace LAKEVIEW = new XMLNamespace("", "http://schemas.microsoft.com/Lakeview/2013/07/01/ingestion");
 
 	@Test
 	public void testBrandAtomUri() {
 		LakeviewFeedCompiler feedCompiler = new LakeviewFeedCompiler(null, false, true);
 		assertEquals("https://xbox.channel4.com/pmlsd/educating-essex/4od.atom", 
				feedCompiler.brandAtomUri("http://www.channel4.com/programmes/educating-essex"));
 	}
 	
 	@Test
 	public void testSeriesAtomUri() {
 		LakeviewFeedCompiler feedCompiler = new LakeviewFeedCompiler(null, false, true);
 		assertEquals("https://xbox.channel4.com/pmlsd/educating-essex/4od.atom#series-1", 
				feedCompiler.seriesAtomUri("http://www.channel4.com/programmes/educating-essex/episode-guide/series-1"));
 	}
 	
 	@Test
 	public void testNonGenericSeriesTitleGenerated() {
         String seriesTitle = "Lord of the Wrongs";
 	    Element element = getSeriesElement(seriesTitle, 12, false);
 	    Elements titles = element.getChildElements("Title", LAKEVIEW.getUri());
 	    
 	    assertEquals("Should have exactly one title", 1, titles.size());
 	    assertEquals("Title should be as set", seriesTitle, titles.get(0).getValue());
 	}
 
 	@Test
 	public void testGenericSeriesTitleGenerated() {
 	    String seriesTitle = "Lord of the Wrongs";
 	    Element element = getSeriesElement(seriesTitle, 12, true);
 	    Elements titles = element.getChildElements("Title", LAKEVIEW.getUri());
 
 	    assertEquals("Should have exactly one title", 1, titles.size());
 	    assertEquals("Title should be generic", "Series 12", titles.get(0).getValue());
 	}
 
 	private Element getSeriesElement(String seriesTitle, int seriesNumber, boolean genericTitleEnabled) {
         LakeviewFeedCompiler feedCompiler = new LakeviewFeedCompiler(null, genericTitleEnabled, true);
         Series series = new Series("seriesUri", "seriesCurie", null);
        series.addAliasUrl("http://www.channel4.com/programmes/brand/episode-guide/series-1");
         Brand parent = new Brand("brandUri", "brandCurie", null);
        parent.addAliasUrl("http://www.channel4.com/programmes/brand");
         series.setTitle(seriesTitle);
         series.withSeriesNumber(seriesNumber);
         
         return feedCompiler.createSeriesElem(series, parent, new DateTime(), null);
 	}
 	
 	@Test
 	public void testNonGenericEpisodeTitleGenerated() {
 	    String episodeTitle="The One That Was Funny";
 	    
         Element element = getEpisodeElement(episodeTitle, 3012, false);
         Elements titles = element.getChildElements("Title", LAKEVIEW.getUri());
         
         assertEquals("Should have exactly one title", 1, titles.size());
         assertEquals("Title should be as set", episodeTitle, titles.get(0).getValue());
 	}
 
 	@Test
 	public void testGenericEpisodeTitleGenerated() {
 	    String episodeTitle="The One That Was Funny";
 
 	    Element element = getEpisodeElement(episodeTitle, 3012, true);
 	    Elements titles = element.getChildElements("Title", LAKEVIEW.getUri());
 
 	    assertEquals("Should have exactly one title", 1, titles.size());
 	    assertEquals("Title should be generic", "Episode 3012", titles.get(0).getValue());
 	}
 
     private Element getEpisodeElement(String episodeTitle, int episodeNumber, boolean genericTitleEnabled) {
         LakeviewFeedCompiler feedCompiler = new LakeviewFeedCompiler(null, genericTitleEnabled, true);
 	    Episode episode = new Episode("http://www.channel4.com/programmes/hierarchical-uri/episode-guide/series-1/episode-1", "episodeCurie", null);
 	    Brand container = new Brand("brandUri", "brandCurie", null);
        container.addAliasUrl("http://www.channel4.com/programmes/brand");
 	    episode.setTitle(episodeTitle);
 	    episode.setEpisodeNumber(episodeNumber);
 	    
 	    Series series = new Series("seriesUri", "seriesCurie", null);
        series.addAliasUrl("http://www.channel4.com/programmes/brand/episode-guide/series-1");
 	    
 	    Version version = new Version();
 	    Encoding encoding = new Encoding();
 	    Location location = new Location();
 	    location.setPolicy(new Policy()
 	                .withPlatform(Platform.XBOX)
 	                .withAvailabilityStart(new DateTime())
 	                .withAvailabilityEnd(new DateTime()));
 	    location.setTransportType(TransportType.APPLICATION);
 	    
 	    encoding.setAvailableAt(ImmutableSet.of(location));
 	    version.setManifestedAs(ImmutableSet.of(encoding));
 	    episode.setVersions(ImmutableSet.of(version));
 	    episode.setContainer(container);
 	    
 	    return feedCompiler.createEpisodeElem(episode, container, series, new DateTime(), null);
     }
     
     @Test
     public void testGeneratesCorrectIdsForProgrammeIdUri() {
         
         LakeviewFeedCompiler feedCompiler = new LakeviewFeedCompiler(null, true, true);
         Episode episode = new Episode();
         episode.setCanonicalUri("http://www.channel4.com/programmes/55103/175");
         episode.addAliasUrl("http://www.channel4.com/programmes/hollyoaks/episode-guide/series-22/episode-175");
         episode.setTitle("Title");
         episode.setEpisodeNumber(3);
         
         Brand container = new Brand("brandUri", "brandCurie", null);
         
         Version version = new Version();
         Encoding encoding = new Encoding();
         Location location = new Location();
         location.setUri("https://ais.channel4.com/asset/3567007");
         location.setPolicy(new Policy()
                     .withPlatform(Platform.XBOX)
                     .withAvailabilityStart(new DateTime())
                     .withAvailabilityEnd(new DateTime()));
         location.setTransportType(TransportType.LINK);
         
         encoding.setAvailableAt(ImmutableSet.of(location));
         version.setManifestedAs(ImmutableSet.of(encoding));
         episode.setVersions(ImmutableSet.of(version));
         episode.setContainer(container);
        
 //        
 //        Element elem = feedCompiler.createEpisodeElem(episode, container, new DateTime(), null);
 //        
 //        assertEquals("http://channel4.com/en-GB/TVEpisode/hollyoaks-series-22-episode-175", 
 //            elem.getFirstChildElement("ItemId", LAKEVIEW.getUri()).getValue());
 //        assertEquals("http://www.channel4.com/programmes/hollyoaks/episode-guide/series-22/episode-175.atom",
 //            elem.getFirstChildElement("PublicWebUri", LAKEVIEW.getUri()).getValue());
 //        assertEquals("https://xbox.channel4.com/pmlsd/hollyoaks/4od.atom#3567007",
 //            elem.getFirstChildElement("ApplicationSpecificData", LAKEVIEW.getUri()).getValue());
         
     }
     
 }
