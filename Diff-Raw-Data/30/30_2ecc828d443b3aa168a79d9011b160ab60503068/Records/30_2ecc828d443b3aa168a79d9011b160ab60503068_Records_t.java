 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.EOFException;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * This class stores information about the user's record
  * @author Naoki Mizuno
  * 
  */
 // TODO: Add "mode" parameter
 
 public class Records {
 	private DataInputStream in;
 	private DataOutputStream out;
 	private String recordsFileName;
 	
 	private ArrayList<String> existingKeyboardLayouts = new ArrayList<String>();
 	private ArrayList<Result> records = new ArrayList<Result>();
 	
 	public Records(String recordsFileName) {
 		this.recordsFileName = recordsFileName;
 	}
 	
 	/**
 	 * Reads the user's records from the binary file. The binary file consists of:
 	 * double (time), int (miss), String (user name), String (keyboard layout), long (date added)
 	 * The last "long" should then be converted to Calendar using new Calendar(new Date(long_data))
 	 * It stores the records into an ArrayList of arrays, which will be used when showing the list of records
 	 * in the showListOfRecords method.
 	 */
 	public void readRecords() {
 		try {
 			in = new DataInputStream(new FileInputStream(recordsFileName));
 			// Make an infinite loop because it will throw an exception and be caught when reaching EOF
 			while (true) {
 				Result result = new Result(in.readDouble(), in.readInt(),
 						in.readUTF(), in.readUTF(), in.readLong());
 				// DEBUG
 				//System.out.println("Read:");
 				//System.out.println("  Time: " + result.getTime());
 				//System.out.println("  Miss: " + result.getMiss());
 				//System.out.println("  User: " + result.getUserName());
 				//System.out.println("  Layout: " + result.getKeyboardLayout());
 				//System.out.println("  Date: " + result.getDate().getTime());
 				//System.out.println();
 				
 				// Also, add to the ArrayList that keeps track of the keyboard layouts at this time.
 				if (!existingKeyboardLayouts.contains(result.getKeyboardLayout()))
 					existingKeyboardLayouts.add(result.getKeyboardLayout());
 				
 				records.add(result);
 			}
 		}
 		catch (FileNotFoundException e) {
 			// When there's not file found
 			return;
 		}
 		catch (EOFException e) {
 			// Do nothing when reaching the end of file
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Writes the record to a binary file with the name {user name}_records.dat
 	 * The schema is double, int, String, String, long.
 	 * When the file already exists, the results will be appended by re-writing all the previous results
 	 * that is stored in the array at the beginning when reading.
 	 * This uses method overloading and gives the parameters to the writeRecords method that accepts Result
 	 * instance as a parameter.
 	 * @param time The time in double that the user took to type all the words.
 	 * @param miss The miss types the user had.
 	 * @param userName The user name in case of records list of all the existing users.
 	 * @param date The date and time the record was added.
 	 * @param keyboardLayout The keyboard layout used when this record was created.
 	 */
 	public void writeRecords(double time, int miss, String userName, String keyboardLayout, long date) {
 		Result result = new Result(time, miss, userName, keyboardLayout, date);
 		writeRecords(result);
 	}
 	
 	/**
 	 * Write the result to a binary file.
 	 * Accepts a Result instance and write that result to the binary file named {user name}_records.dat
 	 * This method first adds the given result to the ArrayList of Result, and then writes the whole
 	 * ArrayList, overwriting if a file with the same name exists.
 	 * @param result A Result instance representing the result of a round.
 	 */
 	public void writeRecords(Result result) {
 		
 		try {
 			out = new DataOutputStream(new FileOutputStream(recordsFileName));
 			
 			// Write the results in the ArrayList (which contains the result of this round)
 			records.add(result);
 			for (int i = 0; i < records.size(); i++) {
 				out.writeDouble(records.get(i).getTime());
 				out.writeInt(records.get(i).getMiss());
 				out.writeUTF(records.get(i).getUserName());
 				out.writeUTF(records.get(i).getKeyboardLayout());
 				out.writeLong(records.get(i).getDate().getTimeInMillis());
 			}
 			
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Returns the ArrayList of keyboard layouts that the user has used in the past.
 	 * It should be used in the changeKeyboardLayout() method in the MainTypePanel class, for showing
 	 * a list of the keyboard layouts when showing the dialog for changing the keyboard layout.
 	 * @return An ArrayList containing the keyboard layouts that has been used in String.
 	 */
 	public ArrayList<String> getExistingKeyboardLayouts() {
 		return existingKeyboardLayouts;
 	}
 	
 	/**
 	 * Shows a list of records using the ShowRecords class, which will display a new JFrame that contains
 	 * the time, miss, user, keyboard layout, and date of all the records that is stored in the file.
 	 * TODO: Overload with String representing filter (e.g. "username" will show only the specified user name)
 	 */
 	public void showListOfRecords() {
 		ShowRecords list = new ShowRecords(records);
 	}
 	
 }
