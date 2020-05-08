 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
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
 
 package de.escidoc.core.adm.service.interfaces;
 
 import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 
 /**
  * The interface for access to the administration tool.
  *
  * @author André Schenk
  */
 public interface AdminHandlerInterface {
 
     /**
      * Delete a list of objects given by their object ids from Fedora. In case of Items this method will also delete all
      * depending Components of the given Items. The deletion runs asynchronously and returns some useful information to
      * the user, e.g. the total number of objects to delete. <b>Example:</b><br/> <br/>
      * <p/>
      * <pre>
     * {@code
     * <?xml version="1.0" encoding="UTF-8"?>
     * <param>
     *   <id>escidoc:1</id>
     *   <id>escidoc:2</id>
     *   <id>escidoc:3</id>
     * </param>
     * }
      * </pre>
      *
      * @param taskParam list of object ids to delete<br/> boolean value to signal if the search index and the Resource
      *                  cache have to be kept in sync. If this value is set to false then the re-indexing and re-caching
      *                  should be run manually afterwards.
      * @return total number of objects deleted, ...
      * @throws InvalidXmlException     Thrown if the taskParam has an invalid structure
      * @throws SystemException         Thrown in case of an internal error.
      * @throws AuthenticationException Thrown if the authentication failed due to an invalid provided eSciDoc user
      *                                 handle.
      * @throws AuthorizationException  Thrown if the authorization failed.
      */
     String deleteObjects(final String taskParam) throws InvalidXmlException, SystemException, AuthenticationException,
         AuthorizationException;
 
     /**
      * Get the current status of the running/finished purging process.
      *
      * @return current status (how many objects are still in the queue)
      * @throws SystemException         Thrown in case of an internal error.
      * @throws AuthenticationException Thrown if the authentication failed due to an invalid provided eSciDoc user
      *                                 handle.
      * @throws AuthorizationException  Thrown if the authorization failed.
      */
     String getPurgeStatus() throws SystemException, AuthenticationException, AuthorizationException;
 
     /**
      * Get the current status of the running/finished reindexing process.
      *
      * @return current status (how many objects are still in the queue)
      * @throws SystemException         Thrown in case of an internal error.
      * @throws AuthenticationException Thrown if the authentication failed due to an invalid provided eSciDoc user
      *                                 handle.
      * @throws AuthorizationException  Thrown if the authorization failed.
      */
     String getReindexStatus() throws SystemException, AuthenticationException, AuthorizationException;
 
     /**
      * Decrease the type of the current status of the running reindexing process by 1.
      *
      * @param objectType object type to decrease
      * @throws InvalidXmlException     Thrown if the given XML is invalid.
      * @throws SystemException         Thrown in case of an internal error.
      * @throws AuthenticationException Thrown if the authentication failed due to an invalid provided eSciDoc user
      *                                 handle.
      * @throws AuthorizationException  Thrown if the authorization failed.
      */
     void decreaseReindexStatus(final String objectType) throws InvalidXmlException, SystemException,
         AuthenticationException, AuthorizationException;
 
     /**
      * Reinitialize the search index. The initialization runs asynchronously and returns some useful information to the
      * user, e.g. the total number of objects found.
      *
      * @param clearIndex      Clear the index before adding objects to it.
      * @param indexNamePrefix name of the index (may be null for "all indexes")
      * @return total number of objects found, ...
      * @throws InvalidSearchQueryException Thrown if a given search query could not be translated into a SQL query.
      * @throws SystemException             Thrown in case of an internal error.
      * @throws AuthenticationException     Thrown if the authentication failed due to an invalid provided eSciDoc user
      *                                     handle.
      * @throws AuthorizationException      Thrown if the authorization failed.
      */
     String reindex(final String clearIndex, final String indexNamePrefix) throws SystemException,
         InvalidSearchQueryException, AuthenticationException, AuthorizationException;
 
     /**
      * Provides a xml structure containing the index-configuration.
      *
      * @return xml structure with index configuration
      * @throws SystemException         Thrown if a framework internal error occurs.
      * @throws AuthenticationException Thrown if the authentication fails due to an invalid provided eSciDoc user
      *                                 handle.
      * @throws AuthorizationException  Thrown if authorization fails.
      */
     String getIndexConfiguration() throws SystemException, AuthenticationException, AuthorizationException;
 
     /**
      * Provides an XML structure containing public configuration properties of the eSciDoc Infrastructure and the
      * earliest creation date of eSciDoc repository objects.
      *
      * @return XML structure with eSciDoc configuration properties
      * @throws SystemException         Thrown in case of an internal error.
      * @throws AuthenticationException Thrown if the authentication failed due to an invalid provided eSciDoc user
      *                                 handle.
      * @throws AuthorizationException  Thrown if the authorization failed.
      */
     String getRepositoryInfo() throws SystemException, AuthenticationException, AuthorizationException;
 
     /**
      * Loads a set of example objects into the framework.
      *
      * @param type Specifies the type of example set which is to load.
      * @return some useful information
      * @throws InvalidSearchQueryException Thrown if a given search query could not be translated into a SQL query.
      * @throws SystemException             Thrown in case of an internal error.
      * @throws AuthenticationException     Thrown if the authentication failed due to an invalid provided eSciDoc user
      *                                     handle.
      * @throws AuthorizationException      Thrown if the authorization failed.
      */
     String loadExamples(final String type) throws InvalidSearchQueryException, SystemException,
         AuthenticationException, AuthorizationException;
 }
