 package package1;
 
 import javax.swing.table.AbstractTableModel;
 import java.util.*;
 import java.io.*;
 import java.text.*;
 public class SiteModel extends AbstractTableModel {
 	private ArrayList<Site> listSite;
 	private String[] columnNames = {"Name Reserving", "Checked-In", 
 			"Days Staying", "Site #", "Tent/RV Info"};
 
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
 			return (listSite.get(row).getDaysStaying());
 		case 3: 
 			return (listSite.get(row).getSiteNumber());
 		case 4:
 			if(listSite.get(row) instanceof Tent)
 				return (((Tent) listSite.get(row)).getNumOfTenters() + " Tenters");
 			else
 				return (((RV) listSite.get(row)).getPower() + " Amps");
 
 		default:
 			return null;
 		}
 	}
 
 	public SiteModel(){
 		super();
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
 
 	public void saveDatabase(String filename){
 		try{
 			FileOutputStream fileOutput = new FileOutputStream(filename);
 			ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput);
 			objectOutput.writeObject(listSite);
 			objectOutput.close();
 		}
 		catch(IOException e){
 			e.printStackTrace();
 		}
 	}
 
 	public void loadDatabase(String filename){
 		try{
 			FileInputStream input = new FileInputStream(filename);
 			ObjectInputStream objectInput = new ObjectInputStream(input);
 			listSite = (ArrayList<Site>)objectInput.readObject();
 			fireTableRowsInserted(0,listSite.size() - 1);
 			objectInput.close();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	public boolean saveAsText(String filename){
 		if(filename.equals(""))
 			return false;
 		try{
 			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
 			out.println(listSite.size());
 			for (int i = 0; i < listSite.size(); i++) {
 				Site s = listSite.get(i);
 				out.println(s.getClass().getName().toLowerCase().charAt(9));
 				out.println(s.getNameReserving());
 				out.println(DateFormat.getDateInstance(DateFormat.SHORT)
 						.format(s.getCheckIn().getTime()));
 				out.println(s.getDaysStaying());
 				out.println(s.getSiteNumber());
 
 				if (s instanceof Tent)
 					out.println(((Tent) s).getNumOfTenters());
 				else
 					out.println(((RV) s).getPower());
 			}
 			out.close();
 			return true;
 		} catch (IOException ex) {
 			return false;
 		}
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
 				String name = scanner.nextLine().trim();
				String siteType = scanner.nextLine().trim();
 
 				GregorianCalendar checkInDate = null;
 				try {
 					DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
 					Date date = formatter.parse(scanner.nextLine().trim());
 					checkInDate = new GregorianCalendar();
 					checkInDate.setTime(date);
 				} catch (ParseException ex) {
 					ex.printStackTrace();
 				}
				//String owner = scanner.nextLine().trim();
 
 				int daysStaying = Integer.parseInt(scanner.nextLine().trim());
 				int siteNumber = Integer.parseInt(scanner.nextLine().trim());
 				int lastParam = Integer.parseInt(scanner.nextLine().trim());
 
 				if (siteType == "t") {
 					Tent t = new Tent(name, checkInDate, daysStaying,siteNumber, lastParam);
 					listSite.add(t);
					fireTableRowsInserted(listSite.size() - 1, listSite.size() - 1);

 				}
 				else if (siteType == "r") {
 					RV r = new RV(name, checkInDate, daysStaying, siteNumber, lastParam);
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
