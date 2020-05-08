 /**
  * Mule CMIS Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.cmis;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.chemistry.opencmis.client.api.ChangeEvent;
 import org.apache.chemistry.opencmis.client.api.ChangeEvents;
 import org.apache.chemistry.opencmis.client.api.CmisObject;
 import org.apache.chemistry.opencmis.client.api.Document;
 import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
 import org.apache.chemistry.opencmis.client.api.Folder;
 import org.apache.chemistry.opencmis.client.api.ItemIterable;
 import org.apache.chemistry.opencmis.client.api.ObjectId;
 import org.apache.chemistry.opencmis.client.api.ObjectType;
 import org.apache.chemistry.opencmis.client.api.OperationContext;
 import org.apache.chemistry.opencmis.client.api.Policy;
 import org.apache.chemistry.opencmis.client.api.QueryResult;
 import org.apache.chemistry.opencmis.client.api.Relationship;
 import org.apache.chemistry.opencmis.client.api.Repository;
 import org.apache.chemistry.opencmis.client.api.Session;
 import org.apache.chemistry.opencmis.client.runtime.ChangeEventsImpl;
 import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
 import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
 import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
 import org.apache.chemistry.opencmis.commons.PropertyIds;
 import org.apache.chemistry.opencmis.commons.SessionParameter;
 import org.apache.chemistry.opencmis.commons.data.Ace;
 import org.apache.chemistry.opencmis.commons.data.Acl;
 import org.apache.chemistry.opencmis.commons.data.ContentStream;
 import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
 import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
 import org.apache.chemistry.opencmis.commons.enums.BindingType;
 import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
 import org.apache.chemistry.opencmis.commons.enums.VersioningState;
 import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
 import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
 import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
 import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 
 /**
  * Implementation of {@link CMISFacade} that use Apache Chemistry Project.
  */
 public class ChemistryCMISFacade implements CMISFacade
 {
     private final Session session;
     private final Map<String, String> connectionParameters;
     
     public ChemistryCMISFacade(final String username,
                                final String password,
                                final String repositoryId,
                                final String baseURL,
                                final boolean useAtomPub)
     {
         this.connectionParameters = paramMap(username, password, repositoryId, baseURL, useAtomPub);
         this.session = createSession(connectionParameters);
     }
 
     public List<Repository> repositories()
     {
         return SessionFactoryImpl.newInstance().getRepositories(connectionParameters);
     }
     
     public RepositoryInfo repositoryInfo()
     {
         return session.getRepositoryInfo();
     }
 
     public ChangeEvents changelog(final String changeLogToken, final boolean includeProperties)
     {
         boolean hasMore = false;
         String token = changeLogToken;
 
         final List<ChangeEvent> changeEvents = new ArrayList<ChangeEvent>();
         long totalNumItems = 0;
         // follow the pages
         do
         {
             final ChangeEvents events = session.getContentChanges(token, includeProperties, 50);
             totalNumItems += events.getTotalNumItems();
 
             changeEvents.addAll(events.getChangeEvents());
             if (events.getHasMoreItems())
             {
                 final String t = events.getLatestChangeLogToken();
                 if (t != null && !t.equals(token))
                 {
                     hasMore = true;
                     token = t;
                 }
             }
         }
         while (hasMore);
 
         return new ChangeEventsImpl(token, changeEvents, false, totalNumItems);
     }
 
     public CmisObject getObjectById(final String objectId)
     {
         try
         {
             return session.getObject(session.createObjectId(objectId));
         }
         catch (final CmisObjectNotFoundException e)
         {
             return null;
         }
     }
 
     public CmisObject getObjectByPath(final String path)
     {
         try
         {
             return session.getObjectByPath(path);
         }
         catch (final CmisObjectNotFoundException e)
         {
             return null;
         }
         catch (final CmisInvalidArgumentException e) 
         {
             return null;
         }
     }
 
     public ObjectId createDocumentById(final String objectId,
                                              final String filename,
                                              final Object content,
                                              final String mimeType,
                                              final org.mule.module.cmis.VersioningState versioningState,
                                              final String objectType)
     {
         Validate.notEmpty(objectId, "objectId is empty");
         
         return createDocument(
             session.getObject(session.createObjectId(objectId)), 
             filename, content, mimeType, versioningState, objectType);
     }
 
     public ObjectId createDocumentByPath(final String folderPath,
                                          final String filename,
                                          final Object content,
                                          final String mimeType,
                                          final org.mule.module.cmis.VersioningState versioningState,
                                          final String objectType)
     {
         Validate.notEmpty(folderPath, "folderPath is empty");
         
         return createDocument(session.getObjectByPath(folderPath), 
             filename, content, mimeType, versioningState, objectType);
     }
 
     /** create a document */
     protected ObjectId createDocument(
                                    final CmisObject folder,
                                    final String filename,
                                    final Object content,
                                    final String mimeType,
                                    final org.mule.module.cmis.VersioningState versioningState,
                                    final String objectType)
     {
         Validate.notNull(folder,    "folder is null");
         Validate.notEmpty(filename, "filename is empty");
         Validate.notNull(content,   "content is null");
         Validate.notEmpty(mimeType, "did you mean application/octet-stream?");
         Validate.notNull(versioningState, "versionState is null");
         VersioningState vs = null; 
         try
         {
            vs = VersioningState.valueOf(versioningState.value());
         }
         catch (final IllegalArgumentException e) 
         {
             throw new IllegalArgumentException(String.format(
                 "Illegal value for versioningState. Given `%s' could be: ",
                 versioningState, Arrays.toString(VersioningState.values())), e);
         }
         
         final Map<String, Object> properties = new HashMap<String, Object>();
         properties.put(PropertyIds.OBJECT_TYPE_ID, objectType);
         properties.put(PropertyIds.NAME, filename);
         return session.createDocument(properties, 
                 session.createObjectId(folder.getId()),
                 createContentStream(filename, mimeType, content), vs);   
     }
     
     public static ContentStream createContentStream(final String filename,
                                                     final String mimeType,
                                                     final Object content)
     {
         final ContentStreamImpl ret;
 
         if (content instanceof String)
         {
             ret = new ContentStreamImpl(filename, mimeType, (String) content);
         }
         else
         {
             ret = new ContentStreamImpl();
             ret.setFileName(filename);
             ret.setMimeType(mimeType);
             if (content instanceof InputStream)
             {
                 ret.setStream((InputStream) content);
             }
             else if (content instanceof byte[])
             {
                 ret.setStream(new ByteArrayInputStream((byte[]) content));
             }
             else
             {
                 throw new IllegalArgumentException("Don't know how to handle content of type"
                                                    + content.getClass());
             }
         }
 
         return ret;
     }
 
     public ObjectId createFolder(final String folderName, final String parentObjectId)
     {
         final Map<String, Object> properties = new HashMap<String, Object>();
         properties.put(PropertyIds.NAME, folderName);
         properties.put(PropertyIds.OBJECT_TYPE_ID,   "cmis:folder");
         try 
         {
             return session.createFolder(properties, session.getObject(
                 session.createObjectId(parentObjectId)));
         }
         catch (final CmisContentAlreadyExistsException e)
         {
             final CmisObject object = session.getObject(session.createObjectId(parentObjectId));
             if (!(object instanceof Folder)) 
             {
                 throw new IllegalArgumentException(parentObjectId + " is not a folder");
             }
             final Folder folder = (Folder) object;
             for (final CmisObject o : folder.getChildren())
             {
                 if (o.getName().equals(folderName)) 
                 {
                     return session.createObjectId(o.getId());
                 }
             }
             
             return null;
         }
     }
 
     public ObjectType getTypeDefinition(final String typeId) 
     {
         Validate.notEmpty(typeId, "typeId is empty");
         return session.getTypeDefinition(typeId);
     }
     
     public ItemIterable<Document> getCheckoutDocs(final String filter, 
                                                   final String orderBy, 
                                                   final Boolean includeACLs) 
     {
         final OperationContext ctx = createOperationContext(filter, orderBy, includeACLs);
         if (ctx != null) 
         {
             return session.getCheckedOutDocs(ctx);
         }
         else 
         {
             return session.getCheckedOutDocs();
         }
     }
     
     public ItemIterable<QueryResult> query(final String statement,
             final Boolean searchAllVersions, final String filter,
             final String orderBy, final Boolean includeACLs) 
     {
         Validate.notEmpty(statement, "statement is empty");
         Validate.notNull(searchAllVersions, "searchAllVersions is empty");
         
         final OperationContext ctx = createOperationContext(filter, orderBy, includeACLs);
         if (ctx != null)
         {
             return session.query(statement, searchAllVersions, ctx);
         }
         else 
         {
             return session.query(statement, searchAllVersions);
         }
     }
    
     public List<Folder> getParentFolders(final CmisObject cmisObject, final String objectId) 
     {
         validateObjectOrId(cmisObject, objectId);
         validateRedundantIdentifier(cmisObject, objectId);
         final CmisObject target = getCmisObject(cmisObject, objectId);
         
         if (target != null  && target instanceof FileableCmisObject) 
         {
             return ((FileableCmisObject) target).getParents();
         }
         return null;
     }
 
     public Object folder(final Folder folder, final String folderId,
                          final NavigationOptions get, final Integer depth,
                          final String filter, final String orderBy, final Boolean includeACLs)
     {
         validateObjectOrId(folder, folderId);
         validateRedundantIdentifier(folder, folderId);
         
         final Folder target = getCmisObject(folder, folderId, Folder.class);
         Object ret = null;
 
         if (target != null)
         {
             if (get.equals(NavigationOptions.DESCENDANTS) || get.equals(NavigationOptions.TREE))
             {
                 Validate.notNull(depth, "depth is null");
             }
             
             if (get.equals(NavigationOptions.PARENT))
             {
                 ret = target.getFolderParent();
             }
             else
             {
                 final OperationContext ctx = createOperationContext(filter, orderBy, includeACLs);
                 if (get.equals(NavigationOptions.CHILDREN))
                 {
                     ret = ctx == null ? target.getChildren() : target.getChildren(ctx);
                 }
                 else if (get.equals(NavigationOptions.DESCENDANTS))
                 {
                     ret = ctx == null ? target.getDescendants(depth) : target.getDescendants(depth, ctx);
                 }
                 else if (get.equals(NavigationOptions.TREE))
                 {
                     ret = ctx == null ? target.getFolderTree(depth) : target.getFolderTree(depth, ctx);
                 }
             }
             return ret;
         }
         
         return ret;
     }
     
     public ContentStream getContentStream(final CmisObject cmisObject, final String objectId)
     {
         validateObjectOrId(cmisObject, objectId);
         validateRedundantIdentifier(cmisObject, objectId);
         
         final CmisObject target = getCmisObject(cmisObject, objectId);
         
         if (target != null && target instanceof Document) 
         {
             return ((Document) target).getContentStream();
         }
         return null;
     }
 
     public FileableCmisObject moveObject(final FileableCmisObject cmisObject,
                                    final String objectId,
                                    final String sourceFolderId,
                                    final String targetFolderId)
     {
         validateObjectOrId(cmisObject, objectId);
         validateRedundantIdentifier(cmisObject, objectId);
         Validate.notEmpty(sourceFolderId, "sourceFolderId is empty");
         Validate.notEmpty(targetFolderId, "targetFolderId is empty");
         
         final FileableCmisObject target = getCmisObject(cmisObject, objectId, FileableCmisObject.class);
         if (target != null)
         {
             return target.move(new ObjectIdImpl(sourceFolderId), new ObjectIdImpl(targetFolderId));
         }
         return null;
     }
 
     public CmisObject updateObjectProperties(final CmisObject cmisObject,
                                              final String objectId, 
                                              final Map<String, Object> properties)
     {
         validateObjectOrId(cmisObject, objectId);
         validateRedundantIdentifier(cmisObject, objectId);
         Validate.notNull(properties, "properties is null");
         
         final CmisObject target = getCmisObject(cmisObject, objectId);
         if (target != null)
         {
             return target.updateProperties(properties);
         }
         return null;
     }
 
     public void delete(CmisObject cmisObject, String objectId, boolean allVersions)
     {
         validateObjectOrId(cmisObject, objectId);
         validateRedundantIdentifier(cmisObject, objectId);
         
         final CmisObject target = getCmisObject(cmisObject, objectId);
         if (target != null)
         {
             target.delete(allVersions);
         }
     }
     
     public List<String> deleteTree(CmisObject folder, String folderId,
             boolean allversions, UnfileObject unfile, boolean continueOnFailure)
     {
         validateObjectOrId(folder, folderId);
         validateRedundantIdentifier(folder, folderId);
         final CmisObject target = getCmisObject(folder, folderId);
         if (target != null && target instanceof Folder)
         {
             return ((Folder) target).deleteTree(allversions, unfile, continueOnFailure);
         }
         return null;
     }
     
     public List<Relationship> getObjectRelationships(final CmisObject cmisObject,
                                                      final String objectId)
     {
         validateObjectOrId(cmisObject, objectId);
         validateRedundantIdentifier(cmisObject, objectId);
         final CmisObject target = getCmisObject(cmisObject, objectId);
         if (target != null)
         {
             return target.getRelationships();
         }
         return null;
     }
 
     public Acl getAcl(final CmisObject cmisObject, final String objectId)
     {
         validateObjectOrId(cmisObject, objectId);
         validateRedundantIdentifier(cmisObject, objectId);
         final CmisObject target = getCmisObject(cmisObject, objectId);
         if (target != null)
         {
             return target.getAcl();        
         }
         return null;
     }
 
     public List<Document> getAllVersions(final CmisObject document, final String documentId,
                                         final String filter,  final String orderBy, 
                                         final Boolean includeACLs)
     {
         validateObjectOrId(document, documentId);
         validateRedundantIdentifier(document, documentId);
         final CmisObject target = getCmisObject(document, documentId);
         
         if (target instanceof Document)
         {
             final OperationContext ctx = createOperationContext(filter, orderBy, includeACLs);
             if (ctx != null)
             {
                 return ((Document) target).getAllVersions(ctx);
             }
             else
             {
                 return ((Document) target).getAllVersions();
             }
         }
         return null;
     }
     
 
     public ObjectId checkOut(final CmisObject document, final String documentId)
     {
         validateObjectOrId(document, documentId);
         validateRedundantIdentifier(document, documentId);
         final CmisObject target = getCmisObject(document, documentId);
         
         if (target != null  && target instanceof Document) 
         {
             return ((Document) target).checkOut();
         }
         return null;
     }
 
 
     public void cancelCheckOut(final CmisObject document, final String documentId)
     {
         validateObjectOrId(document, documentId);
         validateRedundantIdentifier(document, documentId);
         final CmisObject target = getCmisObject(document, documentId);
         if (target != null  && target instanceof Document) 
         {
             ((Document) target).cancelCheckOut();
         }
     }
 
     public ObjectId checkIn(final CmisObject document, final String documentId,
                             final Object content, final String filename, 
                             final String mimeType, boolean major, String checkinComment)
     {
         validateObjectOrId(document, documentId);
         validateRedundantIdentifier(document, documentId);
         Validate.notEmpty(filename, "filename is empty");
         Validate.notNull(content,   "content is null");
         Validate.notEmpty(mimeType, "did you mean application/octet-stream?");
         Validate.notEmpty(checkinComment, "checkinComment is empty");
         
         final CmisObject target = getCmisObject(document, documentId);
         if (target != null && target instanceof Document)
         {
             final Document doc = (Document) target;
             return doc.checkIn(major, Collections.<String, String>emptyMap(), 
                                createContentStream(filename, mimeType, content),
                                checkinComment);
         }
         return null;
     }
     
 
     
     public Acl applyAcl(final CmisObject cmisObject, final String objectId, final List<Ace> addAces, 
                         final List<Ace> removeAces, final AclPropagation aclPropagation)
     {
         validateObjectOrId(cmisObject, objectId);
         validateRedundantIdentifier(cmisObject, objectId);
         final CmisObject target = getCmisObject(cmisObject, objectId);
         if (target != null)
         {
             return target.applyAcl(addAces, removeAces, aclPropagation);
         }
         return null;
     }
     
     public List<Policy> getAppliedPolicies(final CmisObject cmisObject, final String objectId)
     {
         validateObjectOrId(cmisObject, objectId);
         validateRedundantIdentifier(cmisObject, objectId);
         final CmisObject target = getCmisObject(cmisObject, objectId);
         if (target != null)
         {
             return target.getPolicies();
         }
         return null;
     }
 
     
     /**
      * Validates that either a CmisObject or it's ID has been provided.
      */
     private static void validateObjectOrId(final CmisObject object, final String objectId)
     {
         if (object == null && StringUtils.isBlank(objectId))
         {
             throw new IllegalArgumentException("Both the cmis object and it's ID are not set");
         }
     }
     
     /**
      * Validates that and object's ID is the one provided, in case both are not null or blank.   
      */
     private static void validateRedundantIdentifier(final CmisObject object, final String objectId)
     {
         if (object != null && StringUtils.isNotBlank(objectId) && !object.getId().equals(objectId))
         {
             throw new IllegalArgumentException("The id provided does not match the object's ID");
         }
     }
     
     private CmisObject getCmisObject(final CmisObject object, final String objectId)
     {
         return getCmisObject(object, objectId, CmisObject.class);
     }
     
     /**
      * Returns the object if it is not null. Otherwise, get the object by ID and
      * returns it if types match. Returns null if types don't match.
      * @return
      */
     @SuppressWarnings("unchecked")
     private <T> T getCmisObject(final T object, final String objectId, final Class<T> clazz)
     {
         if (object != null)
         {
             return object;
         }
         else
         {
             final CmisObject obj = getObjectById(objectId);
             if (clazz.isAssignableFrom(obj.getClass())) 
             {
                 return (T) obj;
             }
             return null;
         }
     }
     
     
     private static OperationContext createOperationContext(final String filter,
                                                            final String orderBy,
                                                            final Boolean includeACLs) 
     {
         if (StringUtils.isNotBlank(filter) || StringUtils.isNotBlank(orderBy) || includeACLs != null) 
         {
             final OperationContext ctx = new OperationContextImpl();
             if (StringUtils.isNotBlank(filter))
             {
                 ctx.setFilterString(filter);
             }
             if (StringUtils.isNotBlank(orderBy))
             {
                 ctx.setOrderBy(orderBy);
             }
             if (includeACLs != null)
             {
                 ctx.setIncludeAcls(includeACLs);
             }
             return ctx;
         }
         return null;
     }
     
     private static Map<String, String> paramMap(final String username,
                                                 final String password,
                                                 final String repositoryId,
                                                 final String baseURL,
                                                 final boolean useAtomPub)
     {
         Validate.notEmpty(username, "username is empty");
         Validate.notEmpty(password, "password is empty");
         Validate.notEmpty(repositoryId, "repository-id is empty");
         Validate.notEmpty(baseURL, "base-url is empty");
 
         final Map<String, String> parameters = new HashMap<String, String>();
 
         // user credentials
         parameters.put(SessionParameter.USER, username);
         parameters.put(SessionParameter.PASSWORD, password);
 
         // connection settings... we prefer SOAP over ATOMPUB because some rare
         // behaviurs with the ChangeEvents.getLatestChangeLogToken().
         if (!useAtomPub)
         {
             parameters.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
             parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, baseURL + "ACLService?wsdl");
             parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, baseURL + "DiscoveryService?wsdl");
             parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, baseURL + "MultiFilingService?wsdl");
             parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, baseURL + "NavigationService?wsdl");
             parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, baseURL + "ObjectService?wsdl");
             parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, baseURL + "PolicyService?wsdl");
             parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, baseURL
                                                                               + "RelationshipService?wsdl");
             parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, baseURL + "RepositoryService?wsdl");
             parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, baseURL + "VersioningService?wsdl");
         } 
         else 
         {
             parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
             parameters.put(SessionParameter.ATOMPUB_URL, baseURL);
         }
 
         parameters.put(SessionParameter.REPOSITORY_ID, repositoryId);
 
         // session locale
         parameters.put(SessionParameter.LOCALE_ISO3166_COUNTRY, "");
         parameters.put(SessionParameter.LOCALE_ISO639_LANGUAGE, "en");
 
         return parameters;
     }
     
     private static Session createSession(final Map<String, String> parameters)
     {
         Validate.notNull(parameters);
         return SessionFactoryImpl.newInstance().createSession(parameters);
     }
 
 }
