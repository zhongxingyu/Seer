 package zisko.multicastor.program.lang;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.swing.JOptionPane;
 
 @SuppressWarnings("serial")
 public class LanguageManager extends Properties{
 	
 	public final static String[] languages;
 	
 	static{
 		File dir = new File("Language");
 		languages = dir.list(new FilenameFilter() {
 		    public boolean accept(File d, String name) {
 		       return name.endsWith(".lang");
 		    }
 		});
 	}
 	
 	private static String currentLanguage;
 	private static LanguageManager instance;
 	private static String[] keys = {
 		"mi.autoSave",
 		"mi.changeWindowTitle",
 		"mi.language",
 		"mi.saveConfiguration",
 		"mi.loadConfiguration",
 		"mi.errorFileNotFound",
 		"mi.snake",
 		"mi.help",
 		"mi.exit",
 		"mi.about",
 		"mi.beginner",
 		"mi.custom",
 		"mi.menu",
 		"mi.options",
 		"mi.userLevel",
 		"mi.views",
 		"mi.layer2Receiver",
 		"mi.layer2Sender",
 		"mi.layer3Receiver",
 		"mi.layer3Sender",
 		"mi.info",
 		"tab.l2s",
 		"tab.l3s",
 		"tab.l2r",
 		"tab.l3r",
 		"tab.graph",
 		"tab.console",
 		"plus.l2rDescription",
 		"plus.l2sDescription",
 		"plus.l3rDescription",
 		"plus.l3sDescription",
 		"plus.aboutDescription",
 		"miscBorder.mcOverwiew",
 		"miscBorder.mcControl",
 		"miscBorder.mcConfig",
 		"miscBorder.ipGroupAddress",
 		"miscBorder.ipNetworkInterface",
 		"miscBorder.udpPort",
 		"miscBorder.packetRate",
 		"miscBorder.packetLength",
 		"miscBorder.timeToLive",
 		"miscBorder.ipGroupAddress",
 		"miscBorder.ipNetworkInterface",
 		"miscBorder.MacGroupAddress",
 		"miscBorder.NetworkInterface",
 		"button.start",
 		"button.stop",
 		"button.delete",
 		"button.deSelectAll",
 		"button.new",
 		"button.inactive",
 		"button.add",
 		"status.mcSelected",
 		"status.mcTotal",
 		"status.traffic",
 		"status.in",
 		"status.out",
 		"graph.time",
 		"graph.sec",
 		"graph.y",
 		"graph.jitter",
 		"graph.lostPackets",
 		"graph.measuredPacketRate",
 		"graph.packetsPerSec",
 		"graph.current"
 	};
 	
 	private LanguageManager(){
 		this("Language/english.lang");
 	}
 	
 	private LanguageManager(String currentLang){
 		currentLanguage=(new File(currentLang)).getName().replaceAll(".lang", "");
 		try {
 			loadLanguage(currentLang);
 		} catch (FileNotFoundException e) {
 			JOptionPane.showMessageDialog(null, "Can not find the language file at\n"+currentLang+"\nCan not start Multicastor.");
 			System.exit(1);
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(null, "Can not read the language file at\n"+currentLang+"\nCan not start Multicastor.");
 			System.exit(1);
 		} catch (InvalidLanguageFileException e) {
 			JOptionPane.showMessageDialog(null, "The language file at\n"+currentLang+"\nis invalid. Can not start Multicastor.\n Missing value "+e.getErrorKey()+" at index "+e.getErrorIndex()+".");
 			System.exit(1);
 		}
 	}
 
 	public static LanguageManager getInstance(){
 		if (instance==null)
 			return instance=new LanguageManager();
 		else
 			return instance;
 	}
 	
 	private void loadLanguage(String currentLangFile) throws FileNotFoundException, IOException, InvalidLanguageFileException{
 		FileInputStream fis=new FileInputStream(currentLangFile);
 		load(fis);
 		fis.close();
 		//Check the Language File
 		for (int i=0;i<keys.length;i++){
 			if(!containsKey(keys[i])){
 				throw new InvalidLanguageFileException(i,keys[i],keys); 
 			}
 		}
 	}
 
 	public static void setCurrentLanguage(String currentLanguage) {
 		if (instance==null) new LanguageManager("Language/"+currentLanguage+".lang");
 		else{
 		try {
 				getInstance().loadLanguage("Language/"+currentLanguage+".lang");
 			} catch (FileNotFoundException e) {
 				JOptionPane.showMessageDialog(null, "Can not find the language file at\nLanguage/"+currentLanguage+".lang\nCan not change language!");
 			} catch (IOException e) {
 				JOptionPane.showMessageDialog(null, "Can not read the language file at\nLanguage/"+currentLanguage+".lang\nCan not change language!");
 			} catch (InvalidLanguageFileException e) {
				//TODO: Hier muss noch was besseres gebaut werden!
 				JOptionPane.showMessageDialog(null, "The language file at\nLanguage/"+currentLanguage+".lang\nis invalid. Can not change language.\n Missing value "+e.getErrorKey()+" at index "+e.getErrorIndex()+".");
				System.exit(1);
 			}
 		}
 	}
 	
 	public static String getCurrentLanguage() {
 		return currentLanguage;
 	}
 }
