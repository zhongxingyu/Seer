 package org.icemobile.component;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Nils
  * Date: 4/2/13
  * Time: 4:09 PM
  * To change this template use File | Settings | File Templates.
  */
 public interface IDataView extends IMobiComponent {
    public static final String DATAVIEW_CLASS = "mobi-dv";
     public static final String DATAVIEW_MASTER_CLASS = "mobi-dv-mst";
     public static final String DATAVIEW_DETAIL_CLASS = "mobi-dv-det";
    public static final String DATAVIEW_HEAD_CLASS = "mobi-dv-head";
    public static final String DATAVIEW_FOOT_CLASS = "mobi-dv-foot";
    public static final String DATAVIEW_BODY_CLASS = "mobi-dv-body";
     public static final String DATAVIEW_SORT_INDICATOR_CLASS = "mobi-dv-si";
     public static final String DATAVIEW_BOOL_COLUMN_CLASS = "mobi-dv-c-b";
 
     public String getVar();
     public void setVar(String var);
 
     public Object getValue();
     public void setValue(Object value);
 
     public Integer getActiveRowIndex();
     public void setActiveRowIndex(Integer index);
 }
