 package util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.Scanner;
 
 public class HexReader {
 
 	private final static LinkedHashMap<String, String> months = new LinkedHashMap<String, String>();
 	private static Connection con;
 	private static Statement statement;
 	private static final DateFormat DATEFORMAT = new SimpleDateFormat(
 			"dd/MM/yyyy");
 	private static final String DIRECTORY = "\\\\pearl\\temp\\adc-jcu2012";
 
 	static {
 		// initalize LinkedHashMap
 		months.put("jan", "01");
 		months.put("feb", "02");
 		months.put("mar", "03");
 		months.put("apr", "04");
 		months.put("may", "05");
 		months.put("jun", "06");
 		months.put("jul", "07");
 		months.put("aug", "08");
 		months.put("sep", "09");
 		months.put("oct", "10");
 		months.put("nov", "11");
 		months.put("dec", "12");
 	}
 
 	private static boolean DEBUG = true;
 	private String serialNo;
 	private Date calibrationDate;
 
 	private File file;
 
 	public HexReader(File file) {
 		this.file = file;
 	}
 
 	public void run() {
 
 		try {
 
 			Scanner scanner = new Scanner(file);
 
 			int count = 1;
 
 			while (scanner.hasNextLine() && count++ < 10) {
 				String line = scanner.nextLine();
 				if (line.startsWith("* Temperature")) {
 					serialNo = line.split("=")[1].trim();
 					if (DEBUG) {
 						System.out.println(line);
 						System.out.println(serialNo);
 					}
 
 				}
 
 				if (line.startsWith("* System UpLoad Time")) {
 					calibrationDate = formatDate(line.split("=")[1].trim());
 					if (DEBUG) {
 						System.out.println(line);
 						System.out.println("Date: "
 								+ calibrationDate.toString());
 					}
 
 				}
 
 			}
 			scanner.close();
 		}
 
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		try {
 			getConInfo();
 		} catch (ParseException | SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	private void getConInfo() throws ParseException, SQLException {
 		// initalize Database
 		ConnectDB db = new ConnectDB();
 		con = db.getDdConnection();
 
 		statement = con.createStatement();
 		ResultSet results = getConFile();
 
 		while (results.next()) {
 			String conFile = results.getString("Associated_con_file").replaceFirst(
 					"[.][^.]+$", "");
 			String hexFileLocation = DIRECTORY	+ "\\config\\"	+ conFile + "\\data\\raw\\";
 
 			Date startDate = new Date();
 			Date endDate = new Date();
 
 			String stringStartDate = results.getString("Start_Date");
 			String stringEndDate = results.getString("End_Date");
 
 			// Checks for null values
 			if (stringEndDate == null || stringStartDate == null) {
 				continue;
 			}
 			// Checks if there needs to be current date
 			else if (stringEndDate.toLowerCase().equals("current")) {
 				startDate = DATEFORMAT.parse(stringStartDate);
 			} else {
 				startDate = DATEFORMAT.parse(stringStartDate);
 				endDate = DATEFORMAT.parse(stringEndDate);
 			}
 
 			if (DEBUG) {
 				System.out.println(conFile + ", " + stringStartDate + ", "
 						+ stringEndDate);
 				System.out.printf("Start Date: %s%nEnd Date: %s%n",
 						DATEFORMAT.format(startDate),
 						DATEFORMAT.format(endDate));
 			}
 
 			if ((startDate.before(calibrationDate) || startDate
 					.equals(calibrationDate)) && endDate.after(calibrationDate)) {
 				if (DEBUG) {
 					System.out.println("InstrumentID is: " + conFile);
 					System.out.println("File Location is: " + hexFileLocation);
 
 					// Copies the hex to the right location. Not deleting the
 					// original
 					copyDeleteHex(hexFileLocation);
 				}
 			}
 
 		}
 		con.close();
 	}
 
 	private void copyDeleteHex(String hexFileLocation) {
 		InputStream inStream = null;
 		OutputStream outStream = null;
 
 		try {
 			inStream = new FileInputStream(file);
 			outStream = new FileOutputStream(new File(hexFileLocation
 					+ file.getName()));
 
 			byte[] buffer = new byte[1024];
 			int length;
 
 			// copy the file content in bytes
 			while ((length = inStream.read(buffer)) > 0) {
 				outStream.write(buffer, 0, length);
 			}
 
 			inStream.close();
 			outStream.close();
 			
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
	 * Formats the date to the same way as it is in the database
 	 * 
 	 * @param badDate
 	 * @return
 	 */
 	private Date formatDate(String badDate) {
 		String[] dateSplit = badDate.split(" ");
 		String day = dateSplit[1];
 		String month = months.get(dateSplit[0].toLowerCase());
 		String year = dateSplit[2];
 
 		if (DEBUG) {
 			System.out.printf("Day: %s, Month: %s, Year: %s%n", day, month,
 					year);
 		}
 
 		try {
 			Date date = DATEFORMAT.parse(day + "/" + month + "/" + year);
 			return date;
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 
 		// return String.format("%s/%s/%s", day, month, year);
 	}
 
 	private ResultSet getConFile() {
 		try {
 			String sql = String
 					.format("SELECT * FROM Instrument_Calibration as IC where IC.Serial_no = '%s'",
 							serialNo);
 			ResultSet rs = statement.executeQuery(sql);
 			return rs;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public Date getCalibrationDate() {
 		return calibrationDate;
 	}
 
 	public String getSerialNo() {
 		return serialNo;
 	}
 
 //	public static void main(String args[]) {
 //		HexReader reader = new HexReader(
 //				"\\\\pearl\\temp\\adc-jcu2012\\ctd\\GB12071.hex");
 //		reader.run();
 //	}
 }
