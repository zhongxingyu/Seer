 /*
  * A singleton class which manages moon bases. It also identifies the current moon base.
  */
 package spaceappschallenge.moonville.managers;
 
 import java.io.File;
 import java.io.FileInputStream;
import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 import android.content.Context;
 import android.provider.OpenableColumns;
import android.widget.Toast;
 
 import spaceappschallenge.moonville.businessmodels.Difficulty;
 import spaceappschallenge.moonville.businessmodels.MoonBase;
 
 public class MoonBaseManager {
 	// added static since there is only 1 moonbase
 	// it's a bit dirty maybe, but also makes it easier to access it everywhere
 	// when needed -Jos
 	private static MoonBase currentMoonBase;
 
 	public static MoonBase getCurrentMoonBase() {
 		return currentMoonBase;
 	}
 
 	public static void createNewMoonBase(Difficulty diff) {
 		MoonBaseManager.currentMoonBase = new MoonBase(
 				diff.getResearchPoints(), diff.getProspectingLevel(),
 				diff.getMoney());
 	}
 
 	public static void loadSavedMoonbase(Context context) {
 		MoonBase currentMoonBase = null;
 
 		try {
 			FileInputStream fis = new FileInputStream(context.getFilesDir()
 					+ File.separator + Reference.SAVE_FILE);
 			ObjectInputStream ois = new ObjectInputStream(fis);
 
 			currentMoonBase = (MoonBase) ois.readObject();
 
 			ois.close();
 			fis.close();
 		}
 
		catch (FileNotFoundException e) {
			Toast t = Toast.makeText(context,
					"There is no saved Game - Please start a new one.",
					Toast.LENGTH_LONG);
			t.show();
		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		MoonBaseManager.currentMoonBase = currentMoonBase;
 	}
 
 	public static void saveMoonBase(Context context) {
 		try {
 			FileOutputStream fos = new FileOutputStream(context.getFilesDir()
 					+ File.separator + Reference.SAVE_FILE);
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 
 			oos.writeObject(MoonBaseManager.currentMoonBase);
 
 			oos.close();
 			fos.close();
 		}
 
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
