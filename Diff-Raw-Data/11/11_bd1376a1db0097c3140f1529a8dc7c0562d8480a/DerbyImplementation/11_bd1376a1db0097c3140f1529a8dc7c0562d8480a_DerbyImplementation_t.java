 /**
  * 
  */
 package net.skyebook.padloader.database;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 
 import net.skyebook.padloader.record.ADRRecord;
 import net.skyebook.padloader.record.BBLRecord;
 import net.skyebook.padloader.record.Record;
 
 /**
  * Connects to an embedded Derby database
  * @author Skye Book
  *
  */
 public class DerbyImplementation implements DatabaseInterface {
 
 	private final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
 	private String databaseName = "property-address-directory";
 	private String connectionString;
 	private Connection connection;
 
 	private PreparedStatement insertADR;
 	private PreparedStatement insertBBL;
 
 	/**
 	 * 
 	 */
 	public DerbyImplementation() {
 		connectionString = "jdbc:derby:"+databaseName+";create=true";
 
 		// Start Derby
 		try {
 			Class.forName(driver);
 			connection = DriverManager.getConnection(connectionString);
 
 			createTables();
 
 			insertADR = connection.prepareStatement("INSERT INTO ADR VALUES(?, ?, ?, ?, ?," +
 					"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 
 			insertBBL = connection.prepareStatement("INSERT INTO BBL VALUES(?, ?, ?, ?, ?," +
 					"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void createTables() throws IOException, SQLException{
 		Statement createTables = connection.createStatement();
 
 		StringBuilder createTablesString = new StringBuilder();
 		BufferedReader reader = new BufferedReader(new FileReader(new File("sql/create_adr_table.sql")));
 
 		while(reader.ready()){
 			String thisLine = reader.readLine();
 			if(!thisLine.contains("--")){
 				createTablesString.append(thisLine);
 			}
 		}
 		reader.close();
 		
 		createTables.execute(createTablesString.toString());
 		
 		createTablesString = new StringBuilder();
 		reader = new BufferedReader(new FileReader(new File("sql/create_bbl_table.sql")));
 		
 		while(reader.ready()){
 			String thisLine = reader.readLine();
 			if(!thisLine.contains("--")){
 				createTablesString.append(thisLine);
 			}
 		}
 		reader.close();
 
 		createTables.execute(createTablesString.toString());
 	}
 
 	/* (non-Javadoc)
 	 * @see net.skyebook.padloader.database.DatabaseInterface#addRecord(net.skyebook.padloader.record.Record)
 	 */
 	@Override
 	public void addRecord(Record record) {
		if(record instanceof ADRRecord){
			addRecord((ADRRecord)record);
		}
		else{
			addRecord((BBLRecord)record);
		}
 	}
 
 	/* (non-Javadoc)
 	 * @see net.skyebook.padloader.database.DatabaseInterface#addRecord(net.skyebook.padloader.record.ADRRecord)
 	 */
 	@Override
 	public void addRecord(ADRRecord record) {
 		try {
 			insertADR.setShort(1, record.getBoro());
 			insertADR.setInt(2, record.getBlock());
 			insertADR.setInt(3, record.getLot());
 			insertADR.setInt(4, record.getBin());
 			insertADR.setString(5, record.getLhnd());
 			insertADR.setString(6, record.getLhns());
 			insertADR.setString(7, ""+record.getLcontpar());
 			insertADR.setString(8, ""+record.getLsos());
 			insertADR.setString(9, record.getHhnd());
 			insertADR.setString(10, record.getHhns());
 			insertADR.setString(11, ""+record.getHcontpar());
 			insertADR.setString(12, ""+record.getHsos());
 			insertADR.setShort(13, record.getScboro());
 			insertADR.setInt(14, record.getSc5());
 			insertADR.setShort(15, record.getSclgc());
 			insertADR.setString(16, record.getStname());
 			insertADR.setString(17, ""+record.getAddrtype());
 			insertADR.setInt(18, record.getRealb7sc());
 			insertADR.setString(19, createShortArray(record.getValidlgcs()));
 			insertADR.setShort(20, record.getParity());
 			insertADR.setLong(21, record.getB10sc());
 			insertADR.setInt(22, record.getSegid());
 			insertADR.execute();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see net.skyebook.padloader.database.DatabaseInterface#addRecord(net.skyebook.padloader.record.BBLRecord)
 	 */
 	@Override
 	public void addRecord(BBLRecord record) {
 		try {
 			insertBBL.setShort(1, record.getLoboro());
 			insertBBL.setInt(2, record.getLoblock());
 			insertBBL.setInt(3, record.getLolot());
 			insertBBL.setShort(4, record.getLobblscc());
 			insertBBL.setShort(5, record.getHiboro());
 			insertBBL.setInt(6, record.getHiblock());
 			insertBBL.setInt(7, record.getHilot());
 			insertBBL.setShort(8, record.getHibblscc());
 			insertBBL.setShort(9, record.getBoro());
 			insertBBL.setInt(10, record.getBlock());
 			insertBBL.setInt(11, record.getLot());
 			insertBBL.setShort(12, record.getBblscc());
 			insertBBL.setShort(13, record.getBillboro());
 			insertBBL.setInt(14, record.getBillblock());
 			insertBBL.setInt(15, record.getBilllot());
 			insertBBL.setShort(16, record.getBillbblscc());
 			insertBBL.setString(17, ""+record.getCondoflag());
 			insertBBL.setString(18, record.getCondonum());
 			insertBBL.setInt(19, record.getCoopnum());
 			insertBBL.setShort(20, record.getNumbf());
 			insertBBL.setInt(21, record.getNumaddr());
 			insertBBL.setString(22, ""+record.getVacant());
 			insertBBL.setString(23, ""+record.getInterior());
 			insertBBL.execute();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see net.skyebook.padloader.database.DatabaseInterface#findRecord(net.skyebook.padloader.database.ADRQuery)
 	 */
 	@Override
 	public List<ADRRecord> findRecord(ADRQuery query) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.skyebook.padloader.database.DatabaseInterface#findRecord(net.skyebook.padloader.database.BBLQuery)
 	 */
 	@Override
 	public List<BBLRecord> findRecord(BBLQuery query) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private static String createShortArray(short[] shortArray){
		if(shortArray==null) return "0:";
		
 		StringBuilder stringArray = new StringBuilder();
 		stringArray.append(shortArray.length+":");
 		for(int i=0; i<shortArray.length; i++){
 			stringArray.append(shortArray[i]+";");
 		}
 		return stringArray.toString();
 	}
 
 	private static short[] extractShortArray(String stringArray){
 		// get the size of the array
 		short[] shortArray = new short[Integer.parseInt(stringArray.substring(0, stringArray.indexOf(":")))];
 		stringArray = stringArray.substring(stringArray.indexOf(":")+1);
 		for(int i=0; i<shortArray.length; i++){
 			short value = Short.parseShort(stringArray.substring(0, stringArray.indexOf(";")));
 			shortArray[i] = value;
 			stringArray = stringArray.substring(stringArray.indexOf(";")+1);
 		}
 
 		return shortArray;
 	}
 
 }
