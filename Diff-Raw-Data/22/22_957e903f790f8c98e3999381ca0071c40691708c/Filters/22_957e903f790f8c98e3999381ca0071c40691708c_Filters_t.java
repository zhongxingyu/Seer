 package controllers;
 
 import controllers.deadbolt.Restrict;
 import models.general.UnitRole;
 import models.tm.Filter;
 import models.tm.FilterConstraint;
 import models.tm.Project;
 import play.db.jpa.GenericModel;
 import play.mvc.Util;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 
 /**
  *  nikola
  * Date: 5/19/11
  * Time: 5:57 PM
  */
 public class Filters extends TMController{
 
     @Util
     public static void saveFilter(){
 
         Project project = getActiveProject();
 
         Filter filter = new Filter(project);
         filter.name = params.get("name");
         filter.entity = params.get("entity");
         filter.owner = getConnectedUser();
         filter.filterConstraints = new ArrayList<FilterConstraint>();
         
         filter.create();
 
         List<String> par = new ArrayList<String>();
         for (String p : params.all().keySet()) {
             if (p.startsWith("constraint")) {
                 System.out.println(p);
                 par.add(p);
             }
         }
 
         for(String fp : par){
             Matcher m = tagParameterPattern("constraint").matcher(fp);
             FilterConstraint filterConstraint = new FilterConstraint(project);
 
             if (m.matches()) {
                 String key1 = m.group(1).replace("'","");
                 String key2 = m.group(2).replace("['", "").replace("']", "");
                 String value = params.get(fp);
                 System.out.println("key2: " +key2);
                 System.out.println("key1: " +key1);
                 System.out.println("value: " +value);
 
                 
                 filterConstraint.property = key1;
                 filterConstraint.type = key2;
                 filterConstraint.value = value;
 
 
 
                 filter.filterConstraints.add(filterConstraint);
                 filter.save();
 
 
             }
             filterConstraint.create();
         }
     }
 
     public static List<Filter> getFilters(){
        return Filter.find("owner=?", getConnectedUser()).fetch();
     }
 }
