 package com.jpqr.listpad.models;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.os.Environment;
 
 public class Checklist extends ArrayList<String> {
 
 	private static final long serialVersionUID = -4776403069516340972L;
	public static final String DEFAULT_DIRECTORY = Environment.getExternalStorageDirectory().toString() + "/ListPad/";
 	private String mTitle;
 	private File mFile;
 	private String mDelimiter = "\n";
 	private String mOriginalText;
 
 	public Checklist() {
 		this.mTitle = "";
 		this.mFile = new File(DEFAULT_DIRECTORY, "untitled.txt");
 	}
 
 	public Checklist(File file) throws IOException, FileNotFoundException {
 		this.mFile = file;
 		this.mTitle = mFile.getName();
 
 		BufferedReader reader = new BufferedReader(new FileReader(mFile));
 		String line;
 		while ((line = reader.readLine()) != null) {
 			add(line);
 		}
 		reader.close();
 		
 		mOriginalText = toString();
 	}
 	
 	public void toFile() throws IOException {
 		if (!mTitle.equals(mFile.getName())) {
 			File newFile = new File(mFile.getParentFile().getPath(), mTitle);
 			mFile.renameTo(newFile);
 			mFile = newFile;
 		}
 		BufferedWriter writer = new BufferedWriter(new FileWriter(mFile));
 		ArrayList<String> list = getList();
 		for (String item : list) {
 			writer.write(item.trim() + mDelimiter);
 		}
 		writer.close();
 		
 		mOriginalText = toString();
 	}
 
 	public ArrayList<String> getList() {
 		return this;
 	}
 
 	public void fromString(String items) {
 		String[] itemsArray = items.split(mDelimiter);
 		ArrayList<String> list = getList();
 		list.clear();
 		for (String item : itemsArray) {
 			list.add(item.trim());
 		}
 	}
 	
 	@Override
 	public String toString() {
 		ArrayList<String> list = getList();
 		
 		if (list.isEmpty()) {
 			return "";
 		}
 		
 		StringBuilder stringBuilder = new StringBuilder();
 		for (String item : list) {
 			stringBuilder.append(item.trim() + mDelimiter);
 		}
 		
 		return stringBuilder.substring(0, stringBuilder.length()-mDelimiter.length()).toString();
 	}
 
 	public String getTitle() {
 		return mTitle;
 	}
 
 	public void setTitle(String mTitle) {
 		this.mTitle = mTitle;
 	}
 
 	public boolean delete() {
 		return mFile.delete();
 	}
 	
 	public boolean isModified() {
 		return !(mOriginalText.equals(toString()));
 	}
 }
