 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * ComboDialog.java
  *
  * Created on Nov 30, 2010, 3:51:02 PM
  */
 
 package mysqljavacat.dialogs;
 
 import mysqljavacat.utils.StringComparator;
 import mysqljavacat.databaseobjects.DatabaseObj;
 import mysqljavacat.databaseobjects.FuncObj;
 import mysqljavacat.renders.ComboCompleteRender;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.JEditorPane;
 import javax.swing.JList;
 import mysqljavacat.MysqlJavaCatApp;
 import mysqljavacat.MysqlJavaCatView;
 import mysqljavacat.databaseobjects.CompleteObj;
 import mysqljavacat.databaseobjects.TableObj;
 
 /**
  *
  * @author strelok
  */
 public class ComboDialog extends javax.swing.JDialog {
 
     /** Creates new form ComboDialog */
     private JEditorPane edit;
     private boolean prepared = false;
 
     public boolean getPrepared(){
         return prepared;
     }  
 
     public void setPrepared(boolean p){
         prepared = p;
     }
 
     public ComboDialog(JEditorPane editor) {
         super(MysqlJavaCatApp.getFrameFor(editor), false);
         edit = editor;
         initComponents();
         jList1.setCellRenderer(new ComboCompleteRender());
         this.setVisible(false);
     }
     public JList getComboList(){
         return jList1;
     }
     private void FilterAddList(ArrayList<Object> list,ArrayList<Object> input,String filter){
         FilterAddList(list,input,filter,true);
     }
     private void FilterAddList(ArrayList<Object> list,ArrayList<Object> input,String filter,boolean in_the_end){
         Collections.sort(input,new StringComparator());
         if(filter == null){
             if(in_the_end){
                 for(Object s : input)
                     list.add(s);
             }else{
                 for(int i = input.size() - 1; i >= 0;i = i - 1)
                     list.add(0,input.get(i));
             }
         }else{
             Pattern p = Pattern.compile("^" + Pattern.quote(filter),Pattern.CASE_INSENSITIVE);
             if(in_the_end){
                 for(Object s : input)
                     if(p.matcher(s.toString()).find())
                           list.add(s);
             }else{
                 for(int i = input.size() - 1; i >= 0;i = i-1)
                     if(p.matcher(input.get(i).toString()).find())
                           list.add(0,input.get(i));
             }
         }
     }
     public ArrayList<Object> getQueryAliases(DatabaseObj db){
         ArrayList<Object> out = new ArrayList<Object>();
         Pattern pat = Pattern.compile("\\s(\\w+)\\s+AS\\s+(\\w+)\\s",Pattern.CASE_INSENSITIVE);
         Matcher m = pat.matcher(edit.getText());
         while(m.find()){
             String table = m.group(1);
             if(db.getTable(table) != null){
                 out.add(db.getTable(table).createAlias(m.group(2)));
             }
         }
         return out;
     }
 
     public ArrayList<Object> getQueryTables(DatabaseObj db){
         ArrayList<Object> out = new ArrayList<Object>();
         HashMap<String,Boolean> det = new HashMap<String,Boolean>();
         String [] words = edit.getText().split("\\s+");
         for(String s : words)
             if(db.getTable(s) != null && det.get(s) == null){
                 out.add(db.getTable(s));
                 det.put(s,true);
             }        
         return out;
     }
     public ArrayList<Object> getCoplList(String input){
         ArrayList<Object> out = new ArrayList<Object>();
         MysqlJavaCatView main_frame = MysqlJavaCatApp.getApplication().getView();
         HashMap<String, DatabaseObj> db_map = main_frame.getDbMap();
         DatabaseObj cur_db = main_frame.getSelectedDb();
         String [] parts = input.split("\\.");
         ArrayList<Object> query_tables = getQueryTables(cur_db);
         ArrayList<Object> query_aliases = getQueryAliases(cur_db);
         HashMap<String,TableObj> alias_hash = new HashMap<String,TableObj>();
         for(Object alias : query_aliases){
             query_tables.add(alias);
             alias_hash.put(((TableObj)alias).getName(), (TableObj)alias);
         }
         if(input.length() == 0){            
             FilterAddList(out,new ArrayList<Object>(cur_db.getTables()),null);
             if(!query_tables.isEmpty()){
                 for(Object o : query_tables)
                     out.remove(o);
                 FilterAddList(out,query_tables,null,false);
             }
             if(query_tables.size() == 1){
                 FilterAddList(out,new ArrayList<Object>(((TableObj)query_tables.get(0)).getFields()),null,false);
             }
             FilterAddList(out,new ArrayList<Object>(FuncObj.getFucList()),null);
             FilterAddList(out,new ArrayList<Object>(db_map.values()),null);
         }else if(parts.length == 1 && !input.endsWith(".")){            
             FilterAddList(out,new ArrayList<Object>(cur_db.getTables()),parts[0]);
             if (!query_tables.isEmpty()) {
                 for(Object o : query_tables)
                     out.remove(o);
                 FilterAddList(out,query_tables,parts[0],false);
             }
             if(query_tables.size() == 1){
                 FilterAddList(out,new ArrayList<Object>(((TableObj)query_tables.get(0)).getFields()),parts[0],false);
             }
             FilterAddList(out,new ArrayList<Object>(FuncObj.getFucList()),parts[0]);
             FilterAddList(out,new ArrayList<Object>(db_map.values()),parts[0]);
         }else if(parts.length == 1 && input.endsWith(".")){            
             if(alias_hash.get(parts[0]) != null){
                 FilterAddList(out,new ArrayList<Object>(alias_hash.get(parts[0]).getFields()),null);
             } else if (cur_db.getTable(parts[0]) != null){
                 FilterAddList(out,new ArrayList<Object>(cur_db.getTable(parts[0]).getFields()),null);
             }
             if(db_map.get(parts[0]) != null)
                 FilterAddList(out,new ArrayList<Object>(db_map.get(parts[0]).getTables()),null);
         }else if(parts.length == 2 && !input.endsWith(".")){
             if(alias_hash.get(parts[0]) != null)
                 FilterAddList(out,new ArrayList<Object>(alias_hash.get(parts[0]).getFields()),parts[1]);
             else if(cur_db.getTable(parts[0]) != null)
                 FilterAddList(out,new ArrayList<Object>(cur_db.getTable(parts[0]).getFields()),parts[1]);
             if(db_map.get(parts[0]) != null)
                 FilterAddList(out,new ArrayList<Object>(db_map.get(parts[0]).getTables()),parts[1]);
         }else if(parts.length == 2 && input.endsWith(".")){
             if(db_map.get(parts[0]) != null && db_map.get(parts[0]).getTable(parts[1]) != null)
                 db_map.get(parts[0]).getTable(parts[1]).refereshTable();
                 FilterAddList(out,new ArrayList<Object>(db_map.get(parts[0]).getTable(parts[1]).getFields()),null);
         }else if(parts.length == 3 && !input.endsWith(".")){
             if(db_map.get(parts[0]) != null && db_map.get(parts[0]).getTable(parts[1]) != null)
                 db_map.get(parts[0]).getTable(parts[1]).refereshTable();
                 FilterAddList(out,new ArrayList<Object>(db_map.get(parts[0]).getTable(parts[1]).getFields()),parts[2]);
         }
 
         return out;
     }
     public void showFor(String s){
         int x = 0,y = 0;
         if(edit.getCaret().getMagicCaretPosition() != null){
             x = edit.getCaret().getMagicCaretPosition().x;
             y = edit.getCaret().getMagicCaretPosition().y + edit.getFont().getSize();
         }
         x = x + edit.getLocationOnScreen().x;
         y = y + edit.getLocationOnScreen().y;
         this.setBounds(x, y, 400, 300);
         jList1.setListData(getCoplList(s).toArray());
         if(jList1.getModel().getSize() > 0){
             this.setVisible(true);
             jList1.setSelectedIndex(0);
             jList1.ensureIndexIsVisible(0);
         }else{
             this.setVisible(false);
         }
     }
         
     public void hideVithPrepared(){
         prepared = false;
         setVisible(false);
     }
     public void setSelectedInEdit(){
         hideVithPrepared();
        String before_caret = edit.getText().replaceAll("\\r", "").substring(0, edit.getCaretPosition());
         String text_in = ((CompleteObj)jList1.getSelectedValue()).getName();
         Matcher m = Pattern.compile("([\\w]+)\\z").matcher(before_caret);
         if(m.find()){
             edit.setSelectionStart(edit.getCaretPosition() - m.group(1).length());
             edit.setSelectionEnd(edit.getCaretPosition());
         }
         edit.replaceSelection(text_in);
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jScrollPane1 = new javax.swing.JScrollPane();
         jList1 = new javax.swing.JList();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setAlwaysOnTop(true);
         setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         setFocusable(false);
         setFocusableWindowState(false);
         setModalityType(null);
         setName("Form"); // NOI18N
         setUndecorated(true);
 
         jScrollPane1.setName("jScrollPane1"); // NOI18N
 
         jList1.setModel(new javax.swing.AbstractListModel() {
             String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
             public int getSize() { return strings.length; }
             public Object getElementAt(int i) { return strings[i]; }
         });
         jList1.setFocusable(false);
         jList1.setName("jList1"); // NOI18N
         jList1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jList1MouseClicked(evt);
             }
         });
         jScrollPane1.setViewportView(jList1);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 202, Short.MAX_VALUE)
             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 174, Short.MAX_VALUE)
             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
         setSelectedInEdit();
     }//GEN-LAST:event_jList1MouseClicked
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JList jList1;
     private javax.swing.JScrollPane jScrollPane1;
     // End of variables declaration//GEN-END:variables
 
 }
