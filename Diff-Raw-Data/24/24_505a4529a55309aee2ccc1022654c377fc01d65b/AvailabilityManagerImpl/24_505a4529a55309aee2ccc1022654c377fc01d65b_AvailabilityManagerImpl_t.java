 /**
  * 
 * $Id: AvailabilityManagerImpl.java,v 1.10 2006-04-18 16:58:55 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
 * Revision 1.9  2006/04/17 19:11:06  pandyas
 * caMod 2.1 OM changes
 *
  * 
  */
 
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.domain.AnimalAvailability;
 import gov.nih.nci.camod.domain.AnimalDistributor;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.Person;
 import gov.nih.nci.camod.service.AvailabilityManager;
 import gov.nih.nci.camod.webapp.form.AvailabilityData;
 import java.util.List;
 
 public class AvailabilityManagerImpl extends BaseManager implements AvailabilityManager
 {
 
     public List getAll() throws Exception
     {
        log.info("In AvailabilityManagerImpl.getAll");
         return super.getAll(AnimalAvailability.class);
     }
 
     public AnimalAvailability get(String id) throws Exception
     {
        log.info("In AvailabilityManagerImpl.get");
         return (AnimalAvailability) super.get(id, AnimalAvailability.class);
     }
 
     public void save(AnimalAvailability availability) throws Exception
     {
        log.info("In AvailabilityManagerImpl.save");
         super.save(availability);
     }
 
     public void remove(String id,
                        AnimalModel inAnimalModel) throws Exception
     {
        log.info("In AvailabilityManagerImpl.remove");
 
         inAnimalModel.getAnimalAvailabilityCollection().remove(get(id));
         super.save(inAnimalModel);
     }
 
     public AnimalAvailability create(AvailabilityData inAvailabilityData) throws Exception
     {
         log.info("Entering AvailabilityManagerImpl.create");
 
         AnimalAvailability theAvailability = new AnimalAvailability();
         populateAvailability(inAvailabilityData, theAvailability);
 
         log.info("Exiting AvailabilityManagerImpl.create");
 
         return theAvailability;
     }
 
     public AnimalAvailability createInvestigator(AvailabilityData inAvailabilityData) throws Exception
     {
         log.info("Entering AvailabilityManagerImpl.createInvestigator");
 
         AnimalAvailability theAvailability = new AnimalAvailability();
         populateInvestigatorAvailability(inAvailabilityData, theAvailability);
 
         log.info("Exiting AvailabilityManagerImpl.createInvestigator");
 
         return theAvailability;
     }
 
     public void update(AvailabilityData inAvailabilityData,
                        AnimalAvailability inAvailability) throws Exception
     {
         log.info("Entering AvailabilityManagerImpl.update");
         log.info("Updating AvailabilityData (ID): " + inAvailability.getId());
 
         // Populate w/ the new values and save
         editAvailability(inAvailabilityData, inAvailability);
         save(inAvailability);
 
         log.info("Exiting AvailabilityManagerImpl.update");
     }
 
     public void updateInvestigatorAvailability(AvailabilityData inAvailabilityData,
                                                AnimalAvailability inAvailability) throws Exception
     {
         log.info("Entering AvailabilityManagerImpl.updateInvestigatorAvailability");
         log.info("Updating AvailabilityData: " + inAvailability.getId());
 
         // Populate w/ the new values and save
         editInvestigatorAvailability(inAvailabilityData, inAvailability);
         save(inAvailability);
 
         log.info("Exiting AvailabilityManagerImpl.updateInvestigatorAvailability");
     }
 
     private void populateAvailability(AvailabilityData inAvailabilityData,
                                       AnimalAvailability inAvailability) throws Exception
     {
         log.info("Entering AvailabilityManagerImpl.populateAvailability");
 
         inAvailability.setName(inAvailabilityData.getName());
         inAvailability.setStockNumber(inAvailabilityData.getStockNumber());
 
         /* get distributor object */
         AnimalDistributor theDistributor = AnimalDistributorManagerSingleton.instance().getByName(inAvailabilityData.getSource());
         log.info("theDistributor: " + theDistributor);
         
         // set distributor in 2.1
         inAvailability.setAnimalDistributor(theDistributor);
 
         log.info("Added AnimalDistributor");
 
         log.info("Exiting AvailabilityManagerImpl.populateAvailability");
 
     }
 
     private void editAvailability(AvailabilityData inAvailabilityData,
                                   AnimalAvailability inAvailability) throws Exception
     {
         log.info("Entering AvailabilityManagerImpl.editAvailability");
 
         inAvailability.setName(inAvailabilityData.getName());
         inAvailability.setStockNumber(inAvailabilityData.getStockNumber());
 
         log.info("Exiting AvailabilityManagerImpl.editAvailability");
 
     }
 
     private void populateInvestigatorAvailability(AvailabilityData inAvailabilityData,
                                                   AnimalAvailability inAvailability) throws Exception
     {
         log.info("Entering AvailabilityManagerImpl.populateInvestigatorAvailability");
 
         // set Availability name
         inAvailability.setName(inAvailabilityData.getName());
         log.info("setName: " + inAvailabilityData.getName());
 
         if (inAvailabilityData.getStockNumber() != null && inAvailabilityData.getStockNumber().length() > 0)
         {
             /* Convert the PI name from dropdown to PI_id stored in DB */
             Person thePI = PersonManagerSingleton.instance().getByUsername(inAvailabilityData.getStockNumber());
 
             log.info("thePI : " + thePI.toString());
 
             log.info("thePI.getId().toString(): " + thePI.getId().toString());
             inAvailability.setStockNumber(thePI.getId().toString());
         }
         else
         {
             inAvailability.setStockNumber("-1");
         }
 
         /* get distributor object */
         AnimalDistributor theDistributor = AnimalDistributorManagerSingleton.instance().getByName(inAvailabilityData.getSource());
         log.info("theDistributor): " + theDistributor);
 
         inAvailability.setAnimalDistributor(theDistributor);
         log.info("Exiting AvailabilityManagerImpl.populateInvestigatorAvailability");
     }
 
     private void editInvestigatorAvailability(AvailabilityData inAvailabilityData,
                                               AnimalAvailability inAvailability) throws Exception
     {
         log.info("Entering AvailabilityManagerImpl.editInvestigatorAvailability");
 
         // set Availability name
         inAvailability.setName(inAvailabilityData.getName());
         log.info("setName: " + inAvailabilityData.getName());
 
         if (inAvailabilityData.getStockNumber() != null && inAvailabilityData.getStockNumber().length() > 0)
         {
             /* Convert the PI name from dropdown to PI_id stored in DB */
             Person thePI = PersonManagerSingleton.instance().getByUsername(inAvailabilityData.getStockNumber());
 
             log.info("thePI : " + thePI.toString());
 
             log.info("thePI.getId().toString(): " + thePI.getId().toString());
             inAvailability.setStockNumber(thePI.getId().toString());
         }
         else
         {
             inAvailability.setStockNumber("-1");
         }
 
         log.info("Exiting AvailabilityManagerImpl.editInvestigatorAvailability");
     }
 }
