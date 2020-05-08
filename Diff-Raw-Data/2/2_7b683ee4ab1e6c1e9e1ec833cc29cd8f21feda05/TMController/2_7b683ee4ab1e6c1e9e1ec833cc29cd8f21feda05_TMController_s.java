 package controllers;
 
 import java.io.File;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 import controllers.deadbolt.Deadbolt;
 import models.account.Account;
 import models.account.AccountEntity;
 import models.account.User;
 import models.general.TemporalModel;
 import models.general.UnitRole;
 import models.tm.Defect;
 import models.tm.Project;
 import models.tm.ProjectModel;
 import models.tm.Requirement;
 import models.tm.TMUser;
 import models.tm.test.Instance;
 import models.tm.test.Run;
 import models.tm.test.Script;
 import models.tm.test.Tag;
 import org.hibernate.Session;
 import play.cache.Cache;
 import play.data.validation.Required;
 import play.db.jpa.JPA;
 import play.libs.MimeTypes;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Util;
 import play.mvc.With;
 import play.templates.JavaExtensions;
 import util.Logger;
 
 import static play.modules.excel.Excel.renderExcel;
 
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
     @Util
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
                     getConnectedUser().initializeActiveProject();
                 }
             }
         }
     }
 
     /**
      * Load user details for rendering, and update the session expiration timestamp.
      */
     @Before(priority = 1)
     @Util
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
 
     /**
      * Gets the connected user object
      *
      * @return the connected TMUser
      * @deprecated use {@link TMController#getConnectedUserId()} instead
      */
     @Util
     public static TMUser getConnectedUser() {
         if (Security.isConnected()) {
             // TODO FIXME search by user account as well - we can only do this once we know the account from the URL
             return TMUser.find("from TMUser u where u.user.email = ?", Security.connected()).<TMUser>first();
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
 
     /**
      * For logging purposes
      *
      * @return returns the full name of the connected user, null otherwise
      */
     @Util
     public static String getUserNameForLog() {
         if (session != null) {
             return getConnectedUser().user.getDebugString();
         }
         return null;
     }
 
     /**
      * Gets the ID of the connected user
      *
      * @return the ID of the connected {@link TMUser}
      */
     @Util
     public static Long getConnectedUserId() {
         // we'll be able to cache this easily
         return getConnectedUser().getId();
     }
 
     /**
      * @deprecated use getConnectedAccountId()
      */
     @Util
     public static Account getConnectedUserAccount() {
         return getConnectedUser().user.account;
     }
 
     @Util
     public static Long getConnectedUserAccountId() {
         return getConnectedUser().user.account.getId();
     }
 
     /**
      * Gets the active project for the connected user, <code>null</code> if none is set
      *
      * @return the active {@see Project}
      * @deprecated use getActiveProjectId()
      */
     @Util
     public static Project getActiveProject() {
         if (!controllerHasActiveProject()) {
             return null;
         }
         return getConnectedUser().activeProject;
     }
 
     @Util
     public static Long getActiveProjectId() {
         return getActiveProject().getId();
     }
 
     // TODO replace by something automatic
     private final static String[] adminTrees = {"accountRolesAssignmentTree", "projectRolesAssignmentTree", "projectRolesTree", "projectTree", "userTree"};
 
     /**
      * Does this controller have the concept of active project
      *
      * @return <code>true</code> if this is not an admin controller
      */
     @Util
     public static boolean controllerHasActiveProject() {
         if (request.controllerClass.equals(TMTreeController.class)) {
            return Arrays.binarySearch(adminTrees, request.params.get("treeId")) == -1;
         }
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
         if (connectedUser.getProjects().contains(project)) {
 
             // check if there is enough place on the project we want to switch to
 
             // effectively switch the user to the project
             Logger.info(Logger.LogType.USER, "Switching to project '%s'", project.name);
             connectedUser.activeProject = project;
             connectedUser.save();
 
             // invalidate roles caches and others
             Cache.set(session.getId() + "_user", connectedUser);
 
             // reload the page in the current view
             redirect(request.controller + ".index");
 
         } else {
             Logger.error(Logger.LogType.SECURITY, "Attempt to switch to project without membership! Project: '%s'", project.name);
             forbidden("You are not a member of this project.");
         }
 
     }
 
     public static void saveFilter(@Required String name) {
         if (canView()) {
             Filters.saveFilter(name, controllerToEntityMapping.get(request.controllerClass.getName()).getName());
         }
     }
 
     public static void loadFilters() {
         if (canView()) {
             Filters.loadFilters(controllerToEntityMapping.get(request.controllerClass.getName()).getName());
         }
     }
 
     public static void loadFilterById(Long id) {
         if (canView()) {
             Filters.loadFilterById(id);
         }
     }
 
     /**
      * Handler for excel import, on a Controller -> Main entity basis
      */
     public static void export() {
         if (canView()) {
             List data = JPA.em().createQuery(String.format("from %s o", controllerToEntityMapping.get(request.controllerClass.getName()).getSimpleName())).getResultList();
             DateFormat df = new SimpleDateFormat("yyyymmdd");
             renderArgs.put("fileName", getActiveProject().name + "-" + controllerToEntityMapping.get(request.controllerClass.getName()) + "s-" + df.format(new Date()));
             renderExcel(data);
         } else {
             Logger.error(Logger.LogType.SECURITY, "Unauthorized export attempt, controller " + request.controller);
             forbidden();
 
         }
     }
 
     /**
      * Upload handler for Excel files
      *
      * @param files the file array (sent via the jQuery.fileupload plugin)
      */
     public static void uploadExcel(File files) {
 
         if (canCreate()) {
             String contentType = MimeTypes.getContentType(files.getName());
             JsonArray array = new JsonArray();
             JsonObject object = new JsonObject();
             object.addProperty("name", files.getName());
             object.addProperty("type", contentType);
             object.addProperty("size", files.length());
 
             if (!contentType.equals("application/excel")) {
                 object.addProperty("error", "acceptFileTypes");
             } else {
                 // TODO actually import the file
             }
 
             array.add(object);
             renderJSON(array.toString());
         } else {
             Logger.error(Logger.LogType.SECURITY, "Unauthorized upload attempt, controller " + request.controller);
             forbidden();
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
         return getTags(Arrays.asList(tags.split(",")), type);
     }
 
     @Util
     public static List<Tag> getTags(List<String> tags, Tag.TagType type) {
         List<Tag> tagList = new ArrayList<Tag>();
         if (tags != null) {
             for (String name : tags) {
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
 
     @Util
     public static void processTags(String tagsParameterKey, Tag.TagType type) {
         List<String> tags = new ArrayList<String>();
         for (String p : params.all().keySet()) {
             if (p.startsWith(tagsParameterKey)) {
                 tags.add(p);
             }
         }
         List<String> tagNames = new ArrayList<String>();
         for (String t : tags) {
             Matcher m = tagParameterPattern(tagsParameterKey).matcher(t);
             if (m.matches()) {
                 String key = m.group(2);
                 String value = params.get(t);
                 if (key.equals("[name]")) {
                     tagNames.add(value);
                 }
                 params.remove(t);
             }
         }
         // make the play binding happy
         List<Tag> tagList = getTags(tagNames, type);
         List<String> tagIds = new ArrayList<String>();
         for (Tag tag : tagList) {
             tagIds.add(tag.getId().toString());
         }
         params.put(tagsParameterKey + ".id", tagIds.toArray(new String[tagIds.size()]));
     }
 
     @Util
     public static Pattern tagParameterPattern(String prefix) {
         return Pattern.compile("^" + prefix + "\\[([^\\]]+)\\](.*)$");
     }
 
     /////////////////////////////////////////////////////////////
     // The following code is used by the ox.form implementation /
     /////////////////////////////////////////////////////////////
 
     static {
         GsonBuilder builder = new GsonBuilder();
         // for all model entities we take the approach of serializing them by calling toString by default
         // in order to catch them all, we do this for everything extending TemporalModel.
         builder.registerTypeHierarchyAdapter(TemporalModel.class, new JsonSerializer<TemporalModel>() {
             public JsonElement serialize(TemporalModel t, Type type, JsonSerializationContext jsonSerializationContext) {
                 return jsonSerializationContext.serialize(t.toString());
             }
         });
         builder.registerTypeAdapter(Tag.class, Lookups.tagSerializer);
         gson = builder.create();
     }
 
     private static Gson gson;
 
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
 
         String formId = fields[0];
         String[] fieldNames = new String[fields.length - 1];
         for (int i = 1; i < fields.length; i++) {
             fieldNames[i - 1] = fields[i];
         }
 
         // merge required field paths, sorted by length and alphabetical order
         List<String> sortedFields = Arrays.asList(fieldNames);
         Collections.sort(sortedFields);
         for (String f : sortedFields) {
             f = f.replaceAll("_", "\\.");
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
             r = r.replaceAll("_", "\\.");
             Object val = values.get(r);
             // Gson doesn't help here, for some reason it ignores the data format setting in lists...
             if (val instanceof Date) {
                 val = df.format((Date) val);
             }
             result.put(toKOBindingKey(r, formId), val == null ? "" : val);
         }
 
         // render the json string using our custom gson serializer
         renderText(gson.toJson(result));
     }
 
     private static String toKOBindingKey(String r, String formId) {
         return "value_" + formId + "_" + r.replaceAll("\\.", "_");
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
 
 
     /////////////////////////
     // Access rights stuff //
     ////////////////////////
 
     @Util
     protected static boolean canView() {
         return TMDeadboltHandler.getUserRoles(getActiveProject()).getRoles().contains(UnitRole.getViewRole(request.controllerClass));
     }
 
     @Util
     protected static boolean canCreate() {
         return TMDeadboltHandler.getUserRoles(getActiveProject()).getRoles().contains(UnitRole.getCreateRole(request.controllerClass));
     }
 
 
     private final static ImmutableMap<String, Class<? extends ProjectModel>> controllerToEntityMapping = ImmutableMap.of(
             Requirements.class.getName(), Requirement.class,
             Repository.class.getName(), Script.class,
             Preparation.class.getName(), Instance.class,
             Execution.class.getName(), Run.class,
             Defects.class.getName(), Defect.class
     );
 
 
 }
