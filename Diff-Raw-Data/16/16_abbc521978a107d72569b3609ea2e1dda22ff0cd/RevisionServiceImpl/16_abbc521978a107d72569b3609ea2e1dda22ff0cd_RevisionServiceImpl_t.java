 package cz.muni.fi.pa165.pujcovnastroju.serviceimpl;
 
import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;

 import cz.muni.fi.pa165.pujcovnastroju.converter.RevisionDTOConverter;
 import cz.muni.fi.pa165.pujcovnastroju.dao.RevisionDAO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.RevisionDTO;
 import cz.muni.fi.pa165.pujcovnastroju.entity.Revision;
 import cz.muni.fi.pa165.pujcovnastroju.service.RevisionService;
 
 /**
  *
  * @author Matej fucek
  */
 public class RevisionServiceImpl implements RevisionService {
 
     @Autowired
     private RevisionDAO rDAO;
 
     @Override
     public RevisionDTO createBizRevision(RevisionDTO rdto) {
         RevisionDTO dto = null;
         Revision revision = null;
         try {
             revision = rDAO.create(RevisionDTOConverter.dtoToEntity(rdto));
         } catch (IllegalArgumentException e) {
             throw new DataAccessResourceFailureException(
                     "Error occured during storing Revision", e);
         }
         return dto;
 
     }
 
     @Override
     public RevisionDTO updateBizRevision(RevisionDTO revisionDTO) {
         RevisionDTO dto = null;
         Revision revision = null;
         try {
             revision = rDAO.update(RevisionDTOConverter.dtoToEntity(revisionDTO));
         } catch (IllegalArgumentException e) {
             throw new DataAccessResourceFailureException(
                     "Error occured during updating Revision", e);
         }
         return dto;
     }
 
     @Override
     public void deleteBizRevision(RevisionDTO revisionDTO) {
         try {
             rDAO.delete(RevisionDTOConverter.dtoToEntity(revisionDTO));
         } catch (IllegalArgumentException e) {
             throw new DataAccessResourceFailureException(
                     "Error occured during deleting Revision", e);
         }
     }
 
     @Override
     public List<RevisionDTO> findAllrevisionsBizRevision() {
         List<Revision> revisionlist = rDAO.findAllrevisions();
         return RevisionDTOConverter.listToDto(revisionlist);
     }
 
     @Override
     public RevisionDTO readBizRevision(Long revID) {
         RevisionDTO dto = null;
         Revision revision = null;
         try {
             revision = rDAO.read(revID);
             dto = RevisionDTOConverter.entityToDTO(revision);
         } catch (IllegalArgumentException e) {
             throw new DataAccessResourceFailureException(
                     "Error occured during retrieving Revision", e);
         }
         return dto;
 
     }
 
     @Override
     public List<RevisionDTO> findRevisionsByDateBizRevision(Date date, Date date1) {
        List<Revision> revisionList = rDAO.findRevisionsByDate(date, date1);   
     return RevisionDTOConverter.listToDto(revisionList);
 
     }
 }
