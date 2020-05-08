 package de.hswt.hrm.inspection.service;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.di.annotations.Creatable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.common.exception.NotImplementedException;
 import de.hswt.hrm.inspection.dao.core.IBiologicalRatingDao;
 import de.hswt.hrm.inspection.dao.core.IInspectionDao;
 import de.hswt.hrm.inspection.dao.core.IPerformanceDao;
 import de.hswt.hrm.inspection.dao.core.IPhysicalRatingDao;
 import de.hswt.hrm.inspection.model.BiologicalRating;
 import de.hswt.hrm.inspection.model.Inspection;
 import de.hswt.hrm.inspection.model.Performance;
 import de.hswt.hrm.inspection.model.PhysicalRating;
 import de.hswt.hrm.photo.dao.core.IPhotoDao;
 import de.hswt.hrm.photo.model.Photo;
 import de.hswt.hrm.scheme.model.Scheme;
 
 @Creatable
 public class InspectionService {
     private static final Logger LOG = LoggerFactory.getLogger(InspectionService.class);
 
     private final IInspectionDao inspectionDao;
     private final IPhysicalRatingDao physicalDao;
     private final IBiologicalRatingDao biologicalDao;
     private final IPerformanceDao performanceDao;
     private final IPhotoDao photoDao;
 
     @Inject
     public InspectionService(IInspectionDao inspectionDao, IPhysicalRatingDao physicalDao,
     		IBiologicalRatingDao biologicalDao, IPerformanceDao performanceDao,
     		IPhotoDao photoDao) {
         checkNotNull(inspectionDao, "InspectionDao not properly injected.");
         checkNotNull(physicalDao, "PhysicalRatingDao not properly injected.");
         checkNotNull(biologicalDao, "BiologicalRatingDao not properly injected.");
         checkNotNull(performanceDao, "PerformanceDao not properly injected.");
         checkNotNull(photoDao, "PhotoDao not properly injected.");
     	
     	this.inspectionDao = inspectionDao;
     	this.physicalDao = physicalDao;
     	this.biologicalDao = biologicalDao;
     	this.performanceDao = performanceDao;
     	this.photoDao = photoDao;
 
     	// TODO Add log outputs
         if (inspectionDao == null) {
             LOG.error("ContactDao not injected to ContactService.");
         }
     }
     
     public Collection<Inspection> findAll() throws DatabaseException{
         return inspectionDao.findAll();
     }
     
     public Inspection insert (Inspection inspection) throws SaveException{
     	return inspectionDao.insert(inspection);
     }
     
     public Inspection findById(int id) throws ElementNotFoundException, DatabaseException {
     	return inspectionDao.findById(id);
     }
     
     public Collection<BiologicalRating> findBiologicalRating(Inspection inspection) 
     		throws DatabaseException {
     	
     	return biologicalDao.findByInspection(inspection);
     }
     
    public Collection<BiologicalRating> saveBiologicalRatings(
             Collection<BiologicalRating> ratings)
         throws SaveException, DatabaseException {
         
         checkNotNull(ratings, "List of ratings must not be null.");
         checkState(ratings.size() > 0, "List of ratings must at least have one rating.");
         
         // FIXME: better use a DAO method that allows to store the hole collection, as
         // otherwise there is a connection opened per row
         Collection<BiologicalRating> inserted = new ArrayList<>(ratings.size());
         for (BiologicalRating rating : ratings) {
        	if (rating.getId() < 0) {
	            inserted.add(biologicalDao.insert(rating));
	        }
        	else {
        		biologicalDao.update(rating);
        		inserted.add(rating);
        	}
         }
         
         return inserted;
     }
     
     public Collection<PhysicalRating> findPhysicalRating(Inspection inspection)
     		throws DatabaseException {
     	
     	return physicalDao.findByInspection(inspection);
     }
     
     public Collection<PhysicalRating> insertPhysicalRatings(Collection<PhysicalRating> ratings)
             throws SaveException, DatabaseException {
         
         checkNotNull(ratings, "List of ratings must not be null.");
         checkState(ratings.size() > 0, "List of ratings must at least have one rating.");
         
         // FIXME: better use a DAO method that allows to store the hole collection, as
         // otherwise there is a connection opened per row
         Collection<PhysicalRating> inserted = new ArrayList<>(ratings.size());
         for (PhysicalRating rating : ratings) {
             inserted.add(physicalDao.insert(rating));
         }
         
         return inserted;
     }
     
     public void update(PhysicalRating rating) 
             throws ElementNotFoundException, SaveException, DatabaseException {
         checkNotNull(rating, "PhysicalRating must not be null.");
         checkState(rating.getId() >= 0, "PhysicalRating must have a valid ID.");
         
         physicalDao.update(rating);
     }
     
     public Collection<Performance> findPerformance(Inspection inspection) 
     		throws DatabaseException {
  
     	return performanceDao.findByInspection(inspection);
     }
     
     public Collection<Performance> inserPerformanceRatings(Collection<Performance> ratings) 
             throws SaveException, DatabaseException {
         
         checkNotNull(ratings, "List of ratings must not be null.");
         checkState(ratings.size() > 0, "List of ratings must at least have one rating.");
         
         // FIXME: better use a DAO method that allows to store the hole collection, as
         // otherwise there is a connection opened per row
         Collection<Performance> inserted = new ArrayList<>(ratings.size());
         for (Performance rating : ratings) {
             inserted.add(performanceDao.insert(rating));
         }
         
         return inserted;
     }
     
     public void update(Performance rating) 
             throws ElementNotFoundException, SaveException, DatabaseException {
         
         checkNotNull(rating, "PerformanceRating must not be null.");
         checkState(rating.getId() >= 0, "PerformanceRating must have a valid ID.");
         
         performanceDao.update(rating);
     }
 
     public Scheme findScheme(Inspection inspection) throws DatabaseException {
     	return inspectionDao.findScheme(inspection);
     }
     
     public Collection<Photo> findPhoto(Performance performance)
     		throws DatabaseException {
 
     	checkNotNull(performance, "Performance must not be null.");
     	checkState(performance.getId() >= 0, "Performance must have a valid ID.");
     	return photoDao.findByPerformance(performance.getId());
     }
     
     public void addPhoto(Performance performance, Photo photo) 
     		throws SaveException, DatabaseException {
     	
     	checkNotNull(performance, "Performance must not be null.");
     	checkState(performance.getId() >= 0, "Performance must have a valid ID.");
     	checkNotNull(photo, "Photo must not be null.");
     	checkState(photo.getId() >= 0, "Photo must have a valid ID.");
     	
     	photoDao.addPhoto(performance.getId(), photo);
     }
     
     public void removePhoto(Performance performance, Photo photo) 
     		throws ElementNotFoundException, DatabaseException {
     	
     	checkNotNull(performance, "Performance must not be null.");
     	checkState(performance.getId() >= 0, "Performance must have a valid ID.");
     	checkNotNull(photo, "Photo must not be null.");
     	checkState(photo.getId() >= 0, "Photo must have a valid ID.");
     	
     	photoDao.removePhoto(performance.getId(), photo);
     }
     
 }
