 package org.biosharing.utils;
 
 import org.biosharing.model.Standard;
 import org.biosharing.model.StandardFields;
 import org.isatools.isacreator.configuration.Ontology;
 import org.isatools.isacreator.ontologymanager.BioPortalClient;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by the ISA team
  *
  * @author Eamonn Maguire (eamonnmag@gmail.com)
  *         <p/>
  *         Date: 22/05/2012
  *         Time: 11:08
  */
 public class OntologyLocator {
 
     public List<Standard> getAllOntologies() {
         BioPortalClient client = new BioPortalClient();
 
         System.out.println("Locating ontologies in BioPortal.");
 
         List<Ontology> ontologies = client.getAllOntologies(true);
 
         List<Standard> standards = new ArrayList<Standard>();
         if (ontologies != null) {
             System.out.println("Found " + ontologies.size() + " in BioPortal.");
 
 
             for (Ontology ontology : ontologies) {
 
                 if (!ontology.getOntologyAbbreviation().contains("test")) {
                     Standard standard = new Standard();
 
                     standard.initialiseStandard();
 
                     standard.getFieldToValue().put(StandardFields.STANDARD_TITLE, ontology.getOntologyAbbreviation());
                     standard.getFieldToValue().put(StandardFields.FULL_NAME, ontology.getOntologyDisplayLabel());
                     standard.getFieldToValue().put(StandardFields.TYPE, "terminology artifact");
                     standard.getFieldToValue().put(StandardFields.ORGANIZATION_URL, ontology.getHomepage());
                     standard.getFieldToValue().put(StandardFields.CONTACT, ontology.getContactName());
                     // Here we should check for available publications...
                     standards.add(standard);
                 }
             }
         } else {
             System.out.println("Problem encountered with BioPortal. Please try again later.");
         }
        System.out.println("After filtering, we have " + standards.size() + " ontologies in BioPortal.");
         return standards;
     }
 }
