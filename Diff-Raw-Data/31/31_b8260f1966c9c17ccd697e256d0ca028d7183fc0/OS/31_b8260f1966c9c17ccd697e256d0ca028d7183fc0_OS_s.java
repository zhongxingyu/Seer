 package com.github.vortexellauncher;
 
 public enum OS {
 	
 	Windows {
 		public String getDataDir() {
 			return System.getenv("AppData") + "\\." + LAUNCHER_DIR;
 		}
 		public String getNatives() {
 			return "windows_natives.jar";
 		}
 		public String getMCFolderName() {
 			return ".minecraft";
 		}
 	},
 	Mac {
 		public String getDataDir() {
			return "~/Library/Application Support/"+LAUNCHER_DIR;
 		}
 		public String getNatives() {
 			return "macosx_natives.jar";
 		}
 		public String getMCFolderName() {
 			return "minecraft";
 		}
 	},
 	Linux {
 		public String getDataDir() {
 			return "~/."+LAUNCHER_DIR;
 		}
 		public String getNatives() {
 			return "linux_natives.jar";
 		}
 		public String getMCFolderName() {
 			return ".minecraft";
 		}
 	};
 
 	public static final String LAUNCHER_DIR = "modlauncher";
 	
 	private static OS currentOS = null;
 	static {
 		String name = System.getProperty("os.name").toLowerCase();
 		if (name.contains("win")) currentOS = Windows;
 		else if (name.contains("linux")) currentOS = Linux;
 		else if (name.contains("mac")) currentOS = Mac;
 		else throw new IllegalArgumentException("Unknown OS");
 	}
 	
 	public static OS getOS() {
 		return currentOS;
 	}
 	
 	public static String dataDir() {
 		return currentOS.getDataDir();
 	}
	
 	public static String nativesJar() {
 		return currentOS.getNatives();
 	}
	
 	public static String mcFolderName() {
 		return currentOS.getMCFolderName();
 	}
 	
 	public abstract String getDataDir();
 	public abstract String getNatives();
 	public abstract String getMCFolderName();
 }
