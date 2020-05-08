 /**
  * Results.java is a tab that goes into ExplorerView. It is used
  * to view the results of a query defined in the query tab.
  *
  * @author Andrew Hollenbach <anh7216> 
  */
 
 package steam.dbexplorer.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 
 import steam.dbexplorer.controller.ExplorerController;
 
 public class ResultsTab extends JPanel {
 	private ExplorerController controller;
 	private QueryTab queryTab;
 	
 	private JTable results;
 	private JScrollPane scrollPane;
 	
 	public ResultsTab(ExplorerController controller) {
 		super();
 		this.controller = controller;
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		
 		initializeTable();
 				
 		JPanel CUDpanel = createCUDPanel("entry");
 		
 		this.add(scrollPane);
 		this.add(CUDpanel);
 	}
 	
 	private void initializeTable() {
 		results = new JTable();
 		results.setModel(new DefaultTableModel(controller.getData(null,null), controller.getLabels(null)));
 		scrollPane = new JScrollPane(results);
 		results.setFillsViewportHeight(true);
 	}
 	
 	public void updateTable(String tableName) {
 		String[] tmp = {};
		results.setModel(new DefaultTableModel(controller.getData(tableName,tmp), controller.getLabels(tableName)));
 	}
 
 	private JPanel createCUDPanel(String addDeleteWhat) {
 		JPanel p = new JPanel();
 		JButton add = new JButton("Add new " + addDeleteWhat);
 		add.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
             	DefaultTableModel model = (DefaultTableModel) results.getModel();
             	Object[] values = {};
             	model.addRow(values);
             }
         });
 		p.add(add);
 		JButton update = new JButton("Update entries");
 		update.setEnabled(false);
 		p.add(update);
 		JButton delete = new JButton("Remove " + addDeleteWhat);
 		delete.setEnabled(false);
 		p.add(delete);
 		
 		return p;
 	}
 
 	public void setQueryPanelRef(QueryTab queryPanel) {
 		this.queryTab = queryPanel;
 	}
 }
