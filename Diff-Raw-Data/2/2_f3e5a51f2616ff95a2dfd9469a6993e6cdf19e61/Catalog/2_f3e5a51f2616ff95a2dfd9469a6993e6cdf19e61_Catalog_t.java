 package waba.io;
 
 /*
 Copyright (c) 1998, 1999 Wabasoft  All rights reserved.
 
 This software is furnished under a license and may be used only in accordance
 with the terms of that license. This software and documentation, and its
 copyrights are owned by Wabasoft and are protected by copyright law.
 
 THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
 AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
 OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
 INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
 SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.
 
 WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
 MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
 ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
 YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
 */
 
 import java.util.Hashtable;
 import java.util.Vector;
 import waba.sys.Vm;
 
 /**
  * Catalog is a collection of records commonly referred to as a database
  * on small devices.
  * <p>
  * Here is an example showing data being read from records in a catalog:
  *
  * <pre>
  * Catalog c = new Catalog("MyCatalog", Catalog.READ_ONLY);
  * if (!c.isOpen())
  *   return;
  * int count = c.getRecordCount();
  * byte b[] = new byte[10];
  * for (int i = 0; i < count; i++)
  *   {   
  *   c.setRecord(i);
  *   c.readBytes(b, 0, 10);
  *   ...
  *   }
  * c.close();
  * </pre>
  * Notes added by guich@120
  * Catalog now reads from and writes to .pdb files, so you can do a full test of your 
  * database application. 
  * The path of the database is known by the appPath parameter in the html file.
  */
 
 public class Catalog extends Stream
 {
 /** Read-only open mode. */
 public static final int READ_ONLY  = 1;
 /** Write-only open mode. */
 public static final int WRITE_ONLY = 2;
 /** Read-write open mode. */
 public static final int READ_WRITE = 3; // READ | WRITE
 /** Create open mode. Used to create a database if one does not exist. */
 public static final int CREATE = 4;
 
 private boolean _isOpen;
 private String _name;
 private int _mode;
 public static Hashtable _dbHash = new Hashtable();
 private Vector _records = new Vector();
 private int _recordPos;
 private int _cursor;
 private String _creator; // guich@120
 private String _type; // guich@120
 private boolean _modified; // guich@120
 
 /**
  * Opens a catalog with the given name and mode. If mode is CREATE, the 
  * catalog will be created if it does not exist.
  * <p>
  * For PalmOS: A PalmOS creator id and type can be specified by appending
  * a 4 character creator id and 4 character type to the name seperated
  * by periods. For example:
  * <pre>
  * Catalog c = new Catalog("MyCatalog.CRTR.TYPE", Catalog.CREATE);
  * </pre>
  * Will create a PalmOS database with the name "MyCatalog", creator id
  * of "CRTR" and type of "TYPE".
  * <p>
  * If no creator id and type is specified, the creator id will default
  * to the creator id of current waba program and the type will default
  * to "DATA".
  * <p>
  * <br>You must close the catalog to write the data to pdb file on disk!
  * <br><b>Note</b>: since a bug in the original Waba vm, the <i>TYPE</i> 
  * <b>must</b> be identical to <i>MyCatalog</i> and they must have 4 letters. 
  * If you don't follow this rules and you write a conduit to talk with the waba 
  * created database, you will never find it with the conduit! (guich@120)
  * <p>
  * Under PalmOS, the name of the catalog must be 31 characters or less,
  * not including the creator id and type. Windows CE supports a 32
  * character catalog name but to maintain compatibility with PalmOS,
  * you should use 31 characters maximum for the name of the catalog.
  * @param name catalog name
  * @param mode one of READ_ONLY, WRITE_ONLY, READ_WRITE or CREATE
  */
 
 public Catalog(String name, int mode)
 {
 	// parse name
 	java.util.StringTokenizer st = new java.util.StringTokenizer(name,".",false);
 	if (st.countTokens() != 3) 
 	{
 		System.out.println("Invalid catalog name: "+name+". It must be in the form MyCatalog.CRTR.TYPE (see documentation)"); 
 		return;
 	}
 	_name = st.nextToken();
 	_creator = st.nextToken();
 	_type = st.nextToken();
 	_mode = mode;
    
 	if (mode != CREATE) // READ_ONLY, WRITE_ONLY, READ_WRITE
 	{
 		// read the catalog from disk
 		java.io.InputStream is = Vm.openInputStream(getFileName());
 		if (is == null) 
 		{
 		   System.out.println("file "+name+" not found");
 		   return;
 		}
 		try
 		{
    		_records = fromPDB(is,_creator,_type);
    	} catch (Exception e) {e.printStackTrace();}
 	}
 	_dbHash.put(name, this);
    _isOpen = true;
 }
 
 private int _readWriteBytes(byte buf[], int start, int count, boolean isRead)
 {
 	if (_recordPos == -1 || (start < 0 || count < 0) || (start + count > buf.length))
 	   return -1;
 	if ((_mode == READ_ONLY && !isRead) || (_mode == WRITE_ONLY && isRead)) 
 	{
 	   System.out.println("Invalid operation at catalog for this mode");
 	   return -1;
 	}
 
 	byte rec[] = (byte[])_records.elementAt(_recordPos);
 	if (_cursor + count > rec.length)
 		return -1;
 	if (isRead) // guich@120: System.arraycopy is faster than a for loop.
 	   System.arraycopy(rec,_cursor,buf,start,count);
 	else
 	   System.arraycopy(buf,start,rec,_cursor,count);
 	if (!isRead) _modified = true;
 	_cursor += count;
 	return count;
 }
 /**
  * Adds a record to the end of the catalog. If this operation is successful,
  * the position of the new record is returned and the current position is
  * set to the new record. If it is unsuccessful the current position is
  * unset and -1 is returned.
  * @param size the size in bytes of the record to add
  */
 
 public int addRecord(int size)
 {
 	if (!_isOpen)
 		return -1;
 	_recordPos = _records.size();
 	_records.addElement(new byte[size]);
 	//System.out.println("Catalog: added record " + _recordPos +
 	//	" (" + size + " bytes)");
 	_cursor = 0;
 	return _recordPos;
 }
 /**
  * Adds a record to the <pos> position of the catalog. If this operation is successful,
  * the position of the new record is returned and the current position is
  * set to the new record. If it is unsuccessful the current position is
  * unset and -1 is returned.
  * implemented by guich (guich@email.com) in 06/30/2000.
  * @param size the size in bytes of the record to add
  */
 public int addRecord(int size, int pos)	
 {
 	if (!_isOpen || size < 0)
 		return -1;
 	
 	try 
 	{
 		_records.insertElementAt(new byte[size], pos);
 	} 
 	catch (ArrayIndexOutOfBoundsException e) 
 	{
 		return -1;
 	}
 	_recordPos = pos;
 	_cursor = 0;
 	return _recordPos;
 	}
 
 /** returns the file name of this catalog */
 // guich@120
 public String getFileName()
 {
    return _name+".PDB";//+_creator+"."+_type;
 }
 
 /**
  * Closes the catalog: writtes all information back to the pdb file.  
  * Returns true if the operation is successful and false otherwise.
  */
 
 public boolean close()
 {
 //	System.out.println("Catalog: closing " + _name);  
 	if (!_isOpen)
 		return false;
 	if(_mode == CREATE){
 	   java.io.InputStream is = Vm.openInputStream(getFileName());
 	   if(is == null){
 	       java.io.OutputStream os = Vm.openOutputStream(getFileName());
 	       try{
 		   os.close();
 	       } catch (Exception e) {e.printStackTrace();}
 	   } else {
 	       try{
 		   is.close();
 	       } catch (Exception e) {e.printStackTrace();}
 	   }
 	} else if (_modified) // guich@120 - write only if modified
    {
 	   java.io.OutputStream os = Vm.openOutputStream(getFileName());
 	   if (os == null) 
 	   {
 	      System.out.println("could not write pdb when closing catalog "+_name+"!");
 	      return false;
 	   }
 	   try
 	   {
    	   toPDB(os,_records,_creator,_name,_type);
       } catch (Exception e) {e.printStackTrace();}
 	}
 	_isOpen = false;
 	_recordPos = -1;
 	return true;
 	}
 /**
  * Deletes all the records of the catalog. Returns true if the operation is successful and false
  * otherwise. Note that this behavior is diferent from palm os, because in applets we cannot erase files at the server.
  */
 
 public boolean delete()
 {
 	if (!_isOpen)
 		return false;
 	_dbHash.remove(_name);
    
    java.io.OutputStream os = Vm.openOutputStream(getFileName());
    _records.setSize(0); // erases the vector
 	_isOpen = false;
 	_recordPos = -1;
    if (os == null)
       return false;
 	try
 	{
    	toPDB(os,_records,_creator,_name,_type);
    } catch (Exception e) {e.printStackTrace();}
    return true;
 }
 /**
  * Deletes the current record and sets the current record position to -1.
  * The record is immediately removed from the catalog and all subsequent
  * records are moved up one position.
  */
 
 public boolean deleteRecord()
 {
 	if (_recordPos == -1)
 		return false;
 	_records.removeElementAt(_recordPos);
 	_recordPos = -1;
 	return true;
 }
 /**
  * Returns the number of records in the catalog or -1 if the catalog is not open.
  */
 
 public int getRecordCount()
 {
 	if (!_isOpen)
 		return -1;
 
 	return _records.size();
 }
 /**
  * Returns the size of the current record in bytes or -1 if there is no
  * current record.
  */
 
 public int getRecordSize()
 {
 	if (_recordPos == -1)
 		return -1;
 //	System.out.println("Catalog: get size for record " + _recordPos);
 	byte rec[] = (byte[])_records.elementAt(_recordPos);
 	return rec.length;
 }
 /** Inspects a record. use this method with careful, none of the params are checked for validity.
  * the cursor is not advanced, neither the current record position. this method must be used
  * only for a fast way of viewing the contents of a record,
  * like searching for a specific header or filling a grid of data.
  * <i>buf.length</i> bytes (at maximum) are readen from the record into <i>buf</i>.
  * Returns the number of bytes read (can be different of buf.length if buf.length is greater
  * than the record size) or -1 if an error prevented the read operation from occurring. added by guich*/
 public int inspectRecord(byte buf[], int recPosition) 
 {
 	byte rec[] = (byte[]) _records.elementAt(recPosition);
 	int count = Math.min(buf.length, rec.length);
 	System.arraycopy(rec, 0, buf, 0, count);
 	return count;
 }
 /**
  * Returns true if the catalog is open and false otherwise. This can
  * be used to check if opening or creating a catalog was successful.
  */
 
 public boolean isOpen()
 {
 	return _isOpen;
 }
 /**
  * Returns the complete list of oppened catalogs in this vm instance. If no catalogs exist, this
  * method returns null.
  */
 
 public static String []listCatalogs()
 {
 	java.util.Enumeration keys = _dbHash.keys();
 	int n = 0;
 	while (keys.hasMoreElements())
 		{
 		keys.nextElement();
 		n++;
 		}
 	if (n == 0)
 		return null;
 	String names[] = new String[n];
 	n = 0;
 	keys = _dbHash.keys();
 	while (keys.hasMoreElements())
 		names[n++] = (String)keys.nextElement();
 	return names;
 }
 /**
  * Reads bytes from the current record into a byte array. Returns the
  * number of bytes actually read or -1 if an error prevented the
  * read operation from occurring. After the read is complete, the location of
  * the cursor in the current record (where read and write operations start from)
  * is advanced the number of bytes read.
  * @param buf the byte array to read data into
  * @param start the start position in the array
  * @param count the number of bytes to read
  */
 
 public int readBytes(byte buf[], int start, int count)
 {
 	return _readWriteBytes(buf, start, count, true);
 }
 /**
  * Resizes a record. This method changes the size (in bytes) of the current record.
  * The contents of the existing record are preserved if the new size is larger
  * than the existing size. If the new size is less than the existing size, the
  * contents of the record are also preserved but truncated to the new size.
  * Returns true if the operation is successful and false otherwise.
  * @param size the new size of the record
  */
 
 public boolean resizeRecord(int size)
 {
 	if (_recordPos == -1)
 		return false;
 
 	if(size > (1 << 16)){
 		System.err.println("Catalog: trying to resize record(" 
 						   +  _recordPos + ")larger than 64k");
 		return false;
 	}
 
 	byte oldRec[] = (byte[])_records.elementAt(_recordPos);
 	byte newRec[] = new byte[size];
 	int copyLen;
 	if (oldRec.length < newRec.length)
 		copyLen = oldRec.length;
 	else
 		copyLen = newRec.length;
 	System.arraycopy(oldRec, 0, newRec, 0, copyLen);
 	_records.setElementAt(newRec, _recordPos);
 	// _cursor = 0; guich@120
 	return true;
 }
 /**
  * Sets the current record position and locks the given record. The value
  * -1 can be passed to unset and unlock the current record. If the operation
  * is succesful, true is returned and the read/write cursor is set to
  * the beginning of the record. Otherwise, false is returned.
  */
 
 public boolean setRecordPos(int pos)
 {
 	if (pos < 0 || pos >= _records.size())
 	{
 		_recordPos = -1;
 		return false;
 	}
 	_recordPos = pos;
 	_cursor = 0;
 	//System.out.println("Catalog: setting record position to " + _recordPos);
 	return true;
 }
 /**
  * Advances the cursor in the current record a number of bytes. The cursor
  * defines where read and write operations start from in the record. Returns
  * the number of bytes actually skipped or -1 if an error occurs.
  * @param count the number of bytes to skip
  */
 
 public int skipBytes(int count)
 {
 	if (_recordPos == -1)
 		return -1;
 	byte rec[] = (byte[])_records.elementAt(_recordPos);
 	if (_cursor + count > rec.length)
 		return -1;
 //	System.out.println("Catalog: skipping ahead " + count + " bytes");
 	_cursor += count;
 	return count;
 }
 /**
  * Writes to the current record. Returns the number of bytes written or -1
  * if an error prevented the write operation from occurring.
  * After the write is complete, the location of the cursor in the current record
  * (where read and write operations start from) is advanced the number of bytes
  * written.
  * @param buf the byte array to write data from
  * @param start the start position in the byte array
  * @param count the number of bytes to write
  */
 
 public int writeBytes(byte buf[], int start, int count)
 {
 	return _readWriteBytes(buf, start, count, false);
 }
 
 /** converts the records to a pdb file. @param records a vector of array of bytes */
 // added by guich@120
 public static void toPDB(java.io.OutputStream outStream, Vector records, String creatorId, String dbName, String typeId) throws Exception
 {
 	// System.out.println("Writing PDB");
 
    byte name[] = new byte[32];
   short attributes       = (short)0x0008;
    short version          = 1;
    int creationDate       = 0xb08823ad;
    int modificationDate   = 0xb08823ad;
    int lastBackupDate     = 0xb08823ad;
    int modificationNumber = 1;
    int appInfoID          = 0;
    int sortInfoID         = 0;
    byte type[]            = typeId.getBytes();
    byte creator[]         = creatorId.getBytes();
    int uniqueIDSeed       = 0;
       
    // copies the db name to inside the array <name>
    byte []bn = dbName.getBytes();
    for (int i =0; i < name.length; i++)
       name[i] = (i < bn.length)?bn[i]:(byte)0;
 
    short numRecords = (short)records.size();
    java.io.DataOutputStream os = new java.io.DataOutputStream(outStream);
 
    int offset = 80+numRecords*8;
 
    // DatabaseHdrType
    os.write(name);
    os.writeShort(attributes);
    os.writeShort(version);
    os.writeInt(creationDate);
    os.writeInt(modificationDate);
    os.writeInt(lastBackupDate);
    os.writeInt(modificationNumber);
    os.writeInt(appInfoID);
    os.writeInt(sortInfoID);
    os.write(type);
    os.write(creator);
    os.writeInt(uniqueIDSeed);
 
    // RecordListType
    int nextRecordListID = 0;
    os.writeInt(nextRecordListID);
    os.writeShort(numRecords);
    //System.out.println(" Writing " + numRecords + " records");
    for (int i = 0; i < numRecords; i++) 
    {
       os.writeInt(offset); // LocalChunkID
       os.writeByte(0); // attributes
       os.writeByte((byte)(i>>16)); // uniqueID
       os.writeByte((byte)(i>>8)); // uniqueID
       os.writeByte((byte)(i>>0)); // uniqueID
       offset += ((byte [])records.elementAt(i)).length;
    }
    os.writeShort(0); // pad
       
    for (int i=0; i < numRecords; i++) 
       os.write((byte [])records.elementAt(i));
    os.close();
 }
 
 /** reads a pdb file and returns a vector of records. added by guich@120 */
 public static Vector fromPDB(java.io.InputStream sourceStream, String creatorId, String typeId) throws Exception
 {
 	//System.out.println("Reading PDB");
 
    int available = sourceStream.available();
    //System.out.println("Avalible bytes from input: " + available);
    if(available == 0){
        return new Vector();
    }
 
    // ps: i had to read everything to an array because reading one record at a time was reading trash.
    byte []all = new byte[available];
    sourceStream.read(all);
    
    java.io.DataInputStream is = new java.io.DataInputStream(new java.io.ByteArrayInputStream(all));
 
    // DatabaseHdrType
    byte name[] = new byte[32]; // ps: the writted string is in c++ format, so this routine doesnt loads the name correctly (comes trash with it)
    byte type[] = new byte[4];
    byte creator[] = new byte[4];
 
    is.read(name);
    short attributes = is.readShort();
    short version = is.readShort();
    int creationDate = is.readInt();
    int modificationDate = is.readInt();
    int lastBackupDate = is.readInt();
    int modificationNumber = is.readInt();
    int appInfoID = is.readInt();
    int sortInfoID = is.readInt();
    is.read(type);
    is.read(creator);
    int uniqueIDSeed = is.readInt();
       
    // verify if the creatorId is valid
    if (!creatorId.equals(new String(creator)) || !typeId.equals(new String(type)))
       throw new Exception("Error: invalid pdb file!");
 
    // RecordListType
    int nextRecordListID = is.readInt();
    short numRecords = is.readShort();
    //System.out.println(" Found " + numRecords + " records");
 
    // reads the header (meaningless)
    int recOffsets[] = new int[numRecords+1];
    byte recAttributes;                   
    byte recUniqueID[] = new byte[3];
    for (int i=0; i < numRecords; i++) 
    {
       recOffsets[i] = is.readInt();  // offset
       recAttributes = is.readByte();
       is.read(recUniqueID);
    }
    recOffsets[numRecords] = available; // add the total size so we can compute the size of each record
       
    is.readShort(); // pad
    int offset = 80+numRecords*8;
       
    // the records were writted in sequence from here
    Vector v = new Vector(numRecords);
    int size=0;
    for (int i=0;  i < numRecords; i++) 
    {
       size = recOffsets[i+1] - recOffsets[i];
       byte []bytes = new byte[size];
       is.read(bytes);
       v.addElement(bytes);
       //System.out.println("record "+i+": "+size);
    }
    is.close();
       
    return v;
 }
 	public int getError(){return 0;}
 }
