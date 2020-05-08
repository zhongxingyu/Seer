 package com.cvparse.process;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Map;
 
 public class JavaCompileCommand {
 
 	public String compile(String dir, String s) throws IOException {
 		createdir(dir);
 		write(dir, s);
 		return executeCompile(dir, s);
 	}
 
 	private void createdir(String dir) {
 		File dirFile = new File("/tmp/" + dir);
 		if (!dirFile.exists()) {
 			dirFile.mkdir();
 		}
 	}
 	
 	private void write(String dir, String content) {
 		try {
 			File file = new File("/tmp/" + dir + "/Test.java");
 			
 			// if file doesn't exists, then create it
 			if (!file.exists()) {
 				file.createNewFile();
 			}
 
 			FileWriter fw = new FileWriter(file.getAbsoluteFile());
 			BufferedWriter bw = new BufferedWriter(fw);
 			bw.write(content);
 			bw.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 		
 	private String executeCompile(String dir, String s) throws IOException {
 		ProcessBuilder pb = new ProcessBuilder("javac", "/tmp/" + dir + "/Test.java");
 		Map<String, String> env = pb.environment();
 		//env.put("VAR1", "myValue");
 		//env.remove("OTHERVAR");
 		//env.put("VAR2", env.get("VAR1") + "suffix");
 		pb.directory(new File("/tmp/" + dir));
 		File log = new File("log");
 		pb.redirectErrorStream(true);
 		//pb.redirectOutput(log);
 		Process p = pb.start();
 		//assert pb.redirectInput() == Redirect.PIPE;
 		//assert pb.redirectOutput().file() == log;
 		assert p.getInputStream().read() == -1;
 
 		InputStream stdout = p.getInputStream ();
 		BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));
 		
 		String retVal = "";
 		String line = "";
 		while ((line = reader.readLine ()) != null) {
 			retVal += ("".equals(retVal) ? "" : "\n") + line;
 			//System.out.println ("Stdout: " + line);
 		}
 		
		return retVal == null ? "compiled successfully" : retVal;
 	}
 }
