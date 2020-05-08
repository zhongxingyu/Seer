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
 package gov.nih.nci.ncicb.cadsr.loader.util;
 
 import gov.nih.nci.ncicb.cadsr.domain.*;
 
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import java.util.*;
 
 public class ObjectUpdater {
 
   public static void updateByAltName(String altName, Concept[] oldConcepts, Concept[] newConcepts) {
 
     ElementsLists elements = ElementsLists.getInstance();
 
     List<DataElement> des = (List<DataElement>)elements.getElements(DomainObjectFactory.newDataElement().getClass());
 
     for(DataElement de : des) {
       if(de.getDataElementConcept().getProperty().getLongName().equals(altName)) {
         de.getDataElementConcept().getProperty().setPreferredName(preferredNameFromConcepts(newConcepts));
       }
     }
     
     addNewConcepts(newConcepts);
     removeStaleConcepts(oldConcepts);
     
   }
 
   public static void update(ValueMeaning vm, Concept[] oldConcepts, Concept[] newConcepts) {
 
     ConceptDerivationRule condr = newConDR(newConcepts);
 
     vm.setConceptDerivationRule(condr);
     
     addNewConcepts(newConcepts);
     removeStaleConcepts(oldConcepts);
   }
 
   private static ConceptDerivationRule newConDR(Concept[] newConcepts) {
     ConceptDerivationRule condr = DomainObjectFactory.newConceptDerivationRule();
     List<ComponentConcept> compCons = new ArrayList<ComponentConcept>();
  
     int c = 0;
     for(Concept con : newConcepts) {
       ComponentConcept compCon = DomainObjectFactory.newComponentConcept();
       compCon.setConcept(con);
       compCon.setOrder(newConcepts.length - 1 - c);
       compCon.setConceptDerivationRule(condr);
      compCons.add(compCon);
       c++;
     }
     condr.setComponentConcepts(compCons);
 
     return condr;
   }
 
   public static void updateAssociation(ObjectClassRelationship ocr, 
           Concept[] oldConcepts, Concept[] newConcepts) {
     ocr.setConceptDerivationRule(newConDR(newConcepts));
     addNewConcepts(newConcepts);
     removeStaleConcepts(oldConcepts);
   }
   
   public static void updateAssociationSource(ObjectClassRelationship ocr, 
           Concept[] oldConcepts, Concept[] newConcepts) {
 
     ocr.setSourceRoleConceptDerivationRule(newConDR(newConcepts));
     addNewConcepts(newConcepts);
     removeStaleConcepts(oldConcepts);
   }
 
   public static void updateAssociationTarget(ObjectClassRelationship ocr, 
           Concept[] oldConcepts, Concept[] newConcepts) {
       
     ocr.setTargetRoleConceptDerivationRule(newConDR(newConcepts));
     addNewConcepts(newConcepts);
     removeStaleConcepts(oldConcepts);
   }
   
   public static void update(AdminComponent ac, Concept[] oldConcepts, Concept[] newConcepts) {
 
     if(ac instanceof ObjectClass) {
       ObjectClass oc = (ObjectClass)ac;
       oc.setPreferredName(preferredNameFromConcepts(newConcepts));
     } else if(ac instanceof DataElement) {
       DataElement de = (DataElement)ac;
 
       de.getDataElementConcept().getProperty().setPreferredName(preferredNameFromConcepts(newConcepts));
       
     }
 
     addNewConcepts(newConcepts);
     removeStaleConcepts(oldConcepts);
 
   }
 
   public static String preferredNameFromConcepts(List<Concept> concepts) {
     StringBuffer sb = new StringBuffer();
     for(Concept con : concepts) {
       if(sb.length() > 0)
         sb.insert(0, ":");
       sb.insert(0, con.getPreferredName());
     }
     return sb.toString();
   }
 
   public static String preferredNameFromConcepts(Concept[] concepts) {
     StringBuffer sb = new StringBuffer();
     for(Concept con : concepts) {
       if(sb.length() > 0)
         sb.insert(0, ":");
       sb.insert(0, con.getPreferredName());
     }
     return sb.toString();
   }
 
   private static void removeStaleConcepts(Concept[] concepts) {
     ElementsLists elements = ElementsLists.getInstance();
 
     List<ObjectClass> ocs = elements.getElements(DomainObjectFactory.newObjectClass());
     List<Property> props = elements.getElements(DomainObjectFactory.newProperty());
     List<ValueDomain> vds = elements.getElements(DomainObjectFactory.newValueDomain());
     List<ValueMeaning> vms = elements.getElements(DomainObjectFactory.newValueMeaning());
     List<ObjectClassRelationship> ocrs = elements.getElements(DomainObjectFactory.newObjectClassRelationship());
 
     a:
     for(Concept concept : concepts) {
       boolean found = false;
       if(StringUtil.isEmpty(concept.getPreferredName()))
         continue a;
       for(ObjectClass oc : ocs) {
         String[] codes = oc.getPreferredName().split(":");
         for(String code : codes) {
           if(code.equals(concept.getPreferredName())) {
             found = true;
             continue a;
           }
         }
       }
       for(Property prop : props) {
         String[] codes = prop.getPreferredName().split(":");
         for(String code : codes) {
           if(code.equals(concept.getPreferredName())) {
             found = true;
             continue a;
           }
         }
       }
       for(ValueDomain vd : vds) {
         for(ComponentConcept compCon : vd.getConceptDerivationRule().getComponentConcepts()) {
           if(concept.getPreferredName().equals(compCon.getConcept().getPreferredName())) {
             found = true;
             continue a;
           }
         }
       }
       for(ValueMeaning vm : vms) {
         for(ComponentConcept compCon : vm.getConceptDerivationRule().getComponentConcepts()) {
           if(concept.getPreferredName().equals(compCon.getConcept().getPreferredName())) {
             found = true;
             continue a;
           }
         }
       }
       for(ObjectClassRelationship ocr : ocrs) {
         for(ComponentConcept compCon : ocr.getConceptDerivationRule().getComponentConcepts()) {
           if(concept.getPreferredName().equals(compCon.getConcept().getPreferredName())) {
             found = true;
             continue a;
           }
         }
         for(ComponentConcept compCon : ocr.getSourceRoleConceptDerivationRule().getComponentConcepts()) {
           if(concept.getPreferredName().equals(compCon.getConcept().getPreferredName())) {
             found = true;
             continue a;
           }
         }
         for(ComponentConcept compCon : ocr.getTargetRoleConceptDerivationRule().getComponentConcepts()) {
           if(concept.getPreferredName().equals(compCon.getConcept().getPreferredName())) {
             found = true;
             continue a;
           }
         }        
       }
       if(!found) {
         removeFromConcepts(concept);
       }
     }
   }
 
   private static void addNewConcepts(Concept[] newConcepts) {
     ElementsLists elements = ElementsLists.getInstance();
     List<Concept> concepts = elements.getElements(DomainObjectFactory.newConcept());
     
     for(Concept concept : newConcepts) {
       boolean found = false;
       for(Concept con : concepts) {
         if(con.getPreferredName().equals(concept.getPreferredName())) {
           found = true;
           break;
         }
       }
       if(!found) {
         elements.addElement(concept);
       }
     }
 
 
   }
 
   private static void removeFromConcepts(Concept concept) {
     ElementsLists elements = ElementsLists.getInstance();
     List<Concept> concepts = elements.getElements(DomainObjectFactory.newConcept());
     
     for(int i = 0, n = concepts.size(); i<n; i++) {
       if(concepts.get(i).getPreferredName().equals(concept.getPreferredName())) {
         concepts.remove(i);
         return;
       }
     }
     
     concepts.remove(concept);
 
   }
 
 }
