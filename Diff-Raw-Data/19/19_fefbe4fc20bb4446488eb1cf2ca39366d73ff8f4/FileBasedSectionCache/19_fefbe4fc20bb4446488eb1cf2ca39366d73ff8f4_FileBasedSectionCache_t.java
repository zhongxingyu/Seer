 package nz.gen.wellington.guardian.android.api.caching;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.List;
 
 import nz.gen.wellington.guardian.android.model.Section;
 import android.content.Context;
 import android.util.Log;
 
 public class FileBasedSectionCache {
 	
 	private static final String SECTIONS_JSON = "sections.json";
 	private static final String TAG = "FileBasedSectionCache";
 
 	private Context context;
 	
 	public FileBasedSectionCache(Context context) {
 		this.context = context;
 	}
 
 	
 	public void putSections(List<Section> sections) {				
 		try {
 			FileOutputStream fos = FileService.getFileOutputStream(context, SECTIONS_JSON);
 			ObjectOutputStream out = new ObjectOutputStream(fos);
 			out.writeObject(sections);
 			out.close();
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	public List<Section> getSections() {
 		if (!FileService.isLocallyCached(context, SECTIONS_JSON)) {
 			return null;
 		}		
 		Log.i(TAG, "Reading from disk: " + SECTIONS_JSON);
 		try {
 			FileInputStream fis = FileService.getFileInputStream(context, SECTIONS_JSON);
 			ObjectInputStream in = new ObjectInputStream(fis);
 			List<Section> loaded = (List<Section>) in.readObject();
 			in.close();
 			return loaded;
 			
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		} catch (ClassNotFoundException ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 	
 	
 	public void clear() {
 		Log.i(TAG, "Clearing sections");
		if (FileService.isLocallyCached(context, SECTIONS_JSON)) {			
			FileService.clear(context, SECTIONS_JSON);
 			Log.i(TAG, "Cleared: " + SECTIONS_JSON);
 		} else {
 			Log.i(TAG, "No local copy to clear:" + SECTIONS_JSON);
 		}
 	}
 		
 }
