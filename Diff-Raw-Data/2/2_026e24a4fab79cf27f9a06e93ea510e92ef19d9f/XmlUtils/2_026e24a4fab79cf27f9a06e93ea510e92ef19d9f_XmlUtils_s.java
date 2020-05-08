package edu.northwestern.bioinformatics.studycalendar.xml.utils;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
 import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
 
 /**
  * @author John Dzak
  */
 public class XmlUtils {
     private DaoFinder daoFinder;
 
     /**
      * When a Change object is persisted to the database, when retreived the child element is null.
      * We must retrieve it using the child id.
      */
     public PlanTreeNode<?> findChangeChild(Change change) {
         Integer childId = ((ChildrenChange)change).getChildId();
         Class<? extends PlanTreeNode<?>> childClass = ((PlanTreeInnerNode) change.getDelta().getNode()).childClass();
 
         DomainObjectDao dao = daoFinder.findDao(childClass);
         PlanTreeNode<?> child = (PlanTreeNode<?>) dao.getById(childId);
         if (child == null) {
             throw new StudyCalendarSystemException("Problem importing template. Child with class %s and id %s could not be found",
                     childClass.getName(), childId.toString());
         }
         return child;
     }
 
     ////// Bean Setters
     public void setDaoFinder(DaoFinder daoFinder) {
         this.daoFinder = daoFinder;
     }
 }
