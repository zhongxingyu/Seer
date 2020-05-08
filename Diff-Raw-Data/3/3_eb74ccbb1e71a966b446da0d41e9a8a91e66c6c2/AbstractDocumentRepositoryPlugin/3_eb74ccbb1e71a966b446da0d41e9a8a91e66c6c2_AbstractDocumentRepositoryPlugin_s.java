 /*
  * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Nuxeo - initial API and implementation
  *
  * $Id$
  *
  */
 
 package org.nuxeo.ecm.platform.documentrepository.service.plugin.base;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.nuxeo.common.utils.Base64;
 import org.nuxeo.common.utils.IdUtils;
 import org.nuxeo.common.utils.Path;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentRef;
 import org.nuxeo.ecm.core.api.PathRef;
 
 /**
  *
  * Abstract base class for DocumentRepository plugins
  *
  * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
  *
  */
 public abstract class AbstractDocumentRepositoryPlugin {
 
     public static final String REPO_DOC_TYPE = "RepositoryDocType";
 
     public static final String DEFAULT_REPO_DOC_TYPE = "Repository";
 
     public static final String REPO_SUBPART_DOC_TYPE = "RepositorySubPartDocType";
 
     public static final String DEFAULT_REPO_SUBPART_DOC_TYPE = "Folder";
 
     public static final String SUB_PATH_PART_LEN = "RepositorySubPathLen";
 
     public static final String SUB_PATH_PART_NUMBER = "RepositorySubPathNumber";
 
     public static final int DEFAULT_SUB_PATH_PART_LEN = 2;
 
     public static final int DEFAULT_SUB_PATH_PART_NUMBER = 4;
 
     protected static Random randomGen = new Random(System.currentTimeMillis());
 
     protected Map<String, String> params = new HashMap<String, String>();
 
     public void init(Map<String, String> params) {
         this.params = params;
     }
 
     protected String getRepositoryDocType() {
         if (params.containsKey(REPO_DOC_TYPE)) {
             return params.get(REPO_DOC_TYPE);
         }
         return DEFAULT_REPO_DOC_TYPE;
     }
 
     protected String getRepositorySubPartDocType() {
         if (params.containsKey(REPO_SUBPART_DOC_TYPE)) {
             return params.get(REPO_SUBPART_DOC_TYPE);
         }
         return DEFAULT_REPO_SUBPART_DOC_TYPE;
     }
 
     protected int getRepositorySubPathPartLength() {
         if (params.containsKey(SUB_PATH_PART_LEN)) {
             return Integer.parseInt(params.get(SUB_PATH_PART_LEN));
         }
         return DEFAULT_SUB_PATH_PART_LEN;
 
     }
 
     protected int getRepositorySubPathPartNumber() {
         if (params.containsKey(SUB_PATH_PART_NUMBER)) {
             return Integer.parseInt(params.get(SUB_PATH_PART_NUMBER));
         }
         return DEFAULT_SUB_PATH_PART_NUMBER;
     }
 
     protected List<String> getSubStoragePath(DocumentModel docToStore) {
 
         String token = docToStore.getTitle();
         String base64HashedToken = null;
         if (token == null) {
             token = docToStore.getType();
         }
 
         token = token + System.currentTimeMillis() + randomGen.nextLong();
         ;
 
         try {
             byte[] hashedToken = MessageDigest.getInstance("MD5").digest(
                     token.getBytes());
             base64HashedToken = Base64.encodeBytes(hashedToken);
         } catch (NoSuchAlgorithmException e) {
             base64HashedToken = Base64.encodeBytes(token.getBytes());
         }
 
         List<String> parts = new ArrayList<String>();
         int idx = 0;
 
         for (int i = 0; i < getRepositorySubPathPartNumber(); i++) {
             String subPart = base64HashedToken.substring(idx, idx
                     + getRepositorySubPathPartLength());
             parts.add(subPart);
             idx += getRepositorySubPathPartLength();
         }
         return parts;
     }
 
     protected String getAndCheckStoragePath(DocumentModel repo,
             DocumentModel docToStore, SessionHelper helper)
             throws ClientException {
 
         List<String> parts = getSubStoragePath(docToStore);
 
         Path storagePath = new Path(repo.getPathAsString());
 
         String coreRepositoryName = repo.getRepositoryName();
 
         boolean createNewPath = false;
         try {
             for (String part : parts) {
                 String parentPath = storagePath.toString();
                 storagePath = storagePath.append(part);
                 DocumentRef subPathRef = new PathRef(storagePath.toString());
 
                 if (createNewPath
                         || !helper.getUnrestrictedDocumentManager(coreRepositoryName).exists(
                                 subPathRef)) {
                     DocumentModel partDM = helper.getUnrestrictedDocumentManager(
                             coreRepositoryName).createDocumentModel(parentPath,
                             part, getRepositorySubPartDocType());
                     helper.getUnrestrictedDocumentManager(coreRepositoryName).createDocument(
                             partDM);
                     createNewPath = true;
                 }
             }
             helper.getUnrestrictedDocumentManager(coreRepositoryName).save();
         } catch (Exception e) {
             throw new ClientException("Error whil checking storage path", e);
         }
         return storagePath.toString();
     }
 
     protected String getAndCheckStoragePath(DocumentModel repo,
             DocumentModel docToStore) throws ClientException {
 
         SessionHelper helper = new SessionHelper();
 
         try {
             return getAndCheckStoragePath(repo, docToStore, helper);
         } catch (Exception e) {
             throw new ClientException("Error whil checking storage path", e);
         } finally {
             helper.release();
         }
     }
 
     protected DocumentRef createRepository(String coreRepositoryName,
             String parentPath, String repoId) throws ClientException {
 
         SessionHelper helper = new SessionHelper();
 
         try {
             CoreSession unrestrictedDocumentManager = helper.getUnrestrictedDocumentManager(coreRepositoryName);
 
             DocumentModel repo = unrestrictedDocumentManager.createDocumentModel(
                     parentPath, repoId, getRepositoryDocType());
 
             repo = unrestrictedDocumentManager.createDocument(repo);
 
             unrestrictedDocumentManager.save();
 
             repo = repoCreationCallBack(unrestrictedDocumentManager, repo);
 
             unrestrictedDocumentManager.save();
 
             return repo.getRef();
         } catch (Exception e) {
             throw new ClientException("Unable to create repository at path "
                     + parentPath + " with id " + repoId, e);
         } finally {
             helper.release();
         }
     }
 
     /**
      *
      * This call back may be overriden to handle specific processing when repo
      * is created (example : right settings)
      *
      * @param session
      * @param repo
      * @return
      */
     protected DocumentModel repoCreationCallBack(CoreSession session,
             DocumentModel repo) throws ClientException{
         return repo;
     }
 
     protected DocumentModel docCreationCallBack(CoreSession session,
             DocumentModel doc) throws ClientException {
         return doc;
     }
 
     public DocumentModel createDocument(CoreSession clientSession, DocumentModel repo, String typeName, String title)
             throws ClientException {
 
         if (title == null) {
             title = typeName + System.currentTimeMillis();
         }
 
         DocumentModel dm;
         try {
             dm = clientSession.createDocumentModel(typeName);
             dm.setProperty("dublincore", "title", title);
             return createDocument(clientSession, repo,dm);
         } catch (Exception e) {
             e.printStackTrace();
             throw new ClientException("Unable to create DocumentModel",e);
         }
 
     }
 
     public DocumentModel createDocument(CoreSession clientSession,DocumentModel repo, DocumentModel doc)
             throws ClientException {
 
         String storagePath = getAndCheckStoragePath(repo,doc);
 
         String name = doc.getName();
         if (name == null) {
             String title = doc.getTitle();
             if (title == null) {
                 title = doc.getType() + System.currentTimeMillis();
             }
             name = IdUtils.generateId(title);
         }
 
         doc.setPathInfo(storagePath, name);
 
         try {
             doc = clientSession.createDocument(doc);
             doc = docCreationCallBack(clientSession, doc);
             return doc;
         } catch (Exception e) {
             throw new ClientException("Unable to create Document in repository",e);
         }
 
     }
 
 }
