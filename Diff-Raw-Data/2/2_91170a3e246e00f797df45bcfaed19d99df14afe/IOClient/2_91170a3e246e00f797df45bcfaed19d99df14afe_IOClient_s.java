 package story.book.dataclient;
 
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.apache.commons.io.FileUtils;
 
 import story.book.model.Story;
 import story.book.model.StoryInfo;
 import android.content.Context;
 import android.net.Uri;
 import android.os.Environment;
 import android.util.Log;
 
 /**
  * This class is used mainly to save stories on device, retrieve a list of stories on device,
  * or to simply retrieve a story.
  * @author Anthony Ou
  * 
  * 
  */
 public class IOClient extends DataClient {
 
 	private Context context;
 
 	/**
 	 * Unbuffered IO for writing a serialized story. Buffered IO for read a
 	 * serialized story string.
 	 * 
 	 * @param c
 	 *            the Context of the main application since the package name
 	 *            directory can change
 	 */
 	public IOClient(Context c) {
 		super();
 		context = c; //I dont need the context but its nice to have for the future
 	}
 
 	/**
 	 * http://stackoverflow.com/questions/14376807/how-to-read-write-string-from
 	 * -a-file-in-android
 	 * 
 	 * @param SID
 	 *            A fileID
 	 * @param aStory
 	 *            The story to be saved
 	 */
 	public void saveStory(Story aStory) {
 		try {
 			String SID = String.valueOf(aStory.getStoryInfo().getSID());
 			new File(story_dir + SID).mkdir();
 			FileUtils.write(new File(story_dir + SID + "/" + SID), 
 					super.serialize(aStory));
 		} catch (IOException e) {
 			Log.d("error saving a story", "IOclient errors");
 		}
 	}
 
 	/**
 	 * 
 	 * @param SID
 	 *            the story to be deleted
 	 * @return true on success and false on failure
 	 */
 	public void deleteStory(int SID) {
 		try{
 			FileUtils.deleteDirectory(new File(story_dir + "/" + SID));
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Use this when you want a free media file name
 	 * 
 	 * @param SID
 	 * @return a free Uri for a media file to use up
 	 */
 	public Uri URIhandler(int SID, String Extension) {
 		File dir = new File(story_dir+String.valueOf(SID));
 		return Uri.fromFile(
 				new File(dir, Calendar.getInstance().getTimeInMillis()+Extension));
 	}
 	
 	/**
 	 * 
 	 * @return a list of all the SIDs on the internal device.
 	 */
 	public ArrayList<String> getStoryList() {
 		ArrayList<String> listOfFileNames = new ArrayList<String>();
 		for (String temp : new File(story_dir).list()) {
 			listOfFileNames.add(temp);
 		}
 		return listOfFileNames;
 	}
 
 	/**
 	 * 
 	 * @return an array list of StoryInfos
 	 */
 	public ArrayList<StoryInfo> getStoryInfoList() {
 		ArrayList<StoryInfo> listOfStoryInfo = new ArrayList<StoryInfo>();
 		for (String file : getStoryList()) {
 			try{
 				Story s = getStory(Integer.valueOf(file));
 				listOfStoryInfo.add(s.getStoryInfo());
 			}
 			catch (Exception e)
 			{
 				new File(story_dir,file).delete();
 			}
 		}
 		return listOfStoryInfo;
 	}
 	
 	/**
 	 * @return the file path to the application's storage directory
 	 */
 	public String getLocalDirectory() {
 		return this.story_dir;
 	}
 
 	public Boolean checkSID(int SID) {
 		return getStoryList().contains(String.valueOf(SID)) ? false : true;
 	}
 	
 	public void moveDirectory(int oldSID, int newSID) {
 		new File(story_dir+oldSID+"/"+oldSID).renameTo(new File(story_dir+oldSID+"/"+newSID));
 		new File(story_dir+oldSID).renameTo(new File(story_dir+newSID));
 	}
 
 	public int getSID() {
 		ArrayList<String> StoryInfo = getStoryList();
 		for (Integer i = 1; i < Integer.MAX_VALUE; ++i) {
 			if (!StoryInfo.contains(i.toString())) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * http://stackoverflow.com/questions/14376807/how-to-read-write-string-from
 	 * -a-file-in-android
 	 * 
 	 * @param SID
 	 *            The SID of a story
 	 * @returns A string representing a serialized story;
 	 */
 	public Story getStory(int SID) {
 		try {
 			return super.unSerialize(FileUtils.readFileToString(new File(story_dir
 					+ SID + "/" + SID)),Story.class);
 		} catch (Exception e) {
 			Log.d("reading file error", "getStory() error");
 			new File(story_dir + SID ).delete();
 			e.printStackTrace();
			return new Story(new StoryInfo("", -1));
 		}
 	}
 }
