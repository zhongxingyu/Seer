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
 
 package de.escidoc.core.content.relation;
 
 
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import org.escidoc.core.domain.content.relation.ContentRelationTO;
 import org.esidoc.core.utils.io.MimeTypes;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 
@Path("/content-relation")
 @Produces(MimeTypes.TEXT_XML)
 @Consumes(MimeTypes.TEXT_XML)
 public interface ContentRelationRestService {
 
     @PUT
     ContentRelationTO create(ContentRelationTO contentRelationTO)
             throws SystemException, InvalidContentException, MissingAttributeValueException,
             RelationPredicateNotFoundException, InvalidXmlException, ReferencedResourceNotFoundException,
             MissingMethodParameterException, AuthorizationException, AuthenticationException;
 
     @GET
     @Path("/{id}")
     ContentRelationTO retrieve(@PathParam("id") String id)
             throws SystemException, ContentRelationNotFoundException, AuthorizationException, AuthenticationException;
 
     @POST
     @Path("/{id}")
     ContentRelationTO update(@PathParam("id") String id, ContentRelationTO contentRelationTO)
             throws SystemException, InvalidContentException, OptimisticLockingException, MissingAttributeValueException,
             RelationPredicateNotFoundException, InvalidStatusException, ContentRelationNotFoundException,
             InvalidXmlException, ReferencedResourceNotFoundException, LockingException, MissingMethodParameterException,
             AuthorizationException, AuthenticationException;
 
     @DELETE
     @Path("/{id}")
     void delete(@PathParam("id") String id)
             throws SystemException, ContentRelationNotFoundException, LockingException, AuthorizationException,
             AuthenticationException;
 
 }
