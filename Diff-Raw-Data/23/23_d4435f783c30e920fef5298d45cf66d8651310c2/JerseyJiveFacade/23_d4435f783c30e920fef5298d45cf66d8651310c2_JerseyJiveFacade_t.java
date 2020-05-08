 package org.mule.modules.jive;
 
 import org.mule.modules.jive.api.EntityType;
 import org.mule.modules.jive.api.Operation;
 import org.mule.modules.jive.api.xml.XmlMapper;
 import org.mule.modules.jive.utils.ServiceUriFactory;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.WebResource.Builder;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.Map;
 
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 
 public class JerseyJiveFacade implements JiveFacade
 {
     /**The jersey webresource to access rest resources.*/
     private WebResource gateway;
     /**The userID.*/
     private Long userID;
     private String username;
     /**The password.*/
     private String password;
     private String gatewayUri;
     private final XmlMapper mapper = new XmlMapper();
 
     /**Sets the userID requesting it by username.*/
     private void setUserIdByUsername() 
     {
         final String response = this.gateway.path("/userService/users/"
             + this.getUser()).type(MediaType.APPLICATION_FORM_URLENCODED)
             .get(String.class);
         this.setUserID(Long.parseLong(
             StringUtils.substringBetween(response, "<ID>", "</ID>")));
     }
     
     @Override
     public final Map<String, Object> get(final EntityType entityType, final String id)
     {
        return entityType.get(id, mapper, gateway);
     }
     
     @Override
     /**{@inheritDoc}*/
     public final Map<String, Object> execute(final CustomOp customType,
         final Map<String, Object> entity) 
         {
         final Writer writer = new StringWriter();
         final String response;
 
         final Builder partialRequest = this.gateway.path(ServiceUriFactory.
                 generateCustomUri(customType))
             .type(MediaType.APPLICATION_FORM_URLENCODED)
             .header("content-type", "text/xml");
 
         if (customType.getMethod().equals("POST")) 
         {
             map2xml(customType.getRootTagElementName(), entity, writer);
             response = partialRequest.post(String.class, writer.toString());
         } 
         else if (customType.getMethod().equals("GET")) 
         {
             response = partialRequest.get(String.class);
         } 
         else if (customType.getMethod().equals("PUT")) 
         {
             response = partialRequest.put(String.class);
         } 
         else 
         { //It's a DELETE request
             response = partialRequest.delete(String.class);
         }
         return xml2map(new StringReader(response));
     }
 
 
     @Override
     /**{@inheritDoc}*/
     public final Map<String, Object> execute(final String uri,
         final String id) 
     {
 
         throw new NotImplementedException();
     }
     
     public final Map<String, Object> execute(final Operation op,
                                              final Map<String, Object> entity) 
     {
         final String response;
         final Builder partialRequest = this.gateway.path(op.getResourceUri())
             .type(MediaType.APPLICATION_FORM_URLENCODED)
             .header("content-type", "text/xml");
         final Writer writer = new StringWriter();
         map2xml(op.getRootTagElementName(), entity, writer);
         if (op.getProtocol().equals("POST")) 
         {
             response = partialRequest.post(String.class, writer.toString());
         } 
         else if (op.getProtocol().equals("PUT")) 
         {
             response = partialRequest.put(String.class);
         } 
         else 
         {
             response = "";
         }
         return xml2map(new StringReader(response));
     }
     
     public final Map<String, Object> execute(final Operation op,
                                              final String id) 
     {
         final String response;
         
         final StringBuilder opUri = new StringBuilder(op.getResourceUri());
         for (final String part : StringUtils.split(id, ':')) 
         {
             opUri.append("/" + part);
         }
 
         final Builder partialRequest = this.gateway.path(opUri.toString())
             .type(MediaType.APPLICATION_FORM_URLENCODED)
             .header("content-type", "text/xml");
 
         if (op.getProtocol().equals("GET")) 
         {
             response = partialRequest.get(String.class);
         }
         else 
         {
             response = "";
         }
         return xml2map(new StringReader(response));
     }
 
     @Override
     public final Map<String, Object> create(final EntityType type,
                                     final Map<String, Object> entity) 
     {
         Validate.notNull(type);
         Validate.notNull(entity);
         return type.create(type, entity, mapper, gateway);
     }
     
     
 
     /**
      * @param xmlRootTag
      * @param entity
      * @param writer
      * @see org.mule.modules.jive.api.xml.XmlMapper#map2xml(java.lang.String, java.util.Map, java.io.Writer)
      */
     public final void map2xml(final String xmlRootTag, final Map<String, Object> entity, final Writer writer)
     {
         mapper.map2xml(xmlRootTag, entity, writer);
     }
 
     /**
      * @param reader
      * @return
      * @see org.mule.modules.jive.api.xml.XmlMapper#xml2map(java.io.Reader)
      */
     public final Map<String, Object> xml2map(final Reader reader)
     {
         return mapper.xml2map(reader);
     }
 
     @Override
     /**{@inheritDoc}*/
     public final Map<String, Object> delete(final EntityType type,
         final String id) 
     {
         Validate.notNull(type);
         Validate.notEmpty(id);
         return type.delete(type, id, mapper, gateway);
     }
     
     /* (non-Javadoc)
      * @see org.mule.modules.jive.JiveFacade#getAll(org.mule.modules.jive.api.EntityType, java.lang.String)
      */
     @Override
     public Map<String, Object> getAll(EntityType entityType, String id)
     {
         //TODO add validations
         return entityType.getAll(entityType, id, mapper, gateway);
     }
     
     @Override
     public Map<String, Object> update(EntityType entityType, Map<String, Object> entity)
     {
         Validate.notNull(entity);
         Validate.notNull(entity);
         return entityType.put(entityType, entity, mapper, gateway);
     }
 
     /**Call the get count service.
      * @return The count as a {@link Long}
      * @param type The service type
      * */
     public final Map<String, Object> count(final EntityType type, final String id) 
     {
         Validate.notNull(type);
         return type.count(type, id, mapper, gateway);
 //        String response = this.gateway.path(ServiceUriFactory.generateBaseUri(type))
 //            .get(String.class);
 //        return Long.parseLong(StringUtils.substringBetween(
 //            response, "<return>", "</return>"));
     }
 
   
 
     /**Creates the client.
      * @return The jersey client
      * */
     private Client createClient() 
     {
         final ClientConfig config = new DefaultClientConfig();
         return Client.create(config);
     }
 
     /**Creates the webresource.
      * @param gatewayUri The resource uri
      * @param client The jersey client
      * */
     private void createGateway(final String gatewayUri, final Client client) 
     {
         Validate.notNull(client, "Client cannot be empty");
         Validate.notEmpty(gatewayUri, "Gateway cannot be empty");
         client.addFilter(new HTTPBasicAuthFilter(this.getUser(), this.getPassword()));
         this.gateway = client.resource(gatewayUri);
     }
 
     /**
      * @param userID the userID to set
      */
     public final void setUserID(final Long userID)
     {
         this.userID = userID;
     }
 
     /**
      * @return the userID
      */
     public final Long getUserID()
     {
         return userID;
     }
 
     /**
      * @param user the user to set
      */
     public final void setUser(final String user)
     {
         this.username = user;
     }
 
     /**
      * @return the user
      */
     public final String getUser()
     {
         return username;
     }
 
     /**
      * @param pass the pass to set
      */
     public final void setPass(final String pass)
     {
         this.password = pass;
     }
 
     /**
      * @return the pass
      */
     public final String getPassword()
     {
         return password;
     }
     
     @Override
     public final void setGatewayUri(final String gatewayUri) 
     {
         this.gatewayUri  = gatewayUri;
     }
 
     @Override
     public final void init() 
     {
         this.createGateway(gatewayUri, createClient());
         setUserIdByUsername();
     }
 
     @Override
     public final void setUsername(final String user) 
     {
         this.username = user;
     }
 
     @Override
     public final void setPassword(final String pass) 
     {
         this.password = pass;
     }
 
 
 }
