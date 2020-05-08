 package com.evervoid.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 
 import com.evervoid.json.Json;
 import com.evervoid.json.Jsonable;
 import com.evervoid.utils.LoggerUtils;
 import com.evervoid.utils.MathUtils;
 import com.evervoid.utils.ResourceUtils;
 
 public class EVClientSettings implements Jsonable
 {
 	private static final String sPreferencesFileName = "preferences.json";
 
 	public static String getRandomName()
 	{
 		final List<Json> names = Json.fromFile("schema/players.json").getListAttribute("names");
 		return ((Json) MathUtils.getRandomElement(names)).getString();
 	}
 
 	private final File aAppDataDirectory;
	private String aNickname = null;
 	private boolean aSfx = true;
 	private boolean aSound = true;
 
 	public EVClientSettings()
 	{
 		// detect OS in oder to save to correct location
 		aAppDataDirectory = new File(ResourceUtils.getAppDir());
 		final File pref = getPreferencesFile();
 		if (pref.exists()) {
 			// name loading
 			loadSettings();
 		}
 		else {
 			LoggerUtils.getLogger().log(
 					Level.INFO,
 					"Local preference file does not exist (on operating system " + System.getProperty("os.name").toLowerCase()
 							+ ")");
 		}
 		if (aNickname == null) {
 			// load random name
 			aNickname = getRandomName();
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
 
 	public boolean getSfx()
 	{
 		return aSfx;
 	}
 
 	public boolean getSound()
 	{
 		return aSound;
 	}
 
 	public boolean loadSettings()
 	{
 		final Json j = Json.fromFile(getPreferencesFile());
 		try {
			if (j.hasAttribute("name")) {
				aNickname = j.getStringAttribute("name");
			}
			if (j.hasAttribute("sfx")) {
				aSfx = j.getBooleanAttribute("sfx");
			}
			if (j.hasAttribute("sound")) {
				aSound = j.getBooleanAttribute("sound");
			}
 		}
 		catch (final Exception e) {
 			LoggerUtils.getLogger().warning("Troubles reading the settings file");
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	public void setNickname(final String text)
 	{
 		aNickname = text;
 	}
 
 	public void setSfx(final boolean sfx)
 	{
 		aSfx = sfx;
 	}
 
 	public void setSound(final boolean sound)
 	{
 		aSound = sound;
 	}
 
 	@Override
 	public Json toJson()
 	{
 		final Json j = new Json();
 		j.setStringAttribute("name", aNickname);
 		j.setBooleanAttribute("sfx", aSfx);
 		j.setBooleanAttribute("sound", aSound);
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
