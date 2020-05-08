 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.core.queue;
 
 import java.io.Serializable;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 /**
  * TODO:
  *  
  * *) which properties to we need?
  * == general params ==
  * - flag specifying if processing this profile is paused
  * - name
  * - description
  * - amount of processed documents
  * - amount of loaded bytes
  * - restrict to a single domain?
  * - only crawl within a given time-span?
  * 
  * == crawling specific params ==
  * - max depth
  * - include/exclude protocols
  * - max number of crawled documents
  * - max amount of crawled bytes
  * 
  * == parser specific params ==
  * - include/exclude parsers
  * 
  * == indexing specific params ==
 * - include/exclude mimetypes from indexing (even if they should be parsed) 
  * 
  * == filter specific params ==
  * - e.g. special blacklist-filter 
  * - e.g. special URL-rewriter?
  * 
  * *) properties we do _not_ need
  * - starting-URL (defined via depth 0)
  * 
  */
 public interface ICommandProfile {
 	
 	/**
 	 * Specifies which mode is used to filter links:
 	 * <table>
 	 * <tr><td><code>none</code></td><td>filtering disabled</td></tr>
 	 * <tr><td><code>regexp</code></td><td>filtering using regular expressions</td></tr>
 	 * </table>
 	 * 
 	 * @see ICommandProfile#setLinkFilterExpression(String)
 	 * @see ICommandProfile#getLinkFilterExpression()
 	 */	
 	public static enum LinkFilterMode {
 		none,
 		regexp
 	};
 	
 	/**
 	 * @return a unique profile-id (needed by Object-EER mapping)
 	 */
     public int getOID(); 
 
     /**
      * @param OID a unique profile-id (needed by Object-EER mapping)
      */
     public void setOID(int OID); 
     
     public int getMaxDepth();
     
     public void setMaxDepth(int maxDepth);
     
     /**
      * @return the name of this profile
      */
     public String getName();
     
     public void setName(String name);
     
     public void setLinkFilterMode(LinkFilterMode mode);
     public LinkFilterMode getLinkFilterMode();
     
     /**
      * @param filter the expression that is used to filter links. For {@link LinkFilterMode#none} this value is <code>null</code>, 
 	 * for {@link LinkFilterMode#regexp} this is a valid {@link Pattern} in {@link String}-format.
      */
     public void setLinkFilterExpression(String filter);
     public String getLinkFilterExpression();
     
     public void setProperties(Map<String, Serializable> props);
     public Map<String, Serializable> getProperties();
     
     public Serializable getProperty(String propertyName);
     public Serializable removeProperty(String propertyName);
     public void setProperty(String propertyName, Serializable propertyValue);
 }
