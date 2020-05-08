 package gov.nih.nci.ncicb.cadsr.loader.persister;
 
 import gov.nih.nci.ncicb.cadsr.dao.*;
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 
 import gov.nih.nci.ncicb.cadsr.loader.util.PropertyAccessor;
 
 import gov.nih.nci.ncicb.cadsr.loader.defaults.UMLDefaults;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 
 public class OcRecPersister extends UMLPersister {
 
   private static Logger logger = Logger.getLogger(OcRecPersister.class.getName());
 
   public OcRecPersister(ElementsLists list) {
     this.elements = list;
     defaults = UMLDefaults.getInstance();
   }
 
   public void persist() throws PersisterException {
     ObjectClassRelationship ocr = DomainObjectFactory.newObjectClassRelationship();
     List ocrs = (List) elements.getElements(ocr.getClass());
 
     if (ocrs != null) {
       for (ListIterator it = ocrs.listIterator(); it.hasNext();) {
 	ocr = (ObjectClassRelationship) it.next();
 	ocr.setContext(defaults.getContext());
 	ocr.setAudit(defaults.getAudit());
 	ocr.setVersion(defaults.getVersion());
 	ocr.setWorkflowStatus(defaults.getWorkflowStatus());
 
         String sourcePackage = getPackageName(ocr.getSource());
         String targetPackage = getPackageName(ocr.getTarget());
 
 	ocr.setPreferredDefinition(new OCRDefinitionBuilder().buildDefinition(ocr));
 
 	if ((ocr.getLongName() == null) ||
 	    (ocr.getLongName().length() == 0)) {
 	  logger.debug("No Role name for association. Generating one");
 	  ocr.setLongName(new OCRRoleNameBuilder().buildRoleName(ocr));
 	}
 
 	List ocs = elements.
 	  getElements(DomainObjectFactory.newObjectClass()
 		      .getClass());
 
 	for (int j = 0; j < ocs.size(); j++) {
 	  ObjectClass o = (ObjectClass) ocs.get(j);
 
 	  if (o.getLongName().equals(ocr.getSource().getLongName())) {
 	    ocr.setSource(o);
 	  } 
           if (o.getLongName().equals(ocr.getTarget()
 					    .getLongName())) {
 	    ocr.setTarget(o);
 	  }
 	}
 
 //         logger.info(PropertyAccessor
 //                     .getProperty("created.association"));
 	LogUtil.logAc(ocr, logger);
 	logger.info(PropertyAccessor
                     .getProperty("source.role",  ocr.getSourceRole()));
 	logger.info(PropertyAccessor
                     .getProperty("source.class",  ocr.getSource().getLongName()));
 	logger.info(PropertyAccessor.getProperty
                     ("source.cardinality", new Object[]
                       {new Integer(ocr.getSourceLowCardinality()),
                        new Integer(ocr.getSourceHighCardinality())}));
 	logger.info(PropertyAccessor
                     .getProperty("target.role",  ocr.getTargetRole()));
 	logger.info(PropertyAccessor
                     .getProperty("target.class",  ocr.getTarget().getLongName()));
 	logger.info(PropertyAccessor.getProperty
                     ("target.cardinality", new Object[]
                       {new Integer(ocr.getTargetLowCardinality()),
                        new Integer(ocr.getTargetHighCardinality())}));
 	logger.info(PropertyAccessor.getProperty
                     ("direction", ocr.getDirection()));
 	logger.info(PropertyAccessor.getProperty
                     ("type", ocr.getType()));
 
 	// check if association already exists
 	ObjectClassRelationship ocr2 = DomainObjectFactory.newObjectClassRelationship();
 	ocr2.setSource(ocr.getSource());
 	ocr2.setSourceRole(ocr.getSourceRole());
 	ocr2.setTarget(ocr.getTarget());
 	ocr2.setTargetRole(ocr.getTargetRole());
 
 	List eager = new ArrayList();
 	eager.add(EagerConstants.AC_CS_CSI);
 
 	List l = objectClassRelationshipDAO.find(ocr2, eager);
 // 	boolean found = false;
 
 // 	if (l.size() > 0) {
 // 	  for (Iterator it2 = l.iterator(); it2.hasNext();) {
 // 	    ocr2 = (ObjectClassRelationship) it2.next();
 
 // 	    List acCsCsis = (List) ocr2.getAcCsCsis();
 
 // 	    for (Iterator it3 = acCsCsis.iterator(); it3.hasNext();) {
 // 	      AdminComponentClassSchemeClassSchemeItem acCsCsi = (AdminComponentClassSchemeClassSchemeItem) it3.next();
 
 // 	      if (acCsCsi.getCsCsi().getCs().getLongName().equals(defaults.getProjectCs().getLongName())) {
 // 	      found = true;
 // 	      logger.debug("Association with same classification already found");
 // 	      }
 	      
 // 	    }
 // 	  }
 // 	}
         
         if (l.size() > 0) {
           logger.info(PropertyAccessor.getProperty("existed.association"));
           ocr = (ObjectClassRelationship)l.get(0);
         } else {
//           ocr.setPreferredName(
//             ocr.getSource().getPublicId() + "-" +
//             ocr.getSource().getVersion() + ":" + 
//             ocr.getTarget().getPublicId() + "-" + 
//             ocr.getTarget().getVersion()
//             );
 
           ocr.setId(objectClassRelationshipDAO.create(ocr));
           // 	  addProjectCs(ocr);
 	  logger.info(PropertyAccessor.getProperty("created.association"));
 	}
         
         addPackageClassification(ocr, sourcePackage);
         addPackageClassification(ocr, targetPackage);
       }
     }
   }    
   
 }
