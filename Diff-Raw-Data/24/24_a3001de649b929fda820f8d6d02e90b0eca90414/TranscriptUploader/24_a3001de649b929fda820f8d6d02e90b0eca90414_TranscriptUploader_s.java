 package transcript;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.google.gdata.client.Service.GDataRequest;
 import com.google.gdata.client.youtube.*;
 import com.google.gdata.data.youtube.*;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 
 public class TranscriptUploader {
 
 	private String playlistId, username, password;
 	private YouTubeService service;
 	private ArrayList<String> videoIds, transcripts;
 	private ArrayList<File> txts;
 
 	public static final String CAPTION_FEED_URL_FORMAT = "http://gdata.youtube.com/feeds/api/videos/%s/captions";
	public static final String DEVELOPER_KEY = "AI39si5Sfx7cQ-LXq6ERi31r5K19y1x5w94Cnirtp1Vza30kDELcLFCr6LjoYDfplqlpRia19SBYn8ZUffQMXtwGbjuc2ezq0w";
	public static final String CLIENT_ID = "ol-youtube";
 	public static final String CAPTION_FAILURE_TAG = "Caption Upload Failed";
 	public static final String PLAYLIST_FEED_URL_FORMAT = "http://gdata.youtube.com/feeds/api/users/%s/playlists";
 
 	public TranscriptUploader(String username, String password)
 			throws MalformedURLException, IOException, ServiceException {
 
 		transcripts = new ArrayList<String>();
 		videoIds = new ArrayList<String>();
 
 		this.username = username;
 		this.password = password;
 		service = new YouTubeService(CLIENT_ID, DEVELOPER_KEY);
 		service.setUserCredentials(this.username, this.password);
 
 		System.out.println("Authenticated");
 	}
 
 	@SuppressWarnings("unchecked")
 	public int parseTranscripts(File folder) throws IOException {
 		transcripts.clear();
 
 		txts = new ArrayList<File>(Arrays.asList(folder.listFiles()));
 
 		Collections.sort(txts, new AlphanumComparator());
 
 		for (File txt : txts) {
 			StringBuffer buffer = new StringBuffer();
 
 			FileInputStream fis = new FileInputStream(txt);
 			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
 
 			Reader in = new BufferedReader(isr);
 			int ch;
 			while ((ch = in.read()) > -1) {
 				buffer.append((char) ch);
 			}
 			in.close();
 			String txtString = buffer.toString();
 
 			transcripts.add(txtString);
 		}
 
 		return transcripts.size();
 	}
 
 	public int parseTranscripts(File[] files) throws IOException {
 		transcripts.clear();
 
 		txts = new ArrayList<File>(Arrays.asList(files));
 
 		Collections.sort(txts, new AlphanumComparator());
 
 		for (File txt : txts) {
 			StringBuffer buffer = new StringBuffer();
 
 			FileInputStream fis = new FileInputStream(txt);
 			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
 
 			Reader in = new BufferedReader(isr);
 			int ch;
 			while ((ch = in.read()) > -1) {
 				buffer.append((char) ch);
 			}
 			in.close();
 			String txtString = buffer.toString();
 
 			transcripts.add(txtString);
 			System.out.println(txt.getName());
 		}
 
 		System.out.println(transcripts.size() + " Transcripts Found");
 
 		return transcripts.size();
 	}
 
 	public List<PlaylistLinkEntry> getPlaylistLinkEntries()
 			throws MalformedURLException, IOException, ServiceException {
 		String playlistUrl = String.format(PLAYLIST_FEED_URL_FORMAT, "default");
 
 		PlaylistLinkFeed feed = service.getFeed(new URL(playlistUrl
 				+ "?max-results=50"), PlaylistLinkFeed.class);
 
 		List<PlaylistLinkEntry> playlistLinkEntries = feed.getEntries();
 
 		Collections
 				.sort(playlistLinkEntries, new PlaylistLinkEntryComparator());
 
 		return playlistLinkEntries;
 	}
 
 	public int parsePlaylist(PlaylistLinkEntry chosenPlaylist)
 			throws MalformedURLException, IOException, ServiceException {
 		String playlistUrl = chosenPlaylist.getFeedUrl();
 
 		PlaylistFeed playlistFeed = service.getFeed(new URL(playlistUrl
 				+ "?max-results=50"), PlaylistFeed.class);
 
 		videoIds.clear();
 
 		for (PlaylistEntry entry : playlistFeed.getEntries()) {
 			videoIds.add(entry.getMediaGroup().getVideoId());
 		}
 
 		int count = 1;
 		while (playlistFeed.getEntries().size() == 50) {
 			playlistFeed = service.getFeed(new URL(playlistUrl
 					+ "?max-results=50" + "&start-index=" + (count * 50 + 1)),
 					PlaylistFeed.class);
 			for (PlaylistEntry entry : playlistFeed.getEntries()) {
 				videoIds.add(entry.getMediaGroup().getVideoId());
 			}
 			count++;
 		}
 
 		return videoIds.size();
 	}
 
 	public ArrayList<String> getVideoIds() {
 		return videoIds;
 	}
 
	public boolean uploadAll(String contentLanguage) throws MalformedURLException, IOException,
			ServiceException {
 
 		boolean allSuccess = false;
 
 		if (videoIds.size() == transcripts.size()) {
 			allSuccess = true;
 			for (int i = 0; i < videoIds.size(); i++) {
				if (!uploadCaption(videoIds.get(i), transcripts.get(i), contentLanguage)) {
 					allSuccess = false;
 				}
 
 			}
 		} else {
 			System.out
 					.println("Number of Transcripts not equal Number of Videos in Playlist. Cannot Upload.");
 		}
 
 		if (allSuccess) {
 			System.out.println("All transcripts uploaded successfully.");
 		} else {
 			System.out.println("Errors occured. Check log.");
 		}
 
 		return allSuccess;
 	}
 
 	private boolean uploadCaption(String videoId, String captionTrack,
 			String contentLanguage) throws MalformedURLException, IOException,
 			ServiceException {
 
 		String captionsUrl = String.format(CAPTION_FEED_URL_FORMAT, videoId);
 
 		GDataRequest request = service
 				.createInsertRequest(new URL(captionsUrl));
 
 		request.setHeader("Content-Language", contentLanguage);
 		request.setHeader("Content-Type",
 				"application/vnd.youtube.timedtext; charset=UTF-8");
 		request.getRequestStream().write(captionTrack.getBytes("UTF-8"));
 		request.execute();
 
 		BufferedReader bufferedReader = new BufferedReader(
 				new InputStreamReader(request.getResponseStream()));
 		StringBuilder builder = new StringBuilder();
 		String line = null;
 
 		while ((line = bufferedReader.readLine()) != null) {
 			builder.append(line + "\n");
 		}
 
 		bufferedReader.close();
 
 		String responseBody = builder.toString();
 		if (responseBody.contains(CAPTION_FAILURE_TAG)) {
 			System.out.printf("%s transcript upload failed.\n", videoId);
 			return false;
 		} else {
 			System.out.printf("%s transcript upload success.\n", videoId);
 			return true;
 		}
 	}
 
 	public ArrayList<File> getTxts() {
 		return txts;
 	}
 
 	public boolean preUploadCheck() {
 		if (transcripts.size() == videoIds.size()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public class AlphanumComparator implements Comparator {
 
 		private final boolean isDigit(char ch) {
 			return ch >= 48 && ch <= 57;
 		}
 
 		/**
 		 * Length of string is passed in for improved efficiency (only need to
 		 * calculate it once)
 		 **/
 		private final String getChunk(String s, int slength, int marker) {
 			StringBuilder chunk = new StringBuilder();
 			char c = s.charAt(marker);
 			chunk.append(c);
 			marker++;
 			if (isDigit(c)) {
 				while (marker < slength) {
 					c = s.charAt(marker);
 					if (!isDigit(c))
 						break;
 					chunk.append(c);
 					marker++;
 				}
 			} else {
 				while (marker < slength) {
 					c = s.charAt(marker);
 					if (isDigit(c))
 						break;
 					chunk.append(c);
 					marker++;
 				}
 			}
 			return chunk.toString();
 		}
 
 		public int compare(Object o1, Object o2) {
 			if (!(o1 instanceof File) || !(o2 instanceof File)) {
 				return 0;
 			}
 			String s1 = ((File) o1).getName();
 			String s2 = ((File) o2).getName();
 
 			int thisMarker = 0;
 			int thatMarker = 0;
 			int s1Length = s1.length();
 			int s2Length = s2.length();
 
 			while (thisMarker < s1Length && thatMarker < s2Length) {
 				String thisChunk = getChunk(s1, s1Length, thisMarker);
 				thisMarker += thisChunk.length();
 
 				String thatChunk = getChunk(s2, s2Length, thatMarker);
 				thatMarker += thatChunk.length();
 
 				// If both chunks contain numeric characters, sort them
 				// numerically
 				int result = 0;
 				if (isDigit(thisChunk.charAt(0))
 						&& isDigit(thatChunk.charAt(0))) {
 					// Simple chunk comparison by length.
 					int thisChunkLength = thisChunk.length();
 					result = thisChunkLength - thatChunk.length();
 					// If equal, the first different number counts
 					if (result == 0) {
 						for (int i = 0; i < thisChunkLength; i++) {
 							result = thisChunk.charAt(i) - thatChunk.charAt(i);
 							if (result != 0) {
 								return result;
 							}
 						}
 					}
 				} else {
 					result = thisChunk.compareTo(thatChunk);
 				}
 
 				if (result != 0)
 					return result;
 			}
 
 			return s1Length - s2Length;
 		}
 	}
 
 	public class PlaylistLinkEntryComparator implements
 			Comparator<PlaylistLinkEntry> {
 
 		@Override
 		public int compare(PlaylistLinkEntry o1, PlaylistLinkEntry o2) {
 			int compareValue = o1.getTitle().getPlainText()
 					.compareToIgnoreCase(o2.getTitle().getPlainText());
 			return compareValue;
 		}
 
 	}
 }
