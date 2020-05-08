 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License
  * for the specific language governing permissions and limitations under the License.
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
 
 package de.escidoc.core.oum;
 
 import java.util.Map;
 
 import de.escidoc.core.common.exceptions.EscidocException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OperationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.OrganizationalUnitHasChildrenException;
 import de.escidoc.core.common.exceptions.application.violated.OrganizationalUnitHierarchyViolationException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import org.escidoc.core.domain.ou.OrganizationalUnitTO;
 import org.esidoc.core.utils.io.EscidocBinaryContent;
 import org.esidoc.core.utils.io.MimeTypes;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 
@Path("/organizational-unit")
 @Produces(MimeTypes.TEXT_XML)
 @Consumes(MimeTypes.TEXT_XML)
 public interface OrganizationalUnitRestService {
 
     @PUT
     OrganizationalUnitTO create(OrganizationalUnitTO organizationalUnitTO) throws AuthenticationException,
         AuthorizationException, MissingMethodParameterException, SystemException, MissingAttributeValueException,
         MissingElementValueException, OrganizationalUnitNotFoundException, InvalidStatusException,
         XmlCorruptedException, XmlSchemaValidationException, MissingMdRecordException;
 
     @GET
     @Path("/{id}")
     OrganizationalUnitTO retrieve(@PathParam("id") String id) throws AuthenticationException, AuthorizationException,
         MissingMethodParameterException, OrganizationalUnitNotFoundException, SystemException;
 
     @POST
     @Path("/{id}")
     OrganizationalUnitTO update(@PathParam("id") String id, OrganizationalUnitTO organizationalUnitTO)
         throws AuthenticationException, AuthorizationException, MissingMethodParameterException,
         OrganizationalUnitNotFoundException, SystemException, OptimisticLockingException,
         OrganizationalUnitHierarchyViolationException, InvalidXmlException, MissingElementValueException,
         InvalidStatusException;
 
     @DELETE
     @Path("/{id}")
     void delete(@PathParam("id") String id) throws AuthenticationException, AuthorizationException,
         MissingMethodParameterException, OrganizationalUnitNotFoundException, InvalidStatusException,
         OrganizationalUnitHasChildrenException, SystemException;
 
 //    @POST
 //    @Path("/{id}/md-records")
 //    MdRecordTO updateMdRecords(@PathParam("id") String id, MdRecordsTO mdRecordTO) throws AuthenticationException,
 //        AuthorizationException, InvalidXmlException, InvalidStatusException, MissingMethodParameterException,
 //        OptimisticLockingException, OrganizationalUnitNotFoundException, MissingElementValueException, SystemException;
 //
 //    @POST
 //    @Path("/{id}/parents")
 //    ParentsTO updateParents(@PathParam("id") String id, ParentsTO parentsTO) throws AuthenticationException,
 //        AuthorizationException, InvalidXmlException, MissingMethodParameterException, OptimisticLockingException,
 //        OrganizationalUnitHierarchyViolationException, OrganizationalUnitNotFoundException,
 //        MissingElementValueException, SystemException, InvalidStatusException;
 //
 //    @GET
 //    @Path("/{id}/properties")
 //    OrganizationalUnitPropertiesTO retrieveProperties(@PathParam("id") String id) throws AuthenticationException,
 //        AuthorizationException, MissingMethodParameterException, OrganizationalUnitNotFoundException, SystemException;
 //
 //    @GET
 //    @Path("/{id}/resources/{name}")
 //    EscidocBinaryContent retrieveResource(@PathParam("id") String id, @PathParam("name") String resourceName)
 //        throws OrganizationalUnitNotFoundException, AuthenticationException, AuthorizationException,
 //        MissingMethodParameterException, OperationNotFoundException, SystemException;
 //
 //    @GET
 //    @Path("/{id}/resources")
 //    ResourcesTO retrieveResources(@PathParam("id") String id) throws AuthenticationException, AuthorizationException,
 //        MissingMethodParameterException, OrganizationalUnitNotFoundException, SystemException;
 //
 //    @GET
 //    @Path("/{id}/md-records")
 //    MdRecordsTO retrieveMdRecords(@PathParam("id") String id) throws AuthenticationException, AuthorizationException,
 //        MissingMethodParameterException, OrganizationalUnitNotFoundException, SystemException;
 //
 //    @GET
 //    @Path("/{id}/md-records/md-record/{name}")
 //    MdRecordsTO retrieveMdRecord(@PathParam("id") String id, @PathParam("name") String name)
 //        throws AuthenticationException, AuthorizationException, MdRecordNotFoundException,
 //        MissingMethodParameterException, OrganizationalUnitNotFoundException, SystemException;
 //
 //    @GET
 //    @Path("/{id}/parents")
 //    ParentsTO retrieveParents(@PathParam("id") String id) throws AuthenticationException, AuthorizationException,
 //        MissingMethodParameterException, OrganizationalUnitNotFoundException, SystemException;
 //
 //    @GET
 //    @Path("/{id}/resources/parent-objects")
 //    ParentObjectsTO retrieveParentObjects(@PathParam("id") String id) throws AuthenticationException, AuthorizationException,
 //        MissingMethodParameterException, OrganizationalUnitNotFoundException, SystemException;
 //
 //    @GET
 //    @Path("/{id}/resources/successors")
 //    SuccessorsTO retrieveSuccessors(@PathParam("id") String id) throws AuthenticationException, AuthorizationException,
 //        MissingMethodParameterException, OrganizationalUnitNotFoundException, SystemException;
 //
 //    @GET
 //    @Path("/{id}/resources/child-objects")
 //    ChildObjectsTO retrieveChildObjects(@PathParam("id") String id) throws AuthenticationException, AuthorizationException,
 //        MissingMethodParameterException, OrganizationalUnitNotFoundException, SystemException;
 //
 //    @GET
 //    @Path("/{id}/resources/path-list")
 //    PathListTO retrievePathList(@PathParam("id") String id) throws AuthenticationException, AuthorizationException,
 //        OrganizationalUnitNotFoundException, SystemException, MissingMethodParameterException;
 //
 //    @GET
 //    @Path("/{id}/organizational-units")
 //    OrganizationalUnitRefsTO retrieveOrganizationalUnits(final Map<String, String[]> filter) throws MissingMethodParameterException,
 //        SystemException, InvalidSearchQueryException, InvalidXmlException;
 //
 //    @POST
 //    @Path("/{id}/close")
 //    ResultTO close(@PathParam("id") String id, CloseTaskParamTO closeParam) throws AuthenticationException,
 //        AuthorizationException, MissingMethodParameterException, OrganizationalUnitNotFoundException,
 //        InvalidStatusException, SystemException, OptimisticLockingException, InvalidXmlException;
 //
 //    @POST
 //    @Path("/{id}/open")
 //    ResultTO open(@PathParam("id") String id, OpenTaskParamTO openParams) throws AuthenticationException,
 //        AuthorizationException, MissingMethodParameterException, OrganizationalUnitNotFoundException,
 //        InvalidStatusException, SystemException, OptimisticLockingException, InvalidXmlException;
 }
