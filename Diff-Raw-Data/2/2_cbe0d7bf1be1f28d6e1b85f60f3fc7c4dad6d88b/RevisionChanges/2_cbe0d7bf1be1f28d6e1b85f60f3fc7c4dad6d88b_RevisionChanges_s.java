 package edu.northwestern.bioinformatics.studycalendar.web.delta;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.Named;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
 import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import gov.nih.nci.cabig.ctms.lang.StringTools;
 import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
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
 
     private boolean isChildOrTarget(PlanTreeNode node) {
         if (target == null) return true;
         if (node.getClass().isAssignableFrom(target.getClass()) || target.getClass().isAssignableFrom(node.getClass())) {
            return node.equals(target);
         }
         if (!DomainObjectTools.isMoreSpecific(node.getClass(), target.getClass())) {
             return false;
         }
         if (target instanceof PlanTreeInnerNode) {
             return ((PlanTreeInnerNode) target).isAncestorOf(node);
         }
         return false;
     }
 
     // visible for testing
     static String getNodeName(PlanTreeNode node) {
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
         } else if (PlannedEvent.class.isAssignableFrom(node.getClass())) {
             PlannedEvent e = (PlannedEvent) node;
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
 
     private static String nodeTypeName(PlanTreeNode node) {
         if (node instanceof PlannedCalendar) return "calendar";
         if (node instanceof Epoch) return "epoch";
         if (node instanceof Arm) return "arm";  // segment?
         if (node instanceof Period) return "period";
         if (node instanceof PlannedEvent) return "planned activity";
         // note that this default is not generally suitable because the actual
         // class might be, e.g., a CGLIB dynamic subclass
         return node.getClass().getSimpleName().toLowerCase();
     }
 
     private <C extends Change> Flat createFlat(PlanTreeNode node, C change) {
         if (change instanceof ChildrenChange && node instanceof PlanTreeInnerNode) {
             ChildrenChange cChange = (ChildrenChange) change;
             if (cChange.getChild() == null) {
                 log.debug("Locating (potential) child with id {} for {}", cChange.getChildId(), node);
                 DomainObjectDao<PlanTreeNode> dao = daoFinder.findDao(((PlanTreeInnerNode) node).childClass());
                 cChange.setChild(dao.getById(cChange.getChildId()));
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
         private PlanTreeNode<?> node;
         private C change;
 
         public Flat(PlanTreeNode node, C change) {
             this.node = node;
             this.change = change;
         }
 
         public int getId() {
             return change.getId();
         }
 
         public C getChange() {
             return change;
         }
 
         public PlanTreeNode<?> getNode() {
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
         public FlatAdd(PlanTreeNode node, Add change) { super(node, change); }
 
         @Override
         public String getSentence() {
             return new StringBuilder("Add ").append(getNodeName(getChange().getChild()))
                 .append(" to ").append(getNodeName(getNode())).toString();
         }
     }
 
     public class FlatRemove extends Flat<Remove> {
         public FlatRemove(PlanTreeNode node, Remove change) { super(node, change); }
 
         @Override
         public String getSentence() {
             return new StringBuilder("Remove ").append(getNodeName(getChange().getChild()))
                 .append(" from ").append(getNodeName(getNode())).toString();
         }
     }
 
     public class FlatReorder extends Flat<Reorder> {
         public FlatReorder(PlanTreeNode node, Reorder change) { super(node, change); }
 
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
         public FlatPropertyChange(PlanTreeNode node, PropertyChange change) { super(node, change); }
 
         @Override
         public String getSentence() {
             return new StringBuilder(StringUtils.capitalize(getNodeName(getNode()))).append(' ')
                 .append(getChange().getPropertyName()).append(" changed from \"")
                 .append(getChange().getOldValue()).append("\" to \"")
                 .append(getChange().getNewValue()).append('"').toString();
         }
     }
 }
