 package gov.nih.nci.ncicb.cadsr.loader.persister;
 
 import gov.nih.nci.ncicb.cadsr.dao.AdminComponentDAO;
 import gov.nih.nci.ncicb.cadsr.dao.AlternateNameDAO;
 import gov.nih.nci.ncicb.cadsr.dao.ClassSchemeClassSchemeItemDAO;
 import gov.nih.nci.ncicb.cadsr.domain.AdminComponent;
 import gov.nih.nci.ncicb.cadsr.domain.AdminComponentClassSchemeClassSchemeItem;
 import gov.nih.nci.ncicb.cadsr.domain.AlternateName;
 import gov.nih.nci.ncicb.cadsr.domain.ClassSchemeClassSchemeItem;
 import gov.nih.nci.ncicb.cadsr.domain.Concept;
 import gov.nih.nci.ncicb.cadsr.domain.DataElementConcept;
 import gov.nih.nci.ncicb.cadsr.domain.Definition;
 import gov.nih.nci.ncicb.cadsr.domain.DomainObjectFactory;
 import gov.nih.nci.ncicb.cadsr.domain.ReferenceDocument;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.loader.defaults.UMLDefaults;
 import gov.nih.nci.ncicb.cadsr.loader.util.DAOAccessor;
 import gov.nih.nci.ncicb.cadsr.loader.util.PropertyAccessor;
 
 import gov.nih.nci.ncicb.cadsr.loader.util.StringUtil;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 public class PersisterUtil {
   private static Logger logger = Logger.getLogger(PersisterUtil.class.getName());
 
   private AdminComponentDAO adminComponentDAO;
   private AlternateNameDAO alternateNameDAO;
   private ClassSchemeClassSchemeItemDAO classSchemeClassSchemeItemDAO;
 
   private UMLDefaults defaults = UMLDefaults.getInstance();
 
   private ElementsLists elements = ElementsLists.getInstance();
 
   public PersisterUtil() {
    initDAOs();
   }
   
   Map<Character, Character> charReplacementMap = new HashMap<Character, Character>() {
 		{
 			put('', 'Y');
 			put('', '\'');
 			put('', '\'');
 		}
 	};
 
   /* 
    */
   void addAlternateName(AdminComponent ac, AlternateName altName) {
     
     AlternateName queryAN = DomainObjectFactory.newAlternateName();
     queryAN.setName(altName.getName());
     queryAN.setType(altName.getType());
 
     AlternateName foundAN = adminComponentDAO.getAlternateName(ac, queryAN);
     
     
     // for now, only classify with one CS_CSI
     String packageName = null;
     for(AdminComponentClassSchemeClassSchemeItem acCsCsi : ac.getAcCsCsis()) {
       ClassSchemeClassSchemeItem csCsi = acCsCsi.getCsCsi();
       packageName = csCsi.getCsi().getLongName();
       if(!StringUtil.isEmpty(packageName)) {
         break;
       }
     }
     
     ClassSchemeClassSchemeItem packageCsCsi = null;
     if(packageName != null) 
       packageCsCsi = (ClassSchemeClassSchemeItem)defaults.getPackageCsCsis().get(packageName);
     
     
     if(foundAN != null) {
       logger.info(PropertyAccessor.getProperty(
                     "existed.altName", altName.getName()));
       
       if(packageCsCsi != null) {
         ClassSchemeClassSchemeItem foundCsCsi = alternateNameDAO.getClassSchemeClassSchemeItem(foundAN, packageCsCsi);
         
         ClassSchemeClassSchemeItem foundParentCsCsi = null;
         if(packageCsCsi.getParent() != null)
           foundParentCsCsi = alternateNameDAO.getClassSchemeClassSchemeItem(foundAN, packageCsCsi);
         
         
         if(foundCsCsi == null) {
           classSchemeClassSchemeItemDAO.addCsCsi(foundAN, packageCsCsi);
           logger.info(
             PropertyAccessor.getProperty(
               "linked.to.package",
               "Alternate Name"
               ));
         }
         if((foundParentCsCsi == null) && packageCsCsi.getParent() != null) {
           classSchemeClassSchemeItemDAO.addCsCsi(foundAN, packageCsCsi.getParent());
           logger.info(
             PropertyAccessor.getProperty(
               "linked.to.package",
               "Alternate Name"
               ));
         }
       }
     }
     
     if(foundAN == null) {
       AlternateName newAltName = DomainObjectFactory.newAlternateName();
       newAltName.setContext(defaults.getContext());
       newAltName.setAudit(defaults.getAudit());
       newAltName.setName(altName.getName());
       newAltName.setType(altName.getType());
       altName.setId(adminComponentDAO.addAlternateName(ac, newAltName));
       logger.info(PropertyAccessor.getProperty(
                     "added.altName", 
                     new String[] {
                       altName.getName(),
                       ac.getLongName()
                     }));
       
       if(packageName != null) {
         classSchemeClassSchemeItemDAO.addCsCsi(altName, packageCsCsi);
         logger.info(
           PropertyAccessor.getProperty(
             "linked.to.package",
             "Alternate Name"
             ));
         if(packageCsCsi.getParent() != null) {
           classSchemeClassSchemeItemDAO.addCsCsi(altName, packageCsCsi.getParent());
           logger.info(
             PropertyAccessor.getProperty(
               "linked.to.package",
               "Alternate Name"
               ));
         }
       }      
     } 
   }
 
 
   void addAlternateName(AdminComponent ac, String newName, String type, String packageName) {
     
     AlternateName queryAN = DomainObjectFactory.newAlternateName();
     queryAN.setName(newName);
     queryAN.setType(type);
 
     AlternateName foundAN = adminComponentDAO.getAlternateName(ac, queryAN);
     
     ClassSchemeClassSchemeItem packageCsCsi = (ClassSchemeClassSchemeItem)defaults.getPackageCsCsis().get(packageName);
 
     if(foundAN != null) {
       logger.info(PropertyAccessor.getProperty(
                     "existed.altName", newName));
       
       if(packageName == null)
         return;
         
       ClassSchemeClassSchemeItem foundCsCsi = alternateNameDAO.getClassSchemeClassSchemeItem(foundAN, packageCsCsi);
 
       ClassSchemeClassSchemeItem foundParentCsCsi = null;
       if(packageCsCsi.getParent() != null)
         foundParentCsCsi = alternateNameDAO.getClassSchemeClassSchemeItem(foundAN, packageCsCsi);
       
 
       if(foundCsCsi == null) {
         classSchemeClassSchemeItemDAO.addCsCsi(foundAN, packageCsCsi);
         logger.info(
           PropertyAccessor.getProperty(
             "linked.to.package",
             "Alternate Name"
             ));
       }
       if((foundParentCsCsi == null) && packageCsCsi.getParent() != null) {
         classSchemeClassSchemeItemDAO.addCsCsi(foundAN, packageCsCsi.getParent());
         logger.info(
           PropertyAccessor.getProperty(
             "linked.to.package",
             "Alternate Name"
             ));
       }
     }
     
     if(foundAN == null) {
       AlternateName altName = DomainObjectFactory.newAlternateName();
       altName.setContext(defaults.getContext());
       altName.setAudit(defaults.getAudit());
       altName.setName(newName);
       altName.setType(type);
       altName.setId(adminComponentDAO.addAlternateName(ac, altName));
       logger.info(PropertyAccessor.getProperty(
                     "added.altName", 
                     new String[] {
                       altName.getName(),
                       ac.getLongName()
                     }));
       
       if(packageName != null) {
         classSchemeClassSchemeItemDAO.addCsCsi(altName, packageCsCsi);
         logger.info(
           PropertyAccessor.getProperty(
             "linked.to.package",
             "Alternate Name"
             ));
         if(packageCsCsi.getParent() != null) {
           classSchemeClassSchemeItemDAO.addCsCsi(altName, packageCsCsi.getParent());
           logger.info(
             PropertyAccessor.getProperty(
               "linked.to.package",
               "Alternate Name"
               ));
         }
       }      
 
     } 
   }
 
   void addAlternateDefinition(AdminComponent ac, Definition newDef) {
     addAlternateDefinition(ac, newDef.getDefinition(), newDef.getType());
   }
 
   void addAlternateDefinition(AdminComponent ac, String newDef, String type) {
 
     Definition queryDef = DomainObjectFactory.newDefinition();
     queryDef.setDefinition(newDef);
     queryDef.setType(type);
 
     Definition foundDef = adminComponentDAO.getDefinition(ac, queryDef);
 
     // for now, only classify with one CS_CSI
     String packageName = null;
     for(AdminComponentClassSchemeClassSchemeItem acCsCsi : ac.getAcCsCsis()) {
       ClassSchemeClassSchemeItem csCsi = acCsCsi.getCsCsi();
       packageName = csCsi.getCsi().getLongName();
       if(!StringUtil.isEmpty(packageName)) {
         break;
       }
     }
     
     ClassSchemeClassSchemeItem packageCsCsi = null;
     if(packageName != null) 
       packageCsCsi = (ClassSchemeClassSchemeItem)defaults.getPackageCsCsis().get(packageName);
 
     if(foundDef != null) {
       logger.info(PropertyAccessor.getProperty(
                     "existed.altDef", newDef));
       
       if(packageCsCsi != null) {
         ClassSchemeClassSchemeItem foundCsCsi = alternateNameDAO.getClassSchemeClassSchemeItem(foundDef, packageCsCsi);
         
         ClassSchemeClassSchemeItem foundParentCsCsi = null;
         if(packageCsCsi.getParent() != null)
           foundParentCsCsi = alternateNameDAO.getClassSchemeClassSchemeItem(foundDef, packageCsCsi);
         
         
         if(foundCsCsi == null) {
           classSchemeClassSchemeItemDAO.addCsCsi(foundDef, packageCsCsi);
           logger.info(
             PropertyAccessor.getProperty(
               "linked.to.package",
               "Alternate Definition"
               ));
         }
         
         if((foundParentCsCsi == null) && packageCsCsi.getParent() != null) {
           classSchemeClassSchemeItemDAO.addCsCsi(foundDef, packageCsCsi.getParent());
           logger.info(
             PropertyAccessor.getProperty(
               "linked.to.package",
               "Alternate Name"
               ));
         }
       }
     }
     
     if(foundDef == null) {
 
       Definition altDef = DomainObjectFactory.newDefinition();
       altDef.setContext(defaults.getContext());
       altDef.setDefinition(newDef);
       altDef.setAudit(defaults.getAudit());
       altDef.setType(type);
       
      logger.info(" definition before search = "+altDef.getDefinition());
 	    StringBuilder builder = new StringBuilder();
 	    for (char currentChar : altDef.getDefinition().toCharArray()) {
 	    	Character replacementChar = charReplacementMap.get(currentChar);
 	        builder.append(replacementChar != null ? replacementChar : currentChar);
 	    }
 	   altDef.setDefinition(builder.toString());
	    System.out.println("DEC def after encoding =="+altDef.getDefinition());
 
       altDef.setId(adminComponentDAO.addDefinition(ac, altDef));
       logger.info(PropertyAccessor.getProperty(
                     "added.altDef", 
                     new String[] {
                       altDef.getId(),
                       altDef.getDefinition(),
                       ac.getLongName()
                     }));
       
       if(packageCsCsi != null) {
         classSchemeClassSchemeItemDAO.addCsCsi(altDef, packageCsCsi);
         logger.info(
           PropertyAccessor.getProperty(
             "linked.to.package",
             "Alternate Definition"
             ));
         
         if(packageCsCsi.getParent() != null) {
           classSchemeClassSchemeItemDAO.addCsCsi(altDef, packageCsCsi.getParent());
           logger.info(
             PropertyAccessor.getProperty(
               "linked.to.package",
               "Alternate Definition"
               ));
         }
       } 
     }
   }
 
 
   void addAlternateDefinition(AdminComponent ac, String newDef, String type, String packageName) {
 
     Definition queryDef = DomainObjectFactory.newDefinition();
     queryDef.setDefinition(newDef);
     queryDef.setType(type);
 
     Definition foundDef = adminComponentDAO.getDefinition(ac, queryDef);
 
 
     ClassSchemeClassSchemeItem packageCsCsi = (ClassSchemeClassSchemeItem)defaults.getPackageCsCsis().get(packageName);
 
     if(foundDef != null) {
       logger.info(PropertyAccessor.getProperty(
                     "existed.altDef", newDef));
       
       ClassSchemeClassSchemeItem foundCsCsi = alternateNameDAO.getClassSchemeClassSchemeItem(foundDef, packageCsCsi);
       
       ClassSchemeClassSchemeItem foundParentCsCsi = null;
       if(packageCsCsi.getParent() != null)
         foundParentCsCsi = alternateNameDAO.getClassSchemeClassSchemeItem(foundDef, packageCsCsi);
 
 
       if(foundCsCsi == null) {
         classSchemeClassSchemeItemDAO.addCsCsi(foundDef, packageCsCsi);
         logger.info(
           PropertyAccessor.getProperty(
             "linked.to.package",
             "Alternate Definition"
             ));
       }
 
       if((foundParentCsCsi == null) && packageCsCsi.getParent() != null) {
         classSchemeClassSchemeItemDAO.addCsCsi(foundDef, packageCsCsi.getParent());
         logger.info(
           PropertyAccessor.getProperty(
             "linked.to.package",
             "Alternate Name"
             ));
       }
       
     }
     
     if(foundDef == null) {
 
       Definition altDef = DomainObjectFactory.newDefinition();
       altDef.setContext(defaults.getContext());
       altDef.setDefinition(newDef);
       altDef.setAudit(defaults.getAudit());
       altDef.setType(type);
       altDef.setId(adminComponentDAO.addDefinition(ac, altDef));
       logger.info(PropertyAccessor.getProperty(
                     "added.altDef", 
                     new String[] {
                       altDef.getId(),
                       altDef.getDefinition(),
                       ac.getLongName()
                     }));
       
       classSchemeClassSchemeItemDAO.addCsCsi(altDef, packageCsCsi);
       logger.info(
         PropertyAccessor.getProperty(
           "linked.to.package",
           "Alternate Definition"
           ));
       
       if(packageCsCsi.getParent() != null) {
         classSchemeClassSchemeItemDAO.addCsCsi(altDef, packageCsCsi.getParent());
         logger.info(
           PropertyAccessor.getProperty(
             "linked.to.package",
             "Alternate Definition"
             ));
       }
     } 
   }
   
   public void updateRefDocs(final AdminComponent ac) {
 	  List<ReferenceDocument> existingRDs = adminComponentDAO.getReferenceDocuments(ac);
 	  List<ReferenceDocument> newRDs = filterNewRefDocs(existingRDs, ac.getReferenceDocuments());
 	  
 	  if (!newRDs.isEmpty()) {
 		  markPreferred(existingRDs, newRDs);
 		  existingRDs.addAll(newRDs);
 		  
 		  setRDACIdsAndContext(ac.getId(), existingRDs);
 		  
 		  ac.setReferenceDocuments(existingRDs);
 		  
 		  adminComponentDAO.updateRefDocs(ac);
 	  }
   }
   
   private void setRDACIdsAndContext(String acId, List<ReferenceDocument> refDocs) {
 	  for (ReferenceDocument refDoc: refDocs) {
 		  refDoc.setAcId(acId);
 		  refDoc.setContext(defaults.getContext());
 	  }
   }
   
   private void markPreferred(List<ReferenceDocument> existingRDs, List<ReferenceDocument> newRDs) {
 	  if (hasPreferredRD(existingRDs)) {
 		  for (ReferenceDocument newRD: newRDs) {
 			  newRD.setType("Alternate Question Text");
 		  }
 	  }
 	  else {
 		  boolean preferredMarked = false;
 		  for (ReferenceDocument newRD: newRDs) {
 			  if (preferredMarked) {
 				  newRD.setType("Alternate Question Text");
 			  }
 			  
 			  if (!preferredMarked && newRD.getType().equalsIgnoreCase("Preferred Question Text")) {
 				  preferredMarked = true;
 			  }
 		  }
 	  }
   }
   
   private boolean hasPreferredRD(List<ReferenceDocument> refDocs) {
 	  for(ReferenceDocument refDoc: refDocs) {
 		  if (refDoc.getType().equalsIgnoreCase("Preferred Question Text")) {
 			  return true;
 		  }
 	  }
 	  return false;
   }
   
   private List<ReferenceDocument> filterNewRefDocs(List<ReferenceDocument> existingRDs, List<ReferenceDocument> newRDs) {
 	  List<ReferenceDocument> newRefDocs = new ArrayList<ReferenceDocument>();
 	  List<String> existingRefDocNames = getRefDocNames(existingRDs);
 	  
 	  for (ReferenceDocument newRD: newRDs) {
 		  String newRefDocName = newRD.getName();
 		  if (!existingRefDocNames.contains(newRefDocName)) {
 			  newRefDocs.add(newRD);
 		  }
 	  }
 	  return newRefDocs;
   }
   
   private List<String> getRefDocNames(List<ReferenceDocument> refDocs) {
 	  List<String> refDocNames = new ArrayList<String>();
 	  for (ReferenceDocument refDoc: refDocs) {
 		  refDocNames.add(refDoc.getName());
 	  }
 	  
 	  return refDocNames;
   }
 
   DataElementConcept lookupDec(String id) {
     List<DataElementConcept> decs = elements.getElements(DomainObjectFactory.newDataElementConcept());
     
     for (Iterator it = decs.iterator(); it.hasNext(); ) {
       DataElementConcept o = (DataElementConcept) it.next();
 
       if (o.getId().equals(id)) {
         return o;
       }
     }
     return null;
   }
 
   boolean isSameDefinition(String def, Concept[] concepts) {
     if((def == null) || def.length() == 0)
       return true;
 
     StringBuffer sb = new StringBuffer();
     
     for(int i=0; i < concepts.length; i++) {
       if(sb.length() > 0)
         sb.append("\n");
       sb.append(concepts[i].getPreferredDefinition());
     }
 
     return def.equals(sb.toString());
     
   }
 
 
   protected void addPackageClassification(AdminComponent ac) {
 
     // for now, only classify with one CS_CSI
     String packageName = null;
     for(AdminComponentClassSchemeClassSchemeItem acCsCsi : ac.getAcCsCsis()) {
       ClassSchemeClassSchemeItem csCsi = acCsCsi.getCsCsi();
       packageName = csCsi.getCsi().getLongName();
       if(!StringUtil.isEmpty(packageName)) {
         break;
       }
     }
     
     ClassSchemeClassSchemeItem packageCsCsi = null;
     if(packageName != null) 
       packageCsCsi = (ClassSchemeClassSchemeItem)defaults.getPackageCsCsis().get(packageName);
 
 
     
     if(packageCsCsi != null) {
       ClassSchemeClassSchemeItem foundCsCsi = adminComponentDAO.getClassSchemeClassSchemeItem(ac, packageCsCsi);
       
       ClassSchemeClassSchemeItem foundParentCsCsi = null;
       if(packageCsCsi.getParent() != null) {
         foundParentCsCsi = adminComponentDAO.getClassSchemeClassSchemeItem(ac, packageCsCsi.getParent());
       }
       
       List csCsis = new ArrayList();
       
       if (foundCsCsi == null) {
         logger.info(PropertyAccessor.
                     getProperty("attach.package.classification"));
         
         if (packageCsCsi != null) {
           csCsis.add(packageCsCsi);
           if((foundParentCsCsi == null) && packageCsCsi.getParent() != null)
             csCsis.add(packageCsCsi.getParent());
           
           adminComponentDAO.addClassSchemeClassSchemeItems(ac, csCsis);
           logger.info(PropertyAccessor
                       .getProperty("added.package",
                                    new String[] {
                                      packageName, 
                                      ac.getLongName()}));
         } else {
           logger.error(PropertyAccessor.getProperty("missing.package", new String[] {packageName, ac.getLongName()}));
         }
       }
     }
   }
 
 
 
   protected void addPackageClassification(AdminComponent ac, String packageName) {
 
     ClassSchemeClassSchemeItem packageCsCsi = (ClassSchemeClassSchemeItem)defaults.getPackageCsCsis().get(packageName);
 
     ClassSchemeClassSchemeItem foundCsCsi = adminComponentDAO.getClassSchemeClassSchemeItem(ac, packageCsCsi);
 
     ClassSchemeClassSchemeItem foundParentCsCsi = null;
     if(packageCsCsi.getParent() != null) {
       foundParentCsCsi = adminComponentDAO.getClassSchemeClassSchemeItem(ac, packageCsCsi.getParent());
     }
 
     List csCsis = new ArrayList();
 
     if (foundCsCsi == null) {
       logger.info(PropertyAccessor.
                   getProperty("attach.package.classification"));
       
       if (packageCsCsi != null) {
         csCsis.add(packageCsCsi);
         if((foundParentCsCsi == null) && packageCsCsi.getParent() != null)
           csCsis.add(packageCsCsi.getParent());
 
         adminComponentDAO.addClassSchemeClassSchemeItems(ac, csCsis);
         logger.info(PropertyAccessor
                     .getProperty("added.package",
                                  new String[] {
                                    packageName, 
                                    ac.getLongName()}));
       } else {
         logger.error(PropertyAccessor.getProperty("missing.package", new String[] {packageName, ac.getLongName()}));
       }
       
     }
   }
 
 
   private void initDAOs() {
     adminComponentDAO = DAOAccessor.getAdminComponentDAO();
     alternateNameDAO = DAOAccessor.getAlternateNameDAO();
 //    
 //    dataElementDAO = DAOAccessor.getDataElementDAO();
 //    dataElementConceptDAO = DAOAccessor.getDataElementConceptDAO();
 //    valueDomainDAO = DAOAccessor.getValueDomainDAO();
 //    propertyDAO = DAOAccessor.getPropertyDAO();
 //    objectClassDAO = DAOAccessor.getObjectClassDAO();
 //    objectClassRelationshipDAO = DAOAccessor.getObjectClassRelationshipDAO();
 //    classificationSchemeDAO = DAOAccessor.getClassificationSchemeDAO();
 //    classificationSchemeItemDAO = DAOAccessor.getClassificationSchemeItemDAO();
     classSchemeClassSchemeItemDAO = DAOAccessor.getClassSchemeClassSchemeItemDAO();
 //    conceptDAO = DAOAccessor.getConceptDAO();
   }
 }
