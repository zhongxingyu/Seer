 package com.mpower.service.impl;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.persistence.Column;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.BeanWrapperImpl;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.beans.PropertyAccessorUtils;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.mpower.dao.AuditDao;
 import com.mpower.dao.PersonDao;
 import com.mpower.dao.SiteDao;
 import com.mpower.domain.Audit;
 import com.mpower.domain.Auditable;
 import com.mpower.domain.Commitment;
 import com.mpower.domain.CustomField;
 import com.mpower.domain.Person;
 import com.mpower.domain.Viewable;
 import com.mpower.domain.annotation.NotAuditable;
 import com.mpower.domain.customization.FieldDefinition;
 import com.mpower.service.AuditService;
 import com.mpower.service.relationship.RelationshipUtil;
 import com.mpower.type.AuditType;
 
 @Service("auditService")
 @Transactional(propagation = Propagation.REQUIRED)
 public class AuditServiceImpl implements AuditService {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Resource(name = "personDao")
     private PersonDao personDao;
 
     @Resource(name = "auditDao")
     private AuditDao auditDao;
 
     @Resource(name = "siteDao")
     private SiteDao siteDao;
 
     public List<Audit> auditObject(Object object) {
         List<Audit> audits = null;
         if (object instanceof Viewable) {
             if (logger.isDebugEnabled()) {
                 logger.debug("audit Viewable");
             }
             audits = auditViewable((Viewable) object);
         } else if (object instanceof Auditable) {
             if (logger.isDebugEnabled()) {
                 logger.debug("audit Auditable");
             }
             audits = auditAuditable((Auditable) object);
         } else {
             if (logger.isDebugEnabled()) {
                 logger.debug("don't know how to audit object " + (object == null ? null : object.getClass().getName()));
             }
         }
 
         if (audits != null && !audits.isEmpty()) {
             for (Audit audit : audits) {
                 auditDao.auditObject(audit);
             }
         }
         return audits;
     }
 
     private List<Audit> auditViewable(Viewable viewable) {
         List<Audit> audits = new ArrayList<Audit>();
         Date date = new Date();
         BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(viewable);
         if (viewable.getFieldValueMap() == null || viewable.getFieldValueMap().get("id") == null) {
             String name = null;
             if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
                 name = SecurityContextHolder.getContext().getAuthentication().getName();
             }
             audits.add(new Audit(AuditType.CREATE, name, date, "Added " + getClassName(viewable) + " " + viewable.getId(), viewable.getSite(), getClassName(viewable), viewable.getId(), viewable.getPerson()));
             if (logger.isDebugEnabled()) {
                 logger.debug("audit Site " + viewable.getSite().getName() + ": added " + getClassName(viewable) + " " + viewable.getId());
             }
         } 
         else {
             if (viewable instanceof Viewable) {
                 Map<String, String> fieldLabels = viewable.getFieldLabelMap();
                 for (String key : fieldLabels.keySet()) {
                     if (logger.isDebugEnabled()) {
                         logger.debug("key = " + key);
                     }
                     Object originalBeanProperty = viewable.getFieldValueMap().get(key);
                     String fieldName = key;
                     if (bean.isReadableProperty(fieldName) && this.isAuditable(bean, fieldName)) {
                         Object beanProperty = bean.getPropertyValue(fieldName);
                         if (beanProperty instanceof CustomField) {
                             fieldName = key + ".value";
                             beanProperty = bean.getPropertyValue(fieldName);
                             if (viewable instanceof Person) {
                             	FieldDefinition fd = ((Person)viewable).getFieldTypeMap().get(key);
                             	String siteName = viewable.getSite().getName();
                             	if (fd.isRelationship(siteName)) {
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
                             audits.add(new Audit(AuditType.UPDATE, SecurityContextHolder.getContext().getAuthentication().getName(), date, "Id " + viewable.getId() + ": Add " + fieldLabels.get(key) + " " + beanProperty.toString(), viewable.getSite(), getClassName(viewable), viewable.getId(), viewable
                                     .getPerson()));
                             if (logger.isDebugEnabled()) {
                                 logger.debug("audit site " + viewable.getSite().getName() + ", id " + viewable.getId() + ": added " + fieldLabels.get(key) + " " + beanProperty.toString());
                             }
                         } 
                         else if (originalBeanProperty != null && beanProperty == null) {
                             audits.add(new Audit(AuditType.UPDATE, SecurityContextHolder.getContext().getAuthentication().getName(), date, "Id " + viewable.getId() + ": Delete " + fieldLabels.get(key) + " " + originalBeanProperty.toString(), viewable.getSite(), getClassName(viewable), viewable.getId(),
                                     viewable.getPerson()));
                             if (logger.isDebugEnabled()) {
                                 logger.debug("audit site " + viewable.getSite().getName() + ", id " + viewable.getId() + ": delete " + fieldLabels.get(key) + " " + originalBeanProperty.toString());
                             }
                         } 
                        else if (!originalBeanProperty.toString().equals(beanProperty.toString())) {
                             audits.add(new Audit(AuditType.UPDATE, SecurityContextHolder.getContext().getAuthentication().getName(), date, "Id " + viewable.getId() + ": Change " + fieldLabels.get(key) + " from " + originalBeanProperty.toString() + " to " + beanProperty.toString(), viewable.getSite(),
                                     getClassName(viewable), viewable.getId(), viewable.getPerson()));
                             if (logger.isDebugEnabled()) {
                                 logger.debug("audit site " + viewable.getSite().getName() + ", id " + viewable.getId() + ": change field " + fieldLabels.get(key) + " from " + originalBeanProperty.toString() + " to " + beanProperty.toString());
                             }
                         }
                     }
                 }
             }
         }
         return audits;
     }
     
     @SuppressWarnings("unchecked")
     protected boolean isAuditable(BeanWrapper bean, String fieldName) {
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
     	List<Long> list = RelationshipUtil.getIds(fieldValue);
 		List<Person> persons = personDao.readPersons(siteName, list);
     	List<String> displayValues = new ArrayList<String>();
     	// first name last name, without commas
     	for (Person person : persons) {
             displayValues.add(person.getFullName());
         } 
     	return StringUtils.join(displayValues, ", ");
     }
 
     private List<Audit> auditAuditable(Auditable auditable) {
         List<Audit> audits = new ArrayList<Audit>();
         Date date = new Date();
         Auditable originalObject = auditable.getOriginalObject();
         if (originalObject == null || originalObject.getId() == null) {
             String name = null;
             if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
                 name = SecurityContextHolder.getContext().getAuthentication().getName();
             }
             if (logger.isDebugEnabled()) {
                 logger.debug("audit Site " + auditable.getSite().getName() + ": added " + getClassName(auditable) + " " + auditable.getId());
             }
             audits.add(new Audit(AuditType.CREATE, name, date, "Added " + getClassName(auditable) + " " + auditable.getId(), auditable.getSite(), getClassName(auditable), auditable.getId(), auditable.getPerson()));
         } 
         else {
             BeanWrapperImpl bean = new BeanWrapperImpl(auditable);
             Field[] fields = auditable.getClass().getDeclaredFields();
             if (fields != null && fields.length > 0) {
                 BeanWrapperImpl oldBean = new BeanWrapperImpl(auditable.getOriginalObject());
                 for (int i = 0; i < fields.length; i++) {
                     Field field = fields[i];
                     if (field.isAnnotationPresent(Column.class)) {
                         String fieldName = field.getName();
                         Object newFieldValue = bean.getPropertyValue(fieldName);
                         Object oldFieldValue = oldBean.getPropertyValue(fieldName);
                         if (newFieldValue == null && oldFieldValue == null) {
                             continue;
                         } 
                         else if (newFieldValue == null && oldFieldValue != null) {
                             if (logger.isDebugEnabled()) {
                                 logger.debug("audit site " + auditable.getSite().getName() + ", id " + auditable.getId() + ": added " + fieldName + " " + newFieldValue);
                             }
                             audits.add(new Audit(AuditType.UPDATE, SecurityContextHolder.getContext().getAuthentication().getName(), date, "Id " + auditable.getId() + ": Add " + fieldName + " " + newFieldValue, auditable.getSite(), getClassName(auditable), auditable.getId(), auditable.getPerson()));
                         } 
                         else if (newFieldValue != null && oldFieldValue == null) {
                             if (logger.isDebugEnabled()) {
                                 logger.debug("audit site " + auditable.getSite().getName() + ", id " + auditable.getId() + ": delete " + fieldName + " " + oldFieldValue);
                             }
                             audits.add(new Audit(AuditType.UPDATE, SecurityContextHolder.getContext().getAuthentication().getName(), date, "Id " + auditable.getId() + ": Delete " + fieldName + " " + oldFieldValue, auditable.getSite(), getClassName(auditable), auditable.getId(), auditable
                                     .getPerson()));
                         } 
                         else if (!newFieldValue.toString().equals(oldFieldValue.toString())) {
                             if (logger.isDebugEnabled()) {
                                 logger.debug("audit site " + auditable.getSite().getName() + ", id " + auditable.getId() + ": change field " + fieldName + " from " + oldFieldValue.toString() + " to " + newFieldValue.toString());
                             }
                             audits.add(new Audit(AuditType.UPDATE, SecurityContextHolder.getContext().getAuthentication().getName(), date, "Id " + auditable.getId() + ": Change " + fieldName + " from " + oldFieldValue + " to " + newFieldValue, auditable.getSite(), getClassName(auditable), auditable
                                     .getId(), auditable.getPerson()));
                         }
                     }
                 }
             }
         }
         return audits;
     }
 
     @Override
     public List<Audit> allAuditHistoryForSite(String siteName) {
         return auditDao.allAuditHistoryForSite(siteDao.readSite(siteName));
     }
 
     @Override
     public List<Audit> auditHistoryForEntity(String siteName, String entityTypeDisplay, Long objectId) {
         return auditDao.auditHistoryForEntity(siteDao.readSite(siteName), entityTypeDisplay, objectId);
     }
 
     @Override
     public List<Audit> auditHistoryForPerson(Long personId) {
         return auditDao.auditHistoryForPerson(personId);
     }
 
     @Override
     public Audit auditObjectInactive(Object object) {
         Audit audit = null;
         Date date = new Date();
         if (object instanceof Viewable) {
             Viewable viewable = (Viewable) object;
             String user = SecurityContextHolder.getContext().getAuthentication() == null ? "" : SecurityContextHolder.getContext().getAuthentication().getName();
             audit = new Audit(AuditType.UPDATE, user, date, "Inactivated " + getClassName(viewable) + " " + viewable.getId(), viewable.getSite(), getClassName(viewable), viewable.getId(), viewable.getPerson());
         } else if (object instanceof Auditable) {
             Auditable auditable = (Auditable) object;
             String user = SecurityContextHolder.getContext().getAuthentication() == null ? "" : SecurityContextHolder.getContext().getAuthentication().getName();
             audit = new Audit(AuditType.UPDATE, user, date, "Inactivated " + getClassName(auditable) + " " + auditable.getId(), auditable.getSite(), getClassName(auditable), auditable.getId(), auditable.getPerson());
         } else {
             if (logger.isDebugEnabled()) {
                 logger.debug("don't know how to audit object " + (object == null ? null : object.getClass().getName()));
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
         } else {
             name = StringUtils.lowerCase(name);
         }
         return name;
     }
 }
