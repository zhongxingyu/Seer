 package gov.nih.nci.ncicb.cadsr.loader.parser;
 
 import gov.nih.nci.ncicb.cadsr.loader.event.*;
 import gov.nih.nci.ncicb.cadsr.loader.defaults.UMLDefaults;
 
 import org.apache.log4j.Logger;
 
 import org.omg.uml.foundation.core.*;
 import org.omg.uml.foundation.datatypes.MultiplicityRange;
 import org.omg.uml.foundation.extensionmechanisms.*;
 import org.omg.uml.modelmanagement.Model;
 import org.omg.uml.modelmanagement.UmlPackage;
 
 import gov.nih.nci.ncicb.cadsr.loader.util.PropertyAccessor;
 import gov.nih.nci.ncicb.cadsr.loader.util.StringUtil;
 
 import gov.nih.nci.ncicb.cadsr.loader.validator.*;
 
 import gov.nih.nci.codegen.core.util.UML13Utils;
 import gov.nih.nci.codegen.core.access.UML13ModelAccess;
 import gov.nih.nci.codegen.framework.ModelAccess;
 
 import java.io.*;
 
 import java.util.*;
 
 /**
  * Implemetation of <code>Parser</code> for XMI files. Navigates the XMI document and sends UML Object events.
  *
  * @author <a href="mailto:ludetc@mail.nih.gov">Christophe Ludet</a>
  */
 public class XMIParser implements Parser {
   private static final String EA_CONTAINMENT = "containment";
   private static final String EA_UNSPECIFIED = "Unspecified";
   private UMLHandler listener;
 
   private String packageName = "";
   private String className = "";
   private List associations = new ArrayList();
   private Logger logger = Logger.getLogger(XMIParser.class.getName());
   private List generalizationEvents = new ArrayList();
   private List associationEvents = new ArrayList();
 
   private ProgressListener progressListener = null;
 
   private String[] bannedClassNames = null;
   {
     bannedClassNames = PropertyAccessor.getProperty("banned.classNames").split(",");
   }
 
   public void setEventHandler(LoaderHandler handler) {
     this.listener = (UMLHandler) handler;
   }
 
   public void parse(String filename) {
     try {
 
       ProgressEvent evt = new ProgressEvent();
       evt.setMessage("Parsing ...");
       fireProgressEvent(evt);
 
       ModelAccess access = new UML13ModelAccess();
       access.readModel("file:" + filename, "EA Model");
 
       uml.UmlPackage umlExtent = (uml.UmlPackage) access.getOutermostExtent();
       
       Model model = UML13Utils.getModel(umlExtent, "EA Model");
 //       fact = new MdrModelManagerFactoryImpl();
 //       mgr = fact.readModel("", filename);
 //       Model model = mgr.getModel();
 
       Iterator it = model.getOwnedElement().iterator();
 
       while (it.hasNext()) {
         Object o = it.next();
 
         if (o instanceof UmlPackage) {
           doPackage((UmlPackage) o);
         }
         else if (o instanceof DataType) {
           doDataType((DataType) o);
         }
         else if (o instanceof UmlAssociation) {
           doAssociation((UmlAssociation) o);
         }
         else if (o instanceof UmlClass) {
           doClass((UmlClass) o);
         }
         else {
           logger.debug("Root Element: " + o.getClass());
         }
       }
 
       fireLastEvents();
     }
     catch (Exception e) {
       logger.fatal("Could not parse: " + filename);
       logger.fatal(e, e);
     } // end of try-catch
   }
 
   private void doPackage(UmlPackage pack) {
     UMLDefaults defaults = UMLDefaults.getInstance();
 
     if (packageName.length() == 0) {
       //       if(pack.getName().indexOf(" ") == -1)
       packageName = pack.getName();
     }
     else {
       //       if(pack.getName().indexOf(" ") == -1)
       packageName += ("." + pack.getName());
     }
 
     if(isInPackageFilter(packageName)) {
       listener.newPackage(new NewPackageEvent(packageName));
     } else {
       logger.info(PropertyAccessor.getProperty("skip.package", packageName));
     }
 
     Iterator it = pack.getOwnedElement().iterator();
 
     while (it.hasNext()) {
       Object o = it.next();
 
       if (o instanceof UmlPackage) {
         String oldPackage = packageName;
         doPackage((UmlPackage) o);
         packageName = oldPackage;
       }
       else if (o instanceof UmlClass) {
         doClass((UmlClass) o);
       }
       else if (o instanceof Stereotype) {
         doStereotype((Stereotype) o);
       }
       else if (o instanceof Component) {
         doComponent((Component) o);
       }
       else if (o instanceof UmlAssociation) {
         doAssociation((UmlAssociation) o);
       }
       else if (o instanceof Interface) {
         doInterface((Interface) o);
       }
       else {
         logger.debug("Package Child: " + o.getClass());
       }
     }
 
     packageName = "";
   }
 
   private void doClass(UmlClass clazz) {
     UMLDefaults defaults = UMLDefaults.getInstance();
     String pName = getPackageName(clazz);
 
     className = clazz.getName();
 
     if (pName != null) {
       className = pName + "." + className;
     }
 
     ProgressEvent evt = new ProgressEvent();
     evt.setMessage("Parsing " + className);
     fireProgressEvent(evt);
 
     NewClassEvent event = new NewClassEvent(className.trim());
     event.setPackageName(pName);
 
     setConceptInfo(clazz, event, NewConceptEvent.TYPE_CLASS);
 
     logger.debug("CLASS: " + className);
     logger.debug("CLASS PACKAGE: " + getPackageName(clazz));
 
     if(isClassBanned(className)) {
       logger.info(PropertyAccessor.getProperty("class.filtered", className));
       return;
     }
 
     TaggedValue tv = UML13Utils.getTaggedValue(clazz, NewConceptualEvent.TV_DOCUMENTATION);
     if(tv != null) {
       event.setDescription(tv.getValue());
     }
 
     tv = UML13Utils.getTaggedValue(clazz, NewConceptualEvent.TV_HUMAN_REVIEWED);
     if(tv != null) {
       event.setReviewed(tv.getValue().equals("1")?true:false);
     }
 
     if(isInPackageFilter(pName)) {
       listener.newClass(event);
     } else {
       logger.info(PropertyAccessor.getProperty("class.filtered", className));
       return;
     }
 
     for (Iterator it = clazz.getFeature().iterator(); it.hasNext();) {
       Object o = it.next();
 
       if (o instanceof Attribute) {
         doAttribute((Attribute) o);
       }
       else if (o instanceof Operation) {
         doOperation((Operation) o);
       }
       else {
         logger.debug("Class child: " + o.getClass());
       }
     }
 
     className = "";
 
     for (Iterator it = clazz.getGeneralization().iterator(); it.hasNext();) {
       Generalization g = (Generalization) it.next();
 
       if (g.getParent() instanceof UmlClass) {
         UmlClass p = (UmlClass) g.getParent();
         NewGeneralizationEvent gEvent = new NewGeneralizationEvent();
         gEvent.setParentClassName(
           getPackageName(p) + "." + p.getName());
 
         gEvent.setChildClassName(
           getPackageName(clazz) + "." + clazz.getName());
 
         generalizationEvents.add(gEvent);
       }
     }
   }
 
   private void doInterface(Interface interf) {
     className = packageName + "." + interf.getName();
 
     //     logger.debug("Class: " + className);
     listener.newInterface(new NewInterfaceEvent(className.trim()));
 
     Iterator it = interf.getFeature().iterator();
 
     while (it.hasNext()) {
       Object o = it.next();
       if (o instanceof Attribute) {
         doAttribute((Attribute) o);
       }
       else if (o instanceof Operation) {
         doOperation((Operation) o);
       }
       else {
         logger.debug("Class child: " + o.getClass());
       }
     }
 
     className = "";
   }
 
   private void doAttribute(Attribute att) {
     NewAttributeEvent event = new NewAttributeEvent(att.getName().trim());
     event.setClassName(className);
 
     if(att.getType() == null) {
       ValidationItems.getInstance()
         .addItem(new ValidationFatal
                  (PropertyAccessor
                   .getProperty
                   ("validation.type.missing.for"
                    , event.getClassName() + "." + event.getName()),
                   null));
       return;
     }
 
     event.setType(att.getType().getName());
 
     TaggedValue tv = UML13Utils.getTaggedValue(att, NewConceptualEvent.TV_DESCRIPTION);
     if(tv != null) {
       event.setDescription(tv.getValue());
     } else {
       tv = UML13Utils.getTaggedValue(att, NewConceptualEvent.TV_DOCUMENTATION);
       if(tv != null) {
         event.setDescription(tv.getValue());
       }
     }
 
     tv = UML13Utils.getTaggedValue(att, NewConceptualEvent.TV_HUMAN_REVIEWED);
     if(tv != null) {
       event.setReviewed(tv.getValue().equals("1")?true:false);
     }
 
 
     setConceptInfo(att, event, NewConceptEvent.TYPE_PROPERTY);
 
     listener.newAttribute(event);
   }
 
   private void doDataType(DataType dt) {
     listener.newDataType(new NewDataTypeEvent(dt.getName()));
   }
 
   private void doOperation(Operation op) {
     NewOperationEvent event = new NewOperationEvent(op.getName());
     event.setClassName(className);
     listener.newOperation(event);
   }
 
   private void doStereotype(Stereotype st) {
     logger.debug("--- Stereotype " + st.getName());
   }
 
   private void doAssociation(UmlAssociation assoc) {
     Iterator it = assoc.getConnection().iterator();
     NewAssociationEvent event = new NewAssociationEvent();
     event.setRoleName(assoc.getName());
 
     String navig = "";
 
     if (it.hasNext()) {
       Object o = it.next();
 
       if (o instanceof AssociationEnd) {
         AssociationEnd end = (AssociationEnd) o;
 //         logger.debug("end A is navigable: " + end.isNavigable());
 
         if (end.isNavigable()) {
           navig += 'A';
         }
 
         Classifier classif = end.getType();
         if(!isInPackageFilter(getPackageName(classif))) {
           logger.info(PropertyAccessor.getProperty("skip.association", classif.getNamespace().getName()));
           logger.debug("classif name: " + classif.getName());
           return;
         }
 
 
         Collection range = end.getMultiplicity().getRange();
         for (Iterator it2 = range.iterator(); it2.hasNext();) {
           MultiplicityRange mr = (MultiplicityRange) it2.next();
           int low = mr.getLower();
           int high = mr.getUpper();
           event.setALowCardinality(low);
           event.setAHighCardinality(high);
         }
 
         event.setAClassName(getPackageName(classif) + "." + classif.getName());
         event.setARole(end.getName());
         if(event.getAClassName() == null) {
           logger.debug("AClassName: NULL");
           return;
         } else {
           logger.debug("AClassName: " + event.getAClassName());
         }
       }
     } else {
       logger.debug("Association has one missing END");
       return;
     }
 
     if (it.hasNext()) {
       Object o = it.next();
 
       if (o instanceof AssociationEnd) {
         AssociationEnd end = (AssociationEnd) o;
 //         logger.debug("end B is navigable: " + end.isNavigable());
 
         if (end.isNavigable()) {
           navig += 'B';
         }
 
         Classifier classif = end.getType();
         if(!isInPackageFilter(getPackageName(classif))) {
           logger.info(PropertyAccessor.getProperty("skip.association", classif.getName()));
           logger.debug("classif name: " + classif.getNamespace().getName());
           return;
         }
 
         Collection range = end.getMultiplicity().getRange();
         for (Iterator it2 = range.iterator(); it2.hasNext();) {
           MultiplicityRange mr = (MultiplicityRange) it2.next();
           int low = mr.getLower();
           int high = mr.getUpper();
           event.setBLowCardinality(low);
           event.setBHighCardinality(high);
         }
 
         event.setBClassName(getPackageName(classif) + "." + classif.getName());
         event.setBRole(end.getName());
         if(event.getBClassName() == null)
           return;
       }
     } else {
       logger.debug("Association has one missing END");
       return;
     }
 
 //     logger.debug("A END -- " + event.getAClassName() + " " + event.getALowCardinality());
 //     logger.debug("B END -- " + event.getBClassName() + " " + event.getBLowCardinality());
 
     event.setDirection(navig);
 
     logger.debug("Adding association. AClassName: " + event.getAClassName());
     associationEvents.add(event);
   }
 
   private void doComponent(Component comp) {
     logger.debug("--- Component: " + comp.getName());
   }
 
   private String cardinality(AssociationEnd end) {
     Collection range = end.getMultiplicity().getRange();
 
     for (Iterator it = range.iterator(); it.hasNext();) {
       MultiplicityRange mr = (MultiplicityRange) it.next();
       int low = mr.getLower();
       int high = mr.getUpper();
 
       if (low == high) {
         return "" + low;
       }
       else {
         String h = (high >= 0) ? ("" + high) : "*";
 
         return low + ".." + h;
       }
     }
 
     return "";
   }
 
   private void fireLastEvents() {
     for (Iterator it = associationEvents.iterator(); it.hasNext();) {
       listener.newAssociation((NewAssociationEvent) it.next());
     }
 
     for (Iterator it = generalizationEvents.iterator(); it.hasNext();) {
       listener.newGeneralization((NewGeneralizationEvent) it.next());
     }
 
     ProgressEvent evt = new ProgressEvent();
     evt.setGoal(100);
     evt.setStatus(100);
     evt.setMessage("Done");
     fireProgressEvent(evt);
 
   }
 
   private void setConceptInfo(ModelElement elt, NewConceptualEvent event, String type) {
     NewConceptEvent concept = new NewConceptEvent();
     setConceptInfo(elt, concept, type, "", 0);
 
     if(!StringUtil.isEmpty(concept.getConceptCode()))
       event.addConcept(concept);
     
     concept = new NewConceptEvent();
     for(int i=1;setConceptInfo(elt, concept, type, NewConceptEvent.TV_QUALIFIER, i); i++) {
 
       if(!StringUtil.isEmpty(concept.getConceptCode()))
         event.addConcept(concept);
 
       concept = new NewConceptEvent();
     }
 
   }
 
   private boolean setConceptInfo(ModelElement elt, NewConceptEvent event, String type, String pre, int n) {
 
     TaggedValue tv = UML13Utils.getTaggedValue(elt, type + pre + NewConceptEvent.TV_CONCEPT_CODE + ((n>0)?""+n:""));
     if (tv != null) {
       event.setConceptCode(tv.getValue().trim());
     } else 
       return false;
 
     tv = UML13Utils.getTaggedValue(elt, type + pre + NewConceptEvent.TV_CONCEPT_DEFINITION + ((n>0)?""+n:""));
     if (tv != null) {
       event.setConceptDefinition(tv.getValue().trim());
     }
 
     tv = UML13Utils.getTaggedValue(elt, type + pre + NewConceptEvent.TV_CONCEPT_DEFINITION_SOURCE + ((n>0)?""+n:""));
     if (tv != null) {
       event.setConceptDefinitionSource(tv.getValue().trim());
     }
     
     tv = UML13Utils.getTaggedValue(elt, type + pre + NewConceptEvent.TV_CONCEPT_PREFERRED_NAME + ((n>0)?""+n:""));
     if (tv != null) {
       event.setConceptPreferredName(tv.getValue().trim());
     }
 
     event.setOrder(n);
     return true;
 
   }
 
   private boolean isInPackageFilter(String pName) {
     Map packageFilter = UMLDefaults.getInstance().getPackageFilter();
     return (packageFilter.size() == 0) || (packageFilter.containsKey(pName) || (UMLDefaults.getInstance().getDefaultPackageAlias() != null));
   }
 
   private String getPackageName(ModelElement elt) {
     StringBuffer pack = new StringBuffer();
     String s = null;
     do {
       s = null;
       if (elt.getNamespace() != null) {
         s = elt.getNamespace().getName(); 
         if(s.indexOf(" ") == -1) {
 //         if(!s.startsWith("EA ")) {
           if(pack.length() > 0)
             pack.insert(0, '.');
           pack.insert(0, s);
         }
         elt = elt.getNamespace();
       }
     } while (s != null);
     
     return pack.toString();
   }
 
   private boolean isClassBanned(String className) {
     for(int i=0; i<bannedClassNames.length; i++) {
       if(className.indexOf(bannedClassNames[i]) > -1) return true;
     }
     return false;
   }
 
   private void fireProgressEvent(ProgressEvent evt) {
     if(progressListener != null)
       progressListener.newProgressEvent(evt);
   }
 
   public void addProgressListener(ProgressListener listener) {
     progressListener = listener;
   }
 }
