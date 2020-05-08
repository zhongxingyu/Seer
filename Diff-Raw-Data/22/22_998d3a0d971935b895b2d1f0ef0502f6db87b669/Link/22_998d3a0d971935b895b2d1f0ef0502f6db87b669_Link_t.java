 package com.mulesoft.module.dropbox.model;
 
import java.io.Serializable;

 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
public class Link implements Serializable {
    private static final long serialVersionUID = -1L;

     /*  Represents a Dropbox share link: see doc at https://www.dropbox.com/developers/core/docs#shares
      *
      *  {
             "url": "http://db.tt/APqhX1",
             "expires": "Tue, 01 Jan 2030 00:00:00 +0000"
         }
      */
 
     /**
      * A Dropbox link to the given path. The link can be used publicly and directs to a preview page of the file.
      */
     private String url;
 
     /**
      * Expiration date of the link.
      * For compatibility reasons, it returns the link's expiration date in Dropbox's usual date format. All links are currently set to expire far enough in the future so that expiration is effectively not an issue.
      */
     private String expires;
 
 }
