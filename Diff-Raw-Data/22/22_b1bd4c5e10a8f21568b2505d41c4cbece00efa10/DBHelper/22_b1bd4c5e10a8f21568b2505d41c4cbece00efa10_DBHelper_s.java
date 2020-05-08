 package com.MGHWayFinder;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DBHelper extends SQLiteOpenHelper{
 
 	private static String PACKAGE = "com.MGHWayFinder";
     private static String DB_PATH = "/data/data/" + PACKAGE + "/databases/";
     private static String DB_NAME = "MGHDB.sqlite";
  
     private SQLiteDatabase db; 
     private final Context myContext;
  
     public DBHelper(Context context) {
     	super(context, DB_NAME, null, 1);
         this.myContext = context;
     }	
     
     //MAIN LOADER LOOP - CHECK'S FOR DB'S EXISTANCE, LOADS FROM ASSETS IF NEEDED
     public void createDataBase() throws IOException{
  
     	if(!checkDB()){
         	this.getReadableDatabase();
  
         	try {
     			copyDB();
     		} catch (IOException e) {
         		throw new Error("Error copying database");
         	}
     	}
     }
     
     //CHECKS FOR THE EXISTANCE OF THE DB IN THE PROGRAM'S DB FOLDER
     private boolean checkDB(){
     	SQLiteDatabase checkDB = null;
  
     	try{
     		String p = DB_PATH + DB_NAME;
     		checkDB = SQLiteDatabase.openDatabase(p, null, SQLiteDatabase.OPEN_READONLY);
     	}catch(SQLiteException e){
     	
     	}
  
     	if(checkDB != null) checkDB.close();
     	
     	return checkDB != null ? true : false;
     }
  
     //COPIES DB FROM ASSET FOLDER INTO THE PROGRAM'S DB FOLDER
     private void copyDB() throws IOException{
     	
     	String oFileName = DB_PATH + DB_NAME;
     	InputStream iStream = myContext.getAssets().open(DB_NAME);
     	OutputStream oStream = new FileOutputStream(oFileName);
     	byte[] buffer = new byte[1024];
     	int length;
     	
     	while ((length = iStream.read(buffer))>0){
     		oStream.write(buffer, 0, length);
     	}
 
     	oStream.flush();
     	oStream.close();
     	iStream.close();
     }
     
     //USED TO OPEN A DB IF IT EXISTS
     public void openDataBase() throws SQLException{
         String myPath = DB_PATH + DB_NAME;
         
     	db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
     }
     
     //CLOSES THE DB
     @Override
 	public synchronized void close() {
     	    if(db != null)
     		    db.close();
  
     	    super.close();
 	}
  
 	@Override
 	public void onCreate(SQLiteDatabase db) {
  
 	}
  
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  
 	}
  
 	
 //////////////////SQL SELECT STATEMENTS/////////////////////////
 	
 	//RETURNS ALL NIDS IN THE DB
 	public Cursor selectAllNids(){	
 		return db.rawQuery("SELECT nID FROM tblNode", null);
 	}
 	
 	//RETURNS A RECORDSET CONTAINING ALL NODES ON A GIVEN FLOOR
 	public Cursor selectFloorNodes(int floor){
 		return db.rawQuery("SELECT * FROM tblNode WHERE nFloor = " + floor, null);
 	}
 	
 	//RETURNS A RECORDSET CONTAINING ALL NEIGHBOR NODES OF THE GIVEN NODEID
 	public Cursor selectNodeNeighbors(String NodeID){
 		String w = "'" + NodeID + "'";
 		return db.rawQuery("SELECT * FROM tblNeighbors WHERE mNode = " + w, null );
 	}
  
 	//RETURNS A RECORDSET CONTAINING ALL OF THE NODES WITH A GIVEN NTYPE[]
 	public Cursor selectNodeType(int nFloor, ArrayList<String> nType){
 		String w = concatOr(nType, "nType");
 		return db.rawQuery("SELECT * FROM tblNode WHERE nType = " + w, null);
 	}
 	
 	//RETURNS THE FLOOR A GIVEN NODE IS ON
 	public int getNodeFloor(String nID){
 		int out;
 		Cursor q = db.rawQuery("SELECT nFloor FROM tblNode WHERE nID = '" + nID + "'", null);
 		q.moveToFirst();
 		out = q.getInt(0);
 		q.close();
 		return out;
 	}
 	
 	//RETURNS CONNECTING RELATIONSHIPS BETWEEN FLOORS A AND B
 	public Cursor selectInterFloor(int floorA, int floorB){									
 		return db.rawQuery(	"SELECT mNode, nNode " +
 							"FROM tblInterFloor " +
 							"INNER JOIN tblNode AS tblmNode " +
 								"ON tblmNode.nID = tblInterFloor.mNode " +
 							"INNER JOIN tblNode AS tblnNode " +
 								"ON tblnNode.nID = tblInterFloor.nNode " +
 							"WHERE (tblmNode.nFloor = " + floorA + " OR tblmNode.nFloor = " + floorB + ")" + 
 								"AND (tblnNode.nFloor = " + floorA + " OR tblnNode.nFloor = " + floorB + ")", null);
 	}
 	
 	public Cursor selectValidDestinations(){
 		return db.rawQuery("SELECT nID, nFloor, nDep FROM tblNode WHERE nType = 'Navigable' ORDER BY nFloor", null);
 	}
 	
 	//KUNAL - RETURNS ALL DEPARTMENTS - DOESNT SEEM TO BE WORKING
 	public Cursor selectAllDepartments(){	
 		return db.rawQuery("SELECT dptName FROM tblDepartment", null);
 	}
 
 	//KUNAL - RETURNS A RECORDSET CONTAINING ALL MEMBERS OF A DEPARTMENT
 	public Cursor selectDeptMembers(String department){
 		return db.rawQuery("SELECT DRLname || ', ' || drFname FROM tblDoctors WHERE drDeptName = '" + department + "'", null);
 	}
 	
 	// KUNAL - RETURNS A CURSOR OBJECT WITH THE DOCTOR'S PHONE NUMBER 
 	public Cursor selectMemberPhoneNo(String firstName, String lastName){
 		return db.rawQuery("SELECT drPhoneNumber FROM tblDoctors WHERE drFname = '" + firstName + "' AND DRLname = '" + lastName + "'", null);
 	}
 	
 	//CALUM - FOR MAIN UI
 	
 ////////////////////////////////PROGRAM METHODS////////////////////////////////////
 	
 	public Hashtable<String, String> getValidDestinations(){
 		Hashtable<String, String> out = new Hashtable<String, String>();
 		Cursor cursor;
 		String s;
 		
 		cursor = this.selectValidDestinations();
 		cursor.moveToFirst();
 		
 		while(!cursor.isAfterLast()){
 			s = "Floor ";
 			s += cursor.getString(1);
 			s += " - " + cursor.getString(2);
 			
 			out.put(s, cursor.getString(0));
 			
 			cursor.moveToNext();
 		}
 		
 		cursor.close();
 		
 		return out;
 	}
 	
 	//RETURNS AN ARRAYLIST OF VALID NIDS
 	public ArrayList<String> getAllNids(){
 		ArrayList<String> out = new ArrayList<String>();
 		Cursor cursor;
 		
 		cursor = this.selectAllNids();
 		cursor.moveToFirst();
 		
 		while(!cursor.isAfterLast()){
 			out.add(cursor.getString(0));
 			cursor.moveToNext();
 		}
 		
 		cursor.close();
 		
 		return out;
 	}
 	
 	//CONSTRUCTS AND RETURNS A HASHTABLE OF ALL NODES AND THEIR RELATIONSHIPS ON A GIVEN FLOOR
 	public Hashtable<String, Node> buildFloorNodes(int floor){
     	Node NodeM;
     	Hashtable<String, Node> ht = new Hashtable<String, Node>();
     	ArrayList<String> NodeIDs = new ArrayList<String>();
     	Cursor cursor;
     	
     	cursor = this.selectFloorNodes(floor);									//Query DB for Nodes on a given floor
         cursor.moveToFirst();
         
         while(!cursor.isAfterLast()){											//Fill Hashtable for given floor
         	NodeM = new Node(	cursor.getString(1),							//nID
         						cursor.getInt(2),								//x
         						cursor.getInt(3),								//y
         						cursor.getInt(4),								//nFloor
         						cursor.getString(5),							//nType
         						cursor.getString(6));							//nDep
         	
         	ht.put(NodeM.getNodeID(), NodeM);									//add Node to hashtable with corresponding NodeID as the key
         	NodeIDs.add(NodeM.getNodeID());										//build string of NodeID's to query neighbor table
         	
         	cursor.moveToNext();												//move to next record
         }
         
         buildNeighborNodes(ht, NodeIDs);
         cursor.close();
         return ht;
     }
     
 	//CONSTRUCTS INTERFLOOR RELATIONSHIPS
     public void buildInterFloor(int floorA, int floorB, Hashtable<String, Node> ht){
     	String stringM, stringN;
     	Node nodeM, nodeN;
     	Cursor cursor = selectInterFloor(floorA, floorB);
     	
     	cursor.moveToFirst();
     	
     	while(!cursor.isAfterLast()){
     		stringM = cursor.getString(0);
     		stringN = cursor.getString(1);
     		
     		nodeM = ht.get(stringM);								//Retrieve master Node from hashtable
         	nodeN = ht.get(stringN);								//Retrieve neighbor Node from hashtable
         	
         	nodeM.addNeighbor(nodeN);								//Add neighbor Node to master Node
         	
         	cursor.moveToNext();
     	}
     	cursor.close();
     }
 
   //KUNAL - RETURNS AN ARRAYLIST OF ALL DEPARTMENTS - FOR USE WITH SPINNER
   	public ArrayList<String> getAllDepartments(){
   		ArrayList<String> out = new ArrayList<String>();
   		Cursor cursor;
   		
   		cursor = this.selectAllDepartments();
   		cursor.moveToFirst();
   		
   		while(!cursor.isAfterLast()){
   			out.add(cursor.getString(0));
   			cursor.moveToNext();
   		}
   		
   		cursor.close();
   		
   		return out;
   	}
   	
   //KUNAL - RETURNS AN ARRAYLIST OF ALL MEMBERS WITHIN A DEPARTMENT - FOR USE WITH LISTVIEW
   	public ArrayList<String> getAllDeptMembers(String department){
   		ArrayList<String> out = new ArrayList<String>();
   		Cursor cursor;
   		
   		cursor = this.selectDeptMembers(department);
   		cursor.moveToFirst();
   		
   		while(!cursor.isAfterLast()){
   			out.add(cursor.getString(0));
   			cursor.moveToNext();
   		}
   		
   		cursor.close();
   		
   		return out;
   	}
     
 ////////////INTERNAL METHODS//////////////////////////
     
     //CONSTRUCTS RELATIONSHIPS GIVEN A TABLE OF NODES AND AN ARRAYLIST OF THEIR NEIGHBOR NIDS
     protected void buildNeighborNodes(Hashtable<String, Node> ht, ArrayList<String> nIDs){
     	String stringM, stringN;
     	Node NodeM, NodeN;
     	Cursor cursor;
     	
     	for(String it:nIDs){
     		cursor = this.selectNodeNeighbors(it);
     		
     		cursor.moveToFirst();
             
             while(!cursor.isAfterLast()){
             	stringM = cursor.getString(1);							//"Master" NodeID
             	stringN = cursor.getString(2);							//"Neighbor" NodeID
             	
             	NodeM = ht.get(stringM);								//Retrieve master Node from hashtable
             	NodeN = ht.get(stringN);								//Retrieve neighbor Node from hashtable
             	
             	NodeM.addNeighbor(NodeN);								//Add neighbor Node to master Node
             	
             	cursor.moveToNext();
             }
             cursor.close();
     	}
     }
 	
     //CONCATS AN ARRAYLIST OF STRING VARIABLES SEPERATED BY "OR"S
 	protected String concatOr(ArrayList<String> sIn, String cName){
 		String sOut = "";
 		
 		if (sIn.size() >= 1) {
 			sOut = "'" + sIn.get(0) + "'";
 			
 			if (sIn.size() > 1){
 			for(int i = 1; i < sIn.size(); i++)
 				sOut += " OR " + cName + " = " + "'" + sIn.get(i) + "'";
 			}
 		}
 		return sOut;
 	}
 	
 }
