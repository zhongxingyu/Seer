 package com.deluan.jenkins.plugins.rtc.changelog;
 
 import hudson.model.User;
 import hudson.scm.ChangeLogSet;
 import hudson.scm.EditType;
 import org.kohsuke.stapler.export.Exported;
 import org.kohsuke.stapler.export.ExportedBean;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 /**
  * {@link hudson.scm.ChangeLogSet} for IBM Rational Team Concert Source Code Management
  *
  * @author Deluan Cotts Quintao
  */
 public final class JazzChangeSet extends ChangeLogSet.Entry implements Comparable<JazzChangeSet> {
     private static final String DATE_FORMAT = "yyyy-MM-dd-HH:mm:ss";
     private final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
 
     private String user;
     private String email;
     private Date date;
     private String rev;
     private String msg;
 
     private List<Item> items = new ArrayList<Item>();
 
     private List<String> workItems = new ArrayList<String>();
 
     @Exported
     public String getMsg() {
         return msg;
     }
 
     @Exported
     public User getAuthor() {
         return User.get(user + " <" + email + ">");
     }
 
     @Exported
     public String getUser() {
         return user;
     }
 
     @Exported
     public String getEmail() {
         return email;
     }
 
     @Exported
     public Date getDate() {
         return date;
     }
 
     @Exported
     public String getDateStr() {
         return sdf.format(date);
     }
 
     @Exported
     public String getRev() {
         return rev;
     }
 
     @Override
     public Collection<String> getAffectedPaths() {
         Collection<String> paths = new ArrayList<String>(items.size());
         for (Item item : items) {
             paths.add(item.getPath());
         }
         return paths;
     }
 
     public void addWorkItem(String workItem) {
         workItems.add(workItem.trim());
     }
 
     public void addItem(Item item) {
         items.add(item);
         item.setParent(this);
     }
 
     public void addItem(String path, String action) {
         items.add(new Item(path, action));
     }
 
     @Exported
     public List<Item> getItems() {
         return items;
     }
 
     /**
      * Gets all work items associated with this change set
      */
     @Exported
     public List<String> getWorkItems() {
         return workItems;
     }
 
     @Override
     protected void setParent(ChangeLogSet parent) {
         super.setParent(parent);
     }
 
     public void setUser(String user) {
         this.user = user;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public void setDateStr(String dateStr) throws ParseException {
         date = sdf.parse(dateStr);
     }
 
     public void setRev(String rev) {
         this.rev = rev;
     }
 
     public void setMsg(String msg) {
         this.msg = msg;
     }
 
     public int compareTo(JazzChangeSet o) {
         return this.date.compareTo(o.date);
     }
 
     public void copyItemsFrom(JazzChangeSet changeSet2) {
         this.items = new ArrayList<Item>(changeSet2.getItems());
         this.workItems = new ArrayList<String>(changeSet2.getWorkItems());
     }
 
     @ExportedBean(defaultVisibility = 999)
     public static class Item {
         private String path;
         private String action;
         private JazzChangeSet parent;
 
         public Item() {
             this("", "");
         }
 
         public Item(String path, String action) {
             this.path = path;
             this.action = action;
         }
 
         public JazzChangeSet getParent() {
             return parent;
         }
 
         void setParent(JazzChangeSet parent) {
             this.parent = parent;
         }
 
         @Exported
         public String getPath() {
             return path;
         }
 
         public void setPath(String path) {
             this.path = path;
         }
 
         @Exported
         public String getAction() {
             return action;
         }
 
         public void setAction(String action) {
             this.action = action;
         }
 
         @Exported
         public EditType getEditType() {
            if (action.equalsIgnoreCase(EditType.DELETE.getName())) {
                 return EditType.DELETE;
             }
            if (action.equalsIgnoreCase(EditType.ADD.getName())) {
                 return EditType.ADD;
             }
             return EditType.EDIT;
         }
     }
 }
