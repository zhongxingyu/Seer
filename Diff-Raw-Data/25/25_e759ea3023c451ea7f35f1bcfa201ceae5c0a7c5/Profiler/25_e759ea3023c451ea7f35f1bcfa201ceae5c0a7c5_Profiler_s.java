 /*  Profiler.java - class for managing profiles
  *  Copyright (C) 2000 Fredrik Ehnbom
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package org.gjt.fredde.yamm;
 
 import java.io.*;
 import java.util.*;
 
 import org.gjt.fredde.yamm.mail.MessageParser;
 
 /**
  * A class for managing profiles
  * @author Fredrik Ehnbom
 * @version $Id: Profiler.java,v 1.2 2000/04/01 20:50:09 fredde Exp $
  */
 public class Profiler {
 
 	/**
 	 * The properties for the profiles
 	 */
 	public Properties props = new Properties();
 
 	/**
 	 * The profiles
 	 */
 	private Profile[] profiles;
 
 	/**
 	 * Hashtable to lookup profiles
 	 */
 	private Hashtable phash = new Hashtable();
 
 	/**
 	 * Creates all the profiles found in .profiles
 	 */
 	public Profiler() {
 		InputStream in = null;
 		try {
 			in = new FileInputStream(YAMM.home + "/.profiles");
 			props.load(in);
 			in.close();    
 		} catch (IOException propsioe) {
 			propsioe.printStackTrace();
 			System.exit(1);
 		} finally {
 			try {
 				if (in != null) {
 					in.close();
 				}
 			} catch (IOException ioe) {}
 		}
 
 
 		profiles = new Profile[Integer.parseInt(props.getProperty("profile.num", "0"))];
 
 		for (int i = 0; i < profiles.length; i++) {
 			String name  = props.getProperty("profile." + i + ".name");
 			String email = props.getProperty("profile." + i + ".email");
 			String reply = props.getProperty("profile." + i + ".reply");
 			String sign  = props.getProperty("profile." + i + ".sign");
 
 			profiles[i] = new Profile(name, email, reply, sign);
 			phash.put(email, "" + i);
 		}
 	}
 
 	/**
 	 * Adds a profile
 	 */
 	public void add(String name, String email, String reply, String sign) {
 		int profs = Integer.parseInt(props.getProperty("profile.num", "0"));
 
 		props.setProperty("profile." + profs + ".name", name);
 		props.setProperty("profile." + profs + ".email", email);
 		props.setProperty("profile." + profs + ".reply", reply);
 		props.setProperty("profile." + profs + ".sign", sign);
 
 		props.setProperty("profile.num", "" + ++profs);
 
 		OutputStream out = null;
 		try {             
 			out = new FileOutputStream(YAMM.home + "/.profiles");
 			props.store(out, "YAMM profiles");
 			out.close();    
 		} catch (IOException propsioe) {
 			propsioe.printStackTrace();
 		} finally {
 			try {
 				if (out != null) {
 					out.close();
 				}
 			} catch (IOException ioe) {}
 		}
 
 	}
 
 	/**
 	 * Removes a profile
 	 */
 	public void remove(String which) {
 		int num = Integer.parseInt(which);
 		Properties p2 = new Properties();
 
 		p2.setProperty("profile.num", "" + (profiles.length - 1));
 
 		if (num + 1 == profiles.length) {
 			for (int i = 0; i < num; i++) {
 				p2.setProperty("profile." + i + ".name", props.getProperty("profile." + i + ".name"));
 				p2.setProperty("profile." + i + ".email", props.getProperty("profile." + i + ".email"));
 				p2.setProperty("profile." + i + ".reply", props.getProperty("profile." + i + ".reply"));
 				p2.setProperty("profile." + i + ".sign", props.getProperty("profile." + i + ".sign"));
 			}
 		} else {
 			for (int i = 0; i < num; i++) {
 				p2.setProperty("profile." + i + ".name", props.getProperty("profile." + i + ".name"));
 				p2.setProperty("profile." + i + ".email", props.getProperty("profile." + i + ".email"));
 				p2.setProperty("profile." + i + ".reply", props.getProperty("profile." + i + ".reply"));
 				p2.setProperty("profile." + i + ".sign", props.getProperty("profile." + i + ".sign"));
 			}
 			int j = num + 1;
 			for (int i = num; j < profiles.length; i++, j++) {
 				p2.setProperty("profile." + i + ".name", props.getProperty("profile." + j + ".name"));
 				p2.setProperty("profile." + i + ".email", props.getProperty("profile." + j + ".email"));
 				p2.setProperty("profile." + i + ".reply", props.getProperty("profile." + j + ".reply"));
 				p2.setProperty("profile." + i + ".sign", props.getProperty("profile." + j + ".sign"));
 			}
 		}
 
 		props = p2;
 		reload();
 	}
 
 	/**
 	 * Saves the configuration
 	 */
 	public void save() {
 		OutputStream out = null;
 		try {             
 			out = new FileOutputStream(YAMM.home + "/.profiles");
 			props.save(out, "YAMM profiles");
 			out.close();
 		} catch (IOException propsioe) {
 			propsioe.printStackTrace();
 		} finally {
 			try {
 				if (out != null) {
 					out.close();
 				}
 			} catch (IOException ioe) {}
 		}
 	}
 
 	/**
 	 * Reloads the configuration
 	 */
 	public void reload() {
 		save();
 		InputStream in = null;
 		try {             
 			props = new Properties();
 			in = new FileInputStream(YAMM.home + "/.profiles");
 			props.load(in);
 			in.close();
 		} catch (IOException propsioe) {
 			propsioe.printStackTrace();
 		} finally {
 			try {
 				if (in != null) {
 					in.close();
 				}
 			} catch (IOException ioe) {}
 		}
 
 		profiles = new Profile[Integer.parseInt(props.getProperty("profile.num", "0"))];
 		phash.clear();
 
 		for (int i = 0; i < profiles.length; i++) {
 			String name  = props.getProperty("profile." + i + ".name");
 			String email = props.getProperty("profile." + i + ".email");
 			String reply = props.getProperty("profile." + i + ".reply");
 			String sign  = props.getProperty("profile." + i + ".sign");
 
 			profiles[i] = new Profile(name, email, reply, sign);
 			phash.put(email, "" + i);
 		}
 	}
 
 	/**
 	 * Get the number for the default profile
 	 * @return The number of the default profile
 	 */
 	public int getDefault() {
 		return Integer.parseInt(props.getProperty("profile.default", "0"));
 	}
 
 	/**
 	 * Gets the default profile
 	 * @return The default profile
 	 */
 	public Profile getDefaultProfile() {
 		return profiles[getDefault()];
 	}
 
 	/**
 	 * Gets the profiles in String[] format
 	 */
 	public String[] getProfileList() {
 		String[] profs = new String[profiles.length];
 
 		for (int i = 0; i < profs.length; i++) {
 			profs[i] = profiles[i].name + " <" + profiles[i].email + ">";
 		}
 		return profs;
 	}
 
 	/**
 	 * Gets the profiles in Profile[] format
 	 */
 	public Profile[] getProfiles() {
 		return profiles;
 	}
 
 	/**
 	 * Returns the profile for this profile string
 	 * @param profString The profile string which we will get the profile for
 	 */
 	public Profile getProfileFor(String profString) {
		String mail = MessageParser.parseLink(profString)[1];
 
 		Object profNum = phash.get(mail);
 
 		
 		if (profNum == null) {
 			return getDefaultProfile();
 		} else {
 			return profiles[Integer.parseInt(profNum.toString())];
 		}
 	}
 
 	/**
 	 * Returns the profile for this string as a String
 	 * @param profString The string to get the profile for
 	 */
 	public String getProfileString(String profString) {
 		Profile p = getProfileFor(profString);
 
 		return p.name + " <" + p.email + ">";
 	}
 }
 /*
  * Changes:
  * $Log: Profiler.java,v $
  * Revision 1.2  2000/04/01 20:50:09  fredde
  * fixed to make the profiling system work
  *
  * Revision 1.1  2000/03/26 15:26:00  fredde
  * Files for the new profiling system
  *
  */
