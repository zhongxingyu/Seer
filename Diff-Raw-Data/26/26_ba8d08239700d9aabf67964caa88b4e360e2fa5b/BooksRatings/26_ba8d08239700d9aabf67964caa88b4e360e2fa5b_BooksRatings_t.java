 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class BooksRatings {
 
 	/**
 	 * @param args
 	 */
 	private List<Integer> outputs;
 	
 	public void launcher() throws Exception {
 		this.outputs = new ArrayList<Integer>();
 		this.outputs.add(0);
 		this.outputs.add(0);
 		this.outputs.add(0);
 		
 		//prepare ArrayList 
 		List<Integer> USERS_IDS = new ArrayList<Integer>();
 		List<Item> U1_ORIGINAL_ARRAY = new ArrayList<Item>();
 		List<Item> U2_ORIGINAL_ARRAY = new ArrayList<Item>();
 		
 		/**
 		 * Connection to MySQL  
 		 */
 		Mysqler newsql = new  Mysqler();
 		Connection conn = newsql.connect(); //create a connection to mysql
 		
 		/*
 		 * Get distinct users ids and put them in memory in an ArrayList to loop it
 		 * The IDs are sorted in an ASC order => by the SQL query
 		 */ 		 
 		ResultSet rs1 = newsql.make_query(conn, "SELECT DISTINCT (`User-ID`) FROM `BX-Book-Ratings` ORDER BY `User-ID` DESC");
 		while (rs1.next()) { //get distinct user IDs and store them in USERS_IDS arraylist
 			Integer User_ID = rs1.getInt("User-ID"); //System.out.println("User-ID: " + User_ID);
 		    USERS_IDS.add(User_ID);
 		}
		
	    if (rs1 != null) {
	        try {
	            rs1.close();
	        } catch (Exception sqlEx) { } // ignore
	        rs1 = null;
	    }
	    
 //		int rows_limit = USERS_IDS.size(); // 105283 users ;) perfectly working ... 105283 left joins!! my pc will be dead!!
 //		System.out.println("[Number of distinct users] : "+rows_limit);
 
 		int sickness_limiter = 1; //used when i'm sick waiting this to end... i limit loops and break the hell out of here ;) :p
 		//2 for loops to get the checks as:  //1 2 ; 1 3 ; 1 4 ; .....; 2 1; 2 3; 2 4 ; ...
 		for(int i = 0 ;  i  <  USERS_IDS.size()-2 ;  i++  ){
 			for(int j=i+1; j< USERS_IDS.size()-1; j++){
 				//We got the list of all users ID in memory, now we will select 2 of them and dump the values in 2 array 
 				//the result arrays will be used in the transform method
 				int ID1 = USERS_IDS.get(i);
 				int ID2 = USERS_IDS.get(j);
 				
 				ResultSet rs2 = newsql.make_query(conn, "SELECT a.`User-ID` , a.ISBN, a.`Book-Rating` as rating_user1 , b.`User-ID` , b.ISBN, b.`Book-Rating` as rating_user2 "+ 
 														"FROM  `BX-Book-Ratings` a "+
 														"LEFT JOIN  `BX-Book-Ratings` b ON ( a.ISBN = b.ISBN ) "+ 
 														"WHERE a.`User-ID` ="+ID1+" AND b.`User-ID` ="+ID2);//USERS_IDS.get(j));
 				
 				while (rs2.next()) { //cross/loop the ResultSet for this 2 users
 					  int counter = 1;
 					  Integer rating_user1 = rs2.getInt("rating_user1");
 					  Integer rating_user2 = rs2.getInt("rating_user2");
 					  Item a = new Item(counter, rating_user1);
 					  Item b = new Item(counter, rating_user2);
 					  U1_ORIGINAL_ARRAY.add(a); 
 					  U2_ORIGINAL_ARRAY.add(b);
 					  counter++;
 				 } //end while  <=> the internal for loop for the Jester data set
			    if (rs2 != null) {
			        try {
			            rs2.close();
			        } catch (Exception sqlEx) { } // ignore
			        rs2 = null;
			    }
 			} //end internal for
 			Percentiler p = new Percentiler();
 			List<Integer> trans1output = new ArrayList<Integer>();
 			trans1output = p.transform(U1_ORIGINAL_ARRAY, U2_ORIGINAL_ARRAY);			
 			List<Integer> trans2output = new ArrayList<Integer>();
 			trans2output = p.transform(U2_ORIGINAL_ARRAY, U1_ORIGINAL_ARRAY);
 			this.outputs.set(0, (trans1output.get(0) + trans2output.get(0) + this.outputs.get(0) ));
 			this.outputs.set(1, (trans1output.get(1) + trans2output.get(1) + this.outputs.get(1) ));
 			this.outputs.set(2, (trans1output.get(2) + trans2output.get(2) + this.outputs.get(2) ));
 			sickness_limiter++;
			if(sickness_limiter > 100) break;
 		} //end external for
 	System.out.println("-------------\nTotals: \nEqulas:"+this.outputs.get(0)+" \nHits:"+this.outputs.get(1)+"\nMisses:"+this.outputs.get(2));
 		
 	}
 	public static void main(String[] args) throws Exception {
 		// TODO Auto-generated method stub
 		long start = System.currentTimeMillis(); 	//Monitor the execution time - Start
 		BooksRatings bookratings = new BooksRatings();
 		bookratings.launcher();
 		long end = System.currentTimeMillis();		//Monitor the execution time - End
 		System.out.println("Execution time was "+(end-start)+" ms.");
 	}
 
 }
