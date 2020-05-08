 /*
  * The MIT License
  *
  * Copyright 2013 Gravidence.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.gravidence.gravifon.db;
 
 import java.util.List;
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.gravidence.gravifon.db.domain.UserDocument;
 import org.gravidence.gravifon.db.message.CreateDocumentResponse;
 import org.gravidence.gravifon.exception.GravifonException;
 import org.gravidence.gravifon.exception.error.GravifonError;
 import org.gravidence.gravifon.resource.bean.UserBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 
 /**
  * CouchDB JAX-RS client to <code>/users</code> database.
  * 
  * @author Maksim Liauchuk <maksim_liauchuk@fastmail.fm>
  */
 public class UsersDBClient implements InitializingBean {
     
     private static final Logger LOGGER = LoggerFactory.getLogger(UsersDBClient.class);
     
     /**
      * @see #setDbClient(org.gravidence.gravifon.db.CouchDBClient)
      */
     private CouchDBClient dbClient;
     
     /**
      * JAX-RS client target associated with <code>/users</code> database.
      */
     private WebTarget dbTarget;
     
     /**
      * JAX-RS client target associated with <code>main/all_usernames</code> view.
      */
     private WebTarget viewMainAllUsernamesTarget;
 
     /**
      * Sets {@link CouchDBClient} instance.
      * 
      * @param dbClient CouchDB client instance
      */
     public void setDbClient(CouchDBClient dbClient) {
         this.dbClient = dbClient;
     }
 
     @Override
     public void afterPropertiesSet() throws Exception {
         dbTarget = dbClient.getTarget()
                 .path("users");
         
         viewMainAllUsernamesTarget = getViewTarget("main", "all_usernames");
     }
     
     /**
      * Retrieves user amount.<p>
      * User amount is equal to <code>main/all_usernames</code> view size.
      * 
      * @return user amount
      * 
      * @see #viewMainAllUsernamesTarget
      * @see ViewQueryExecutor#querySize(javax.ws.rs.client.WebTarget)
      */
     public long retrieveUserAmount() {
         return ViewQueryExecutor.querySize(viewMainAllUsernamesTarget);
     }
     
     /**
      * Creates a new user {@link UserDocument document}.
      * 
      * @param user new user details document
      * @return created user document identifier and revision
      */
     public CreateDocumentResponse createUser(UserDocument user) {
         Response response = dbTarget
                 .request(MediaType.APPLICATION_JSON_TYPE)
                 .post(Entity.json(user));
         
         CreateDocumentResponse document;
         if (CouchDBClient.isSuccessful(response)) {
             document = response.readEntity(CreateDocumentResponse.class);
             LOGGER.trace("'{}' user created", new UserBean().updateBean(user).updateBean(document));
         }
         else {
             LOGGER.error("Failed to create '{}' user: [{}] {}", user.getUsername(),
                     response.getStatus(), response.getStatusInfo().getReasonPhrase());
             
             response.close();
             
             throw new GravifonException(GravifonError.DATABASE_OPERATION, "Failed to create user.");
         }
         
         return document;
     }
     
     /**
      * Retrieves existing user {@link UserDocument document}.
      * 
      * @param id user identifier
      * @return user details document if found, <code>null</code> otherwise
      */
     public UserDocument retrieveUserByID(String id) {
         Response response = dbTarget
                 .path(id)
                 .request(MediaType.APPLICATION_JSON_TYPE)
                 .get();
         
         UserDocument document;
         if (CouchDBClient.isSuccessful(response)) {
             document = response.readEntity(UserDocument.class);
             LOGGER.trace("'{}' user retrieved", document);
         }
         else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
             document = null;
         }
         else {
             LOGGER.error("Failed to retrieve user for '{}' id: [{}] {}", id,
                     response.getStatus(), response.getStatusInfo().getReasonPhrase());
             
             response.close();
             
             throw new GravifonException(GravifonError.DATABASE_OPERATION, "Failed to retrieve user.");
         }
         
         return document;
     }
     
     /**
     * Retrieves existing user {@link UserDocument document}.
      * 
      * @param username username
      * @return user details document if found, <code>null</code> otherwise
      */
     public UserDocument retrieveUserByUsername(String username) {
         ViewQueryArguments args = new ViewQueryArguments()
                .addKey(username)
                 .addIncludeDocs(true);
         
         List<UserDocument> documents = ViewQueryExecutor.queryDocuments(viewMainAllUsernamesTarget, args,
                 UserDocument.class);
         
         // usernames are unique, so don't care about multiple results
         return documents == null ? null : documents.get(0);
     }
     
     /**
      * Updates existing user details.
      * 
      * @param user user details document
      * @return updated user document identifier and revision
      */
     public CreateDocumentResponse updateUser(UserDocument user) {
         Response response = dbTarget
                 .path(user.getId())
                 .request(MediaType.APPLICATION_JSON_TYPE)
                 .put(Entity.json(user));
         
         CreateDocumentResponse document;
         if (CouchDBClient.isSuccessful(response)) {
             document = response.readEntity(CreateDocumentResponse.class);
             LOGGER.trace("'{}' user updated", new UserBean().updateBean(user).updateBean(document));
         }
         else {
             LOGGER.error("Failed to update '{}' user: [{}] {}", user,
                     response.getStatus(), response.getStatusInfo().getReasonPhrase());
             
             response.close();
             
             throw new GravifonException(GravifonError.DATABASE_OPERATION, "Failed to update user.");
         }
         
         return document;
     }
     
     /**
      * Deletes existing user.
      * 
      * @param user existing user details document
      */
     public void deleteUser(UserDocument user) {
         Response response = dbTarget
                 .path(user.getId())
                 .queryParam("rev", user.getRevision())
                 .request(MediaType.APPLICATION_JSON_TYPE)
                 .delete();
         
         if (CouchDBClient.isSuccessful(response)) {
 //            UserDocument document = response.readEntity(UserDocument.class);
             LOGGER.trace("'{}' user deleted", user);
         }
         else {
             LOGGER.error("Failed to delete '{}' user: [{}] {}", user,
                     response.getStatus(), response.getStatusInfo().getReasonPhrase());
             
             response.close();
             
             throw new GravifonException(GravifonError.DATABASE_OPERATION, "Failed to delete user.");
         }
     }
     
     /**
      * Creates JAX-RS client target associated with specified design document view of <code>/users</code> database.
      * 
      * @param designDocName design document name
      * @param viewName view name
      * @return design document view specific target
      */
     private WebTarget getViewTarget(String designDocName, String viewName) {
         return dbTarget
                 .path("_design")
                 .path(designDocName)
                 .path("_view")
                 .path(viewName);
     }
     
 }
