 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 William Chesters
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc At paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 package org.melati.admin;
 
 
 import java.util.Hashtable;
 
 import org.melati.Melati;
 import org.melati.poem.AccessPoemException;
 import org.melati.poem.Field;
 import org.melati.poem.Persistent;
 import org.melati.poem.Table;
 import org.melati.poem.Treeable;
 import org.melati.poem.util.ArrayUtils;
 import org.melati.poem.util.StringUtils;
 import org.melati.template.MarkupLanguage;
 import org.melati.util.HttpServletRequestCompat;
 import org.melati.util.JSStaticTree;
 import org.melati.util.Tree;
 
 
 /**
  * A utility object for placing in a <code>ServletTemplateContext</code>.
  */
 public class AdminUtils {
   
   private String contextPath;
   private String servletURL;
   private String staticURL;
   private String logicalDatabase;
   
   /**
    *  Constructor. 
    */
   public AdminUtils(Melati melati) {
     this(melati.getRequest() == null ? null : HttpServletRequestCompat.getContextPath(melati.getRequest()),
          melati.getRequest() == null ? null : melati.getRequest().getServletPath(),
          melati.getConfig().getStaticURL() ,
          melati.getPoemContext().getLogicalDatabase());    
   }
 
   /**
    *  Constructor. 
    */
   private AdminUtils(String contextPath, String servlet, 
                     String staticURL, String logicalDatabase) {
     this.contextPath = contextPath;
     this.servletURL = contextPath + servlet;
     this.staticURL = staticURL;
     this.logicalDatabase = logicalDatabase;
     // HACK if we are using 2.0 Servlet API then zone is
     // included in servlet and contextPath is empty
     if (contextPath == "") {
       this.contextPath = servlet.substring(0, servlet.lastIndexOf("/"));
     }
   }
   
   /**
    * @return the name of the default table to display  
    */
   public static String getPrimaryDisplayTable(Melati melati) { 
     if (Admin.getPrimaryDisplayTable() == null) 
       Admin.setPrimaryDisplayTable(melati.getDatabase().
           getSettingTable().get(Admin.class.getName() + ".PrimaryDisplayTable"));
     if (Admin.getPrimaryDisplayTable() == null)
       Admin.setPrimaryDisplayTable("columninfo");
     return Admin.getPrimaryDisplayTable();
   }
   
   /**
    * @param melati to get db from
    * @return the stylesheet for screen media  
    */
   public String getScreenStylesheetURL(Melati melati) {
     if (Admin.getScreenStylesheetURL() == null) 
       Admin.setScreenStylesheetURL(melati.getDatabase().
           getSettingTable().get(Admin.class.getName() + ".ScreenStylesheetURL"));
     if (Admin.getScreenStylesheetURL() == null)
       Admin.setScreenStylesheetURL("/admin.css");
     return staticURL + Admin.getScreenStylesheetURL();
   }
   /**
    * @return the settings table setup url
    */
   public String getSetupURL() {
     return servletURL + "/" + logicalDatabase + 
         "/setting/setup";
   }
   
   
   /**
    * Check if setting in db, provide default if not, do not 
    * write default to db. 
    * 
    * @param melati to get db from
    * @return the homepage URL for this databse  
    */
   public String getHomepageURL(Melati melati) {
     if (Admin.getHomepageURL() == null) 
       Admin.setHomepageURL(melati.getDatabase().
           getSettingTable().get(Admin.class.getName() + ".HomepageURL"));
     if (Admin.getHomepageURL() == null)
       Admin.setHomepageURL("http://www.melati.org/");
     return Admin.getHomepageURL();
   }
   
   /**
    * @param melati the melati
    * @param name of template
    * @return name prepended with ldb, table and troid if not null
    */
   public String getURL(Melati melati, String name) { 
     String url = servletURL + "/" + logicalDatabase;
     if (melati.getTable() != null)
       url += "/" + melati.getTable().getName();
     if (melati.getObject() != null)
       url += "/" + melati.getObject().getTroid();
     return url + "/" + name;
   }
   /**
    * @return name prepended with ldb and table name
    */
   public String getURL(Table table, String name) { 
     String url = servletURL + "/" + logicalDatabase;
     url += "/" + table.getName();
     return url + "/" + name;
   }
   
   
   /** @return The Main URL. */
   public String MainURL(String ld) {
     String url = servletURL + "/" + ld;
     return url + "/Main";
   }
   /** @return The Main URL. */
   public String MainURL(Melati melati) {
     return getURL(melati, "Main");
   }
   /** @return The Main URL after deletion of a tableinfo */
   public String MainURL(Table table) {
     return getURL(table, "Main");
   }
   /** @return The Main URL after creatioin of a tableinfo */
   public String MainURL(Table table,Persistent p) {
     String url = servletURL + "/" + logicalDatabase;
     url += "/" + table.getName();
     url += "/" + p.troid();
     return url + "/" + "Main";
  }
   
   /** @return The Top URL. */
   public String TopURL(Melati melati) {
     return getURL(melati, "Top");
   }
   
   /**
    * @return The Bottom URL.
    */
   public String BottomURL(Table table, Melati melati) {
     return  servletURL + "/" + logicalDatabase + 
         "/" + table.getName() +
         (melati.getObject() != null &&  
                 melati.getObject().getTable() == table ? 
                         "/" + melati.getObject().getTroid() 
                         : "") + 
         "/Bottom";
   }
   /**
    * @return The Bottom URL.
    */
   public String BottomURL(Melati melati) {
     String url =  servletURL + "/" + logicalDatabase + "/";
     if (melati.getTable() != null)
       url += melati.getTable().getName();
     else 
       url += getPrimaryDisplayTable(melati); 
     if (melati.getObject() != null)
       url += "/" + melati.getObject().getTroid();
     url += "/Bottom";
     return url;
   }
   /**
    * @return The Left URL.
    */
   public String TableURL(Melati melati) {
     return getURL(melati, "Table");
   }
   
   /**
    * @return The Right URL.
    */
   public String RecordURL(Persistent object) throws AccessPoemException {
     return servletURL + "/" + logicalDatabase + "/" + object.getTable().getName()
             + "/" + object.troid() + "/Record";
   }
 
   /**
    * @return The Right URL.
    */
   public String RecordURL(Persistent object, String returnTarget, String returnURL) throws AccessPoemException {
     return servletURL + "/" + logicalDatabase + "/" + object.getTable().getName()
             + "/" + object.troid() + "/Record" + 
             "?returnTarget=" + returnTarget + 
             "&returnURL=" + returnURL;
   }
 
   /**
    * @return The Right URL.
    */
   public String RecordURL(Melati melati) throws AccessPoemException {
     return getURL(melati, "Record");
   }
 
   /**
    * @return The Primary Select URL.
    */
   public String PrimarySelectURL(Melati melati) {
     return getURL(melati, "PrimarySelect");
   }
 
   /**
    * @return The Selection URL.
    */
   public String SelectionURL(Table table) {
     return SelectionURL(table,"admin_record");
   }
   /**
    * @return The Selection URL.
    */
   public String SelectionURL(Table table, String returnTarget) {
     return SelectionURL(table, "admin_record", returnTarget);
   }
   /**
    * @param table
    * @param target
    * @param returnTarget
    * @return the url
    */
   public String SelectionURL(Table table, String target, String returnTarget) {
     return servletURL + "/" + logicalDatabase + "/" + table.getName()
             + "/Selection?" +
             "target=" + target +  
             "&returnTarget=" + returnTarget;
   }
   
   /**
    * Toggle the sort order of column.
    * @return the same url with the toggle field added or removed
    */
   public String ToggledOrderSelectionURL(Melati melati, String field, String value) { 
     String url = melati.sameURLWith(field,value);
     String toggleField = "&" + field + "-toggle=true";
     if (url.endsWith(toggleField))
       return url.substring(0,url.length() - toggleField.length());
     else 
       return url + "&" + field + "-toggle=true";
   }
   
   /**
    * @param melati
    * @return The Selection URL.
    */
   public String SelectionURL(Melati melati) {
     return SelectionURL(melati,"admin_record");    
   }
 
   /**
    * @return The Selection URL.
    */
   public String SelectionURL(Melati melati, String returnTarget) {
     return servletURL + "/" + 
         logicalDatabase + "/" + 
         melati.getTable().getName()
           + "/Selection?" +
           "target=admin_record" + 
           "&returnTarget=" + (returnTarget == null ? "" : returnTarget) + 
           (melati.getObject() == null ? 
               "" : 
               "&field_id=" + melati.getObject().troid());
   }
   
   /**
    * @return The Selection Right URL.
    */
   public String SelectionRightURL(Table table) {
     return servletURL + "/" + logicalDatabase + "/" + table.getName()
     + "/SelectionRight";
   }
 
   /**
    * @return The Navigation URL.
    */
   public String NavigationURL(Table table) {
     return servletURL + "/" + logicalDatabase + "/" + table.getName()
     + "/Navigation";
   }
   
   /**
    * @return The Edit Header URL.
    */
   public String EditHeaderURL(Melati melati) throws AccessPoemException {
     if (melati.getObject() == null)
       return getURL(melati, "blank");
     else
       return getURL(melati, "EditHeader");
   }
 
   /**
    * @return The Edit URL.
    */
   public String EditURL(Melati melati) throws AccessPoemException {
     if (melati.getObject() == null)
       return getURL(melati, "blank");
     else
       return getURL(melati, "Edit");
   }
   /**
    * @return The Edit URL.
    */
   public String EditURL(Persistent object) throws AccessPoemException {
     return servletURL + "/" + logicalDatabase + "/" + object.getTable().getName()
             + "/" + object.troid() + "/Edit";
   }
 
   /**
    * @param melati
    * @return the name of the Record Fields frame
    */
   public String EditFrameName(Melati melati) { 
     String name = "admin_edit";
     name += "_" + melati.getTable().getName();
     if (melati.getObject() != null) 
       name += "_" + melati.getObject().troid();
     return name;
   }
   /**
    * @return The Tree URL.
    */
   public String TreeURL(Persistent object) throws AccessPoemException {
     return servletURL + "/" + logicalDatabase + "/" + object.getTable().getName()
             + "/" + object.troid() + "/Tree";
   }
   
   /**
    * @return The Tree URL.
    */
   public String TreeURL(Table table) throws AccessPoemException {
     return servletURL + "/" + logicalDatabase + "/" + table.getName()
             +  "/Tree";
   }
   
 
   /**
    * @return The Add URL.
    */
   public String AddURL(Table table) throws AccessPoemException {
     return servletURL
             + "/"
             + logicalDatabase
             + "/" 
             + table.getName() 
             + "/" 
             + "Add";
   }
 
   /**
    * @return The Popup URL.
    */
   public String PopUpURL(Table table) {
     return servletURL + "/" + logicalDatabase + "/" + table.getName() + "/PopUp";
   }
   
   /**
    * @return The Selection Window URL.
    */
   public String SelectionWindowURL(Table table) {
     return servletURL + "/" + logicalDatabase + "/" + table.getName()
             + "/SelectionWindow?target=";
   }
 
   /**
    * @return The Selection Window Primary Select URL.
    */
   public String SelectionWindowPrimarySelectURL(Table table) {
     return servletURL + "/" + logicalDatabase + "/" + table.getName()
             + "/SelectionWindowPrimarySelect";
   }
 
   /**
    * @return The Selection Window Selection URL.
    */
   public String SelectionWindowSelectionURL(Table table) {
     return servletURL + "/" + logicalDatabase + "/" + table.getName()
             + "/SelectionWindowSelection";
   }
   
   /**
    * @return The Status URL.
    */
   public String StatusURL() {
     return contextPath + "/org.melati.admin.Status/" + logicalDatabase;
   }
   
   /**
    * @return The Session Analysis URL.
    */
   public String SessionURL() {
     return contextPath + "/org.melati.test.SessionAnalysisServlet";
   }
   
   /**
    * @return The URL for DSD generation. 
    */
   public String DsdURL() {
     return servletURL + "/" + logicalDatabase + "/DSD";
   }
   
   /**
    * In an insert situation we will not have a Troid, so cannot pass it through.
    * If your upload handler depends on having a persistent, then you should
    * override your upload template so that it prevents uploading in an insert
    * situation.
    * 
    * @param table table object belongs to
    * @param object the Persistent we are dealing with
    * @param field the upload field
    * @return Upload Url
    */
   public String UploadURL(Table table, Persistent object, Field field) {
     return upload(table, object) + "/Upload?field=" + field.getName();
   }
   
   /**
    * Upload URL.
    * 
    * @param table table object belongs to
    * @param object the Persistent we are dealing with
    * @param field the upload field
    * @return Upload done URL
    */
   public String UploadHandlerURL(Table table, Persistent object, String field) {
     return upload(table, object) + "/UploadDone?field=" + field;
   }
   private String upload(Table table, Persistent object) {
     String url = servletURL + "/" + logicalDatabase + "/" + table.getName();
     if (object != null)
       url += "/" + object.troid();
     return url;
   }
   
  
   /**
    * Render the specials directly to the output.
    *  
    * FIXME No longer rendering directly
    * 
    * @param melati the Melati
    * @param ml The MarkupLanguage we are using
    * @param object a Persistent to render the specials of 
    * @return an empty String
    * @throws Exception maybe
    */
   public String specialFacilities(Melati melati, MarkupLanguage ml,
           Persistent object) throws Exception {
   if (object instanceof AdminSpecialised)
     melati.getTemplateEngine().expandTemplate(melati.getWriter(),
           ((AdminSpecialised) object).adminSpecialFacilities(melati, ml),
           melati.getTemplateContext());
   return "";
   /*
   if (object instanceof AdminSpecialised)
       return melati.getTemplateEngine().expandedTemplate(
           melati.getTemplateEngine().template(
               ((AdminSpecialised) object).adminSpecialFacilities(melati, ml)),
               melati.getTemplateContext());
     else 
       return "";
     */
   }
 
   /**
    * @return Defaults to /MelatiStatic/admin
    */
   public String getStaticURL() {
     return staticURL;
   }
 
   /**
    *  Create a tree. 
    * @param node  a tree node
    * @return a tree with node as its root
    */
   public JSStaticTree createTree(Treeable node) {
     return new JSStaticTree(new Tree(node), getStaticURL());
   }
   
   /**
   *  Create a forest of trees. 
    * @param table  the table to tree 
    * @return a tree with node as its root
    */
   public JSStaticTree createForest(Table table) {
     Object[] all = ArrayUtils.arrayOf(table.selection());
     Hashtable hasParent = new Hashtable();
     for (int i = 0; i < all.length; i++) {
      if (hasParent.get(all[i]) == null) { 
         Treeable[] kids = ((Treeable)all[i]).getChildren();
         for (int j = 0; j < kids.length; j++)
           hasParent.put(kids[j], Boolean.TRUE);
       }
     }
     int count = 0;
     for (int i = 0; i < all.length; i++) {
      if (hasParent.get(all[i]) == null){ 
         count++;
       }
     }
     Treeable[] roots = new Treeable[count];
     int j = 0;
     for (int i = 0; i < all.length; i++) {
      if (hasParent.get(all[i]) == null) {
        roots[j] = (Treeable)all[i];
         j++;
       }
     }
     return new JSStaticTree(roots, getStaticURL());
   }
 
   /**
    * @param qualifiedName
    * @return text followuing the last dot
    */
   public static String simpleName(String qualifiedName) { 
     return qualifiedName.substring(
         qualifiedName.lastIndexOf('.') != -1 ?
             qualifiedName.lastIndexOf('.') + 1 : 
             0,
         qualifiedName.length());
   }
   
   /**
    * @param in the String to escape
    * @return the escaped String
    */
   public static String csvEscaped(String in) { 
     StringBuffer b = new StringBuffer();
     StringUtils.appendEscaped(b, in, '"', '"');
     return b.toString();
   }
 }
