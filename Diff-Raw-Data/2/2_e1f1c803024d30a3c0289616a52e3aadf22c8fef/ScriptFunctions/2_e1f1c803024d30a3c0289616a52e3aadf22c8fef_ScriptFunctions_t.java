 package scripting;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.file.Paths;
 import java.util.Random;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 import org.wishray.copernicus.NeptuneCore;
 import org.wishray.copernicus.Sound;
 
 import shared.Message;
 import shared.RoomManager;
 
 public final class ScriptFunctions {
 
 	private Random gen = new Random();
 
 	public final int rand(int i) {
 		return gen.nextInt(i);
 	}
 
 	public final int properRand(int i) {
 		return gen.nextInt(i) + 1;
 	}
 
 	public final int rand(int x, int y) {
 		return gen.nextInt(y - x) + x;
 	}
 
 	public final boolean prob(int x) {
 		return !(gen.nextInt(100) + 1 > x);
 	}
 
 	public final void writeln(String s) {
 		write(s+"\n");
 	}
 
 	public final void write(String s) {
 		System.out.print(s);
 	}
 	
 	public final void print(String s){
 		write(s);
 	}
 	
 	public final void println(String s){
 		writeln(s);
 	}
 
 	public final void format(String s, Object... args) {
 		System.out.format(s, args);
 	}
 
 	public final boolean string2File(String fileName, String data) {
 
 		return string2File(fileName, false, data);
 	}
 
 	public final boolean string2File(String fileName, boolean override,
 			String data) {
 
 		File f = new File(fileName);
 		try {
 			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
 			bw.close();
 		} catch (IOException ex) {
 			org.apache.log4j.Logger fLog = org.apache.log4j.Logger.getLogger("log.script.scriptfunctions");
 			fLog.error("string2file failed.", ex);
 		}
 		return false;
 	}
 
 	public final String file2String(String fileName) {
 
 		StringBuffer contents = new StringBuffer();
 		File f = new File(fileName);
 		if (!f.exists())
 			return "";
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(f));
 			String text = null;
 			while ((text = reader.readLine()) != null) {
 				contents.append(text).append(
 						System.getProperty("line.separator"));
 			}
 			reader.close();
 		} catch (Exception e){
 			org.apache.log4j.Logger fLog = org.apache.log4j.Logger.getLogger("log.script.scriptfunctions");
 			fLog.error("file2string failed.", e);
 		}
 
 		return contents.toString();
 	}
 
 	public final Channel findChannel(String name) {
 		return ScriptVars.curConnection.getChannel(name);
 	}
 	
 	public final User findUser(String name) {
 		for(User u : ScriptVars.curChannel.getUsers()){
 			if(u.getNick().toLowerCase().equals(name.toLowerCase())){
 				return u;
 			}
 		}
 		return null;
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
		if(s.length()==0)
			return false;
 		return s.split(" ").length >= args;
 	}
 
 	public final void beep() {
 		RoomManager.getMain().getDisplay().beep();
 	}
 
 	public final File[] flist(String path){
 		return Paths.get(path).toFile().listFiles();
 	}
 	
 	public final void error(String err) {
 		error("System",err);
 	}
 
 	public final void error(String sender, String err) {
 		RoomManager.enQueue(new Message(ScriptVars.curConnection, err,
 				sender, ScriptVars.curChannel.getName(), Message.CONSOLE));
 	}
 	
 	public final void invoke(String script, String function, String args){
 		for(Script s : ScriptManager.scripts){
 			if(s.getFunctions().contains(function)){
 				s.invoke(function, args);
 			}
 		}
 	}
 	
 	public final boolean playSound(String path){
 		File f = new File(path);
 		if(!f.exists()) return false;
 		
 		SoundData.curSound = new Sound();
 		SoundData.curSound.Load(path);
 		if(SoundData.curSound.GetError() != null ) return false;
 		
 		SoundData.curId = NeptuneCore.PlaySound(SoundData.curSound);
 		
 		return true;
 	}
 	
 	public final void stopSound() {
 	}
 }
