 package view;
 
 import java.util.List;
 
 import javax.swing.table.AbstractTableModel;
 
 import domain.Book;
 import domain.Copy;
 import domain.Library;
 import domain.Loan;
 
 public class LoanTableModel extends AbstractTableModel {
 
 	private Library lib;
 
 	public LoanTableModel(Library lib) {
 		this.lib = lib;
 	}
 
 	private String[] columnNames = new String[] {
 			"Status", "Copy-ID", "Title", "Lend to" 
 	};
 
 	private static final long serialVersionUID = 3924577490865829762L;
 	Class[] columnTypes = new Class[] {
 		String.class, String.class, String.class, String.class
 	};
 	@Override
 	public Class<?> getColumnClass(int columnIndex) {
 		return columnTypes[columnIndex];
 	}
 	boolean[] columnEditables = new boolean[] {
 		false, false, false, false
 	};
 	public boolean isCellEditable(int row, int column) {
 		return columnEditables[column];
 	}
 	@Override
 	public int getColumnCount() {
 		return columnTypes.length;
 	}
 	@Override
 	public int getRowCount() {
 		return lib.getLentLoans().size();
 	}
 	
 	@Override
 	public Object getValueAt(int arg0, int arg1) {
 		List<Loan> loans = lib.getLentLoans();
 		
 		if(loans.size() < 1)
 			return "";
 		Loan loan = loans.get(arg0);
 		switch (arg1) {
 		case 0:
 			if(loan.isOverdue()){
 				return (String)"Overdue!";
 			}
 			return (String)"Ok";
 		case 1:
			return "" + loan.getCopy().getInventoryNumber();
 			
 		case 2:
 			return loan.getCopy().getTitle().getName();
 			
 		default:
 			return (String)loan.getCustomer().getFirstname() + " " + loan.getCustomer().getLastname();
 			
 		}
 	}
 	
 	@Override
 	public String getColumnName(int column) {
 		return columnNames[column];
 	}
 
 }
