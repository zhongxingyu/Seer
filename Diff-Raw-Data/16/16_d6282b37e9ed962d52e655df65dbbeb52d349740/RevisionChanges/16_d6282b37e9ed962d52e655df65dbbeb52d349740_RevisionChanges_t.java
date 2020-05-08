 package edu.northwestern.bioinformatics.studycalendar.web.delta;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
 import edu.northwestern.bioinformatics.studycalendar.domain.DomainObjectTools;
 import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
 import gov.nih.nci.cabig.ctms.domain.DomainObject;
 import gov.nih.nci.cabig.ctms.lang.StringTools;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Exposes the changes in a revision for easy iteration and display.
  *
  * @author Rhett Sutphin
  */
 public class RevisionChanges {
     private final Logger log = LoggerFactory.getLogger(getClass());
     private DaoFinder daoFinder;
 
     private Revision revision;
     private Study source;
     // if specified, limit the exposed changes to those that apply
     // to this node or its children (TODO)
     private PlanTreeNode<?> target;
 
     public RevisionChanges(DaoFinder daoFinder, Revision revision, Study source) {
         this(daoFinder, revision, source, null);
     }
 
     public RevisionChanges(DaoFinder daoFinder, Revision revision, Study source, PlanTreeNode<?> node) {
         this.daoFinder = daoFinder;
         this.revision = revision;
         this.source = source;
         this.target = node;
     }
 
     public List<Flat> getFlattened() {
         List<Flat> flattened = new ArrayList<Flat>();
         for (Delta<?> delta : revision.getDeltas()) {
             if (isChildOrTarget(delta.getNode())) {
                 for (Change change : delta.getChanges()) {
                     flattened.add(createFlat(delta.getNode(), change));
                 }
             }
         }
         return flattened;
     }
 
     private boolean isChildOrTarget(DomainObject node) {
         if (target == null) return true;
         if (node.getClass().isAssignableFrom(target.getClass()) || target.getClass().isAssignableFrom(node.getClass())) {
             return node.getId().equals(target.getId());
         }
         if (!DomainObjectTools.isMoreSpecific(node.getClass(), target.getClass())) {
             return false;
         }
         if (target instanceof PlanTreeInnerNode) {
             return ((PlanTreeInnerNode) target).isAncestorOf((PlanTreeNode) node);
         }
         return false;
     }
 
     // visible for testing
     static String getNodeName(DomainObject node) {
         StringBuilder sb = new StringBuilder();
         if (node == null) {
            sb.append("unknown");
         } else if (node instanceof Named) {
             String name = ((Named) node).getName();
             if (name == null) {
                 sb.append("unnamed ").append(nodeTypeName(node));
             } else {
                 sb.append(nodeTypeName(node)).append(' ').append(name);
             }
         } else if (PlannedCalendar.class.isAssignableFrom(node.getClass())) {
             sb.append("the template");
         } else if (PlannedActivity.class.isAssignableFrom(node.getClass())) {
             PlannedActivity e = (PlannedActivity) node;
             if (e.getActivity() == null) {
                 sb.append("a planned activity");
             } else {
                 sb.append("a planned ").append(e.getActivity().getName());
             }
         } else {
             sb.append("unexpected node: ").append(node);
         }
         return sb.toString();
     }
 
     private static String nodeTypeName(DomainObject node) {
         if (node instanceof PlannedCalendar) return "calendar";
         if (node instanceof Epoch) return "epoch";
         if (node instanceof StudySegment) return "studySegment";  // segment?
         if (node instanceof Period) return "period";
         if (node instanceof PlannedActivity) return "planned activity";
         if (node instanceof Population) return "population";
         // note that this default is not generally suitable because the actual
         // class might be, e.g., a CGLIB dynamic subclass
         return node.getClass().getSimpleName().toLowerCase();
     }
 
     @SuppressWarnings({ "RawUseOfParameterizedType" })
     private <C extends Change> Flat createFlat(DomainObject node, C change) {
         if (change instanceof ChildrenChange && node instanceof Parent) {
             ChildrenChange cChange = (ChildrenChange) change;
             if (cChange.getChild() == null) {
                 log.debug("Locating (potential) child with id {} for {}", cChange.getChildId(), node);
                 DomainObjectDao dao = daoFinder.findDao(((Parent) node).childClass());
                 cChange.setChild((Child<?>) dao.getById(cChange.getChildId()));
             }
         }
 
         if (change instanceof Add) {
             return new FlatAdd(node, (Add) change);
         } else if (change instanceof Remove) {
             return new FlatRemove(node, (Remove) change);
         } else if (change instanceof Reorder) {
             return new FlatReorder(node, (Reorder) change);
         } else if (change instanceof PropertyChange) {
             return new FlatPropertyChange(node, (PropertyChange) change);
         } else {
             throw new StudyCalendarSystemException("Unsupported change type: %s (%s)",
                 change, change.getClass().getName());
         }
     }
 
     public abstract class Flat<C extends Change> {
         private DomainObject node;
         private C change;
 
         public Flat(DomainObject node, C change) {
             this.node = node;
             this.change = change;
         }
 
         public int getId() {
             return change.getId();
         }
 
         public C getChange() {
             return change;
         }
 
         public DomainObject getNode() {
             return node;
         }
 
         public abstract String getSentence();
 
         @Override
         public String toString() {
             return new StringBuilder(getClass().getSimpleName()).append('[')
                 .append(getChange()).append(" on ").append(getNode()).append(']')
                 .toString();
         }
     }
 
     public class FlatAdd extends Flat<Add> {
         public FlatAdd(DomainObject node, Add change) { super(node, change); }
 
         @Override
         public String getSentence() {
             return new StringBuilder("Add ").append(getNodeName(getChange().getChild()))
                 .append(" to ").append(getNodeName(getNode())).toString();
         }
     }
 
     public class FlatRemove extends Flat<Remove> {
         public FlatRemove(DomainObject node, Remove change) { super(node, change); }
 
         @Override
         public String getSentence() {
             return new StringBuilder("Remove ").append(getNodeName(getChange().getChild()))
                 .append(" from ").append(getNodeName(getNode())).toString();
         }
     }
 
     public class FlatReorder extends Flat<Reorder> {
         public FlatReorder(DomainObject node, Reorder change) { super(node, change); }
 
         @Override
         public String getSentence() {
             int diff = getChange().getNewIndex() - getChange().getOldIndex();
 
             return new StringBuilder("Move ").append(getNodeName(getChange().getChild()))
                 .append(diff < 0 ? " up " : " down ")
                 .append(StringTools.createCountString(Math.abs(diff), "space"))
                 .append(" in ").append(getNodeName(getNode()))
                 .toString();
         }
     }
 
     public class FlatPropertyChange extends Flat<PropertyChange> {
         public FlatPropertyChange(DomainObject node, PropertyChange change) { super(node, change); }
 
         @Override
         public String getSentence() {
            //all the extra work is done to display the name of the node at the time of the amendment, not the current name.
            String fullName = getNodeName(getNode());
            String nodeName = null;
            if (getNode() instanceof Named) {
                nodeName = ((Named) getNode()).getName();
            }

            String nameToUse;
            if (nodeName != null && !nodeName.equals(getChange().getOldValue())) {
                String keywordNode = fullName.substring(0, fullName.indexOf(nodeName));
                nameToUse = new StringBuilder(keywordNode).append(' ').append(getChange().getOldValue()).toString();
            } else {
                nameToUse = fullName;
            }
            return new StringBuilder(StringUtils.capitalize(nameToUse)).append(' ')
                 .append(getChange().getPropertyName()).append(" changed from \"")
                 .append(getChange().getOldValue()).append("\" to \"")
                 .append(getChange().getNewValue()).append('"').toString();
         }
     }
 }
