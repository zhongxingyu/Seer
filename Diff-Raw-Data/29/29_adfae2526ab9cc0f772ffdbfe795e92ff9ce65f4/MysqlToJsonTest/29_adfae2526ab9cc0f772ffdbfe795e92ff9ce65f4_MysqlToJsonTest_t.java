 import java.sql.PreparedStatement;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import java.io.BufferedReader; 
 import java.io.IOException; 
 import java.io.InputStreamReader; 
 import java.util.ArrayList; 
 import java.util.LinkedList;
 import java.lang.Integer;
 
 import java.io.PrintWriter;
 
 
 public class MysqlToJsonTest{
 	
 	public static void main(String args[]){
 		
 		if(args.length < 2){
 			System.out.println("ERROR: USAGE =");
 			System.out.println("$ java -cp .:mysql-connector-java-5.1.20-bin.jar MysqlToJsonTest _user_ _password_");
 			return;
 		}
		//x = 100, y = 50
 		boolean iamdebugging = true;
		int screenSizeHorizontal = 2500;  // horizontal pixels
		int verticalStepSize = 50;  // change to -30 when needed and also change rootNodeY
		int horizontalStepSize = 100; // default value, will be reduced later if sentence is too long and goes out of bound with default value
		boolean SCALE = false;
 		String jsonFormat = "{\\\"subject_x\\\":\\\"xxxsubject_xxxx\\\",\\\"subject_y\\\":\\\"xxxsubject_yxxx\\\",\\\"subject_z\\\":\\\"999\\\",\\\"subject_val\\\":\\\"999\\\",\\\"object1_x\\\":\\\"xxxobject1_xxxx\\\",\\\"object1_y\\\":\\\"xxxobject1_yxxx\\\",\\\"object1_z\\\":\\\"999\\\",\\\"object1_val\\\":\\\"999\\\",\\\"type\\\":\\\"SD2\\\",\\\"direction\\\":\\\"1\\\",\\\"subject_name\\\":\\\"xxxsubject_namexxx\\\",\\\"subject_name_alias\\\":\\\"xxxsubject_namexxx\\\",\\\"object1_name\\\":\\\"xxxobject1_namexxx\\\",\\\"object1_name_alias\\\":\\\"xxxobject1_namexxx\\\",\\\"verb\\\":\\\"xxxverbxxx\\\"}";
 		
 		FillRelations fillRelations1 = new FillRelations(args[0],args[1]);
 		PrintWriter output = null;
 		PreparedStatement pst = null;
 		ResultSet sentencesRS = null;
 		ResultSet sentencePhrasesRelRS = null;
 		
 		ResultSet sentencePhraseRS = null;
 		ResultSet numRowsRS = null; // num rows in sentence_phrase_tbl of a sentence
 		ResultSet childCountRS = null;
		ResultSet vertLvlRS = null; 
 		
 		try {
 			
             output = new PrintWriter("json_node.txt");
             
             
             int chapterID = 210; // elastin = 210
             pst = fillRelations1.getConnection().prepareStatement("SELECT sentence_id FROM sentence_tbl WHERE chapter_id = " + chapterID);
             sentencesRS = pst.executeQuery();
             
             // for every sentence in the chapter
             while(sentencesRS.next()){
             	String sentenceID = sentencesRS.getString(1);
             	
             	
             	pst = fillRelations1.getConnection().prepareStatement("SELECT object_phrase_id, target_phrase_id FROM sentence_phrase_rel_tbl WHERE sentence_id = " + sentenceID);
             	sentencePhrasesRelRS = pst.executeQuery();
             	TagCounter tc = new TagCounter(); // used for counting NP0, NP1, etc.
             	CoordinateCounter cc = new CoordinateCounter(); // used for counting subject_x, subject_y, object_x, and object_y
             	String stringToWrite = "\"[";
             	String lastIterObjectPhraseID = "";
             	
             	// used for calculating the y coordinate of the child
             	int nthChild = 0; 
             	int numChildren = 0;
             	
             	sentencePhrasesRelRS.next(); // skip the very first sentencePhraseRel entry because berkeleyParser returns result like this: ( (S (NP Dog)))
             	
             	// handling the first/root node, later part of this section does the same thing as the following while loop do
             	if(sentencePhrasesRelRS.next()){
             		// count the number of sentence_phrase_rel entries in this sentence
             		pst = fillRelations1.getConnection().prepareStatement("SELECT count(*) FROM sentence_phrase_tbl WHERE sentence_id = " + sentenceID);
             		numRowsRS = pst.executeQuery();
             		numRowsRS.next();
             		int numRows = numRowsRS.getInt(1);
             		
            		// scale down horizontalStepSize if the default value will go out of bound    
            		if(SCALE)        		
		        		if(numRows * horizontalStepSize > screenSizeHorizontal) 
		        			horizontalStepSize = screenSizeHorizontal / numRows ;
             		
             		// determine the coordinate of the root node
             		int rootNodeX = screenSizeHorizontal / 2; 
             		int rootNodeY = 30; // determines the start point of the y coordinate
             		
             		
             		
             		
             		
             		//
             		// mostly the same thing as the following while loop do
             		//
             		String jsonNode = jsonFormat;
             		String objectPhraseID = sentencePhrasesRelRS.getString(1); // root node's id
             		String targetPhraseID = sentencePhrasesRelRS.getString(2);
             		
             		cc.addUnique(objectPhraseID,rootNodeX,rootNodeY); // the only time objectPhraseID needed to be added to cc (because it's a root)
             		jsonNode = jsonNode.replaceAll("xxxsubject_xxxx",(new Integer(rootNodeX)).toString());
             		jsonNode = jsonNode.replaceAll("xxxsubject_yxxx",(new Integer(rootNodeY)).toString());
             		
             		// object phrase (in json node: subject)
             		pst = fillRelations1.getConnection().prepareStatement("SELECT sentence_phrase_text_TAG FROM sentence_phrase_tbl WHERE sentence_phrase_id = " + objectPhraseID);
             		sentencePhraseRS = pst.executeQuery(); // the query should only give 1 result
             		sentencePhraseRS.next();
             		String subjectName = sentencePhraseRS.getString(1);
             		
             		// currentCounter() when subject is the same as last iteration of loop
             		// advanceCounter() when subject is different as last iteration of loop
             		if(objectPhraseID.equals(lastIterObjectPhraseID))
             			jsonNode = jsonNode.replaceAll("xxxsubject_namexxx",tc.currentCounter(subjectName));
             		else
             			jsonNode = jsonNode.replaceAll("xxxsubject_namexxx",tc.advanceCounter(subjectName)); 
             		
             		
             		
             		// target phrase (in json node: object)
             		pst = fillRelations1.getConnection().prepareStatement("SELECT sentence_phrase_text_TAG, sentence_phrase_text FROM sentence_phrase_tbl WHERE sentence_phrase_id = " + targetPhraseID);
             		sentencePhraseRS = pst.executeQuery(); // the query should only give 1 result
             		sentencePhraseRS.next();
             		String objectName = sentencePhraseRS.getString(1); //sentence_phrase_text_TAG
             		String verb = "is related to ";
             		
             		// checking for leaf node
            			String alternateObjectName = sentencePhraseRS.getString(2);
            			if(alternateObjectName.equals("")) // if reached leaf node
            				objectName = tc.advanceCounter(objectName);
            			else{
            				verb = objectName; // sentence_phrase_text_TAG
            				objectName = alternateObjectName; // sentence_phrase_text
            				
            				
            				// check for tags like PRP$, WP$ and replace $ with \$ because of regex used later in replaceAll()
            				if(verb.contains("$"))
            					verb = verb.replaceAll("\\$","\\\\\\$");; // need 6 backslashs because of regex http://stackoverflow.com/questions/11041674/escape-java-regexp-metacharacters
            				
            				
             		}
             		jsonNode = jsonNode.replaceAll("xxxobject1_namexxx",objectName);
             		jsonNode = jsonNode.replaceAll("xxxverbxxx",verb);
             		
             		//take care of coords for object1
             		pst = fillRelations1.getConnection().prepareStatement("SELECT count(*) from sentence_phrase_rel_tbl where object_phrase_id = " + objectPhraseID);
             		childCountRS = pst.executeQuery();
             		childCountRS.next();
             		numChildren = childCountRS.getInt(1);
             		int objectX = (nthChild - numChildren/2) * horizontalStepSize + cc.getXCoord(objectPhraseID);
            		int objectY = cc.getYCoord(objectPhraseID) + verticalStepSize;             		
             		cc.addUnique(targetPhraseID,objectX,objectY);
             		jsonNode = jsonNode.replaceAll("xxxobject1_xxxx",(new Integer(objectX)).toString());
             		jsonNode = jsonNode.replaceAll("xxxobject1_yxxx",(new Integer(objectY)).toString());
             		
             		stringToWrite += jsonNode + ",";
             		
             		lastIterObjectPhraseID = objectPhraseID;
             		nthChild++;
             		
             	}
             	
             	
             	// handling the remaining of the sentence; for every sentence_phrase_rel entry of the sentence
             	while(sentencePhrasesRelRS.next()){
             		String jsonNode = jsonFormat;
             		String objectPhraseID = sentencePhrasesRelRS.getString(1);
             		String targetPhraseID = sentencePhrasesRelRS.getString(2);
             		
             		if(!lastIterObjectPhraseID.equals(objectPhraseID)){ // changed to a new parent
             			nthChild = 0;
             			pst = fillRelations1.getConnection().prepareStatement("SELECT count(*) from sentence_phrase_rel_tbl where object_phrase_id = " + objectPhraseID);
             			childCountRS = pst.executeQuery();
             			childCountRS.next();
             			numChildren = childCountRS.getInt(1);
             		}
             		
             		// dont need to add the objectPhraseID into cc since it should alrdy been added as targetPhraseID in previous loop
             		jsonNode = jsonNode.replaceAll("xxxsubject_xxxx",(new Integer(cc.getXCoord(objectPhraseID))).toString());
             		jsonNode = jsonNode.replaceAll("xxxsubject_yxxx",(new Integer(cc.getYCoord(objectPhraseID))).toString());
             		
             		// object phrase (in json node: subject)
             		pst = fillRelations1.getConnection().prepareStatement("SELECT sentence_phrase_text_TAG FROM sentence_phrase_tbl WHERE sentence_phrase_id = " + objectPhraseID);
             		sentencePhraseRS = pst.executeQuery(); // the query should only give 1 result
             		sentencePhraseRS.next();
             		String subjectName = sentencePhraseRS.getString(1);
             		
             	
             		// currentCounter() when subject is the same as last iteration of loop
             		// advanceCounter() when subject is different as last iteration of loop
             		if(objectPhraseID.equals(lastIterObjectPhraseID))
             			jsonNode = jsonNode.replaceAll("xxxsubject_namexxx",tc.currentCounter(subjectName));
             		else
             			jsonNode = jsonNode.replaceAll("xxxsubject_namexxx",tc.advanceCounter(subjectName)); 
             		
             		
             		
             		// target phrase (in json node: object)
             		pst = fillRelations1.getConnection().prepareStatement("SELECT sentence_phrase_text_TAG, sentence_phrase_text FROM sentence_phrase_tbl WHERE sentence_phrase_id = " + targetPhraseID);
             		sentencePhraseRS = pst.executeQuery(); // the query should only give 1 result
             		sentencePhraseRS.next();
             		String objectName = sentencePhraseRS.getString(1); //sentence_phrase_text_TAG
             		String verb = "is related to ";
             		
             		// checking for leaf node
            			String alternateObjectName = sentencePhraseRS.getString(2);
            			if(alternateObjectName.equals("")) // if reached leaf node
            				objectName = tc.advanceCounter(objectName);
            			else{
            				verb = objectName; // sentence_phrase_text_TAG
            				objectName = alternateObjectName; // sentence_phrase_text
            				
            				
            				// check for tags like PRP$, WP$ and replace $ with \$ because of regex used later in replaceAll()
            				if(verb.contains("$"))
            					verb = verb.replaceAll("\\$","\\\\\\$");; // need 6 backslashs because of regex http://stackoverflow.com/questions/11041674/escape-java-regexp-metacharacters
            				
            				
             		}
             		jsonNode = jsonNode.replaceAll("xxxobject1_namexxx",objectName);
             		jsonNode = jsonNode.replaceAll("xxxverbxxx",verb);
             		
             		//take care of coords
             		int objectX = (nthChild - numChildren/2) * horizontalStepSize + cc.getXCoord(objectPhraseID);
            		int objectY = cc.getYCoord(objectPhraseID) + verticalStepSize;             		
             		cc.addUnique(targetPhraseID,objectX,objectY);
             		jsonNode = jsonNode.replaceAll("xxxobject1_xxxx",(new Integer(objectX)).toString());
             		jsonNode = jsonNode.replaceAll("xxxobject1_yxxx",(new Integer(objectY)).toString());
             		
             		stringToWrite += jsonNode + ",";
             		
             
             		lastIterObjectPhraseID = objectPhraseID;
             		nthChild++;
             	}
             	
             	
             	stringToWrite = stringToWrite.substring(0, stringToWrite.length() - 1); // take out the last comma
             	stringToWrite += "]\"\n";
             	output.write(stringToWrite);
             }
             
             
             
 
         }catch(IOException ioe){
 			ioe.printStackTrace();
 		}catch (SQLException ex){
             Logger lgr = Logger.getLogger(MysqlToJsonTest.class.getName());
             lgr.log(Level.SEVERE, ex.getMessage(), ex);
 
         } finally {
            fillRelations1.closeConnection();
            output.close();
            try{
 		       pst.close();
 		       sentencesRS.close();
 		       sentencePhrasesRelRS.close();
 		       sentencePhraseRS.close();
 		       numRowsRS.close();
 		       childCountRS.close();
		       if(vertLvlRS != null)
		           vertLvlRS.close();
            }catch(SQLException sqle){
                Logger lgr = Logger.getLogger(MysqlToJsonTest.class.getName());
                lgr.log(Level.SEVERE, sqle.getMessage(), sqle);
            }
         }
 		
 	}
 	
 	/*
 		Currently, only check word level tag with this method because according to Penn Treebank, only word level tags contains metacharacters like '$'
 		e.g. PRP$, WP$
 	*/
 	public static boolean containsMetaCharacter(String s){
 		// only checking for '$'
 		
 		// implement me!
 			
 		return true;
 	}
 	
 	public static void escapeSequence(String s){
 		// implement me!
 	}
 	
 	
 }
