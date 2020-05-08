 /*
 Property List Utility - LGPL 3.0 licensed
 Copyright (C) 2012  Yørn de Jong
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3.0 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 
 File is part of the Property List project.
 Project page on http://plist.sf.net/
 */
 package net.sf.plist.defaults;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 
 /**
  * Helper class for {@link NSDefaults} to provide defaults on multiple operating systems.
  */
 abstract class OperatingSystemPath {
 	
 	/**
 	 * Class for generic (probably *NIX) operating system
 	 */
 	static class DefaultSystemPath extends OperatingSystemPath {
 		@Override
 		public File getPListPath(final Scope scope) {
 			switch(scope) {
 				case USER:
 					return new File(System.getProperty("user.home")+"/.preferences/");
 				case USER_BYHOST:
 					return new File(System.getProperty("user.home")+"/.preferences/ByHost/");
 				case SYSTEM:
 					return new File("/etc/preferences/");
 			}
 			throw new NullPointerException();
 		}
 
 		@Override
 		public boolean isLowerCasePreferred() {
 			return true;
 		}
 	}
 	/**
 	 * Class for the Mac operating system
 	 */
 	static class OSXSystemPath extends OperatingSystemPath {
 		@Override
 		public File getPListPath(final Scope scope) {
 			switch(scope) {
 				case USER:
 					return new File(System.getProperty("user.home")+"/Library/Preferences/");
 				case USER_BYHOST:
 					return new File(System.getProperty("user.home")+"/Library/Preferences/ByHost/");
 				case SYSTEM:
 					return new File("/Library/Preferences/");
 			}
 			throw new NullPointerException();
 		}
 
 		@Override
 		public String getMachineUUID() {
 			try {
 				BufferedReader reader = new BufferedReader(new InputStreamReader(
 						Runtime.getRuntime().exec("system_profiler SPHardwareDataType")
 						.getInputStream()));
 				String line = reader.readLine();
 				while(line != null) {
 					if (line.trim().startsWith("Hardware UUID: "))
 						return line.trim().substring("Hardware UUID: ".length());
 					line = reader.readLine();
 				}
 				return super.getMachineUUID();
 			} catch (Exception e) {
 				return super.getMachineUUID();
 			}
 		}
 
 		@Override
 		public boolean isLowerCasePreferred() {
 			return false;
 		}
 	}
 	/**
 	 * Class for the Linux operating system
 	 */
 	static class LinuxSystemPath extends DefaultSystemPath {
 		@Override
 		public String getMachineUUID() {
 			try {
 				BufferedReader reader = new BufferedReader(new InputStreamReader(
 						Runtime.getRuntime().exec("hal-get-property --udi /org/freedesktop/Hal/devices/computer --key system.hardware.uuid")
 						.getInputStream()));
 				return reader.readLine();
 			} catch (Exception e) {
 				return super.getMachineUUID();
 			}
 		}
 	}
 	
 	// UUID Linux: hal-get-property --udi /org/freedesktop/Hal/devices/computer --key system.hardware.uuid
 	// UUID OS X: system_profiler SPHardwareDataType | awk '/UUID/{sub(/^[ \t]+/, "")}; NR == 17 {print}'
 	// UUID Windows: reg query "HKLM\SYSTEM\ControlSet001\Control\IDConfigDB\Hardware Profiles\0001" /v HwProfileGuid
 	
 	/**
 	 * Get the instance for the current operating system
 	 */
 	public static OperatingSystemPath getInstance() {
 		if (System.getProperty("os.name").toLowerCase().contains("mac"))
 			return new OSXSystemPath();
 		if (System.getProperty("os.name").toLowerCase().contains("linux"))
 			return new LinuxSystemPath();
 		return new DefaultSystemPath();
 	}
 
 	/**
 	 * Get the Property List file for a given domain and scope
 	 * 
 	 * @param domain	the domain
 	 * @param scope	the scope
 	 * @return	the property list file
 	 */
 	public final File getPListFile(String domain, final Scope scope) {
 		if (domain == null)
 			domain = isLowerCasePreferred() ? ".globalpreferences" : ".GlobalPreferences";
 		if (scope.isByHost())
 			return new File(getPListPath(scope)+File.separator+domain+"."+getMachineUUID()+".plist");
 		return new File(getPListPath(scope)+File.separator+domain+".plist");
 	}
 	
 	public abstract boolean isLowerCasePreferred();
 
 	/**
 	 * Get the path to the directory where defaults are stored for a given scope
 	 * 
 	 * @param scope	the scope
 	 * @return	the directory
 	 */
 	public abstract File getPListPath(final Scope scope);
 
 	/**
 	 * Get the UUID of the machine running the program
 	 * 
 	 * @return	the UUID
 	 */
 	public String getMachineUUID() {
 		StringBuilder result = new StringBuilder();
 		try {
 			// Basic UUID determination using MAC address,
 			// when determining using more modern methods fail
 			// or are not available
 			for(byte b : NetworkInterface.getNetworkInterfaces().nextElement().getHardwareAddress()) {
				result.append(Integer.toString(b&0xFF, 0x10));
 			}
 			return result.toString();
 		} catch (SocketException e) {
 			// When all else fails
 			return System.getProperty("user.name")
 				+ System.getProperty("user.region")
 				+ System.getProperty("user.language")
 				+ "-"
 				+ System.getProperty("os.name")
 				+ System.getProperty("os.version")
 				+ System.getProperty("os.arch");
 		}
 	}
 
 }
