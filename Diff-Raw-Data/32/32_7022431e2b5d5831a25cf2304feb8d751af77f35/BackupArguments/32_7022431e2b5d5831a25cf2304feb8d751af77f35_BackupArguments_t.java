 package com.johnpickup.backup;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.beust.jcommander.Parameter;
 
 public class BackupArguments {
 	@Parameter(description = "<Source> <destination>")
 	private List<String> directories = new ArrayList<String>();
 
 	@Parameter(names = "-exclude", description = "Regex for files to exlude")
 	private List<String> exlusions = new ArrayList<String>();
 
 	@Parameter(names = "-noreport", description = "Don't output a report")
 	private boolean noreport = false;
 
	@Parameter(names = "-progress", description = "Display Progress")
	private boolean progress;

 	public List<String> getDirectories() {
 		return directories;
 	}
 
 	public List<String> getExlusions() {
 		return exlusions;
 	}
 
 	public boolean isNoreport() {
 		return noreport;
 	}

	public boolean isProgress() {
		return progress;
	}
 }
