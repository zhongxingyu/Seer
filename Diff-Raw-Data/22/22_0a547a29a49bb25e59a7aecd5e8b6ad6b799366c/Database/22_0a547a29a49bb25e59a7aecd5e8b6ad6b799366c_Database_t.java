 package Barramundi;
 
 import java.util.Vector;
 import java.util.Enumeration;
 import java.io.*;
 import java.lang.ClassNotFoundException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Constructor;
 import javax.swing.JFrame;
 import javax.swing.DefaultListModel;
 import javax.swing.JOptionPane;
 
 /**
 	Class to handle reading and writing of databases.  A list of selected
 	records is also mainainted as an interface with a GUI selection tool.
 	
 	Files are stored as strings (to facilitate manual editing!)
 	with a single record per line.	Fields are delimited by DELIMITER.
 	It is up to the "record type" class to define the format of the strings
 	using toString() and fromString().
 	
     Any record whose first field starts with COMMENT_CHAR is treated as 
     a comment, and is not loaded.
 
 	A database also stores which of the records are currently "selected"
 	by the user.
 	
 	@author Andrew Turpin (aturpin@discoveriesinsight.org)
 	@version 0.1 April 1999
 */
 
 public class Database {
                                            static char COMMENT_CHAR = '-';
 	/** Delimiter between fields in DB  */ String delimiter;
 	/** Records loaded from DB	        */ protected DefaultListModel list;
 	
 	                                       private String filename, recordType;
 	/** Flag if file needs to written   */ private boolean changed;
 
 	/** 
 		Load the contents of the file into "list".
 		Set selected to the first record in each database. (for no particular reason)
 		@param f Filename to read
 		@param s Class to instantiate for each record. s should implement the DatabaseRecord interface.
 		@param d Delimeter between fields on each line.  (Can be multiple chars a la StringTokenizer).
 	*/	
 	Database(String f, String r, String d) throws DatabaseException { 
 		filename = f;
 		recordType = r;
 		delimiter = d;
 		changed = false;
 
 		list = new DefaultListModel();
 		
 		try { 
 			Class c = Class.forName(recordType); 
 			Class p[] = new Class[] {Class.forName("java.lang.String"), Class.forName("java.lang.String")};
 			Constructor cons = c.getDeclaredConstructor(p);
 			
 		 	BufferedReader in = new BufferedReader(new FileReader(filename));
 			String line = in.readLine();
 			while (line != null) {
 			    if (line.charAt(0) != COMMENT_CHAR) {
 				Object params[] = {line, delimiter};
 				list.addElement(cons.newInstance(params));
 			    }
 			    line = in.readLine();
             }
         	in.close();
 	    } catch (FileNotFoundException noFile) {
            throw new DatabaseException ("Could not find database file " + filename);
         } catch (IOException e) {
            throw new DatabaseException ("IO on database file "+ filename + " " + e);
         } catch (NoSuchMethodException e) {
 	       throw new DatabaseException("Database record class " + recordType +
 					" should have a constructor " +	recordType + "(String s, String delimiter).");
         } catch (SecurityException e) {
 	        throw new DatabaseException("Spooky security database record " + e);
         } catch (InvocationTargetException e) {
             throw new DatabaseException ("Cannot invoke database record class " + recordType + " " + e +
             		"\nPerhaps the data file format does not match that described by the"+
             		"\nfromString() method of " + recordType + ", or it is not delimited correctly.");
         } catch (InstantiationException e) {
             throw new DatabaseException ("Cannot instantiate database record class " + recordType + " " + e);
         } catch (IllegalAccessException e) {
             throw new DatabaseException ("Illegal access when instantiating database record class " + recordType + " " +e);
         } catch (ClassNotFoundException e) {
              throw new DatabaseException ("Class " + recordType + " could not be found.");
         }
 	} // end constructor
 	
 	/** 
 		Return a vector of the records that are selected according to 
 		the 'selected' list.
 	*/	
 	public Vector getSelected(int indicies[]) { 
 		Vector selectedList = new Vector();
 		for(int i = 0 ; i < indicies.length ; i++)
 			selectedList.addElement(list.elementAt(indicies[i]));
 		return selectedList;
 	}// end getSelected()
 	
 	/**
 		Add record to "list" by creating a new object and then calling the edit()
 		method of that object to fill in the params.
 		@param frame Parent window to which any GUI widgets required can be attached.
 	*/
 	public void addRecord(JFrame frame) { 
 		try { 
 			Class c = Class.forName(recordType); 
			Constructor cons[] = c.getDeclaredConstructors();
			DatabaseRecord newRecord = (DatabaseRecord)cons[0].newInstance(null);
 			if (newRecord.edit(frame)) {
 				list.addElement(newRecord);
 				changed = true;
 			}
 	    } catch (Exception e) {
 	    	JOptionPane.showMessageDialog(frame,e.getMessage(),"shut",JOptionPane.ERROR_MESSAGE);
 	    }
 	}//addRecord()
 	
 	/**
 		Delete record number "i" from "list".
 		@param frame Parent window to which any GUI widgets required can be attached.
 		@param i      Index of record to delete
 	*/
 	public void deleteRecord(JFrame parentFrame, int i) {
 		try { list.remove(i); 		
 			  changed = true;
 		} catch (ArrayIndexOutOfBoundsException e) { ; }
 	}// deleteRecord()
 	
 	/**
 		Edit an existing record in "list" by calling the edit()
 		method of that object to fill in the params.
 		@param frame Parent window to which any GUI widgets required can be attached.
 		@param i      Index of record to edit
 	*/
 	public void editRecord(JFrame frame, int i) { 
 		try { 
 			DatabaseRecord record = (DatabaseRecord)list.elementAt(i);
 			if (record.edit(frame)) {
 				list.set(i, record); // forces list update
 				changed = true;
 			}
 		} catch (ArrayIndexOutOfBoundsException e) { ; }
 	}//editRecord()
 	
 	/**
 		If changed is true, this database needs to be written back to disk.
 	*/
 	public void update() throws DatabaseException {
 		if (changed)
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
 			Enumeration e = list.elements();
 			while (e.hasMoreElements()) {
 				out.write(((DatabaseRecord)(e.nextElement())).toDBString(delimiter));
 				out.write("\n");
 			}
 			out.close();
 		} catch (IOException ex) {
 			System.err.println("Trouble writing "+filename+" back to disk\n"+ex);
 		}
 	}// update()
 	
 } // end Database class
