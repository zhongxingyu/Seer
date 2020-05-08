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
 
 public class ConceptUtil {
 
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
 
 
   public static String preferredDefinitionFromConcepts(Concept[] concepts) {
     StringBuffer sb = new StringBuffer();
     
     for(Concept con : concepts) {
       if(sb.length() > 0)
         sb.append(":");
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
        compCon.setOrder(concepts.size() - 1 - c);
      else 
        compCon.setOrder(c);
        
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
 
 }
