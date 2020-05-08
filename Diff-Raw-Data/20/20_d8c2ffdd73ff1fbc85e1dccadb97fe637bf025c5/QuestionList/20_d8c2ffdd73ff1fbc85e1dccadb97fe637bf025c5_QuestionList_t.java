 /*
  * This file is part of DropletAsk.
  *
  * Copyright (c) 2011-2012, SpoutDev <http://www.spout.org/>
  * DropletAsk is licensed under the SpoutDev License Version 1.
  *
  * DropletAsk is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * In addition, 180 days after any changes are published, you can use the
  * software, incorporating those changes, under the terms of the MIT license,
  * as described in the SpoutDev License Version 1.
  *
  * DropletAsk is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License,
  * the MIT license and the SpoutDev License Version 1 along with this program.
  * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
  * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
  * including the MIT license.
  */
 package org.spout.droplet.ask;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.spout.api.chat.ChatArguments;
 
 public class QuestionList {
 	private final DropletAsk plugin = DropletAsk.getInstance();
 	private final File file = new File(plugin.getDataFolder(), "questions.txt");
 	private final List<ChatArguments> questions = new ArrayList<ChatArguments>();
 
 	public void load() {
 		try {
 			if (!file.exists()) {
 				file.getParentFile().mkdirs();
 				file.createNewFile();
 			}
 			BufferedReader in = new BufferedReader(new FileReader(file.getAbsoluteFile()));
 			String line;
 			while ((line = in.readLine()) != null) {
 				questions.add(ChatArguments.fromString(line));
 			}
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void save() {
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
 			for (ChatArguments question : questions) {
 				out.write(question.asString());
 				out.newLine();
 			}
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public List<ChatArguments> get() {
 		return questions;
 	}
 
 	public void add(Object... question) {
 		questions.add(new ChatArguments(question));
 	}
 
 	public void remove(int index) {
 		questions.remove(index);
 	}
 
	public boolean hasNext() {
		return !questions.isEmpty();
	}

 	public ChatArguments next() {
 		ChatArguments question = questions.get(0);
 		questions.remove(question);
 		return question;
 	}
 }
