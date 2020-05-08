 package org.jboss.pressgang.ccms.server.rest.v1;
 
 import javax.enterprise.context.ApplicationScoped;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.model.ImageFile;
 import org.jboss.pressgang.ccms.model.LanguageImage;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTImageCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTLanguageImageCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTImageCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTLanguageImageCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTImageV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTLanguageImageV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
 import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectCollectionFactory;
 import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectFactory;
 import org.jboss.pressgang.ccms.server.utils.EnversUtilities;
 import org.jboss.resteasy.spi.BadRequestException;
 
 @ApplicationScoped
 public class ImageV1Factory extends RESTDataObjectFactory<RESTImageV1, ImageFile, RESTImageCollectionV1, RESTImageCollectionItemV1> {
     @Override
     public RESTImageV1 createRESTEntityFromDBEntityInternal(final ImageFile entity, final String baseUrl, final String dataType,
             final ExpandDataTrunk expand, final Number revision, final boolean expandParentReferences) {
         assert entity != null : "Parameter entity can not be null";
         assert baseUrl != null : "Parameter baseUrl can not be null";
 
         final RESTImageV1 retValue = new RESTImageV1();
 
         final List<String> expandOptions = new ArrayList<String>();
         expandOptions.add(RESTImageV1.LANGUAGEIMAGES_NAME);
         expandOptions.add(RESTBaseEntityV1.LOG_DETAILS_NAME);
         if (revision == null) {
             expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);
         }
         retValue.setExpand(expandOptions);
 
         retValue.setId(entity.getImageFileId());
         retValue.setDescription(entity.getDescription());
 
         // REVISIONS
         if (revision == null && expand != null && expand.contains(RESTBaseEntityV1.REVISIONS_NAME)) {
             retValue.setRevisions(RESTDataObjectCollectionFactory.create(RESTImageCollectionV1.class, new ImageV1Factory(), entity,
                     EnversUtilities.getRevisions(entityManager, entity), RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl,
                     entityManager));
         }
 
         // LANGUAGE IMAGES
         if (expand != null && expand.contains(RESTImageV1.LANGUAGEIMAGES_NAME)) {
             retValue.setLanguageImages_OTM(
                     RESTDataObjectCollectionFactory.create(RESTLanguageImageCollectionV1.class, new LanguageImageV1Factory(),
                             entity.getLanguageImagesArray(), RESTImageV1.LANGUAGEIMAGES_NAME, dataType, expand, baseUrl, false,
                             entityManager));
         }
 
         retValue.setLinks(baseUrl, RESTv1Constants.IMAGE_URL_NAME, dataType, retValue.getId());
 
         return retValue;
     }
 
     @Override
     public void syncDBEntityWithRESTEntity(final ImageFile entity, final RESTImageV1 dataObject) {
         if (dataObject.hasParameterSet(RESTImageV1.DESCRIPTION_NAME)) entity.setDescription(dataObject.getDescription());
 
         /* One To Many - Add will create a child entity */
         if (dataObject.hasParameterSet(
                 RESTImageV1.LANGUAGEIMAGES_NAME) && dataObject.getLanguageImages_OTM() != null && dataObject.getLanguageImages_OTM()
                 .getItems() != null) {
             dataObject.getLanguageImages_OTM().removeInvalidChangeItemRequests();
 
             for (final RESTLanguageImageCollectionItemV1 restEntityItem : dataObject.getLanguageImages_OTM().getItems()) {
                 final RESTLanguageImageV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem() || restEntityItem.returnIsRemoveItem()) {
                     if (restEntityItem.returnIsAddItem()) {
                        final LanguageImage dbEntity = new LanguageImage();
                         dbEntity.setImageFile(entity);
                        new LanguageImageV1Factory().syncDBEntityWithRESTEntity(dbEntity, restEntity);
                         entity.getLanguageImages().add(dbEntity);
                     } else if (restEntityItem.returnIsRemoveItem()) {
                         final LanguageImage dbEntity = entityManager.find(LanguageImage.class, restEntity.getId());
                         if (dbEntity == null)
                             throw new BadRequestException("No LanguageImage entity was found with the primary key " + restEntity.getId());
 
                         entity.getLanguageImages().remove(dbEntity);
                         entityManager.remove(dbEntity);
                     }
                 } else if (restEntityItem.returnIsUpdateItem()) {
                     final LanguageImage dbEntity = entityManager.find(LanguageImage.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No LanguageImage entity was found with the primary key " + restEntity.getId());
                     if (!entity.getLanguageImages().contains(dbEntity)) throw new BadRequestException(
                             "No LanguageImage entity was found with the primary key " + restEntity.getId() + " for Image " + entity.getId
                                     ());
 
                    new LanguageImageV1Factory().syncDBEntityWithRESTEntity(dbEntity, restEntity);
                 }
             }
         }
     }
 
     @Override
     protected Class<ImageFile> getDatabaseClass() {
         return ImageFile.class;
     }
 }
