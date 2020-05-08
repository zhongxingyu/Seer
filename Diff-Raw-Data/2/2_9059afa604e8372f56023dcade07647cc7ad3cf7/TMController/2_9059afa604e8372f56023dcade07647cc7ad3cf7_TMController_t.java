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
 import models.account.Auth;
 import models.tm.Project;
 import models.tm.User;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Util;
 import play.mvc.With;
 import play.templates.JavaExtensions;
 
 /**
  * Parent controller for the TM application.
  * Sets common view template data such as username, visible menu elements, ...
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @With(Deadbolt.class)
 public class TMController extends Controller {
 
     @Before
     public static void setConnectedUser() {
         if (Security.isConnected()) {
             // FIXME search by account, too!
             Auth a = Auth.find("byEmail", Security.connected()).first();
             renderArgs.put("firstName", a.firstName);
             renderArgs.put("lastName", a.lastName);
 
             if (!request.controller.startsWith("admin")) {
                 renderArgs.put("activeProject", getActiveProject());
             }
 
         }
     }
 
     public static User getConnectedUser() {
         if (Security.isConnected()) {
             // FIXME search by account, too!
             User user = User.find("from User u where u.authentication.email = ?", Security.connected()).first();
             return user;
         } else {
             // TODO test this!
             flash.put("url", "GET".equals(request.method) ? request.url : "/");
             try {
                 Secure.login();
             } catch (Throwable throwable) {
                 // TODO
                 throwable.printStackTrace();
             }
 
         }
         return null;
     }
 
     public static Account getUserAccount() {
         return getConnectedUser().authentication.account;
     }
 
     /**
      * Gets the active project for the connected user, <code>null</code> if none is set
      *
      * @return the active {@see Project}
      */
     public static Project getActiveProject() {
 
         if (request.controller.startsWith("admin")) {
             throw new RuntimeException("Active project can't be fetched in the admin area");
         }
 
         // TODO freaking cache this or we have an extra query each time we create a project-related entity!
         if (session.get("activeProject") != null) {
             Long id = Long.valueOf(session.get("activeProject"));
             if (id != null) {
                 return Project.findById(id);
             }
         }
         return null;
     }
 
     @Util
     public static void checkInAccount(AccountEntity accountEntity) {
         if (!accountEntity.isInAccount(getUserAccount())) {
             unauthorized();
         }
     }
 
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
                     values.put(s, getValue(parentValue, s.substring(parent.length() + 1, s.length())));
                 }
             }
         }
         Map<String, Object> result = new HashMap<String, Object>();
         for (String r : sortedFields) {
             Object val = values.get(r);
             // Gson doesn't help here, for some reason it ignores the data format setting in lists...
             if(val instanceof Date) {
                 val = df.format((Date)val);
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
