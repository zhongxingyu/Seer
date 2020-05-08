 package at.ac.tuwien.sepm.service.impl;
 
 import at.ac.tuwien.sepm.dao.LvaDateDao;
 import at.ac.tuwien.sepm.entity.LvaDate;
 import at.ac.tuwien.sepm.entity.LvaDateType;
 import at.ac.tuwien.sepm.service.LvaDateService;
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
 public class LvaDateServiceImpl implements LvaDateService {
 
     @Autowired
     LvaDateDao lvaDateDao;
 
     Logger logger = LogManager.getLogger(this.getClass().getSimpleName());
 
     @Override
     public boolean create(LvaDate toCreate) throws ServiceException, ValidationException {
         try {
             this.validateLvaDate(toCreate);
             boolean created = lvaDateDao.create(toCreate);
             return created;
         } catch(ServiceException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ValidationException("Exception: "+ e.getMessage());
         } catch(IOException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ValidationException("Exception: "+ e.getMessage());
         } catch(DataAccessException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 
     @Override
     public LvaDate readById(int id) throws ServiceException, ValidationException {
         try {
             this.validateID(id);
             LvaDate lvaDate = lvaDateDao.readById(id);
             return lvaDate;
         } catch(ServiceException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ValidationException("Exception: "+ e.getMessage());
         } catch(DataAccessException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 
     @Override
     public List<LvaDate> readByLva(int lvaId) throws ServiceException, ValidationException {
         try {
             this.validateID(lvaId);
             List<LvaDate> lvaDateList = lvaDateDao.readByLva(lvaId);
             return lvaDateList;
         } catch(ServiceException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ValidationException("Exception: "+ e.getMessage());
         } catch(DataAccessException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 
     @Override
     public List<LvaDate> readByLvaAndType(int lvaId, LvaDateType type) throws ServiceException, ValidationException {
         if(type == null) {
             logger.error("invalid lvaDateType!");
             throw new ValidationException("invalid lvaDateType!");
         }
         try {
             this.validateID(lvaId);
             List<LvaDate> lvaDateList = lvaDateDao.readByLvaAndType(lvaId, type);
             return lvaDateList;
         } catch(ServiceException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ValidationException("Exception: "+ e.getMessage());
         } catch(DataAccessException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 
     @Override
     public boolean update(LvaDate toUpdate) throws ServiceException, ValidationException {
         try {
             this.validateID(toUpdate.getId());
             this.validateLvaDate(toUpdate);
             boolean updated = lvaDateDao.update(toUpdate);
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
     public boolean delete(int id) throws ServiceException, ValidationException {
         try {
             this.validateID(id);
             boolean deleted = lvaDateDao.delete(id);
             return deleted;
         } catch(ServiceException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ValidationException("Exception: "+ e.getMessage());
         } catch(DataAccessException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 
     @Override
     public List<LvaDate> getAllDeadlines() throws ServiceException{
         List<LvaDate> deadlineList = null;
         try {
             deadlineList = lvaDateDao.getAllDeadlines();
             return deadlineList;
         } catch(DataAccessException e) {
             logger.error("Exception: "+ e.getMessage());
             throw new ServiceException("Exception: "+ e.getMessage());
         }
     }
 
     @Override
     public void validateLvaDate(LvaDate toValidate) throws ServiceException {
         String error_msg = "";
         boolean valid = true;
 
        if(toValidate.getLva() < 0) {
             valid = false;
             error_msg += "invalid LVA!\n";
         }
         if(toValidate.getName() == null ) {
             valid = false;
             error_msg += "invalid name!\n";
         }
         if(toValidate.getDescription() == null) {
             valid = false;
             error_msg += "invalid description(null)!\n";
         }
         if(toValidate.getType() == null) {
             valid = false;
             error_msg += "invalid lvaDateType!\n";
         }
         if(toValidate.getStart() == null || toValidate.getStop() == null || toValidate.getStart().isAfter(toValidate.getStop())) {
             valid = false;
             error_msg += "invalid time!\n";
         }
         if(!valid) {
             logger.error("Invalid LvaDate: "+ error_msg);
             throw new ServiceException("Invalid LvaDate: "+ error_msg);
         }
     }
 
     @Override
     public void validateID(int id) throws ServiceException {
         if(id <= 0) {
             logger.error("invalid id!");
             throw new ServiceException("invalid id!");
         }
     }
 }
