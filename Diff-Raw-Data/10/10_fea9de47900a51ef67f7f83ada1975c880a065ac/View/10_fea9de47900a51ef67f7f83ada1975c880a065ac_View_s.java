 package ausleihe.view;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.ScrollPane;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Enumeration;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.TableColumnModelListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableModel;
 
 import ausleihe.controller.Controller;
 import ausleihe.view.panel.CreatePanel;
 import ausleihe.view.panel.Login;
 import ausleihe.view.panel.ToolBar;
 
 public class View extends JFrame{
     private static final long serialVersionUID = 1L;
     
     private Controller controller;
     private JPanel login_tool_Pannel;
     private JPanel main_Pannel;
     
     Login login;
     ToolBar tools;
     CreatePanel createPanel;
     JTable table;
     
     private String currentTableName;
     
     public View(Controller controller){ 
         super();
         this.controller = controller;
         this.login_tool_Pannel = new JPanel();
         this.main_Pannel = new JPanel();
         
         login = new Login(controller);
         tools = new ToolBar(controller);
         
         login_tool_Pannel.setLayout(new FlowLayout());
         
         login_tool_Pannel.add(login);
         login_tool_Pannel.add(tools);    
         
         setLayout(new BorderLayout());
         add(login_tool_Pannel, BorderLayout.NORTH);
         add(main_Pannel, BorderLayout.CENTER);
         
         pack();
         setVisible(true);
     }
     
     public void showTableModel(final TableModel tableModle){
         table = new JTable(tableModle);
         
         tableModle.addTableModelListener(new TableModelListener() {
             
             @Override
             public void tableChanged(TableModelEvent arg0) {
                 String primary_name = tableModle.getColumnName(0);
                 String column_name = tableModle.getColumnName(arg0.getColumn());
                 String value = (String) tableModle.getValueAt(arg0.getFirstRow(), arg0.getColumn());
                 String key = (String) tableModle.getValueAt(arg0.getFirstRow(), 0);
                 
                 controller.updateValue(primary_name, column_name, key, value);
             }
         });
         
         //table.gets
         
         JScrollPane scroll = new JScrollPane(table);
         scroll.setPreferredSize(new Dimension(600,600));
         
         main_Pannel.setLayout(new BorderLayout());
         main_Pannel.removeAll();
         main_Pannel.add(scroll, BorderLayout.CENTER);
         main_Pannel.setPreferredSize(new Dimension(800,600));
         
         showCreatePanel(tableModle);
         
         pack();
         repaint();
     }
     
     public void deleteSelectedTupels(){
        int start = table.getSelectedRow();
        int end = start + table.getSelectedRowCount();
         
        while(start < end){
            controller.deleteTupel(table.getColumnName(0), (String) table.getValueAt(start, 0));
            ++start;
         }
         
         controller.loadTable(currentTableName);
         pack();
         repaint();
     }
     
     public void showCreatePanel(TableModel tableModle){
         main_Pannel.add(new CreatePanel(controller,this, tableModle), BorderLayout.EAST);
     }
     
     public void setConnected(String username){
         login.setConnected(username);
         tools.loadTools(username);
         pack();
         repaint();
     }
     public void setConnectionFailed(){
         login.add(new JLabel("Connection Failed ..."), BorderLayout.SOUTH);
         pack();
         repaint();
     }
     
     public void setCurrentTableName(String currentTableName){
         this.currentTableName = currentTableName;
     }
     
     public String getCurrentTableName(){
         return currentTableName;
     }
 
 }
