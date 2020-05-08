 package scripting;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Random;
 
 import org.pircbotx.Channel;
 
 import connection.KEllyBot;
 
 public final class ScriptFunctions {
 
     private Random gen = new Random();
 	
     public final int rand(int i) {
     	return gen.nextInt(i);
     }
     
     public final int properRand(int i) {
     	return gen.nextInt(i)+1;
     }
 
 	public final int rand(int x, int y) {
 		return gen.nextInt(y - x) + x;
 	}
 	
 	public final boolean prob(int x) {
     	return !(gen.nextInt(100) + 1 > x);
     }
 	
 	public final void writeln(String s){
 		System.out.println(s);
 	}
 	
 	public final void write(String s){
 		System.out.print(s);
 	}
 	
 	public final void format(String s, Object... args){
 		System.out.format(s, args);
 	}
 
 	public final boolean string2File(String fileName, String data){
 		
 		return string2File(fileName, false, data);
 	}	
 	
 	public final boolean string2File(String fileName, boolean override, String data){
 		
 		File f = new File(fileName);
 		try {
 			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
 			bw.close();
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 		return false;
 	}
 	
 	public final String file2String(String fileName){
 
 		StringBuffer contents = new StringBuffer();
 		File f = new File(fileName);
 		if(!f.exists())return "";
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(f));
 			String text = null;
 			while((text = reader.readLine())!=null){
 				contents.append(text).append(System.getProperty("line.separator"));
 			}
 			reader.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return contents.toString();
 	}
 	
 	public final Channel findChannel(KEllyBot bot, String name){
 		return bot.getChannel(name);
 	}
 	
     public final String[] getArgs(String s, int args) {
 		String[] temp = s.split(" ");
 		String[] rVal = new String[args];
 		
 		for (int i = 0; i < args; i++) {
 			rVal[i] = temp[i];
 		}
 		
 		rVal[args - 1] = s.substring(s.lastIndexOf(temp[args - 1]));
 		
 		return rVal;
     }
     
     public final boolean checkArgs(String s, int args) {
 		return s.split(" ").length >= args;
     }
     
 	//TODO: sounds
 }
