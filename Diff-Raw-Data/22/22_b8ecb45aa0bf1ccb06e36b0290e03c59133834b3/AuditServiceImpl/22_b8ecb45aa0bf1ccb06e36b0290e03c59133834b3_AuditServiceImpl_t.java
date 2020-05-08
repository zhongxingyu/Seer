 package com.orangeleap.tangerine.service.impl;
 
 import java.lang.reflect.Field;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.beans.PropertyAccessorUtils;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.orangeleap.tangerine.dao.AuditDao;
 import com.orangeleap.tangerine.dao.ConstituentDao;
 import com.orangeleap.tangerine.domain.AbstractEntity;
 import com.orangeleap.tangerine.domain.Audit;
 import com.orangeleap.tangerine.domain.Auditable;
 import com.orangeleap.tangerine.domain.Person;
 import com.orangeleap.tangerine.domain.annotation.NotAuditable;
 import com.orangeleap.tangerine.domain.customization.CustomField;
 import com.orangeleap.tangerine.domain.customization.FieldDefinition;
 import com.orangeleap.tangerine.domain.paymentInfo.Commitment;
 import com.orangeleap.tangerine.service.AuditService;
 import com.orangeleap.tangerine.service.RelationshipService;
 import com.orangeleap.tangerine.service.relationship.RelationshipUtil;
 import com.orangeleap.tangerine.type.AuditType;
 import com.orangeleap.tangerine.util.TangerineUserHelper;
 
 @Service("auditService")
 @Transactional(propagation = Propagation.REQUIRED)
 public class AuditServiceImpl extends AbstractTangerineService implements AuditService {
 
     private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Resource(name = "relationshipService")
     private RelationshipService relationshipService;
     
     @Resource(name = "constituentDAO")
     private ConstituentDao constituentDao;
 
     @Resource(name = "auditDAO")
     private AuditDao auditDao;
     
     @Resource(name="tangerineUserHelper")
     protected TangerineUserHelper tangerineUserHelper;
 
     @Override
     public List<Audit> auditObject(Object object) {
         return auditObject(object, tangerineUserHelper.lookupUserId());
     }
 
     @Override
     public List<Audit> auditObject(Object object, Person constituent) {
         return auditObject(object, constituent.getId());
     }
 
     @Override
     public List<Audit> auditObject(Object object, Long userId) {
         List<Audit> audits = null;
         if (object instanceof AbstractEntity) {
             if (logger.isDebugEnabled()) {
                 logger.debug("auditObject: audit AbstractEntity");
             }
             audits = auditEntity((AbstractEntity) object, userId);
         } 
         else if (object instanceof Auditable) {
             if (logger.isDebugEnabled()) {
                 logger.debug("auditObject: audit Auditable");
             }
             audits = auditAuditable((Auditable) object, userId);
         } 
         else {
             if (logger.isInfoEnabled()) {
                 logger.info("auditObject: don't know how to audit object " + (object == null ? null : object.getClass().getName()));
             }
         }
 
         if (audits != null && !audits.isEmpty()) {
             for (Audit audit : audits) {
                 auditDao.auditObject(audit);
             }
         }
         return audits;
     }
 
     private List<Audit> auditEntity(AbstractEntity entity, Long userId) {
         if (logger.isDebugEnabled()) {
             logger.debug("auditEntity: userId = " + userId + " entity = " + entity);
         }
     	String siteName = tangerineUserHelper.lookupUserSiteName();
         List<Audit> audits = new ArrayList<Audit>();
         Date date = new Date();
         BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(entity);
         if (entity.getFieldValueMap() == null || entity.getFieldValueMap().get("id") == null) {
             String name = null;
             if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
                 name = SecurityContextHolder.getContext().getAuthentication().getName();
             }
             audits.add(new Audit(AuditType.CREATE, name, date, "Added " + getClassName(entity) + " " + entity.getId(), siteName, getClassName(entity), entity.getId(), userId));
             if (logger.isDebugEnabled()) {
                 logger.debug("audit Site " + siteName + ": added " + getClassName(entity) + " " + entity.getId());
             }
         } 
         else {
             Map<String, String> fieldLabels = entity.getFieldLabelMap();
             for (String key : fieldLabels.keySet()) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("key = " + key);
                 }
                 Object originalBeanProperty = entity.getFieldValueMap().get(key);
                 String fieldName = key;
                 if (bean.isReadableProperty(fieldName) && this.isAuditable(bean, fieldName)) {
                     Object beanProperty = bean.getPropertyValue(fieldName);
                     if (beanProperty instanceof CustomField) {
                         fieldName = key + ".value";
                         beanProperty = bean.getPropertyValue(fieldName);
                         if (entity instanceof Person) {
                         	FieldDefinition fd = ((Person)entity).getFieldTypeMap().get(key);
                         	if (relationshipService.isRelationship(fd)) {
                                 if (originalBeanProperty != null) {
                                     originalBeanProperty = dereference(siteName, originalBeanProperty.toString());
                                 }
                                 if (beanProperty != null) {
                                     beanProperty = dereference(siteName, beanProperty.toString());
                                 }
                         	}
                         }
                     }
                     if (beanProperty instanceof String) {
                         beanProperty = StringUtils.trimToNull((String) beanProperty);
                     } 
                     else if (beanProperty instanceof Person) {
                         fieldName = key + ".displayValue";
                         beanProperty = bean.getPropertyValue(fieldName);
                     }
                     if (originalBeanProperty == null && beanProperty == null) {
                         continue;
                     } 
                     else if (originalBeanProperty == null && beanProperty != null) {
                         audits.add(new Audit(AuditType.UPDATE, SecurityContextHolder.getContext().getAuthentication().getName(), date, "Id " + entity.getId() + ": Add " + fieldLabels.get(key) + " " + beanProperty.toString(), 
                                 siteName, getClassName(entity), entity.getId(), userId));
                         if (logger.isDebugEnabled()) {
                             logger.debug("audit site " + siteName + ", id " + entity.getId() + ": added " + fieldLabels.get(key) + " " + beanProperty.toString());
                         }
                     } 
                     else if (originalBeanProperty != null && beanProperty == null) {
                         audits.add(new Audit(AuditType.UPDATE, SecurityContextHolder.getContext().getAuthentication().getName(), date, "Id " + entity.getId() + ": Delete " + fieldLabels.get(key) + " " + originalBeanProperty.toString(), 
                                 siteName, getClassName(entity), entity.getId(), userId));
                         if (logger.isDebugEnabled()) {
                             logger.debug("audit site " + siteName + ", id " + entity.getId() + ": delete " + fieldLabels.get(key) + " " + originalBeanProperty.toString());
                         }
                     } 
                     else if ( !(beanProperty instanceof AbstractEntity) && !originalBeanProperty.toString().equals(beanProperty.toString())) {
 
                         // if the properties are dates, run them through the formatter and compare the
                         // output first, which will stop some dumb audit events
                         if (beanProperty instanceof java.util.Date) {
                             String newDate = null;
                             String oldDate = null;
                             synchronized(dateFormat) {
                                 newDate = dateFormat.format( (Date) beanProperty);
                                 oldDate = dateFormat.format( (Date) originalBeanProperty);
                             }
 
                             if (newDate.equals(oldDate)) {
                                 continue;
                             }
                         }
 
                         audits.add(new Audit(AuditType.UPDATE, SecurityContextHolder.getContext().getAuthentication().getName(), date, "Id " + entity.getId() + ": Change " + fieldLabels.get(key) + " from " + originalBeanProperty.toString() + " to " + beanProperty.toString(), 
                                 siteName, getClassName(entity), entity.getId(), userId));
                         if (logger.isDebugEnabled()) {
                             logger.debug("audit site " + siteName + ", id " + entity.getId() + ": change field " + fieldLabels.get(key) + " from " + originalBeanProperty.toString() + " to " + beanProperty.toString());
                         }
                     }
                 }
             }
         }
         return audits;
     }
     
     @SuppressWarnings("unchecked")
     protected boolean isAuditable(BeanWrapper bean, String fieldName) {
         if (logger.isDebugEnabled()) {
             logger.debug("isAuditable: bean = " + bean + " fieldName = " + fieldName);
         }
         boolean auditable = true;
         try {
             Class clazz = bean.getWrappedClass(); 
             if (PropertyAccessorUtils.isNestedOrIndexedProperty(fieldName)) {
                 int lastNestedClassIndex = PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(fieldName);
                 if (lastNestedClassIndex > -1) {
                     String nestedPath = fieldName.substring(0, lastNestedClassIndex);
                     if (bean.isReadableProperty(nestedPath)) {
                         clazz = bean.getPropertyValue(nestedPath).getClass();
                         fieldName = fieldName.substring(lastNestedClassIndex + 1);
                     }
                 }
             }
             
             Field field = clazz.getDeclaredField(fieldName);
             if (field.isAnnotationPresent(NotAuditable.class)) {
                 auditable = field.getAnnotation(NotAuditable.class).auditValue();
             }
         }
         catch (Exception ex) {
             if (logger.isInfoEnabled()) {
                 logger.info("isAuditable: Not able to determine if fieldName = " + fieldName + " is auditable, assuming it is");
             }
         }
         return auditable;
     }
     
     // Get Person name from id list.
     private String dereference(String siteName, String fieldValue) {
         if (logger.isDebugEnabled()) {
             logger.debug("dereference: siteName = " + siteName + " fieldValue = " + fieldValue);
         }
        String names = null;
        if (org.springframework.util.StringUtils.hasText(fieldValue)) {
        	List<Long> list = RelationshipUtil.getIds(fieldValue);
        	if (list != null && list.isEmpty() == false) {
        		List<Person> constituents = constituentDao.readConstituentsByIds(list);
            	List<String> displayValues = new ArrayList<String>();
            	// first name last name, without commas
            	for (Person constituent : constituents) {
                    displayValues.add(constituent.getFullName());
                } 
            	names = StringUtils.join(displayValues, ", ");
        	}
        }
    	return names;
     }
 
     private List<Audit> auditAuditable(Auditable auditable, Long userId) {
         if (logger.isDebugEnabled()) {
             logger.debug("auditAuditable: userId = " + userId + " auditable = " + auditable);
         }
     	String siteName = tangerineUserHelper.lookupUserSiteName();
     	String authName = SecurityContextHolder.getContext().getAuthentication().getName();
     	
         List<Audit> audits = new ArrayList<Audit>();
         Date date = new Date();
         Auditable originalObject = auditable.getOriginalObject();
         if (originalObject == null || originalObject.getId() == null) {
             String name = null;
             if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
                 name = SecurityContextHolder.getContext().getAuthentication().getName();
             }
             if (logger.isDebugEnabled()) {
                 logger.debug("audit Site " + siteName + ": added " + getClassName(auditable) + " " + auditable.getId());
             }
             audits.add(new Audit(AuditType.CREATE, name, date, "Added " + getClassName(auditable) + " " + auditable.getId(), siteName, getClassName(auditable), auditable.getId(), userId));
         } 
         else {
             BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(auditable);
             Field[] fields = auditable.getClass().getDeclaredFields();
             if (fields != null && fields.length > 0) {
                 BeanWrapper oldBean = PropertyAccessorFactory.forBeanPropertyAccess(auditable.getOriginalObject());
                 for (int i = 0; i < fields.length; i++) {
                     Field field = fields[i];
                     String fieldName = field.getName();
                     Object newFieldValue = null;
                     if (bean.isReadableProperty(fieldName)) {
                         newFieldValue = bean.getPropertyValue(fieldName);
                     }
                     Object oldFieldValue = null;
                     if (oldBean.isReadableProperty(fieldName)) {
                         oldFieldValue = oldBean.getPropertyValue(fieldName);
                     }
                     if (newFieldValue == null && oldFieldValue == null) {
                         continue;
                     } 
                     else if (newFieldValue == null && oldFieldValue != null) {
                         if (logger.isDebugEnabled()) {
                             logger.debug("audit site " + siteName + ", id " + auditable.getId() + ": added " + fieldName + " " + newFieldValue);
                         }
                         audits.add(new Audit(AuditType.UPDATE, authName, date, "Id " + auditable.getId() + ": Add " + fieldName + " " + newFieldValue, 
                                 siteName, getClassName(auditable), auditable.getId(), userId));
                     } 
                     else if (newFieldValue != null && oldFieldValue == null) {
                         if (logger.isDebugEnabled()) {
                             logger.debug("audit site " + siteName + ", id " + auditable.getId() + ": delete " + fieldName + " " + oldFieldValue);
                         }
                         audits.add(new Audit(AuditType.UPDATE, authName, date, "Id " + auditable.getId() + ": Delete " + fieldName + " " + oldFieldValue, 
                                 siteName, getClassName(auditable), auditable.getId(), userId));
                     } 
                     else if (!newFieldValue.toString().equals(oldFieldValue.toString())) {
                         if (logger.isDebugEnabled()) {
                             logger.debug("audit site " + siteName + ", id " + auditable.getId() + ": change field " + fieldName + " from " + oldFieldValue.toString() + " to " + newFieldValue.toString());
                         }
                         audits.add(new Audit(AuditType.UPDATE, authName, date, "Id " + auditable.getId() + ": Change " + fieldName + " from " + oldFieldValue + " to " + newFieldValue, 
                                 siteName, getClassName(auditable), auditable.getId(), userId));
                     }
                 }
             }
         }
         return audits;
     }
 
     @Override
     public List<Audit> allAuditHistoryForSite() {
         if (logger.isDebugEnabled()) {
             logger.debug("allAuditHistoryForSite:");
         }
         return auditDao.allAuditHistoryForSite();
     }
 
     @Override
     public List<Audit> auditHistoryForEntity(String entityTypeDisplay, Long objectId) {
         if (logger.isDebugEnabled()) {
             logger.debug("auditHistoryForEntity: entityTypeDisplay = " + entityTypeDisplay + " objectId = " + objectId);
         }
         return auditDao.auditHistoryForEntity(entityTypeDisplay, objectId);
     }
 
     @Override
     public List<Audit> auditHistoryForConstituent(Long constituentId) {
         if (logger.isDebugEnabled()) {
             logger.debug("auditHistoryForConstituent: constituentId = " + constituentId);
         }
         return auditDao.auditHistoryForConstituent(constituentId);
     }
 
     @Override
     public Audit auditObjectInactive(Object object) {
         return auditObjectInactive(object, tangerineUserHelper.lookupUserId());
     }
 
     @Override
     public Audit auditObjectInactive(Object object, Person constituent) {
         return auditObjectInactive(object, constituent.getId());
     }
     
     @Override
     public Audit auditObjectInactive(Object object, Long userId) {
         if (logger.isDebugEnabled()) {
             logger.debug("auditObjectInactive: object = " + object);
         }
     	String siteName = tangerineUserHelper.lookupUserSiteName();
         Audit audit = null;
         Date date = new Date();
         if (object instanceof AbstractEntity) {
             AbstractEntity entity = (AbstractEntity) object;
             String user = SecurityContextHolder.getContext().getAuthentication() == null ? "" : SecurityContextHolder.getContext().getAuthentication().getName();
             audit = new Audit(AuditType.UPDATE, user, date, "Inactivated " + getClassName(entity) + " " + entity.getId(), siteName, getClassName(entity), entity.getId(), userId);
         } 
         else if (object instanceof Auditable) {
             Auditable auditable = (Auditable) object;
             String user = SecurityContextHolder.getContext().getAuthentication() == null ? "" : SecurityContextHolder.getContext().getAuthentication().getName();
             audit = new Audit(AuditType.UPDATE, user, date, "Inactivated " + getClassName(auditable) + " " + auditable.getId(), siteName, getClassName(auditable), auditable.getId(), userId);
         } 
         else {
             if (logger.isInfoEnabled()) {
                 logger.info("don't know how to audit object " + (object == null ? null : object.getClass().getName()));
             }
         }
 
         if (audit != null) {
             auditDao.auditObject(audit);
         }
         return audit;
     }
 
     private String getClassName(Object object) {
         String name = object.getClass().getSimpleName();
         if (object instanceof Commitment) {
             name = StringUtils.lowerCase(((Commitment) object).getCommitmentType().getDisplayName());
         } 
         else {
             name = StringUtils.lowerCase(name);
         }
         return name;
     }
 }
