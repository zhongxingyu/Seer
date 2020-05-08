 package com.secondarybrain.boilerpipe;
 import gnu.getopt.Getopt;
 
import java.io.BufferedReader;
import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import de.l3s.boilerpipe.BoilerpipeProcessingException;
 import de.l3s.boilerpipe.extractors.ArticleExtractor;
 
 public class Main {
 	
 	public static enum Mode {
 		URL, File
 	}
 	
 	private Mode mode;
 	private String uri;
 	
 	private void init(String[] args) throws Exception {
     	Getopt g = new Getopt("boilerpipe", args, "u:f:");
     	
     	int c = 0;
     	while ((c = g.getopt()) != -1) {
     		switch(c) {
     		case 'u':
     			mode = Mode.URL;
     			uri = g.getOptarg();
     			break;
     		
     		case 'f':
     			mode = Mode.File;
     			uri = g.getOptarg();
     			break;
     		}
     	}
     	
     	if (mode != null) {
     		execute();
     	}
 	}
 	
	private void execute() throws FileNotFoundException, BoilerpipeProcessingException, MalformedURLException, UnsupportedEncodingException {
 		String text = null;
 		if (Mode.File.equals(mode)) {
			//Reader reader = new InputStreamReader(new FileInputStream(uri), "UTF-8");
			Reader reader = new FileReader(uri);
			text = ArticleExtractor.INSTANCE.getText(reader);
 		}
		else if (Mode.URL.equals(mode)) {
 			text = ArticleExtractor.INSTANCE.getText(new URL(uri));
 		}
		System.out.println(text);
 	}
 
     public static void main(String[] args) throws Exception {
     	new Main().init(args);
     }
 }
