 package org.vinodkd.jnv;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.ArrayList;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.InputStream;
 import java.io.IOException;
 
 public class Notes implements Model{
 	private HashMap<String,Note> notes = new HashMap<String,Note>();
 
 	private final String ABOUTNOTE_lOC = "res/about.txt";
 	public Notes() {}
 
 	public void add(Note n){ notes.put(n.getTitle(),n);	}
 	public void set(String title, String contents) {
 		Note n = new Note(title,contents);
 		notes.put(title,n);
 	}
 
 	public Note get(String title)	{ return notes.get(title);}
 
 	public void load(){
 		// read from file here
 	}
 
 	public void store(){
 		// write to file here
 	}
 
 	public Object getInitialValue(){
 		load();
 		Note aboutNote = new Note("About", getAboutText());
 		add(aboutNote);
 		return notes;
 	}
 
 	private String getAboutText(){
 		InputStream is = getClass().getClassLoader().getResourceAsStream(ABOUTNOTE_lOC);
 		BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
 		StringBuffer aboutText = new StringBuffer("");
 		String line;
 		try{
 			while((line=br.readLine())!=null){
				aboutText.append(line);
 			}
 		}catch(IOException ioe){
 			// TODO: PUT IN DEFAULT TEXT IF THIS FAILS
 		}
 		return aboutText.toString();
 
 	}
 
 	public Object getCurrentValue(){
 		return notes;
 	}
 
 	public List<String> search(String searchText){
 		ArrayList<String> searchResult = new ArrayList<String>();
 		Set<String> titles = notes.keySet();
 		for(String title: titles){
 			if(title.contains(searchText)){
 				searchResult.add(title);
 			}
 		}
 		return searchResult;
 	}
 }
