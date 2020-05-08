 package org.pit.fetegeo.importer.processors;
 
 import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
 import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
 import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
 import org.pit.fetegeo.importer.objects.GenericTag;
 import org.pit.fetegeo.importer.objects.Name;
 import org.pit.fetegeo.importer.objects.Place;
 import org.pit.fetegeo.importer.objects.PostalCode;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 /**
  * Author: Pit Apps
  * Date: 10/26/12
  * Time: 3:48 PM
  * <p/>
  * Processes all Tags relating to a Node, Way or Relation.
  * Extracts any relevant data and returns a list of Tags.
  */
 public class TagProcessor {
 
   private List<GenericTag> tags;
   private static final Pattern SPACE_PC_PATTERN = Pattern.compile("\\w+ \\w+");
   private static final Pattern DASH_PC_PATTERN = Pattern.compile("\\w+-\\w+");
 
   // We will only save highways tagged as any of the following.
   // We really don't care about pedestrian zones and roundabouts p.ex.
   private static final List<String> HIGHWAYS = Arrays.asList("motorway", "trunk", "primary", "secondary", "tertiary", "living_street", "residential", "road", "unclassified");
   private static final List<String> NON_HIGHWAY = Arrays.asList("junction", "ice_road", "cycleway");
 
   public List<GenericTag> process(Entity entity) {
     String key, value;
     tags = new ArrayList<GenericTag>();
 
     for (Tag tag : entity.getTags()) {
       key = tag.getKey();
       value = tag.getValue();
 
       // Is this a place?
       if (key.equalsIgnoreCase("place") || (key.equalsIgnoreCase("boundary") && value.equalsIgnoreCase("administrative"))) {
         processPlace(entity);
         break;
       }
       // Maybe it's a highway!
       else if (key.equalsIgnoreCase("highway") && HIGHWAYS.contains(value.trim().toLowerCase())) {
         processHighway(entity);
         break;
       }
     }
     return tags;
   }
 
   /*
     Method adds a new type to the type map.
    */
   private void addToTypeList(String type) {
     Map<String, Long> typeMap = GenericTag.getTypeMap();
     if (!typeMap.containsKey(type) && !type.isEmpty()) {
       typeMap.put(type, (long) typeMap.size());
     }
   }
 
   /*
     If the entity is a place, we extract all information that could be useful and put it into a Place object
     so that we can write it to file later.
    */
   private void processPlace(Entity entity) {
     Place place = new Place();
     List<Name> nameList = new ArrayList<Name>();
     String key, value;
     boolean isCountry = false, isBoundary = false;
 
     place.setOsmId(entity.getId());
 
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
       // Some population numbers are given as '#### (YYYY)' or '~####'...
       // So we'll have to do an ugly try catch block to find out if we're dealing with an actual number.
       // We also print out the error so that it can hopefully be corrected in the OSM data.
       else if (key.equalsIgnoreCase("population")) {
         try {
           place.setPopulation(Long.valueOf(value.trim()));
         } catch (NumberFormatException nfe) {
          System.err.println("Bad population at " + entity.getType().name() + ": " + entity.getId() + " population " + value + ". Trying to extract number...");
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
         processPostalCode(entity, tag);
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
 
   /*
     Extract HIGHWAYS from our Tags.
     We only really need the name and a postal_code if the road is linked to one.
    */
   private void processHighway(Entity entity) {
     Place highway = new Place();
     List<Name> nameList = new ArrayList<Name>();
     String key, value;
 
     highway.setOsmId(entity.getId());
 
     for (Tag tag : entity.getTags()) {
       key = tag.getKey();
       value = tag.getValue();
       if (key.equalsIgnoreCase("highway")) {
         highway.setType(value);
       } else if (key.startsWith("name:") || key.endsWith("name")) {
         nameList.add(new Name(value, key));
       } else if (key.equalsIgnoreCase("postal_code")) {
         processPostalCode(entity, tag);
       }
       // Some highways are roundabouts; which we don't want
       else if (NON_HIGHWAY.contains(key.trim().toLowerCase())) {
         return;
       }
     }
 
     // We don't want HIGHWAYS without names
     if (nameList.isEmpty()) {
       return;
     }
 
     highway.setNameList(nameList);
 
     addToTypeList(highway.getType());
     tags.add(highway);
   }
 
   /*
     If we found a postal_code tag above, we come here to work out what kind of post code we're dealing with.
    */
   private void processPostalCode(Entity entity, Tag tag) {
     String code = tag.getValue().trim();  // trim whitespace as some postcodes are badly formatted
     PostalCode postalCode;
 
     // sometimes we have more than one postcode separated by a comma or semi-colon.
     // We don't care about those as it means the data is imprecise (where on the road does one postcode start and where does it end?)
     if (code.matches(",|;")) {
       return;
     }
 
     // Check if the postcode is a multi-postcode (some countries use dashes, others use spaces to separate them)
     if (SPACE_PC_PATTERN.matcher(code).matches()) {
       String[] multiPC = code.split(" ");
       postalCode = new PostalCode(multiPC[0], multiPC[1]);
     } else if (DASH_PC_PATTERN.matcher(code).matches()) {
       String[] multiPC = code.split("-");
       postalCode = new PostalCode(multiPC[0], multiPC[1]);
     } else {
       postalCode = new PostalCode(code);
     }
 
     postalCode.setOsmId(entity.getId());
     postalCode.setType(entity.getType().toString());
     tags.add(postalCode);
 
     addToTypeList(postalCode.getType());
 
   }
 
 }
