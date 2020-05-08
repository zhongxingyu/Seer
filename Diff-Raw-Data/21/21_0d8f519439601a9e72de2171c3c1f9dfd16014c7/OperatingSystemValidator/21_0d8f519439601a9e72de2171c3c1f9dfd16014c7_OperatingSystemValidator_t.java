 package de.fhb.projects.Twitchess.controller.osvalidator;
 
 import de.fhb.projects.Twitchess.exception.OperatingSystemNotSupportedException;
 
 public class OperatingSystemValidator {
	private OperatingSystem os;
 
 	public OperatingSystem getOperatingSystem() {
		String systemName = System.getProperty("os.name").toLowerCase();
		getOperatingSystem(systemName);
		return os;
 	}
 
 	protected void getOperatingSystem(String os) {
 		if (isWindows(os)) {
			this.os = OperatingSystem.WINDOWS;
 		} else if (isMac(os)) {
			this.os = OperatingSystem.MAC;
 		} else if (isUnix(os)) {
			this.os = OperatingSystem.UNIX;
 		} else if (isSolaris(os)) {
			this.os = OperatingSystem.SUN;
 		} else {
 			throw new OperatingSystemNotSupportedException(
 					"Your operating system is not supported.");
 		}
 	}
 
 	public boolean isWindows(String os) {
 		return (os.toLowerCase().indexOf("win") >= 0);
 	}
 
 	public boolean isMac(String os) {
 		return (os.toLowerCase().indexOf("mac") >= 0);
 	}
 
 	public boolean isUnix(String os) {
 		return (os.toLowerCase().indexOf("nix") >= 0 || os.toLowerCase()
 				.indexOf("nux") >= 0);
 	}
 
 	public boolean isSolaris(String os) {
 		return (os.toLowerCase().indexOf("sun") >= 0
 				|| os.toLowerCase().indexOf("sunos") >= 0 || os.toLowerCase()
 				.indexOf("sol") >= 0);
 	}
 }
