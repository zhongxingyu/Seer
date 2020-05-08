 package story.book.dataclient;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.commons.io.FilenameUtils;
 
 import story.book.model.Annotation;
 import story.book.model.BinaryIllustration;
 import story.book.model.Illustration;
 import story.book.model.Story;
 import story.book.model.StoryFragment;
 import story.book.model.StoryInfo;
 import android.util.Log;
 
 import com.google.gson.reflect.TypeToken;
 
 /**
  * ESClient is a DataClient class that represents 
  * an online storage location for Stories. Stories
  * are saved to an Elastic Search server.
  * 
  * @author Vina Nguyen
  * @author Anthony Ou
  */
 
 public class ESClient extends DataClient {	
 	private String stories_folder = "stories/";
 	private String binaries_folder = "";
 
 	/**
 	 *  Publishes a story.	 
 	 *  
 	 * @param SID is a Story's SID
 	 */
 	public void saveStory(Story story) {
 		publishStory(story);
 		publishIllustrations(story);
 	}
 
 	/**
 	 *  Uploads a Story object to the server.	 
 	 *  
 	 * @param story is the Story to be uploaded
 	 */
 	private void publishStory(Story story){
 		try {
 			int SID = story.getStoryInfo().getSID();
 			String stringSID = String.valueOf(SID);
 			String story_string = super.serialize(story);
 
 			// Write the Story to the server
 			new ESWrite(stories_folder).execute(stringSID, story_string);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}		
 	}
 
 	/**
 	 *  Uploads a Story's binary illustrations to the server.	 
 	 *  
 	 * @param story is the Story to be uploaded
 	 */
 	private void publishIllustrations(Story story){
 		int SID = story.getStoryInfo().getSID();
 		String stringSID = String.valueOf(SID);
 
 		//Get list of binary files		
 		HashMap<Integer, StoryFragment> fragmentMap = story.getStoryFragments();
 
 		for (Integer key: fragmentMap.keySet()) {
 			StoryFragment f = fragmentMap.get(key);
 
 			//encode Illustrations
 			ArrayList<Illustration> illusrations = f.getIllustrations();
 			for (Illustration i: illusrations) {
 				if (i instanceof BinaryIllustration) {
 					uploadBinaryFile((BinaryIllustration)i, stringSID);
 				}
 			}
 		}
 	}
 
 	/**
 	 *  Publishes an annotation to a story.	 
 	 *  
 	 * @param annotation to be published
 	 * @param story that the annotation is part of
 	 */
 	public void publishAnnotation(Annotation a, Story story) {
 		int SID = story.getStoryInfo().getSID();
 		String stringSID = String.valueOf(SID);
 
 		//publish the annotation (if necessary)
 		Illustration i = a.getIllustration();
 		if (i instanceof BinaryIllustration) {
 			uploadBinaryFile((BinaryIllustration)i, stringSID);
 		}
 
 		//re-publish the story as fragment has new annotation
 		publishStory(story);
 	}
 
 	/**
 	 *  Uploads a single binary illustration to the server.	 
 	 *  
 	 * @param BinaryIllustration to be uploaded
 	 * @param SID the illustration belongs to 
 	 */
 	private void uploadBinaryFile(BinaryIllustration bi, String SID) {
 		String path =  story_dir + SID + "/";
 		BinaryFile bf = new BinaryFile(bi.getContent(), bi.encodeIllustration(path));
 
 		String binary_string = super.serialize(bf);
 		try {
 			path = bf.getContent();
 			path = FilenameUtils.removeExtension(path);
 			new ESWrite(binaries_folder).execute(SID + "/" + path, binary_string);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 *  Downloads a single binary illustration from the server.	 
 	 *  
 	 * @param BinaryIllustration to be uploaded
 	 * @param SID the illustration belongs to 
 	 */
 	private void downloadBinaryFile(String file, String sid_string) {
 		String path =  story_dir + sid_string + "/";
 
 		try {
 			String server_read = new ESRead(binaries_folder).execute(sid_string+"/"+file).get();
 			Type type = new TypeToken<ESData<BinaryFile>>(){}.getType();
 			ESData<BinaryFile> es = (ESData<BinaryFile>) super.unSerialize(server_read, type);
 			BinaryFile binFile = es.getSource();
 			binFile.decode(path);
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 *  Reads the in-use SID list from the server. 
 	 *  
 	 * @return SIDList
 	 * @throws ExecutionException 
 	 * @throws InterruptedException 
 	 */
 	private ArrayList<String> readSIDList() {
 		String server_read;
 		try {
 			server_read = new ESRead(stories_folder).execute("_search?fields=_id").get();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			server_read = "";
 		} 
 		Type type = new TypeToken<ESResponse>(){}.getType();
 		ESResponse es = (ESResponse) super.unSerialize(server_read, type);
 		return (ArrayList<String>) es.getIDs();
 	}
 
 	private ArrayList<String> readIllustrations(Story story) {
 		int SID = story.getStoryInfo().getSID();
 		String stringSID = String.valueOf(SID);
 
 		ArrayList<String> list = new ArrayList<String>();
 
 		HashMap<Integer, StoryFragment> fragmentMap = story.getStoryFragments();
 
 		for (Integer key: fragmentMap.keySet()) {
 			StoryFragment f = fragmentMap.get(key);
 
 			// get Binary Illustrations files
 			ArrayList<Illustration> illusrations = f.getIllustrations();
 			for (Illustration i: illusrations) {
 				if (i instanceof BinaryIllustration) {
 					String file = ((BinaryIllustration)i).getContent();
 					downloadBinaryFile(FilenameUtils.removeExtension(file), stringSID );
 					//list.add(FilenameUtils.removeExtension(file));
 				}
 			}
 
 			// get Binary Annotations files
 			ArrayList<Annotation> annotations = f.getAnnotations();
 			for (Annotation a: annotations) {
 				Illustration i = a.getIllustration();
 				if (i instanceof BinaryIllustration) {
 					String file = ((BinaryIllustration)i).getContent();
 					downloadBinaryFile(FilenameUtils.removeExtension(file), stringSID );
 				}
 			}
 		}
 
 		return list;
 	}
 
 	private Story readStory (String sid_string) {
 		Story story = null;
 		// Get the story from the server
 		try {
 			String server_read = new ESRead(stories_folder).execute(sid_string ).get();
 			Type type = new TypeToken<ESData<Story>>(){}.getType();
 			ESData<Story> es = (ESData<Story>) super.unSerialize(server_read, type);
 
 			story = es.getSource();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 
 		return story;
 	}
 
 	/**
 	 * Retrieves an online story (to be used for download or viewing).	 
 	 *  
 	 * @param SID is a Story's SID
 	 * @return a null on failure or a Story object 
 	 */
 	public Story getStory(int SID) {
 		try {			
 			String sid_string = String.valueOf(SID);
 
 			// Read the story from the server
 			Story story = readStory(sid_string);
 
 			// Read the illustrations from the server
 			ArrayList<String> filenames = readIllustrations(story);
 
 			return  story;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 
 	}
 
 	@Override
 	public ArrayList<StoryInfo> search(String term) {
 		String server_read = "";
 		try {
 			server_read = new ESRead(stories_folder).execute("_search?q="+term.toLowerCase()+"&fields=storyInfo" ).get();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return getStoryInfoFromField(server_read);
 		
 	}
 	
 	private ArrayList<StoryInfo> getStoryInfoFromField(String server_read) {
 		Type type = new TypeToken<ESResponse<StoryInfoItem>>(){}.getType();
 		ESResponse<StoryInfoItem> es =  super.unSerialize(server_read, type);
 		ArrayList<StoryInfo> storyinfo = new ArrayList<StoryInfo>();
 		for(StoryInfoItem item : es.getFields())
 			storyinfo.add(item.storyInfo);
 		return storyinfo;	
 	}
 	
 	/**
 	 * This is really a strange class to explain.
 	 * When I search by a field, the items inside field are generated
 	 * automatically. And that field is named after the variable name.
 	 * 
 	 * Basically the shortest answer is that 'field' is 
 	 * never the type you are looking for but a container
 	 * of what you are looking for.
 	 * 
 	 * @author Anthony Ou
 	 * @see StoryInfoItem
 	 * @see getStoryInfoFromField
 	 */
 	private class StoryInfoItem{public StoryInfo storyInfo;}
 	
 	public ArrayList<StoryInfo> getStoryInfoList() {
 		try {
 			String server_read = new ESRead(stories_folder).execute("_search?&fields=storyInfo").get();
 			return getStoryInfoFromField(server_read);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public Boolean checkSID(int SID) {
 		ArrayList<String> list = readSIDList();
		
		if (!list.contains(String.valueOf(SID))) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public int getSID() {
 		ArrayList<String> list = readSIDList();
 		for (Integer i = 0; i < Integer.MAX_VALUE; i++) {
 			if (!list.contains(i.toString())) {
 				return i;
 			} 
 		}
 
 		return -1;
 	}
 }
