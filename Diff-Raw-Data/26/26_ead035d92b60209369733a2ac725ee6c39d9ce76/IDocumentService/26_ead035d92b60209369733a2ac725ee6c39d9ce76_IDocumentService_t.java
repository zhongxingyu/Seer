 package fr.cg95.cvq.service.document;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.pdfbox.pdmodel.PDDocument;
 
 import com.lowagie.text.DocumentException;
 
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.document.DocumentBinary;
 import fr.cg95.cvq.business.document.DocumentState;
 import fr.cg95.cvq.business.document.DocumentType;
 import fr.cg95.cvq.exception.CvqDisabledFunctionalityException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.annotation.IsUser;
 import fr.cg95.cvq.service.document.annotation.IsDocument;
 
 /**
  * @author bor@zenexity.fr
  */
 public interface IDocumentService {
 
     /**
      * Add a document to the system.
      *
      * This step is only used to create the document's administrative information.
      * To add the document's binary data, use the {@link #addPage} method.
      *
      * @param document the document to add
      * @return the document's id
      */
     Long create(@IsDocument Document document)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Modify an existing document.
      */
     void modify(@IsDocument Document document)
         throws CvqException;
 
 	/**
      * Delete an existing document.
      */
     void delete(@IsDocument final Long id);
 
     Document getById(@IsDocument final Long documentId);
 
     /**
      * Add a page to an existing document.
      *
      * If no page is specified, add document binary at the last page
      */
     void addPage(@IsDocument final Long documentId, final byte[] data)
         throws CvqException;
 
     /**
      * Modify a page of an existing document.
      */
     void modifyPage(@IsDocument final Long documentId, final int dataIndex, final byte[] data)
         throws CvqException;
 
     /**
      * Remove a page from an existing document.
      */
     void deletePage(@IsDocument final Long documentId, final Integer pageId)
         throws CvqDisabledFunctionalityException;
 
     /**
      * Get all binary data associated to a document.
      */
     Set<DocumentBinary> getAllPages(@IsDocument final Long documentId);
 
     /**
      * Get already provided documents for the given
      *       {@link fr.cg95.cvq.business.document.DocumentType}.
      *
      * @param docType the document type to search for
      * @param homeFolderId the home folder for which we are searching
      * @param individualId an optional individual to restrict the search to
      */
     List<Document> getProvidedDocuments(final DocumentType docType, 
         @IsUser final Long homeFolderId, @IsUser final Long individualId)
         throws CvqException;
 
     /**
      * Get documents associated to an home folder.
      */
     List<Document> getHomeFolderDocuments(@IsUser final Long homeFolderId,
         int maxResults);
 
     /**
      * Get documents associated to an individual.
      */
     List<Document> getIndividualDocuments(@IsUser final Long individualId);
 
     /**
      * Check, for all known local authorities, that the end validity date of documents 
      * in state PENDING, CHECKED or VALIDATED has not been reached. If it has been reached,
      * the document state is set to OUTDATED.
      * 
      * This method is currently used by an internal job.
      */
     void checkDocumentsValidity() throws CvqException;
 
     /**
      * Dispatcher method for workflow document method.
      */
     void updateDocumentState(final Long id, final DocumentState ds, final String message, 
             final Date validityDate)
         throws CvqException, CvqInvalidTransitionException;
 
     /**
      * Set the previously draft document to pending state
      * @param id the ID of the document
      * @throws CvqObjectNotFoundException if the document is not found
      * @throws CvqInvalidTransitionException if the document is not in draft state
      */
     void pending(@IsDocument final Long id)
         throws CvqInvalidTransitionException;
 
     void rePending(Long id)
         throws CvqInvalidTransitionException;
     /**
      * Validate a document.
      *
      * @param id the document's id
      * @param validityDate the document's validity date. If none is provided, the
      *                     default value is used
      * @param message a optional message associated with the validation
      */
     void validate(@IsDocument final Long id, final Date validityDate, final String message) // FIXME message unused
         throws CvqException, CvqObjectNotFoundException,
                CvqInvalidTransitionException;
 
    void validate(@IsDocument final Long id, final String message) 
        throws CvqException, CvqObjectNotFoundException, CvqInvalidTransitionException;

     /**
      * Refuse the validation of a document.
      *
      * @param id the document's id
      * @param message a mandatory message associated with the refusal
      */
     void refuse(final Long id, final String message)
         throws CvqInvalidTransitionException;
 
     /**
      * Notify that the document has been checked.
      *
      * @param id the document's id
      * @param message a optional message (eg 'bring the original ones')
      */
     void check(final Long id, final String message)
         throws CvqInvalidTransitionException;
 
     /**
      * Get possible state transitions from the given document state.
      *
      * @return an array of {@link fr.cg95.cvq.business.document.DocumentState}
      *         objects
      * @see fr.cg95.cvq.business.document.DocumentState
      */
     DocumentState[] getPossibleTransitions(DocumentState rs)
         throws CvqException;
 
     /**
      * Get the states for which the document is still editable.
      */
     List<DocumentState> getEditableStates();
 
     /**
      * Provides ecitizen-oreinted document searching 
      * 
      * @param searchParams hash of parameters
      * @param max max rows to display
      * @param offset display offset
      * @return found documents
      */
     List<Document> search(Hashtable<String,Object> searchParams,int max,int offset);
 
     /**
      * Get a count of documents matching the given criteria.
      */
     Integer searchCount(Hashtable<String,Object> searchParams);
 
     void mergeDocumentBinary(@IsDocument Document document) throws CvqException;
     
     PDDocument byteToPDDocument(byte[] data) throws CvqException;
 
     void rotate(Long id, int index, boolean trigonometric)
         throws CvqException, DocumentException, IOException;
 }
