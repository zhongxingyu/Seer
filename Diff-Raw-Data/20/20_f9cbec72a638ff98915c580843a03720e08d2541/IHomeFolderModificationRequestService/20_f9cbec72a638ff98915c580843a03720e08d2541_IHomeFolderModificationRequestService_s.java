 package fr.cg95.cvq.service.request.ecitizen;
 
 import java.util.List;
 import java.util.Set;
 
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.request.ecitizen.HomeFolderModificationRequest;
 import fr.cg95.cvq.business.users.Address;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.CreationBean;
 import fr.cg95.cvq.business.users.HistoryEntry;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.service.request.IRequestService;
 
 /**
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public interface IHomeFolderModificationRequestService extends IRequestService {
 
     public final String SERVICE_NAME = "homeFolderModificationRequestService";
 
     /**
      * Create an home folder modification request in DB.
      *
      * @param homeFolderId the {@link HomeFolder home folder}'s id
      * @param requesterId the {@link Adult adult} who issued the request
      * @return the newly created home folder modification request
      */
     HomeFolderModificationRequest create(final Long homeFolderId, final Long requesterId)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Persists changes associated to an home folder modification request.
      *
      * @param hfmr the request with the original home folder containing
      *             the original individual and adress
      * @param adults the modified other home folder adults
      * @param children the modified home folder children
      * @param adress the modified home folder adress
      * @return a creation bean (in case there had an home folder responsible
      *         change, we have to return the generated login for the new
      *         home folder responsible) or null if not needed
      */
     @Deprecated
     CreationBean modify(final HomeFolderModificationRequest hfmr,
             final List<Adult> adults, final List<Child> children, final Address adress)
         throws CvqException;
     
     CreationBean modify(final HomeFolderModificationRequest hfmr,
             final List<Adult> adults, final List<Child> children, List<Adult> foreignRoleOwners, 
             final Address adress, List<Document> documents)
         throws CvqException;
     
     /**
      * Return all history entries associated to a given request.
      */
     Set<HistoryEntry> getHistoryEntries(final Long hfmrId)
         throws CvqException;
 }
