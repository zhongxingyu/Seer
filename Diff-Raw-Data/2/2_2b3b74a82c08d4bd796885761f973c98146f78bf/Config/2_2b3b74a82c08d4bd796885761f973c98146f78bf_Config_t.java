 package pack;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import javax.swing.JOptionPane;
 
 public enum Config {
 	
 	INSTANCE;
 	
 	private ArrayList<myProp> Props = new ArrayList<myProp>();
 	
 	private static final String  iniConfigFile = "dataSystem/ini.properties";
 	
 	private class myProp {
 		private String		configFile;
 		public Properties	prop;
 		private boolean		writable;
 		
 		public myProp(String f, boolean w) {
 			setConfigFile(f);
 			setWritable(w);
 			prop = new Properties();
 			System.out.println("[Config] Read "+f+" !");
 			try {
 				prop.load(new FileInputStream(f));
 			} catch (FileNotFoundException e1) {
 				System.out.println("[ERR]Config File " + f + " Not Found for reading");
 				JOptionPane.showMessageDialog(null, "Fichier de configuration " + f +
 						"\nnon trouv pour lecture!", "Erreur", JOptionPane.ERROR_MESSAGE);
 				return;
 			} catch (IOException e) {
 				System.out.println("[ERR]Config Read Error" + f);
 				JOptionPane.showMessageDialog(null, "Erreur de lecture de du fichier " + f +
 						" !", "Erreur", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 		}
 		public boolean contains(String param) throws ConfigException {
 			return prop.containsKey(param);
 		}
 		public String get(String param) throws ConfigException {
 			if (prop.containsKey(param)) {
 				return prop.getProperty(param); 
 			}
 			else {
 				throw new ConfigException("key not found");
 			}
 		}
 		public int getI(String param) throws ConfigException {
 			if (prop.containsKey(param)) {
 				return Integer.parseInt(prop.getProperty(param)); 
 			}
 			else {
 				throw new ConfigException("key not found");
 			}
 		}
 		public float getF(String param) throws ConfigException {
 			if (prop.containsKey(param)) {
 				return Float.parseFloat(prop.getProperty(param)); 
 			}
 			else {
 				throw new ConfigException("key not found");
 			}
 		}
 		public boolean getB(String param) throws ConfigException {
 			if (prop.containsKey(param)) {
 				return prop.getProperty(param).equalsIgnoreCase("true");
 			}
 			else {
 				throw new ConfigException("key not found");
 			}
 		}
 		public void set(String param, String val) throws ConfigException {
 			if (prop.containsKey(param)) {
 				prop.setProperty(param,val);
 			}
 			else {
 				throw new ConfigException("key not found");
 			}
 		}
 		public boolean isWritable() {
 			return writable;
 		}
 		public void setWritable(boolean writable) {
 			this.writable = writable;
 		}
 		public String getConfigFile() {
 			return configFile;
 		}
 		public void setConfigFile(String configFile) {
 			this.configFile = configFile;
 		}
 		public void write() {
 			try {
 				this.prop.store(new FileOutputStream(this.configFile), null);
 			} catch (FileNotFoundException e) {
 				System.out.println("[ERR] Config File Not Found for writing");
 				JOptionPane.showMessageDialog(null, "Fichier de configuration " + this.configFile +
 						"\nnon trouv pour criture!", "Erreur", JOptionPane.ERROR_MESSAGE);
 			} catch (IOException e) {
 				System.out.println("[ERR] Config Write Error");
 				JOptionPane.showMessageDialog(null, "Erreur d'criture du fichier de configuration !",
 						"Erreur", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 
 	private Config() {
 		//System.out.println("[Config()] : Init "+iniConfigFile+" !");
 		myProp iniProp = new myProp(iniConfigFile,false);
 		Props.add(iniProp);
 		
 		try {
 			int nFiles = iniProp.getI("conf.files");
 			for(int i=1; i<=nFiles; i++) {
 				String file = iniProp.get("conf.dir") + iniProp.get("conf.file."+i) +".properties";
 				//System.out.println("[Config()] : Init "+file+" !");
 				myProp prop = new myProp(file,iniProp.getB("conf.save."+i));
 				Props.add(prop);
 			}
 		}
 		catch (ConfigException e) {
 			System.out.println("[ERR] Config() " + e.getMessage());
 		}
 	}
 	
 	public static void Write() {
 		for (myProp p : INSTANCE.Props) {
 			if (p.isWritable()) {
 				System.out.println("[Config] Write " + p.getConfigFile());
 				p.write();
 			}
 		}
 	}
 	
 	public static String get(String param) {
 		try {
 			for (myProp p : INSTANCE.Props) {
 				if (p.contains(param)) {
 					return p.get(param);
 				}
 			}
 		} catch (ConfigException e) {
 			System.out.println("[ERR] get("+param+"):" + e.getMessage());
 		}
 		System.out.println("[WRN] get("+param+"): key not found!");
		return null;
 	}
 	public static int getI(String param) {
 		try {
 			for (myProp p : INSTANCE.Props) {
 				if (p.contains(param)) {
 					return p.getI(param);
 				}
 			}
 		} catch (ConfigException e) {
 			System.out.println("[ERR] getI("+param+"):" + e.getMessage());
 		}
 		System.out.println("[WRN] getI("+param+"): key not found!");
 		return 0;
 	}
 	public static float getF(String param) {
 		try {
 			for (myProp p : INSTANCE.Props) {
 				if (p.contains(param)) {
 					return p.getF(param);
 				}
 			}
 		} catch (ConfigException e) {
 			System.out.println("[ERR] getF("+param+"):" + e.getMessage());
 		}
 		System.out.println("[WRN] getF("+param+"): key not found!");
 		return 0;
 	}
 	public static boolean getB(String param) {
 		try {
 			for (myProp p : INSTANCE.Props) {
 				if (p.contains(param)) {
 					return p.getB(param);
 				}
 			}
 		} catch (ConfigException e) {
 			System.out.println("[ERR] getB("+param+"):" + e.getMessage());
 		}
 		System.out.println("[WRN] getB("+param+"): key not found!");
 		return false;
 	}
 	public static void set(String param, String val) {
 		try {
 			for (myProp p : INSTANCE.Props) {
 				if (p.contains(param)) {
 					p.set(param, val);
 				}
 			}
 		} catch (ConfigException e) {
 			System.out.println("[ERR] set("+param+","+val+"):" + e.getMessage());
 		}
 		System.out.println("[WRN] set("+param+"): key not found!");
 	}
 	public static URL getRes(String param) {
 		return Config.class.getResource("/res/"+param);
 	}
 }
