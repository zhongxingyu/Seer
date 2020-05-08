 package com.example.tvshowcrawler.test;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Parcel;
 import android.test.AndroidTestCase;
 
 import com.example.tvshowcrawler.EpisodeInfo;
 import com.example.tvshowcrawler.JSONUtils;
 import com.example.tvshowcrawler.TVShow;
 import com.example.tvshowcrawler.TVShows;
 import com.example.tvshowcrawler.TorrentItem;
 
 public class TVShowTest extends AndroidTestCase
 {
 	public TVShowTest()
 	{
 		super();
 	}
 
 	@Override
 	public void testAndroidTestCaseSetupProperly()
 	{
 		// test proper setup of tested class here
 		super.testAndroidTestCaseSetupProperly();
 	}
 
 	public void testEpisodeInfoParsing()
 	{
 		EpisodeInfo ei = EpisodeInfo.fromString("01x19^Secrets^Apr/03/2012");
 
 		assertEquals(ei.getTitle(), "Secrets");
 		assertEquals(ei.getSeason(), 1);
 		assertEquals(ei.getEpisode(), 19);
 
 		// test air time parsing and calculation
 		Calendar cal = new GregorianCalendar(2012, 3, 3);
 		cal.setTimeZone(TimeZone.getTimeZone("ET"));
 		cal.add(Calendar.HOUR_OF_DAY, 21);
 		cal.add(Calendar.HOUR_OF_DAY, 6);
 		// convert to local (UTC) time
 		Calendar localTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
 		localTime.setTimeInMillis(cal.getTimeInMillis());
 		cal = localTime;
 
 		assertEquals(ei.getAirTime(), cal);
 	}
 
 	public void testJSONSerialization()
 	{
 		String name = "New Girl";
 		int season = 1;
 		int episode = 17;
 
 		TVShow showOut = new TVShow(name, season, episode);
 		ArrayList<String> excludedKeyWords = new ArrayList<String>();
 		excludedKeyWords.add("keyword1");
 		excludedKeyWords.add("keyword2");
 		excludedKeyWords.add("keyword3");
 		showOut.setExcludedKeyWords(excludedKeyWords);
 
 		JSONObject joOut = null;
 		try
 		{
 			joOut = showOut.toJSONObject();
 		} catch (JSONException e)
 		{
 			fail(e.toString());
 		}
 		String jsonString = null;
 		try
 		{
 			jsonString = joOut.toString(4);
 		} catch (JSONException e)
 		{
 			fail(e.toString());
 		}
 		JSONObject joIn = null;
 		try
 		{
 			joIn = new JSONObject(jsonString);
 		} catch (JSONException e)
 		{
 			fail(e.toString());
 		}
 		TVShow showIn = new TVShow();
 		try
 		{
 			showIn.fromJSONObject(joIn);
 		} catch (JSONException e)
 		{
 			fail(e.toString());
 		}
 		assertEquals(showOut, showIn);
 
 		// test file IO
 		final String FILENAME = "tvshows-TEST.json";
 		TVShows tvShowsOut = new TVShows();
 		tvShowsOut.add(showOut);
 		try
 		{
 			JSONUtils.saveAppJSONFile(getContext(), FILENAME, tvShowsOut.toJSONObject());
 		} catch (JSONException e)
 		{
 			fail(e.toString());
 		}
 		File file = getContext().getFileStreamPath(FILENAME);
 		assertTrue(file.exists());
 
 		TVShows tvShowsIn = new TVShows();
 		JSONObject jo = JSONUtils.loadAppJSONFile(getContext(), FILENAME);
 		try
 		{
 			tvShowsIn.fromJSONObject(jo);
 		} catch (JSONException e)
 		{
 			fail(e.toString());
 		}
 		assertEquals(tvShowsOut, tvShowsIn);
 	}
 
 	public void testParcelable()
 	{
 		String name = "New Girl";
 		int season = 1;
 		int episode = 17;
 
 		TVShow showOut = new TVShow(name, season, episode);
 		ArrayList<String> excludedKeyWords = new ArrayList<String>();
 		excludedKeyWords.add("keyword1");
 		excludedKeyWords.add("keyword2");
 		excludedKeyWords.add("keyword3");
 		showOut.setExcludedKeyWords(excludedKeyWords);
 
 		Parcel parcel = Parcel.obtain();
 		showOut.writeToParcel(parcel, 0);
 		// done writing, now reset parcel for reading
 		parcel.setDataPosition(0);
 		// finish round trip
 		TVShow createFromParcel = TVShow.CREATOR.createFromParcel(parcel);
 
 		assertEquals(showOut, createFromParcel);
		parcel.recycle();
 	}
 
 	public void testQueryAllSites()
 	{
 		String name = "New Girl";
 		int season = 1;
 		int episode = 17;
 
 		TVShow show = new TVShow(name, season, episode);
 
 		assertEquals(show.getTorrentItems(), null);
 		show.queryAllSites(season, episode);
 		ArrayList<TorrentItem> torrentItems = show.getTorrentItems();
 		assertTrue(torrentItems != null);
 		assertTrue(torrentItems.size() > 0);
 		TorrentItem item = torrentItems.get(0);
 		assertTrue(item.getName().contains(name) || item.getName().contains(name.replaceAll(" ", ".")));
 		assertTrue(!item.getMagnetLink().isEmpty());
 		assertTrue(item.getMagnetLink().startsWith("magnet:?xt="));
 	}
 
 	public void testQueryKickAssTorrents()
 	{
 		String name = "New Girl";
 		int season = 1;
 		int episode = 17;
 
 		TVShow show = new TVShow(name, season, episode);
 
 		assertEquals(show.getTorrentItems(), null);
 		show.queryKickAssTorrents(season, episode);
 		ArrayList<TorrentItem> torrentItems = show.getTorrentItems();
 		assertTrue(torrentItems != null);
 		assertTrue(torrentItems.size() > 0);
 		TorrentItem item = torrentItems.get(0);
 		assertTrue(item.getName().contains(name));
 		assertTrue(!item.getMagnetLink().isEmpty());
 		assertTrue(item.getMagnetLink().startsWith("magnet:?xt="));
 	}
 
 	public void testQueryPirateBay()
 	{
 		String name = "New Girl";
 		int season = 1;
 		int episode = 17;
 
 		TVShow show = new TVShow(name, season, episode);
 
 		assertEquals(show.getTorrentItems(), null);
 		show.queryPirateBay(season, episode);
 		ArrayList<TorrentItem> torrentItems = show.getTorrentItems();
 		assertTrue(torrentItems != null);
 		assertTrue(torrentItems.size() > 0);
 		TorrentItem item = torrentItems.get(0);
 		assertTrue(item.getName().contains(name) || item.getName().contains(name.replaceAll(" ", ".")));
 		assertTrue(!item.getMagnetLink().isEmpty());
 		assertTrue(item.getMagnetLink().startsWith("magnet:?xt="));
 	}
 
 	public void testReadFromUrl()
 	{
 		try
 		{
 			URL url = new URL("http://de.wikipedia.org/wiki/Test");
 			String html = TVShow.readFromUrl(url);
 			assertTrue(html.length() > 0);
 		} catch (MalformedURLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public void testTVRageParsing()
 	{
 		// use "ended" show 30 Rock
 		String name = "30 Rock";
 		int season = 7;
 		int episode = 13;
 
 		TVShow show = new TVShow(name, season, episode);
 
 		show.updateTVRageInfo();
 
 		EpisodeInfo eInfo = show.getLastEpisode();
 		assertNotNull(eInfo);
 		assertEquals(7, eInfo.getSeason());
 		assertEquals(13, eInfo.getEpisode());
 		assertEquals("Last Lunch", eInfo.getTitle());
 
 		assertNull(show.getNextEpisode());
 	}
 
 	@Override
 	protected void setUp() throws Exception
 	{
 		// called before first test
 		super.setUp();
 	}
 
 	@Override
 	protected void tearDown() throws Exception
 	{
 		// called after last test
 		super.tearDown();
 	}
 }
