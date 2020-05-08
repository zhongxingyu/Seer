 package org.sortdc.sortdc.resources;
 
 import java.util.Map;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.sortdc.sortdc.Classifier;
 import org.sortdc.sortdc.Log;
 import org.sortdc.sortdc.dao.Category;
 import org.sortdc.sortdc.dao.Document;
 import org.sortdc.sortdc.resources.dto.CategoryDTO;
 import org.sortdc.sortdc.resources.dto.DocumentDTO;
 
 @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 public class DocumentResource {
 
     private Classifier classifier;
     private Category category;
     private Document document;
 
     public DocumentResource(Classifier classifier, Category category, Document document) {
         this.classifier = classifier;
         this.category = category;
         this.document = document;
     }
 
     /**
      * Retrieves information of a category
      * 
      * @param category_id
      * @return
      */
     @GET
     public DocumentDTO get() {
         String classifier_id = this.classifier.getId();
         DocumentDTO document_dto = new DocumentDTO(classifier_id, this.document.getId());
         document_dto.category = new CategoryDTO(classifier_id, this.category.getId());
         document_dto.setTokens(this.document.getTokensOccurrences());
         return document_dto;
     }
 
     /**
      * Updates a document
      * 
      * @return
      */
     @PUT
     @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public DocumentDTO put(DocumentDTO request) {
        if (request == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

         if (request.category != null && request.category.id != null) {
             if (!request.category.id.matches("^[a-zA-Z0-9_-]+$")) {
                 throw new WebApplicationException(Response.Status.BAD_REQUEST);
             }
             document.setCategoryId(request.category.id);
         }
 
         Map<String, Integer> tokens;
         try {
             tokens = this.classifier.extractTokens(request.text, request.html, request.getTokens());
         } catch (Exception e) {
             Log.getInstance().add(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
         if (tokens.isEmpty()) {
             throw new WebApplicationException(Response.Status.BAD_REQUEST);
         }
         try {
             document.setTokensOccurrences(tokens);
             this.classifier.saveDocument(document);
         } catch (Exception e) {
             Log.getInstance().add(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 
         return this.get();
     }
 
     /**
      * Updates a document
      * 
      * @return
      */
     @PUT
     @Consumes(MediaType.TEXT_PLAIN)
     public DocumentDTO putText(String text) {
         Map<String, Integer> tokens;
         try {
             tokens = this.classifier.extractTokens(text, null, null);
         } catch (Exception e) {
             Log.getInstance().add(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
         if (tokens.isEmpty()) {
             throw new WebApplicationException(Response.Status.BAD_REQUEST);
         }
         try {
             this.document.setTokensOccurrences(tokens);
             this.classifier.saveDocument(this.document);
         } catch (Exception e) {
             Log.getInstance().add(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
         return this.get();
     }
 
     /**
      * Deletes document
      * 
      * @return
      */
     @DELETE
     public Response delete() {
         try {
             this.classifier.deleteDocumentById(this.document.getId());
         } catch (Exception e) {
             Log.getInstance().add(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
         return Response.ok().build();
     }
 }
