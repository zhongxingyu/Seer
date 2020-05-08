 /*
  * This file is part of MineQuest-API, version 2, Specifications for the MineQuest system.
  * MineQuest-API, version 2 is licensed under GNU Lesser General Public License v3.
  * Copyright (C) 2012 The MineQuest Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.MineQuest.API.Quest;
 
import java.io.BufferedReader;
 import java.io.File;
import java.io.FileInputStream;
 import java.io.FileNotFoundException;
import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.logging.Level;
 
import org.apache.commons.lang3.text.translate.UnicodeEscaper;

 import com.theminequest.MineQuest.API.Managers;
 
 public class QuestParser {
 	
	private static final UnicodeEscaper UNIESCAPE = new UnicodeEscaper();
	
 	/**
 	 * Indicate that this class can handle Quest file details.
 	 * @author Robert Xu <robxu9@gmail.com>
 	 *
 	 */
 	public static interface QHandler {
 		
 		/**
 		 * Parse the details from this line of the quest file.<br>
 		 * If a line contains:<br>
 		 * <code>ID:tas1,tas2,tas3:444</code><br>
 		 * Then the line is split by <code>:</code>. This line
 		 * does NOT contain detail type. (In example above, <code>ID</code>
 		 * will be omitted.)
 		 * @param q QuestDetails to manipulate
 		 * @param line Details
 		 */
 		void parseDetails(QuestDetails q, List<String> line);
 		
 	}
 	
 	private Map<String,Class<? extends QHandler>> methods = Collections.synchronizedMap(new LinkedHashMap<String,Class<? extends QHandler>>());
 	
 	/**
 	 * Register a QHandler for use in parsing Quests.
 	 * @param name Name to associate with
 	 * @param c Class that implements QHandler
 	 */
 	public void addClassHandler(String name, Class<? extends QHandler> c){
 		methods.put(name.toLowerCase(), c);
 	}
 	
 	/**
 	 * Deregisters a QHandler for use in parsing Quests.
 	 * @param name Name to remove.
 	 */
 	public void rmClassHandler(String name){
 		methods.remove(name.toLowerCase());
 	}
 	
 	public void parseDefinition(QuestDetails questDetails) throws FileNotFoundException{
		FileInputStream f = new FileInputStream((File)questDetails.getProperty(QuestDetails.QUEST_FILE));
 		Scanner filereader = null;
 		try {
			filereader = new Scanner(new BufferedReader(new InputStreamReader(f,"UTF-8")));
 			while (filereader.hasNextLine()) {
				String nextline = filereader.nextLine();
				nextline = UNIESCAPE.translate(nextline);
 				ArrayList<String> ar = new ArrayList<String>();
 				for (String s : nextline.split(":"))
 					ar.add(s);
 				String type = ar.get(0).toLowerCase();
 				Class<? extends QHandler> c = methods.get(type);
 				if (c==null)
 					continue;
 				Method m;
 				try {
 					m = c.getMethod("parseDetails", QuestDetails.class, List.class);
 				} catch (SecurityException e1) {
 					e1.printStackTrace();
 					continue;
 				} catch (NoSuchMethodException e1) {
 					e1.printStackTrace();
 					continue;
 				}
 				/*
 				 * We don't need the type in the array we pass,
 				 * so we remove it.
 				 */
 				ar.remove(0);
 				try {
 					m.invoke(c.newInstance(), questDetails, ar);
 				} catch (Exception e1){
 					e1.printStackTrace();
 					Managers.log(Level.WARNING, "[Parser] Unable to launch parser " + c.getName() +"...");
 				}
 			}
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (filereader!=null)
 				filereader.close();
 		}
 	}
 	
 }
