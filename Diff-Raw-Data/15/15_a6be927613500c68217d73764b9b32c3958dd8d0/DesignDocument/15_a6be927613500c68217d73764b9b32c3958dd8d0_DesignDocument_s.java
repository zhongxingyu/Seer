 package org.jcouchdb.document;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jcouchdb.util.Util;
 import org.svenson.JSONProperty;
 import org.svenson.JSONTypeHint;
 
 public class DesignDocument
     extends BaseDocument
 {
     /**
      *
      */
     private static final long serialVersionUID = 2315187506718291465L;
 
     public final static String PREFIX = "_design/";
 
     private String language = "javascript";
 
     private Map<String, View> views = new HashMap<String, View>();
     
     private Map<String, String> shows;
 
     private Map<String, String> lists;
     
     private String validateOnDocUpdate;
 
     public DesignDocument(String id, String revision)
     {
         setId(id);
         setRevision(revision);
     }
 
     public DesignDocument()
     {
         this(null, null);
     }
 
     public DesignDocument(String id)
     {
         this(id, null);
     }
 
     /**
      * Sets the id for the design document ( the "_design/" prefix which will be
      * added automatically )
      */
     @Override
     public void setId(String id)
     {
         super.setId(extendId(id));
     }
     
     @Override
     public String getId()
     {
         return super.getId();//.replace("%2F", "/");
     }
 
     public String getLanguage()
     {
         return language;
     }
 
     public void setLanguage(String language)
     {
         this.language = language;
     }
 
     @JSONProperty(value = "validate_doc_update", ignoreIfNull = true)
     public String getValidateOnDocUpdate()
     {
         return validateOnDocUpdate;
     }
 
     public void setValidateOnDocUpdate(String validateOnDocUpdate)
     {
         this.validateOnDocUpdate = validateOnDocUpdate;
     }
 
     @JSONTypeHint(View.class)
     public Map<String, View> getViews()
     {
         return views;
     }
 
     public View getView(String name)
     {
         return views.get(name);
     }
 
     /**
      * Sets all views of the given design document.
      *
      * @param views
      */
     public void setViews(Map<String, View> views)
     {
         this.views = views;
     }
 
     /**
      * Adds a view to this design document.
      *
      * @param name      name of the view
      * @param view      view
      */
     public void addView(String name, View view)
     {
         views.put(name, view);
     }
 
     /**
      * Ensures that the id has the design document prefix and returns the id
      * @param id    id
      * @return  id with design document prefix
      */
     public static String extendId(String id)
     {
         if (id != null)
         {
 //            if (id.startsWith(PREFIX_UNESCAPED))
 //            {
 //                id = PREFIX + id.substring(PREFIX_UNESCAPED.length());
 //            }
 //            else
             if (!id.startsWith(PREFIX))
             {
                 id = PREFIX + id;
             }
         }
         return id;
     }
 
     /**
      * Equality based on id, language and view comparison <em>without</em> revision comparison.
      * This method basically checks if the other design document has exactly the same id, views
      * and language.
      *
      * @param that
      * @return
      */
     public boolean equalsIncludingContent(DesignDocument that)
     {
         return Util.equals(this.getId(), that.getId()) &&
                Util.equals(this.getLanguage(), that.getLanguage()) &&
                Util.equals(this.getViews(), that.getViews());
 
     }
 
     @Override
     public String toString()
     {
         return super.toString()+": views = "+views;
     }
     
     @JSONProperty(value = "shows", ignoreIfNull = true)
     public void setShowFunctions(Map<String, String> shows)
     {
         this.shows = shows;
     }
     
     public Map<String, String> getShowFunctions()
     {
         return shows;
     }
     
     @JSONProperty(value = "lists", ignoreIfNull = true)
     public void setListFunctions(Map<String, String> lists)
     {
         this.lists = lists;
     }
 
     public Map<String, String> getListFunctions()
     {
         return lists;
     }
 
     /**
      * Adds a show function to the design document
      *
      */
     public void addShowFunction(String name, String showFn)
     {
         if (shows == null)
         {
             shows = new HashMap<String, String>();
         }
         
         shows.put(name, showFn);
     }
 
     /**
      * Adds a show function to the design document
      *
      */
     public void addListFunction(String name, String listFn)
     {
         if (lists == null)
         {
             lists = new HashMap<String, String>();
         }
         
         lists.put(name, listFn);
     }
 }
