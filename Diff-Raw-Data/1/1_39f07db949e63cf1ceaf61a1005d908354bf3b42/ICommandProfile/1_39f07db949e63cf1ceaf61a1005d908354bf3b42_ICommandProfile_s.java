 package org.paxle.core.queue;
 
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
 }
