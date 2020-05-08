 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.om.business.fedora;
 
 import de.escidoc.core.common.exceptions.application.ApplicationException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.om.business.stax.handler.OntologyHandler;
 
 import javax.xml.stream.XMLStreamException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Rozita Friedman
  */
 public final class OntologyUtility {
 
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentRelationsUtility.class);
 
     /**
      * Private constructor to avoid instantiation.
      */
     private OntologyUtility() {
     }
 
     /**
      * Check if content-relations ontologie contains predicate.
      * 
      * @param predicateUriReference
      *            The predicate with uri reference
      * @return true if predicate is defined within ontologie, false otherwise.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
      * @throws InvalidContentException
      */
     public static boolean checkPredicate(final String predicateUriReference) throws WebserverSystemException,
         EncodingSystemException, XmlParserSystemException, InvalidContentException {
 
         final StaxParser sp = new StaxParser();
 
         final OntologyHandler ontologyHandler = new OntologyHandler(sp, predicateUriReference);
         sp.addHandler(ontologyHandler);
 
         final String[] locations = getLocations();
 
         for (final String location : locations) {
             final InputStream in = getInputStream(location);
 
             try {
                 sp.parse(in);
             }
             catch (final ApplicationException e) {
                 XmlUtility.handleUnexpectedStaxParserException("", e);
             }
             catch (final XMLStreamException e) {
                 XmlUtility.handleUnexpectedStaxParserException("", e);
             }
             catch (final IntegritySystemException e) {
                 XmlUtility.handleUnexpectedStaxParserException("", e);
             }
             catch (final TripleStoreSystemException e) {
                 XmlUtility.handleUnexpectedStaxParserException("", e);
             }
             try {
                 in.close();
             }
             catch (IOException e) {
                 if (LOGGER.isWarnEnabled()) {
                     LOGGER.warn("Could not close stream.");
                 }
                 else if (LOGGER.isDebugEnabled()) {
                     LOGGER.debug("Could not close stream.", e);
                 }
             }
         }
 
         return ontologyHandler.isExist();
     }
 
     /**
      * Get location of ontology/predicate list for content relations.
      * 
      * @return location of file with PREDICATES
      * @throws WebserverSystemException
      *             Thrown if loading escidoc configuration failed.
      */
     private static String[] getLocations() {
 
         String[] locations;
         String location = EscidocConfiguration.getInstance().get(EscidocConfiguration.CONTENT_RELATIONS_URL);
 
         // default location
         // FIXME use a more qualified place for default configurations
         if (location == null) {
             locations =
                 new String[] { EscidocConfiguration.getInstance().appendToSelfURL(
                     "/ontologies/mpdl-ontologies/content-relations.xml") };
         }
         else {
             locations = location.split("\\s+");
 
             // expand local paths with selfUrl
             for (int i = 0; i < locations.length; i++) {
                 if (!locations[i].startsWith("http://")) {
                     locations[i] = EscidocConfiguration.getInstance().appendToSelfURL(locations[i]);
                 }
             }
         }
 
         return locations;
     }
 
     /**
      * Get InputStream from location.
      * 
      * @param location
      *            file location
      * @return InputStream from location
      * @throws WebserverSystemException
      *             Thrown if open of InputStream failed.
      */
     private static InputStream getInputStream(final String location) throws WebserverSystemException {
 
         final URLConnection conn;
         try {
             conn = new URL(location).openConnection();
         }
         catch (final MalformedURLException e) {
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Problem while loading resource '" + location + "'.");
             }
             else if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Problem while loading resource '" + location + "'.", e);
             }
             throw new WebserverSystemException(e);
         }
         catch (final IOException e) {
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Problem while loading resource '" + location + "'.");
             }
             else if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Problem while loading resource '" + location + "'.", e);
             }
             throw new WebserverSystemException(e);
         }
         final InputStream in;
         try {
             in = conn.getInputStream();
         }
         catch (final IOException e) {
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Problem while loading resource '" + location + "'.");
             }
             else if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Problem while loading resource '" + location + "'.", e);
             }
             throw new WebserverSystemException(e);
         }
         return in;
     }
 
 }
