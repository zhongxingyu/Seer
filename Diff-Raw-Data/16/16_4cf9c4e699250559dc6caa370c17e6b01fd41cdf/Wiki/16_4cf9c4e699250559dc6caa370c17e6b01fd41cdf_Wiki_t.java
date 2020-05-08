 /**
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Wiki.
  *
  * The Initial Developer of the Original Code is Technology Concepts
  * and Design, Inc.
  * Copyright (C) 2000 Technology Concepts and Design, Inc.  All
  * Rights Reserved.
  *
  * Contributor(s): Lane Sharman (OpenDoors Software)
  *                 Justin Wells (Semiotek Inc.)
  *                 Eric B. Ridge (Technology Concepts and Design, Inc.)
  *
  * Alternatively, the contents of this file may be used under the
  * terms of the GNU General Public License Version 2 or later (the
  * "GPL"), in which case the provisions of the GPL are applicable
  * instead of those above.  If you wish to allow use of your
  * version of this file only under the terms of the GPL and not to
  * allow others to use your version of this file under the MPL,
  * indicate your decision by deleting the provisions above and
  * replace them with the notice and other provisions required by
  * the GPL.  If you do not delete the provisions above, a recipient
  * may use your version of this file under either the MPL or the
  * GPL.
  *
  *
  * This product includes sofware developed by OpenDoors Software.
  *
  * This product includes software developed by Justin Wells and Semiotek Inc.
  * for use in the WebMacro ServletFramework (http://www.webmacro.org).
  */
 package org.tcdi.opensource.wiki;
 
 import java.util.*;
 import java.io.*;
 
 import org.opendoors.instant.vlh.*;
 import org.opendoors.intf.QueueListener;
 
 import org.tcdi.opensource.wiki.parser.*;
 import org.tcdi.opensource.wiki.renderer.*;
 import org.tcdi.opensource.wiki.builder.*;
 import org.tcdi.opensource.wiki.search.*;
 import org.tcdi.opensource.util.*;
 
 /**
  *
  * The main WikiSystem Implementation.  This implementation
  * is "Powered by VLH".
  *
  * @see org.opendoors.instant.vlh.VLHProxy
  *
  * @author Eric B. Ridge
  */
 final public class Wiki implements WikiSystem, QueueListener {
     public static final String PAGE_properties   = "PageStore";
     public static final String USER_properties   = "UserStore";
     public static final String DEFAULT_PROPERTIES = "Wiki.properties";
     
     private String _version = null;
     private Properties _properties;
     private String _propsFilename;
     private Date _dateStarted;
     
     private Hashtable _pageStore;
     private Hashtable _userStore;
     
     /** array of all valid page names */
     private volatile String[] _pageNames;
     private volatile Map _pageLookup;
     
     /** the page renderer this Wiki is configured to use */
     private WikiPageRenderer _pageRenderer;
     
     /** the page builder class we should use */
     private Class _pageBuilderClass;
     
     /**
      * This constructor will load the Wiki.Properties file
      * from the classpath
      */
     public Wiki() throws Exception {
         this (DEFAULT_PROPERTIES);
     }
     
     /**
      * This constructor will load the specified .properties file
      *
      * @param propertiesFilename path to a .properties file for the Wiki
      */
     public Wiki(String propertiesFilename) throws Exception {
         if (propertiesFilename == null)
             propertiesFilename = DEFAULT_PROPERTIES;
         
         create(propertiesFilename);
         _dateStarted = new Date();
     }
     
     public WikiPage getPage(String title) {
         return (WikiPage) _pageStore.get(title);
     }
     
     public Enumeration getPageNames() {
         return _pageStore.keys();
     }
     
     public String[] getCurrentPageNames() {
         return _pageNames;
     }
     
     public void savePage(WikiPage page) { savePage(page, page.getTitle()); }
     
     public void savePage(WikiPage page, String title) {
         WikiPage oldVersion = (WikiPage) _pageStore.get(title);
        boolean isnew = false;
         if (oldVersion != null) {
             // backup old version of this page
             oldVersion.setTitle(oldVersion.getTitle() + "." + oldVersion.getVersion());
             _pageStore.put(oldVersion.getTitle() + "." + oldVersion.getVersion(), page);
         } else {
            // this is a brand new page
 	    isnew = true;
        }	    
         
         // save this page
         _pageStore.put(title, page);

        if (isnew)  
           populatePageNames ();
         
         try {
             indexPage(page);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     public void indexCurrentPages () throws Exception {
         for (int x=0; x<_pageNames.length; x++) {
             System.err.println ("Indexing " + _pageNames[x]);
             indexPage (getPage (_pageNames[x]));
         }
     }
     
     private void indexPage(WikiPage page) throws Exception {
         if (page == null)
             return;
         
         WikiPageIndexer indexer = new LuceneIndexer();
         indexer.index(this, page);
     }
     
     public void deletePage(String title) {
         // backup page we're about to remove
         WikiPage oldPage = getPage(title);
         if (oldPage != null) {
             oldPage.setTitle(".DELETED-" + oldPage.getTitle());
             _pageStore.put(".DELETED-" + oldPage.getTitle(), oldPage);
         }
         
         // remove the page
         _pageStore.remove(title);
         populatePageNames();
     }
     public void renamePage(String oldName, String newName) {
         // TODO: implement this
     }
     public boolean containsPage(String title) {
         return _pageLookup.containsKey (title);
     }
     
     public WikiUser getUser(String uid) {
         return (WikiUser) _userStore.get(uid);
     }
     public Enumeration getUserNames() {
         return _userStore.keys();
     }
     public void deleteUser(String uid) {
         _userStore.remove(uid);
     }
     public WikiUser registerUser(String uid, String fullname, String password, Hashtable attributes) throws Exception {
         if (getUser(uid) != null)
             throw new Exception("Username already exists");
         
         WikiUser newUser = new WikiUser(fullname, password);
         newUser.setIdentifier(uid);  // TODO: need another VLH store for a UID<-->username mapping
         
         if (attributes != null) {
             Enumeration enum = attributes.keys();
             while (enum.hasMoreElements()) {
                 String key = enum.nextElement().toString();
                 newUser.setAttribute(key, attributes.get(key).toString());
             }
         }
         
         _userStore.put(uid, newUser);
         
         return newUser;
     }
     public void updateUser(WikiUser user) {
         _userStore.put(user.getIdentifier(), user);
     }
     
     public boolean isAdministrator(WikiUser user) {
         if (user == null)
             return false;
         
         return _properties.getProperty("Administrators").indexOf(user.getIdentifier() + ";") > -1;
     }
     
     public Properties getProperties() { return _properties; }
     
     public void reloadProperties() throws Exception {
         InputStream in;
         _properties = new Properties();
         
         if (_propsFilename.startsWith ("/")) {
             in = new FileInputStream(_propsFilename);
         } else {
            in = this.getClass().getClassLoader().getResourceAsStream(_propsFilename);
         }
         
         _properties.load(in);
 
         try {
             in.close();
         } catch (Exception e) {
             // throw it away.
         }
     }
     
     public String getVersion()   { return _version; }
     public Date getDateStarted() { return _dateStarted; }
     
     /**
      * The exception handler for VLH
      */
     public void readQueue(Object exception) {
         ((Exception) exception).printStackTrace();
     }
     
     
     
     //
     // private methods
     //
     /**
      * does necessary up-front configurations for the Wiki system.
      * @param propertiesFilename the location of the properties file
      *                           to use.  If null, load the DEFAULT_PROPERTIES file from the
      *                           system classpath
      */
     private void create(String propertiesFilename) throws Exception {
         _propsFilename = propertiesFilename;
         
         reloadProperties();
         
         _version = _properties.getProperty("Version");
         
         try { _pageStore = createStore(PAGE_properties); }
         catch (Exception e) { System.err.println ("Error creating " + PAGE_properties); e.printStackTrace (); }
         
         try { _userStore = createStore(USER_properties); }
         catch (Exception e) { System.err.println ("Error creating " + USER_properties); e.printStackTrace ();}
         
         try { _pageBuilderClass = Class.forName(_properties.getProperty("PageBuilder")); }
         catch (Exception e) { System.err.println ("Cannot load page builder class"); e.printStackTrace ();}
         
         try {
             String urlrendererClassname  = _properties.getProperty("URLRenderer");
             String pagerendererClassname = _properties.getProperty("PageRenderer");
             
             // the page renderer
             _pageRenderer = (WikiPageRenderer) Class.forName(pagerendererClassname).newInstance();
             _pageRenderer.init(this);
         } catch (Exception e) {
             System.err.println ("Cannot create page renderer");
             e.printStackTrace ();
         }
         
         populatePageNames();
     }
     
     /**
      * populate our internal array of non-deleted, current version
      * page names
      */
     private void populatePageNames() {
         Map pageLookup = new HashMap ();
         Enumeration enum = getPageNames();
         List l = new ArrayList();
         while (enum.hasMoreElements()) {
             String pageName = (String) enum.nextElement();
             if (pageName.indexOf('.') == -1) {
                 l.add(pageName);   // only add current pages (ie, not deleted or old versions)
                 pageLookup.put (pageName, pageName);
             }
         }
         
         _pageNames = (String[]) l.toArray(new String[0]);
         _pageLookup = pageLookup;
     }
     
     
     /**
      * Creates a store of the specified name, getting properties
      * from our properties file
      */
     private Hashtable createStore(String name) throws Exception {
         Properties props = new Properties();
         props.put("ProxyImplementation",  (String) _properties.get(name + ".ProxyImplementation"));
         props.put("PartitionKey",         (String) _properties.get(name + ".PartitionKey"));
         props.put("ImmutableCertificate", (String) _properties.get(name + ".ImmutableCertificate"));
         props.put("MutableCertificate",   (String) _properties.get(name + ".MutableCertificate"));
         props.put("Creator",              (String) _properties.get(name + ".Creator"));
         props.put("Server",               (String) _properties.get(name + ".Server"));
         props.put("Port",                 (String) _properties.get(name + ".Port"));
         
         VLHProvider provider = VLHProvider.getInstance();
         try {
             provider.createPartition(props);
         }
         catch (Exception e) {
             if (e.toString() != null && e.toString().indexOf("Partition exists") == -1)
                 e.printStackTrace();
         }
         
         return provider.getAgentAsHashtable(props, this);
     }
     
     /**
      * test harness
      *
      * Usage:
      *    args[0] - WikiPage name to display title of
      *    args[1] - properties file  (optional)
      *          if ommited, will load Wiki.properties from classpath
      */
     public static void main(String[] args) throws Exception {
         Wiki wiki;
         
         if (args.length > 1)
             wiki = new Wiki(args[1]); // load properties from args[1]
         else
             wiki = new Wiki();  // load properties from classpath
         
         System.out.println(wiki.getPage(args[0] != null ? args[0] : (String) wiki._properties.get("DefaultPage")));
     }
     
     /**
      * Create a new WikiPage object (but not add it to the page store).  Caller
      * is responsible for setting the list of related pages
      */
     public WikiPage createPage(String title, String author, String data) throws Exception {
         WikiPageBuilder builder = (WikiPageBuilder) this._pageBuilderClass.newInstance();
         builder.init(this);
         WikiParser parser = new WikiParser(new ByteArrayInputStream(data.getBytes()));
         
         WikiPage page = parser.parse(builder);
         page.setTitle(title);
         page.setAuthor(author);
         page.setUnparsedData(data);
         return page;
     }
     
     /**
      * parse page
      */
     public void parsePage(WikiPage page) throws Exception {
         WikiPage tmp = createPage(page.getTitle(), page.getAuthor(), page.getUnparsedData());
         page.setWikiData(tmp.getData());
     }
     
     /**
      * reparse a page
      */
     private void reparsePage (WikiPage page) throws Exception {
         WikiPageBuilder builder = (WikiPageBuilder) this._pageBuilderClass.newInstance();
         builder.init(this);
         WikiParser parser = new WikiParser (new ByteArrayInputStream (page.getUnparsedData().getBytes()));
         WikiPage tmp = parser.parse (builder);
         
         page.setWikiData (tmp.getData());
         this.savePage (page);
     }
     
     /**
      * Is the specified String a Wiki page reference?
      */
     public boolean isWikiPageReference(String image) {
         char[] chars = image.toCharArray();
         int ucase=0, lcase=0;
         for (int x=0; x<chars.length; x++) {
             char c = chars[x];
             if (c >= 'A' && c <= 'Z')
                 ucase++;
             else if (c >= 'a' && c <= 'z')
                 lcase++;
             else if (c >= '0' && c <= '9')
                 lcase++;
             else
                 return false;
         }
         return ucase>=2 && lcase >=3;
     }
     
     /**
      * What page renderer should be used?
      */
     public WikiPageRenderer getPageRenderer() {
         return _pageRenderer;
     }
     
     /**
      * What is the name of the "Start Page" for this Wiki?
      */
     public String getStartPage() {
         return _properties.getProperty("StartPage").trim();
     }
     
}
