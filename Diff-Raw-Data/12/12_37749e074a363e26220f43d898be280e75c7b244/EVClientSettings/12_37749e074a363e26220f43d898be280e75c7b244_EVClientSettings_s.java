 package com.evervoid.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.evervoid.json.Json;
 import com.evervoid.json.Jsonable;
 import com.evervoid.utils.MathUtils;
 
 public class EVClientSettings implements Jsonable
 {
 	private static final String sPreferencesFileName = "preferences.json";
 	private final File aAppDataDirectory;
 	private String aNickname;
 
 	public EVClientSettings()
 	{
 		// detect OS in oder to save to correct location
 		if (System.getProperty("os.name").toLowerCase().contains("win")) {
 			// windows
 			aAppDataDirectory = new File(System.getenv("APPDATA") + "/everVoid");
 		}
 		else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
 			// mac
 			aAppDataDirectory = new File(System.getProperty("user.home") + "/Library/Application Support/everVoid");
 		}
 		else {
 			// default - assume unix
 			aAppDataDirectory = new File(System.getProperty("user.home") + "/.everVoid");
 		}
 		Json j = null;
		if ((aAppDataDirectory.isDirectory())) {
			j = Json.fromFile(getPreferencesFile());
 		}
 		else {
 			EverVoidClient.getLogger().log(
 					Level.INFO,
 					"Local preference file does not exist (on operating system " + System.getProperty("os.name").toLowerCase()
 							+ ")");
 			j = Json.fromFile("res/home/appdata/" + sPreferencesFileName);
 		}
 		// name loading
 		if (j.getAttribute("name").isNull()) {
 			// load random name
 			final List<Json> names = Json.fromFile("res/schema/players.json").getListAttribute("names");
 			aNickname = ((Json) MathUtils.getRandomElement(names)).getString();
 		}
 		else {
 			aNickname = j.getStringAttribute("name");
 		}
 	}
 
 	public void close()
 	{
 		// Write back to file
 	}
 
 	public File getAppData()
 	{
 		return aAppDataDirectory;
 	}
 
 	public String getNickname()
 	{
 		return aNickname;
 	}
 
 	public File getPreferencesFile()
 	{
 		return new File(aAppDataDirectory, sPreferencesFileName);
 	}
 
 	public boolean loadSettings()
 	{
 		final Json j = Json.fromFile(getPreferencesFile());
 		try {
 			aNickname = j.getStringAttribute("name");
 		}
 		catch (final Exception e) {
 			Logger.getLogger(EverVoidClient.class.getName()).warning("Settings File did not exist");
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	public void setNickname(final String text)
 	{
 		aNickname = text;
 	}
 
 	@Override
 	public Json toJson()
 	{
 		final Json j = new Json();
 		j.setStringAttribute("name", aNickname);
 		return j;
 	}
 
 	@Override
 	public String toString()
 	{
 		return toJson().toPrettyString();
 	}
 
 	public void writeSettings()
 	{
 		final File file = getPreferencesFile();
 		try {
 			file.getParentFile().mkdirs();
 			file.createNewFile();
 		}
 		catch (final IOException e) {
 			// file already exists
 		}
 		toJson().toFile(file);
 	}
 }
