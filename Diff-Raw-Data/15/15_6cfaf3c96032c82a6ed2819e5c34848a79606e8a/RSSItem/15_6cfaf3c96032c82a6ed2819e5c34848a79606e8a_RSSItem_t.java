 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a Berkeley-style license:
 
   Copyright (c) 2004-2005 Brian M. Clapper. All rights reserved.
 
   Redistribution and use in source and binary forms are permitted provided
   that: (1) source distributions retain this entire copyright notice and
   comment; and (2) modifications made to the software are prominently
   mentioned, and a copy of the original software (or a pointer to its
   location) are included. The name of the author may not be used to endorse
   or promote products derived from this software without specific prior
   written permission.
 
   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
   WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 
   Effectively, this means you can do what you want with the software except
   remove this notice or take advantage of the author's name. If you modify
   the software and redistribute your modified version, you must indicate that
   your version is a modification of the original, and you must provide either
   a pointer to or a copy of the original.
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn.parser;
 
 import org.clapper.curn.FeedInfo;
 
 import org.clapper.util.text.TextUtil;
 import org.clapper.util.text.HTMLUtil;
 
 import java.net.URL;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * This abstract class defines a simplified view of an RSS item, providing
  * only the methods necessary for <i>curn</i> to work. <i>curn</i> uses the
  * {@link RSSParserFactory} class to get a specific implementation of
  * <tt>RSSParser</tt>, which returns <tt>RSSChannel</tt>-conforming objects
  * that, in turn, return item objects that subclass <tt>RSSItem</tt>. This
  * strategy isolates the bulk of the code from the underlying RSS parser,
  * making it easier to substitute different parsers as more of them become
  * available. <tt>RSSItem</tt>. This strategy isolates the bulk of the code
  * from the underlying RSS parser, making it easier to substitute different
  * parsers as more of them become available.
  *
  * @see RSSParserFactory
  * @see RSSParser
  * @see RSSChannel
  *
  * @version <tt>$Revision$</tt>
  */
 public abstract class RSSItem
 {
     /*----------------------------------------------------------------------*\
                                  Constants
     \*----------------------------------------------------------------------*/
 
     /**
      * Constant defining the pseudo-MIME type to use for default content.
      */
     public static final String DEFAULT_CONTENT_TYPE = "*";
 
     /*----------------------------------------------------------------------*\
                            Private Instance Data
     \*----------------------------------------------------------------------*/
 
     private Map<String,String> contentMap = new HashMap<String,String>();
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Get the item's content, if available. Some feed types (e.g., Atom)
      * support multiple content sections, each with its own MIME type; the
      * <tt>mimeType</tt> parameter specifies the caller's desired MIME
      * type.
      *
      * @param mimeType  the desired MIME type
      *
      * @return the content (or the default content), or null if no content
      *         of the desired MIME type is available
      */
     public String getContent (String mimeType)
     {
         String result = null;
 
         result = (String) contentMap.get (mimeType);
         if (result == null)
             result = (String) contentMap.get (DEFAULT_CONTENT_TYPE);
 
         return result;
     }
 
     /**
      * Get the first content item that matches one of a list of MIME types.
      *
      * @param mimeTypes  an array of MIME types to match, in order
      *
      * @return the first matching content string, or null if none was found.
      *         Returns the default content (if set), if there's no exact
      *         match.
      */
     public final String getFirstContentOfType (String[] mimeTypes)
     {
         String result = null;
 
         for (int i = 0; i < mimeTypes.length; i++)
         {
             result = (String) contentMap.get (mimeTypes[i]);
             if (! TextUtil.stringIsEmpty (result))
                 break;
         }
 
         if (result == null)
             result = (String) contentMap.get (DEFAULT_CONTENT_TYPE);
 
         return result;
     }
 
     /**
      * Set the content for a specific MIME type. If the
      * <tt>isDefault</tt> flag is <tt>true</tt>, then this content
      * is served up as the default whenever content for a specific MIME type
      * is requested but isn't available.
      *
      * @param content    the content string
      * @param mimeType   the MIME type to associate with the content
      */
     public void setContent (String content, String mimeType)
     {
         contentMap.put (mimeType, content);
     }   
 
     /**
      * Utility method to get the summary to display for an
      * <tt>RSSItem</tt>. Consults the configuration data in the specified
      * {@link FeedInfo} object to determine whether to truncate the
      * summary, use the description (i.e., content) if the summary isn't
      * available, etc. Strips HTML by default. (Use the
      * {@link #getSummaryToDisplay(FeedInfo,String[],boolean) alternate version}
      * of this method to control the HTML-stripping behavior.
      *
      * @param feedInfo  the corresponding feed's <tt>FeedInfo</tt> object
      * @param mimeTypes desired MIME types; used only if no summary is
      *                  available, and the content field should be used
      *
      * @return the summary string to use, or null if unavailable
      *
      * @see #getSummary
      * @see #getFirstContentOfType
      */
     public String getSummaryToDisplay (FeedInfo feedInfo,
                                        String[] mimeTypes)
     {
         return getSummaryToDisplay (feedInfo, mimeTypes, true);
     }
 
     /**
      * Utility method to get the summary to display for an
      * <tt>RSSItem</tt>. Consults the configuration data in the specified
      * {@link FeedInfo} object to determine whether to truncate the
      * summary, use the description if the summary isn't available, etc.
      * Optionally strips HTML from the resulting string.
      *
      * @param feedInfo  the corresponding feed's <tt>FeedInfo</tt> object
      * @param mimeTypes desired MIME types; used only if no summary is
      *                  available, and the content field should be used
      * @param stripHTML whether or not to strip HTML from the string
      *
      * @return the summary string to use, or null if unavailable
      *
      * @see #getSummary
      * @see #getFirstContentOfType
      */
     public String getSummaryToDisplay (FeedInfo feedInfo,
                                        String[] mimeTypes,
                                        boolean  stripHTML)
     {
         String summary = getSummary();
 
         if (TextUtil.stringIsEmpty (summary))
             summary = null;
 
         if ((summary == null) && (! feedInfo.summarizeOnly()))
         {
             assert (mimeTypes != null);
             summary = getFirstContentOfType (mimeTypes);
             if (TextUtil.stringIsEmpty (summary))
                 summary = null;
         }
 
         if (summary != null)
         {
             if (stripHTML)
                 summary = HTMLUtil.textFromHTML (summary);
 
             int maxSize = feedInfo.getMaxSummarySize();
             if (maxSize != FeedInfo.NO_MAX_SUMMARY_SIZE)
                 summary = truncateSummary (summary, maxSize);
         }
 
         return summary;
     }
 
     /**
      * Get the item's author. This method simply calls the {@link
      * #getAuthors} method and combines the results into a comma-delimited
      * string.
      *
      * @return the author string, or null if not available
      *
      * @see #setAuthor
      * @see #getAuthors
      */
     public final String getAuthor()
     {
         Collection<String> authors = getAuthors();
         String             result  = null;
 
         if ((authors != null) && (authors.size() > 0))
             result = TextUtil.join (authors, ", ");
 
         return result;
     }
 
     /**
      * Set the item's author. This method clears the existing authors
      * field, then calls {@link #addAuthor}. You're better off calling
      * {@link #addAuthor} directly, since some sites support multiple
      * authors for a feed item.
      *
      * @param newAuthor  the author, or null if not available
      *
      * @see #setAuthors
      */
     public final void setAuthor (String newAuthor)
     {
         clearAuthors();
         addAuthor (newAuthor);
     }
 
     /**
      * Set the item's list of authors to the specified <tt>Collection</tt>.
      * This method clears the existing authors field, then calls
      * {@link #addAuthor} for every string in the <tt>Collection</tt>.
      *
     * @param newAuthors  the author, or null if not available
      *
      * @see #addAuthor
      * @see #getAuthor
     * @see #clearAuthors
      */
     public final void setAuthors (Collection<String> newAuthors)
     {
         clearAuthors();
         for (String author : newAuthors)
             addAuthor (author);
     }
 
     /*----------------------------------------------------------------------*\
                           Public Abstract Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Get the item's title
      *
      * @return the item's title, or null if there isn't one
      *
      * @see #setTitle
      */
     public abstract String getTitle();
 
     /**
      * Set the item's title
      *
      * @param newTitle  the item's title, or null if there isn't one
      *
      * @see #getTitle
      */
     public abstract void setTitle (String newTitle);
 
     /**
      * Get the item's published link (its URL).
      *
      * @return the URL, or null if not available
      */
     public abstract URL getLink();
 
     /**
      * Change the item's link (its URL)
      *
      * @param url the new link value
      */
     public abstract void setLink (URL url);
 
     /**
      * Get the item's summary (also sometimes called the description or
      * synopsis).
      *
      * @return the summary, or null if not available
      *
      * @see #setSummary
      * @see #getSummaryToDisplay
      */
     public abstract String getSummary();
 
     /**
      * Set the item's summary (also sometimes called the description or
      * synopsis).
      *
      * @param newSummary the summary, or null if not available
      *
      * @see #getSummary
      */
     public abstract void setSummary (String newSummary);
 
     /**
      * Get the item's author list.
      *
      * @return the authors, or null (or an empty <tt>Collection</tt>) if
      *         not available
      *
      * @see #addAuthor
      * @see #clearAuthors
      * @see #setAuthors
      */
     public abstract Collection<String> getAuthors();
 
     /**
      * Add to the item's author list.
      *
      * @param author  another author string to add
      *
      * @see #getAuthors
      * @see #clearAuthors
      * @see #setAuthors
      */
     public abstract void addAuthor (String author);
 
     /**
      * Clear the authors list.
      *
      * @see #getAuthors
      * @see #addAuthor
      * @see #setAuthors
      */
     public abstract void clearAuthors();
 
     /**
      * Get the categories the item belongs to.
      *
      * @return a <tt>Collection</tt> of category strings (<tt>String</tt>
      *         objects) or null if not applicable
      */
     public abstract Collection<String> getCategories();
 
     /**
      * Get the item's publication date.
      *
      * @return the date, or null if not available
      */
     public abstract Date getPublicationDate();
 
     /**
      * Get the item's ID field, if any.
      *
      * @return the ID field, or null if not set
      */
     public abstract String getID();
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Truncate an RSS item's summary to a specified size. Truncates on
      * word boundary, if possible.
      *
      * @param summary  the summary to truncate
      * @param maxSize  the maximum size
      *
      * @return the truncated summary
      */
     private String truncateSummary (String summary, int maxSize)
     {
         summary = summary.trim();
 
         if (summary.length() > maxSize)
         {
             // Allow for ellipsis
 
             if (maxSize < 4)
                 maxSize = 4;
 
             maxSize -= 4;
 
             int last = maxSize;
             char[] ch = summary.toCharArray();
             int i = last;
 
             // If we're in the middle of a word, find the first hunk of
             // white space.
 
             while ((! Character.isWhitespace (ch[i])) && (i-- >= 0))
                 continue;
 
             // Next, get rid of trailing white space.
 
             while ((Character.isWhitespace (ch[i])) && (i-- >= 0))
                 continue;
 
             // Handle underflow.
 
             if (i >= 0)
                 last = i;
 
             StringBuffer buf = new StringBuffer (summary.substring (0,
                                                                     last + 1));
             buf.append (" ...");
             summary = buf.toString();
         }
 
         return summary;
     }
 }
