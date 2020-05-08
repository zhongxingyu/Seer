 package view;
 
 import java.util.List;
 
 import javax.swing.table.AbstractTableModel;
 
 import domain.Book;
 import domain.Copy;
 import domain.Library;
 
 public class DetailWindowTableModel extends AbstractTableModel {
 
 	Library library;
 	Book book;
 	List<Copy> list;
 	
 	public DetailWindowTableModel(Library library, Book book){
 		this.library = library;
 		this.book = book;
 		this.list = library.getCopiesOfBook(book);
 		
 	}
 	
     private String[] columnNames = {"ID", "Titel", "Zustand"};
     
     public int getColumnCount() {
         return columnNames.length;
     }
 
     public int getRowCount() {
        return list.size();
     }
 
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
         Copy column  = list.get(rowIndex);
         
         switch (columnIndex){
            case 0 : 
                 return column.getInventoryNumber();
                             
             case 1:
             	return column.getTitle();
                             
             case 2:
             	return column.getCondition();
                               
             default:
                 throw new UnsupportedOperationException("Da ist wohl was Schiefgelaufen beim laden der Daten in die Tabelle");
                 
         }
 	}
 
 	public String getColumnName(int col) {
         return columnNames[col];
     }
 	
 }
