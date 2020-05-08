 /*
  * FolderManager.java
  *
  * Created on March 29, 2009, 11:04 AM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.rameses.osiris2;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Map;
 
 /**
  *
  * @author elmo
  */
 public class FolderManager implements Serializable {
     
     //these are package level
     private Map folders = new Hashtable();
     
     private AppContext appContext;
     
     FolderManager(AppContext app) {
         this.appContext = app;
     }
     
     public Map getFolders() {
         return folders;
     }
     
     //when adding folders, check first if it already exists in the folders tree.
     //if the folder already exists use that instead.
     public Folder addFolder( Folder folder, Folder parent ) {
         String fullId = folder.getId();
         if(fullId==null) 
             throw new IllegalStateException("Error adding folder. Folder must have an Id!");
         if( !fullId.startsWith("/")) fullId = "/" + fullId;
         
         if( parent !=null) {
             fullId = parent.getFullId() + "/" + folder.getId();
         }
         if(! folders.containsKey(fullId) ) {
             folder.setFullId(fullId);
             folders.put(folder.getFullId(), folder);
             if(parent!=null) {
                 parent.getFolders().add( folder );
                 folder.setParent(parent);
                 Collections.sort(parent.getFolders());
             }
             return folder;
         } else {
            return (Folder) folders.get(folder.getFullId());
         }
     }
 
     public AppContext getAppContext() {
         return appContext;
     }
     
     
 }
