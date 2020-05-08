 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package mysqljavacat;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.JButton;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.table.DefaultTableModel;
 
 /**
  *
  * @author strelok
  */
 public class SqlTab extends JScrollPane{
     private String file_name;
     private JLabel tab_label;
     private JEditorPane editor;
     private JPanel tab_header;
     private SqlTabbedPane sqlTab;
     private ComboDialog combo_dialog;
     private DefaultTableModel result_col_model;
     private Long request_time;
     private Integer row_count;
 
     public void setResultColModel(DefaultTableModel model){
         result_col_model = model;
     }
     public DefaultTableModel getResultColModel(){
         return result_col_model;
     }
     public void setRequestTime(Long rt){
         request_time = rt;
     }
     public Long getRequestTime(){
         return request_time;
     }
     public void setRowCount(Integer rc){
         row_count = rc;
     }
     public Integer getRowCount(){
         return row_count;
     }
 
 
     public void setSqlTab(SqlTabbedPane s){
         sqlTab = s;
     }
     private SqlTab getSqlTab(){
         return this;
     }
     public ComboDialog getComboDialog(){
         return combo_dialog;
     }
     public SqlTab() {        
         editor = new JEditorPane();
         this.setBorder(null);
         this.setViewportView(editor);
         editor.setContentType("text/sql");
         combo_dialog = new ComboDialog(editor);
 
         editor.addFocusListener(new FocusListener() {
             public void focusGained(FocusEvent e) {
 
             }
             public void focusLost(FocusEvent e) {
                 if(e.getOppositeComponent() == null || !(e.getOppositeComponent().getClass() == JList.class && (JList)e.getOppositeComponent() == combo_dialog.getComboList())){
                     combo_dialog.hideVithPrepared();
                 }
             }
         });
         final CaretListener caretListener = new CaretListener() {
             public void caretUpdate(CaretEvent e) {
                 String lastword = "";
                 String input = editor.getText().replace("\r\n", "\n").substring(0, e.getDot());
                Matcher m = Pattern.compile("(\\w+)\\z").matcher(input);
                 if(m.find())
                     lastword = m.group(1);
 /*
                 System.out.println("----------------------------------------E");
                 System.out.println(e.getDot());
                 System.out.println(editor.getText().substring(0, e.getDot()));
                 System.out.println("----------------------------------------I");
                 System.out.println(input);
                 System.out.println("----------------------------------------L");
                 System.out.println(lastword);
                 System.out.println("----------------------------------------");
  */
                 if(combo_dialog.isVisible() && lastword.equals(""))
                     combo_dialog.hideVithPrepared();
                 if(combo_dialog.getPrepared()){
                     combo_dialog.showFor(lastword);
                    editor.grabFocus();
                 }
             }
         };
         editor.addCaretListener(caretListener);
         editor.addMouseListener(new MouseListener() {
 
             public void mouseClicked(MouseEvent e) {
                 combo_dialog.hideVithPrepared();
             }
 
             public void mousePressed(MouseEvent e) {
             }
 
             public void mouseReleased(MouseEvent e) {
             }
 
             public void mouseEntered(MouseEvent e) {
             }
 
             public void mouseExited(MouseEvent e) {
             }
         });
         editor.addKeyListener(new KeyListener() {
 
             
             public void keyPressed(KeyEvent e) {
                 if(!combo_dialog.getPrepared() && !combo_dialog.isVisible()){
                     if(e.getKeyChar() == '.'){
                         combo_dialog.setPrepared(true);
                     }else if(e.isControlDown() && e.getKeyCode() == 32){
                         combo_dialog.setPrepared(true);
                         caretListener.caretUpdate(new CaretEvent(e.getSource()) {
                             @Override
                             public int getDot() {
                                 return editor.getCaretPosition();
                             }
 
                             @Override
                             public int getMark() {
                                 return editor.getCaretPosition();
                             }
                         });
                     }                      
                     
                 }else if(combo_dialog.isVisible()){
                     if(e.getKeyCode() == 38){
                         JList combo_list = combo_dialog.getComboList();
                         if(combo_list.getSelectedIndex() == 0){
                             combo_list.setSelectedIndex(combo_list.getModel().getSize() - 1 );
                             combo_list.ensureIndexIsVisible(combo_list.getModel().getSize() - 1 );
                         }else{
                             combo_list.setSelectedIndex(combo_list.getSelectedIndex() - 1);
                             combo_list.ensureIndexIsVisible(combo_list.getSelectedIndex() - 1);
                         }
                         e.setKeyCode(0);
                     }else if(e.getKeyCode() == 40){
                         JList combo_list = combo_dialog.getComboList();
                         if(combo_list.getSelectedIndex() == combo_list.getModel().getSize() -1){
                             combo_list.setSelectedIndex(0);
                             combo_list.ensureIndexIsVisible(0);
                         }else{
                             combo_list.setSelectedIndex(combo_list.getSelectedIndex() + 1);
                             combo_list.ensureIndexIsVisible(combo_list.getSelectedIndex() + 1);
                         }
                         e.setKeyCode(0);
                     }else if(e.getKeyCode() == 10 || e.getKeyCode() == 9){
                         e.setKeyCode(0);
                         combo_dialog.setSelectedInEdit();
                     }
                    editor.grabFocus();
                 }
             }
 
             public void keyTyped(KeyEvent e) {
 
             }
 
             public void keyReleased(KeyEvent e) {
 
             }
 
         });
         JButton tabCloseButton = new JButton();
         tabCloseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mysqljavacat/resources/button_cancel-16.png"))); // NOI18N
         tabCloseButton.setPreferredSize(new Dimension(20, 20));
         tabCloseButton.setBorderPainted(false);
         tabCloseButton.setFocusPainted(false);
         tabCloseButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent ae) {
             if(sqlTab.getTabCount() == 1){
                 sqlTab.resetTabCount();
                 sqlTab.createTab();
             }
             sqlTab.remove(getSqlTab());
             MysqlJavaCatApp.getApplication().deleteFile(new File("current",file_name));
           }
         });
         tab_label = new JLabel();
         tab_header = new JPanel();
         tab_header.setOpaque(false);
         tab_header.add(tab_label);
         tab_header.add(tabCloseButton);
     }
 
     public String getFilename(){
         return file_name;
     }
     public void setFilename(String s){
         file_name = s;
     }
     public JEditorPane getEditPane(){
         return editor;
     }
     public JPanel getTabHeader(){
         return tab_header;
     }
     public void setTabLabel(String s){
         tab_label.setText(s);
     }
 
 }
