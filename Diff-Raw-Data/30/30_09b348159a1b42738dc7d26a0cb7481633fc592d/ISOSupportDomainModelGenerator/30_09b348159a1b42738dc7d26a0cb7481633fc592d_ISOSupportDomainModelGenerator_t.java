 package org.cagrid.iso21090.model.tools;
 
 import gov.nih.nci.cagrid.metadata.MetadataUtils;
 import gov.nih.nci.cagrid.metadata.common.SemanticMetadata;
 import gov.nih.nci.cagrid.metadata.common.UMLClassUmlAttributeCollection;
 import gov.nih.nci.cagrid.metadata.dataservice.DomainModel;
 import gov.nih.nci.cagrid.metadata.dataservice.DomainModelExposedUMLAssociationCollection;
 import gov.nih.nci.cagrid.metadata.dataservice.DomainModelExposedUMLClassCollection;
 import gov.nih.nci.cagrid.metadata.dataservice.DomainModelUmlGeneralizationCollection;
 import gov.nih.nci.cagrid.metadata.dataservice.UMLAssociationEdge;
 import gov.nih.nci.cagrid.metadata.dataservice.UMLAssociationSourceUMLAssociationEdge;
 import gov.nih.nci.cagrid.metadata.dataservice.UMLAssociationTargetUMLAssociationEdge;
 import gov.nih.nci.cagrid.metadata.dataservice.UMLClassReference;
 import gov.nih.nci.cagrid.metadata.xmi.XMIConstants;
 import gov.nih.nci.ncicb.xmiinout.domain.UMLAssociation;
 import gov.nih.nci.ncicb.xmiinout.domain.UMLAttribute;
 import gov.nih.nci.ncicb.xmiinout.domain.UMLClass;
 import gov.nih.nci.ncicb.xmiinout.domain.UMLDatatype;
 import gov.nih.nci.ncicb.xmiinout.domain.UMLGeneralizable;
 import gov.nih.nci.ncicb.xmiinout.domain.UMLGeneralization;
 import gov.nih.nci.ncicb.xmiinout.domain.UMLModel;
 import gov.nih.nci.ncicb.xmiinout.domain.UMLPackage;
 import gov.nih.nci.ncicb.xmiinout.domain.UMLTaggedValue;
 import gov.nih.nci.ncicb.xmiinout.domain.bean.UMLDatatypeBean;
 import gov.nih.nci.ncicb.xmiinout.handler.HandlerEnum;
 import gov.nih.nci.ncicb.xmiinout.handler.XmiException;
 import gov.nih.nci.ncicb.xmiinout.handler.XmiHandlerFactory;
 import gov.nih.nci.ncicb.xmiinout.handler.XmiInOutHandler;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 public class ISOSupportDomainModelGenerator {
     
     // logical model package contains our data model
     public static final String LOGICAL_MODEL_PACKAGE_PREFIX = "Logical View.Logical Model.";
     
     // regex for package excludes
     public static final String DEFAULT_PACKAGE_EXCLUDE_REGEX_LEAVE_ISO = ".*?java.*,.*?[V|v]alue.?[D|d]omain.*";
     public static final String DEFAULT_PACKAGE_EXCLUDE_REGEX = ".*?java.*,.*?[V|v]alue.?[D|d]omain.*,.*?iso21090.*";
     public static final String ISO_PACKAGE_REGEX = ".*?iso21090.*";
     public static final String JAVA_PACKAGE_REGEX = ".*?java.*";
     public static final Pattern ISO_PATTERN = Pattern.compile(ISO_PACKAGE_REGEX);
     public static final Pattern JAVA_PATTERN = Pattern.compile(JAVA_PACKAGE_REGEX);
     
     // cadsr ID tag values
     public static final String XMI_TAG_CADSR_DE_ID = "CADSR_DE_ID";
     public static final String XMI_TAG_CADSR_DE_VERSION = "CADSR_DE_VERSION";
     
     private static Log LOG = LogFactory.getLog(ISOSupportDomainModelGenerator.class);
 
     private XmiInOutHandler handler = null;
     private String projectShortName = null;
     private String projectLongName = null;
     private String projectDescription = null;
     private String projectVersion = null;
     private String packageExcludeRegex = null;
     private float attributeVersion = 1.0f;
 
 
     public ISOSupportDomainModelGenerator(HandlerEnum xmiType) {
         this.handler = XmiHandlerFactory.getXmiHandler(xmiType);
     }
 
 
     public String getProjectShortName() {
         return projectShortName;
     }
 
 
     public void setProjectShortName(String projectShortName) {
         this.projectShortName = projectShortName;
     }
 
 
     public String getProjectLongName() {
         return projectLongName;
     }
 
 
     public void setProjectLongName(String projectLongName) {
         this.projectLongName = projectLongName;
     }
 
 
     public String getProjectDescription() {
         return projectDescription;
     }
 
 
     public void setProjectDescription(String projectDescription) {
         this.projectDescription = projectDescription;
     }
 
 
     public String getProjectVersion() {
         return projectVersion;
     }
 
 
     public void setProjectVersion(String projectVersion) {
         this.projectVersion = projectVersion;
     }
 
 
     public String getPackageExcludeRegex() {
         return packageExcludeRegex != null ? packageExcludeRegex : DEFAULT_PACKAGE_EXCLUDE_REGEX_LEAVE_ISO;
     }
 
 
     public void setPackageExcludeRegex(String packageExcludeRegex) {
         this.packageExcludeRegex = packageExcludeRegex;
     }
 
 
     public float getAttributeVersion() {
         return attributeVersion;
     }
 
 
     public void setAttributeVersion(float attributeVersion) {
         this.attributeVersion = attributeVersion;
     }
     
     
     public DomainModel generateDomainModel(String xmiFilename) throws XmiException, IOException {
         LOG.debug("Loading XMI from " + xmiFilename);
         handler.load(xmiFilename);
         LOG.debug("Loading complete, parsing model");
         UMLModel umlModel = handler.getModel();
         DomainModel domain = new DomainModel();
         domain.setProjectShortName(getProjectShortName());
         domain.setProjectVersion(getProjectVersion());
         domain.setProjectLongName(getProjectLongName());
         domain.setProjectDescription(getProjectDescription());
         List<UMLClass> umlClasses = getAllClassesInModel(umlModel);
         Set<UMLAssociation> umlAssociationsToProcess = new HashSet<UMLAssociation>();
         Map<String, gov.nih.nci.cagrid.metadata.dataservice.UMLClass> domainClasses = 
             new HashMap<String, gov.nih.nci.cagrid.metadata.dataservice.UMLClass>();
         List<gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation> domainAssociations = 
             new ArrayList<gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation>();
         List<gov.nih.nci.cagrid.metadata.dataservice.UMLGeneralization> domainGeneralizations = 
             new ArrayList<gov.nih.nci.cagrid.metadata.dataservice.UMLGeneralization>();
         LOG.debug("Creating initial class list");
         // have to swap references to the non-existing gov.nih.nci.iso21090.String and Uri classes
         // to the real java.lang.String class.  No idea why the ISO ones are in models.
         UMLClass badIsoStringClass = null;
         UMLClass badIsoUriClass = null;
         UMLClass badIsoUidClass = null;
         UMLClass badIsoCodeClass = null;
         UMLClass javaStringClass = null;
         UMLClass javaDoubleClass = null;
         // have to pre-create all the classes so I can properly create associations w/ refs between them
         for (UMLClass clazz : umlClasses) {
             if (clazz.getPackage() == null) {
                 LOG.debug("Skipping class with null package: " + clazz.getName());
             } else {
                 String fullPackageName = getFullPackageName(clazz);
                 // TODO: replace this ugly if.. block with a hash map or something
                 if (fullPackageName.equals(LOGICAL_MODEL_PACKAGE_PREFIX + "gov.nih.nci.iso21090") && clazz.getName().equals("String")) {
                     badIsoStringClass = clazz;
                 }
                 if (fullPackageName.equals(LOGICAL_MODEL_PACKAGE_PREFIX + "gov.nih.nci.iso21090") && clazz.getName().equals("Uri")) {
                     badIsoUriClass = clazz;
                 }
                 if (fullPackageName.equals(LOGICAL_MODEL_PACKAGE_PREFIX + "gov.nih.nci.iso21090") && clazz.getName().equals("Uid")) {
                     badIsoUidClass = clazz;
                 }
                 if (fullPackageName.equals(LOGICAL_MODEL_PACKAGE_PREFIX + "gov.nih.nci.iso21090") && clazz.getName().equals("Code")) {
                     badIsoCodeClass = clazz;
                 }
                 if (fullPackageName.equals(LOGICAL_MODEL_PACKAGE_PREFIX + "java.lang") && clazz.getName().equals("String")) {
                     javaStringClass = clazz;
                 }
                 if (fullPackageName.equals(LOGICAL_MODEL_PACKAGE_PREFIX + "java.lang") && clazz.getName().equals("Double")) {
                     javaDoubleClass = clazz;
                 }
                 // TODO: there may be others (REAL -> Double, Binary -> byte[])
                 // Satish is supposed to send me this list
                 
                 // filter out anything not in the logical model and matching the exclude regex
                 if (shouldExcludePackage(fullPackageName)) {
                     LOG.debug("Excluding class " + fullPackageName + "." + clazz.getName());
                 } else if (typeIsEnumeration(clazz)) {
                     LOG.debug("Skipping " + clazz.getName() + " since it's an enumeration to be used as simple attribute value");
                 } else {
                     // basic class info
                     String strippedPackageName = fullPackageName.substring(LOGICAL_MODEL_PACKAGE_PREFIX.length());
                     boolean isIsoClass = ISO_PATTERN.matcher(strippedPackageName).matches();
                     String shortClassName = isIsoClass ? cleanUpIsoClassName(clazz.getName()) : clazz.getName();
                     LOG.debug("Creating model class " + strippedPackageName + "." + shortClassName);
                     gov.nih.nci.cagrid.metadata.dataservice.UMLClass c = new gov.nih.nci.cagrid.metadata.dataservice.UMLClass();
                     c.setPackageName(strippedPackageName);
                     c.setClassName(shortClassName);
                     c.setId(String.valueOf(clazz.hashCode()));
                     // ISO types aren't targetable; everything else is by default
                     LOG.debug("Class " + (isIsoClass ? "is" : "is not") + " targetable");
                     c.setAllowableAsTarget(!isIsoClass);
                     domainClasses.put(c.getPackageName() + "." + c.getClassName(), c);
                     umlAssociationsToProcess.addAll(clazz.getAssociations());
                 }
             }
         }
         
         // process through the classes again
         LOG.debug("Reprocessing classes to figure out attributes");
         for (UMLClass clazz : umlClasses) {
             if (clazz.getPackage() == null) {
                 LOG.debug("Skipping class with null package: " + clazz.getName());
             } else {
                 String fullPackageName = getFullPackageName(clazz);
                 // filter out anything not in the logical model and matching the exclude regex
                 if (shouldExcludePackage(fullPackageName)) {
                     LOG.debug("Excluding class " + fullPackageName + "." + clazz.getName());
                 } else if (typeIsEnumeration(clazz)) {
                     LOG.debug("Skipping " + clazz.getName() + " since it's an enumeration to be used as simple attribute value");
                 } else {
                     // basic class info
                     String strippedPackageName = fullPackageName.substring(LOGICAL_MODEL_PACKAGE_PREFIX.length());
                     boolean isIsoClass = ISO_PATTERN.matcher(strippedPackageName).matches();
                     String shortClassName = isIsoClass ? cleanUpIsoClassName(clazz.getName()) : clazz.getName();
                     String fullClassName = strippedPackageName + "." + shortClassName;
                     gov.nih.nci.cagrid.metadata.dataservice.UMLClass c = domainClasses.get(fullClassName);
                     
                     // attributes
                     LOG.debug("Processing attributes of the class");
                     List<UMLAttribute> umlAttribs = clazz.getAttributes();
                     List<gov.nih.nci.cagrid.metadata.common.UMLAttribute> attribs = 
                         new ArrayList<gov.nih.nci.cagrid.metadata.common.UMLAttribute>(umlAttribs.size());
                     for (UMLAttribute attrib : umlAttribs) {
                        if (clazz.getName().toLowerCase().startsWith("dset")) {
                            LOG.info("dset");
                        }
                         LOG.debug("Creating class attribute " + attrib.getName());
                         // determine the data type of the attribute
                         UMLDatatype rawAttributeDatatype = attrib.getDatatype();
                         boolean isCollection = attributeTypeRepresentsCollection(rawAttributeDatatype.getName());
                         boolean isGeneric = attributeTypeRepresentsGeneric(rawAttributeDatatype.getName());
                         // TODO: if generic, create an association from the generic to the specific
                         LOG.debug("Attribute " + (isCollection ? "represents" : "does not represent") + " a collection");
                         LOG.debug("Attribute " + (isGeneric ? "represents" : "does not represent") + " a generic");
                         // sometimes, a user just types in the name of the datatype and it doesn't
                         // really reference a UMLClass instance in the model.  Even though this is
                         // an error, the SDK has a heuristic to deal with it, so we do too.
                         if (rawAttributeDatatype.getClass().equals(UMLDatatypeBean.class) && rawAttributeDatatype.getName().equals("Real")) {
                             LOG.debug("Special handling for Real typed attribute");
                             rawAttributeDatatype = javaDoubleClass;
                         }
                         UMLDatatype attributeDatatype = deriveRealClass(rawAttributeDatatype, umlClasses);
                         if (attributeDatatype == null) {
                             // no class could be found with our heuristic!
                             LOG.warn("NO ATTRIBUTE DATATYPE COULD BE INFERED.  FALLING BACK TO " + rawAttributeDatatype.getName());
                             attributeDatatype = rawAttributeDatatype;
                         }
                         
                         // switch out weird ISO classes that represent simple types with the Java equivalent
                         if (attributeDatatype.equals(badIsoStringClass)) {
                             attributeDatatype = javaStringClass;
                         } else if (attributeDatatype.equals(badIsoUriClass)) {
                             attributeDatatype = javaStringClass;
                         } else if (attributeDatatype.equals(badIsoUidClass)) {
                             attributeDatatype = javaStringClass;
                         } else if (attributeDatatype.equals(badIsoCodeClass)) {
                             attributeDatatype = javaStringClass;
                         }
                         
                         String attributeDatatypeName = attributeDatatype.getName();
                         if (attributeDatatype instanceof UMLClass) {
                             String rawAttribTypePackage = getFullPackageName((UMLClass) attributeDatatype);
                             String datatypeClassname = null;
                             if (ISO_PATTERN.matcher(rawAttribTypePackage).matches()) {
                                 datatypeClassname = cleanUpIsoClassName(attributeDatatype.getName());
                             } else {
                                 datatypeClassname = attributeDatatype.getName();
                             }
                             attributeDatatypeName = 
                                 rawAttribTypePackage.substring(LOGICAL_MODEL_PACKAGE_PREFIX.length()) + 
                                     "." + datatypeClassname;
                         }
                         LOG.debug("Attribute datatype determined to be " + attributeDatatypeName);
                         if (attributeDatatype instanceof UMLClass && 
                             typeIsEnumeration((UMLClass) attributeDatatype)) {
                             // turn attributes of an Enum type into String attributes; let the query processor handle it internally
                             LOG.debug("Attribute datatype is Enumeration.");
                             gov.nih.nci.cagrid.metadata.common.UMLAttribute a = 
                                 new gov.nih.nci.cagrid.metadata.common.UMLAttribute();
                             a.setName(attrib.getName());
                             // TODO: evaluate this, using String for now
                             a.setDataTypeName("java.lang.String");
                             a.setVersion(getAttributeVersion());
                             // look at tagged values for the concept code info
                             annotateAttribute(a, attrib.getTaggedValues());
                             // keep the attribute
                             attribs.add(a);
                         } else if (ISO_PATTERN.matcher(attributeDatatypeName).matches()) {
                             // CAVEAT: have to turn "attributes" that are ISO types into unidirectional Associations.
                             LOG.debug("Attribute datatype is complex.  This will be modeled as a unidirectional Association");
                             gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation isoAssociation = 
                                 new gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation();
                             isoAssociation.setBidirectional(false);
                             UMLAssociationEdge sourceEdge = new UMLAssociationEdge();
                             sourceEdge.setMaxCardinality(1);
                             sourceEdge.setMinCardinality(0);
                             sourceEdge.setRoleName(attrib.getName());
                             sourceEdge.setUMLClassReference(new UMLClassReference(c.getId()));
                             isoAssociation.setSourceUMLAssociationEdge(new UMLAssociationSourceUMLAssociationEdge(sourceEdge));
                             UMLAssociationEdge targetEdge = new UMLAssociationEdge();
                             targetEdge.setMaxCardinality(isCollection ? -1 : 1);
                             targetEdge.setMinCardinality(0);
                             targetEdge.setRoleName(attrib.getName());
                             targetEdge.setUMLClassReference(new UMLClassReference(String.valueOf(attributeDatatype.hashCode())));
                             isoAssociation.setTargetUMLAssociationEdge(new UMLAssociationTargetUMLAssociationEdge(targetEdge));
                             // keep the association
                             domainAssociations.add(isoAssociation);
                             if (isGeneric) {
                                 LOG.debug("Type is generic, adding additional association to the generic type");
                                 // need a further association from the iso datatype to its inner generic type
                                 // only "item" for DSet, but "any", "high", and "low" for Ivl
                                 boolean isDset = attributeDatatype.getName().equalsIgnoreCase("DSet");
                                 String[] roleNames = isDset ?
                                     new String[] {"item"} :
                                     new String[] {"high", "low", "any", "width"};
                                 for (String role : roleNames) {
                                     gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation genericAssoc = 
                                         new gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation();
                                     genericAssoc.setSourceUMLAssociationEdge(new UMLAssociationSourceUMLAssociationEdge(targetEdge));
                                     UMLAssociationEdge genericTargetEdge = new UMLAssociationEdge();
                                     genericTargetEdge.setMaxCardinality(isCollection ? -1 : 1);
                                     genericTargetEdge.setMinCardinality(0);
                                     // get the generic specific type
                                     String specificTypeName = getGenericSpecificType(rawAttributeDatatype.getName());
                                     if (role.equals("width") && specificTypeName.equalsIgnoreCase("Pq")) {
                                         LOG.debug("Ivl<Pq> has width of type Pqv");
                                         specificTypeName = "Pqv";
                                     }
                                     UMLClass specificType = deriveRealClass(specificTypeName, umlClasses);
                                     genericTargetEdge.setRoleName(role);
                                     genericTargetEdge.setUMLClassReference(new UMLClassReference(String.valueOf(specificType.hashCode())));
                                     genericAssoc.setTargetUMLAssociationEdge(new UMLAssociationTargetUMLAssociationEdge(genericTargetEdge));
                                     domainAssociations.add(genericAssoc);
                                 }
                                 /*
                                 if (!isDset) {
                                     // also add the "width" association to QTY for IVL
                                     gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation genericAssoc = 
                                         new gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation();
                                     genericAssoc.setSourceUMLAssociationEdge(new UMLAssociationSourceUMLAssociationEdge(targetEdge));
                                     UMLAssociationEdge qtyTargetEdge = new UMLAssociationEdge();
                                     qtyTargetEdge.setMaxCardinality(isCollection ? -1 : 1);
                                     qtyTargetEdge.setMinCardinality(0);
                                     // use the ISO QTY class here
                                     qtyTargetEdge.setRoleName("width");
                                     qtyTargetEdge.setUMLClassReference(new UMLClassReference(String.valueOf(isoQtyClass.hashCode())));
                                     genericAssoc.setTargetUMLAssociationEdge(new UMLAssociationTargetUMLAssociationEdge(qtyTargetEdge));
                                     domainAssociations.add(genericAssoc);
                                 }
                                 */
                             }
                         } else if (!isCollection) {
                             LOG.debug("Attribute datatype is simple.");
                             gov.nih.nci.cagrid.metadata.common.UMLAttribute a = 
                                 new gov.nih.nci.cagrid.metadata.common.UMLAttribute();
                             a.setName(attrib.getName());
                             a.setDataTypeName(attributeDatatypeName);
                             a.setVersion(getAttributeVersion());
                             // look at tagged values for the concept code info
                             annotateAttribute(a, attrib.getTaggedValues());
                             // keep the attribute
                             attribs.add(a);
                         } else {
                             LOG.warn("Not sure how to handle attribute " + fullClassName + "." + attrib.getName() 
                                 + " with data type " + attributeDatatypeName);
                         }
                     }
                     c.setUmlAttributeCollection(new UMLClassUmlAttributeCollection(
                         attribs.toArray(new gov.nih.nci.cagrid.metadata.common.UMLAttribute[0])));
                 }
             }
         }
         
         // associations
         LOG.debug("Processing associations");
         for (UMLAssociation assoc : umlAssociationsToProcess) {
             gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation a = 
                 new gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation();
             boolean bidirectional = associationIsBidirectional(assoc);
             a.setBidirectional(bidirectional);
             // source
             // the 0th edge describes the relationship from the perspective of THIS class
             UMLAssociationEdge sourceEdge = new UMLAssociationEdge();
             // the max and min cardinality of the associated datatype
             sourceEdge.setMaxCardinality(assoc.getAssociationEnds().get(0).getHighMultiplicity());
             sourceEdge.setMinCardinality(assoc.getAssociationEnds().get(0).getLowMultiplicity());
             // the role name (i.e. name of the variable in our class)
             sourceEdge.setRoleName(assoc.getAssociationEnds().get(0).getRoleName());
             // create a reference to the associated datatype
             UMLClass sourceClass = (UMLClass) assoc.getAssociationEnds().get(0).getUMLElement();
             sourceEdge.setUMLClassReference(new UMLClassReference(String.valueOf(sourceClass.hashCode())));
             // target
             UMLAssociationEdge targetEdge = new UMLAssociationEdge();
             targetEdge.setMaxCardinality(assoc.getAssociationEnds().get(1).getHighMultiplicity());
             targetEdge.setMinCardinality(assoc.getAssociationEnds().get(1).getLowMultiplicity());
             targetEdge.setRoleName(assoc.getAssociationEnds().get(1).getRoleName());
             UMLClass targetClass = (UMLClass) assoc.getAssociationEnds().get(1).getUMLElement();
             targetEdge.setUMLClassReference(new UMLClassReference(String.valueOf(targetClass.hashCode())));
             a.setSourceUMLAssociationEdge(new UMLAssociationSourceUMLAssociationEdge(sourceEdge));
             a.setTargetUMLAssociationEdge(new UMLAssociationTargetUMLAssociationEdge(targetEdge));
             domainAssociations.add(a);
             if (LOG.isDebugEnabled()) {
             LOG.debug(getFullPackageName(sourceClass) + "." + targetClass.getName() + 
                 " (" + sourceEdge.getRoleName() + ") "
                 + (a.isBidirectional() ? "<-->" : "--->") + " (" +
                 targetEdge.getRoleName() + ") " +
                 getFullPackageName(targetClass) + "." + targetClass.getName());
             }
         }
         
         // generalizations
         List<UMLGeneralization> umlGeneralizations = umlModel.getGeneralizations();
         for (UMLGeneralization gen : umlGeneralizations) {
             gov.nih.nci.cagrid.metadata.dataservice.UMLGeneralization g = 
                 new gov.nih.nci.cagrid.metadata.dataservice.UMLGeneralization();
             UMLGeneralizable sub = gen.getSubtype();
             String subId = String.valueOf(sub.hashCode());
             if (sub instanceof UMLDatatype) {
                 UMLClass derived = deriveRealClass((UMLDatatype) sub, umlClasses);
                 if (derived != null) {
                     String fqClassName = getFullPackageName(derived) + "." + derived.getName();
                     if (ISO_PATTERN.matcher(fqClassName).matches()) {
                         fqClassName = getFullPackageName(derived) + "." + cleanUpIsoClassName(derived.getName());
                     }
                     fqClassName = fqClassName.substring(LOGICAL_MODEL_PACKAGE_PREFIX.length());
                     LOG.debug("Datatype resolved to " + fqClassName);
                     subId = domainClasses.get(fqClassName).getId();
                 } else {
                     LOG.warn("GENERALIZATION DATATYPE NOT FOUND (" + sub.getName() + ")");
                     LOG.warn("SKIPPING THIS GENERALIZATION");
                     continue;
                 }
             } else {
                 LOG.error("I DONT KNOW WHAT TO DO WITH " + sub.getClass().getName());
                 LOG.error("SKIPPING THIS GENERALIZATION");
                 continue;
             }
             UMLGeneralizable sup = gen.getSupertype();
             String supId = String.valueOf(sup.hashCode());
             if (sup instanceof UMLDatatype) {
                 UMLClass derived = deriveRealClass((UMLDatatype) sup, umlClasses);
                 if (derived != null) {
                     String fqClassName = getFullPackageName(derived) + "." + derived.getName();
                     if (ISO_PATTERN.matcher(fqClassName).matches()) {
                         fqClassName = getFullPackageName(derived) + "." + cleanUpIsoClassName(derived.getName());
                     }
                     fqClassName = fqClassName.substring(LOGICAL_MODEL_PACKAGE_PREFIX.length());
                     LOG.debug("Datatype resolved to " + fqClassName);
                     supId = domainClasses.get(fqClassName).getId();
                 } else {
                     LOG.error("GENERALIZATION DATATYPE NOT FOUND (" + sup.getName() + ")");
                     LOG.error("SKIPPING THIS GENERALIZATION");
                     continue;
                 }
             } else {
                 LOG.error("I DONT KNOW WHAT TO DO WITH " + sup.getClass().getName());
                 LOG.error("SKIPPING THIS GENERALIZATION");
                 continue;
             }
             g.setSubClassReference(new UMLClassReference(subId));
             g.setSuperClassReference(new UMLClassReference(supId));
             domainGeneralizations.add(g);
         }
         
         // build the model
         domain.setExposedUMLClassCollection(
             new DomainModelExposedUMLClassCollection(
                 domainClasses.values().toArray(
                     new gov.nih.nci.cagrid.metadata.dataservice.UMLClass[0])));
         domain.setExposedUMLAssociationCollection(
             new DomainModelExposedUMLAssociationCollection(
                 domainAssociations.toArray(
                     new gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation[0])));
         domain.setUmlGeneralizationCollection(
             new DomainModelUmlGeneralizationCollection(
                 domainGeneralizations.toArray(
                     new gov.nih.nci.cagrid.metadata.dataservice.UMLGeneralization[0])));
         
         return domain;
     }
     
     
     private boolean associationIsBidirectional(UMLAssociation association) {
         boolean bidirectional = false;
         UMLTaggedValue tag = association.getTaggedValue("direction");
         if (tag != null) {
             LOG.debug("Association direction: " + tag.getValue());
             // System.out.println("Association direction: " + tag.getValue());
             bidirectional = "Bi-Directional".equals(tag.getValue()) ||
                 "Unspecified".equals(tag.getValue());
         }
         return bidirectional;
     }
     
     
     private List<UMLClass> getAllClassesInModel(UMLModel model) {
         List<UMLClass> classes = new LinkedList<UMLClass>();
         List<UMLPackage> packages = new ArrayList<UMLPackage>();
         packages.addAll(model.getPackages());
         for (int i = 0; i < packages.size(); i++) {
             UMLPackage pack = packages.get(i);
             packages.addAll(pack.getPackages());
             classes.addAll(pack.getClasses());
         }
         return classes;
     }
     
     
     private String getFullPackageName(UMLClass clazz) {
         StringBuffer buf = new StringBuffer();
         UMLPackage pack = clazz.getPackage();
         boolean first = true;
         while (pack != null) {
             if (!first) {
                 buf.insert(0, '.');   
             }
             first = false;
             buf.insert(0, pack.getName());
             pack = pack.getParent();
         }
         return buf.toString();
     }
     
     
     private UMLClass deriveRealClass(UMLDatatype datatype, List<UMLClass> searchClasses) {
         LOG.debug("Deriving real class for datatype " + datatype.getName());
         UMLClass determinedClass = null;
         if (datatype instanceof UMLClass) {
             determinedClass = (UMLClass) datatype;
         } else {
             determinedClass = deriveRealClass(datatype.getName(), searchClasses);
         }
         return determinedClass;
     }
     
     
     private UMLClass deriveRealClass(String shortName, List<UMLClass> searchClasses) {
         LOG.debug("Deriving real UML class for data type " + shortName);
         UMLClass determinedClass = null;
         String name = shortName;
         if (attributeTypeRepresentsCollection(shortName)) {
             name = stripCollectionRepresentation(shortName);
         } else if (attributeTypeRepresentsGeneric(shortName)) {
             name = cleanUpIsoClassName(name);
         }
         String isoCleanName = cleanUpIsoClassName(name);
         UMLClass candidate = null;
         UMLClass javaCandidate = null;
         UMLClass isoCandidate = null;
         for (UMLClass c : searchClasses) {
             String packName = getFullPackageName(c);
             if (packName.startsWith(LOGICAL_MODEL_PACKAGE_PREFIX)) {
                 String className = c.getName();
                 if (JAVA_PATTERN.matcher(packName).matches() && className.equals(name)) {
                     javaCandidate = c;
                 } else if (ISO_PATTERN.matcher(packName).matches() && isoCleanName.equals(cleanUpIsoClassName(className))) {
                     isoCandidate = c;
                 } else if (name.equals(c.getName())) {
                     candidate = c;
                 }
             }
         }
         // prefer the java, then the ISO, then the non-ISO candidate
         if (javaCandidate != null) {
             LOG.debug("Determined class to be a Java type");
             determinedClass = javaCandidate;
         } else if (isoCandidate != null) {
             LOG.debug("Deterined class to be an ISO type");
             determinedClass = isoCandidate;
         } else {
             LOG.debug("Determined class to be of unknown type");
             determinedClass = candidate;
         }
         return determinedClass;
     }
     
     
     private void annotateAttribute(
         gov.nih.nci.cagrid.metadata.common.UMLAttribute currentAttribute, 
         Collection<UMLTaggedValue> taggedValues) {
         LOG.debug("Annotating attribute " + currentAttribute.getName());
         // if a CADSR_DE_ID is provided, prefer it over the
         // autogenerated EA ID.  EA IDs will all be converted to be 
         // negative values so they're out of the way of valid
         // caDSR IDs
         for (UMLTaggedValue taggedValue : taggedValues) {
             String name = taggedValue.getName();
             String value = taggedValue.getValue();
             if ("ea_guid".equals(name) 
                 && currentAttribute.getPublicID() == 0) {
                 long idValue = Long.MIN_VALUE + Math.abs(value.hashCode());
                 currentAttribute.setPublicID(idValue);
             } else if (XMI_TAG_CADSR_DE_ID.equals(name)) {
                 currentAttribute.setPublicID(Long.parseLong(value));
             } else if (XMI_TAG_CADSR_DE_VERSION.equals(name)) {
                 currentAttribute.setVersion(Float.parseFloat(value));
             } else if (XMIConstants.XMI_TAG_DESCRIPTION.equals(name)) {
                 // set the attribute's description
                 currentAttribute.setDescription(value);
             } else if (name != null && 
                 (name.startsWith(XMIConstants.XMI_TAG_PROPERTY_CONCEPT_CODE)
                 || name.startsWith(XMIConstants.XMI_TAG_PROPERTY_QUALIFIER_CONCEPT_CODE)
                 || name.startsWith(XMIConstants.XMI_TAG_PROPERTY_CONCEPT_PREFERRED_NAME)
                 || name.startsWith(XMIConstants.XMI_TAG_PROPERTY_QUALIFIER_CONCEPT_PREFERRED_NAME)
                 || (name.startsWith(XMIConstants.XMI_TAG_PROPERTY_CONCEPT_DEFINITION) 
                     && !name.startsWith(XMIConstants.XMI_TAG_PROPERTY_CONCEPT_DEFINITION_SOURCE))
                 || (name.startsWith(XMIConstants.XMI_TAG_PROPERTY_QUALIFIER_CONCEPT_DEFINITION) 
                     && !name.startsWith(XMIConstants.XMI_TAG_PROPERTY_QUALIFIER_CONCEPT_DEFINITION_SOURCE)))) {
                 // add semantic metadata to the UML attribute
                 int order = 0;
                 char c = name.charAt(name.length() - 1);
                 if (Character.isDigit(c)) {
                     order = Integer.parseInt(String.valueOf(c));
                 }
                 
                 SemanticMetadata[] currentMetadata = currentAttribute.getSemanticMetadata();
                 List<SemanticMetadata> orderedMetadata = new ArrayList<SemanticMetadata>();
                 Collections.addAll(orderedMetadata, currentMetadata != null ? currentMetadata : new SemanticMetadata[0]);
                 
                 // grow the ordered metadata to the appropriate size if needed
                 int size = orderedMetadata.size();
                 if (size <= order) {
                     for (int i = orderedMetadata.size(); i <= order; i++) {
                         orderedMetadata.add(new SemanticMetadata());
                     }
                 }
 
                 SemanticMetadata sm = orderedMetadata.get(order);
                 if (name.indexOf(XMIConstants.XMI_TAG_CONCEPT_CODE) != -1) {
                     sm.setOrder(Integer.valueOf(order));
                     sm.setConceptCode(value);
                 } else if (name.indexOf(XMIConstants.XMI_TAG_PREFERRED_NAME) != -1) {
                     sm.setConceptName(value);
                 } else if (name.indexOf(XMIConstants.XMI_TAG_CONCEPT_DEFINITION) != -1) {
                     sm.setConceptDefinition(value);
                 }
             }
         }
     }
     
     
     private boolean shouldExcludePackage(String packageName) {
         boolean exclude = false;
         exclude = !packageName.startsWith(LOGICAL_MODEL_PACKAGE_PREFIX);
         if (!exclude) {
             // check regexes
             StringTokenizer regexTokenizer = new StringTokenizer(getPackageExcludeRegex(), ",");
             while (!exclude && regexTokenizer.hasMoreTokens()) {
                 exclude = Pattern.matches(regexTokenizer.nextToken(), packageName);
             }
         }
         return exclude;
     }
     
     
     /**
      * Cleans up the ISO class name in the model to match
      * the Java class name in the common project
      * 
      * eg1) II -> Ii
      * eg2) EN.ON - >EnOn
      * eg3) DSET<II> -> DSet
      * eg4) IVL<TS> ->Ivl<Ts>
      * eg4) AddressPartType -> AddressPartType
      * 
      * @param isoClassName
      * @return
      */
     private String cleanUpIsoClassName(String isoClassName) {
         LOG.debug("Cleaning up ISO class name " + isoClassName);
         String clean = null;
        if (isoClassName.equals("DSET") || isoClassName.startsWith("DSET<")) {
             // DSET requires some magic...
             clean = "DSet"; 
         } else if (isoClassName.equals("BL.NONNULL")) {
             // a special case where there's nothing sane to do
             clean = "BlNonNull";
         } else if (isoClassName.startsWith("IVL<")) {
             String[] parts = isoClassName.split("<");
             clean = cleanUpIsoClassName(parts[0]) + "<" + cleanUpIsoClassName(parts[1]);
         } else {
             // some ISO classes have dots in their name (AD.XP) which need
             // turned into something that standard package / class name parsers
             // will recognize (AdXp)
             StringTokenizer tok = new StringTokenizer(isoClassName, ".");
             StringBuffer cleaned = new StringBuffer();
             while (tok.hasMoreTokens()) {
                 String part = tok.nextToken();
                 // ISO class names usually YELL AT YOU IN ALL CAPS, so calm that down a bit...
                 boolean isAllCaps = isoClassName.toUpperCase().equals(isoClassName);
                 if (isAllCaps) {
                     cleaned.append(part.charAt(0));
                     if (part.length() != 1) {
                         cleaned.append(part.toLowerCase().substring(1));
                     }
                 } else {
                     cleaned.append(part);
                 }
             }
             clean = cleaned.toString();
         }
         LOG.debug("Cleaned up: " + clean);
         return clean;
     }
     
     
     private boolean attributeTypeRepresentsCollection(String datatype) {
         return (datatype.startsWith("Sequence(") || datatype.startsWith("Set("))
             && datatype.endsWith(")");
     }
     
     
     private boolean attributeTypeRepresentsGeneric(String datatype) {
         return datatype.contains("<");
     }
     
     
     private String stripCollectionRepresentation(String name) {
         int start = name.indexOf('(');
         int end = name.indexOf(')', start);
         return name.substring(start + 1, end);
     }
     
     
     private String stripGeneric(String name) {
         int start = name.indexOf('<');
         return name.substring(0, start);
     }
     
     
     private String getGenericSpecificType(String name) {
         int start = name.indexOf('<');
         int end = name.indexOf('>', start);
         return name.substring(start + 1, end);
     }
     
     
     private UMLPackage findIsoPackage(Collection<UMLPackage> packs) {
         for (UMLPackage p : packs) {
             // determine the package name
             StringBuffer buf = new StringBuffer();
             boolean first = true;
             UMLPackage pack = p;
             while (pack != null) {
                 if (!first) {
                     buf.insert(0, '.');   
                 }
                 first = false;
                 buf.insert(0, pack.getName());
                 pack = pack.getParent();
             }
             if (buf.toString().equals(LOGICAL_MODEL_PACKAGE_PREFIX + "gov.nih.nci.iso21090")) {
                 return p;
             } else {
                 UMLPackage maybe = findIsoPackage(p.getPackages());
                 if (maybe != null) {
                     return maybe;
                 }
             }
         }
         return null;
     }
     
     
     private boolean typeIsEnumeration(UMLClass clazz) {
         boolean isEnum = false;
         String stereotype = clazz.getStereotype();
         if (stereotype != null) {
             isEnum = stereotype.toLowerCase().equals("enumeration");
         }
         return isEnum;
     }
 
 
     public static void main(String[] args) {
         ISOSupportDomainModelGenerator generator = new ISOSupportDomainModelGenerator(HandlerEnum.EADefault);
         try {
             System.out.println("generating model");
             DomainModel model = generator.generateDomainModel("test/resources/sdk.xmi");
             System.out.println("serializing");
             FileWriter writer = new FileWriter("sample-domain-model.xml");
             MetadataUtils.serializeDomainModel(model, writer);
             System.out.println(writer.toString());
             writer.close();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
        
        /*
         try {
             XmiInOutHandler handler = XmiHandlerFactory.getXmiHandler(HandlerEnum.EADefault);
             handler.load("test/resources/sdk.xmi");
             UMLModel model = handler.getModel();
             List<UMLClass> classes = generator.getAllClassesInModel(model);
             UMLClass discovered = generator.deriveRealClass("DSET<AD>", classes);
             if (discovered == null) {
                 System.out.println("fail");
             } else {
                 System.out.println(generator.getFullPackageName(discovered) + "." + discovered.getName());
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
        */
     }
 }
