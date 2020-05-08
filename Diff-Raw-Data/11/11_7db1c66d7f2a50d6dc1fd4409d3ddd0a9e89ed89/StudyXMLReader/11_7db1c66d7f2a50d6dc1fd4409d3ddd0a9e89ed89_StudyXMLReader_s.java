 package edu.northwestern.bioinformatics.studycalendar.xml.readers;
 
 import static java.util.Collections.singletonList;
 
 import static edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode.cast;
 import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.PLANNED_ACTIVITY;
 import static edu.nwu.bioinformatics.commons.CollectionUtils.firstElement;
 import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.EPOCH;
 import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.ADD;
 import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.NAME;
 import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.NODE_ID;
 
 import static java.lang.Boolean.valueOf;
 
 import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.PLANNDED_CALENDAR;
 import edu.northwestern.bioinformatics.studycalendar.dao.*;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
 import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.ASSIGNED_IDENTIFIER;
 import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.ID;
 import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
 import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
 import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
 import edu.northwestern.bioinformatics.studycalendar.xml.validators.Schema;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.XMLConstants;
 import java.io.InputStream;
 import java.util.*;
 import java.text.SimpleDateFormat;
 
 public class StudyXMLReader  {
     private StudyDao studyDao;
     private DeltaDao deltaDao;
     private AmendmentDao amendmentDao;
     private PlannedCalendarDao plannedCalendarDao;
     private ChangeDao changeDao;
     private TemplateService templateService;
 
     SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
     private EpochDao epochDao;
     private StudySegmentDao studySegmentDao;
     private PeriodDao periodDao;
     private PlannedActivityDao plannedActivityDao;
     private ActivityDao activityDao;
 
     public Study read(InputStream dataFile) throws Exception {
 
         // create a SchemaFactory that conforms to W3C XML Schema
         SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 
         javax.xml.validation.Schema schema = sf.newSchema(Schema.template.file());
 
         // get a DOM factory
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 
         // configure the factory
         dbf.setNamespaceAware(true);
 
         // Ignore whitespace
         dbf.setIgnoringElementContentWhitespace(true);
 
         // set schema on the factory
         dbf.setSchema(schema);
 
         // create a new parser that validates documents against a schema
         DocumentBuilder db = dbf.newDocumentBuilder();
 
         Document doc = db.parse(dataFile);
 
         return parseStudy(doc);
     }
 
     protected Study parseStudy(Document doc) throws Exception {
         Element element = doc.getDocumentElement();
         String gridId = element.getAttribute(ID);
         Study study = studyDao.getByGridId(gridId);
         if (study == null) {
             study = new Study();
             study.setGridId(gridId);
             study.setAssignedIdentifier(element.getAttribute(ASSIGNED_IDENTIFIER));
         }
 
         addPlannedCalendar(element, study);
         addAmendments(element, study);
 
         return study;
     }
 
     protected void addPlannedCalendar(Element parentElement, Study parent) throws Exception {
         NodeList nodes = parentElement.getElementsByTagName(PLANNDED_CALENDAR);
 
         Element element = ((Element)nodes.item(0));
         String gridId = element.getAttribute(ID);
         PlannedCalendar calendar = plannedCalendarDao.getByGridId(gridId);
         if (calendar == null) {
             calendar = new PlannedCalendar();
             calendar.setGridId(gridId);
             calendar.setStudy(parent);
         }

        parent.setPlannedCalendar(calendar);
     }
 
 
     protected void addAmendments(Element parentElement, Study parent) throws Exception {
         List<Amendment> amendments = new ArrayList<Amendment>();
 
         NodeList nodes = parentElement.getElementsByTagName(AMENDMENT);
         for (int i=0; i < nodes.getLength(); i++) {
 
             Element element = ((Element)nodes.item(i));
 
             String gridId = element.getAttribute(ID);
             Amendment amendment = amendmentDao.getByGridId(gridId);
             if (amendment == null) {
                 amendment = new Amendment();
                 amendment.setGridId(gridId);
                 amendment.setName(element.getAttribute(NAME));
                 amendment.setMandatory(valueOf(element.getAttribute(MANDATORY)));
                 amendment.setDate(formatter.parse(element.getAttribute(DATE)));
 
                 String prevAmendmentGridId = element.getAttribute(PREVIOUS_AMENDMENT_ID);
                 if (prevAmendmentGridId != null) {
                     for (Amendment searchAmendment : amendments) {
                         if (searchAmendment.getGridId().equals(prevAmendmentGridId)) {
                             amendment.setPreviousAmendment(searchAmendment);
                         }
                     }
                 }
             }
             amendments.add(amendment);
 
             addDeltas(element, amendment, parent);
 
             amendmentDao.save(amendment);
         }
 
         Collections.reverse(amendments);
         parent.setAmendment(firstElement(amendments));
     }
 
 
     protected void addDeltas(Element parentElement, Amendment amendment, Study study) {
         Collection<Node> deltaNodes = new NodeListCollection(parentElement.getChildNodes());
 
         for (Node deltaNode : deltaNodes) {
             Element deltaElement = (Element) deltaNode;
 
             // Get Delta from datastore
             String deltaGridId = deltaElement.getAttribute(ID);
             Delta<?> delta = deltaDao.getByGridId(deltaGridId);
 
             // Create Delta if not in datastore
             if (delta == null) {
                 delta = createDelta(deltaElement);
 
                 PlanTreeNode<?> planTreeNode = createPlanTreeNode(deltaElement);
 
                 assignDeltaAttributes(delta, deltaGridId, amendment, planTreeNode);
                 
                 amendment.addDelta(delta);
             }
 
             addChanges(deltaElement, delta, study);
         }
     }
 
     protected void addChanges(Element parent, Delta<?> delta, Study study) {
         List<Node> addNodes = new NodeListCollection(parent.getChildNodes());
 
         for (Node addNode : addNodes) {
             Element element = (Element) addNode;
 
              // Get Add if Delta Exists
             String gridId = element.getAttribute(ID);
             Change change = changeDao.getByGridId(gridId);
 
             if (change == null) {
                 if (ADD.equals(element.getNodeName())) {
                     change = new Add();
                 } else if(REMOVE.equals(element.getNodeName()) ){
                     change = new Remove();
                     String childGridId = element.getAttribute(CHILD_ID);
                     Element changeElement = getElementById(parent, childGridId);
                     PlanTreeNode<?> planTreeNode = getExistingChild(changeElement, childGridId);
                     ((Remove)change).setChild(planTreeNode);
                     ((Remove)change).setChildId(planTreeNode.getId());
                 } else if (REORDER.equals(element.getNodeName())) {
                     change = new Reorder();
                     String childGridId = element.getAttribute(CHILD_ID);
                     Element changeElement = getElementById(parent, childGridId);
                     PlanTreeNode<?> planTreeNode = getExistingChild(changeElement, childGridId);
                     ((Reorder)change).setChild(planTreeNode);
                     ((Reorder)change).setChildId(planTreeNode.getId());
                     ((Reorder)change).setOldIndex(new Integer(element.getAttribute(OLD_INDEX)));
                     ((Reorder)change).setNewIndex(new Integer(element.getAttribute(NEW_INDEX)));
                 } else if (PROPERTY_CHANGE.equals(element.getNodeName())) {
                     change = new PropertyChange();
                     change.setGridId(element.getAttribute(CHILD_ID));
                     ((PropertyChange) change).setPropertyName(element.getAttribute(PROPERTY_NAME));
                     ((PropertyChange) change).setOldValue(element.getAttribute(OLD_VALUE));
                     ((PropertyChange) change).setNewValue(element.getAttribute(NEW_VALUE));
                 }else {
                     throw new StudyCalendarError("Cannot find Change Node for: %s", element.getNodeName());
                 }
 
                 change.setGridId(gridId);
 
                 delta.addChange(change);
 
             }
 
             addChildNode(element, change, study);
         }
     }
 
 
     protected void addChildNode(Element parent, Change change, Study study) {
         List<Node> childNodes = new NodeListCollection(parent.getChildNodes());
 
         for (Node childNode : childNodes) {
             Element element = (Element) childNode;
             String childGridId = element.getAttribute(ID);
 
             if (change instanceof ChildrenChange) {
 
                 PlanTreeNode<?> child = getExistingChild(element, childGridId);
 
                 if (child == null) {
                    child = createChild(element, null);
 
                    ((ChildrenChange) change).setChild(child);
                 }
 
                 addPlanTreeNode(element, child, study);
             }
         }
     }
 
     protected void addPlanTreeNode(Element parent, PlanTreeNode<?> parentTreeNode, Study study) {
         List<Node> childNodes = new NodeListCollection(parent.getChildNodes());
 
         for (Node childNode : childNodes) {
             Element element = (Element) childNode;
             String childGridId = element.getAttribute(ID);
 
             if (ACTIVITY.equals(element.getNodeName())) {
                 break;
             }
 
             PlanTreeNode<?> child = getExistingChild(element, childGridId);
 
             if (child == null) {
                 child = createChild(element, null);
 
                 cast(parentTreeNode).addChild(child);
             }
 
             addPlanTreeNode(element, child, study);
         }
     }
 
 
 
     /* Class Helpers */
 
     private PlanTreeNode<?> getExistingChild(Element childElement, String childGridId) {
         if (EPOCH.equals(childElement.getNodeName())) {
             return epochDao.getByGridId(childGridId);
         } else if (STUDY_SEGMENT.equals(childElement.getNodeName())) {
             return studySegmentDao.getByGridId(childGridId);
         } else if  (PERIOD.equals(childElement.getNodeName())) {
             return periodDao.getByGridId(childGridId);
         } else if (PLANNED_ACTIVITY.equals(childElement.getNodeName())) {
             return plannedActivityDao.getByGridId(childGridId);
         }  else {
             throw new StudyCalendarError("Cannot find Child Node for: %s", childElement.getNodeName());
         }
     }
 
     private PlanTreeNode<?> createChild(Element childElement, PlanTreeInnerNode parent ) {
         if (EPOCH.equals(childElement.getNodeName())) {
             Epoch epoch = new Epoch();
             epoch.setGridId(childElement.getAttribute(ID));
             epoch.setName(childElement.getAttribute(NAME));
             return epoch;
         } else if (STUDY_SEGMENT.equals(childElement.getNodeName())) {
             StudySegment studySegment = new StudySegment();
             studySegment.setGridId(childElement.getAttribute(ID));
             studySegment.setName(childElement.getAttribute(NAME));
             return studySegment;
         } else if  (PERIOD.equals(childElement.getNodeName())) {
             Period period = new Period();
             period.setGridId(childElement.getAttribute(ID));
             period.setName(childElement.getAttribute(NAME));
             return period;
         } else if (PLANNED_ACTIVITY.equals(childElement.getNodeName())) {
             PlannedActivity plannedActivity = new PlannedActivity();
             plannedActivity.setGridId(childElement.getAttribute(ID));
             plannedActivity.setDay(new Integer(childElement.getAttribute(DAY)));
             plannedActivity.setDetails(childElement.getAttribute(DETAILS));
             plannedActivity.setCondition(childElement.getAttribute(CONDITION));
 
             addActivity(childElement, plannedActivity);
             return plannedActivity;
         } else {
             throw new StudyCalendarError("Cannot find Child Node for: %s", childElement.getNodeName());
         }
     }
 
     private void addActivity(Element parentElement, PlannedActivity parent ) {
         Element element = (Element) parentElement.getFirstChild();
         Activity activity =  new Activity();
         activity.setGridId(element.getAttribute(ID));
         activity.setName(element.getAttribute(NAME));
         activity.setDescription(element.getAttribute(DESCRIPTION));
         activity.setType(ActivityType.getById(Integer.parseInt(element.getAttribute(TYPE_ID))));
         activity.setCode(element.getAttribute(CODE));
 
         parent.setActivity(activity);
     }
 
 
       private Delta<?> createDelta(Element delta) {
         String nodeGridId = delta.getAttribute(NODE_ID);
         Element node = getElementById(delta, nodeGridId);
 
         if (PLANNDED_CALENDAR.equals(node.getNodeName())) {
             return new PlannedCalendarDelta();
         } else if (EPOCH.equals(node.getNodeName())) {
             return new EpochDelta();
         } else if (STUDY_SEGMENT.equals(node.getNodeName())) {
             return new StudySegmentDelta();
         } else if (PERIOD.equals(node.getNodeName())) {
             return new PeriodDelta();
         } else if (PLANNED_ACTIVITY.equals(node.getNodeName())) {
             return new PlannedActivityDelta();
         } else {
             throw new StudyCalendarError("Cannot find PlanTreeNode Type for: %s", node.getNodeName());
         }
     }
 
    /* private PlanTreeNode<?> createPlanTreeNodeParam(Element delta, Study study) {
         String nodeGridId = delta.getAttribute(NODE_ID);
         Element node = delta.getOwnerDocument().getElementById(nodeGridId);
 
         PlanTreeNode<?> param;
         if (PLANNDED_CALENDAR.equals(node.getNodeName())) {
             param = new PlannedCalendar();
         } else if (EPOCH.equals(node.getNodeName())) {
             param = new Epoch();
         } else if (STUDY_SEGMENT.equals(node.getNodeName())) {
             param = new StudySegment();
         } else if (PERIOD.equals(node.getNodeName())) {
             param = new Period();
         } else if (PLANNED_ACTIVITY.equals(node.getNodeName())) {
             param = new PlannedActivity();
         } else {
             throw new StudyCalendarError("Cannot find PlanTreeNode Type for: %s", node.getNodeName());
         }
 
         param.setGridId(nodeGridId);
         PlanTreeNode<?> planTreeNode = templateService.findEquivalentChild(study, param);
 
         if (planTreeNode == null) {
             throw new StudyCalendarError("Cannot find PlanTreeNode for: %s [%s]", node.getNodeName(), nodeGridId);
         }
 
         return planTreeNode;
     }*/
 
      private PlanTreeNode<?> createPlanTreeNode(Element delta) {
         String nodeGridId = delta.getAttribute(NODE_ID);
         Element node = getElementById(delta, nodeGridId);
 
         PlanTreeNode<?> planTreeNode;
         if (PLANNDED_CALENDAR.equals(node.getNodeName())) {
             planTreeNode = plannedCalendarDao.getByGridId(nodeGridId);
         } else if (EPOCH.equals(node.getNodeName())) {
            planTreeNode = epochDao.getByGridId(nodeGridId);
         } else if (STUDY_SEGMENT.equals(node.getNodeName())) {
             planTreeNode = studySegmentDao.getByGridId(nodeGridId);
         } else if (PERIOD.equals(node.getNodeName())) {
             planTreeNode = periodDao.getByGridId(nodeGridId);
         } else if (PLANNED_ACTIVITY.equals(node.getNodeName())) {
             planTreeNode = plannedActivityDao.getByGridId(nodeGridId);
         } else {
             throw new StudyCalendarError("Cannot find PlanTreeNode Type for: %s", node.getNodeName());
         }
 
         if (planTreeNode == null) {
             throw new StudyCalendarError("Cannot find PlanTreeNode for: %s [%s]", node.getNodeName(), nodeGridId);
         }
 
         return planTreeNode;
     }
 
     private void assignDeltaAttributes(Delta<?> delta, String gridId, Amendment amendment, PlanTreeNode<?> node) {
         if (delta instanceof PlannedCalendarDelta) {
             ((PlannedCalendarDelta) delta).setNode((PlannedCalendar) node);
         } else if (delta instanceof EpochDelta) {
             ((EpochDelta) delta).setNode((Epoch) node);
         } else if (delta instanceof StudySegmentDelta) {
             ((StudySegmentDelta) delta).setNode((StudySegment) node);
         } else if (delta instanceof PeriodDelta) {
             ((PeriodDelta) delta).setNode((Period) node);
         } else if (delta instanceof PlannedActivityDelta) {
             ((PlannedActivityDelta) delta).setNode((PlannedActivity) node);
         } else {
             throw new StudyCalendarError("Cannot find Delta Class for: %s", delta.getClass());
         }
 
         delta.setRevision(amendment);
         delta.setGridId(gridId);
     }
 
     protected Element getElementById(Node node, String string) {
         List<Node> nodes = getAllNodes(node.getOwnerDocument().getFirstChild());
         Node found = findNodeById(nodes, string);
 
         return (Element) found;
     }
 
     protected List<Node> getAllNodes(Node node) {
         if (!node.hasChildNodes())
             return singletonList(node);
 
         List<Node> master = new ArrayList<Node>();
         master.add(node);
 
         List<Node> children = new NodeListCollection(node.getChildNodes());
         for (Node child : children) {
             master.addAll(getAllNodes(child));
         }
         
         return master ;
     }
 
     protected Node findNodeById(List<Node> nodes, String id) {
         for (Node node : nodes) {
             Element element = (Element) node;
             if (element.getAttribute(ID).equals(id)) {
                 return node;
             }
         }
         return null;
     }
 
 
     private class NodeListCollection extends AbstractList<Node> {
 
         private NodeList nodeList;
 
         public NodeListCollection(NodeList nodeList) {
             this.nodeList = nodeList;
         }
 
         public Node get(int i) {
             return nodeList.item(i);
         }
 
         public int size() {
             return nodeList.getLength();
         }
 
     }
 
     /* Dao and Service Setters */
     public void setStudyDao(StudyDao studyDao) {
         this.studyDao = studyDao;
     }
 
     public void setAmendmentDao(AmendmentDao amendmentDao) {
         this.amendmentDao = amendmentDao;
     }
 
     public void setPlannedCalendarDao(PlannedCalendarDao plannedCalendarDao) {
         this.plannedCalendarDao = plannedCalendarDao;
     }
 
     public void setDeltaDao(DeltaDao deltaDao) {
         this.deltaDao = deltaDao;
     }
     public void setChangeDao(ChangeDao changeDao) {
         this.changeDao = changeDao;
     }
 
     public void setTemplateService(TemplateService templateService) {
         this.templateService = templateService;
     }
 
     public void setEpochDao(EpochDao epochDao) {
         this.epochDao = epochDao;
     }
 
     public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
         this.studySegmentDao = studySegmentDao;
     }
 
     public void setPeriodDao(PeriodDao periodDao) {
         this.periodDao = periodDao;
     }
 
     public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
         this.plannedActivityDao = plannedActivityDao;
     }
 
     public void setActivityDao(ActivityDao activityDao) {
         this.activityDao = activityDao;
     }
 }
