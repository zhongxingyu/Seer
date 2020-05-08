 package com.mmounirou.spotiboard.spotify;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.Maps;
 import com.google.common.io.Files;
 import com.mmounirou.spotiboard.billboard.Track;
 
 public class TrackCache implements Closeable
 {
 	private Map<Track, String> m_cache = Maps.newHashMap();
 	private OutputStreamWriter stream;
 
 	public TrackCache() throws IOException
 	{
 		File cacheDir = new File(FileUtils.getTempDirectory(), "spotify");
 		cacheDir.mkdirs();
 		File cacheFile = new File(cacheDir, "hrefCache");
 		if (cacheFile.exists())
 		{
 			List<String> strExtendedTracks = Files.readLines(cacheFile, Charsets.UTF_8);
 			for (String strExtendedTrack : strExtendedTracks)
 			{
 				String[] extendedTrackSplitted = strExtendedTrack.split(",");
 				String m_artist = extendedTrackSplitted[0];
 				String m_song = extendedTrackSplitted[1];
 				String m_href = extendedTrackSplitted[2];
 				Track track = new Track(m_artist, m_song);
 				m_cache.put(track, m_href);
 			}
 		}
 
 		stream = Files.newWriterSupplier(cacheFile, Charsets.UTF_8, true).getOutput();
 	}
 
 	public String get(Track track)
 	{
 		return m_cache.get(track);
 	}
 
 	public void put(Track track, String strHref)
 	{
 		m_cache.put(track, strHref);
 		try
 		{
 			stream.write(String.format("%s,%s,%s\n", track.getArtist(), track.getSong(), strHref));
 		} catch (IOException e)
 		{
 			// just a try write
 		}
 	}
 
 	@Override
 	public void close() throws IOException
 	{
 		stream.close();
 	}
 
 }
