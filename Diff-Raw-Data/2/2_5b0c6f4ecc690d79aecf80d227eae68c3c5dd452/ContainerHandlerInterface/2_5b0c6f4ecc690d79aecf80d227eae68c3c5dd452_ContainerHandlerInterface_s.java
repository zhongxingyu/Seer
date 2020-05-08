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
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContextException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContextStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidItemStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingContentException;
 import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ContainerNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContextNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.FileNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OperationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.XmlSchemaNotFoundException;
 import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyExistsException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyWithdrawnException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyAttributeViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyElementViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyVersionException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 
 import java.util.Map;
 
 import org.escidoc.core.utils.io.EscidocBinaryContent;
 
 /**
  * Interface of a container handler.
  * 
  * @author Torsten Tetteroo
  */
 public interface ContainerHandlerInterface {
 
     /**
      * Create a container<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The provided XML data in the body is only accepted if the size is less than ESCIDOC_MAX_XML_SIZE.<br/>
      * See chapter 4 for detailed information about input and output data elements<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The XML data is validated against the XML-Schema of a container.</li>
      * <li>It's checked weather the context id is provided. In a REST case it's checked weather REST-URL of a context is
      * correct.</li>
      * <li>It's checked weather the content-model id is provided. In a REST case it's checked weather REST-URL of a
      * content-model is correct</li>
      * <li>It's checked weather the context to the provided id exists.</li>
      * <li>It's checked weather the content-model to the provided id exists.</li>
      * <li>If a "struct-map" section is set, it's checked weather resources to all provided member ids exist.</li>
      * <li>If a "relations" section is set, it's checked weather resources to all provided relations targets exist and
      * weather used relations are part of the related ontologies.</li>
      * <li>The public-status of the container is set to "pending".</li>
      * <li>The Version 1 of the container is created. All members referenced in the struct-map are added to the
      * container.</li>
      * <li>The container is added to the provided context.</li>
      * <li>The XML input data is updated and some new data is added.</li>
      * <li>The XML representation of the container corresponding to XML-schema is returned as output.</li>
      * </ul>
      * <p/>
      * See chapter 4 for detailed information about input and output data elements.<br/>
      * Creates a resource container with the provided data.
      * 
      * @param xmlData
      *            The XML representation of the container to be created corresponding to XML-schema "container.xsd".
      * @return The XML representation of the created container corresponding to XML-schema "container.xsd".
      * @throws ContextNotFoundException
      *             Thrown if the context specified in the provided data cannot be found.
      * @throws ContentModelNotFoundException
      *             Thrown if the content model specified in the provided data cannot be found.
      * @throws ReferencedResourceNotFoundException
      *             If a resource referred from the provided data could not be found.
      * @throws InvalidContentException
      *             Thrown if the content is invalid.
      * @throws MissingMethodParameterException
      *             If one of the input parameters is not provided.
      * @throws XmlCorruptedException
      *             Thrown if provided data is corrupted.
      * @throws XmlSchemaValidationException
      *             Thrown if the schema validation of the provided data fails.
      * @throws MissingAttributeValueException
      *             Thrown if an value of an attribute not set, but expected.
      * @throws MissingElementValueException
      *             Thrown if a text of an element not set, but expected.
      * @throws MissingMdRecordException
      *             Thrown if the required metadata record (with name 'escidoc') is not provided.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws RelationPredicateNotFoundException
      *             Thrown if a content relation predicate specified in the provided data does not belong to the provided
      *             ontology.
      * @throws AuthenticationException
      *             Thrown if authentication fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws InvalidStatusException
      *             Thrown if the status of the specified context is not valid for executing the action.
      */
     @Validate(param = 0, resolver = "getContainerSchemaLocation")
     String create(final String xmlData) throws ContextNotFoundException, ContentModelNotFoundException,
         InvalidContentException, MissingMethodParameterException, MissingAttributeValueException,
         MissingElementValueException, SystemException, ReferencedResourceNotFoundException,
         RelationPredicateNotFoundException, AuthenticationException, AuthorizationException, InvalidStatusException,
         MissingMdRecordException, XmlCorruptedException, XmlSchemaValidationException;
 
     /**
      * Deletes a resource container with the provided id.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The public-status is "pending" or "in-revision".<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * There exist no members (items or containers) of this container.<br/>
      * <p/>
      * (Otherwise the removing of the container will fail)<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided Id.</li>
      * <li>The container will be deleted from IR.</li>
      * <li>No data is returned.</li>
      * </ul>
      * <p/>
      * <b>Note:</b> There are additional restrictions for deleting a container due to its Content-Type, e.g. if the
      * container is a Collection and already contains one or more Objects the deletion of the container will fail until
      * all contained Objects are moved to another Collection. These rules will be defined by the specified
      * Content-Model.<br/>
      * 
      * @param id
      *            The id of the container.
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id does not exist in the framework.
      * @throws LockingException
      *             Thrown if the container is locked and the current user is not the one who locked it.
      * @throws InvalidStatusException
      *             Thrown if a container in the current status is not allowed to delete.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws MissingMethodParameterException
      *             Thrown if no data is provided.
      * @throws AuthenticationException
      *             Thrown if authentication fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      */
     void delete(final String id) throws ContainerNotFoundException, LockingException, InvalidStatusException,
         SystemException, MissingMethodParameterException, AuthenticationException, AuthorizationException;
 
     /**
      * Retrieves a container resource with the provided id.<br/>
      * <p/>
      * This method retrieves the object which contains a list of its members but the method will not retrieve all
      * members in addition.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>The XML representation of the container corresponding to XML-schema is returned as output.</li>
      * </ul>
      * 
      * @param id
      *            The id of the container.
      * @return The XML representation of the retrieved container corresponding to XML-schema "container.xsd".
      * @throws AuthenticationException
      *             Thrown if authentication fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws MissingMethodParameterException
      *             Thrown if no data is provided.
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id does not exist in the framework.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      */
     String retrieve(final String id) throws AuthenticationException, AuthorizationException,
         MissingMethodParameterException, ContainerNotFoundException, SystemException;
 
     /**
      * Updates the container resource with the provided id using the provided data.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The provided XML data in the body is only accepted if the size is less than ESCIDOC_MAX_XML_SIZE.<br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * Only the latest version can be used here.<br/>
      * <p/>
      * See chapter 4 for detailed information about input and output data elements.<br/>
      * <p/>
      * The section "properties" has to be provided. It has to contain at least one element "content-model-specific".
      * Elements of this section and their values can be changed, deleted or added. Elements "name" and "description" and
      * their values can not be changed direct in this section. They can be updated via update of the corresponing
      * elements from "escidoc" md-record. All other elements will be discarded and set by the framework.<br/>
      * <p/>
      * The section "md-records" has to be provieded and has to contain an element "md-record" with an attribute "name"
      * set to "escidoc".<br/>
      * <p/>
      * <p/>
      * A relations section contains a list of "relation" elements with existing relations data of the provided
      * container, which should remain in a new version and a new relations data. The framework will remove all existing
      * relations of the provided container, which are not on the list. It is checked, if provided relation targets and
      * provided relation predicates exist. The attribute "xlink:href" of the "relation" element is set to a REST-url and
      * respectively the attribute "objid" is set to id of the target. A target id may not contain a version number.<br/>
      * <p/>
      * The section "struct-map" is read-only section. It will be discarded and set by the framework.<br/>
      * <p/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The XML data is validated against the XML-Schema of a container.</li>
      * <li>If changed, the internal metadata record is updated.</li>
      * <li>If the status of the version is "released" a new version is created and gets the status "pending" otherwise a
      * new version is created with the same status as before.</li>
      * <li>If the container is modified a new version of the container is created.</li>
      * <li>The XML input data is updated and some new data is added.</li>
      * <li>The XML representation of the container corresponding to XML-schema is returned as output.</li>
      * </ul>
      * <b>Note:</b> The status of a container can't bet changed by this method nor can't it be moved to a different
      * context. Please use the methods dedicated for this purpose.<br/>
      * 
      * @param id
      *            The id of the container.
      * @param xmlData
      *            The XML representation of the container to be updated corresponding to XML-schema "container.xsd".
      * @return The XML representation of the updated container corresponding to XML-schema "container.xsd".
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id does not exist in the framework.
      * @throws LockingException
      *             Thrown if the container is locked and the current user is not the one who locked it.
      * @throws InvalidContentException
      *             Thrown if the provided data for the resource is invalid.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws MissingMdRecordException
      *             Thrown if required md-record is missing
      * @throws InvalidXmlException
      *             Thrown if the schema validation of the provided data fails or provided data is corrupted.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws InvalidStatusException
      *             Thrown if a container in the current status is not allowed to update.
      * @throws ReadonlyVersionException
      *             If the provided container id does not identify the latest version.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws ReferencedResourceNotFoundException
      *             If a resource referred from the provided data could not be found.
      * @throws RelationPredicateNotFoundException
      *             Thrown if a content relation predicate specified in the provided data does not belong to the provided
      *             ontology.
      * @throws AuthenticationException
      *             Thrown if authentication fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws MissingAttributeValueException
      *             It a mandatory attribute value is missing.
      */
     @Validate(param = 1, resolver = "getContainerSchemaLocation")
     String update(final String id, final String xmlData) throws ContainerNotFoundException, LockingException,
         InvalidContentException, MissingMethodParameterException, InvalidXmlException, OptimisticLockingException,
         InvalidStatusException, ReadonlyVersionException, SystemException, ReferencedResourceNotFoundException,
         RelationPredicateNotFoundException, AuthenticationException, AuthorizationException,
         MissingAttributeValueException, MissingMdRecordException;
 
     /**
      * The list of all containers matching the given filter criteria will be created.
      * <p/>
      * <br/>
      * See chapter "Filters" for detailed information about filter definitions.
      * 
      * @param filter
      *            The filter criteria to select the containers given as a map of key - value pairs
      * @return The XML representation of the the filtered list of containers corresponding to SRW schema.
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into a CQL query
      * @throws InvalidXmlException
      *             If the given xml is not valid.
      * @throws MissingMethodParameterException
      *             If the parameter filter is not given.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      */
     String retrieveContainers(final Map<String, String[]> filter) throws InvalidXmlException,
         InvalidSearchQueryException, MissingMethodParameterException, SystemException;
 
     /**
      * Retrieve a list of members of a container applying filters.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Check whether all filter criteria names are valid.</li>
      * <li>The members are accessed using the provided filters.</li>
      * <li>The XML representations of the list of all members corresponding to SRW schema is returned as output</li>
      * </ul>
      * <br/>
      * See chapter "Filters" for detailed information about filter definitions.
      * 
      * @param id
      *            The id of the container.
      * @param filter
      *            The filter criteria to select the containers given as a map of key - value pairs
      * @return The XML representation of the list of member corresponding to the SRW schema.
      * @throws MissingMethodParameterException
      *             If the parameter filter is not given.
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id does not exist in the framework.
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into a CQL query
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      */
     String retrieveMembers(final String id, final Map<String, String[]> filter) throws InvalidSearchQueryException,
         MissingMethodParameterException, ContainerNotFoundException, SystemException;
 
     /**
      * Retrieve a list of tocs of a container applying filters.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Check whether all filter criteria names are valid.</li>
      * <li>The members are accessed using the provided filters.</li>
      * <li>The XML representations of the list of all members corresponding to the SRW schema is returned as output</li>
      * </ul>
      * <br/>
      * See chapter "Filters" for detailed information about filter definitions.
      * 
      * @param id
      *            The id of the container.
      * @param filter
      *            The filter criteria to select the containers given as a map of key - value pairs.
      * @return The XML representation of the list of member corresponding to the SRW schema.
      * @throws MissingMethodParameterException
      *             If the parameter filter is not given.
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id does not exist in the framework.
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into a CQL query
      * @throws InvalidXmlException
      *             If the given xml is not valid.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      */
     String retrieveTocs(final String id, final Map<String, String[]> filter) throws InvalidSearchQueryException,
         MissingMethodParameterException, ContainerNotFoundException, InvalidXmlException, SystemException;
 
     /**
      * Add one or more members to a container.<br/>
      * <p/>
      * This members will be added to the member list of that container.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * All referenced objects must exist<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * Only the latest version can be used here.<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>Add the members to the member list.</li>
      * <li>A new version is created.</li>
      * <li>A XML containing the new latest version timestamp is returned.</li>
      * </ul>
      * The members to be added to the container are listed in the "param" section of the input data using their IDs.
      * <p/>
      * <b>Parameter for request:</b> (example)<br/>
      * <p/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;id&gt;escidoc:23232&lt;/id&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;id&gt;escidoc:12121&lt;/id&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the container.
      * @param taskParam The XML representation of task parameters conforming to members-task-param.xsd. 
      * Including the timestamp of the last modification (attribute 'last-modification-date', required, necessary for optimistical locking purpose)
      * and the the list of Members id to be add to the container. (example above)
      * @return last-modification-date within XML (result.xsd)
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id does not exist in the framework.
      * @throws LockingException
      *             Thrown if the container is locked and the current user is not the one who locked it.
      * @throws InvalidContentException
      *             Thrown if for any ids there is no a resource in the framework or the resource is neither Item nor
      *             Container.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws InvalidContextException
      *             Thrown if a context of any member differs from a context of the container with the provided id.
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws MissingAttributeValueException
      *             It a mandatory attribute value is missing.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      */
     @Validate(param = 1, resolver = "getMembersTaskParamSchemaLocation")
     String addMembers(final String id, final String taskParam) throws ContainerNotFoundException, LockingException,
         InvalidContentException, MissingMethodParameterException, SystemException, InvalidContextException,
         AuthenticationException, AuthorizationException, OptimisticLockingException, MissingAttributeValueException;
 
     /**
      * Add one or more tocs to a container.<br/>
      * <p/>
      * The tocs will be added to the member list of that container.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * All referenced objects must exist<br/>
      * <p/>
      * All referenced objects must have Toc content model<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * Only the latest version can be used here.<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>Add the tocs to the member list.</li>
      * <li>A new version is created.</li>
      * <li>A XML containing the new latest version timestamp is returned.</li>
      * </ul>
      * The tocs to be added to the container are listed in the "param" section of the input data using their IDs.
      * <p/>
      * <b>Parameter for request:</b> (example)<br/>
      * <p/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;id&gt;escidoc:23232&lt;/id&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;id&gt;escidoc:12121&lt;/id&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the container.
      * @param taskParam
      *            The list of tocs to add to the container. (See example above.)
      * @return last-modification-date within XML (result.xsd)
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id does not exist in the framework.
      * @throws LockingException
      *             Thrown if the container is locked and the current user is not the one who locked it.
      * @throws InvalidContentException
      *             Thrown if for any ids there is no a resource in the framework.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws InvalidContextException
      *             Thrown if a context of any member differs from a context of the container with the provided id.
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws MissingAttributeValueException
      *             It a mandatory attribute value is missing.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      */
     @Validate(param = 1, resolver = "getAssignTaskParamSchemaLocation")
     String addTocs(final String id, final String taskParam) throws ContainerNotFoundException, LockingException,
         InvalidContentException, MissingMethodParameterException, SystemException, InvalidContextException,
         AuthenticationException, AuthorizationException, OptimisticLockingException, MissingAttributeValueException;
 
     /**
      * Remove one or more members from a container.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The provided XML data in the body is only accepted if the size is less than ESCIDOC_MAX_XML_SIZE.<br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "released" or "withdrawn".<br/>
      * <p/>
      * Only the latest version can be used here.<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>Remove the members from the member list.</li>
      * <li>A new version is created.</li>
      * <li>A XML containing the new latest version timestamp is returned.</li>
      * </ul>
      * The members to be removed from the container are listed in the "param" section of the input data using their IDs.
      * <p/>
      * <b>Parameter for request:</b> (example)<br/>
      * <p/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;id&gt;escidoc:23232&lt;/id&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;id&gt;escidoc:12121&lt;/id&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the container.
      * @param taskParam The XML representation of task parameters conforming to members-task-param.xsd. 
      * Including the timestamp of the last modification (attribute 'last-modification-date', required, necessary for optimistical locking purpose)
      * and the list of Members id to be removed from the container.
      *  (example above) (See example above.)
      * @return last-modification-date within XML (result.xsd)
      * @throws ContextNotFoundException
      *             Thrown if provided Context could not be found
      * @throws LockingException
      *             Thrown in case of an optimistic locking error.
      * @throws XmlSchemaValidationException
      *             Thrown if the schema validation of the provided data fails.
      * @throws ItemNotFoundException
      *             Thrown if the removed member should be an Item but could not be found
      * @throws ContainerNotFoundException
      *             Thrown if the removed member should be an Container but could not be found
      * @throws InvalidContextStatusException
      *             Thrown if the Context is in invalid status
      * @throws InvalidItemStatusException
      *             Thrown if the Item member is in invalid status
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws InvalidContentException
      *             Thrown if the taskParam has invalid content
      */
     @Validate(param = 1, resolver = "getMembersTaskParamSchemaLocation")
     String removeMembers(final String id, final String taskParam) throws ContextNotFoundException, LockingException,
         XmlSchemaValidationException, ItemNotFoundException, InvalidContextStatusException, InvalidItemStatusException,
         AuthenticationException, AuthorizationException, SystemException, ContainerNotFoundException,
         InvalidContentException;
 
     //
     // Subresource - metadata record
     //
 
     /**
      * Add a Metadata Record to a container.<br/>
      * <p/>
      * Method is deprecated because of inconsistent naming. Use createMdRecord instead of.
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The provided XML data in the body is only accepted if the size is less than ESCIDOC_MAX_XML_SIZE.<br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>Add new metadata record to the container.</li>
      * <li>The container is marked for indexing (to be done by SB).</li>
      * <li>A new version is created.</li>
      * <li>In case of public-status "released", the status of the new version is set to "pending".</li>
      * <li>Timestamp for the container of the latest modification in the system is updated to the current time.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the container.
      * @param xmlData
      *            The XML representation of the metadata record to be created.
      * @return The XML representation of the created metadata record.
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id cannot be found.
      * @throws LockingException
      *             Thrown in case of an optimistic locking error.
      * @throws MissingMethodParameterException
      *             Thrown if a method parameter is missing
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws InvalidXmlException
      *             Thrown if taskParam contains invalid data.
      */
     @Deprecated
     @Validate(param = 2, resolver = "getContainerSchemaLocation")
     String createMetadataRecord(final String id, final String xmlData) throws ContainerNotFoundException,
         InvalidXmlException, LockingException, MissingMethodParameterException, AuthenticationException,
         AuthorizationException, SystemException;
 
     /**
      * Add a Metadata Record to a container.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The provided XML data in the body is only accepted if the size is less than ESCIDOC_MAX_XML_SIZE.<br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>Add new metadata record to the container.</li>
      * <li>The container is marked for indexing (to be done by SB).</li>
      * <li>A new version is created.</li>
      * <li>In case of public-status "released", the status of the new version is set to "pending".</li>
      * <li>Timestamp for the container of the latest modification in the system is updated to the current time.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the container.
      * @param xmlData
      *            The XML representation of the metadata record to be created.
      * @return The XML representation of the created metadata record.
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id cannot be found.
      * @throws LockingException
      *             Thrown in case of an optimistic locking error.
      * @throws MissingMethodParameterException
      *             Thrown if a method parameter is missing
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws InvalidXmlException
      *             Thrown if taskParam contains invalid data.
      */
    @Validate(param = 2, resolver = "getContainerSchemaLocation")
     String createMdRecord(final String id, final String xmlData) throws ContainerNotFoundException,
         InvalidXmlException, LockingException, MissingMethodParameterException, AuthenticationException,
         AuthorizationException, SystemException;
 
     /**
      * Retrieve a metadata record with the provided name of a container with the provided id.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * The metadata record with the provided name must exist in the container<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>The XML data for the Metadata Record is created.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the container.
      * @param mdRecordId
      *            The id of the Metadata Record.
      * @return The XML Metadata Record of the container.
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id cannot be found.
      * @throws MdRecordNotFoundException
      *             Thrown if the container does not have the specified metadata record.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      */
     String retrieveMdRecord(final String id, final String mdRecordId) throws ContainerNotFoundException,
         MissingMethodParameterException, MdRecordNotFoundException, AuthenticationException, AuthorizationException,
         SystemException;
 
     /**
      * Retrieve content of MD Record.
      * 
      * @param id
      *            The id of the container.
      * @param mdRecordId
      *            The id of the Metadata Record.
      * @return XML of Md Record content.
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id cannot be found.
      * @throws MdRecordNotFoundException
      *             Thrown if the container does not have the specified metadata record.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      */
     String retrieveMdRecordContent(final String id, final String mdRecordId) throws ContainerNotFoundException,
         MdRecordNotFoundException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
         SystemException;
 
     /**
      * Retrieve content of DC record.
      * 
      * @param id
      *            The id of the container.
      * @return DC record.
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id cannot be found.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      */
     String retrieveDcRecordContent(final String id) throws ContainerNotFoundException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException, SystemException;
 
     /**
      * Updates the metadata record of a container.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The provided XML data in the body is only accepted if the size is less than ESCIDOC_MAX_XML_SIZE.<br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>Update the Metadata Record.</li>
      * <li>The container is marked for indexing (to be done by SB).</li>
      * <li>A new version is created.</li>
      * <li>In case of public-status "released", a new revision will be created.</li>
      * <li>Timestamp of the latest modification in the system is updated to the current time.</li>
      * <li>The updated XML data is returned as output.</li>
      * </ul>
      * 
      * @param id
      *            The id of the container.
      * @param mdRecordId
      *            The id of the metadata record.
      * @param xmlData
      *            The XML Metadata Record for the container.
      * @return The XML Metadata Record of the container.
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id cannot be found.
      * @throws LockingException
      *             Thrown in case of an optimistic locking error.
      * @throws XmlSchemaNotFoundException
      *             Thrown if the specified schema cannot be found.
      * @throws MdRecordNotFoundException
      *             Thrown if the container does not have the specified metadata record.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws InvalidStatusException
      *             Thrown if Conmtainer is in wrong status to update.
      * @throws InvalidXmlException
      *             Thrown if taskParam contains invalid data.
      * @throws ReadonlyVersionException
      *             If the provided container id does not identify the latest version.
      */
     @Validate(param = 2, resolver = "getContainerSchemaLocation")
     String updateMetadataRecord(final String id, final String mdRecordId, final String xmlData)
         throws ContainerNotFoundException, LockingException, XmlSchemaNotFoundException, MdRecordNotFoundException,
         MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException,
         InvalidXmlException, InvalidStatusException, ReadonlyVersionException;
 
     //
     // Subresource - metadata records
     //
 
     /**
      * Retrieve all Metadata Records of a container.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>The XML data for the set of Metadata Records is created.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the container.
      * @return The XML representation of the retrieved metadata records corresponding to XML-schema
      *         "metadatarecords.xsd".
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id cannot be found.
      * @throws MissingMethodParameterException
      *             Thrown if the container id is not provided.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      */
     String retrieveMdRecords(final String id) throws ContainerNotFoundException, MissingMethodParameterException,
         AuthenticationException, AuthorizationException, SystemException;
 
     //
     // Subresource - properties
     //
 
     /**
      * Retrieve the subresource properties of a container.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Retrieve the properties of the container. This includes the Content Model specific properties, too</li>
      * <li>The XML data for the properties is created.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the container.
      * @return The XML representation of the properties subresource corresponding to XML-schema "properties.xsd".
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id cannot be found.
      * @throws MissingMethodParameterException
      *             Thrown if the container id is not provided.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      */
     String retrieveProperties(final String id) throws ContainerNotFoundException, MissingMethodParameterException,
         AuthenticationException, AuthorizationException, SystemException;
 
     //
     // Subresource - resources
     //
 
     /**
      * Retrieve the list of related resources of a container with the provided id.<br/>
      * This methods returns a list of additional resources which aren't stored in IR but created on request.<br/>
      * <p/>
      * <b>Prerequisites:</b><br>
      * <p/>
      * The container must exist<br>
      * <p/>
      * <b>Tasks:</b><br>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Determine which resources are available.</li>
      * <li>Create the list of resources.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the container.
      * @return The XML representation of the list of virtual resources of the container.
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id cannot be found.
      * @throws MissingMethodParameterException
      *             Thrown if the container id is not provided.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      */
     String retrieveResources(final String id) throws ContainerNotFoundException, MissingMethodParameterException,
         AuthenticationException, AuthorizationException, SystemException;
 
     /**
      * Retrieve the content of the specified virtual Resources of a container.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * The container must exist.<br/>
      * The specified resource must exist.<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided Id.</li>
      * <li>Determine if the resource is available.</li>
      * <li>Create the content of the resource.</li>
      * <li>The data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the container.
      * @param resourceName
      *            The name of the resource.
      * @param parameters
      *            Optional parameters to influence the content of the resource.
      * @return The content of the resource.
      * @throws ContainerNotFoundException
      *             Thrown if an container with the specified id cannot be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If the ID or the resource name is not provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws OperationNotFoundException
      *             If there is no operation for the given name.
      */
     EscidocBinaryContent retrieveResource(
         final String id, final String resourceName, final Map<String, String[]> parameters) throws SystemException,
         ContainerNotFoundException, MissingMethodParameterException, AuthenticationException, AuthorizationException,
         OperationNotFoundException;
 
     //
     // Subresource - relations
     //
 
     /**
      * Retrieve the list of Relations of the container, where the container is the relations source.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Determine which relations are available.</li>
      * <li>Create the list of relations.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the container.
      * @return The XML representation of the list of relations of the container corresponding to XML-schema
      *         "relations.xsd".
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id cannot be found.
      * @throws MissingMethodParameterException
      *             Thrown if the container id is not provided.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      */
     String retrieveRelations(final String id) throws ContainerNotFoundException, MissingMethodParameterException,
         AuthenticationException, AuthorizationException, SystemException;
 
     /**
      * Retrieve Struct Map of Container.
      * 
      * @param id
      *            The id of the Container.
      * @return Cotntainer struct-map.
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id cannot be found.
      * @throws MissingMethodParameterException
      *             Thrown if the container id is not provided.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      */
     String retrieveStructMap(final String id) throws ContainerNotFoundException, MissingMethodParameterException,
         AuthenticationException, AuthorizationException, SystemException;
 
     //
     // Version history
     //
 
     /**
      * Retrieves the versioning history of the container with the provided id.<br/>
      * <p/>
      * This method is only allowed if the user is a depositor of that object or other users with special privileges,
      * e.g. someone like an Admin.<br/>
      * <p/>
      * The version history contains a version element for every version of the object inside a version-history element.
      * A version element contains elements with values for version-number, timestamp, version-status, valid-status and a
      * comment which belongs to the latest update of that version.<br/>
      * <p/>
      * A modification of the object that does not create a new version is recorded as premis event inside a events
      * element in the version element.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Retrieve all version and revision information.</li>
      * <li>The XML datastream 'version-history' is returned.</li>
      * </ul>
      * <b>Example:</b><br/>
      * <p/>
      * 
      * <pre>
      * &lt;escidocVersions:version-history&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;!-- namespaces omitted for readability --&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;escidocVersions:version objid=&quot;escidoc:13164:5&quot;
      * </pre>
      * <p/>
      * 
      * <pre>
      * timestamp = &quot;2007-08-24T15:11:45.218Z&quot;
      * </pre>
      * <p/>
      * 
      * <pre>
      *        xlink:href=&quot;/ir/container/escidoc:13164:5&quot;
      * </pre>
      * <p/>
      * 
      * <pre>
      *        xlink:title=&quot;Version 5&quot;
      * </pre>
      * <p/>
      * 
      * <pre>
      *        xml:base=&quot;http://localhost:8080&quot; xlink:type=&quot;simple&quot;
      * </pre>
      * <p/>
      * 
      * <pre>
      *        last-modification-date=&quot;2007-08-24T15:11:45.218Z&quot; &gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *     &lt;escidocVersions:version-number&gt;5&lt;/escidocVersions:version-number&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *     &lt;escidocVersions:timestamp&gt;2007-08-24T15:11:45.218Z
      * </pre>
      * <p/>
      * 
      * <pre>
      *     &lt;/escidocVersions:timestamp&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *     &lt;escidocVersions:version-status&gt;released&lt;/escidocVersions:version-status&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *     &lt;escidocVersions:valid-status&gt;valid&lt;/escidocVersions:valid-status&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *     &lt;escidocVersions:comment&gt;Update comment&lt;/escidocVersions:comment&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *     &lt;escidocVersions:events&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *      &lt;premis:event xmlId=&quot;v5e1&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *      &lt;premis:eventIdentifier&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *       &lt;premis:eventIdentifierType&gt;URL&lt;/premis:eventIdentifierType&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *       &lt;premis:eventIdentifierValue&gt;/ir/container/version-history#v5e1
      * </pre>
      * <p/>
      * 
      * <pre>
      *       &lt;/premis:eventIdentifierValue&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *      &lt;/premis:eventIdentifier&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *      &lt;premis:eventType&gt;update&lt;/premis:eventType&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *      &lt;premis:eventDateTime&gt;2007-08-24T15:11:45.218Z&lt;/premis:eventDateTime&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *      &lt;premis:eventDetail&gt;Update comment&lt;/premis:eventDetail&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *      &lt;premis:linkingAgentIdentifier
      * </pre>
      * <p/>
      * 
      * <pre>
      *              xlink:href=&quot;/aa/user-account/escidoc:user42&quot;
      * </pre>
      * <p/>
      * 
      * <pre>
      *              xlink:title=&quot;roland&quot; xlink:type=&quot;simple&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *        &lt;premis:linkingAgentIdentifierType&gt;escidoc-internal
      * </pre>
      * <p/>
      * 
      * <pre>
      *        &lt;/premis:linkingAgentIdentifierType&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *       &lt;premis:linkingAgentIdentifierValue&gt;escidoc:user42
      * </pre>
      * <p/>
      * 
      * <pre>
      *       &lt;/premis:linkingAgentIdentifierValue&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *      &lt;/premis:linkingAgentIdentifier&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *      &lt;premis:linkingObjectIdentifier&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *       &lt;premis:linkingObjectIdentifierType&gt;escidoc-internal
      * </pre>
      * <p/>
      * 
      * <pre>
      *       &lt;/premis:linkingObjectIdentifierType&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *       &lt;premis:linkingObjectIdentifierValue&gt;escidoc:13164
      * </pre>
      * <p/>
      * 
      * <pre>
      *       &lt;/premis:linkingObjectIdentifierValue&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/premis:linkingObjectIdentifier&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *     &lt;/premis:event&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *    &lt;/escidocVersions:events&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;/escidocVersions:version&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;!-- some more versions --&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/escidocVersions:version-history&gt;
      * </pre>
      * 
      * @param id
      *            The id of the container.
      * @return The XML representation of the version history of the container. (see example above)
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id cannot be found.
      * @throws MissingMethodParameterException
      *             Thrown if the container id is not provided.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      */
     String retrieveVersionHistory(final String id) throws ContainerNotFoundException, MissingMethodParameterException,
         AuthenticationException, AuthorizationException, SystemException;
 
     /**
      * Retrieve a list with references to all Containers to that this Container is directly subordinated.<br />
      * <br />
      * <b>Prerequisites:</b><br />
      * <br />
      * The Container must exist.<br />
      * <br />
      * <b>Tasks:</b>
      * <ul>
      * <li>The XML representation of a list of references to the parent Containers is created.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The identifier of the Container.
      * @return The XML representation of the parents of that Container corresponding to XML schema "parents.xsd".
      * @throws MissingMethodParameterException
      *             Thrown if the XML data is not provided.
      * @throws ContainerNotFoundException
      *             Thrown if an Container with the provided id does not exist.
      * @throws SystemException
      *             Thrown in case of an internal error.
      * @throws AuthenticationException
      *             Thrown if the authentication failed due to an invalid provided eSciDoc user handle.
      * @throws AuthorizationException
      *             Thrown if the authorization failed.
      */
     String retrieveParents(final String id) throws AuthenticationException, AuthorizationException,
         MissingMethodParameterException, ContainerNotFoundException, SystemException;
 
     /**
      * Release a Container.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The latest-version-status is "submitted".<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * Only the lastest version can be used here.<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The status of the latest-version is changed to "released".</li>
      * <li>Latest version date is updated.</li>
      * <li>No new version is created.</li>
      * <li>A XML containing the new latest version timestamp is returned.</li>
      * </ul>
      * <b>Parameter for request:</b> (example)<br/>
      * <p/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;comment&gt;release comment.&lt;/comment&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the Container to be released.
      * @param taskParam The XML representation of task parameters conforming to status-task-param.xsd. 
      * Including the timestamp of the last modification (attribute 'last-modification-date', required, necessary for optimistical locking purpose)
      * and an optional comment. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * @throws ContainerNotFoundException
      *             Thrown if a Container with the specified id cannot be found.
      * @throws LockingException
      *             Thrown if the container is locked and the current user is not the one who locked it.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws AuthenticationException
      *             Thrown if authentication fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws InvalidStatusException
      *             Thrown if a container in the current status is not allowed to update.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws ReadonlyVersionException
      *             If the provided container id does not identify the latest version.
      * @throws InvalidXmlException
      *             Thrown if taskParam has invalid structure.
      */
     @Validate(param = 1, resolver = "getStatusTaskParamSchemaLocation")
     String release(final String id, final String taskParam) throws ContainerNotFoundException, LockingException,
         MissingMethodParameterException, AuthenticationException, AuthorizationException, InvalidStatusException,
         SystemException, OptimisticLockingException, ReadonlyVersionException, InvalidXmlException;
 
     /**
      * Submit a Container.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The latest-version-status is "pending" or "in-revision".<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * Only the lastest version can be used here.<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The status of the latest-version is changed to "submitted".</li>
      * <li>Latest version date is updated.</li>
      * <li>No new version is created.</li>
      * <li>A XML containing the new latest version timestamp is returned.</li>
      * </ul>
      * <b>Parameter for request:</b> (example)<br/>
      * <p/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;comment&gt;submit comment.&lt;/comment&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the Container to be submitted.
      * @param taskParam The XML representation of task parameters conforming to status-task-param.xsd. 
      * Including the timestamp of the last modification (attribute 'last-modification-date', required, necessary for optimistical locking purpose)
      * and an optional comment. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * @throws ContainerNotFoundException
      *             Thrown if a Container with the specified id cannot be found.
      * @throws LockingException
      *             Thrown if the container is locked and the current user is not the one who locked it.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws AuthenticationException
      *             Thrown if authentication fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws InvalidStatusException
      *             Thrown if a container in the current status is not allowed to update.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws ReadonlyVersionException
      *             If the provided container id does not identify the latest version.
      * @throws InvalidXmlException
      *             Thrown if taskParam has invalid structure.
      */
     @Validate(param = 1, resolver = "getStatusTaskParamSchemaLocation")
     String submit(final String id, final String taskParam) throws ContainerNotFoundException, LockingException,
         MissingMethodParameterException, AuthenticationException, AuthorizationException, InvalidStatusException,
         SystemException, OptimisticLockingException, ReadonlyVersionException, InvalidXmlException;
 
     /**
      * Revise a Container.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The latest-version-status is "submitted".<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * Only the lastest version can be used here.<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The status of the latest-version is changed to "in-revision".</li>
      * <li>Latest version date is updated.</li>
      * <li>No new version is created.</li>
      * <li>A XML containing the new latest version timestamp is returned.</li>
      * </ul>
      * <b>Parameter for request:</b> (example)<br/>
      * <p/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;comment&gt;revise comment.&lt;/comment&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the Container to be revised.
      * @param taskParam The XML representation of task parameters conforming to status-task-param.xsd. 
      * Including the timestamp of the last modification (attribute 'last-modification-date', required, necessary for optimistical locking purpose)
      * and an optional comment. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * @throws ContainerNotFoundException
      *             Thrown if a Container with the specified id cannot be found.
      * @throws LockingException
      *             Thrown if the container is locked and the current user is not the one who locked it.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws InvalidStatusException
      *             Thrown if a container in the current status is not allowed to update.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws ReadonlyVersionException
      *             If the provided container id does not identify the latest version.
      * @throws XmlCorruptedException
      *             Thrown if taskParam has invalid structure.
      */
     @Validate(param = 1, resolver = "getStatusTaskParamSchemaLocation")
     String revise(final String id, final String taskParam) throws ContainerNotFoundException, LockingException,
         MissingMethodParameterException, InvalidStatusException, SystemException, OptimisticLockingException,
         ReadonlyVersionException, XmlCorruptedException;
 
     /**
      * Withdraw a Container.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The public-status is "released".<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * Only the lastest version can be used here.<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The status of the latest-version is changed to "withdrawn".</li>
      * <li>Latest version date is updated.</li>
      * <li>No new version is created.</li>
      * <li>A XML containing the new latest version timestamp is returned.</li>
      * </ul>
      * <b>Parameter for request:</b> (example)<br/>
      * <p/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;comment&gt;withdraw comment.&lt;/comment&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the Container to be withdrawn.
      * @param taskParam The XML representation of task parameters conforming to status-task-param.xsd. 
      * Including the timestamp of the last modification (attribute 'last-modification-date', required, necessary for optimistical locking purpose)
      * and an optional comment. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * @throws ContainerNotFoundException
      *             Thrown if a Container with the specified id cannot be found.
      * @throws LockingException
      *             Thrown if the container is locked and the current user is not the one who locked it.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws AuthenticationException
      *             Thrown if authentication fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws InvalidStatusException
      *             Thrown if a container in the current status is not allowed to update.
      * @throws AlreadyWithdrawnException
      *             If the item is already withdrawn.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws ReadonlyVersionException
      *             If the provided container id does not identify the latest version.
      * @throws InvalidXmlException
      *             Thrown if taskParam has invalid structure.
      */
     @Validate(param = 1, resolver = "getStatusTaskParamSchemaLocation")
     String withdraw(final String id, final String taskParam) throws ContainerNotFoundException, LockingException,
         MissingMethodParameterException, AuthenticationException, AuthorizationException, InvalidStatusException,
         SystemException, OptimisticLockingException, AlreadyWithdrawnException, ReadonlyVersionException,
         InvalidXmlException;
 
     /**
      * Lock a Container.<br/>
      * <p/>
      * The Container will be locked for changes until the lockOwner (the current user) or an Administrator unlocks the
      * Container.<br/>
      * <p/>
      * <b>Prerequisites:</b>
      * <p/>
      * The container must exist<br/>
      * The container is not locked.<br/>
      * The public-status is not "withdrawn".
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The lock-status of the container is changed to "locked".</li>
      * <li>The lock-date is added to the container.</li>
      * <li>The lock-owner is added to the container.</li>
      * <li>No new version is created.</li>
      * <li>A XML containing the new latest version timestamp is returned (in comply to result.xsd).</li>
      * </ul>
      * <b>Parameter for request:</b><br/>
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot; /&gt;
      * </pre>
      * 
      * @param id
      *            The id of the Container to be locked.
      * @param taskParam The XML representation of task parameters conforming to lock-task-param.xsd. 
      * Including the timestamp of the last modification of the container (attribute 'last-modification-date', required).
      * The last-modification-date is necessary for optimistical locking purpose. (example above)
      * @return last-modification-date within XML (result.xsd)
      * @throws ContainerNotFoundException
      *             Thrown if the Container was not found.
      * @throws LockingException
      *             Thrown if the Container could not been locked.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws AuthenticationException
      *             Thrown if authentication fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws InvalidStatusException
      *             Thrown if Container is in invalid status to lock.
      * @throws InvalidXmlException
      *             Thrown if taskParam has invalid structure.
      */
     @Validate(param = 1, resolver = "getLockTaskParamSchemaLocation")
     String lock(final String id, final String taskParam) throws ContainerNotFoundException, LockingException,
         MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException,
         OptimisticLockingException, InvalidStatusException, InvalidXmlException;
 
     /**
      * UnLock a Container.<br/>
      * <p/>
      * The Container will be unlocked. The lockOwner or an Administrator may unlock the Container.
      * <p/>
      * <b>Prerequisites:</b>
      * <p/>
      * The container must exist<br/>
      * The container is locked.<br/>
      * The public-status is not "withdrawn".
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The lock-status of the container is changed to "unlocked".</li>
      * <li>The lock-date is removed from the container.</li>
      * <li>The lock-owner is removed from the container.</li>
      * <li>No new version is created.</li>
      * <li>A XML containing the new latest version timestamp is returned (in comply to result.xsd).</li>
      * </ul>
      * <b>Parameter for request:</b><br/>
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot; /&gt;
      * </pre>
      * 
      * @param id
      *            The id of the Container to be unlocked.
      * @param taskParam The XML representation of task parameters conforming to lock-task-param.xsd. 
      * Including the timestamp of the last modification of the container (attribute 'last-modification-date', required).
      * The last-modification-date is necessary for optimistical locking purpose. (example above)
      * @return last-modification-date within XML (result.xsd)
      * @throws ContainerNotFoundException
      *             Thrown if the Container was not found.
      * @throws LockingException
      *             Thrown if the Container could not been unlocked.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws AuthenticationException
      *             Thrown if authentication fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws InvalidStatusException
      *             Thrown if Conmtainer is in wrong status to unlock.
      * @throws InvalidXmlException
      *             Thrown if taskParam contains invalid data.
      */
     @Validate(param = 1, resolver = "getLockTaskParamSchemaLocation")
     String unlock(final String id, final String taskParam) throws ContainerNotFoundException, LockingException,
         MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException,
         OptimisticLockingException, InvalidStatusException, InvalidXmlException;
 
     /**
      * Move a container from one Context to an other.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The target context must exist<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The target context is accessed using the provided reference.</li>
      * <li>It is checked whether the input adhere to the allowed values defined in the Admin Descriptor of the provided
      * Context.</li>
      * <li>The container is added to the provided Context.</li>
      * <li>An XML representation of the container is created.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * <p/>
      * <b>Parameter for request:</b> (example)<br/>
      * <p/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;
      * </pre>
      * <p/>
      * 
      * <pre>
      *           context=&quot;&lt;context-id&gt;&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param containerId
      *            The id of the container.
      * @param taskParam
      *            The timestamp of the last modification of the container and the new Context Id.
      * @return The XML representation of the the container corresponding to XML-schema "container.xsd".
      * @throws ContainerNotFoundException
      *             Thrown if a container with the provided id cannot be found.
      * @throws MissingMethodParameterException
      *             Thrown if the container id is not provided.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws AuthenticationException
      *             Thrown if authorization fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws ContextNotFoundException
      *             Thrown if new Context could not be found.
      * @throws InvalidContentException
      *             Thrown if taskParam contains invalid content.
      * @throws LockingException
      *             Thrown if Container is locked.
      */
     String moveToContext(final String containerId, final String taskParam) throws ContainerNotFoundException,
         ContextNotFoundException, InvalidContentException, LockingException, MissingMethodParameterException,
         AuthenticationException, AuthorizationException, SystemException;
 
     /**
      * Creates a new item and add it to the member list of the container with the provided id.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The provided XML data in the body is only accepted if the size is less than ESCIDOC_MAX_XML_SIZE.<br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * Only the lastest version can be used here.<br/>
      * <p/>
      * All "prerequisites" for "create" of an item has to be considered.
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The item is added to the container.</li>
      * <li>A new version is created.</li>
      * <li>The XML representation of the item corresponding to XML-schema is returned as output.</li>
      * </ul>
      * 
      * @param containerId
      *            The id of the container.
      * @param xmlData
      *            The XML representation of the item to be created corresponding to XML-schema "item.xsd".
      * @return The XML representation of the created item corresponding to XML-schema "item.xsd".
      * @throws ContainerNotFoundException
      *             Thrown if the Container with the provided id was not found.
      * @throws MissingContentException
      *             If some mandatory content is missing.
      * @throws ContextNotFoundException
      *             Thrown if the context specified in the provided data cannot be found.
      * @throws ContentModelNotFoundException
      *             Thrown if the content model specified in the provided data cannot be found.
      * @throws ReadonlyElementViolationException
      *             If a read-only element is set.
      * @throws MissingAttributeValueException
      *             It a mandatory attribute value is missing.
      * @throws MissingElementValueException
      *             If a mandatory element value is missing.
      * @throws ReadonlyAttributeViolationException
      *             If a read-only attribute is set.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws MissingMdRecordException
      *             Thrown if required md-record is missing
      * @throws InvalidXmlException
      *             If the provided data is not valid XML.
      * @throws FileNotFoundException
      *             Thrown if a file cannot be found.
      * @throws LockingException
      *             Thrown if the container is locked and the current user is not the one who locked it.
      * @throws InvalidContentException
      *             Thrown if the content is invalid.
      * @throws InvalidContextException
      *             Thrown if a context specified in the provided data differs from a context of the container with the
      *             provided id.
      * @throws InvalidStatusException
      *             Thrown if the container in the current status not allows to create a new Item.
      * @throws ReferencedResourceNotFoundException
      *             Thrown if a content relation target specified in the provided data does not exist in the framework.
      * @throws RelationPredicateNotFoundException
      *             Thrown if a content relation predicate specified in the provided data does not belong to the provided
      *             ontology.
      * @throws AuthenticationException
      *             Thrown if authentication fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      */
     @Validate(param = 1, resolver = "getItemSchemaLocation")
     @Deprecated
     String createItem(final String containerId, final String xmlData) throws ContainerNotFoundException,
         MissingContentException, ContextNotFoundException, ContentModelNotFoundException,
         ReadonlyElementViolationException, MissingAttributeValueException, MissingElementValueException,
         ReadonlyAttributeViolationException, MissingMethodParameterException, InvalidXmlException,
         FileNotFoundException, LockingException, InvalidContentException, InvalidContextException,
         RelationPredicateNotFoundException, ReferencedResourceNotFoundException, SystemException,
         AuthenticationException, AuthorizationException, MissingMdRecordException, InvalidStatusException;
 
     /**
      * Creates a new container and add it to the member list of the container with the provided id.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The provided XML data in the body is only accepted if the size is less than ESCIDOC_MAX_XML_SIZE.<br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * Only the lastest version can be used here.<br/>
      * <p/>
      * All "prerequisites" for "create" of an item has to be considered.
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The new container is added to the container.</li>
      * <li>A new version is created.</li>
      * <li>The XML representation of the new container corresponding to XML-schema is returned as output.</li>
      * </ul>
      * 
      * @param containerId
      *            The id of the container.
      * @param xmlData
      *            The XML representation of the item to be created corresponding to XML-schema "container.xsd".
      * @return The XML representation of the created item corresponding to XML-schema "container.xsd".
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      * @throws ContainerNotFoundException
      *             Thrown if the Container with the provided id was not found.
      * @throws LockingException
      *             Thrown if the container is locked and the current user is not the one who locked it.
      * @throws ContextNotFoundException
      *             Thrown if the context specified in the provided data cannot be found.
      * @throws ContentModelNotFoundException
      *             Thrown if the content model specified in the provided data cannot be found.
      * @throws InvalidContentException
      *             Thrown if the content is invalid.
      * @throws InvalidXmlException
      *             If the provided data is not valid XML.
      * @throws MissingAttributeValueException
      *             It a mandatory attribute value is missing.
      * @throws MissingElementValueException
      *             If a mandatory element value is missing.
      * @throws MissingMdRecordException
      *             Thrown if required md-record is missing
      * @throws InvalidContextException
      *             Thrown if a context specified in the provided data differs from a context of the container with the
      *             provided id.
      * @throws ReferencedResourceNotFoundException
      *             Thrown if a content relation target specified in the provided data does not exist in the framework.
      * @throws RelationPredicateNotFoundException
      *             Thrown if a content relation predicate specified in the provided data does not belong to the provided
      *             ontology.
      * @throws AuthenticationException
      *             Thrown if authentication fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws InvalidStatusException
      *             Thrown if an organizational unit is in an invalid status.
      */
     @Validate(param = 1, resolver = "getContainerSchemaLocation")
     @Deprecated
     String createContainer(final String containerId, final String xmlData) throws MissingMethodParameterException,
         ContainerNotFoundException, LockingException, ContextNotFoundException, ContentModelNotFoundException,
         InvalidContentException, InvalidXmlException, MissingAttributeValueException, MissingElementValueException,
         AuthenticationException, AuthorizationException, InvalidContextException, RelationPredicateNotFoundException,
         InvalidStatusException, ReferencedResourceNotFoundException, MissingMdRecordException, SystemException;
 
     /**
      * Add provided content relations to a container with the provided id.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * Only the lastest version can be used here.<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The new relations, which have the container as a source are created according to a provided data</li>
      * <li>Latest version date is updated.</li>
      * <li>A new version is created.</li>
      * <li>Method checks if content relations with provided data already exist in the system. If there are matched
      * content relations the AlreadyExistException will be thrown.</li>
      * <li>For content relations data that does not match existing relations the framework checks if provided target ids
      * contain a version nummer. In this case an InvalidContentException will be thrown. Then framework checks if
      * resources with a provided ids and provided predicates from provided ontology exist in the system. New relations
      * will be created.</li>
      * <li>A XML containing the new latest version timestamp is returned.</li>
      * </ul>
      * <b>Parameter for request:</b> (example)<br/>
      * <p/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;relation&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;targetId&gt;escidoc:500&lt;/targetId&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;predicate&gt;http://www.escidoc.de/ontologies/mpdl-ontologies/
      * </pre>
      * <p/>
      * 
      * <pre>
      *   content-relations#isAnnotationOf&lt;/predicate&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;/relation&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;relation&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;targetId&gt;escidoc:340&lt;/targetId&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;predicate&gt;http://www.escidoc.de/ontologies/mpdl-ontologies/
      * </pre>
      * <p/>
      * 
      * <pre>
      *   content-relations#isRevisonOf&lt;/predicate&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;/relation&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the container, which will be a source of new relations.
      * @param param
      *            xml structure with a last modification date of the source container and a list of sections containing
      *            respectively a target id and a content relation predicate. A target id may not contain a version
      *            number: (see description above)
      * @return last-modification-date within XML (result.xsd)
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws ContainerNotFoundException
      *             Thrown if the Container with the provided id was not found.
      * @throws OptimisticLockingException
      *             OptimisticLockingException If the provided latest-modification-date does not match.
      * @throws ReferencedResourceNotFoundException
      *             Thrown if a content relation target specified in the provided data does not exist in the framework.
      * @throws RelationPredicateNotFoundException
      *             Thrown if a content relation predicate specified in the provided data does not belong to the provided
      *             ontology.
      * @throws AlreadyExistsException
      *             Thrown if a content relation specified in a provided data already exists
      * @throws InvalidStatusException
      *             Thrown if a container in the current status is not allowed to to add new content relations to.
      * @throws InvalidXmlException
      *             Thrown if the schema validation of the provided data fails or provided data is corrupted.
      * @throws MissingElementValueException
      *             If a mandatory element value is missing.
      * @throws LockingException
      *             Thrown if the container is locked and the current user is not the one who locked it.
      * @throws ReadonlyVersionException
      *             If the provided container id does not identify the latest version.
      * @throws InvalidContentException
      *             Thrown if the provided data for the resource is invalid.
      * @throws AuthenticationException
      *             Thrown if authentication fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      * @throws MissingMethodParameterException
      *             Thrown if one of expected input parameter is missing.
      */
     @Validate(param = 1, resolver = "getRelationTaskParamSchemaLocation")
     String addContentRelations(final String id, final String param) throws SystemException, ContainerNotFoundException,
         OptimisticLockingException, ReferencedResourceNotFoundException, RelationPredicateNotFoundException,
         AlreadyExistsException, InvalidStatusException, InvalidXmlException, MissingElementValueException,
         LockingException, ReadonlyVersionException, InvalidContentException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException;
 
     /**
      * Remove provided content relations to a container with the provided id.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * The public-status is not "withdrawn".<br/>
      * <p/>
      * Only the lastest version can be used here.<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided reference.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The relations to be deleted, which have the container as a source are deleted according to a provided data</li>
      * <li>Latest version date is updated.</li>
      * <li>A new version is created.</li>
      * <li>The method checks if a source container contains all relations with provided data. Otherwise a
      * ContentRelationNotFoundException will be thrown.</li>
      * <li>The relations will be deleted from the source container.</li>
      * <li>No data is returned.</li>
      * </ul>
      * <b>Parameter for request:</b> (example)<br/>
      * <p/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;relation&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;targetId&gt;escidoc:500&lt;/targetId&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;predicate&gt;http://www.escidoc.de/ontologies/mpdl-ontologies/
      * </pre>
      * <p/>
      * 
      * <pre>
      *   content-relations#isAnnotationOf&lt;/predicate&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;/relation&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;relation&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;targetId&gt;escidoc:340&lt;/targetId&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;predicate&gt;http://www.escidoc.de/ontologies/mpdl-ontologies/
      * </pre>
      * <p/>
      * 
      * <pre>
      *   content-relations#isRevisonOf&lt;/predicate&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *  &lt;/relation&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the container, which will be a source of deleted relations.
      * @param param
      *            xml structure with a last modification date of the source container and a list of sections containing
      *            respectively a target id and a content relation predicate. A target id may not contain a version
      *            number: (see description above)
      * @return last-modification-date within XML (result.xsd)
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws ContainerNotFoundException
      *             Thrown if the Container with the provided id was not found.
      * @throws OptimisticLockingException
      *             OptimisticLockingException If the provided latest-modification-date does not match.
      * @throws InvalidStatusException
      *             Thrown if a container in the current status is not allowed to to remove new content relations from.
      * @throws MissingElementValueException
      *             If a mandatory element value is missing.
      * @throws InvalidXmlException
      *             Thrown if the schema validation of the provided data fails or provided data is corrupted.
      * @throws ContentRelationNotFoundException
      *             Thrown if a container with the provided id does not have a content relation specified in the provided
      *             xml data
      * @throws LockingException
      *             Thrown if the container is locked and the current user is not the one who locked it.
      * @throws ReadonlyVersionException
      *             If the provided container id does not identify the latest version.
      * @throws AuthenticationException
      *             Thrown if authentication fails.
      * @throws AuthorizationException
      *             Thrown if authorization fails.
      */
     @Validate(param = 1, resolver = "getRelationTaskParamSchemaLocation")
     String removeContentRelations(final String id, final String param) throws SystemException,
         ContainerNotFoundException, OptimisticLockingException, InvalidStatusException, MissingElementValueException,
         InvalidXmlException, ContentRelationNotFoundException, LockingException, ReadonlyVersionException,
         AuthenticationException, AuthorizationException;
 
     /**
      * Assign a Persistent Identifier (PID) to an Container.<br/>
      * <p/>
      * This PID represents the object reference and schould used to identify the newest accessible Container. <br/>
      * <p/>
      * The used PID Service and the assignment behavior is configured in <b>escidoc-core.properties</b>.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * No object pid is assigned to the Container.<br/>
      * <p/>
      * The Container is not locked.<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Container is accessed using the provided Id.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>Identifier is created and with the provided URL registered in the configured PID System.</li>
      * <li>Persistent Identifier is added to the Container.</li>
      * <li>The assigned Persistent Identifier is returned within an XML structure.</li>
      * </ul>
      * <p/>
      * <b>Parameter for request:</b> (example)<br/>
      * <br/>
      * <p/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;url&gt;http://application.url/some/resource&lt;/url&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * <p/>
      * <b>Responce:</b> (example)<br/>
      * <p/>
      * 
      * <pre>
      * &lt;param&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;pid&gt;hdl:12345/98765&lt;/pid&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * <p/>
      * PID Assignment with extern managed PIDs:
      * <p/>
      * Another alternative is to register an PID which is managed outside of the framework. Extend an Item with an
      * already existing PID is also possible through the assignObjectPid() method. Instead of providing the URL to the
      * resource is an XML element called 'pid' to include. E.g.
      * <p/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;pid&gt;somePid&lt;/pid&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * <p/>
      * The value of the pid element is used within the framework as PID. Be aware that the value of the pid element is
      * not checked!
      * 
      * @param id
      *            The container ID.
      * @param taskParam
      *            The timestamp of the last modification of the container and at minimum the URL which is to register in
      *            the PID resolver. This parameter list is forwarded to the PID resolver and could be extended. (see
      *            example above)
      * @return last-modification-date within XML (result.xsd)
      * @throws InvalidStatusException
      *             Thrown if a container in the current status is not allowed to to assign a new persistent identifier.
      * @throws ContainerNotFoundException
      *             Thrown if the Container with the provided id was not found.
      * @throws LockingException
      *             Thrown if the container is locked and the current user is not the one who locked it.
      * @throws MissingMethodParameterException
      *             Thrown if parameter data contains not the requiered data.
      * @throws OptimisticLockingException
      *             OptimisticLockingException If the provided latest-modification-date does not match.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws InvalidXmlException
      *             Thrown if the taskParam has invalid structure.
      */
     @Validate(param = 1, resolver = "getAssignPidTaskParamSchemaLocation")
     String assignObjectPid(final String id, final String taskParam) throws InvalidStatusException,
         ContainerNotFoundException, LockingException, MissingMethodParameterException, OptimisticLockingException,
         SystemException, InvalidXmlException;
 
     /**
      * Assign a Persistent Identifier (PID) to a version of the Container.<br/>
      * <p/>
      * This PID represents the reference to a specific version.<br/>
      * <p/>
      * The used PID Service and the assignment behavior is configured in <b>escidoc-core.properties</b>.<br/>
      * <p/>
      * <b>Prerequisites:</b><br/>
      * <p/>
      * The container must exist<br/>
      * <p/>
      * The container is not locked.<br/>
      * <p/>
      * No version pid is assigned to the version of the container.<br/>
      * <p/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The container is accessed using the provided Id.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>Identifier is created and with the provided URL registered in the configured PID System.</li>
      * <li>Persistent Identifier is added as version PID to the container.</li>
      * <li>The assigned Persistent Identifier is returned within an XML structure.</li>
      * </ul>
      * <p/>
      * <b>Parameter for request:</b> (example)<br/>
      * <p/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;url&gt;http://application.url/some/resource&lt;/url&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * <p/>
      * <b>Responce:</b> (example)<br/>
      * <p/>
      * 
      * <pre>
      * &lt;param&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;pid&gt;hdl:12345/98765&lt;/pid&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * <p/>
      * PID Assignment with extern managed PIDs:
      * <p/>
      * Another alternative is to register an PID which is managed outside of the framework. Extend an Item with an
      * already existing PID is also possible through the assignObjectPid() method. Instead of providing the URL to the
      * resource is an XML element called 'pid' to include. E.g.
      * <p/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      *   &lt;pid&gt;somePid&lt;/pid&gt;
      * </pre>
      * <p/>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * <p/>
      * The value of the pid element is used within the framework as PID. Be aware that the value of the pid element is
      * not checked!
      * 
      * @param id
      *            The container ID.
      * @param taskParam
      *            The timestamp of the last modification of the container and at minimum the URL which is to register in
      *            the PID resolver. This parameter list is forwarded to the PID resolver and could be extended. (see
      *            example above)
      * @return last-modification-date within XML (result.xsd)
      * @throws ContainerNotFoundException
      *             Thrown if the Container with the provided id was not found.
      * @throws LockingException
      *             Thrown if the container is locked and the current user is not the one who locked it.
      * @throws MissingMethodParameterException
      *             Thrown if parameter data contains not the requiered data.
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      * @throws OptimisticLockingException
      *             OptimisticLockingException If the provided latest-modification-date does not match.
      * @throws InvalidStatusException
      *             Thrown if a container in the current status is not allowed to to assign a new persistent identifier.
      * @throws ReadonlyVersionException
      *             Thrown if a provided container version id is not a latest version.
      * @throws XmlCorruptedException
      *             Thrown if the taskParam has invalid structure.
      */
     @Validate(param = 1, resolver = "getAssignPidTaskParamSchemaLocation")
     String assignVersionPid(final String id, final String taskParam) throws ContainerNotFoundException,
         LockingException, MissingMethodParameterException, SystemException, OptimisticLockingException,
         InvalidStatusException, XmlCorruptedException, ReadonlyVersionException;
 }
