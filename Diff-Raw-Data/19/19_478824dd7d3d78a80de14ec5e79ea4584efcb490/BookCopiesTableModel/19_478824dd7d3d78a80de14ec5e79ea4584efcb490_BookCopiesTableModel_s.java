 package viewModels;
 
import java.text.DecimalFormat;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.table.AbstractTableModel;
 
 import localization.Messages;

 import domain.Book;
 import domain.Copy.Condition;
 import domain.Library;
 import domain.Loan;
 
 public class BookCopiesTableModel extends AbstractTableModel implements
 		Observer {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5278540270938445385L;
	private static final DecimalFormat myFormat = new DecimalFormat("000");
 	private Library lib;
 	private Book book;
 
 	public BookCopiesTableModel(Library lib, Book theBook) {
 		lib.addObserver(this);
 		this.lib = lib;
 		this.book = theBook;
 	}
 
 	public Class<?> getColumnClass(int c) {
 		return getValueAt(0, c).getClass();
 	}
 
 	@Override
 	public int getColumnCount() {
 		return 4;
 	}
 
	@Override 
 	public int getRowCount() {
 		if (lib == null)
 			return 0;
 		return lib.getCopiesOfBook(book).size();
 	}
 
 	public boolean isCellEditable(int row, int col) {
 		if (col == 3) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	// this method is called to set the value of each cell
 	@Override
 	public Object getValueAt(int row, int column) {
 		if (row == -1) {
 			return "";
 		}
 		switch (column) {
 
 		case 1:
 			if (lib.isCopyLent(lib.getCopiesOfBook(book).get(row))) {
 				return Messages.getString("Domain.Book.Unavailable") + " / "
 						+ Messages.getString("Domain.Book.Lent");
 			} else if (!lib.getCopiesOfBook(book).get(row).isInLendable()) {
 				return Messages.getString("Domain.Book.Unavailable") + " / "
 						+ Messages.getString("Domain.Book.BadCondition");
 			} else {
 				return Messages.getString("Domain.Book.Available");
 			}
 
 		case 3:
 			return lib.getCopiesOfBook(book).get(row).getCondition();
 		case 2:
 			for (Loan l : lib.getOpenLoans()) {
 				if (l.getCopy() == lib.getCopiesOfBook(book).get(row)) {
 					return l.getCustomer().getName() + " "
							+ l.getCustomer().getSurname() + " (" + l.getdueDatetoString() + ")";
 				}
 			}
 			return "";
		case 0:
			return myFormat.format(lib.getCopiesOfBook(book).get(row).getInventoryNumber());

 		default:
 			return "";
 		}
 
 	}
 
 	@Override
 	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 		if (columnIndex == 3) {
 			lib.getCopiesOfBook(book).get(rowIndex)
 					.setCondition((Condition) aValue);
 		}
 	}
 
 	// This method will be used to display the name of columns
 	public String getColumnName(int col) {
 		switch (col) {
 		case 0:
 			return Messages.getString("Domain.Copy.inventoryNumber");
 		case 1:
 			return Messages.getString("Domain.Copy.Status");
 		case 2:
 			return Messages.getString("Domain.BookCopies.CustomerTitle");
 		case 3:
 			return Messages.getString("Domain.Copy.condition");
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public void update(Observable o, Object modelRowEvent) {
 
 		fireTableDataChanged();
 	}
 
 }
