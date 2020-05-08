 package heig.igl3.roc2.GUI;
 
 import heig.igl3.roc2.Business.Budget;
 import heig.igl3.roc2.Business.Categorie;
 import heig.igl3.roc2.Business.Mouvement;
 import heig.igl3.roc2.Business.SousCategorie;
 import heig.igl3.roc2.Data.Roc2DB;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.JTableHeader;
 
 @SuppressWarnings("serial")
 public class MouvementView extends JPanel implements ActionListener, ListSelectionListener {
 	
 	protected static final String DefaultTableModel = null;
 	MouvModel tm;
 	JTable tbl;
 	private Budget budget;
 	String headers[];
 	JButton btAdd, btEdit, btDel;
 	ListSelectionModel listSelec;
 	Mouvement selectedMouv;
 	int selectedMouvIndex;
 	
 	public MouvementView(Budget budget) {
 		
 		
 		// Structure
 		JPanel mouvPanel = new JPanel(new BorderLayout(0,0));
 		JPanel btPanel = new JPanel();
 		
 		// Buttons
 		btAdd = new JButton("Ajouter");
 		btAdd.setName("addMouv");
 		btAdd.addActionListener(this);
 		
 		btEdit = new JButton("Editer");
 		btEdit.setName("editMouv");
 		btEdit.addActionListener(this);
 		btEdit.setEnabled(false);
 		
 		btDel = new JButton("Supprimer");
 		btDel.setName("delMouv");
 		btDel.addActionListener(this);
 		btDel.setEnabled(false);
 		
 		btPanel.add(btAdd);
 		btPanel.add(btEdit);
 		btPanel.add(btDel);
 		mouvPanel.add(btPanel,BorderLayout.SOUTH);
 		
 		ArrayList<String> headers = new ArrayList<String>(Arrays.asList("id", "Libellé", "Montant", "Type", "Date", "Periodicité", "Catégorie", "Sous-catégorie", "idBudget"));
 		this.budget = budget;
 		this.setLayout(new BorderLayout(0,0));
 		this.tm = new MouvModel(budget.mouvements, headers);
 		this.tbl = new JTable(tm);
 		tbl.setDragEnabled(true);
 		tbl.setAutoCreateRowSorter(true);
 		listSelec = tbl.getSelectionModel();
 		listSelec.addListSelectionListener(this);
 		hideColumn(tbl, 0);
 		hideColumn(tbl, 8);
 		mouvPanel.add(new JScrollPane(tbl));
 		this.add(mouvPanel);
 		
 		
 	}
 	
 	private void hideColumn(JTable table, int index) {
 		 table.getColumnModel().getColumn(index).setMaxWidth(0);
 		 table.getColumnModel().getColumn(index).setMinWidth(0);
 		 table.getColumnModel().getColumn(index).setPreferredWidth(0);
 	}
 	
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		JButton source = (JButton) e.getSource();
 		String actionCommand = source.getName();
 		if(!listSelec.isSelectionEmpty()) {
 			selectedMouv = tm.list.get(tbl.getSelectedRow());
 		}
 		MouvementEditor frame;
 		switch(actionCommand) {
 		case "addMouv":
 			frame = new MouvementEditor(null, true, budget);
             frame.setSize(350,300);
             frame.setLocationRelativeTo(null);
             frame.setUndecorated(false);
             frame.setVisible(true);
             frame.dispose();
             if(frame.mouvement != null){
             	tm.addRow(frame.mouvement);
             	//budget.mouvements.add(frame.mouvement);
             }
 			break;
 		case "editMouv":
 			frame = new MouvementEditor(null, true, budget, selectedMouv);
             frame.setSize(350,300);
             frame.setLocationRelativeTo(null);
             frame.setUndecorated(false);
             frame.setVisible(true);
             frame.dispose();
             if(frame.mouvement != null){
             	tm.list.remove(selectedMouv);
             	//tm.addRow(frame.mouvement);
             	budget.mouvements.remove(selectedMouv);
             	budget.mouvements.add(frame.mouvement);
             	tbl.setModel(tm);
             	tbl.repaint();
             }
 			break;
 		case "delMouv":
 			// Une fois l'item supprimé, il y a valueChanged
 			// Création d'un runnable qui sera appelé par invokeLater quand la fenêtre sera au repos 
			Runnable run = new Runnable(){
				public void run(){
					if(Roc2DB.delMouvement(selectedMouv.id)){
						budget.categories.remove(selectedMouv);
						tm.removeRow(selectedMouv);
 					}
					tbl.repaint();
				}
			};
			SwingUtilities.invokeLater(run);
 			break;
 		}
 		
 	}
 	
 	@Override
 	public void valueChanged(ListSelectionEvent e) {
 		selectedMouvIndex = tbl.getSelectedRow();
 		if (selectedMouvIndex > -1) {
 			Mouvement newSelectedMouv = tm.list.get(selectedMouvIndex);
 			if (selectedMouv == null || selectedMouv != newSelectedMouv) {
 				selectedMouv = newSelectedMouv;
 				btEdit.setEnabled(true);
 				btDel.setEnabled(true);
 			}	
 		}
 	}
 	
 	class MouvModel extends AbstractTableModel {
 		
 		public ArrayList<Mouvement> list;
 		private ArrayList<String> columnNames;
 		
 		public MouvModel(ArrayList<Mouvement> list, ArrayList<String>columnNames) {
 			this.list = list;
 			this.columnNames = columnNames;
 		}
 		
 		public String getColumnName(int col) {
 			return columnNames.get(col);
 		}
 
 		@Override
 		public int getRowCount() {
 			return list.size();
 		}
 
 		@Override
 		public int getColumnCount() {
 			return 9;
 		}
 
 		@Override
 		public Object getValueAt(int rowIndex, int columnIndex) {
 			switch(columnIndex) {
 			case 0:
 				return list.get(rowIndex).id;
 			case 1:
 				return list.get(rowIndex).libelle;
 			case 2:
 				return list.get(rowIndex).montant;
 			case 3:
 				switch(list.get(rowIndex).type) {
 				case 0:
 					return "Unique";
 				default:
 					return "Récurrent";
 				}
 			case 4:
 				DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
 		        GregorianCalendar c = list.get(rowIndex).date;
 				return df.format(c.getTime());
 			case 5:
 				return list.get(rowIndex).periodicite;
 			case 6:
 				return budget.getCategorie(list.get(rowIndex).idCategorie);
 			case 7:
 				return budget.getSousCategorie(list.get(rowIndex).idCategorie, list.get(rowIndex).idSousCategorie);
 			case 8:
 				return list.get(rowIndex).idBudget;
 			default:
 				return list.get(rowIndex).id;
 			}
 		}
 		
 		public void removeRow(Mouvement mouv) {
 			list.remove(mouv);
 		}
 		
 		public void addRow(Mouvement mouv) {
 			this.list.add(mouv);
 			this.fireTableDataChanged();
 		}
 		
 		
 	}
 
 }
