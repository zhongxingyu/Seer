 package org.linter;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.Proxy;
 import java.net.URL;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.Inflater;
 import java.util.zip.InflaterInputStream;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.log4j.Logger;
 
 /**
  * Fetch a web page meta data by URL, performs all URL redirection and interaction
  * with ServiceParsers 
  */
 public class LintedPage {
 	
 	/**
 	 * Log4J Logger
 	 */
 	static private Logger logger = Logger.getLogger(LintedPage.class);
 	
 	/**
 	 * Pattern for matching specific parts of URLs
 	 */
 	private static final Pattern URL_PATTERN = Pattern.compile(
 			"^((\\w+)://)?" +
 			"((\\w+):?(\\w+)?@)?" + 
 			"([^/\\?:]+)" +
 			":?" +
 			"(\\d+)?" +
 			"(/?[^\\?]+)?" +
 			"\\??" +
 			"([^#]+)?" +
 			"#?(\\w*)");
 	
 	/**
 	 * Pattern for determining if a URL is full or relative
 	 */
 	private static final String RELATIVE_URL_TEST = "://";
 	
 	/**
 	 * HTTP User Agent
 	 */
 	public static final String HTTP_USER_AGENT = "Mozilla/5.0 (compatible; Linter/1.0)"; // our own custom user agent based on Googlebot
 	
 	/**
 	 * HTTP Connection Timeout
 	 */
 	public static final int HTTP_CONNECT_TIMEOUT = 10000;	// 10 sec
 	
 	/**
 	 * HTTP Read Timeout
 	 */
 	public static final int HTTP_READ_TIMEOUT = 5000;	// 5 sec
 	
 	/**
 	 * HTTP Max Content Length, 1MB
 	 */
 	public static final int HTTP_MAX_CONTENT_LENGTH = 1048576; 	// 1 MB in bytes 
 	
 	/*
 	 * Trust manager for SSL
 	 */
 	private static TrustManager[] TRUST_MANAGER = null;
 	
 	/**
 	 * Parse OK State
 	 */
 	private boolean _parseOk = false;
 	
 	/**
 	 * Parse Error
 	 */
 	private String _parseError;
 	
 	/**
 	 * Scraped meta data
 	 */
 	private LintedData _metaData;
 	
 	/**
 	 * Original URL provided by Linter Consumer, not redirected
 	 */
 	private String _originalUrl;
 	
 	/**
 	 * List of all alias URLs found in redirection and stripping parameters 
 	 */
 	private ArrayList<String> _aliases;
 	
 	/**
 	 * Destination URL after all redirection
 	 */
 	private String _destinationUrl;
 	
 	/**
 	 * List of all redirections, subset of aliases
 	 */
 	private ArrayList<String> _redirectUrlList;
 	
 	/*
 	 * Total processing, downloading, and meta data scrape time
 	 */
 	private long _processingTime;
 	
 	 
 		
 	/**
 	 * Create a blank linted page, expects you to call {@link process} at some point 
 	 * @param originalUrl	URL to be processed
 	 */
 	public LintedPage(String originalUrl) {
 		_originalUrl = originalUrl;
 		_metaData = new LintedData();
 		_redirectUrlList = new ArrayList<String>();
 		_aliases = new ArrayList<String>();
 	}
 	
 	/***
 	 * Process the original URL, including alias resolution, scraping and metadata extraction
 	 */
 	public void process() {
 		final long startTime = System.nanoTime();
 		final long endTime;
 		try {
 			processRunner();
 		} finally {
 		  endTime = System.nanoTime();
 		}
 		_processingTime = endTime - startTime;
 	}
 	
 	/***
 	 * Process the original URL associated with this LintedPage
 	 */
 	private void processRunner() {
 		logger.info("Processing URL: " + _originalUrl);
 		
 		logger.debug("Expanding any shortened URLs...");
 		if (followUrlRedirects()) {
 			logger.debug("Scraping & cleaning HTML...");
 			scrapeMetadata();
 		}
 	}
 	
 	/***
 	 * Follows the originalUrl to its destination, saving any aliases along the way. This is useful to expand
 	 * URL shortening services.
 	 * @return True if successful, false otherwise
 	 */
 	public boolean followUrlRedirects() {
 		ArrayList<String> aliases = new ArrayList<String>();
 		
 		String currentLocation = _originalUrl;
 		String lastLocation = null;
 		
 		while (currentLocation != null) {
 			try {				
 				URL url = new URL(currentLocation);
 				_redirectUrlList.add( currentLocation );
 				
 				logger.trace("Following " + currentLocation + "...");										
 			    
 				// Lazy initialize the trust manager
 				if( TRUST_MANAGER == null ) {
 					initTrustManager();
 				}
 				
 				HttpURLConnection connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
 
 				connection.setInstanceFollowRedirects(false);
 				connection.setRequestMethod("HEAD"); // only want the headers
 				connection.setConnectTimeout(LintedPage.HTTP_CONNECT_TIMEOUT);
 				connection.setReadTimeout(LintedPage.HTTP_READ_TIMEOUT);
 				connection.setRequestProperty("User-Agent", LintedPage.HTTP_USER_AGENT);
 				if (lastLocation != null)
 					connection.setRequestProperty("Referer", lastLocation);
 				connection.connect();
 				
 				
 				String nextLocation = connection.getHeaderField("Location");
 				if (nextLocation != null) {
 					// Did we get a relative redirect?
 					if (!nextLocation.contains(LintedPage.RELATIVE_URL_TEST)) {
 						String prefix;
 						
 						Matcher m = getUrlMatcher(currentLocation);
 						if (m.matches()) {
 							prefix = m.group(1) + m.group(6);
 						} else {
 							// graceful degradation (not that great but it should work) -- find everything to the left of the 1st '/' after ://
 							int endIndex = currentLocation.indexOf('/', currentLocation.indexOf(LintedPage.RELATIVE_URL_TEST) + LintedPage.RELATIVE_URL_TEST.length());
 							prefix = currentLocation.substring(0, endIndex);
 						}
 						
 						logger.trace("Relative URL redirect. Appending prefix: " + prefix);
 						nextLocation = prefix + nextLocation;
 					}
 					
 					if (nextLocation.equals(currentLocation) || aliases.contains(nextLocation)) {
 						logger.trace("Discovered loop redirect. Not following redirect to " + nextLocation);
 						_destinationUrl = currentLocation;
 						currentLocation = null;
 					} else {
 						logger.trace("Discovered redirect to " + nextLocation);
 						aliases.add(currentLocation);
 						lastLocation = currentLocation;
 						currentLocation = nextLocation;
 					}
 				} else {
 					logger.trace("URL resolved to its destination: " + currentLocation);
 					_destinationUrl = currentLocation;
 					currentLocation = null;
 				}
 				connection.disconnect();
 			} catch (MalformedURLException ex) {
 				logger.error("Invalid URL [" + currentLocation + "]: " + ex);
 				_parseError = ex.toString();
 				return false;
 			} catch (IOException ioe) {
 				logger.error("IO Exception [" + currentLocation + "]: " + ioe);
 				_parseError = ioe.toString();
 				return false;
 			} catch (Exception ex) {
 				logger.error("Exception [" + currentLocation + "]: " + ex);
 				_parseError = ex.toString();
 				return false;
 			}
 		}
 		
 		_aliases = aliases;
 
 		return true;
 	}
 	
 	/**
 	 * Scrapes the metadata on this page (can be called separately from {@link process}
 	 */
 	public void scrapeMetadata() {		
 		final String logPrefix = "[" + this.getDestinationUrl() + "] ";
 		
 		logger.trace(logPrefix + "Downloading and scraping page contents...");
 		
 		InputStream inStr = null;
 		HttpURLConnection connection = null;
 		try {
 			URL url = new URL(this.getDestinationUrl());
 			connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
 			connection.setConnectTimeout(LintedPage.HTTP_CONNECT_TIMEOUT);
 			connection.setReadTimeout(LintedPage.HTTP_READ_TIMEOUT);
 			connection.setRequestProperty("User-Agent", LintedPage.HTTP_USER_AGENT);
 			connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
 			connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
 			
 			String contentType = connection.getContentType();
 			if (contentType == null)
 				contentType = "unknown";
 			if (!contentType.toLowerCase().contains("text/html") && !contentType.toLowerCase().contains("text/plain")) {
 								
 				if( contentType.toLowerCase().contains("image/png") || contentType.toLowerCase().contains("image/jpeg") ) {
					getMetaData().put( "preview_image_url", this.getDestinationUrl() );
 					_parseOk = true;
 				}
 				
 				logger.warn(logPrefix + "Not downloading or scraping page because content-type was: " + contentType);
 				return;
 			}
 			
 			int contentLength = connection.getContentLength();
 			if (contentLength > LintedPage.HTTP_MAX_CONTENT_LENGTH) {
 				logger.warn(logPrefix + "Not downloading or scraping page because content-length was too large: " + Integer.toString(contentLength) + " (max: " + Integer.toString(LintedPage.HTTP_MAX_CONTENT_LENGTH) + ")");
 				return;
 			}
 			
 			String encoding = connection.getContentEncoding();
 			if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
 			    inStr = new GZIPInputStream(connection.getInputStream());
 			} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
 			    inStr = new InflaterInputStream(connection.getInputStream(),
 			      new Inflater(true));
 			} else {
 			    inStr = connection.getInputStream();
 			}
 		} catch (FileNotFoundException fnf) {
 			_parseError = "HTTP ERROR 404";
 			logger.error(logPrefix + " " + _parseError);
 			return;
 		} catch (IOException ioe) {
 			try {
 				_parseError = "Unable to download page [HTTP ERROR " + Integer.toString(connection.getResponseCode()) + "]: " + ioe;
 			} catch (IOException e) {
 				// We'd get an ioexception on the above try{} clause if don't have a response code
 				_parseError = " Unable to download page: " + ioe;
 			}
 			logger.error(logPrefix + " " + _parseError);
 			return;
 		} catch (Exception ex) {
 			logger.error(logPrefix + "Unable to download page: " + ex);
 			_parseError = ex.toString();
 			return;
 		}
 		
 		ServiceParser parser = ServiceParserChainManager.getInstance().getServiceParser( this.getDestinationUrl() );
 		parser.setRawContent( inStr );
 		parser.setRedirectUrlList( _redirectUrlList );
 		_parseOk = parser.parse();
 		_metaData = parser.getMetaData();
 
 		// Update the URL, if modified by the ServiceParser
 		String url = _metaData.getString( "url" );
 		if( url != null && !url.isEmpty() ) {
 			_destinationUrl = url;
 		}
 		
 		// Update alias URLs, if modified by the ServiceParser
 		if( _metaData.get( "alias_urls" ) != null ) {
 			Object[] arr = (Object[]) _metaData.get( "alias_urls" );			
 			_aliases = new ArrayList<String>( Arrays.asList( Arrays.copyOf( arr, arr.length, String[].class) ) );
 		}
 		
 		// Get any parse error from the ServiceParser
 		String parseError = parser.getParseError();
 		if( parseError != null && !parseError.isEmpty() ) {
 			_parseError = parseError;
 		}
 	}
 	
 	/**
 	 * Whether or not the parse completed successfully
 	 * @return True if successful
 	 */
 	public Boolean getParseOk() {
 		return _parseOk;
 	}
 	
 	/**
 	 * The error message we encountered during parsing, if any
 	 * @return Parser error
 	 */
 	public String getParseError() {
 		return _parseError;
 	}
 	
 	/**
 	 * Returns the original URL (before any shortened URLs were expanded)
 	 * @return Original URL
 	 */
 	public String getOriginalUrl() {
 		return _originalUrl;
 	}
 	
 	/**
 	 * Gets any aliases for this URL -- e.g. any redirects between the original URL and its actual destination
 	 * @return Array of alias URLs
 	 */
 	public String[] getAliases() {
 		return _aliases.toArray(new String[0]);
 	}
 	
 	/**
 	 * Gets the final destination, after expanding any shortened URLs starting from the original URL
 	 * @return Final, resolved URL
 	 */
 	public String getDestinationUrl() {
 		// If we failed to parse, the destination URL might not be set, so let's just make it the original URL since
 		// that's the best we can do.
 		if (_destinationUrl == null && !getParseOk())
 			_destinationUrl = _originalUrl;
 		return _destinationUrl;
 	}
 		
 	/**
 	 * Gets the processing time in milliseconds
 	 * @return Time in MS
 	 */
 	public long getProcessingTimeMillis() {
 		return TimeUnit.NANOSECONDS.toMillis(_processingTime);
 	}
 	
 	/**
 	 * Gets the processing time in human-readable format
 	 * @return Time, human-readable
 	 */
 	public String getProcessingTimeForHumans() {
 
 		// mm:ss is:
 		//	    TimeUnit.NANOSECONDS.toMinutes(_processingTime),
 		//	    TimeUnit.NANOSECONDS.toSeconds(_processingTime) - 
 		//	    TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(_processingTime))
 		
 		Float millis = new Float(getProcessingTimeMillis() / 1000);
 		return String.format("%.4f", millis);
 	}
 	
 	/**
 	 * Output Linter status and meta data as a human-readable string
 	 * @return Linter status and meta data 
 	 */
 	public String toDebugString() {
 		StringBuilder sb = new StringBuilder(_originalUrl);
 		sb.append(" {\n");
 		sb.append("\tPARSE OK:\t\t"); sb.append(this.getParseOk()); sb.append('\n');
 		if (!this.getParseOk()) {
 			sb.append("\tPARSE ERROR:\t\t"); sb.append(this.getParseError()); sb.append('\n');
 		}
 		sb.append("\tALIASES:");
 			if (_aliases == null || _aliases.size() == 0)
 				sb.append("\t\tNONE\n");
 			else {
 				sb.append('\n');
 				for (String alias : _aliases) {
 					sb.append("\t\t"); sb.append(alias); sb.append('\n');
 				}
 			}		
 		
 		sb.append("\tDEST URL:\t\t"); sb.append(this.getDestinationUrl()); sb.append('\n');
 
 		if( _metaData != null ) {
 			sb.append( _metaData.getPrettyDebugString() );
 		} else {
 			sb.append( "\tNo meta data parsed.\n" );
 		}
 		
 		sb.append("} in "); sb.append(this.getProcessingTimeForHumans()); sb.append(" s\n");
 		
 		return sb.toString();
 	}	
 	
 	/**
 	 * Get pattern matcher for detecting full URLs
 	 * @return URL pattern matcher
 	 */
 	private Matcher getUrlMatcher(String url) {
 		return LintedPage.URL_PATTERN.matcher(url);
 	}
 
 	/**
 	 * Initialize trust manager- does no checking, accepts all certificates
 	 */
 	private void initTrustManager() throws KeyManagementException {
 
 		// Create a new TrustManager that accepts all certificates
 		TRUST_MANAGER = new TrustManager[]{
 				new X509TrustManager() {
 					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
 						return null;
 					}
 					public void checkClientTrusted(
 							java.security.cert.X509Certificate[] certs, String authType) {
 					}
 					public void checkServerTrusted(
 							java.security.cert.X509Certificate[] certs, String authType) {
 					}
 				}
 		};
 
 		SSLContext sc;
 		try {
 			sc = SSLContext.getInstance("SSL");
 			sc.init(null, TRUST_MANAGER, new java.security.SecureRandom());
 			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 		} catch (NoSuchAlgorithmException e) {
 			logger.error( "Error configuring SSL", e);
 		}			    	    
 	}
 	
 	/**
 	 * Get LintedData object containing all meta data scraped from this page
 	 * @return LintedData
 	 */
 	public LintedData getMetaData() {
 		return _metaData;
 	}
 	
 	/**
 	 * Remove a list of parameters from a URL, automatically adds the original url to the alias list
 	 * @param parameters	Array of parameters to remove from URL 
 	 */
 	public void removeDestinationUrlParamters( String[] parameters ) {		
 		String urlRemoved = URLParser.removeParameters( _destinationUrl, parameters );
 		if( urlRemoved != null && urlRemoved.compareTo( _destinationUrl ) != 0 ) {
 			_aliases.add( _destinationUrl );
 			_destinationUrl = urlRemoved;
 		}		
 	}
 	
 	/**
 	 * Identifies if the Linted Page has a complete set of basic meta data
 	 * This is somewhat arbitrary, but includes Title, Description, and Preview Image URL
 	 * 
 	 * TODO: Consider relocating to a more consumer-specific location
 	 * 
 	 * @return true if complete set
 	 */
 	public boolean hasBasicMetaDataSet() {		
 		boolean complete = false;
 		
 		LintedData data = getMetaData();
 		if( ( data.hasKey( "title" ) && !data.getString( "title" ).isEmpty() ) 
 			&& ( data.hasKey( "description" ) && !data.getString( "description" ).isEmpty() )
			&& ( data.hasKey( "preview_image_url" ) && !data.getString( "preview_image_url" ).isEmpty() ) ) {
 			complete = true;
 		}
 		return complete;
 	}
 }
