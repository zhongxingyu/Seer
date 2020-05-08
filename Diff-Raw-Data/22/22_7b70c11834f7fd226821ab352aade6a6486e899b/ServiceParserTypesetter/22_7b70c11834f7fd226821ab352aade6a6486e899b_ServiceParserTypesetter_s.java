 package org.linter;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 import net.htmlparser.jericho.Source;
 
 /**
  * Sets the "type" field, indicating that a page has been recognized
  * to have a focus on a Photo, Image, or general page content;
  * 
  * The pattern matches all URLs;
  * 
  * Images are identified by URLs matching known, popular image hosting
  * providers;
  * 
  * Videos are identified by the presence of video meta data;
  */
 public class ServiceParserTypesetter extends ServiceParserPartial {
 	
 	/**
 	 * URL pattern, wildcard
 	 */
 	private static final Pattern PATTERN = Pattern.compile( 
 			".*" 
 			);
 	
 	
 	/**
 	 * Pattern matching all URLS
 	 * 
 	 * @return Wildcard pattern .*
 	 */
 	@Override
 	public Pattern getServicePattern() {
 		return PATTERN;
 	}
 
 	/**
 	 * Parse, determine content type
 	 * 
 	 * @return True
 	 */
 	@Override
 	public boolean parse() {
 		String type = null;
 		if( isImage() ) {
 			type = "image";
 		} else if (isVideo() ) {
 			type = "video";
 		} else {
 			type = "link";
 		}
 		
 		getMetaData().put( "type", type );		
 		
 		parseWithSuccessor();		
 		return true;
 	}
 
 	/**
 	 * Determine if URL has a primary focus on an image; 
 	 * Image urls are most easily identified by common provider URLs
 	 *  
 	 * @return True if image
 	 */
 	private boolean isImage() {
 		boolean isImage = false;
 		
 		// Make sure a preview image was found
		if( getMetaData().get( "preview-image-url" ) != null ) {
 		
 			final Pattern imageHostPatterns[] = { 
 					Pattern.compile( "http://twitpic\\.com/.*" ), 
 					Pattern.compile( "http://instagr\\.am/p/.*" ),
 					Pattern.compile( "http://flickr\\.com/photos/.*" ),
 					Pattern.compile( "http://twitrpix\\.com/.*" ),
 					Pattern.compile( "http://yfrog\\.com/.*" ),
 					Pattern.compile( "http://.*\\.posterous\\.com/.*" ),
 					Pattern.compile( "http://ow\\.ly/i/.*" )
 					};
 			
 			for( int i = 0; i < imageHostPatterns.length; i++ ) {
 				Matcher m = imageHostPatterns[i].matcher( _url );
 				if( m.matches() ) {
 					isImage = true;
 					break;
 				}
 			}
 			
 		}
 		
 		return isImage;
 	}
 
 	/**
 	 * Determine if URL has a primary focus on a video;
 	 * Video urls are most easily identified by the presence of video meta tags
 	 *  
 	 * @return True if video
 	 */	
 	private boolean isVideo() {
 		boolean isVideo = false;
 		
 		Source source = getJerichoSource();
 		if( source != null ) {								
 			if( source.getFirstElement( "property", "og:video", false ) != null 			// <meta property="og:video" content="http://www.youtube.com/v/Ezuz_-eZTMI?version=3&amp;autohide=1">
 				|| source.getFirstElement( "content", "video", false ) != null 				// <meta property="og:type" content="video"> 
 				|| source.getFirstElement( "rel", "video_src", false ) != null  			// <link rel="video_src" href='http://cdn.livestream.com/grid/LSPlayer.swf?channel=occupynyc&autoPlay=true'/>
 				|| source.getFirstElement( "property", "og:video:type", false ) != null 	// <meta property="og:video:type" content="application/x-shockwave-flash">
 				|| source.getFirstElement( "property", "og:video:width", false ) != null 	// <meta property="og:video:width" content="398">
 				|| source.getFirstElement( "property", "og:video:height", false ) != null 	// <meta property="og:video:height" content="224">
 				) {	
 				isVideo = true;
 			}			
 		}							
 		return isVideo;
 	}
 
 }
