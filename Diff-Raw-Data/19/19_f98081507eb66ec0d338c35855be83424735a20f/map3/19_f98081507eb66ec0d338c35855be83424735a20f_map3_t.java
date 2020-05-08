 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.*;
 
 public class map3 {
 	Parser.txt txt;
 	Database db;
 	ArrayList<Integer> currentItems;
 	Iterator<Integer> it;
 	HashSet<Integer> currentItemsClicked;
 	Integer currentUser;
 	String currenttype;
 	Integer currentNumberRecommend;
 	Integer currentNumberClick;
 	Double currentAP3=0.0;
 	Double totalAP3=0.0;
 	Integer N = 0;
 
 	public map3(String solutionFile, Database db) throws IOException{
 		this.txt= new Parser.txt(solutionFile);
 		this.db = db;
 		txt.hasNext();
 	}
 	
 	
 	
 
 	/**
 	 * 
 	 * @param
 	 * @return set next line, returns false when there are no next line
 	 * @throws Exception
 	 */
 	public Boolean hasNextLine() throws Exception{
 		totalAP3=+currentAP3;
 		N++;
 		if(this.txt.hasNext()){
 			//Read the file line
 			ArrayList<String> line = this.solutionParser(txt.next());
 			this.currentUser = Integer.getInteger(line.get(0));
             Debug.pl(line.get(1));
 			this.currentItemsClicked = Parser.spaceInteger2HashsetParser(line.get(1));
 			this.currenttype = line.get(2);
 			this.currentAP3 = 0.0;
 			this.currentNumberRecommend = 0;
 			this.currentNumberClick = 0;
 			//Read corresponding rec_log_test from DB
 			Statement stat = db.getStatement();
 			String sql = "SELECT itemId  FROM  rec_log_test WHERE UserID = "+this.currentUser+";";
 			ResultSet res = stat.executeQuery(sql);
 			this.currentItems = new ArrayList<Integer>();
 			while(res.next()){
 				int itemID = res.getInt("itemID");
 				currentItems.add(itemID);
 			}
 			this.it = this.currentItems.iterator();
 		return true;
 		}
 	return false;
 	}
 
 	
 	
 	public boolean add_recommend(int recommendedItem){
 		this.currentNumberRecommend++;
 		if(this.currentItemsClicked.contains(recommendedItem) && !(this.currentNumberClick>3)){
 			this.currentNumberClick++;
 			this.currentAP3 = ((this.currentNumberClick-1)*currentAP3+(this.currentNumberClick/this.currentNumberRecommend))/(this.currentNumberClick);
 		}
 		if(this.currentNumberClick>=3){
 			return false;
 		}
 		return true;
 	}
 	
 	
 	public boolean hasNextItem(){
 		return this.it.hasNext(); 
 	}
 	
 	
 	public int nextItem(){
 		return this.it.next();
 	}
 	
 	
 	
 	public double getMAP3(){
 		return this.totalAP3/this.N;
 	}
 
 
     public static ArrayList<String> solutionParser(String input){
         StringTokenizer st = new StringTokenizer(input,",");
         ArrayList<String> data = new ArrayList<String>();
         for(int i=0;i<3;i++){
             Debug.pl(i);
             data.add(i,"");
        }
        
        st.hasMoreTokens();
        data.set(0,st.nextToken());
        
        st.hasMoreTokens();
        boolean isNotType = st.nextToken() !="Public" || st.nextToken() != "Private";
        if(isNotType){
        	data.set(1,st.nextToken());
        	st.hasMoreTokens();
        	data.set(2, st.nextToken());
        }else{
        	data.set(2,st.nextToken());
         }
         return data;
     }
 }
