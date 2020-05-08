 package org.jboss.pressgang.ccms.server.rest.v1;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.model.LanguageImage;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTLanguageImageCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTLanguageImageCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTLanguageImageV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
 import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectCollectionFactory;
 import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectFactory;
 import org.jboss.pressgang.ccms.server.utils.EnversUtilities;
 
 @ApplicationScoped
 public class LanguageImageV1Factory extends RESTDataObjectFactory<RESTLanguageImageV1, LanguageImage, RESTLanguageImageCollectionV1,
         RESTLanguageImageCollectionItemV1> {
     @Inject
     protected ImageV1Factory imageFactory;
 
     @Override
     public RESTLanguageImageV1 createRESTEntityFromDBEntityInternal(final LanguageImage entity, final String baseUrl, final String dataType,
             final ExpandDataTrunk expand, final Number revision, final boolean expandParentReferences) {
         assert entity != null : "Parameter entity can not be null";
         assert baseUrl != null : "Parameter baseUrl can not be null";
 
         final RESTLanguageImageV1 retValue = new RESTLanguageImageV1();
 
         final List<String> expandOptions = new ArrayList<String>();
         expandOptions.add(RESTLanguageImageV1.IMAGEDATA_NAME);
         expandOptions.add(RESTLanguageImageV1.IMAGEDATABASE64_NAME);
        expandOptions.add(RESTLanguageImageV1.IMAGEDATABASE64_NAME);
         expandOptions.add(RESTBaseEntityV1.LOG_DETAILS_NAME);
 
         if (revision == null) expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);
 
         retValue.setExpand(expandOptions);
 
         retValue.setId(entity.getLanguageImageId());
         retValue.setLocale(entity.getLocale());
         retValue.setFilename(entity.getOriginalFileName());
 
         /* potentially large fields need to be expanded manually */
         if (expand != null && expand.contains(RESTLanguageImageV1.IMAGEDATA_NAME)) retValue.setImageData(entity.getImageData());
         if (expand != null && expand.contains(RESTLanguageImageV1.IMAGEDATABASE64_NAME))
             retValue.setImageDataBase64(entity.getImageDataBase64());
         if (expand != null && expand.contains(RESTLanguageImageV1.THUMBNAIL_NAME)) retValue.setThumbnail(entity.getThumbnailData());
 
         /* Set the object references */
         if (expandParentReferences && expand != null && expand.contains(RESTLanguageImageV1.IMAGE_NAME) && entity.getImageFile() != null) {
             retValue.setImage(imageFactory.createRESTEntityFromDBEntity(entity.getImageFile(), baseUrl, dataType,
                     expand.get(RESTLanguageImageV1.IMAGE_NAME)));
         }
 
         // REVISIONS
         if (revision == null && expand != null && expand.contains(RESTBaseEntityV1.REVISIONS_NAME)) {
             retValue.setRevisions(RESTDataObjectCollectionFactory.create(RESTLanguageImageCollectionV1.class, this, entity,
                     EnversUtilities.getRevisions(entityManager, entity), RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl,
                     entityManager));
         }
 
         return retValue;
     }
 
     @Override
     public void syncDBEntityWithRESTEntity(final LanguageImage entity, final RESTLanguageImageV1 dataObject) {
         if (dataObject.hasParameterSet(RESTLanguageImageV1.LOCALE_NAME)) entity.setLocale(dataObject.getLocale());
         if (dataObject.hasParameterSet(RESTLanguageImageV1.IMAGEDATA_NAME)) entity.setImageData(dataObject.getImageData());
         if (dataObject.hasParameterSet(RESTLanguageImageV1.FILENAME_NAME)) entity.setOriginalFileName(dataObject.getFilename());
     }
 
     @Override
     protected Class<LanguageImage> getDatabaseClass() {
         return LanguageImage.class;
     }
 
 }
