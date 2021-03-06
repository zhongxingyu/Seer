 /* -*- tab-width: 4 -*-
  *
  * Electric(tm) VLSI Design System
  *
  * File: BookshelfWeights.java
  *
  * Copyright (c) 2010 Sun Microsystems and Static Free Software
  *
  * Electric(tm) is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * Electric(tm) is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Electric(tm); see the file COPYING.  If not, write to
  * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  * Boston, Mass 02111-1307, USA.
  */
 package com.sun.electric.tool.io.input.bookshelf;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.StringTokenizer;
 
import com.sun.electric.database.text.TextUtils;
import com.sun.electric.tool.Job;
 import com.sun.electric.tool.io.input.bookshelf.BookshelfNodes.BookshelfNode;
 
 /**
  * @author Felix Schmidt
  * 
  */
 public class BookshelfWeights implements BookshelfInputParser<Void> {
 
 	private String nodesFile;
 
 	public BookshelfWeights(String nodesFile) {
 		this.nodesFile = nodesFile;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.sun.electric.tool.io.input.bookshelf.BookshelfInputParser#parse()
 	 */
 	public Void parse() throws IOException {
 
 		// Job.getUserInterface().setProgressNote("Parse Weights File");
 		File file = new File(this.nodesFile);
 		FileReader freader = new FileReader(file);
 		BufferedReader rin = new BufferedReader(freader);
 
 		String line;
 		while ((line = rin.readLine()) != null) {
 			if (line.startsWith("   ")) {
 				StringTokenizer tokenizer = new StringTokenizer(line, " ");
 				int i = 0;
 				String node = null;
 				int weight = 1;
 				while (tokenizer.hasMoreElements()) {
 					if (i == 0) {
 						node = tokenizer.nextToken();
 					} else if (i == 1) {
 						weight = Integer.parseInt(tokenizer.nextToken());
 					} else {
 						tokenizer.nextToken();
 					}
 					i++;
 				}
 
 				BookshelfNode bn = BookshelfNode.findNode(node);
 
 				if (bn != null)
 					bn.setWeight(weight);
 			}
 		}
 
 		return null;
 	}
 
 }
