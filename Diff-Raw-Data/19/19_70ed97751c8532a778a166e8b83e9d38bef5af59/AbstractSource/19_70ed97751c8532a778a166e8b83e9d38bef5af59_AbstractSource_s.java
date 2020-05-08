 /*
  * Copyright 2011 Tomas Schlosser
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.analyzer.factories;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 
 import org.analyzer.Source;
 
 abstract class AbstractSource implements Source {
 	private final String description;
 
 	public AbstractSource(String description) {
 		this.description = description;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String getText() {
 		StringBuilder result = new StringBuilder();
 		BufferedReader br = null;
 
 		try {
 			br = new BufferedReader(getReader());
 			String line;
 
 			while ((line = br.readLine()) != null) {
 				result.append(line).append("\n");
 			}
 		} catch (IOException ex) {
 			System.err.println(ex);
 			ex.printStackTrace();
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
					System.err.println(e);
					e.printStackTrace();
 				}
 			}
 		}
 
 		return result.toString();
 	}
 
 	public Reader getReader() {
 		return new InputStreamReader(getStream());
 	}
 }
