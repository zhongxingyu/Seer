 package org.levasoft.streetdroid;
 
 /**
  * Topic interface.
  *
  */
 public interface ITopic {
 	
 	/**
 	 * Returns topic title
 	 */
 	String getTitle();
 
 	/**
 	 * Returns topic author username
 	 */
 	String getAuthor();
 	
 	/**
 	 * Returns topic URL in LiveStreet system,
 	 * for example: http://example.com//blog/123.html
 	 */
 	String getTopicUrl();	
 
 	/**
 	 * Returns topic blog name
 	 */
 	String getBlog();
 
 	/**
 	 * Return topic blog URL, it could be personal or collective blog. 
 	 * e.g. http://example.com/my/user/
 	 * or http://example.com/blog/news/ 
 	 */
 	String getBlogUrl();
 
 	/**
 	 * Returns topic content in HTML format
 	 */
 	String getContent();
 
 	/**
	 * Return topic publishing time in format defined by topic class implementation.
 	 */
 	String getDateTime();
 
 	/**
 	 * Returns the list of topic comments.
 	 */
 	IComment[] getComments();
 
 	/**
 	 * Flag indicating whether article downloading is complete or not.
 	 * It's complete only if topic was completely downloaded and parsed from the topic HTML text.
 	 * If topic brief text was parsed from RSS, it's considered not complete.
 	 */
 	boolean getDownloadComplete();
 	
 	/**
 	 * Returns information necessary for voting for topic
 	 */
 	VotingDetails getVotingDetails();
 
 	/**
 	 * Returns the site where this topic was published
 	 */
 	Site getSite();
 
 	/**
 	 * Returns the URL of the images parsed out of the topic HTML text. This image is displayed
 	 * in the topic list as a preview image.
 	 */
 	String getFrontImageUrl();
 }
