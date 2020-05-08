 package org.motechproject.care.reporting.service;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.collections.Transformer;
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.care.reporting.domain.SelfUpdatable;
 import org.motechproject.care.reporting.domain.dimension.*;
 import org.motechproject.care.reporting.domain.measure.Form;
 import org.motechproject.care.reporting.enums.CaseType;
 import org.motechproject.care.reporting.factory.FormFactory;
 import org.motechproject.care.reporting.mapper.CareReportingMapper;
 import org.motechproject.care.reporting.processors.CaseNotFoundException;
 import org.motechproject.care.reporting.repository.Repository;
 import org.motechproject.care.reporting.utils.ObjectUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.*;
 
 import static java.lang.String.format;
 import static org.motechproject.care.reporting.utils.AnnotationUtils.getExternalPrimaryKeyField;
 import static org.motechproject.care.reporting.utils.AnnotationUtils.getExternalPrimaryKeyValue;
 
 @Service
 @Transactional
 public class CareService implements org.motechproject.care.reporting.service.Service {
     private static final Logger logger = LoggerFactory.getLogger("commcare-reporting-mapper");
 
     private Repository dbRepository;
     private CareReportingMapper careReportingMapper;
 
     @Autowired
     public CareService(Repository dbRepository) {
         this.dbRepository = dbRepository;
         this.careReportingMapper = CareReportingMapper.getInstance(this);
     }
 
     @Override
     public <T> Integer save(T instance) {
         return dbRepository.save(instance);
     }
 
     @Override
     public <T> void update(T entity) {
         dbRepository.update(entity);
     }
 
     @Override
     public Object execute(String query) {
         return dbRepository.execute(query);
     }
 
 
     @Override
     public <T extends SelfUpdatable<T>> T saveByExternalPrimaryKey(Class<T> entityClass, Map<String, String> values) {
         T entity = careReportingMapper.map(entityClass, values);
         saveOrUpdateByExternalPrimaryKey(entity);
         return entity;
     }
 
     private <T extends SelfUpdatable<T>> void saveOrUpdateByExternalPrimaryKey(T entity) {
         T persistedObject = dbRepository.findByExternalPrimaryKey(((Class<T>) entity.getClass()), getExternalPrimaryKeyValue(entity));
         if (persistedObject == null)
             dbRepository.save(entity);
         else {
             persistedObject.updateToLatest(entity);
             dbRepository.save(persistedObject);
         }
     }
 
     @Override
     public <T extends SelfUpdatable<T>> void saveOrUpdateAllByExternalPrimaryKey(Class clazz, List<T> updatedEntities) {
         List<T> existingEntities = findAllByExternalPrimaryKey(clazz, updatedEntities);
         List<T> toBeSavedEntities = processToBeSavedEntities(updatedEntities, existingEntities);
         dbRepository.saveOrUpdateAll(toBeSavedEntities);
     }
 
     private <T extends SelfUpdatable> List<T> processToBeSavedEntities(List<T> updatedEntities, List<T> existingEntities) {
         List<T> toBeSavedEntities = new ArrayList<>();
         for (final T updatedEntity : updatedEntities) {
             T existing = (T) CollectionUtils.find(existingEntities, new Predicate() {
                 @Override
                 public boolean evaluate(Object object) {
                     return getExternalPrimaryKeyValue(object).equals(getExternalPrimaryKeyValue(updatedEntity));
                 }
             });
             if (existing != null) {
                 existing.updateToLatest(updatedEntity);
                 toBeSavedEntities.add(existing);
             } else {
                 toBeSavedEntities.add(updatedEntity);
             }
         }
         return toBeSavedEntities;
     }
 
     private <T> List<T> findAllByExternalPrimaryKey(Class clazz, List<T> entities) {
         List<String> externalPrimaryKeyValues = new ArrayList<>();
         CollectionUtils.collect(entities, new Transformer() {
             @Override
             public Object transform(Object input) {
                 return getExternalPrimaryKeyValue(input);
             }
         }, externalPrimaryKeyValues);
         return dbRepository.findAllByField(clazz, externalPrimaryKeyValues, getExternalPrimaryKeyField(clazz).getName());
     }
 
     @Override
     public FlwGroup getOrCreateGroup(String groupId) {
         return getOrCreateNew(FlwGroup.class, "groupId", groupId);
     }
 
     @Override
     public MotherCase getOrCreateMotherCase(String caseId) {
         return getOrCreateNew(MotherCase.class, "caseId", caseId);
     }
 
     @Override
     public MotherCase getMotherCase(String caseId) {
         return get(MotherCase.class, "caseId", caseId);
     }
 
     @Override
     public ChildCase getChildCase(String caseId) {
         return get(ChildCase.class, "caseId", caseId);
     }
 
     @Override
     public ChildCase getOrCreateChildCase(String caseId) {
         return getOrCreateNew(ChildCase.class, "caseId", caseId);
     }
 
     @Override
     public Flw getOrCreateFlw(String flwId) {
         return getOrCreateNew(Flw.class, "flwId", flwId);
     }
 
     @Override
     public LocationDimension getLocation(String state, String district, String block) {
 
         if(containsNullOrEmpty(state, district, block)) {
             return getUnknownLocation();
         }
 
          Map<String, Object> fieldMaps = getLocationMap(state, district, block);
         LocationDimension locationDimension = dbRepository.get(LocationDimension.class, fieldMaps, null);
 
         return locationDimension == null ? getUnknownLocation() : locationDimension;
     }
 
     private LocationDimension getUnknownLocation() {
         final String unknownLocation = "UNKNOWN";
         LocationDimension locationDimension;Map<String, Object> unknownMap = getLocationMap(unknownLocation, unknownLocation, unknownLocation);
         locationDimension = dbRepository.get(LocationDimension.class, unknownMap, null);
         return locationDimension;
     }
 
     private Map<String, Object> getLocationMap(final String state,final String district,final String block) {
         return new HashMap<String, Object>() {{
             put("state", StringUtils.upperCase(state));
             put("district", StringUtils.upperCase(district));
             put("block", StringUtils.upperCase(block));
         }};
     }
 
     private boolean containsNullOrEmpty(final String state,final String district,final String block) {
         return StringUtils.isEmpty(state) || StringUtils.isEmpty(district) || StringUtils.isEmpty(block);
     }
 
     @Override
     public <T> T getOrCreateNew(Class<T> type, String fieldName, String value) {
         T instance = dbRepository.get(type, fieldName, value);
         if (null != instance)
             return instance;
 
         T newInstance;
         try {
             newInstance = type.newInstance();
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
         ObjectUtils.set(newInstance, fieldName, value);
 
         return newInstance;
     }
 
     @Override
     public <T> T get(Class<T> type, String fieldName, Object value) {
         return dbRepository.get(type, fieldName, value);
     }
 
     @Override
     public <T> T get(Class<T> type, Map<String, Object> fieldMap, Map<String, String> aliasMapping) {
         return dbRepository.get(type, fieldMap, aliasMapping);
     }
 
     @Override
     public void processAndSaveForms(Map<String, String> motherFormValues, List<Map<String, String>> childFormValues) {
         if (motherFormValues != null)
             saveForm(CaseType.MOTHER, motherFormValues);
 
         for (Map<String, String> childFormValue : childFormValues) {
             saveForm(CaseType.CHILD, childFormValue);
         }
     }
 
     private void saveForm(CaseType caseType, Map<String, String> formValues) {
         String xmlns = formValues.get("xmlns");
         String instanceId = formValues.get("instanceId");
         Class<?> formClass = FormFactory.getForm(xmlns, caseType);
         if (formClass == null) {
             logger.warn(String.format("No form found for xmlns:%s, instanceId:%s", xmlns, instanceId));
             return;
         }
 
         final Form existingForm = getForm(caseType, formClass, instanceId, formValues.get("caseId"));
         final Form currentForm = (Form) careReportingMapper.map(formClass, formValues);
         if(existingForm == null) {
             dbRepository.save(currentForm);
         }
         else if (existingForm.getServerDateModified() != null && (currentForm.getServerDateModified() == null ||
                  currentForm.getServerDateModified().before(existingForm.getServerDateModified()))) {
             logger.warn(format("Cannot save form. Latest %s form with instance id %s already exists.", formClass.getName(), instanceId));
         }
         else {
             logger.info(format("Deleting existing %s form with instance id %s and saving a latest form.", formClass.getName(), instanceId));
             dbRepository.delete(existingForm);
             dbRepository.save(currentForm);
         }
     }
 
     private Form getForm(CaseType caseType, Class<?> type, String instanceId, String caseId) {
         Map<String, Object> fieldMap = new HashMap<>();
         fieldMap.put("instanceId", instanceId);
         Map<String, String> aliasMap = new HashMap<>();
         if (CaseType.CHILD.equals(caseType)) {
             fieldMap.put("cc.caseId", caseId);
             aliasMap.put("childCase", "cc");
         }
 
         return (Form) get(type, fieldMap, aliasMap);
     }
 
     @Override
     public void closeCase(String caseId, Map<String, String> updatedValues) {
         MotherCase motherCase = getMotherCase(caseId);
         Date closedOn = careReportingMapper.map(updatedValues.get("closedOn"), Date.class);
         if (motherCase != null) {
             Date previouslyClosedOnForMother = motherCase.getClosedOn();
             if (canBeClosed(closedOn, previouslyClosedOnForMother)) {
                 careReportingMapper.map(motherCase, updatedValues);
                 update(motherCase);
             }
         } else {
             ChildCase childCase = getChildCase(caseId);
             if (childCase == null) {
                 logger.error(format("[Case Not Found To Close] Cannot find case %s to close.", caseId));
                throw new CaseNotFoundException(caseId);
             }
             Date previouslyClosedOnForChild = childCase.getClosedOn();
             if (canBeClosed(closedOn, previouslyClosedOnForChild)) {
                 careReportingMapper.map(childCase, updatedValues);
                 update(childCase);
             }
         }
     }
 
     private boolean canBeClosed(Date closedOn, Date previouslyClosedOn) {
         return previouslyClosedOn == null || !closedOn.before(previouslyClosedOn);
     }
 }
