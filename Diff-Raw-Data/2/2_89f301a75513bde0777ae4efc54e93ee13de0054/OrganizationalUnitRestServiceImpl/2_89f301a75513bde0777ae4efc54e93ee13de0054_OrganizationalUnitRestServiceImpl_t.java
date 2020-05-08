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
package de.escidoc.core.oum.internal;
 
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
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
 import de.escidoc.core.oum.OrganizationalUnitRestService;
 import de.escidoc.core.oum.service.interfaces.OrganizationalUnitHandlerInterface;
 import org.escidoc.core.domain.ou.OrganizationalUnitTO;
 import org.escidoc.core.service.ServiceUtility;
 import org.esidoc.core.utils.io.Stream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 
 /**
  * REST Service Implementation for Organizational Unit.
  * 
  * @author SWA
  * 
  */
 @Service
 public class OrganizationalUnitRestServiceImpl implements OrganizationalUnitRestService {
 
     private final static Logger LOG = LoggerFactory.getLogger(OrganizationalUnitRestServiceImpl.class);
 
     @Autowired
     @Qualifier("service.OrganizationalUnitHandler")
     private OrganizationalUnitHandlerInterface organizationalUnitHandler;
 
     private JAXBContext jaxbContext;
 
     protected OrganizationalUnitRestServiceImpl() {
         try {
             this.jaxbContext = JAXBContext.newInstance(OrganizationalUnitTO.class);
         }
         catch (JAXBException e) {
             LOG.error("Error on initialising JAXB context.", e);
         }
     }
 
     @Override
     public OrganizationalUnitTO create(final OrganizationalUnitTO organizationalUnitTO) throws AuthenticationException,
         AuthorizationException, MissingMethodParameterException, SystemException, MissingAttributeValueException,
         MissingElementValueException, OrganizationalUnitNotFoundException, InvalidStatusException,
         XmlCorruptedException, XmlSchemaValidationException, MissingMdRecordException {
 
         return ServiceUtility.fromXML(OrganizationalUnitTO.class,
             this.organizationalUnitHandler.create((ServiceUtility.toXML(organizationalUnitTO))));
 
     }
 
     @Override
     public OrganizationalUnitTO retrieve(final String id) throws AuthenticationException, AuthorizationException,
         MissingMethodParameterException, OrganizationalUnitNotFoundException, SystemException {
 
         return ServiceUtility.fromXML(OrganizationalUnitTO.class, this.organizationalUnitHandler.retrieve(id));
     }
 
     @Override
     public OrganizationalUnitTO update(final String id, final OrganizationalUnitTO organizationalUnitTO)
         throws AuthenticationException, AuthorizationException, MissingMethodParameterException,
         OrganizationalUnitNotFoundException, SystemException, OptimisticLockingException,
         OrganizationalUnitHierarchyViolationException, InvalidXmlException, MissingElementValueException,
         InvalidStatusException {
 
         return ServiceUtility.fromXML(OrganizationalUnitTO.class, this.organizationalUnitHandler.update(id, ServiceUtility.toXML(organizationalUnitTO)));
     }
 
     @Override
     public void delete(final String id) throws AuthenticationException, AuthorizationException,
         MissingMethodParameterException, OrganizationalUnitNotFoundException, InvalidStatusException,
         OrganizationalUnitHasChildrenException, SystemException {
 
         this.organizationalUnitHandler.delete(id);
     }
 
 }
