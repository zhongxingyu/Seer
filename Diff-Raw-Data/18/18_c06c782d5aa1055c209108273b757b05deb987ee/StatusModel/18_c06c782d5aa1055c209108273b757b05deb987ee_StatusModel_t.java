 package package1;
 
 import java.io.File;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.swing.table.AbstractTableModel;
 
 public class StatusModel extends AbstractTableModel{
 	private static final long serialVersionUID = 1L;
 	private ArrayList<Site> listSite;
 	
 	private String[] columnNames = {"Name Reserving", "Checked-In", 
 			"Site #", "Estimated Days", "Days Remaining"};
 	
	private BetterGregorianCalendar date;
	
 	@Override
 	public String getColumnName(int col){
 		return columnNames[col];
 	}
 	
 	@Override
 	public int getColumnCount() {
 		return columnNames.length;
 	}
 
 	@Override
 	public int getRowCount() {
 		return listSite.size();
 	}
 
 	@Override
 	public Object getValueAt(int row, int col) {
 		switch(col){
 		case 0:
 			return(listSite.get(row).getNameReserving());
 		case 1:
 			return (DateFormat.getDateInstance(DateFormat.SHORT)
 					.format(listSite.get(row).getCheckIn().getTime()));
 		case 2:
 			return (listSite.get(row).getSiteNumber());
 		case 3: 
 			return (listSite.get(row).getDaysStaying());
 		case 4:
			return ((listSite.get(row).getCheckIn().daysSince(date))) + 730497 + listSite.get(row).getDaysStaying();
			
 		default:
 			return null;
 		}
 	}
 
	public StatusModel(BetterGregorianCalendar d){
 		super();
		date = d;
 		listSite = new ArrayList<Site>();
 	}
 	
 	public void checkOut(int i){
 		listSite.remove(i);
 		fireTableRowsInserted(0, listSite.size());
 	}
 
 	public void addSite(Site s){
 		listSite.add(s);
 		fireTableRowsInserted(0, listSite.size());
 	}
 
 	public Site getSite(int i){
 		return listSite.get(i);
 	}
 
 	public int getSize(){
 		return listSite.size();
 	}
 	
 	public void loadFromText(String filename) {
 		listSite.clear();
 		fireTableRowsDeleted( 0, listSite.size());
 
 		try {
 			Scanner scanner = new Scanner(new File(filename));
 
 			// Should clear the arrayList and screen....
 			// Do we do this?
 
 			int size = Integer.parseInt(scanner.nextLine().trim());
 
 			for (int i = 0; i < size; i++) {
 				String siteType = scanner.nextLine().trim();
 
 				String name = scanner.nextLine().trim();
 
 				String checkInDate = null;
 				checkInDate = scanner.nextLine().trim();
 				int daysStaying = Integer.parseInt(scanner.nextLine().trim());
 				int siteNumber = Integer.parseInt(scanner.nextLine().trim());
 				int lastParam = Integer.parseInt(scanner.nextLine().trim());
 				
 				if (siteType.equals("t")) {
 					Tent t;
 					t = new Tent(name, checkInDate, daysStaying,siteNumber, lastParam);
 					listSite.add(t);
 					fireTableRowsInserted(listSite.size() - 1, listSite.size() - 1);
 
 				}
 				else if (siteType.equals("r")) {
 					RV r;
 					r = new RV(name, checkInDate, daysStaying, siteNumber, lastParam);
 					listSite.add(r);
 					fireTableRowsInserted(listSite.size() - 1, listSite.size() - 1);
 
 				}
 
 			}
 
 			scanner.close();
 			
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 }
