 package gov.nih.nci.ncicb.cadsr.loader.persister;
 
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 
 import org.apache.log4j.Logger;
 import gov.nih.nci.ncicb.cadsr.loader.defaults.UMLDefaults;
 import gov.nih.nci.ncicb.cadsr.loader.util.PropertyAccessor;
 
 import java.util.*;
 
 
 public class PackagePersister extends UMLPersister {
   
   private static Logger logger = Logger.getLogger(PackagePersister.class.getName());
 
   public PackagePersister(ElementsLists list) {
     this.elements = list;
     defaults = UMLDefaults.getInstance();
   }
 
   public void persist() {
     ClassificationSchemeItem pkg = DomainObjectFactory.newClassificationSchemeItem();
 
     ClassificationSchemeItem subProject = DomainObjectFactory.newClassificationSchemeItem();
 
     List packages = (List) elements.getElements(pkg.getClass());
 
     Map packageCsCsis = defaults.getPackageCsCsis();
 
     if (packages != null) {
       for (ListIterator it = packages.listIterator(); it.hasNext();) {
 	pkg = (ClassificationSchemeItem) it.next();
 	pkg.setAudit(defaults.getAudit());
 	pkg.setType(ClassificationSchemeItem.TYPE_UML_PACKAGE);
 
         subProject.setName(defaults.getPackageDisplay(pkg.getName()));
         // Verify is there is a sub project
         if(!subProject.getName().equals(pkg.getName())) {
           subProject.setAudit(defaults.getAudit());
           subProject.setType(ClassificationSchemeItem.TYPE_UML_PROJECT
 );
           // See if it already exist in DB
           List l = classificationSchemeItemDAO.find(subProject);
           
           if (l.size() == 0) { // not in DB, create it.
             subProject.setId(classificationSchemeItemDAO.create(subProject));
           } else {
             subProject = (ClassificationSchemeItem) l.get(0);
           }
         } else {
 
         }
         
 
 	// See if it already exist in DB
 	List l = classificationSchemeItemDAO.find(pkg);
 
 	if (l.size() == 0) { // not in DB, create it.
 	  pkg.setId(classificationSchemeItemDAO.create(pkg));
 	} else {
 	  pkg = (ClassificationSchemeItem) l.get(0);
 	}
 
         ClassSchemeClassSchemeItem subCsCsi = null;
         if(subProject.getId() != null) {
           subCsCsi = linkCsiToCs(subProject, defaults.getProjectCs(), null);
         }
 
         ClassSchemeClassSchemeItem packageCsCsi = linkCsiToCs(pkg, defaults.getProjectCs(), subCsCsi);
 
 	// Put CS_CSI in cache so OCs can use it
 	packageCsCsis.put(pkg.getName(), packageCsCsi);
 
         
       }
     }
 
   }
 
   private ClassSchemeClassSchemeItem linkCsiToCs(ClassificationSchemeItem csi, ClassificationScheme cs, ClassSchemeClassSchemeItem parent) {
 	List csCsis = cs.getCsCsis();
 	boolean found = false;
 	ClassSchemeClassSchemeItem newCsCsi = null;
 
         if(csCsis != null && csCsis.size() > 0) {
           for (ListIterator it = csCsis.listIterator(); it.hasNext();) {
             ClassSchemeClassSchemeItem csCsi = (ClassSchemeClassSchemeItem) it.next();
             
             if (csCsi.getCsi().getType().equals(csi.getType()) &&
                 csCsi.getCsi().getName().equals(csi.getName())) {
              // There's already a CS_CSI. 
              // Does it have the same parent?
              
              if(
                 ((csCsi.getParent() == null) 
                  && (parent == null))
                 || 
                 ((csCsi.getParent() != null)
                  && (parent != null)
                  && (csCsi.getParent().getId() == parent.getId()))
                 )
                {
                  newCsCsi = csCsi;
//                   if(parent != null) 
//                     newCsCsi.setParent(parent);
                  found = true;
                }
            } 
           }
         }
 
 	if (!found) {
 	  logger.info(
             PropertyAccessor
             .getProperty("link.package.to.project", csi.getName()));
           newCsCsi = DomainObjectFactory.newClassSchemeClassSchemeItem();
 	  newCsCsi.setCs(cs);
 	  newCsCsi.setCsi(csi);
 	  newCsCsi.setLabel(csi.getName());
           if(newCsCsi.getLabel().length() > 30)
             newCsCsi.setLabel(
               newCsCsi.getLabel().substring(0, 29));
 	  newCsCsi.setAudit(defaults.getAudit());
           if(parent != null) {
             newCsCsi.setParent(parent);
           }
 	  newCsCsi.setId(classificationSchemeDAO
                          .addClassificationSchemeItem
                          (cs, newCsCsi));
 
           defaults.refreshProjectCs();
 	  logger.info(PropertyAccessor.getProperty("added.package"));
 	}
         return newCsCsi;
   }
 
 }
