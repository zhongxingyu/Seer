 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package de.escidoc.core.om.service.interfaces;
 
 import de.escidoc.core.common.annotation.Validate;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.PidAlreadyAssignedException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 
 import java.util.Map;
 
 /**
  * Interface for content relation handler.
  *
  * @author Steffen Wagner
  */
 public interface ContentRelationHandlerInterface {
 
     /**
      * Creates a Content Relation with the provided data.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The provided XML data in the body is only accepted if the size is less than ESCIDOC_MAX_XML_SIZE.<br/>
      * <p/>
      * The Content Relation may not exist<br/>
      * <p/>
      * See chapter 4 for detailed information about input and output data elements.<br/>
      * <p/>
      * <b>Tasks:</b><br/> <ul> <li>The XML data is validated</li> <li>The XML representation of the Content Relation
      * corresponding to XML-schema is returned as output.</li> </ul>
      *
      * @param xmlData The XML representation of the Content Relation to be created corresponding to XML-schema
      *                "content-relation.xsd".
      * @return The XML representation of the created Content Relation corresponding to XML-schema
      *         "content-relation.xsd".
      * @throws AuthenticationException        Thrown if authentication fails.
      * @throws AuthorizationException         Thrown if authorization fails.
      * @throws MissingAttributeValueException Thrown if a value of an attribute is missing.
      * @throws MissingMethodParameterException
      *                                        Thrown if method parameter is empty
      * @throws InvalidXmlException            Thrown if a provided XML is not valid.
      * @throws InvalidContentException        Thrown if a provided XML contains an invalid data.
      * @throws ReferencedResourceNotFoundException
      *                                        Thrown if one of the referenced resources is not exist.
      * @throws RelationPredicateNotFoundException
      *                                        Thrown if a provided relation type is not on the predicate list.
      * @throws SystemException                Thrown if a framework internal error occurs.
      */
     @Validate(param = 0, resolver = "getContentRelationSchemaLocation")
     String create(final String xmlData) throws AuthenticationException, AuthorizationException,
         MissingAttributeValueException, MissingMethodParameterException, InvalidXmlException, InvalidContentException,
         ReferencedResourceNotFoundException, RelationPredicateNotFoundException, SystemException;
 
     /**
      * Deletes the specified Content Relation.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The Content Relation must exist<br/>
      * <p/>
      * <b>Tasks:</b><br/> <ul> <li>The Content Relation is accessed using the provided reference.</li> <li>The content
      * will be removed from IR.</li> <li>No data is returned.</li> </ul>
      *
      * @param id The id of the resource Content Relation to be deleted.
      * @throws AuthenticationException Thrown if authentication fails.
      * @throws AuthorizationException  Thrown if authorization fails.
      * @throws ContentRelationNotFoundException
      *                                 Thrown if a content relation with the provided id cannot be found.
      * @throws LockingException        Thrown if Content Relation is locked by other user
      * @throws SystemException         Thrown if internal error occurs.
      */
     void delete(final String id) throws AuthenticationException, AuthorizationException,
         ContentRelationNotFoundException, SystemException, LockingException;
 
     /**
      * Retrieves the specified Content Relation.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The Content Relation must exist<br/>
      * <p/>
      * <b>Tasks:</b><br/> <ul> <li>The Content Relation is accessed using the provided reference.</li> <li>The XML
      * representation of the Content Relation corresponding to XML-schema is returned as output.</li> </ul>
      *
      * @param id The id of the Content Relation to be retrieved.
      * @return The XML representation of the retrieved Content Relation corresponding to XML-schema
      *         "content-relation.xsd".
      * @throws AuthenticationException Thrown if authentication fails.
      * @throws AuthorizationException  Thrown if authorization fails.
      * @throws ContentRelationNotFoundException
      *                                 Thrown if a Content Relation with the provided id cannot be found.
      * @throws SystemException         Thrown if a framework internal error occurs.
      */
     String retrieve(String id) throws AuthenticationException, AuthorizationException,
         ContentRelationNotFoundException, SystemException;
 
     /**
      * Retrieves a filtered list of content relations.
      *
      * @param parameterMap map of key - value pairs describing the filter
      * @return Returns XML representation of the list of content relation objects.
      * @throws InvalidSearchQueryException thrown if the given search query could not be translated into a SQL query
      * @throws SystemException             If case of internal error.
      */
     String retrieveContentRelations(final Map<String, String[]> parameterMap) throws InvalidSearchQueryException,
         SystemException;
 
     /**
      * Retrieves the properties of a Content Relation.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The Content Relation must exist<br/>
      * <p/>
      * <b>Tasks:</b><br/> <ul> <li>The Content Relation is accessed using the provided reference.</li> <li>The XML
      * representation of the Content Relation properties corresponding to XML-schema is returned as output.</li> </ul>
      *
      * @param id The id of the Content Relation to be retrieved.
      * @return The XML representation of the retrieved Content Relation properties corresponding to XML-schema "Content
      *         Relation.xsd".
      * @throws AuthenticationException Thrown if authentication fails.
      * @throws AuthorizationException  Thrown if authorization fails.
      * @throws ContentRelationNotFoundException
      *                                 Thrown if a Content Relation with the provided id cannot be found.
      * @throws SystemException         Thrown if a framework internal error occurs.
      */
     String retrieveProperties(final String id) throws AuthenticationException, AuthorizationException,
         ContentRelationNotFoundException, SystemException;
 
     /**
      * Update the specified Content Relation with the provided data.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The provided XML data in the body is only accepted if the size is less than ESCIDOC_MAX_XML_SIZE.<br/>
      * <p/>
      * The Content Relation must exist<br/> The the public-status is not "released".<br/> The content-relation is not
      * locked by another user.<br/> See chapter 4 for detailed information about input and output data elements.<br/>
      * <p/>
      * <b>Tasks:</b><br/> <ul> <li>The Content Relation is accessed using the provided reference.</li> <li>Optimistic
      * Locking criteria is checked.</li> <li>The XML data is validated.</li> <li>A md-records section contains the
      * md-records of the provided content-relation, which should remain after update and new md-records. The framework
      * will remove all existing md-records of the provided content-relation, which are not on this section.</li> <li>If
      * changed, the metadata records are updated.</li> <li>If changed, the description of the content-relation is
      * updated.</li> <li>The XML input data is updated and some new data is added.</li> <li>The XML representation of
      * the Content Relation corresponding to XML-schema is returned as output.</li> </ul>
      *
      * @param id      objid of Content Relation
      * @param xmlData XML representation of Content Relation
      * @return XML representation of updated Content Relation
      * @throws AuthenticationException        Thrown if authentication fails.
      * @throws AuthorizationException         Thrown if authorization fails.
      * @throws ContentRelationNotFoundException
      *                                        Thrown if no Content Relation could be found under provided objid
      * @throws OptimisticLockingException     Thrown if resource is updated in the meantime and last modification date
      *                                        differs
      * @throws InvalidStatusException         Thrown if resource has invalid status to update
      * @throws MissingAttributeValueException Thrown if attribute value is missing
      * @throws LockingException               Thrown if resource is locked through other user
      * @throws MissingMethodParameterException
      *                                        Thrown if method parameter is missing
      * @throws ReferencedResourceNotFoundException
      *                                        Thrown if referenced resource does not exist.
      * @throws RelationPredicateNotFoundException
      *                                        Thrown if the predicate is not registered.
      * @throws InvalidContentException        Thrown if content is invalid
      * @throws InvalidXmlException            Thrown if XML is invalid
      * @throws MissingAttributeValueException Thrown if attribute value is missing
      * @throws SystemException                Thrown if a framework internal error occurs.
      */
     @Validate(param = 1, resolver = "getContentRelationSchemaLocation")
     String update(final String id, final String xmlData) throws AuthenticationException, AuthorizationException,
         ContentRelationNotFoundException, OptimisticLockingException, InvalidContentException, InvalidStatusException,
         LockingException, MissingAttributeValueException, MissingMethodParameterException, SystemException,
         InvalidXmlException, ReferencedResourceNotFoundException, RelationPredicateNotFoundException;
 
     /**
      * Lock a content-relation.<br/>
      * <p/>
      * The content-relation will be locked by a user and no other user will be able to change this item until the
      * Lock-User (or the Admin) will unlock this item.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
     * <p/>
      * The content-relation must exist<br/>
     * <p/>
      * The content-relation is not locked.<br/>
     * <p/>
     * The public-status is not "withdrawn".<br/>
      * <p/>
      * <b>Tasks:</b><br/> <ul> <li>The content-relation is accessed using the provided Id.</li> <li>Optimistic Locking
      * criteria is checked.</li> <li>The lock-status of the content-relation is changed to "locked".</li> <li>The
     * lock-date and lock-owner are added to the content-relation.</li> <li>No data is returned.</li> </ul>
      *
      * @param id        The id of the content-relation to be revised.
      * @param taskParam The time stamp of the last modification of the content-relation. Necessary for optimistic
      *                  locking purpose. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * @throws AuthenticationException    Thrown if authentication fails.
      * @throws AuthorizationException     Thrown if authorization fails.
      * @throws ContentRelationNotFoundException
      *                                    Thrown if a content-relation with the specified id cannot be found.
      * @throws LockingException           Thrown if the content-relation is locked and the current user is not the one
      *                                    who locked it.
      * @throws InvalidStatusException     Thrown in case of an invalid status.
      * @throws MissingMethodParameterException
      *                                    If no data is provided.
      * @throws SystemException            If an internal error occurs.
      * @throws OptimisticLockingException If the provided latest-modification-date does not match.
      * @throws InvalidXmlException        Thrown if the taskParam has invalid structure.
      * @throws InvalidContentException    Thrown if the content of taskParam is invalid.
      */
     @Validate(param = 1, resolver = "getLockTaskParamSchemaLocation")
     String lock(final String id, final String taskParam) throws AuthenticationException, AuthorizationException,
         ContentRelationNotFoundException, LockingException, InvalidContentException, MissingMethodParameterException,
         SystemException, OptimisticLockingException, InvalidXmlException, InvalidStatusException;
 
     /**
      * Unlock a content-relation.<br/> The content-relation will be unlocked.<br> /
      * <p/>
      * <b>Prerequisites:</b><br/>
     * <p/>
      * The content-relation must exist<br/>
     * <p/>
      * The content-relation is in lock-status "locked".<br/>
     * <p/>
      * Only the user who has locked the content-relation (and the Admin) are allowed to unlock the
     * content-relation.<br/>
      * <p/>
      * <b>Tasks:</b><br/> <ul> <li>The content-relation is accessed using the provided Id.</li> <li>Optimistic Locking
      * criteria is checked.</li> <li>The lock-status of the content-relation is changed to "unlocked".</li> <li>The
     * lock-date and lock-owner are removed from the content-relation.</li> <li>No data is returned.</li> </ul>
      *
      * @param id        The id of the content-relation to be revised.
      * @param taskParam The time stamp of the last modification of the content-relation. Necessary for optimistic
      *                  locking purpose. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * @throws AuthenticationException    Thrown if authentication fails.
      * @throws AuthorizationException     Thrown if authorization fails.
      * @throws ContentRelationNotFoundException
      *                                    Thrown if a content-relation with the specified id cannot be found.
      * @throws LockingException           Thrown if the content-relation is locked and the current user is not the one
      *                                    who locked it.
      * @throws MissingMethodParameterException
      *                                    If no data is provided.
      * @throws SystemException            If an internal error occurs.
      * @throws OptimisticLockingException If the provided latest-modification-date does not match.
      * @throws InvalidXmlException        Thrown if the taskParam has invalid structure.
      * @throws InvalidContentException    Thrown if the content of taskParam is invalid.
      * @throws InvalidStatusException     Thrown if resource is not locked.
      */
     @Validate(param = 1, resolver = "getLockTaskParamSchemaLocation")
     String unlock(final String id, final String taskParam) throws AuthenticationException, AuthorizationException,
         ContentRelationNotFoundException, LockingException, MissingMethodParameterException, SystemException,
         OptimisticLockingException, InvalidXmlException, InvalidContentException, InvalidStatusException;
 
     /**
      * Submit a content-relation with a provided id.
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The content-relation must exist<br/>
      * <p/>
      * The content-relation is not locked by another user.<br/>
      * <p/>
      * The public-status is "pending" on "in-revision".<br/>
      * <p/>
      * <b>Tasks:</b><br/> <ul> <li>The content-relation is accessed using the provided Id.</li> <li>Optimistic Locking
      * criteria is checked.</li> <li>The public-status is changed to "submitted".</li> <li>No data is returned.</li>
      * </ul>
      *
      * @param id        The id of the content-relation to be revised.
      * @param taskParam The time stamp of the last modification of the content-relation. Necessary for optimistic
      *                  locking purpose. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * @throws AuthenticationException    Thrown if authentication fails.
      * @throws AuthorizationException     Thrown if authorization fails.
      * @throws ContentRelationNotFoundException
      *                                    Thrown if a content-relation with the specified id cannot be found.
      * @throws LockingException           Thrown if the content-relation is locked and the current user is not the one
      *                                    who locked it.
      * @throws InvalidStatusException     Thrown in case of an invalid status.
      * @throws MissingMethodParameterException
      *                                    If no data is provided.
      * @throws SystemException            If an internal error occurs.
      * @throws OptimisticLockingException If the provided latest-modification-date does not match.
      * @throws InvalidXmlException        Thrown if the taskParam has invalid structure.
      * @throws InvalidContentException    Thrown if the content of taskParam is invalid.
      */
     String submit(final String id, final String taskParam) throws AuthenticationException, AuthorizationException,
         ContentRelationNotFoundException, LockingException, InvalidStatusException, MissingMethodParameterException,
         SystemException, OptimisticLockingException, InvalidXmlException, InvalidContentException;
 
     /**
      * Revise a content-relation with a provided id.
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The content-relation must exist<br/>
      * <p/>
      * The content-relation is not locked by another user.<br/>
      * <p/>
      * The public-status is "submitted".<br/>
      * <p/>
      * <b>Tasks:</b><br/> <ul> <li>The content-relation is accessed using the provided Id.</li> <li>Optimistic Locking
      * criteria is checked.</li> <li>The public-status is changed to "in-revision".</li> <li>No data is returned.</li>
      * </ul>
      *
      * @param id        The id of the content-relation to be revised.
      * @param taskParam The time stamp of the last modification of the content-relation. Necessary for optimistic
      *                  locking purpose. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * @throws AuthenticationException    Thrown if authentication fails.
      * @throws AuthorizationException     Thrown if authorization fails.
      * @throws ContentRelationNotFoundException
      *                                    Thrown if a content-relation with the specified id cannot be found.
      * @throws LockingException           Thrown if the content-relation is locked and the current user is not the one
      *                                    who locked it.
      * @throws InvalidStatusException     Thrown in case of an invalid status.
      * @throws MissingMethodParameterException
      *                                    If no data is provided.
      * @throws SystemException            If an internal error occurs.
      * @throws OptimisticLockingException If the provided latest-modification-date does not match.
      * @throws XmlCorruptedException      Thrown if the taskParam has invalid structure.
      * @throws InvalidContentException    Thrown if the content of taskParam is invalid.
      */
     String revise(final String id, final String taskParam) throws AuthenticationException, AuthorizationException,
         ContentRelationNotFoundException, LockingException, InvalidStatusException, MissingMethodParameterException,
         SystemException, OptimisticLockingException, XmlCorruptedException, InvalidContentException;
 
     /**
      * Release a content-relation with a provided id.
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The content-relation must exist<br/>
      * <p/>
      * The content-relation is not locked by another user.<br/>
      * <p/>
      * The public-status is "submitted".<br/>
      * <p/>
      * <b>Tasks:</b><br/> <ul> <li>The content-relation is accessed using the provided Id.</li> <li>Optimistic Locking
      * criteria is checked.</li> <li>The public-status is changed to "released".</li> <li>No data is returned.</li>
      * </ul>
      *
      * @param id        The id of the content-relation to be revised.
      * @param taskParam The time stamp of the last modification of the content-relation. Necessary for optimistic
      *                  locking purpose. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * @throws AuthenticationException    Thrown if authentication fails.
      * @throws AuthorizationException     Thrown if authorization fails.
      * @throws ContentRelationNotFoundException
      *                                    Thrown if a content-relation with the specified id cannot be found.
      * @throws LockingException           Thrown if the content-relation is locked and the current user is not the one
      *                                    who locked it.
      * @throws InvalidStatusException     Thrown in case of an invalid status.
      * @throws MissingMethodParameterException
      *                                    If no data is provided.
      * @throws SystemException            If an internal error occurs.
      * @throws OptimisticLockingException If the provided latest-modification-date does not match.
      * @throws InvalidXmlException        Thrown if the taskParam has invalid structure.
      * @throws InvalidContentException    Thrown if the content of taskParam is invalid.
      */
     String release(final String id, final String taskParam) throws AuthenticationException, AuthorizationException,
         ContentRelationNotFoundException, LockingException, InvalidStatusException, MissingMethodParameterException,
         SystemException, OptimisticLockingException, InvalidXmlException, InvalidContentException;
 
     /**
      * Assign persistent identifier to a Content-relation object.
      *
      * @param id        The Id of the Content-relation witch is to assign with an ObjectPid.
      * @param taskParam XML snippet with parameter for the persistent identifier system.
      * @return The assigned persistent identifier for the Content-relation.
      * @throws AuthenticationException     Thrown if authentication fails.
      * @throws AuthorizationException      Thrown if authorization fails.
      * @throws ContentRelationNotFoundException
      *                                     Thrown if the object with id is does not exist or is no Item.
      * @throws LockingException            Thrown if the Item is locked
      * @throws MissingMethodParameterException
      *                                     Thrown if a parameter is missing within {@code taskParam}.
      * @throws OptimisticLockingException  Thrown if Item was altered in the mean time.
      * @throws PidAlreadyAssignedException Thrown if a Content-relation is already assigned a PID.
      * @throws InvalidXmlException         Thrown if taskParam has invalid XML.
      * @throws SystemException             Thrown in case of internal error.
      */
     String assignObjectPid(final String id, final String taskParam) throws AuthenticationException,
         AuthorizationException, ContentRelationNotFoundException, LockingException, MissingMethodParameterException,
         OptimisticLockingException, InvalidXmlException, SystemException, PidAlreadyAssignedException;
 
     /**
      * Retrieves escidoc XML representation of ContentRelations md-records.
      *
      * @param id objid of ContentRelation resource
      * @return escidoc XML representation of ContentRelation
      * @throws AuthenticationException Thrown if authentication fails.
      * @throws AuthorizationException  Thrown if authorization fails.
      * @throws ContentRelationNotFoundException
      *                                 Thrown if under provided id no ContentRelation could be found
      * @throws SystemException         Thrown if internal error occurs.
      */
     String retrieveMdRecords(final String id) throws AuthenticationException, AuthorizationException,
         ContentRelationNotFoundException, SystemException;
 
     /**
      * Retrieves escidoc XML representation of ContentRelations md-record with a provided name.
      *
      * @param id   objid of ContentRelation resource
      * @param name name of a md-record
      * @return escidoc XML representation of ContentRelation
      * @throws AuthenticationException   Thrown if authentication fails.
      * @throws AuthorizationException    Thrown if authorization fails.
      * @throws ContentRelationNotFoundException
      *                                   Thrown if under provided id no ContentRelation could be found
      * @throws MdRecordNotFoundException Thrown if md-record with provided name was not found
      * @throws SystemException           Thrown if internal error occurs.
      */
     String retrieveMdRecord(final String id, final String name) throws AuthenticationException, AuthorizationException,
         ContentRelationNotFoundException, MdRecordNotFoundException, SystemException;
 
     /**
      * Retrieves a list of registered predicates which can be used to create content relations.
      *
      * @return String containing a list with registered predicates.
      * @throws InvalidContentException Thrown if a xml file with an ontology has invalid content
      * @throws InvalidXmlException     Thrown if a xml file with an ontology is invalid rdf/xml
      * @throws SystemException         Thrown if internal error occurs.
      */
     String retrieveRegisteredPredicates() throws InvalidContentException, InvalidXmlException, SystemException;
 
     /**
      * Retrieve the subresource "resources" (@see Virtual Resource). <br/>
      * <p/>
      * This method returns a list of resources which are in functional relation to this resource.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The Content Relation must exist<br/>
      * <p/>
      * <b>Tasks:</b><br/> <ul> <li>The Content Relation is accessed using the provided reference.</li> <li>Determine
      * which resources are available.</li> <li>Create the list of resources.</li> <li>The XML representation of the
      * Content Relation resources corresponding to XML-schema is returned as output.</li> </ul>
      *
      * @param id The id of the Content Relation.
      * @return The XML representation of the list of virtual resources (@see Virtual Resource) of the Content Relation,
      *         corresponding to XML-schema "resources.xsd".
      * @throws AuthenticationException Thrown if authentication fails.
      * @throws AuthorizationException  Thrown if authorization fails.
      * @throws ContentRelationNotFoundException
      *                                 Thrown if a ContentRelation with the provided id cannot be found.
      * @throws MissingMethodParameterException
      *                                 Thrown if method parameter is missing.
      * @throws SystemException         Thrown if internal error occurs.
      */
     String retrieveResources(String id) throws AuthenticationException, AuthorizationException,
         ContentRelationNotFoundException, MissingMethodParameterException, SystemException;
 
 }
