 /* TemporalType.java - created on Mar 18, 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package org.theeuropeanlibrary.model.common.qualifier;
 
 /**
  * Qualifier for time references.
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @author Nuno Freire <nfreire@gmail.com>
  * @since Mar 18, 2011
  */
 public enum TemporalRelation {
     /**
      * An publication, or issued date
      */
     PUBLICATION,
     /**
      * When a resource was created
      */
     CREATION,
     /**
      * The content of a resource has this time as subject.
      */
     SUBJECT,
     /**
      * When a resource was copyrighted
      */
     COPYRIGHT,
     /**
      * An unqualified date associated with the resource. The specific semantics of the date is
      * unknown
      */
     GENERAL,
     /**
      * the content of dc:date, it has clearly something to to with creation or availability, but it
      * is not specified which of these.
      */
     CREATION_OR_AVAILABILITY,
     /**
      * see dcterms
      */
     ACCEPTED,
     /**
      * see dcterms
      */
     AVAILABLE,
     /**
      * see dcterms
      */
     CAPTURED,
     /**
      * see dcterms
      */
     MODIFIED,
     /**
      * see dcterms
      */
     SUBMITTED,
     /**
      * see dcterms
      */
     VALID,
 
     /**
      * harvested
      */
     OP_HARVESTED,
     /**
      * loaded
      */
     OP_LOADED,
 
     /**
      * date of an issue
      */
    ISSUE,
 }
