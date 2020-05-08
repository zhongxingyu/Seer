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
 package gov.nih.nci.ncicb.cadsr.loader.persister;
 
 import gov.nih.nci.ncicb.cadsr.dao.*;
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.loader.defaults.UMLDefaults;
 import gov.nih.nci.ncicb.cadsr.loader.event.ProgressEvent;
 import gov.nih.nci.ncicb.cadsr.loader.event.ProgressListener;
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 
 /**
  *
  * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
  */
 public class DEPersister implements Persister {
   private static Logger logger = Logger.getLogger(DEPersister.class.getName());
   public static String DE_PREFERRED_NAME_DELIMITER = "v";
   public static String DE_PREFERRED_NAME_CONCAT_CHAR = ":";
   public static String DE_PREFERRED_DEF_CONCAT_CHAR = "_";
 
   private UMLDefaults defaults = UMLDefaults.getInstance();
   private ElementsLists elements = ElementsLists.getInstance();
 
   private ProgressListener progressListener = null;
   
   private PersisterUtil persisterUtil;
   
   private DataElementDAO dataElementDAO;
 
   public DEPersister() {
     initDAOs();
   }
 
   public void persist() {
     DataElement de = DomainObjectFactory.newDataElement();
     List<DataElement> des = elements.getElements(de);
 
     logger.debug("des...");
 
     int count = 0;
     sendProgressEvent(count++, des.size(), "DEs");
 
 
     if (des != null) {
       for (ListIterator<DataElement> it = des.listIterator(); it.hasNext();) {
         try {
           de = it.next();
 
           sendProgressEvent(count++, des.size(), "DE : " + de.getLongName());
 
           DataElement newDe = DomainObjectFactory.newDataElement();
 
           de.setDataElementConcept(
             persisterUtil.lookupDec(de.getDataElementConcept().getId()));
           newDe.setDataElementConcept(de.getDataElementConcept());
 
           ValueDomain vd = LookupUtil.lookupValueDomain(de.getValueDomain());
           if(vd == null) {
             logger.error("Value Domain " + de.getValueDomain().getLongName() + " does not exist.");
             logger.error("CDE " + de.getLongName() + " DID NOT GET LOADED");
             continue;
           }
           de.setValueDomain(vd);
           //           }
           newDe.setValueDomain(de.getValueDomain());
 
           if(!StringUtil.isEmpty(de.getPublicId()) && de.getVersion() != null) {
             newDe = existingMapping(de);
 
               /* if DE alreay exists, check context
                * If context is different, add Used_by alt_name
                */
             de.setId(newDe.getId());
             if (!newDe.getContext().getId().equals(defaults.getContext().getId())) {
               AlternateName _an = DomainObjectFactory.newAlternateName();
               _an.setName(defaults.getContext().getName());
               _an.setType(AlternateName.TYPE_USED_BY);
               persisterUtil.addAlternateName(de, _an);
             }
             logger.info(PropertyAccessor.getProperty("mapped.to.existing.de"));
           } else {
             List l = dataElementDAO.find(newDe);
             de.setLongName(this.deriveLongName(de));
             
             if (l.size() == 0) {
               de.setContext(defaults.getContext());
               de.setPreferredName(this.derivePreferredName(de));
               de.setVersion(new Float(1.0f));
               de.setWorkflowStatus(defaults.getWorkflowStatus());
               
               de.setPreferredDefinition(
                                         de.getDataElementConcept().getPreferredDefinition()
                                         + DE_PREFERRED_DEF_CONCAT_CHAR +
                                         de.getValueDomain().getPreferredDefinition()
                                         );
               
               de.setAudit(defaults.getAudit());
               de.setLifecycle(defaults.getLifecycle());
               logger.debug("Creating DE: " + de.getLongName());
               List<AlternateName> altNames = new ArrayList<AlternateName>(de.getAlternateNames());
               List<Definition> altDefs = new ArrayList<Definition>(de.getDefinitions());
               de.removeAlternateNames();
               de.removeDefinitions();
 
               newDe = dataElementDAO.create(de);
               
               // restore altNames
               for(AlternateName an : altNames) {
                 de.addAlternateName(an);
               }
               // restore altDefs
               for(Definition def : altDefs) {
                 de.addDefinition(def);
               }
               
               logger.info(PropertyAccessor.getProperty("created.de"));
             }
             else {
               newDe = (DataElement) l.get(0);
               logger.info(PropertyAccessor.getProperty("existed.de"));
               
               /* if DE alreay exists, check context
                * If context is different, add Used_by alt_name
                */
               de.setId(newDe.getId());
               if (!newDe.getContext().getId().equals(defaults.getContext().getId())) {
                 persisterUtil.addAlternateName(
                                  de, defaults.getContext().getName(), AlternateName.TYPE_USED_BY,
                                  null);
               }
             }
           }
 
           LogUtil.logAc(newDe, logger);
           logger.info(
             PropertyAccessor.getProperty(
               "vd.preferredName", newDe.getValueDomain().getPreferredName()));
 
           de.setId(newDe.getId());
           persisterUtil.addPackageClassification(de);
 
           for(AlternateName altName : de.getAlternateNames()) {
             persisterUtil.addAlternateName(de, altName);
           }
 
           for(Definition def : de.getDefinitions()) {
             persisterUtil.addAlternateDefinition(de, def);
           }
           
           /**
            *  update the reference docs
            */
           if (de.getReferenceDocuments() != null) {
        	  persisterUtil.updateRefDocs(de, de.getReferenceDocuments());
           }
 
           it.set(newDe);
         }
         catch (PersisterException e) {
           logger.error("Could not persist DE: " + de.getLongName());
           logger.error(e.getMessage());
         } // end of try-catch
       }
     }
   }
   
   //Will need to declare this method as abstract method in UMLPersistor
   protected String derivePreferredName (AdminComponent ac ) {
     DataElement de = (DataElement)ac;
     String preferredName =
       ConventionUtil.publicIdVersion(de.getDataElementConcept())
       +DE_PREFERRED_NAME_CONCAT_CHAR
       +ConventionUtil.publicIdVersion(de.getValueDomain());
     return preferredName;
   }
   
   //Will need to declare this method as abstract method in UMLPersistor
   protected String deriveLongName (AdminComponent ac ) {
     DataElement de = (DataElement)ac;
     String longName = de.getDataElementConcept().getLongName() 
                      + " "
                      +de.getValueDomain().getLongName();
     return longName;
   }
 
   private DataElement existingMapping(DataElement de) {
 
     List<String> eager = new ArrayList<String>();
     eager.add(EagerConstants.AC_CS_CSI);
     
     List<DataElement> l = dataElementDAO.find(de, eager);
 
     if(l.size() == 0)
       throw new PersisterException(PropertyAccessor.getProperty("de.existing.error", ConventionUtil.publicIdVersion(de)));
     
     DataElement existingDe = l.get(0);
 
     return existingDe;
 
   }
 
   protected void sendProgressEvent(int status, int goal, String message) {
     if(progressListener != null) {
       ProgressEvent pEvent = new ProgressEvent();
       pEvent.setMessage(message);
       pEvent.setStatus(status);
       pEvent.setGoal(goal);
       
       progressListener.newProgressEvent(pEvent);
 
     }
   }
 
   public void setProgressListener(ProgressListener listener) {
     progressListener = listener;
   }
   
   public void setPersisterUtil(PersisterUtil pu) {
     persisterUtil = pu;
   }
   
   private void initDAOs()  {
     dataElementDAO = DAOAccessor.getDataElementDAO();
   }
 
 }
