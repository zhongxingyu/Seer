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
 package de.escidoc.core.oum.business.fedora.organizationalunit;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.HandlerBase;
 import de.escidoc.core.common.business.fedora.Utility;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.TmeException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingContentException;
 import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContextNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyExistsException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.PidAlreadyAssignedException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyAttributeViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyElementViolationException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.MultipleExtractor2;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit;
 import de.escidoc.core.oum.business.renderer.VelocityXmlOrganizationalUnitFoXmlRenderer;
 import de.escidoc.core.oum.business.renderer.interfaces.OrganizationalUnitFoXmlRendererInterface;
 import de.escidoc.core.oum.business.renderer.interfaces.OrganizationalUnitRendererInterface;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 
 import javax.xml.stream.XMLStreamException;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 /**
  * This class contains common methods for all handler classes of an organizational unit.
  *
  * @author Michael Schneider
  */
 public class OrganizationalUnitHandlerBase extends HandlerBase {
 
     private Deque<List<String>> pathes;
 
     @Autowired
     @Qualifier("business.Utility")
     private Utility utility;
 
     private OrganizationalUnit organizationalUnit;
 
     @Autowired
     private OrganizationalUnitRendererInterface renderer;
 
     private OrganizationalUnitFoXmlRendererInterface foxmlRenderer;
 
     public static final String DATA_ENCLOSING_TAG_START = "<data-contents>";
 
     public static final String DATA_ENCLOSING_TAG_END = "</data-contents>";
 
     /**
      * Binds an organizational unit object to this handler.
      *
      * @param id The id of the organizational unit which should be bound to this Handler.
      * @throws OrganizationalUnitNotFoundException
      *          If no organizational unit with the given id exists.
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     protected void setOrganizationalUnit(final String id) throws OrganizationalUnitNotFoundException,
         TripleStoreSystemException, IntegritySystemException {
 
         if (id != null) {
             try {
                 this.organizationalUnit = new OrganizationalUnit(id);
             }
             catch (final ResourceNotFoundException e) {
                 throw new OrganizationalUnitNotFoundException(e);
             }
         }
         else {
             this.organizationalUnit = null;
         }
     }
 
     /**
      * @return Get the current organizational unit resource.
      */
     protected OrganizationalUnit getOrganizationalUnit() {
         return this.organizationalUnit;
     }
 
     /**
      * @return Returns the utility.
      */
     @Override
     protected Utility getUtility() {
         return this.utility;
     }
 
     /**
      * Check the name. It may neither be empty nor null. Additionally it must be unique within its scope.
      *
      * @param name    The name.
      * @throws MissingElementValueException It the name is empty or null.
      */
     protected void checkName(final String name) throws MissingElementValueException {
        if (name == null || name.isEmpty()) {
             throw new MissingElementValueException("Name of organizational unit must be set!");
         }
     }
 
     /**
      * Initialize the pathes queue.
      */
     protected void initPathes() {
         this.pathes = new ArrayDeque<List<String>>();
     }
 
     /**
      * @return The pathes queue.
      */
     protected Deque<List<String>> getPathes() {
         return this.pathes;
     }
 
     /**
      * Expands the given path with its parents. If there are no parents the given path is the only result.
      *
      * @param path The path to expand.
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     protected void expandPaths(final List<String> path) throws TripleStoreSystemException {
 
         final List<String> organizationalUnitIds = getTripleStoreUtility().getParents(path.get(path.size() - 1));
         if (organizationalUnitIds != null) {
             if (organizationalUnitIds.isEmpty()) {
                 getPathes().push(new ArrayList<String>(path));
             }
             else {
                 for (final String parent : organizationalUnitIds) {
                     final List<String> newPath = new ArrayList<String>(path);
                     newPath.add(parent);
                     getPathes().push(newPath);
 
                 }
             }
         }
     }
 
     /**
      * Parse the organizational unit xml for create purposes with the stax parser.
      *
      * @param xml    The xml to parse.
      * @param parser The stax parser.
      * @throws MissingAttributeValueException If a mandatory attribute was not set.
      * @throws MissingElementValueException   If a mandatory element was not set.
      * @throws OrganizationalUnitNotFoundException
      *                                        If the organizational unit does not exist.
      * @throws XmlCorruptedException          Thrown if the schema validation of the provided data failed.
      * @throws MissingMdRecordException       If the required md-record is missing
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     protected void parseIncomingXmlForCreate(final String xml, final StaxParser parser)
         throws MissingAttributeValueException, MissingElementValueException, OrganizationalUnitNotFoundException,
         XmlCorruptedException, MissingMdRecordException, EncodingSystemException, IntegritySystemException,
         TripleStoreSystemException, XmlParserSystemException, WebserverSystemException {
 
         try {
             parser.parse(XmlUtility.convertToByteArrayInputStream(xml));
             parser.clearHandlerChain();
         }
         catch (final LockingException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final XMLStreamException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final OptimisticLockingException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final AlreadyExistsException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ContentModelNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ContextNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final MissingContentException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ReferencedResourceNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final RelationPredicateNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ContentRelationNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ReadonlyElementViolationException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ReadonlyAttributeViolationException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final InvalidContentException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final InvalidStatusException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final PidAlreadyAssignedException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final TmeException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
     }
 
     /**
      * Parse the organizational unit xml for update purposes with the stax parser.
      *
      * @param xml    The xml to parse.
      * @param parser The stax parser.
      * @throws OptimisticLockingException If the organizational unit was changed in the meantime.
      * @throws OrganizationalUnitNotFoundException
      *                                    If the organizational unit does not exist.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      */
     protected void parseIncomingXmlForUpdate(final String xml, final StaxParser parser)
         throws OptimisticLockingException, OrganizationalUnitNotFoundException, EncodingSystemException,
         IntegritySystemException, TripleStoreSystemException, XmlParserSystemException, WebserverSystemException,
         XmlCorruptedException {
 
         try {
             parser.parse(XmlUtility.convertToByteArrayInputStream(xml));
             parser.clearHandlerChain();
         }
         catch (final XMLStreamException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final MissingAttributeValueException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final MissingElementValueException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final MissingContentException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final MissingMdRecordException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ContextNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ContentModelNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final LockingException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final AlreadyExistsException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ReferencedResourceNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final RelationPredicateNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ContentRelationNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ReadonlyAttributeViolationException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final InvalidContentException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final InvalidStatusException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final ReadonlyElementViolationException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final PidAlreadyAssignedException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (final TmeException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
     }
 
     /**
      * Creates and initlizes a {@link MultipleExtractor2} that uses the provided {@link StaxParser}.<br/> The created
      * {@code MultipleExtractor2} extracts the pathes OU_ORGANIZATION_DETAILS_PATH.<br/> It is initilized with the
      * prefixes for ORGANIZATIONAL_UNIT_NAMESPACE_URI and XLINK_NS_URI.
      *
      * @param sp           The {@link StaxParser} to use.
      * @param mdRecordPath The xpath to organization-details element.
      * @return Returns the created {@link MultipleExtractor2} .
      */
     protected MultipleExtractor2 createMultipleExtractor(final StaxParser sp, final String mdRecordPath) {
 
         final HashMap<String, String> extractPathes = new HashMap<String, String>();
         extractPathes.put(mdRecordPath, "name");
         final Map<String, String> namespaceMap = new HashMap<String, String>(2);
         namespaceMap.put(Constants.ORGANIZATIONAL_UNIT_NAMESPACE_URI, Constants.ORGANIZATIONAL_UNIT_PREFIX);
         namespaceMap.put(Constants.XLINK_NS_URI, Constants.XLINK_NS_PREFIX);
         return new MultipleExtractor2(namespaceMap, extractPathes, sp);
     }
 
     /**
      * @return The foxml renderer.
      */
     public OrganizationalUnitFoXmlRendererInterface getFoxmlRenderer() {
 
         if (this.foxmlRenderer == null) {
             this.foxmlRenderer = new VelocityXmlOrganizationalUnitFoXmlRenderer();
         }
         return this.foxmlRenderer;
     }
 
     /**
      * @return the renderer
      */
     protected OrganizationalUnitRendererInterface getRenderer() {
         return this.renderer;
     }
 }
