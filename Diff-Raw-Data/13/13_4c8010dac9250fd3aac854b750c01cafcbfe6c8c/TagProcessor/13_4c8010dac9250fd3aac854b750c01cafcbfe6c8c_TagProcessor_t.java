 package org.pit.fetegeo.importer.processors;
 
 import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
 import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
 import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
 import org.pit.fetegeo.importer.objects.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Author: Pit Apps
  * Date: 10/26/12
  * Time: 3:48 PM
  */
 public class TagProcessor {
 
   private List<GenericTag> tags;
 
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
       } else if (key.startsWith("addr")) {
         processAddress(entity);    // TODO: IGNORE THIS TAG?
         break;
       }
     }
     return tags;
   }
 
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
     place.setOriginEntity(entity.getType());
 
     for (Tag tag : entity.getTags()) {
       key = tag.getKey();
       value = tag.getValue();
       if (key.equalsIgnoreCase("place")) {
         if (place.getType() == null || place.getType().isEmpty()) { // don't overwrite boundary type
           place.setType(value);
           if (value.equalsIgnoreCase("country")) { // we're dealing with a country here
             isCountry = true;
           }
         }
       } else if (key.equalsIgnoreCase("boundary") && value.equalsIgnoreCase("administrative")) {
         place.setType("boundary");
       } else if (key.startsWith("name:") || key.endsWith("name") || key.equalsIgnoreCase("place_name")) {
         nameList.add(new Name(value, key));
       } else if (key.equalsIgnoreCase("population")) {
        try {
          place.setPopulation(Long.valueOf(value));
        }
        // Some population numbers are given as '#### (YYYY)'...
        catch (NumberFormatException nfe) {
          value = value.replaceAll("\\([^\\(]*\\)", "");
          try {
            place.setPopulation(Long.valueOf(value));
          } catch (NumberFormatException nfe2) {
            // Not a number?
          }
        }
       } else if (key.equalsIgnoreCase("postal_code")) {
         processPostalCode(entity, tag, place);
       }
 
       // find the boundaries of a country. Not all admin_level=2 relations are countries though.
       else if (key.equalsIgnoreCase("admin_level") && value.equalsIgnoreCase("2")) {
         if (entity.getType().equals(EntityType.Relation)) {
           isBoundary = true;
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
     Highway highway = new Highway();
     List<Name> nameList = new ArrayList<Name>();
     String key, value;
 
     highway.setId(entity.getId());
     highway.setOriginEntity(entity.getType());
 
     for (Tag tag : entity.getTags()) {
       key = tag.getKey();
       value = tag.getValue();
       if (key.equalsIgnoreCase("highway")) {
         highway.setType(value);
       } else if (key.startsWith("name:") || key.endsWith("name")) {
         nameList.add(new Name(value, key));
       } else if (key.equalsIgnoreCase("ref")) {
         highway.setRef(value);
       } else if (key.equalsIgnoreCase("postal_code")) {
         processPostalCode(entity, tag, highway);
       }
     }
 
     // We don't want highways without names
     if (nameList.isEmpty() && highway.getRef() == null) {
       return;
     }
 
     highway.setNameList(nameList);
 
     addToTypeList(highway.getType());
     tags.add(highway);
   }
 
   private void processAddress(Entity entity) {
     Address address = new Address();
     List<Name> nameList = new ArrayList<Name>();
     String key, value;
 
     address.setId(entity.getId());
     address.setOriginEntity(entity.getType());
     address.setType("addr");
 
     for (Tag tag : entity.getTags()) {
       key = tag.getKey();
       value = tag.getValue();
 
       if (key.startsWith("addr:")) {
         String addressType = key.split(":")[1];
         if (addressType.equalsIgnoreCase("housenumber")) {
           address.setHouseNumber(value);
         } else if (addressType.equalsIgnoreCase("street")) {
           address.setStreetName(value);
         }
       } else if (key.startsWith("name:") || key.endsWith("name")) {
         nameList.add(new Name(value, key));
       }
       if (key.equalsIgnoreCase("postal_code") || key.equalsIgnoreCase("addr:postcode")) {  // TODO: do we really need these? probably not! (too specific)
         processPostalCode(entity, tag, address);
       }
     }
 
     // We don't want addresses without names AND refs
     if (nameList.isEmpty() || address.getRef() == null) {
       return;
     }
 
     address.setNameList(nameList);
 
     tags.add(address);
   }
 
   // We allow for duplicate postcodes so that we can then store all locations for a given post_code (and combine the area using PostGIS)
   private void processPostalCode(Entity entity, Tag tag, GenericTag genericTag) {
     String[] values = tag.getValue().split(",|;"); // sometimes we have more than one postcode separated by a comma or semi-colon
 
     for (String code : values) {
       PostalCode postalCode = new PostalCode(code.trim()); // trim whitespace as some postcodes are badly formatted
       postalCode.setId(entity.getId());
       postalCode.setType(entity.getType().toString());
       postalCode.setOriginEntity(entity.getType());
       tags.add(postalCode);
       genericTag.setPostCodeId(postalCode.getPostCodeId());
     }
   }
 
 }
