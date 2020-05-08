 /*
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  *
  * The Original Code is the Kowari Metadata Store.
  *
  * The Initial Developer of the Original Code is Plugged In Software Pty
  * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
  * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
  * Plugged In Software Pty Ltd. All Rights Reserved.
  *
  * Contributor(s): N/A.
  *
  * [NOTE: The text of this Exhibit A may differ slightly from the text
  * of the notices in the Source Code files of the Original Code. You
  * should use the text of this Exhibit A rather than the text found in the
  * Original Code Source Code for Your Modifications.]
  *
  */
 package org.mulgara.content.rdfxml.writer;
 
 // Java 2 standard packages
 import java.util.HashMap;
 import java.net.URI;
 
 // Apache packages
 import org.apache.log4j.Logger;
 
 // JRDF
 import org.jrdf.graph.Node;
 import org.jrdf.graph.URIReference;
 import org.jrdf.graph.GraphException;
 import org.jrdf.vocabulary.RDF;
 import org.jrdf.vocabulary.RDFS;
 
 // Local packages
 import org.mulgara.query.TuplesException;
 import org.mulgara.resolver.spi.GlobalizeException;
 import org.mulgara.resolver.spi.ResolverSession;
 import org.mulgara.resolver.spi.Statements;
 
 import java.util.*;
 
 /**
  * Map that contains all namespaces for a set of Statements.
  *
  * @created 2004-02-20
  *
  * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
  *
  * @version $Revision: 1.8 $
  *
  * @modified $Date: 2005/01/05 04:58:03 $
  *
  * @maintenanceAuthor $Author: newmana $
  *
  * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
  *
  * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
  *      Software Pty Ltd</a>
  *
  * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
  */
public class NamespaceMap extends HashMap<String,String> {
 
   /** For serialization */
   private static final long serialVersionUID = 1161744419591660130L;
 
   /** Logger. This is named after the class. */
   private final static Logger logger = Logger.getLogger(NamespaceMap.class.getName());
 
   /** A mirror of this map (where keys and values are swapped) */
  private Map<String,String> mirror = null;
 
   /** Prefix used to abbreviate RDF Namespace */
   private static final String RDF_PREFIX = "rdf";
 
   /** Prefix used to abbreviate RDFS Namespace */
   private static final String RDFS_PREFIX = "rdfs";
 
   /**
    * Constructor. Populates the map with all unique namespaces in the statements.
    *
    * @param statements Statements
    * @param session ResolverSession
    * @throws GraphException
    */
   public NamespaceMap(Statements statements, ResolverSession session) throws GraphException {
 
    mirror = new HashMap<String,String>();
 
     //add default namespaces
    put(RDF_PREFIX, RDF.BASE_URI.toString());
    put(RDFS_PREFIX, RDFS.BASE_URI.toString());
     put("owl", "http://www.w3.org/2002/07/owl#");
     put("dc", "http://purl.org/dc/elements/1.1/");
 
     //read namespaces from the statements
     try {
       populate(statements, session);
     } catch (TuplesException tuplesException) {
       throw new GraphException("Could not read statements.", tuplesException);
     } catch (GlobalizeException globalException) {
       throw new GraphException("Could not globalize statements.", globalException);
     }
   }
 
   /**
    * Evaluates the statements and adds namespace mappings for all unique
    * namespaces.
    *
    * @param statements Statements
    * @param session ResolverSession
    * @throws TuplesException
    * @throws GraphException
    * @throws GlobalizeException
    */
   private void populate(Statements statements, ResolverSession session) throws
       TuplesException, GraphException, GlobalizeException {
 
     Statements clonedStatements = (Statements)statements.clone();
 
     try {
 
       //last nodes to be evaluated
       long subject = -1;
       long predicate = -1;
       long object = -1;
 
       //current nodes
       long newSubject = -1;
       long newPredicate = -1;
       long newObject = -1;
 
       clonedStatements.beforeFirst();
       while (clonedStatements.next()) {
 
         newSubject = clonedStatements.getSubject();
         newPredicate = clonedStatements.getPredicate();
         newObject = clonedStatements.getObject();
 
         //evaluate nodes that have changed
         if (newSubject != subject) {
           subject = newSubject;
           evaluateAndPut(subject, session);
         }
         if (newPredicate != predicate) {
           predicate = newPredicate;
           evaluateAndPut(predicate, session);
         }
         if (newObject != object) {
           object = newObject;
           evaluateAndPut(object, session);
         }
       }
     } finally {
 
       clonedStatements.close();
     }
   }
 
   /**
    * Globalizes the node ID and evaluates it if it is an URI.
    *
    * @param nodeID long
    * @param session ResolverSession
    * @throws GlobalizeException
    * @throws GraphException
    */
   protected void evaluateAndPut(long nodeID, ResolverSession session) throws
       GlobalizeException, GraphException {
 
     //only URI's need namespace substitution
     Node node = session.globalize(nodeID);
     if ((node != null)
         && (node instanceof URIReference)) {
 
       this.addNamespaceURI(((URIReference) node).getURI());
     }
   }
 
   /**
    * Evaluates a URI and adds it to the namespace map as a namespace.
    *
    * @param uri URI
    * @throws GraphException
    */
   protected void addNamespaceURI(URI uri) throws GraphException {
 
     if (uri == null) throw new IllegalArgumentException("URI argument is null.");
 
     //extract namespace from URI
     String uriString = uri.toString();
     String newURI = toNamespaceURI(uriString);
 
     //only add namespace if it is new
     if ((newURI != null) && !containsValue(newURI)) {
       //add to namespaces
       put("ns" + size(), newURI);
     }
   }
 
   /**
    * Extracts the root namespace from an URI.
    *
    * @param uri URI
    * @throws GraphException
    * @return String
    */
   private String toNamespaceURI(String uri) throws GraphException {
 
     if (uri == null) throw new IllegalArgumentException("URI argument is null.");
 
     //return original string by default
     String nsURI = uri;
 
     //work backwards until a '/', '#' or ':' is encountered
     char currentChar = 0;
     for (int i = (uri.length() - 1); i >= 0; i--) {
 
       currentChar = uri.charAt(i);
       if ((currentChar == '/')
           || (currentChar == '#')
           || (currentChar == ':')) {
 
         //copy the string up to that point and return
         nsURI = uri.substring(0, i) + currentChar;
         return nsURI;
       }
     }
 
     assert nsURI != null : "Extracted namespace is null";
     return nsURI;
   }
 
   /**
    * Returns the key used to represent RDF.baseURI:
    * (http://www.w3.org/1999/02/22-rdf-syntax-ns#).
    *
    * @return String
    */
   public String getRDFPrefix() {
     return RDF_PREFIX;
   }
 
   /**
    * Substitutes part of the uri with the corresponding namespace from the map.
    *
    * @param uri String
    * @throws GraphException
    * @return String
    */
   public String replaceNamespace(String uri) throws GraphException {
 
     String newURI = null;
     String nsURI = toNamespaceURI(uri);
     String key = mirror.get(nsURI);
 
     if (key == null) throw new GraphException("Namespace: " + nsURI + " has not been mapped.");
 
     //should all or part of the URI be replaced?
     if (uri.equals(nsURI)) {
 
       //replace uri with entity
       newURI = "&" + key + ";";
 
       //this may produce invalid XML
       logger.warn("Replacing URI: " + uri + " with ENTITY: " + newURI +
           ". Namepace replacement may be invalid XML.");
     } else if (uri.startsWith(nsURI)) {
 
       //replace namespace part with key
       newURI = uri.replaceAll(nsURI, key + ":");
     }
 
     assert newURI != null;
     //replace any entities
     newURI = replaceCollection(newURI);
     return newURI;
   }
 
   /**
    * If the URI has a fragment representing a collection (eg. Bag) item, it is
    * replaced with li.
    *
    * @param original original URI.
    * @return new URI with any necessary li.
    * @throws GraphException
    */
   private String replaceCollection(String original) throws GraphException {
 
     //value to be returned
     String uri = original;
 
     //validate URI
     if (original != null) uri = original.replaceAll("_[0-9]+", "li");
 
     return uri;
   }
 
 
   /**
    * Overridden to allow for bi-directional mapping. Not intended to be called
    * outside this class.
    *
    * @param key String
    * @param value Object
    * @return Object
    */
  @Override
  public String put(String key, String value) {
 
     mirror.put(value, key);
     return super.put(key, value);
   }
 
 }
