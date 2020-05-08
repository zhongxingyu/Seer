 package de.fhb.projects.chesstwitterbot.controller.osvalidator;
 
 public class OperatingSystemValidator {
 	static OperatingSystem operatingSystem;
 
 	public static OperatingSystem getOperatingSystem() {
 		String os = System.getProperty("os.name").toLowerCase();
 		if (isWindows(os)) {
 			operatingSystem = OperatingSystem.WINDOWS;
 		} else if (isMac(os)) {
 			operatingSystem = OperatingSystem.MAC;
 		} else if (isUnix(os)) {
 			operatingSystem = OperatingSystem.UNIX;
 		} else if (isSolaris(os)) {
 			operatingSystem = OperatingSystem.SUN;
 		} else {
			System.out.println("Your OS is not supported with this chessengine");
 		}
 		return operatingSystem;
 	}
 
 	public static boolean isWindows(String os) {
 		return (os.indexOf("win") >= 0);
 	}
 
 	public static boolean isMac(String os) {
 		return (os.indexOf("mac") >= 0);
 	}
 
 	public static boolean isUnix(String os) {
 		return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
 	}
 
 	public static boolean isSolaris(String os) {
 		return (os.indexOf("sunos") >= 0);
 	}
 
 	public static void main(String[] args) {
 		//TODO for testings
 		System.out.println(OperatingSystemValidator.getOperatingSystem());
 	}
 }
