 package gov.nih.nci.ncicb.cadsr.loader.persister;
 
 import gov.nih.nci.ncicb.cadsr.loader.UMLDefaults;
 import gov.nih.nci.ncicb.cadsr.dao.*;
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 
 public class DECPersister extends UMLPersister {
 
   private static Logger logger = Logger.getLogger(DECPersister.class.getName());
 
   public DECPersister(ElementsLists list) {
     this.elements = list;
     defaults = UMLDefaults.getInstance();
   }
 
   public void persist() throws PersisterException {
     DataElementConcept dec = DomainObjectFactory.newDataElementConcept();
     List decs = (List) elements.getElements(dec.getClass());
 
    logger.debug("decs... ");
     if (decs != null) {
       for (ListIterator it = decs.listIterator(); it.hasNext();) {
 	dec = (DataElementConcept) it.next();
 
 	dec.setContext(defaults.getContext());
 	dec.setConceptualDomain(defaults.getConceptualDomain());
 
 	int ind = dec.getLongName().lastIndexOf(".");
 
 	if (ind > 0) {
 	  dec.setLongName(dec.getLongName().substring(ind + 1));
 	}
 
 	logger.debug("dec name: " + dec.getLongName());
 
 	// does this dec exist?
 	List l = dataElementConceptDAO.find(dec);
 
 	if (l.size() == 0) {
 	  // !!!!! TODO
 	  dec.setPreferredDefinition(dec.getLongName());
 	  dec.setPreferredName(dec.getLongName());
 
 	  dec.setVersion(defaults.getVersion());
 	  dec.setWorkflowStatus(defaults.getWorkflowStatus());
 
 	  List ocs = elements.getElements(DomainObjectFactory.newObjectClass()
 					  .getClass());
 
 	  for (int j = 0; j < ocs.size(); j++) {
 	    ObjectClass o = (ObjectClass) ocs.get(j);
 
 	    if (o.getLongName().equals(dec.getObjectClass()
 				       .getLongName())) {
 	      dec.setObjectClass(o);
 	    }
 	  }
 
 	  List props = elements.getElements(DomainObjectFactory.newProperty()
 					    .getClass());
 
 	  for (int j = 0; j < props.size(); j++) {
 	    Property o = (Property) props.get(j);
 
 	    if (o.getLongName().equals(dec.getProperty()
 				       .getLongName())) {
 	      dec.setProperty(o);
 	    }
 	  }
 
 	  dec.setAudit(defaults.getAudit());
 	  dec.setId(dataElementConceptDAO.create(dec));
 	  logger.info("Created DataElementConcept: ");
 	} else {
 	  dec = (DataElementConcept) l.get(0);
 	  logger.info("DataElementConcept Existed: ");
 	}
 
 	LogUtil.logAc(dec, logger);
 	logger.info("-- Object Class (long_name): " +
                     dec.getObjectClass().getLongName());
 	logger.info("-- Property (long_name): " +
                     dec.getProperty().getLongName());
 
 	addProjectCs(dec);
 	it.set(dec);
 
 	// add designation to hold package name
 	// !!!! TODO
       }
     }
 
   }
 
 
 }
