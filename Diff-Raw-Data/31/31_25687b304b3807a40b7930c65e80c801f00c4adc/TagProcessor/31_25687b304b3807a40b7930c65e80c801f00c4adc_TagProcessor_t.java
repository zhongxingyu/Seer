 package org.pit.fetegeo.importer.processors;
 
 import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
 import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
 import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
 import org.pit.fetegeo.importer.objects.GenericTag;
 import org.pit.fetegeo.importer.objects.Name;
 import org.pit.fetegeo.importer.objects.Place;
 import org.pit.fetegeo.importer.objects.PostalCode;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 /**
  * Author: Pit Apps
  * Date: 10/26/12
  * Time: 3:48 PM
  */
 public class TagProcessor {
 
   private List<GenericTag> tags;
   private static final Pattern spacePcPattern = Pattern.compile("\\w+ \\w+");
   private static final Pattern dashPcPattern = Pattern.compile("\\w+-\\w+");
 
   public List<GenericTag> process(Entity entity) {
     String key, value;
     tags = new ArrayList<GenericTag>();
 
     for (Tag tag : entity.getTags()) {
       key = tag.getKey();
       value = tag.getValue();
       if (key.equalsIgnoreCase("place") || (key.equalsIgnoreCase("boundary") && value.equalsIgnoreCase("administrative"))) {
         processPlace(entity);
         break;
       } else if (key.equalsIgnoreCase("highway")) {
         // TODO: limit this to only a couple values?
         processHighway(entity);
         break;
       }
     }
     return tags;
   }
 
   /*
     Method adds a new type to out type map.
    */
   private void addToTypeList(String type) {
     Map<String, Long> typeMap = GenericTag.getTypeMap();
     if (!typeMap.containsKey(type)) {
       typeMap.put(type, (long) typeMap.size());
     }
   }
 
   private void processPlace(Entity entity) {
     Place place = new Place();
     List<Name> nameList = new ArrayList<Name>();
     String key, value;
     boolean isCountry = false, isBoundary = false;
 
     place.setId(entity.getId());
 
     for (Tag tag : entity.getTags()) {
       key = tag.getKey();
       value = tag.getValue();
 
       // Set the place type
       if (key.equalsIgnoreCase("place")) {
         if (place.getType() == null || place.getType().isEmpty()) { // don't overwrite boundary type
           place.setType(value);
           if (value.equalsIgnoreCase("country")) { // we're dealing with a country here
             isCountry = true;
           }
         }
       }
 
       // Set boundary
       else if (key.equalsIgnoreCase("boundary") && value.equalsIgnoreCase("administrative")) {
         place.setType("boundary");
       }
 
       // Set name
       else if (key.startsWith("name:") || key.endsWith("name") || key.equalsIgnoreCase("place_name")) {
         nameList.add(new Name(value, key));
       }
 
       // Set population
       else if (key.equalsIgnoreCase("population")) {
         try {
           place.setPopulation(Long.valueOf(value.trim()));
         }
         // Some population numbers are given as '#### (YYYY)' or '~####'...
         catch (NumberFormatException nfe) {
           System.err.println("Bad population at Entity: " + entity.getId() + " population " + value + ". Trying to extract number...");
           value = value.replaceAll("\\([^\\(]*\\)|[^\\d]", "").trim();
           try {
             place.setPopulation(Long.valueOf(value));
           } catch (NumberFormatException nfe2) {
             System.err.println("Could not extract number from " + value);
           }
         }
       }
 
       // Look for postcodes
       else if (key.equalsIgnoreCase("postal_code")) {
         processPostalCode(entity, tag, place);
       }
 
       // Set the admin Level
       else if (key.equalsIgnoreCase("admin_level")) {
         try {
           Integer adminLevel = Integer.valueOf(value);
           place.setAdminLevel(adminLevel);
           // find the boundaries of a country. Not all admin_level=2 relations are countries though.
           if (adminLevel == 2) {
             if (entity.getType().equals(EntityType.Relation)) {
               isBoundary = true;
             }
           }
         } catch (NumberFormatException nfe) {
           System.err.println("Bad admin level at Entity: " + entity.getId() + " admin level: " + value);
         }
       }
     }
 
     place.setNameList(nameList);
 
     // Set the country id if it's a country (we're using english names)
     if (isCountry || isBoundary) {
       Long enISOCode = LanguageProcessor.findLanguageId("en");
       for (Name name : nameList) {
         // If the name is in english, proceed
         if (!name.isLocalised() || (name.getLanguageId() != null && name.getLanguageId().equals(enISOCode))) {
           Long countryId = CountryCodeProcessor.findCountryId(name.getName());
           if (countryId != null) {
             place.setCountryId(countryId);
             break;
           }
         }
       }
     }
 
     // If this tag has a new type, add it to our list
     addToTypeList(place.getType());
     // Add all tags to the list
     tags.add(place);
   }
 
   private void processHighway(Entity entity) {
     Place highway = new Place();
     List<Name> nameList = new ArrayList<Name>();
     String key, value;
 
     highway.setId(entity.getId());
 
     for (Tag tag : entity.getTags()) {
       key = tag.getKey();
       value = tag.getValue();
       if (key.equalsIgnoreCase("highway")) {
         highway.setType(value);
       } else if (key.startsWith("name:") || key.endsWith("name")) {
         nameList.add(new Name(value, key));
       } else if (key.equalsIgnoreCase("postal_code")) {
         processPostalCode(entity, tag, highway);
       }
     }
 
     // We don't want highways without names
     if (nameList.isEmpty()) {
       return;
     }
 
     highway.setNameList(nameList);
 
     addToTypeList(highway.getType());
     tags.add(highway);
   }
 
   private void processPostalCode(Entity entity, Tag tag, GenericTag genericTag) {
     String code = tag.getValue().trim();  // trim whitespace as some postcodes are badly formatted
     PostalCode postalCode;
 
     // sometimes we have more than one postcode separated by a comma or semi-colon. We don't care about those
     if (code.matches(",|;")) {
       return;
     }
 
     // Check if the postcode is a multi-postcode (some countries use dashes, others use spaces to separate them)
     if (spacePcPattern.matcher(code).matches()) {
       String[] multiPC = code.split(" ");
       postalCode = new PostalCode(multiPC[0], multiPC[1]);
     } else if (dashPcPattern.matcher(code).matches()) {
       String[] multiPC = code.split("-");
       postalCode = new PostalCode(multiPC[0], multiPC[1]);
     } else {
       postalCode = new PostalCode(code);
     }
 
     postalCode.setId(entity.getId());
     postalCode.setType(entity.getType().toString());
     tags.add(postalCode);
 
     addToTypeList(postalCode.getType());
 
   }
 
 }
