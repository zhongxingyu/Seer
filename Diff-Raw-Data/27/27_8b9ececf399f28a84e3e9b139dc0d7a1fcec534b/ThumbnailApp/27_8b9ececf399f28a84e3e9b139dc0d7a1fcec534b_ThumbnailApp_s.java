 package com.jasonwjones.jumbnail;
 
 import com.beust.jcommander.Parameter;
 
 public class ThumbnailApp {
 
 	@Parameter(names = {"-inputFolder", "-i"}, description = "Source folder to look in for image files", required = true)
 	private String inputFolder;
 	
 	@Parameter(names = {"-outputFolder", "-o"}, description = "Output foder to place generated thumbnails", required = true)
 	private String outputFolder;
 	
 	@Parameter(names = {"-skipExisting", "-s"}, description = "Skip conversion if the target file exists.", required = false)
	private boolean skipExisting = true;
 
 	@Parameter(names = {"-width", "-w"}, description = "Width of output file", required = true)
 	private int width;
 	
 	@Parameter(names = {"-height", "-h"}, description = "Height of output file", required = true)
 	private int height;
 	
 	
 	public String getInputFolder() {
 		return inputFolder;
 	}
 
 	public void setInputFolder(String inputFolder) {
 		this.inputFolder = inputFolder;
 	}
 
 	public String getOutputFolder() {
 		return outputFolder;
 	}
 
 	public void setOutputFolder(String outputFolder) {
 		this.outputFolder = outputFolder;
 	}
 
 	public boolean isSkipExisting() {
 		return skipExisting;
 	}
 
 	public void setSkipExisting(boolean skipExisting) {
 		this.skipExisting = skipExisting;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public void setHeight(int height) {
 		this.height = height;
 	}
 	
 }
