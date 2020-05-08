 package gov.nih.nci.ncicb.cadsr.loader.validator;
 import gov.nih.nci.ncicb.cadsr.domain.AdminComponent;
 import gov.nih.nci.ncicb.cadsr.domain.DataElement;
 import gov.nih.nci.ncicb.cadsr.domain.DomainObjectFactory;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.loader.event.ProgressListener;
 import gov.nih.nci.ncicb.cadsr.loader.util.PropertyAccessor;
 import java.util.*;
 
 public class LatestVersionValidator implements Validator 
 {
   private ElementsLists elements = ElementsLists.getInstance();
   
   private ValidationItems items = ValidationItems.getInstance();
   
   public LatestVersionValidator()
   {
   }
   
   public void addProgressListener(ProgressListener l) {
 
   }
   
   public ValidationItems validate() 
   {
     List<DataElement> des = (List<DataElement>)elements.getElements(DomainObjectFactory.newDataElement().getClass());
     if(des != null) {
       for(DataElement de : des) {
         if(de.getLatestVersionIndicator().equals(
            AdminComponent.LATEST_VERSION_IND_NO))
               items.addItem(new ValidationError
                             (PropertyAccessor.getProperty
                               ("de.no.latestversion",
                                 de.getDataElementConcept().getLongName()), de));
       }
     }
     return items;
   }
 }
