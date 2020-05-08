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
 
 import org.clapper.util.text.TextUtil;
 
 import java.util.Collection;
 
 /**
  * Abstract parent class of {@link RSSItem} and {@link RSSChannel},
  * containing various shared methods.
  *
  * @see RSSParser
  * @see RSSChannel
  * @see RSSItem
  *
  * @version <tt>$Revision$</tt>
  */
 public abstract class RSSElement
 {
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
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
 
     /**
      * Get the link with a specific MIME type and one of a set of
      * link types.
      *
      * @param mimeType  the desired MIME type
      * @param linkTypes one or more link types that are acceptable
      *
      * @return the link, or null no matches were found
      */
     public final RSSLink getLink (String mimeType, RSSLink.Type ... linkTypes)
     {
         return ParserUtil.findMatchingLink (getLinks(), mimeType, linkTypes);
     }
 
     /**
      * Get the first link with the specified link type.
      *
      * @param linkType  the link type
      *
      * @return the link, or null if no match was found
      */
     public final RSSLink getLink (RSSLink.Type linkType)
     {
         return ParserUtil.findMatchingLink (getLinks(), linkType);
     }
 
     /**
      * Get the first link with the specified MIME type.
      *
      * @param mimeType  the MIME type
      *
      * @return the link, or null if no match was found
      */
     public final RSSLink getLink (String mimeType)
     {
         return ParserUtil.findMatchingLink (getLinks(), mimeType);
     }
 
     /**
      * Get the first link with the specified MIME type. Fall back to the
     * {@link RSSLink#Type#SELF SELF} link if not found. Fall back to the
      * first available link if the <tt>SELF</tt> link is not found.
      *
      * @param mimeType  the MIME type
      *
      * @return the link, or null if no links are available
      */
     public final RSSLink getLinkWithFallback (String mimeType)
     {
         RSSLink              link  = null;
         Collection<RSSLink>  links = getLinks();
 
         if (links.size() > 0)
         {
             link = ParserUtil.findMatchingLink (links, mimeType);
             if (link == null)
             {
                 link = getLink (RSSLink.Type.SELF);
                 if (link == null)
                     link = links.iterator().next();
             }
         }
 
         return link;
     }
 
     /**
      * Get the URL associated with this item. This method first attempts to
     * get the {@link RSSLink#Type#SELF SELF} link. If that fails, it looks
     * for the first {@link RSSLink#Type#ALTERNATE ALTERNATE} link.
      *
      * @return the {@link RSSLink} for the URL, or null if it can't be found
      */
     public final RSSLink getURL()
     {
         // First, try to find the SELF link. If that's not available, use the
         // first ALTERNATE link.
 
         RSSLink link = getLink (RSSLink.Type.SELF);
 
         if (link == null)
             link = getLink (RSSLink.Type.ALTERNATE);
 
         return link;
     }
 
     /*----------------------------------------------------------------------*\
                           Public Abstract Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Get the element's list of published links (its URLs). Each
      * element in the returned <tt>Collection</tt> is an
      * {@link RSSLink} object.
      *
      * @return the collection of links, or an empty list if there are none.
      *         The result will never be null.
      */
     public abstract Collection<RSSLink> getLinks();
 
     /**
      * Get the item's author list.
      *
      * @return the authors, or null (or an empty <tt>Collection</tt>) if
      *         not available
      *
      * @see #addAuthor
      * @see #clearAuthors
      * @see #setAuthor
      * @see #getAuthor
      */
     public abstract Collection<String> getAuthors();
 
     /**
      * Add to the element's author list.
      *
      * @param author  another author string to add
      *
      * @see #getAuthors
      * @see #clearAuthors
      * @see #setAuthor
      * @see #getAuthor
      */
     public abstract void addAuthor (String author);
 
     /**
      * Clear the authors list.
      *
      * @see #getAuthors
      * @see #addAuthor
      * @see #setAuthor
      * @see #getAuthor
      */
     public abstract void clearAuthors();
 
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
