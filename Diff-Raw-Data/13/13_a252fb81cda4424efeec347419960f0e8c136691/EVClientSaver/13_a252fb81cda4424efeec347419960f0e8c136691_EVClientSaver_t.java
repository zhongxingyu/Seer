 package com.evervoid.client;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.evervoid.json.Json;
 import com.evervoid.state.EVGameState;
 
 public class EVClientSaver
 {
 	private static EVClientSaver sInstance = null;
 	public static final String sSaveFileExtension = ".evervoid";
 
 	public static List<File> getAvailableSaveFiles()
 	{
 		return getInstance().getSaveFiles();
 	}
 
 	private static EVClientSaver getInstance()
 	{
 		if (sInstance == null) {
 			sInstance = new EVClientSaver();
 		}
 		return sInstance;
 	}
 
 	public static File getSaveFilesDirectory()
 	{
 		final File directory = EverVoidClient.getSettings().getAppData();
 		if (!directory.isDirectory()) {
 			if (directory.exists()) {
 				directory.delete();
 			}
 			directory.mkdirs();
 		}
 		return directory;
 	}
 
 	public static boolean save(final File file, final EVGameState state)
 	{
 		return getInstance().saveGame(file, state);
 	}
 
 	private EVClientSaver()
 	{
 	}
 
 	private List<File> getSaveFiles()
 	{
 		final File[] children = getSaveFilesDirectory().listFiles(new FilenameFilter()
 		{
 			@Override
 			public boolean accept(final File dir, final String name)
 			{
				return name.toLowerCase().endsWith(sSaveFileExtension.toLowerCase());
 			}
 		});
 		if (children == null) {
 			return new ArrayList<File>();
 		}
 		final List<File> saveFiles = new ArrayList<File>(children.length);
 		for (final File f : children) {
 			saveFiles.add(f);
 		}
 		return saveFiles;
 	}
 
 	public Json loadGame(final String filename)
 	{
 		return Json.fromFile(new File(getSaveFilesDirectory(), filename));
 	}
 
 	private boolean saveGame(final File file, final EVGameState state)
 	{
 		return state.toJson().toFile(file);
 	}
 }
