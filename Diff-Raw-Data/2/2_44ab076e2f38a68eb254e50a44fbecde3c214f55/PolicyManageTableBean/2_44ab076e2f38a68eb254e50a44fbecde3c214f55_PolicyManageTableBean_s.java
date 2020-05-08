 package com.sun.identity.admin.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class PolicyManageTableBean implements Serializable {
 
     public int getCellWidth() {
         return cellWidth;
     }
 
     public List<String> getColumnsVisible() {
         return columnsVisible;
     }
 
     public void setColumnsVisible(List<String> columnsVisible) {
         this.columnsVisible = columnsVisible;
     }
 
     public void setPrivilegeBeans(List<PrivilegeBean> privilegeBeans) {
         this.privilegeBeans = privilegeBeans;
     }
 
     public static class SortKey implements Serializable {
         private boolean ascending = false;
         private String column = "name";
 
         public SortKey() {
             // nothing
         }
 
         public SortKey(String column, boolean ascending) {
             this.column = column;
             this.ascending = ascending;
         }
 
         public boolean isAscending() {
             return ascending;
         }
 
         public void setAscending(boolean ascending) {
             this.ascending = ascending;
         }
 
         public String getColumn() {
             return column;
         }
 
         public void setColumn(String column) {
             this.column = column;
         }
 
         @Override
         public int hashCode() {
             return toString().hashCode();
         }
 
         @Override
         public boolean equals(Object o) {
             if (!(o instanceof SortKey)) {
                 return false;
             }
 
             SortKey other = (SortKey)o;
             return other.toString().endsWith(toString());
         }
 
         @Override
         public String toString() {
             return column+":"+ascending;
         }
     }
 
     private SortKey sortKey = new SortKey();
     private List<PrivilegeBean> privilegeBeans;
     private static Map<SortKey,Comparator> comparators = new HashMap<SortKey,Comparator>();
     private int cellWidth = 20;
     private List<String> columnsVisible = new ArrayList<String>();
 
     static {
         comparators.put(new SortKey("name", true), new PrivilegeBean.NameComparator(true));
         comparators.put(new SortKey("name", false), new PrivilegeBean.NameComparator(false));
        comparators.put(new SortKey("description", true), new PrivilegeBean.NameComparator(true));
        comparators.put(new SortKey("description", false), new PrivilegeBean.NameComparator(false));
         comparators.put(new SortKey("description", true), new PrivilegeBean.DescriptionComparator(true));
         comparators.put(new SortKey("description", false), new PrivilegeBean.DescriptionComparator(false));
         comparators.put(new SortKey("birth", true), new PrivilegeBean.BirthComparator(true));
         comparators.put(new SortKey("birth", false), new PrivilegeBean.BirthComparator(false));
         comparators.put(new SortKey("author", true), new PrivilegeBean.AuthorComparator(true));
         comparators.put(new SortKey("author", false), new PrivilegeBean.AuthorComparator(false));
         comparators.put(new SortKey("modified", true), new PrivilegeBean.ModifiedComparator(true));
         comparators.put(new SortKey("modified", false), new PrivilegeBean.ModifiedComparator(false));
         comparators.put(new SortKey("modifier", true), new PrivilegeBean.ModifierComparator(true));
         comparators.put(new SortKey("modifier", false), new PrivilegeBean.ModifierComparator(false));
     }
 
     public PolicyManageTableBean() {
         columnsVisible.add("description");
         columnsVisible.add("resources");
         columnsVisible.add("subject");
         columnsVisible.add("modified");
         columnsVisible.add("remove");
     }
 
     public SortKey getSortKey() {
         return sortKey;
     }
 
     public void setSortKey(SortKey sortKey) {
         this.sortKey = sortKey;
     }
 
     public void sort() {
         Comparator c = comparators.get(sortKey);
         Collections.sort(privilegeBeans, c);
     }
 
     public boolean isResourcesColumnVisible() {
         return getColumnsVisible().contains("resources");
     }
 
     public boolean isDescriptionColumnVisible() {
         return getColumnsVisible().contains("description");
     }
 
     public boolean isExceptionsColumnVisible() {
         return getColumnsVisible().contains("exceptions");
     }
 
     public boolean isSubjectColumnVisible() {
         return getColumnsVisible().contains("subject");
     }
 
     public boolean isConditionColumnVisible() {
         return getColumnsVisible().contains("condition");
     }
 
     public boolean isActionColumnVisible() {
         return getColumnsVisible().contains("action");
     }
 
     public boolean isBirthColumnVisible() {
         return getColumnsVisible().contains("birth");
     }
 
     public boolean isModifiedColumnVisible() {
         return getColumnsVisible().contains("modified");
     }
 
     public boolean isAuthorColumnVisible() {
         return getColumnsVisible().contains("author");
     }
 
     public boolean isModifierColumnVisible() {
         return getColumnsVisible().contains("modifier");
     }
 
     public boolean isRemoveColumnVisible() {
         return getColumnsVisible().contains("remove");
     }
     public boolean isExportColumnVisible() {
         return getColumnsVisible().contains("export");
     }
 }
