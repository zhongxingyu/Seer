 package com.david.fileSystem;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 public class SaveFile<E>{
 
 	private TextFile textFile;
 	private TextTransformer<E> transformer;
 	
 	public SaveFile(URL path, TextTransformer<E> transformer) {
 		this(new File(path.getFile()), transformer);
 	}
 
 	public SaveFile(String path, TextTransformer<E> transformer) {
 		this(new File(path), transformer);
 	}
 
 	public SaveFile(File f, TextTransformer<E> transformer) {
 		this(new TextFile(f), transformer);
 	}
 
 	public SaveFile(TextFile f, TextTransformer<E> transformer) {
 		textFile = f;
 		this.transformer = transformer;
 	}
 
 	public void save(List<E> data) throws IOException {
 		String save = "";
 		for(E w : data){
 			save += transformer.toWrite(w) + "\n";
 		}
 		textFile.override(save);
 	}
 	
 	public List<E> load() throws IOException {
		String[] lines = textFile.getLines();
 		List<E> data = new ArrayList<E>();
 		for (int i = 0; i < lines.length; i++) {
 			data.add(transformer.fromRead(lines[i]));
 		}
 		return data;
 	}
 		
 	public boolean saveSucces(List<E> data) {
 		try {
 			save(data);
 			return true;
 		} catch(IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public boolean fileExsist(){
 		return getTextFile().getExists();
 	}
 	
 	public TextFile getTextFile() {
 		return textFile;
 	}
 	
 }
