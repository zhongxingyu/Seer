 package fr.cg95.cvq.service.request;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.annotation.IsHomeFolder;
 import fr.cg95.cvq.service.request.annotation.IsRequest;
 import fr.cg95.cvq.util.Critere;
 
 public interface IRequestSearchService {
 
     /**
      * Get a constrained list of requests according to a set of criteria and requirements.
      *
      * @param criteriaSet a set of {@link Critere criteria} to be applied to the search
      * @param sort an ordering to apply to results. value is one of the SEARCH_* static
      *        string defined in this service (null to use default sort on requests ids)
      * @param dir the direction of the sort (asc or desc, asc by default)
      * @param recordsReturned the number of records to return (-1 to get all results)
      * @param startIndex the start index of the records to return
      * @param full get full request objects with their specific data
      */
     List<Request> get(Set<Critere> criteriaSet, final String sort, final String dir, 
         final int recordsReturned, final int startIndex, final boolean full)
         throws CvqException;
 
     /**
      * Get a count of requests matching the given criteria.
      */
     Long getCount(Set<Critere> criteriaSet)
         throws CvqException;
     
     /**
      * Get a request by id.
      */
     Request getById(@IsRequest final Long id, final boolean full)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get all requests belonging to the given home folder.
      */
     List<Request> getByHomeFolderId(@IsHomeFolder final Long homeFolderId, final boolean full)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get all requests of the given type belonging to the given home folder.
      */
     List<Request> getByHomeFolderIdAndRequestLabel(@IsHomeFolder final Long homeFolderId, 
         final String requestLabel, final boolean full)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get the generated certificate for the given request at the given step.
      */
     byte[] getCertificate(@IsRequest final Long requestId, final RequestState requestState)
         throws CvqException;
 
     /**
      * Get the most recent certificate for the given request.
     * 
     * it is :
     * <ul>
     *   <li>The one generated at validation time if it exists</li>
     *   <li>The one generated at creation time if it does not</li>
     * </ul>
      */
     byte[] getCertificate(@IsRequest final Long requestId)
         throws CvqException;
 
     File getArchives(List<String> names)
         throws IOException;
 
     /**
      * @return the names of the archives which could not be deleted
      */
     List<String> deleteArchives(List<String> names);
 }
