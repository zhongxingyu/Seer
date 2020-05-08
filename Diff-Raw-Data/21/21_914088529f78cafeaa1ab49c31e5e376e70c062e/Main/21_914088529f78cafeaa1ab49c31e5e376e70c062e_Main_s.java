 package com.secondarybrain.boilerpipe;
 import gnu.getopt.Getopt;
 
import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
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
 	
	private void execute() throws FileNotFoundException, BoilerpipeProcessingException, MalformedURLException {
 		String text = null;
 		if (Mode.File.equals(mode)) {
			text = ArticleExtractor.INSTANCE.getText(new FileReader(new File(uri)));
 		}
		else if (Mode.URL.equals(mode)) {;
 			text = ArticleExtractor.INSTANCE.getText(new URL(uri));
 		}
        System.out.println(text);
 	}
 
     public static void main(String[] args) throws Exception {
     	new Main().init(args);
     }
 }
