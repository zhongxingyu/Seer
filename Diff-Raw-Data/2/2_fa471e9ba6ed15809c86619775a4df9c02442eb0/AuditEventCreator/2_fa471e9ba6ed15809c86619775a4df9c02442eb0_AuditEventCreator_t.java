 package edu.northwestern.bioinformatics.studycalendar.dao.auditing;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
 import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEvent;
 import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
 import gov.nih.nci.cabig.ctms.audit.domain.Operation;
 import gov.nih.nci.cabig.ctms.audit.exception.AuditSystemException;
 import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
 import gov.nih.nci.cabig.ctms.domain.DomainObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.*;
 
 /**
  * @author Jalpa Patel
  */
 @Transactional
 public class AuditEventCreator {
     private final Logger log = LoggerFactory.getLogger(getClass());
     private AuditEventDao auditEventDao;
 
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
             AuditEvent event = new AuditEvent(entity, operation, DataAuditInfo.copy(info));
             return event;
         }
         return null;
     }
 
     public void saveAuditEvent(DataAuditEvent event) {
         auditEventDao.saveEvent(event);
     }
 
     public void setAuditEventDao(AuditEventDao auditEventDao) {
         this.auditEventDao = auditEventDao;
     }
 }
