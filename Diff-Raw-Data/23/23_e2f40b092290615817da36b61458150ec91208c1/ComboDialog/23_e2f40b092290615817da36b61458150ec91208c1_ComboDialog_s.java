 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * ComboDialog.java
  *
  * Created on Nov 30, 2010, 3:51:02 PM
  */
 
 package mysqljavacat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.JEditorPane;
 import javax.swing.JList;
 
 /**
  *
  * @author strelok
  */
 public class ComboDialog extends javax.swing.JDialog {
 
     /** Creates new form ComboDialog */
     private JEditorPane edit;
     private boolean prepared = false;
     private String compl_string;
 
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
         this.setVisible(false);
     }
     public JList getComboList(){
         return jList1;
     }
     private void FilterAddList(ArrayList<String> list,Set<String> input,String filter){
         Set<String> set = new TreeSet<String>();
         for(String s : input)
                 set.add(s);
         if(filter == null){
             for(String s : set)
                 list.add(s);
         }else{
             Pattern p = Pattern.compile("^" + Pattern.quote(filter),Pattern.CASE_INSENSITIVE);
             for(String s : set){
                 if(p.matcher(s).find()){
                     list.add(s);
                 }
             }
         }
     }
     public ArrayList<String> getCoplList(String input){
         ArrayList<String> out = new ArrayList<String>();
         MysqlJavaCatView main_frame = (MysqlJavaCatView)MysqlJavaCatApp.getApplication().getMainView();
         HashMap<String, HashMap<String, ArrayList<String>>> db_map = main_frame.getDbMap();
         String cur_db = main_frame.getSelectedDb();
         String [] parts = input.split("\\.");
         if(input.length() == 0){
             FilterAddList(out,db_map.get(cur_db).keySet(),null);
             FilterAddList(out,db_map.keySet(),null);
         }else if(parts.length == 1 && !input.endsWith(".")){
             FilterAddList(out,db_map.get(cur_db).keySet(),parts[0]);
             FilterAddList(out,db_map.keySet(),parts[0]);
         }else if(parts.length == 1 && input.endsWith(".")){
             if(db_map.get(cur_db).get(parts[0]) != null)
                 FilterAddList(out,new HashSet<String>(db_map.get(cur_db).get(parts[0])),null);
             if(db_map.get(parts[0]) != null)
                 FilterAddList(out,db_map.get(parts[0]).keySet(),null);
         }else if(parts.length == 2 && !input.endsWith(".")){
             if(db_map.get(cur_db).get(parts[0]) != null)
                 FilterAddList(out,new HashSet<String>(db_map.get(cur_db).get(parts[0])),parts[1]);
             if(db_map.get(parts[0]) != null)
                 FilterAddList(out,new HashSet<String>(db_map.get(cur_db).get(parts[0])),parts[1]);
         }else if(parts.length == 2 && input.endsWith(".")){
             if(db_map.get(parts[0]) != null && db_map.get(parts[0]).get(parts[1]) != null)
                 FilterAddList(out,new HashSet<String>(db_map.get(parts[0]).get(parts[1])),null);                
         }else if(parts.length == 3 && !input.endsWith(".")){
             if(db_map.get(parts[0]) != null && db_map.get(parts[0]).get(parts[1]) != null)
                 FilterAddList(out,new HashSet<String>(db_map.get(parts[0]).get(parts[1])),parts[2]);
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
        this.setBounds(x, y, 200, 300);        
         jList1.setListData(getCoplList(s).toArray());
         if(jList1.getModel().getSize() > 0){
             this.setVisible(true);
             jList1.setSelectedIndex(0);
            edit.requestFocus();
             compl_string = s;
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
         String textinput = edit.getText().replace("\r\n", "\n");       
         String textout = "";
         String before_caret = textinput.substring(0,edit.getCaretPosition());
         String after_caret = "";
         if(textinput.length() > edit.getCaretPosition()){
             after_caret = textinput.substring(edit.getCaretPosition(),textinput.length());
         }
         String [] parts = compl_string.split("\\.");
         if(compl_string.endsWith(".") || compl_string.equals("")){
             textout = before_caret + jList1.getSelectedValue().toString() + after_caret;
             edit.setText(textout.replace("\n", "\r\n"));
             edit.setCaretPosition((before_caret + jList1.getSelectedValue().toString()).length());
         }else if(!compl_string.endsWith(".") && parts.length > 0){
             int before_pos = before_caret.length() - parts[parts.length - 1].length();
             if(before_pos > 0){
                 before_caret = before_caret.substring(0,before_pos);
             }else{
                 before_caret = "";
             }
             textout = before_caret + jList1.getSelectedValue().toString() + after_caret;
             edit.setText(textout.replace("\n", "\r\n"));
             edit.setCaretPosition((before_caret + jList1.getSelectedValue().toString()).length());
         }
         
     }
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jScrollPane1 = new javax.swing.JScrollPane();
         jList1 = new javax.swing.JList();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setAlwaysOnTop(true);
         setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         setModalityType(null);
         setName("Form"); // NOI18N
         setUndecorated(true);
 
         jScrollPane1.setName("jScrollPane1"); // NOI18N
 
         jList1.setModel(new javax.swing.AbstractListModel() {
             String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
             public int getSize() { return strings.length; }
             public Object getElementAt(int i) { return strings[i]; }
         });
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
