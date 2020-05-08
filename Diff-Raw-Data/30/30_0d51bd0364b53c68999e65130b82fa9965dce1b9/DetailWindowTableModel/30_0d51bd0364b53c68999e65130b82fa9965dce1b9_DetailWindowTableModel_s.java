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
 	
    private String[] columnNames = {"Verfügbar", "Titel", "Author", "Verlag"};
     
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
                return "0";
                             
             case 1:
            	return "1";
                             
             case 2:
            	return "2";
                               
            case 3:
                return "3";
                          
             default:
                 throw new UnsupportedOperationException("Da ist wohl was Schiefgelaufen beim laden der Daten in die Tabelle");
                 
         }
 	}
 
 	
 }
