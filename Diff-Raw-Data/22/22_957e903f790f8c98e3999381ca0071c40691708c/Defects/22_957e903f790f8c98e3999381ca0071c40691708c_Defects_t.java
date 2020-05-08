 package controllers;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.Query;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonSerializer;
 import controllers.deadbolt.Restrict;
 import controllers.tabularasa.TableController;
 import models.general.UnitRole;
 import models.tm.*;
 import models.tm.test.Tag;
 import org.apache.commons.lang.StringUtils;
 import play.mvc.Before;
 import play.mvc.Router;
 import util.FilterQuery;
 
 
 /**
  * @author Nikola Milivojevic
  */
 public class Defects extends TMController {
 
     private static final String[] sortBy = {"id", "name", "tags", "assignedTo", "submittedBy", "status.name", "created"};
 
     @Restrict(UnitRole.DEFECTVIEW)
     public static void index() {
         List<TMUser> users = TMUser.listByProject(getActiveProject().getId());
         render(users);
     }
 
     @Restrict(UnitRole.DEFECTVIEW)
     public static void defects(String tableId,
                                Integer iDisplayStart,
                                Integer iDisplayLength,
                                String sColumns,
                                String sEcho,
                                String title,
                                Integer titleCase,
                                String tags,
                                String status,
                                Long assignedToId,
                                Long submittedById,
                                Date dateFrom,
                                Date dateTo,
                                Integer iSortCol_0,
                                String sSortDir_0) {
 
 
         FilterQuery fq = new FilterQuery(Defect.class);
 
         fq.addFilter("project", "=", getActiveProject());
 
         if (title != null && !StringUtils.isEmpty(title)) {
             if (titleCase == null)
                 titleCase = 0;
             switch (titleCase) {
                 case (0):
                     fq.addFilter("name", "=", title);
                     break;
                 case (1):
                     fq.addFilter("name", "like", '%' + title + '%');
                     break;
                 case (2):
                     fq.addFilter("name", "like", title + '%');
                     break;
                 case (3):
                     fq.addFilter("name", "not like", '%' + title + '%');
                     break;
             }
         }
         if (tags != null && !StringUtils.isEmpty(tags)) {
             fq.addJoin("tags", "o", "t");
             fq.setDistinct(true);
             // Hibernate has a nasty bug that will yield in a ClassCastException when passing a String[], so we need to cast here
             fq.addWhere("t.name in (:tags)", "tags", Arrays.asList(tags.split(",")));
             fq.addAfterWhere("group by o.id having count(t.id) = " + tags.split(",").length);
         }
         if (status != null && !StringUtils.isEmpty(status)) {
             fq.addFilter("status.name", "=", status);
         }
         if (assignedToId != null) {
             fq.addFilter("assignedTo.id", "=", assignedToId);
         }
         if (submittedById != null) {
             fq.addFilter("submittedBy.id", "=", submittedById);
         }
         if (dateFrom != null) {
             fq.addWhere("o.created >= :dateFrom", "dateFrom", dateFrom);
         }
         if (dateTo != null) {
             fq.addWhere("o.created <= :dateTo", "dateTo", dateTo);
         }
 
         if (iSortCol_0 != null)
             fq.addAfterWhere("order by " + sortBy[iSortCol_0] + " " + sSortDir_0);
 
 
         Query query = fq.build();
 
 
         if (iDisplayStart != null) {
             query.setFirstResult(iDisplayStart);
         }
         if (iDisplayLength != null) {
             query.setMaxResults(iDisplayLength);
         }
 
         List defects = query.getResultList();
 
         TableController.renderJSON(defects, Defect.class, Defect.count(), sColumns, sEcho);
     }
 
     @Before
     public static void handleTags() {
         if (request.actionMethod.equals("createDefect") || request.actionMethod.equals("updateDefect")) {
             processTags("defect.tags", Tag.TagType.DEFECT);
         }
     }
 
     @Restrict(UnitRole.DEFECTCREATE)
     public static void createDefect(Defect defect) {
         defect.submittedBy = getConnectedUser();
         defect.account = getConnectedUserAccount();
         defect.project = getActiveProject();
         defect.status = DefectStatus.getDefaultDefectStatus();
         defect.create();
         ok();
     }
 
     @Restrict(UnitRole.DEFECTEDIT)
     public static void updateDefect(Defect defect) {
         Defect d = Defect.findById(defect.id);
         d.name = defect.name;
         d.description = defect.description;
         d.assignedTo = defect.assignedTo;
         d.status = defect.status;
         d.save();
         ok();
     }
 
     @Restrict(UnitRole.DEFECTVIEW)
     public static void defectDetails(Long baseObjectId, String[] fields) {
         Defect defect = Defect.findById(baseObjectId);
         renderFields(defect, fields);
     }
 
     @Restrict(UnitRole.DEFECTDELETE)
     public static void deleteDefect(Long defectId) {
         Defect defect = Defect.findById(defectId);
         if(defect==null){
             error("Defect is not found!");
         }
         defect.delete();
         ok();
     }
 
     @Restrict(UnitRole.DEFECTVIEW)
     public static void allTags(String q) {
         Lookups.allTags(getActiveProject().getId(), Tag.TagType.DEFECT, q);
     }
 
     @Restrict(UnitRole.DEFECTVIEW)
     public static void export() {
         export(Defect.class);
 
     }
 
     public static void saveFilter(){
         Filters.saveFilter();
     }
 
     public static void loadFilters(){
 
         JsonObject result = new JsonObject();
         JsonArray jsonFilters = new JsonArray();
         List<Filter> filterList = Filters.getFilters();
         for (Filter f : filterList) {
             JsonObject c = new JsonObject();
             c.addProperty("filterId", f.getId());
             c.addProperty("name", f.getName());
             jsonFilters.add(c);
         }
         result.add("availableFilters", jsonFilters);
         renderJSON(result.toString());
 
     }
 
     public static void loadFilterById(Long id){
        Filter filter = Filter.find("from Filter f where f.id = ? and f.owner = ?", id, getConnectedUser()).<Filter>first();
         JsonObject c = new JsonObject();
         for(FilterConstraint fc: filter.filterConstraints){
             c.addProperty(fc.property, fc.value);
         }
         renderJSON(c.toString());
     }
 
 }
