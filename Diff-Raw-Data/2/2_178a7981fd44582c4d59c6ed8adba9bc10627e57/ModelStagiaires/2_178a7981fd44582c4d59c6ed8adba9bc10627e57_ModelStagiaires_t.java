 package data;
 
 import java.awt.Font;
 import java.util.ArrayList;
 
 import javax.swing.JTable;
 import javax.swing.UIManager;
 import javax.swing.plaf.FontUIResource;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableColumnModel;
 
 import pack.Stage;
 import pack.Stagiaire;
 
 import ui.TableModelSorter;
 
 
 public class ModelStagiaires extends AbstractTableModel {
 	
 	private static final long serialVersionUID = -4189189270303777510L;
 	
 	//attributs
 	private Stage stage;
 	private ArrayList<Stagiaire> stagiaires;
 	private final String[] entetes = {"Id", "Nom", "Prenom", "Mat", "Sp", "Comment"};
 	private TableModelSorter sorter;
 	private JTable table;
 
 	private boolean mod = false;
 	
 	private static final int[][] colWidths = { 
 			{ 40, 50, 50},
 			{ 80,200,150},
 			{ 80,200,150},
 			{ 60,150, 90},
 			{ 60, 60, 50},
 			{ 60,200,100}
 			};
 
 	/**
 	 * constructeur
 	 * @param stagiaireList
 	 */
 	public ModelStagiaires(JTable _table) {
         super();
      	//stagiaires = _stagiaires;
         stagiaires = new ArrayList<Stagiaire>();
     	table = _table;
 		sorter = new TableModelSorter(this);
 		table.setModel(sorter);
 		TableColumnModel tcm = table.getColumnModel();
     	for (int i =0; i<tcm.getColumnCount(); i++) {
 			tcm.getColumn(i).setMinWidth(colWidths[i][0]);
 			tcm.getColumn(i).setMaxWidth(colWidths[i][1]);
 			tcm.getColumn(i).setPreferredWidth(colWidths[i][2]);
     	}
 
     	sorter.setTableHeader(table.getTableHeader());
 
 		table.setRowHeight(18);
 		FontUIResource uiFont = (FontUIResource) UIManager.get("Table.font");
 		Font font = new Font(uiFont.getName(), uiFont.getStyle(), uiFont.getSize()+2);
 		table.setFont(font);
 		fireTableDataChanged();
     }
 	
 	public void setStage(Stage s) {
 		stage = s;
 		stagiaires = stage.getStagiaireList();
 		fireTableDataChanged();
 	}
 	
 	/**
 	 * retourne le nombre de lignes
 	 */
 	public int getRowCount() {
 		return stagiaires.size();
 	}
 
 	/**
 	 * retourne le nombre de colonnes
 	 */
 	public int getColumnCount() {
 		return entetes.length;
 	}
 
 	/**
 	 * retourne les noms de colonnes
 	 */
 	public String getColumnName(int columnIndex) {
         return entetes[columnIndex];
     }
 	
 	public void newStagiaire() {
		Stagiaire st = new Stagiaire("", stage.getCode(), stage.getDateStr(), stage.getDateStr(), "", "", "", "");
 		stage.ajoutStagiaire(st);
         setMod(true);
 		fireTableDataChanged();
 	}
 	
 	
     public ArrayList<Stagiaire> getSelectedStagiaires() {
     	ArrayList<Stagiaire> sl = new ArrayList<Stagiaire>();
     	int[] idx = table.getSelectedRows();
     	for (int i : idx) {
     		if (i >= 0) {
     			sl.add(stagiaires.get(sorter.modelIndex(i)));
     		}
     	}
     	return sl;
     }
 
 	public void removeStagiaires(ArrayList<Stagiaire> sl) {
 		stage.supprimerStagiaire(sl);
 		setMod(true);
 		fireTableDataChanged();
 	}
 	
 	public void removeSelectedStagiaires() {
     	removeStagiaires(getSelectedStagiaires());
 	}
 
 	/**
 	 * retourne la valeur de la case du tableau dont les coordonnes sont passs en paramtre
 	 */
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		
 		switch(columnIndex){
 			case 0:
 				return (rowIndex+1);
         	case 1:
             	return stagiaires.get(rowIndex).getNom();
         	case 2:
             	return stagiaires.get(rowIndex).getPrenom();
         	case 3:
             	return stagiaires.get(rowIndex).getMatricule();
         	case 4:
             	return stagiaires.get(rowIndex).getSpe();
         	case 5:
             	return stagiaires.get(rowIndex).getComment();
         	default:
             	return null;
 		}
 	}
 	
     @Override
     public boolean isCellEditable(int rowIndex, int columnIndex) {
     	boolean write = false;
     	switch (columnIndex) {
     		case 0:
     			write = false;
     			break;
     		case 1:
 	    	case 2:
 	    	case 3:
 	    	case 4:
 	    	case 5:
 	    		write = true;
     	}
     	return write;
     }
 
     @Override
     public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
         if(aValue != null){
             Stagiaire s = stagiaires.get(rowIndex);
             
             switch(columnIndex){
                 case 1:
                 	s.setNom((String)aValue);
                     break;
                 case 2:
                 	s.setPrenom((String)aValue);
                     break;
                 case 3:
                 	s.setMatricule((String)aValue);
                     break;
                 case 4:
 					s.setSpe((String)aValue);
                    break;
                 case 5:
 					s.setSecteur((String)aValue);
                	break;
                 default:
                     System.out.println("[ERR] ModelStagiaires.setValueAt("+columnIndex+")");
             }
             setMod(true);
             System.out.println("Set Value " + columnIndex + " of " + rowIndex);
         }
     }
 
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
     public Class getColumnClass(int columnIndex){
         switch(columnIndex){
             default:
                 return Object.class;
         }
     }
 
 	public boolean isMod() {
 		return mod;
 	}
 
 	public void setMod(boolean mod) {
 		this.mod = mod;
 	}
 
 }//fin class
