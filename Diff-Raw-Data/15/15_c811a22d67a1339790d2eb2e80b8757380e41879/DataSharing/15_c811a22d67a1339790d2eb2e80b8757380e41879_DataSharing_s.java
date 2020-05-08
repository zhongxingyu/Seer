 
 /**
  * 
  * 
  */
 package org.iplantc.core.uidiskresource.client.sharing.models;
 
 import org.iplantc.core.uicommons.client.collaborators.models.Collaborator;
 import org.iplantc.core.uicommons.client.models.sharing.Sharing;
 import org.iplantc.core.uidiskresource.client.models.DiskResourceAutoBeanFactory;
 import org.iplantc.core.uidiskresource.client.models.Permissions;
 import org.iplantc.core.uidiskresource.client.util.DiskResourceUtil;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONBoolean;
 import com.google.gwt.json.client.JSONObject;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 
 /**
  * 
  * @author sriram
  * 
  */
 public class DataSharing extends Sharing {
 
     public static final String READ = "read";
     public static final String WRITE = "write";
     public static final String OWN = "own";
     
     private boolean readable;
     private boolean writable;
     private boolean owner;
     private String path;
     private String displayPermission;
 
     public static enum TYPE {
         FILE, FOLDER
     };
 
     public DataSharing(Collaborator c, Permissions p, String path) {
         super(c);
         setPath(path);
         if (p != null) {
             setReadable(p.isReadable());
             setWritable(p.isWritable());
             setOwner(p.isOwner());
             if (isOwner()) {
                 setDisplayPermission(OWN);
             } else if (isWritable()) {
                 setDisplayPermission(WRITE);
             } else {
                 setDisplayPermission(READ);
             }
         }
 
     }
 
     public void setPath(String path) {
         this.path = path;
     }
 
     public String getResourceName() {
         return DiskResourceUtil.parseNameFromPath(path);
     }
 
     public void setReadable(boolean read) {
         readable = read;
         if (read) {
            setWritable(false);
            setOwner(false);
             setDisplayPermission(READ);
         }
     }
 
     public void setWritable(boolean write) {
         writable = write;
         if (write) {
            setReadable(true);
             setDisplayPermission(WRITE);
         }
        setOwner(false);
     }
 
     public void setOwner(boolean own) {
         owner = own;
         if (own) {
            setReadable(true);
            setWritable(true);
             setDisplayPermission(OWN);
         }
     }
 
     public boolean isReadable() {
         return readable;
     }
 
     public boolean isWritable() {
         return writable;
     }
 
     public boolean isOwner() {
         return owner;
     }
 
     public String getPath() {
         return path;
     }
 
     @Override
     public String getKey() {
         return super.getKey() + getPath();
     }
 
     public void setDisplayPermission(String perm) {
         displayPermission = perm;
     }
 
     public String getDisplayPermission() {
         return displayPermission;
     }
 
     @Override
     public boolean equals(Object o) {
         if (o == null || !(o instanceof DataSharing)) {
             return false;
         }
         DataSharing s = (DataSharing)o;
         return getKey().equals(s.getKey()) && s.getDisplayPermission().equals(getDisplayPermission());
     }
 
     @Override
     public DataSharing copy() {
         Collaborator c = getCollaborator();
         JSONObject obj = new JSONObject();
         obj.put(READ, JSONBoolean.getInstance(isReadable()));
         obj.put(WRITE, JSONBoolean.getInstance(isWritable()));
         obj.put(OWN, JSONBoolean.getInstance(isOwner()));
         String path = getPath();
         DiskResourceAutoBeanFactory factory = GWT.create(DiskResourceAutoBeanFactory.class);
         AutoBean<Permissions> bean = AutoBeanCodex.decode(factory, Permissions.class, obj.toString());
         return new DataSharing(c, bean.as(), path);
     }
 
 }
