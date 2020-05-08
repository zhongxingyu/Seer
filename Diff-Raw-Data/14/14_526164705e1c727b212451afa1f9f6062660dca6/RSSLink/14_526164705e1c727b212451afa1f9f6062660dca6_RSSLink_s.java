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
 
 /**
  * An <tt>RSSLink</tt> object describes a link to a URL, including any
  * metadata about the URL's content (if available).
  *
  * @see RSSItem
  * @see RSSChannel
  *
  * @version <tt>$Revision$</tt>
  */
 public class RSSLink
 {
     /*----------------------------------------------------------------------*\
                                  Constants
     \*----------------------------------------------------------------------*/
 
     /**
      * Link types.
      */
     public enum Type
     {
         /**
          * The link refers back to the RSS feed.
          */
         SELF,
 
         /**
          * The link refers to an alternate page containing the same
          * information, but perhaps in a different format (e.g., HTML).
          */
         ALTERNATE
     };
 
     /*----------------------------------------------------------------------*\
                            Private Instance Data
     \*----------------------------------------------------------------------*/
 
     private URL    url      = null;
     private String mimeType = null;
     private Type   linkType = Type.SELF;
 
     /*----------------------------------------------------------------------*\
                                Constructors
     \*----------------------------------------------------------------------*/
 
     /**
      * Create an empty <tt>RSSLink</tt> object.
      *
     * @see #RSSLink{URL,Type,String}
      */
     public RSSLink()
     {
     }
 
     /**
      * Create and populate a new <tt>RSSLink</tt> object.
      *
      * @param url      the link's URL
      * @param mimeType the MIME type. Must not be null.
      * @param linkType the link type
      */
     public RSSLink (URL url, String mimeType, Type linkType)
     {
         this.url      = url;
         this.mimeType = mimeType;
         this.linkType = linkType;
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Get the URL for this link.
      *
      * @return the URL, or null if not set yet.
      *
      * @see #setURL
      */
     public URL getURL()
     {
         return url;
     }
 
     /**
      * Set the URL for this link.
      *
      * @param url  the URL, or null if not set yet.
      *
      * @see #getURL
      */
     public void setURL (URL url)
     {
         this.url = url;
     }
 
     /**
      * Get the MIME type for this link.
      *
      * @return the MIME type, or null if not set yet.
      *
      * @see #setMIMEType
      */
     public String getMIMEType()
     {
         return mimeType;
     }
 
     /**
      * Set the MIME type for this link.
      *
      * @param mimeType  the MIME Type, or null if not set yet.
      *
      * @see #getMIMEType
      */
     public void setMIMEType (String mimeType)
     {
         this.mimeType = mimeType;
     }
 
     /**
      * Get the link type for this link.
      *
      * @return the link type, or null if not set yet.
      *
      * @see #setLinkType
      */
     public Type getLinkType()
     {
         return linkType;
     }
 
     /**
      * Set the link type for this link.
      *
      * @param linkType  the link Type, or null if not set yet.
      *
      * @see #getLinkType
      */
     public void setLinkType (Type linkType)
     {
         this.linkType = linkType;
     }
 
     /**
      * Get the string representation of this link (the URL).
      *
      * @return the string version
      */
     public String toString()
     {
         return (url == null) ? "null" : url.toExternalForm();
     }
 }
