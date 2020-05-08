 package de.hswt.hrm.report.latex;
 
 import de.hswt.hrm.contact.model.Contact;
 import de.hswt.hrm.plant.model.Plant;
 import de.hswt.hrm.place.model.Place;
 
 public class SystemOverviewParser {
 
     private final String CUSTOMER = ":customer";
     private final String CUSTOMER_STREET = ":customerStreet";
     private final String CUSTOMER_CITY = ":customerCity";
     private final String CUSTOMER_OBJECT = ":customerObject";
     private final String OBJECT_STREET = ":objectStreet";
     private final String OBJECT_NUMBER = ":objectNumber";
     private final String CONTRACTOR_NAME = ":contractorName";
     private final String CONTRACTOR_STREET = ":contractorStreet";
     private final String CONTRACTOR_CITY = ":contractorCity";
     private final String CONTROLLER_NAME = ":controllerName";
     private final String CONTROLLER_STREET = ":controllerStreet";
     private final String CONTROLLER_CITY = ":controllerCity";
     private final String PLANT = ":plant";
     private final String PLACE = ":place";
     private final String AREAS = ":areas";
     private final String INSPECTION_DATE = ":inspectionDate";
     private final String REPORT_DATE = ":reportDate";
 
     public String parse(String templ, Contact contactCustomer, Contact contactContractor,
             Contact conctactController, Plant plant, Place place) {
         String template = templ;
        template.replace(CUSTOMER,
                contactCustomer.getFirstName() + " " + contactCustomer.getLastName());
         template.replace(CUSTOMER_STREET,
                 contactCustomer.getStreet() + " " + contactCustomer.getStreetNo());
         template.replace(CUSTOMER_CITY,
                 contactCustomer.getPostCode() + " " + contactCustomer.getCity());
         template.replace(CUSTOMER_OBJECT, place.getPlaceName());
         template.replace(OBJECT_STREET, place.getStreet() + " " + place.getStreetNo());
         template.replace(OBJECT_NUMBER, place.getPostCode() + " " + place.getCity());
        template.replace(CONTRACTOR_NAME, contactContractor.getFirstName() + " "
                + contactContractor.getLastName());
         template.replace(CONTRACTOR_STREET,
                 contactContractor.getStreet() + " " + contactContractor.getStreetNo());
         template.replace(CONTRACTOR_CITY,
                 contactContractor.getPostCode() + " " + contactContractor.getCity());
        template.replace(CONTROLLER_NAME, conctactController.getFirstName() + " "
                + conctactController.getLastName());
         template.replace(CONTROLLER_STREET, conctactController.getStreet() + " "
                + conctactController.getLastName());
         template.replace(CONTROLLER_CITY, conctactController.getPostCode() + " "
                 + conctactController.getCity());
         template.replace(PLANT, plant.getDescription());
         // template.replace(PLACE,plant.getLocation());
 
         // template.replace(INSPECTION_DATE,);
         // template.replace(REPORT_DATE,);
 
         return template;
     }
 }
