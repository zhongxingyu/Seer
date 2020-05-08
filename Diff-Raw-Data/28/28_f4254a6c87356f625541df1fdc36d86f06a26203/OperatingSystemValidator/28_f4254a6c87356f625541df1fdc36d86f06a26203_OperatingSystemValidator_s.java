 package de.fhb.projects.Twitchess.controller.osvalidator;
 
 import de.fhb.projects.Twitchess.exception.OperatingSystemNotSupportedException;
 
 public class OperatingSystemValidator {
	static OperatingSystem operatingSystem;
 
 	public OperatingSystem getOperatingSystem() {
 		String os = System.getProperty("os.name").toLowerCase();
 		getOperatingSystem(os);
 		return operatingSystem;
 	}
 
 	protected void getOperatingSystem(String os) {
 		if (isWindows(os)) {
 			operatingSystem = OperatingSystem.WINDOWS;
 		} else if (isMac(os)) {
 			operatingSystem = OperatingSystem.MAC;
 		} else if (isUnix(os)) {
 			operatingSystem = OperatingSystem.UNIX;
 		} else if (isSolaris(os)) {
 			operatingSystem = OperatingSystem.SUN;
 		} else {
 			throw new OperatingSystemNotSupportedException(
 					"Your operating system is not supported.");
 		}
 	}
 
 	public boolean isWindows(String os) {
 		return (os.indexOf("win") >= 0);
 	}
 
 	public boolean isMac(String os) {
 		return (os.indexOf("mac") >= 0);
 	}
 
 	public boolean isUnix(String os) {
 		return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
 	}
 
 	public boolean isSolaris(String os) {
 		return (os.indexOf("sunos") >= 0);
 	}
 }
