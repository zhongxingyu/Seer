 package cubrikproject.tud.likelines.service.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 
 import cubrikproject.tud.likelines.service.activator.Activator;
 import cubrikproject.tud.likelines.util.YouTubeComment;
 import cubrikproject.tud.likelines.util.YouTubeComment.TimePoint;
 import cubrikproject.tud.likelines.util.YouTubeDL;
 import cubrikproject.tud.likelines.util.YouTubeDL.YouTubeStream;
 import cubrikproject.tud.likelines.webservice.LikeLinesWebService;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.smila.utils.config.ConfigUtils;
 
 /**
  * The service responsible for launching indexing processes in the background.
  * 
  * @author R. Vliegendhart
  */
 public class LLIndexer implements cubrikproject.tud.likelines.service.interfaces.LLIndexer {
 	
 	private final static Log _log = LogFactory.getLog(LLIndexer.class);
 	private final String propertiesFile = getClass().getSimpleName() + ".properties";
 	private final Properties props;
 	
 	private final String ffmpegPath;
 	private final String motionActivityPath;
 	
 	private final String DEFAULT_FFMPEG = "ffmpeg";
 	private final String DEFAULT_MOTIONACTIVITY = "motionActivity";
 	final Map<String, String> secretKeys;
 	
 	Transcoder transcoder;
 	MotionActivityAnalyzer motionActivityAnalyzer;
 	
 	private final Set<String> indexedVideos = new HashSet<String>();  
 	
 	public LLIndexer() {
 		System.out.println(">>> LLIndexer: Reading (configuration/)" + Activator.BUNDLE_NAME + "/" + propertiesFile);
 		
 		props = loadProperties();
 		
 		String ffmpegPathFromProps = nullIfMissing( getProperty("ffmpegPath") );
 		String motionActivityPathFromProps = nullIfMissing( getProperty("motionActivityPath") );
 		
 		ffmpegPath = (ffmpegPathFromProps == null) ?
 				findOnPath(DEFAULT_FFMPEG) : ffmpegPathFromProps;
 		
 		motionActivityPath = (motionActivityPathFromProps == null) ?
 				findOnPath(DEFAULT_MOTIONACTIVITY) : motionActivityPathFromProps;	
 		
 		secretKeys = getSecretKeys();
 		testSecretKeys(secretKeys);
 		
 		transcoder = (ffmpegPath == null) ? null : new Transcoder(ffmpegPath);
 		motionActivityAnalyzer = (motionActivityPath == null) ? null : new MotionActivityAnalyzer(motionActivityPath);
 		
 		if (_log.isInfoEnabled()) {
 			_log.info("LLIndexer created using following setting");
 			_log.info(" -ffmpegPath=" + ffmpegPath);
 			_log.info(" -motionActivityPath=" + motionActivityPath);
 		}
 
 	}
 	
 	@Override
 	synchronized public void scheduleMCA(String videoId, LikeLinesWebService llServer) {
 		if (indexedVideos.contains(videoId)) {
 			System.err.println("scheduleMCA: Ignoring videoId " + videoId + " since it's being indexed or has been.");
 			return;
 		}
 		// For now, simplistic mechanism to prevent duplicate work:
 		indexedVideos.add(videoId); 
 		Thread thread = new Thread(new MCATask(videoId, llServer));
 		thread.start();
 	}
 	
 	public class MCATask implements Runnable {
 		
 		private final String videoId;
		private final String tmpPath;
 		private final LikeLinesWebService llServer;
 		private final String serverUrl; 
 		
 		public MCATask(String videoId, LikeLinesWebService llServer) {
 			this.videoId = videoId;
 			this.llServer = llServer;
 			this.serverUrl = llServer.serverUrl;
			tmpPath = System.getProperty("java.io.tmpdir");
 		}
 		
 		@Override
 		public void run() {
 			System.err.println("MCATask: Starting: " + videoId);
 			String secretKey = secretKeys.get(serverUrl);
 			
 			if (!videoId.startsWith("YouTube:")) {
 				System.err.println("MCATask only supports YouTube:<id>!");
 				return;
 			}
 			// future implementation should introduce branches for different URL schemes
 			final String youtubeId = videoId.substring("YouTube:".length());
 			
 			try {
 				final Map<String, String> videoInfo = YouTubeDL.getVideoInfo(youtubeId);
 				final boolean ageGate = YouTubeDL.isAgeRestrictedVideo(youtubeId);
 				
 				YouTubeStream firstStream = null;
 				for (YouTubeStream stream : YouTubeDL.getDownloadStreams(videoInfo, ageGate)) {
 					firstStream = stream;
 					break;
 				}
 				
 				if (firstStream == null) {
 					System.err.println("ERR: No stream found!");
 					return;
 				}
 				
				final String downloadPath = new File(tmpPath, "mca-" + youtubeId + firstStream.getExtension()).getPath();
 				firstStream.downloadTo(downloadPath);
 				
				final String convertPath = new File(tmpPath, "mca-" + youtubeId + "-conv.mpg").getPath();
 				boolean transcodeSuccess = transcoder.transcodeAndWait(downloadPath, convertPath) == 0;
 				
 				System.err.println("MCATask: Done converting, now starting motion analysis");
 				
 				final double[] motionScores = motionActivityAnalyzer.analyze(convertPath);
 				
 				System.err.println("MCATask: Now downloading comments");
 				
 				List<Integer> deeplinksList = new ArrayList<Integer>();
 				for (YouTubeComment cmnt : YouTubeComment.retrieveDeepLinkComments(youtubeId))
 					for (TimePoint deeplink : cmnt.deeplinks)
 						deeplinksList.add(deeplink.inSeconds);
 				
 				final double[] deeplinks = new double[deeplinksList.size()];
 				for (int i = 0; i < deeplinks.length; i++)
 					deeplinks[i] = deeplinksList.get(i);
 				
 				System.err.println("MCATask: Submitting MCA results to server: " + serverUrl);
 				
 				llServer.postMCA(videoId, "motionActivity", llServer.MCA_TYPE_CURVE, motionScores, secretKey);
 				llServer.postMCA(videoId, "deeplinks", llServer.MCA_TYPE_POINT, deeplinks, secretKey);
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 				throw new RuntimeException(e);
 			}
 			catch (InterruptedException e) {
 				e.printStackTrace();
 				throw new RuntimeException(e);
 			}
 			catch (RuntimeException e) {
 				e.printStackTrace();
 				throw e;
 			}
 			
 			System.err.println("MCATask: Done: " + videoId);
 		}
 	}
 	
 	/**
 	 * Loads the property file
 	 * 
 	 * @return The Properties or null if it does not exist
 	 */
 	private Properties loadProperties() {
 		Properties res = null;
 		try {
 			res = ConfigUtils.getConfigProperties(Activator.BUNDLE_NAME, propertiesFile);
 		} catch (Exception e) {
 			_log.warn(e);
 		}
 		return res;
 	}
 	
 	/**
 	 * Gets a property or null if the properties file or the property don't exist
 	 * 
 	 * @return The Properties or null if it does not exist
 	 */
 	private String getProperty(String propertyName) {
 		return props == null ? null : props.getProperty(propertyName);
 	}
 	
 	/**
 	 * Find an executable on the PATH
 	 * 
 	 * @param executable Name of the executable to search for
 	 * @return A path if found, otherwise null
 	 */
 	private String findOnPath(String executable) {
 		final boolean w32 = System.getProperty("os.name").startsWith("Windows");
 		final String exeSuffix = w32 ? ".exe" : "";
 		executable += exeSuffix;
 		
 		for (String path: System.getenv("PATH").split(File.pathSeparator)) {
 			File f = new File(path, executable);
 			if (f.exists())
 				return f.getAbsolutePath();
 		}
 		return null;
 	}
 	
 	/**
 	 * Maps a path to null if it does not exist.
 	 * 
 	 * @param path The path to check as a String
 	 * @return The path if it exists, otherwise null
 	 */
 	private String nullIfMissing(String path) {
 		return (path != null && new File(path).exists()) ? path : null;
 	}
 	
 	/**
 	 * Loads the secret keys from the properties file and stores it in a map
 	 * 
 	 * @return A mapping from LikeLines server URLs to their secret keys. 
 	 */
 	private Map<String,String> getSecretKeys() {
 		final HashMap<String, String> res = new HashMap<String,String>();
 		final String secretKeysProperty = getProperty("secretKeys");
 		
 		final String[] secretKeysAndServers = (secretKeysProperty==null) ? new String[0] : secretKeysProperty.split("\\s+");
 		
 		if (secretKeysAndServers.length % 2 != 0) {
 			_log.warn("LLIndexer: secretKeys property should contain an even number of items after splitting!");
 			return res;
 		}
 		
 		for (int i = 0; i < secretKeysAndServers.length; i += 2) {
 			final String secretKey = secretKeysAndServers[i];
 			final String server = LikeLinesWebService.ensureTrailingSlash(secretKeysAndServers[i + 1]);
 			res.put(server, secretKey);
 		}
 
 		return res;
 	}
 	
 	/**
 	 * Tests and removes secret keys that are invalid.
 	 * 
 	 * @param secretKeys
 	 */
 	private static void testSecretKeys(Map<String,String> secretKeys) {
 		final List<String> failedServers = new ArrayList<String>();
 		
 		for (final Entry<String, String> kv : secretKeys.entrySet()) {
 			final String server = kv.getKey();
 			final String key = kv.getValue();
 			
 			LikeLinesWebService llserver = null;
 			try {
 				llserver = new LikeLinesWebService(server);
 			} catch (MalformedURLException e) {
 				_log.warn("LLIndexer: testSecretKeys: malformed URL in server: " + server);
 				failedServers.add(server);
 				e.printStackTrace();
 				continue;
 			}
 			
 			try {
 				if (!llserver.testKey(key)) {
 					_log.warn("LLIndexer: testSecretKeys: incorrect key for server: " + server);
 					failedServers.add(server);
 					continue;
 				}
 					
 			} catch (IOException e) {
 				_log.warn("LLIndexer: testSecretKeys: I/O error, server down? " + server);
 				failedServers.add(server);
 				e.printStackTrace();
 				continue;
 			}
 		}
 		
 		for (final String server : failedServers)
 			secretKeys.remove(server);
 	}
 
 }
