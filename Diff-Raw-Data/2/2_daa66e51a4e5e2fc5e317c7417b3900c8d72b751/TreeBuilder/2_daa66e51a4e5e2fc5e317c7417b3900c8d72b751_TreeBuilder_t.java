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
 package gov.nih.nci.ncicb.cadsr.loader.ui.tree;
 
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.loader.defaults.UMLDefaults;
 import gov.nih.nci.ncicb.cadsr.loader.*;
 import gov.nih.nci.ncicb.cadsr.loader.validator.*;
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.*;
 import gov.nih.nci.ncicb.cadsr.loader.ReviewTracker;
 
 import java.util.*;
 
 public class TreeBuilder implements UserPreferencesListener {
 
   private ElementsLists elements;
   private UMLDefaults defaults = UMLDefaults.getInstance();
 
   private UMLNode rootNode;
 
   private List<TreeListener> treeListeners = new ArrayList();
   private boolean inClassAssociations = false,
     showAssociations = true, showValueDomains = true,
     showInheritedAttributes = false;
   
   private ReviewTracker reviewTracker;
   private static TreeBuilder instance = new TreeBuilder();
   
   private TreeBuilder() {
     RunMode runMode = (RunMode)(UserSelections.getInstance().getProperty("MODE"));
     if(runMode.equals(RunMode.Curator)) {
       reviewTracker = ReviewTracker.getInstance(ReviewTrackerType.Curator);
     } else {
       reviewTracker = ReviewTracker.getInstance(ReviewTrackerType.Owner);
     }
   }
 
   public static TreeBuilder getInstance() {
     return instance;
   }
   
   public void init() 
   {
 
     UserPreferences prefs = UserPreferences.getInstance();
     prefs.addUserPreferencesListener(this);
 
     UserSelections selections = UserSelections.getInstance();
     
     inClassAssociations = new Boolean (prefs.getViewAssociationType());
 
     // only show association node in Review Mode
     showAssociations = selections.getProperty("MODE").equals(RunMode.Reviewer);
 
     //showValueDomains = !selections.getProperty("MODE").equals(RunMode.Curator);
     
     showInheritedAttributes = prefs.getShowInheritedAttributes();
   }
 
   public UMLNode buildTree(ElementsLists elements) {
 
     this.elements = elements;
 
     rootNode = new RootNode();
     doPackages(rootNode);
 
     if(showValueDomains) {
       UMLNode vdNode = new PackageNode("Value Domain", "Value Domains");
       doValueDomains(vdNode);
       if(vdNode.getChildren().size() > 0)
         rootNode.addChild(vdNode);
     }
     
     if(!inClassAssociations && showAssociations)
       doAssociations(rootNode);
 
     return rootNode;
   }
 
   public UMLNode getRootNode() {
     return rootNode;
   }
 
   private List<ValidationItem> findValidationItems(Object o) {
     List<ValidationItem> result = new ArrayList<ValidationItem>();
 
     ValidationItems items = ValidationItems.getInstance();
 
     Set<ValidationError> errors = items.getErrors();
     for(ValidationError error : errors) {
       if(error.getRootCause() == o)
         result.add(error);
     }
 
     Set<ValidationWarning> warnings = items.getWarnings();
     for(ValidationWarning warning : warnings) {
       if(warning.getRootCause() == o)
         result.add(warning);
     }
     return result;
   }
 
   private void doPackages(UMLNode parentNode) {
     
     ClassificationSchemeItem pkg = DomainObjectFactory.newClassificationSchemeItem();
     List<ClassificationSchemeItem> packages = elements.getElements(pkg);
     
     for(ClassificationSchemeItem pack : packages) {
       String alias = defaults.getPackageDisplay(pack.getName()); 
         UMLNode node = new PackageNode(pack.getName(), alias);
         parentNode.addChild(node);
 
         doClasses(node);
     }
   }
 
   private void doClasses(UMLNode parentNode) {
     // Find all classes which are in this package
     String packageName = parentNode.getFullPath();
 
     ObjectClass oc = DomainObjectFactory.newObjectClass();
     List<ObjectClass> ocs = elements.getElements(oc);
 
     for(ObjectClass o : ocs) {
       String className = null;
       for(AlternateName an : o.getAlternateNames()) {
         if(an.getType().equals(AlternateName.TYPE_CLASS_FULL_NAME))
           className = an.getName();
       }
 
       int ind = className.lastIndexOf(".");
       packageName = className.substring(0, ind);
       if(packageName.equals(parentNode.getFullPath())) {
         UMLNode node = new ClassNode(o);
    
         parentNode.addChild(node);
      
         ((ClassNode) node).setReviewed(
           reviewTracker.get(node.getFullPath()));
         
         doAttributes(node);
         if(inClassAssociations && showAssociations)
           doAssociations(node,o);
         
         List<ValidationItem> items = findValidationItems(o);
         for(ValidationItem item : items) {
           ValidationNode vNode = null;
           if (item instanceof ValidationWarning) {
             vNode = new WarningNode(item);
           } else {
             vNode = new ErrorNode(item);
           }
           node.addValidationNode(vNode);
         }
       }
     }
 
   }
 
   private void doAttributes(UMLNode parentNode) {
     // Find all DEs that have this OC.
     DataElement o = DomainObjectFactory.newDataElement();
     List<DataElement> des = elements.getElements(o);
     List<UMLNode> inherited = new ArrayList();
     PackageNode inheritedPackage = new PackageNode("Inherited Attributes", "Inherited Attributes");
     
       for(DataElement de : des) {
         try {
           String fullClassName = null;
           for(AlternateName an : de.getDataElementConcept().getObjectClass().getAlternateNames()) {
             if(an.getType().equals(AlternateName.TYPE_CLASS_FULL_NAME))
               fullClassName = an.getName();
           }
 
           if(fullClassName.equals(parentNode.getFullPath())) {
             UMLNode node = new AttributeNode(de);
 
           
             Boolean reviewed = reviewTracker.get(node.getFullPath());
             if(reviewed != null) {
               parentNode.addChild(node);
               ((AttributeNode) node).setReviewed(reviewed);
             }
             else {
               node = new InheritedAttributeNode(de);
               inherited.add(node);
             }
             
             List<ValidationItem> items = findValidationItems(de.getDataElementConcept().getProperty());
             for(ValidationItem item : items) {
               ValidationNode vNode = null;
               if (item instanceof ValidationWarning) {
                 vNode = new WarningNode(item);
               } else {
                 vNode = new ErrorNode(item);
               }
               node.addValidationNode(vNode);
             }
             items = findValidationItems(de);
             for(ValidationItem item : items) {
               ValidationNode vNode = null;
               if (item instanceof ValidationWarning) {
                 vNode = new WarningNode(item);
               } else {
                 vNode = new ErrorNode(item);
               }
               node.addValidationNode(vNode);
             }
           }
         } catch (NullPointerException e){
           e.printStackTrace();
         } // end of try-catch
       }
       if(showInheritedAttributes) {
         for(UMLNode inherit : inherited)
           inheritedPackage.addChild(inherit);
       }
       
       if(inheritedPackage.getChildren().size() > 0)
         parentNode.addChild(inheritedPackage);
   }
 
   private void doValueDomains(UMLNode parentNode) {
     UMLNode vdNode = new PackageNode("Value Domains", "Value Domains");
     
     List<ValueDomain> vds = elements.getElements(DomainObjectFactory.newValueDomain());
     
     for(ValueDomain vd : vds) {
       UMLNode node = new ValueDomainNode(vd);
       
       parentNode.addChild(node);
       
       ((ValueDomainNode) node).setReviewed
         (reviewTracker.get(node.getFullPath()));
       
       doValueMeanings(node);
 
       List<ValidationItem> items = findValidationItems(vd);
       for(ValidationItem item : items) {
         ValidationNode vNode = null;
         if (item instanceof ValidationWarning) {
           vNode = new WarningNode(item);
         } else {
           vNode = new ErrorNode(item);
         }
         node.addValidationNode(vNode);
       }
     }
   }
 
   private void doValueMeanings(UMLNode parentNode) {
     // Find all DEs that have this OC.
     List<ValueMeaning> vms = elements.getElements(DomainObjectFactory.newValueMeaning());
     
     for(ValueMeaning vm : vms) {
       try {
         if(isInValueDomain(parentNode.getFullPath(), vm)) {
           UMLNode node = new ValueMeaningNode(vm, parentNode.getFullPath());
 
           Boolean reviewed = reviewTracker.get(node.getFullPath());
           if(reviewed != null) {
             parentNode.addChild(node);
             ((ValueMeaningNode) node).setReviewed(reviewed);
           }
 
           List<ValidationItem> items = findValidationItems(vm);
           for(ValidationItem item : items) {
             ValidationNode vNode = null;
             if (item instanceof ValidationWarning) {
               vNode = new WarningNode(item);
             } else {
               vNode = new ErrorNode(item);
             }
             node.addValidationNode(vNode);
           }
         }
       } catch (NullPointerException e){
         e.printStackTrace();
       } // end of try-catch
     }
   }
 
   private void doAssociations(UMLNode parentNode) {
     UMLNode assocNode = new PackageNode("Associations", "Associations");
 
     ObjectClassRelationship o = DomainObjectFactory.newObjectClassRelationship();
     List<ObjectClassRelationship> ocrs = elements.getElements(o);
 
     for(ObjectClassRelationship ocr : ocrs) {
       AssociationNode node = new AssociationNode(ocr);
 
       Boolean reviewed = reviewTracker.get(node.getFullPath());
       if (reviewed != null) {
           node.setReviewed(reviewed.booleanValue());
       }
       
       List<ValidationItem> items = findValidationItems(ocr);
       for(ValidationItem item : items) {
         ValidationNode vNode = null;
         if (item instanceof ValidationWarning) {
           vNode = new WarningNode(item);
         } else {
           vNode = new ErrorNode(item);
         }
         node.addValidationNode(vNode);
       }
 
       assocNode.addChild(node);
 
       doAssociationEnd(node, AssociationEndNode.TYPE_SOURCE);
       doAssociationEnd(node, AssociationEndNode.TYPE_TARGET);
     }
 
     parentNode.addChild(assocNode);
 
   }
 
   private void doAssociationEnd(UMLNode parentNode, int type) {
     
     ObjectClassRelationship ocr = (ObjectClassRelationship)parentNode.getUserObject();
     AssociationEndNode node = new AssociationEndNode(ocr, type);
     Boolean reviewed = reviewTracker.get(node.getFullPath());
     if (reviewed != null) {
         node.setReviewed(reviewed.booleanValue());
     }
     
 //     List<ValidationItem> items = findValidationItems(ocr);
 //     for(ValidationItem item : items) {
 //       ValidationNode vNode = null;
 //       if (item instanceof ValidationWarning) {
 //         vNode = new WarningNode(item);
 //       } else {
 //         vNode = new ErrorNode(item);
 //       }
 //       node.addValidationNode(vNode);
 //     }
     
     parentNode.addChild(node);
 
   }
   
    private void doAssociations(UMLNode parentNode, ObjectClass oc) {
       UMLNode assocNode = new PackageNode("Associations", "Associations");
 
     ObjectClassRelationship o = DomainObjectFactory.newObjectClassRelationship();
     List<ObjectClassRelationship> ocrs = elements.getElements(o);
 
     for(ObjectClassRelationship ocr : ocrs) {
       if(ocr.getSource().getLongName().equals(oc.getLongName()) 
          | ocr.getTarget().getLongName().equals(oc.getLongName())) {
 
         UMLNode node = new AssociationNode(ocr);
 
         List<ValidationItem> items = findValidationItems(ocr);
         for(ValidationItem item : items) {
           ValidationNode vNode = null;
           if (item instanceof ValidationWarning) {
             vNode = new WarningNode(item);
           } else {
             vNode = new ErrorNode(item);
           }
           node.addValidationNode(vNode);
         }
 
         assocNode.addChild(node);
 
        doAssociationEnd(node, AssociationEndNode.TYPE_SOURCE);
        doAssociationEnd(node, AssociationEndNode.TYPE_TARGET);
       }
     }
 
     parentNode.addChild(assocNode);
    }
 
   public void preferenceChange(UserPreferencesEvent event) 
   {
     if(event.getTypeOfEvent() == UserPreferencesEvent.VIEW_ASSOCIATION) 
     {
       inClassAssociations = new Boolean (event.getValue());
       buildTree(elements);
       TreeEvent tEvent = new TreeEvent();
       fireTreeEvent(tEvent);
     }
     if(event.getTypeOfEvent() == UserPreferencesEvent.SHOW_INHERITED_ATTRIBUTES)
     {
       showInheritedAttributes = Boolean.valueOf(event.getValue());
       buildTree(elements);
       TreeEvent treeEvent = new TreeEvent();
       fireTreeEvent(treeEvent);
     }
     if(event.getTypeOfEvent() == UserPreferencesEvent.SORT_ELEMENTS)
     {
       buildTree(elements);
       TreeEvent treeEvent = new TreeEvent();
       fireTreeEvent(treeEvent);
     }
   }
 
   public void addTreeListener(TreeListener listener) 
   {
     treeListeners.add(listener);
   }
 
   public void fireTreeEvent(TreeEvent event) 
   {
     for(TreeListener l : treeListeners)
       l.treeChange(event);
   }
 
   private boolean isInValueDomain(String vdName, ValueMeaning vm) {
     List<ValueDomain> vds = elements.getElements(DomainObjectFactory.newValueDomain());
     
     for(ValueDomain vd : vds) {
       if(vd.getLongName().equals(vdName)) {
         if(vd.getPermissibleValues() != null)
           for(PermissibleValue pv : vd.getPermissibleValues()) 
             if(pv.getValueMeaning() == vm)
               return true;
         return false;
       }
     }
     return false;
     
   }
   
 }
