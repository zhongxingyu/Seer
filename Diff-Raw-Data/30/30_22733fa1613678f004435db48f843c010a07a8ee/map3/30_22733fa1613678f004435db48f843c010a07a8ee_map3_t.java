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
	int currentUser;
 	String currenttype;
	Double currentNumberRecommend;
	Double currentNumberClick;
 	Double currentAP3=0.0;
 	Double totalAP3=0.0;
	double N = 0;
	double N_private =0;
	double N_public = 0;
 
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
 		Debug.pl("Total AP3:"+totalAP3);
 		
 		if(this.txt.hasNext()){
 			//Read the file line
 			ArrayList<String> line = map3.solutionParser(txt.next());
 			this.currentUser = Integer.parseInt(line.get(0));
 			this.currentItemsClicked = Parser.spaceInteger2HashsetParser(line.get(1));
 			this.currenttype = line.get(2);
 			if(currenttype.contains("Priv")) N_private++;
 			if(currenttype.contains("Pub")) N_public++;
 			this.currentAP3 = 0.0;
			this.currentNumberRecommend = 0.0;
			this.currentNumberClick = 0.0;
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
		
			totalAP3+=currentAP3;
			N++; 
			return true;
			  
		
 		}
 	return false;
 	}
 
 	
 	
 	public boolean add_recommend(int recommendedItem){
 		this.currentNumberRecommend++;
 		Debug.pl("Recommended item exist in list: "+this.currentItemsClicked.contains(recommendedItem));
 		Debug.pl("Current clicks: "+this.currentNumberClick);
 		Debug.pl("Current recommend: "+currentNumberRecommend);
 		if(this.currentItemsClicked.contains(recommendedItem) && !(this.currentNumberClick>3)){
 			this.currentNumberClick++;
 			this.currentAP3 += ((this.currentNumberClick-1)*currentAP3+(this.currentNumberClick/this.currentNumberRecommend))/(this.currentNumberClick);
 			Debug.pl(currentAP3);
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
             data.add(i,"");
         }
         
         String data3 = st.nextToken();
         data.set(0,data3);
         
         String data1 = st.nextToken();
         boolean isType = data1.contains("P");
         if(!isType){
         	data.set(1,data1);
         	String data2=st.nextToken();
         	data.set(2, data2);
         }else{
         	data.set(2,data1);
         }
         return data;
     }
 }
