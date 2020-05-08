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
 
 import java.net.URL;
 import java.util.Collection;
 import java.util.Date;
 
 import org.clapper.util.text.TextUtil;
 
 /**
  * This abstract class defines a simplified view of an RSS channel,
  * providing only the methods necessary for <i>curn</i> to work.
  * <i>curn</i> uses the {@link RSSParserFactory} class to get a specific
  * implementation of <tt>RSSParser</tt>, which returns an object that is a
  * subclass of this class. This strategy isolates the bulk of the code from
  * the underlying RSS parser, making it easier to substitute different
  * parsers as more of them become available.
  *
  * @see RSSParserFactory
  * @see RSSParser
  * @see RSSItem
  *
  * @version <tt>$Revision$</tt>
  */
 public abstract class RSSChannel
 {
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Get the author of the feed. This method simply calls the
      * {@link #getAuthors}
      * method and combines the results into a comma-delimited string.
      *
      * @return the author, or null if not available
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
      * Set the item's author. This method clears the author field, then
      * calls {@link #addAuthor}. You're better off calling {@link #addAuthor}
      * directly, since some sites support multiple authors for a feed item.
      *
      * @param newAuthor  the author, or null if not available
      */
     public final void setAuthor (String newAuthor)
     {
         clearAuthors();
         addAuthor (newAuthor);
     }
 
     /**
      * Set the channel's list of authors to the specified
      * <tt>Collection</tt>. This method clears the existing authors field,
      * then calls {@link #addAuthor} for every string in the
      * <tt>Collection</tt>.
      *
     * @param newAuthor  the author, or null if not available
      *
      * @see #addAuthor
      * @see #getAuthor
     * @see #clearAuthor
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
      * Get a <tt>Collection</tt> of the items in this channel. All objects
      * in the collection are of type <tt>RSSItem</tt>.
      *
      * @return a (new) <tt>Collection</tt> of <tt>RSSItem</tt> objects.
      *         The collection will be empty (never null) if there are no
      *         items. This <tt>Collection</tt> is expected to be a copy of
      *         whatever the channel is really storing. (That is, if the
      *         underlying implementation is using a <tt>Collection</tt> to
      *         store its <tt>RSSItem</tt> objects, it should not return
      *         that <tt>Collection</tt> directly; instead, it should return
      *         a copy.)
      */
     public abstract Collection<RSSItem> getItems();
 
     /**
      * Change the items the channel the ones in the specified collection.
      * If the collection is empty, the items are cleared. The items are
      * copied from the supplied collection. (A reference to the supplied
      * collection is <i>not</i> saved in this object.)
      *
      * @param newItems  new collection of <tt>RSSItem</tt> items.
      */
     public abstract void setItems (Collection<? extends RSSItem> newItems);
 
     /**
      * Get the channel's title
      *
      * @return the channel's title, or null if there isn't one
      *
      * @see #setTitle(String)
      */
     public abstract String getTitle();
 
     /**
      * Set the channel's title
      *
      * @param newTitle the channel's title, or null if there isn't one
      *
      * @see #getTitle()
      */
     public abstract void setTitle (String newTitle);
 
     /**
      * Get the channel's description
      *
      * @return the channel's description, or null if there isn't one
      */
     public abstract String getDescription();
 
     /**
      * Get the channel's published link (its URL).
      *
      * @return the URL, or null if not available
      */
     public abstract URL getLink();
 
     /**
      * Get the channel's publication date.
      *
      * @return the date, or null if not available
      */
     public abstract Date getPublicationDate();
 
     /**
      * Get the channel's copyright string
      *
      * @return the copyright string, or null if not available
      */
     public abstract String getCopyright();
 
     /**
      * Get the RSS format the channel is using.
      *
      * @return the format, or null if not available
      */
     public abstract String getRSSFormat();
 
     /**
      * Get the channel's author list.
      *
      * @return the authors, or null (or an empty <tt>Collection</tt>) if
      *         not available
      *
      * @see #addAuthor
      * @see #clearAuthors
      * @see #setAuthor
      */
     public abstract Collection<String> getAuthors();
 
     /**
      * Add to the channel's author list.
      *
      * @param author  another author string to add
      *
      * @see #getAuthors
      * @see #clearAuthors
      * @see #setAuthor
      */
     public abstract void addAuthor (String author);
 
     /**
      * Clear the authors list.
      *
      * @see #getAuthors
      * @see #addAuthor
      * @see #setAuthor
      */
     public abstract void clearAuthors();
 }
