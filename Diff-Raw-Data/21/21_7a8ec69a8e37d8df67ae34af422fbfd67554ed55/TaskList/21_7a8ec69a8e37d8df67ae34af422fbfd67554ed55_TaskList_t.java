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
import java.util.Calendar;
 import java.util.Collections;
import java.util.Comparator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class TaskList {
 	String lastFilter = "";
 	File file = null;
 	ArrayList<Task> mainList = new ArrayList<Task>();
 	ArrayList<Task> filterList = new ArrayList<Task>();
 	ArrayList<String> tagList = new ArrayList<String>();
 	ArrayList<String> tagFilters = new ArrayList<String>();
 
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
 			Collections.sort(this.mainList);
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
 		Collections.sort(this.mainList);
 
 		this.generateTagList();
 	}
 	public void add(Task source) {
 		this.mainList.add(source);
 		Collections.sort(this.mainList);
 
 		this.generateTagList();
 	}
 	public void remove(int id) {
 		this.mainList.remove(this.filterList.get(id));
 		Collections.sort(this.mainList);
 
 		this.generateTagList();
 		for(int i = 0; i < this.tagFilters.size(); i++) {
 			if(!this.tagList.contains(this.tagFilters.get(i))) {
 				this.tagFilters.remove(i);
 				i--;
 			}
 		}
 	}
 
 	public void generateTagList() {
 		this.tagList.clear();
 		for(int i = 0; i < this.mainList.size(); i++) {
 			String[] tags = this.mainList.get(i).getTags();
 			for(int j = 0; j < tags.length; j++) {
 				char type = tags[j].charAt(0);
 				switch(type) {
 					case '+':
 					case '@':
 					case '#':
 					case '!':
 						String[] subtags = tags[j].substring(1).split("/");
 						for(int k = 0; k < subtags.length; k++) {
 							if(this.tagList.contains(type + subtags[k])) continue;
 							this.tagList.add(type + subtags[k]);
 						}
 						break;
 					default:
 						if(this.tagList.contains(tags[j])) continue;
 						this.tagList.add(tags[j]);
 				}
 			}
 		}
		Collections.sort(this.tagList, new Comparator<String>() {
			@Override
			public int compare(String one, String two) {
				Calendar c1 = Task.matcherToCalendar(Task.re_date.matcher(one));
				Calendar c2 = Task.matcherToCalendar(Task.re_date.matcher(two));
				if(!c1.equals(c2)) {
					return c2.compareTo(c1);
				}

				if(one.charAt(0) == '!' && two.charAt(0) == '!') {
					return two.compareTo(one);
				}

				return one.compareTo(two);
			}
		});
 	}
 
 	public void addTagFilter(String tag) {
 		this.tagFilters.add(tag);
 	}
 	public void removeTagFilter(String tag) {
 		if(this.tagFilters.contains(tag)) {
 			this.tagFilters.remove(tag);
 		}
 	}
 	public boolean hasTagFilter(String tag) {
 		return this.tagFilters.contains(tag);
 	}
 	public void clearTagFilter() {
 		this.tagFilters.clear();
 	}
 
 	public void filter(String search) {
 		this.lastFilter = search;
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
 	public void filter() {
 		this.filter(this.lastFilter);
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
