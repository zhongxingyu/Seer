 /*
  * Copyright 2000-2003 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
  *
  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
  *
  * "This product includes software developed by Oracle, Inc. and the National Cancer Institute."
  *
  * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself, wherever such third-party acknowledgments normally appear.
  *
  * 3. The names "The National Cancer Institute", "NCI" and "Oracle" must not be used to endorse or promote products derived from this software.
  *
  * 4. This license does not authorize the incorporation of this software into any proprietary programs. This license does not authorize the recipient to use any trademarks owned by either NCI or Oracle, Inc.
  *
  * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, ORACLE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
  */
 package gov.nih.nci.ncicb.cadsr.loader.event;
 
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 
 import gov.nih.nci.ncicb.cadsr.loader.ReviewTracker;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 import gov.nih.nci.ncicb.cadsr.loader.defaults.*;
 import gov.nih.nci.ncicb.cadsr.loader.ext.*;
 import gov.nih.nci.ncicb.cadsr.loader.ChangeTracker;
 
 import gov.nih.nci.ncicb.cadsr.loader.ReviewTrackerType;
 import gov.nih.nci.ncicb.cadsr.loader.validator.ValidationWarning;
 import gov.nih.nci.ncicb.cadsr.loader.validator.ValidationError;
 import gov.nih.nci.ncicb.cadsr.loader.validator.ValidationItems;
 
 /**
  * This class implements UMLHandler specifically to handle UML events and 
  * convert them into caDSR objects.<br> The handler's responsibility is to 
  * transform events received into cadsr domain objects, and store those objects 
  * in the Elements List.
  *
  * @author <a href="mailto:ludetc@mail.nih.gov">Christophe Ludet</a>
  */
 public class UMLDefaultHandler 
   implements UMLHandler, CadsrModuleListener, RunModeListener {
 
   private ElementsLists elements;
   private Logger logger = Logger.getLogger(UMLDefaultHandler.class.getName());
   private List packageList = new ArrayList();
   
   private ReviewTracker reviewTracker;
 
   private CadsrModule cadsrModule;
 
   // keeps track of the mapping between an oc and the DE that set it's public id
   // key = oc Id / version
   // Value = the DE that set oc id / version
   private Map<String, DataElement> ocMapping = new HashMap<String, DataElement>();
 
   private InheritedAttributeList inheritedAttributes = InheritedAttributeList.getInstance();
   
   public UMLDefaultHandler() {
     this.elements = ElementsLists.getInstance();
   }
 
   public void setRunMode(RunMode runMode) {
     if(runMode.equals(RunMode.Curator)) {
       reviewTracker = reviewTracker.getInstance(ReviewTrackerType.Curator);
     } else {
       reviewTracker = reviewTracker.getInstance(ReviewTrackerType.Owner);
     }
   }
 
   public void newPackage(NewPackageEvent event) {
     logger.info("Package: " + event.getName());
 
 
     // handle this here if the package has GME info. If not, let classes handle package so we don't add empty package.
     if(!StringUtil.isEmpty(event.getGmeNamespace())) {
       ClassificationSchemeItem csi = DomainObjectFactory.newClassificationSchemeItem();
       String pName = event.getName();
       csi.setName(pName);
       if (!packageList.contains(pName)) {
         elements.addElement(csi);
         packageList.add(pName);
       }
       
       AlternateName gmeNamespaceName = DomainObjectFactory.newAlternateName();
       gmeNamespaceName.setType(AlternateName.TYPE_GME_NAMESPACE);
       gmeNamespaceName.setName(event.getGmeNamespace());
       csi.addAlternateName(gmeNamespaceName);
 
     }
   }
 
   public void newOperation(NewOperationEvent event) {
     logger.debug("Operation: " + event.getClassName() + "." +
                  event.getName());
   }
 
   public void newValueDomain(NewValueDomainEvent event) {
     logger.info("Value Domain: " + event.getName());
     
     List<Concept> concepts = createConcepts(event);
 
     ValueDomain vd = DomainObjectFactory.newValueDomain();
 
 
     if(!StringUtil.isEmpty(event.getVdId()) && event.getVdVersion() != null) {
       ValueDomain existingVd = null;
       Map<String, Object> queryFields =
         new HashMap<String, Object>();
       queryFields.put(CadsrModule.PUBLIC_ID, event.getVdId());
       queryFields.put(CadsrModule.VERSION, event.getVdVersion());
       
       List<ValueDomain> result = null;
 
       try {
         result =  new ArrayList<ValueDomain>(cadsrModule.findValueDomain(queryFields));
       } catch (Exception e){
         logger.error("Could not query cadsr module ", e);
       } // end of try-catch
       
       if(result.size() == 0) {
 //         ChangeTracker changeTracker = ChangeTracker.getInstance();
 
         ValidationError item = new ValidationError
           (PropertyAccessor.getProperty("local.vd.doesnt.exist", event.getName(),
                                         event.getVdId() + "v" + event.getVdVersion()), vd);
 
         ValidationItems.getInstance()
           .addItem(item);
         
         vd.setPublicId(null);
         vd.setVersion(null);
 //         changeTracker.put
 //           (className + "." + attributeName, 
 //            true);
       } else {
         existingVd = result.get(0);
         vd.setLongName(existingVd.getLongName());
         vd.setPreferredDefinition(existingVd.getPreferredDefinition());
         vd.setVdType(existingVd.getVdType());
         vd.setDataType(existingVd.getDataType());
 
         vd.setConceptualDomain(existingVd.getConceptualDomain());
         vd.setRepresentation(existingVd.getRepresentation());
 
         vd.setConceptDerivationRule(existingVd.getConceptDerivationRule());
       }
     } else {
       vd.setLongName(event.getName());
       vd.setPreferredDefinition(event.getDescription());
       vd.setVdType(event.getType());
       vd.setDataType(event.getDatatype());
 
       ConceptualDomain cd = DomainObjectFactory.newConceptualDomain();
       cd.setPublicId(event.getCdId());
       cd.setVersion(event.getCdVersion());
       
       // un comment the following to lookup CD. 
       //     Map<String, Object> queryFields = 
       //         new HashMap<String, Object>();
       //     queryFields.put(CadsrModule.PUBLIC_ID, event.getPersistenceId());
       //     queryFields.put(CadsrModule.VERSION, event.getPersistenceVersion());
       //     List<ConceptualDomain> result = null;
       
       //     try {
       //       result =  new ArrayList<ConceptualDomain>(cadsrModule.findConceptualDomain(queryFields));
       //     } catch (Exception e){
       //       logger.error("Could not query cadsr module ", e);
       //     } // end of try-catch
       
       //     if(result.size() > 0) {
       //       cd = result.get(0);
       //     }
       
       vd.setConceptualDomain(cd);
 
       Representation repTerm = DomainObjectFactory.newRepresentation();
       repTerm.setPublicId(event.getRepTermId());
       repTerm.setVersion(event.getRepTermVersion());
       vd.setRepresentation(repTerm);
 
       //     if(concepts.size() > 0)
       vd.setConceptDerivationRule(ConceptUtil.createConceptDerivationRule(concepts, false));
 
     }      
     
     // create CSI for package (since 3.2)
     ClassificationSchemeItem csi = DomainObjectFactory.newClassificationSchemeItem();
     String pName = event.getPackageName();
     csi.setName(pName);
     if (!packageList.contains(pName)) {
       elements.addElement(csi);
       packageList.add(pName);
     }
 
     AdminComponentClassSchemeClassSchemeItem acCsCsi = DomainObjectFactory.newAdminComponentClassSchemeClassSchemeItem();
     ClassSchemeClassSchemeItem csCsi = DomainObjectFactory.newClassSchemeClassSchemeItem();
     csCsi.setCsi(csi);
     acCsCsi.setCsCsi(csCsi);
     List<AdminComponentClassSchemeClassSchemeItem> l = new ArrayList<AdminComponentClassSchemeClassSchemeItem>();
     l.add(acCsCsi);
     vd.setAcCsCsis(l);
 
     elements.addElement(vd);
     reviewTracker.put(event.getName(), event.isReviewed());
   }
 
   public void newValueMeaning(NewValueMeaningEvent event) {
     logger.info("Value Meaning: " + event.getName());
     
     List<Concept> concepts = createConcepts(event);
 
     ValueDomain vd = LookupUtil.lookupValueDomain(event.getValueDomainName());
 
     ValueMeaning vm = DomainObjectFactory.newValueMeaning();
     vm.setLongName(event.getName());
 
     vm.setLifecycle(UMLDefaults.getInstance().getLifecycle());
 
     Concept[] vmConcepts = new Concept[concepts.size()];
     concepts.toArray(vmConcepts);
 
 //     Definition vmAltDef = DomainObjectFactory.newDefinition();
 //     vmAltDef.setType(Definition.TYPE_UML_VM);
      
 //     if(!StringUtil.isEmpty(event.getDescription())) {
 //       vmAltDef.setDefinition(event.getDescription());
 //     } else {
 // //       vmAltDef.setDefinition(ValueMeaning.DEFAULT_DEFINITION);
 //     }
 
 
     // if this VM has concepts, then use evs definition 
     // if VM has no concept, then use user defined definition
     if(concepts != null && concepts.size() > 0) {
       vm.setPreferredDefinition(ConceptUtil
                                 .preferredDefinitionFromConcepts(vmConcepts));
       
 //       if(!StringUtil.isEmpty(event.getDescription())) 
 //         vm.addDefinition(vmAltDef);
 
     } else {
       if(!StringUtil.isEmpty(event.getDescription())) {
         vm.setPreferredDefinition(event.getDescription());
       } else 
         vm.setPreferredDefinition(ValueMeaning.DEFAULT_DEFINITION);
 
 //       if(!StringUtil.isEmpty(event.getDescription())) 
 //         vm.addDefinition(vmAltDef);
 
     }
     
     vm.setConceptDerivationRule(ConceptUtil.createConceptDerivationRule(concepts, true));
 
     PermissibleValue pv = DomainObjectFactory.newPermissibleValue();
     pv.setValueMeaning(vm);
     pv.setValue(event.getName());
     pv.setLifecycle(UMLDefaults.getInstance().getLifecycle());
     
     vd.addPermissibleValue(pv);
 
     AlternateName vmAltName = DomainObjectFactory.newAlternateName();
     vmAltName.setType(AlternateName.TYPE_UML_VM);
     vmAltName.setName(event.getName());
     vm.addAlternateName(vmAltName);
 
     if(!StringUtil.isEmpty(event.getDescription())) {
       Definition vmAltDef = DomainObjectFactory.newDefinition();
       vmAltDef.setType(Definition.TYPE_UML_VM);
       vmAltDef.setDefinition(event.getDescription());
       vm.addDefinition(vmAltDef);
     }
 
     elements.addElement(vm);
     reviewTracker.put("ValueDomains." + event.getValueDomainName() + "." + event.getName(), event.isReviewed());
 
   }
   
   public void newClass(NewClassEvent event) {
     logger.info("Class: " + event.getName());
     
     List<Concept> concepts = createConcepts(event);
 
     ObjectClass oc = DomainObjectFactory.newObjectClass();
 
     // store concept codes in preferredName
     oc.setPreferredName(ConceptUtil.preferredNameFromConcepts(concepts));
     
     oc.setLongName(event.getName());
     if(event.getDescription() != null && event.getDescription().length() > 0)
       oc.setPreferredDefinition(event.getDescription());
     else 
       oc.setPreferredDefinition("");
 
     elements.addElement(oc);
     reviewTracker.put(event.getName(), event.isReviewed());
     
     ClassificationSchemeItem csi = DomainObjectFactory.newClassificationSchemeItem();
 
     String pName = event.getPackageName();
 
     csi.setName(pName);
     
     if (!packageList.contains(pName)) {
       elements.addElement(csi);
       packageList.add(pName);
     }
 
     // Store package names
     AdminComponentClassSchemeClassSchemeItem acCsCsi = DomainObjectFactory.newAdminComponentClassSchemeClassSchemeItem();
     ClassSchemeClassSchemeItem csCsi = DomainObjectFactory.newClassSchemeClassSchemeItem();
     csCsi.setCsi(csi);
     acCsCsi.setCsCsi(csCsi);
     List<AdminComponentClassSchemeClassSchemeItem> l = new ArrayList<AdminComponentClassSchemeClassSchemeItem>();
     l.add(acCsCsi);
     oc.setAcCsCsis(l);
 
     AlternateName fullName = DomainObjectFactory.newAlternateName();
     fullName.setType(AlternateName.TYPE_CLASS_FULL_NAME);
     fullName.setName(event.getName());
     AlternateName className = DomainObjectFactory.newAlternateName();
     className.setType(AlternateName.TYPE_UML_CLASS);
     className.setName(event.getName().substring(event.getName().lastIndexOf(".") + 1));
 
     if(!StringUtil.isEmpty(event.getGmeNamespace())) {
       AlternateName gmeNamespaceName = DomainObjectFactory.newAlternateName();
       gmeNamespaceName.setType(AlternateName.TYPE_GME_NAMESPACE);
       gmeNamespaceName.setName(event.getGmeNamespace());
       oc.addAlternateName(gmeNamespaceName);
     }
     if(!StringUtil.isEmpty(event.getGmeXmlElement())) {
       AlternateName gmeXmlElementName = DomainObjectFactory.newAlternateName();
       gmeXmlElementName.setType(AlternateName.TYPE_GME_XML_ELEMENT);
       gmeXmlElementName.setName(event.getGmeXmlElement());
       oc.addAlternateName(gmeXmlElementName);
     }
 
     oc.addAlternateName(fullName);
     oc.addAlternateName(className);
 
   }
 
   public void newAttribute(NewAttributeEvent event) {
     logger.info("Attribute: " + event.getClassName() + "." +
                  event.getName());
 
 
     DataElement de = DomainObjectFactory.newDataElement();
 
     // populate if there is valid existing mapping
     DataElement existingDe = null;
     if(event.getPersistenceId() != null) {
       existingDe = findExistingDe(event.getPersistenceId(), event.getPersistenceVersion(), de, event.getClassName(), event.getName());
     } 
 
     List<Concept> concepts = createConcepts(event);
     
     Property prop = DomainObjectFactory.newProperty();
 
     // store concept codes in preferredName
     prop.setPreferredName(ConceptUtil.preferredNameFromConcepts(concepts));
 
     //     prop.setPreferredName(event.getName());
     prop.setLongName(event.getName());
 
     String propName = event.getName();
 
     String s = event.getClassName();
     int ind = s.lastIndexOf(".");
     String className = s.substring(ind + 1);
     String packageName = s.substring(0, ind);
 
     DataElementConcept dec = DomainObjectFactory.newDataElementConcept();
     dec.setLongName(className + ":" + propName);
     dec.setProperty(prop);
     
     logger.debug("DEC LONG_NAME: " + dec.getLongName());
     
     ObjectClass oc = DomainObjectFactory.newObjectClass();
     List<ObjectClass> ocs = elements.getElements(oc);
     
     for (ObjectClass o : ocs) {
       String fullClassName = null;
       for(AlternateName an : o.getAlternateNames()) {
         if(an.getType().equals(AlternateName.TYPE_CLASS_FULL_NAME))
           fullClassName = an.getName();
       }
       if (fullClassName.equals(event.getClassName())) {
         oc = o;
       }
     }
 
     dec.setObjectClass(oc);
     de.setDataElementConcept(dec);
 
     String datatype = event.getType().trim();
     // save the datatype. We will use it if we clear a CDE mapping.
     elements.addElement(new AttributeDatatypePair(packageName + "." + className + "." + propName, datatype.toLowerCase()));
 
     if(DatatypeMapping.getKeys().contains(datatype.toLowerCase())) 
       datatype = DatatypeMapping.getMapping().get(datatype.toLowerCase());
     
     if(existingDe != null) {
       mapToExistingDE(oc, de, existingDe, event.getClassName(), event.getName());
 
     } else {
       de.setLongName(dec.getLongName() + " " + event.getType());
     }
     
     if(de.getValueDomain() == null) {
 
       ValueDomain vd = DomainObjectFactory.newValueDomain();
       vd.setLongName(datatype);
       
       if(event.getTypeId() != null) {
         populateExistingVd(vd, event.getTypeId(), event.getTypeVersion(), event.getClassName() + "." + event.getName());
       }
       
       de.setValueDomain(vd);
       
     }
 
     logger.debug("DE LONG_NAME: " + de.getLongName());
 
     // Store alt Name for DE:
     // packageName.ClassName.PropertyName
     AlternateName fullName = DomainObjectFactory.newAlternateName();
     fullName.setType(AlternateName.TYPE_FULL_NAME);
     fullName.setName(packageName + "." + className + "." + propName);
     de.addAlternateName(fullName);
     
     // Store alt Name for DE:
     // ClassName:PropertyName
     fullName = DomainObjectFactory.newAlternateName();
     fullName.setType(AlternateName.TYPE_UML_DE);
     fullName.setName(className + ":" + propName);
     de.addAlternateName(fullName);
 
 
     if(!StringUtil.isEmpty(event.getGmeXmlLocRef())) {
       AlternateName gmeXmlLocRefName = DomainObjectFactory.newAlternateName();
       gmeXmlLocRefName.setType(AlternateName.TYPE_GME_XML_LOC_REF);
       gmeXmlLocRefName.setName(event.getGmeXmlLocRef());
       de.addAlternateName(gmeXmlLocRefName);
     }
 
     if(!StringUtil.isEmpty(event.getDescription())) {
       Definition altDef = DomainObjectFactory.newDefinition();
       altDef.setType(Definition.TYPE_UML_DE);
       altDef.setDefinition(event.getDescription());
       de.addDefinition(altDef);
 
       altDef = DomainObjectFactory.newDefinition();
       altDef.setType(Definition.TYPE_UML_DEC);
       altDef.setDefinition(event.getDescription());
       dec.addDefinition(altDef);
     }
 
     // Add packages to Prop, DE and DEC.
     prop.setAcCsCsis(oc.getAcCsCsis());
     de.setAcCsCsis(oc.getAcCsCsis());
     dec.setAcCsCsis(oc.getAcCsCsis());
 
     reviewTracker.put(event.getClassName() + "." + event.getName(), event.isReviewed());
 
     elements.addElement(de);
     elements.addElement(dec);
     elements.addElement(prop);
   }
 
     private DataElement findExistingDe(String id, Float version, DataElement de, String className, String attributeName) {
     
       DataElement existingDe = null;
       Map<String, Object> queryFields =
         new HashMap<String, Object>();
       queryFields.put(CadsrModule.PUBLIC_ID, id);
       queryFields.put(CadsrModule.VERSION, version);
 
       List<DataElement> result = null;
 
       try {
         result =  new ArrayList<DataElement>(cadsrModule.findDataElement(queryFields));
       } catch (Exception e){
         logger.error("Could not query cadsr module ", e);
       } // end of try-catch
 
       if(result.size() == 0) {
         ChangeTracker changeTracker = ChangeTracker.getInstance();
 
         ValidationError item = new ValidationError(PropertyAccessor.getProperty("de.doesnt.exist", className + "." + attributeName,
                                                                                id + "v" + version), de);
         item.setIncludeInInherited(true);
         ValidationItems.getInstance()
           .addItem(item);
 
         
         de.setPublicId(null);
         de.setVersion(null);
         changeTracker.put
           (className + "." + attributeName, 
            true);
       } else {
         existingDe = result.get(0);
       }
       return existingDe;
     }
 
     private void mapToExistingDE(ObjectClass oc, DataElement de, DataElement existingDe, String className, String attributeName) {
 
       if(existingDe.getDataElementConcept().getObjectClass() == null
          || existingDe.getDataElementConcept().getProperty() == null) {
 
        ValidationWarning item = new ValidationWarning(PropertyAccessor.getProperty("de.invalid"),de); 
         ValidationItems.getInstance()
           .addItem(item);
         return;
       }
 
       if(oc.getPublicId() != null) {
         // Verify conflicts
 
         if(!existingDe.getDataElementConcept().getObjectClass().getPublicId().equals(oc.getPublicId()) || !existingDe.getDataElementConcept().getObjectClass().getVersion().equals(oc.getVersion())) {
           // Oc was already mapped by an existing DE. This DE conflicts with the previous mapping. 
 
           
           ValidationError item = new ValidationError(PropertyAccessor.getProperty("de.conflict", new String[] 
               {className + "." + attributeName,
                ocMapping.get(ConventionUtil.publicIdVersion(oc)).getLongName()}), de);
 
           item.setIncludeInInherited(true);
           ValidationItems.getInstance()
             .addItem(item);
           
 
         }
       } else {
         oc.setPublicId(existingDe.getDataElementConcept().getObjectClass().getPublicId());
         oc.setVersion(existingDe.getDataElementConcept().getObjectClass().getVersion());
         // Keep track so if there's conflict, we know both ends of the conflict
         ocMapping.put(ConventionUtil.publicIdVersion(oc), de);
 
         oc.setLongName(existingDe.getDataElementConcept().getObjectClass().getLongName());
         oc.setPreferredName("");
         ChangeTracker changeTracker = ChangeTracker.getInstance();
         changeTracker.put
           (className, 
            true);
       }
       
       // The DE's property was already set, probably by the parent. It must be the same. We don't override here.
       if(de.getDataElementConcept().getProperty().getPublicId() != null) {
         if(!existingDe.getDataElementConcept().getProperty().getPublicId().equals(de.getDataElementConcept().getProperty().getPublicId())
            || 
            !existingDe.getDataElementConcept().getProperty().getVersion().equals(de.getDataElementConcept().getProperty().getVersion())
            ) {
           ValidationError item = new ValidationError(PropertyAccessor.getProperty("inherited.and.parent.property.mismatch", new String[] 
             {className + "." + attributeName,
              ocMapping.get(ConventionUtil.publicIdVersion(oc)).getLongName()}), de);
           
           item.setIncludeInInherited(true);
           ValidationItems.getInstance()
             .addItem(item);
         }
         return;
       }
 
       de.getDataElementConcept().getProperty().setPublicId(existingDe.getDataElementConcept().getProperty().getPublicId());
       de.getDataElementConcept().getProperty().setVersion(existingDe.getDataElementConcept().getProperty().getVersion());
 
       de.setLongName(existingDe.getLongName());
       de.setContext(existingDe.getContext());
       de.setPublicId(existingDe.getPublicId());
       de.setVersion(existingDe.getVersion());
       de.setLatestVersionIndicator(existingDe.getLatestVersionIndicator());
       de.setValueDomain(existingDe.getValueDomain());
     }
 
     private void populateExistingVd(ValueDomain vd, String vdId, Float vdVersion, String attributeName) {
       Map<String, Object> queryFields =
         new HashMap<String, Object>();
       queryFields.put(CadsrModule.PUBLIC_ID, vdId);
       queryFields.put(CadsrModule.VERSION, vdVersion);
 
       List<ValueDomain> result = null;
 
       try {
         result =  new ArrayList<ValueDomain>(cadsrModule.findValueDomain(queryFields));
       } catch (Exception e){
         logger.error("Could not query cadsr module ", e);
       } // end of try-catch
 
       if(result.size() == 0) {
         ChangeTracker changeTracker = ChangeTracker.getInstance();
 
         ValidationError item = new ValidationError(PropertyAccessor.getProperty("vd.doesnt.exist", attributeName,
                                                                                vdId + "v" + vdVersion), null);
         item.setIncludeInInherited(true);
         ValidationItems.getInstance()
           .addItem(item);
 
       
         vd.setPublicId(null);
         vd.setVersion(null);
         changeTracker.put
         (attributeName, 
          true);
       } else {
         ValueDomain existingVd  = result.get(0);
         vd.setLongName(existingVd.getLongName());
         vd.setPublicId(existingVd.getPublicId());
         vd.setVersion(existingVd.getVersion());
         vd.setContext(existingVd.getContext());
         vd.setDataType(existingVd.getDataType());
       }
     }
 
   public void newInterface(NewInterfaceEvent event) {
     logger.debug("Interface: " + event.getName());
   }
 
   public void newStereotype(NewStereotypeEvent event) {
     logger.debug("Stereotype: " + event.getName());
   }
 
   public void newDataType(NewDataTypeEvent event) {
     logger.debug("DataType: " + event.getName());
   }
 
   public void newAssociation(NewAssociationEvent event) {
     ObjectClassRelationship ocr = DomainObjectFactory.newObjectClassRelationship();
     ObjectClass oc = DomainObjectFactory.newObjectClass();
     
     NewAssociationEndEvent aEvent = event.getAEvent();
     NewAssociationEndEvent bEvent = event.getBEvent();
     NewAssociationEndEvent sEvent = null;
     NewAssociationEndEvent tEvent = null; 
     
     List<ObjectClass> ocs = elements.getElements(oc);
 
     boolean aDone = false, 
       bDone = false;
 
     for(ObjectClass o : ocs) {
       String classFullName = null;
       for(AlternateName an : o.getAlternateNames()) {
         if(an.getType().equals(AlternateName.TYPE_CLASS_FULL_NAME))
           classFullName = an.getName();
       }
       if (classFullName == null) {
           System.err.println("No full class name found for "+o.getLongName());
           continue;
       }
 
       if (!aDone && (classFullName.equals(aEvent.getClassName()))) {
         if (event.getDirection().equals("B")) {
           sEvent = aEvent;
           ocr.setSource(o);
           ocr.setSourceRole(aEvent.getRoleName());
           ocr.setSourceLowCardinality(aEvent.getLowCardinality());
           ocr.setSourceHighCardinality(aEvent.getHighCardinality());
         } else {
           tEvent = aEvent;
           ocr.setTarget(o);
           ocr.setTargetRole(aEvent.getRoleName());
           ocr.setTargetLowCardinality(aEvent.getLowCardinality());
           ocr.setTargetHighCardinality(aEvent.getHighCardinality());
         }
         aDone = true;
       }
       if (!bDone && (classFullName.equals(bEvent.getClassName()))) {
         if (event.getDirection().equals("B")) {
           tEvent = bEvent;
           ocr.setTarget(o);
           ocr.setTargetRole(bEvent.getRoleName());
           ocr.setTargetLowCardinality(bEvent.getLowCardinality());
           ocr.setTargetHighCardinality(bEvent.getHighCardinality());
         } else {
           sEvent = bEvent;
           ocr.setSource(o);
           ocr.setSourceRole(bEvent.getRoleName());
           ocr.setSourceLowCardinality(bEvent.getLowCardinality());
           ocr.setSourceHighCardinality(bEvent.getHighCardinality());
         }
         bDone = true;
       }
     }
 
     if (event.getDirection().equals("AB")) {
       ocr.setDirection(ObjectClassRelationship.DIRECTION_BOTH);
     } else {
       ocr.setDirection(ObjectClassRelationship.DIRECTION_SINGLE);
     }
 
     ocr.setLongName(event.getRoleName());
     ocr.setType(ObjectClassRelationship.TYPE_HAS);
 
     if(event.getConcepts() != null && event.getConcepts().size() > 0)
       ocr.setConceptDerivationRule(
         ConceptUtil.createConceptDerivationRule(createConcepts(event), true));
 
     if(sEvent.getConcepts() != null && sEvent.getConcepts().size() > 0)
       ocr.setSourceRoleConceptDerivationRule(
         ConceptUtil.createConceptDerivationRule(createConcepts(sEvent), true));
 
     if(tEvent.getConcepts() != null && tEvent.getConcepts().size() > 0)
       ocr.setTargetRoleConceptDerivationRule(
         ConceptUtil.createConceptDerivationRule(createConcepts(tEvent), true));
     
     if(!aDone)
       logger.debug("!aDone: " + aEvent.getClassName() + " -- " + bEvent.getClassName());
 
     if(!bDone) 
       logger.debug("!bDone: " + aEvent.getClassName() + " -- " + bEvent.getClassName());
 
     if(!StringUtil.isEmpty(event.getGmeSourceLocRef())) {
       AlternateName an = DomainObjectFactory.newAlternateName();
       an.setType(AlternateName.TYPE_GME_SRC_XML_LOC_REF);
       an.setName(event.getGmeSourceLocRef());
       ocr.addAlternateName(an);
     }
 
     if(!StringUtil.isEmpty(event.getGmeTargetLocRef())) {
       AlternateName an = DomainObjectFactory.newAlternateName();
       an.setType(AlternateName.TYPE_GME_TARGET_XML_LOC_REF);
       an.setName(event.getGmeTargetLocRef());
       ocr.addAlternateName(an);
     }
 
     elements.addElement(ocr);
 
     OCRRoleNameBuilder nameBuilder = new OCRRoleNameBuilder();
     String fullName = nameBuilder.buildRoleName(ocr);
     reviewTracker.put(fullName, event.isReviewed());
     reviewTracker.put(fullName+" Source", sEvent.isReviewed());
     reviewTracker.put(fullName+" Target", tEvent.isReviewed());
   }
 
   public void newGeneralization(NewGeneralizationEvent event) {
     ObjectClassRelationship ocr = DomainObjectFactory.newObjectClassRelationship();
 
     AlternateName an = DomainObjectFactory.newAlternateName();
     an.setName(event.getParentClassName());
     an.setType(AlternateName.TYPE_CLASS_FULL_NAME);
     ocr.setTarget(LookupUtil.lookupObjectClass(an));
     
     an.setName(event.getChildClassName());
     ocr.setSource(LookupUtil.lookupObjectClass(an));
 
     ocr.setType(ObjectClassRelationship.TYPE_IS);
 
     // Inherit all attributes
     // Find all DECs:
     ObjectClass parentOc = ocr.getTarget(),
       childOc = ocr.getSource();
 
     if(childOc == null || parentOc == null) {
       logger.warn("Skipping generalization because parent or child can't be found. Did you filter out some classes?");
       return;
     }
 
     List newElts = new ArrayList();
     List<DataElement> des = elements.getElements(DomainObjectFactory.newDataElement());
     if(des != null)
       for(DataElement de : des) {
         DataElementConcept dec = de.getDataElementConcept();
         if(dec.getObjectClass() == parentOc) {
 
           DataElementConcept newDec = DomainObjectFactory.newDataElementConcept();
           newDec.setProperty(dec.getProperty());
           newDec.setObjectClass(childOc);
 
 //           for(Definition def : dec.getDefinitions()) {
 //             newDec.addDefinition(def);
 //           }
           
           String propName = newDec.getProperty().getLongName();
           
           String s = event.getChildClassName();
           int ind = s.lastIndexOf(".");
           String className = s.substring(ind + 1);
           String packageName = s.substring(0, ind);
 
           newDec.setLongName(className + ":" + propName);		
           DataElement newDe = DomainObjectFactory.newDataElement();
           newDe.setDataElementConcept(newDec);
           newDe.setLongName(newDec.getLongName() + " " + de.getValueDomain().getLongName());
 
           // check existing de mapping
           IdVersionPair deIdVersionPair = event.getPersistenceMapping(propName);
           if(deIdVersionPair != null) {
             DataElement existingDe = findExistingDe(deIdVersionPair.getId(), deIdVersionPair.getVersion(), newDe, className, propName);
             
             if(existingDe != null) {
               mapToExistingDE(childOc, newDe, existingDe, className, propName);
             }
           } 
           if(newDe.getPublicId() == null) {
             // check local VD mapping
             String localType = event.getDatatypeMapping(propName);
             if(localType != null) {
               ValueDomain vd = DomainObjectFactory.newValueDomain();
               vd.setLongName(localType);
               newDe.setValueDomain(vd);
             } else {
               // check existing vd mapping
               IdVersionPair vdIdVersionPair = event.getTypeMapping(propName);
               if(vdIdVersionPair != null) {
                 ValueDomain existingVd = DomainObjectFactory.newValueDomain();
                 populateExistingVd(existingVd, vdIdVersionPair.getId(), vdIdVersionPair.getVersion(), className + "." + propName);
                 newDe.setValueDomain(existingVd);
               } else {
                 ValueDomain newVD = DomainObjectFactory.newValueDomain();
                 newVD.setLongName(de.getValueDomain().getLongName());
                 newVD.setPublicId(de.getValueDomain().getPublicId());
                 newVD.setVersion(de.getValueDomain().getVersion()); 
                 newVD.setContext(de.getValueDomain().getContext());
                 newDe.setValueDomain(newVD);
               }
             }
           }
         
         
           
           Boolean isReviewed = event.getReview(propName);
           if(isReviewed != null)
             reviewTracker.put(event.getChildClassName() + "." + propName, isReviewed);
           
 
 //           for(Definition def : de.getDefinitions()) {
           for(Definition def : inheritedAttributes.getTopParent(de).getDefinitions()) {
             if(def.getType().equals(Definition.TYPE_UML_DE)) {
               Definition newDef = DomainObjectFactory.newDefinition();
               newDef.setType(Definition.TYPE_UML_DE);
               newDef.setDefinition(childOc.getPreferredDefinition() + " " + def.getDefinition());
               newDe.addDefinition(newDef);
             }
           }
 
           AlternateName fullName = DomainObjectFactory.newAlternateName();
           fullName.setType(AlternateName.TYPE_FULL_NAME);
           fullName.setName(packageName + "." + className + "." + propName);
           newDe.addAlternateName(fullName);
           
           // Store alt Name for DE:
           // ClassName:PropertyName
           fullName = DomainObjectFactory.newAlternateName();
           fullName.setType(AlternateName.TYPE_UML_DE);
           fullName.setName(className + ":" + propName);
           newDe.addAlternateName(fullName);
 
           
 
 
 //           for(Iterator it2 = de.getAlternateNames().iterator(); it2.hasNext();) {
 //             AlternateName an = (AlternateName)it2.next();
 //             newDe.addAlternateName(an);
 //           }
 
           newDe.setAcCsCsis(childOc.getAcCsCsis());
           newDec.setAcCsCsis(childOc.getAcCsCsis());
 
           Property oldProp = de.getDataElementConcept().getProperty();
           List oldAcCsCsis = oldProp.getAcCsCsis();
           List newAcCsCsis = new ArrayList(childOc.getAcCsCsis());
           newAcCsCsis.addAll(oldAcCsCsis);
           oldProp.setAcCsCsis(newAcCsCsis);
 
 //           newElts.add(newProp);
           newElts.add(newDe);
           newElts.add(newDec);
 
           inheritedAttributes.add(newDe, de);
 
         }
       }
     
     for(Iterator it = newElts.iterator(); it.hasNext();
         elements.addElement(it.next()));
     
     
     elements.addElement(ocr);
 
     logger.debug("Generalization: ");
     logger.debug("Source:");
     logger.debug("-- " + ocr.getSource().getLongName());
     logger.debug("Target: ");
     if(ocr.getTarget() != null)
       logger.debug("-- " + ocr.getTarget().getLongName());
     else {
       logger.error("Target does not exist: ");
       logger.error("Parent: " + event.getParentClassName());
     }
 
       
   }
 
   private Concept newConcept(NewConceptEvent event) {
     Concept concept = DomainObjectFactory.newConcept();
 
     concept.setPreferredName(event.getConceptCode());
     concept.setPreferredDefinition(event.getConceptDefinition());
     concept.setDefinitionSource(event.getConceptDefinitionSource());
     concept.setLongName(event.getConceptPreferredName());
 
     elements.addElement(concept);
     return concept;
   }
 
   private List<Concept> createConcepts(NewConceptualEvent event) {
     List<Concept> concepts = new ArrayList<Concept>();
     List<NewConceptEvent> conEvs = event.getConcepts();
     for(NewConceptEvent conEv : conEvs) {
       concepts.add(newConcept(conEv));
     }
 
     return concepts;
   }
 
 
 //  private void verifyConcepts(AdminComponent cause, List concepts) {
 //    for(Iterator it = concepts.iterator(); it.hasNext(); ) {
 //      Concept concept = (Concept)it.next();
 //      if(StringUtil.isEmpty(concept.getPreferredName())) {
 //        ValidationItems.getInstance()
 //          .addItem(new ValidationError(
 //                                       PropertyAccessor.getProperty("validation.concept.missing.for", cause.getLongName()),
 //                                       cause));
 ////         elements.addElement(new ConceptError(
 ////                        ConceptError.SEVERITY_ERROR,
 ////                        PropertyAccessor.getProperty("validation.concept.missing.for", eltName)));
 //      }
 //    }
 //  }
 
   public void setCadsrModule(CadsrModule module) {
     this.cadsrModule = module;
   }
 
   public void beginParsing() {}
   public void endParsing() {}
   public void addProgressListener(ProgressListener listener) {}
 }
