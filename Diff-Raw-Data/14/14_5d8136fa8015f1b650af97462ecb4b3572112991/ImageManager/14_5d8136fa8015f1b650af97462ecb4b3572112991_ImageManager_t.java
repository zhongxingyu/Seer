 package edu.cis350.mosstalkwords;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 
 import android.content.Context;
 
 public class ImageManager {
 	
 	public Images_SDB img_SDB;
 	public DatabaseHandler dbHandler;
 	public WordQuestDataHandler wqHandler;
 	public WordQuest wq;
 	private static boolean wordQuestUpdatedFromSDB;
 	
 	public ImageManager(String userName,Context context) {
 		dbHandler = new DatabaseHandler(context);
 		dbHandler.getTable(userName);
 		wq = new WordQuest(context);
 		wq.getTable(userName);
 		wqHandler = new WordQuestDataHandler(context);
 		wqHandler.createTable(userName);
 		wordQuestUpdatedFromSDB = false;
 	}
 	
 	public List<ImageStatistics> getImagesForCategory(String category){
 		
 		List<ImageStatistics> imageStatisticsList = new ArrayList<ImageStatistics>();
 		
 		img_SDB = new Images_SDB();
 		
 		List<Image> imageList = img_SDB.returnImages(category);
 		
 		for(int i = 0; i<imageList.size(); i++){
 			ImageStatistics imageStatistics = new ImageStatistics();
 			
 			imageStatistics.setImageName(imageList.get(i).getWord());
 			imageStatistics.setCategory(imageList.get(i).getCategory());
 			imageStatistics.setUrl(imageList.get(i).getUrl());
 			imageStatistics.setWordHints(0);
 			imageStatistics.setSoundHints(0);
 			imageStatistics.setSolved(false);
 			imageStatistics.setAttempts(0);
 			UserStimuli userStimuli = dbHandler.getUserStimuli(imageList.get(i).getWord());
 			if(userStimuli == null)
 			{
 				imageStatistics.setIsFavorite(false);
 				imageStatistics.setLastSeen(null);
 				imageStatistics.setSeenToday(false);
 			}
 			else
 			{
 				imageStatistics.setIsFavorite(userStimuli.getIsFavorite()==0?false:true);
 				imageStatistics.setLastSeen(userStimuli.getLastSeen());
 				if(checkIfSeenToday(userStimuli.getLastSeen()))
 					imageStatistics.setSeenToday(true);
 				else
 					imageStatistics.setSeenToday(false);
 			}
 			imageStatisticsList.add(imageStatistics);
 		}
 		
 		return imageStatisticsList;
 		
 		
 	}
 	
 	public List<ImageStatistics> getImagesForFavorites(){
 		
 		List<ImageStatistics> imageStatisticsList = new ArrayList<ImageStatistics>();
 		
 		
 		/* Code to Fetch Favorite Images from database table and create a list of 20 favorite images */
 		
 		List<UserStimuli> usList = new ArrayList<UserStimuli>();
 		usList = dbHandler.getFavoriteStimuli();
 
 		Collections.shuffle(usList);
 		
 		if(usList.size() > 0)
 		{
 			for(int i=0;i < 20;i++)
 			{
 				ImageStatistics imageStatistics = new ImageStatistics();
 				
 				imageStatistics.setImageName(usList.get(i).getImageName());
 				imageStatistics.setUrl(usList.get(i).getUrl());
 				imageStatistics.setCategory(usList.get(i).getCategory());
 				imageStatistics.setIsFavorite(usList.get(i).getIsFavorite()==0?false:true);
 				imageStatistics.setWordHints(0);
 				imageStatistics.setSoundHints(0);
 				imageStatistics.setSolved(false);
 				imageStatistics.setAttempts(0);
 				if(checkIfSeenToday(usList.get(i).getLastSeen()))
 					imageStatistics.setSeenToday(true);
 				else
 					imageStatistics.setSeenToday(false);
 				imageStatistics.setLastSeen(usList.get(i).getLastSeen());				
 				
 				imageStatisticsList.add(imageStatistics);
 				
 				if(i+1 == 20 || i+1 == usList.size())
 					break;
 			}
 		}
 		return imageStatisticsList;
 	}
 
 	public boolean checkIfSeenToday (Calendar lastSeen) {
 		if(lastSeen == null)
 		{
 			return false;
 		}
 		
 		double hours = (System.currentTimeMillis() - lastSeen.getTimeInMillis()) / 1000 / 60 / 60;			
 		if(hours<=24)
 			return true;
 		else
 			return false;
 	}
 	
 	public void setUserStimuli(List<ImageStatistics> imageStatisticsList) {
 		
 		UserStimuli userStimuli = new UserStimuli();
 		for(int i = 0; i<imageStatisticsList.size(); i++) {
 			
 			UserStimuli prevUserStimuli = dbHandler.getUserStimuli(imageStatisticsList.get(i).getImageName());
 			
 			userStimuli.setImageName(imageStatisticsList.get(i).getImageName());
 			userStimuli.setCategory(imageStatisticsList.get(i).getCategory());
 			userStimuli.setUrl(imageStatisticsList.get(i).getUrl());
 			userStimuli.setIsFavorite(imageStatisticsList.get(i).getIsFavorite()==true?1:0);
 			userStimuli.setLastSeen(imageStatisticsList.get(i).getLastSeen());
 
 			if(prevUserStimuli != null)
 			{
 				userStimuli.setAttempts(prevUserStimuli.getAttempts()+1);
 				userStimuli.setCorrectAttempts(prevUserStimuli.getCorrectAttempts()+(imageStatisticsList.get(i).isSolved()==true?1:0));
 				userStimuli.setSoundHints(prevUserStimuli.getSoundHints()+(imageStatisticsList.get(i).getSoundHints()));
 				userStimuli.setPlaywordHints(prevUserStimuli.getPlaywordHints()+(imageStatisticsList.get(i).getWordHints()));
 				if(imageStatisticsList.get(i).getWordHints()==0 && imageStatisticsList.get(i).getSoundHints() == 0 && imageStatisticsList.get(i).isSolved() == true)
 					userStimuli.setNoHint(prevUserStimuli.getNoHint()+1);
 				else
 					userStimuli.setNoHint(prevUserStimuli.getNoHint());
 			}
 			else
 			{
 				userStimuli.setAttempts(1);
 				userStimuli.setCorrectAttempts(imageStatisticsList.get(i).isSolved()==true?1:0);
 				userStimuli.setSoundHints(imageStatisticsList.get(i).getSoundHints());
 				userStimuli.setPlaywordHints(imageStatisticsList.get(i).getWordHints());
 				if(imageStatisticsList.get(i).getWordHints()==0 && imageStatisticsList.get(i).getSoundHints() == 0 && imageStatisticsList.get(i).isSolved() == true)
 					userStimuli.setNoHint(1);
 				else
 					userStimuli.setNoHint(0);
 			}
 			dbHandler.setStimuli(userStimuli);
 		}
 	}
 	
 	public boolean updateWordQuest(List<ImageStatistics> imgList, int currentLevel)
 	{
 		return wqHandler.updateWordQuest(imgList, currentLevel);
 	}
 	
 	public List<ImageStatistics> getImagesForWordQuest(int level){
 		if(wordQuestUpdatedFromSDB == false)
 		{
 			img_SDB = new Images_SDB();
 			/* Getting all images from Simple DB and updating the Word Quest Table */
 			List<Image> imgList = new ArrayList<Image>();
 			imgList = img_SDB.returnAllImages();
 			
 			System.out.println("Images from Simple DB:" + imgList.size());
 			
 			wqHandler.updateWQTable(imgList);
 			wordQuestUpdatedFromSDB = true;
 		}
 		
 
		return wq.getImagesForLevel(level);
 		
		/*
  		List<ImageStatistics> imageList = wq.getImagesForLevel(level);
 
 		for(ImageStatistics is : imageList)
 		{
 			UserStimuli userStimuli = dbHandler.getUserStimuli(is.getImageName());
 			if(userStimuli != null)
 			{
 				is.setIsFavorite(userStimuli.getIsFavorite()==0?false:true);
 			}
 		}
 		
 		return imageList;
		*/
 		
 	}
 	
 	/* This function will return upto which levels are unlocked 
 	 * For example, if this method returns 2, it means first two levels are unlocked.
 	 * Call this every time user selects word quest module.
 	 * */
 	public int getLevelsForWordQuest() {
 		return wq.getLevelsForMode("easy");
 	}
 	/*public String getWordQuestData(){
 		
 	}*/
 	
 	/* This method takes mode as parameter and returns the levels unlocked for 
      * that particular mode.
      * Mode must be one of the following:
      * "easy", "medium", "hard", "harder", "hardest"
      * Example: If method returns 3 for "easy" mode, it means you need to show
      * levels 1,2 and 3 unlocked and further levels will be locked.
      *  */
 	public int getLevelsForMode(String mode){
 		return wq.getLevelsForMode(mode);
 	}
 	
 	/* This method takes user name as argument and returns HTML file containing word quest
 	 * performance table for that particular user.*/
 	public File getWordQuestReport(String userName) throws IOException{
 		return wq.generateWordQuestReport(userName);
 	}
 }
