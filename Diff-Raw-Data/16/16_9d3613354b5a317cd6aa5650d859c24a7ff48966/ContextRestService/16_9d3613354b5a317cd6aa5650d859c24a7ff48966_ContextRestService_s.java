 /**
  * 
  */
 package de.escidoc.core.context;
 
 import java.util.Map;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 
 import org.escidoc.core.domain.context.AdminDescriptorTO;
 import org.escidoc.core.domain.context.AdminDescriptorsTO;
 import org.escidoc.core.domain.context.ContextPropertiesTO;
 import org.escidoc.core.domain.context.ContextTO;
 import org.esidoc.core.utils.io.EscidocBinaryContent;
 import org.esidoc.core.utils.io.MimeTypes;
 
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
 
 /**
  * @author Marko Voß
  * 
  */
 
@Path("/context")
 @Produces(MimeTypes.TEXT_XML)
 @Consumes(MimeTypes.TEXT_XML)
 public interface ContextRestService {
 
     @PUT
     ContextTO create(ContextTO contextTO) throws MissingMethodParameterException, ContextNameNotUniqueException,
         AuthenticationException, AuthorizationException, SystemException, ContentModelNotFoundException,
         ReadonlyElementViolationException, MissingAttributeValueException, MissingElementValueException,
         ReadonlyAttributeViolationException, InvalidContentException, OrganizationalUnitNotFoundException,
         InvalidStatusException, XmlCorruptedException, XmlSchemaValidationException;
 
     @GET
     @Path("/{id}")
     ContextTO retrieve(@PathParam("id") String id) throws ContextNotFoundException, MissingMethodParameterException,
         AuthenticationException, AuthorizationException, SystemException;
 
     @PUT
     @Path("/{id}")
     ContextTO update(@PathParam("id") String id, ContextTO contextTO) throws ContextNotFoundException,
         MissingMethodParameterException, InvalidContentException, InvalidStatusException, AuthenticationException,
         AuthorizationException, ReadonlyElementViolationException, ReadonlyAttributeViolationException,
         OptimisticLockingException, ContextNameNotUniqueException, InvalidXmlException, MissingElementValueException,
         SystemException;
 
     @DELETE
     @Path("/{id}")
     void delete(@PathParam("id") String id) throws ContextNotFoundException, ContextNotEmptyException,
         MissingMethodParameterException, InvalidStatusException, AuthenticationException, AuthorizationException,
         SystemException;
 
     @GET
     @Path("/{id}/properties")
     ContextPropertiesTO retrieveProperties(@PathParam("id") String id) throws ContextNotFoundException, SystemException;
 
     /**
      * FIXME Map
      */
     @GET
     @Path("/{id}/resources/{resourceName}")
     EscidocBinaryContent retrieveResource(
         @PathParam("id") String id, @PathParam("resourceName") String resourceName,
         final Map<String, String[]> parameters) throws OperationNotFoundException, ContextNotFoundException,
         MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException;
 
     @GET
     @Path("/{id}/resources")
     String retrieveResources(@PathParam("id") String id) throws ContextNotFoundException,
         MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException;
 
     /**
      * FIXME Map
      */
     @GET
     String retrieveContexts(final Map<String, String[]> filter) throws MissingMethodParameterException, SystemException;
 
     /**
      * FIXME Map
      */
     @GET
     @Path("/{id}/resources/members")
     String retrieveMembers(@PathParam("id") String id, final Map<String, String[]> filter)
         throws ContextNotFoundException, MissingMethodParameterException, SystemException;
 
     @GET
     @Path("/{id}/admin-descriptor/{name}")
     AdminDescriptorTO retrieveAdminDescriptor(@PathParam("id") String id, @PathParam("name") String name)
         throws ContextNotFoundException, MissingMethodParameterException, AuthenticationException,
         AuthorizationException, SystemException, AdminDescriptorNotFoundException;
 
     @GET
     @Path("/{id}/admin-descriptor")
     AdminDescriptorsTO retrieveAdminDescriptors(@PathParam("id") String id) throws ContextNotFoundException,
         MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException;
 
     @POST
     @Path("/{id}/admin-descriptor/")
     AdminDescriptorTO updateAdminDescriptor(@PathParam("id") String id, AdminDescriptorTO adminDescriptorTO)
         throws ContextNotFoundException, MissingMethodParameterException, AuthenticationException,
         AuthorizationException, SystemException, OptimisticLockingException, AdminDescriptorNotFoundException,
         InvalidXmlException;
 
     /**
      * FIXME taskParam
      */
     @POST
     @Path("/{id}/open")
     String open(@PathParam("id") String id, final String taskParam) throws ContextNotFoundException,
         MissingMethodParameterException, InvalidStatusException, AuthenticationException, AuthorizationException,
         OptimisticLockingException, InvalidXmlException, SystemException, LockingException, StreamNotFoundException;
 
     /**
      * FIXME taskParam
      */
     @POST
     @Path("/{id}/close")
     String close(@PathParam("id") String id, final String taskParam) throws ContextNotFoundException,
         MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException,
         OptimisticLockingException, InvalidXmlException, InvalidStatusException, LockingException,
         StreamNotFoundException;
 }
