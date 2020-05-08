 /*
  * Copyright 2008 - 2010 Lars Heuer (heuer[at]semagia.com). All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.isotopicmaps.sdsharetests;
 
 /**
  * Common constant values useful for Atomico client / server implementations.
  * 
  * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
  * @version $Rev: 66 $ - $Date: 2010-09-06 17:29:07 +0200 (Mo, 06 Sep 2010) $
  */
 public interface IConstants {
 
     /**
      * Constant for the server address property name.
      */
     public static final String SERVER_ADDRESS_PROPERTY = "org.isotopicmaps.sdshare.serveraddress";
 
     /**
      * Atom 1.0 namespace.
      */
     public final static String NS_ATOM = "http://www.w3.org/2005/Atom";
 
     /**
      * SDShare namespace.
      */
    public final static String NS_SDSHARE = "http://www.egovpt.org/sdshare/";
 
     /**
      * Media type application/atom+xml
      */
     public static final String MEDIA_TYPE_ATOM_XML = "application/atom+xml";
 
     /**
      * Media type for XML Topic Maps.
      */
     public final static String MEDIA_TYPE_XTM = "application/x-tm+xml";
 
     /**
      * Media type for XML Topic Maps v1.0.
      */
     public final static String MEDIA_TYPE_XTM_10 = MEDIA_TYPE_XTM + ";version=1.0";
 
     /**
      * Media type for XML Topic Maps v2.0.
      */
     public final static String MEDIA_TYPE_XTM_20 = MEDIA_TYPE_XTM + ";version=2.0";
 
     /**
      * Media type for XML Topic Maps v2.1.
      */
     public final static String MEDIA_TYPE_XTM_21 = MEDIA_TYPE_XTM + ";version=2.1";
 
     /**
      * Media type for the Compact Topic Maps syntax.
      */
     public static final String MEDIA_TYPE_CTM = "application/x-tm+ctm";
 
     /**
      * Media type for RDF/XML.
      */
     public final static String MEDIA_TYPE_RDF_XML = "application/rdf+xml";
 
     /**
      * Element name which contains the subject identifier in a TMShare Atom feed.
      */
     public final static String ELEMENT_SID = "TopicSI";
 
     /**
      * Element name which contains the server source locator prefix in a 
      * TMShare Atom feed.
      */
     public final static String ELEMENT_PREFIX = "ServerSrcLocatorPrefix";
 
     /**
      * Element name which contains a dependency IRI in a TMShare Atom feed.
      */
     public final static String ELEMENT_DEPENDENCY = "dependency";
 
     /**
      * Attribute value for the link type "alternate"
      */
     public static final String REL_ALTERNATE = "alternate";
 
     /**
      * Attribute value for the link type "self" 
      */
     public static final String REL_SELF = "self";
 
     /**
      * Attribute value for the link type "collectionfeed"
      */
     public static final String REL_COLLECTION_FEED = NS_SDSHARE + "collectionfeed";
 
     /**
      * Attribute value for the link type "fragmentsfeed"
      */
     public static final String REL_FRAGMENTS_FEED = NS_SDSHARE + "fragmentsfeed";
 
     /**
      * Attribute value for the link type "snapshotsfeed"
      */
     public static final String REL_SNAPSHOTS_FEED = NS_SDSHARE + "snapshotsfeed";
 
 }
