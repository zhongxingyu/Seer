 package controllers;
 
 import controllers.securesocial.SecureSocial;
 import models.*;
 import org.apache.commons.lang.StringUtils;
 import play.db.jpa.JPA;
 import play.mvc.Before;
 import play.mvc.Controller;
 import utils.Utils;
 
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: super
  * Date: 3/18/12
  * Time: 3:45 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ToDoLists extends Controller {
 
     /**
      * Check whether the project is visible to the user
      */
     @Before(only = {"loadLists"})
      static void checkReadAccess() {
         Projects.checkVisibility();
     }
 
     /**
      * Check whether the project has the user as a member
      */
     @Before(unless = {"loadLists"})
     static void checkWriteAccess() {
         Projects.checkMembership();
     }
 
     /**
      * required json data structure on the UI {'list':[], 'total':..}
      * <p/>
      * also load view options from session
      * <p/>
      * this requires visibility to the project
      */
     public static void loadLists(Long projectId) {
         Project proj = Project.findById(projectId);
         List<ToDoList> toDoLists = new ArrayList<ToDoList>();
         if (proj != null) {
             toDoLists = ToDoList.find("project=? order by name", proj).fetch();
         }
         for (ToDoList toDoList : toDoLists) {
             loadListViewOptions(toDoList);
         }
         renderJSON(Utils.toJson(toDoLists));
     }
 
     /**
      * required json data structure on the UI {'list':[], 'total':..}
      * <p/>
      * This requires to be project member
      */
     public static void addList(String name, Long projectId) {
         ToDoList toDoList = new ToDoList();
         toDoList.name = name;
         toDoList.project = Project.findById(projectId);
         toDoList.creator = User.loadBySocialUser(SecureSocial.getCurrentUser());
         toDoList.save();
         try {
             Integer lastOrderIndex = (Integer) JPA.em()
                     .createQuery("select orderIndex from ToDoList order by orderIndex desc")
                     .setMaxResults(1)
                     .getSingleResult();
             toDoList.orderIndex = lastOrderIndex + 1;
         } catch (NoResultException nre) {
             // orderIndex should remain the default 0
         }
         renderText(Utils.toJson(toDoList));
     }
 
     /**
      * rename a list
      * requires to be the owner of list
      */
     public static void renameList(Long list, String name) {
         ToDoList toDoList = ToDoList.findById(list);
         if (toDoList != null) {
             toDoList.name = name;
             toDoList.save();
         }
 
         renderText(Utils.toJson(toDoList));
     }
 
     /**
      * clear (delete) a list. only if all tasks are completed
      * requires to be the owner of list
      */
     public static void clearCompletedInList(Long list) {
         int count = 0;
 
         ToDoList toDoList = ToDoList.findById(list);
         if (toDoList != null) {
             // delete in one shot
 //            count = JPA.em().createQuery("delete from ToDo t where t.dateCompleted is not null and t.toDoList.id=" + list).executeUpdate();
 //            count = JPA.em().createQuery("delete from ToDo t where t.dateCompleted is not null and t.toDoList.id=" + list).executeUpdate();
             List<ToDo> toDos = ToDo.find("toDoList=? and dateCompleted is not null", toDoList).fetch();
             for (ToDo toDo : toDos) {
                 toDo.delete();
                 count++;
             }
         }
 
         renderText(Utils.toJson(count));
     }
 
 
     /**
      * changes order of a list. has to use native query because case when then won't work otherwise.
      * <p/>
      * should be user specific
      */
     public static void changeListOrder(Long[] order) {
         int count = 0;
         if (order != null && order.length > 0) {
             String updateQuery = "update ToDoList t set t.orderIndex=CASE\n";
             for (int i=0; i<order.length; i++) {
                 Long id = order[i];
                 updateQuery += " WHEN t.id=" + id + " THEN " + i + "\n";
             }
             updateQuery +=  " END where t.id in (" + StringUtils.join(order, ",") + ")";
             count = JPA.em().createQuery(updateQuery).executeUpdate();
         }
 
         renderText(Utils.toJson(count));
     }
 
     /**
      * delete a list
      * <p/>
      * must be the owner of the list or admin of project
      */
     public static void deleteList(Long list) {
         ToDoList toDoList = ToDoList.findById(list);
         if (toDoList != null) {
             // first delete all tasks within the list
             List<ToDo> todos = ToDo.find("byToDoList", toDoList).fetch();
             for (ToDo todo : todos) {
                 todo.delete();
             }
             toDoList.delete();
         }
 
         renderText(Utils.toJson(toDoList));
     }
 
     /**
      * sets new sort order of a list
      * <p/>
      * should be user specific
      */
     public static void setListSort(Long list, String sort) {
        ToDoList toDoList = ToDoList.findById(list);
        Sort sortOrder = Sort.valueOf(sort);
        if (toDoList != null) {
             manageListViewOptions(list, true, ViewOptionType.SORT, sortOrder);
            toDoList.sort = sortOrder;
         }
 
        renderText(Utils.toJson(toDoList));
     }
 
     /**
      * toggles notesExpanded boolean property of a list
      * <p/>
      * should be user specific
      */
     public static void toggleNotesExpanded(Long list, Boolean notesExpanded) {
         ToDoList toDoList = ToDoList.findById(list);
         if (toDoList != null) {
             manageListViewOptions(list, notesExpanded, ViewOptionType.EXPAND_NOTES, null);
             toDoList.notesExpanded = notesExpanded;
         }
 
         renderText(Utils.toJson(toDoList));
     }
 
     /**
      * toggles notesExpanded boolean property of a list
      * <p/>
      * should be user specific
      */
     public static void toggleShowMetadata(Long list, Boolean showMetadata) {
         ToDoList toDoList = ToDoList.findById(list);
         if (toDoList != null) {
             manageListViewOptions(list, showMetadata, ViewOptionType.SHOW_METADATA, null);
             toDoList.showMetadata = showMetadata;
         }
 
         renderText(Utils.toJson(toDoList));
     }
 
     /**
      * get all tags contained in a to do list. If an id of -1 is passed, it means all lists in the project
      * TODO if list=-1, we should have project id and get tags under that project id
      *
      * <p/>
      * check visibility
      */
     public static void tagCloud(Long list, Long projectId) {
 
         List<Tag> tags = null;
         if (list > 0) {
             tags = JPA.em()
                 .createQuery("select t from ToDo todo right join todo.tags t where todo.toDoList.id=" + list)
                 .getResultList();
         } else if (list == -1) {
             Project proj = Project.findById(projectId);
             if (proj != null) {
                 Query query = JPA.em()
                         .createQuery("select t from Tag t where project=:project");
                 query.setParameter("project", proj);
                 tags = query.getResultList();
             }
         }
 
         renderText(Utils.toJson(tags));
     }
 
     /**
      * this manages the list view option stored in session. For example, list=1_1_2, the first digit is list id,
      * the second is SHOW_COMPLETED and NOTES_EXPANDED, the third digit is sorting by
      * @param listId
      * @param selected
      * @param viewOptionType
      * @param newSort
      */
     static void manageListViewOptions(Long listId, Boolean selected, ViewOptionType viewOptionType, Sort newSort) {
         String key = "list";
         String lists = session.get(key);
         if (StringUtils.isNotEmpty(lists)) {
             String[] listIdArray = lists.split(",");
             StringBuilder sb = new StringBuilder(50);
             int count = 0; // limit the number of list stored
             String currentList = null;
             for (String list : listIdArray) {
                 if (list.startsWith(listId.toString() + "_")) {
                     String[] oldValues = list.split("_");
                     int optionValues = Integer.valueOf(oldValues[1]);
                     int sortValue = oldValues.length > 2 ? Integer.valueOf(oldValues[2]) : 0;
                     if (viewOptionType.equals(ViewOptionType.SORT)) {
                         sortValue = newSort != null ? newSort.ordinal() : sortValue;
                     } else {
                         optionValues = selected ?
                                 optionValues | (1 << viewOptionType.ordinal()) :
                                 optionValues & (1 << viewOptionType.ordinal());
                     }
                     currentList = listId + "_" + optionValues + "_" + sortValue;
                 } else if (count < 11) {
                     // limit the number of list to 12
                     sb.append(",").append(list);
                     count++;
                 }
             }
             if (currentList == null) {
                 // new list whose options not yet stored
                 if (viewOptionType.equals(ViewOptionType.SORT)) {
                     currentList = listId + "_0_" + (newSort != null ? newSort.ordinal() : 0);
                 } else {
                     currentList = listId + "_" + (selected ? 1 << viewOptionType.ordinal() : 0) + "_0";
                 }
 
             }
             session.put(key, currentList + sb.toString());
         } else if (selected) {
             // add view option for this list to session
             session.put(key, listId + "_" + (1 << viewOptionType.ordinal()) + "_" + (newSort != null ? newSort.ordinal() : 0));
         }
     }
 
     private static void loadListViewOptions(ToDoList toDoList) {
         String key = "list";
         String lists = session.get(key);
         try {
             if (StringUtils.isNotEmpty(lists)) {
                 String[] listIdArray = lists.split(",");
                 StringBuilder sb = new StringBuilder(50);
                 int count = 0; // limit the number of list stored
                 String currentList = null;
                 for (String list : listIdArray) {
                     if (list.startsWith(toDoList.id.toString() + "_")) {
                         String[] oldValues = list.split("_");
                         toDoList.showCompleted = (Integer.valueOf(oldValues[1]) & (1 << ViewOptionType.SHOW_COMPLETED.ordinal())) >> ViewOptionType.SHOW_COMPLETED.ordinal() == 1;
                         toDoList.notesExpanded = (Integer.valueOf(oldValues[1]) & (1 << ViewOptionType.EXPAND_NOTES.ordinal())) >> ViewOptionType.EXPAND_NOTES.ordinal() == 1;
                         toDoList.showMetadata = (Integer.valueOf(oldValues[1]) & (1 << ViewOptionType.SHOW_METADATA.ordinal())) >> ViewOptionType.SHOW_METADATA.ordinal() == 1;
                         toDoList.sort = oldValues.length > 2 ? Sort.values()[Integer.valueOf(oldValues[2])] : Sort.DEFAULT;
                     }
                 }
             }
         } catch (Exception e) {
             // something is wrong, ignore view options altogether
             session.remove(key);
         }
     }
 }
