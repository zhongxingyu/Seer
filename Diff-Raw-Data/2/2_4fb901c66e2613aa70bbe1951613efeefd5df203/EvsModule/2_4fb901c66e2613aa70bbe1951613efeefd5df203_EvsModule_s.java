 /*
  * Copyright 2000-2005 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
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
 package gov.nih.nci.ncicb.cadsr.loader.ext;
 import gov.nih.nci.ncicb.cadsr.domain.Concept;
 import gov.nih.nci.ncicb.cadsr.domain.DomainObjectFactory;
 import gov.nih.nci.ncicb.cadsr.evs.EVSConcept;
 import gov.nih.nci.ncicb.cadsr.evs.LexEVSQueryService;
 import gov.nih.nci.ncicb.cadsr.evs.LexEVSQueryServiceImpl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.LexGrid.commonTypes.Source;
 import org.LexGrid.concepts.Definition;
 import org.apache.log4j.Logger;
 
 /**
  * Layer to the EVS external API.
  *
  * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
  */
 public class EvsModule 
 {
 
   private Logger logger = Logger.getLogger(EvsModule.class.getName());
 
   private static LexEVSQueryService evsService = new LexEVSQueryServiceImpl();
 
   private String vocabName = null;
 
   public EvsModule(String vocabName) {
     this.vocabName = vocabName;
   }
 
   public EvsModule() {
     this.vocabName = "NCI_Thesaurus";
   }
   
   public EvsResult findByConceptCode(String code, boolean includeRetired) 
   {
     try {
       List<EVSConcept> evsConcepts = (List<EVSConcept>)evsService.findConceptsByCode(code, includeRetired, 100, vocabName);
       
       for(EVSConcept evsConcept : evsConcepts) {
         return evsConceptToEvsResult(evsConcept);
       }
     } catch (Exception e){
       logger.warn(e.getMessage());
     } // end of try-catch
     
     return null;
   }
   
   public Collection<EvsResult> findByPreferredName(String s, boolean includeRetired) 
   {
     Collection<EvsResult> result = new ArrayList<EvsResult>();
 
     try {
       List<EVSConcept> evsConcepts = evsService.findConceptsByPreferredName(s, includeRetired, vocabName);
       
       for(EVSConcept evsConcept : evsConcepts) {
         result.add(evsConceptToEvsResult(evsConcept));
       }
     } catch (Exception e){
       e.printStackTrace();
     } // end of try-catch
 
     return result;
   }
 
   public Collection<EvsResult> findBySynonym(String s, boolean includeRetired) 
   {
     s = s.replace('%','*');
 
     Collection<EvsResult> result = new ArrayList<EvsResult>();
 
     try {
       List<EVSConcept> evsConcepts = evsService.findConceptsBySynonym(s, includeRetired, 100, vocabName);
       
       for(EVSConcept evsConcept : evsConcepts) {
         result.add(evsConceptToEvsResult(evsConcept));
       }
     } catch (Exception e){
       e.printStackTrace();
     } // end of try-catch
 
     return result;
   }
 
 
   private EvsResult evsConceptToEvsResult(EVSConcept evsConcept) {
 
     Concept c = DomainObjectFactory.newConcept();
     c.setPreferredName(evsConcept.getCode());
     c.setLongName(evsConcept.getPreferredName());
 
     Definition def = null;
     Source src = null;
 
     // if there's an NCI definition, pick that
     // Otherwise, first one that comes
     for(java.util.Iterator it = evsConcept.getDefinitions().iterator(); it.hasNext();) {
       Definition d = (Definition)it.next();      
 
       if(def == null)
         def = d;
       Source[] sources = d.getSource();
       for (Source source: sources) {
     	  if (source.getContent().equals("NCI")) {
     		  def = d;
     		  src = source;
     	  }
       }
     }
 
 //     if(evsConcept.getDefinitions().size() > 0) {
 //       def = (gov.nih.nci.evs.domain.Definition)evsConcept.getDefinitions().get(0);
 //     }
 
     if(def != null) {
       c.setPreferredDefinition(def.getValue().getContent());
      c.setDefinitionSource(src.getContent());
     } else 
       c.setPreferredDefinition("");
     
     String[] syns = new String[evsConcept.getSynonyms().size()];
     evsConcept.getSynonyms().toArray(syns);
     
     return new EvsResult(c, evsConcept.getName() ,syns);
     
 
   }
 }
