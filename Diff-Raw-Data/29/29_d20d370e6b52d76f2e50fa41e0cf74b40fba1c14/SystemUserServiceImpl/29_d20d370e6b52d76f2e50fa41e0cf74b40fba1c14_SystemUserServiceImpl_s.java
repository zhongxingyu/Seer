 package cz.muni.fi.pa165.pujcovnastroju.serviceimpl;
 
 import cz.muni.fi.pa165.pujcovnastroju.converter.SystemUserDTOConverter;
 import cz.muni.fi.pa165.pujcovnastroju.converter.UserTypeDTOConverter;
 import cz.muni.fi.pa165.pujcovnastroju.dao.SystemUserDAO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.SystemUserDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.UserTypeEnumDTO;
 import cz.muni.fi.pa165.pujcovnastroju.entity.SystemUser;
 import java.util.List;
 import org.springframework.dao.DataAccessResourceFailureException;
 
 /**
  *
  * @author Vojtech Schlemmer
  */
public class SystemUserServiceImpl {
     
     private SystemUserDAO userDao;
    
     public SystemUserDTO create(SystemUserDTO userDTO){
         SystemUser user = null;
         SystemUserDTO userDTO2 = null;
         try{
             user = userDao.create(SystemUserDTOConverter.dtoToEntity(userDTO));
             userDTO2 = SystemUserDTOConverter.entityToDTO(user);
         } catch (IllegalArgumentException e) {
             throw new DataAccessResourceFailureException(
                     "Error occured during storing Machine", e);
         }
         return userDTO2;
     }
     
     public SystemUserDTO read(Long id){
         SystemUser user = null;
         SystemUserDTO userDTO = null;
         try{
             user = userDao.read(id);
             userDTO = SystemUserDTOConverter.entityToDTO(user);
         } catch (IllegalArgumentException e) {
             throw new DataAccessResourceFailureException(
                     "Error occured during storing Machine", e);
         }
         return userDTO;
     }
     
     public SystemUserDTO update(SystemUserDTO userDTO){
         SystemUser user = null;
         SystemUserDTO userDTO2 = null;
         try{
             user = userDao.update(SystemUserDTOConverter.dtoToEntity(userDTO));
             userDTO2 = SystemUserDTOConverter.entityToDTO(user);
         } catch (IllegalArgumentException e) {
             throw new DataAccessResourceFailureException(
                     "Error occured during storing Machine", e);
         }
         return userDTO2;
     }
     
     public void delete(SystemUserDTO userDTO){
         try{
             userDao.delete(SystemUserDTOConverter.dtoToEntity(userDTO));
         } catch (IllegalArgumentException e) {
             throw new DataAccessResourceFailureException(
                     "Error occured during storing Machine", e);
         }
     }
     
     public List<SystemUserDTO> findAllSystemUsers(){
         List<SystemUser> userList = userDao.findAllSystemUsers();
         return SystemUserDTOConverter.listToDTO(userList);
     }
     
     public List<SystemUserDTO> getSystemUsersByParams(String firstName,
 			String lastName, UserTypeEnumDTO type){
         List<SystemUser> userList = userDao.getSystemUsersByParams(firstName, 
                 lastName, UserTypeDTOConverter.dtoToEntity(type));
         return SystemUserDTOConverter.listToDTO(userList);
     }
 }
