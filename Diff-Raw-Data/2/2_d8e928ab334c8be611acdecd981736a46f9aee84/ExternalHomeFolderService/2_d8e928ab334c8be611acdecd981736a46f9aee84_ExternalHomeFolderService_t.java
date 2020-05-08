 package fr.cg95.cvq.service.users.external.impl;
 
 import java.util.List;
 import java.util.UUID;
 
 import fr.cg95.cvq.business.payment.external.ExternalApplication;
 import fr.cg95.cvq.business.payment.external.ExternalHomeFolder;
 import fr.cg95.cvq.business.users.external.HomeFolderMapping;
 import fr.cg95.cvq.business.users.external.IndividualMapping;
 import fr.cg95.cvq.dao.IGenericDAO;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.users.external.IExternalHomeFolderService;
 
 public class ExternalHomeFolderService implements IExternalHomeFolderService {
 
     private IGenericDAO genericDAO;
 
     @Override
     public void createHomeFolderMapping(HomeFolderMapping homeFolderMapping)
             throws CvqModelException {
         genericDAO.create(homeFolderMapping);
     }
 
     @Override
     public void modifyHomeFolderMapping(HomeFolderMapping homeFolderMapping)
             throws CvqModelException {
         genericDAO.update(homeFolderMapping);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public HomeFolderMapping getHomeFolderMapping (String externalServiceLabel, Long homeFolderId) {
         return genericDAO.simpleSelect(HomeFolderMapping.class)
                 .and("externalServiceLabel", externalServiceLabel)
                 .and("homeFolderId", homeFolderId).unique();
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public HomeFolderMapping getHomeFolderMapping(
             String externalServiceLabel, String externalCapdematId) {
         return genericDAO.simpleSelect(HomeFolderMapping.class)
                 .and("externalServiceLabel", externalServiceLabel)
                .and("externalCapDematId", externalCapdematId).unique();
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public HomeFolderMapping getHomeFolderMapping(
             String externalServiceLabel, ExternalHomeFolder eh) {
         return genericDAO.simpleSelect(HomeFolderMapping.class)
                 .and("externalServiceLabel", externalServiceLabel)
                 .and("externalId", eh.getCompositeIdForMapping()).unique();
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public HomeFolderMapping getHomeFolderMapping(
             String externalServiceLabel, ExternalApplication externalApplication, String externalHomeFolderId) {
         ExternalHomeFolder eh = new ExternalHomeFolder(externalHomeFolderId, externalApplication, null);
         return getHomeFolderMapping(externalServiceLabel, eh);
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public List<HomeFolderMapping> getHomeFolderMappings(Long homeFolderId) {
         return genericDAO.simpleSelect(HomeFolderMapping.class)
                 .and("homeFolderId", homeFolderId).list();
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void setExternalId(String externalServiceLabel, Long homeFolderId, Long individualId, 
             String externalId) {
         HomeFolderMapping mapping = getHomeFolderMapping(externalServiceLabel, homeFolderId);
         IndividualMapping iMapping = genericDAO.simpleSelect(IndividualMapping.class)
                 .and("homeFolderMapping", mapping).and("individualId", individualId).unique();
         if (iMapping == null) {
             mapping.getIndividualsMappings().add(new IndividualMapping(individualId, externalId, mapping));
         } else {
             iMapping.setExternalId(externalId);
         }
         genericDAO.update(mapping);
     }
 
     @Override
     @Context(types = {ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void addHomeFolderMapping(String externalServiceLabel, Long homeFolderId,
             String externalId) {
 
         HomeFolderMapping esim =
             getHomeFolderMapping(externalServiceLabel, homeFolderId);
         if (esim == null) {
             esim = new HomeFolderMapping();
             esim.setExternalServiceLabel(externalServiceLabel);
             esim.setHomeFolderId(homeFolderId);
             esim.setExternalCapDematId(UUID.randomUUID().toString());
         }
 
         esim.setExternalId(externalId);
 
         genericDAO.create(esim);
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void deleteHomeFolderMapping(String externalServiceLabel, ExternalHomeFolder eh) {
         genericDAO.delete(getHomeFolderMapping(externalServiceLabel, eh));
     }
 
     @Override
     @Context(types = {ContextType.AGENT, ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void deleteHomeFolderMappings(final String externalServiceLabel, final Long homeFolderId) {
         HomeFolderMapping esim = getHomeFolderMapping(externalServiceLabel, homeFolderId);
         genericDAO.delete(esim);
     }
 
     public void setGenericDAO(IGenericDAO genericDAO) {
         this.genericDAO = genericDAO;
     }
 
 }
