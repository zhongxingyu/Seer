 package models;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import com.avaje.ebean.Page;
 import views.formdata.SurferFormData;
 
 /**
  * Database that stores Surfers.
  */
 public class SurferDB {
   private static final int PAGE_SIZE = 15;
   private static final int RANDOM_SURFER_LIST_SIZE = 3;
   
   /**
    * Add a Surfer to the database.
    * @param slug The slug of the Surfer.
    * @param surferFD Form data of the Surfer.
    * @return The Surfer that was just added.
    */
   public static Surfer add(String slug, SurferFormData surferFD) {
     Surfer surfer;
     if (!doesSurferExist(slug)) {
       surfer = new Surfer(surferFD.name, surferFD.hometown, surferFD.awards, surferFD.carouselURL, surferFD.bio, 
           surferFD.bioURL, surferFD.slug, surferFD.type, surferFD.footstyle, surferFD.country);
       SurferUpdate update = new SurferUpdate("Create", surfer.getName());
       SurferUpdateDB.addUpdate(update);
       surfer.save();
     }
     else {
       surfer = getSurfer(slug);
       surfer.setName(surferFD.name);
       surfer.setHometown(surferFD.hometown);
       surfer.setAwards(surferFD.awards);
       surfer.setCarouselURL(surferFD.carouselURL);
       surfer.setBio(surferFD.bio);
       surfer.setBioURL(surferFD.bioURL);
       surfer.setType(surferFD.type);
       surfer.setFootstyle(surferFD.footstyle);
       surfer.setCountry(surferFD.country);
       SurferUpdateDB.addUpdate(new SurferUpdate("Edit", surfer.getName()));
       surfer.save();
     }
     return surfer;
   }
   
   /**
    * Retrieve the footstyle list.
    * @return footArray as a list 
    */
   public static List<String> getFootstyleList() {
     String[] footArray = {"Goofy", "Regular"};
     return Arrays.asList(footArray);
   }
   
   /**
    * Retrieve the rating list.
    * @return ratingArray as a list
    */
   public static List<String> getRatingList() {
     String[] ratingArray = {"5", "4", "3", "2", "1"};
     return Arrays.asList(ratingArray);
   }
 
   /**
    * Retrieve a Surfer from the database.
    * @param slug The slug of the Surfer.
    * @return The Surfer with the matching slug.
    */
   public static Surfer getSurfer(String slug) {
     return Surfer.find().where().eq("slug", slug).findUnique();
   }
   
   /**
    * Get a List of all Surfer's in the database.
    * @return A List of all Surfers.
    */
   public static List<Surfer> getSurferList() {
     return Surfer.find().all();
   }
   
   /**
    * Get a randomized List of 3 Surfers.
    * @return A List of 3 random Surfers.
    */
   public static List<Surfer> getRandomSurferList() {
     List<Surfer> list = getSurferList();
     Collections.shuffle(list);
     return list.subList(0, RANDOM_SURFER_LIST_SIZE);
   }
   
   /**
    * Check if a Surfer already exists.
    * @param slug Slug of surfer.
    * @return True if the Surfer exists, false otherwise.
    */
   public static boolean doesSurferExist(String slug) {
     return (getSurfer(slug) != null);
   }
 
   /**
    * Delete  Surfer.
    * @param slug Slug of the Surfer. 
    */
   public static void deleteSurfer(String slug) {
     Surfer surfer = getSurfer(slug);
     SurferUpdateDB.addUpdate(new SurferUpdate("Delete", SurferDB.getSurfer(slug).getName()));
     for (Favorite favorite : surfer.getFavorites()) {
       favorite.getUserInfo().getFavorites().remove(favorite);
       favorite.getUserInfo().save();
       favorite.delete();
     }
     surfer.delete();
   }
   
   /**
    * Return a list of Surfers that match the given criteria.
    * @param term The search term.
    * @param type The type of surfer.
    * @param country The country of the surfer.
    * @param page Page number to retrieve.
    * @return A List of Surfers that match the search criteria.
    */
   public static Page<Surfer> search(String term, String type, String country, int page) {
     if (type.equals("") && country.equals("")) {
       return Surfer.find().where().icontains("name", term).order("name")
           .findPagingList(PAGE_SIZE).setFetchAhead(false).getPage(page);     
     }
     else if (type.equals("")) {
       return Surfer.find().where().icontains("name", term).ieq("country", country)
           .order("name").findPagingList(PAGE_SIZE).setFetchAhead(false).getPage(page);
     }
     else if (country.equals("")) {
       return Surfer.find().where().icontains("name", term).ieq("type", type)
           .order("name").findPagingList(PAGE_SIZE).setFetchAhead(false).getPage(page);
     }
     else {
       return Surfer.find().where().icontains("name", term).ieq("type", type)
           .ieq("country", country).order("name").findPagingList(PAGE_SIZE).setFetchAhead(false).getPage(page);
     }
   }
 }
