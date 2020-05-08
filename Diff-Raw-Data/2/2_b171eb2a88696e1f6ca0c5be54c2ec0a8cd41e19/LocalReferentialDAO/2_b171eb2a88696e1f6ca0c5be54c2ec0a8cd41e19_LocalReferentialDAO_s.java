 package fr.cg95.cvq.dao.request.xml;
 
 import fr.cg95.cvq.business.authority.LocalAuthorityResource.Type;
 import fr.cg95.cvq.business.request.LocalReferentialType;
 import fr.cg95.cvq.dao.request.ILocalReferentialDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.request.IRequestServiceRegistry;
 import fr.cg95.cvq.service.request.LocalReferential;
 import fr.cg95.cvq.util.translation.ITranslationService;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import org.apache.commons.lang.WordUtils;
 import org.apache.log4j.Logger;
 
 /**
  * Implementation of ILocalReferentialDAO using XML files.
  * @author julien
  */
 public class LocalReferentialDAO implements ILocalReferentialDAO {
     
     private IRequestServiceRegistry requestServiceRegistry;
     private ILocalAuthorityRegistry localAuthorityRegistry;
     private ITranslationService translationService;
     private static Logger logger = Logger.getLogger(LocalReferentialDAO.class);
 
     public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
         this.localAuthorityRegistry = localAuthorityRegistry;
     }
 
     public void setRequestServiceRegistry(IRequestServiceRegistry requestServiceRegistry) {
         this.requestServiceRegistry = requestServiceRegistry;
     }
 
     public void setTranslationService(ITranslationService translationService) {
         this.translationService = translationService;
     }
     
 
     @Override
     public Set<LocalReferentialType> listByRequestType(final String requestTypeLabel) {
         File file = getOrCreateLocalReferentialFile(requestTypeLabel);
         if (file == null) {
             return null;
         }
         try {
             return LocalReferentialXml.xmlToModel(file);
         } catch (CvqException e) {
             return null;
         }
     }
 
     @Override
     public LocalReferentialType getByRequestTypeAndName(final String requestTypeLabel, final String typeName) {
         final Set<LocalReferentialType> lrts = listByRequestType(requestTypeLabel);
         if (lrts == null) {
             return null;
         }
         for (final LocalReferentialType lrt : lrts) {
             if (lrt.getName().equals(typeName)) {
                 return lrt;
             }
         }
         return null;
     }
 
     @Override
     public void save(final String requestTypeLabel, final LocalReferentialType lrt) throws CvqException {
         File file = getOrCreateLocalReferentialFile(requestTypeLabel);
         if (file != null) {
             LocalReferentialXml.modelToXml(lrt, file);
         }
     }
     
     /**
      * @param requestTypeLabel
      * @return The file containing the local refererential for the given request type, or null if it’s does not have one
      */
     private File getOrCreateLocalReferentialFile(final String requestTypeLabel) {
         final String fileName = requestServiceRegistry.getRequestService(requestTypeLabel).getLocalReferentialFilename();
         final File file = getLocalReferentialFile(fileName);
         if (file != null && file.exists()) {
             return file;
         } else {
             logger.debug("No local referential file found, creating an empty one.");
             if (!file.exists())
                 try {
                     file.createNewFile();
                 } catch (IOException e) {
                     logger.error("Failed to create the new local referential " + file.getName());
                     return null;
                 }
             final Set<LocalReferentialType> lrts = generateEmptyLocalReferential(requestTypeLabel);
             if (!lrts.isEmpty()) {
                 try {
                     LocalReferentialXml.modelToXml(lrts, file);
                     logger.debug("Local referential skeleton saved in file: "+ file.getAbsolutePath());
                     return file;
                 } catch (CvqException ex) {
                     logger.error("Failed to save the generated local referential");
                 }
             }
             logger.debug("No local referential for this request type");
             return null;
         }
     }
     
     /**
      * @param requestTypeLabel
      * @return The File associated to the given requestTypeLabel, or null if this request type does not have a local referential
      */
     private File getLocalReferentialFile(final String fileName) {
         if (fileName != null) {
             return localAuthorityRegistry.getLocalAuthorityResourceFile(Type.LOCAL_REFERENTIAL, fileName, false);
         }
         return null;
     }
     
     private Set<LocalReferentialType> generateEmptyLocalReferential(String requestTypeLabel) {
         final Set<LocalReferentialType> lrts = new LinkedHashSet<LocalReferentialType>();
         try {
             final Class<?> clazz = requestServiceRegistry.getRequestService(requestTypeLabel).getSkeletonRequest()
                     .getSpecificData().getClass();
 
             final String prefix = translationService.generateInitialism(requestTypeLabel);
             for (Field field : clazz.getDeclaredFields()) {
                 // logger.debug("inspecting field " + field.getName());
                 // HACK Due to Java’s type erasure we can’t retrieve local referentials fields (type List<LocalReferentialData>)
                 // So we look for fields of type List<?> annotated with the validation annotation @LocalReferential
                 if (field.getType().isAssignableFrom(List.class) && field.getAnnotation(LocalReferential.class) != null) {
                     // logger.debug("LocalReferentialFound: " + field.getName());
                     final LocalReferentialType lrt;
                     lrt = new LocalReferentialType();
                     lrt.setName(WordUtils.capitalize(field.getName()));
                     lrt.setLabel(translationService.translate(prefix+".property."+field.getName()+".label"));
                     lrts.add(lrt);
                 }
             }
         } catch (CvqException ex) {
             
         }
         
         return lrts;
     }
 }
