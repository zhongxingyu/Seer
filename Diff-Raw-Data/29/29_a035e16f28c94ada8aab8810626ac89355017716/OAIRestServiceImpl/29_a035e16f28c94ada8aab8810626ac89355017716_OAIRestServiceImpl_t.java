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
 package org.escidoc.core.oai.internal;
 
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
 import de.escidoc.core.common.exceptions.application.missing.MissingLicenceException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ContainerNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContextNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.FileNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.XmlSchemaNotFoundException;
 import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyDeletedException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyExistsException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyPublishedException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyWithdrawnException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.NotPublishedException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.OrganizationalUnitHasChildrenException;
 import de.escidoc.core.common.exceptions.application.violated.OrganizationalUnitHierarchyViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyAttributeViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyElementViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyVersionException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyViolationException;
 import de.escidoc.core.common.exceptions.application.violated.UniqueConstraintViolationException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.oai.service.interfaces.SetDefinitionHandlerInterface;
 import org.escidoc.core.domain.service.ServiceUtility;
 import org.escidoc.core.utils.io.Stream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 
 import org.escidoc.core.oai.OAIRestService;
 import org.escidoc.core.domain.container.ContainerTO;
 import org.escidoc.core.domain.oai.SetDefinitionTO;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.rmi.RemoteException;
 import java.util.Map;
 
 /**
  * REST Service Implementation for OAI Set Definition Service.
  * 
  * @author SWA
  * 
  */
 @Service
 public class OAIRestServiceImpl implements OAIRestService {
 
     private final static Logger LOG = LoggerFactory.getLogger(OAIRestServiceImpl.class);
 
     @Autowired
    @Qualifier("service.SetDefinitionHandler")
     private SetDefinitionHandlerInterface oaiHandler;
 
     private JAXBContext jaxbContext;
 
     protected OAIRestServiceImpl() {
         try {
             this.jaxbContext = JAXBContext.newInstance(SetDefinitionTO.class);
         }
         catch (JAXBException e) {
             LOG.error("Error on initialising JAXB context.", e);
         }
     }
 
     public SetDefinitionTO create(final SetDefinitionTO setDefinitionTO) throws UniqueConstraintViolationException,
         InvalidXmlException, MissingMethodParameterException, SystemException, AuthenticationException,
         AuthorizationException {
 
         return ServiceUtility.fromXML(SetDefinitionTO.class,
             this.oaiHandler.create(ServiceUtility.toXML(setDefinitionTO)));
     }
 
     public SetDefinitionTO retrieve(final String id) throws ResourceNotFoundException, MissingMethodParameterException,
         SystemException, AuthenticationException, AuthorizationException {
 
         return ServiceUtility.fromXML(SetDefinitionTO.class, this.oaiHandler.retrieve(id));
     }
 
     public SetDefinitionTO update(final String id, final SetDefinitionTO setDefinitionTO)
         throws ResourceNotFoundException, OptimisticLockingException, MissingMethodParameterException, SystemException,
         AuthenticationException, AuthorizationException {
 
         return ServiceUtility.fromXML(SetDefinitionTO.class,
             this.oaiHandler.update(id, ServiceUtility.toXML(setDefinitionTO)));
     }
 
     public void delete(final String id) throws ResourceNotFoundException, MissingMethodParameterException,
         SystemException, AuthenticationException, AuthorizationException {
 
         this.oaiHandler.delete(id);
     }
 
     // FIXME
     // SetDefinitionListTO retrieveSetDefinitions(final Map<String, String[]> filter) throws AuthenticationException,
     // AuthorizationException, MissingMethodParameterException, InvalidSearchQueryException, SystemException {
     //
     // return ServiceUtility.fromXML(SetDefinitionTO.class,
     // this.oaiHandler.create(ServiceUtility.toXML(setDefinitionTO)));
     // }
 }
