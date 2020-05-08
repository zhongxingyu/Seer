 package controllers;
 
 import java.lang.reflect.Method;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import controllers.deadbolt.Deadbolt;
 import models.account.Account;
 import models.account.AccountEntity;
 import models.account.User;
 import models.tm.Project;
 import models.tm.TMUser;
 import models.tm.test.Tag;
 import org.hibernate.Session;
 import play.cache.Cache;
 import play.db.jpa.JPA;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Util;
 import play.mvc.With;
 import play.templates.JavaExtensions;
 import util.Logger;
 
 /**
  * Parent controller for the TM application.
  * Sets common view template data such as username, visible menu elements, ...
  * Contains common utility methods.
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @With(Deadbolt.class)
 public class TMController extends Controller {
 
     /**
      * Set Hibernate filters for multi-tenancy and so on.
      */
     @Before(priority = 0)
     public static void setFilters() {
         if (Security.isConnected()) {
             // account filter
             Long accountId = getConnectedUser().account.getId();
             ((Session) JPA.em().getDelegate()).enableFilter("account").setParameter("account_id", accountId);
 
             // active users filter
             ((Session) JPA.em().getDelegate()).enableFilter("activeUser").setParameter("active", true);
             ((Session) JPA.em().getDelegate()).enableFilter("activeTMUser").setParameter("active", true);
 
             // we need to do the lookup for an active project here first, otherwise we enable the filter before passing the parameter.
             if (controllerHasActiveProject()) {
                 Project p = getActiveProject();
                 if (p != null) {
                     ((Session) JPA.em().getDelegate()).enableFilter("project").setParameter("project_id", p.getId());
                 } else {
                     getConnectedUser().<TMUser>merge().initializeActiveProject();
                 }
             }
         }
     }
 
     /**
      * Load user details for rendering, and update the session expiration timestamp.
      */
     @Before(priority = 1)
     public static void loadConnectedUser() {
         if (Security.isConnected()) {
             User user = getConnectedUser().user;
             renderArgs.put("firstName", user.firstName);
             renderArgs.put("lastName", user.lastName);
             if (controllerHasActiveProject()) {
                 renderArgs.put("activeProject", getActiveProject());
                 renderArgs.put("projects", getConnectedUser().getProjects());
             }
 
             // TODO write a job that goes over this time every 5 minutes and flushes the time to the database
             // TODO but only if it is more recent than the time in the database
             // TODO also disconnect users for which the session is not active any longer.
             Cache.set(session.getId() + "_session_expiration", Security.getSessionExpirationTimestamp());
         }
     }
 
     public static TMUser getConnectedUser() {
         // TODO cache this better
         if (Security.isConnected()) {
             // temporary
            TMUser u = (TMUser) Cache.get(session.getId() + "_user");
             if (u == null) {
                 // TODO FIXME search by user account as well - we can only do this once we know the account from the URL
                 u = TMUser.find("from TMUser u where u.user.email = ?", Security.connected()).<TMUser>first();
                Cache.set(session.getId() + "_user", u);
             }
             return u;
         } else {
             // TODO test this!
             flash.put("url", "GET".equals(request.method) ? request.url : "/");
             try {
                 Secure.login();
             } catch (Throwable throwable) {
                 Logger.error(Logger.LogType.TECHNICAL, throwable, "Error while logging in user upon connected user query through getConnectedUser()");
             }
 
         }
         return null;
     }
 
     public static Account getConnectedUserAccount() {
         return getConnectedUser().user.account;
     }
 
     /**
      * Gets the active project for the connected user, <code>null</code> if none is set
      *
      * @return the active {@see Project}
      */
     public static Project getActiveProject() {
         if (!controllerHasActiveProject()) {
             throw new RuntimeException("Active project can't be fetched in the admin area");
         }
         return getConnectedUser().activeProject;
     }
 
     /**
      * Does this controller have the concept of active project
      *
      * @return <code>true</code> if this is not an admin controller
      */
     private static boolean controllerHasActiveProject() {
         return !request.controller.startsWith("admin");
     }
 
     /**
      * Switches the user to a different project
      *
      * @param projectId the id of the project to switch to
      */
     public static void switchActiveProject(Long projectId) {
         if (!controllerHasActiveProject()) {
             // ???
             Logger.error(Logger.LogType.TECHNICAL, "Attempt to switch to a different active project from the admin area, project ID %s, controller %s", projectId, request.controllerClass.getName());
             error("Cannot switch to a different project from the administration area");
         }
 
         checkAuthenticity();
         Project project = Lookups.getProject(projectId);
         if (project == null) {
             Logger.error(Logger.LogType.SECURITY, "Trying to switch to non-existing project with ID %s", projectId);
             notFound("Can't find project with ID " + projectId);
         }
         TMUser connectedUser = getConnectedUser();
         connectedUser = connectedUser.merge();
         if (connectedUser.getProjects().contains(project)) {
 
             // check if there is enough place on the project we want to switch to
 
             // effectively switch the user to the project
             Logger.info(Logger.LogType.USER, "Switching to project '%s'", project.name);
             connectedUser.activeProject = project;
             connectedUser.save();
 
             // invalidate roles caches and others
            Cache.set(session.getId() + "_user", connectedUser);
 
             // figure out a way to switch to the current view
             System.out.println();
             System.out.println();
             System.out.println();
             System.out.println(request.controller);
             System.out.println();
             System.out.println();
             System.out.println();
             System.out.println();
             redirect(request.controller + ".index");
 
         } else {
             Logger.error(Logger.LogType.SECURITY, "Attempt to switch to project without membership! Project: '%s'", project.name);
             forbidden("You are not a member of this project.");
         }
 
     }
 
     @Util
     public static void checkInAccount(AccountEntity accountEntity) {
         if (!accountEntity.isInAccount(getConnectedUserAccount())) {
             Logger.fatal(Logger.LogType.SECURITY, "Entity %s with ID %s is not in account %s of user %s", accountEntity.getClass(), accountEntity.getId(), getConnectedUserAccount().name, Security.connected());
             forbidden();
         }
     }
 
     /**
      * Returns a list of Tag instances given a comma-separated list of strings (tag names)
      *
      * @param tags a comma-separated list of strings
      * @param type the type of tags
      * @return a list of {@link Tag} instances
      */
     @Util
     public static List<Tag> getTags(String tags, Tag.TagType type) {
         List<Tag> tagList = new ArrayList<Tag>();
         if (tags != null) {
             for (String name : tags.split(",")) {
                 Tag t = Tag.find("from Tag t where t.name = ? and t.type = '" + type.name() + "' and t.project = ?", name.trim(), getActiveProject()).first();
                 if (t == null && name.trim().length() > 0) {
                     t = new Tag(getActiveProject());
                     t.name = name.trim();
                     t.type = type;
                     t.create();
                 }
                 if (!tagList.contains(t)) {
                     tagList.add(t);
                 }
             }
         }
         return tagList;
     }
 
 
     /////////////////////////////////////////////////////////////
     // The following code is used by the ox.form implementation /
     /////////////////////////////////////////////////////////////
 
     private final static DateFormat df = new SimpleDateFormat(play.Play.configuration.getProperty("date.format"));
 
     /**
      * Renders the values of given fields of a base object as JSON string. Values in the JSON object are prefixed with "value_".
      *
      * @param base   the base object
      * @param fields the fields (paths) to render
      */
     @Util
     public static void renderFields(Object base, String[] fields) {
         Map<String, Object> values = new HashMap<String, Object>();
         List<String> toResolve = new ArrayList<String>();
 
         // merge required field paths, sorted by length and alphabetical order
         List<String> sortedFields = Arrays.asList(fields);
         Collections.sort(sortedFields);
         for (String f : sortedFields) {
             String[] path = f.split("\\.");
             String resolve = path[0];
             for (int i = 1; i < path.length + 1; i++) {
                 if (!toResolve.contains(resolve)) {
                     toResolve.add(resolve);
                 }
                 if (i < path.length) {
                     resolve = resolve + "." + path[i];
                 }
             }
         }
 
         // here we do two assumptions: that we only have one kind of baseObject, and that the sorting works so that it will be first
         // in the future we may want to add support for more than one baseObject
         values.put(toResolve.get(0), base);
 
         for (String s : toResolve) {
             if (!values.containsKey(s)) {
                 if (s.indexOf(".") == -1) {
                     values.put(s, getValue(base, s));
                 } else {
                     // since we did resolve the required field paths beforehand, we can safely rely on our parent being already resolved
                     String parent = s.substring(0, s.lastIndexOf("."));
                     Object parentValue = values.get(parent);
                     if (parentValue != null) {
                         values.put(s, getValue(parentValue, s.substring(parent.length() + 1, s.length())));
                     } else {
                         // "nullable pointer"... we just ignore it in this case
                         values.put(s, null);
                     }
                 }
             }
         }
         Map<String, Object> result = new HashMap<String, Object>();
         for (String r : sortedFields) {
             Object val = values.get(r);
             // Gson doesn't help here, for some reason it ignores the data format setting in lists...
             if (val instanceof Date) {
                 val = df.format((Date) val);
             }
             result.put("value_" + r.replaceAll("\\.", "_"), val == null ? "" : val);
         }
         renderJSON(result);
     }
 
     @Util
     private static Object getValue(Object base, String s) {
         Method getter = null;
         try {
             getter = base.getClass().getMethod("get" + JavaExtensions.capFirst(s));
             return getter.invoke(base, new Object[0]);
         } catch (Throwable t) {
             // do nothing for now, but let the console know
             t.printStackTrace();
         }
         return null;
     }
 
 }
