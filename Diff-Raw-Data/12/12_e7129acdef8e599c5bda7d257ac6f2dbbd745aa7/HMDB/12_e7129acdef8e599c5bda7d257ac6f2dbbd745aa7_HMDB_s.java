 /*
  * HMDB.java - HashMap Database
  *
  * Copyright (C) 2012 Matthew Khouzam
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package com.matthew.hookersandblackjack;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 
 public class HMDB {
 
 	private static final long DEFAULT_MONEY = 50L;
 	private HashMap<String, Long> prop;
 	private File dbFile;
 
 	public HMDB(String fileName) throws IOException {
 		super();
 		dbFile = new File(fileName);
		if (!dbFile.exists())
 			dbFile.createNewFile();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				dbFile));
		oos.writeObject(new HashMap<String, Long>());
		oos.close();
 	}
 
 	/**
 	 * 
 	 * @param key
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	synchronized public Long get(String key) {
 		Long ret = null;
 		FileInputStream reader;
 		try {
 			reader = new FileInputStream(dbFile);
 			ObjectInputStream ois = new ObjectInputStream(reader);
 			prop = (HashMap<String, Long>) ois.readObject();
 			if (prop == null) {
 				prop = new HashMap<String, Long>();
 				prop.put(key, 50L);
 				ObjectOutputStream oos = new ObjectOutputStream(
 						new FileOutputStream(dbFile));
 				oos.writeObject(prop);
 				oos.close();
 			}
 			if (!prop.containsKey(key)) {
 				prop.put(key, DEFAULT_MONEY);
 			}
 			ret = prop.get(key);
 
 			ois.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	/**
 	 * 
 	 * @param key
 	 * @param value
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	synchronized public Long put(String key, Long value) {
 		try {
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
 					dbFile));
 			prop = (HashMap<String, Long>) ois.readObject();
 			if (prop == null) {
 				prop = new HashMap<String, Long>();
 				prop.put(key, 50L);
 				ObjectOutputStream oos = new ObjectOutputStream(
 						new FileOutputStream(dbFile));
 				oos.writeObject(prop);
 				oos.close();
 			}
 			ois.close();
 			prop.put(key, value);
 			ObjectOutputStream oos = new ObjectOutputStream(
 					new FileOutputStream(dbFile));
 			oos.writeObject(prop);
 			oos.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return value;
 	}
 }
