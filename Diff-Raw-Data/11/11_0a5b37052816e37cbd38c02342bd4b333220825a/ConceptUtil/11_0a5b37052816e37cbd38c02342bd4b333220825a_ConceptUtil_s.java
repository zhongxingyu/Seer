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
 
 import gov.nih.nci.ncicb.cadsr.loader.ext.CadsrModule;
 
 import gov.nih.nci.ncicb.cadsr.loader.ext.CadsrModuleListener;
 
 import java.util.*;
 
 public class ConceptUtil implements CadsrModuleListener {
 
   private static CadsrModule cadsrModule;
 
   /**
    * @return true if this concept code is used by at least one element (OC, Prop, etc...)
    */
   public static boolean isConceptUsed(Concept con) {
     ElementsLists elements = ElementsLists.getInstance();
     
     List<ObjectClass> ocs = elements.getElements(DomainObjectFactory.newObjectClass());
     for(ObjectClass oc : ocs) {
       if(StringUtil.isEmpty(oc.getPublicId())) {
         String[] codes = oc.getPreferredName().split(":");
         for(String code : codes) {
           if(code.equals(con.getPreferredName()))
             return true;
         }
       }
     }
 
     List<Property> props = elements.getElements(DomainObjectFactory.newProperty());
     for(Property prop : props) {
       if(StringUtil.isEmpty(prop.getPublicId())) {
         String[] codes = prop.getPreferredName().split(":");
         for(String code : codes) {
           if(code.equals(con.getPreferredName()))
             return true;
         }
       }
     }
 
     List<ValueMeaning> vms = elements.getElements(DomainObjectFactory.newValueMeaning());
     for(ValueMeaning vm : vms) {
       if(StringUtil.isEmpty(vm.getPublicId()) && vm.getConceptDerivationRule() != null) {
         for(ComponentConcept comp : vm.getConceptDerivationRule().getComponentConcepts()) {
           if(comp.getConcept().getPreferredName().equals(con.getPreferredName()))
             return true;
         }
       }
     }
 
     List<ValueDomain> vds = elements.getElements(DomainObjectFactory.newValueDomain());
     for(ValueDomain vd : vds) {
       if(StringUtil.isEmpty(vd.getPublicId()) && vd.getConceptDerivationRule() != null) {
         for(ComponentConcept comp : vd.getConceptDerivationRule().getComponentConcepts()) {
           if(comp.getConcept().getPreferredName().equals(con.getPreferredName()))
             return true;
         }
       }
     }
 
     List<ObjectClassRelationship> ocrs = elements.getElements(DomainObjectFactory.newObjectClassRelationship());
     for(ObjectClassRelationship ocr : ocrs) {
       ConceptDerivationRule conDR = ocr.getConceptDerivationRule();
       if(StringUtil.isEmpty(ocr.getPublicId()) && conDR != null) {
         for(ComponentConcept comp : conDR.getComponentConcepts())
           if(comp.getConcept().getPreferredName().equals(con.getPreferredName()))
             return true;
       }
 
       conDR = ocr.getSourceRoleConceptDerivationRule();
       if(conDR != null) {
         for(ComponentConcept comp : conDR.getComponentConcepts())
           if(comp.getConcept().getPreferredName().equals(con.getPreferredName()))
             return true;
       }
 
       conDR = ocr.getTargetRoleConceptDerivationRule();
       if(conDR != null) {
         for(ComponentConcept comp : conDR.getComponentConcepts())
           if(comp.getConcept().getPreferredName().equals(con.getPreferredName()))
             return true;
       }
     }
 
     return false;
   }
 
   public static Concept getConceptFromCode(String conceptCode) {
     ElementsLists elements = ElementsLists.getInstance();
     List<Concept> concepts = elements.getElements(DomainObjectFactory.newConcept());
     
     for(Concept con : concepts) {
       if(con.getPreferredName().equals(conceptCode))
         return con;
     }
     
     return null;
   }
 
   public static String longNameFromConcepts(Concept[] concepts) {
     StringBuffer sb = new StringBuffer();
     
     for(Concept con : concepts) {
       if(sb.length() > 0)
         sb.append(" ");
       sb.append(StringUtil.upperFirst(con.getLongName()));
     }
 
     return sb.toString();
 
   }
 
   public static String longNameFromConcepts(List<Concept> concepts) {
 
     Concept[] conArr = new Concept[concepts.size()];
     
     return longNameFromConcepts(concepts.toArray(conArr));
 
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
 
   public static String preferredNameFromConceptDerivationRule(ConceptDerivationRule condr) {
     if(condr == null) {
       return "";
     }
     List<Concept> concepts = new ArrayList<Concept>();
     for(ComponentConcept comp : condr.getComponentConcepts()) {
       concepts.add(comp.getConcept());
     }
     return preferredNameFromConcepts(concepts);
   }
 
 
   public static String preferredDefinitionFromConcepts(Concept[] concepts) {
     return preferredDefinitionFromConcepts(concepts, false);
   }
   
 
   public static String preferredDefinitionFromConcepts(Concept[] concepts, boolean reverse) {
     StringBuilder sb = new StringBuilder();
     
     for(Concept con : concepts) {
       if(sb.length() > 0)
         if(reverse)
           sb.insert(0, ":");
         else
           sb.append(":");
       
       if(reverse)
         sb.insert(0, con.getPreferredDefinition());
       else
         sb.append(con.getPreferredDefinition());
     }
 
     return sb.toString();
   }
 
   public static String[] getConceptCodes(ValueMeaning vm) {
     return getConceptCodes(vm.getConceptDerivationRule());
 //     List<ComponentConcept> compCons = vm.getConceptDerivationRule().getComponentConcepts();
 //     String[] conceptCodes = new String[compCons.size()];
 //     for(int i = 0; i < compCons.size(); i++) 
 //       conceptCodes[compCons.size()-i-1] = compCons.get(i).getConcept().getPreferredName();
     
 //     return conceptCodes;
   }
 
   public static String[] getConceptCodes(ConceptDerivationRule conDR) {
     List<ComponentConcept> compCons = conDR.getComponentConcepts();
     String[] conceptCodes = new String[compCons.size()];
     for(int i = 0; i<compCons.size(); i++) 
       conceptCodes[i] = compCons.get(i).getConcept().getPreferredName();
     
     return conceptCodes;
   }
 
   /**
    * transforms list of concepts into concept derivation rule.
    * @param reverseOrder set to true to reverse the order of concepts.
    */
   public static ConceptDerivationRule createConceptDerivationRule(List<Concept> concepts, boolean reverseOrder) {
 
     ConceptDerivationRule condr = DomainObjectFactory.newConceptDerivationRule();
     List<ComponentConcept> compCons = new ArrayList<ComponentConcept>();
  
     int c = 0;
     for(Concept con : concepts) {
       ComponentConcept compCon = DomainObjectFactory.newComponentConcept();
       compCon.setConcept(con);
       if(reverseOrder)
         compCon.setOrder(c);
       else 
         compCon.setOrder(concepts.size() - 1 - c);
         
       compCon.setConceptDerivationRule(condr);
       if(reverseOrder)
         compCons.add(0, compCon);
       else 
         compCons.add(compCon);
       c++;
     }
 
     condr.setComponentConcepts(compCons);
     return condr;
     
 
   }
 
   /**
    * returns the condr for an OC if there's one, build one based on preferredName if none. This is only useful because preferredName is used for concepts with OCs. Should be refactored.
    * Create One based on publicID if mapped to publicID
    */
   public static ConceptDerivationRule findConceptDerivationRule(ObjectClass oc) {
     ConceptDerivationRule condr = oc.getConceptDerivationRule();
     
     if(!StringUtil.isEmpty(oc.getPublicId())) {
       
       List<Concept> concepts = cadsrModule.getConcepts(oc);
       
       condr = DomainObjectFactory.newConceptDerivationRule();
       List<ComponentConcept> compCons = new ArrayList<ComponentConcept>();
       
       int c = 0;
       for(Concept concept : concepts) {
         ComponentConcept compCon = DomainObjectFactory.newComponentConcept();
         compCon.setConcept(concept);
         compCon.setOrder(concepts.size() - 1 - c);
         compCon.setConceptDerivationRule(condr);
         compCons.add(0, compCon);
         c++;
       }
       condr.setComponentConcepts(compCons);
     
       oc.setConceptDerivationRule(condr);
       
     } else if(condr == null) {
       condr = DomainObjectFactory.newConceptDerivationRule();
       List<ComponentConcept> compCons = new ArrayList<ComponentConcept>();
       
       if(!StringUtil.isEmpty(oc.getPreferredName())) {
         String[] conceptCodes = oc.getPreferredName().split(":");
         
         int c = 0;
         for(String conceptCode : conceptCodes) {
           ComponentConcept compCon = DomainObjectFactory.newComponentConcept();
           compCon.setConcept(ConceptUtil.getConceptFromCode(conceptCode));
           compCon.setOrder(conceptCodes.length - 1 - c);
           compCon.setConceptDerivationRule(condr);
           compCons.add(0, compCon);
           c++;
         }
       }
 
       condr.setComponentConcepts(compCons);
         
       oc.setConceptDerivationRule(condr);
     } 
 
     return condr;
   }
   
   public void setCadsrModule(CadsrModule cadsrModule) {
     this.cadsrModule = cadsrModule;
   }
 
 }
