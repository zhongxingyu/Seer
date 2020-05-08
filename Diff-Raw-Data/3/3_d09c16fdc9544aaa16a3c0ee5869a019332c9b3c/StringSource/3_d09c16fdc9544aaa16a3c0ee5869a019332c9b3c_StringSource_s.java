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
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.StringReader;
 
 import org.analyzer.Source;
 
 class StringSource implements Source {
 	private final String source;
 	private final String description;
 
 	public StringSource(String source) {
 		this(source, null);
 	}
 
 	public StringSource(String source, String description) {
 		this.source = source;
 		this.description = description;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String getText() {
 		return source;
 	}
 
 	public Reader getReader() {
 		return new StringReader(source);
 	}
 
 	public InputStream getStream() {
 		return new ByteArrayInputStream(source.getBytes());
 	}
 
 }
