 /*
  * @author Kyle Kemp
  */
 package scripting;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.file.Paths;
 import java.util.Random;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 import org.wishray.copernicus.NeptuneCore;
 import org.wishray.copernicus.Sound;
 import org.wishray.copernicus.SoundReference;
 
 import connection.KEllyBot;
 
 import shared.Message;
 import shared.RoomManager;
 
 /**
  * The Class ScriptFunctions.
  */
 public final class ScriptFunctions {
 
 	/** The random generator. */
 	private Random gen = new Random();
 
 	/**
 	 * Gets a random number between 0 and i-1.
 	 *
 	 * @param i the i
 	 * @return the int
 	 */
 	public final int rand(int i) {
 		return gen.nextInt(i);
 	}
 
 	/**
 	 * Gets a random number between 1 and i.
 	 *
 	 * @param i the i
 	 * @return the int
 	 */
 	public final int properRand(int i) {
 		return gen.nextInt(i) + 1;
 	}
 
 	/**
 	 * Gets a random number between x and y.
 	 *
 	 * @param x the x
 	 * @param y the y
 	 * @return the int
 	 */
 	public final int rand(int x, int y) {
 		return gen.nextInt(y - x) + x;
 	}
 
 	/**
 	 * Check if the given probability (0-100) passes.
 	 *
 	 * @param x the x
 	 * @return true, if successful
 	 */
 	public final boolean prob(int x) {
 		return !(gen.nextInt(100) + 1 > x);
 	}
 
 	/**
 	 * Write a line of text to the console (with newlines).
 	 *
 	 * @param s the s
 	 */
 	public final void writeln(String s) {
 		write(s + "\n");
 	}
 
 	/**
 	 * Write a line of text to the console (without newlines).
 	 *
 	 * @param s the s
 	 */
 	public final void write(String s) {
 		System.out.print(s);
 	}
 
 	/**
 	 * An alias for write().
 	 *
 	 * @param s the s
 	 */
 	public final void print(String s) {
 		write(s);
 	}
 
 	/**
 	 * An alias for writeln().
 	 *
 	 * @param s the s
 	 */
 	public final void println(String s) {
 		writeln(s);
 	}
 
 	/**
 	 * Wrapper for System.out.format().
 	 *
 	 * @param s the s
 	 * @param args the args
 	 */
 	public final void format(String s, Object... args) {
 		System.out.format(s, args);
 	}
 
 	/**
 	 * Turn a string into a file.
 	 *
 	 * @param fileName the file name
 	 * @param data the data
 	 * @return true, if successful
 	 */
 	public final boolean string2File(String fileName, String data) {
 
 		return string2File(fileName, false, data);
 	}
 
 	/**
 	 * Turn a string into a file.
 	 *
 	 * @param fileName the file name
 	 * @param override if true, will append to the file instead of overwriting it.
 	 * @param data the data
 	 * @return true, if successful
 	 */
 	public final boolean string2File(String fileName, boolean override,
 			String data) {
 
 		File f = new File(fileName);
 		try {
 			BufferedWriter bw = new BufferedWriter(new FileWriter(f, override));
 			bw.close();
 		} catch (IOException ex) {
 			org.apache.log4j.Logger fLog = org.apache.log4j.Logger
 					.getLogger("log.script.scriptfunctions");
 			fLog.error("string2file failed.", ex);
 		}
 		return false;
 	}
 
 	/**
 	 * Read a file into a string.
 	 *
 	 * @param fileName the file name
 	 * @return the string
 	 */
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
 		} catch (Exception e) {
 			org.apache.log4j.Logger fLog = org.apache.log4j.Logger
 					.getLogger("log.script.scriptfunctions");
 			fLog.error("file2string failed.", e);
 		}
 
 		return contents.toString();
 	}
 
 	/**
 	 * Find channel.
 	 *
 	 * @param name the name
 	 * @return the channel
 	 */
 	public final Channel findChannel(String name) {
 		return ScriptVars.curConnection.getChannel(name);
 	}
 
 	/**
 	 * Find user.
 	 *
 	 * @param name the name
 	 * @return the user
 	 */
 	public final User findUser(String name) {
 		for (User u : ScriptVars.curChannel.getUsers()) {
 			if (u.getNick().toLowerCase().equals(name.toLowerCase())) {
 				return u;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the args from a given string.
 	 *
 	 * @param s the s
 	 * @param args the args
 	 * @return the args
 	 */
 	public final String[] getArgs(String s, int args) {
 		String[] temp = s.split(" ");
 		String[] rVal = new String[args];
 
 		for (int i = 0; i < args; i++) {
 			rVal[i] = temp[i];
 		}
 
 		rVal[args - 1] = s.substring(s.lastIndexOf(temp[args - 1]));
 
 		return rVal;
 	}
 
 	/**
 	 * Check if the string contains a proper amount of arguments.
 	 *
 	 * @param s the s
 	 * @param args the args
 	 * @return true, if successful
 	 */
 	public final boolean checkArgs(String s, int args) {
 		if (s.length() == 0)
 			return false;
 		return s.split(" ").length >= args;
 	}
 
 	/**
 	 * Beep the computer.
 	 */
 	public final void beep() {
 		RoomManager.getMain().getDisplay().beep();
 	}
 
 	/**
 	 * List all files in a directory.
 	 *
 	 * @param path the path
 	 * @return the file[]
 	 */
 	public final File[] flist(String path) {
 		return Paths.get(path).toFile().listFiles();
 	}
 
 	/**
 	 * Write an error to the screen.
 	 *
 	 * @param err the err
 	 */
 	public final void error(String err) {
 		error(KEllyBot.systemName, err);
 	}
 
 	/**
 	 * Write an error to the screen.
 	 *
 	 * @param sender the sender
 	 * @param err the err
 	 */
 	public final void error(String sender, String err) {
 		RoomManager.enQueue(new Message(ScriptVars.curConnection, err, sender,
 				ScriptVars.curChannel.getName(), Message.CONSOLE));
 	}
 
 	/**
 	 * Invoke a function from another script.
 	 *
 	 * @param script the script
 	 * @param function the function
 	 * @param args the args
 	 */
 	public final void invoke(String script, String function, String args) {
 		for (Script s : ScriptManager.scripts) {
 			if (s.getFunctions().contains(function)
 					&& s.getName().equals(script)) {
 				s.invoke(function, args);
 			}
 		}
 	}
 
 	/**
 	 * Alias for invoke.
 	 *
 	 * @param script the script
 	 * @param function the function
 	 * @param args the args
 	 */
 	public final void invokefrom(String script, String function, String args) {
 		invoke(script, function, args);
 	}
 
 	/**
 	 * Play sound.
 	 *
 	 * @param path the path
 	 * @return true, if successful
 	 */
 	public final boolean playSound(String path) {
 		File f = new File(path);
 		if (!f.exists())
 			return false;
 
 		SoundData.curSound = new Sound();
 		SoundData.curSound.Load(path);
 		if (SoundData.curSound.GetError() != null)
 			return false;
 
 		SoundData.curId = NeptuneCore.PlaySound(SoundData.curSound);
 		SoundData.soundRef = new SoundReference(SoundData.curId);
 		SoundData.isPaused = false;
 
 		return true;
 	}
 
 	/**
 	 * Stop sound.
 	 */
 	public final void stopSound() {
 		if (SoundData.soundRef == null)
 			return;
 		SoundData.soundRef.Stop();
 	}
 
 	/**
 	 * Pause sound.
 	 */
 	public final void pauseSound() {
 		if (SoundData.soundRef == null)
 			return;
 		if (SoundData.isPaused) {
 			SoundData.soundRef.Play();
 		} else {
 			SoundData.soundRef.Pause();
 		}
 	}
 
 	/**
 	 * Open file dialog.
 	 *
 	 * @return the string
 	 */
 	public final String openFileDialog() {
 		return new FileDialog(RoomManager.getMain().getShell(), SWT.OPEN)
 				.open();
 	}
 
 	/**
 	 * Save file dialog.
 	 *
 	 * @return the string
 	 */
 	public final String saveFileDialog() {
 		return new FileDialog(RoomManager.getMain().getShell(), SWT.SAVE)
 				.open();
 	}

 }
