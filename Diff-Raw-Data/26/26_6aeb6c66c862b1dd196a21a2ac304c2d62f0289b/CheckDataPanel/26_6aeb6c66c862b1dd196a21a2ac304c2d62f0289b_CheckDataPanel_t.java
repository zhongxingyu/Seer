 package edu.mit.wi.haploview;
 
 import javax.swing.*;
 import javax.swing.event.TableModelListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.table.*;
 import java.awt.*;
 import java.io.*;
 import java.util.Vector;
 
 import edu.mit.wi.pedfile.MarkerResult;
 import edu.mit.wi.pedfile.PedFile;
 import edu.mit.wi.pedfile.PedFileException;
 
 public class CheckDataPanel extends JPanel implements TableModelListener{
 	private JTable table;
     private CheckDataTableModel tableModel;
 	private PedFile pedfile;
     private HaploData theData;
 
     boolean changed;
     static int STATUS_COL = 8;
 
     public CheckDataPanel(HaploData hd, boolean disp) throws IOException, PedFileException{
         STATUS_COL = 8;
         setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
         pedfile = hd.getPedFile();
         theData = hd;
         Vector result = pedfile.getResults();
 
         int numResults = result.size();
         Vector tableColumnNames = new Vector();
         tableColumnNames.add("#");
         if (theData.infoKnown){
             tableColumnNames.add("Name");
             tableColumnNames.add("Position");
             STATUS_COL += 2;
         }
         tableColumnNames.add("ObsHET");
         tableColumnNames.add("PredHET");
         tableColumnNames.add("HWpval");
         tableColumnNames.add("%Geno");
         tableColumnNames.add("FamTrio");
         tableColumnNames.add("MendErr");
         tableColumnNames.add("MAF");
         tableColumnNames.add("Rating");
 
         Vector tableData = new Vector();
         int[] markerRatings = new int[numResults];
         for (int i = 0; i < numResults; i++){
             Vector tempVect = new Vector();
             MarkerResult currentResult = (MarkerResult)result.get(i);
             tempVect.add(new Integer(i+1));
             if (theData.infoKnown){
                 tempVect.add(Chromosome.getUnfilteredMarker(i).getName());
                 tempVect.add(new Long(Chromosome.getUnfilteredMarker(i).getPosition()));
             }
             tempVect.add(new Double(currentResult.getObsHet()));
             tempVect.add(new Double(currentResult.getPredHet()));
             tempVect.add(new Double(currentResult.getHWpvalue()));
             tempVect.add(new Double(currentResult.getGenoPercent()));
             tempVect.add(new Integer(currentResult.getFamTrioNum()));
             tempVect.add(new Integer(currentResult.getMendErrNum()));
            tempVect.add(new Double(currentResult.getMAF()));
 
             if (currentResult.getRating() > 0){
                 tempVect.add(new Boolean(true));
             }else{
                 tempVect.add(new Boolean(false));
             }
 
             //this value is never displayed, just kept for bookkeeping
             markerRatings[i] = currentResult.getRating();
 
             tableData.add(tempVect.clone());
         }
 
         tableModel = new CheckDataTableModel(tableColumnNames, tableData, markerRatings);
         tableModel.addTableModelListener(this);
 
         if (disp){
             table = new JTable(tableModel);
             final CheckDataCellRenderer renderer = new CheckDataCellRenderer();
             try{
                 table.setDefaultRenderer(Class.forName("java.lang.Double"), renderer);
                 table.setDefaultRenderer(Class.forName("java.lang.Integer"), renderer);
                 table.setDefaultRenderer(Class.forName("java.lang.Long"), renderer);
             }catch (Exception e){
             }
 
             table.getColumnModel().getColumn(0).setPreferredWidth(30);
             table.getColumnModel().getColumn(0).setMinWidth(30);
             if (theData.infoKnown){
                 table.getColumnModel().getColumn(1).setMinWidth(100);
             }
             JScrollPane tableScroller = new JScrollPane(table);
             tableScroller.setMaximumSize(new Dimension(600, tableScroller.getPreferredSize().height));
             add(tableScroller);
         }
     }
 
 	public PedFile getPedFile(){
 		return pedfile;
 	}
 
 	public JTable getTable(){
 		return table;
 	}
 
     public void tableChanged(TableModelEvent e) {
         if (e.getColumn() == STATUS_COL){
             changed = true;
         }
     }
 
     public void printTable(File outfile) throws IOException{
         FileWriter checkWriter = null;
         if (outfile != null){
             checkWriter = new FileWriter(outfile);
         }
 
         int numCols = tableModel.getColumnCount();
         StringBuffer header = new StringBuffer();
         for (int i = 0; i < numCols; i++){
             header.append(tableModel.getColumnName(i)).append("\t");
         }
         header.append("\n");
 
         if (outfile != null){
             checkWriter.write(header.toString());
         }else{
             System.out.print(header.toString());
         }
         for (int i = 0; i < tableModel.getRowCount(); i++){
             StringBuffer sb = new StringBuffer();
             //don't print the true/false vals in last column
             for (int j = 0; j < numCols-1; j++){
                 sb.append(tableModel.getValueAt(i,j)).append("\t");
             }
             //print BAD if last column is false
             if (((Boolean)tableModel.getValueAt(i, numCols-1)).booleanValue()){
                 sb.append("\n");
             }else{
                 sb.append("BAD\n");
             }
             if (outfile != null){
                 checkWriter.write(sb.toString());
             }else{
                 System.out.print(sb.toString());
             }
         }
         if (outfile != null){
             checkWriter.close();
         }
     }
 
     public void selectAll(){
         for (int i = 0; i < table.getRowCount(); i++){
             table.setValueAt(new Boolean(true), i, STATUS_COL);
         }
 	changed = true;
     }
 
     public void redoRatings(){
         try{
             Vector result = pedfile.check();

             for (int i = 0; i < table.getRowCount(); i++){
                 MarkerResult cur = (MarkerResult)result.get(i);
                int curRating = cur.getRating();
                if (curRating > 0){
                     table.setValueAt(new Boolean(true),i,STATUS_COL);
                 }else{
                     table.setValueAt(new Boolean(false),i,STATUS_COL);
                 }
                tableModel.setRating(i,curRating);
             }
            changed = true;
         }catch (Exception e){
             e.printStackTrace();
         }
     }
 
     class CheckDataTableModel extends AbstractTableModel {
 		Vector columnNames; Vector data; int[] ratings;
 
 		public CheckDataTableModel(Vector c, Vector d, int[] r){
 			columnNames=c;
 			data=d;
             ratings = r;
 		}
 
 		public int getColumnCount(){
 			return columnNames.size();
 		}
 
 		public int getRowCount(){
 			return data.size();
 		}
 
 		public Object getValueAt(int row, int column){
 			return ((Vector)data.elementAt(row)).elementAt(column);
 		}
 
 		public Class getColumnClass(int c){
 			return getValueAt(0, c).getClass();
 		}
 
 		public int getRating(int row){
 			return ratings[row];
 		}
 
        public void setRating(int row, int value) {
            if(row < ratings.length) {
                ratings[row] = value;
            }
        }

 		public String getColumnName(int n){
 			return (String)columnNames.elementAt(n);
 		}
 
 		public boolean isCellEditable(int row, int col){
 			if (getColumnName(col).equals("Rating")){
 				return true;
 			}else{
 				return false;
 			}
 		}
 
 
 
 		public void setValueAt(Object value, int row, int col){
 			((Vector)data.elementAt(row)).set(col, value);
 			fireTableCellUpdated(row, col);
 		}
 	}
 
 	class CheckDataCellRenderer extends DefaultTableCellRenderer {
 		public Component getTableCellRendererComponent
 		        (JTable table, Object value, boolean isSelected,
 		         boolean hasFocus, int row, int column)
 		{
 			Component cell = super.getTableCellRendererComponent
 			        (table, value, isSelected, hasFocus, row, column);
 			int myRating = ((CheckDataTableModel)table.getModel()).getRating(row);
            String thisColumnName = table.getColumnName(column);
             cell.setForeground(Color.black);
             //bitmasking to decode the status bits
             if (myRating < 0){
                 myRating *= -1;
                 if ((myRating & 1) != 0){
                     if(thisColumnName.equals("ObsHET")){
                         cell.setForeground(Color.red);
                     }
                 }
                 if ((myRating & 2) != 0){
                     if (thisColumnName.equals("%Geno")){
                         cell.setForeground(Color.red);
                     }
                 }
                 if ((myRating & 4) != 0){
                     if (thisColumnName.equals("HWpval")){
                         cell.setForeground(Color.red);
                     }
                 }
                 if ((myRating & 8) != 0){
                     if (thisColumnName.equals("MendErr")){
                         cell.setForeground(Color.red);
                     }
                 }
                 if ((myRating & 16) != 0){
                     if (thisColumnName.equals("MAF")){
                         cell.setForeground(Color.red);
                     }
                 }
             }
 			return cell;
 		}
 	}
 


 }
