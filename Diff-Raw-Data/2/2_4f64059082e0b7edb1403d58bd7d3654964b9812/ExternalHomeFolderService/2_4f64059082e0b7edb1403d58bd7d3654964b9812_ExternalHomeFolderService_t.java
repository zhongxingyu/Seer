 package fr.cg95.cvq.service.users.external.impl;
 
 import java.util.List;
 import java.util.UUID;
 
 import fr.cg95.cvq.business.payment.external.ExternalApplication;
 import fr.cg95.cvq.business.payment.external.ExternalHomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.external.HomeFolderMapping;
 import fr.cg95.cvq.business.users.external.IndividualMapping;
 import fr.cg95.cvq.dao.jpa.IGenericDAO;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.users.external.IExternalHomeFolderService;
 
 public class ExternalHomeFolderService implements IExternalHomeFolderService {
 
     private IGenericDAO genericDAO;
 
     @Override
     public void createHomeFolderMapping(HomeFolderMapping homeFolderMapping) {
         genericDAO.create(homeFolderMapping);
         // Flush session to ensure every user of this home folder mapping will be able to retrieve it
         // in the current session
         HibernateUtil.getSession().flush();
     }
 
     @Override
     public void modifyHomeFolderMapping(HomeFolderMapping homeFolderMapping) {
         genericDAO.update(homeFolderMapping);
         // Flush session to ensure every user of this home folder mapping will be able to retrieve it
         // in the current session
         HibernateUtil.getSession().flush();
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.EXTERNAL_SERVICE},
             privilege = ContextPrivilege.READ)
     public HomeFolderMapping getHomeFolderMapping(String externalServiceLabel, Long homeFolderId) {
         return genericDAO.simpleSelect(HomeFolderMapping.class)
                 .and("externalServiceLabel", externalServiceLabel)
                 .and("homeFolderId", homeFolderId).unique();
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.EXTERNAL_SERVICE, ContextType.ADMIN},
             privilege = ContextPrivilege.READ)
     public HomeFolderMapping getHomeFolderMapping(
             String externalServiceLabel, String externalCapDematId) {
         return genericDAO.simpleSelect(HomeFolderMapping.class)
                 .and("externalServiceLabel", externalServiceLabel)
                 .and("externalCapDematId", externalCapDematId).unique();
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
     public IndividualMapping getIndividualMapping(HomeFolderMapping homeFolderMapping, Long individualId) {
         return genericDAO.simpleSelect(IndividualMapping.class)
                 .and("homeFolderMapping", homeFolderMapping)
                 .and("individualId", individualId).unique();
     }
 
     @Override
    @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.ADMIN}, privilege = ContextPrivilege.READ)
     public List<HomeFolderMapping> getHomeFolderMappings(Long homeFolderId) {
         return genericDAO.simpleSelect(HomeFolderMapping.class)
                 .and("homeFolderId", homeFolderId).list();
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public List<IndividualMapping> getIndividualMappings(Long individualId) {
         return genericDAO.simpleSelect(IndividualMapping.class)
                 .and("individualId", individualId).list();
     }
 
     @Override
     public IndividualMapping getIndividualMapping(Individual individual, String externalServiceLabel) {
         HomeFolderMapping homeFolderMapping = getHomeFolderMapping(externalServiceLabel, individual.getHomeFolder().getId());
         return genericDAO.simpleSelect(IndividualMapping.class)
                 .and("individualId", individual.getId())
                 .and("homeFolderMapping", homeFolderMapping).unique();
     }
 
     @Override
     @Context(types = {ContextType.AGENT, ContextType.EXTERNAL_SERVICE}, privilege = ContextPrivilege.WRITE)
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
         modifyHomeFolderMapping(mapping);
     }
 
     @Override
     @Context(types = {ContextType.AGENT, ContextType.EXTERNAL_SERVICE}, privilege = ContextPrivilege.WRITE)
     public void addHomeFolderMapping(String externalServiceLabel, Long homeFolderId,
             String externalId) {
 
         HomeFolderMapping esim =
             getHomeFolderMapping(externalServiceLabel, homeFolderId);
         if (esim == null) {
             esim = new HomeFolderMapping();
             esim.setExternalServiceLabel(externalServiceLabel);
             esim.setHomeFolderId(homeFolderId);
             esim.setExternalCapDematId(UUID.randomUUID().toString());
             esim.setExternalId(externalId);
             createHomeFolderMapping(esim);
         } else {
             esim.setExternalId(externalId);
             modifyHomeFolderMapping(esim);
         }
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
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT, ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void deleteIndividualMapping(final HomeFolderMapping homeFolderMapping, final Long individualId) {
         IndividualMapping im = getIndividualMapping(homeFolderMapping, individualId);
         genericDAO.delete(im);
     }
 
     public void setGenericDAO(IGenericDAO genericDAO) {
         this.genericDAO = genericDAO;
     }
 
 }
