 package at.ac.tuwien.sepm.service.impl;
 
 import at.ac.tuwien.sepm.dao.LvaDao;
 import at.ac.tuwien.sepm.entity.LVA;
 import at.ac.tuwien.sepm.service.LVAService;
 import at.ac.tuwien.sepm.service.PropertyService;
 import at.ac.tuwien.sepm.service.Semester;
 import at.ac.tuwien.sepm.service.ServiceException;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.stereotype.Service;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * @author Lena Lenz
  */
 @Service
 public class LVAServiceImpl implements LVAService {
 
     Logger logger = LogManager.getLogger(this.getClass().getSimpleName());
 
     @Autowired
     private PropertyService propertyService;
 
     @Autowired
     LvaDao lvaDao;
 
     @Override
     public boolean create(LVA toCreate) throws ServiceException, ValidationException {
         if(toCreate==null){
             throw new ServiceException("must not be null");
         }
         try {
             this.validateLVA(toCreate);
             boolean created = lvaDao.create(toCreate);
             return created;
         } catch(ServiceException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ValidationException("Exception: "+ e.getMessage());
         } catch(DataAccessException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         } catch(IOException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 
     @Override
     public LVA readById(int id) throws ServiceException, ValidationException {
         try {
             this.validateID(id);
             LVA lva = lvaDao.readById(id);
             return lva;
         } catch(ServiceException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ValidationException("Exception: "+ e.getMessage());
         } catch(DataAccessException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 
     @Override
     public List<LVA> readByMetaLva(int metaLvaId) throws ServiceException, ValidationException {
         try {
             this.validateID(metaLvaId);
             List<LVA> lvaList = lvaDao.readByMetaLva(metaLvaId);
             return lvaList;
         } catch(ServiceException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ValidationException("Exception: "+ e.getMessage());
         } catch(DataAccessException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 
     @Override
     public List<LVA> readUncompletedByYearSemesterStudyProgress(int year, Semester semester, boolean isInStudyProgress) throws ServiceException, ValidationException {
         if(semester==null){
             throw new ServiceException("semester must not be null");
         }
         if(year < 0 || semester == null) {
             logger.error("invalid parameters!");
             throw new ValidationException("invalid parameters!");
         }
         try {
             List<LVA> lvaList = lvaDao.readUncompletedByYearSemesterStudyProgress(year, semester, isInStudyProgress);
             return lvaList;
         } catch(DataAccessException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 
     @Override
     public LVA readByIdWithoutLvaDates(int id) throws ServiceException, ValidationException {
         try {
             this.validateID(id);
             LVA lva = lvaDao.readByIdWithoutLvaDates(id);
             return lva;
         } catch(ServiceException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ValidationException("Exception: "+ e.getMessage());
         } catch(DataAccessException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 
     @Override
     public List<LVA> readByYearAndSemester(int year, boolean isWinterSemester) throws ServiceException, ValidationException {
         if(year < 0 ) {
             logger.error("invalid parameter!");
             throw new ValidationException("invalid parameter!");
         }
         try {
             List<LVA> lvaList = lvaDao.readByYearAndSemester(year, isWinterSemester);
             return lvaList;
         } catch(DataAccessException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 
     @Override
     public boolean update(LVA toUpdate) throws ServiceException, ValidationException {
         if(toUpdate==null){
             throw new ServiceException("toUpdate must not be null");
         }
         try {
             this.validateID(toUpdate.getId());
             this.validateLVA(toUpdate);
             boolean updated = lvaDao.update(toUpdate);
             return updated;
         } catch(ServiceException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ValidationException("Exception: "+ e.getMessage());
         } catch(DataAccessException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         } catch(IOException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 
     @Override
     public void validateLVA(LVA toValidate) throws ServiceException {
         if(toValidate==null){
             throw new ServiceException("toValidate must not be null");
         }
         String error_msg = "";
         boolean valid = true;
 
         if(toValidate.getMetaLVA() == null) {
             valid = false;
             error_msg += "invalid metaLVA!\n";
         }
         if(toValidate.getDescription() == null ) {
             valid = false;
             error_msg += "invalid description!\n";
         }
         if(toValidate.getSemester() == null) {
             valid = false;
             error_msg += "invalid semester!(null)\n";
         }
         if(toValidate.getYear() < 0) {
             valid = false;
             error_msg += "invalid year!(< 0)\n";
         }
         if(valid == false) {
             logger.error("Invalid LVA: "+ error_msg);
             throw new ServiceException("Invalid LVA: "+ error_msg);
         }
     }
 
     @Override
     public void validateID(int id) throws ServiceException {
         if(id < 0) {
             logger.error("invalid id!");
             throw new ServiceException("invalid id!");
         }
     }
 
     @Override
     public List<LVA> readAll() throws ServiceException, ValidationException {
         try {
             return lvaDao.readAll();
         } catch(DataAccessException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 
     @Override
     public boolean isFirstSemesterAWinterSemester() throws ServiceException {
        if (propertyService.getProperty("user.firstSemester") != null) {
             return propertyService.getProperty("user.firstSemester") == "WS";
         } else {
             logger.error("Erstes Semester Property File wurde nicht gefunden.");
             throw new ServiceException("Erstes Semester Property File wurde nicht gefunden.");
         }
     }
 
     @Override
     public int firstYear() throws ServiceException {
        if (propertyService.getProperty("user.firstYear") != null) {
             return Integer.parseInt(propertyService.getProperty("user.firstYear"));
         } else {
             logger.error("Erstes Jahr Property File wurde nicht gefunden.");
             throw new ServiceException("Erstes Jahr Property File wurde nicht gefunden.");
         }
     }
 
     @Override
     public int numberOfSemestersInStudyProgress() throws ServiceException {
         try {
             int beginning = Integer.parseInt(propertyService.getProperty("user.firstYear"));
             LVA temp = null;
             for (LVA l : readAll()) {
                 if (temp == null)
                     temp = l;
                 if(l.isInStudyProgress()) {
                     if (l.getYear() >= temp.getYear())
                         temp = l;
                 }
             }
             int x = temp.getYear()-beginning;
             return x*2;
         } catch (ValidationException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 }
