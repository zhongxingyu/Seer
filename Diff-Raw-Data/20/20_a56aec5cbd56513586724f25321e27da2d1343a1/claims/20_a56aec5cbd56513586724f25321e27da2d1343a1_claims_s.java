 package ntu.im.bilab.panda.kobuta;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Iterator;
 
 public class claims {
 	static final String DRIVER = "com.mysql.jdbc.Driver";
 	static final String DATABASE_URL = "jdbc:mysql://140.112.107.207/mypaper";   //which database
 	static final String DATABASE_URL2 = "jdbc:mysql://140.112.107.207/patent_value";   //which database
 	static final String USERNAME = "root";
 	static final String PASSWORD = "123456";
 	
 	static int OLDEST_YEAR = 1976;
 	static int YOUNGEST_YEAR = 2009;
 	
 	private Connection connection = null;
 	private Statement statement = null;
 	private ResultSet resultSet = null; 
 	private Connection connection2 = null;
 	private Statement statement2 = null;
 	private ResultSet resultSet2 = null; 
 	
 	private String result_claim;
 	private String result_description;
 	private String[] claim;
 	
 	private int NumOfClaim = 0;
 	private int NumOfIndepClaim = 0;
	private int NumOfDepClaim =0;
	private int LengthOfDescription =0;
	private float AveLength_IndepClaim= 0;
 	private int Length_IndepClaim = 0;
 
 	public void Open(){
 		try {
 			Class.forName( DRIVER );
 			connection = DriverManager.getConnection( DATABASE_URL, USERNAME, PASSWORD );
 			statement = connection.createStatement();
 			
 		}//end try 
 		catch( SQLException sqlException )
 		{
 			sqlException.printStackTrace();
 			System.exit(1);
 		}//end catch
 		catch ( ClassNotFoundException classNotFound ) 
 		{
 			classNotFound.printStackTrace();
 		}//end catch
 	}
 	public void Close(){
 	    try
 	    {
 	      if(connection!=null)
 	      {
 	        connection.close();
 	        connection = null;
 	      }
 	    }
 	    catch(SQLException e)
 	    {
 	      System.out.println("Close Exception :" + e.toString());
 	    }
 	  }	
 	public void Open2(){
 		try {
 			Class.forName( DRIVER );
 			connection2 = DriverManager.getConnection( DATABASE_URL2, USERNAME, PASSWORD );
 			statement2 = connection2.createStatement();
 			
 		}//end try 
 		catch( SQLException sqlException )
 		{
 			sqlException.printStackTrace();
 			System.exit(1);
 		}//end catch
 		catch ( ClassNotFoundException classNotFound ) 
 		{
 			classNotFound.printStackTrace();
 		}//end catch
 	}
 	public void Close2(){
 		    try
 		    {
 		      if(connection2!=null)
 		      {
 		        connection2.close();
 		        connection2 = null;
 		      }
 		    }
 		    catch(SQLException e)
 		    {
 		      System.out.println("Close Exception :" + e.toString());
 		    }
 	 }
 
 	public void GetTable(String pid){//search table content_1976-2009
 		int year;
 		for(year = OLDEST_YEAR ; year <= YOUNGEST_YEAR ; year++)
 		{
 			try
 			{
 				String selectSQL = "SELECT `Claims`, `Description` FROM `content_"+ year +"` WHERE `Patent_id`='"+pid+"'";
 				statement = connection.createStatement();
 				resultSet = statement.executeQuery(selectSQL);	
 				if(resultSet.absolute(1)== true){
 			    	//focal_year = year;
 					break;		    
 				}
 				
 			}
 			catch(SQLException e){
 				System.out.println(e.toString());
 			}		
 		}
 		
 	}
 	public void Count_Description() throws SQLException{
 		if( resultSet.absolute(1) != false){
 			result_description = resultSet.getString("Description");
 			String[] des = result_description.split(" ");
 			LengthOfDescription = des.length;
 		}
 		else{
 			LengthOfDescription = -1;
 		}
 	}
 	public void Count_DepClaim(){
 		int counter= 0;
 		for (int i=1;i<claim.length;i++){ //row 0 isn't claim description
 			if(claim[i].contains("claim") || claim[i].contains("Claim")){ //this is dependent claim
 				counter++;
 			}
 			else { //if is independent claim, count the length of all IndepClaims.
 				String[] IndepClaim = claim[i].split(" ");
 				Length_IndepClaim = Length_IndepClaim + IndepClaim.length;
 			}
 		}
 		NumOfDepClaim = counter;
 	}
 
 	public void ExtractClaims( String PatentID) throws SQLException{
 		
 		GetTable(PatentID);
 		if( resultSet.absolute(1) != false){
 			result_claim = resultSet.getString("Claims");
 			if(result_claim == null || result_claim.equals("")){ //focal patent didn't have claim info.
 				NumOfClaim = 0;
 				NumOfIndepClaim = 0;
 				NumOfDepClaim = 0;
 				AveLength_IndepClaim = 0;
 				//System.out.println("database doesn't have Patent "+PatentID+"'s claim content");
 			}
 			else{
 				claim = result_claim.split("[0-9]+\\.");
 				NumOfClaim = claim.length-1;
 				if(NumOfClaim != 0){
 					Count_DepClaim();
 					NumOfIndepClaim = NumOfClaim - NumOfDepClaim;
 					AveLength_IndepClaim = (float)Length_IndepClaim  / NumOfIndepClaim ;
 					Length_IndepClaim =0;
 				}
 				else{
 					NumOfIndepClaim = 0;
 					NumOfDepClaim = 0;
 					AveLength_IndepClaim = 0;
 				}
 			}
 		}
 		else{
 			NumOfClaim = -1;
 			NumOfIndepClaim = -1;
 			NumOfDepClaim = -1;
 			AveLength_IndepClaim = -1;
 		}
 		
 		
 	
 	}
 	public void DBUpdate()throws SQLException{
 		Open();
 		Open2();
 		String selectSQL2 = "SELECT Patent_id FROM attacker_expert";
 		resultSet2 = statement2.executeQuery(selectSQL2);			    
 		List<String> patent_id_list = new LinkedList<String>(); 
 				
 		while(resultSet2.next()) {
 			patent_id_list.add(resultSet2.getString("Patent_id").trim());
 		}
 				
 		Iterator<String> pItr = patent_id_list.iterator();
 		while(pItr.hasNext()){
 		String patentID = pItr.next();
 		System.out.println("For patent "+patentID +": ");
 		ExtractClaims(patentID);
 		Count_Description();
 		//System.out.println(AveLength_IndepClaim);
 		statement2.executeUpdate("UPDATE attacker_expert SET num_of_claims ='"+NumOfClaim+"' WHERE Patent_id = '"+patentID+"'");
 		//statement2.executeUpdate("UPDATE licensability_negative SET num_of_claims ='"+NumOfClaim+"', num_of_indep_claims = '"+ NumOfIndepClaim +"', num_of_dep_claims ='"+NumOfDepClaim+"', ave_length_of_indep_claims = '"+AveLength_IndepClaim+"' WHERE Patent_id = '"+patentID+"'");
 		// num_of_dep_claims ='"+NumOfDepClaim+"', 
 		
 		}//end while
 		System.out.println("Program End");
 		
 		Close();
 		Close2();
 	}
 	public static void main(String[] args) throws SQLException {
 		//String patentID = "RE29093";
 		claims cl = new claims();
 		cl.DBUpdate();
 		/*cl.Open();
 		cl.Open2();
 		cl.ExtractClaims("D403674");
 		cl.Close();
 		cl.Close2();*/
 	}
 }
