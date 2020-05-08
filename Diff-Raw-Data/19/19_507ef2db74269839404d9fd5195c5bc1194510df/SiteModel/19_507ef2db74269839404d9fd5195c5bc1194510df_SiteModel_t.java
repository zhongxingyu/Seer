 package package1;
 
 import javax.swing.JOptionPane;
 import javax.swing.table.AbstractTableModel;
 
 import java.util.*;
 import java.io.*;
 public class SiteModel extends AbstractTableModel {
 	
 	/** default serial version UID */
 	private static final long serialVersionUID = 1L;
 	
 	/** Array list of site */
 	private ArrayList<Site> listSite;
 	
 	/** names of the columns */
 	private String[] columnNames = {"Name Reserving", "Checked-In", 
 			"Days Staying", "Site #", "Tent/RV Info"};
 	
 	/** Keeps track of the sort your on */
 	private int sortOption;
 	
 	/******************************************************************
 	 * default constructor for SiteModel
 	 *****************************************************************/
 	public SiteModel(){
 		super();
 		sortOption = 0;
 		listSite = new ArrayList<Site>();
 	}
 	
 	/******************************************************************
 	 * gets the column name for each column
 	 * @param col the column number
 	 * @return String the name of the column
 	 *****************************************************************/
 	@Override
 	public String getColumnName(int col){
 		return columnNames[col];
 	}
 	
 	/******************************************************************
 	 * gets the length of the columns
 	 * @return int the length of the columns
 	 *****************************************************************/
 	@Override
 	public int getColumnCount() {
 		return columnNames.length;
 	}
 	
 	/******************************************************************
 	 * gets the number of rows
 	 * @return int the number of rows
 	 *****************************************************************/
 	@Override
 	public int getRowCount() {
 		return listSite.size();
 	}
 
 	/******************************************************************
 	 * sets the values of the table according the names and rows
 	 * @param int row the row number
 	 * @param int col the column number
 	 * @return Object returns the table values
 	 *****************************************************************/
 	@Override
 	public Object getValueAt(int row, int col) {
 		switch(col){
 		case 0:
 			// return the name reserving
 			return(listSite.get(row).getNameReserving());
 		case 1:
 			// return the date in the format mm/dd/yyyy
 			return GUICampingReg.SIMPLE_FORMAT
 					.format(listSite.get(row).getCheckIn().getTime());
 		case 2:
 			// return the days staying
 			return (listSite.get(row).getDaysStaying());
 		case 3: 
 			// return the site number
 			return (listSite.get(row).getSiteNumber());
 		case 4:
 			// return the number of tenters
 			// or the power used
 			int n;
 			if(listSite.get(row) instanceof Tent){
 				n = ((Tent) listSite.get(row)).getNumOfTenters();
 				return (n + " Tent" + ( n == 1 ? "er" : "ers"));
 			}
 			else
 				return (((RV) listSite.get(row)).getPower() + " Amps");
 
 		default:
 			return null;
 		}
 	}
 
 	/******************************************************************
 	 * removes a site from the table 
 	 * @param i the row to be removed
 	 *****************************************************************/
 	public void checkOut(int i){
 		listSite.remove(i);
 		fireTableRowsInserted(0, listSite.size());
 	}
 
 	/******************************************************************
 	 * adds a site to the table
 	 * @param s the site to be added
 	 *****************************************************************/
 	public void addSite(Site s){
 		listSite.add(s);
 		fireTableRowsInserted(0, listSite.size());
 	}
 
 	/******************************************************************
 	 * gets the site at the requested row
 	 * @param i the row to get the site at
 	 * @return Site the site to be returned
 	 *****************************************************************/
 	public Site getSite(int i){
 		return listSite.get(i);
 	}
 
 	/******************************************************************
 	 * gets the size of the list
 	 * @return returns the list size
 	 *****************************************************************/
 	public int getSize(){
 		return listSite.size();
 	}
 
 	/******************************************************************
 	 * Saves the table to a serializable file
 	 * @param filename the name of the file
 	 *****************************************************************/
 	public void saveDatabase(String filename){
 		try{
 			// new output stream
 			FileOutputStream fileOutput = new FileOutputStream(filename);
 			ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput);
 			// write the table to a file
 			objectOutput.writeObject(listSite);
 			objectOutput.close();
 		}
 		catch(IOException e){
 			e.printStackTrace();
 		}
 	}
 
 	/******************************************************************
 	 * loads the serializable file
 	 * @param filename the name of the file
 	 *****************************************************************/
 	public void loadDatabase(String filename){
 		try{
 			// new input stream
 			FileInputStream input = new FileInputStream(filename);
 			ObjectInputStream objectInput = new ObjectInputStream(input);
 			// read in the file and cast it as an ArrayList of site
 			listSite = (ArrayList<Site>)objectInput.readObject();
 			fireTableRowsInserted(0,listSite.size() - 1);
 			objectInput.close();
 		}
 		catch(Exception e){
 			JOptionPane.showMessageDialog(null, "File not Recognized", "Error", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	/******************************************************************
 	 * saves the file as a text file
 	 * @param filename the name of the file
 	 * @return boolean whether or not it was successful 
 	 *****************************************************************/
 	public boolean saveAsText(String filename){
 		if(filename.equals(""))
 			return false;
 		try{
 			PrintWriter out = new PrintWriter(
 					new BufferedWriter(new FileWriter(filename)));
 			// print out the size
 			out.println(listSite.size());
 			for (int i = 0; i < listSite.size(); i++) {
 				Site s = listSite.get(i);
 				// print out the character representing the site(t or r)
 				out.println(s.getClass().getName().toLowerCase().charAt(9));
 				// print out the name
 				out.println(s.getNameReserving());
 				// print out the date in mm/dd/yyyy
 				out.println(GUICampingReg.SIMPLE_FORMAT
 						.format(s.getCheckIn().getTime()));
 				// print out the days staying
 				out.println(s.getDaysStaying());
 				// print out the site number
 				out.println(s.getSiteNumber());
 				// if it is a tent, print the tenters
 				if (s instanceof Tent)
 					out.println(((Tent) s).getNumOfTenters());
 				// if it is an RV, print the power
 				else
 					out.println(((RV) s).getPower());
 			}
 			out.close();
 			return true;
 		} catch (IOException ex) {
 			return false;
 		}
 	}
 	
 	/******************************************************************
 	 * loads the table from a text file
 	 * @param filename the name of the file
 	 *****************************************************************/
 	public void loadFromText(String filename) {
 		listSite.clear();
 		fireTableRowsDeleted( 0, listSite.size());
 
 		try {
 			Scanner scanner = new Scanner(new File(filename));
 			
 			int size = Integer.parseInt(scanner.nextLine().trim());
 
 			for (int i = 0; i < size; i++) {
 				// gets the type of the site
 				String siteType = scanner.nextLine().trim();
 				// gets the name
 				String name = scanner.nextLine().trim();
 
 				String checkInDate = null;
 				// gets the check in date
 				checkInDate = scanner.nextLine().trim();
 				// gets the days staying
 				int daysStaying = Integer.parseInt(scanner.nextLine().trim());
 				// gets the site number
 				int siteNumber = Integer.parseInt(scanner.nextLine().trim());
 				// gets the tenters or the power
 				int lastParam = Integer.parseInt(scanner.nextLine().trim());
 				
 				// if it is a tent, add a tent site
 				if (siteType.equals("t")) {
 					Tent t;
 					t = new Tent(name, checkInDate, daysStaying,siteNumber, lastParam);
 					listSite.add(t);
 					fireTableRowsInserted(listSite.size() - 1, listSite.size() - 1);
 
 				}
 				// if it is an RV, add an RV site
 				else if (siteType.equals("r")) {
 					RV r;
 					r = new RV(name, checkInDate, daysStaying, siteNumber, lastParam);
 					listSite.add(r);
 					fireTableRowsInserted(listSite.size() - 1, listSite.size() - 1);
 
 				}
 
 			}
 
 			scanner.close();
 			
 		} catch (Exception ex) {
 			JOptionPane.showMessageDialog(null, "File not Recognized", "Error", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	public void sort(int col){
 		
 		switch (col){
 		case 0:
 			if (sortOption%4 == 0) {
 				Collections.sort(listSite, Site.Comparators.ASC_FIRSTNAME);
 			} else if (sortOption%4 == 1) {
 				Collections.sort(listSite, Site.Comparators.DES_FIRSTNAME);
 			}
 			
 			sortOption++;
 			sortOption -= (sortOption>=4)?4:0;
 			break;
 		case 1:
 			if (sortOption%2 == 0) {
 				Collections.sort(listSite, Site.Comparators.ASC_CHECKIN);
 			} else if (sortOption%2 == 1) {
 				Collections.sort(listSite, Site.Comparators.DES_CHECKIN);
 			}
 			
 			sortOption++;
 			sortOption -= (sortOption>=2)?2:0;
 			break;
 		case 2:
 			if (sortOption%2 == 0) {
 				Collections.sort(listSite, Site.Comparators.ASC_DAYS);
 			} else if (sortOption%2 == 1) {
 				Collections.sort(listSite, Site.Comparators.DES_DAYS);
 			}
 			
 			sortOption++;
 			sortOption -= (sortOption>=2)?2:0;
 			break;
 		case 3:
 			if (sortOption%2 == 0) {
 				Collections.sort(listSite, Site.Comparators.ASC_SITENUMBER);
 			} else if (sortOption%2 == 1) {
 				Collections.sort(listSite, Site.Comparators.DES_SITENUMBER);
 			}
 			
 			sortOption++;
 			sortOption -= (sortOption>=2)?2:0;
 			break;
 		case 4:
 			ArrayList<Site> listTents = new ArrayList<Site>();
 			ArrayList<Site> listRVs = new ArrayList<Site>();

 			for(int i = 0; i<listSite.size(); i++) {
 				if (listSite.get(i) instanceof Tent) {
 					listTents.add(listSite.get(i));
 				}
 				if (listSite.get(i) instanceof RV){	
 					listRVs.add(listSite.get(i));
 				}
 			}

 			listSite.clear();
 			if (sortOption%2 == 0) {
 				Collections.sort(listTents, Tent.Comparators.ASC_TENTERS);
 				Collections.sort(listRVs, RV.Comparators.ASC_POWER);
 				if (sortOption%4 == 0) {
 					listSite.addAll(listTents);
 					listSite.addAll(listRVs);
 				}
 				if (sortOption%4 == 2) {
 					listSite.addAll(listRVs);
 					listSite.addAll(listTents);
 				}
 			}
 			if (sortOption%2 == 1) {
 				Collections.sort(listTents, Tent.Comparators.DES_TENTERS);
 				Collections.sort(listRVs, RV.Comparators.DES_POWER);
 				if (sortOption%4 == 1) {
 					listSite.addAll(listTents);
 					listSite.addAll(listRVs);
 				}
 				if (sortOption%4 == 3) {
 					listSite.addAll(listRVs);
 					listSite.addAll(listTents);
 				}
 			}
 			break;
 		default:
			System.out.println("Default");
 			break;
 		}
 	}
 
 }
