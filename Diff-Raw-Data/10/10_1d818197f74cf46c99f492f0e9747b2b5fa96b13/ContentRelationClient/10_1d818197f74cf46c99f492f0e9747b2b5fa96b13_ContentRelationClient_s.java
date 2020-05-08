 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.common.client.servlet.om;
 
 import de.escidoc.core.test.common.client.servlet.ClientBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.common.client.servlet.interfaces.ContentRelationHandlerClientInterface;
 
 import java.util.Map;
 
 /**
  * Offers access methods to the escidoc interfaces of the content relation resource.
  * 
  * @author Steffen Wagner
  */
 public class ContentRelationClient extends ClientBase implements ContentRelationHandlerClientInterface {
 
     /**
      * Retrieve the xml representation of all virtual resources of the content relation.
      * 
      * @param id
      *            The id of the content relation.
      * @return The HttpMethod after the service call .
      * @throws Exception
      *             If the service call fails.
      */
     public Object retrieveResources(final String id) throws Exception {
 
         return callEsciDoc("ContentRelation.retrieveResources", METHOD_RETRIEVE_RESOURCES, Constants.HTTP_METHOD_GET,
             Constants.CONTENT_RELATION_BASE_URI, new String[] { id, Constants.SUB_RESOURCES });
     }
 
     /**
      * Retrieve the xml representation of a content relation.
      * 
      * @param id
      *            The id of the content relation.
      * @return The HttpMethod after the service call .
      * @throws Exception
      *             If the service call fails.
      */
     @Override
     public Object retrieve(final String id) throws Exception {
 
         return callEsciDoc("ContentRelation.retrieve", METHOD_RETRIEVE, Constants.HTTP_METHOD_GET,
             Constants.CONTENT_RELATION_BASE_URI, new String[] { id });
     }
 
     /**
      * Retrieve content relations.
      * 
      * @param filter
      *            The filter param.
      * @return The HttpMethod after the service call .
      * @throws Exception
      *             If the service call fails.
      */
     public Object retrieveContentRelations(final Map<String, String[]> filter) throws Exception {
 
         return callEsciDoc("ContentRelation.retrieveContentRelations", METHOD_RETRIEVE_CONTENT_RELATIONS,
             Constants.HTTP_METHOD_GET, Constants.CONTENT_RELATIONS_BASE_URI, new String[] {}, filter);
     }
 
     /**
      * Retrieve the xml representation of the properties of a resource.
      * 
      * @param id
      *            The resource id.
      * @return The HttpMethod after the service call .
      * @throws Exception
      *             If the service call fails.
      */
     public Object retrieveProperties(final String id) throws Exception {
 
         return callEsciDoc("ContentRelation.retrieveProperties", METHOD_RETRIEVE_PROPERTIES, Constants.HTTP_METHOD_GET,
             Constants.CONTENT_RELATION_BASE_URI, new String[] { id, Constants.SUB_PROPERTIES });
     }
 
     /**
      * Create a content relation in the escidoc framework.
      * 
      * @param contentRelationXml
      *            The xml representation of the content relation.
      * @return The HttpMethod after the service call .
      * @throws Exception
      *             If the service call fails.
      */
     @Override
     public Object create(final Object contentRelationXml) throws Exception {
 
         return callEsciDoc("ContentRelation.create", METHOD_CREATE, Constants.HTTP_METHOD_PUT,
             Constants.CONTENT_RELATION_BASE_URI, new String[] {}, changeToString(contentRelationXml));
     }
 
     /**
      * Update a content relation in the escidoc framework.
      * 
      * @param id
      *            The id of the content relation.
      * @param contentRelationXml
      *            The xml representation of the content relation.
      * @return The HttpMethod after the service call .
      * @throws Exception
      *             If the service call fails.
      */
     @Override
     public Object update(final String id, final Object contentRelationXml) throws Exception {
 
         return callEsciDoc("ContentRelation.update", METHOD_UPDATE, Constants.HTTP_METHOD_PUT,
             Constants.CONTENT_RELATION_BASE_URI, new String[] { id }, changeToString(contentRelationXml));
     }
 
     /**
      * Delete a content relation from the escidoc framework.
      * 
      * @param id
      *            The id of the content relation.
      * @return The HttpMethod after the service call .
      * @throws Exception
      *             If the service call fails.
      */
     @Override
     public Object delete(final String id) throws Exception {
 
         return callEsciDoc("ContentRelation.delete", METHOD_DELETE, Constants.HTTP_METHOD_DELETE,
             Constants.CONTENT_RELATION_BASE_URI, new String[] { id });
     }
 
     /**
      * Submit a content relation.
      * 
      * @param id
      *            The id of the content relation.
      * @return The HttpMethod after the service call .
      * @throws Exception
      *             If the service call fails.
      */
     public Object submit(final String id, final String param) throws Exception {
         return callEsciDoc("ContentRelation.submit", METHOD_SUBMIT, Constants.HTTP_METHOD_POST,
             Constants.CONTENT_RELATION_BASE_URI, new String[] { id, Constants.SUB_SUBMIT }, param);
 
     }
 
     /**
      * Release a content relation.
      * 
      * @param id
      *            The id of the content relation.
      * @return The HttpMethod after the service call .
      * @throws Exception
      *             If the service call fails.
      */
     public Object release(final String id, final String param) throws Exception {
         return callEsciDoc("ContentRelation.release", METHOD_RELEASE, Constants.HTTP_METHOD_POST,
             Constants.CONTENT_RELATION_BASE_URI, new String[] { id, Constants.SUB_RELEASE }, param);
 
     }
 
     /**
      * Revise a content relation.
      * 
      * @param id
      *            The id of the content relation.
      * @return The HttpMethod after the service call .
      * @throws Exception
      *             If the service call fails.
      */
     public Object revise(final String id, final String param) throws Exception {
         return callEsciDoc("ContentRelation.revise", METHOD_REVISE, Constants.HTTP_METHOD_POST,
             Constants.CONTENT_RELATION_BASE_URI, new String[] { id, Constants.SUB_REVISE }, param);
 
     }
 
     /**
      * Lock a Content Relation for other user access.
      * 
      * @param id
      *            The id of the content relation.
      * @return The HttpMethod after the service call .
      * @throws Exception
      *             If the service call fails.
      */
     public Object lock(final String id, final String param) throws Exception {
         return callEsciDoc("ContentRelation.lock", METHOD_LOCK, Constants.HTTP_METHOD_POST,
             Constants.CONTENT_RELATION_BASE_URI, new String[] { id, Constants.SUB_LOCK }, param);
     }
 
     /**
      * Unlock a Content Relation for other user access.
      * 
      * @param id
      *            The id of the content relation.
      * @return The HttpMethod after the service call .
      * @throws Exception
      *             If the service call fails.
      */
     public Object unlock(final String id, final String param) throws Exception {
         return callEsciDoc("ContentRelation.unlock", METHOD_UNLOCK, Constants.HTTP_METHOD_POST,
             Constants.CONTENT_RELATION_BASE_URI, new String[] { id, Constants.SUB_UNLOCK }, param);
     }
 
     /**
      * Assign persistent identifier to a Content-relation object.
      * 
      * @return The HttpMethod after the service call .
      * @throws Exception
      *             If the service call fails.
      */
 
     public Object assignObjectPid(final String id, final String param) throws Exception {
         return callEsciDoc("ContentRelation.assignObjectPid", METHOD_ASSIGN_OBJECT_PID, Constants.HTTP_METHOD_POST,
             Constants.CONTENT_RELATION_BASE_URI, new String[] { id, Constants.SUB_ASSIGN_OBJECT_PID }, param);
     }
 
     /**
      * Get escidoc XML representation of ContentRelations md-records.
      * 
      * @return The HttpMethod after the service call .
      * @throws Exception
      *             If the service call fails.
      */
     public Object retrieveMdRecords(final String id) throws Exception {
         return callEsciDoc("ContentRelation.retrieveMdRecords", METHOD_RETRIEVE_MD_RECORDS, Constants.HTTP_METHOD_GET,
             Constants.CONTENT_RELATION_BASE_URI, new String[] { id, Constants.SUB_MD_RECORDS });
     }
 
     /**
      * Get escidoc XML representation of ContentRelations md-record with a provided id.
      * 
      * @param id
      *            objid of ContentRelation resource
      * @param name
      *            name of a md-record * @return The HttpMethod after the service call (REST) or the result object
      *            (SOAP).
      * @throws Exception
      *             If the service call fails.
      */
 
     public Object retrieveMdRecord(final String id, final String name) throws Exception {
         return callEsciDoc("ContentRelation.retrieveMdRecord", METHOD_RETRIEVE_MD_RECORD, Constants.HTTP_METHOD_GET,
             Constants.CONTENT_RELATION_BASE_URI, new String[] { id, Constants.SUB_MD_RECORD, name });
     }
 
     /**
      * Get eSciDoc XML representation of Content Relations registered predicates.
      * 
      * @return
      * @throws Exception
      */
     public Object retrieveRegisteredPredicates() throws Exception {
         return callEsciDoc("ContentRelation.retrieveRegisteredPredicates", METHOD_RETRIEVE_REGISTERED_PREDICATES,
            Constants.HTTP_METHOD_GET, Constants.CONTENT_RELATION_BASE_URI, new String[] {});
 
     }
 }
