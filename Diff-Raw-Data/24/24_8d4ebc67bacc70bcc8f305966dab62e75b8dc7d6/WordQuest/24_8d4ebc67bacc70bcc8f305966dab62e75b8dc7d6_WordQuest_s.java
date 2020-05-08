 package edu.cis350.mosstalkwords;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.os.Environment;
 import android.util.Log;
 
 public class WordQuest extends SQLiteOpenHelper {
 	
	  // All Static variables
     // Database Version
     private static final int DATABASE_VERSION = 1;
  
     // Database Name
     private static final String DATABASE_NAME = "mossWords";
 
     
     // Contacts table name
     // Table name according to user name
     private String tableName;
     
     public WordQuest(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 	 
     public void getTable(String uname) {
     	String userName = uname.toLowerCase();
     	tableName = userName + "_wordQuest";
     }
     
     public int getLevelsForMode(String mode)
     {
     	/* Need to start from level 1 and go till locked level is found */
     	int level = 1;
     	boolean lockedLevelFound = false;
     	SQLiteDatabase db = this.getReadableDatabase();
 		
     	
     	do{
	    		String getProgress = "select count(*) from " + tableName + " where progress >= 80 AND level = "+ level;
 	    		String getUnassisted = "select count(*) from " + tableName + " where unassistedGreaterThan24 >= 1 AND level = "+ level;
 	    		
 	    		
 	        	Cursor cursor1 = db.rawQuery(getProgress, null);
 	        	Cursor cursor2 = db.rawQuery(getUnassisted, null);
 	    		/* Get 100 images of a level */
 	    		if(cursor1 != null && cursor2 != null)
 	        	{
 	    			
 	    			cursor1.moveToFirst();
 	        		cursor2.moveToFirst();
 	        		if(mode.equalsIgnoreCase("easy"))
 	    	    	{
 		        		if(Integer.parseInt(cursor1.getString(0)) >= 10 && Integer.parseInt(cursor2.getString(0)) >= 6)
 		        			level++;
 		        		else
 			        	{
 		        			lockedLevelFound = true;
 			        	}
 	    	    	}
 	        		else if(mode.equalsIgnoreCase("medium"))
 	        		{
 	        			if(Integer.parseInt(cursor1.getString(0)) >= 60 && Integer.parseInt(cursor2.getString(0)) >= 40)
 		        			level++;
 		        		else
 			        	{
 		        			lockedLevelFound = true;
 			        	}
 	        		}
 	        		else if(mode.equalsIgnoreCase("hard"))
 	        		{
 	        			if(Integer.parseInt(cursor1.getString(0)) >= 70 && Integer.parseInt(cursor2.getString(0)) >= 50)
 		        			level++;
 		        		else
 			        	{
 		        			lockedLevelFound = true;
 			        	}
 	        		}
 	        		else if(mode.equalsIgnoreCase("harder"))
 	        		{
 	        			if(Integer.parseInt(cursor1.getString(0)) >= 80 && Integer.parseInt(cursor2.getString(0)) >= 60)
 		        			level++;
 		        		else
 			        	{
 		        			lockedLevelFound = true;
 			        	}
 	        		}
 	        		else if(mode.equalsIgnoreCase("hardest"))
 	        		{
 	        			if(Integer.parseInt(cursor1.getString(0)) >= 90 && Integer.parseInt(cursor2.getString(0)) >= 70)
 		        			level++;
 		        		else
 			        	{
 		        			lockedLevelFound = true;
 			        	}
 	        		}
 	        	}
 	    }while(lockedLevelFound == false && level <= 4);
     	return level;
     }
     
     public List<ImageStatistics> getImagesForLevel(int level){
     	
     			SQLiteDatabase db = this.getReadableDatabase();
 				String getItems = "select * from " + tableName + " where level = "+ level + " order by weight desc";
 	    		List<ImageStatistics> returnList = new ArrayList<ImageStatistics>();
 	        	Cursor cursor = db.rawQuery(getItems, null);
 	        	int num = 1;
 	        	
 	    		if(cursor != null)
 	        	{
 	    			cursor.moveToFirst();
 	    			do
 	    			{
 	    				ImageStatistics imageStat = new ImageStatistics();
 	    				try{
 	    				imageStat.setImageName(cursor.getString(1));
 	    				imageStat.setCategory(null);
 	    				imageStat.setWordHints(0);
 	    				imageStat.setSoundHints(0);
 	    				imageStat.setSolved(false);
 	    				imageStat.setAttempts(0);
     					imageStat.setIsFavorite(false);
     					Calendar cal = Calendar.getInstance();
     					cal.setTime(new Date(cursor.getString(9)));
     					imageStat.setLastSeen(cal);   
     					imageStat.setSeenToday(checkIfSeenToday(cal));
 	    				imageStat.setUrl(cursor.getString(11));
 	    				
 	    				returnList.add(imageStat);
 	    				cursor.moveToNext();
 	    				num++;
 	    				}
 	    				catch (Exception e){
 	    					Log.d("Exception","Less Than 20 Images in a Level");
 	    					return null;
 	    				}
 	    			}while(num <= (20 - level + 1));
 	        	}
 	    		
 	    		for(int i = level-1; i > 0; i--)
 	    		{	
 	    			ImageStatistics imageStat = new ImageStatistics();
 	    			getItems = "select * from " + tableName + " where level = "+ i + " order by weight desc";
 		        	Cursor cursor2 = db.rawQuery(getItems, null);
 		        	
 		        	if(cursor2 != null){
 		        		cursor2.moveToFirst();
 		        		imageStat.setImageName(cursor2.getString(1));
 	    				imageStat.setCategory(null);
 	    				imageStat.setWordHints(0);
 	    				imageStat.setSoundHints(0);
 	    				imageStat.setSolved(false);
 	    				imageStat.setAttempts(0);
     					imageStat.setIsFavorite(false);
     					Calendar cal = Calendar.getInstance();
     					cal.setTime(new Date(cursor2.getString(9)));
     					imageStat.setLastSeen(cal);   
     					imageStat.setSeenToday(checkIfSeenToday(cal));
 	    				imageStat.setUrl(cursor2.getString(11));
 	    				returnList.add(imageStat);
 		        	}
 	    		}
 	    		
 	    		return returnList;
     }
     
     public boolean checkIfSeenToday (Calendar lastSeen) {
 		if(lastSeen != null)
 		{
 	    	double hours = (System.currentTimeMillis() - lastSeen.getTimeInMillis()) / 1000 / 60 / 60;			
 			if(hours<=24)
 				return true;
 			else
 				return false;
 		}
 		return false;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
 		// TODO Auto-generated method stub
 		
 	}
     
 	public File generateWordQuestReport(String username) throws IOException {
 		File path = Environment.getExternalStorageDirectory();
 		File dir = new File(path.getAbsolutePath() + "/htmlfiles");
 		dir.mkdirs();
 		
 		File reportFile = null;
 		String timeStamp = new SimpleDateFormat("dd_MMM").format(Calendar.getInstance().getTime());
 		
 		reportFile = new File(dir, (username+"_Report_"+timeStamp+".html"));
 		
 		try {
 			FileWriter report = new FileWriter(reportFile, true);
 			String wordQuestReportHTML = generateWordQuestHTML(username);
 			report.write(wordQuestReportHTML);
 			report.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			Log.d("Exception","Exception in creating a report file: " + e.toString());
 		}
 		
 		return reportFile;
 	}
 
 	public String generateWordQuestHTML(String username){
 		SQLiteDatabase db = this.getReadableDatabase();
 		String getAllImagesData = "select * from " + tableName;
 		
     	Cursor cursor = db.rawQuery(getAllImagesData, null);
     	
 		String wordQuestHTML = "";
 		String timeStamp = new SimpleDateFormat("dd/MM/yyyy_HH:mm").format(Calendar.getInstance().getTime());
 		
 		wordQuestHTML += "<html> <head> <title> Report of " + username + " on " + 
 				timeStamp + "</title> " +
 		  		"<style type=\"text/css\"> td,h1 {text-align:center} " +
 		  		"</style>" +
 		  		"<link rel=\"stylesheet\" " +
 		  		"href=\"http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css\"/>" +
 		  		"<script type=\"text/javascript\"" +
 		  		"src=\"http://code.jquery.com/jquery-1.10.2.min.js\">" +
 		  		"</script>" +
 		  		"<script type=\"text/javascript\"" +
 		  		"src=\"http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js\">" +
 		  		"</script>" +
 		  		"<script type=\"text/javascript\"> " +
 		  		" $(document).ready(function(){" +
 		  		"	$(\"#wqTable\").dataTable();" +
 		  			"});</script>" +
 		  		"</head>";
 		
 		wordQuestHTML += "<body> <h1> Report of " + username + " on " + 
 						  timeStamp + "</h1>";
 		
 		/* Progress column is near to weight making table easy to understand */
 		wordQuestHTML += "<table border=\"0\" id=\"wqTable\"> <thead> <tr>" +
 						"<th> Level </th>" +
 						"<th> Image Name </th>" +
 						"<th> Weight </th>" +
 						"<th> Progress </th>" +
 						"<th> #Times successful unassisted naming when image last seen > 24 hrs </th>" +
 						"<th> Count for Penalty Box </th>" +
 						"<th> #Times successful unassisted naming when image last seen < 24 hrs </th>" +
 						"<th> #Times successful assisted naming </th>" +
 						"<th> Trial # of an item at last presentation </th>" +
 						"<th> Lifetime trial #</th>" +
 						"<th> Image Last Seen </th> " +
 						"</tr> </thead><tbody>";
 		
 		/* Populating the table from Database to HTML */
 		if(cursor != null && cursor.getCount()!=0)
     	{
 			cursor.moveToFirst();
 			while(cursor.moveToNext())
 			{
 				wordQuestHTML += "<tr> <td>" + cursor.getString(0) + "</td>" +
 						"<td>" + cursor.getString(1) + "</td>" +
 						"<td>" + cursor.getString(2) + "</td>" +
 						"<td>" + cursor.getString(10) + "</td>" +
 						"<td>" + cursor.getString(3) + "</td>" +
 						"<td>" + cursor.getString(4) + "</td>" +
 						"<td>" + cursor.getString(5) + "</td>" +
 						"<td>" + cursor.getString(6) + "</td>" +
 						"<td>" + cursor.getString(7) + "</td>" +
 						"<td>" + cursor.getString(8) + "</td>" +
 						"<td>" + cursor.getString(9) + "</td> </tr>";
 			}
     	}
 		else
 			return null;
 		
 		wordQuestHTML += "</tbody></table> </body> </html>";
 	
 		return wordQuestHTML;
 	}
 }
