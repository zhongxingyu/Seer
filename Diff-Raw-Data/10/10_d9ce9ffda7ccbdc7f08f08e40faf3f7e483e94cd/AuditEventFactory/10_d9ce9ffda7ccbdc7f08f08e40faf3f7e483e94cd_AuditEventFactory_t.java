 package edu.northwestern.bioinformatics.studycalendar.service.auditing;
 
 import edu.northwestern.bioinformatics.studycalendar.core.editors.EditorUtils;
 import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
 import edu.northwestern.bioinformatics.studycalendar.domain.tools.DateFormat;
 import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEventValue;
 import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
 import gov.nih.nci.cabig.ctms.audit.domain.Operation;
 import gov.nih.nci.cabig.ctms.audit.exception.AuditSystemException;
 import gov.nih.nci.cabig.ctms.audit.util.AuditUtil;
 import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
 import gov.nih.nci.cabig.ctms.domain.DomainObject;
 import gov.nih.nci.cabig.ctms.lang.ComparisonTools;
 import org.hibernate.EntityMode;
 import org.hibernate.collection.PersistentCollection;
 import org.hibernate.engine.CollectionEntry;
 import org.hibernate.type.AbstractComponentType;
 import org.hibernate.type.CollectionType;
 import org.hibernate.type.Type;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * @author Jalpa Patel
  */
 @Transactional
 public class AuditEventFactory {
     private final Logger log = LoggerFactory.getLogger(getClass());
     private static final EntityMode ENTITY_MODE = EntityMode.POJO;
     private static final String HIBERNATE_BACK_REF_STRING = "Backref";
 
     private boolean canAudit(final Object entity) {
         if (entity instanceof AbstractMutableDomainObject) {
             if (((AbstractMutableDomainObject) entity).getId() != null) {
                return true;
             } else {
                 log.error("Entity {} doesn't have any Id.", new Object[] { entity.getClass().getName()});
                 return false;
             }
 
         } else {
             log.debug("No auditing for instances of " + entity.getClass().getName());
             return false;
         }
     }
 
     public AuditEvent createAuditEvent(final Object entity, final Operation operation) {
         DataAuditInfo info = (DataAuditInfo) gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo.getLocal();
         if (canAudit(entity)) {
             if (info == null) {
                 throw new AuditSystemException("Can not audit; no local audit info available");
             }
             if (log.isTraceEnabled()) {
                 String idS;
                 if (entity instanceof DomainObject) {
                     idS = ((DomainObject) entity).getId().toString();
                 } else {
                     idS = "<unknown>";
                 }
                 log.trace("Logging {} on {}#{} by {}", new Object[] { operation, entity.getClass().getName(), idS, info });
             }
             if (AuditEvent.getUserAction() != null) {
                log.debug("Attaching user action id {} to the audit event {} on {}",
                       new Object[] { AuditEvent.getUserAction().getGridId(), operation, entity.getClass().getName()});
             }
             AuditEvent event = new AuditEvent(entity, operation, DataAuditInfo.copy(info), AuditEvent.getUserAction());
             return event;
         }
         return null;
     }
 
     public void appendEventValues(AuditEvent event, Type propertyType, String propertyName,
                                    Object previousState, Object currentState) {
         if (ignoreCurrentStateForType(propertyType) || propertyName.indexOf(HIBERNATE_BACK_REF_STRING) > 0) {
             return;
         } else if (propertyType.isComponentType()) {
             addComponentValues(event, (AbstractComponentType) propertyType, propertyName, previousState, currentState);
         } else {
             if (ignoreStates(previousState, currentState)) {
                 return;
             } else {
                 String prevValue = scalarValue(previousState);
                 String curValue = scalarValue(currentState);
                 if (prevValue != null && curValue != null && prevValue.equals(curValue)) {
                     return;
                 }
                 event.addValue(new DataAuditEventValue(propertyName, prevValue, curValue));
             }
         }
     }
 
     private void addComponentValues(AuditEvent event, AbstractComponentType propertyType,
                                   String propertyName, Object previousState, Object currentState) {
         Object[] componentPrevState = getComponentState(propertyType, previousState);
         Object[] componentCurState = getComponentState(propertyType, currentState);
 
         if (ignoreStates(componentPrevState, componentCurState)) {
             return;
         } else {
             int index = componentCurState == null ? componentPrevState.length : componentCurState.length;
             for (int i=0; i<index; i++) {
                 String compPropertyName = propertyName.concat(".").concat(propertyType.getPropertyNames()[i]);
                 if (componentPrevState != null && componentCurState != null) {
                     if (!ComparisonTools.nullSafeEquals(componentCurState[i], componentPrevState[i])) {
                         event.addValue(new DataAuditEventValue(compPropertyName,
                             scalarValue(componentPrevState[i]),
                             scalarValue(componentCurState[i])));
                     }
                 } else {
                     event.addValue(new DataAuditEventValue(compPropertyName,
                         componentPrevState == null ? null : scalarValue(componentPrevState[i]),
                         componentCurState == null ? null : scalarValue(componentCurState[i])));
                 }
             }
         }
     }
 
     private Object[] getComponentState(AbstractComponentType propertyType, Object state) {
         return state == null ? null : propertyType.getPropertyValues(state,
                 ENTITY_MODE);
     }
 
     private boolean ignoreStates(Object prevState, Object curState) {
         return prevState == null && curState == null;
     }
 
     private boolean ignoreStates(Object[] componentPrevState, Object[] componentCurState) {
         return componentPrevState == null && componentCurState == null;
     }
 
     private boolean ignoreCurrentStateForType(Type type) {
         return type instanceof CollectionType;
     }
 
     private String scalarValue(Object propertyValue) {
         if (propertyValue == null) {
             return null;
         } else if (AuditUtil.getObjectId(propertyValue) != null) {
             Integer id = AuditUtil.getObjectId(propertyValue);
             return id.toString();
         } else {
             if (propertyValue instanceof Date) {
                 return DateFormat.getISO8601Format().format(propertyValue);
             } else {
                 return propertyValue.toString();
             }
         }
     }
 
     private Collection getNewCollectionContent(PersistentCollection newCollection) {
         if (newCollection == null) {
             return null;
         } else {
            return (Collection) newCollection;
         }
     }
 
     private Collection getOldCollectionContent(Serializable oldCollection) {
         if (oldCollection == null) {
             return null;
         } else if (oldCollection instanceof Map) {
             return ((Map) oldCollection).keySet();
         } else {
             return (Collection) oldCollection;
         }
     }
 
     private String scalarCollectionValue(Collection coll, Type elementType){
         Iterator it = coll.iterator();
         StringBuffer sb = new StringBuffer();
         String objValue;
         while (it.hasNext()) {
             Object obj  = it.next();
             if (elementType != null && elementType.isComponentType()) {
                 Object[] componentObj = getComponentState((AbstractComponentType) elementType, obj);
                 objValue = scalarCollectionValue(Arrays.asList(componentObj), null);
             }  else {
                 objValue = scalarValue(obj);
             }
             if (objValue != null) {
                 sb.append(EditorUtils.getEncodedString(objValue));
                 if (it.hasNext()) {
                     sb.append(EditorUtils.SEPARATOR);
                 }
             }
         }
 
         if(sb.length() == 0) {
             return null;
         } else {
             return sb.toString();
         }
     }
 
     public void appendCollectionEventValues(AuditEvent auditEvent, String propertyName, Serializable oldColl,
                                             PersistentCollection newColl, CollectionEntry collectionEntry) {
         Type elementType = collectionEntry.getLoadedPersister().getElementType();
         if (oldColl == null && newColl == null) {
              return;
         } else {
              Collection newCollection = getNewCollectionContent(newColl);
              Collection oldCollection = getOldCollectionContent(oldColl);
              auditEvent.addValue(new DataAuditEventValue(propertyName,
                            oldCollection == null ? null : scalarCollectionValue(oldCollection, elementType),
                            newCollection == null ? null : scalarCollectionValue(newCollection, elementType)));
 
         }
     }
 }
