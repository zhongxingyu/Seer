 package enduro;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import enduro.racer.InputHandler;
 import enduro.racer.configuration.ConfigParser;
 
 /**
  * this is the main class. it parses a command line of the type -COMMAND KEY
  * where COMMAND may be config, list and output.
  * The config file is which config file which should be read.
  * The input file is the file which the program reads from, which previous consistency
  * (first file: name file, second file: start file and the following lines finish time files)
  * The output is where the output should be written
  * The result files listed in list.txt are outputted as html files 
  */
 public class MainClass {
 	
 	public static boolean debug = false;
 	
 	public static void main(String[] args) {
 		for(String arg: args)
 			System.out.println(arg);
 		String input = "list.txt";
 		String output = "result.txt";
 		// String html = "";
 		for(int i = 0; i < args.length; i++) {
 			if(i+1 < args.length) {
 				if(args[i].compareTo("-config")==0) {
 					ConfigParser.getInstance(args[i+1]);
 				} else if(args[i].compareTo("-list")==0) {
 					input = args[i+1];
 				} else if(args[i].compareTo("-output")==0) {
 					output = args[i+1];
 				/*
 				} else if(args[i].compareTo("-html")==0) {
 					html = args[i+1];*/
 				} else if(args[i].equals("-debug")) {
 					if(args[i+1].equals("true")) {
 						debug = true;
 					}
 				}
 			}
 		}
 		
 		InputHandler handler = new InputHandler();
 		
 		String[] files = getLines(input);
 		if(files.length >= 3) {
 			
 			//there is a name, start and finish file
 			if(files[0].startsWith("@Advanced")) {
 				
 				System.out.println("advanced list parsing activated");
 				for(String line: files) {
 					String temp = line;
 					if(line.startsWith("name:")) {
 						temp = temp.substring(5);
 						System.out.println("namefile: " + temp);
 						handler.addNameFile(temp);
 					} else if(line.startsWith("start:")) {
 						temp = temp.substring(6);
 						System.out.println("startfile: " + temp);
 						handler.addStartFile(temp);
 					} else if(line.startsWith("finish:")) {
 						temp = temp.substring(7);
 						System.out.println("finishfile: " + temp);
 						handler.addFinishFile(temp);
 					} else {
						if(!line.startsWith("@Advanced"))
							System.out.println("ERROR: line in list file is unparsable: " + line);
 					}
 				}
 			} else {
 				handler.addNameFile(files[0]);
 				handler.addStartFile(files[1]);
 				for(int i = 2; i < files.length; i++) {
 					handler.addFinishFile(files[i]);
 				}
 			}
 			
 			
 			try {
 				FileWriter writer = new FileWriter(output);
 				writer.append(handler.print());
 				writer.close();
 				for (int i = 0; i < files.length; i++) {
 					String[] filesNames = files[i].split("/");
 					String fileName = filesNames[filesNames.length-1];
 					TxtToHtml htmlWriter = new TxtToHtml();
 					System.out.println(fileName);
 					htmlWriter.makeHtmlFile(files[i], fileName+".html");
 				}
 							
 			} catch (IOException e) {
 				System.out.println("unable to write to output file");
 			}
 		} else {
 			System.out.println("unable to load calculations - file list is incomplete");
 		}
 		if(!debug)
 			JOptionPane.showMessageDialog(null, "Sortering klar!");
 	}
 
 	/**
 	 * a very basic filereader which reads a file and returns linewise what it reads as an array of Strings.
 	 * 
 	 * @param fileLocation the relative / absolute path to the file to be read.
 	 * @return an array of strings, the linewise content of the file.
 	 */
 	private static String[] getLines(String fileLocation) {
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(fileLocation));
 			
 			ArrayList<String> res = new ArrayList<String>();
 			
 			while(reader.ready())
 				res.add(reader.readLine());
 			
 			return res.toArray(new String[res.size()]);
 		} catch(Exception E) {
 			System.out.println("file not found: " + fileLocation);
 			return new String[]{};
 		}
 		
 	}
 }
