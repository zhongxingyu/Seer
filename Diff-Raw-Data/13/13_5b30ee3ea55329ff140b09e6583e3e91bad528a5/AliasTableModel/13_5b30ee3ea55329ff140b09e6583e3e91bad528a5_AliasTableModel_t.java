 package iotc.gui;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 import javax.swing.JOptionPane;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 
 /**
  * Userのalias用テーブルモデル
  * @author atsushi-o
  */
 public class AliasTableModel extends DefaultTableModel {
     private final String[] columnName = {"Medium", "Alias"};
     private final Map<String, String> list;
     private boolean editable = false;
 
     public AliasTableModel() {
         super(0, 2);
         list = new LinkedHashMap();
         addTableModelListener(new TableModelListener(){
             @Override
             public void tableChanged(TableModelEvent tme) {
                 if (tme.getType() != TableModelEvent.UPDATE) return;
                 String key = (String)getValueAt(tme.getFirstRow(), 0);
                 String val = (String)getValueAt(tme.getFirstRow(), 1);
                 for (int i = 0; i < getRowCount(); i++) {
                     if (i != tme.getFirstRow() && getValueAt(i, 0).equals(key)) {
                         JOptionPane.showMessageDialog(null, "This medium is already added", "Error", JOptionPane.ERROR_MESSAGE);
                         setValueAt("", tme.getFirstRow(), 0);
                         return;
                     }
                 }
                 list.put(key, val);
             }
         });
     }
 
     /**
      * セルの編集可否を設定
      * @param editable
      */
     public void setEditable(boolean editable) {
         this.editable = editable;
     }
 
     /**
      * Aliasを追加する
      * @param medium
      * @param alias
      */
     public void addAlias(String medium, String alias) {
         addRow(new Object[]{medium, alias});
         list.put(medium, alias);
     }
     /**
      * MapでAliasを追加する
      * @param alias
      */
     public void addAliasMap(Map<String, String> alias) {
         for (Map.Entry<String, String> e : alias.entrySet()) {
             addAlias(e.getKey(), e.getValue());
         }
     }
     /**
      * 指定された行のAliasを削除する
      * @param row
      */
     public void removeAlias(int row) {
         String key = (String)getValueAt(row, 0);
         removeRow(row);
         list.remove(key);
     }
     /**
      * AliasのMapを取得する
      * @return
      */
     public Map<String, String> getAliasMap() {
         return list;
     }
     /**
      * Aliasの文字列表現を取得する
      * @return
      */
     public String getAliasAsString() {
         StringBuilder sb = new StringBuilder();
         for (Map.Entry<String, String> e : list.entrySet()) {
             sb.append(e.getKey()).append(":").append(e.getValue());
             sb.append(",");
         }
        return sb.length()!=0?sb.substring(0, sb.length()-1):"";
     }
     /**
      * 全てのAliasを削除する
      */
     public void clearAllAlias() {
         for (int i = getRowCount() - 1; i >= 0; i--) {
             removeRow(i);
         }
         list.clear();
     }
 
     @Override
     public String getColumnName(int c) {
         return columnName[c];
     }
     @Override
     public boolean isCellEditable(int row, int column) {
         return this.editable;
     }
     @Override
     public Class getColumnClass(int column) {
         switch (column) {
             case 0:
                 return String.class;
             case 1:
                 return String.class;
             default:
                 return String.class;
         }
     }
 }
