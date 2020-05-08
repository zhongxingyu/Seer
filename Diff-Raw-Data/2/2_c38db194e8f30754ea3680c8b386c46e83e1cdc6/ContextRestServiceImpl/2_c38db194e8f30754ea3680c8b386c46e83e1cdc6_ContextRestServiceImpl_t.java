 /**
  * 
  */
 package de.escidoc.core.context.internal;
 
 import java.util.Map;
 
 import org.escidoc.core.domain.context.AdminDescriptorTO;
 import org.escidoc.core.domain.context.AdminDescriptorsTO;
 import org.escidoc.core.domain.context.ContextPropertiesTO;
 import org.escidoc.core.domain.context.ContextTO;
 import org.escidoc.core.service.ServiceUtility;
 import org.esidoc.core.utils.io.EscidocBinaryContent;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 
 import de.escidoc.core.common.exceptions.application.invalid.ContextNotEmptyException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.AdminDescriptorNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContextNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OperationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.application.violated.ContextNameNotUniqueException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyAttributeViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyElementViolationException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.context.ContextRestService;
 import de.escidoc.core.om.service.interfaces.ContextHandlerInterface;
 
 /**
  * @author Marko Voß
  * 
  */
 public class ContextRestServiceImpl implements ContextRestService {
 
     @Autowired
    @Qualifier("service.ContextHandler")
     private ContextHandlerInterface contextHandler;
 
     /**
      * 
      */
     protected ContextRestServiceImpl() {
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.context.ContextRestService#create(org.escidoc.core.domain.context.ContextTO)
      */
     @Override
     public ContextTO create(final ContextTO contextTO) throws MissingMethodParameterException,
         ContextNameNotUniqueException, AuthenticationException, AuthorizationException, SystemException,
         ContentModelNotFoundException, ReadonlyElementViolationException, MissingAttributeValueException,
         MissingElementValueException, ReadonlyAttributeViolationException, InvalidContentException,
         OrganizationalUnitNotFoundException, InvalidStatusException, XmlCorruptedException,
         XmlSchemaValidationException {
 
         return ServiceUtility.fromXML(ContextTO.class, this.contextHandler.create(ServiceUtility.toXML(contextTO)));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.context.ContextRestService#retrieve(java.lang.String)
      */
     @Override
     public ContextTO retrieve(final String id) throws ContextNotFoundException, MissingMethodParameterException,
         AuthenticationException, AuthorizationException, SystemException {
 
         return ServiceUtility.fromXML(ContextTO.class, this.contextHandler.retrieve(id));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.context.ContextRestService#update(java.lang.String,
      * org.escidoc.core.domain.context.ContextTO)
      */
     @Override
     public ContextTO update(final String id, final ContextTO contextTO) throws ContextNotFoundException,
         MissingMethodParameterException, InvalidContentException, InvalidStatusException, AuthenticationException,
         AuthorizationException, ReadonlyElementViolationException, ReadonlyAttributeViolationException,
         OptimisticLockingException, ContextNameNotUniqueException, InvalidXmlException, MissingElementValueException,
         SystemException {
 
         return ServiceUtility.fromXML(ContextTO.class, this.contextHandler.update(id, ServiceUtility.toXML(contextTO)));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.context.ContextRestService#delete(java.lang.String)
      */
     @Override
     public void delete(final String id) throws ContextNotFoundException, ContextNotEmptyException,
         MissingMethodParameterException, InvalidStatusException, AuthenticationException, AuthorizationException,
         SystemException {
         this.contextHandler.delete(id);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.context.ContextRestService#retrieveProperties(java.lang.String)
      */
     @Override
     public ContextPropertiesTO retrieveProperties(final String id) throws ContextNotFoundException, SystemException {
         return ServiceUtility.fromXML(ContextPropertiesTO.class, this.contextHandler.retrieveProperties(id));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.context.ContextRestService#retrieveResource(java.lang.String, java.lang.String,
      * java.util.Map)
      */
     @Override
     public EscidocBinaryContent retrieveResource(
         final String id, final String resourceName, final Map<String, String[]> parameters)
         throws OperationNotFoundException, ContextNotFoundException, MissingMethodParameterException,
         AuthenticationException, AuthorizationException, SystemException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.context.ContextRestService#retrieveResources(java.lang.String)
      */
     @Override
     public String retrieveResources(final String id) throws ContextNotFoundException, MissingMethodParameterException,
         AuthenticationException, AuthorizationException, SystemException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.context.ContextRestService#retrieveContexts(java.util.Map)
      */
     @Override
     public String retrieveContexts(final Map<String, String[]> filter) throws MissingMethodParameterException,
         SystemException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.context.ContextRestService#retrieveMembers(java.lang.String, java.util.Map)
      */
     @Override
     public String retrieveMembers(final String id, final Map<String, String[]> filter) throws ContextNotFoundException,
         MissingMethodParameterException, SystemException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.context.ContextRestService#retrieveAdminDescriptor(java.lang.String, java.lang.String)
      */
     @Override
     public AdminDescriptorTO retrieveAdminDescriptor(final String id, final String name)
         throws ContextNotFoundException, MissingMethodParameterException, AuthenticationException,
         AuthorizationException, SystemException, AdminDescriptorNotFoundException {
 
         return ServiceUtility.fromXML(AdminDescriptorTO.class, this.contextHandler.retrieveAdminDescriptor(id, name));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.context.ContextRestService#retrieveAdminDescriptors(java.lang.String)
      */
     @Override
     public AdminDescriptorsTO retrieveAdminDescriptors(final String id) throws ContextNotFoundException,
         MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException {
 
         return ServiceUtility.fromXML(AdminDescriptorsTO.class, this.contextHandler.retrieveAdminDescriptors(id));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.context.ContextRestService#updateAdminDescriptor(java.lang.String,
      * org.escidoc.core.domain.context.AdminDescriptorTO)
      */
     @Override
     public AdminDescriptorTO updateAdminDescriptor(final String id, final AdminDescriptorTO adminDescriptorTO)
         throws ContextNotFoundException, MissingMethodParameterException, AuthenticationException,
         AuthorizationException, SystemException, OptimisticLockingException, AdminDescriptorNotFoundException,
         InvalidXmlException {
 
         return ServiceUtility.fromXML(AdminDescriptorTO.class,
             this.contextHandler.updateAdminDescriptor(id, ServiceUtility.toXML(adminDescriptorTO)));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.context.ContextRestService#open(java.lang.String, java.lang.String)
      */
     @Override
     public String open(final String id, final String taskParam) throws ContextNotFoundException,
         MissingMethodParameterException, InvalidStatusException, AuthenticationException, AuthorizationException,
         OptimisticLockingException, InvalidXmlException, SystemException, LockingException, StreamNotFoundException {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.context.ContextRestService#close(java.lang.String, java.lang.String)
      */
     @Override
     public String close(final String id, final String taskParam) throws ContextNotFoundException,
         MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException,
         OptimisticLockingException, InvalidXmlException, InvalidStatusException, LockingException,
         StreamNotFoundException {
         // TODO Auto-generated method stub
         return null;
     }
 
 }
