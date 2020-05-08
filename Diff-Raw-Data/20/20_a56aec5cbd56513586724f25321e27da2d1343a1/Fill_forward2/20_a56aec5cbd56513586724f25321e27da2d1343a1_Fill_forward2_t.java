 package ntu.im.bilab.panda.turtle;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 public class Fill_forward2 {
 	static String driver = "com.mysql.jdbc.Driver";
 	static String url = "jdbc:mysql://140.112.107.207/patent_value";
 	static String user = "root";
 	static String password = "123456";
 	static int lowestYear = 1976;
 	static int highestYear = 2009;
 	static String tmp_patent_id = "D262492";
 	static int tmp_patent_year = 1981;
 	
 	public static void main(String[] args) throws Exception {
 		fillAttr("attacker_expert");
 		fillAttr("attacker_troll_negative");
 		fillAttr("attacker_troll_positive");
 	}
 	
 	static void fillAttr(String updateTable){
 		Connection conn = null;
 		Statement stmt = null;
 		//String updateTable = "licensability_negative";
 		
 		try { 
 			Class.forName(driver); 
 			conn = DriverManager.getConnection(url, user , password);	
 			stmt = conn.createStatement(); 
 			
 			ResultSet result = stmt.executeQuery("SELECT Patent_id, Patent_year FROM "+updateTable); 
 			Map<String,Integer> patent_list = new TreeMap<String,Integer>(); 
 			
 			while(result.next()) {
 				int patent_year = result.getInt("Patent_year");
 				if(patent_year!=-1)
 					patent_list.put(result.getString("Patent_id").trim(), patent_year);
 			}
 			
 			Iterator<String> pItr = patent_list.keySet().iterator();
 			while(pItr.hasNext()){
 				String patent_id = pItr.next();
 				int patent_year = patent_list.get(patent_id);
 			  
 		    	try{
					Map<String,Integer> fwPatents = ForwardCite.getForwardList(patent_id, patent_year);
 					Map<String, Integer> fw_result = ForwardCite.calForward(patent_id, patent_year, fwPatents);
 					
 					int num_of_fwd_citations = fw_result.get("num_of_fwd_citations");
 					int num_of_fwd_3years = fw_result.get("num_of_fwd_3years");
 					int num_of_fwd_5years = fw_result.get("num_of_fwd_5years");
 					float ave_num_of_fwd = ForwardCite.getAvgForward(patent_id, patent_year, fwPatents);
 					float fwd_selfcitation_rate = ForwardCite.calFwSelfCite(patent_id, patent_year, fwPatents);
 					float generality_IPC = ForwardCite.getGenerality(patent_id, patent_year,"ipc", fwPatents);
 					float generality_USPC = ForwardCite.getGenerality(patent_id, patent_year,"ccl", fwPatents);
 					//float extensive_generality = ForwardCite.getExtGenerality(patent_id, patent_year, fwPatents);
 					
 					String sql = "UPDATE `patent_value`.`"+updateTable+"` SET " +
 							"`num_of_fwd_citations` = '"+num_of_fwd_citations+"'" +
 							",`num_of_fwd_3years` = '"+num_of_fwd_3years+"' " +
 							",`num_of_fwd_5years` = '"+num_of_fwd_5years+"' " +
 							",`ave_num_of_fwd` = '"+ave_num_of_fwd+"' " +
 							",`fwd_selfcitation_rate` = '"+fwd_selfcitation_rate+"' " +
 							",`generality_IPC` = '"+generality_IPC+"' " +
 							",`generality_USPC` = '"+generality_USPC+"' " +
 							//",`extensive_generality` = '"+extensive_generality+"' " +
 							  "WHERE `Patent_id` = '"+patent_id+"'";
 					stmt.executeUpdate(sql);
 					System.out.println(patent_id+" "+patent_year+" "
 							+num_of_fwd_citations+" "+num_of_fwd_3years+" "+num_of_fwd_5years+" "+ave_num_of_fwd+" "
 							+fwd_selfcitation_rate+""+generality_IPC+" "+generality_USPC);
 					//System.out.println(patent_id+" "+patent_year+" "+num_of_fwd_citations+" "+num_of_fwd_3years+" "+extensive_generality);
 				}catch(Exception e){
 					System.out.println(patent_id+"ȮɥX!");
 					e.printStackTrace();
 				}
 			 
 				
 			}
 		
 		} 
 		catch(ClassNotFoundException e) { 
 			System.out.println("Can't find driver class"); 
 			e.printStackTrace(); 
 		} 
 		catch(SQLException e) { 
 			e.printStackTrace(); 
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		finally {
 			if(stmt != null) {
 				try {
 					stmt.close();
 				} 
 				catch(SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			if(conn != null) {
 				try {
 					conn.close();
 				}
 				catch(SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		} 
 	}
 }
