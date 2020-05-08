 package com.mmounirou.spotiboard;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import javax.annotation.Nullable;
 import javax.swing.filechooser.FileSystemView;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.FluentIterable;
 import com.google.common.collect.Iterables;
 import com.mmounirou.spotiboard.billboard.BilboardChartRss;
 import com.mmounirou.spotiboard.billboard.ChartRssException;
 import com.mmounirou.spotiboard.billboard.Track;
 import com.mmounirou.spotiboard.spotify.SpotifyException;
 import com.mmounirou.spotiboard.spotify.SpotifyHrefQuery;
 import com.mmounirou.spotiboard.spotify.TrackCache;
 
 public class SpotiBoard
 {
 	public static final Logger LOGGER = Logger.getLogger(SpotiBoard.class);
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws ChartRssException 
 	 * @throws SpotifyException 
 	 */
 	public static void main(String[] args) throws IOException
 	{
 		if (args.length == 0)
 		{
 			System.err.println("usage : java -jar spotiboard.jar <charts-folder>");
 			return;
 		}
 
		final File resultDir = new File(FileSystemView.getFileSystemView().getDefaultDirectory(), args[0]);
 		resultDir.mkdirs();
 
 		Iterable<String> chartsRss = getCharts();
 
 		TrackCache cache = new TrackCache();
 		try
 		{
 			final SpotifyHrefQuery hrefQuery = new SpotifyHrefQuery(cache);
 			Iterable<String> results = FluentIterable.from(chartsRss).transform(new Function<String, String>()
 			{
 
 				@Override
 				@Nullable
 				public String apply(@Nullable String chartRss)
 				{
 
 					try
 					{
 
 						long begin = System.currentTimeMillis();
 						BilboardChartRss bilboardChartRss = BilboardChartRss.getInstance(chartRss);
 						Map<Track, String> trackHrefs = hrefQuery.getTrackHrefs(bilboardChartRss.getSongs());
 
 						File resultFile = new File(resultDir, bilboardChartRss.getTitle());
 						FileUtils.writeLines(resultFile, Charsets.UTF_8.displayName(), trackHrefs.values());
 
 						LOGGER.info(String.format("%s chart exported in %s in %d s", bilboardChartRss.getTitle(), resultFile.getAbsolutePath(),
 								(int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - begin)));
 
 					} catch (Exception e)
 					{
 						LOGGER.error(String.format("fail to export %s charts", chartRss), e);
 					}
 
 					return "";
 				}
 			});
 
 			// consume iterables
 			Iterables.size(results);
 
 		} finally
 		{
 			cache.close();
 		}
 
 	}
 
 	private static Iterable<String> getCharts() throws IOException
 	{
 		InputStream chartsStreams = SpotiBoard.class.getResourceAsStream("/billboard.charts");
 		try
 		{
 			List<String> readLines = IOUtils.readLines(chartsStreams, Charsets.UTF_8);
 			return Iterables.filter(readLines, new Predicate<String>()
 			{
 
 				@Override
 				public boolean apply(@Nullable String input)
 				{
 					return !StringUtils.startsWith(input, "#");
 				}
 			});
 
 		} finally
 		{
 			IOUtils.closeQuietly(chartsStreams);
 		}
 	}
 
 }
