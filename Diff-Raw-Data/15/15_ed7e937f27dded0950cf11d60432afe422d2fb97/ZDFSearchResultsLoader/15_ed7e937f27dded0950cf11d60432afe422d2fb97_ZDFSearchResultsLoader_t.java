 package com.halcyonwaves.apps.meinemediathek.loaders;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.acra.ACRA;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.content.AsyncTaskLoader;
 import android.content.Context;
 import android.os.Environment;
 import android.util.Log;
 
 import com.halcyonwaves.apps.meinemediathek.Consts;
 import com.halcyonwaves.apps.meinemediathek.SearchResultEntry;
 
 public class ZDFSearchResultsLoader extends AsyncTaskLoader< List< SearchResultEntry > > {
 
 	private final static String BASE_SEARCH_URL = "http://www.zdf.de/ZDFmediathek/suche?flash=off&offset=0&sucheText=";
 
 	private static final String TAG = "SearchLoader";
 	private final Pattern OffsetPattern = Pattern.compile( "offset\\=(\\d*)" );
 	private final Pattern PreviewImagePattern = Pattern.compile( "contentblob\\/(\\d*)" );
 
 	private String searchFor = null;
 	private List< SearchResultEntry > searchResults = null;
 
 	private boolean socketException = false;
 
 	public ZDFSearchResultsLoader( final Context context, final String searchFor ) {
 		super( context );
 		this.searchFor = searchFor;
 	}
 
 	@Override
 	public void deliverResult( final List< SearchResultEntry > results ) {
 		// an asynchronous query came in while the loader is stopped. We don't need the result.
 		if( this.isReset() ) {
 			if( results != null ) {
 				this.onReleaseResources( results );
 			}
 		}
 		final List< SearchResultEntry > oldResults = results;
 		this.searchResults = results;
 
 		// if the Loader is currently started, we can immediately deliver its results.
 		if( this.isStarted() ) {
 			super.deliverResult( results );
 		}
 
 		// at this point we can release the resources associated with old object
 		if( oldResults != null ) {
 			this.onReleaseResources( oldResults );
 		}
 	}
 
 	@Override
 	public List< SearchResultEntry > loadInBackground() {
 		// just write the search keyword into the log file
 		Log.v( ZDFSearchResultsLoader.TAG, "Starting to load data for the following search query: " + this.searchFor );
 
 		// be sure that the search keyword is well-formed
 		String preparedSearchKeyword = "";
 		try {
 			preparedSearchKeyword = URLEncoder.encode( this.searchFor, "utf-8" );
 		} catch( final UnsupportedEncodingException e ) {
 			Log.e( ZDFSearchResultsLoader.TAG, "Failed to to a proper URL encoding of the search keywords.", e );
 		}
 		Log.v( ZDFSearchResultsLoader.TAG, "The keywords were URL encoded and are now represented as: " + preparedSearchKeyword );
 
 		// create the list we want to return
 		final List< SearchResultEntry > foundTitles = new ArrayList< SearchResultEntry >();
 
 		//
 		int currentOffset = -1;
 		int nextOffset = 0;
 		String currentForwardLink = ZDFSearchResultsLoader.BASE_SEARCH_URL + preparedSearchKeyword;
 
 		// try to download the response of the webpage to the search query
 		try {
 			// create a list with the URLs we have to visit
 			final List< String > linksToVisit = new ArrayList< String >();
 
 			// loop through all search result pages
 			while( nextOffset > currentOffset ) {
 				Log.v( ZDFSearchResultsLoader.TAG, String.format( "Starting to parse new search results page. Currently we have %d links grabbed.", linksToVisit.size() ) );
 
 				// query for the results and get a handle to the returned HTML code
 				final Document fetchedResults = Jsoup.connect( currentForwardLink ).userAgent( Consts.DESKTOP_USER_AGENT ).timeout( Consts.SOCKET_TIMEOUT_IN_SECONDS * 1000 ).get();
 				final Elements foundLinks = fetchedResults.select( "a[href]" );
 				for( final Element currentLink : foundLinks ) {
 					if( currentLink.attr( "href" ).contains( "/ZDFmediathek/beitrag/video" ) ) {
 						linksToVisit.add( currentLink.attr( "abs:href" ) );
 					}
 				}
 
 				// get the link which brings us to the next page of the search results
 				final Elements forwardLink = fetchedResults.select( "a[href].forward" );
 				currentForwardLink = new String( forwardLink.attr( "abs:href" ) );
 
 				// check if the page with the next search results is a new page we haven't visit until now
 				final Matcher nextOffsetMatcher = this.OffsetPattern.matcher( currentForwardLink );
 				if( nextOffsetMatcher.find() ) {
 					currentOffset = nextOffset;
 					try {
 						nextOffset = Integer.parseInt( nextOffsetMatcher.group( 1 ) );
 					} catch( final NumberFormatException e ) {
 						Log.w( ZDFSearchResultsLoader.TAG, "Failed to parse the offset for the next results page. The parsed content was: " + nextOffsetMatcher.group( 1 ) );
 					}
 				}
 			}
 
 			// remove link duplicates
 			final HashSet< String > uniqueURLs = new HashSet< String >( linksToVisit );
 			Log.v( ZDFSearchResultsLoader.TAG, String.format( "Searching for links finished. After removing duplicates we end with %d movies pages to parse.", uniqueURLs.size() ) );
 
 			// after we fetched the links for all of our episodes, start fetching information about the episodes
 			for( final String currentURL : uniqueURLs ) {
 
 				// download the website for the selected URL
 				final Document currentEpisodeDoc = Jsoup.connect( currentURL ).userAgent( Consts.DESKTOP_USER_AGENT ).timeout( Consts.SOCKET_TIMEOUT_IN_SECONDS * 1000 ).get();
 
 				final Elements epoisodeTitle = currentEpisodeDoc.select( "h1.beitragHeadline" );
 				final Elements episodeDescription = currentEpisodeDoc.select( "div.beitrag > p.kurztext" );
				//final Elements episodeImage = currentEpisodeDoc.select( "div.beitrag > img" );
				final Elements styleDefBackgroundImage = currentEpisodeDoc.select( "style" );
 				final Elements downloadLinks = currentEpisodeDoc.select( "a[href]" );
				
				//
				String syledef = styleDefBackgroundImage.html();
				String episodeImage = "http://www.zdf.de" + syledef.replaceAll( "[\\s{}\\w\\#]*background-image:\\surl\\((\\/ZDFmediathek/.*)\\)[\\s\\w:;{}]*", "$1" );
				Log.v( ZDFSearchResultsLoader.TAG, "Image download URL: " + episodeImage );
 
 				// try to extract the first ASX link
 				String downloadLinkText = "";
 				for( final Element currentDownloadLinkElement : downloadLinks ) {
 					if( currentDownloadLinkElement.attr( "href" ).endsWith( ".asx" ) ) {
 						downloadLinkText = currentDownloadLinkElement.attr( "abs:href" );
 						break;
 					}
 				}
 
 				// extract the unique name for the episode preview image
 				String episodeImageName = "preview_000000.jpg"; // TODO
				final Matcher eposiodeImagePreviewNameMatcher = this.PreviewImagePattern.matcher( episodeImage );
 				if( eposiodeImagePreviewNameMatcher.find() ) {
 					episodeImageName = "preview_" + eposiodeImagePreviewNameMatcher.group( 1 ) + ".jpg";
 				} else {
 					episodeImageName = UUID.randomUUID().toString() + ".jpg";
 				}
 
 				// combine the extracted episode image name with the storage path
 				final File storagePath = this.getContext().getExternalFilesDir( Environment.DIRECTORY_PICTURES );
 				final File pictureFile = new File( storagePath, episodeImageName );
 
 				// just download the preview image if it is not already cached
 				if( !pictureFile.exists() ) {
 					final FileOutputStream pictureOutputStream = new FileOutputStream( pictureFile );
 
					final URL imageUrl = new URL( episodeImage );
 					final URLConnection imageUrlConnection = imageUrl.openConnection();
 					imageUrlConnection.setRequestProperty( "User-Agent", Consts.DESKTOP_USER_AGENT );
 					final BufferedInputStream in = new BufferedInputStream( imageUrlConnection.getInputStream() );
 
 					final byte[] buf = new byte[ 1024 ];
 					int n = 0;
 					while( (n = in.read( buf )) >= 0 ) {
 						pictureOutputStream.write( buf, 0, n );
 					}
 
 					pictureOutputStream.flush();
 					pictureOutputStream.close();
 					in.close();
 				}
 
 				// add all extracted information to our result entry representation
 				foundTitles.add( new SearchResultEntry( epoisodeTitle.first().text(), episodeDescription.first().text(), pictureFile, downloadLinkText ) );
 
 			}
 
 		} catch( final SocketTimeoutException e ) {
 			Log.e( ZDFSearchResultsLoader.TAG, "Failed to fetch the search results as a socket timedout.", e );
 			this.socketException = true;
 		} catch( final IOException e ) {
 			Log.e( ZDFSearchResultsLoader.TAG, "Failed to fetch the search results from the website.", e );
 		} catch( final ExceptionInInitializerError e ) {
 			ACRA.getErrorReporter().putCustomData( "rawSearchKeyword", this.searchFor );
 			ACRA.getErrorReporter().putCustomData( "preparedSearchKeyword", preparedSearchKeyword );
 			ACRA.getErrorReporter().putCustomData( "nextOffset", String.format( "%d", currentOffset ) );
 			ACRA.getErrorReporter().putCustomData( "nextOffset", String.format( "%d", nextOffset ) );
 			ACRA.getErrorReporter().putCustomData( "currentForwardLink", currentForwardLink );
 			ACRA.getErrorReporter().handleException( e );
 		}
 
 		// return the list of found items
 		return foundTitles;
 	}
 
 	protected void onReleaseResources( final List< SearchResultEntry > apps ) {
 		// for a simple List<> there is nothing to do. For something
 	}
 
 	@Override
 	protected void onStartLoading() {
 		// bugfix for this issue:
 		// http://code.google.com/p/android/issues/detail?id=14944
 		// http://blog.blundell-apps.com/tut-asynctask-loader-using-support-library/
 		super.onStartLoading();
 		if( null != this.searchResults ) {
 			this.deliverResult( this.searchResults );
 		} else {
 			this.forceLoad();
 		}
 	}
 
 	public boolean socketTimeoutOccurred() {
 		return this.socketException;
 	}
 
 }
