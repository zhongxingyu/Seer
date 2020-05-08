 package edacc.experiment;
 
 import edacc.model.DatabaseConnector;
 import javax.swing.table.AbstractTableModel;
 import java.util.ArrayList;
 import edacc.model.Solver;
 import edacc.model.SolverDAO;
 import java.util.HashMap;
 
 /**
  *
  * @author daniel
  */
 public class SolverTableModel extends AbstractTableModel {
 
     private static final String[] columns_noCompetition = {"Name", "Version", "binary name", "md5", "description", "Selected"};
     private static final String[] columns_competition = {"Name", "Version", "binary name", "md5", "description", "categories", "Selected"};
     private String[] columns = columns_competition;
     private String[] categories;
     private boolean isCompetition;
     private ArrayList<Solver> solvers;
     private Boolean[] selected;
 
     public SolverTableModel() {
         this.solvers = new ArrayList<Solver>();
     }
 
     public void setSolvers(ArrayList<Solver> solvers) {
         this.solvers = solvers;
         this.selected = new Boolean[solvers.size()];
         for (int i = 0; i < solvers.size(); i++) {
             selected[i] = false;
         }
         try {
             isCompetition = DatabaseConnector.getInstance().isCompetitionDB();
         } catch (Exception e) {
             isCompetition = false;
         }
         categories = null;
         if (isCompetition) {
             columns = columns_competition;
             if (solvers != null) {
                 categories = new String[solvers.size()];
                 try {
                     HashMap<Integer, ArrayList<String>> cats = SolverDAO.getCompetitionCategories();
                     for (int i = 0; i < solvers.size(); i++) {
                         String tmp = "";
                         ArrayList<String> catlist = cats.get(solvers.get(i).getId());
                         if (catlist != null) {
                             for (int k = 0; k < catlist.size(); k++) {
                                 tmp += catlist.get(k);
                                 if (k != catlist.size() - 1) {
                                     tmp += ", ";
                                 }
                             }
                         }
                         categories[i] = tmp;
                     }
                 } catch (Exception e) {
                 }
             }
         } else {
             columns = columns_noCompetition;
         }
         this.fireTableStructureChanged();
     }
 
     @Override
     public int getRowCount() {
         return solvers.size();
     }
 
     @Override
     public int getColumnCount() {
         return columns.length;
     }
 
     @Override
     public String getColumnName(int col) {
         return columns[col];
     }
 
     @Override
     public Class getColumnClass(int col) {
         return getValueAt(0, col).getClass();
     }
 
     @Override
     public boolean isCellEditable(int row, int col) {
         if (isCompetition) {
             col--;
         }
        if (col == 4) {
             return true;
         }
         return false;
     }
 
     @Override
     public void setValueAt(Object value, int row, int col) {
         if (isCompetition) {
             col--;
         }
        if (col == 4) {
             selected[row] = (Boolean) value;
         }
         fireTableCellUpdated(row, col);
     }
 
     @Override
     public Object getValueAt(int rowIndex, int columnIndex) {
         switch (columnIndex) {
             case 0:
                 return solvers.get(rowIndex).getName();
             case 1:
                 return solvers.get(rowIndex).getVersion();
             case 2:
                 return solvers.get(rowIndex).getBinaryName();
             case 3:
                 return solvers.get(rowIndex).getMd5();
             case 4:
                 return solvers.get(rowIndex).getDescription();
             case 5:
                 if (isCompetition) {
                     return categories[rowIndex] == null ? "" : categories[rowIndex];
                 } else {
                     return selected[rowIndex];
                 }
             case 6:
                 return selected[rowIndex];
             default:
                 return "";
         }
     }
 
     public void setSolverSelected(int solverId, boolean value) {
         for (int i = 0; i < solvers.size(); i++) {
             if (solvers.get(i).getId() == solverId) {
                 selected[i] = value;
                this.fireTableCellUpdated(i, 4);
                 break;
             }
         }
     }
 
     public Solver getSolver(int row) {
         return solvers.get(row);
     }
 
     public boolean isSelected(int row) {
         return selected[row];
     }
 
     public void setSelected(int row, boolean sel) {
         selected[row] = sel;
         fireTableRowsUpdated(row, row);
     }
 }
