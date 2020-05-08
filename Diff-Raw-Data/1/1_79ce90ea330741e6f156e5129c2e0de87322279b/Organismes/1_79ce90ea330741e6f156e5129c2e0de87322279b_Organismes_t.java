 package controllers;
 
 import models.*;
 import notifier.Mails;
 import play.Logger;
 import play.data.validation.Required;
 import play.data.validation.Valid;
 import play.modules.search.Query;
 import play.modules.search.Search;
 import play.mvc.Scope;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * Controller to manage organisme pages.
  */
 public class Organismes extends AbstractController {
 
     /**
      * Display the last version of an organisme.
      *
      * @param id
      */
     public static void show(Long id) {
         OrganismeMaster master = OrganismeMaster.findById(id);
         notFoundIfNull(master);
         Organisme organisme = master.getLastVersion();
         render(id, organisme);
     }
 
     /**
      * Display the organisme form for edition (or creation).
      *
      * @param id
      */
     public static void edit(Long id) {
         // only authenticated user can edit
         isValidUser();
 
         // retrieve object
         Organisme organisme = null;
         if (id != null) {
             OrganismeMaster master = OrganismeMaster.findById(id);
             notFoundIfNull(master);
             organisme = master.getLastVersion();
         }
 
         // depends objects
         List<OrganismeType> types = OrganismeType.findAll();
         List<OrganismeActivite> activites = OrganismeActivite.findAll();
         List<OrganismeNbSalarie> nbSalaries = OrganismeNbSalarie.findAll();
 
         // render
         render(id, organisme, types, activites, nbSalaries);
     }
 
     /**
      * Save the organisme.
      *
      * @param id
      * @param organisme
      */
     public static void save(Long id, @Valid Organisme organisme, @Required Boolean cgu, Boolean participez) {
         // only authenticated user can save
         isValidUser();
 
         // is it valid ?
         if (validation.hasErrors()) {
             params.flash();
             validation.keep();
             edit(id);
         }
 
         // retrieve organisme master or create it
         OrganismeMaster master = new OrganismeMaster();
         if (id != null) {
             master = OrganismeMaster.findById(id);
         }
         else{
             master.save();
         }
 
         // don't remove the old logo
         if(organisme.logo == null){
            if(master.getLastVersion() != null)
             organisme.logo = master.getLastVersion().logo;
         }
 
         organisme.master = master;
         organisme.save();
         master.save();
 
         // send alert for admin
         if(!hasAdminRight()){
             Mails.organisme(master, getCurrentUser(), participez);
         }
 
         // redirect user to show
         show(master.id);
     }
 
     /**
      * Display the history of the organisme.
      *
      * @param id
      */
     public static void history(Long id) {
         // only for admin
         isAdminUser();
 
         // retrieve organisme master
         OrganismeMaster master = OrganismeMaster.findById(id);
         notFoundIfNull(master);
 
         render(master);
     }
 
     /**
      * Display the version of the organisme.
      *
      * @param id
      */
     public static void version(Long id) {
         // only for admin
         isAdminUser();
 
         // retrive organisme
         Organisme organisme = Organisme.findById(id);
         notFoundIfNull(organisme);
 
         render("@show", organisme, organisme.master.id);
     }
 
     /**
      * Compare two version
      *
      * @param from
      * @param to
      */
     public static void compare(Long from, Long to) {
         // only for admin
         isAdminUser();
 
         // retrive organisme
         Organisme organismeFrom = Organisme.findById(from);
         Organisme organismeTo = Organisme.findById(to);
 
         // depends objects
         List<OrganismeType> types = OrganismeType.findAll();
         List<OrganismeActivite> activites = OrganismeActivite.findAll();
         List<OrganismeNbSalarie> nbSalaries = OrganismeNbSalarie.findAll();
 
         render(organismeFrom, organismeTo, types, activites, nbSalaries);
     }
 
     /**
      * Produce a RSS of last ten updated/created organisation.
      */
     public static void rss() {
         List<OrganismeMaster> masters = OrganismeMaster.findAll();
         response.contentType = "application/rss+xml";
         render(masters);
     }
 
     /**
      * Produce a CSV of all organisation items.
      */
     public static void csv() {
         response.contentType = "text/csv";
         response.setHeader("Content-Disposition", "attachment;filename=organismes.csv");
         renderText(OrganismeMaster.toCsv());
     }
 
     /**
      * Render the logo of an organisme.
      *
      * @param id
      */
     public static void logo(Long id) {
         // retrieve organisme master or create it
         OrganismeMaster master = OrganismeMaster.findById(id);
         notFoundIfNull(master);
 
         Organisme organisme = master.getLastVersion();
         notFoundIfNull(organisme.logo);
 
         response.setContentTypeIfNotSet(organisme.logo.type());
         renderBinary(organisme.logo.get());
     }
 
     public static void search(String query, List<Long> typologies, List<String> deps, Integer page) {
 
         if(page == null){
             page = 1;
         }
 
         Map<String, Object> result = OrganismeMaster.search(query, typologies, deps, page);
         List<OrganismeMaster> organismes = (List<OrganismeMaster>) result.get(OrganismeMaster.MAP_RESULT_LIST);
         Integer nbItems = (Integer) result.get(OrganismeMaster.MAP_RESULT_NB);
         Long nbTotal = Long.valueOf(OrganismeMaster.count());
 
         // populate render
         List<OrganismeType> types = OrganismeType.findAll();
         render(query, typologies, deps, types, organismes, page, nbItems, nbTotal);
     }
 
     /**
      * Render the admin interface for organisme (list all or search)
      */
     public static void admin(String search) {
         isAdminUser();
 
         List<OrganismeMaster> organismes = OrganismeMaster.findAll();
         render(organismes);
     }
 
     /**
      * Admin action to delete on organisme.
      *
      * @param id
      */
     public static void delete(Long id) {
         isAdminUser();
 
         OrganismeMaster organisme = OrganismeMaster.findById(id);
         notFoundIfNull(organisme);
         organisme.delete();
 
         admin(null);
     }
 
     /**
      * Admin action to set partenaire.
      *
      * @param id
      */
     public static void partenaire(Long id, Boolean isPartenaire) {
         isAdminUser();
 
         OrganismeMaster organisme = OrganismeMaster.findById(id);
         if (isPartenaire) {
             organisme.isPartenaire = Boolean.TRUE;
         } else {
             organisme.isPartenaire = Boolean.FALSE;
         }
         organisme.save();
         admin(null);
     }
 
     /**
      * View to list all partenaires.
      */
     public static void partenaires() {
         List<OrganismeMaster> partenaires = OrganismeMaster.getAllPartenaires();
 
         render(partenaires);
     }
 }
