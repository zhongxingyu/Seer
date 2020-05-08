 package JPushy.LevelServer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.net.DatagramPacket;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import JPushy.MultiPlayer.ICommandHandler;

 /**
  * 
  * @author Marcel Benning
  * 
  */
 public class LevelServerCmdHandler implements ICommandHandler {
 
 	LevelServer	levelServer;
 
 	public LevelServerCmdHandler(LevelServer levelServer) {
 		this.levelServer = levelServer;
 	}
 
 	String	rgetLevels			= "^--[gG][eE][tT][lL][eE][vV][eE][lL][sS]$";
 	String	rloadLevel			= "^--[lL][oO][aA][dD][lL][eE][vV][eE][lL]#([a-zA-Z\\s]{1,})";
 	String	rloadLevelConfig	= "^--[lL][oO][aA][dD][lL][eE][vV][eE][lL][cC][oO][nN][fF][iI][gG]#([a-zA-Z\\s]{1,})";
 
	String	errorLevel			= "<level name=\"error\" version='0.1'>\n" + "<stage id=0>\n" + "11111111111111111111111\n" + "10000000000000000000001\n" + "10111011001100111011001\n" + "10100010101010101010101\n" + "10111010101010101010101\n" + "10100011001100101011001\n" + "10111010101010111010101\n" + "10000000000000000000001\n" + "11111111111111111111111\n" + "</stage>\n";

 	private void handleCommand(String msg, DatagramPacket packet) {
 		if (msg.matches(rgetLevels)) {
 			getLevels(msg, packet);
 		} else if (msg.matches(rloadLevel)) {
 			loadLevel(msg, packet);
 		} else if (msg.matches(rloadLevelConfig)) {
 			loadLevelConfig(msg, packet);
 		}
 	}
 
 	private void getLevels(String msg, DatagramPacket packet) {
 		levelServer.getLevelConnectionHandler().sendPacket(packet, levelServer.getLevelHandler().buildLevelDataString());
 	}
 
 	private void loadLevel(String msg, DatagramPacket packet) {
 		Pattern p = Pattern.compile(rloadLevel);
 		Matcher m = p.matcher(msg);
 		if (m.matches()) {
 			String name = m.group(1);
 			levelServer.getLevelConnectionHandler().sendPacket(packet, packFile(name));
 		}
 	}
 
 	private void loadLevelConfig(String msg, DatagramPacket packet) {
 		Pattern p = Pattern.compile(rloadLevelConfig);
 		Matcher m = p.matcher(msg);
 		if (m.matches()) {
 			String name = m.group(1);
 			levelServer.getLevelConnectionHandler().sendPacket(packet, packConfigFile(name));
 		}
 	}
 
 	public String packFile(String levelname) {
 		File dataFolder = new File(levelServer.getLevelHandler().getPath());
 		for (File f : dataFolder.listFiles()) {
 			if (!f.isDirectory()) {
 				String tmp = f.getName();
 				if (tmp.endsWith(".lvl")) {
 					LevelData data = levelServer.getLevelHandler().readLevelData(tmp);
 					if (data != null) {
 						if (data.getName().equals(levelname)) {
 							BufferedReader reader;
 							try {
 								System.out.println("File : " + f.getAbsoluteFile().toString());
 								reader = new BufferedReader(new FileReader(f));
 								String line;
 								String lines = "";
 								while ((line = reader.readLine()) != null) {
 									lines += line + "\n";
 								}
 								reader.close();
 								return lines;
 							} catch (Exception e) {
 								e.printStackTrace();
 							}
 						}
 					}
 				}
 			}
 		}
		return errorLevel;
 	}
 
 	public String packConfigFile(String levelname) {
 		File dataFolder = new File(levelServer.getLevelHandler().getPath());
 		for (File f : dataFolder.listFiles()) {
 			if (!f.isDirectory()) {
 				String tmp = f.getName();
 				if (tmp.endsWith(".lvl")) {
 					LevelData data = levelServer.getLevelHandler().readLevelData(tmp);
 					if (data != null) {
 						if (data.getName().equals(levelname)) {
 							BufferedReader reader;
 							try {
 								System.out.println("File : " + f.getAbsoluteFile().toString().replace(".lvl", ".cfg"));
 								reader = new BufferedReader(new FileReader(new File(f.getAbsoluteFile().toString().replace(".lvl", ".cfg"))));
 								String line;
 								String lines = "";
 								while ((line = reader.readLine()) != null) {
 									lines += line + "\n";
 								}
 								reader.close();
 								return lines;
 							} catch (Exception e) {
 								e.printStackTrace();
 							}
 						}
 					}
 				}
 			}
 		}
 		return "";
 	}
 
 	@Override
 	public void onCommand(String msg, DatagramPacket packet) {
 		handleCommand(msg, packet);
 	}
 
 	@Override
 	public void onCommand(String[] args, DatagramPacket packet) {
 	}
 }
