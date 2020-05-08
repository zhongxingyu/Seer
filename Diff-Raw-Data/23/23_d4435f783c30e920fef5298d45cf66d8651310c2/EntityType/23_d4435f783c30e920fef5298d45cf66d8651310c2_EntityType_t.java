 /*
  * $Id$
  * ---------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules.jive.api;
 
 import static org.mule.modules.jive.api.EntityTypeBuilder.from;
 
 import org.mule.modules.jive.api.xml.XmlMapper;
 import org.mule.modules.jive.utils.ServiceUriFactory;
 
 import com.sun.jersey.api.client.WebResource;
 
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 /** Services enum. */
 public final class EntityType
 {
 
     /** Addressbook service. */
     public static final EntityType ADDRESSBOOK = from("ADDRESSBOOK").withServiceUri("addressBookService").build();
     /** Audit service. */
     public static final EntityType AUDIT = from("AUDIT").build();
     /** Avatar service. */
     public static final EntityType AVATAR = from("AVATAR").build();
     /** Blog service. */
     public static final EntityType BLOG = from("BLOG").build();
     /** Task service. */
     public static final EntityType TASK = from("TASK").build();
     /** Comment service. */
     public static final EntityType COMMENT = from("COMMENT").build();
     /** Community service. */
     public static final EntityType COMMUNITY = from("COMMUNITY").build();
     /** Document service. */
     public static final EntityType DOCUMENT = from("DOCUMENT").build();
     /** Entitlement service. */
     public static final EntityType ENTITLEMENT = from("ENTITLEMENT").build();
     /** Forum service. */
     public static final EntityType FORUM = from("FORUM").build();
     /** Group service. */
     public static final EntityType GROUP = from("GROUP").build();
     /** Intant Messages service. */
     public static final EntityType IMSERVICE = from("IMSERVICE").build();
     /** Plugin service. */
     public static final EntityType PLUGIN = from("PLUGIN").build();
     /** Poll service. */
     public static final EntityType POLL = from("POLL").build();
     /** Private message service. */
     public static final EntityType PRIVATE_MESSAGE = from("PRIVATE_MESSAGE").build();
     /** Profile field service. */
     public static final EntityType PROFILE_FIELD = from("PROFILE_FIELD").build();
     /** Profile search service. */
     public static final EntityType PROFILE_SEARCH = from("PROFILE_SEARCH").build();
     /** Profile service. */
     public static final EntityType PROFILE = from("PROFILE").build();
     /** Project service. */
     public static final EntityType PROJECT = from("PROJECT").build();
     /** Ratings service. */
     public static final EntityType RATINGS = from("RATINGS").build();
     /** Reference service. */
     public static final EntityType REFERENCE = from("REFERENCE").build();
     /** Search service. */
     public static final EntityType SEARCH = from("SEARCH").build();
     /** Social group service. */
     public static final EntityType SOCIAL_GROUP = from("SOCIAL_GROUP").build();
     /** Status level service. */
     public static final EntityType STATUS_LEVEL = from("STATUS_LEVEL").build();
     /** System properties service. */
     public static final EntityType SYSTEM_PROPERTIES = from("SYSTEM_PROPERTIES").build();
     /** Tags service. */
     public static final EntityType TAG = from("TAG").build();
     /** User service. */
     public static final EntityType USER = from("USER").build();
     /** Video service. */
     public static final EntityType VIDEO = from("VIDEO").build();
     /** Watch service. */
     public static final EntityType WATCH = from("WATCH").build();
     
     private String serviceUri;
     private ReferenceOperation deleteOperation;
     private PayloadOperation createOperation;
     private PayloadOperation putOperation;
     private String entityTypeName;
     private ReferenceOperation getAllOperation;
     private ReferenceOperation getOperation;
     private ReferenceOperation countOperation;
 
     /**
      * @param entityType
      * @param serviceNameException
      * @param createOp
      * @param deleteOp
      * @param getOp
      * @param getAllOp
      * @param putOp
      * @param countOp
      */
     public EntityType(String entityType,
                       String serviceNameException,
                       PayloadOperation createOp,
                       ReferenceOperation deleteOp,
                       ReferenceOperation getOp,
                       ReferenceOperation getAllOp,
                       PayloadOperation putOp,
                       ReferenceOperation countOp)
     {
         this.entityTypeName = entityType;
         this.serviceUri = serviceNameException;
         this.createOperation = createOp;
         this.deleteOperation = deleteOp;
         this.getOperation = getOp;
         this.getAllOperation = getAllOp;
         this.putOperation = putOp;
         this.countOperation = countOp;
     }
 
     /**
      * @return the service name retrieved from the enum name.
      */
     private String getServiceName()
     {
 
         return this.entityTypeName.toLowerCase() + "Service";
     }
 
     /**
      * @return the rootElement of the xml request
      */
     public String getXmlRootElementName()
     {
         return StringUtils.capitalize(this.entityTypeName.toLowerCase());
     }
 
     /**
      * @return the serviceUri
      */
     public String getServiceUri()
     {
         if (serviceUri == EntityTypes.DEFAULT_SERVICE_URI)
         {
             return "/" + getServiceName();
         }
         return "/" + serviceUri;
     }
 
     /**
      * Generates the complete uri for the get or delete service.
      * 
      * @param this The {@link EntityType} that is being executed
      * @param id A {@link String} containing the path parameters to add
      * @return The resouce uri with the path parameters added
      */
     public String getCompleteUri(final String id)
     {
         final StringBuffer completeUri = new StringBuffer();
         final String[] pathParams = StringUtils.split(id, ':');
 
         completeUri.append(ServiceUriFactory.generateBaseUri(this));
         for (int i = 0; i < pathParams.length; i++)
         {
             completeUri.append("/" + pathParams[i]);
         }
         return completeUri.toString();
     }
 
     /**
      * @param type
      * @param entity
      * @param mapper 
      * @param resource 
      * @return
      */
     public Map<String, Object> create(final EntityType type, final Map<String, Object> entity,
         final XmlMapper mapper, final WebResource resource)
     {
         return createOperation.execute(resource, mapper, type, entity);
     }
     
     /**
      * @param type
      * @param id
      * @param mapper
      * @param resource
      * @return
      */
     public Map<String, Object> delete(final EntityType type, final String id,
         final XmlMapper mapper, final WebResource resource) 
     {
         return deleteOperation.execute(resource, mapper, type, id);
     }
     
     /**
      * @param type
      * @param id
      * @param mapper
      * @param resource
      * @return
      */
     public Map<String, Object> count(final EntityType type, final String id,
         final XmlMapper mapper, final WebResource resource)
     {
         return countOperation.execute(resource, mapper, type, id);
     }
     
     /**
      * @param type
      * @param id
      * @param mapper
      * @param resource
      * @return
      */
    public Map<String, Object> get(final String id,
         final XmlMapper mapper, final WebResource resource)
     {
        return getOperation.execute(resource, mapper, this, id);
     }
     
     /**
      * @param type
      * @param id
      * @param mapper
      * @param resource
      * @return
      */
     public Map<String, Object> getAll(final EntityType type, final String id,
         final XmlMapper mapper, final WebResource resource)
     {
         return getAllOperation.execute(resource, mapper, type, id);
     }
     
     /**
      * @param type
      * @param entityData
      * @param mapper
      * @param resource
      * @return
      */
     public Map<String, Object> put(final EntityType type, final Map<String, Object> entityData,
         final XmlMapper mapper, final WebResource resource)
     {
         return putOperation.execute(resource, mapper, type, entityData);
     }
     
     /**
      * 
      */
     public String generateBaseUri()
     {
         final StringBuilder uri = new StringBuilder(getServiceUri() + "/");
         uri.append(pluralize(this.entityTypeName.toLowerCase()));
         return uri.toString();
     }
     
     /**Pluralizes the service name.
      * @param str The {@link String} to pluralize
      * @return str in plural*/
     private static String pluralize(final String str) 
     {
         if (str.endsWith("y")) 
         {
             return StringUtils.substringBeforeLast(str, "y") + "ies";
         }
         return str + "s";
     }
 
 }
