 package com.soofw.trk;
 
 import android.util.Log; // FIXME
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class TaskList {
 	final private static Pattern re_tag = Pattern.compile("(^|\\s)([\\@\\#\\+]([\\w\\/]+))");
 	final private static Pattern re_at = Pattern.compile("(^|\\s)(\\@([\\w\\/]+))");
 	final private static Pattern re_hash = Pattern.compile("(^|\\s)(\\#([\\w\\/]+))");
 	final private static Pattern re_plus = Pattern.compile("(^|\\s)(\\+([\\w\\/]+))");
 
 	private File file= null;
 	private ArrayList<Task> mainList = new ArrayList<Task>();
 	private ArrayList<Task> filterList = new ArrayList<Task>();
 	private ArrayList<String> tagList = new ArrayList<String>();
 	private ArrayList<String> tagFilters = new ArrayList<String>();
 
 	public TaskList(File file) {
 		this.file = file;
 	}
 
 	public void read() {
 		try {
 			String line = null;
 			BufferedReader reader = new BufferedReader(new FileReader(this.file));
 
 			while(true) {
 				line = reader.readLine();
 				if(line == null) break;
 
 				this.mainList.add(new Task(line));
 			}
 			this.filterList.addAll(this.mainList);
 			this.generateTagList();
 
 			reader.close();
 		} catch(FileNotFoundException e) {
 			// FIXME
 			Log.e("TRK", e.getMessage());
 		} catch(IOException e) {
 			// FIXME
 			Log.e("TRK", e.getMessage());
 		}
 	}
 
 	public void write() {
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(this.file));
 
 			// FIXME sort
 			for(int i = 0; i < this.mainList.size(); i++) {
 				writer.write(this.mainList.get(i).source + "\n");
 			}
 
 			writer.flush();
 			writer.close();
 		} catch(IOException e) {
 			// FIXME
 			Log.e("TRK", e.getMessage());
 		}
 	}
 
 	public void add(String source) {
 		this.mainList.add(new Task(source));
 		this.generateTagList();
 	}
 	public void add(Task source) {
 		this.mainList.add(source);
 		this.generateTagList();
 	}
 	public void remove(int id) {
		this.mainList.remove(id);
 		this.generateTagList();
 	}
 
 	public void generateTagList() {
 		this.tagList.clear();
 		for(int i = 0; i < this.mainList.size(); i++) {
 			Matcher m = null;
 
 			m = re_tag.matcher(this.mainList.get(i).source);
 			while(m.find()) {
 				char type = m.group(2).charAt(0);
 				String[] subtags = m.group(2).substring(1).split("/");
 				for(int j = 0; j < subtags.length; j++) {
 					if(this.tagList.contains(type + subtags[j])) continue;
 					this.tagList.add(type + subtags[j]);
 				}
 			}
 		}
 		Collections.sort(this.tagList);
 	}
 
 	public void addTagFilter(String tag) {
 		this.tagFilters.add(tag);
 	}
 	public void removeTagFilter(String tag) {
 		if(this.tagFilters.contains(tag)) {
 			this.tagFilters.remove(tag);
 		}
 	}
 	public void setTagFilter(String tag) {
 		this.tagFilters.clear();
 		this.tagFilters.add(tag);
 	}
 	public void clearTagFilter() {
 		this.tagFilters.clear();
 	}
 
 	public void filter(String search) {
 		this.filterList.clear();
 		for(int i = 0; i < this.mainList.size(); i++) {
 			if(!this.mainList.get(i).contains(search)) continue;
 			if(this.tagFilters.size() > 0) {
 				boolean add = false;
 				for(int j = 0; j < this.tagFilters.size(); j++) {
 					if(this.mainList.get(i).matches(this.tagFilters.get(j))) {
 						add = true;
 						break;
 					}
 				}
 				if(!add) continue;
 			}
 
 			this.filterList.add(this.mainList.get(i));
 		}
 	}
 
 	public ArrayList<Task> getMainList() {
 		return this.mainList;
 	}
 	public ArrayList<Task> getFilterList() {
 		return this.filterList;
 	}
 
 	public ArrayList<String> getTagList() {
 		return this.tagList;
 	}
 }
